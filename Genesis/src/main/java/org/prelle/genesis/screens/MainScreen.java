package org.prelle.genesis.screens;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.genesis.ExternalTools;
import org.prelle.genesis.page.CharactersOverviewPage;
import org.prelle.javafx.AlertType;
import org.prelle.javafx.CloseType;
import org.prelle.javafx.FontIcon;
import org.prelle.javafx.ManagedScreen;
import org.prelle.javafx.NavigButtonControl;
import org.prelle.javafx.SymbolIcon;

import de.rpgframework.ConfigContainer;
import de.rpgframework.ConfigOption;
import de.rpgframework.RPGFramework;
import de.rpgframework.RPGFrameworkLoader;
import de.rpgframework.character.CharacterProvider;
import de.rpgframework.character.CharacterProviderLoader;
import de.rpgframework.character.RulePlugin;
import de.rpgframework.character.RulePluginFeatures;
import de.rpgframework.core.BabylonEvent;
import de.rpgframework.core.BabylonEventBus;
import de.rpgframework.core.BabylonEventListener;
import de.rpgframework.core.BabylonEventType;
import de.rpgframework.core.CommandBus;
import de.rpgframework.core.CommandResult;
import de.rpgframework.core.CommandType;
import de.rpgframework.core.RoleplayingSystem;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.util.StringConverter;

/**
 * @author prelle
 *
 */
public class MainScreen extends ManagedScreen implements BabylonEventListener {

	private final static int RECOMMENDED_WIDTH =1680;
	private final static int RECOMMENDED_HEIGHT=1050;

	private final static Logger logger = LogManager.getLogger("genesis");
	private final static ResourceBundle RES = ResourceBundle.getBundle(MainScreen.class.getName());

	private RPGFramework rpgFramework;
	private CharacterProvider charProv;

	private CharactersOverviewPage pgCharacters;
	private SettingsScreen scSettings;

	private MenuItem navCharacters;
	private MenuItem menuCharAdd;
	private MenuItem btnData;

	private ExternalTools external;

	//-------------------------------------------------------------------
	/**
	 */
	public MainScreen(ConfigContainer cfgGenesis) {
		BabylonEventBus.add(this);
		rpgFramework = RPGFrameworkLoader.getInstance();
		charProv     = CharacterProviderLoader.getCharacterProvider();
		logger.debug("<init>");
		external = new ExternalTools();

		logger.debug("<init> initComponents");
		initComponents(cfgGenesis);
		logger.debug("<init> initLayout");
		initLayout();
		logger.debug("<init> initInteractivity");
		initInteractivity();

		external = new ExternalTools();
		external.prepareConfigNodes(cfgGenesis);
		logger.debug("<init>  done");
	}

	//-------------------------------------------------------------------
	private void initComponents(ConfigContainer cfgGenesis) {
		pgCharacters = new CharactersOverviewPage(cfgGenesis);
		scSettings   = new SettingsScreen(external);

		/*
		 * Action area
		 */
		navCharacters = new MenuItem(RES.getString("navitem.characters"), new SymbolIcon("AddFriend"));
//		menuCharAdd   = new MenuItem(RES.getString("tooltip.char.add"), new SymbolIcon("add"));
		btnData       = new MenuItem(RES.getString("navitem.data"), new FontIcon("\uE765"));

//		menuCharAdd = new MenuItem(RES.getString("menuitem.add"),  new Label("\uE8B8"));
		getNavigationItems().addAll(navCharacters);

		updateDataInputContextMenu();
	}

	//-------------------------------------------------------------------
	private void initLayout() {
		setLandingPage(pgCharacters);
	}

	//-------------------------------------------------------------------
	private void initInteractivity() {
//		menuCharAdd.setOnAction(event -> addClicked(event));
		navCharacters.setOnAction(event -> createClicked(event));
		btnData.setOnAction(event -> dataClicked(event));
	}

