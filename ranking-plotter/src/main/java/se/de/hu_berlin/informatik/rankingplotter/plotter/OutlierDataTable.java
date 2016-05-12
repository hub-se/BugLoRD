/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.awt.Color;
import de.erichseifert.gral.data.DataTable;

/**
 * Class that contains all relevant data tables and a labeling map.
 * 
 * @author Simon Heiden
 * 
 * @see DataTable
 */
public class OutlierDataTable extends ADataTable {

	/**
	 * Creates a new {@link OutlierDataTable} object.
	 * @param plotColor
	 * the color in which to plot the data points in
	 */
	public OutlierDataTable(Color plotColor) {
		this();
		setPlotColor(plotColor);
	}
	
	/**
	 * Creates a new {@link OutlierDataTable} object. The data points
	 * are plotted in black.
	 */
	@SuppressWarnings("unchecked")
	public OutlierDataTable() {
		setTable(new DataTable(Integer.class, Integer.class, String.class));
	}

}
