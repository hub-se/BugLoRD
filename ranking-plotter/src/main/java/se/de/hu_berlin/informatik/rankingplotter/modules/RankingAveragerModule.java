/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plot;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingFileWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.DataTableCollection;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.DiffDataTableCollection;
import se.de.hu_berlin.informatik.utils.threaded.ExecutorServiceProvider;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * Module that takes a {@link List} of {@link RankingFileWrapper} objects,  adds the data points
 * to the corresponding data tables, produces a label map and returns a {@link DataTableCollection}.
 * 
 * @author Simon Heiden
 */
public class RankingAveragerModule extends AModule<List<RankingFileWrapper>, DataTableCollection> {

	private String localizerName;
	private Integer[] range;
	
	private List<RankingFileWrapper> averagedRankings;
	private boolean firstInput = true;
	
	/**
	 * Creates a new {@link RankingAveragerModule} object with the given parameters.
	 * @param localizerName
	 * identifier of the SBFL localizer
	 * @param range
	 * maximum value of data points that are plotted
	 */
	public RankingAveragerModule(String localizerName, Integer[] range) {
		super(true);
		this.localizerName = localizerName;
		this.range = range;
		averagedRankings = new ArrayList<>();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public DataTableCollection processItem(List<RankingFileWrapper> rankingFiles) {
		//create an empty ranking file wrapper that will hold the averaged rankings, etc.
		//only need to create this once! (and can't create it at initialization...)
		if (firstInput ) {
			for (final RankingFileWrapper item : rankingFiles) {
				averagedRankings.add(new RankingFileWrapper(null, 
						item.getSBFLPercentage(), 
						item.getGlobalNLFLPercentage(), 
						item.getLocalNLFLPercentage(), 
						false, ParserStrategy.NO_CHANGE, false, false));
			}
			firstInput = false;
		}
		
		ExecutorServiceProvider provider = new ExecutorServiceProvider(5);

		//update the averaged rankings
		int fileno = 0;
		for (final RankingFileWrapper item : rankingFiles) {
			provider.getExecutorService()
			.submit(new RankingUpdateCall(averagedRankings.get(fileno), item));
			++fileno;
		}
		
		provider.shutdownAndWaitForTermination(false);
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#getResultFromCollectedItems()
	 */
	public DataTableCollection getResultFromCollectedItems() {
		DiffDataTableCollection tables = new DiffDataTableCollection();
		Integer temp = null;
		if (range == null) {
			temp = Plot.DEFAULT_RANGE;
		} else if (range != null && range.length > 1) {
			temp = range[1];
		} else if (range != null) {
			temp = range[0];
		}
		
		//set the labels
		int fileno = 0;
		boolean muExists = false;
		tables.getLabelMap().put((double)fileno, localizerName);
		for (final RankingFileWrapper item : averagedRankings) {
			if (item.getLocalNLFL() > 0) {
				muExists = true;
				break;
			}
		}
		for (final RankingFileWrapper item : averagedRankings) {
			++fileno;
			if (muExists) {
				tables.addLabel(fileno, item.getSBFL(), 1.0-item.getLocalNLFL());
			} else {
				tables.addLabel(fileno, item.getSBFL());
			}
		}
		
		//add the data points to the tables
		final int[] outlierCount = new int[averagedRankings.size()];
		fileno = 0;
		for (final RankingFileWrapper averagedRanking : averagedRankings) {
			++fileno;
			double rank;
			
			tables.addData(DiffDataTableCollection.HIT_AT_1, fileno, averagedRanking.getHitAtXMap().get(1));
			tables.addData(DiffDataTableCollection.HIT_AT_5, fileno, averagedRanking.getHitAtXMap().get(5));
			tables.addData(DiffDataTableCollection.HIT_AT_10, fileno, averagedRanking.getHitAtXMap().get(10));
			tables.addData(DiffDataTableCollection.HIT_AT_20, fileno, averagedRanking.getHitAtXMap().get(20));
			tables.addData(DiffDataTableCollection.HIT_AT_30, fileno, averagedRanking.getHitAtXMap().get(30));
			tables.addData(DiffDataTableCollection.HIT_AT_50, fileno, averagedRanking.getHitAtXMap().get(50));
			tables.addData(DiffDataTableCollection.HIT_AT_100, fileno, averagedRanking.getHitAtXMap().get(100));
			tables.addData(DiffDataTableCollection.HIT_AT_INF, fileno, averagedRanking.getHitAtXMap().get(Integer.MAX_VALUE));
			
			if (averagedRanking.getMinRankSum() > 0) {
				rank = averagedRanking.getMeanFirstRank();
				if (tables.addData(ChangeWrapper.MEAN_FIRST_RANK, fileno, rank) && rank > temp) {
//					++outlierCount[fileno-1];
				}
			}
			if (averagedRanking.getAll() > 0) {
				rank = averagedRanking.getAllAverage();
				if (tables.addData(ChangeWrapper.SIGNIFICANCE_ALL, fileno, rank) && rank > temp) {
//					++outlierCount[fileno-1];
				}
			}
			if (averagedRanking.getUnsignificantChanges() > 0) {
				rank = averagedRanking.getUnsignificantChangesAverage();
				if (tables.addData(ChangeWrapper.SIGNIFICANCE_NONE, fileno, rank) && rank > temp) {
					++outlierCount[fileno-1];
				}
			}
			if (averagedRanking.getLowSignificanceChanges() > 0) {
				rank = averagedRanking.getLowSignificanceChangesAverage();
				if (tables.addData(ChangeWrapper.SIGNIFICANCE_LOW, fileno, rank) && rank > temp) {
					++outlierCount[fileno-1];
				}
			}
			if (averagedRanking.getMediumSignificanceChanges() > 0) {
				rank = averagedRanking.getMediumSignificanceChangesAverage();
				if (tables.addData(ChangeWrapper.SIGNIFICANCE_MEDIUM, fileno, rank) && rank > temp) {
					++outlierCount[fileno-1];
				}
			}
			if (averagedRanking.getHighSignificanceChanges() > 0) {
				rank = averagedRanking.getHighSignificanceChangesAverage();
				if (tables.addData(ChangeWrapper.SIGNIFICANCE_HIGH, fileno, rank) && rank > temp) {
					++outlierCount[fileno-1];
				}
			}
			if (averagedRanking.getCrucialSignificanceChanges() > 0) {
				rank = averagedRanking.getCrucialSignificanceChangesAverage();
				if (tables.addData(ChangeWrapper.SIGNIFICANCE_CRUCIAL, fileno, rank) && rank > temp) {
					++outlierCount[fileno-1];
				}
			}
			
			
			if (averagedRanking.getModChanges() > 0) {
				tables.addData(ChangeWrapper.MOD_CHANGE, fileno, averagedRanking.getModChangesAverage());
			}
			
			if (averagedRanking.getModDeletes() > 0) {
				tables.addData(ChangeWrapper.MOD_DELETE, fileno, averagedRanking.getModDeletesAverage());
			}
			
			if (averagedRanking.getModUnknowns() > 0) {
				tables.addData(ChangeWrapper.MOD_UNKNOWN, fileno, averagedRanking.getModUnknownsAverage());
			}
			
			if (averagedRanking.getModInserts() > 0) {
				tables.addData(ChangeWrapper.MOD_INSERT, fileno, averagedRanking.getModInsertsAverage());
			}
			
		}
		
		tables.addOutlierData(temp, outlierCount);
		
		return tables;
	}

	private class RankingUpdateCall implements Runnable {

		final private RankingFileWrapper ar;
		final private RankingFileWrapper item;
		
		
		public RankingUpdateCall(RankingFileWrapper ar, RankingFileWrapper item) {
			super();
			this.ar = ar;
			this.item = item;
		}

		@Override
		public void run() {
			if (item.getMinRank() != Integer.MAX_VALUE) {
				ar.addToMinRankSum(item.getMinRank());
				ar.addToMinRankCount(1);
			}
			
			for (Entry<Integer,Integer> entry : item.getHitAtXMap().entrySet()) {
				int key = entry.getKey();
				ar.getHitAtXMap().put(key, entry.getValue() + ar.getHitAtXMap().get(key));
			}
			
			ar.addToAllSum(item.getAllSum());
			ar.addToAll(item.getAll());
			
			ar.addToUnsignificantChangesSum(item.getUnsignificantChangesSum());
			ar.addToUnsignificantChanges(item.getUnsignificantChanges());
			
			ar.addToLowSignificanceChangesSum(item.getLowSignificanceChangesSum());
			ar.addToLowSignificanceChanges(item.getLowSignificanceChanges());
			
			ar.addToMediumSignificanceChangesSum(item.getMediumSignificanceChangesSum());
			ar.addToMediumSignificanceChanges(item.getMediumSignificanceChanges());
			
			ar.addToHighSignificanceChangesSum(item.getHighSignificanceChangesSum());
			ar.addToHighSignificanceChanges(item.getHighSignificanceChanges());
			
			ar.addToCrucialSignificanceChangesSum(item.getCrucialSignificanceChangesSum());
			ar.addToCrucialSignificanceChanges(item.getCrucialSignificanceChanges());
			
			
			ar.addToModChangesSum(item.getModChangesSum());
			ar.addToModChanges(item.getModChanges());
			
			ar.addToModDeletesSum(item.getModDeletesSum());
			ar.addToModDeletes(item.getModDeletes());
			
			ar.addToModInsertsSum(item.getModInsertsSum());
			ar.addToModInserts(item.getModInserts());
			
			ar.addToModUnknownsSum(item.getModUnknownsSum());
			ar.addToModUnknowns(item.getModUnknowns());
		}
		
	}
}
