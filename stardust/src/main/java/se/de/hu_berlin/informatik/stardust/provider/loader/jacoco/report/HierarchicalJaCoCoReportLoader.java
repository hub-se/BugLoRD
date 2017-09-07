/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.loader.jacoco.report;

import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HierarchicalHitSpectra;

public abstract class HierarchicalJaCoCoReportLoader<T, K extends ITrace<T>> extends JaCoCoReportLoader<T, K> {

	private HierarchicalHitSpectra<String, T> methodSpectra;
	private HierarchicalHitSpectra<String, String> classSpectra;
	private HierarchicalHitSpectra<String, String> packageSpectra;

	public HierarchicalJaCoCoReportLoader(HierarchicalHitSpectra<String, String> packageSpectra,
			HierarchicalHitSpectra<String, String> classSpectra,
			HierarchicalHitSpectra<String, T> methodSpectra) {
		this.methodSpectra = methodSpectra;
		this.classSpectra = classSpectra;
		this.packageSpectra = packageSpectra;
	}

	@Override
	protected void onNewPackage(String packageName) {
		// do nothing
	}

	@Override
	protected void onNewClass(String packageName, String classFilePath) {
		packageSpectra.setParent(packageName, classFilePath);
	}

	@Override
	protected void onNewMethod(String packageName, String classFilePath, String methodName) {
		classSpectra.setParent(classFilePath, methodName);
	}

	@Override
	protected void onNewLine(String packageName, String classFilePath, String methodName, T lineIdentifier) {
		methodSpectra.setParent(methodName, lineIdentifier);
	}

}
