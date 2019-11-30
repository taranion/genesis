/**
 *
 */
package org.prelle.genesis.jfx.common;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.genesis.Constants;
import org.prelle.javafx.AlertType;
import org.prelle.javafx.FontIcon;
import org.prelle.javafx.ManagedScreen;
import org.prelle.javafx.ScreenManager;

import de.rpgframework.character.Attachment;
import de.rpgframework.character.CharacterHandle;
import de.rpgframework.character.CharacterHandle.Format;
import de.rpgframework.character.CharacterHandle.Type;
import de.rpgframework.character.CharacterProviderLoader;
import de.rpgframework.core.CommandBus;
import de.rpgframework.core.CommandResult;
import de.rpgframework.core.CommandType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;

/**
 * @author Stefan
 *
 */
public class CommonCharacterScreenPage extends ManagedScreen {

	private static ResourceBundle GUICOMMON = Constants.RES;

	private final static Logger logger = LogManager.getLogger("gui");

	private CharacterHandle model;

	private ListView<Attachment> lvCharacter;
	private ListView<Attachment> lvBackground;
	private ListView<Attachment> lvReports;

	private Button btnCharacter;
	private Button btnBackground;
	private Button btnReport;

	private FontIcon dndDelete;

	private File initialDir;

	//--------------------------------------------------------------------
	public CommonCharacterScreenPage() {
		initComponents();
		initLayout();
		initInteractivity();
	}

	//--------------------------------------------------------------------
	private void initComponents() {
		lvCharacter  = new ListView<>();
		lvBackground = new ListView<>();
		lvReports    = new ListView<>();

		lvCharacter.setCellFactory(new Callback<ListView<Attachment>, ListCell<Attachment>>() {
			public ListCell<Attachment> call(ListView<Attachment> param) {
				return new AttachmentCell(model, CommonCharacterScreenPage.this);
			}
		});
		lvBackground.setCellFactory(new Callback<ListView<Attachment>, ListCell<Attachment>>() {
			public ListCell<Attachment> call(ListView<Attachment> param) {
				return new AttachmentCell(model, CommonCharacterScreenPage.this);
			}
		});
		lvReports.setCellFactory(new Callback<ListView<Attachment>, ListCell<Attachment>>() {
			public ListCell<Attachment> call(ListView<Attachment> param) {
				return new AttachmentCell(model, CommonCharacterScreenPage.this);
			}
		});

		btnCharacter  = new Button(null, new FontIcon("\uE0C5"));
		btnBackground = new Button(null, new FontIcon("\uE0C5"));
		btnReport     = new Button(null, new FontIcon("\uE0C5"));
		btnCharacter.setStyle("-fx-font-size: 150%");
		btnBackground.setStyle("-fx-font-size: 150%");
		btnReport.setStyle("-fx-font-size: 150%");

		dndDelete     = new FontIcon("\uE107");
		dndDelete.setStyle("-fx-font-size: 600%");
	}

