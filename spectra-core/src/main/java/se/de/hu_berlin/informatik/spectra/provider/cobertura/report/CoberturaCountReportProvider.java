/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider.cobertura.report;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.Node.NodeType;
import se.de.hu_berlin.informatik.spectra.core.count.CountTrace;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.provider.AbstractSpectraProvider;
import se.de.hu_berlin.informatik.spectra.provider.loader.ICoverageDataLoader;
import se.de.hu_berlin.informatik.spectra.provider.loader.cobertura.report.CoberturaCountReportLoader;

/**
 * Loads Cobertura reports to {@link HitSpectra} objects where each covered line
 * is represented by one node and each file represents one trace in the
 * resulting spectra.
 */
public class CoberturaCountReportProvider<K extends CountTrace<SourceCodeBlock>>
		extends AbstractSpectraProvider<SourceCodeBlock, K, CoberturaReportWrapper> {

	private final ICoverageDataLoader<SourceCodeBlock, K, CoberturaReportWrapper> loader;

	public CoberturaCountReportProvider(ISpectra<SourceCodeBlock, K> lineSpectra, boolean fullSpectra) {
		super(lineSpectra, fullSpectra);

		loader = new CoberturaCountReportLoader<SourceCodeBlock, K>() {

			@Override
			public SourceCodeBlock getIdentifier(String packageName, String sourceFilePath, String methodNameAndSig,
					int lineNumber, NodeType nodeType) {
				return new SourceCodeBlock(packageName, sourceFilePath, methodNameAndSig, lineNumber, nodeType);
			}
			
			@Override
			public int getNodeIndex(String sourceFilePath, int lineNumber, NodeType nodeType) {
				SourceCodeBlock identifier = new SourceCodeBlock(null, sourceFilePath, null, lineNumber, nodeType);
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
	protected ICoverageDataLoader<SourceCodeBlock, K, CoberturaReportWrapper> getLoader() {
		return loader;
	}

}
