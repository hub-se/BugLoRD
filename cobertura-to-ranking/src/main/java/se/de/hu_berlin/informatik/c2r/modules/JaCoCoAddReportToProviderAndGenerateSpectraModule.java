/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.jacoco.JaCoCoReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.jacoco.JaCoCoReportWrapper;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class JaCoCoAddReportToProviderAndGenerateSpectraModule extends AbstractProcessor<JaCoCoReportWrapper, ISpectra<SourceCodeBlock>> {

	final private JaCoCoReportProvider provider;
	private boolean saveFailedTraces = false;
	private JaCoCoHitTraceModule hitTraceModule = null;
	
	public JaCoCoAddReportToProviderAndGenerateSpectraModule(final boolean aggregateSpectra, 
			final String failedTracesOutputDir, boolean fullSpectra) {
		super();
		this.provider = new JaCoCoReportProvider(aggregateSpectra, false, fullSpectra);
		if (failedTracesOutputDir != null) {
			this.saveFailedTraces = true;
			hitTraceModule = new JaCoCoHitTraceModule(failedTracesOutputDir);
		}
	}
	
	public JaCoCoAddReportToProviderAndGenerateSpectraModule(final boolean aggregateSpectra) {
		this(aggregateSpectra, null, false);
	}
	
	public JaCoCoAddReportToProviderAndGenerateSpectraModule() {
		this(false);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<SourceCodeBlock> processItem(final JaCoCoReportWrapper reportWrapper) {

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
