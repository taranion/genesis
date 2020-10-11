package org.prelle.genesis.print;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rpgframework.ResourceI18N;
import de.rpgframework.print.PDFPrintElement;
import de.rpgframework.print.PDFPrintElement.RenderingParameter;
import de.rpgframework.print.PDFPrintElementFeature;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

class LayoutGridElementListCell extends ListCell<LayoutGridElement> {

	private final static Logger logger = LogManager.getLogger("genesis");

	private PDFPrintElement data;

	//--------------------------------------------------------------------
	public LayoutGridElementListCell() {
		setOnDragDetected(event -> dragStarted(event));
	}

	//--------------------------------------------------------------------
	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	public void updateItem(LayoutGridElement item, boolean empty) {
		super.updateItem(item, empty);
		this.data = item;

		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			int width = item.getRequiredColumns()*20 + (item.getRequiredColumns()-1)*5;
			Canvas canvas = new Canvas(width, 62);
			GraphicsContext ctx = canvas.getGraphicsContext2D();
			ctx.setFill(Color.WHITE);
			ctx.setStroke(Color.BLACK);
			ctx.fill();
			for (int i=0; i<item.getRequiredColumns(); i++) {
				int x = i*25;
				ctx.strokeRect(x,  0, 20, 20);
				ctx.strokeRect(x, 25, 20, 20);
				ctx.strokeLine(x, 58,  x, 50);
				ctx.strokeLine(x, 50, x+20, 50);
				ctx.strokeLine(x+20, 50, x+20, 62);
			}
			setGraphic(canvas);
		}
	}

	//--------------------------------------------------------------------
	public void dragStarted(MouseEvent event) {
		logger.debug("drag");

		Node source = (Node) event.getSource();

		/* drag was detected, start a drag-and-drop gesture*/
		/* allow any transfer mode */
		Dragboard db = source.startDragAndDrop(TransferMode.ANY);

		/* Put a string on a dragboard */
		ClipboardContent content = new ClipboardContent();
		String id = "grid:"+data.getId();
		content.putString(id);
		db.setContent(content);

		/* Drag image */
		WritableImage snapshot = source.snapshot(new SnapshotParameters(), null);
		db.setDragView(snapshot);

		event.consume();
	}

}