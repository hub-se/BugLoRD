package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;

import java.io.File;
import java.nio.file.Paths;

/**
 * Runs a single experiment.
 *
 * @author Simon Heiden
 */
public class GenCombinedRankingsEH extends AbstractConsumingProcessor<BuggyFixedEntity<?>> {

    private final String lmRankingIdentifier;
    private final String suffix;

    private final String[] percentages;
    private final String[] localizers;
    private final NormalizationStrategy strategy;

    /**
     * Initializes a {@link GenCombinedRankingsEH} object with the given parameters.
     *
     * @param suffix              a suffix to append to the ranking directory (may be null)
     * @param lmRankingIdentifier an LM ranking identifier
     * @param localizers          the localizers to consider
     * @param percentages         the percentages to compute combined rankings for
     * @param strategy            the normalization strategy to be used
     */
    public GenCombinedRankingsEH(String suffix, String lmRankingIdentifier, String[] localizers,
                                 String[] percentages, NormalizationStrategy strategy) {
        super();
        this.suffix = suffix;
        this.lmRankingIdentifier = lmRankingIdentifier;
        this.localizers = localizers;
        this.percentages = percentages;
        this.strategy = strategy;
    }

    @Override
    public void consumeItem(BuggyFixedEntity<?> buggyEntity) {
        Log.out(this, "Processing %s.", buggyEntity);

        Entity bug = buggyEntity.getBuggyVersion();

        /* #====================================================================================
         * # query sentences to the LM via kenLM,
         * # combine the generated rankings
         * #==================================================================================== */

        File buggyVersionDir = bug.getWorkDir(true).toFile();

        if (!buggyVersionDir.exists()) {
            Log.err(this, "Work directory doesn't exist: '" + buggyVersionDir + "'.");
            Log.err(this, "Error while querying sentences and/or combining rankings. Skipping '"
                    + buggyEntity + "'.");
            return;
        }

        /* #====================================================================================
         * # generate the combined rankings for each given localizer and the given LM
         * #==================================================================================== */

        String combinedRankingDir = bug.getWorkDataDir()
                .resolve(suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
                .resolve(BugLoRDConstants.DIR_NAME_COMBINED_RANKING)
                .toString();
        new File(combinedRankingDir).mkdirs();


        for (String localizer : localizers) {
//		Log.out(this, "Processing: " + localizer);

            new CombineAndStoreRankingsProcessor(suffix, localizer, lmRankingIdentifier, percentages,
                    strategy, Paths.get(combinedRankingDir))
                    .submit(buggyEntity);

        }
    }

}

