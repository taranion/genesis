package org.prelle.genesis.print;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.prelle.genesis.Constants;

import de.rpgframework.ResourceI18N;
import de.rpgframework.character.RuleSpecificCharacterObject;
import de.rpgframework.print.ElementCell;
import de.rpgframework.print.MultiRowCell;
import de.rpgframework.print.PDFPrintElement;
import de.rpgframework.print.PDFPrintElementFeature;
import de.rpgframework.print.PrintLine;
import de.rpgframework.print.SavedRenderOptions;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

class PrintLineComponentWithMenu extends StackPane {

	private final static Logger logger = LogManager.getLogger("genesis");
	private final static ResourceBundle RES = ResourceBundle.getBundle(PrintLineComponentWithMenu.class.getName());

	private RuleSpecificCharacterObject character;
	private ElementCell cell;
	private PagePane parent;
	private PrintLine line;
	private MultiRowCell multiRow;

	private ImageView iView;
	private HBox lineIcons;
	private Button btnDelete;
	private Button btnToggleLeftRight;
	private Button btnExpandBelow;
	private Button btnGrowVert;
	private Button btnShrinkVert;
	private Button btnGrowHori;
	private Button btnShrinkHori;
	private Button btnFilter;
	private Button btnPick;

	//--------------------------------------------------------------------
	public PrintLineComponentWithMenu(RuleSpecificCharacterObject charac, ImageView iView, PrintLine line, MultiRowCell multi, ElementCell cell, PagePane parent) {
		this.cell = cell;
		this.character = charac;
		this.parent = parent;
		this.iView = iView;
		this.line  = line;
		this.multiRow = multi;

		initComponents();
		initLayout();
		initInteractivity();
	}

	//--------------------------------------------------------------------
	private void initComponents() {
		btnDelete = new Button("\uE107");
		btnDelete.setTooltip(new Tooltip(ResourceI18N.get(RES,"screen.printtemplate.tooltip.delete")));
		btnDelete.getStyleClass().addAll("mini-button");

		btnToggleLeftRight = new Button("\uE13C");
		btnToggleLeftRight.setTooltip(new Tooltip(ResourceI18N.get(RES,"screen.printtemplate.tooltip.togglelr")));
		btnToggleLeftRight.getStyleClass().addAll("mini-button");

		btnExpandBelow = new Button("\uE147");
		btnExpandBelow.setTooltip(new Tooltip(ResourceI18N.get(RES,"screen.printtemplate.tooltip.expandRow")));
		btnExpandBelow.getStyleClass().addAll("mini-button");

		btnShrinkVert = new Button("\uE010");
		btnShrinkVert.setTooltip(new Tooltip(RES.getString("screen.printtemplate.tooltip.shrinkV")));
		btnShrinkVert.getStyleClass().addAll("mini-button");

		btnGrowVert = new Button("\uE011");
		btnGrowVert.setTooltip(new Tooltip(ResourceI18N.get(RES,"screen.printtemplate.tooltip.growV")));
		btnGrowVert.getStyleClass().addAll("mini-button");

		btnShrinkHori = new Button("\uE012");
		btnShrinkHori.setTooltip(new Tooltip(ResourceI18N.get(RES,"screen.printtemplate.tooltip.shrinkH")));
		btnShrinkHori.getStyleClass().addAll("mini-button");

		btnGrowHori = new Button("\uE013");
		btnGrowHori.setTooltip(new Tooltip(ResourceI18N.get(RES,"screen.printtemplate.tooltip.growH")));
		btnGrowHori.getStyleClass().addAll("mini-button");

		btnFilter = new Button("\uE16E");
		btnFilter.setTooltip(new Tooltip(ResourceI18N.get(RES,"screen.printtemplate.tooltip.filter")));
		btnFilter.getStyleClass().addAll("mini-button");

		btnPick = new Button("\uE094");
		btnPick.setTooltip(new Tooltip(ResourceI18N.get(RES,"screen.printtemplate.tooltip.pick")));
		btnPick.getStyleClass().addAll("mini-button");
	}

