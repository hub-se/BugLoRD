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
import java.util.Map;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
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
public class CombiningRankingsEH extends AbstractProcessor<BuggyFixedEntity, RankingFileWrapper> {

	final private String localizer;
	final private ParserStrategy strategy;
	final private String[] sbflPercentages;
	final private NormalizationStrategy normStrategy;
	final private String lmRankingFileName;
	private String suffix;

	/**
	 * Initializes a {@link CombiningRankingsEH} object with the given
	 * parameters.
	 * @param suffix
	 * a suffix to append to the ranking directory (may be null)
	 * @param localizer
	 * a fault localizer
	 * @param strategy
	 * which strategy to use. May take the lowest or the highest ranking of a
	 * range of equal-value rankings or may compute the average
	 * @param sbflPercentages
	 * an array of percentage values that determine the weighting of the SBFL
	 * ranking to the NLFL ranking
	 * @param normStrategy
	 * whether the rankings should be normalized before combining
	 * @param lmRankingFileName
	 * the file name of the lm ranking file
	 */
	public CombiningRankingsEH(String suffix, String localizer, ParserStrategy strategy, String[] sbflPercentages,
			NormalizationStrategy normStrategy, String lmRankingFileName) {
		super();
		this.suffix = suffix;
		this.localizer = localizer;
		this.strategy = strategy;
		this.sbflPercentages = sbflPercentages;
		this.normStrategy = normStrategy;
		this.lmRankingFileName = lmRankingFileName;
	}

	@Override
	public RankingFileWrapper processItem(BuggyFixedEntity entity,
			ProcessorSocket<BuggyFixedEntity, RankingFileWrapper> socket) {
		Entity bug = entity.getBuggyVersion();

		Map<String, List<ChangeWrapper>> changeInformation = entity.loadChangesFromFile();

		double[] sBFLpercentages = { 0.0, 10.0, 20.0, 50.0, 75.0, 90.0, 100.0 };
		if (sbflPercentages != null) {
			sBFLpercentages = new double[sbflPercentages.length];
			for (int i = 0; i < sbflPercentages.length; ++i) {
				sBFLpercentages[i] = Double.parseDouble(sbflPercentages[i]);
			}
		}

		// TODO: change that for other benchmarks...
		String project = bug.getWorkDataDir().getParent().getParent().getFileName().toString();
		String bugDirName = bug.getWorkDataDir().getParent().getFileName().toString();
		int bugId = Integer.valueOf(bugDirName);

		Path sbflRankingFile = bug.getWorkDataDir().resolve(
				suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
				.resolve(localizer).resolve(BugLoRDConstants.FILENAME_RANKING_FILE);
		Ranking<String> sbflRanking = Ranking
				.load(sbflRankingFile, false, RankingStrategy.WORST, RankingStrategy.BEST, RankingStrategy.WORST);

		Path traceFile = bug.getWorkDataDir().resolve(
				suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
				.resolve(BugLoRDConstants.FILENAME_TRACE_FILE);

		String lmRankingFileDir = bug.getWorkDataDir().resolve(
				suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
				.resolve(BugLoRDConstants.DIR_NAME_LM_RANKING).toString();

		Ranking<String> lmRanking = createCompleteRanking(
				traceFile, Paths.get(lmRankingFileDir).resolve(lmRankingFileName));

		if (normStrategy != null) {
			sbflRanking = Ranking.normalize(sbflRanking, normStrategy);
			lmRanking = Ranking.normalize(lmRanking, normStrategy);
		}

		for (double sbflPercentage : sBFLpercentages) {
			socket.produce(
					getRankingFileWrapperFromRankings(
							sbflRanking, lmRanking, changeInformation, sbflPercentage, strategy, project, bugId));
		}

		return null;
	}

	private static Ranking<String> createCompleteRanking(Path traceFile, Path globalRankingFile) {
		Ranking<String> ranking = new SimpleRanking<>(false);
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
				ranking.add(traceLine, rankingValue);
			}
		} catch (IOException e) {
			Log.abort(CombiningRankingsEH.class, e, "Could not read trace file or lm ranking file.");
			return ranking;
		}

		return ranking;
	}

	public static RankingFileWrapper getRankingFileWrapperFromRankings(Ranking<String> ranking1,
			Ranking<String> ranking2, Map<String, List<ChangeWrapper>> changeInformation, double ranking1Percentage,
			ParserStrategy parserStrategy, String project, int bugId) {
		Ranking<String> combinedRanking = getCombinedRanking(ranking1, ranking2, ranking1Percentage);

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
