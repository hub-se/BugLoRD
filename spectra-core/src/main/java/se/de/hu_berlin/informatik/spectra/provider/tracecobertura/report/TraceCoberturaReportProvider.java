/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.report;

import java.util.List;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.core.traces.RawTraceCollector;
import se.de.hu_berlin.informatik.spectra.provider.AbstractSpectraProvider;
import se.de.hu_berlin.informatik.spectra.provider.loader.ICoverageDataLoader;
import se.de.hu_berlin.informatik.spectra.provider.loader.tracecobertura.report.TraceCoberturaReportLoader;

/**
 * Loads Cobertura reports to {@link HitSpectra} objects where each covered line
 * is represented by one node and each file represents one trace in the
 * resulting spectra.
 */
public class TraceCoberturaReportProvider<K extends ITrace<SourceCodeBlock>>
		extends AbstractSpectraProvider<SourceCodeBlock, K, TraceCoberturaReportWrapper> {

	private TraceCoberturaReportLoader<SourceCodeBlock, K> loader;

	public TraceCoberturaReportProvider(ISpectra<SourceCodeBlock, K> lineSpectra, boolean fullSpectra) {
		super(lineSpectra, fullSpectra);

		loader = new TraceCoberturaReportLoader<SourceCodeBlock, K>() {

			@Override
			public SourceCodeBlock getIdentifier(String packageName, String sourceFilePath, String methodNameAndSig,
					int lineNumber) {
				return new SourceCodeBlock(packageName, sourceFilePath, methodNameAndSig, lineNumber);
			}

			@Override
			public int getNodeIndex(String sourceFilePath, int lineNumber) {
				SourceCodeBlock identifier = new SourceCodeBlock(null, sourceFilePath, null, lineNumber);
				INode<SourceCodeBlock> node = lineSpectra.getNode(identifier);
				if (node == null) {
					return -1;
				} else {
					return node.getIndex();
				}
			}

		};
	}

	@Override
	protected ICoverageDataLoader<SourceCodeBlock, K, TraceCoberturaReportWrapper> getLoader() {
		return loader;
	}
	
	@Override
	public ISpectra<SourceCodeBlock, ? super K> loadSpectra() throws IllegalStateException {
		ISpectra<SourceCodeBlock, ? super K> spectra = super.loadSpectra();
		
		// generate execution traces from raw traces
		RawTraceCollector traceCollector = loader.getTraceCollector();
		// store the indexer with the spectra
		spectra.setIndexer(loader.getTraceCollector().getIndexer());
		
		// generate the execution traces for each test case and add them to the spectra;
		// this needs to be done AFTER all tests have been executed
		for (ITrace<?> trace : spectra.getTraces()) {
			// generate execution traces from collected raw traces
			List<ExecutionTrace> executionTraces = traceCollector.getExecutionTraces(trace.getIdentifier());
			// add those traces to the test case
			for (ExecutionTrace executionTrace : executionTraces) {
				trace.addExecutionTrace(executionTrace);
			}
		}
		
		return spectra;
	}

}
