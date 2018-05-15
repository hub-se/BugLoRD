/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.modification.Modification;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;
import se.de.hu_berlin.informatik.utils.processors.sockets.eh.EHWithInputAndReturn;

/**
 * {@link EHWithInputAndReturn} object that ...
 * 
 * @author Simon Heiden
 */
public class CombiningRankingsEH extends AbstractProcessor<BuggyFixedEntity<?>, RankingFileWrapper> {

	final private ParserStrategy strategy;
	final private String[] ranking1Percentages;
	final private NormalizationStrategy normStrategy;
	final private String rankingIdentifier1;
	final private String rankingIdentifier2;
	private String suffix;

	/**
	 * Initializes a {@link CombiningRankingsEH} object with the given
	 * parameters.
	 * @param suffix
	 * a suffix to append to the ranking directory (may be null)
	 * @param rankingIdentifier1
	 * a fault localizer identifier or an lm ranking file name
	 * @param rankingIdentifier2
	 * a fault localizer identifier or an lm ranking file name
	 * @param strategy
	 * which strategy to use. May take the lowest or the highest ranking of a
	 * range of equal-value rankings or may compute the average
	 * @param ranking1Percentages
	 * an array of percentage values that determine the weighting of the first
	 * ranking to the second ranking
	 * @param normStrategy
	 * whether the rankings should be normalized before combining
	 */
	public CombiningRankingsEH(String suffix, String rankingIdentifier1, String rankingIdentifier2,
			ParserStrategy strategy, String[] ranking1Percentages, NormalizationStrategy normStrategy) {
		super();
		this.suffix = suffix;
		this.strategy = strategy;
		this.ranking1Percentages = ranking1Percentages;
		this.normStrategy = normStrategy;
		this.rankingIdentifier1 = rankingIdentifier1;
		this.rankingIdentifier2 = rankingIdentifier2;
	}

	@Override
	public RankingFileWrapper processItem(BuggyFixedEntity<?> entity,
			ProcessorSocket<BuggyFixedEntity<?>, RankingFileWrapper> socket) {
		Entity bug = entity.getBuggyVersion();

		Map<String, List<Modification>> changeInformation = entity.loadChangesFromFile();

		double[] ranking1percentages = { 0.0, 10.0, 20.0, 50.0, 75.0, 90.0, 100.0 };
		if (ranking1Percentages != null) {
			ranking1percentages = new double[ranking1Percentages.length];
			for (int i = 0; i < ranking1Percentages.length; ++i) {
				ranking1percentages[i] = Double.parseDouble(ranking1Percentages[i]);
			}
		}

		// TODO: change that for other benchmarks...
		String project = bug.getWorkDataDir().getParent().getParent().getFileName().toString();
		String bugDirName = bug.getWorkDataDir().getParent().getFileName().toString();
		int bugId = Integer.valueOf(bugDirName);

		Ranking<SourceCodeBlock> ranking1 = RankingUtils.getRanking(bug, suffix, rankingIdentifier1);
		if (ranking1 == null) {
			Log.abort(this, "Found no ranking with identifier '%s'.", rankingIdentifier1);
		}

		Ranking<SourceCodeBlock> ranking2 = RankingUtils.getRanking(bug, suffix, rankingIdentifier2);
		if (ranking2 == null) {
			Log.abort(this, "Found no ranking with identifier '%s'.", rankingIdentifier2);
		}

		if (normStrategy != null) {
			ranking1 = Ranking.normalize(ranking1, normStrategy);
			ranking2 = Ranking.normalize(ranking2, normStrategy);
		}

		for (double percentage : ranking1percentages) {
			socket.produce(
					getRankingFileWrapperFromRankings(
							ranking1, ranking2, changeInformation, percentage, strategy, project, bugId));
		}

		return null;
	}

	public static RankingFileWrapper getRankingFileWrapperFromRankings(Ranking<SourceCodeBlock> ranking1,
			Ranking<SourceCodeBlock> ranking2, Map<String, List<Modification>> changeInformation, double ranking1Percentage,
			ParserStrategy parserStrategy, String project, int bugId) {
		Ranking<SourceCodeBlock> combinedRanking = RankingUtils.getCombinedRanking(ranking1, ranking2, ranking1Percentage);

		return new RankingFileWrapper(project, bugId, combinedRanking, ranking1Percentage, changeInformation,
				parserStrategy);
	}

	

	// public static <T> Ranking<T> getCombinedNormalizedRanking(Ranking<T>
	// sbflRanking, Ranking<T> lmRanking,
	// double sbflPercentage, NormalizationStrategy normStrategy2) {
	// return Ranking.combine(
	// sbflRanking, lmRanking, (k, v) -> (sbflPercentage * k + (100.0 -
	// sbflPercentage) * v), normStrategy2);
	// }

}
