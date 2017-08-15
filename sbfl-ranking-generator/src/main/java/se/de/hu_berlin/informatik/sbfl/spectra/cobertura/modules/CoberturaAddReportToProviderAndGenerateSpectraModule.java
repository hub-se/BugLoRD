/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.cobertura.modules;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.CoberturaReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.CoberturaReportWrapper;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class CoberturaAddReportToProviderAndGenerateSpectraModule extends AbstractProcessor<CoberturaReportWrapper, ISpectra<SourceCodeBlock>> {

	final private CoberturaReportProvider provider;
	private boolean saveFailedTraces = false;
	private CoberturaHitTraceModule hitTraceModule = null;
	
	public CoberturaAddReportToProviderAndGenerateSpectraModule(final boolean aggregateSpectra, 
			final String failedTracesOutputDir) {
		super();
		this.provider = new CoberturaReportProvider(aggregateSpectra, false);
		if (failedTracesOutputDir != null) {
			this.saveFailedTraces = true;
			hitTraceModule = new CoberturaHitTraceModule(failedTracesOutputDir);
		}
	}
	
	public CoberturaAddReportToProviderAndGenerateSpectraModule(final boolean aggregateSpectra) {
		this(aggregateSpectra, null);
	}
	
	public CoberturaAddReportToProviderAndGenerateSpectraModule() {
		this(false);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<SourceCodeBlock> processItem(final CoberturaReportWrapper reportWrapper) {

		if (saveFailedTraces && !reportWrapper.isSuccessful()) {
			hitTraceModule.submit(reportWrapper);
		}
		
		if (!provider.addData(reportWrapper)) {
			Log.err(this, "Could not add report '%s'.", reportWrapper.getIdentifier());
			throw new IllegalStateException("Adding a report failed. Can not provide correct spectra.");
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
