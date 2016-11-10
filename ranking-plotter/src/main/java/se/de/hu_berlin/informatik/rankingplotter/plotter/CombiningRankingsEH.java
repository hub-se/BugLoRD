/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JConstants;
import se.de.hu_berlin.informatik.benchmark.ranking.NormalizedRanking;
import se.de.hu_berlin.informatik.benchmark.ranking.Ranking;
import se.de.hu_berlin.informatik.benchmark.ranking.Ranking.RankingStrategy;
import se.de.hu_berlin.informatik.benchmark.ranking.SimpleMarkedRanking;
import se.de.hu_berlin.informatik.benchmark.ranking.SimpleNormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * {@link EHWithInputAndReturn} object that ...
 * 
 * @author Simon Heiden
 */
public class CombiningRankingsEH extends EHWithInputAndReturn<BuggyFixedEntity,RankingFileWrapper> {

	final private String localizer;
	final private ParserStrategy strategy;
	final private String[] sbflPercentages;
	final private boolean normalized;
	
	/**
	 * Initializes a {@link CombiningRankingsEH} object with the given parameters.
	 * @param localizer
	 * a fault localizer
	 * @param strategy
	 * which strategy to use. May take the lowest or the highest ranking of a range of 
	 * equal-value rankings or may compute the average
	 * @param sbflPercentages
	 * an array of percentage values that determine the weighting 
	 * of the SBFL ranking to the NLFL ranking
	 * @param normalized
	 * whether the rankings should be normalized before combining
	 */
	public CombiningRankingsEH(String localizer, ParserStrategy strategy,
			String[] sbflPercentages, boolean normalized) {
		super();
		this.localizer = localizer;
		this.strategy = strategy;
		this.sbflPercentages = sbflPercentages;
		this.normalized = normalized;
	}

	@Override
	public RankingFileWrapper processInput(BuggyFixedEntity entity) {
		
		Path sbflRankingFile = entity.getWorkDataDir().resolve(Defects4JConstants.DIR_NAME_RANKING).resolve(localizer).resolve(Defects4JConstants.FILENAME_RANKING_FILE);
		Ranking<String> sbflRanking = Ranking.load(sbflRankingFile, false, RankingStrategy.WORST, 
				RankingStrategy.BEST, RankingStrategy.WORST);
		
		Path lmRankingFile = entity.getWorkDataDir().resolve(Defects4JConstants.DIR_NAME_RANKING).resolve(Defects4JConstants.FILENAME_LM_RANKING);
		Ranking<String>lmRanking = Ranking.load(lmRankingFile, false, RankingStrategy.ZERO,
				RankingStrategy.BEST, RankingStrategy.WORST);

		
		Map<String, List<ChangeWrapper>> changeInformation = entity.loadChangesFromFile(); 
		
		double[] sBFLpercentages = {0.0, 10.0, 20.0, 50.0, 75.0, 90.0, 100.0};
		if (sbflPercentages != null) {
			sBFLpercentages = new double[sbflPercentages.length];
			for (int i = 0; i < sbflPercentages.length; ++i) {
				sBFLpercentages[i] = Double.parseDouble(sbflPercentages[i]);
			}
		}
		
		//TODO: change that for other benchmarks...
		String project = entity.getWorkDataDir().getParent().getParent().getFileName().toString();
		String bugDirName = entity.getWorkDataDir().getParent().getFileName().toString();
		int bugId = Integer.valueOf(bugDirName);
		for (double sbflPercentage : sBFLpercentages) {
			manualOutput(getRankingWrapper(
					sbflRanking, lmRanking, changeInformation,
					project, bugId, sbflPercentage, strategy, normalized));
		}
		
		return null;
	}

	public static RankingFileWrapper getRankingWrapper(Ranking<String> sbflRanking, Ranking<String> lmRanking,
			Map<String, List<ChangeWrapper>> changeInformation, String project, int bugId, double sbflPercentage, 
			ParserStrategy strategy, boolean normalized) {
		Ranking<String> combinedRanking;
		if (normalized) {
			combinedRanking = getCombinedNormalizedRanking(sbflRanking, lmRanking, sbflPercentage);
		} else {
			combinedRanking = getCombinedRanking(sbflRanking, lmRanking, sbflPercentage);
		}
		return new RankingFileWrapper(project, bugId, new SimpleMarkedRanking<>(combinedRanking), 
				sbflPercentage, changeInformation, strategy);
	}

	public static <T> Ranking<T> getCombinedRanking(Ranking<T> sbflRanking, Ranking<T> lmRanking, double sbflPercentage) {
		return Ranking.combine(sbflRanking, lmRanking, 
				(k,v) -> (sbflPercentage*k + ((100.0 - sbflPercentage)/10.0)*v)); //LM_rank div 10 !!
	}

	public static <T> Ranking<T> getCombinedNormalizedRanking(Ranking<T> sbflRanking, Ranking<T> lmRanking, double sbflPercentage) {
		return NormalizedRanking.combine(sbflRanking, lmRanking, 
				(k,v) -> (sbflPercentage*k + (100.0 - sbflPercentage)*v), NormalizationStrategy.ZeroToOne);
	}
	
	public static class Factory extends EHWithInputAndReturnFactory<BuggyFixedEntity,RankingFileWrapper> {

		final private String localizer;
		final private ParserStrategy strategy;
		final private String[] sbflPercentages;
		final private boolean normalized;
		
		/**
		 * Initializes a {@link Factory} object with the given parameters.
		 * @param localizer
		 * a fault localizer
		 * @param strategy
		 * which strategy to use. May take the lowest or the highest ranking of a range of 
		 * equal-value rankings or may compute the average
		 * @param sbflPercentages
		 * an array of percentage values that determine the weighting 
		 * of the SBFL ranking to the NLFL ranking
		 * @param normalized
		 * whether the rankings should be normalized before combining
		 */
		public Factory(String localizer, ParserStrategy strategy,
				String[] sbflPercentages, boolean normalized) {
			super(CombiningRankingsEH.class);
			this.localizer = localizer;
			this.strategy = strategy;
			this.sbflPercentages = sbflPercentages;
			this.normalized = normalized;
		}

		@Override
		public EHWithInputAndReturn<BuggyFixedEntity, RankingFileWrapper> newFreshInstance() {
			return new CombiningRankingsEH(localizer, strategy, sbflPercentages, normalized);
		}

	}

	@Override
	public void resetAndInit() {
		//not needed
	}
}

