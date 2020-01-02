package org.prelle.genesis.page;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.genesis.Constants;
import org.prelle.genesis.ExternalTools;
import org.prelle.genesis.PrintHelper;
import org.prelle.genesis.jfx.common.CommonCharacterScreenPage;
import org.prelle.genesis.page.CharacterListCell.CharActionEvent;
import org.prelle.javafx.AlertType;
import org.prelle.javafx.CloseType;
import org.prelle.javafx.ManagedScreenPage;
import org.prelle.javafx.NavigButtonControl;
import org.prelle.javafx.ScreenManager;
import org.prelle.javafx.ScreenManagerProvider;

import de.rpgframework.ConfigContainer;
import de.rpgframework.RPGFramework;
import de.rpgframework.RPGFrameworkLoader;
import de.rpgframework.ResourceI18N;
import de.rpgframework.character.CharacterHandle;
import de.rpgframework.character.CharacterProvider;
import de.rpgframework.character.CharacterProviderLoader;
import de.rpgframework.character.RulePlugin;
import de.rpgframework.character.RulePluginFeatures;
import de.rpgframework.core.BabylonEventBus;
import de.rpgframework.core.BabylonEventType;
import de.rpgframework.core.CommandBus;
import de.rpgframework.core.CommandResult;
import de.rpgframework.core.CommandType;
import de.rpgframework.core.RoleplayingSystem;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * @author prelle
 *
 */
public class CharactersOverviewPage extends ManagedScreenPage implements EventHandler<CharActionEvent> {

	private final static Logger logger = LogManager.getLogger("genesis");
	private final static ResourceBundle RES = Constants.RES;

	private Map<RoleplayingSystem, ObservableList<CharacterHandle>> charsBySystem;

	private RPGFramework rpgFramework;
	private CharacterProvider charProv;

	private FlowPane charPane;
	private Map<RoleplayingSystem, CharacterListView> scrollsBySystem;
	
	private ExternalTools external;
	
	private MenuItem actionAdd;

	//-------------------------------------------------------------------
	public CharactersOverviewPage(ExternalTools external) {
		super("Genesis");
		this.external = external;
		rpgFramework = RPGFrameworkLoader.getInstance();
		logger.info("RPGFramework = "+rpgFramework);
		charProv     = CharacterProviderLoader.getCharacterProvider();
		if (charProv==null)
			throw new NullPointerException("No CharacterProvider found");
		charsBySystem= new HashMap<>();
		scrollsBySystem = new HashMap<>();
		logger.debug("<init>");

		initComponents();
		initLayout();
		initInteractivity();
	}

	//-------------------------------------------------------------------
	private void initComponents() {
		charPane = new FlowPane(Orientation.VERTICAL);
		actionAdd = new MenuItem(ResourceI18N.get(RES,"menuitem.create"), new Label("\uE1E2"));
	}

	//-------------------------------------------------------------------
	private void initLayout() {
//		getCommandBar().getPrimaryCommands().add(actionAdd);
		getCommandBar().setOpen(true);
		
		charPane.setHgap(40);
		charPane.setVgap(20);
		charPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
//		setStyle("-fx-background-color: lime");
//		charPane.setStyle("-fx-background-color: yellow");
		VBox.setVgrow(charPane, Priority.ALWAYS);
		
		logger.info("######################Set content of CharOverView to "+charPane);
		
		ScrollPane scroll = new ScrollPane(charPane);
		scroll.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.setContent(scroll);
	}

	//-------------------------------------------------------------------
	private void initInteractivity() {
		charPane.maxHeightProperty().bind(this.heightProperty().subtract(30));
		charPane.minHeightProperty().bind(this.heightProperty().subtract(30));
		
		actionAdd.setOnAction(ev -> createClicked(ev));
	}

	//-------------------------------------------------------------------
	private CharacterListView createRoleplayingSystemColumn(RoleplayingSystem system) {
		CharacterListView column = new CharacterListView(system, charsBySystem.get(system), this);
		column.maxHeightProperty().bind(charPane.heightProperty());
		column.setOnAction(this);
		return column;
	}

