package org.prelle.genesis.print;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.javafx.AlertType;
import org.prelle.javafx.AppBarButton;
import org.prelle.javafx.CloseType;
import org.prelle.javafx.CommandBar;
import org.prelle.javafx.ManagedDialog;
import org.prelle.javafx.SymbolIcon;
import org.prelle.rpgframework.jfx.print.LayoutGridPane;

import de.rpgframework.ResourceI18N;
import de.rpgframework.character.RuleSpecificCharacterObject;
import de.rpgframework.core.RoleplayingSystem;
import de.rpgframework.print.LayoutGrid;
import de.rpgframework.print.PDFPrintElement;
import de.rpgframework.print.PrintTemplate;
import de.rpgframework.print.TemplateController;
import de.rpgframework.print.TemplateFactory;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/**
 * @author Stefan
 *
 */
public class PrintTemplateEditorScreen extends ManagedDialog {

	private final static Logger logger = LogManager.getLogger("genesis");
	private final static ResourceBundle RES = ResourceBundle.getBundle(PrintTemplateEditorScreen.class.getName());

	private List<PDFPrintElement> elements;
	private Map<String,PDFPrintElement> elementMap;
	private RuleSpecificCharacterObject charac;

	private Label noWYSIWYG;
	private CommandBar command;
	private ScrollPane scroll;
	
	private AppBarButton btnAdd;
	private AppBarButton btnDel;
	private AppBarButton btnImg;
	private Button btnPrev;
	private Label lbPage;
	private Button btnNext;
	private ListView<LayoutGridElement> lvLayouts;
	private ListView<PDFPrintElement> lvElements;
	private LayoutGridPane lgPage;

	private PrintTemplate template;
	private TemplateController provider;
	private RoleplayingSystem system;
	private Path backgroundImage;
	
	//--------------------------------------------------------------------
	public PrintTemplateEditorScreen(List<PDFPrintElement> elements, RoleplayingSystem system) {
		super(ResourceI18N.get(RES,"screen.print_template_editor.title"), null, CloseType.OK);
		this.elements = elements;
		this.system   = system;
		this.elementMap = new HashMap<>();
		for (PDFPrintElement elem : elements)
			this.elementMap.put(elem.getId(), elem);

		template = new PrintTemplate();
		
		initComponents();
		initLayout();
		initInteractivity();
		
		provider  = TemplateFactory.newTemplateController(template.get(0), elementMap);
		showPage(0);
	}


	//--------------------------------------------------------------------
	private void initComponents() {
		// Pager
		btnPrev = new Button(null, new SymbolIcon("back"));
		btnPrev.getStyleClass().add("app-bar-button");
		btnPrev.setTooltip(new Tooltip(ResourceI18N.get(RES, "button.prev")));
		lbPage  = new Label("?");
		btnNext = new Button(null,new SymbolIcon("forward"));
		btnNext.getStyleClass().add("app-bar-button");
		btnNext.setTooltip(new Tooltip(ResourceI18N.get(RES, "button.next")));
		HBox pager = new HBox(10,btnPrev,lbPage,btnNext);
		pager.setAlignment(Pos.CENTER_LEFT);
		
		
		btnAdd = new AppBarButton(ResourceI18N.get(RES, "button.add"), new SymbolIcon("add"));
		btnDel = new AppBarButton(ResourceI18N.get(RES, "button.delete"), new SymbolIcon("delete"));
		btnImg = new AppBarButton(ResourceI18N.get(RES, "button.background"), new SymbolIcon("camera"));
		command = new CommandBar();
		command.setContent(pager);
		command.getPrimaryCommands().addAll(btnAdd,btnDel, btnImg);
		noWYSIWYG = new Label(ResourceI18N.get(RES,"screen.printtemplate.nowysiwyg"));
		LayoutGrid page = TemplateFactory.createPageDefinition(6);
		template.add(page);
		TemplateController ctrl = TemplateFactory.newTemplateController(page, elementMap);
		lgPage = new LayoutGridPane(page, ctrl, 
				(int)PrintTemplateConstants.COLUMN_WIDTH, 
				PrintTemplateConstants.COLUMN_GAP, elementMap);
		
		lvLayouts = new ListView<>();
		lvLayouts.setOrientation(Orientation.HORIZONTAL);
		lvLayouts.setCellFactory(lv -> new LayoutGridElementListCell());
		for (int x=0; x<5; x++) {
			lvLayouts.getItems().add( new LayoutGridElement(x,0) );
		}

		lvElements = new ListView<>();
		lvElements.getItems().addAll(elements);
		lvElements.setCellFactory(lv -> new PDFPrintElementListCell());
	}

