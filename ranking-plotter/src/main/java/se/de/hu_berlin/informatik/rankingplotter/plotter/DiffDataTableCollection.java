/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.awt.Color;
import java.text.DecimalFormat;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.util.GraphicsUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

/**
 * Extension of {@link DataTableCollection}, specifically for storing data about
 * the four possible modifications regarding a 'diff' of two files.
 * (Being 'a' for appended lines, 'c' for changed lines, 'd' for deleted lines
 * and 'n' for neighboring lines.)
 * 
 * @author Simon Heiden
 * 
 * @see DataTableCollection
 */
public class DiffDataTableCollection extends DataTableCollection {

	public DiffDataTableCollection() {
		super(	//outlier table
				new OutlierDataTable(), 
				//appends
				new NormalDataTable(GraphicsUtils.deriveDarker(Color.GREEN)),
				//changes
				new NormalDataTable(GraphicsUtils.deriveDarker(Color.BLUE)),
				//deletes
				new NormalDataTable(GraphicsUtils.deriveDarker(Color.RED)),
				//neighbor lines
				new NormalDataTable(GraphicsUtils.deriveBrighter(new Color( 55, 170, 200))),
				//all modifications
				new NormalDataTable(Color.BLACK));
	}

	/**
	 * Adds a label to the tick labels map.
	 * @param tick
	 * specifies the tick on the x-axis which shall be labeled
	 * @param sBFLpercentage
	 * is the percentage part that the SBFL ranking constitutes to the whole ranking
	 */
	public void addLabel(int tick, double sBFLpercentage) {
		getLabelMap().put((double)tick, "\u03BB: " + new DecimalFormat("#.##").format(sBFLpercentage));
	}
	
	/**
	 * Adds a label to the tick labels map.
	 * @param tick
	 * specifies the tick on the x-axis which shall be labeled
	 * @param sBFLpercentage
	 * is the percentage part that the SBFL ranking constitutes to the whole ranking
	 * @param localNLFLpercentage
	 * is the percentage part that the local NLFL ranking constitutes to the whole ranking
	 */
	public void addLabel(int tick, double sBFLpercentage, double localNLFLpercentage) {
		getLabelMap().put((double)tick, "\u03BB: " + new DecimalFormat("#.##").format(sBFLpercentage) + ", \u00B5: " + new DecimalFormat("#.##").format(localNLFLpercentage));
	}

	/**
	 * Adds data points to the respective {@link DataTable}s.
	 * @param modification
	 * a modification identifier ('a', 'c' or 'd', or 'n' for neighboring lines)
	 * @param fileno
	 * the number of the input ranking file the line is from
	 * @param ranking
	 * the ranking of the line
	 * @param useNeighbors
	 * sets if the neighbor related data points should be processed
	 * @param equalRankingPlus
	 * the number of rankings that have the same ranking as the current line
	 * and lie above it (lower ranking in the file)
	 * @param equalRankingMinus
	 * the number of rankings that have the same ranking as the current line
	 * and lie below it (higher ranking in the file)
	 * @return
	 * true if a data point has been added, false otherwise
	 */
	public boolean addData(String modification, final int fileno, final double ranking, 
			final boolean useNeighbors, Double equalRankingPlus, Double equalRankingMinus) {
		try {
			if (modification.equals("x")) {
//				if (ranking <= range) {
					getTables()[4].add(fileno, ranking, 
							equalRankingPlus, equalRankingMinus, "x");
//				}
				return true;
			}
			if (modification.equals("a")) {
//				if (ranking <= range) {
					getTables()[0].add(fileno, ranking, 
							equalRankingPlus, equalRankingMinus, "a");
//				}
				return true;
			}
			if (modification.equals("c")) {
//				if (ranking <= range) {
					getTables()[1].add(fileno, ranking, 
							equalRankingPlus, equalRankingMinus, "c");
//				}
				return true;
			}
			if (modification.equals("d")) {
//				if (ranking <= range) {
					getTables()[2].add(fileno, ranking, 
							equalRankingPlus, equalRankingMinus, "d");
//				}
				return true;
			}
			
			if (useNeighbors && modification.equals("n")) {
//				if (ranking <= range) {
					getTables()[3].add(fileno, ranking,
							equalRankingPlus, equalRankingMinus, "n");
//				}
				return true;
			}
		} catch (Exception e) {
			Misc.abort("Could not add data.");
		}
		return false;
	}
	
	/**
	 * Adds data points to the respective {@link DataTable}s without equal ranking 
	 * ranges specified.
	 * @param modification
	 * a modification identifier ('a', 'c' or 'd', or 'n' for neighboring lines)
	 * @param fileno
	 * the number of the input ranking file the line is from
	 * @param ranking
	 * the ranking of the line
	 * @param useNeighbors
	 * sets if the neighbor related data points should be processed
	 * @return
	 * true if a data point has been added, false otherwise
	 */
	public boolean addData(String modification, final int fileno, final double ranking, 
			final boolean useNeighbors) {
		return addData(modification, fileno, ranking, useNeighbors, null, null);
	}
	
	
	
	/**
	 * Adds data points to the outlier {@link DataTable}.
	 * @param range
	 * the maximum range in which data points are plotted
	 * @param outlierCount
	 * is an array containing the amount of data points outside of the plot range
	 * @return
	 * true if everything went fine, false otherwise
	 */
	public boolean addOutlierData(final Integer range, final int[] outlierCount) {
		try {
			for (int i = 0; i < outlierCount.length; ++i) {
				if (outlierCount[i] > 0) {
					getOutlierTable().add(i+1, range+1, "+" + String.valueOf(outlierCount[i]));
				}
			}
			return true;
		} catch (Exception e) {
			Misc.err("Could not add outlier data.");
		}
		return false;
	}

}