	//-------------------------------------------------------------------
	public void refresh() {
		logger.info("refresh()-----------------------------------------------");
		charsBySystem.clear();
		charPane.getChildren().clear();
		// Create a list of characters, sorted by modification date
		// In the same time, build a ordered list of recently used roleplaying systems
		logger.debug("START: call getMyCharacters()");
		List<CharacterHandle> toSort = charProv.getMyCharacters();
		logger.debug("STOP : call getMyCharacters()");
		List<RoleplayingSystem> orderedSystems = new ArrayList<>();
		Collections.sort(toSort, new Comparator<CharacterHandle>() {
			public int compare(CharacterHandle o1, CharacterHandle o2) {
				return -1*o1.getLastModified().compareTo(o2.getLastModified());
			}
		});

		// Build separate lists by roleplaying system
		for (CharacterHandle handle : toSort) {
//			logger.debug("CHAR: "+handle.getName()+" for "+handle.getRuleIdentifier());
			// Update ordered roleplaying systems
			if (!orderedSystems.contains(handle.getRuleIdentifier()))
				orderedSystems.add(handle.getRuleIdentifier());
			// Update separate character lists
			ObservableList<CharacterHandle> systemList = addRoleplayingSystem(handle.getRuleIdentifier());
			systemList.add(handle);
		}

//		for (RoleplayingSystem system : orderedSystems) {
//			charPane.getChildren().add(scrollsBySystem.get(system));
//		}
		logger.info("STOP: refresh()-----------------------------------------------");
	}

	//-------------------------------------------------------------------
	private void showCommonCharacterScreen(CharacterHandle handle) {
			logger.debug("showCommonCharacterScreen on "+handle);
			CommonCharacterScreenPage screen = new CommonCharacterScreenPage();
			screen.setData(handle);
			getScreenManager().navigateTo(screen);
	}

	//-------------------------------------------------------------------
	private void deleteClicked(CharacterHandle selected) {
		logger.debug("Delete "+selected);

		CloseType response = getScreenManager().showAlertAndCall(
				AlertType.CONFIRMATION,
				RES.getString("confirm.delete.char"),
				ResourceI18N.format(RES,"confirm.delete.char.long", selected.getRuleIdentifier().getName(), selected.getName())
				);
		logger.debug("Response was "+response);
		if (response==CloseType.OK || response==CloseType.YES) {
			logger.info("delete character "+selected);
			try {
				charProv.deleteCharacter(selected);
				RoleplayingSystem rules = selected.getRuleIdentifier();
				charsBySystem.get(rules).remove(selected);
				((VBox)scrollsBySystem.get(rules)).getChildren().removeIf(new Predicate<Node>() {
					@Override
					public boolean test(Node t) {
						return t.getUserData()==selected;
					}
				});
				//				deselect();

			} catch (Exception e) {
				StringWriter buf = new StringWriter();
				e.printStackTrace(new PrintWriter(buf));
				getScreenManager().showAlertAndCall(AlertType.ERROR,
						ResourceI18N.format(RES, "error.deleting.char", ""),
						ResourceI18N.format(RES,"error.deleting.char", buf.toString())
						);
			}
		}
	};

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

		VBox layout = new VBox(20,message, buttons);


