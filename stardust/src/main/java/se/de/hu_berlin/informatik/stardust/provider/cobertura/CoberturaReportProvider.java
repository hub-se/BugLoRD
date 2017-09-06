/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.cobertura;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.HitSpectra;

/**
 * Loads Cobertura reports to {@link HitSpectra} objects where each covered line is represented by one node and each file
 * represents one trace in the resulting spectra.
 */
public class CoberturaReportProvider extends AbstractSpectraFromCoberturaReportProvider<SourceCodeBlock> {

	public CoberturaReportProvider() {
		super();
	}

	public CoberturaReportProvider(boolean usesAggregate, boolean storeHits) {
		super(usesAggregate, storeHits);
	}

	@Override
	public SourceCodeBlock getIdentifier(String packageName, String sourceFilePath, String methodNameAndSig,
			int lineNumber) {
		return new SourceCodeBlock(packageName, sourceFilePath, methodNameAndSig, lineNumber);
	}
	
}