	//--------------------------------------------------------------------
	private void initLayout() {
		lgPage.setMaxHeight(Double.MAX_VALUE);
//		lgPage.setPrefHeight(PrintTemplateConstants.PAGE_HEIGHT);
//		lgPage.setPrefWidth(PrintTemplateConstants.PAGE_WIDTH);

		scroll = new ScrollPane(lgPage);
		scroll.setFitToHeight(true);
		scroll.setMaxHeight(Double.MAX_VALUE);
		scroll.setStyle("-fx-background-color: #d0d0d0; -fx-min-height: 600px"); 
		VBox boxPage = new VBox(command, scroll);
		VBox.setVgrow(scroll, Priority.ALWAYS);

		lvLayouts.setStyle("-fx-pref-width: 720px; -fx-pref-height: 80px;");

		lvElements.setMaxHeight(Double.MAX_VALUE);
		lvElements.setStyle("-fx-pref-width: 720px");

//		VBox rightSide = new VBox(20, lvLayouts, lvElements);
		VBox rightSide = new VBox(20, lvElements);
		VBox.setVgrow(lvElements, Priority.ALWAYS);
		
		// Side by side
//		HBox content = new HBox(20);
//		HBox.setHgrow(boxPage, Priority.SOMETIMES);
//		HBox.setHgrow(lvElements, Priority.SOMETIMES);
//		content.getChildren().addAll(boxPage, lvElements);
//		content.setMaxHeight(Double.MAX_VALUE);
		GridPane content = new GridPane();
		content.setHgap(20);
		content.add(boxPage, 0, 0);
		content.add(rightSide, 1, 0);
		ColumnConstraints cs = new ColumnConstraints();
		cs.setPercentWidth(50);
		content.getColumnConstraints().add(cs);

		VBox metaContent = new VBox(20);
//		VBox.setVgrow(content, Priority.ALWAYS);
		metaContent.getChildren().addAll(noWYSIWYG, content);
		setContent(metaContent);
	}

	//--------------------------------------------------------------------
	private void initInteractivity() {
//		sceneProperty().addListener( (ov2,o2,n2) -> {
//			getScene().heightProperty().addListener( (ov,o,n) -> {
//				logger.info("    lgPage size is "+lgPage.getWidth()+"x"+lgPage.getHeight());
//				logger.info("    scroll size is "+scroll.getWidth()+"x"+scroll.getHeight());
//				float scale = (float)(scroll.getHeight()/(PrintTemplateConstants.PAGE_HEIGHT));
//				if (scale<0.03) scale=0.5f;
//				logger.info("Scale is "+scale+"   page size is "+lgPage.getWidth()+"x"+lgPage.getHeight());
//				logger.info("    pref size is "+lgPage.getPrefWidth()+"x"+lgPage.getPrefHeight());
//
//				scroll.setContent(new Label("Hallo"));
//				lgPage.setScaleX(scale);
//				lgPage.setScaleY(scale);
//				scroll.setContent(lgPage);
//				scroll.autosize();
//				this.updateBounds();
//			});	
//			getScene().widthProperty().addListener( (ov,o,n) -> {
//				logger.info("    lgPage size is "+lgPage.getWidth()+"x"+lgPage.getHeight());
//				logger.info("    scroll size is "+scroll.getWidth()+"x"+scroll.getHeight());
//				float scale = (float)(scroll.getWidth()/(PrintTemplateConstants.PAGE_WIDTH));
//				if (scale<0.03) scale=0.5f;
//				logger.info("Scale is "+scale+"   page size is "+lgPage.getWidth()+"x"+lgPage.getHeight());
//				logger.info("    pref size is "+lgPage.getPrefWidth()+"x"+lgPage.getPrefHeight());
//				lgPage.setScaleX(scale);
//				lgPage.setScaleY(scale);
//				scroll.setContent(lgPage);
//				scroll.autosize();
//				this.updateBounds();
//			});
//		});
		
		scroll.viewportBoundsProperty().addListener( (ov,o,n) -> scrollPaneResized());
//		scroll.heightProperty().addListener( (ov,o,n) -> scrollPaneResized());

		btnDel.setOnAction(ev -> {
			CloseType result = getScreenManager().showAlertAndCall(
					AlertType.CONFIRMATION,
					ResourceI18N.get(RES,"screen.printtemplate.deleteconfirm.title"),
					ResourceI18N.get(RES,"screen.printtemplate.deleteconfirm.desc")
					);
			if (result==CloseType.OK || result==CloseType.YES) {
				logger.info("User confirmed to delete page");
				provider.deletePage(template, lgPage.getInput());
				showPage(0);

			}
		});

		btnAdd.setOnAction(ev -> {
			logger.info("CREATE clicked");
			provider.createPage(template);
			int num = template.indexOf(lgPage.getInput());
			lbPage.setText(ResourceI18N.format(RES, "label.page", num+1, template.size()));
		});

		btnImg.setOnAction(ev -> {
			logger.debug("Select background image clicked");
			FileChooser.ExtensionFilter images = new FileChooser.ExtensionFilter(
					ResourceI18N.get(RES,"filechooser.images"), "*.png", "*.jpg");
			FileChooser chooser = new FileChooser();
			Preferences pref = 	Preferences.userRoot().node("/org/prelle/genesis");
			String initialDirName = pref.get("printtemplate_pick_bg_dir", null);
			if (initialDirName!=null) {
				Path initialDir = Paths.get(initialDirName);
				if (Files.exists(initialDir))
					chooser.setInitialDirectory(initialDir.toFile());
			}

			chooser.getExtensionFilters().add(images);
			File file = chooser.showOpenDialog(PrintTemplateEditorScreen.this.getScene().getWindow());
			if (file!=null) {
				logger.info("User chose "+file+" as background");
				provider.addBackgroundImage(template, system, file.toPath());
				lgPage.setBackgroundImage(file);
				// Load image and display it
				setBackgroundImage(file.toPath());
				// Memorize directory
				pref.put("printtemplate_pick_bg_dir", file.getParent());
			}
		});
		
		btnNext.setOnAction(ev -> {
			logger.debug("NEXT clicked");
			LayoutGrid current = lgPage.getInput();
			int pos = template.indexOf(current);
			pos++;
			if ( pos<template.size()) {
				showPage(pos);
			}
		});
		btnPrev.setOnAction(ev -> {
			logger.debug("PREVIOUS clicked");
			LayoutGrid current = lgPage.getInput();
			int pos = template.indexOf(current);
			pos--;
			if ( pos>=0) {
				showPage(pos);
			}
		});
	}

