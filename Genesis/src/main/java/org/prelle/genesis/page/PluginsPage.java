package org.prelle.genesis.page;

import java.net.URL;
import java.text.DateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.genesis.Genesis5Main;
import org.prelle.javafx.CommandBar.DisplayState;
import org.prelle.javafx.ManagedScreenPage;

import de.rpgframework.PluginDescriptor;
import de.rpgframework.PluginState;
import de.rpgframework.RPGFrameworkLoader;
import de.rpgframework.ResourceI18N;
import de.rpgframework.core.RoleplayingSystem;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * @author prelle
 *
 */
public class PluginsPage extends ManagedScreenPage {

	private static ResourceBundle RES = ResourceBundle.getBundle(PluginsPage.class.getName());;
	private final static Logger logger = LogManager.getLogger("genesis");
	
	private Label lbExplain;
	private TableView<PluginDescriptor> table;
	
	private Label lbName;
	private Label lbVendor;
	private Label lbSystem;
	private Label lbVersion;
	private Label lbDate;
	private Label lbState;
	private Hyperlink lbInfoURL;
	private Hyperlink lbBugtracker;
	
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
		lbExplain= new Label(ResourceI18N.get(RES, "pluginspage.explain"));
		table = new TableView<PluginDescriptor>();
		
		lbName   = new Label();
		lbVendor = new Label();
		lbSystem = new Label();
		lbVersion= new Label();
		lbDate   = new Label();
		lbState  = new Label();
		lbInfoURL= new Hyperlink();
		lbBugtracker = new Hyperlink();
	}
	
	//--------------------------------------------------------------------
	private void initLayout() {
		lbExplain.setWrapText(true);
		
		table.setStyle("-fx-min-width: 40em");
		table.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		createColumns();
		
		Label hdName    = new Label(ResourceI18N.get(RES, "label.name"));
		Label hdVendor  = new Label(ResourceI18N.get(RES, "label.vendor"));
		Label hdSystem  = new Label(ResourceI18N.get(RES, "label.system"));
		Label hdVersion = new Label(ResourceI18N.get(RES, "label.version"));
		Label hdDate    = new Label(ResourceI18N.get(RES, "label.date"));
		Label hdState   = new Label(ResourceI18N.get(RES, "label.state"));
		Label hdInfoURL   = new Label(ResourceI18N.get(RES, "label.homepage"));
		Label hdBugtracker= new Label(ResourceI18N.get(RES, "label.bugtracker"));
		
		hdName.getStyleClass().add("base");
		hdVendor.getStyleClass().add("base");
		hdSystem.getStyleClass().add("base");
		hdVersion.getStyleClass().add("base");
		hdDate.getStyleClass().add("base");
		hdState.getStyleClass().add("base");
		hdInfoURL.getStyleClass().add("base");
		hdBugtracker.getStyleClass().add("base");
		
		GridPane details = new GridPane();
		details.setHgap(5);
		details.setVgap(10);
		details.add(hdName   , 0, 0);
		details.add(lbName   , 1, 0);
		details.add(hdVendor , 0, 1);
		details.add(lbVendor , 1, 1);
		details.add(hdSystem , 0, 2);
		details.add(lbSystem , 1, 2);
		details.add(hdVersion, 0, 3);
		details.add(lbVersion, 1, 3);
		details.add(hdDate   , 0, 4);
		details.add(lbDate   , 1, 4);
		details.add(hdState  , 0, 5);
		details.add(lbState  , 1, 5);
		details.add(hdInfoURL, 0, 6);
		details.add(lbInfoURL, 1, 6);
		details.add(hdBugtracker, 0, 7);
		details.add(lbBugtracker, 1, 7);
		
		details.setStyle("-fx-min-width: 30em");
		
		HBox masterDetail = new HBox(20, table, details);
		HBox.setHgrow(table, Priority.ALWAYS);
		HBox.setMargin(table, new Insets(10,10,0,10));
		HBox.setMargin(details, new Insets(10,10,0,10));
		
		VBox layout = new VBox(20, lbExplain, masterDetail);
		VBox.setVgrow(masterDetail, Priority.ALWAYS);
		masterDetail.setMaxHeight(Double.MAX_VALUE);
		setContent(layout);
	}
	
	//--------------------------------------------------------------------
	private void initInteractivity() {
		table.getSelectionModel().selectedItemProperty().addListener( (ov,o,n) -> {
			if (n!=null) {
				lbName.setText(n.getName());
				lbVendor.setText(n.getVendor());
				lbSystem.setText(n.system);
				lbVersion.setText(n.getVersion()+"");
				lbDate.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(n.timestamp.toEpochMilli()));
				lbState.setText(n.getState()+"");
				if (n.homepage!=null)
					lbInfoURL.setText(n.homepage+"");
				else
					lbInfoURL.setText(null);
				lbInfoURL.setUserData(n.homepage);
				if (n.bugtracker!=null)
					lbBugtracker.setText(n.bugtracker+"");
				else
					lbBugtracker.setText(null);
				lbBugtracker.setUserData(n.bugtracker);
			}
		});
		
		lbInfoURL.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent event) {
		    	logger.info("Show Homepage in external browser: "+lbInfoURL.getUserData());
		    	if ( lbInfoURL.getUserData()!=null ) {
		    		Genesis5Main.getHostServicesDelegate().showDocument(  ((URL)lbInfoURL.getUserData()).toString() );
		    	}
		    }
		});
		lbBugtracker.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent event) {
		    	logger.info("Show Homepage in external browser: "+lbBugtracker.getUserData());
		    	if ( lbBugtracker.getUserData()!=null ) {
		    		Genesis5Main.getHostServicesDelegate().showDocument(  ((URL)lbBugtracker.getUserData()).toString() );
		    	}
		    }
		});
	}
	
	//--------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	private void createColumns() {
		TableColumn<PluginDescriptor, Boolean> colLoad   = new TableColumn<PluginDescriptor, Boolean>(ResourceI18N.get(RES, "column.use"));
		TableColumn<PluginDescriptor, String> colSystem  = new TableColumn<PluginDescriptor, String>(ResourceI18N.get(RES, "column.system"));
		TableColumn<PluginDescriptor, String[]> colName  = new TableColumn<PluginDescriptor, String[]>(ResourceI18N.get(RES, "column.name"));
		TableColumn<PluginDescriptor, String> colVersion = new TableColumn<PluginDescriptor, String>(ResourceI18N.get(RES, "column.version"));
		TableColumn<PluginDescriptor, PluginState> colState  = new TableColumn<PluginDescriptor, PluginState>(ResourceI18N.get(RES, "column.state"));
		TableColumn<PluginDescriptor, Instant> colTime   = new TableColumn<PluginDescriptor, Instant>(ResourceI18N.get(RES, "column.timestamp"));

		colLoad.setCellValueFactory( cdf -> new SimpleBooleanProperty(RPGFrameworkLoader.getInstance().getPluginRegistry().getPluginLoading(cdf.getValue().uuid)));
		colName.setCellValueFactory( cdf -> {
			return new SimpleObjectProperty<String[]>(new String[] {cdf.getValue().name, cdf.getValue().vendor});
		});
		colVersion.setCellValueFactory( new PropertyValueFactory<>("Version"));