	//--------------------------------------------------------------------
	private void initLayout() {
		FontIcon icon = new FontIcon("\uE13D");
		icon.setStyle("-fx-font-size: 600%");

		Label description = new Label(GUICOMMON.getString("commoncharacterscreen.descr"));
		description.setWrapText(true);
		description.setMaxHeight(Double.MAX_VALUE);
		description.setAlignment(Pos.TOP_LEFT);

		VBox descrPane = new VBox();
		VBox.setVgrow(description, Priority.ALWAYS);
		descrPane.getStyleClass().add("description-pane");
		descrPane.getChildren().addAll(icon, description, dndDelete);
		descrPane.setAlignment(Pos.TOP_CENTER);
		descrPane.setStyle("-fx-min-width: 20em; -fx-pref-width: 20em; -fx-padding: 1em 3em 1em 1em; -fx-spacing: 1em;");

		/*
		 *  Character
		 */
		Label headCharacter = new Label(GUICOMMON.getString("commoncharacterscreen.col.character"));
		headCharacter.getStyleClass().add("section-head");
		headCharacter.setMaxWidth(Double.MAX_VALUE);
		HBox lineCharacter = new HBox(headCharacter, btnCharacter);
		HBox.setHgrow(headCharacter, Priority.ALWAYS);

		ImageView bgCharacter = new ImageView(new Image(getClass().getResourceAsStream("images/Icon_Character.png")));
		bgCharacter.setFitHeight(96);
		bgCharacter.setFitWidth(96);
		bgCharacter.setStyle("-fx-opacity: 0.5");

		StackPane stackCharacter = new StackPane();
		stackCharacter.setAlignment(Pos.BOTTOM_CENTER);
		stackCharacter.getChildren().addAll(bgCharacter, lvCharacter);
		stackCharacter.getStyleClass().add("content");

		VBox bxCharacter = new VBox(20);
		bxCharacter.setStyle("-fx-min-width: 20em");
		bxCharacter.setMaxHeight(Double.MAX_VALUE);
		bxCharacter.getChildren().addAll(lineCharacter, stackCharacter);
		VBox.setVgrow(stackCharacter, Priority.ALWAYS);

		/*
		 * Background
		 */
		Label headBackground= new Label(GUICOMMON.getString("commoncharacterscreen.col.background"));
		headBackground.getStyleClass().add("section-head");
		headBackground.setMaxWidth(Double.MAX_VALUE);
		HBox lineBackground = new HBox(headBackground, btnBackground);
		HBox.setHgrow(headBackground, Priority.ALWAYS);

		ImageView bgBackground= new ImageView(new Image(getClass().getResourceAsStream("images/Icon_Background.png")));
		bgBackground.setFitHeight(96);
		bgBackground.setFitWidth(96);
		bgBackground.setStyle("-fx-opacity: 0.5");

		StackPane stackBackground = new StackPane();
		stackBackground.setAlignment(Pos.BOTTOM_CENTER);
		stackBackground.getChildren().addAll(bgBackground, lvBackground);
		stackBackground.getStyleClass().add("content");

		VBox bxBackground= new VBox(20);
		bxBackground.setStyle("-fx-min-width: 20em");
		bxBackground.setMaxHeight(Double.MAX_VALUE);
		bxBackground.getChildren().addAll(lineBackground, stackBackground);
		VBox.setVgrow(stackBackground, Priority.ALWAYS);

		/*
		 * Reports
		 */
		Label headReport    = new Label(GUICOMMON.getString("commoncharacterscreen.col.report"));
		headReport.getStyleClass().add("section-head");
		headReport.setMaxWidth(Double.MAX_VALUE);
		HBox lineReport = new HBox(headReport, btnReport);
		HBox.setHgrow(headReport, Priority.ALWAYS);

		ImageView bgReports   = new ImageView(new Image(getClass().getResourceAsStream("images/Icon_Reports.png")));
		bgReports.setFitHeight(96);
		bgReports.setFitWidth(96);
		bgReports.setStyle("-fx-opacity: 0.5");

		StackPane stackReport = new StackPane();
		stackReport.setAlignment(Pos.BOTTOM_CENTER);
		stackReport.getChildren().addAll(bgReports, lvReports);
		stackReport.getStyleClass().add("content");

		VBox bxReport    = new VBox(20);
		bxReport.setStyle("-fx-min-width: 20em;");
		bxReport.setMaxHeight(Double.MAX_VALUE);
		bxReport.getChildren().addAll(lineReport, stackReport);
		VBox.setVgrow(stackReport, Priority.ALWAYS);

		/*
		 * Combine
		 */
		HBox tiles = new HBox();
		tiles.setSpacing(20);
		tiles.getChildren().addAll(bxCharacter, bxBackground, bxReport);
		ScrollPane scroll = new ScrollPane(tiles);
		scroll.setFitToHeight(true);

		HBox content = new HBox(40);
		content.getChildren().addAll(descrPane, scroll);
		setContent(content);
		HBox.setMargin(tiles, new Insets(0,0,20,0));
	}

	//--------------------------------------------------------------------
	private void initInteractivity() {
		btnCharacter.setOnAction(event -> addTo(Type.CHARACTER));
		btnBackground.setOnAction(event -> addTo(Type.BACKGROUND));
		btnReport.setOnAction(event -> addTo(Type.REPORT));

		dndDelete.setOnDragDropped(event -> dragDropped(event));
		dndDelete.setOnDragOver(event -> dragOver(event));
	}

	//--------------------------------------------------------------------
	public void setData(CharacterHandle handle) {
		this.model = handle;
		setHeader(handle.getName());

		lvCharacter.getItems().clear();
		lvBackground.getItems().clear();
		lvReports.getItems().clear();

		lvCharacter.getItems().addAll(handle.getAttachments(Type.CHARACTER));
		lvBackground.getItems().addAll(handle.getAttachments(Type.BACKGROUND));
		lvReports.getItems().addAll(handle.getAttachments(Type.REPORT));

		Collections.sort(lvCharacter.getItems(), new Comparator<Attachment>() {
			public int compare(Attachment o1, Attachment o2) {
				// TODO Auto-generated method stub
				return ((Integer)o1.getFormat().ordinal()).compareTo(o2.getFormat().ordinal());
			}
		});
	}

	//--------------------------------------------------------------------
	public void addTo(Type type) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(GUICOMMON.getString("commoncharacterscreen.filechooser.open."+type.name().toLowerCase()));
		if (initialDir!=null)
			fileChooser.setInitialDirectory(initialDir);
		switch (type) {
		case BACKGROUND:
			fileChooser.getExtensionFilters().add(new ExtensionFilter(GUICOMMON.getString("commoncharacterscreen.filechooser.filter.documents"), Arrays.asList("*.pdf","*.txt","*.html","*.htm")));
			break;
		default:
		}
		File result = fileChooser.showOpenDialog(getManager().getScene().getWindow());