	//--------------------------------------------------------------------
	private void scrollPaneResized() {
//		logger.info("Scrollpane resized to "+scroll.getWidth()+"x"+scroll.getHeight());
		logger.info("Scrollpane viewport resized to "+scroll.getViewportBounds().getWidth()+"x"+scroll.getViewportBounds().getHeight());
		float scale = (float)(scroll.getViewportBounds().getWidth()/lgPage.getWidth());
		if (scale<0.03) scale=0.5f;
		logger.info("Scale is "+scale+"   page size is "+lgPage.getWidth()+"x"+lgPage.getHeight());
//		logger.info("    pref size is "+lgPage.getPrefWidth()+"x"+lgPage.getPrefHeight());
		Group grp = new Group(lgPage);
		lgPage.setScaleX(scale);
		lgPage.setScaleY(scale);
		scroll.setContent(grp);
//		scroll.autosize();
//		scroll.setVvalue(0.5);
//		scroll.setHvalue(0.5);
		this.updateBounds();
	}


	//--------------------------------------------------------------------
	public PrintTemplate getTemplate() {
//		if (template==null) {
//			template = PrintManagerLoader.getInstance().createTemplate(lvPages.getItems());
//		}
//
//		template.setPages(lvPages.getItems());
		if (backgroundImage!=null)
			template.setBackgroundImage(backgroundImage);
		return template;
	}

	//--------------------------------------------------------------------
	public void setCharacter(RuleSpecificCharacterObject charac) {
		this.charac = charac;
		lgPage.setCharacter(charac);
	}

	//--------------------------------------------------------------------
	public void setData(RuleSpecificCharacterObject charac, PrintTemplate template) {
		if (template==null)
			throw new NullPointerException("template");
		this.template = template;
		this.charac = charac;

		logger.debug("setData");
		lgPage.setCharacter(charac);
		lgPage.setInput(template.get(0));
	}


//	//--------------------------------------------------------------------
//	public void deletePage(LayoutGrid page) {
//		logger.debug("delete page "+page);
////		lvPages.getItems().remove(page);
////		if (lvPages.getItems().isEmpty()) {
////			lvPages.getItems().add(PrintManagerLoader.getInstance().createLayoutGrid(PrintTemplateConstants.MAX_COLUMNS));
////		}
//	}
//
//
//	//--------------------------------------------------------------------
//	public void addPage() {
//		logger.debug("add page");
////		lvPages.getItems().add(PrintManagerLoader.getInstance().createLayoutGrid(PrintTemplateConstants.MAX_COLUMNS));
//	}


	//--------------------------------------------------------------------
	/**
	 * @return the system
	 */
	public RoleplayingSystem getSystem() {
		return system;
	}

	//--------------------------------------------------------------------
	public void setBackgroundImage(Path path) {
		this.backgroundImage = path;
		lgPage.setBackgroundImage(path.toFile());
	}

	//--------------------------------------------------------------------
	public RuleSpecificCharacterObject getCharacter() {
		return charac;
	}
	
	//---------------------------------------------------------
	private void showPage(int num) {
		if (num>=template.size())
			throw new ArrayIndexOutOfBoundsException("Must be between 0 and "+(template.size()-1));
		LayoutGrid page = template.get(num);
		lgPage.setInput(page);
		lbPage.setText(ResourceI18N.format(RES, "label.page", num+1, template.size()));
		if (template.getBackgroundImage()!=null) {
			lgPage.setBackgroundImage(template.getBackgroundImage().toFile());
		}
	}
	
}

