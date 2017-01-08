/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.NativeReport;
import se.de.hu_berlin.informatik.c2r.LockableProjectData;
import se.de.hu_berlin.informatik.c2r.MyTouchCollector;
import se.de.hu_berlin.informatik.c2r.StatisticsData;
import se.de.hu_berlin.informatik.c2r.TestStatistics;
import se.de.hu_berlin.informatik.c2r.TestWrapper;
import se.de.hu_berlin.informatik.stardust.provider.LineWrapper;
import se.de.hu_berlin.informatik.stardust.provider.MyLineData;
import se.de.hu_berlin.informatik.stardust.provider.ReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.OutputStreamManipulationUtilities;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;
import se.de.hu_berlin.informatik.utils.tracking.TrackingStrategy;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class TestRunAndReportModule extends AbstractModule<TestWrapper, ReportWrapper> {

	final private String testOutput;
	//	final private Path dataFile;
	//	final private Path dataFileBackup;
	//	final private Path coverageXmlFile;
	final private Arguments reportArguments;
	//	final private ReportFormat reportFormat;
	final private boolean debugOutput;
	final private Long timeout;

	final private StatisticsCollector<StatisticsData> statisticsContainer;

	private ProjectData initialProjectData;
	//	private Field globalProjectData = null;
	//	private Lock globalProjectDataLock = null;

	final private boolean fullSpectra;

	final private TestRunModule testRunner;
	private Map<Class<?>, Integer> registeredClasses;

	public TestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir) {
		this(dataFile, testOutput, srcDir, false, false, null, 1);
	}

	public TestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir, 
			final boolean fullSpectra, final boolean debugOutput, Long timeout, final int repeatCount) {
		this(dataFile, testOutput, srcDir, fullSpectra, debugOutput, timeout, repeatCount, null);
	}

	@SuppressWarnings("unchecked")
	public TestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir, 
			final boolean fullSpectra, final boolean debugOutput, Long timeout, final int repeatCount,
			final StatisticsCollector<StatisticsData> statisticsContainer) {
		super(true);
		this.statisticsContainer = statisticsContainer;
		this.testOutput = testOutput;

		String baseDir = null;
		validateDataFile(dataFile.toString());
		validateAndCreateDestinationDirectory(this.testOutput);

		ArgumentsBuilder builder = new ArgumentsBuilder();
		builder.setDataFile(dataFile.toString());
		builder.setDestinationDirectory(this.testOutput);
		builder.addSources(srcDir, baseDir == null);

		reportArguments = builder.build();

		this.fullSpectra = fullSpectra;

		//in the original data file, all (executable) lines are contained, even though they are not executed at all;
		//so if we want to have the full spectra, we have to make a backup and load it again for each run test
		if (this.fullSpectra) {
			initialProjectData = CoverageDataFileHandler.loadCoverageData(dataFile.toFile());
		} else {
			initialProjectData = new ProjectData();
		}

		this.debugOutput = debugOutput;
		this.timeout = timeout;

		this.testRunner = new TestRunModule(this.testOutput, debugOutput, this.timeout, repeatCount);

		//disable std output
		if (!this.debugOutput) {
			System.out.flush();
			OutputStreamManipulationUtilities.switchOffStdOut();
		}

		//initialize/reset the project data
		ProjectData.saveGlobalProjectData();
		//turn off auto saving (removes the shutdown hook inside of Cobertura)
		ProjectData.turnOffAutoSave();

		//enable std output
		if (!this.debugOutput) {
			System.out.flush();
			OutputStreamManipulationUtilities.switchOnStdOut();
		}

		//try to get access to necessary fields from Cobertura with reflection...
		try {
			Field registeredClassesField = TouchCollector.class.getDeclaredField("registeredClasses");
			registeredClassesField.setAccessible(true);
			registeredClasses = (Map<Class<?>, Integer>) registeredClassesField.get(null);
		} catch (Exception e) {
			//if reflection doesn't work, get the classes from the data file
			Collection<ClassData> classes;
			if (this.fullSpectra) {
				classes = initialProjectData.getClasses();
			} else {
				classes = CoverageDataFileHandler.loadCoverageData(dataFile.toFile()).getClasses();
			}
			registeredClasses = new HashMap<>();
			for (ClassData classData : classes) {
				try {
					registeredClasses.put(Class.forName(classData.getName()), 0);
				} catch (ClassNotFoundException e1) {
					Log.err(this, "Class '%s' not found for registration.", classData.getName());
				}
			}
		}
	}

	private void validateDataFile(String value) {
		File dataFile = new File(value);
		if (!dataFile.exists()) {
			Log.abort(this, "Error: data file " + dataFile.getAbsolutePath()
			+ " does not exist");
		}
		if (!dataFile.isFile()) {
			Log.abort(this, "Error: data file " + dataFile.getAbsolutePath()
			+ " must be a regular file");
		}
	}

	private void validateAndCreateDestinationDirectory(String value) {
		File destinationDir = new File(value);
		if (destinationDir.exists() && !destinationDir.isDirectory()) {
			Log.abort(this, "Error: destination directory " + destinationDir
					+ " already exists but is not a directory");
		}
		destinationDir.mkdirs();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public ReportWrapper processItem(final TestWrapper testWrapper) {
		TestStatistics testStatistics = null;
		LockableProjectData lastProjectData = null;
		int iterationCounter = -1;
		boolean isEqual = false;
		boolean differentCoverage = false;
		
		LockableProjectData projectData = null;
		
		while (!isEqual) {
			++iterationCounter;

			//(try to) run the test and get the statistics
			TestStatistics tempTestStatistics = testRunner.submit(testWrapper).getResult();

			if (testStatistics == null) {
				testStatistics = tempTestStatistics;
			} else {
				testStatistics.mergeWith(tempTestStatistics);
			}

			//see if the test was executed and finished execution normally
			if (testStatistics.couldBeFinished()) {

				projectData = new LockableProjectData();

//				/*
//				 * Now sleep a bit in case there is a thread still holding a reference to the "old"
//				 * globalProjectData. We want it to finish its updates.
//				 * (Is 1000 ms really enough in this case?)
//				 */
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					//do nothing
//				}

				MyTouchCollector.applyTouchesOnProjectData2(registeredClasses, projectData);

				projectData.lock();

//				/*
//				 * Wait for some time for all writing to finish, here?
//				 */
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					//do nothing
//				}

//				Log.out(this, "Project data for: " + testWrapper + System.lineSeparator() + projectDataToString(projectData, false));

				if (!isEmpty(ProjectData.getGlobalProjectData())) {
					Log.err(this, testWrapper + ": Global project data was updated.");
					testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, testWrapper + ": Global project data was updated.");
				}

				if (lastProjectData != null) {
					isEqual = containsSameCoverage(projectData, lastProjectData);
					if (!isEqual) {
						differentCoverage = true;
					}
				}

				lastProjectData = projectData;

			} else {
				projectData = null;
				if (testStatistics.getErrorMsg() != null) {
					Log.err(this, testStatistics.getErrorMsg());
				}
				break;
			}
		}

		if (testStatistics.couldBeFinished()) {
			if (differentCoverage) {
				Log.warn(this, testWrapper + ": Repeated test execution generated different coverage.");
				testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, testWrapper + ": Repeated test execution generated different coverage.");
			}
			testStatistics.addStatisticsElement(StatisticsData.REPORT_ITERATIONS, iterationCounter);
			if (!testStatistics.wasSuccessful()) {
				testStatistics.addStatisticsElement(StatisticsData.FAILED_TEST_COVERAGE, 
						"Project data for failed test: " + testWrapper + System.lineSeparator() + projectDataToString(projectData, true));
			}
		}

		if (statisticsContainer != null) {
			statisticsContainer.addStatistics(testStatistics);
		}

		if (testStatistics.couldBeFinished()) {
			//generate the report

			ComplexityCalculator complexityCalculator = null;
//			= new ComplexityCalculator(reportArguments.getSources());
//			complexityCalculator.setEncoding(reportArguments.getEncoding());
//			complexityCalculator.setCalculateMethodComplexity(
//					reportArguments.isCalculateMethodComplexity());

			NativeReport report = new NativeReport(projectData, reportArguments
					.getDestinationDirectory(), reportArguments.getSources(),
					complexityCalculator, reportArguments.getEncoding());

			return new ReportWrapper(report, initialProjectData, testWrapper.toString(), testStatistics.wasSuccessful());
		}

		return null;
	}

	private boolean isEmpty(ProjectData projectData) {
		return projectData.getPackages().isEmpty();
	}

	private boolean containsSameCoverage(ProjectData projectData2, ProjectData lastProjectData) {
		//it should not be the same object
		if (projectData2 == lastProjectData) {
			return false;
		}
		// loop over all packages
		@SuppressWarnings("unchecked")
		SortedSet<PackageData> packages = projectData2.getPackages();
		Iterator<PackageData> itPackages = packages.iterator();
		@SuppressWarnings("unchecked")
		SortedSet<PackageData> packagesLast = lastProjectData.getPackages();
		Iterator<PackageData> itPackagesLast = packagesLast.iterator();
		if (packages.size() != packagesLast.size()) {
			Log.err(this, "Unequal amount of stored packages.");
			return false;
		}
		while (itPackages.hasNext()) {
			PackageData packageData = itPackages.next();
			PackageData packageDataLast = itPackagesLast.next();

			if (!packageData.getName().equals(packageDataLast.getName())) {
				Log.err(this, "Package names don't match.");
				return false;
			}

			// loop over all classes of the package
			@SuppressWarnings("unchecked")
			Collection<SourceFileData> sourceFiles = packageData.getSourceFiles();
			Iterator<SourceFileData> itSourceFiles = sourceFiles.iterator();
			@SuppressWarnings("unchecked")
			Collection<SourceFileData> sourceFilesLast = packageDataLast.getSourceFiles();
			Iterator<SourceFileData> itSourceFilesLast = sourceFilesLast.iterator();
			if (sourceFiles.size() != sourceFilesLast.size()) {
				Log.err(this, "Unequal amount of stored source files for package '%s'.", packageData.getName());
				return false;
			}
			while (itSourceFiles.hasNext()) {
				SourceFileData fileData = itSourceFiles.next();
				SourceFileData fileDataLast = itSourceFilesLast.next();

				if (!fileData.getName().equals(fileDataLast.getName())) {
					Log.err(this, "Source file names don't match for package '%s'.", packageData.getName());
					return false;
				}
				@SuppressWarnings("unchecked")
				SortedSet<ClassData> classes = fileData.getClasses();
				Iterator<ClassData> itClasses = classes.iterator();
				@SuppressWarnings("unchecked")
				SortedSet<ClassData> classesLast = fileDataLast.getClasses();
				Iterator<ClassData> itClassesLast = classesLast.iterator();
				if (classes.size() != classesLast.size()) {
					Log.err(this, "Unequal amount of stored classes for file '%s'.", fileData.getName());
					return false;
				}
				while (itClasses.hasNext()) {
					ClassData classData = itClasses.next();
					ClassData classDataLast = itClassesLast.next();

					if (!classData.getName().equals(classDataLast.getName())) {
						Log.err(this, "Class names don't match for file '%s'.", fileData.getName());
						return false;
					}
					if (!classData.getSourceFileName().equals(classDataLast.getSourceFileName())) {
						Log.err(this, "Source file names don't match for file '%s'.", fileData.getName());
						return false;
					}

					// loop over all methods of the class
					SortedSet<String> sortedMethods = new TreeSet<>();
					sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
					Iterator<String> itMethods = sortedMethods.iterator();
					SortedSet<String> sortedMethodsLast = new TreeSet<>();
					sortedMethodsLast.addAll(classDataLast.getMethodNamesAndDescriptors());
					Iterator<String> itMethodsLast = sortedMethodsLast.iterator();
					if (sortedMethods.size() != sortedMethodsLast.size()) {
						Log.err(this, "Unequal amount of stored methods for class '%s'.", classData.getName());
						return false;
					}
					while (itMethods.hasNext()) {
						final String methodNameAndSig = itMethods.next();
						final String methodNameAndSigLast = itMethodsLast.next();
						if (!methodNameAndSig.equals(methodNameAndSigLast)) {
							Log.err(this, "Methods don't match for class '%s'.", classData.getName());
							return false;
						}

						// loop over all lines of the method
						SortedSet<CoverageData> sortedLines = new TreeSet<>();
						sortedLines.addAll(classData.getLines(methodNameAndSig));
						Iterator<CoverageData> itLines = sortedLines.iterator();
						SortedSet<CoverageData> sortedLinesLast = new TreeSet<>();
						sortedLinesLast.addAll(classDataLast.getLines(methodNameAndSigLast));
						Iterator<CoverageData> itLinesLast = sortedLinesLast.iterator();
						if (sortedLines.size() != sortedLinesLast.size()) {
							Log.err(this, "Unequal amount of stored lines for method '%s'.", methodNameAndSig);
							return false;
						}
						while (itLines.hasNext()) {
							LineWrapper lineData = new LineWrapper(itLines.next());
							LineWrapper lineDataLast = new LineWrapper(itLinesLast.next());

							if (lineData.getLineNumber() != lineDataLast.getLineNumber()) {
								Log.err(this, "Line numbers don't match for method '%s'.", methodNameAndSig);
								return false;
							}
							
							if (lineData.isCovered() != lineDataLast.isCovered()) {
								Log.err(this, "Coverage doesn't match for method '%s', line %d.", methodNameAndSig, lineData.getLineNumber());
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
	
//	private String projectDataToString(ProjectData projectData) {
//		StringBuilder builder = new StringBuilder();
//		
//		// loop over all packages
//		@SuppressWarnings("unchecked")
//		SortedSet<PackageData> packages = projectData.getPackages();
//		Iterator<PackageData> itPackages = packages.iterator();
//		while (itPackages.hasNext()) {
//			PackageData packageData = itPackages.next();
//			builder.append("" + packageData.getName() + System.lineSeparator());
//
//			// loop over all classes of the package
//			@SuppressWarnings("unchecked")
//			Collection<SourceFileData> sourceFiles = packageData.getSourceFiles();
//			Iterator<SourceFileData> itSourceFiles = sourceFiles.iterator();
//			while (itSourceFiles.hasNext()) {
//				SourceFileData fileData = itSourceFiles.next();
//				builder.append("  " + fileData.getName() + System.lineSeparator());
//				
//				@SuppressWarnings("unchecked")
//				SortedSet<ClassData> classes = fileData.getClasses();
//				Iterator<ClassData> itClasses = classes.iterator();
//				while (itClasses.hasNext()) {
//					ClassData classData = itClasses.next();
//					builder.append("    " + classData.getName() + System.lineSeparator());
//
//					// loop over all methods of the class
//					SortedSet<String> sortedMethods = new TreeSet<>();
//					sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
//					Iterator<String> itMethods = sortedMethods.iterator();
//					while (itMethods.hasNext()) {
//						final String methodNameAndSig = itMethods.next();
//						builder.append("      " + methodNameAndSig + System.lineSeparator());
//
//						// loop over all lines of the method
//						SortedSet<CoverageData> sortedLines = new TreeSet<>();
//						sortedLines.addAll(classData.getLines(methodNameAndSig));
//						Iterator<CoverageData> itLines = sortedLines.iterator();
//						builder.append("       ");
//						while (itLines.hasNext()) {
//							LineData lineData = (LineData) itLines.next();
//							builder.append(" " + lineData.getLineNumber() + "(" + lineData.getHits() + ")");
//						}
//						builder.append(System.lineSeparator());
//					}
//				}
//			}
//		}
//		return builder.toString();
//	}
	
	private String projectDataToString(ProjectData projectData, boolean onlyUseCovered) {
		StringBuilder builder = new StringBuilder();
		
		// loop over all packages
		@SuppressWarnings("unchecked")
		SortedSet<PackageData> packages = projectData.getPackages();
		Iterator<PackageData> itPackages = packages.iterator();
		while (itPackages.hasNext()) {
			boolean packageWasCovered = false;
			PackageData packageData = itPackages.next();
			String nextPackage = packageData.getName() + System.lineSeparator();

			// loop over all classes of the package
			@SuppressWarnings("unchecked")
			Collection<SourceFileData> sourceFiles = packageData.getSourceFiles();
			Iterator<SourceFileData> itSourceFiles = sourceFiles.iterator();
			while (itSourceFiles.hasNext()) {
				boolean fileWasCovered = false;
				SourceFileData fileData = itSourceFiles.next();
				String nextFile = "  " + fileData.getName() + System.lineSeparator();
				
				@SuppressWarnings("unchecked")
				SortedSet<ClassData> classes = fileData.getClasses();
				Iterator<ClassData> itClasses = classes.iterator();
				while (itClasses.hasNext()) {
					boolean classWasCovered = false;
					ClassData classData = itClasses.next();
					String nextClass = "    " + classData.getName() + System.lineSeparator();

					// loop over all methods of the class
					SortedSet<String> sortedMethods = new TreeSet<>();
					sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
					Iterator<String> itMethods = sortedMethods.iterator();
					while (itMethods.hasNext()) {
						boolean methodWasCovered = false;
						final String methodNameAndSig = itMethods.next();
						String nextMethod = "      " + methodNameAndSig + System.lineSeparator();

						// loop over all lines of the method
						SortedSet<CoverageData> sortedLines = new TreeSet<>();
						sortedLines.addAll(classData.getLines(methodNameAndSig));
						Iterator<CoverageData> itLines = sortedLines.iterator();
						nextMethod += "       ";
						while (itLines.hasNext()) {
							MyLineData lineData = (MyLineData) itLines.next();
							if (!onlyUseCovered || lineData.isCovered()) {
								methodWasCovered = true;
								nextMethod += " " + lineData.getLineNumber() + "(" + lineData.getHits() + ")";
							}
						}
						nextMethod += System.lineSeparator();
						if (methodWasCovered) {
							classWasCovered = true;
							nextClass += nextMethod;
						}
					}
					if (classWasCovered) {
						fileWasCovered = true;
						nextFile += nextClass;
					}
				}
				if (fileWasCovered) {
					packageWasCovered = true;
					nextPackage += nextFile;
				}
			}
			if (packageWasCovered) {
				builder.append(nextPackage);
			}
		}
		return builder.toString();
	}

	@Override
	public boolean finalShutdown() {
		testRunner.finalShutdown();
		return super.finalShutdown();
	}

	@Override
	public AbstractModule<TestWrapper, ReportWrapper> enableTracking() {
		super.enableTracking();
		delegateTrackingTo(testRunner);
		return this;
	}

	@Override
	public AbstractModule<TestWrapper, ReportWrapper> enableTracking(int stepWidth) {
		super.enableTracking(stepWidth);
		delegateTrackingTo(testRunner);
		return this;
	}

	@Override
	public AbstractModule<TestWrapper, ReportWrapper> enableTracking(TrackingStrategy tracker) {
		super.enableTracking(tracker);
		delegateTrackingTo(testRunner);
		return this;
	}

	@Override
	public AbstractModule<TestWrapper, ReportWrapper> enableTracking(boolean useProgressBar) {
		super.enableTracking(useProgressBar);
		delegateTrackingTo(testRunner);
		return this;
	}

	@Override
	public AbstractModule<TestWrapper, ReportWrapper> enableTracking(boolean useProgressBar, int stepWidth) {
		super.enableTracking(useProgressBar, stepWidth);
		delegateTrackingTo(testRunner);
		return this;
	}

}
