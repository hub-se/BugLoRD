/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.cobertura.modules;

import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.CoberturaReportProvider;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.CoberturaReportWrapper;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class CoberturaAddReportToProviderAndGenerateSpectraModule extends AbstractProcessor<CoberturaReportWrapper, ISpectra<SourceCodeBlock>> {

	final private CoberturaReportProvider provider;
	private boolean saveFailedTraces = false;
	private CoberturaHitTraceModule hitTraceModule = null;
	StatisticsCollector<StatisticsData> statisticsContainer;
	private boolean errorState = false;
	
	public CoberturaAddReportToProviderAndGenerateSpectraModule(final boolean aggregateSpectra, 
			final String failedTracesOutputDir, StatisticsCollector<StatisticsData> statisticsContainer) {
		super();
		this.provider = new CoberturaReportProvider(aggregateSpectra, false);
		this.statisticsContainer = statisticsContainer;
		if (failedTracesOutputDir != null) {
			this.saveFailedTraces = true;
			hitTraceModule = new CoberturaHitTraceModule(failedTracesOutputDir);
		}
	}
	
	public CoberturaAddReportToProviderAndGenerateSpectraModule(final boolean aggregateSpectra, 
			StatisticsCollector<StatisticsData> statisticsContainer) {
		this(aggregateSpectra, null, statisticsContainer);
	}
	
	public CoberturaAddReportToProviderAndGenerateSpectraModule() {
		this(false, null);
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<SourceCodeBlock> processItem(final CoberturaReportWrapper reportWrapper) {
		
		if (reportWrapper == CoberturaTestRunAndReportModule.ERROR_WRAPPER) {
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
