/**
 *
 */
package org.prelle.genesis.print;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.genesis.Constants;
import org.prelle.javafx.CloseType;
import org.prelle.javafx.ManagedDialog;

import de.rpgframework.character.RuleSpecificCharacterObject;
import de.rpgframework.core.RoleplayingSystem;
import de.rpgframework.print.PDFPrintElement;
import de.rpgframework.print.PageDefinition;
import de.rpgframework.print.PrintManagerLoader;
import de.rpgframework.print.PrintTemplate;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * @author Stefan
 *
 */
/**
 * @author Stefan
 *
 */
public class PrintTemplateEditorScreen extends ManagedDialog {

	private final static Logger logger = LogManager.getLogger("genesis");
	private final static ResourceBundle GENESIS = Constants.RES;

	private List<PDFPrintElement> elements;
	private Map<String,PDFPrintElement> elementMap;
	private RuleSpecificCharacterObject charac;

	private Label noWYSIWYG;
	private ListView<PDFPrintElement> lvElements;
	private ListView<PageDefinition> lvPages;

	private PrintTemplate template;
	private RoleplayingSystem system;
	private Path backgroundImage;

	//--------------------------------------------------------------------
	public PrintTemplateEditorScreen(List<PDFPrintElement> elements, RoleplayingSystem system) {
		super(GENESIS.getString("screen.print_template_editor.title"), null, CloseType.OK);
		this.elements = elements;
		this.system   = system;
		this.elementMap = new HashMap<>();
		for (PDFPrintElement elem : elements)
			this.elementMap.put(elem.getId(), elem);

		initComponents();
		initLayout();
		initInteractivity();
	}


	//--------------------------------------------------------------------
	private void initComponents() {
		noWYSIWYG = new Label(GENESIS.getString("screen.printtemplate.nowysiwyg"));
		lvPages = new ListView<>();
		lvPages.getItems().add(PrintManagerLoader.getInstance().createPageDefinition(6));
		logger.debug("Added a page to the listview");
		lvPages.setCellFactory(lv -> new PageDefinitionListCell(elementMap, this));

		lvElements = new ListView<>();
		lvElements.getItems().addAll(elements);
		lvElements.setCellFactory(lv -> new PDFPrintElementListCell());
	}

	//--------------------------------------------------------------------
	private void initLayout() {

		lvPages.setMaxHeight(Double.MAX_VALUE);
		lvElements.setMaxHeight(Double.MAX_VALUE);

		lvPages.setPrefWidth(PrintTemplateConstants.PAGE_WIDTH+150);
		lvElements.setStyle("-fx-pref-width: 720px");

		HBox content = new HBox(20);
//		HBox.setHgrow(lvPages, Priority.SOMETIMES);
//		HBox.setHgrow(lvElements, Priority.SOMETIMES);
		content.getChildren().addAll(lvPages, lvElements);
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
		if (template==null) {
			template = PrintManagerLoader.getInstance().createTemplate(lvPages.getItems());
		}

		template.setPages(lvPages.getItems());
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
		lvPages.getItems().clear();
		template.forEach(item -> lvPages.getItems().add(item));
	}


	//--------------------------------------------------------------------
	public void deletePage(PageDefinition page) {
		logger.debug("delete page "+page);
		lvPages.getItems().remove(page);
		if (lvPages.getItems().isEmpty()) {
			lvPages.getItems().add(PrintManagerLoader.getInstance().createPageDefinition(PrintTemplateConstants.MAX_COLUMNS));
		}
	}


	//--------------------------------------------------------------------
	public void addPage() {
		logger.debug("add page");
		lvPages.getItems().add(PrintManagerLoader.getInstance().createPageDefinition(PrintTemplateConstants.MAX_COLUMNS));
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

	//--------------------------------------------------------------------
	public List<PageDefinition> getPageList() {
		return lvPages.getItems();
	}

	//--------------------------------------------------------------------
	public RuleSpecificCharacterObject getCharacter() {
		return charac;
	}
}

class PageDefinitionListCell extends ListCell<PageDefinition> {

	private final static Logger logger = LogManager.getLogger("genesis");

	@SuppressWarnings("unused")
	private PageDefinition data;
	private PagePane pane;
	private PrintTemplateEditorScreen parent;

	//--------------------------------------------------------------------
	public PageDefinitionListCell(Map<String, PDFPrintElement> elementMap, PrintTemplateEditorScreen prov) {
		pane = new PagePane(elementMap, prov);
		this.parent = prov;
		setStyle("-fx-background-color: #c0c0c0; -fx-margin: 10px; -fx-padding: 2em");
		getStyleClass().add("print-page-cell");
	}

	//--------------------------------------------------------------------
	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	public void updateItem(PageDefinition item, boolean empty) {
		super.updateItem(item, empty);
		this.data = item;

		logger.debug("Update ListCell with page "+item);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			pane.setData(item, parent.getPageList().indexOf(item)+1);
			setGraphic(pane);
		}
	}

}
