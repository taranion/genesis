/**
 *
 */
package org.prelle.genesis.page;

import de.rpgframework.RPGFrameworkLoader;
import de.rpgframework.ResourceI18N;
import de.rpgframework.core.ActivationKey;
import de.rpgframework.core.CommandBus;
import de.rpgframework.core.CommandResult;
import de.rpgframework.core.CommandType;
import de.rpgframework.core.License;
import de.rpgframework.core.RoleplayingSystem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.javafx.AlertType;
import org.prelle.javafx.CloseType;
import org.prelle.javafx.CommandBar.DisplayState;
import org.prelle.javafx.FontIcon;
import org.prelle.javafx.ManagedScreenPage;
import org.prelle.javafx.NavigButtonControl;
import org.prelle.javafx.ScreenManager;
import org.prelle.javafx.ScreenManagerProvider;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author prelle
 *
 */
public class LicensePage extends ManagedScreenPage {

	private class DialogReturn {
		CloseType closed;
		String name;
		String mail;
		public DialogReturn(CloseType closed, String name, String mail) {
			this.closed = closed;
			this.mail   = mail;
			this.name   = name;
		}
	}

	private static ResourceBundle GUICOMMON = ResourceBundle.getBundle(LicensePage.class.getName());;

	private final static Logger logger = LogManager.getLogger("gui");
	private final static DateFormat FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);

	private HBox content;

	private TextField tfKeyInput;
	private Button    btnAdd;
	private LicenseListSection section;

	//-------------------------------------------------------------------
	/**
	 */
	public LicensePage() {
		super(ResourceI18N.get(GUICOMMON,"licensescreen.heading"));

		initComponents();
		initLayout();
		initInteractivity();
		setData();
		getCommandBar().setDisplayState(DisplayState.HIDDEN);
	}

	//--------------------------------------------------------------------
	private void initComponents() {
		btnAdd = new Button(ResourceI18N.get(GUICOMMON,"button.add"));
		btnAdd.getStyleClass().add("bordered");
		tfKeyInput = new TextField();
		tfKeyInput.setPromptText(ResourceI18N.get(GUICOMMON,"licensescreen.placeholder.add"));

		section = new LicenseListSection(ResourceI18N.get(GUICOMMON,"section.licenses"), new ScreenManagerProvider() {
			@Override
			public ScreenManager getScreenManager() {
				return getManager();
			}
		});
	}

	//--------------------------------------------------------------------
	private void initLayout() {
		FontIcon icon = new FontIcon("\uE192");
		icon.setStyle("-fx-font-size: 300%");
		//		ImageView image = new ImageView(new Image(ClassLoader.getSystemResourceAsStream("images/light/puzzle-100.png")));
		//		image.setFitHeight(192);
		//		image.setFitWidth(192);

		Label description = new Label(ResourceI18N.get(GUICOMMON,"licensescreen.descr"));
		description.setWrapText(true);

		Region spacing = new Region();
		spacing.setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(spacing, Priority.ALWAYS);
		Label lblTrash = new Label(ResourceI18N.get(GUICOMMON,"licensescreen.trash"));
		lblTrash.setWrapText(true);
		lblTrash.setAlignment(Pos.CENTER);

		VBox descrPane = new VBox();
		descrPane.setMaxHeight(Double.MAX_VALUE);
		descrPane.getStyleClass().add("description-pane");
		descrPane.getChildren().addAll(icon, description, spacing);
		descrPane.setAlignment(Pos.TOP_CENTER);
		descrPane.setStyle("-fx-min-width: 20em; -fx-pref-width: 20em; -fx-padding: 1em 3em 1em 1em; -fx-spacing: 1em;");

		tfKeyInput.setPrefColumnCount(50);
		tfKeyInput.setMinHeight(50);
		tfKeyInput.setPrefHeight(70);
		HBox line = new HBox(20);
		line.getChildren().addAll(tfKeyInput, btnAdd);
		line.setAlignment(Pos.TOP_LEFT);

		VBox contentPane = new VBox(40);
//		contentPane.getChildren().addAll(line, scroll);
		contentPane.getChildren().addAll(line, section);

		content = new HBox();
		content.getChildren().addAll(descrPane, contentPane);
		setContent(content);
	}

	//--------------------------------------------------------------------
	private Object detectLicenseKeyType(String val) {
		logger.debug("Trying to detect license type");
		CommandResult result = CommandBus.fireCommand(this, CommandType.LICENSE_TYPE_DETECT, val);
		if (result.wasSuccessful()) {
			logger.info("License type detected: "+result.getReturnValue());
			return result.getReturnValue();
		} else {
			logger.warn("Failed detecting license: "+result.getMessage());
			if (result.wasProcessed())
				return result.getMessage();
			return null;
		}
	}

	//--------------------------------------------------------------------
	private void initInteractivity() {
		btnAdd.setOnAction(event -> {
			String val = tfKeyInput.getText();
			tfKeyInput.setText(null);
			if (val==null) {
				getManager().showAlertAndCall(AlertType.ERROR, "Nothing entered", "Hey, you forgot to enter the license or activation key.");
				return;
			}
			if ("572DFE98-046B-4C41-9443-17C8C9C2CBB3".equals(val.trim())) {
				getManager().showAlertAndCall(AlertType.NOTIFICATION, "Nicht dein Ernst, oder?", "So, jetzt schlagen wir bitte einmal mit dem Kopf auf die Tischplatte. \nDu hast den Beispielcode aus der bebilderten Erklär-Anleitung abgetippt.\nNatürlich musst Du da den Code eintippen, den Du zugeschickt bekommen hast.");
				return;
			}
			Object detected = detectLicenseKeyType(val);
			logger.debug("Detected = "+detected);
			if (detected==null) {
				logger.error("Failed detecting key type of: "+val);
				getManager().showAlertAndCall(AlertType.ERROR, "License verification failed", "Entweder ist der Lizenzserver abgestürzt, oder Genesis läuft nicht in der richtigen Sprache für die Lizenz.");
				return;
			}
			logger.debug("Detected = "+detected.getClass());
			if (detected instanceof String) {
				logger.error("Failed detecting key type of: "+val);
				if (String.valueOf(detected).equals("Cannot detect type of license."))
					getManager().showAlertAndCall(AlertType.ERROR, (String)detected, "The data you entered is neither an activation key, nor a license key.\nCheck that the entered string does not contain any line breaks.");
				else
					getManager().showAlertAndCall(AlertType.ERROR, (String)detected, "The process failed. The message was: "+detected+"\nPlease verify the entered data.\nYou can contact license@rpgframework.de for help.");
				return;
			}
			logger.debug("Detected2 = "+(detected instanceof ActivationKey));
			logger.debug("Detected3 = "+(ActivationKey.class.isAssignableFrom(detected.getClass())));
			logger.debug("Interfaces = "+Arrays.asList(detected.getClass().getInterfaces()));
			logger.debug("Superclass = "+detected.getClass().getSuperclass());
			logger.debug("Superclass = "+Arrays.asList(detected.getClass().getInterfaces()).contains(ActivationKey.class));
			if (detected instanceof ActivationKey) {
				logger.debug("handle activation key");
				DialogReturn dResult = presentActivationDialog((ActivationKey) detected);
				if (dResult.closed==CloseType.CANCEL) {
					logger.warn("Activation dialog was cancelled");
					return;
				} else {
					// Name and mail entered, generate license
					CommandResult result = CommandBus.fireCommand(this, CommandType.LICENSE_CREATE, val, (ActivationKey)detected, dResult.name, dResult.mail);
					if (result.wasSuccessful()) {
						Object[] lResult = (Object[]) result.getReturnValue();
						int success = (Integer)lResult[0];
						if (success==0)
							getManager().showAlertAndCall(AlertType.NOTIFICATION,
									GUICOMMON.getString("dialog.afteractivation.title"),
									GUICOMMON.getString("dialog.afteractivation.desc"));
						else
							getManager().showAlertAndCall(AlertType.NOTIFICATION,
									GUICOMMON.getString("dialog.afterreactivation.title"),
									GUICOMMON.getString("dialog.afterreactivation.desc"));
					} else {
						getManager().showAlertAndCall(AlertType.NOTIFICATION,
								GUICOMMON.getString("dialog.failedactivation.title"),
								String.format(GUICOMMON.getString("dialog.failedactivation.desc"), result.getMessage()));

					}
				}
			} else if (detected instanceof License) {
				logger.debug("Trying to add license "+val);
				CommandResult result = CommandBus.fireCommand(this, CommandType.LICENSE_ADD, val);
				if (result.wasSuccessful()) {
					logger.info("License added: "+result.getReturnValue());
					setData();
//					addLicense((License) result.getReturnValue());
				} else {
					logger.warn("Failed adding license: "+result.getMessage());
				}
			} else {
				logger.error("Failed detecting key type of: "+val);
				getManager().showAlertAndCall(AlertType.ERROR, "Failed detecting key type", "Key seems to be neither an activation nor license key");
			}
		});
	}

	//--------------------------------------------------------------------
	private void setData() {
		List<License> sectionData = new ArrayList<>();
		
		// Build a map with lists of all licenses, indexed by rules
		Map<RoleplayingSystem, List<License>> licensesByRules =
				new HashMap<RoleplayingSystem, List<License>>();
		for (RoleplayingSystem rules : RoleplayingSystem.values()) {
			List<License> list = RPGFrameworkLoader.getInstance().getLicenseManager().getLicenses(rules);
			// Only add list when it has at least one entry
			if (list.isEmpty())
				logger.debug("No licenses for "+rules);
			else {
				logger.debug("Found licenses for "+rules+": "+list);
				licensesByRules.put(rules, list);
				sectionData.addAll(list);
			}
		}
		section.setData(sectionData);
	}

