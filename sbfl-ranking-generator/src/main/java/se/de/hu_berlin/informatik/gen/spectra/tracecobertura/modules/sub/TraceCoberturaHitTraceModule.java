/**
 * 
 */
package se.de.hu_berlin.informatik.gen.spectra.tracecobertura.modules.sub;

import java.nio.file.Paths;

import se.de.hu_berlin.informatik.gen.spectra.modules.TraceFileModule;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.CoberturaSpectraProviderFactory;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.report.CoberturaReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.report.CoberturaReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Computes the hit trace for the wrapped Cobertura report and saves the
 * trace file to the hard drive.
 * 
 * @author Simon Heiden
 */
public class TraceCoberturaHitTraceModule extends AbstractProcessor<CoberturaReportWrapper, Object> {

	final private String outputdir;
	
	/**
	 * Creates a new {@link TraceCoberturaHitTraceModule} object with the given parameters.
	 * @param outputdir
	 * path to output directory
	 */
	public TraceCoberturaHitTraceModule(final String outputdir) {
		super();
		this.outputdir = outputdir;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public Object processItem(final CoberturaReportWrapper report) {
		computeHitTrace(report);
		return null;
	}
	
	/**
	 * Calculates a single hit trace from the given report to output/trace_inputfilename.trc.
	 * @param report
	 * a Cobertura report wrapper
	 */
	private void computeHitTrace(final CoberturaReportWrapper report) {
		final CoberturaReportProvider<?> provider = CoberturaSpectraProviderFactory.getHitSpectraFromReportProvider(false);
		if (!provider.addData(report)) {
			Log.err(this, "Could not add report '%s'.", report.getIdentifier());
			return;
		}

		try {
			new TraceFileModule<>(Paths.get(outputdir), report.getIdentifier().replace(':','_'))
			.submit(provider.loadSpectra());
		} catch (IllegalStateException e) {
			Log.err(this, e, "Providing the spectra failed.");
		}
	}

}
