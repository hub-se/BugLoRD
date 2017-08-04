/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import se.de.hu_berlin.informatik.stardust.localizer.HitRanking;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.NoRanking;
import se.de.hu_berlin.informatik.stardust.provider.jacoco.JaCoCoReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.jacoco.JaCoCoReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Computes the hit trace for the wrapped JaCoCo report and saves the
 * trace file to the hard drive.
 * 
 * @author Simon Heiden
 */
public class JaCoCoHitTraceModule extends AbstractProcessor<JaCoCoReportWrapper, Object> {

	final private String outputdir;
	
	/**
	 * Creates a new {@link JaCoCoHitTraceModule} object with the given parameters.
	 * @param outputdir
	 * path to output directory
	 */
	public JaCoCoHitTraceModule(final String outputdir) {
		super();
		this.outputdir = outputdir;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public Object processItem(final JaCoCoReportWrapper report) {
		computeHitTrace(report);
		return null;
	}
	
	/**
	 * Calculates a single hit trace from the given report to output/inputfilename.trc.
	 * @param report
	 * a Cobertura report wrapper
	 */
	private void computeHitTrace(final JaCoCoReportWrapper report) {
		final JaCoCoReportProvider provider = new JaCoCoReportProvider();
		if (!provider.addData(report)) {
			Log.err(this, "Could not add report '%s'.", report.getIdentifier());
			return;
		}

		try {
			final HitRanking<SourceCodeBlock> ranking = new NoRanking<SourceCodeBlock>(true).localizeHit(provider.loadSpectra());
			Paths.get(outputdir).toFile().mkdirs();
			ranking.save(outputdir + File.separator + report.getIdentifier().replace(':','_') + ".trc");
		} catch (IllegalStateException e) {
			Log.err(this, e, "Providing the spectra failed.");
		} catch (IOException e) {
			Log.err(this, e, "Could not save ranking for report in '%s'. (hit trace)%n", 
					outputdir + File.separator + report.getIdentifier().replace(':','_') + ".trc");
		}
	}

}
