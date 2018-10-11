/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.tracecobertura.report;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.AbstractSpectraProvider;
import se.de.hu_berlin.informatik.stardust.provider.loader.ICoverageDataLoader;
import se.de.hu_berlin.informatik.stardust.provider.loader.tracecobertura.report.TraceCoberturaReportLoader;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitSpectra;

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

		};
	}

	@Override
	protected ICoverageDataLoader<SourceCodeBlock, K, TraceCoberturaReportWrapper> getLoader() {
		return loader;
	}

}
