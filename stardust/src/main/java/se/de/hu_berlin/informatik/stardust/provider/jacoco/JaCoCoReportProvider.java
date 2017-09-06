/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.jacoco;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.HitSpectra;

/**
 * Loads JaCoCo reports to {@link HitSpectra} objects where each covered line is represented by one node and each file
 * represents one trace in the resulting spectra.
 */
public class JaCoCoReportProvider extends AbstractSpectraFromJaCoCoReportProvider<SourceCodeBlock> {

	public JaCoCoReportProvider() {
		super();
	}

	public JaCoCoReportProvider(boolean usesAggregate, boolean storeHits, boolean fullSpectra) {
		super(usesAggregate, storeHits, fullSpectra);
	}

	@Override
	public SourceCodeBlock getIdentifier(String packageName, String sourceFilePath, String methodNameAndSig,
			int lineNumber) {
		return new SourceCodeBlock(packageName, sourceFilePath, methodNameAndSig, lineNumber);
	}
	
}
