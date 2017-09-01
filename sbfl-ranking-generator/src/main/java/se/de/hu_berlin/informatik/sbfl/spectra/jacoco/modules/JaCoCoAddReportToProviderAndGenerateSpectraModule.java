/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.jacoco.modules;

import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.jacoco.JaCoCoReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.jacoco.JaCoCoReportWrapper;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class JaCoCoAddReportToProviderAndGenerateSpectraModule extends AbstractProcessor<JaCoCoReportWrapper, ISpectra<SourceCodeBlock>> {

	final private JaCoCoReportProvider provider;
	private boolean saveFailedTraces = false;
	private JaCoCoHitTraceModule hitTraceModule = null;
	StatisticsCollector<StatisticsData> statisticsContainer;
	private boolean errorState = false;
	
	public JaCoCoAddReportToProviderAndGenerateSpectraModule(final boolean aggregateSpectra, 
			final String failedTracesOutputDir, boolean fullSpectra, 
			StatisticsCollector<StatisticsData> statisticsContainer) {
		super();
		this.provider = new JaCoCoReportProvider(aggregateSpectra, false, fullSpectra);
		this.statisticsContainer = statisticsContainer;
		if (failedTracesOutputDir != null) {
			this.saveFailedTraces = true;
			hitTraceModule = new JaCoCoHitTraceModule(failedTracesOutputDir);
		}
	}
	
	public JaCoCoAddReportToProviderAndGenerateSpectraModule(final boolean aggregateSpectra,
			StatisticsCollector<StatisticsData> statisticsContainer) {
		this(aggregateSpectra, null, false, statisticsContainer);
	}
	
	public JaCoCoAddReportToProviderAndGenerateSpectraModule() {
		this(false, null);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<SourceCodeBlock> processItem(final JaCoCoReportWrapper reportWrapper) {

		if (reportWrapper == JaCoCoTestRunAndReportModule.ERROR_WRAPPER) {
			errorState  = true;
			return null;
		}
		
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
		if (errorState) {
			Log.err(this, "Providing the spectra failed.");
			return null;
		}
		
		try {
			ISpectra<SourceCodeBlock> spectra = provider.loadSpectra();
			if (statisticsContainer != null && spectra != null) {
				statisticsContainer.addStatisticsElement(StatisticsData.NODES, spectra.getNodes().size());
//				for (INode<SourceCodeBlock> node : spectra.getNodes()) {
//					Log.out(this, "%s", node.getIdentifier());
//				}
			}
			return spectra;
		} catch (IllegalStateException e) {
			Log.err(this, e, "Providing the spectra failed.");
		}
		return null;
	}

}
