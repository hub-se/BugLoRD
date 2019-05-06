/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider.loader.cobertura.report;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.report.CoberturaReportWrapper;
import se.de.hu_berlin.informatik.spectra.provider.loader.AbstractCoverageDataLoader;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.LineData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ClassData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.PackageData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SourceFileData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageData;

public abstract class CoberturaReportLoader<T, K extends ITrace<T>>
		extends AbstractCoverageDataLoader<T, K, CoberturaReportWrapper> {

	int traceCount = 0;

	@Override
	public boolean loadSingleCoverageData(ISpectra<T, K> lineSpectra, final CoberturaReportWrapper reportWrapper,
			final boolean fullSpectra) {
		if (reportWrapper == null || reportWrapper.getReport() == null) {
			return false;
		}

		K trace = null;

		ProjectData projectData = reportWrapper.getReport().getProjectData();
		if (projectData == null) {
			return false;
		}

		if (reportWrapper.getIdentifier() == null) {
			trace = lineSpectra.addTrace(String.valueOf(++traceCount), traceCount, reportWrapper.isSuccessful());
		} else {
			trace = lineSpectra.addTrace(reportWrapper.getIdentifier(), ++traceCount, reportWrapper.isSuccessful());
		}

		// loop over all packages
        for (CoverageData coverageData2 : projectData.getPackages()) {
            PackageData packageData = (PackageData) coverageData2;
            final String packageName = packageData.getName();

            onNewPackage(packageName, trace);

            // loop over all classes of the package
            for (SourceFileData sourceFileData : packageData.getSourceFiles()) {
                for (CoverageData coverageData1 : sourceFileData.getClasses()) {
                    ClassData classData = (ClassData) coverageData1;
                    // TODO: use actual class name!?
                    final String actualClassName = classData.getName();
                    final String sourceFilePath = classData.getSourceFileName();

                    onNewClass(packageName, sourceFilePath, trace);

                    // loop over all methods of the class
                    // SortedSet<String> sortedMethods = new TreeSet<>();
                    // sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
                    for (String methodNameAndSig : classData.getMethodNamesAndDescriptors()) {
                        // String name = methodNameAndSig.substring(0,
                        // methodNameAndSig.indexOf('('));
                        // String signature =
                        // methodNameAndSig.substring(methodNameAndSig.indexOf('('));

                        final String methodIdentifier = String.format("%s:%s", actualClassName, methodNameAndSig);

                        onNewMethod(packageName, sourceFilePath, methodIdentifier, trace);

                        // loop over all lines of the method
                        // SortedSet<CoverageData> sortedLines = new
                        // TreeSet<>();
                        // sortedLines.addAll(classData.getLines(methodNameAndSig));
                        for (CoverageData coverageData : classData.getLines(methodNameAndSig)) {
                            LineData lineData = (LineData) coverageData;

                            // set node involvement
                            final T lineIdentifier = getIdentifier(
                                    packageName, sourceFilePath, methodNameAndSig, lineData.getLineNumber());

                            onNewLine(
                                    packageName, sourceFilePath, methodIdentifier, lineIdentifier, lineSpectra, trace,
                                    fullSpectra, lineData.getHits());
                        }

                        onLeavingMethod(packageName, sourceFilePath, methodIdentifier, lineSpectra, trace);
                    }

                    onLeavingClass(packageName, sourceFilePath, lineSpectra, trace);
                }
            }

            onLeavingPackage(packageName, lineSpectra, trace);
        }
		return true;
	}

}
