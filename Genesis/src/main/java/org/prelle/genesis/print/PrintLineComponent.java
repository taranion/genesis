/**
 *
 */
package org.prelle.genesis.print;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rpgframework.character.RuleSpecificCharacterObject;
import de.rpgframework.print.ElementCell;
import de.rpgframework.print.EmptyCell;
import de.rpgframework.print.MultiRowCell;
import de.rpgframework.print.PDFPrintElement;
import de.rpgframework.print.PrintCell;
import de.rpgframework.print.PrintLine;
import de.rpgframework.print.PrintManagerLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

/**
 * @author Stefan
 *
 */
public class PrintLineComponent extends HBox implements RowComponent {

	private final static Logger logger = LogManager.getLogger("genesis");

	private final static Double HEIGHT = 50.0;

	private RowConstraints rowCons;
	private Map<String, PDFPrintElement> elements;
	private PagePane page;
	private PrintLine line;
	private int yAdd, yShow;
	private RuleSpecificCharacterObject charac;

	//--------------------------------------------------------------------
	public PrintLineComponent(RuleSpecificCharacterObject charac, Map<String, PDFPrintElement> elementMap, PagePane page, PrintLine line, int yAdd, int yShow) {
		super(PrintTemplateConstants.COLUMN_GAP);
		elements = elementMap;
		this.page = page;
		this.charac = charac;
		this.line = line;
		this.yShow  = yShow;
		this.yAdd  = yAdd;
		rowCons = new RowConstraints();
		rowCons.setMinHeight(HEIGHT);
//		setPrefHeight(HEIGHT);
		setMinHeight(HEIGHT);
		setPrefWidth(Double.MAX_VALUE);

		setOnDragOver(ev -> dragOver(ev));
		setOnDragDropped(event -> dragDropped(event));
	}

	//--------------------------------------------------------------------
	/**
	 * @see org.prelle.genesis.print.RowComponent#getRowConstraints()
	 */
	@Override
	public RowConstraints getRowConstraints() {
		return rowCons;
	}

	//--------------------------------------------------------------------
	private Region render(PrintLine line, MultiRowCell multi, PrintCell cell) {
		if (cell instanceof ElementCell) {
			ElementCell ref = (ElementCell)cell;
			int reqCol = ref.getRequiredColumns() + ref.getSavedRenderOptions().getHorizontalGrow();
			Image img = new Image(new ByteArrayInputStream(ref.getCachedImage()));
			logger.debug("Image size is "+img.getWidth()+"x"+img.getHeight());
			ImageView iView = new ImageView(img);
			double renderWidth = PrintTemplateConstants.COLUMN_WIDTH*reqCol + (reqCol-1)*PrintTemplateConstants.COLUMN_GAP;
			iView.setPreserveRatio(true);
			iView.setFitWidth(renderWidth);
			PrintLineComponentWithMenu par = new PrintLineComponentWithMenu(charac, iView, line, multi, ref, page);
			par.setMaxWidth(renderWidth);
			par.setPrefWidth(renderWidth);
			double scaledHeight = (renderWidth/img.getWidth())*img.getHeight();
//			logger.debug("debug "+ref.getElement().getId()+" to "+par.getHeight()+" // "+scaledHeight);
			par.setPrefHeight(scaledHeight);
			return par;
		} else if (cell instanceof EmptyCell) {
			double renderWidth = PrintTemplateConstants.COLUMN_WIDTH*cell.getRequiredColumns() + (cell.getRequiredColumns()-1)*PrintTemplateConstants.COLUMN_GAP;
			Region spacing = new Region();
			spacing.setMaxWidth(renderWidth);
			spacing.setPrefWidth(renderWidth);
			return spacing;
		} else if (cell instanceof MultiRowCell) {
			double renderWidth = PrintTemplateConstants.COLUMN_WIDTH*cell.getRequiredColumns() + (cell.getRequiredColumns()-1)*PrintTemplateConstants.COLUMN_GAP;
			VBox box = new VBox(1);
			double totalHeight = 0;
			for (PrintCell innerCell : ((MultiRowCell)cell)) {
				Region inner = render(null, (MultiRowCell)cell, innerCell);
				totalHeight += inner.getPrefHeight();
				box.getChildren().add(inner);
			}
			totalHeight += 20;
			box.setMaxWidth(renderWidth);
			box.setPrefWidth(renderWidth);
			box.setPrefHeight(totalHeight);
			// Add empty inner line
			Region spacing = new Region();
			spacing.setMaxWidth(renderWidth);
			spacing.setPrefWidth(renderWidth);
			spacing.setMinHeight(20);
			spacing.setPrefHeight(50);
			spacing.setOnDragOver(ev -> dragOverMultiRowCell((MultiRowCell) cell, ev));
			spacing.setOnDragDropped(ev -> dragDroppedMultiRowCell((MultiRowCell) cell, ev));
			spacing.setMouseTransparent(false);
			box.getChildren().add(spacing);
			return box;
		} else {
			logger.error("Don't know how to render "+cell.getClass());
		}
		return null;
	}

