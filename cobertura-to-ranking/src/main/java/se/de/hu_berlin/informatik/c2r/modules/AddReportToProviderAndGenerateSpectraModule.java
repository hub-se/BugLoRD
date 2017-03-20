/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.CoberturaReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.ReportWrapper;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class AddReportToProviderAndGenerateSpectraModule extends AbstractProcessor<ReportWrapper, ISpectra<SourceCodeBlock>> {

	final private CoberturaReportProvider provider;
	private boolean saveFailedTraces = false;
	private HitTraceModule hitTraceModule = null;
	
	public AddReportToProviderAndGenerateSpectraModule(final boolean aggregateSpectra, 
			final String failedTracesOutputDir) {
		super();
		this.provider = new CoberturaReportProvider(aggregateSpectra);
		if (failedTracesOutputDir != null) {
			this.saveFailedTraces = true;
			hitTraceModule = new HitTraceModule(failedTracesOutputDir);
		}
	}
	
	public AddReportToProviderAndGenerateSpectraModule(final boolean aggregateSpectra) {
		this(aggregateSpectra, null);
	}
	
	public AddReportToProviderAndGenerateSpectraModule() {
		this(false);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<SourceCodeBlock> processItem(final ReportWrapper reportWrapper) {

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
