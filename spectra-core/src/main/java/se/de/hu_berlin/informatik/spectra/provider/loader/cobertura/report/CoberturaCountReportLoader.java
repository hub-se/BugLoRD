/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider.loader.cobertura.report;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.count.CountTrace;

public abstract class CoberturaCountReportLoader<T, K extends CountTrace<T>> extends CoberturaReportLoader<T, K> {

	@Override
	protected void onNewLine(String packageName, String classFilePath, String methodName, T lineIdentifier,
			ISpectra<T, K> lineSpectra, K currentTrace, boolean fullSpectra, long numberOfHits) {
		super.onNewLine(
				packageName, classFilePath, methodName, lineIdentifier, lineSpectra, currentTrace, fullSpectra,
				numberOfHits);
		if (numberOfHits > 0) {
			currentTrace.setHits(lineIdentifier, numberOfHits);
		} else if (fullSpectra) {
			lineSpectra.getOrCreateNode(lineIdentifier);
		}
	}

}
