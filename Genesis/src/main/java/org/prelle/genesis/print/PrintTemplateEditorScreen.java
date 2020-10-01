package org.prelle.genesis.print;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.javafx.AppBarButton;
import org.prelle.javafx.CloseType;
import org.prelle.javafx.CommandBar;
import org.prelle.javafx.ManagedDialog;
import org.prelle.javafx.SymbolIcon;
import org.prelle.rpgframework.jfx.print.LayoutGridPane;

import de.rpgframework.ResourceI18N;
import de.rpgframework.character.RuleSpecificCharacterObject;
import de.rpgframework.core.RoleplayingSystem;
import de.rpgframework.print.LayoutGrid;
import de.rpgframework.print.PDFPrintElement;
import de.rpgframework.print.PrintTemplate;
import de.rpgframework.print.TemplateController;
import de.rpgframework.print.TemplateFactory;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * @author Stefan
 *
 */
public class PrintTemplateEditorScreen extends ManagedDialog {

	private final static Logger logger = LogManager.getLogger("genesis");
	private final static ResourceBundle RES = ResourceBundle.getBundle(PrintTemplateEditorScreen.class.getName());

	private List<PDFPrintElement> elements;
	private Map<String,PDFPrintElement> elementMap;
	private RuleSpecificCharacterObject charac;

	private Label noWYSIWYG;
	private CommandBar command;
	private AppBarButton btnAdd;
	private AppBarButton btnDel;
	private ListView<PDFPrintElement> lvElements;
	private LayoutGridPane lgPage;

	private PrintTemplate template;
	private RoleplayingSystem system;
	private Path backgroundImage;

	//--------------------------------------------------------------------
	public PrintTemplateEditorScreen(List<PDFPrintElement> elements, RoleplayingSystem system) {
		super(ResourceI18N.get(RES,"screen.print_template_editor.title"), null, CloseType.OK);
		this.elements = elements;
		this.system   = system;
		this.elementMap = new HashMap<>();
		for (PDFPrintElement elem : elements)
			this.elementMap.put(elem.getId(), elem);

		template = new PrintTemplate();
		
		initComponents();
		initLayout();
		initInteractivity();
	}


	//--------------------------------------------------------------------
	private void initComponents() {
		btnAdd = new AppBarButton(ResourceI18N.get(RES, "button.add"), new SymbolIcon("add"));
		btnDel = new AppBarButton(ResourceI18N.get(RES, "button.delete"), new SymbolIcon("delete"));
		command = new CommandBar();
		command.getPrimaryCommands().addAll(btnAdd,btnDel);
		noWYSIWYG = new Label(ResourceI18N.get(RES,"screen.printtemplate.nowysiwyg"));
		LayoutGrid page = TemplateFactory.createPageDefinition(6);
		template.add(page);
		TemplateController ctrl = TemplateFactory.newTemplateController(page, elementMap);
		lgPage = new LayoutGridPane(page, ctrl, 
				(int)PrintTemplateConstants.COLUMN_WIDTH, 
				PrintTemplateConstants.COLUMN_GAP, elementMap);
		logger.debug("Added a page to the listview");

		lvElements = new ListView<>();
		lvElements.getItems().addAll(elements);
		lvElements.setCellFactory(lv -> new PDFPrintElementListCell());
	}

	//--------------------------------------------------------------------
	private void initLayout() {
		ScrollPane scroll = new ScrollPane(lgPage);
		scroll.setStyle("-fx-min-height: 700px");
		scroll.setFitToWidth(true);
		scroll.setMaxHeight(Double.MAX_VALUE);
		scroll.setPrefWidth(PrintTemplateConstants.PAGE_WIDTH+150);
		VBox boxPage = new VBox(command, scroll);

		lvElements.setMaxHeight(Double.MAX_VALUE);
		lvElements.setStyle("-fx-pref-width: 720px");

		HBox content = new HBox(20);
//		HBox.setHgrow(lvPages, Priority.SOMETIMES);
//		HBox.setHgrow(lvElements, Priority.SOMETIMES);
		content.getChildren().addAll(boxPage, lvElements);
		content.setMaxHeight(Double.MAX_VALUE);

		VBox metaContent = new VBox(20);
		VBox.setVgrow(content, Priority.ALWAYS);
		metaContent.getChildren().addAll(noWYSIWYG, content);
		setContent(metaContent);
	}

	//--------------------------------------------------------------------
	private void initInteractivity() {
	}

	//--------------------------------------------------------------------
	public PrintTemplate getTemplate() {
//		if (template==null) {
//			template = PrintManagerLoader.getInstance().createTemplate(lvPages.getItems());
//		}
//
//		template.setPages(lvPages.getItems());
		if (backgroundImage!=null)
			template.setBackgroundImage(backgroundImage);
		return template;
	}

	//--------------------------------------------------------------------
	public void setCharacter(RuleSpecificCharacterObject charac) {
		this.charac = charac;
	}

	//--------------------------------------------------------------------
	public void setData(RuleSpecificCharacterObject charac, PrintTemplate template) {
		if (template==null)
			throw new NullPointerException("template");
		this.template = template;
		this.charac = charac;

		logger.debug("setData");
//		lgPage.set
//		lvPages.getItems().clear();
//		template.forEach(item -> lvPages.getItems().add(item));
	}


	//--------------------------------------------------------------------
	public void deletePage(LayoutGrid page) {
		logger.debug("delete page "+page);
//		lvPages.getItems().remove(page);
//		if (lvPages.getItems().isEmpty()) {
//			lvPages.getItems().add(PrintManagerLoader.getInstance().createLayoutGrid(PrintTemplateConstants.MAX_COLUMNS));
//		}
	}


	//--------------------------------------------------------------------
	public void addPage() {
		logger.debug("add page");
//		lvPages.getItems().add(PrintManagerLoader.getInstance().createLayoutGrid(PrintTemplateConstants.MAX_COLUMNS));
	}


	//--------------------------------------------------------------------
	/**
	 * @return the system
	 */
	public RoleplayingSystem getSystem() {
		return system;
	}

	//--------------------------------------------------------------------
	public void setBackgroundImage(Path path) {
		this.backgroundImage = path;
	}

//	//--------------------------------------------------------------------
//	public List<LayoutGrid> getPageList() {
//		return lvPages.getItems();
//	}

	//--------------------------------------------------------------------
	public RuleSpecificCharacterObject getCharacter() {
		return charac;
	}
}

//class LayoutGridListCell extends ListCell<LayoutGrid> {
//
//	private final static Logger logger = LogManager.getLogger("genesis");
//
//	@SuppressWarnings("unused")
//	private LayoutGrid data;
//	private PagePane pane;
//	private PrintTemplateEditorScreen parent;
//
//	//--------------------------------------------------------------------
//	public LayoutGridListCell(Map<String, PDFPrintElement> elementMap, PrintTemplateEditorScreen prov) {
//		pane = new PagePane(elementMap, prov);
//		this.parent = prov;
//		setStyle("-fx-background-color: #c0c0c0; -fx-margin: 10px; -fx-padding: 1em");
//		getStyleClass().add("print-page-cell");
//	}
//
//	//--------------------------------------------------------------------
//	/**
//	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
//	 */
//	@Override
//	public void updateItem(LayoutGrid item, boolean empty) {
//		super.updateItem(item, empty);
//		this.data = item;
//
//		logger.debug("Update ListCell with page "+item);
//		if (empty) {
//			setGraphic(null);
//			setText(null);
//		} else {
//			pane.setData(item, parent.getPageList().indexOf(item)+1);
//			setGraphic(pane);
//		}
//	}
//
//}
