package org.prelle.genesis.print;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.genesis.Constants;
import org.prelle.javafx.AlertType;
import org.prelle.javafx.CloseType;

import de.rpgframework.print.PDFPrintElement;
import de.rpgframework.print.PageDefinition;
import de.rpgframework.print.PrintLine;
import de.rpgframework.print.PrintManagerLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;


class PagePane extends HBox {

	private final static Logger logger = LogManager.getLogger("genesis");
	private final static ResourceBundle RES = Constants.RES;

	private Map<String, PDFPrintElement> elements;
	private PageDefinition page;
	private PrintTemplateEditorScreen provider;

	private Region background;
	private VBox content;
	private GridPane grid;
	private Label lbPage;

	private Button btnDelete;
	private Button btnBackground;
	private Button btnAdd;

	//--------------------------------------------------------------------
	public PagePane(Map<String, PDFPrintElement> elementMap, PrintTemplateEditorScreen provider) {
		elements = elementMap;
		this.provider = provider;
		initComponents();
		initLayout();
		initInteractivity();

//		update();
	}

	//--------------------------------------------------------------------
	private void initComponents() {
		grid = new GridPane();
		grid.setGridLinesVisible(true);
		grid.setHgap(PrintTemplateConstants.COLUMN_GAP);
//		grid.setVgap(PrintTemplateConstants.COLUMN_GAP);
		grid.setMouseTransparent(true);
		grid.setPrefSize(PrintTemplateConstants.PAGE_WIDTH, PrintTemplateConstants.PAGE_HEIGHT);

		background = new Region();
		background.setPrefSize(PrintTemplateConstants.PAGE_WIDTH, PrintTemplateConstants.PAGE_HEIGHT);

		content = new VBox();
		content.setPrefSize(PrintTemplateConstants.PAGE_WIDTH, PrintTemplateConstants.PAGE_HEIGHT);

		btnDelete = new Button("\uE107");
		btnDelete.setStyle("-fx-font-size: 200%; -fx-text-fill: white; -fx-font-family: 'Segoe UI Symbol';");
		btnDelete.setTooltip(new Tooltip(RES.getString("screen.printtemplate.button.delete_page")));

//		btnBackground = new Button(new String(Character.toChars(127912))); // 1f3a8
//		btnBackground = new Button("\uEB9F");
		btnBackground = new Button("\uE114");
		btnBackground.setStyle("-fx-font-size: 200%; -fx-text-fill: white; -fx-font-family: 'Segoe UI Symbol'");
//		btnBackground.setStyle("-fx-font-size: 200%; -fx-text-fill: white; -fx-font-face: 'Segoe UI Symbol'");
		btnBackground.setTooltip(new Tooltip(RES.getString("screen.printtemplate.button.background")));

		btnAdd = new Button("\uE109");  // E0C5
		btnAdd.setStyle("-fx-font-size: 200%; -fx-text-fill: white; -fx-font-family: 'Segoe UI Symbol'");
		btnAdd.setTooltip(new Tooltip(RES.getString("screen.printtemplate.button.add_page")));


	}

	//--------------------------------------------------------------------
	private void initLayout() {
		lbPage = new Label(RES.getString("label.page"));
		lbPage.setStyle("-fx-font-size: 200%; -fx-text-fill: white;");
		lbPage.setRotate(-90);
		Group grpPage = new Group(lbPage);

		VBox perPageLine = new VBox();
		perPageLine.setAlignment(Pos.TOP_CENTER);
		perPageLine.setStyle("-fx-spacing: 1em;");
		perPageLine.getChildren().addAll(btnDelete, btnBackground, btnAdd);

		StackPane lineContent = new StackPane();
		lineContent.setMaxHeight(PrintTemplateConstants.PAGE_HEIGHT);
		lineContent.getChildren().addAll(grpPage, perPageLine);
		lineContent.setStyle("-fx-background-color: #808080");
		StackPane.setAlignment(grpPage, Pos.BOTTOM_CENTER);
		StackPane.setAlignment(perPageLine, Pos.TOP_CENTER);
		StackPane.setMargin(grpPage, new Insets(0, 0, 20, 0));

		StackPane realContent = new StackPane(background, content, grid);
		realContent.setStyle("-fx-background-color: white");

		getChildren().addAll(lineContent, realContent);

		setColumnWidths();
	}

	//--------------------------------------------------------------------
	private void initInteractivity() {
		btnDelete.setOnAction(ev -> {
			CloseType result = provider.getScreenManager().showAlertAndCall(
					AlertType.CONFIRMATION,
					RES.getString("screen.printtemplate.deleteconfirm.title"),
					RES.getString("screen.printtemplate.deleteconfirm.desc")
					);
			if (result==CloseType.OK || result==CloseType.YES) {
				logger.info("User confirmed to delete page");
				provider.deletePage(page);

			}
		});

		btnAdd.setOnAction(ev -> {
			provider.addPage();
		});

		btnBackground.setOnAction(ev -> {
			logger.debug("Select background image clicked");
			FileChooser.ExtensionFilter images = new FileChooser.ExtensionFilter(
				    RES.getString("filechooser.images"), "*.png", "*.jpg");
			FileChooser chooser = new FileChooser();
			Preferences pref = 	Preferences.userRoot().node("/org/prelle/genesis");
			String initialDirName = pref.get("printtemplate_pick_bg_dir", null);
			if (initialDirName!=null) {
				Path initialDir = Paths.get(initialDirName);
				if (Files.exists(initialDir))
					chooser.setInitialDirectory(initialDir.toFile());
			}

			chooser.getExtensionFilters().add(images);
			File file = chooser.showOpenDialog(PagePane.this.getScene().getWindow());
			if (file!=null) {
				logger.info("User chose "+file+" as background");
				PrintManagerLoader.getInstance().addBackgroundImage(provider.getSystem(), file.toPath());
				provider.setBackgroundImage(file.toPath());
				// Load image and display it
				setBackgroundImage(file);
				// Memorize directory
				pref.put("printtemplate_pick_bg_dir", file.getParent());
			}
		});
	}

