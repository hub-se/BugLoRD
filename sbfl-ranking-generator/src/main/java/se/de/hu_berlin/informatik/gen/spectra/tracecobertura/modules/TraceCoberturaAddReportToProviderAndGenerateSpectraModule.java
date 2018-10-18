/**
 * 
 */
package se.de.hu_berlin.informatik.gen.spectra.tracecobertura.modules;

import se.de.hu_berlin.informatik.gen.spectra.cobertura.modules.sub.CoberturaHitTraceModule;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.count.CountTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.TraceCoberturaSpectraProviderFactory;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.report.TraceCoberturaCountReportProvider;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.report.TraceCoberturaReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class TraceCoberturaAddReportToProviderAndGenerateSpectraModule extends AbstractProcessor<TraceCoberturaReportWrapper, ISpectra<SourceCodeBlock, ?>> {

	final private TraceCoberturaCountReportProvider<CountTrace<SourceCodeBlock>> provider;
	private boolean saveFailedTraces = false;
	private CoberturaHitTraceModule hitTraceModule = null;
	StatisticsCollector<StatisticsData> statisticsContainer;
	private boolean errorState = false;
	
	public TraceCoberturaAddReportToProviderAndGenerateSpectraModule(
			final String failedTracesOutputDir, boolean fullSpectra, StatisticsCollector<StatisticsData> statisticsContainer) {
		super();
		this.provider = TraceCoberturaSpectraProviderFactory.getCountSpectraFromReportProvider(fullSpectra);
		this.statisticsContainer = statisticsContainer;
		if (failedTracesOutputDir != null) {
			this.saveFailedTraces = true;
			hitTraceModule = new CoberturaHitTraceModule(failedTracesOutputDir);
		}
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public ISpectra<SourceCodeBlock, ?> processItem(final TraceCoberturaReportWrapper reportWrapper) {
		
		if (reportWrapper == TraceCoberturaRunSingleTestAndReportModule.ERROR_WRAPPER) {
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
	public ISpectra<SourceCodeBlock, ?> getResultFromCollectedItems() {
		if (errorState) {
			Log.err(this, "Providing the spectra failed.");
			return null;
		}
		
		try {
			ISpectra<SourceCodeBlock, ?> spectra = provider.loadSpectra();
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