		NavigButtonControl control = new NavigButtonControl();
		control.setCallback( new Callback<CloseType, Boolean>() {
			public Boolean call(CloseType param) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		cbRules.selectedToggleProperty().addListener( (ov,o,n) -> {
			control.setDisabled(CloseType.OK, n==null);
		});

		CloseType result = getScreenManager().showAlertAndCall(AlertType.QUESTION, RES.getString("newchardialog.title"), layout, control);
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

		logger.debug("ScreenManager = "+getManager());
		if (getScreenManager()==null) 
			throw new NullPointerException("ScreenManager not set");

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
		cbRules.getItems().remove(RoleplayingSystem.ALL);
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

		NavigButtonControl control = new NavigButtonControl();
		tfName.textProperty().addListener( (ov,o,n) -> {
			control.setDisabled(CloseType.OK, n==null || n.isEmpty());
		});
		
		CloseType closed = getScreenManager().showAlertAndCall(
				AlertType.QUESTION,
				RES.getString("dialog.addchar.title"),
				grid, control);
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
		ScreenManager manager = getScreenManager();
		//		ctxMenuAdd.hide();
		CommandResult result = CommandBus.fireCommand(this, CommandType.SHOW_CHARACTER_CREATION_GUI,
				rules,
				getScene()!=null?(this.getScene().getWindow()):null,
				manager
				);
		if (!result.wasProcessed()) {
			logger.error("Character creation failed: "+result.getMessage());
			manager.showAlertAndCall(
					AlertType.ERROR,
					"Das hätte nicht passieren dürfen",
					"Es hat sich kein Plugin gefunden, welches das Erzeugen von Charakteren dieses Systems erlaubt.");
			return;
		}
		if (!result.wasSuccessful()) {
			manager.showAlertAndCall(
					AlertType.ERROR,
					"Es gab einen Fehler beim Erzeugen des Charakters",
					result.getMessage());
			return;
		}

		// Result can be accessed when childClosed is called
	}

	//-------------------------------------------------------------------
	private void editClicked(CharacterHandle selected) {
		logger.info("edit character "+selected.getName());
		logger.debug("fireCommand "+CommandType.SHOW_CHARACTER_MODIFICATION_GUI);
		ScreenManager manager = getScreenManager();
		boolean willWork = CommandBus.canProcessCommand(this, CommandType.SHOW_CHARACTER_MODIFICATION_GUI,
				selected.getRuleIdentifier(),
				selected.getCharacter(),
				selected,
				(getScene()!=null)?getScene().getWindow():null,
						manager
				);
		if (!willWork) {
			logger.warn("TODO: Remote open "+selected);
			try {
				external.open(selected);
			} catch (IOException e) {
				manager.showAlertAndCall(AlertType.ERROR, "Error opening externally", e.toString());
			} catch (Exception e) {
				StringWriter out = new StringWriter();
				e.printStackTrace(new PrintWriter(out));
				manager.showAlertAndCall(AlertType.ERROR, "Error opening externally", out.toString());
			}
			return;
		}
//		edited = selected;
		CommandResult result = CommandBus.fireCommand(this, CommandType.SHOW_CHARACTER_MODIFICATION_GUI,
				selected.getRuleIdentifier(),
				selected.getCharacter(),
				selected,
				(getScene()!=null)?getScene().getWindow():null,
				manager
				);
		logger.debug("After calling SHOW_CHARACTER_MODIFICATION_GUI  result was processed="+result.wasProcessed()+"/succ="+result.wasSuccessful()+"/mess="+result.getMessage());
		if (!result.wasProcessed()) {
			manager.showAlertAndCall(
					AlertType.ERROR,
					"Das hätte nicht passieren dürfen",
					"Es hat sich kein Plugin gefunden, welches das Editieren von Charakteren dieses Systems erlaubt."
					);
		} else if (!result.wasSuccessful()) {
			manager.showAlertAndCall(
					AlertType.ERROR,
					"Das hätte nicht passieren dürfen",
					"Der Plugin-Aufruf zum Editieren des Charakters war nicht erfolgreich.\nMeldung: "+result.getMessage()
					);
		}
	};

	//--------------------------------------------------------------------
	private ObservableList<CharacterHandle> addRoleplayingSystem(RoleplayingSystem rules) {
		ObservableList<CharacterHandle> list = charsBySystem.get(rules);
		if (list==null) {
			list = FXCollections.observableArrayList();
			charsBySystem.put(rules, list);
			scrollsBySystem.put(rules, createRoleplayingSystemColumn(rules));
			charPane.getChildren().add(scrollsBySystem.get(rules));
		}
		return list;
	}

	//-------------------------------------------------------------------
	/**
	 * @see javafx.event.EventHandler#handle(javafx.event.Event)
	 */
	@Override
	public void handle(CharActionEvent event) {
		logger.info("handle "+event.getCommand());
		CharacterHandle handle = event.getCharacter();
		switch (event.getCommand()) {
		case CharacterListCell.DELETE:
			deleteClicked(handle);
			break;
		case CharacterListCell.ATTACH:
			showCommonCharacterScreen(handle);
			break;
		case CharacterListCell.PRINT:
			PrintHelper.printClicked(handle, getScreenManager());
			break;
		case CharacterListCell.EDIT:
			editClicked(handle);
			break;
		default:
			logger.warn("Don't know how to handle "+event.getCommand()+" for "+handle);
		}
	}


}

//-------------------------------------------------------------------
//-------------------------------------------------------------------
class CharacterListView extends VBox {

