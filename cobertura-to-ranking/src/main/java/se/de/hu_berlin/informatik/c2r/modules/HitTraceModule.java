/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.File;
import java.nio.file.Paths;

import se.de.hu_berlin.informatik.stardust.localizer.HitRanking;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.NoRanking;
import se.de.hu_berlin.informatik.stardust.provider.CoberturaProvider;
import se.de.hu_berlin.informatik.stardust.provider.ReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * Computes the hit trace for the wrapped Cobertura report and saves the
 * trace file to the hard drive.
 * 
 * @author Simon Heiden
 */
public class HitTraceModule extends AbstractModule<ReportWrapper, Object> {

	final private String outputdir;
	
	/**
	 * Creates a new {@link HitTraceModule} object with the given parameters.
	 * @param outputdir
	 * path to output directory
	 */
	public HitTraceModule(final String outputdir) {
		super(true);
		this.outputdir = outputdir;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public Object processItem(final ReportWrapper report) {
		computeHitTrace(report);
		return null;
	}
	
	/**
	 * Calculates a single hit trace from the given report to output/inputfilename.trc.
	 * @param report
	 * a Cobertura report wrapper
	 */
	private void computeHitTrace(final ReportWrapper report) {
		final CoberturaProvider provider = new CoberturaProvider();
		provider.addReport(report);

		try {
			final HitRanking<SourceCodeBlock> ranking = new NoRanking<SourceCodeBlock>(true).localizeHit(provider.loadSpectra());
			Paths.get(outputdir).toFile().mkdirs();
			ranking.save(outputdir + File.separator + report.getIdentifier().replace(':','_') + ".trc");
		} catch (Exception e1) {
			Log.err(this, e1, "Could not save ranking for report in '%s'. (hit trace)%n", 
					outputdir + File.separator + report.getIdentifier().replace(':','_') + ".trc");
		}
	}

}
