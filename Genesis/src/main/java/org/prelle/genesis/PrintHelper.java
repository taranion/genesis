/**
 *
 */
package org.prelle.genesis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.prelle.genesis.print.PrintTemplateEditorScreen;
import org.prelle.javafx.AlertType;
import org.prelle.javafx.CloseType;
import org.prelle.javafx.ScreenManager;

import de.rpgframework.ConfigOption;
import de.rpgframework.ResourceI18N;
import de.rpgframework.character.Attachment;
import de.rpgframework.character.CharacterHandle;
import de.rpgframework.character.CharacterHandle.Format;
import de.rpgframework.character.CharacterHandle.Type;
import de.rpgframework.character.CharacterProvider;
import de.rpgframework.character.CharacterProviderLoader;
import de.rpgframework.character.RuleSpecificCharacterObject;
import de.rpgframework.core.CommandBus;
import de.rpgframework.core.CommandResult;
import de.rpgframework.core.CommandType;
import de.rpgframework.core.RoleplayingSystem;
import de.rpgframework.print.LayoutGrid;
import de.rpgframework.print.PDFPrintElement;
import de.rpgframework.print.PrintManagerLoader;
import de.rpgframework.print.PrintTemplate;
import de.rpgframework.print.PrintType;
import de.rpgframework.print.TemplateFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;

/**
 * @author prelle
 *
 */
@SuppressWarnings({ "exports" })
public class PrintHelper {

	private final static Logger logger = LogManager.getLogger("genesis");
	private final static ResourceBundle RES = ResourceBundle.getBundle(PrintHelper.class.getName());

	//-------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void printClicked(CharacterHandle selected, ScreenManager manager) {
		logger.debug("print clicked");

		/*
		 * Find plugins that are willing to process this command
		 */
		List<String> plugins = CommandBus.findCommandProcessors(null, CommandType.PRINT_GET_OPTIONS,
				selected.getRuleIdentifier(),
				selected.getCharacter(),
				manager.getScene().getWindow(),
				manager
				);
		logger.info("Willing print plugins: "+plugins);

		/*
		 * Now ask all plugins for their templates (if there are any)
		 */
		Map<String, CommandResult> templResults = new HashMap<>();
		for (String plugin : plugins) {
			CommandResult result = CommandBus.fireCommand(plugin,  (Object)null, CommandType.PRINT_GET_ELEMENTS,
					selected.getRuleIdentifier(),
					selected.getCharacter(),
					manager.getScene().getWindow(),
					manager
					);
			if (result.wasProcessed() && result.wasSuccessful()) {
				logger.debug("Plugin "+plugin+" responded "+result);
				Object[] retVals = (Object[]) result.getReturnValue();
				logger.debug("  Returned  "+Arrays.toString(retVals));
				//				Collection<PrintType>      types = (Collection<PrintType>)retVals[0];
				//				Collection<ConfigOption> options = (Collection<ConfigOption>) retVals[1];
				//				logger.debug("  Can print   "+types);
				//				logger.debug("  Options are "+options);
				templResults.put(plugin, result);
			} else {
				// Print module does not support selecting templates
				logger.debug("Plugin "+plugin+" does not support templates");
			}
		}

		/*
		 * Now ask all plugins for their options
		 */
		Map<String, CommandResult> results = new HashMap<>();
		for (String plugin : plugins) {
			CommandResult result = CommandBus.fireCommand(plugin, (Object)null, CommandType.PRINT_GET_OPTIONS,
					selected.getRuleIdentifier(),
					selected.getCharacter(),
					manager.getScene().getWindow(),
					manager
					);
			if (result.wasProcessed() && result.wasSuccessful()) {
				logger.debug("Plugin "+plugin+" responded "+result);
				Object[] retVals = (Object[]) result.getReturnValue();
				logger.debug("  Returned  "+Arrays.toString(retVals));
				Collection<PrintType>      types = (Collection<PrintType>)retVals[0];
				Collection<ConfigOption> options = (Collection<ConfigOption>) retVals[1];
				logger.debug("  Can print   "+types);
				logger.debug("  Options are "+options);
				results.put(plugin, result);
			} else {
				logger.error("Failed asking print plugin '"+plugin+"' for its options: "+result.getMessage());
			}
		}

