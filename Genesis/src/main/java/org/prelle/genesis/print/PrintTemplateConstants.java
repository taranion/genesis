/**
 *
 */
package org.prelle.genesis.print;

/**
 * @author Stefan
 *
 */
public interface PrintTemplateConstants {

	public final static double PAGE_WIDTH  = 1150; //595.0;
	public final static double PAGE_HEIGHT = 1644; //842.0;
	public final static int MAX_COLUMNS = 6;
	public final static int COLUMN_GAP  = 5;
	public final static double COLUMN_WIDTH= (PAGE_WIDTH-(MAX_COLUMNS-1)*COLUMN_GAP)/MAX_COLUMNS;

}
