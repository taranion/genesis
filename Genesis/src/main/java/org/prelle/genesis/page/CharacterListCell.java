package org.prelle.genesis.page;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.genesis.Genesis5Main;

import de.rpgframework.ResourceI18N;
import de.rpgframework.character.Attachment;
import de.rpgframework.character.CharacterHandle;
import de.rpgframework.character.CharacterHandle.Format;
import de.rpgframework.character.CharacterHandle.Type;
import de.rpgframework.character.RuleSpecificCharacterObject;
import de.rpgframework.core.BabylonEventBus;
import de.rpgframework.core.BabylonEventType;
import de.rpgframework.core.CommandBus;
import de.rpgframework.core.CommandType;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

//-------------------------------------------------------------------
//-------------------------------------------------------------------
class CharacterListCell extends StackPane {

	public static final String DELETE = "DELETE";
	public static final String EDIT   = "EDIT";
	public static final String PRINT  = "PRINT";
	public static final String ATTACH = "ATTACH";

	@SuppressWarnings("serial")
	public class CharActionEvent extends ActionEvent {
		private String command;
		private CharacterHandle handle;
		public CharActionEvent(Object src, EventTarget target, CharacterHandle handle, String cmd) {
			super(src, target);
			this.command = cmd;
			this.handle  = handle;
		}
		public String getCommand() { return command; }
		public CharacterHandle getCharacter() { return handle; }
	}

	private final static ResourceBundle RES = ResourceBundle.getBundle(CharacterListCell.class.getName());
	private final static Logger logger = LogManager.getLogger("genesis");

	private CharacterListView parent;
	private CharacterHandle handle;
	private Label lblChar;
	private FlowPane buttons;
	private Button btnCharEdit;
	private Button btnCharPrint;
	private Button btnCharAttach;
	private Button btnCharDelete;

	//-------------------------------------------------------------------
	public CharacterListCell(CharacterHandle handle, CharacterListView parent) {
		this.handle = handle;
		this.parent = parent;

		initComponents();
		initLayout();
		initInteractivity();
	}

	//-------------------------------------------------------------------
	private void initComponents() {
		lblChar = new Label(handle.getName());
		lblChar.setMaxWidth(Double.MAX_VALUE);
		lblChar.getStyleClass().add("text-body");
		// Character image
		try {
			if (handle.getFirstAttachment(Type.CHARACTER, Format.IMAGE)!=null) {
				ImageView iView = new ImageView();
				iView.setFitHeight(96);
				iView.setFitWidth(96);
				iView.setImage(new Image(new ByteArrayInputStream(handle.getFirstAttachment(Type.CHARACTER, Format.IMAGE).getData())));
				lblChar.setGraphic(iView);
			} else {
				// Default image
				ImageView iView = new ImageView();
				iView.setFitHeight(96);
				iView.setFitWidth(96);
				iView.setImage(new Image(Genesis5Main.class.getResourceAsStream("images/guest-256.png")));
				lblChar.setGraphic(iView);
			}
		} catch (Exception e) {
			logger.error( "Error loading character image for "+handle.getName(),e);
			StringWriter mess = new StringWriter();
			e.printStackTrace(new PrintWriter(mess));
			BabylonEventBus.fireEvent(BabylonEventType.UI_MESSAGE, 2, "Error loading image for "+handle.getName()+"\n\n"+mess.toString());
		}
		lblChar.setGraphicTextGap(5);

		btnCharEdit   = new Button("\uE70F"); // \E104
		btnCharDelete = new Button("\uE74D");
//		btnCharPrint  = new Button(new String(Character.toChars(128438)));
		btnCharPrint  = new Button("\uE749");
		btnCharAttach = new Button("\uE723");
		btnCharEdit.getStyleClass().add("icon");
		btnCharDelete.getStyleClass().add("icon");
		btnCharAttach.getStyleClass().add("icon");
		btnCharPrint.getStyleClass().add("icon");
		btnCharEdit  .setStyle("-fx-background-color: areacolor-action; -fx-text-fill: white; -fx-font-size: 140%; ");
		btnCharDelete.setStyle("-fx-background-color: areacolor-action; -fx-text-fill: white; -fx-font-size: 140%; ");
		btnCharPrint .setStyle("-fx-background-color: areacolor-action; -fx-text-fill: white; -fx-font-size: 140%; ");
		btnCharAttach.setStyle("-fx-background-color: areacolor-action; -fx-text-fill: white; -fx-font-size: 140%; ");

		btnCharEdit.setTooltip(new Tooltip(ResourceI18N.get(RES,"tooltip.char.edit")));
		btnCharPrint.setTooltip(new Tooltip(ResourceI18N.get(RES,"tooltip.char.print")));
		btnCharAttach.setTooltip(new Tooltip(ResourceI18N.get(RES,"tooltip.char.attach")));
		btnCharDelete.setTooltip(new Tooltip(ResourceI18N.get(RES,"tooltip.char.delete")));
		// Disable options depending on selection by default
		//		btnCharEdit.setDisable(true);
		//		btnCharDelete.setDisable(true);
		//		btnCharAttach.setDisable(true);
		//		btnCharPrint.setDisable(true);

	}