	//-------------------------------------------------------------------
	public void navigationItemChanged(MenuItem oldValue, MenuItem newValue) {
		logger.debug("Navig to "+newValue);
		if (newValue==null)
			return;
		if (newValue==navCharacters) {
			showPage(pgCharacters);
		} else if ("settings".equals(newValue.getId())) {
			getManager().navigateTo(scSettings);
		}
	}

	//-------------------------------------------------------------------
	public void refresh() {
		logger.info("refresh()-----------------------------------------------");
		pgCharacters.refresh();
		scSettings.refresh();
	}

	//-------------------------------------------------------------------
	private void createClicked(ActionEvent ev) {
		logger.info("createClicked");

		Label message = new Label(RES.getString("newchardialog.content"));
		message.setWrapText(true);

		FlowPane buttons = new FlowPane();
		buttons.setStyle("-fx-hgap: 3em; -fx-vgap: 3em");

		ToggleGroup cbRules = new ToggleGroup();
		for (RulePlugin<?> plugin : CharacterProviderLoader.getRulePlugins()) {
			logger.trace("Plugin "+plugin+" has "+plugin.getSupportedFeatures());
			if (plugin.getSupportedFeatures().contains(RulePluginFeatures.CHARACTER_CREATION)) {
				logger.debug("Offer "+plugin.getRules());
				ToggleButton item = new ToggleButton(plugin.getRules().getName());
				cbRules.getToggles().add(item);
				item.setUserData(plugin.getRules());
//				item.setOnAction(event -> addCharacter(plugin.getRules()));
				buttons.getChildren().add(item);
			}
		}
		
		/*
		 * GEN-363 If there is only one system offered, automatically pick it
		 */
		if (buttons.getChildren().size()==1) {
			logger.debug("Auto-pick RPG "+buttons.getChildren().get(0).getUserData() );
			addCharacter( (RoleplayingSystem) buttons.getChildren().get(0).getUserData() );
			return;
		}
		

		VBox layout = new VBox(20,message, buttons);


		NavigButtonControl control = new NavigButtonControl();
		cbRules.selectedToggleProperty().addListener( (ov,o,n) -> {
			control.setDisabled(CloseType.OK, n==null);
		});

		CloseType result = getManager().showAlertAndCall(AlertType.QUESTION, RES.getString("newchardialog.title"), layout, control);
		if (result==CloseType.OK) {
			RoleplayingSystem rules = (cbRules.getSelectedToggle()!=null)?((RoleplayingSystem)cbRules.getSelectedToggle().getUserData()):null;
			if (rules==null) {
				logger.warn("User made no selection what RPG to create character for");
				BabylonEventBus.fireEvent(BabylonEventType.UI_MESSAGE, 2, RES.getString("newchardialog.error.no_system_selected"));
				return;
			}
			logger.info("User requests to create a character for "+rules);
			addCharacter(rules);
		}
	};

