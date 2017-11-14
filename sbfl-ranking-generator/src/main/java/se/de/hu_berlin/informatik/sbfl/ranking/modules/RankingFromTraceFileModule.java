/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.ranking.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.stardust.localizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer.ComputationStrategies;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.FaultLocalizerFactory;
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
public class RankingFromTraceFileModule<T> extends AbstractProcessor<String, String> {

	final private String outputdir;
	final private List<IFaultLocalizer<String>> localizers;
	final private ComputationStrategies strategy;
	
	/**
	 * @param strategy
	 * the strategy to use for computation of the rankings
	 * @param outputdir
	 * path to the output directory
	 * @param localizers
	 * a list of Cobertura localizer identifiers
	 */
	public RankingFromTraceFileModule(final ComputationStrategies strategy, 
			final String outputdir, final String... localizers) {
		super();
		this.strategy = strategy;
		this.outputdir = outputdir;
		if (localizers == null) {
			this.localizers = new ArrayList<>(0);
		} else {
			this.localizers = new ArrayList<>(localizers.length);

			//check if the given localizers can be found and abort in the negative case
			for (int i = 0; i < localizers.length; ++i) {
				try {
					this.localizers.add(FaultLocalizerFactory.newInstance(localizers[i]));
				} catch (IllegalArgumentException e) {
					Log.abort(this, e, "Could not find localizer '%s'.", localizers[i]);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public String processItem(final String traceFile) {
		final ProgressBarTracker tracker = new ProgressBarTracker(1, localizers.size());
		
		String metricsFile = outputdir + File.separator + BugLoRDConstants.FILENAME_METRICS_FILE;

		if (!new File(metricsFile).exists()) {
			Log.abort(this, "Could not find metrics file '%s'.", metricsFile);
		}
		
		ILocalizer<String> localizer = new LocalizerFromFile(traceFile, metricsFile);
		
		//calculate the SBFL rankings, if any localizers are given
		for (final IFaultLocalizer<String> localizer2 : localizers) {
			final String className = localizer.getClass().getSimpleName();
			tracker.track("...calculating " + className + " ranking.");
			// Log.out(this, "...calculating " + className + " ranking.");
			
			generateRanking(localizer, localizer2, className.toLowerCase(Locale.getDefault()));
		}

		return traceFile;
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
	private void generateRanking(final ILocalizer<String> localizer, 
			final IFaultLocalizer<String> localizer2, final String subfolder) {
		try {
			final Ranking<INode<String>> ranking = localizer.localize(localizer2, strategy);
			Paths.get(outputdir + File.separator + subfolder).toFile().mkdirs();
			ranking.save(outputdir + File.separator + subfolder + File.separator + BugLoRDConstants.FILENAME_RANKING_FILE);
		} catch (IOException e) {
			Log.err(this, e, "Could not save ranking in '%s'.", 
					outputdir + File.separator + subfolder + File.separator + BugLoRDConstants.FILENAME_RANKING_FILE);
		}
	}

}
