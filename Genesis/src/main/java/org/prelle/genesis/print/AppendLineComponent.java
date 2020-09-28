package org.prelle.genesis.print;

import java.util.Map;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rpgframework.ResourceI18N;
import de.rpgframework.print.ElementCell;
import de.rpgframework.print.PDFPrintElement;
import de.rpgframework.print.PrintManagerLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.RowConstraints;

/**
 * @author Stefan
 *
 */
public class AppendLineComponent extends Label implements RowComponent {

	private final static Logger logger = LogManager.getLogger("genesis");
	private final static ResourceBundle RES = ResourceBundle.getBundle(PrintTemplateEditorScreen.class.getName());

	private final static Double HEIGHT = 50.0;

	private RowConstraints rowCons;
	private Map<String, PDFPrintElement> elements;
	private PagePane page;
	private int addLine;
	private int showLine;
	private int lastX = -1;

	//--------------------------------------------------------------------
	public AppendLineComponent(Map<String, PDFPrintElement> elementMap, PagePane page, int add, int show) {
		super(ResourceI18N.get(RES,"screen.printtemplate.appendline"));
		elements = elementMap;
		this.page = page;
		this.addLine = add;
		this.showLine = show;
		rowCons = new RowConstraints();
		rowCons.setPrefHeight(HEIGHT);
		rowCons.setMinHeight(HEIGHT);
		setPrefHeight(HEIGHT);
		setMinHeight(HEIGHT);
		setPrefWidth(Double.MAX_VALUE);
		setAlignment(Pos.CENTER);
		setStyle("-fx-background-color: yellow");

		setOnDragOver(ev -> dragOver(ev));
		setOnDragDropped(ev -> dragDropped(ev));
	}

	//--------------------------------------------------------------------
	private void dragOver(DragEvent event) {
		Node target = (Node) event.getSource();
		if (event.getGestureSource() != target && event.getDragboard().hasString()) {
			String enhanceID = event.getDragboard().getString();

			int pos = enhanceID.indexOf(":");
			if (pos>0) {
				String head = enhanceID.substring(0, pos);
				String tail = enhanceID.substring(pos+1);
				if (head.equals("element")) {
					ElementCell ref = PrintManagerLoader.getInstance().createElementCell(elements.get(tail));

					int x = (int)(event.getX() / (PrintTemplateConstants.COLUMN_WIDTH + PrintTemplateConstants.COLUMN_GAP));
					if (x!=lastX) {
						lastX = x;
						page.markGrid(x,showLine, ref.getRequiredColumns(), true);
					}
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				}
			}

		}
	}
	
	//--------------------------------------------------------------------
	private void dragDropped(DragEvent event) {
		Dragboard db = event.getDragboard();
		lastX = -1;
		boolean success = false;
		if (db.hasString()) {
			String enhanceID = db.getString();
			logger.debug("Dropped "+enhanceID);

			int pos = enhanceID.indexOf(":");
			if (pos>0) {
				String head = enhanceID.substring(0, pos);
				String tail = enhanceID.substring(pos+1);
				if (head.equals("element")) {
					logger.debug("Dropped element "+tail+" = "+elements.get(tail));
					ElementCell ref = PrintManagerLoader.getInstance().createElementCell(elements.get(tail));
					int line = addLine;
					logger.debug("Line "+line);
					int x = (int)(event.getX() / (PrintTemplateConstants.COLUMN_WIDTH + PrintTemplateConstants.COLUMN_GAP));
					try {
						page.getPageDefinition().add(x, line, ref);
					} catch (Exception e) {
						logger.warn("Error (normal) dropping to line "+line+": "+e);
					}
					logger.debug("  added cell at "+x+","+line+" - refresh view");

					// Remove marks
					page.markGrid(0, 0, 0, true);
					page.update();
				}
			}
		}
		/* let the source know whether the string was successfully
		 * transferred and used */
		event.setDropCompleted(success);

		event.consume();
	}

	//--------------------------------------------------------------------
	/**
	 * @see org.prelle.genesis.print.RowComponent#getRowConstraints()
	 */
	@Override
	public RowConstraints getRowConstraints() {
		return rowCons;
	}

}
