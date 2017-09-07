/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.cobertura.report;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.AbstractHierarchicalSpectraProvider;
import se.de.hu_berlin.informatik.stardust.provider.loader.ICoverageDataLoader;
import se.de.hu_berlin.informatik.stardust.provider.loader.cobertura.report.HierarchicalCoberturaReportLoader;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitSpectra;

/**
 * Loads Cobertura reports to {@link HitSpectra} objects where each covered line
 * is represented by one node and each file represents one trace in the
 * resulting spectra.
 */
public class HierarchicalCoberturaReportProvider<K extends ITrace<SourceCodeBlock>>
		extends AbstractHierarchicalSpectraProvider<SourceCodeBlock, K, CoberturaReportWrapper> {

	private ICoverageDataLoader<SourceCodeBlock, K, CoberturaReportWrapper> loader;

	public HierarchicalCoberturaReportProvider(ISpectra<SourceCodeBlock, K> lineSpectra, boolean fullSpectra) {
		super(lineSpectra, fullSpectra);

		loader = new HierarchicalCoberturaReportLoader<SourceCodeBlock, K>(packageSpectra, classSpectra,
				methodSpectra) {

			@Override
			public SourceCodeBlock getIdentifier(String packageName, String sourceFilePath, String methodNameAndSig,
					int lineNumber) {
				return new SourceCodeBlock(packageName, sourceFilePath, methodNameAndSig, lineNumber);
			}

		};
	}

	@Override
	protected ICoverageDataLoader<SourceCodeBlock, K, CoberturaReportWrapper> getLoader() {
		return loader;
	}

}