		/*
		 * Sort in a way that plugins with PDF support are listed first
		 */
		List<Entry<String, CommandResult>> available = new ArrayList<>(results.entrySet());
		Collections.sort(available, new Comparator<Entry<String, CommandResult>>() {
			public int compare(Entry<String, CommandResult> o1, Entry<String, CommandResult> o2) {
				CommandResult result1 = o1.getValue();
				CommandResult result2 = o2.getValue();
				Object[] retVals1 = (Object[]) result1.getReturnValue();
				Object[] retVals2 = (Object[]) result2.getReturnValue();
				Collection<PrintType> types1 = (Collection<PrintType>)retVals1[0];
				Collection<PrintType> types2 = (Collection<PrintType>)retVals2[0];
				Integer min1 = Integer.MAX_VALUE;
				Integer min2 = Integer.MAX_VALUE;
				for (PrintType element:types1) min1=Math.min(min1, element.ordinal());
				for (PrintType element:types2) min2=Math.min(min2, element.ordinal());

				int cmp =  min1.compareTo(min2);
				if (cmp!=0) return cmp;
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		logger.debug("Preference "+available);

		/*
		 * Prepare GUI
		 */
		Map<String, Parent> optionDialogs = new HashMap<>();
		StackPane stack = new StackPane();
		ChoiceBox<String> cbPlugins = new ChoiceBox<>();
		for (Entry<String,CommandResult> entry : available) {
			String plugin = entry.getKey();
			cbPlugins.getItems().add(plugin);
			// Result from OPTIONS request
			CommandResult result = entry.getValue();
			Object[] retVals = (Object[]) result.getReturnValue();
			Collection<PrintType>      types = (Collection<PrintType>)retVals[0];
			Collection<ConfigOption> options = (Collection<ConfigOption>) retVals[1];
			// Result from TEMPLATES request
			result = templResults.get(entry.getKey());
			List<PDFPrintElement> elements = new ArrayList<>();
			if (result!=null) {
				retVals = (Object[]) result.getReturnValue();
				elements = (List<PDFPrintElement>)retVals[0];
			}
			try {
				Parent dialog = preparePrintDialog(manager,
						selected.getCharacter(),
						selected.getRuleIdentifier(),
						types,
						options,
						elements);
				optionDialogs.put(plugin, dialog);
				stack.getChildren().add(dialog);
			} catch (Exception e) {
				logger.error("Error building print option pane",e);
			}
		}
		cbPlugins.getSelectionModel().selectedItemProperty().addListener( (ov,o,n) -> {
			stack.getChildren().forEach(elem -> elem.setVisible(false));
			if (n!=null && optionDialogs.containsKey(n))
				optionDialogs.get(n).setVisible(true);
		});
		cbPlugins.getSelectionModel().select(0);

		VBox choiceDialog = new VBox(20);
		choiceDialog.getChildren().addAll(cbPlugins, stack);

		CloseType closed = manager.showAlertAndCall(AlertType.QUESTION, ResourceI18N.get(RES, "dialog.printoptions.title"), choiceDialog);
		if (closed==CloseType.CANCEL || closed==CloseType.NO)
			return;

		/*
		 * Print Plugin selected
		 */
		String selectedPlugin = cbPlugins.getSelectionModel().getSelectedItem();
		CommandResult result = results.get(selectedPlugin);
		Object[] retVals = (Object[]) result.getReturnValue();
		Collection<ConfigOption<?>> options = (Collection<ConfigOption<?>>) retVals[1];

		Parent dialog = optionDialogs.get(selectedPlugin);
		logger.debug("readPrintDialogInput for "+selectedPlugin);
		PrintType type = readPrintDialogInput(options, dialog);
		logger.info("Print as "+type+" using plugin '"+selectedPlugin+"'");
		PrintTemplate dynamicTemplate = (PrintTemplate)dialog.getUserData();

		// Now really print
		result = CommandBus.fireCommand(selectedPlugin, (Object)null, CommandType.PRINT,
				selected.getRuleIdentifier(),
				selected.getCharacter(),
				manager.getScene().getWindow(),
				manager,
				type,
				dynamicTemplate
				);
		if (!result.wasProcessed()) {
			logger.warn("No plugin processed PRINT command");
			manager.showAlertAndCall(
					AlertType.ERROR,
					ResourceI18N.get(RES, "error.print.no_plugin.title"),
					ResourceI18N.get(RES, "error.print.no_plugin.mess")
					);
			return;
		}
		if (!result.wasSuccessful()) {
			logger.warn("processing PRINT command failed: "+result.getMessage());
			String todo = String.format(
					ResourceI18N.get(RES, "label.consultlogfile"),
					getLoggerFileName()
					);

			manager.showAlertAndCall(
					AlertType.ERROR,
					ResourceI18N.get(RES, "error.print.generic.title"),
					todo+"\n\n"+result.getMessage()
					);
			return;
		}
		if (result.getReturnValue()==null) {
			return;
		}

		/*
		 * Update or create attachment for player
		 */
		Path resultFile = (Path)result.getReturnValue();
		try {
			byte[] raw = Files.readAllBytes(resultFile);
			Format format = null;
			switch (type) {
			case PDF : format = Format.PDF; break;
			case HTML: format = Format.HTML; break;
			case BBCODE: format = Format.TEXT; break;
			case IMAGE: format = Format.IMAGE; break;
			default:
			}

			if (format!=null) {
				logger.debug("Create an attachment for "+format);
				CharacterProvider charProv = CharacterProviderLoader.getCharacterProvider();
				Attachment pdfAttachment = selected.getFirstAttachment(Type.CHARACTER, format);
				if (pdfAttachment==null) {
					charProv.addAttachment(selected, Type.CHARACTER, format, null, raw);
				} else {
					pdfAttachment.setData(raw);
					charProv.modifyAttachment(selected, pdfAttachment);
				}
			}
		} catch (IOException e) {
			logger.error("Failed creating PDF attachment",e);
		}


		String mess = ResourceI18N.format(RES, "message.print.success", result.getReturnValue());
		manager.showAlertAndCall(AlertType.NOTIFICATION, "", mess);
		
		// Eventually open file
		logger.debug("Open in PDF viewer");
		Genesis5Main.getHostServicesDelegate().showDocument(resultFile.toUri().toString());
	}

	//-------------------------------------------------------------------
	private static String getLoggerFileName() {
		try {
			org.apache.logging.log4j.core.Logger loggerImpl = (org.apache.logging.log4j.core.Logger) logger;
			Appender appender = loggerImpl.getParent().getAppenders().get("fileLogger");
			if (appender==null) {
				System.err.println("Cannot detect logfile");
				return System.getProperty("logdir");
			}
			// Unfortunately, File is no longer an option to return, here.
			return ((RollingFileAppender) appender).getFileName();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return System.getProperty("logdir");
	}
	
	//-------------------------------------------------------------------
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Parent preparePrintDialog(ScreenManager manager, RuleSpecificCharacterObject charac, RoleplayingSystem system, Collection<PrintType> types, Collection<ConfigOption> options,
			List<PDFPrintElement> elements) {
		logger.debug("preparePrintDialog() with options="+options);
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);

		int i=0;
		/*
		 * Print Type
		 */
		ChoiceBox<PrintType> cbTypes = new ChoiceBox<>();
		cbTypes.setId("printtype");
		cbTypes.getItems().addAll(types);
		cbTypes.setValue(types.iterator().next());
		grid.add(new Label(ResourceI18N.get(RES, "label.printtype")), 0, i);
		grid.add(cbTypes, 1, i);

		// Template
		if (elements!=null && !elements.isEmpty()) {
			ChoiceBox<PrintTemplate> cbTemplates = new ChoiceBox<>();
			// Ask framework for all stored templates
			cbTemplates.getItems().add(null);
			cbTemplates.getItems().addAll(
					PrintManagerLoader.getInstance().getAvailableTemplates(system)
					);
			cbTemplates.setConverter(new StringConverter<PrintTemplate>() {
				public PrintTemplate fromString(String value) {
					return null;
				}
				public String toString(PrintTemplate value) {
					if (value!=null)
						return value.getName();
					return "-";
				}
			});
			cbTemplates.getSelectionModel().selectedItemProperty().addListener( (ov,o,n) -> grid.setUserData(n));

			// Edit Button
			Button btnEditTemp = new Button(ResourceI18N.get(RES, "label.customize"));
			btnEditTemp.setOnAction(event -> {
				PrintTemplate templ = openPrintTemplateEditor(manager, charac, cbTemplates.getValue(), elements, system);
				if (templ!=null) {
					/*
					 * Ensure template has a name.
					 * If it already had a name and is given a different name
					 * it is saved as a new template, otherwise under the old name
					 */
					logger.debug("ask name for template");
					Label lbName = new Label(ResourceI18N.get(RES, "dialog.printtemplate.name"));
					TextField tfName = new TextField();
					VBox box = new VBox(10);
					box.getChildren().addAll(lbName, tfName);
					if (templ.getName()!=null) {
						Label lbDesc = new Label(RES.getString("dialog.printtemplate.desc"));
						lbDesc.setWrapText(true);
						tfName.setText(templ.getName());
						box.getChildren().add(1, lbDesc);
					}
					CloseType closed = manager.showAlertAndCall(AlertType.QUESTION, ResourceI18N.get(RES, "dialog.printtemplate.title"), box);
					String newName = tfName.getText();
					if (closed==CloseType.OK) {
						// User chose a name or left in unchanged
						if (templ==cbTemplates.getValue()) {
							// Template was set before
							if (newName.equals(templ.getName())) {
								// Overwrite
								logger.info("Overwrite template "+newName+" ");
							} else {
								// Add as new template
								logger.info("Save "+newName+" as new template");
								List<LayoutGrid> pages = new ArrayList<>();
								templ.forEach(e -> pages.add(e));
								templ = PrintManagerLoader.getInstance().createTemplate(pages);
								templ.setName(newName);
								cbTemplates.getItems().add(templ);
								cbTemplates.setValue(templ);
							}
						} else {
							// New template
							templ.setName(newName);
							cbTemplates.getItems().add(templ);
							cbTemplates.setValue(templ);
						}

						// Save
						logger.debug("Save template "+templ.getName());
						PrintManagerLoader.getInstance().saveTemplate(system, templ);
					} else {
						templ.setName("Unsaved");
						cbTemplates.getItems().add(templ);
						cbTemplates.setValue(templ);
					}

					if (!cbTemplates.getItems().contains(templ)) {
						cbTemplates.getItems().add(templ);
						cbTemplates.setValue(templ);
					}
				}

			});
			HBox tempLine = new HBox(5);
			tempLine.getChildren().addAll(cbTemplates, btnEditTemp);
			i++;
			grid.add(new Label(ResourceI18N.get(RES,"label.template")), 0, i);
			grid.add(tempLine, 1, i);
		}


		for (ConfigOption option : options) {
			logger.debug("Option "+option);
			// DUmmy
			if (option==null) {
				logger.fatal("STOP HERE - Option is NullPointer");
				System.exit(1);
			}
			// Ignore the config option that stores templates, since it is already being displayed
			if (option.getLocalId().equals("template"))
				continue;
			i++;
			grid.add(new Label(option.getName()), 0, i);
			switch (option.getType()) {
			case TEXT:
				TextField text = new TextField(String.valueOf(option.getValue()));
				text.setId(option.getPathID());
				text.setUserData(option);
				grid.add(text, 1,i);
				break;
			case PASSWORD:
				PasswordField pass = new PasswordField();
				pass.setText(String.valueOf(option.getValue()));
				pass.setId(option.getPathID());
				pass.setUserData(option);
				grid.add(pass, 1,i);
				break;
			case CHOICE:
				ChoiceBox<Object> choice = new ChoiceBox<>();
				choice.setConverter(new StringConverter<Object>() {
					public String toString(Object object) { return option.getOptionName(object); }
					public Object fromString(String string) {return null; }
				});
				choice.getItems().addAll(option.getChoiceOptions());
				choice.setValue(option.getValue());
				choice.setId(option.getPathID());
				choice.setUserData(option);
				grid.add(choice, 1,i);
				break;
			case DIRECTORY:
				Path oldPath = Paths.get(String.valueOf(option.getValue()));
				if (!Files.exists(oldPath)) {
					String newVal = System.getProperties().getProperty("user.home");
					logger.warn("Correct invalid path "+oldPath+" to "+newVal);
					((ConfigOption<String>)option).set( newVal);
				}
				TextField dir = new TextField(String.valueOf(option.getValue()));
				Button dirSelect = new Button(ResourceI18N.get(RES, "button.select"));
				HBox dirLine = new HBox(5);
				dirLine.getChildren().addAll(dir, dirSelect);
				dir.setId(option.getPathID());
				dir.setUserData(option);
				grid.add(dirLine, 1,i);
				dirSelect.setOnAction(event -> {
					DirectoryChooser chooser = new DirectoryChooser();
					File oldValue = null;
					try {
						oldValue = new File((String) option.getValue());
					} catch (NullPointerException e) {
						/*Dann eben nicht*/
						logger.warn("Error opening "+option.getValue()+": "+e);
					}
					if (oldValue == null || !oldValue.exists()) {
						oldValue = new File(System.getProperties().getProperty("user.home"));
					}
					chooser.setInitialDirectory(oldValue);
					File selected = chooser.showDialog(manager.getScene().getWindow());
					if (selected != null) {
						dir.setText(selected.getAbsolutePath());
					}

				});
				break;
			case BOOLEAN:
				CheckBox box = new CheckBox();
				box.setId(option.getPathID());
				box.setUserData(option);
				box.setSelected((boolean)option.getValue());
				grid.add(box, 1,i);
				break;
			default:
				logger.error("TODO: implement "+option.getType()+" config option");
			}
		}


		return grid;
	}

	//-------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	private static PrintType readPrintDialogInput(Collection<ConfigOption<?>> options, Parent dialog) {

		PrintType type = null;

		for (Node node : dialog.getChildrenUnmodifiable()) {
			if (node.getId()!=null && node.getId().equals("printtype")) {
				type = ((ChoiceBox<PrintType>)node).getValue();
				continue;
			}
			if (node.getUserData()!=null) {
				ConfigOption<?> option = (ConfigOption<?>)node.getUserData();
				switch (option.getType()) {
				case TEXT:
				case DIRECTORY:
				case FILE:
					logger.debug("Set '"+option.getName()+"' to "+((TextField)node).getText());
					((ConfigOption<String>)option).set(((TextField)node).getText());
					break;
				case PASSWORD:
					logger.debug("Set '"+option.getName()+"' to "+((PasswordField)node).getText());
					((ConfigOption<String>)option).set(((PasswordField)node).getText());
					break;
				case CHOICE:
					logger.debug("Set '"+option.getName()+"' to "+((ChoiceBox<Object>)node).getValue());
					((ConfigOption<Object>)option).set(((ChoiceBox<Object>)node).getValue());
					break;
				case BOOLEAN:
					logger.debug("Set '"+option.getName()+"' to "+((CheckBox)node).isSelected());
					((ConfigOption<Boolean>)option).set(((CheckBox)node).isSelected());
					break;
				default:
					logger.error("TODO: implement reading "+option.getType()+" config option");
				}
				continue;
			}

			// Maybe recurse
			if (node.getUserData()==null && node instanceof Parent)
				readPrintDialogInput(options, (Parent) node);
		}

		return type;
	}

	//-------------------------------------------------------------------
	private static PrintTemplate openPrintTemplateEditor(ScreenManager manager, RuleSpecificCharacterObject charac, PrintTemplate template, List<PDFPrintElement> elements, RoleplayingSystem system) {
		logger.info("openPrintTemplateEditor for "+template);
		/*
		 * Verify and resolve all element identifier in the template
		 */
		if (template!=null) {
			logger.warn("TODO: verify template");
//			TemplateController ctrl = TemplateFactory.newTemplateController(page, elementMap)
//			List<String> notFound = template.resolveIDs(elements);
//			if (!notFound.isEmpty()) {
//				logger.warn("PrintTemplate "+template.getName()+" has unknown element references: "+notFound);
//				manager.showAlertAndCall(AlertType.ERROR,
//						ResourceI18N.get(RES, "error.printtemplate.unknown_ids.title"),
//						ResourceI18N.format(RES, "error.printtemplate.unknown_ids.desc", notFound));
//			} else
//				logger.warn("All references resolved");
		}

		PrintTemplateEditorScreen screen = new PrintTemplateEditorScreen(elements,system);
		if (template!=null) {
			screen.setData(charac, template);
		} else
			screen.setCharacter(charac);
		CloseType ret = (CloseType) manager.showAndWait(screen);
		logger.warn("Dialog returned "+ret);
		if (ret==CloseType.OK) {
			return screen.getTemplate();
		}
		return null;
	}

}
