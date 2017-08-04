/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.jacoco;

import java.io.File;
import java.util.Iterator;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import se.de.hu_berlin.informatik.stardust.spectra.HierarchicalSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.IMutableTrace;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Loads JaCoCo reports to {@link Spectra} objects where each covered line is represented by one node and each file
 * represents one trace in the resulting spectra.
 * 
 * @author Simon
 *
 * @param <T>
 * the type of nodes in the spectra to provide
 */
public abstract class AbstractSpectraFromJaCoCoReportProvider<T> extends AbstractSpectraFromJaCoCoProvider<T, JaCoCoReportWrapper> {

	/**
	 * Create a JaCoCo provider.
	 */
	public AbstractSpectraFromJaCoCoReportProvider() {
		this(true, false, false);
	}

	public AbstractSpectraFromJaCoCoReportProvider(boolean usesAggregate, boolean storeHits, boolean fullSpectra) {
		super(usesAggregate, storeHits, fullSpectra);
	}

	@Override
	public boolean addData(JaCoCoReportWrapper reportWrapper) {
		storeLastCoverageData(reportWrapper);

		if (usesAggregate()) {
			return this.loadSingleCoverageData(reportWrapper, getAggregateSpectra());
		} else {
			getDataList().add(reportWrapper);
		}
		return true;
	}

	@Override
	public boolean loadSingleCoverageData(final JaCoCoReportWrapper reportWrapper, final ISpectra<T> lineSpectra,
			final HierarchicalSpectra<String, T> methodSpectra,
			final HierarchicalSpectra<String, String> classSpectra,
			final HierarchicalSpectra<String, String> packageSpectra,
			final boolean onlyAddInitialNodes) {
		if (reportWrapper == null) {
			return false;
		}

		IMutableTrace<T> trace = null;

		IBundleCoverage projectData = reportWrapper.getCoverageBundle();

		if (projectData == null) {
			return false;
		}

		if (onlyAddInitialNodes) {
			Log.out(this, "Populating spectra with initial set of nodes...");
		} else {
			if (reportWrapper.getIdentifier() == null) {
				trace = lineSpectra.addTrace(
						"_", 
						reportWrapper.isSuccessful());	
			} else {
				trace = lineSpectra.addTrace(
						reportWrapper.getIdentifier(), 
						reportWrapper.isSuccessful());
			}
		}

		final boolean createHierarchicalSpectra = methodSpectra != null && classSpectra != null
				&& packageSpectra != null;

		// loop over all packages
		Iterator<IPackageCoverage> itPackages = projectData.getPackages().iterator();
		while (itPackages.hasNext()) {
			IPackageCoverage packageData = itPackages.next();
			final String packageName = packageData.getName().replace(File.separatorChar, '.');

			// loop over all classes of the package
			Iterator<IClassCoverage> itClasses = packageData.getClasses().iterator();
			while (itClasses.hasNext()) {
				IClassCoverage classData = itClasses.next();
				//TODO: use actual class name!?
				final String actualClassName = classData.getName();
				final String sourceFilePath = actualClassName + ".java";

				// if necessary, create hierarchical spectra
				if (createHierarchicalSpectra) {
					packageSpectra.setParent(packageName, sourceFilePath);
				}

				// loop over all methods of the class
				Iterator<IMethodCoverage> itMethods = classData.getMethods().iterator();
				while (itMethods.hasNext()) {
					final IMethodCoverage method = itMethods.next();
					final String methodNameAndSig = method.getName() + //(method.getSignature() == null ? method.getDesc() : method.getSignature());
							 method.getDesc();

					final String methodIdentifier = String.format("%s:%s", actualClassName, methodNameAndSig);

					// if necessary, create hierarchical spectra
					if (createHierarchicalSpectra) {
						classSpectra.setParent(sourceFilePath, methodIdentifier);
					}

					// loop over all lines of the method
					for (int i = method.getFirstLine(); i <= method.getLastLine(); ++i) {
						ILine line = method.getLine(i);

						final int status = line.getStatus();
						if (status != ICounter.EMPTY) {
							// set node involvement
							final T lineIdentifier = getIdentifier(
									packageName, sourceFilePath, methodNameAndSig, i);

							if (onlyAddInitialNodes) {
								lineSpectra.getOrCreateNode(lineIdentifier);
							} else {
								if (status != ICounter.NOT_COVERED) {
									trace.setInvolvement(lineIdentifier, true);
								}
							}

							// if necessary, create hierarchical spectra
							if (createHierarchicalSpectra) {
								methodSpectra.setParent(methodIdentifier, lineIdentifier);
							}
						}
					}
				}
			}
		}
		return true;
	}

}
