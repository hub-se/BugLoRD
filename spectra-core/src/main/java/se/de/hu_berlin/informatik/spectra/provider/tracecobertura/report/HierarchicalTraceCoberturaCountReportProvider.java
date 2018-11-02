/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.report;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.count.CountTrace;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.provider.AbstractHierarchicalSpectraProvider;
import se.de.hu_berlin.informatik.spectra.provider.loader.ICoverageDataLoader;
import se.de.hu_berlin.informatik.spectra.provider.loader.tracecobertura.report.HierarchicalTraceCoberturaCountReportLoader;

/**
 * Loads Cobertura reports to {@link HitSpectra} objects where each covered line
 * is represented by one node and each file represents one trace in the
 * resulting spectra.
 */
public class HierarchicalTraceCoberturaCountReportProvider<K extends CountTrace<SourceCodeBlock>>
		extends AbstractHierarchicalSpectraProvider<SourceCodeBlock, K, TraceCoberturaReportWrapper> {

	private HierarchicalTraceCoberturaCountReportLoader<SourceCodeBlock, K> loader;

	public HierarchicalTraceCoberturaCountReportProvider(ISpectra<SourceCodeBlock, K> lineSpectra, 
			boolean fullSpectra, Path tempOutputDir) {
		super(lineSpectra, fullSpectra);

		loader = new HierarchicalTraceCoberturaCountReportLoader<SourceCodeBlock, K>(packageSpectra, classSpectra,
				methodSpectra, tempOutputDir) {

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
		loader.addExecutionTracesToSpectra(spectra);
		
		return spectra;
	}

}
