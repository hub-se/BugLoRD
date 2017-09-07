/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.ranking.modules;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.CoberturaSpectraProviderFactory;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.xml.CoberturaCoverageWrapper;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.xml.CoberturaXMLProvider;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class AddXMLCoverageToProviderAndGenerateSpectraModule extends AbstractProcessor<CoberturaCoverageWrapper, ISpectra<SourceCodeBlock, ?>> {

	final private CoberturaXMLProvider<?> provider;
	private boolean saveFailedTraces = false;
	private XMLCoverageToHitTraceModule hitTraceModule = null;
	
	public AddXMLCoverageToProviderAndGenerateSpectraModule( 
			final String failedTracesOutputDir, boolean fullSpectra) {
		super();
		this.provider = CoberturaSpectraProviderFactory.getHitSpectraFromXMLProvider(fullSpectra);
		if (failedTracesOutputDir != null) {
			this.saveFailedTraces = true;
			hitTraceModule = new XMLCoverageToHitTraceModule(failedTracesOutputDir);
		}
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<SourceCodeBlock, ?> processItem(final CoberturaCoverageWrapper coverage) {

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
	public ISpectra<SourceCodeBlock, ?> getResultFromCollectedItems() {
		try {
			return provider.loadSpectra();
		} catch (IllegalStateException e) {
			Log.err(this, e, "Providing the spectra failed.");
		}
		return null;
	}

}
