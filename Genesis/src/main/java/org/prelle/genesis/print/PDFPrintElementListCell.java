package org.prelle.genesis.print;

import java.io.ByteArrayInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rpgframework.print.PDFPrintElement;
import de.rpgframework.print.PDFPrintElement.RenderingParameter;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
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

class PDFPrintElementListCell extends ListCell<PDFPrintElement> {

	private final static Logger logger = LogManager.getLogger("genesis");

	private PDFPrintElement data;

	//--------------------------------------------------------------------
	public PDFPrintElementListCell() {
		setOnDragDetected(event -> dragStarted(event));
	}

	//--------------------------------------------------------------------
	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	public void updateItem(PDFPrintElement item, boolean empty) {
		super.updateItem(item, empty);
		this.data = item;

		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
//			setText(String.valueOf(item));
			Image img = new Image(new ByteArrayInputStream(item.render(new RenderingParameter())));
			ImageView iview = new ImageView(img);
			iview.setFitWidth(img.getWidth()*0.50);
			iview.setPreserveRatio(true);
			VBox imgBox = new VBox(iview);
			imgBox.setPadding(new Insets(2));

			Label label = new Label(item.getDescription());
//			label.setPrefWidth(Double.MAX_VALUE);
			label.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white; -fx-min-width: 25em");

			VBox box = new VBox();
			box.getChildren().addAll(label, imgBox);
			box.setPrefWidth(700);
			box.setStyle("-fx-border-color: rgba(0,0,0,0.5); -fx-border-width: 2px; -fx-border-style: solid;");
			label.prefWidthProperty().bind(box.widthProperty());
			box.prefWidthProperty().bind(img.widthProperty().multiply(0.7));
			setGraphic(box);
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
		String id = "element:"+data.getId();
		content.putString(id);
		db.setContent(content);

		/* Drag image */
		WritableImage snapshot = source.snapshot(new SnapshotParameters(), null);
		db.setDragView(snapshot);

		event.consume();
	}

}