	//-------------------------------------------------------------------
	private void initLayout() {
		buttons = new FlowPane(btnCharEdit, btnCharPrint, btnCharAttach, btnCharDelete);
		buttons.setStyle("-fx-hgap: 1em");
		buttons.setAlignment(Pos.BOTTOM_RIGHT);

		getChildren().addAll(lblChar);
	}

	//-------------------------------------------------------------------
	private void initInteractivity() {
		setOnMouseClicked(event -> mouseClicked(event, handle));
		setOnMouseEntered(ev -> mouseEnteredChar(lblChar, handle));
		setOnMouseExited(ev -> mouseExitedChar(lblChar, handle));

		btnCharDelete.setOnAction( ev -> {logger.info("Del "); parent.fireAction(new CharActionEvent(this, null, handle, DELETE));});
		btnCharEdit  .setOnAction( ev -> parent.fireAction(new CharActionEvent(this, null, handle, EDIT)));
		btnCharPrint .setOnAction( ev -> parent.fireAction(new CharActionEvent(this, null, handle, PRINT)));
		btnCharAttach.setOnAction( ev -> parent.fireAction(new CharActionEvent(this, null, handle, ATTACH)));
	}


	//-------------------------------------------------------------------
	private void mouseEnteredChar(Label label, CharacterHandle handle) {
		label.getStyleClass().add("highlighted");
	}

	//-------------------------------------------------------------------
	private void mouseExitedChar(Label label, CharacterHandle handle) {
		label.getStyleClass().remove("highlighted");
	}

	//-------------------------------------------------------------------
	private void mouseClicked(MouseEvent ev, CharacterHandle handle) {
		if (ev.getClickCount()==1) {
			select();
			ev.consume();
			return;
		}
		if (ev.getClickCount()==2)
			parent.fireAction(new CharActionEvent(ev.getSource(), this, handle, EDIT));
	}

	//-------------------------------------------------------------------
	/**
	 * @return the handle
	 */
	public CharacterHandle getHandle() {
		return handle;
	}

	//-------------------------------------------------------------------
	void deselect() {
		getStyleClass().remove("selection-border");
		getChildren().remove(buttons);
		btnCharEdit.setVisible(false);
		btnCharDelete.setVisible(false);
		btnCharPrint.setVisible(false);
		btnCharAttach.setVisible(false);
	}

	//-------------------------------------------------------------------
	private void select() {
		parent.clearSelection();
		getStyleClass().add("selection-border");

		logger.debug("Calling getCharacter() on "+handle.getClass()+" with rulespecific "+handle.getCharacter());
		RuleSpecificCharacterObject ruleSpec = null;
		try {
			ruleSpec = handle.getCharacter();
		} catch (Exception e) {
			logger.error("Failed reading character",e);
			StringWriter out = new StringWriter();
			e.printStackTrace(new PrintWriter(out));
			BabylonEventBus.fireEvent(BabylonEventType.UI_MESSAGE, 2, out);
			return;
		}
		logger.debug("ruleSpec = "+ruleSpec);

		boolean willWork = CommandBus.canProcessCommand(this, CommandType.SHOW_CHARACTER_MODIFICATION_GUI,
				handle.getRuleIdentifier(),
				ruleSpec,
				handle,
				(btnCharEdit.getScene()!=null)?btnCharEdit.getScene().getWindow():null,
						parent.getScreenManager()
				);
		boolean willPrint = CommandBus.findCommandProcessors(this, CommandType.PRINT_GET_OPTIONS, handle.getRuleIdentifier(), ruleSpec, handle, parent.getScreenManager()).size()>0;

		logger.debug("Editing  "+ruleSpec+" will work = "+willWork);
		logger.debug("Printing "+ruleSpec+" will work = "+willPrint);
		Attachment externalData = handle.getFirstAttachment(Type.CHARACTER, Format.RULESPECIFIC_EXTERNAL);
		if (externalData==null)
			externalData = handle.getFirstAttachment(Type.CHARACTER, Format.RULESPECIFIC);
		boolean willWorkExternally = false;
		//		if (external.canOpen(handle)) {
		//			willWorkExternally = true;
		//		}
		logger.debug("External  "+externalData+" will work = "+willWorkExternally);


		/*
		 * Buttons
		 */
		btnCharEdit  .setVisible( (ruleSpec!=null && willWork) || willWorkExternally );
		btnCharDelete.setVisible(true);
		btnCharPrint .setVisible(ruleSpec!=null || willPrint);
		btnCharAttach.setVisible(true);

		// Add button
		getChildren().add(buttons);
	}

}