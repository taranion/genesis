package org.prelle.genesis.page;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.prelle.javafx.ResponsiveControl;
import org.prelle.javafx.WindowMode;

import de.rpgframework.ConfigContainer;
import de.rpgframework.ConfigOption;
import de.rpgframework.RPGFramework;
import de.rpgframework.RPGFrameworkLoader;
import de.rpgframework.ResourceI18N;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;

/**
 * @author prelle
 *
 */
public class BabylonConfigurationNode extends StackPane implements ResponsiveControl {
	
	private static ResourceBundle GUICOMMON = ResourceBundle.getBundle(BabylonConfigurationNode.class.getName());
	
	private Preferences frameworkPrefs = Preferences.userRoot().node("/de/rpgframework");

	private ConfigOption<Boolean> optAskOnStartup;
	private ConfigOption<Locale> optLanguage;
	private ConfigOption<String> optDataDir;
	
	private ChoiceBox<Locale> cbLanguage;
	private TextField tfDataDir;
	private CheckBox  cbAskOnStartup;
	
	private Label lbLanguage;
	private Label lbDataDir;
	private Button    btnDatadir;
	private HBox lineDirBt; 

	private Label deGeneral;
	private Label deLanguage;
	private Label deDataDir;
	private Label deAskOnStartup;
	
	private VBox layoutSmall;
	private GridPane layoutWide;
	
	//-------------------------------------------------------------------
	/**
	 */
	@SuppressWarnings("unchecked")
	public BabylonConfigurationNode() {
		ConfigContainer cfgRoot = RPGFrameworkLoader.getInstance().getConfiguration();
		optAskOnStartup = (ConfigOption<Boolean>) ((ConfigContainer)cfgRoot.getChild("rules")).getChild("askOnStartUp");
		optLanguage = (ConfigOption<Locale>) cfgRoot.getChild(RPGFramework.PROP_LANGUAGE);
		optDataDir  = (ConfigOption<String>) cfgRoot.getChild(RPGFramework.PROP_DATADIR);

		initComponents();
		initLayoutWide();
		setData();
		initInteractivity();
		
	}

	//-------------------------------------------------------------------
	private void initComponents() {
		deGeneral  = new Label(ResourceI18N.get(GUICOMMON, "configdesc.babylon"));
		deGeneral.getStyleClass().add("text-tertiary-info");
		deGeneral.setStyle("-fx-text-fill: textcolor-highlight-primary;");

		/*
		 * Language
		 */
		lbLanguage = new Label(ResourceI18N.get(GUICOMMON, "label.language"));
		lbLanguage.getStyleClass().add("base");
		cbLanguage = new ChoiceBox<>();
		cbLanguage.getItems().addAll(optLanguage.getChoiceOptions());
		cbLanguage.setConverter(new StringConverter<Locale>() {
			public String toString(Locale loc) {
//				return optLanguage.getValueConverter().toString(loc);
				return ResourceI18N.get(GUICOMMON, "language."+loc.getLanguage());
			}
			public Locale fromString(String string) {return optLanguage.getValueConverter().fromString(string);}
		});
		deLanguage = new Label(ResourceI18N.get(GUICOMMON, "configdesc.babylon.language"));
		
		/**
		 * DataDir
		 */
		tfDataDir  = new TextField();
		tfDataDir.setStyle("-fx-max-width: 50em");
		btnDatadir = new Button(ResourceI18N.get(GUICOMMON, "label.select"));
		System.err.println("btnDatadir = "+btnDatadir.getStyleClass());
		btnDatadir.getStyleClass().add("bordered");
		lineDirBt = new HBox(5, tfDataDir, btnDatadir);
		lineDirBt.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(tfDataDir, Priority.ALWAYS);
		lbDataDir  = new Label(optDataDir.getName());
		lbDataDir.getStyleClass().add("base");
		deDataDir  = new Label(ResourceI18N.get(GUICOMMON, "configdesc.babylon.dataDir"));
		
		/**
		 * Ask (for rules) on startup?
		 */
		cbAskOnStartup = new CheckBox(optAskOnStartup.getName());
		cbAskOnStartup.getStyleClass().add("base");
		
		deAskOnStartup = new Label(ResourceI18N.get(GUICOMMON, "configdesc.babylon.rules.askOnStartUp"));
		deLanguage.setWrapText(true);
		deDataDir.setWrapText(true);
	}

