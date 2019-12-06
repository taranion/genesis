package org.prelle.genesis.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.javafx.CommandBar.DisplayState;
import org.prelle.javafx.ManagedScreenPage;

import de.rpgframework.ResourceI18N;
import de.rpgframework.character.CharacterProviderLoader;
import de.rpgframework.character.PluginDescriptor;
import de.rpgframework.character.PluginState;
import de.rpgframework.character.RulePlugin;
import de.rpgframework.character.RulePluginFeatures;
import de.rpgframework.core.RoleplayingSystem;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * @author prelle
 *
 */
public class PluginsPage extends ManagedScreenPage {

	private static ResourceBundle RES = ResourceBundle.getBundle(PluginsPage.class.getName());;
	private final static Logger logger = LogManager.getLogger("genesis");
	
	private TabPane tabs;
	private Map<RoleplayingSystem,TableView<RulePlugin<?>>> tablesBySystem;

	//-------------------------------------------------------------------
	/**
	 */
	public PluginsPage() {
		super(ResourceI18N.get(RES,"pluginspage.heading"));
		initComponents();
		initLayout();
		initInteractivity();
		refresh();
		getCommandBar().setDisplayState(DisplayState.HIDDEN);
	}
	
	//--------------------------------------------------------------------
	private void initComponents() {
		tabs = new TabPane();
		tablesBySystem = new HashMap<RoleplayingSystem, TableView<RulePlugin<?>>>();
	}
	
	//--------------------------------------------------------------------
	private void initLayout() {
		setContent(tabs);
	}
	
	//--------------------------------------------------------------------
	private void initInteractivity() {
	}
	
	//--------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	private TableView<RulePlugin<?>> createTableFor(RoleplayingSystem system) {
		TableColumn<RulePlugin<?>, String> colName    = new TableColumn<RulePlugin<?>, String>(ResourceI18N.get(RES, "column.name"));
		TableColumn<RulePlugin<?>, String> colVersion = new TableColumn<RulePlugin<?>, String>(ResourceI18N.get(RES, "column.version"));
		TableColumn<RulePlugin<?>, String> colAuthor  = new TableColumn<RulePlugin<?>, String>(ResourceI18N.get(RES, "column.author"));
//		TableColumn<RulePlugin<?>, String> colIssues  = new TableColumn<RulePlugin<?>, String>(ResourceI18N.get(RES, "column.issues"));
		TableColumn<RulePlugin<?>, PluginState> colState  = new TableColumn<RulePlugin<?>, PluginState>(ResourceI18N.get(RES, "column.state"));

		colName.setCellValueFactory( new PropertyValueFactory<>("ReadableName"));
		colAuthor.setCellValueFactory( cdf -> new SimpleStringProperty(cdf.getValue().getClass().getPackage().getImplementationVendor()));
		colVersion.setCellValueFactory( cdf -> new SimpleStringProperty(cdf.getValue().getClass().getPackage().getImplementationVersion()));
//		colIssues.setCellValueFactory( cdf -> new SimpleStringProperty(cdf.getValue().getClass().getProtectionDomain().getCodeSource()+""));
		colState.setCellValueFactory( cdf -> {
			PluginDescriptor descr = CharacterProviderLoader.getPluginDescriptor(cdf.getValue());
			if (descr==null)
				return null;
			return new SimpleObjectProperty<PluginState>(descr.state);
		});
		TableView<RulePlugin<?>> table = new TableView<RulePlugin<?>>();
		table.getColumns().addAll(colName,  colVersion, colAuthor, colState);
		
