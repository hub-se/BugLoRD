/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.util.List;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.benchmark.ranking.RankingMetric;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingFileWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.StatisticsCollection;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Module that takes a {@link List} of {@link RankingFileWrapper} objects, adds the data points
 * to the corresponding data tables and returns a {@link StatisticsCollection}.
 * 
 * @author Simon Heiden
 */
public class DataAdderModule extends AbstractModule<List<RankingFileWrapper>, StatisticsCollection> {

	/**
	 * Creates a new {@link DataAdderModule} object with the given parameters.
	 */
	public DataAdderModule() {
		super(true);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public StatisticsCollection processItem(List<RankingFileWrapper> rankingFiles) {
		//add data and labels for plotting
		StatisticsCollection tables = new StatisticsCollection();

		for (final RankingFileWrapper item : rankingFiles) {
			double sbflPercentage = item.getSBFL();

			if (item.getRanking() != null) {
				for (Entry<String, List<ChangeWrapper>> entry : item.getLineToModMap().entrySet()) {
					RankingMetric<String> metric = item.getRanking().getRankingMetrics(entry.getKey());
					tables.addValuePair(RankingFileWrapper.getHighestSignificanceLevel(entry.getValue()).toString(),
							sbflPercentage, Double.valueOf(metric.getRanking()), 
							Double.valueOf(metric.getBestRanking()), Double.valueOf(metric.getWorstRanking()));
				}
				item.getRanking().outdateRankingCache();
			}
			
		}

		return tables;
	}

	
}
