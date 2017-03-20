/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.CoberturaXMLProvider;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.CoverageWrapper;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class AddXMLCoverageToProviderAndGenerateSpectraModule extends AbstractProcessor<CoverageWrapper, ISpectra<SourceCodeBlock>> {

	final private CoberturaXMLProvider provider;
	private boolean saveFailedTraces = false;
	private XMLCoverageToHitTraceModule hitTraceModule = null;
	
	public AddXMLCoverageToProviderAndGenerateSpectraModule( 
			final String failedTracesOutputDir) {
		super();
		this.provider = new CoberturaXMLProvider();
		if (failedTracesOutputDir != null) {
			this.saveFailedTraces = true;
			hitTraceModule = new XMLCoverageToHitTraceModule(failedTracesOutputDir);
		}
	}
	
	public AddXMLCoverageToProviderAndGenerateSpectraModule() {
		this(null);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<SourceCodeBlock> processItem(final CoverageWrapper coverage) {

		if (saveFailedTraces && !coverage.isSuccessful()) {
			hitTraceModule.submit(coverage);
		}

		if (!provider.addData(coverage.getXmlCoverageFile().toString(), 
				coverage.getIdentifier(), coverage.isSuccessful())) {
			Log.err(this, "Could not add XML coverage file '%s'.", coverage.getXmlCoverageFile().toString());
			throw new IllegalStateException("Adding an XML coverage file failed. Can not provide correct spectra.");
		}

		return null;
	}

	@Override
	public ISpectra<SourceCodeBlock> getResultFromCollectedItems() {
		try {
			return provider.loadSpectra();
		} catch (IllegalStateException e) {
			Log.err(this, e, "Providing the spectra failed.");
		}
		return null;
	}

}
