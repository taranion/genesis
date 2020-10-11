/**
 * 
 */
package org.prelle.genesis.print;

import java.util.ArrayList;
import java.util.List;

import de.rpgframework.character.RuleSpecificCharacterObject;
import de.rpgframework.print.PDFPrintElement;
import de.rpgframework.print.PDFPrintElementFeature;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import de.rpgframework.print.PDFPrintElement.RenderingParameter;

/**
 * @author stefan
 *
 */
public class LayoutGridElement implements PDFPrintElement {
	
	private int width;

	//---------------------------------------------------------
	public LayoutGridElement(int width, int height) {
		this.width = width;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.print.PDFPrintElement#getRequiredColumns()
	 */
	@Override
	public int getRequiredColumns() {
		return width;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.print.PDFPrintElement#render(de.rpgframework.print.PDFPrintElement.RenderingParameter)
	 */
	@Override
	public byte[] render(RenderingParameter parameter) {
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.print.PDFPrintElement#getId()
	 */
	@Override
	public String getId() {
		return String.valueOf(width);
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.print.PDFPrintElement#hasFeature(de.rpgframework.print.PDFPrintElementFeature)
	 */
	@Override
	public boolean hasFeature(PDFPrintElementFeature feature) {
		return false;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.print.PDFPrintElement#getIndexableObjectNames(de.rpgframework.character.RuleSpecificCharacterObject)
	 */
	@Override
	public List<String> getIndexableObjectNames(RuleSpecificCharacterObject character) {
		return new ArrayList<String>();
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.print.PDFPrintElement#getFilterOptions()
	 */
	@Override
	public List<String> getFilterOptions() {
		return new ArrayList<String>();
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.print.PDFPrintElement#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Layout Grid "+width+"x";
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.print.PDFPrintElement#getNextHorizontalGrowth(de.rpgframework.print.PDFPrintElement.RenderingParameter)
	 */
	@Override
	public int getNextHorizontalGrowth(RenderingParameter parameter) {
		// TODO Auto-generated method stub
		return 0;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.print.PDFPrintElement#getPreviousHorizontalGrowth(de.rpgframework.print.PDFPrintElement.RenderingParameter)
	 */
	@Override
	public int getPreviousHorizontalGrowth(RenderingParameter parameter) {
		// TODO Auto-generated method stub
		return 0;
	}

}
