/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.cobertura.modules;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.NativeReport;
import se.de.hu_berlin.informatik.sbfl.StatisticsData;
import se.de.hu_berlin.informatik.sbfl.TestStatistics;
import se.de.hu_berlin.informatik.sbfl.TestWrapper;
import se.de.hu_berlin.informatik.sbfl.spectra.modules.TestRunModule;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.CoberturaReportWrapper;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.coverage.LockableProjectData;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.coverage.MyTouchCollector;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class CoberturaTestRunAndReportModule extends AbstractProcessor<TestWrapper, CoberturaReportWrapper> {

	final private String testOutput;
	final private Arguments reportArguments;
	final private Long timeout;

	final private StatisticsCollector<StatisticsData> statisticsContainer;
	
	final private static LockableProjectData UNDEFINED_COVERAGE_DUMMY = new LockableProjectData();
	final private static LockableProjectData UNFINISHED_EXECUTION_DUMMY = new LockableProjectData();
	final private static LockableProjectData WRONG_COVERAGE_DUMMY = new LockableProjectData();
	
	final private File dataFile;
	private ProjectData initialProjectData;

	final private boolean fullSpectra;

	final private TestRunModule testRunner;
	final private CoberturaTestRunInNewJVMModule testRunnerNewJVM;
	
	private Map<Class<?>, Integer> registeredClasses;
	final private boolean alwaysUseSeparateJVM;
	
	private int testCounter = 0;
	
	final private Set<String> knownFailingtests;
	private int failedTestCounter = 0;
	private boolean failingTestErrorOccurred = false;

	@SuppressWarnings("unchecked")
	public CoberturaTestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir, 
			final boolean fullSpectra, final boolean debugOutput, Long timeout, final int repeatCount,
			String instrumentedClassPath, final String javaHome, boolean useSeparateJVMalways, String[] failingtests,
			final StatisticsCollector<StatisticsData> statisticsContainer, ClassLoader cl) {
		super();
		if (failingtests == null) {
			knownFailingtests = null;
		} else {
			knownFailingtests = new HashSet<>();
			addKnownFailingTests(failingtests);
		}
		
		UNDEFINED_COVERAGE_DUMMY.lock();
		UNFINISHED_EXECUTION_DUMMY.lock();
		WRONG_COVERAGE_DUMMY.lock();
		
		this.statisticsContainer = statisticsContainer;
		this.testOutput = testOutput;

		this.dataFile = dataFile.toFile();
		String baseDir = null;
		validateDataFile(this.dataFile.toString());
		validateAndCreateDestinationDirectory(this.testOutput);

		ArgumentsBuilder builder = new ArgumentsBuilder();
		builder.setDataFile(this.dataFile.toString());
		builder.setDestinationDirectory(this.testOutput);
		builder.addSources(srcDir, baseDir == null);

		reportArguments = builder.build();

		this.fullSpectra = fullSpectra;

		//in the original data file, all (executable) lines are contained, even though they are not executed at all;
		//so if we want to have the full spectra, we have to make a backup and load it again for each run test
		if (this.fullSpectra) {
			initialProjectData = CoverageDataFileHandler.loadCoverageData(this.dataFile);
		} else {
			initialProjectData = new ProjectData();
		}

		this.timeout = timeout;
		
		this.alwaysUseSeparateJVM = instrumentedClassPath != null && useSeparateJVMalways;

		if (this.alwaysUseSeparateJVM) {
			this.testRunner = null;
		} else {
			this.testRunner = new TestRunModule(this.testOutput, debugOutput, this.timeout, repeatCount, cl);
		}
		
		this.testRunnerNewJVM = new CoberturaTestRunInNewJVMModule(this.testOutput, debugOutput, this.timeout, repeatCount, 
				instrumentedClassPath, this.dataFile.toPath(), javaHome);
		
		//initialize/reset the project data
		ProjectData.saveGlobalProjectData();
		//turn off auto saving (removes the shutdown hook inside of Cobertura)
		ProjectData.turnOffAutoSave();

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

	private void addKnownFailingTests(String[] failingtests) {
		for (String failingTest : failingtests) {
			// format: qualified.class.name::TestMethodName
			String[] split = failingTest.split("::");
			if (split.length == 2) {
				knownFailingtests.add(failingTest);
			} else {
				Log.err(this, "Given failing test has wrong format: '%s'", failingTest);
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
	@Override
	public CoberturaReportWrapper processItem(final TestWrapper testWrapper, ProcessorSocket<TestWrapper, CoberturaReportWrapper> socket) {
		socket.allowOnlyForcedTracks();
		socket.forceTrack(testWrapper.toString());
		++testCounter;
//		Log.out(this, "Now processing: '%s'.", testWrapper);
		
		TestStatistics testStatistics = new TestStatistics();

		ProjectData projectData = UNDEFINED_COVERAGE_DUMMY;
		
		if (alwaysUseSeparateJVM) {
			projectData = runTestInNewJVM(testWrapper, testStatistics);
		} else {
			projectData = runTestLocally(testWrapper, testStatistics);
		}
		
		// check for "correct" (intended) test execution
		if (knownFailingtests != null) {
			if (testStatistics.couldBeFinished()) {
				String testName = testWrapper.toString();
				if (testStatistics.wasSuccessful()) {
					if (knownFailingtests.contains(testName)) {
						testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
								"Test '" + testName + "' was successful but should fail.");
						failingTestErrorOccurred = true;
					}
				} else {
					if (knownFailingtests.contains(testName)) {
						++failedTestCounter;
					} else {
						testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
								"Test '" + testName + "' failed but should be successful.");
						failingTestErrorOccurred = true;
					}
				}
			}
		}

//		if (isNormalData(projectData) || projectData == WRONG_COVERAGE_DUMMY) {
//			if (!testStatistics.wasSuccessful()) {
//				testStatistics.addStatisticsElement(StatisticsData.FAILED_TEST_COVERAGE, 
//						"Project data for failed test: " + testWrapper + System.lineSeparator() 
//						+ LockableProjectData.projectDataToString(projectData, true));
//			}
//		}

		if (testStatistics.getErrorMsg() != null) {
			Log.err(this, testStatistics.getErrorMsg());
		}
		
		if (statisticsContainer != null) {
			statisticsContainer.addStatistics(testStatistics);
		}

		if (isNormalData(projectData)) {
			return generateReport(testWrapper, testStatistics, projectData);
		} else {
			return null;
		}
	}
	
	private static boolean isNormalData(ProjectData projectData) {
		return projectData != null && 
				projectData != WRONG_COVERAGE_DUMMY && 
				projectData != UNDEFINED_COVERAGE_DUMMY && 
				projectData != UNFINISHED_EXECUTION_DUMMY;
	}
	
	private ProjectData runTestLocally(final TestWrapper testWrapper, 
			final TestStatistics testStatistics) {
		//sadly, we have to check if the coverage data has properly been reset...
		boolean isResetted = false;
		int maxTryCount = 3;
		int tryCount = 0;
		LockableProjectData projectData2 = UNDEFINED_COVERAGE_DUMMY;
		while (!isResetted && tryCount < maxTryCount) {
			++tryCount;
			projectData2 = new LockableProjectData();
			MyTouchCollector.resetTouchesOnProjectData2(registeredClasses, projectData2);
//			LockableProjectData.resetLines(projectData2);
			if (!LockableProjectData.containsCoveredLines(projectData2)) {
				isResetted = true;
			}
		}
		
		LockableProjectData projectData = UNDEFINED_COVERAGE_DUMMY;

		//(try to) run the test and get the statistics
		TestStatistics testResult = testRunner.submit(testWrapper).getResult();
		testStatistics.mergeWith(testResult);

		//see if the test was executed and finished execution normally
		if (testResult.couldBeFinished()) {
			// wait for some milliseconds
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// do nothing
			}
			projectData = new LockableProjectData();

			MyTouchCollector.applyTouchesOnProjectData2(registeredClasses, projectData);

			((LockableProjectData)projectData).lock();

//			Log.out(this, "Project data for: " + testWrapper + System.lineSeparator() + projectDataToString(projectData, false));
			
			if (!isResetted) {
				testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
						"Coverage data not empty before running test " + testCounter + ".");
//				if (!projectData.subtract(projectData2)) {
//					testStatistics.addStatisticsElement(StatisticsData.WRONG_COVERAGE, 1);
//					testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
//							testWrapper + ": Wrong coverage data on test no. " + testCounter + ".");
//				}
			}

		} else {
			projectData = null;
		}
		
		return projectData;
	}
	
	private ProjectData runTestInNewJVM(TestWrapper testWrapper, TestStatistics testStatistics) {
		ProjectData projectData;
		FileUtils.delete(dataFile);
		//(try to) run the test in new JVM and get the statistics
		TestStatistics testResult = testRunnerNewJVM.submit(testWrapper).getResult();
		testStatistics.mergeWith(testResult);
		testStatistics.addStatisticsElement(StatisticsData.SEPARATE_JVM, 1);
		
		//see if the test was executed and finished execution normally
		if (testResult.couldBeFinished()) {
			// wait for some milliseconds
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// do nothing
			}
			if (dataFile.exists()) {
				projectData = CoverageDataFileHandler.loadCoverageData(dataFile);
			} else {
				projectData = UNDEFINED_COVERAGE_DUMMY;
				Log.err(this, testWrapper + ": Data file does not exist after running test no. " + testCounter + ".");
				testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
						testWrapper + ": Data file does not exist after running test no. " + testCounter + ".");
			}
		} else {
			projectData = UNFINISHED_EXECUTION_DUMMY;
		}
		return projectData;
	}
	
	private CoberturaReportWrapper generateReport(final TestWrapper testWrapper, 
			TestStatistics testStatistics, ProjectData projectData) {
		//generate the report
		ComplexityCalculator complexityCalculator = null;
//			= new ComplexityCalculator(reportArguments.getSources());
//			complexityCalculator.setEncoding(reportArguments.getEncoding());
//			complexityCalculator.setCalculateMethodComplexity(
//					reportArguments.isCalculateMethodComplexity());

		NativeReport report = new NativeReport(projectData, reportArguments
				.getDestinationDirectory(), reportArguments.getSources(),
				complexityCalculator, reportArguments.getEncoding());

		return new CoberturaReportWrapper(report, initialProjectData, 
				testWrapper.toString(), testStatistics.wasSuccessful());
	}

	
	@Override
	public CoberturaReportWrapper getResultFromCollectedItems() {
		// in the end, check if number of failing tests is correct (if given)
		if (knownFailingtests != null) {
			if (failingTestErrorOccurred) {
				Log.err(this, "Some test execution had the wrong result!");
				System.exit(1);
			}
			if (knownFailingtests.size() > failedTestCounter) {
				Log.err(this, "Not all specified failing tests have been executed! Expected: %d, Actual: %d", 
						knownFailingtests.size(), failedTestCounter);
				System.exit(1);
			}
		}
		return super.getResultFromCollectedItems();
	}

	@Override
	public boolean finalShutdown() {
		if (testRunner != null) {
			testRunner.finalShutdown();
		}
		return super.finalShutdown();
	}

}