	//--------------------------------------------------------------------
	private void setBackgroundImage(File file) {
		background.setStyle("-fx-background-size: cover; -fx-background-image: url(\"file:///"+file.getAbsolutePath().replace("\\", "/").replace(" ", "%20")+"\")");
		logger.debug("Background style now: "+background.getStyle());
	}

	//--------------------------------------------------------------------
	public PageDefinition getPageDefinition() {
		return page;
	}

	//--------------------------------------------------------------------
	public void setData(PageDefinition page, int i) {
		logger.debug("setData("+page+")");
		this.page = page;
		lbPage.setText(RES.getString("label.page")+" "+i);
		update();
	}

	//--------------------------------------------------------------------
	private void setColumnWidths() {
		// Set column widths
		grid.getColumnConstraints().clear();
		for (int i=0; i<PrintTemplateConstants.MAX_COLUMNS; i++) {
			ColumnConstraints cons = new ColumnConstraints();
			cons.setPrefWidth(PrintTemplateConstants.COLUMN_WIDTH);
			cons.setMaxWidth(PrintTemplateConstants.COLUMN_WIDTH);
			cons.setFillWidth(true);
			grid.getColumnConstraints().add(cons);
		}
	}


	//--------------------------------------------------------------------
	void update() {
		logger.debug("START: update");
		if (provider.getTemplate().getBackgroundImage()!=null) {
			setBackgroundImage(provider.getTemplate().getBackgroundImage().toFile());
		}

		content.getChildren().clear();
		int y=0;
		int addToLine = 0;
		/*
		 * If there are lines, use this virtual first line to allow
		 * prepending a new line
		 */
		if (page.iterator().hasNext()) {
			// First line
			PrependLineComponent newFirstLine = new PrependLineComponent(elements, this);
			content.getChildren().add(newFirstLine);
			y++;
		}

		// Real content
		for (PrintLine line : page) {
			logger.debug("  START line "+y+" = "+line);
			try {
				PrintLineComponent comp = new PrintLineComponent(provider.getCharacter(), elements, this, line, addToLine, y);
				comp.refresh();
				content.getChildren().add(comp);
			} catch (Exception e) {
				logger.error("Error displaying line "+line,e);
			}
			logger.debug("  STOP  line "+y+" = "+line);
			y++;
			addToLine++;
		}
		// Last Line
		AppendLineComponent appendLast = new AppendLineComponent(elements, this, addToLine, y);
		content.getChildren().add(appendLast);

		/*
		 * Calculate the grid according to real components
		 */
		grid.getRowConstraints().clear();
		grid.getChildren().clear();
		y=0;
		for (Node node : content.getChildren()) {
			RowComponent row = (RowComponent)node;
			grid.getRowConstraints().add(row.getRowConstraints());
			logger.trace("  Pref. Height of "+row+" = "+row.getRowConstraints().getPrefHeight());
			// Cells
			for (int i=0; i<PrintTemplateConstants.MAX_COLUMNS; i++) {
				Region reg = new Region();
				reg.setMaxHeight(Double.MAX_VALUE);
				reg.setMaxWidth(Double.MAX_VALUE);
				reg.setUserData(i+","+y);
				grid.add(reg, i, y);
			}
			y++;
		}
		logger.debug("STOP : update");
	}

	//--------------------------------------------------------------------
	public int getLine(double y) {
		int line = 0;
		for (Node child : content.getChildren()) {
			RowComponent comp = (RowComponent)child;
			if (comp.getHeight()<1)
				return line;
			if (y> comp.getHeight()) {
				y -= comp.getHeight();
				line++;
			} else
				return line;
			y-=PrintTemplateConstants.COLUMN_GAP;
		}
		return line;
	}


	//--------------------------------------------------------------------
	void markGrid(int x, int y, int width, boolean possible) {
		List<String> idsToMark = new ArrayList<String>();
		for (int i=x; i<(x+width); i++)
			idsToMark.add(i+","+y);

		for (Node node : grid.getChildren()) {
			if ( idsToMark.contains( node.getUserData())) {
				if (!possible) {
					node.setStyle("-fx-background-color: rgb(255,0,0); -fx-opacity: 0.5");
				} else {
					node.setStyle("-fx-background-color: rgb(0,255,0); -fx-opacity: 0.5");
				}
			} else
				node.setStyle("-fx-background-color: rgb(255,0,0); -fx-opacity: 0");
		}
	}

	//--------------------------------------------------------------------
	void clearGrid() {
		for (Node node : grid.getChildren())
			node.setStyle("-fx-background-color: rgb(255,0,0); -fx-opacity: 0");
	}

}