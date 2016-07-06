/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter.datatables;

import java.awt.Color;
import java.util.Collection;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.data.statistics.Statistics;

/**
 * Data table wrapper class that contains, for example, 
 * desired plot colors.
 * 
 * @author Simon Heiden
 * 
 * @see DataTable
 */
public abstract class ADataTable {

	private DataTable table;
	private Color plotColor = Color.BLACK;

	public Color getPlotColor() {
		return plotColor;
	}
	
	public void setPlotColor(Color plotColor) {
		this.plotColor = plotColor;
	}
	
	public DataTable getTable() {
		return table;
	}
	
	public void setTable(DataTable table) {
		this.table = table;
	}
	
	public void add(Comparable<?>... values) {
		table.add(values);
	}
	
	public void add(Collection<? extends Comparable<?>> values) {
		table.add(values);
	}

	public double getMaxY(int col) {
		return table.getColumn(col).getStatistics(Statistics.MAX);
	}
	
	public double getMinY(int col) {
		return table.getColumn(col).getStatistics(Statistics.MIN);
	}
}
