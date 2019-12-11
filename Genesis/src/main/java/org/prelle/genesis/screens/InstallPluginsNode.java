/**
 * 
 */
package org.prelle.genesis.screens;

import java.util.ResourceBundle;

import de.rpgframework.ResourceI18N;
import de.rpgframework.character.PluginDescriptor;
import de.rpgframework.character.PluginRegistry;
import de.rpgframework.core.RoleplayingSystem;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * @author prelle
 *
 */
public class InstallPluginsNode extends VBox {

	private static ResourceBundle RES = ResourceBundle.getBundle(InstallPluginsNode.class.getName());;
	
	private Label lbExplain;
	private ListView<PluginDescriptor> lvPlugins;
	
	//-------------------------------------------------------------------
	public InstallPluginsNode() {
		initComponents();
		initLayout();
		initInteractivity();
		
		lvPlugins.getItems().addAll(PluginRegistry.getKnownPlugins());
	}
	
	//-------------------------------------------------------------------
	private void initComponents() {
		lbExplain = new Label(ResourceI18N.get(RES, "text.explain"));
		lbExplain.setWrapText(true);
		lvPlugins = new ListView<PluginDescriptor>();
		lvPlugins.setCellFactory( list -> new InstallPluginListCell());
	}
	
	//-------------------------------------------------------------------
	private void initLayout() {
		getChildren().addAll(lbExplain, lvPlugins);
		setSpacing(20);
	}
	
	//-------------------------------------------------------------------
	private void initInteractivity() {
	}
	
}

class InstallPluginListCell extends ListCell<PluginDescriptor> {
	
	private CheckBox cbSelected;
	private Label lbName, lbAuthor, lbRules, lbState, lbVersion, lbDate;
	private GridPane grid;
	
	//-------------------------------------------------------------------
	public InstallPluginListCell() {
		 cbSelected = new CheckBox();
		 lbName  = new Label();
		 lbAuthor= new Label();
		 lbRules = new Label();
		 lbVersion = new Label();
		 lbState = new Label();
		 lbDate = new Label();
		 
		 lbName.setStyle("-fx-font-weight: bold");
		 
		 grid = new GridPane();
		 grid.setStyle("-fx-hgap: 0.5em");
		 grid.add(cbSelected, 0, 0, 1,2);
		 grid.add(lbName  , 1, 0);
		 grid.add(lbAuthor, 1, 1);
		 grid.add(lbRules , 2, 0);
		 grid.add(lbState , 2, 1);
		 grid.add(lbVersion, 3, 0);
		 grid.add(lbDate   , 3, 1);
		 
		 grid.getColumnConstraints().add(new ColumnConstraints(30));
		 grid.getColumnConstraints().add(new ColumnConstraints(200));
		 grid.getColumnConstraints().add(new ColumnConstraints(100));
		 
		 cbSelected.selectedProperty().addListener( (ov,o,n) -> {
			 PluginRegistry.setPluginLoading(getItem().uuid, n);
		 });
	}
	
	//-------------------------------------------------------------------
	public void updateItem(PluginDescriptor item, boolean empty) {
		super.updateItem(item, empty);
		
		if (empty) {
			setGraphic(null);
		} else {
			setGraphic(grid);
			
			cbSelected.setSelected(PluginRegistry.getPluginLoading(item.uuid));
			lbName.setText(item.getName());
			lbAuthor.setText(item.getVendor());
			lbState.setText(item.getState()+"");
			if (item.system!=null)
				lbRules.setText(RoleplayingSystem.valueOf(item.system).getName());
			lbVersion.setText(item.getVersion());
			lbVersion.setText(item.timestamp+"");
		}
	}	

}
