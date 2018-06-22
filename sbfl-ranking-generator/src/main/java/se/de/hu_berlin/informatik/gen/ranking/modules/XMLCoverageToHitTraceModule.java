/**
 * 
 */
package se.de.hu_berlin.informatik.gen.ranking.modules;

import java.nio.file.Paths;

import se.de.hu_berlin.informatik.gen.spectra.modules.TraceFileModule;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.CoberturaSpectraProviderFactory;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.xml.CoberturaCoverageWrapper;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.xml.CoberturaXMLProvider;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Computes the hit trace for the wrapped Cobertura report and saves the
 * trace file to the hard drive.
 * 
 * @author Simon Heiden
 */
public class XMLCoverageToHitTraceModule extends AbstractProcessor<CoberturaCoverageWrapper, Object> {

	final private String outputdir;
	
	/**
	 * Creates a new {@link XMLCoverageToHitTraceModule} object with the given parameters.
	 * @param outputdir
	 * path to output directory
	 */
	public XMLCoverageToHitTraceModule(final String outputdir) {
		super();
		this.outputdir = outputdir;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public Object processItem(final CoberturaCoverageWrapper coverage) {
		computeHitTrace(coverage);
		return null;
	}
	
	/**
	 * Calculates a single hit trace from the given input xml file to output/trace_inputfilename.trc.
	 * @param input
	 * path to Cobertura trace file in xml format
	 */
	private void computeHitTrace(final CoberturaCoverageWrapper coverage) {
		final CoberturaXMLProvider<?> provider = CoberturaSpectraProviderFactory.getHitSpectraFromXMLProvider(false);
		if (!provider.addData(coverage.getXmlCoverageFile().toString(), coverage.getIdentifier(), true)) {
			Log.err(this, "Could not add XML coverage file '%s'.", coverage.getXmlCoverageFile().toString());
			return;
		}

		try {
			new TraceFileModule<>(Paths.get(outputdir), coverage.getXmlCoverageFile().getName().replace(':','_'))
			.submit(provider.loadSpectra());
		} catch (IllegalStateException e) {
			Log.err(this, e, "Providing the spectra failed.");
		}
	}

}
