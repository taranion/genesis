/**
 * 
 */
package org.prelle.genesis.jfx.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.prelle.genesis.Constants;
import org.prelle.javafx.CommandBar.DisplayState;
import org.prelle.javafx.FontIcon;
import org.prelle.javafx.ManagedScreenPage;

import de.rpgframework.ResourceI18N;
import de.rpgframework.character.CharacterProviderLoader;
import de.rpgframework.character.RulePlugin;
import de.rpgframework.character.RulePluginFeatures;
import de.rpgframework.core.RoleplayingSystem;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * @author prelle
 *
 */
public class PluginsPage extends ManagedScreenPage {
	
	private static ResourceBundle GUICOMMON = Constants.RES;
	
	private HBox content;
	
	private Map<RoleplayingSystem, VBox> rules;
	private VBox frameworkPlugins;
	private FlowPane tiles;

	//-------------------------------------------------------------------
	/**
	 */
	public PluginsPage() {
		super(ResourceI18N.get(GUICOMMON,"pluginsscreen.heading"));
		initComponents();
		initLayout();
		initInteractivity();
		setData();
		getCommandBar().setDisplayState(DisplayState.HIDDEN);
	}
	
	//--------------------------------------------------------------------
	private void initComponents() {
		Label lblRules = new Label(ResourceI18N.get(GUICOMMON,"label.rules"));
		Label lblOnline = new Label(ResourceI18N.get(GUICOMMON,"label.onlineservices"));
		lblRules.getStyleClass().add("section-head");
		lblOnline.getStyleClass().add("section-head");
		
		frameworkPlugins = new VBox();
		frameworkPlugins.getStyleClass().add("content");
		frameworkPlugins.setStyle("-fx-min-height: 5em; -fx-padding: 1em;");
		frameworkPlugins.getChildren().add(lblOnline);
		
		rules = new HashMap<>();
		tiles = new FlowPane(Orientation.HORIZONTAL);
	}
	
	//--------------------------------------------------------------------
	private void initLayout() {
		FontIcon icon = new FontIcon("\uE141");
		icon.setStyle("-fx-font-size: 300%");

		Label description = new Label(ResourceI18N.get(GUICOMMON,"pluginsscreen.descr"));
		description.setWrapText(true);

		VBox descrPane = new VBox();
		descrPane.getStyleClass().add("description-pane");
		descrPane.getChildren().addAll(icon, description);
		descrPane.setAlignment(Pos.TOP_CENTER);
		descrPane.setStyle("-fx-min-width: 20em; -fx-pref-width: 20em; -fx-padding: 1em 3em 1em 1em; -fx-spacing: 1em;");

		tiles.setVgap(20);
		tiles.setHgap(40);

		frameworkPlugins.setStyle("-fx-min-width: 20em; -fx-pref-width: 25em;");

		HBox bxPlugins = new HBox(20);
		bxPlugins.getChildren().addAll(frameworkPlugins, tiles);
		
		ScrollPane scrollTiles = new ScrollPane(bxPlugins);
		scrollTiles.setFitToWidth(true);
		widthProperty().addListener((ov,o,n) -> {
			scrollTiles.setPrefViewportWidth( ((Double)n) - 20 - descrPane.getWidth());
		}); 
		
		content = new HBox(20);
		content.getChildren().addAll(descrPane, scrollTiles);
		setContent(content);
	}
	
	//--------------------------------------------------------------------
	private void initInteractivity() {
	}
	
	//--------------------------------------------------------------------
	private void setData() {
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
		for (RulePlugin<?> plugin : plugins) {
			VBox vbox = rules.get(plugin.getRules());
			if (vbox==null) {
				Label name = new Label(plugin.getRules().getName());
				name.getStyleClass().add("section-head");
				vbox = new VBox(5);		
				vbox.getStyleClass().add("content");
				vbox.setStyle("-fx-min-height: 5em; -fx-padding: 1em;");
				vbox.getChildren().add(name);
				rules.put(plugin.getRules(), vbox);
				tiles.getChildren().add(vbox);
			}
			
			// Plugin tile
			StringBuffer features_S = new StringBuffer();
			for (RulePluginFeatures feature : plugin.getSupportedFeatures()) {
				features_S.append(feature.toString()+", ");
			}
			Package pack = plugin.getClass().getPackage();
			System.out.println("PluginsScreen: pack="+pack+"  ImplTitle="+pack.getImplementationTitle()+"   ImplVend="+pack.getImplementationVendor()+"  src="+plugin.getClass().getProtectionDomain().getCodeSource());
			
			// Line 1
//			Label name = new Label(plugin.getRules().getName()+"/"+features_S);
			Label name = new Label(plugin.getRules().getName()+"/"+plugin.getClass().getSimpleName());
			name.getStyleClass().add("text-body");
			if (pack.getImplementationTitle()!=null)
				name.setText(pack.getImplementationTitle());
			
			Label version = new Label();
			if (pack.getImplementationVersion()!=null)
				version.setText(pack.getImplementationVersion());
			version.getStyleClass().add("text-secondary-info");
			
			HBox line1 = new HBox(10);
			line1.getChildren().addAll(name, version);
			HBox.setHgrow(name, Priority.ALWAYS);
			name.setMaxWidth(Double.MAX_VALUE);

			// Line 2
			Label author = new Label(plugin.getClass().getPackage().getName());
			if (pack.getImplementationVendor()!=null)
				author.setText(pack.getImplementationVendor());
			author.getStyleClass().add("text-tertiary-info");
			
			Label features = new Label(features_S.toString());
			features.getStyleClass().add("text-tertiary-info");
			features.setAlignment(Pos.CENTER_RIGHT);
			features.setMaxWidth(Double.MAX_VALUE);
			
			HBox line2 = new HBox(10);
			line2.getChildren().addAll(author, features);
			HBox.setHgrow(author, Priority.ALWAYS);
			author.setMaxWidth(Double.MAX_VALUE);

			// Layout
			VBox entry = new VBox();
			entry.getChildren().addAll(line1, line2);
			
			vbox.getChildren().add(entry);
		}
		
		
		/*
		 * OnlineServices
		 */
		boolean found = false;
//		if (RPGFrameworkLoader.getInstance().getSocialNetworkProvider()!=null) {
//			for (OnlineService plugin : RPGFrameworkLoader.getInstance().getSocialNetworkProvider().getOnlineServices()) {
//				found = true;
//				Label name = new Label(plugin.getName());
//				StringBuffer features_S = new StringBuffer();
//				for (Feature feature : plugin.getFeatures()) {
//					features_S.append(feature.toString()+", ");
//				}
//				Label features = new Label(features_S.toString());
//				features.getStyleClass().add("label-note");
//				features.setAlignment(Pos.CENTER_RIGHT);
//				features.setMaxWidth(Double.MAX_VALUE);
//
//				VBox entry = new VBox();
//				entry.getChildren().addAll(name, features);
//
//				frameworkPlugins.getChildren().add(entry);
//			}
//		}
		if (!found) {
			Label placeholder = new Label(ResourceI18N.get(GUICOMMON,"pluginsscreen.placeholder.onlineservices"));
			placeholder.getStyleClass().add("text-small-secondary");
			placeholder.setWrapText(true);
			frameworkPlugins.getChildren().add(placeholder);
		}
		
		
	}

}
