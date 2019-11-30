package org.prelle.genesis.page;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.prelle.genesis.ExternalTools;
import org.prelle.javafx.AlertType;
import org.prelle.javafx.CloseType;
import org.prelle.javafx.CommandBar.DisplayState;
import org.prelle.javafx.FontIcon;
import org.prelle.javafx.ManagedScreenPage;

import de.rpgframework.core.RoleplayingSystem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * @author prelle
 *
 */
public class ExternalToolsPage extends ManagedScreenPage {
	
	private static ResourceBundle RES = ResourceBundle.getBundle("org/prelle/genesis/i18n/genesis");
	
	private ExternalTools external;
	
	private HBox content;
	
	private VBox listPane;
	private MenuItem    btnAdd;

	//-------------------------------------------------------------------
	/**
	 */
	public ExternalToolsPage(ExternalTools external) {
		super(RES.getString("screen.externaltools.heading"));
		this.external = external;
		if (external==null)
			throw new NullPointerException("external");
		initComponents();
		initLayout();
		initInteractivity();
		getCommandBar().setDisplayState(DisplayState.HIDDEN);
		
		refresh();
	}
	
	//--------------------------------------------------------------------
	private void initComponents() {

		FontIcon iconAdd = new FontIcon("\uE17E\uE109");

		btnAdd = new MenuItem(null, iconAdd);
		btnAdd.getStyleClass().add("bordered");
		
		getCommandBar().getPrimaryCommands().add(btnAdd);
//		getStaticButtons().add(btnAdd);
	}
	
	//--------------------------------------------------------------------
	private void initLayout() {
		FontIcon icon = new FontIcon("\uE17D");

		Label description = new Label(RES.getString("screen.externaltools.descr"));
		description.setWrapText(true);
		
		VBox descrPane = new VBox();
		descrPane.setMaxHeight(Double.MAX_VALUE);
		descrPane.getStyleClass().add("description-pane");
		descrPane.getChildren().addAll(icon, description);
		descrPane.setAlignment(Pos.TOP_CENTER);
		descrPane.setStyle("-fx-min-width: 20em; -fx-pref-width: 20em; -fx-padding: 1em 3em 1em 1em; -fx-spacing: 1em;");

		listPane = new VBox();
		listPane.setStyle("-fx-spacing: 2em");
		ScrollPane scroll = new ScrollPane(listPane);
		
		content = new HBox();
		content.getChildren().addAll(descrPane, scroll);
		setContent(content);
	}
	
	//--------------------------------------------------------------------
	private void initInteractivity() {
		btnAdd.setOnAction(event -> {
			Label lblRules = new Label(RES.getString("screen.externaltools.system"));
			Label lblPath  = new Label(RES.getString("screen.externaltools.path"));
			lblRules.getStyleClass().add("text-small-subheader");
			lblPath .getStyleClass().add("text-small-subheader");
			Button btnSelect = new Button(RES.getString("button.select"));
			btnSelect.getStyleClass().add("bordered");
			ChoiceBox<RoleplayingSystem> cbRules = new ChoiceBox<>();
			cbRules.setConverter(new StringConverter<RoleplayingSystem>() {
				public String toString(RoleplayingSystem val) { return val.getName(); }
				public RoleplayingSystem fromString(String val) { return null; }
			});
			cbRules.getItems().addAll(RoleplayingSystem.values());
			cbRules.getItems().remove(RoleplayingSystem.ALL);
			Collections.sort(cbRules.getItems(), new Comparator<RoleplayingSystem>() {
				public int compare(RoleplayingSystem o1, RoleplayingSystem o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			
			TextField tfPath = new TextField();
			tfPath.setStyle("-fx-pref-width: 40em");
			HBox pathLine = new HBox(20);
			pathLine.getChildren().addAll(tfPath, btnSelect);
			
			// Select button
			btnSelect.setOnAction(event2 -> {
				Path path = chooseExecutable();
				if (path!=null) 
					tfPath.setText(path.toString());
			});
			
			VBox content = new VBox(5);
			content.getChildren().addAll(lblRules, cbRules, lblPath, pathLine);
			VBox.setMargin(lblPath, new Insets(20, 0, 0, 0));
			
			CloseType result = getManager().showAlertAndCall(AlertType.QUESTION, "", content);
			if (result==CloseType.OK || result==CloseType.YES) {
				Path path = FileSystems.getDefault().getPath(tfPath.getText());
				external.setTool(cbRules.getValue(), path);
				refresh();
			}
		});
	}
	
	//--------------------------------------------------------------------
	public Path chooseExecutable() {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("EXE", "*.exe"),
                new FileChooser.ExtensionFilter("ALL", "*")
            );
		File selection = chooser.showOpenDialog(new Stage());
		if (selection!=null) {
			return selection.toPath();
		}
		return null;
	}
	
	//--------------------------------------------------------------------
	private void refresh() {
		listPane.getChildren().clear();
		
		Map<RoleplayingSystem, Path> pathes = external.getConfiguredPathes();
		List<RoleplayingSystem> keys = new ArrayList<RoleplayingSystem>(pathes.keySet());
		Collections.sort(keys, new Comparator<RoleplayingSystem>() {
			@Override
			public int compare(RoleplayingSystem o1, RoleplayingSystem o2) {
				return Collator.getInstance().compare(o1.getName(), o2.getName());
			}
		});
		for (RoleplayingSystem rules : keys ) {
			Path path = pathes.get(rules);
			
			Label lblSystem = new Label(rules.getName());
			Label lblPath   = new Label(RES.getString("screen.externaltools.path"));
			lblPath.getStyleClass().add("text-small-subheader");
			lblSystem .setStyle("-fx-font-weight: bold; -fx-font-size: 150%"); 
			TextField tfPath = new TextField(path.toString());
			tfPath.setStyle("-fx-pref-width: 40em");
			tfPath.setPromptText(RES.getString("screen.externaltools.path.prompt"));
			Button btnSelect = new Button(RES.getString("button.select"));
			btnSelect.getStyleClass().add("bordered");
			btnSelect.setOnAction(event -> {
				Path path2 = chooseExecutable();
				if (path2!=null) {
					external.setTool(rules, path2);
					pathes.put(rules, path2);
					tfPath.setText(path2.toString());
				}
			});
			
			HBox line2 = new HBox();
			line2.getChildren().addAll(lblPath, tfPath, btnSelect);
			
			VBox bxLines = new VBox();
			bxLines.setStyle("-fx-spacing: 1em");
			bxLines.getChildren().addAll(lblSystem, line2);
			
			FontIcon icon = new FontIcon("\uE107");
			Button btnTrash = new Button(null,icon);
			btnTrash.setOnAction(event3 -> {
				external.unsetTool(rules);
				refresh();
			});
			
			HBox cell = new HBox(10);
			cell.getChildren().addAll(bxLines, btnTrash);
			cell.setStyle("-fx-background-color: derive(white, -8%)");
			
			listPane.getChildren().add(cell);
		}
	}

}