	//-------------------------------------------------------------------
	private void addClicked(ActionEvent event) {
		logger.info("addClicked");
		Label lblDesc = new Label(RES.getString("dialog.addchar.desc"));
		Label lblName = new Label(RES.getString("label.name"));
		Label lblRules= new Label(RES.getString("label.rules"));
		TextField tfName = new TextField();
		tfName.setPrefColumnCount(40);
		ChoiceBox<RoleplayingSystem> cbRules = new ChoiceBox<>();
		cbRules.setConverter(new StringConverter<RoleplayingSystem>() {
			public String toString(RoleplayingSystem object) {return object.getName();}
			public RoleplayingSystem fromString(String string) {return null;}
		});

		/*
		 * Build a list of possible roleplaying systems
		 * Allow only those that are not supported for creation
		 */
		List<RoleplayingSystem> rules = new ArrayList<>(Arrays.asList(RoleplayingSystem.values()));
		for (RulePlugin<?> plugin : CharacterProviderLoader.getRulePlugins()) {
			if (plugin.getSupportedFeatures().contains(RulePluginFeatures.CHARACTER_CREATION)) {
				rules.remove(plugin.getRules());
			}
		}
		cbRules.getItems().addAll(rules);
		Collections.sort(cbRules.getItems(), new Comparator<RoleplayingSystem>() {
			public int compare(RoleplayingSystem arg0, RoleplayingSystem arg1) {
				return Collator.getInstance().compare(arg0.getName(), arg1.getName());
			}
		});
		cbRules.getSelectionModel().select(0);


		// Layout
		GridPane grid = new GridPane();
		grid.add(lblDesc , 0, 0, 2,1);
		grid.add(lblName , 0, 1);
		grid.add( tfName , 1, 1);
		grid.add(lblRules, 0, 2);
		grid.add( cbRules, 1, 2);
		grid.setVgap(20);
		grid.setHgap(20);

//		NavigButtonControl control = new NavigButtonControl();
//		tfName.textProperty().addListener( (ov,o,n) -> {
//			control.setDisabled(CloseType.OK, n==null || n.isEmpty());
//		});

		CloseType closed = getManager().showAlertAndCall(
				AlertType.QUESTION,
				RES.getString("dialog.addchar.title"),
				grid);
		if (closed==CloseType.OK) {
			String charName = tfName.getText();
			RoleplayingSystem ruleSystem = cbRules.getValue();
			try {
				charProv.createCharacter(charName, ruleSystem);
			} catch (IOException e) {
				logger.error("Failed creating character",e);
			}
		}
	};

	//-------------------------------------------------------------------
	private void addCharacter(RoleplayingSystem rules) {
		logger.info("create "+rules+" character");
		//		ctxMenuAdd.hide();
		CommandResult result = CommandBus.fireCommand(this, CommandType.SHOW_CHARACTER_CREATION_GUI,
				rules,
				getScene()!=null?(this.getScene().getWindow()):null,
				getManager()
				);
		if (!result.wasProcessed()) {
			getManager().showAlertAndCall(
					AlertType.ERROR,
					RES.getString("error.no_creation.plugin.title"),
					RES.getString("error.no_creation.plugin.message"));
			return;
		}
		if (!result.wasSuccessful()) {
			getManager().showAlertAndCall(
					AlertType.ERROR,
					RES.getString("error.creating_character.title"),
					result.getMessage());
			return;
		}

		// Result can be accessed when childClosed is called
	}

	//-------------------------------------------------------------------
	private void settingsClicked() {
		logger.debug("settings clicked");

		SettingsScreen dialog = new SettingsScreen(external);
		getManager().navigateTo(dialog);
	}

	//-------------------------------------------------------------------
	public void childClosed(ManagedScreen child, CloseType type) {
		logger.debug("childClosed("+child+", "+type+") ");

//		if (type==CloseType.CANCEL) {
//			logger.info("Editing cancelled - reloading character");
//			try {
//				if (edited!=null) {
//					edited.setCharacter(null);
//					edited.getCharacter();
//					edited = null;
//				}
//			} catch (IOException e) {
//				logger.error("Failed reloading character",e);
//				manager.showAlertAndCall(AlertType.ERROR, "", RES.getString("error.reloading.char"));
//			}
//		} else if (type==CloseType.APPLY) {
//			logger.debug("Applying");
//			if (edited!=null && edited.getCharacter()==null) {
//				edited.getCharacter();
//				edited = null;
//			}
//		}
	}