		if (result!=null) {
			logger.debug("Shall add file "+result);
			initialDir = result.getParentFile();
			String fname = result.getName();
			String suffix = "";
			if (fname.lastIndexOf('.')>0)
				suffix = fname.substring(fname.lastIndexOf('.')+1);
			logger.debug("Suffix is "+suffix);
			CharacterHandle.Format format = Format.RULESPECIFIC;
			if (suffix.equalsIgnoreCase("png") || suffix.equalsIgnoreCase("jpg") || suffix.equalsIgnoreCase("gif") || suffix.equalsIgnoreCase("png")) {
				format = Format.IMAGE;
			} else if (suffix.equalsIgnoreCase("txt")) {
				format = Format.TEXT;
			} else if (suffix.equalsIgnoreCase("pdf")) {
				format = Format.PDF;
			} else if (suffix.equalsIgnoreCase("htm") || suffix.equalsIgnoreCase("html")) {
				format = Format.HTML;
			}
			logger.debug("File format is "+format);

			try {
				byte[] data = Files.readAllBytes(result.toPath());
				Attachment attach = CharacterProviderLoader.getCharacterProvider().addAttachment(model, type, format, fname, data);
				attach.setFilename(fname);
				switch (type) {
				case CHARACTER :	lvCharacter.getItems().add(attach); break;
				case BACKGROUND:	lvBackground.getItems().add(attach); break;
				case REPORT    :	lvReports.getItems().add(attach); break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	//-------------------------------------------------------------------
	void deleteAttachment(Attachment attach) {
		logger.info("delete attachment "+attach);
		try {
			CharacterProviderLoader.getCharacterProvider().removeAttachment(model, attach);
			switch (attach.getType()) {
			case BACKGROUND: lvBackground.getItems().remove(attach); break;
			case CHARACTER : lvCharacter.getItems().remove(attach); break;
			case REPORT    : lvReports.getItems().remove(attach); break;
			}
		} catch (IOException e) {
			logger.error("Failed deleting attachment",e);
		}
	}

	//-------------------------------------------------------------------
	private void dragDropped(DragEvent event) {
       /* if there is a string data on dragboard, read it and use it */
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasString()) {
            String filename = db.getString();
        	logger.debug("Dropped "+filename);
        	for (Attachment attach : model.getAttachments()) {
        		if (attach.getFilename().equals(filename)) {
        			deleteAttachment(attach);
        			return;
        		}
        	}
			logger.warn("Did not find attachment to delete by filename '"+filename+"'");
        }
        /* let the source know whether the string was successfully
         * transferred and used */
        event.setDropCompleted(success);

        event.consume();
	}

	//-------------------------------------------------------------------
	private void dragOver(DragEvent event) {
		Node target = (Node) event.getSource();
		if (event.getGestureSource() != target && event.getDragboard().hasString()) {
            /* allow for both copying and moving, whatever user chooses */
            event.acceptTransferModes(TransferMode.MOVE);
        }
	}

}

class AttachmentCell extends ListCell<Attachment> {

	private final static Logger logger = LogManager.getLogger("gui");
	private static ResourceBundle GUICOMMON = ResourceBundle.getBundle("org/prelle/rpgframework/jfx/common/i18n/guicommon");

	private static Image ICON_PDF;
	private static Image ICON_HTML;
	private static Image ICON_DICE;
	private static Image ICON_TXT;

	//--------------------------------------------------------------------
	static {
		ICON_PDF  = new Image(AttachmentCell.class.getResourceAsStream("images/Icon_PDF.png"));
		ICON_HTML = new Image(AttachmentCell.class.getResourceAsStream("images/Icon_HTML.png"));
		ICON_DICE = new Image(AttachmentCell.class.getResourceAsStream("images/Icon_Dice.png"));
		ICON_TXT  = new Image(AttachmentCell.class.getResourceAsStream("images/Icon_TXT.png"));
	}

	private Attachment data;
	private CharacterHandle handle;
	private ScreenManager manager;

	private ContextMenu context;
	private MenuItem menuOpen;
	private MenuItem menuDelete;

	//--------------------------------------------------------------------
	public AttachmentCell(CharacterHandle handle, CommonCharacterScreenPage parent) {
		this.handle = handle;
		this.manager = parent.getScreenManager();

		setContentDisplay(ContentDisplay.TOP);
		setAlignment(Pos.CENTER);
//		setStyle("-fx-background-color: transparent");
		setOnDragDetected(event -> dragStarted(event));

		/*
		 * Context menu
		 */
		menuOpen = new MenuItem(GUICOMMON.getString("commoncharacterscreen.open"));
		menuOpen.setOnAction(event -> open());
		menuDelete = new MenuItem(GUICOMMON.getString("commoncharacterscreen.delete"));
		menuDelete.setOnAction(event -> parent.deleteAttachment(data));
		context = new ContextMenu();
		context.getItems().addAll(menuOpen,menuDelete);
		setContextMenu(context);

		if (Desktop.isDesktopSupported()) {
			setOnMouseClicked(event -> {
				leftClicked(event);
			});

		}
	}

	//--------------------------------------------------------------------
	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	public void updateItem(Attachment item, boolean empty) {
		super.updateItem(item, empty);
		this.data = item;

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			ImageView iView = null;
			setText(item.getFilename());
			switch (item.getFormat()) {
			case IMAGE:
				Image img = new Image(new ByteArrayInputStream(item.getData()));
				iView = new ImageView(img);
				iView.setFitHeight(96);
				iView.setFitWidth(96);
				setGraphic(iView);
				break;
			case HTML:
				iView = new ImageView(ICON_HTML);
				iView.setFitHeight(96);
				iView.setFitWidth(96);
				setGraphic(iView);
				break;
			case PDF:
				iView = new ImageView(ICON_PDF);
				iView.setFitHeight(96);
				iView.setFitWidth(96);
				setGraphic(iView);
				break;
			case TEXT:
				iView = new ImageView(ICON_TXT);
				iView.setFitHeight(96);
				iView.setFitWidth(96);
				setGraphic(iView);
				break;
			case RULESPECIFIC:
				iView = new ImageView(ICON_DICE);
				iView.setFitHeight(96);
				iView.setFitWidth(96);
				setGraphic(iView);
				boolean willWork = CommandBus.canProcessCommand(this, CommandType.SHOW_CHARACTER_MODIFICATION_GUI,
						handle.getRuleIdentifier(),
						handle.getCharacter(),
						handle,
						(this.getScene()!=null)?this.getScene().getWindow():null,
						manager
						);
				menuOpen.setDisable(!willWork);
				setStyle("-fx-opacity: "+(willWork?"1.0":"0.5"));
				break;
			default:
			}
		}
	}

	//-------------------------------------------------------------------
	private void dragStarted(MouseEvent event) {
		if (data==null)
			return;

		Node source = (Node) event.getSource();

		/* drag was detected, start a drag-and-drop gesture*/
        /* allow any transfer mode */
        Dragboard db = source.startDragAndDrop(TransferMode.ANY);

        /* Put a string on a dragboard */
        ClipboardContent content = new ClipboardContent();
        content.putString(data.getFilename());
        db.setContent(content);

        /* Drag image */
        WritableImage snapshot = source.snapshot(new SnapshotParameters(), null);
        db.setDragView(snapshot);

        event.consume();
    }

	//-------------------------------------------------------------------
	private void leftClicked(MouseEvent event) {
		if (event.getClickCount()!=2)
			return;
		if (data==null)
			return;
		open();
	}

	//-------------------------------------------------------------------
	private void open() {
		try {
			/*
			 * React depending on format
			 */
			switch (data.getFormat()) {
			case RULESPECIFIC:
				boolean willWork = CommandBus.canProcessCommand(this, CommandType.SHOW_CHARACTER_MODIFICATION_GUI,
						handle.getRuleIdentifier(),
						handle.getCharacter(),
						handle,
						(this.getScene()!=null)?this.getScene().getWindow():null,
						manager
						);
				if (!willWork)
					break;
				CommandResult result = CommandBus.fireCommand(this, CommandType.SHOW_CHARACTER_MODIFICATION_GUI,
						handle.getRuleIdentifier(),
						handle.getCharacter(),
						handle,
						this.getScene().getWindow(),
						manager
						);
				if (!result.wasProcessed()) {
					manager.showAlertAndCall(
							AlertType.ERROR,
							"Das hätte nicht passieren dürfen",
							"Es hat sich kein Plugin gefunden, welches das Editieren von Charakteren dieses Systems erlaubt."
							);
				}
				if (!result.wasSuccessful()) {
					manager.showAlertAndCall(
							AlertType.ERROR,
							"Plugin Error",
							result.getMessage()
							);
				}
				break;
//			case IMAGE:
//				break;
			default:
				Path tmp = Files.createTempFile("rpgframework-",data.getFilename());
				Files.write(tmp, data.getData());

				Thread thread = new Thread(new Runnable(){
					public void run() {
						try {
							Desktop.getDesktop().open(tmp.toFile());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}});
				thread.setDaemon(true);
				thread.start();
			}
		} catch (Exception e) {
			logger.error("Failed opening file",e);
		}

	}

}