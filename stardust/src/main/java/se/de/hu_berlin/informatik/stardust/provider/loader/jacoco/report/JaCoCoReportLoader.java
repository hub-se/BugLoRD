/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.loader.jacoco.report;

import java.util.Iterator;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;

import se.de.hu_berlin.informatik.stardust.provider.jacoco.report.JaCoCoReportWrapper;
import se.de.hu_berlin.informatik.stardust.provider.loader.ICoverageDataLoader;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public abstract class JaCoCoReportLoader<T, K extends ITrace<T>>
		implements ICoverageDataLoader<T, K, JaCoCoReportWrapper> {

	int traceCount = 0;

	@Override
	public boolean loadSingleCoverageData(ISpectra<T, K> lineSpectra, final JaCoCoReportWrapper reportWrapper,
			final boolean onlyAddNodes) {
		if (reportWrapper == null) {
			return false;
		}

		IBundleCoverage projectData = reportWrapper.getCoverageBundle();

		if (projectData == null) {
			return false;
		}
		
		ITrace<T> trace = null;

		if (onlyAddNodes) {
			Log.out(this, "Populating spectra with given nodes...");
		} else {
			if (reportWrapper.getIdentifier() == null) {
				trace = lineSpectra.addTrace(
						String.valueOf(++traceCount), 
						reportWrapper.isSuccessful());	
			} else {
				trace = lineSpectra.addTrace(
						reportWrapper.getIdentifier(), 
						reportWrapper.isSuccessful());
			}
		}

		// loop over all packages
		Iterator<IPackageCoverage> itPackages = projectData.getPackages().iterator();
		while (itPackages.hasNext()) {
			IPackageCoverage packageData = itPackages.next();
			final String packageName = packageData.getName().replace('/', '.');

			onNewPackage(packageName);
			
			// loop over all classes of the package
			Iterator<IClassCoverage> itClasses = packageData.getClasses().iterator();
			while (itClasses.hasNext()) {
				IClassCoverage classData = itClasses.next();
				//TODO: use actual class name!?
				
				String sourceFilePath = null;
				
				String actualClassPath = classData.getName();
				int pos = actualClassPath.indexOf('$');
				if (pos != -1) {
					actualClassPath = actualClassPath.substring(0, pos);
				}
				String sourceFileName = classData.getSourceFileName();
				if (sourceFileName != null && !sourceFileName.equals("")) {
					int pos2 = actualClassPath.lastIndexOf('/');
					if (pos2 != -1) {
						sourceFilePath = actualClassPath.substring(0, pos2 + 1) + sourceFileName;
					} else {
						sourceFilePath = sourceFileName;
					}
				} else {
					sourceFilePath = actualClassPath + ".java";
				}

				onNewClass(packageName, sourceFileName);

				// loop over all methods of the class
				Iterator<IMethodCoverage> itMethods = classData.getMethods().iterator();
				while (itMethods.hasNext()) {
					final IMethodCoverage method = itMethods.next();
					final String methodNameAndSig = method.getName() + //(method.getSignature() == null ? method.getDesc() : method.getSignature());
							 method.getDesc();

					final String methodIdentifier = String.format("%s:%s", actualClassPath, methodNameAndSig);

					onNewMethod(packageName, sourceFileName, methodIdentifier);

					// loop over all lines of the method
					for (int i = method.getFirstLine(); i <= method.getLastLine(); ++i) {
						ILine line = method.getLine(i);

						final int status = line.getStatus();
						if (status != ICounter.EMPTY) {
							// set node involvement
							final T lineIdentifier = getIdentifier(
									packageName, sourceFilePath, methodNameAndSig, i);

							if (onlyAddNodes) {
								lineSpectra.getOrCreateNode(lineIdentifier);
							} else {
								if (status != ICounter.NOT_COVERED) {
									trace.setInvolvement(lineIdentifier, true);
								}
							}

							onNewLine(packageName, sourceFileName, methodIdentifier, lineIdentifier);
						}
					}
				}
			}
		}
		return true;
	}

	protected void onNewPackage(String packageName) {
		// nothing to do
	}

	protected void onNewClass(String packageName, String classFilePath) {
		// nothing to do
	}

	protected void onNewMethod(String packageName, String classFilePath, String methodName) {
		// nothing to do
	}

	protected void onNewLine(String packageName, String classFilePath, String methodName, T lineIdentifier) {
		// nothing to do
	}

}
