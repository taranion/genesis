package org.prelle.genesis.page;

import java.text.DateFormat;
import java.util.Date;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.genesis.Constants;
import org.prelle.javafx.AlertType;
import org.prelle.javafx.CloseType;
import org.prelle.javafx.ScreenManagerProvider;
import org.prelle.rpgframework.jfx.ListSection;

import de.rpgframework.RPGFrameworkLoader;
import de.rpgframework.core.CommandBus;
import de.rpgframework.core.CommandResult;
import de.rpgframework.core.CommandType;
import de.rpgframework.core.License;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * @author Stefan Prelle
 *
 */
public class LicenseListSection extends ListSection<License> {

	private final static Logger logger = LogManager.getLogger("gui");

	private static ResourceBundle GUICOMMON = Constants.RES;

	//-------------------------------------------------------------------
	public LicenseListSection(String title, ScreenManagerProvider provider) {
		super(title, provider, (PropertyResourceBundle) Constants.RES);
		super.list.setCellFactory(lv -> new LicenseCell());
		setAddButton(null);
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.rpgframework.jfx.ListSection#onAdd()
	 */
	@Override
	protected void onAdd() {
		// TODO Auto-generated method stub
		
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.rpgframework.jfx.ListSection#onDelete()
	 */
	@Override
	protected void onDelete() {
		License lic = super.list.getSelectionModel().getSelectedItem();
		logger.info("onDelete "+lic);
		CloseType answer = provider.getScreenManager().showAlertAndCall(AlertType.QUESTION,
				GUICOMMON.getString("licensescreen.removeQuestion.head"),
				GUICOMMON.getString("licensescreen.removeQuestion.desc"));
		if (answer!=CloseType.OK)
			return;

		CommandResult result = CommandBus.fireCommand(this, CommandType.LICENSE_REMOVE, lic);
		if (result.wasSuccessful()) {
			logger.info("License removed: "+result.getReturnValue());
			if (((boolean)result.getReturnValue())==true) {
				list.getItems().remove(lic);
				return;
			}
		}

		provider.getScreenManager().showAlertAndCall(AlertType.ERROR,
				GUICOMMON.getString("licensescreen.removeFailed.head"),
				GUICOMMON.getString("licensescreen.removeFailed.desc")+result.getMessage());
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

}

class LicenseCell extends ListCell<License> {

	private static ResourceBundle GUICOMMON = Constants.RES;

	private final static DateFormat FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);
	
	private Parent layout;
	private Label rules, licensedTo, validity, country;
	
	public LicenseCell() {
		rules  = new Label();
		rules.getStyleClass().add("title");
		licensedTo  = new Label();
		licensedTo.getStyleClass().add("text-secondary-info");
		validity  = new Label();
		validity.getStyleClass().add("text-tertiary-info");
		country  = new Label();
		country.getStyleClass().addAll("title","symbol-icon");
		country.setStyle("-fx-font-family: 'Segoe UI Symbol'");
		VBox box = new VBox(rules,licensedTo,validity);
		layout = new StackPane(country, box);
		StackPane.setAlignment(box, Pos.CENTER_LEFT);
		StackPane.setAlignment(country, Pos.CENTER_RIGHT);
	}
	
	public void updateItem(License item, boolean empty) {
		super.updateItem(item, empty);
		
		if (empty) {
			setGraphic(null);
		} else {
			String stateText = null;
			// Check validity
			boolean valid = RPGFrameworkLoader.getInstance().getLicenseManager().hasLicense(item.getSystem(), item.getValue());
			if (item.getUntil()>0)
				stateText = GUICOMMON.getString("label.license.validUntil")+" "+FORMAT.format(new Date(item.getUntil()));
			else
				stateText = GUICOMMON.getString("label.license.validSince")+" "+FORMAT.format(new Date(item.getFrom()));
			if (!valid)
				stateText = GUICOMMON.getString("label.license.invalid");

			String flag = null;
			if ("de".equals(item.getLanguage()) || item.getLanguage()==null) {
				int[] codepoints = {0x1F1E9, 0x1F1EA};
				flag = new String(codepoints, 0, codepoints.length);
			} else if ("en".equals(item.getLanguage())) {
				int[] codepoints = {0x1F1EC, 0x1F1E7};
				flag = new String(codepoints, 0, codepoints.length);
			}
			country.setText(flag);
			rules.setText(item.getSystem().getName());
			licensedTo.setText(item.getName()+" ("+item.getMail()+")");
			validity.setText(stateText);
			if (valid) {
				validity.setStyle("-fx-text-fill: #008000;");
			} else {
				validity.setStyle("-fx-text-fill: #800000;");
			}
			
			setGraphic(layout);
		}
	}
}
