/**
 *
 */
package org.prelle.genesis.print;

/**
 * @author Stefan
 *
 */
public interface PrintTemplateConstants {

	final static double PAGE_WIDTH  = 595.0;
	final static double PAGE_HEIGHT = 842.0;
	final static int MAX_COLUMNS = 6;
	final static int COLUMN_GAP  = 5;
	final static double COLUMN_WIDTH= (PAGE_WIDTH-(MAX_COLUMNS-1)*COLUMN_GAP)/MAX_COLUMNS;

}
