/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.NativeReport;
import se.de.hu_berlin.informatik.c2r.StatisticsData;
import se.de.hu_berlin.informatik.c2r.TestStatistics;
import se.de.hu_berlin.informatik.c2r.TestWrapper;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.LockableProjectData;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.MyTouchCollector;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.ReportWrapper;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class TestRunAndReportModule extends AbstractModule<TestWrapper, ReportWrapper> {

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
	final private TestRunInNewJVMModule testRunnerNewJVM;
	
	private Map<Class<?>, Integer> registeredClasses;
	final private boolean alwaysUseSeparateJVM;
	
	private int testCounter = 0;

	public TestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir,
			String instrumentedClassPath, final String javaHome, boolean useSeparateJVMalways) {
		this(dataFile, testOutput, srcDir, false, false, null, 1, instrumentedClassPath, javaHome, useSeparateJVMalways);
	}

	public TestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir, 
			final boolean fullSpectra, final boolean debugOutput, Long timeout, final int repeatCount,
			String instrumentedClassPath, final String javaHome, boolean useSeparateJVMalways) {
		this(dataFile, testOutput, srcDir, fullSpectra, debugOutput, timeout, repeatCount, 
				instrumentedClassPath, javaHome, useSeparateJVMalways, null);
	}

	@SuppressWarnings("unchecked")
	public TestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir, 
			final boolean fullSpectra, final boolean debugOutput, Long timeout, final int repeatCount,
			String instrumentedClassPath, final String javaHome, boolean useSeparateJVMalways,
			final StatisticsCollector<StatisticsData> statisticsContainer) {
		super(true);
		UNDEFINED_COVERAGE_DUMMY.lock();
		UNFINISHED_EXECUTION_DUMMY.lock();
		WRONG_COVERAGE_DUMMY.lock();
		allowOnlyForcedTracks();
		
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
			this.testRunner = new TestRunModule(this.testOutput, debugOutput, this.timeout, repeatCount);
		}
		
		this.testRunnerNewJVM = new TestRunInNewJVMModule(this.testOutput, debugOutput, this.timeout, repeatCount, 
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
		forceTrack(testWrapper.toString());
		++testCounter;
//		Log.out(this, "Now processing: '%s'.", testWrapper);
		
		TestStatistics testStatistics = new TestStatistics();
		ProjectData lastProjectData = null;
		
		ProjectData projectData = UNDEFINED_COVERAGE_DUMMY;
		
		if (alwaysUseSeparateJVM) {
			projectData = runTestInNewJVM(testWrapper, testStatistics);
//			Log.out(this, testStatistics.toString());
		} else {
			//technically, this will, for now, always be executed 2 times, since running a test
			//in a separate JVM can't produce "wrong" coverage (at least not the dummy object...)
			int iterationCount = 0;
			boolean isFirst = true;
			while (iterationCount < 5
					&& (projectData == UNDEFINED_COVERAGE_DUMMY || projectData == WRONG_COVERAGE_DUMMY)) {
				++iterationCount;
				projectData = runTestLocallyOrInJVM(testWrapper, testStatistics, lastProjectData);
				lastProjectData = projectData;
				if (isFirst && projectData != UNFINISHED_EXECUTION_DUMMY) {
					projectData = UNDEFINED_COVERAGE_DUMMY;
					isFirst = false;
				}
			}
		}

		if (projectData != UNFINISHED_EXECUTION_DUMMY && projectData != UNDEFINED_COVERAGE_DUMMY) {
//			testStatistics.addStatisticsElement(StatisticsData.REPORT_ITERATIONS, iterationCount);
			if (!testStatistics.wasSuccessful()) {
				testStatistics.addStatisticsElement(StatisticsData.FAILED_TEST_COVERAGE, 
						"Project data for failed test: " + testWrapper + System.lineSeparator() 
						+ LockableProjectData.projectDataToString(projectData, true));
			}
		}

		if (testStatistics.getErrorMsg() != null) {
			Log.err(this, testStatistics.getErrorMsg());
		}
		
		if (statisticsContainer != null) {
			statisticsContainer.addStatistics(testStatistics);
		}

		if (projectData == UNFINISHED_EXECUTION_DUMMY || projectData == UNDEFINED_COVERAGE_DUMMY) {
			return null;
		} else {
			return generateReport(testWrapper, testStatistics, projectData);
		}
	}
	
	private static boolean isNormalData(ProjectData projectData) {
		return projectData != null && 
				projectData != WRONG_COVERAGE_DUMMY && 
				projectData != UNDEFINED_COVERAGE_DUMMY && 
				projectData != UNFINISHED_EXECUTION_DUMMY;
	}
	
	private ProjectData runTestLocallyOrInJVM(final TestWrapper testWrapper, 
			TestStatistics testStatistics, ProjectData lastProjectData) {
		ProjectData projectData;
		if (lastProjectData == WRONG_COVERAGE_DUMMY) {
			projectData = runTestInNewJVM(testWrapper, testStatistics);
		} else {
			projectData = runTestLocally(testWrapper, testStatistics);
		}
		
		//see if the test was executed and finished execution normally
		if (isNormalData(lastProjectData) && isNormalData(projectData)) {
			boolean isEqual = LockableProjectData.containsSameCoverage(projectData, lastProjectData);
			if (!isEqual) {
				testStatistics.addStatisticsElement(StatisticsData.DIFFERENT_COVERAGE, 1);
				testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
						testWrapper + ": Repeated test execution generated different coverage.");
				projectData.merge(lastProjectData);
			}
		}
		
		return projectData;
	}
	
	private ProjectData runTestLocally(final TestWrapper testWrapper, 
			final TestStatistics testStatistics) {
		//sadly, we have to check if the coverage data has properly been reset...
		//TODO: try to find a way to properly reset the data so that no touches get collected
		//(or try to find the real cause, if there is another than we thought of...)
		//maybe, while applying the touches, some lines of the instrumented classes are executed?
		//update: lines are covered without running a test, sometimes?!...
		boolean isResetted = false;
		int maxTryCount = 2;
		int tryCount = 0;
		LockableProjectData projectData2 = UNDEFINED_COVERAGE_DUMMY;
		while (!isResetted && tryCount < maxTryCount) {
			++tryCount;
			projectData2 = new LockableProjectData();
			MyTouchCollector.applyTouchesOnProjectData2(registeredClasses, projectData2);
			if (!LockableProjectData.containsCoveredLines(projectData2)) {
				isResetted = true;
			}
		}
		
		LockableProjectData projectData = UNDEFINED_COVERAGE_DUMMY;

		//(try to) run the test and get the statistics
		testStatistics.mergeWith(testRunner.submit(testWrapper).getResult());

		//see if the test was executed and finished execution normally
		if (testStatistics.couldBeFinished()) {
			projectData = new LockableProjectData();

			MyTouchCollector.applyTouchesOnProjectData2(registeredClasses, projectData);

			((LockableProjectData)projectData).lock();

//			Log.out(this, "Project data for: " + testWrapper + System.lineSeparator() + projectDataToString(projectData, false));

			if (LockableProjectData.containsCoveredLines(ProjectData.getGlobalProjectData())) {
				testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
						testWrapper + ": Global project data was updated after running test no. " + testCounter + ".");
			}
			
			if (!isResetted) {
				if (testCounter == 1) {
					testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
							"Coverage data is not empty before running the first test. Will be subtracted from the 'real' data.");
				}
				if (!projectData.subtract(projectData2)) {
					testStatistics.addStatisticsElement(StatisticsData.WRONG_COVERAGE, 1);
					testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
							testWrapper + ": Wrong coverage data on test no. " + testCounter + ".");
				}
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
		testStatistics.mergeWith(testRunnerNewJVM.submit(testWrapper).getResult());
		testStatistics.addStatisticsElement(StatisticsData.SEPARATE_JVM, 1);
		
		//see if the test was executed and finished execution normally
		if (testStatistics.couldBeFinished()) {
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
	
	private ReportWrapper generateReport(final TestWrapper testWrapper, 
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

		return new ReportWrapper(report, initialProjectData, 
				testWrapper.toString(), testStatistics.wasSuccessful());
	}

	@Override
	public boolean finalShutdown() {
		if (testRunner != null) {
			testRunner.finalShutdown();
		}
		return super.finalShutdown();
	}

}