	//--------------------------------------------------------------------
	/**
	 * @see org.prelle.rpgframework.api.BabylonEventListener#handleAppEvent(org.prelle.rpgframework.api.BabylonEvent)
	 */
	@Override
	public void handleAppEvent(BabylonEvent event) {
		switch (event.getType()) {
		case UI_MESSAGE:
			int type_i = (Integer)event.getData()[0];
			String mess = (String)event.getData()[1];
			AlertType type = AlertType.ERROR;
			switch (type_i) {
			case 0: // INFO
				type = AlertType.CONFIRMATION;
				break;
			case 1: // WARN
			case 2: // ERROR
				type = AlertType.ERROR;
				break;
			}
			String todo = String.format(
					RES.getString("label.consultlogfile"),
					System.getProperty("logdir")
					);
			if (getManager()!=null)
				getManager().showAlertAndCall(type, RES.getString("label.internalError"), mess+"\n\n"+todo);
			else {
				Platform.runLater(new Runnable() {
					public void run() {
						javafx.scene.control.Alert.AlertType type2 = javafx.scene.control.Alert.AlertType.ERROR;
						switch (type_i) {
						case 0: // INFO
							type2 = javafx.scene.control.Alert.AlertType.INFORMATION;
							break;
						case 1: // WARN
						case 2: // ERROR
							type2 = javafx.scene.control.Alert.AlertType.ERROR;
							break;
						}
						Alert a = new Alert(type2);
						a.setTitle(RES.getString("label.internalError"));
						a.setHeaderText(RES.getString("label.internalError"));
						a.setResizable(true);
						a.getDialogPane().setPrefSize(600, 400);
						a.setContentText(mess+"\n\n"+todo);
						a.showAndWait();
					}
				});
			}
			break;
		case CHAR_ADDED:
		case CHAR_MODIFIED:
		case CHAR_REMOVED:
		case CHAR_RENAMED:
			logger.debug("RCV "+event);
			pgCharacters.refresh();
			break;
		case CONFIG_OPTION_CHANGED:
			ConfigOption<?> opt = (ConfigOption<?>) event.getData()[0];
			logger.info("OPTION CHANGED: "+opt.getPathID()+" // "+opt.getLocalId());
			if (opt.getLocalId().equals("developer_mode"))
				updateDataInputContextMenu();
			break;
//		case PRINT_REQUESTED:
//			logger.debug("RCV "+event);
//			CharacterHandle handle = (CharacterHandle)event.getData()[0];
//			PrintHelper.printClicked(handle, getScreenManager());
//			break;
		default:
			logger.warn("Ignore event "+event);
		}

	}

	//-------------------------------------------------------------------
	private void checkResolution() {
		int bestWidth = 0;
		int bestHeight = 0;
		for (Screen screen : Screen.getScreens()) {
			int width = (int) screen.getBounds().getWidth();
			int height= (int) screen.getBounds().getHeight();
			logger.info("Found screen width resolution "+width+"x"+height);
			if (width>bestWidth && height>bestHeight) {
				bestWidth = width;
				bestHeight= height;
			}
		}
		if (bestWidth<RECOMMENDED_WIDTH || bestHeight<RECOMMENDED_HEIGHT) {
			if (getScene().getWindow().getRenderScaleX()>1.0) {
				String mess = String.format(RES.getString("warning.scaling.message"), bestWidth, bestHeight);
				getManager().showAlertAndCall(AlertType.ERROR, RES.getString("warning.scaling.title"), mess);
			} else {
				String winScale = "Render-Scale = "+getScene().getWindow().getRenderScaleX()+"  Output scale="+getScene().getWindow().getOutputScaleX();

				String mess = String.format(RES.getString("warning.resolution.message"), bestWidth, bestHeight, RECOMMENDED_WIDTH, RECOMMENDED_HEIGHT)+"\n\n"+winScale;
				getManager().showAlertAndCall(AlertType.ERROR, RES.getString("warning.resolution.title"), mess);
			}
		}

	}

	//-------------------------------------------------------------------
	public void greet() {
		checkResolution();
	}

