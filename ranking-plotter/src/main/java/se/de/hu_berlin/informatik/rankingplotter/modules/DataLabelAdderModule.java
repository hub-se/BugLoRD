/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.util.List;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plot;
import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingFileWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.DataTableCollection;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.DiffDataTableCollection;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * Module that takes a {@link List} of {@link RankingFileWrapper} objects, adds the data points
 * to the corresponding data tables, produces a label map and returns a {@link DataTableCollection}.
 * 
 * @author Simon Heiden
 */
public class DataLabelAdderModule extends AModule<List<RankingFileWrapper>, DataTableCollection> {

	private String localizerName;
	private Integer[] range;
	
	/**
	 * Creates a new {@link DataLabelAdderModule} object with the given parameters.
	 * @param localizerName
	 * identifier of the SBFL localizer
	 * @param range
	 * maximum value of data points that are plotted
	 */
	public DataLabelAdderModule(String localizerName, Integer[] range) {
		super(true);
		this.localizerName = localizerName;
		this.range = range;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public DataTableCollection processItem(List<RankingFileWrapper> rankingFiles) {
		//add data and labels for plotting
		DiffDataTableCollection tables = new DiffDataTableCollection();
		Integer temp = null;
		if (range == null) {
			temp = Plot.DEFAULT_RANGE;
		} else if (range != null && range.length > 1) {
			temp = range[1];
		} else if (range != null) {
			temp = range[0];
		}
		
		int fileno = 0;
		boolean muExists = false;
		tables.getLabelMap().put((double)fileno, localizerName);
		for (final RankingFileWrapper item : rankingFiles) {
			if (item.getLocalNLFL() > 0) {
				muExists = true;
				break;
			}
		}
		final int[] outlierCount = new int[rankingFiles.size()];
		for (final RankingFileWrapper item : rankingFiles) {
			++fileno;
			if (muExists) {
				tables.addLabel(fileno, item.getSBFL(), 1.0-item.getLocalNLFL());
			} else {
				tables.addLabel(fileno, item.getSBFL());
			}
			
			if (item.getRankings() != null) {
				for (Entry<Integer, List<ChangeWrapper>> entry : item.getLineToModMap().entrySet()) {
					int rank = entry.getKey();
					double parsedRank = item.getRankings()[rank-1];
					if (tables.addData(RankingFileWrapper.getHighestSignificanceLevel(entry.getValue()).toString(), 
							fileno, rank, 
							(double)(item.getLastAppearance().get(parsedRank) - rank), 
							(double)(rank - item.getFirstAppearance().get(parsedRank))) 
							&& rank > temp) {
						++outlierCount[fileno-1];
					}
				}
			} else {
				for (Entry<Integer, List<ChangeWrapper>> entry : item.getLineToModMap().entrySet()) {
					int rank = entry.getKey();
					if (tables.addData(RankingFileWrapper.getHighestSignificanceLevel(entry.getValue()).toString(), 
							fileno, rank) && rank > temp) {
						++outlierCount[fileno-1];
					}
				}
			}
			
		}

		tables.addOutlierData(temp, outlierCount);
		
		return tables;
	}

	
}
