/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider.loader.tracecobertura.report;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.RawTraceCollector;
import se.de.hu_berlin.informatik.spectra.provider.loader.AbstractCoverageDataLoader;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ExecutionTraceCollector;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ClassData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.LineData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.PackageData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SourceFileData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.report.TraceCoberturaReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public abstract class TraceCoberturaReportLoader<T, K extends ITrace<T>>
		extends AbstractCoverageDataLoader<T, K, TraceCoberturaReportWrapper> {

	private Path tempOutputDir;
	private RawTraceCollector traceCollector;

	public TraceCoberturaReportLoader(Path tempOutputDir) {
		this.tempOutputDir = tempOutputDir;
		traceCollector = new RawTraceCollector(this.tempOutputDir);
	}
	
	private int traceCount = 0;
	
	@Override
	public boolean loadSingleCoverageData(ISpectra<T, K> lineSpectra, final TraceCoberturaReportWrapper reportWrapper,
			final boolean fullSpectra) {
		if (reportWrapper == null || reportWrapper.getReport() == null) {
			return false;
		}

		K trace = null;

		ProjectData projectData = reportWrapper.getReport().getProjectData();
		if (projectData == null) {
			return false;
		}

		String testId;
		if (reportWrapper.getIdentifier() == null) {
			testId = String.valueOf(++traceCount);
		} else {
			++traceCount;
			testId =reportWrapper.getIdentifier();
		}
		
		trace = lineSpectra.addTrace(testId, traceCount, reportWrapper.isSuccessful());
		
		// loop over all packages
		Iterator<CoverageData> itPackages = projectData.getPackages().iterator();
		while (itPackages.hasNext()) {
			PackageData packageData = (PackageData) itPackages.next();
			final String packageName = packageData.getName();

			onNewPackage(packageName, trace);

			// loop over all classes of the package
			Iterator<SourceFileData> itSourceFiles = packageData.getSourceFiles().iterator();
			while (itSourceFiles.hasNext()) {
				Iterator<CoverageData> itClasses = itSourceFiles.next().getClasses().iterator();
				while (itClasses.hasNext()) {
					ClassData classData = (ClassData) itClasses.next();
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
							LineData lineData = (LineData) itLines.next();

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
		
		if (projectData.getExecutionTraces() == null) {
			Log.err(this, "Execution trace is null for test '%s'.", testId);
			return true;
		}

		if (projectData.getExecutionTraces().isEmpty()) {
			Log.warn(this, "No execution trace for test '%s'.", testId);
		} else {
			// TODO debug output
			// for (Object classData : projectData.getClasses()) {
			// Log.out(true, this, ((MyClassData)classData).getName());
			// }
			// Log.out(true, this, "Trace: " + reportWrapper.getIdentifier());
			Map<Integer, String> idToClassNameMap = projectData.getIdToClassNameMap();
			int threadId = -1;
			for (Entry<Long, List<String>> executionTrace : projectData.getExecutionTraces().entrySet()) {
				++threadId;
				List<Integer> traceOfNodeIDs = new ArrayList<>();
				// int lastNodeIndex = -1;

				// Log.out(true, this, "Thread: " + executionTrace.getKey());
				for (String string : executionTrace.getValue()) {
					String[] statement = string.split(ExecutionTraceCollector.SPLIT_CHAR);
					// Log.out(true, this, "statement: " + string);
					// TODO store the class names with '.' from the beginning, or use the '/' version?
					String classSourceFileName = idToClassNameMap.get(Integer.valueOf(statement[0]));
					if (classSourceFileName == null) {
						throw new IllegalStateException("No class name found for class ID: " + statement[0]);
					}
					ClassData classData = projectData.getClassData(classSourceFileName.replace('/', '.'));

					if (classData != null) {
						Integer counterId = Integer.valueOf(statement[1]);
						Integer lineNumber = classData.getCounterIdToLineNumberMap().get(counterId);
						Log.out(true, this, classSourceFileName + ", counter  ID " + statement[1] +
								", line " + (lineNumber == null ? "null" : String.valueOf(lineNumber)) +
								(statement.length > 2 ? " (from branch)" : ""));

						if (lineNumber != null) {
							int nodeIndex = getNodeIndex(classData.getSourceFileName(), lineNumber);
							if (nodeIndex != -1) {
								traceOfNodeIDs.add(nodeIndex);
							} else {
								throw new IllegalStateException("Node not found in spectra: "
										+ classData.getSourceFileName() + ":" + lineNumber);
							}
						} else {
							throw new IllegalStateException("No line data found for counter ID: " + counterId
									+ " in class: " + classData.getName());
						}
					} else {
						throw new IllegalStateException("Class data for '" + classSourceFileName + "' not found.");
					}
				}

				// add the execution trace to the coverage trace and, thus, to the spectra
//				 trace.addExecutionTrace(traceOfNodeIDs);
				// collect the raw trace for future compression, etc.
				traceCollector.addRawTraceToPool(traceCount, threadId, traceOfNodeIDs);
			}
		}
		
		return true;
	}

	public void addExecutionTracesToSpectra(ISpectra<SourceCodeBlock, ? super K> spectra) {
		// store the indexer with the spectra
		spectra.setIndexer(traceCollector.getIndexer());
		
		// generate execution traces from raw traces
		spectra.setRawTraceCollector(traceCollector);
		
//		// generate the execution traces for each test case and add them to the spectra;
//		// this needs to be done AFTER all tests have been executed
//		for (ITrace<?> trace : spectra.getTraces()) {
//			// generate execution traces from collected raw traces
//			List<ExecutionTrace> executionTraces = traceCollector.getExecutionTraces(trace.getIndex());
//			if (executionTraces != null) {
//				// add those traces to the test case
//				for (ExecutionTrace executionTrace : executionTraces) {
//					trace.addExecutionTrace(executionTrace);
//				}
//			}
//		}
//		
//		// remove temporary zip file containing the raw traces
//		try {
//			traceCollector.finalize();
//		} catch (Throwable e) {
//			// meh...
//		}
	}
	
}
