/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider.cobertura.xml;

import java.io.File;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.provider.AbstractHierarchicalSpectraProvider;
import se.de.hu_berlin.informatik.spectra.provider.loader.ICoverageDataLoader;
import se.de.hu_berlin.informatik.spectra.provider.loader.cobertura.xml.HierarchicalCoberturaXMLLoader;

/**
 * Loads Cobertura reports to {@link HitSpectra} objects where each covered line
 * is represented by one node and each file represents one trace in the
 * resulting spectra.
 */
public class HierarchicalCoberturaXMLProvider<K extends ITrace<SourceCodeBlock>>
		extends AbstractHierarchicalSpectraProvider<SourceCodeBlock, K, CoberturaCoverageWrapper> {

	private final ICoverageDataLoader<SourceCodeBlock, K, CoberturaCoverageWrapper> loader;

	public HierarchicalCoberturaXMLProvider(ISpectra<SourceCodeBlock, K> lineSpectra, boolean fullSpectra) {
		super(lineSpectra, fullSpectra);

		loader = new HierarchicalCoberturaXMLLoader<SourceCodeBlock, K>(packageSpectra, classSpectra, methodSpectra) {

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
	protected ICoverageDataLoader<SourceCodeBlock, K, CoberturaCoverageWrapper> getLoader() {
		return loader;
	}

	public boolean addData(String xmlFilePath, String identifier, boolean successful) {
		return super.addData(new CoberturaCoverageWrapper(new File(xmlFilePath), identifier, successful));
	}

}