//		colAuthor.setCellValueFactory( cdf -> new SimpleStringProperty(cdf.getValue().getClass().getPackage().getImplementationVendor()));
//		colIssues.setCellValueFactory( cdf -> new SimpleStringProperty(cdf.getValue().getClass().getProtectionDomain().getCodeSource()+""));
		colState.setCellValueFactory( new PropertyValueFactory<>("state"));
		colSystem.setCellValueFactory( cdf -> {
			PluginDescriptor descr = cdf.getValue();
			if (descr.system!=null) {
				try {
					return new SimpleStringProperty(RoleplayingSystem.valueOf(descr.system).getName());
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return new SimpleStringProperty("-");
		});
		colTime.setCellValueFactory( new PropertyValueFactory<>("timestamp"));
		
		colName.setCellFactory( (table) -> new PluginNameCell());
		colLoad.setCellFactory( (table) -> new PluginLoadCell());
		
		table.getColumns().addAll(colLoad, colSystem, colName,  colVersion, colState, colTime);
		
	}
	
	//--------------------------------------------------------------------
	public void refresh() {
		table.getItems().clear();
		
		/*
		 * Sort rule plugins
		 */
		List<PluginDescriptor> plugins = new ArrayList<>(RPGFrameworkLoader.getInstance().getPluginRegistry().getKnownRemotePlugins());
		table.getItems().addAll(plugins);
		
	}

}

class PluginNameCell extends TableCell<PluginDescriptor,String[]> {
	
	private Label lbName = new Label();
	private Label lbAuthor = new Label();
	private VBox layout;
	
	public PluginNameCell() {
		layout = new VBox(5, lbName, lbAuthor);
		lbName.getStyleClass().add("base");
	}
	
	public void updateItem(String[] item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
		} else {
			lbName.setText(item[0]);
			lbAuthor.setText(item[1]);
			setGraphic(layout);
		}
		
	}
}


class PluginLoadCell extends TableCell<PluginDescriptor,Boolean> {
	
	private CheckBox cb;
	
	public PluginLoadCell() {
		cb = new CheckBox();
		cb.selectedProperty().addListener( (ov,o,n) -> {
			if (getTableRow()!=null && getTableRow().getItem()!=null)
				RPGFrameworkLoader.getInstance().getPluginRegistry().setPluginLoading(getTableRow().getItem().uuid, n);
		});
	}
	
	public void updateItem(Boolean item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
		} else {
			cb.setSelected(item);
			setGraphic(cb);
		}
		
	}
}

