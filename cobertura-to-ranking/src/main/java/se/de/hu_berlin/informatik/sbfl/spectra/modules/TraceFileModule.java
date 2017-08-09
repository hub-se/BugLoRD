/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.stardust.localizer.HitRanking;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.NoRanking;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Computes a trace file for all coverage data stored in the 
 * input spectra.
 * 
 * @author Simon Heiden
 */
public class TraceFileModule<T> extends AbstractProcessor<ISpectra<T>, Object> {

	final private String outputdir;

	/**
	 * @param outputdir
	 * path to the output directory
	 */
	public TraceFileModule(final String outputdir) {
		super();
		this.outputdir = outputdir;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<T> processItem(final ISpectra<T> spectra) {
		//save a trace file that contains all executed lines
		try {
			final HitRanking<T> ranking = new NoRanking<T>(false).localizeHit(spectra);
			Path traceFileOutput = Paths.get(outputdir, BugLoRDConstants.FILENAME_TRACE_FILE);
			FileUtils.ensureParentDir(traceFileOutput.toFile());
			ranking.save(traceFileOutput.toString());
		} catch (IOException e1) {
			Log.err(this, e1, "Could not save hit trace for spectra in '%s'.", 
					outputdir + File.separator + BugLoRDConstants.FILENAME_TRACE_FILE);
		}
		
		return spectra;
	}

}
