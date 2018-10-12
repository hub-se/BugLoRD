/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.loader.tracecobertura.report;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.cobertura.coveragedata.CoverageData;
import se.de.hu_berlin.informatik.stardust.provider.tracecobertura.coveragedata.ExecutionTraceCollector;
import se.de.hu_berlin.informatik.stardust.provider.tracecobertura.coveragedata.LineWrapper;
import se.de.hu_berlin.informatik.stardust.provider.tracecobertura.coveragedata.MyClassData;
import se.de.hu_berlin.informatik.stardust.provider.tracecobertura.coveragedata.MyLineData;
import se.de.hu_berlin.informatik.stardust.provider.tracecobertura.coveragedata.PackageData;
import se.de.hu_berlin.informatik.stardust.provider.tracecobertura.coveragedata.SourceFileData;
import se.de.hu_berlin.informatik.stardust.provider.tracecobertura.coveragedata.TraceProjectData;
import se.de.hu_berlin.informatik.stardust.provider.loader.AbstractCoverageDataLoader;
import se.de.hu_berlin.informatik.stardust.provider.tracecobertura.report.TraceCoberturaReportWrapper;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
//import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;

public abstract class TraceCoberturaReportLoader<T, K extends ITrace<T>>
		extends AbstractCoverageDataLoader<T, K, TraceCoberturaReportWrapper> {

	int traceCount = 0;

	@Override
	public boolean loadSingleCoverageData(ISpectra<T, K> lineSpectra, final TraceCoberturaReportWrapper reportWrapper,
			final boolean fullSpectra) {
		if (reportWrapper == null || reportWrapper.getReport() == null) {
			return false;
		}

		K trace = null;

		TraceProjectData projectData = reportWrapper.getReport().getProjectData();
		if (projectData == null) {
			return false;
		}

		if (reportWrapper.getIdentifier() == null) {
			trace = lineSpectra.addTrace(String.valueOf(++traceCount), reportWrapper.isSuccessful());
		} else {
			trace = lineSpectra.addTrace(reportWrapper.getIdentifier(), reportWrapper.isSuccessful());
		}
		
		// loop over all packages
		@SuppressWarnings("unchecked")
		Iterator<PackageData> itPackages = projectData.getPackages().iterator();
		while (itPackages.hasNext()) {
			PackageData packageData = itPackages.next();
			final String packageName = packageData.getName();

			onNewPackage(packageName, trace);

			// loop over all classes of the package
			@SuppressWarnings("unchecked")
			Iterator<SourceFileData> itSourceFiles = packageData.getSourceFiles().iterator();
			while (itSourceFiles.hasNext()) {
				@SuppressWarnings("unchecked")
				Iterator<MyClassData> itClasses = itSourceFiles.next().getClasses().iterator();
				while (itClasses.hasNext()) {
					MyClassData classData = itClasses.next();
					// TODO: use actual class name!?
					final String actualClassName = classData.getName();
					final String sourceFilePath = classData.getSourceFileName();

					onNewClass(packageName, sourceFilePath, trace);

					// loop over all methods of the class
					// SortedSet<String> sortedMethods = new TreeSet<>();
					// sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
					Iterator<String> itMethods = classData.getMethodNamesAndDescriptors().iterator();
					while (itMethods.hasNext()) {
						final String methodNameAndSig = itMethods.next();
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
						Iterator<CoverageData> itLines = classData.getLines(methodNameAndSig).iterator();
						while (itLines.hasNext()) {
							LineWrapper lineData = new LineWrapper(itLines.next());

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
		

		// TODO debug output
//		for (Object classData : projectData.getClasses()) {
//			Log.out(true, this, ((MyClassData)classData).getName());
//		}
//		Log.out(true, this, "Trace: " + reportWrapper.getIdentifier());
		Map<Integer, String> idToClassNameMap = projectData.getIdToClassNameMap();
		for (Entry<Long, List<String>> executionTrace : projectData.getExecutionTraces().entrySet()) {
			List<Integer> traceOfNodeIDs = new ArrayList<>();
			int lastNodeIndex = -1;
			
//			Log.out(true, this, "Thread: " + executionTrace.getKey());
			for (String string : executionTrace.getValue()) {
				String[] statement = string.split(ExecutionTraceCollector.SPLIT_CHAR);
				// TODO store the class names with '.' from the beginning, or use the '/' version?
				String classSourceFileName = idToClassNameMap.get(Integer.valueOf(statement[0]));
				MyClassData classData = projectData.getClassData(classSourceFileName.replace('/', '.'));

				if (classData != null) {
					Integer counterId = Integer.valueOf(statement[1]);
					MyLineData lineData = classData.getCounterIdToMyLineDataMap().get(counterId);
//					Log.out(true, this, classSourceFileName + ", counter ID " + statement[1] +
//							", line " + (lineData == null ? "null" : String.valueOf(lineData.getLineNumber()) + ", hits: " + lineData.getHits()));
					
					if (lineData != null) {
						int nodeIndex = getNodeIndex(classData.getSourceFileName(), lineData.getLineNumber());
						if (nodeIndex != -1) {
							if (nodeIndex == lastNodeIndex) {
//								Log.out(true, this, "(node repeated)");
							} else {
								traceOfNodeIDs.add(nodeIndex);
								lastNodeIndex = nodeIndex;
//								Log.out(true, this, "(node index: " + nodeIndex + ")");
							}
						} else {
//							Log.out(true, this, "(node not found in spectra)");
						}
					}
				} else {
//					Log.out(true, this, classSourceFileName + 
//							" (not found), counter ID " + statement[1]);
				}
			}
			
			// add the execution trace to the coverage trace and, thus, to the spectra
			trace.addExecutionTrace(traceOfNodeIDs);
		}

		return true;
	}

}
