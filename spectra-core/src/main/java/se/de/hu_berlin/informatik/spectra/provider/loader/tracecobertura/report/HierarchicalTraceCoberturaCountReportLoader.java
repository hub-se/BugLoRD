/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider.loader.tracecobertura.report;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.count.CountTrace;
import se.de.hu_berlin.informatik.spectra.core.hit.HierarchicalHitSpectra;

public abstract class HierarchicalTraceCoberturaCountReportLoader<K extends CountTrace<SourceCodeBlock>> extends TraceCoberturaCountReportLoader<K> {

	private final HierarchicalHitSpectra<String, SourceCodeBlock> methodSpectra;
	private final HierarchicalHitSpectra<String, String> classSpectra;
	private final HierarchicalHitSpectra<String, String> packageSpectra;

	public HierarchicalTraceCoberturaCountReportLoader(HierarchicalHitSpectra<String, String> packageSpectra,
			HierarchicalHitSpectra<String, String> classSpectra, HierarchicalHitSpectra<String, SourceCodeBlock> methodSpectra,
			Path tempOutputDir) {
		super(tempOutputDir);
		this.methodSpectra = methodSpectra;
		this.classSpectra = classSpectra;
		this.packageSpectra = packageSpectra;
	}

	@Override
	protected void onNewClass(String packageName, String classFilePath, K currentTrace) {
		super.onNewClass(packageName, classFilePath, currentTrace);
		packageSpectra.setParent(packageName, classFilePath);
	}

	@Override
	protected void onNewMethod(String packageName, String classFilePath, String methodName, K currentTrace) {
		super.onNewMethod(packageName, classFilePath, methodName, currentTrace);
		classSpectra.setParent(classFilePath, methodName);
	}

	@Override
	protected void onNewLine(String packageName, String classFilePath, String methodName, SourceCodeBlock lineIdentifier,
			ISpectra<SourceCodeBlock, K> lineSpectra, K currentTrace, boolean fullSpectra, long numberOfHits) {
		super.onNewLine(
				packageName, classFilePath, methodName, lineIdentifier, lineSpectra, currentTrace, fullSpectra,
				numberOfHits);
		methodSpectra.setParent(methodName, lineIdentifier);
	}

}
