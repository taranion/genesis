package org.prelle.genesis.page;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.javafx.ManagedScreenPage;
import org.prelle.javafx.ResponsiveControl;
import org.prelle.javafx.WindowMode;
import org.prelle.genesis.Constants;
import org.prelle.javafx.CommandBar.DisplayState;

import de.rpgframework.ConfigContainer;
import de.rpgframework.ConfigOption;
import de.rpgframework.RPGFrameworkLoader;
import de.rpgframework.ResourceI18N;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * @author prelle
 *
 */
public class GenesisConfigurationPage extends ManagedScreenPage {

	private final static Logger logger = LogManager.getLogger("genesis");
	
	private static ResourceBundle RES = ResourceBundle.getBundle(GenesisConfigurationPage.class.getName());
	
	private VBox layout;
	
	private BabylonConfigurationNode nodeBabylon;
	private GenesisConfigurationNode nodeGenesis;
	private SplittermondConfigurationNode nodeSplittermond;
	private SR6ConfigurationNode nodeShadowrun6;

	//-------------------------------------------------------------------
	/**
	 */
	public GenesisConfigurationPage() {
		super(ResourceI18N.get(RES,"screen.configuration.heading"));
		initComponents();
		initLayout();
		initInteractivity();
		getCommandBar().setDisplayState(DisplayState.HIDDEN);
	}
	
	//--------------------------------------------------------------------
	private void initComponents() {
		nodeBabylon = new BabylonConfigurationNode();
		nodeGenesis = new GenesisConfigurationNode();
		try { nodeSplittermond = new SplittermondConfigurationNode(); } catch (Exception e) { logger.warn("Failed initializing Splittermond settings",e); }
		try { nodeShadowrun6   = new SR6ConfigurationNode(); } catch (Exception e) { logger.warn("Failed initializing Shadowrun 6 settings",e); }
	}
	
	//--------------------------------------------------------------------
	private void initLayout() {
		
		layout = new VBox();
		layout.setStyle("-fx-spacing: 2em;");
		layout.setMaxWidth(Double.MAX_VALUE);
		
		layout.getChildren().addAll(nodeBabylon, nodeGenesis);
		if (nodeSplittermond!=null)
			layout.getChildren().add(nodeSplittermond);
		if (nodeShadowrun6!=null)
			layout.getChildren().add(nodeShadowrun6);
		ScrollPane scroll = new ScrollPane(layout);
		scroll.setFitToWidth(true);
		scroll.setMaxWidth(Double.MAX_VALUE);
		
//		HBox wider = new HBox(scroll);
//		HBox.setHgrow(scroll, Priority.ALWAYS);
		
		setContent(scroll);
	}
	
	//--------------------------------------------------------------------
	private void initInteractivity() {
	}

}

class GenesisConfigurationNode extends StackPane implements ResponsiveControl {
	
	private static ResourceBundle RES = ResourceBundle.getBundle(GenesisConfigurationPage.class.getName());;

	private ConfigContainer cfgGenesis;
	private ConfigOption<Boolean> optFullScreen;
	private ConfigOption<Boolean> optSecondary;
	
	private ChoiceBox<Locale> cbLanguage;
	private CheckBox  cbFullScreen;
	private CheckBox  cbSecondary;

	private Label deGeneral;
	private Label deSecondary;
	private Label deFullScreen;

	private VBox layoutSmall;
	private GridPane layoutWide;
	
	//-------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public GenesisConfigurationNode() {
		ConfigContainer cfgRoot = RPGFrameworkLoader.getInstance().getConfiguration();
		cfgGenesis = (ConfigContainer) cfgRoot.getChild("genesis");
		optFullScreen = (ConfigOption<Boolean>) cfgGenesis.getOption(Constants.PREFKEY_FULLSCREEN);
		optSecondary  = (ConfigOption<Boolean>) cfgGenesis.getOption(Constants.PREFKEY_USE_SECONDARY_SCREEN);
		