//	//-------------------------------------------------------------------
//	private void performRemove(License lic) {
//		CloseType answer = getManager().showAlertAndCall(AlertType.QUESTION,
//				GUICOMMON.getString("licensescreen.removeQuestion.head"),
//				GUICOMMON.getString("licensescreen.removeQuestion.desc"));
//		if (answer!=CloseType.OK)
//			return;
//
//		CommandResult result = CommandBus.fireCommand(this, CommandType.LICENSE_REMOVE, lic);
//		if (result.wasSuccessful()) {
//			logger.info("License removed: "+result.getReturnValue());
//			if (((boolean)result.getReturnValue())==true) {
//				removeLicense(lic);
//				return;
//			}
//		}
//
//		getManager().showAlertAndCall(AlertType.ERROR,
//				GUICOMMON.getString("licensescreen.removeFailed.head"),
//				GUICOMMON.getString("licensescreen.removeFailed.desc")+result.getMessage());
//	}

	//--------------------------------------------------------------------
	private DialogReturn presentActivationDialog(ActivationKey key) {
		logger.debug("presentActivationDialog()");

		Label desc    = new Label(ResourceI18N.get(GUICOMMON,"dialog.activation.desc"));
		desc.setWrapText(true);
		Label lblName = new Label(ResourceI18N.get(GUICOMMON,"label.activation.name"));
		Label lblMail = new Label(ResourceI18N.get(GUICOMMON,"label.activation.mail"));
		Label lblMailRepeat = new Label(ResourceI18N.get(GUICOMMON,"label.activation.mailrepeat"));
		TextField tfName = new TextField();
		TextField tfMail = new TextField();
		TextField tfMailRepeat = new TextField();
		tfName.setPromptText(ResourceI18N.get(GUICOMMON,"prompt.activation.name"));
		tfMail.setPromptText(ResourceI18N.get(GUICOMMON,"prompt.activation.mail"));
		tfMailRepeat.setPromptText(ResourceI18N.get(GUICOMMON,"prompt.activation.mail"));
		VBox layout = new VBox(5);
		layout.getChildren().addAll(desc, lblName, tfName, lblMail, tfMail, lblMailRepeat, tfMailRepeat);
		VBox.setMargin(lblName, new Insets(10, 0, 0, 0));
		VBox.setMargin(lblMail, new Insets(10, 0, 0, 0));
		VBox.setMargin(lblMailRepeat, new Insets(10, 0, 0, 0));

		NavigButtonControl control = new NavigButtonControl();
		tfName.textProperty().addListener((ov, o, n) -> control.setDisabled(CloseType.OK, !inputValid(tfName.getText(), tfMail.getText(), tfMailRepeat.getText())));
		tfMail.textProperty().addListener((ov, o, n) -> control.setDisabled(CloseType.OK, !inputValid(tfName.getText(), tfMail.getText(), tfMailRepeat.getText())));
		tfMailRepeat.textProperty().addListener((ov, o, n) -> control.setDisabled(CloseType.OK, !inputValid(tfName.getText(), tfMail.getText(), tfMailRepeat.getText())));

		CloseType closed = getManager().showAlertAndCall(AlertType.QUESTION, ResourceI18N.get(GUICOMMON,"dialog.activation.title"), layout, control);
		return new DialogReturn(closed, tfName.getText(), tfMail.getText());
	}

	private boolean inputValid(String name, String email, String emailRepeat){
		if (name == null || email == null || emailRepeat == null) {
			return false;
		}
		if (name.length() <= 1 || email.length() <=1 || emailRepeat.length() <= 1) {
			return false;
		}
		return email.equals(emailRepeat);
	}

}
