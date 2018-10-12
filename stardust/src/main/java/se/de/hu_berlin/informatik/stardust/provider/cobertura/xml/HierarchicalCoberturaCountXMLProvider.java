/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.cobertura.xml;

import java.io.File;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.AbstractHierarchicalSpectraProvider;
import se.de.hu_berlin.informatik.stardust.provider.loader.ICoverageDataLoader;
import se.de.hu_berlin.informatik.stardust.provider.loader.cobertura.xml.HierarchicalCoberturaCountXMLLoader;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.count.CountTrace;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitSpectra;

/**
 * Loads Cobertura reports to {@link HitSpectra} objects where each covered line
 * is represented by one node and each file represents one trace in the
 * resulting spectra.
 */
public class HierarchicalCoberturaCountXMLProvider<K extends CountTrace<SourceCodeBlock>>
		extends AbstractHierarchicalSpectraProvider<SourceCodeBlock, K, CoberturaCoverageWrapper> {

	private ICoverageDataLoader<SourceCodeBlock, K, CoberturaCoverageWrapper> loader;

	public HierarchicalCoberturaCountXMLProvider(ISpectra<SourceCodeBlock, K> lineSpectra, boolean fullSpectra) {
		super(lineSpectra, fullSpectra);

		loader = new HierarchicalCoberturaCountXMLLoader<SourceCodeBlock, K>(packageSpectra, classSpectra,
				methodSpectra) {

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
