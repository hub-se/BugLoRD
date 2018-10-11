/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.tracecobertura.report;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.AbstractHierarchicalSpectraProvider;
import se.de.hu_berlin.informatik.stardust.provider.loader.ICoverageDataLoader;
import se.de.hu_berlin.informatik.stardust.provider.loader.tracecobertura.report.HierarchicalTraceCoberturaCountReportLoader;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.count.CountTrace;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitSpectra;

/**
 * Loads Cobertura reports to {@link HitSpectra} objects where each covered line
 * is represented by one node and each file represents one trace in the
 * resulting spectra.
 */
public class HierarchicalTraceCoberturaCountReportProvider<K extends CountTrace<SourceCodeBlock>>
		extends AbstractHierarchicalSpectraProvider<SourceCodeBlock, K, TraceCoberturaReportWrapper> {

	private ICoverageDataLoader<SourceCodeBlock, K, TraceCoberturaReportWrapper> loader;

	public HierarchicalTraceCoberturaCountReportProvider(ISpectra<SourceCodeBlock, K> lineSpectra, boolean fullSpectra) {
		super(lineSpectra, fullSpectra);

		loader = new HierarchicalTraceCoberturaCountReportLoader<SourceCodeBlock, K>(packageSpectra, classSpectra,
				methodSpectra) {

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
