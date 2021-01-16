package se.de.hu_berlin.informatik.rankingplotter.plotter;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.experiments.ranking.RankedElement;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking.RankingValueReplacementStrategy;
import se.de.hu_berlin.informatik.utils.experiments.ranking.RankingMetric;
import se.de.hu_berlin.informatik.utils.experiments.ranking.SimpleRanking;
import se.de.hu_berlin.informatik.utils.miscellaneous.FromString;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RankingUtils {

    /**
     * Computes the wasted effort metric of an element in the ranking. This is
     * equal to the number of nodes that are ranked higher than the given
     * element.
     *
     * @param ranking the ranking
     * @param element the node to compute the metric for.
     * @param <T>     the type of the elements
     * @return number of nodes ranked higher as the given node.
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

    public static <T extends FromString<T>> Ranking<T> getRanking(T dummy, Entity bug, String suffix, String rankingIdentifier) {
        return getRanking(dummy, null, bug, suffix, rankingIdentifier);
    }

    public static <T extends FromString<T>> Ranking<T> getRanking(T dummy, Path bugDir, Entity bug, String suffix, String rankingIdentifier) {
        Ranking<T> ranking = null;

        ranking = tryToGetRanking(dummy, bugDir, bug, suffix, rankingIdentifier.toLowerCase(Locale.getDefault()));

        if (ranking == null && !rankingIdentifier.toLowerCase(Locale.getDefault()).equals(rankingIdentifier)) {
            ranking = tryToGetRanking(dummy, bugDir, bug, suffix, rankingIdentifier);
        }

        return ranking;
    }

    public static <T extends FromString<T>> Ranking<T> tryToGetRanking(T dummy, Entity bug, String suffix, String rankingIdentifier) {
        return tryToGetRanking(dummy, null, bug, suffix, rankingIdentifier);
    }

    public static <T extends FromString<T>> Ranking<T> tryToGetRanking(T dummy, Path bugDir, Entity bug, String suffix, String rankingIdentifier) {
        Ranking<T> ranking;
        Path sbflRankingFile;
        // identifier might be an SBFL ranking that is based on a trace file
        if (bugDir == null) {
            sbflRankingFile = bug.getWorkDataDir().resolve(
                    suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
                    .resolve(rankingIdentifier).resolve(BugLoRDConstants.FILENAME_TRACE_RANKING_FILE);
        } else {
            sbflRankingFile = bugDir.resolve(
                    suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
                    .resolve(rankingIdentifier).resolve(BugLoRDConstants.FILENAME_TRACE_RANKING_FILE);
        }

        if (sbflRankingFile.toFile().exists()) {
            // identifier is a trace based SBFL ranking
            Path traceFile = bug.getWorkDataDir()
                    .resolve(
                            suffix == null ? BugLoRDConstants.DIR_NAME_RANKING
                                    : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
                    .resolve(BugLoRDConstants.getTraceFileFileName(null));

            ranking = createCompleteRanking(dummy, traceFile, sbflRankingFile);
        } else {
            // identifier might be a "normal" sbfl ranking file
            if (bugDir == null) {
                sbflRankingFile = bug.getWorkDataDir().resolve(
                        suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
                        .resolve(rankingIdentifier).resolve(BugLoRDConstants.FILENAME_RANKING_FILE);
                System.out.println("------- Filename resolved -------"); //PT
            } else {
                sbflRankingFile = bugDir.resolve(
                        suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
                        .resolve(rankingIdentifier).resolve(BugLoRDConstants.FILENAME_RANKING_FILE);
                System.out.println("------- Filename resolved -------"); //PT
            }

            if (sbflRankingFile.toFile().exists()) {
                // identifier is an SBFL ranking
                ranking = Ranking.load(
                        sbflRankingFile, false, dummy::getFromString, RankingValueReplacementStrategy.WORST,
                        RankingValueReplacementStrategy.BEST, RankingValueReplacementStrategy.WORST);
            } else {
                // identifier is (probably) an lm ranking or a combined ranking
                String lmRankingFileDir = bug.getWorkDataDir()
                        .resolve(
                                suffix == null ? BugLoRDConstants.DIR_NAME_RANKING
                                        : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
                        .resolve(BugLoRDConstants.DIR_NAME_LM_RANKING).toString();

                Path path = Paths.get(lmRankingFileDir).resolve(rankingIdentifier);
                if (path.toFile().exists()) {
                    // identifier is an lm ranking
                    Path traceFile = bug.getWorkDataDir()
                            .resolve(
                                    suffix == null ? BugLoRDConstants.DIR_NAME_RANKING
                                            : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
                            .resolve(BugLoRDConstants.getTraceFileFileName(null));

                    ranking = createCompleteRanking(dummy, traceFile, Paths.get(lmRankingFileDir).resolve(rankingIdentifier));
                } else {
                    // identifier is (probably) a combined ranking
                    String combinedRankingFileDir = bug.getWorkDataDir()
                            .resolve(
                                    suffix == null ? BugLoRDConstants.DIR_NAME_RANKING
                                            : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
                            .resolve(BugLoRDConstants.DIR_NAME_COMBINED_RANKING).toString();

                    path = Paths.get(combinedRankingFileDir).resolve(rankingIdentifier);
                    if (!path.toFile().exists()) {
                        return null;
                    }

                    ranking = Ranking.load(
                            path, false, dummy::getFromString, RankingValueReplacementStrategy.WORST,
                            RankingValueReplacementStrategy.BEST, RankingValueReplacementStrategy.WORST);
                }
            }
        }
        return ranking;
    }

    private static <T extends FromString<T>> Ranking<T> createCompleteRanking(T dummy, Path traceFile, Path globalRankingFile) {
        Ranking<T> ranking = new SimpleRanking<>(false);
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
                ranking.add(dummy.getFromString(traceLine), rankingValue);
            }
        } catch (IOException e) {
            Log.abort(RankingUtils.class, e, "Could not read trace file or lm ranking file.");
        }

        return ranking;
    }

    public static SourceCodeBlockRankingMetrics getSourceCodeBlockRankingMetrics(Ranking<SourceCodeBlock> ranking, SourceCodeBlock element) {
        if (!ranking.getElements().contains(element)) {
            Log.abort(RankingUtils.class, "Could not find element '%s' in ranking.", element);
        }

        RankingMetric<SourceCodeBlock> rankingMetrics = ranking.getRankingMetrics(element);

        List<RankedElement<SourceCodeBlock>> elements = ranking.getSortedRankedElements();

        int bestRanking = rankingMetrics.getBestRanking();
        int worstRanking = rankingMetrics.getWorstRanking();
        int counter = 0;

        int minFiles = 0;
        int maxFiles = 0;
        Set<String> paths = new HashSet<>();
        int minMethods = 0;
        int maxMethods = 0;
        Set<String> methods = new HashSet<>();
        for (RankedElement<SourceCodeBlock> item : elements) {
            ++counter;
            paths.add(item.getElement().getFilePath());
            methods.add(item.getElement().getFilePath() + item.getElement().getMethodName());

            if (counter == bestRanking) {
                minFiles = paths.size();
                minMethods = methods.size();
            }
            if (counter == worstRanking) {
                maxFiles = paths.size();
                maxMethods = methods.size();
                break;
            }
        }

        return new SourceCodeBlockRankingMetrics(minFiles, maxFiles, minMethods, maxMethods);
    }

    public static class SourceCodeBlockRankingMetrics {

        private final int minFiles;
        private final int maxFiles;
        private final int minMethods;
        private final int maxMethods;

        public SourceCodeBlockRankingMetrics(int minFiles, int maxFiles, int minMethods, int maxMethods) {
            this.minFiles = minFiles;
            this.maxFiles = maxFiles;
            this.minMethods = minMethods;
            this.maxMethods = maxMethods;
        }


        public int getMinFiles() {
            return minFiles;
        }


        public int getMaxFiles() {
            return maxFiles;
        }


        public int getMinMethods() {
            return minMethods;
        }


        public int getMaxMethods() {
            return maxMethods;
        }

    }
}