	//-------------------------------------------------------------------
	private void initLayoutWide() {
		Label headBabylon = new Label(ResourceI18N.get(GUICOMMON, "configsection.babylon"));
		headBabylon.getStyleClass().add("text-subheader");

		layoutWide = new GridPane();
		layoutWide.setMaxWidth(Double.MAX_VALUE);
		layoutWide.setStyle("-fx-hgap: 1em");
		
		layoutWide.add(headBabylon, 0, 0, 2,1);
		layoutWide.add(deGeneral , 0, 1, 2,1);

		layoutWide.add(lbLanguage, 0, 2);
		layoutWide.add(cbLanguage, 1, 2);
		layoutWide.add(deLanguage, 1, 3);
		GridPane.setMargin(lbLanguage, new Insets(10, 0, 0, 0));
		GridPane.setMargin(cbLanguage, new Insets(10, 0, 0, 0));
		
		layoutWide.add(lbDataDir, 0, 4);
		layoutWide.add(lineDirBt, 1, 4);
		layoutWide.add(deDataDir, 1, 5);
		GridPane.setMargin(lbDataDir, new Insets(10, 0, 0, 0));
		GridPane.setMargin(lineDirBt, new Insets(10, 0, 0, 0));
		GridPane.setHgrow(lineDirBt, Priority.ALWAYS);

		layoutWide.add(cbAskOnStartup, 0, 6, 2,1);
		layoutWide.add(deAskOnStartup, 1, 7);
		GridPane.setMargin(cbAskOnStartup, new Insets(10, 0, 0, 0));
		
		getChildren().clear();
		getChildren().add(layoutWide);
	}

	//-------------------------------------------------------------------
	private void initLayoutSmall() {
		Label headBabylon = new Label(ResourceI18N.get(GUICOMMON, "configsection.babylon"));
		headBabylon.getStyleClass().add("text-subheader");

		layoutSmall = new VBox();
		
		layoutSmall.getChildren().addAll(headBabylon, deGeneral);
		layoutSmall.getChildren().addAll(lbLanguage, cbLanguage, deLanguage);
		layoutSmall.getChildren().addAll(lbDataDir , lineDirBt , deDataDir);
		layoutSmall.getChildren().addAll(cbAskOnStartup, deAskOnStartup);
		VBox.setMargin(deGeneral, new Insets(10, 0, 0, 0));
		VBox.setMargin(lbLanguage, new Insets(10, 0, 0, 0));
		VBox.setMargin(lbDataDir, new Insets(10, 0, 0, 0));
		VBox.setMargin(cbAskOnStartup, new Insets(10, 0, 0, 0));
		
		getChildren().clear();
		getChildren().add(layoutSmall);
	}

	//-------------------------------------------------------------------
	private void initInteractivity() {
		cbLanguage.getSelectionModel().selectedItemProperty().addListener( (ov,o,n) -> optLanguage.set(n));
		btnDatadir.setOnAction(event -> {
			String dataDir = frameworkPrefs.get(RPGFramework.PROP_DATADIR, null);
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setInitialDirectory(new File(dataDir));
			 chooser.setTitle(ResourceI18N.get(GUICOMMON, "generalsettingsscreen.changedatadir.title"));
			 File selected = chooser.showDialog(BabylonConfigurationNode.this.getScene().getWindow());
			 if (selected!= null) {
				 if (selected.getAbsolutePath().equals(dataDir))
					 return;
				 frameworkPrefs.put(RPGFramework.PROP_DATADIR, selected.getAbsolutePath());
				 tfDataDir.setText(selected.getAbsolutePath());
//				 manager.showAlertAndCall(AlertType.NOTIFICATION, 
//						 GUICOMMON.getString("generalsettingsscreen.changedatadir.confirm.title"), 
//						 GUICOMMON.getString("generalsettingsscreen.changedatadir.confirm.descr"));
			 }
		});
		tfDataDir.textProperty().addListener( (ov,o,n) -> frameworkPrefs.put(RPGFramework.PROP_DATADIR, n));
		cbAskOnStartup.selectedProperty().addListener( (ov,o,n) -> optAskOnStartup.set(n));
	}
	
	//--------------------------------------------------------------------
	private void setData() {
		cbLanguage.setValue((Locale)optLanguage.getValue());
		tfDataDir.setText(optDataDir.getStringValue());
		cbAskOnStartup.selectedProperty().set((boolean) optAskOnStartup.getValue());
	}

	//-------------------------------------------------------------------
	/**
	 * @param value
	 */
	@Override
	public void setResponsiveMode(WindowMode value) {
		switch (value) {
		case MINIMAL:
			initLayoutSmall();
			break;
		default:
			if (getChildren().contains(layoutSmall))
				initLayoutWide();
			break;
		}
	}

}
