/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.CoberturaProvider;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.ReportWrapper;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class AddReportToProviderAndGenerateSpectraModule extends AbstractModule<ReportWrapper, ISpectra<SourceCodeBlock>> {

	final private CoberturaProvider provider;
	private boolean saveFailedTraces = false;
	private HitTraceModule hitTraceModule = null;
	
	public AddReportToProviderAndGenerateSpectraModule(final boolean aggregateSpectra, 
			final String failedTracesOutputDir) {
		super(true);
		this.provider = new CoberturaProvider(aggregateSpectra);
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
		
		provider.addReport(reportWrapper);

		return null;
	}

	@Override
	public ISpectra<SourceCodeBlock> getResultFromCollectedItems() {
		try {
			return provider.loadSpectra();
		} catch (Exception e) {
			Log.err(this, e, "Providing the spectra failed.");
		}
		return null;
	}

}