		initComponents();
		initLayoutWide();
		setData();
		initInteractivity();
	}

	//-------------------------------------------------------------------
	private void initComponents() {
		cbLanguage = new ChoiceBox<>();
		cbLanguage.getItems().addAll(Locale.GERMAN, Locale.ENGLISH, Locale.FRENCH);
		cbLanguage.setConverter(new StringConverter<Locale>() {
			public String toString(Locale loc) {
				return RES.getString("language."+loc.getLanguage());
			}
			public Locale fromString(String string) {return null;}
		});
		cbFullScreen = new CheckBox(optFullScreen.getName());
		cbSecondary  = new CheckBox(optSecondary.getName());
		cbFullScreen.getStyleClass().add("base");
		cbSecondary.getStyleClass().add("base");
		
		deGeneral  = new Label(ResourceI18N.get(RES,"configdesc.genesis"));
		deGeneral.getStyleClass().add("text-tertiary-info");
		deGeneral.setStyle("-fx-text-fill: textcolor-highlight-primary;");
		deFullScreen = new Label(ResourceI18N.get(RES,"configdesc.genesis.fullscreen"));
		deSecondary  = new Label(ResourceI18N.get(RES,"configdesc.genesis.secondary"));
		deFullScreen.setWrapText(true);
		deSecondary.setWrapText(true);
	}

	//-------------------------------------------------------------------
	private void initLayoutWide() {
		Label headBabylon = new Label(ResourceI18N.get(RES,"configsection.genesis"));
		headBabylon.getStyleClass().add("text-subheader");

		Label headSpliMo = new Label(ResourceI18N.get(RES,"configsection.splittermond"));
		headSpliMo.getStyleClass().add("text-subheader");

		layoutWide = new GridPane();
		layoutWide.setMaxWidth(Double.MAX_VALUE);
		layoutWide.setStyle("-fx-hgap: 1em");
		
		layoutWide.add(headBabylon, 0, 0, 2,1);
		layoutWide.add(deGeneral , 0, 1, 2,1);

//		layoutWide.add(lbLanguage, 0, 2);
//		layoutWide.add(cbLanguage, 1, 2);
//		layoutWide.add(deLanguage, 1, 3);
//		GridPane.setMargin(lbLanguage, new Insets(10, 0, 0, 0));
//		GridPane.setMargin(cbLanguage, new Insets(10, 0, 0, 0));
		
		layoutWide.add(cbFullScreen, 0, 2, 2,1);
		layoutWide.add(deFullScreen, 1, 3);
		GridPane.setMargin(cbFullScreen, new Insets(10, 0, 0, 0));
		
		layoutWide.add(cbSecondary, 0, 4, 2,1);
		layoutWide.add(deSecondary, 1, 5);
		GridPane.setMargin(cbSecondary, new Insets(10, 0, 0, 0));
		
		layoutWide.getColumnConstraints().add(new ColumnConstraints(30));
		
		getChildren().clear();
		getChildren().add(layoutWide);
	}

	//-------------------------------------------------------------------
	private void initLayoutSmall() {
		Label headBabylon = new Label(ResourceI18N.get(RES,"configsection.genesis"));
		headBabylon.getStyleClass().add("text-subheader");

		layoutSmall = new VBox();
		
		layoutSmall.getChildren().addAll(headBabylon, deGeneral);
//		layoutSmall.getChildren().addAll(lbLanguage, cbLanguage, deLanguage);
		layoutSmall.getChildren().addAll(cbFullScreen , deFullScreen);
		layoutSmall.getChildren().addAll(cbSecondary , deSecondary);
		VBox.setMargin(deGeneral, new Insets(10, 0, 0, 0));
		VBox.setMargin(cbFullScreen, new Insets(10, 0, 0, 0));
		VBox.setMargin(cbSecondary, new Insets(10, 0, 0, 0));
		
		getChildren().clear();
		getChildren().add(layoutSmall);
	}

	//-------------------------------------------------------------------
	private void initInteractivity() {
		cbFullScreen.selectedProperty().addListener( (ov,o,n) -> optFullScreen.set(n));
		cbSecondary.selectedProperty().addListener( (ov,o,n) -> optSecondary.set(n));
	}
	
	//--------------------------------------------------------------------
	private void setData() {
//		logger.debug("Search properties in "+prefs);
//		
//		String lang = prefs.get("language", null);
//		if (lang==null) {
//			lang = Locale.getDefault().getLanguage();
//		}
//		if (lang.equals("de"))
//			languages.setValue(Locale.GERMAN);
//		else if (lang.equals("en"))
//			languages.setValue(Locale.ENGLISH);
//
//		// Resolution
//		DPIProfile dpiV = ModernUI.getDPIProfile();
//		if (dpiV==null) {
//			dpi.setValue(DPIProfile.MEDIUM);
//		} else
//			dpi.setValue(dpiV);
		
		cbFullScreen.selectedProperty().set((boolean) optFullScreen.getValue());
		cbSecondary.selectedProperty().set((boolean) optSecondary.getValue());
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.javafx.ResponsiveControl#setResponsiveMode(org.prelle.javafx.WindowMode)
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


class SplittermondConfigurationNode extends StackPane implements ResponsiveControl {

	private final static Logger logger = LogManager.getLogger("genesis");
	
	private static ResourceBundle RES = ResourceBundle.getBundle(GenesisConfigurationPage.class.getName());;

	private ConfigContainer cfgSpliMo;
	private ConfigOption<Double> optHGFactor;
	
	private Label deHgFactor;
	private TextField tfHgFactor;

	private VBox layoutSmall;
	private GridPane layoutWide;
	
	//-------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public SplittermondConfigurationNode() {
		ConfigContainer cfgRoot = RPGFrameworkLoader.getInstance().getConfiguration();
		ConfigContainer plugins = (ConfigContainer) cfgRoot.getChild("plugins");
		try {
			cfgSpliMo = (ConfigContainer) plugins.getChild("splittermond");
			optHGFactor = (ConfigOption<Double>) cfgSpliMo.getOption("exp_factor");
			
			initComponents();
			initLayoutWide();
			setData();
			initInteractivity();
		} catch (NoSuchElementException e) {
			logger.debug("Don't show Splittermond options");
		}
	}

	//-------------------------------------------------------------------
	private void initComponents() {
		tfHgFactor = new TextField();
		tfHgFactor.setPromptText("1.0");
		tfHgFactor.setPrefColumnCount(4);
		
		deHgFactor = new Label(RES.getString("configdesc.splittermond.hgFactor.desc"));

	}

	//-------------------------------------------------------------------
	private void initLayoutWide() {
		Label headSpliMo = new Label(ResourceI18N.get(RES,"configsection.splittermond"));
		headSpliMo.getStyleClass().add("text-subheader");

		layoutWide = new GridPane();
		layoutWide.setMaxWidth(Double.MAX_VALUE);
		layoutWide.setStyle("-fx-hgap: 1em");
		
		layoutWide.add(headSpliMo, 0, 0, 2,1);
		
		Label lbHgFactor = new Label(ResourceI18N.get(RES,"configdesc.splittermond.hgFactor"));
		lbHgFactor.getStyleClass().add("base");
		layoutWide.add(lbHgFactor, 0, 1);
		layoutWide.add(tfHgFactor, 1, 1);
		layoutWide.add(deHgFactor, 1, 2);
//		GridPane.setMargin(cbFullScreen, new Insets(10, 0, 0, 0));
		
//		layoutWide.getColumnConstraints().add(new ColumnConstraints(30));
//		
		getChildren().clear();
		getChildren().add(layoutWide);
	}

	//-------------------------------------------------------------------
	private void initLayoutSmall() {
		Label headSpliMo = new Label(ResourceI18N.get(RES,"configsection.splittermond"));
		headSpliMo.getStyleClass().add("text-subheader");

		layoutSmall = new VBox();
		
		Label lbHgFactor = new Label(ResourceI18N.get(RES,"configdesc.splittermond.hgFactor"));
		lbHgFactor.getStyleClass().add("base");

		layoutSmall.getChildren().addAll(headSpliMo);
//		layoutSmall.getChildren().addAll(lbLanguage, cbLanguage, deLanguage);
		layoutSmall.getChildren().addAll(new HBox(20, lbHgFactor, tfHgFactor), deHgFactor);
		
		getChildren().clear();
		getChildren().add(layoutSmall);
	}

	//-------------------------------------------------------------------
	private void initInteractivity() {
		tfHgFactor.textProperty().addListener( (ov,o,n) -> {
			try {
				Double parsed = Double.parseDouble(tfHgFactor.getText());
				optHGFactor.set(parsed);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	//--------------------------------------------------------------------
	private void setData() {
		tfHgFactor.textProperty().set( String.valueOf(optHGFactor.getValue()));
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.javafx.ResponsiveControl#setResponsiveMode(org.prelle.javafx.WindowMode)
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


class SR6ConfigurationNode extends StackPane implements ResponsiveControl {

	private final static Logger logger = LogManager.getLogger("genesis");
	
	private static ResourceBundle RES = ResourceBundle.getBundle(GenesisConfigurationPage.class.getName());;

	private ConfigContainer cfgShadowrun;
	private ConfigOption<Boolean> optIgnoreLanguage;
	
	private Label deIgnoreLanguage;
	private CheckBox cbIgnoreLanguage;

	private VBox layoutSmall;
	private GridPane layoutWide;
	
	//-------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public SR6ConfigurationNode() {
		ConfigContainer cfgRoot = RPGFrameworkLoader.getInstance().getConfiguration();
		ConfigContainer plugins = (ConfigContainer) cfgRoot.getChild("plugins");
		try {
			cfgShadowrun = (ConfigContainer) plugins.getChild("shadowrun6");
			optIgnoreLanguage = (ConfigOption<Boolean>) cfgShadowrun.getOption("data.ignore_plugin_language");
			
			initComponents();
			initLayoutWide();
			setData();
			initInteractivity();
		} catch (NoSuchElementException e) {
			logger.debug("Don't show Shadowrun options");
			return;
		}
	}

	//-------------------------------------------------------------------
	private void initComponents() {
		cbIgnoreLanguage = new CheckBox(ResourceI18N.get(RES,"configdesc.shadowrun6.ignoreLanguage"));
		cbIgnoreLanguage.getStyleClass().add("base");
		
		deIgnoreLanguage = new Label(RES.getString("configdesc.shadowrun6.ignoreLanguage.desc"));

	}

	//-------------------------------------------------------------------
	private void initLayoutWide() {
		Label headSpliMo = new Label(ResourceI18N.get(RES,"configsection.shadowrun6"));
		headSpliMo.getStyleClass().add("text-subheader");

		layoutWide = new GridPane();
		layoutWide.setMaxWidth(Double.MAX_VALUE);
		layoutWide.setStyle("-fx-hgap: 1em");
		
		layoutWide.add(headSpliMo, 0, 0, 2,1);
		
		layoutWide.add(cbIgnoreLanguage, 0, 1, 2,1);
		layoutWide.add(deIgnoreLanguage, 1, 2);
//		GridPane.setMargin(cbFullScreen, new Insets(10, 0, 0, 0));
		
//		layoutWide.getColumnConstraints().add(new ColumnConstraints(30));
//		
		getChildren().clear();
		getChildren().add(layoutWide);
	}

	//-------------------------------------------------------------------
	private void initLayoutSmall() {
		Label headSpliMo = new Label(ResourceI18N.get(RES,"configsection.shadowrun6"));
		headSpliMo.getStyleClass().add("text-subheader");

		layoutSmall = new VBox();
		
		layoutSmall.getChildren().addAll(headSpliMo);
//		layoutSmall.getChildren().addAll(lbLanguage, cbLanguage, deLanguage);
		layoutSmall.getChildren().addAll(new HBox(20, cbIgnoreLanguage), deIgnoreLanguage);
		
		getChildren().clear();
		getChildren().add(layoutSmall);
	}

	//-------------------------------------------------------------------
	private void initInteractivity() {
		cbIgnoreLanguage.selectedProperty().addListener( (ov,o,n) -> optIgnoreLanguage.set(n));
	}
	
	//--------------------------------------------------------------------
	private void setData() {
		cbIgnoreLanguage.selectedProperty().set( optIgnoreLanguage.getValue());
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.javafx.ResponsiveControl#setResponsiveMode(org.prelle.javafx.WindowMode)
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