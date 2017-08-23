/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.SimpleRanking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking.RankingStrategy;
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

		Map<String, List<ChangeWrapper>> changeInformation = entity.loadChangesFromFile();

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

		Ranking<SourceCodeBlock> ranking1 = getRanking(bug, suffix, rankingIdentifier1);
		if (ranking1 == null) {
			Log.abort(this, "Found no ranking with identifier '%s'.", rankingIdentifier1);
		}

		Ranking<SourceCodeBlock> ranking2 = getRanking(bug, suffix, rankingIdentifier2);
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

	private static Ranking<SourceCodeBlock> getRanking(Entity bug, String suffix, String rankingIdentifier) {
		Ranking<SourceCodeBlock> ranking = null;

		ranking = tryToGetRanking(bug, suffix, rankingIdentifier.toLowerCase(Locale.getDefault()));
		
		if (ranking == null && !rankingIdentifier.toLowerCase(Locale.getDefault()).equals(rankingIdentifier)) {
			ranking = tryToGetRanking(bug, suffix, rankingIdentifier);
		}

		return ranking;
	}

	public static Ranking<SourceCodeBlock> tryToGetRanking(Entity bug, String suffix, String rankingIdentifier) {
		Ranking<SourceCodeBlock> ranking;
		Path sbflRankingFile = bug.getWorkDataDir().resolve(
				suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
				.resolve(rankingIdentifier).resolve(BugLoRDConstants.FILENAME_RANKING_FILE);

		if (sbflRankingFile.toFile().exists()) {
			// identifier is an SBFL ranking
			ranking = Ranking
					.load(sbflRankingFile, false, SourceCodeBlock::getNewBlockFromString, 
							RankingStrategy.WORST, RankingStrategy.BEST, RankingStrategy.WORST);
		} else {
			// identifier is (probably) an lm ranking
			String lmRankingFileDir = bug.getWorkDataDir()
					.resolve(
							suffix == null ? BugLoRDConstants.DIR_NAME_RANKING
									: BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
					.resolve(BugLoRDConstants.DIR_NAME_LM_RANKING).toString();

			Path path = Paths.get(lmRankingFileDir).resolve(rankingIdentifier);
			if (!path.toFile().exists()) {
				return null;
			}

			Path traceFile = bug.getWorkDataDir()
					.resolve(
							suffix == null ? BugLoRDConstants.DIR_NAME_RANKING
									: BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
					.resolve(BugLoRDConstants.FILENAME_TRACE_FILE);

			ranking = createCompleteRanking(traceFile, Paths.get(lmRankingFileDir).resolve(rankingIdentifier));
		}
		return ranking;
	}

	private static Ranking<SourceCodeBlock> createCompleteRanking(Path traceFile, Path globalRankingFile) {
		Ranking<SourceCodeBlock> ranking = new SimpleRanking<>(false);
		try (BufferedReader traceFileReader = Files.newBufferedReader(traceFile, StandardCharsets.UTF_8);
				BufferedReader rankingFileReader = Files.newBufferedReader(globalRankingFile, StandardCharsets.UTF_8)) {
			String traceLine;
			String rankingLine;
			while ((traceLine = traceFileReader.readLine()) != null
					&& (rankingLine = rankingFileReader.readLine()) != null) {
				double rankingValue;
				if (rankingLine.equals("nan")) {
					// rankingValue = Double.NaN;
					rankingValue = 0.0;
				} else {
					rankingValue = Double.valueOf(rankingLine);
				}
				ranking.add(SourceCodeBlock.getNewBlockFromString(traceLine), rankingValue);
			}
		} catch (IOException e) {
			Log.abort(CombiningRankingsEH.class, e, "Could not read trace file or lm ranking file.");
			return ranking;
		}

		return ranking;
	}

	public static RankingFileWrapper getRankingFileWrapperFromRankings(Ranking<SourceCodeBlock> ranking1,
			Ranking<SourceCodeBlock> ranking2, Map<String, List<ChangeWrapper>> changeInformation, double ranking1Percentage,
			ParserStrategy parserStrategy, String project, int bugId) {
		Ranking<SourceCodeBlock> combinedRanking = getCombinedRanking(ranking1, ranking2, ranking1Percentage);

		return new RankingFileWrapper(project, bugId, combinedRanking, ranking1Percentage, changeInformation,
				parserStrategy);
	}

	public static <T> Ranking<T> getCombinedRanking(Ranking<T> sbflRanking, Ranking<T> lmRanking,
			double sbflPercentage) {
		return Ranking.combine(sbflRanking, lmRanking, (k, v) -> (sbflPercentage * k + (100.0 - sbflPercentage) * v));
	}

	// public static <T> Ranking<T> getCombinedNormalizedRanking(Ranking<T>
	// sbflRanking, Ranking<T> lmRanking,
	// double sbflPercentage, NormalizationStrategy normStrategy2) {
	// return Ranking.combine(
	// sbflRanking, lmRanking, (k, v) -> (sbflPercentage * k + (100.0 -
	// sbflPercentage) * v), normStrategy2);
	// }

}
