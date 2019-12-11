/**
 *
 */
package org.prelle.genesis.print;

import javafx.scene.layout.RowConstraints;

/**
 * @author Stefan
 *
 */
public interface RowComponent {

	public RowConstraints getRowConstraints();

	public double getHeight();

}
