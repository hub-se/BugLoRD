/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter.datatables;

import java.util.HashMap;
import java.util.Map;

import de.erichseifert.gral.data.DataTable;

/**
 * Class that contains all relevant data tables and a labeling map.
 * 
 * @author Simon Heiden
 * 
 * @see DataTable
 */
public class DataTableCollection {

	private Map<Double, String> labels = new HashMap<Double, String>();
	private Integer maxX = null;
	
	private NormalDataTable[] tables;
	private OutlierDataTable outlierTable = null;
	
	/**
	 * Creates a new {@link DataTableCollection} object.
	 * @param outlierTable
	 * a data table that contains data about outliers (data points which are not in plot range)
	 * @param tables
	 * data tables that can be plotted
	 */
	public DataTableCollection(OutlierDataTable outlierTable, NormalDataTable... tables) {
		this(tables);
		this.outlierTable = outlierTable;
	}
	
	/**
	 * Creates a new {@link DataTableCollection} object.
	 * @param tables
	 * data tables that can be plotted
	 */
	public DataTableCollection(NormalDataTable... tables) {
		this.tables = tables;
	}
	
	public NormalDataTable[] getTables() {
		return tables;
	}
	
	public OutlierDataTable getOutlierTable() {
		return outlierTable;
	}
	
	/**
	 * @return
	 * the label map
	 */
	public Map<Double, String> getLabelMap() {
		return labels;
	}

	public int getMaxX() {
		if (maxX != null) {
			return maxX;
		} else {
			return labels.size()-1;
		}
	}
	
	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}
	
	public int getMaxY() {
		double maxY = 0;
		for (NormalDataTable table : tables) {
			maxY = maxY < table.getMaxY() ? table.getMaxY() : maxY;
		}
		return (int) maxY;
	}
	
	public int getMaxYFromTables(int... tableIndices) {
		int maxY = 0;
		for (int i : tableIndices) {
			maxY = maxY < getMaxY(i) ? getMaxY(i) : maxY;
		}
		return maxY;
	}
	
	public int getMinY() {
		double minY = getMaxY();
		for (NormalDataTable table : tables) {
			minY = minY > table.getMinY() ? table.getMinY() : minY;
		}
		return (int) minY;
	}
	
	public int getMinYFromTables(int... tableIndices) {
		int minY = getMaxY(tableIndices[0]);
		for (int i  : tableIndices) {
			minY = minY > getMinY(i) ? getMinY(i) : minY;
		}
		return minY;
	}
	
	public int getMaxY(int tableIndex) {
		return (int)tables[tableIndex].getMaxY();
	}
	
	public int getMinY(int tableIndex) {
		return (int)tables[tableIndex].getMinY();
	}
	
//	public int getMaxYFromColumns(int tableIndex, Integer[] cols) {
//		return (int)tables[tableIndex].getMaxYFromCols(cols);
//	}
//	
//	public int getMinYFromColumns(int tableIndex, Integer[] cols) {
//		return (int)tables[tableIndex].getMinYFromCols(cols);
//	}
//	
//	public int getMaxYFromTablesFromColumns(Integer[] cols, int... tableIndices) {
//		if (tableIndices == null) {
//			int maxY = 0;
//			for (NormalDataTable table : tables) {
//				int temp = (int)table.getMinYFromCols(cols);
//				maxY = maxY < temp ? temp : maxY;
//			}
//			return maxY;
//		} else {
//			int maxY = 0;
//			for (int i : tableIndices) {
//				int temp = getMaxYFromColumns(i, cols);
//				maxY = maxY < temp ? temp : maxY;
//			}
//			return maxY;
//		}
//	}
//	
//	public int getMinYFromTablesFromColumns(Integer[] cols, int... tableIndices) {
//		if (tableIndices == null) {
//			int minY = getMaxYFromTablesFromColumns(cols);
//			for (NormalDataTable table : tables) {
//				int temp = (int)table.getMinYFromCols(cols);
//				minY = minY > temp ? temp : minY;
//			}
//			return minY;
//		} else {
//			int minY = getMaxYFromColumns(tableIndices[0], cols);
//			for (int i  : tableIndices) {
//				int temp = getMinYFromColumns(i, cols);
//				minY = minY > temp ? temp : minY;
//			}
//			return minY;
//		}
//	}
	
	public int getPlotWidth() {
		return getMaxX()+1 > 20 ? 70 + (getMaxX()+1) * 12 : 530;
	}
	
	public int getPlotHeightFromRange(int range) {
//		return 195 + 15 + range * 8;
		return 195 + 15 + range * 3;
	}
	
	public int getPlotHeight() {
		return getPlotHeightFromRange(getMaxY() - getMinY());
	}
	
	public int getPlotHeightFromTables(int... tableIndices) {
		return getPlotHeightFromRange(getMaxYFromTables(tableIndices) - getMinYFromTables(tableIndices));
	}
	
//	public int getPlotHeightFromTables(int... tableIndices) {
//		return getPlotHeightFromRange(150);
//	}
	
	public int getPlotHeightFromAbsoluteValue(int height) {
		return getPlotHeightFromRange(height);
	}
}
