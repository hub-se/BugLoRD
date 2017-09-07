/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.cobertura.xml;

import java.io.File;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.AbstractSpectraProvider;
import se.de.hu_berlin.informatik.stardust.provider.loader.ICoverageDataLoader;
import se.de.hu_berlin.informatik.stardust.provider.loader.cobertura.xml.CoberturaXMLLoader;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitSpectra;

/**
 * Loads cobertura.xml files to {@link HitSpectra} objects where each covered
 * line is represented by one node and each file represents one trace in the
 * resulting spectra.
 */
public class CoberturaXMLProvider<K extends ITrace<SourceCodeBlock>>
		extends AbstractSpectraProvider<SourceCodeBlock, K, CoberturaCoverageWrapper> {

	private ICoverageDataLoader<SourceCodeBlock, K, CoberturaCoverageWrapper> loader;
	
	public CoberturaXMLProvider(ISpectra<SourceCodeBlock, K> lineSpectra,
			boolean fullSpectra) {
		super(lineSpectra, fullSpectra);

		loader = new CoberturaXMLLoader<SourceCodeBlock, K>() {

			@Override
			public SourceCodeBlock getIdentifier(String packageName, String sourceFilePath, String methodNameAndSig,
					int lineNumber) {
				return new SourceCodeBlock(packageName, sourceFilePath, methodNameAndSig, lineNumber);
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