		tablesBySystem.put(system, table);
		return table;
	}
	
	//--------------------------------------------------------------------
	public void refresh() {
		tabs.getTabs().clear();
		tablesBySystem.clear();
		
		/*
		 * Sort rule plugins
		 */
		List<RulePlugin<?>> plugins = new ArrayList<>(CharacterProviderLoader.getRulePlugins());
		Collections.sort(plugins, new Comparator<RulePlugin<?>>() {
			public int compare(RulePlugin<?> o1, RulePlugin<?> o2) {
				if (o1.getRules()!=o2.getRules()) {
					return Integer.compare(o1.getRules().ordinal(), o2.getRules().ordinal());
				}
				if (o1.getSupportedFeatures().contains(RulePluginFeatures.PERSISTENCE) && !o2.getSupportedFeatures().contains(RulePluginFeatures.PERSISTENCE)) return -1;
				return o1.getReadableName().compareTo(o2.getReadableName());
			}
		});
		
		
		/*
		 * Rules
		 */
		logger.info("++++++++++++++++++++++++++++++plugins");
		for (RulePlugin<?> plugin : plugins) {
			logger.info("  check plugin "+plugin);
			TableView<RulePlugin<?>> table = tablesBySystem.get(plugin.getRules());
			if (table==null) {
				table = createTableFor(plugin.getRules());
				Tab tab = new Tab(plugin.getRules().getName());
				tab.setContent(table);
				tab.setClosable(false);
				tabs.getTabs().add(tab);
			}
			table.getItems().add(plugin);
//			
//			VBox vbox = rules.get(plugin.getRules());
//			if (vbox==null) {
//				Label name = new Label(plugin.getRules().getName());
//				name.getStyleClass().add("section-head");
//				vbox = new VBox(5);		
//				vbox.getStyleClass().add("content");
//				vbox.setStyle("-fx-min-height: 5em; -fx-padding: 1em;");
//				vbox.getChildren().add(name);
//				rules.put(plugin.getRules(), vbox);
//				tiles.getChildren().add(vbox);
//			}
//			
//			// Plugin tile
//			StringBuffer features_S = new StringBuffer();
//			for (RulePluginFeatures feature : plugin.getSupportedFeatures()) {
//				features_S.append(feature.toString()+", ");
//			}
//			Package pack = plugin.getClass().getPackage();
//			System.out.println("PluginsScreen: pack="+pack+"  ImplTitle="+pack.getImplementationTitle()+"   ImplVend="+pack.getImplementationVendor()+"  src="+plugin.getClass().getProtectionDomain().getCodeSource());
//			
//			// Line 1
////			Label name = new Label(plugin.getRules().getName()+"/"+features_S);
//			Label name = new Label(plugin.getRules().getName()+"/"+plugin.getClass().getSimpleName());
//			name.getStyleClass().add("text-body");
//			if (pack.getImplementationTitle()!=null)
//				name.setText(pack.getImplementationTitle());
//			
//			Label version = new Label();
//			if (pack.getImplementationVersion()!=null)
//				version.setText(pack.getImplementationVersion());
//			version.getStyleClass().add("text-secondary-info");
//			
//			HBox line1 = new HBox(10);
//			line1.getChildren().addAll(name, version);
//			HBox.setHgrow(name, Priority.ALWAYS);
//			name.setMaxWidth(Double.MAX_VALUE);
//
//			// Line 2
//			Label author = new Label(plugin.getClass().getPackage().getName());
//			if (pack.getImplementationVendor()!=null)
//				author.setText(pack.getImplementationVendor());
//			author.getStyleClass().add("text-tertiary-info");
//			
//			Label features = new Label(features_S.toString());
//			features.getStyleClass().add("text-tertiary-info");
//			features.setAlignment(Pos.CENTER_RIGHT);
//			features.setMaxWidth(Double.MAX_VALUE);
//			
//			HBox line2 = new HBox(10);
//			line2.getChildren().addAll(author, features);
//			HBox.setHgrow(author, Priority.ALWAYS);
//			author.setMaxWidth(Double.MAX_VALUE);
//
//			// Layout
//			VBox entry = new VBox();
//			entry.getChildren().addAll(line1, line2);
//			
//			vbox.getChildren().add(entry);
		}
	}

}