	private RoleplayingSystem system;
	private ObservableList<CharacterHandle> list;
	private VBox scrollColumn;
	private Label systHead;
	private ScreenManagerProvider provider;
	private EventHandler<CharacterListCell.CharActionEvent> handler;

	public CharacterListView(RoleplayingSystem system, ObservableList<CharacterHandle> list, ScreenManagerProvider provider) {
		this.system = system;
		this.list   = list;
		this.provider = provider;

		initComponents();
		initLayout();
		initInteractivity();
	}

	//-------------------------------------------------------------------
	public void refresh() {
		scrollColumn.getChildren().clear();
		// Add characters
		for (CharacterHandle handle : list) {
			CharacterListCell cell = new CharacterListCell(handle, this);
			scrollColumn.getChildren().add(cell);
		}
	}

	//-------------------------------------------------------------------
	public void setOnAction(EventHandler<CharacterListCell.CharActionEvent> handler) {
		this.handler = handler;
	}

	//-------------------------------------------------------------------
	public boolean contains(CharacterHandle handle) {
		return list.contains(handle);
	}

	//-------------------------------------------------------------------
	public void clearSelection() {
		for (Node node : scrollColumn.getChildren()) {
			((CharacterListCell)node).deselect();
		}
	}

	//-------------------------------------------------------------------
	ScreenManager getScreenManager() {
		return provider.getScreenManager();
	}

	//-------------------------------------------------------------------
	private void initComponents() {
		systHead = new Label(system.getName());
		systHead.getStyleClass().add("section-head");

		scrollColumn = new VBox();
	}

	//-------------------------------------------------------------------
	private void initLayout() {
		setPrefWidth(300);
		setSpacing(10);
		getStyleClass().add("content");

		scrollColumn.setFillWidth(true);
		//			scrollColumn.setPrefWidth(300);
		scrollColumn.setSpacing(10);

		ScrollPane scroll = new ScrollPane(scrollColumn);
		scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		scroll.setFitToWidth(true);
		getChildren().addAll(systHead, scroll);
		//		scroll.maxHeightProperty().bind(charPane.heightProperty().subtract(10).subtract(10));
		scroll.minHeightProperty().set(64);

		// Add characters
		for (CharacterHandle handle : list) {
			CharacterListCell cell = new CharacterListCell(handle, this);
			scrollColumn.getChildren().add(cell);
		}
	}

	//-------------------------------------------------------------------
	private void initInteractivity() {
		list.addListener(new ListChangeListener<CharacterHandle>() {
			public void onChanged(Change<? extends CharacterHandle> c) {
//				logger.debug("Change "+c);
				while (c.next()) {
					if (c.wasAdded()) {
						int pos = c.getFrom();
						for (CharacterHandle tmp : c.getAddedSubList()) {
							scrollColumn.getChildren().add(pos++, new CharacterListCell(tmp, CharacterListView.this));
						}
					} else if (c.wasRemoved()) {
						for (CharacterHandle tmp : c.getRemoved()) {
							for (Node cell : new ArrayList<Node>(scrollColumn.getChildren())) {
								if ( ((CharacterListCell)cell).getHandle()==tmp) {
									scrollColumn.getChildren().remove(cell);
								}
							}

						}
					}
				}
			}
		});
	}

	//-------------------------------------------------------------------
	void fireAction(CharActionEvent event) {
		if (handler!=null)
			handler.handle(event);

	}

}