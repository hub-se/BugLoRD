package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.rankingplotter.plotter.CombiningRankingsEH;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.SimpleRanking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking.RankingStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class RankingUtils {

	/**
	 * Computes the wasted effort metric of an element in the ranking. This is
	 * equal to the number of nodes that are ranked higher than the given
	 * element.
	 * @param ranking
	 * the ranking
	 * @param element
	 * the node to compute the metric for.
	 * @return number of nodes ranked higher as the given node.
	 * @param <T>
	 * the type of the elements
	 */
	public static <T> int wastedEffort(final Ranking<T> ranking, final T element) {
		return ranking.wastedEffort(element);
	}

	public static <T> double meanRankingValue(final Ranking<T> ranking) {
		double value = 0.0;
		int count = 0;
		for (T element : ranking.getElements()) {
			double rankingValue = ranking.getRankingValue(element);
			if (Double.isNaN(rankingValue)) {
				continue;
			}
			value += rankingValue;
			++count;
		}
		return value / count;
	}

	public static <T> Ranking<T> getCombinedRanking(Ranking<T> firstRanking, Ranking<T> secondRanking,
			double firstRankingPercentage) {
		return Ranking.combine(
				firstRanking, secondRanking,
				(k, v) -> (firstRankingPercentage * k + (100.0 - firstRankingPercentage) * v));
	}

	public static Ranking<SourceCodeBlock> getRanking(Entity bug, String suffix, String rankingIdentifier) {
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
			ranking = Ranking.load(
					sbflRankingFile, false, SourceCodeBlock::getNewBlockFromString, RankingStrategy.WORST,
					RankingStrategy.BEST, RankingStrategy.WORST);
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
}
