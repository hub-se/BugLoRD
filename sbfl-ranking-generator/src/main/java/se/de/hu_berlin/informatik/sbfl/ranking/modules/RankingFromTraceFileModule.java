/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.ranking.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.stardust.localizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer.ComputationStrategies;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.ILocalizer;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.LocalizerFromFile;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.tracking.ProgressBarTracker;

/**
 * Computes rankings for all coverage data stored in the 
 * input spectra and saves multiple ranking files for
 * various SBFL formulae to the hard drive.
 * 
 * @author Simon Heiden
 */
public class RankingFromTraceFileModule extends AbstractProcessor<List<IFaultLocalizer<SourceCodeBlock>>, List<IFaultLocalizer<SourceCodeBlock>>> {

	final private String outputdir;
	final private ComputationStrategies strategy;

	private Path traceFilePath;
	private Path metricsFilePath;
	
	/**
	 * @param traceFilePath
	 * a trace file
	 * @param metricsFilePath
	 * a metrics file
	 * @param strategy
	 * the strategy to use for computation of the rankings
	 * @param outputdir
	 * path to the output directory
	 */
	public RankingFromTraceFileModule(final Path traceFilePath, final Path metricsFilePath,
			final ComputationStrategies strategy, final String outputdir) {
		super();
		this.traceFilePath = traceFilePath;
		this.metricsFilePath = metricsFilePath;
		this.strategy = strategy;
		this.outputdir = outputdir;
		
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public List<IFaultLocalizer<SourceCodeBlock>> processItem(final List<IFaultLocalizer<SourceCodeBlock>> localizers) {
		final ProgressBarTracker tracker = new ProgressBarTracker(1, localizers.size());
		
		ILocalizer<SourceCodeBlock> localizer = new LocalizerFromFile(
				traceFilePath.toString(), metricsFilePath.toString());
		
		//calculate the SBFL rankings, if any localizers are given
		for (final IFaultLocalizer<SourceCodeBlock> localizer2 : localizers) {
			final String className = localizer2.getClass().getSimpleName();
			tracker.track("...calculating " + className + " ranking.");
			// Log.out(this, "...calculating " + className + " ranking.");
			
			generateRanking(localizer, localizer2, className.toLowerCase(Locale.getDefault()));
		}

		return localizers;
	}

	/**
	 * Generates and saves a specific SBFL ranking. 
	 * @param localizer
	 * Cobertura line spectra
	 * @param localizer2
	 * provides specific SBFL formulae
	 * @param subfolder
	 * name of a subfolder to be used
	 */
	private void generateRanking(final ILocalizer<SourceCodeBlock> localizer, 
			final IFaultLocalizer<SourceCodeBlock> localizer2, final String subfolder) {
		try {
			final Ranking<INode<SourceCodeBlock>> ranking = localizer.localize(localizer2, strategy);
			Paths.get(outputdir + File.separator + subfolder).toFile().mkdirs();
			ranking.saveOnlyScores(new Comparator<INode<SourceCodeBlock>>() {
				@Override
				public int compare(INode<SourceCodeBlock> o1, INode<SourceCodeBlock> o2) {
					return o1.getIdentifier().compareTo(o2.getIdentifier());
				}
			}, outputdir + File.separator + subfolder + File.separator + BugLoRDConstants.FILENAME_TRACE_RANKING_FILE);
		} catch (IOException e) {
			Log.err(this, e, "Could not save ranking in '%s'.", 
					outputdir + File.separator + subfolder + File.separator + BugLoRDConstants.FILENAME_TRACE_RANKING_FILE);
		}
	}

}
