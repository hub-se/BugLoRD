/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.report;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
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

	private ICoverageDataLoader<SourceCodeBlock, K, TraceCoberturaReportWrapper> loader;

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

}
