package se.de.hu_berlin.informatik.gen.ranking.modules;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.faultlocalizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.ILocalizerCache;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.LocalizerCacheFromFile;
import se.de.hu_berlin.informatik.spectra.util.Indexable;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.tracking.ProgressBarTracker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Computes rankings for all coverage data stored in the
 * input spectra and saves multiple ranking files for
 * various SBFL formulae to the hard drive.
 *
 * @param <T> type of node identifiers
 * @author Simon Heiden
 */
public class RankingFromTraceFileModule<T extends Indexable<T> & Comparable<T>> extends AbstractProcessor<List<IFaultLocalizer<T>>, List<IFaultLocalizer<T>>> {

    final private String outputdir;
    final private ComputationStrategies strategy;

    private final Path traceFilePath;
    private final Path metricsFilePath;
    private final T dummy;

    /**
     * @param traceFilePath   a trace file
     * @param metricsFilePath a metrics file
     * @param strategy        the strategy to use for computation of the rankings
     * @param outputdir       path to the output directory
     */
    public RankingFromTraceFileModule(T dummy, final Path traceFilePath, final Path metricsFilePath,
                                      final ComputationStrategies strategy, final String outputdir) {
        super();
        this.traceFilePath = traceFilePath;
        this.metricsFilePath = metricsFilePath;
        this.strategy = strategy;
        this.outputdir = outputdir;
        this.dummy = dummy;

    }

    /* (non-Javadoc)
     * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
     */
    @Override
    public List<IFaultLocalizer<T>> processItem(final List<IFaultLocalizer<T>> localizers) {
        final ProgressBarTracker tracker = new ProgressBarTracker(1, localizers.size());

        ILocalizerCache<T> localizer = new LocalizerCacheFromFile<>(dummy,
                traceFilePath.toString(), metricsFilePath.toString());

        //calculate the SBFL rankings, if any localizers are given
        for (final IFaultLocalizer<T> localizer2 : localizers) {
            final String className = localizer2.getName();
            tracker.track("...calculating " + className + " ranking.");
            // Log.out(this, "...calculating " + className + " ranking.");

            generateRanking(localizer, localizer2, className.toLowerCase(Locale.getDefault()));
        }

        return localizers;
    }

    /**
     * Generates and saves a specific SBFL ranking.
     *
     * @param localizer  Cobertura line spectra
     * @param localizer2 provides specific SBFL formulae
     * @param subfolder  name of a subfolder to be used
     */
    private void generateRanking(final ILocalizerCache<T> localizer,
                                 final IFaultLocalizer<T> localizer2, final String subfolder) {
        try {
            final Ranking<INode<T>> ranking = localizer2.localize(localizer, strategy);
            Paths.get(outputdir + File.separator + subfolder).toFile().mkdirs();
            ranking.saveOnlyScores(Comparator.comparing(INode::getIdentifier), outputdir + File.separator + subfolder + File.separator + BugLoRDConstants.FILENAME_TRACE_RANKING_FILE);
        } catch (IOException e) {
            Log.err(this, e, "Could not save ranking in '%s'.",
                    outputdir + File.separator + subfolder + File.separator + BugLoRDConstants.FILENAME_TRACE_RANKING_FILE);
        }
    }

}