	//--------------------------------------------------------------------
	private void initLayout() {
		lineIcons = new HBox(5);
		lineIcons.setAlignment(Pos.CENTER_RIGHT);
		lineIcons.setVisible(false);
		lineIcons.setStyle("-fx-pref-height: 1.5em; -fx-max-height: 1.5em; -fx-background-color: rgba(0,0,0,0.5)");
		lineIcons.getChildren().add(btnDelete);
		if (cell.getElement().hasFeature(PDFPrintElementFeature.ORIENTATION))
			lineIcons.getChildren().add(btnToggleLeftRight);
		if (line!=null)
			lineIcons.getChildren().add(btnExpandBelow);
		if (cell.getElement().hasFeature(PDFPrintElementFeature.EXPAND_VERTICAL)) {
			if (cell.getSavedRenderOptions().getVerticalGrow()>0)
				lineIcons.getChildren().add(btnShrinkVert);
			lineIcons.getChildren().add(btnGrowVert);
		}
		if (cell.getElement().hasFeature(PDFPrintElementFeature.EXPAND_HORIZONTAL)) {
			if (cell.getSavedRenderOptions().getHorizontalGrow()>0)
				lineIcons.getChildren().add(btnShrinkHori);
			if (line.canGrowHorizontal(cell))
				lineIcons.getChildren().add(btnGrowHori);
		}
		if (cell.getElement().hasFeature(PDFPrintElementFeature.INDEXABLE))
			lineIcons.getChildren().add(btnPick);
		if (cell.getElement().hasFeature(PDFPrintElementFeature.FILTER))
			lineIcons.getChildren().add(btnFilter);

		getChildren().addAll(iView, lineIcons);
		StackPane.setAlignment(lineIcons, Pos.TOP_CENTER);
	}

