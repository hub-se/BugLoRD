/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter.datatables;

import java.awt.Color;
import java.text.DecimalFormat;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.util.GraphicsUtils;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
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
	
	public static final String ALL_ID = "x";
	public static final String UNSIGNIFICANT_ID = "n";
	public static final String LOW_SIGNIFICANCE_ID = "l";
	public static final String MEDIUM_SIGNIFICANCE_ID = "m";
	public static final String HIGH_SIGNIFICANCE_ID = "h";
	public static final String CRUCIAL_SIGNIFICANCE_ID = "c";

	public DiffDataTableCollection() {
		super(	//outlier table
				new OutlierDataTable(), 
				//unsignificant changes
				new NormalDataTable(GraphicsUtils.deriveDarker(Color.lightGray)),
				//low significance changes
				new NormalDataTable(GraphicsUtils.deriveDarker(Color.GREEN)),
				//medium significance changes
				new NormalDataTable(Color.YELLOW),
				//high significance changes
				new NormalDataTable(Color.ORANGE),
				//crucial significance changes
				new NormalDataTable(GraphicsUtils.deriveDarker(Color.RED)),
				//all changes
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
	 * a modification identifier
	 * @param fileno
	 * the number of the input ranking file the line is from
	 * @param ranking
	 * the ranking of the line
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
			Double equalRankingPlus, Double equalRankingMinus) {
		try {
			
			switch(modification) {
			case ChangeWrapper.SIGNIFICANCE_ALL:
				getTables()[5].add(fileno, ranking, 
						equalRankingPlus, equalRankingMinus, ALL_ID);
				break;
			case ChangeWrapper.SIGNIFICANCE_NONE:
				getTables()[0].add(fileno, ranking, 
						equalRankingPlus, equalRankingMinus, UNSIGNIFICANT_ID);
				break;
			case ChangeWrapper.SIGNIFICANCE_LOW:
				getTables()[1].add(fileno, ranking, 
						equalRankingPlus, equalRankingMinus, LOW_SIGNIFICANCE_ID);
				break;
			case ChangeWrapper.SIGNIFICANCE_MEDIUM:
				getTables()[2].add(fileno, ranking, 
						equalRankingPlus, equalRankingMinus, MEDIUM_SIGNIFICANCE_ID);
				break;
			case ChangeWrapper.SIGNIFICANCE_HIGH:
				getTables()[3].add(fileno, ranking, 
						equalRankingPlus, equalRankingMinus, HIGH_SIGNIFICANCE_ID);
				break;
			case ChangeWrapper.SIGNIFICANCE_CRUCIAL:
				getTables()[4].add(fileno, ranking, 
						equalRankingPlus, equalRankingMinus, CRUCIAL_SIGNIFICANCE_ID);
				break;
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
	 * a modification identifier
	 * @param fileno
	 * the number of the input ranking file the line is from
	 * @param ranking
	 * the ranking of the line
	 * @return
	 * true if a data point has been added, false otherwise
	 */
	public boolean addData(String modification, final int fileno, final double ranking) {
		return addData(modification, fileno, ranking, null, null);
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
