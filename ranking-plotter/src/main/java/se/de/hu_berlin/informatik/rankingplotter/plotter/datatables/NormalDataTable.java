/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter.datatables;

import java.awt.Color;
import de.erichseifert.gral.data.DataTable;

/**
 * Class that contains all relevant data tables and a labeling map.
 * 
 * @author Simon Heiden
 * 
 * @see DataTable
 */
public class NormalDataTable extends ADataTable {

	/**
	 * Creates a new {@link NormalDataTable} object.
	 * @param plotColor
	 * the color in which to plot the data points in
	 */
	public NormalDataTable(Color plotColor) {
		this();
		setPlotColor(plotColor);
	}
	
	/**
	 * Creates a new {@link NormalDataTable} object. The data points
	 * are plotted in black.
	 */
	@SuppressWarnings("unchecked")
	public NormalDataTable() {
		setTable(new DataTable(Integer.class, Double.class, Double.class, Double.class, String.class));
	}

	public double getMaxY() {
		return getMaxY(1);
	}
	
	public double getMinY() {
		return getMinY(1);
	}
	
//	public double getMinYFromCols(Integer... cols) {
//		double minY = getMaxY(1);
//		for (int i = 0; i < getTable().getColumn(0).size(); ++i) {
//			int index = (int)getTable().getColumn(0).get(i);
//			for (Integer col : cols) {
//				if (col == index) {
//					minY = (double)getTable().getColumn(1).get(index) < minY ? (double)getTable().getColumn(1).get(index) : minY;
//					break;
//				}
//			}
//		}
//		return minY;
//	}
//	
//	public double getMaxYFromCols(Integer... cols) {
//		double maxY = 0;
//		for (int i = 0; i < getTable().getColumn(0).size(); ++i) {
//			int index = (int)getTable().getColumn(0).get(i);
//			for (Integer col : cols) {
//				if (col == index) {
//					maxY = (double)getTable().getColumn(1).get(index) > maxY ? (double)getTable().getColumn(1).get(index) : maxY;
//					break;
//				}
//			}
//		}
//		return maxY;
//	}
	
}