	//-------------------------------------------------------------------
	private void dataClicked(ActionEvent event) {
		logger.info("data clicked");
		//		ContextMenu popup = btnData.getContextMenu();
		//		popup.setAnchorLocation(AnchorLocation.CONTENT_BOTTOM_RIGHT);
		//		popup.show(btnData, event.getScreenX(), event.getScreenY());
		Label lblDesc = new Label(RES.getString("dialog.enterdata.desc"));
		Label lblRules= new Label(RES.getString("label.rules"));
		ChoiceBox<RoleplayingSystem> cbRules = new ChoiceBox<>();
		cbRules.setConverter(new StringConverter<RoleplayingSystem>() {
			public String toString(RoleplayingSystem object) {return object.getName();}
			public RoleplayingSystem fromString(String string) {return null;}
		});

		/*
		 * Build a list of possible roleplaying systems
		 * Allow only those that are not supported for creation
		 */
		List<RoleplayingSystem> rules = new ArrayList<>(Arrays.asList());
		for (RulePlugin<?> plugin : CharacterProviderLoader.getRulePlugins()) {
			if (plugin.getSupportedFeatures().contains(RulePluginFeatures.DATA_INPUT)) {
				rules.add(plugin.getRules());
			}
		}
		cbRules.getItems().addAll(rules);
		Collections.sort(cbRules.getItems(), new Comparator<RoleplayingSystem>() {
			public int compare(RoleplayingSystem arg0, RoleplayingSystem arg1) {
				return Collator.getInstance().compare(arg0.getName(), arg1.getName());
			}
		});
//		cbRules.getSelectionModel().select(0);


		// Layout
		GridPane grid = new GridPane();
		grid.add(lblDesc , 0, 0, 2,1);
		grid.add(lblRules, 0, 2);
		grid.add( cbRules, 1, 2);
		grid.setVgap(20);
		grid.setHgap(20);

//		NavigButtonControl control = new NavigButtonControl();
//		cbRules.getSelectionModel().selectedItemProperty().addListener( (ov,o,n) -> {
//			control.setDisabled(CloseType.OK, n==null);
//		});

		CloseType closed = getManager().showAlertAndCall(
				AlertType.QUESTION,
				RES.getString("dialog.enterdata.title"),
				grid);
		if (closed==CloseType.OK) {
			RoleplayingSystem ruleSystem = cbRules.getValue();
			try {
				enterData(ruleSystem);
			} catch (Exception e) {
				logger.error("Failed entering data",e);
			}
		}
	};

	//-------------------------------------------------------------------
	private void updateDataInputContextMenu() {
		logger.debug("Update DataInput context menu");
		ContextMenu context = new ContextMenu();
		for (RulePlugin<?> plugin : CharacterProviderLoader.getRulePlugins()) {
			if (plugin.getSupportedFeatures().contains(RulePluginFeatures.DATA_INPUT)) {
				MenuItem item = new MenuItem(plugin.getRules().getName());
				item.setUserData(plugin.getRules());
				item.setOnAction(event -> enterData(plugin.getRules()));
				context.getItems().add(item);
			}
		}
		//		btnData.setContextMenu(context);
		if (context.getItems().isEmpty()) {
			logger.debug("Disable data input button since there are no ruleplugins available");
			btnData.setDisable(true);
		} else {
			btnData.setDisable(false);
		}

	}

	//-------------------------------------------------------------------
	private void enterData(RoleplayingSystem rules) {
		logger.info("enter data for "+rules);
		logger.info("this.getScene() = "+this.getScene());
		logger.info("MainScreenthis.getScene() = "+MainScreen.this.getScene());
		javafx.stage.Window window = null;
		if (this.getScene()!=null)
			window = this.getScene().getWindow();
		CommandResult result = CommandBus.fireCommand(this, CommandType.SHOW_DATA_INPUT_GUI,
				rules,
				window,
				getManager()
				);
		if (!result.wasProcessed()) {
			getManager().showAlertAndCall(
					AlertType.ERROR,
					"Das hätte nicht passieren dürfen",
					"Es hat sich kein Plugin gefunden, welches das Eingeben von Daten dieses Systems erlaubt.");
			return;
		}
		if (!result.wasSuccessful()) {
			getManager().showAlertAndCall(
					AlertType.ERROR,
					"Es gab einen Fehler beim Öffnen des Eingabedialogs",
					result.getMessage());
			return;
		}

		// Result can be accessed when childClosed is called
	}

}