	//--------------------------------------------------------------------
	void refresh() {
		getChildren().clear();
		double max = 0;
		for (PrintCell cell : line) {
			logger.trace("    START cell with "+cell.getRequiredColumns()+" width");
			Region rendered = render(line, null, cell);
			if (rendered!=null) {
				rendered.setStyle("-fx-border-width: 1px; -fx-border-style: dotted; -fx-border-color: #B0B0B0;");
				max = Math.max(max, rendered.getPrefHeight());
				getChildren().add(rendered);

			}
		}
		logger.trace("Change pref height of "+this+" to "+max);
		rowCons.setPrefHeight(max);
		this.setPrefHeight(max);
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

					try {
						int x = (int)(event.getX() / (PrintTemplateConstants.COLUMN_WIDTH + PrintTemplateConstants.COLUMN_GAP));
						boolean possible = !line.isOccupied(x, ref.getRequiredColumns());
						page.markGrid(x,yShow, ref.getRequiredColumns(), possible);
						event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}
	}

	//--------------------------------------------------------------------
	private void dragOverMultiRowCell(MultiRowCell cell, DragEvent event) {
		Node target = (Node) event.getSource();
		logger.debug("Drag over MultiRowCell"+event);
		page.clearGrid();
		if (event.getGestureSource() != target && event.getDragboard().hasString()) {
				String enhanceID = event.getDragboard().getString();

				int pos = enhanceID.indexOf(":");
				if (pos>0) {
					String head = enhanceID.substring(0, pos);
					String tail = enhanceID.substring(pos+1);
					if (head.equals("element")) {
						ElementCell ref = PrintManagerLoader.getInstance().createElementCell(elements.get(tail));
						if (ref.getRequiredColumns()==cell.getRequiredColumns())
							((Region)event.getSource()).setStyle("-fx-background-color: rgb(0,255,0); -fx-opacity: 0.5");
						else
							((Region)event.getSource()).setStyle("-fx-background-color: rgb(255,0,0); -fx-opacity: 0.5");
						event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
						event.consume();
					}
				}

		}
	}

	//--------------------------------------------------------------------
	private void dragDropped(DragEvent event) {
		Dragboard db = event.getDragboard();
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
					try {
						ElementCell ref = PrintManagerLoader.getInstance().createElementCell(elements.get(tail));
//					int line = page.getLine(event.getY());
						int line = yAdd;
						int x = (int)(event.getX() / (PrintTemplateConstants.COLUMN_WIDTH + PrintTemplateConstants.COLUMN_GAP));
						logger.debug("Dropped at "+x+"x"+line);
						try {
							page.getPageDefinition().add(x, line, ref);
						} catch (Exception e) {
							logger.warn("Error (normal) dropping to line "+line+": "+e);
						}
						logger.debug("  added cell - refresh view");
						refresh();
//					page.iterator().next().add(ref);
						// Remove marks
						page.markGrid(0, 0, 0, true);
						page.update();
					} catch (Exception e) {
						logger.error("Error dropping component",e);
					}
				}
			}
		}
		/* let the source know whether the string was successfully
		 * transferred and used */
		event.setDropCompleted(success);

		event.consume();
	}

	//--------------------------------------------------------------------
	private void dragDroppedMultiRowCell(MultiRowCell cell, DragEvent event) {
		Dragboard db = event.getDragboard();
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
					cell.add(ref);
					logger.debug("  added cell - refresh view");
					// Remove marks
					((Region)event.getSource()).setStyle("-fx-background-color: rgb(255,0,0); -fx-opacity: 0");
					page.update();
					/* let the source know whether the string was successfully
					 * transferred and used */
					event.setDropCompleted(true);
					event.consume();
				}
			}
		}
	}

}
