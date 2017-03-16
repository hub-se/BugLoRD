/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking.RankingStrategy;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.Producer;
import se.de.hu_berlin.informatik.utils.processors.sockets.eh.EHWithInputAndReturn;

/**
 * {@link EHWithInputAndReturn} object that ...
 * 
 * @author Simon Heiden
 */
public class CombiningRankingsEH extends AbstractProcessor<BuggyFixedEntity,RankingFileWrapper> {

	final private String localizer;
	final private ParserStrategy strategy;
	final private String[] sbflPercentages;
	final private NormalizationStrategy normStrategy;
	private String suffix;
	
	/**
	 * Initializes a {@link CombiningRankingsEH} object with the given parameters.
	 * @param suffix 
	 * a suffix to append to the ranking directory (may be null)
	 * @param localizer
	 * a fault localizer
	 * @param strategy
	 * which strategy to use. May take the lowest or the highest ranking of a range of 
	 * equal-value rankings or may compute the average
	 * @param sbflPercentages
	 * an array of percentage values that determine the weighting 
	 * of the SBFL ranking to the NLFL ranking
	 * @param normStrategy
	 * whether the rankings should be normalized before combining
	 */
	public CombiningRankingsEH(String suffix, String localizer, ParserStrategy strategy,
			String[] sbflPercentages, NormalizationStrategy normStrategy) {
		super();
		this.suffix = suffix;
		this.localizer = localizer;
		this.strategy = strategy;
		this.sbflPercentages = sbflPercentages;
		this.normStrategy = normStrategy;
	}

	@Override
	public RankingFileWrapper processItem(BuggyFixedEntity entity, Producer<RankingFileWrapper> producer) {
		Entity bug = entity.getBuggyVersion();
		
		Map<String, List<ChangeWrapper>> changeInformation = entity.loadChangesFromFile(); 
		
		double[] sBFLpercentages = {0.0, 10.0, 20.0, 50.0, 75.0, 90.0, 100.0};
		if (sbflPercentages != null) {
			sBFLpercentages = new double[sbflPercentages.length];
			for (int i = 0; i < sbflPercentages.length; ++i) {
				sBFLpercentages[i] = Double.parseDouble(sbflPercentages[i]);
			}
		}
		
		//TODO: change that for other benchmarks...
		String project = bug.getWorkDataDir().getParent().getParent().getFileName().toString();
		String bugDirName = bug.getWorkDataDir().getParent().getFileName().toString();
		int bugId = Integer.valueOf(bugDirName);
		for (double sbflPercentage : sBFLpercentages) {
			producer.produce(getRankingWrapper(
					suffix, entity, localizer, changeInformation,
					project, bugId, sbflPercentage, strategy, normStrategy));
		}
		
		return null;
	}

	public static RankingFileWrapper getRankingWrapper(String suffix, BuggyFixedEntity entity, String localizer,
			Map<String, List<ChangeWrapper>> changeInformation, String project, int bugId, double sbflPercentage, 
			ParserStrategy strategy, NormalizationStrategy normStrategy) {
		Entity bug = entity.getBuggyVersion();
		
		Path sbflRankingFile = bug.getWorkDataDir().resolve(suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : 
			BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix).resolve(localizer).resolve(BugLoRDConstants.FILENAME_RANKING_FILE);
		Ranking<String> sbflRanking = Ranking.load(sbflRankingFile, false, RankingStrategy.WORST, 
				RankingStrategy.BEST, RankingStrategy.WORST);
		
		Path lmRankingFile = bug.getWorkDataDir().resolve(suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : 
			BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix).resolve(BugLoRDConstants.FILENAME_LM_RANKING);
		Ranking<String>lmRanking = Ranking.load(lmRankingFile, false, RankingStrategy.ZERO,
				RankingStrategy.BEST, RankingStrategy.WORST);
		
		return getRankingFileWrapperFromRankings(sbflRanking, lmRanking, changeInformation, 
				sbflPercentage, strategy, normStrategy, project, bugId);
	}

	public static RankingFileWrapper getRankingFileWrapperFromRankings(Ranking<String> ranking1, Ranking<String> ranking2, 
			Map<String, List<ChangeWrapper>> changeInformation, double ranking1Percentage,
			ParserStrategy parserStrategy, NormalizationStrategy normStrategy, String project, int bugId) {
		Ranking<String> combinedRanking;
		if (normStrategy != null) {
			combinedRanking = getCombinedNormalizedRanking(ranking1, ranking2, ranking1Percentage, normStrategy);
		} else {
			combinedRanking = getCombinedRanking(ranking1, ranking2, ranking1Percentage);
		}
		
		return new RankingFileWrapper(project, bugId, combinedRanking, 
				ranking1Percentage, changeInformation, parserStrategy);
	}
	
	public static <T> Ranking<T> getCombinedRanking(Ranking<T> sbflRanking, Ranking<T> lmRanking, double sbflPercentage) {
		return Ranking.combine(sbflRanking, lmRanking, 
				(k,v) -> (sbflPercentage*k + ((100.0 - sbflPercentage)/10.0)*v)); //LM_rank div 10 !!
	}

	public static <T> Ranking<T> getCombinedNormalizedRanking(Ranking<T> sbflRanking, Ranking<T> lmRanking, 
			double sbflPercentage, NormalizationStrategy normStrategy2) {
		return Ranking.combine(sbflRanking, lmRanking, 
				(k,v) -> (sbflPercentage*k + (100.0 - sbflPercentage)*v), normStrategy2);
	}
	
}