	//--------------------------------------------------------------------
	private void initInteractivity() {
		setOnMouseEntered(ev -> lineIcons.setVisible(true));
		setOnMouseExited(ev -> lineIcons.setVisible(false));
		setOnDragDetected(ev -> dragStarted(ev));

		btnDelete.setOnAction(ev -> {
			logger.debug("Delete clicked");
			if (line!=null)
				line.remove(cell);
			else if (multiRow!=null)
				multiRow.remove(cell);
			parent.update();
		});

		btnToggleLeftRight.setOnAction(ev -> {
			logger.debug("Toggle left/right clicked");
			PDFPrintElement.Orientation old = cell.getSavedRenderOptions().getAsRenderingParameter().getOrientation();
			switch (old) {
			case LEFT:
				logger.info("Switch orientation of "+cell.getElementId()+" to RIGHT");
				cell.getSavedRenderOptions().setOrientation(PDFPrintElement.Orientation.RIGHT);
				break;
			case RIGHT:
				cell.getSavedRenderOptions().setOrientation(PDFPrintElement.Orientation.LEFT);
				logger.info("Switch orientation of "+cell.getElementId()+" to LEFT");
				break;
			case STANDALONE:
				cell.getSavedRenderOptions().setOrientation(PDFPrintElement.Orientation.LEFT);
				logger.info("Switch orientation of "+cell.getElementId()+" to LEFT");
				break;
			}
			refresh();
		});

		btnExpandBelow.setOnAction(ev -> {
			logger.trace("expand below on "+line);
			line.convertToMiniTable(cell);
			parent.update();
		});

		btnGrowHori.setOnAction(ev -> {
			logger.debug("Grow horizontal");
			SavedRenderOptions opt = cell.getSavedRenderOptions();
			if (line.canGrowHorizontal(cell)) {
				logger.debug("  grow by "+cell.getElement().getNextHorizontalGrowth(cell.getSavedRenderOptions().getAsRenderingParameter())+" columns");
				opt.setHorizontalGrow(opt.getHorizontalGrow()+cell.getElement().getNextHorizontalGrowth(cell.getSavedRenderOptions().getAsRenderingParameter()));
				line.growHorizontal(cell);
				refresh();
				parent.update();
			}
		});

		btnShrinkHori.setOnAction(ev -> {
			logger.debug("Shrink horizontal");
			SavedRenderOptions opt = cell.getSavedRenderOptions();
			if (opt.getHorizontalGrow()>0) {
				opt.setHorizontalGrow(opt.getHorizontalGrow()-cell.getElement().getPreviousHorizontalGrowth(cell.getSavedRenderOptions().getAsRenderingParameter()));
				line.shrinkHorizontal(cell);
				refresh();
				parent.update();
			}
		});

		btnGrowVert.setOnAction(ev -> {
			logger.debug("Grow vertical");
			SavedRenderOptions opt = cell.getSavedRenderOptions();
			opt.setVerticalGrow(opt.getVerticalGrow()+1);
			refresh();
			parent.update();
		});

		btnShrinkVert.setOnAction(ev -> {
			logger.debug("Shrink vertical");
			SavedRenderOptions opt = cell.getSavedRenderOptions();
			if (opt.getVerticalGrow()>0) {
				opt.setVerticalGrow(opt.getVerticalGrow()-1);
				refresh();
				parent.update();
			}
		});

		btnPick.setOnMouseClicked(ev -> {
			List<String> options = cell.getElement().getIndexableObjectNames(character);
			logger.debug("List = "+options);

			ContextMenu ctx = new ContextMenu();
			ctx.setHideOnEscape(true);
			int pos=-1;
			for (String name : options) {
				pos++;
				MenuItem item = new MenuItem(name);
				item.setUserData(pos);
				item.setOnAction(ev2 -> {
					ctx.hide();
					int index = (Integer)((MenuItem)ev2.getSource()).getUserData();
					logger.info("Selected list index "+index+" for component ");
					cell.getSavedRenderOptions().setSelectedIndex(index);
					refresh();
					parent.update();
					});
				ctx.getItems().add(item);
			}
			ctx.setAutoHide(false);
			ctx.show(btnPick, ev.getScreenX()-1, ev.getScreenY()-1);
		});

		btnFilter.setOnMouseClicked(ev -> {
			List<String> options = cell.getElement().getFilterOptions();
			logger.debug("Filter options of "+cell.getElement().getClass().getSimpleName()+" = "+options);

			ContextMenu ctx = new ContextMenu();
			ctx.setHideOnEscape(true);
			int pos=-1;
			for (String name : options) {
				pos++;
				MenuItem item = new MenuItem(name);
				item.setUserData(pos);
				item.setOnAction(ev2 -> {
					ctx.hide();
					cell.getSavedRenderOptions().setVariantIndex(
						(Integer)((MenuItem)ev2.getSource()).getUserData());
					logger.debug("Picked display variant "+cell.getSavedRenderOptions().getVariantIndex()+" from "+options);
					refresh();
					parent.update();
					});
				ctx.getItems().add(item);
			}
			ctx.setAutoHide(false);
			ctx.show(btnFilter, ev.getScreenX()-1, ev.getScreenY()-1);
		});
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
		String id = "element:"+cell.getElementId();
		content.putString(id);
		db.setContent(content);

		/* Drag image */
		WritableImage snapshot = source.snapshot(new SnapshotParameters(), null);
		db.setDragView(snapshot);

		line.remove(cell);
		event.consume();
	}

	//--------------------------------------------------------------------
	private void refresh() {
		cell.refresh();
		Image img = new Image(new ByteArrayInputStream(cell.getCachedImage()));
		iView.setImage(img);

		btnShrinkHori.setVisible(cell.getSavedRenderOptions().getHorizontalGrow()>0);
		btnShrinkVert.setVisible(cell.getSavedRenderOptions().getVerticalGrow()>0);
	}

	//--------------------------------------------------------------------
	public void setOrientation(PDFPrintElement.Orientation value) {
		logger.debug("Orientation set to "+value);
		cell.getSavedRenderOptions().setOrientation(value);
		refresh();
	}

}