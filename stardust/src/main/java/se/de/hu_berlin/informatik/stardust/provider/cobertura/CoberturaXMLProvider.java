/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.cobertura;

import java.io.File;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;

/**
 * Loads cobertura.xml files to {@link Spectra} objects where each covered line is represented by one node and each file
 * represents one trace in the resulting spectra.
 */
public class CoberturaXMLProvider extends AbstractSpectraFromCoberturaXMLProvider<SourceCodeBlock> {

	public CoberturaXMLProvider() {
		super();
	}

	public CoberturaXMLProvider(boolean usesAggregate, boolean storeHits) {
		super(usesAggregate, storeHits);
	}

	public boolean addData(File xmlCoverageFile, String testIdentifier, boolean successful) {
		return addData(new CoberturaCoverageWrapper(xmlCoverageFile, testIdentifier, successful));
	}
	
	public boolean addData(String xmlCoverageFile, String testIdentifier, boolean successful) {
		return addData(new CoberturaCoverageWrapper(new File(xmlCoverageFile), testIdentifier, successful));
	}
	
	@Override
	public SourceCodeBlock getIdentifier(String packageName, String sourceFilePath, String methodNameAndSig,
			int lineNumber) {
		return new SourceCodeBlock(packageName, sourceFilePath, methodNameAndSig, lineNumber);
	}
	
}
