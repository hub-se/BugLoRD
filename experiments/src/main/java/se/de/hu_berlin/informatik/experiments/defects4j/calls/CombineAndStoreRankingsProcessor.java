package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingUtils;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;
import se.de.hu_berlin.informatik.utils.processors.sockets.eh.EHWithInputAndReturn;

import java.io.IOException;
import java.nio.file.Path;

/**
 * {@link EHWithInputAndReturn} object that ...
 *
 * @author Simon Heiden
 */
public class CombineAndStoreRankingsProcessor extends AbstractProcessor<BuggyFixedEntity<?>, BuggyFixedEntity<?>> {

    final private String[] ranking1Percentages;
    final private NormalizationStrategy normStrategy;
    final private String rankingIdentifier1;
    final private String rankingIdentifier2;
    private String suffix;
    private final Path outputDir;

    /**
     * Initializes a {@link CombineAndStoreRankingsProcessor} object with the given
     * parameters.
     *
     * @param suffix              a suffix to append to the ranking directory (may be null)
     * @param rankingIdentifier1  a fault localizer identifier or an lm ranking file name
     * @param rankingIdentifier2  a fault localizer identifier or an lm ranking file name
     * @param ranking1Percentages an array of percentage values that determine the weighting of the first
     *                            ranking to the second ranking
     * @param normStrategy        whether the rankings should be normalized before combining
     * @param outputDir           the output directory
     */
    public CombineAndStoreRankingsProcessor(String suffix, String rankingIdentifier1, String rankingIdentifier2,
                                            String[] ranking1Percentages, NormalizationStrategy normStrategy, Path outputDir) {
        super();
        this.ranking1Percentages = ranking1Percentages;
        this.normStrategy = normStrategy;
        this.rankingIdentifier1 = rankingIdentifier1;
        this.rankingIdentifier2 = rankingIdentifier2;
        this.outputDir = outputDir;
    }

    @Override
    public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> entity,
                                           ProcessorSocket<BuggyFixedEntity<?>, BuggyFixedEntity<?>> socket) {
        Entity bug = entity.getBuggyVersion();

        double[] ranking1percentages = {50.0};
        if (ranking1Percentages != null) {
            ranking1percentages = new double[ranking1Percentages.length];
            for (int i = 0; i < ranking1Percentages.length; ++i) {
                ranking1percentages[i] = Double.parseDouble(ranking1Percentages[i]);
            }
        }

        Ranking<SourceCodeBlock> ranking1 = RankingUtils.getRanking(SourceCodeBlock.DUMMY, bug, suffix, rankingIdentifier1);
        if (ranking1 == null) {
            Log.abort(this, "Found no ranking with identifier '%s'.", rankingIdentifier1);
        }

        Ranking<SourceCodeBlock> ranking2 = RankingUtils.getRanking(SourceCodeBlock.DUMMY, bug, suffix, rankingIdentifier2);
        if (ranking2 == null) {
            Log.abort(this, "Found no ranking with identifier '%s'.", rankingIdentifier2);
        }

        if (normStrategy != null) {
            ranking1 = Ranking.normalize(ranking1, normStrategy);
            ranking2 = Ranking.normalize(ranking2, normStrategy);
        }

        for (double percentage : ranking1percentages) {

            Ranking<SourceCodeBlock> combinedRanking = RankingUtils.getCombinedRanking(ranking1, ranking2, percentage);

            String output = outputDir.resolve(rankingIdentifier1 + "_" + rankingIdentifier2 + "_" + (int) percentage + ".rnk").toString();
            try {
                combinedRanking.save(output);
            } catch (IOException e) {
                Log.err(this, "Could not save combined ranking to '%s'.", output);
            }
        }

        return null;
    }

}
