/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.modules;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

/**
 * 
 * 
 * @author Simon Heiden
 */
public abstract class AbstractTestRunAndReportModule<T extends Serializable, R> extends AbstractProcessor<TestWrapper, R> {

	final private StatisticsCollector<StatisticsData> statisticsContainer;
	
	final private static int UNDEFINED_COVERAGE = 0;
	final private static int UNFINISHED_EXECUTION = 1;
	final private static int WRONG_COVERAGE = 2;
	final private static int CORRECT_EXECUTION = 3;

	private int currentState = UNDEFINED_COVERAGE;
	
	final private boolean alwaysUseSeparateJVM;
	private boolean alwaysUseJava7;
	
	private int testCounter = 0;
	
	final private Set<String> knownFailingtests;
	private int failedTestCounter = 0;
	private boolean testErrorOccurred = false;
	private int testErrorCounter = 0;
	
	private AbstractTestRunLocallyModule<T> testRunLocallyModule;
	private AbstractTestRunInNewJVMModule<T> testRunInNewJVMModule;
	private AbstractTestRunInNewJVMModuleWithJava7Runner<T> testRunInNewJVMModuleWithJava7Runner;

	private int maxErrors;

	public AbstractTestRunAndReportModule(final String testOutput, 
			final boolean debugOutput, Long timeout, final int repeatCount,
			boolean useSeparateJVMalways, boolean alwaysUseJava7, int maxErrors, String[] failingtests,
			final StatisticsCollector<StatisticsData> statisticsContainer, ClassLoader cl) {
		super();
		if (failingtests == null) {
			knownFailingtests = null;
		} else {
			knownFailingtests = new HashSet<>();
			addKnownFailingTests(failingtests);
		}
		this.maxErrors = maxErrors;
		
		this.statisticsContainer = statisticsContainer;

		this.alwaysUseSeparateJVM = useSeparateJVMalways;
		
		this.alwaysUseJava7 = alwaysUseJava7;
		
	}
	
	private AbstractTestRunLocallyModule<T> getTestRunLocallyModule() {
		if (testRunLocallyModule == null) {
			testRunLocallyModule = newTestRunLocallyModule();
		}
		return testRunLocallyModule;
	}
	
	private AbstractTestRunInNewJVMModule<T> getTestRunInNewJVMModule() {
		if (testRunInNewJVMModule == null) {
			testRunInNewJVMModule = newTestRunInNewJVMModule();
		}
		return testRunInNewJVMModule;
	}
	
	private AbstractTestRunInNewJVMModuleWithJava7Runner<T> getTestRunInNewJVMModuleWithJava7Runner() {
		if (testRunInNewJVMModuleWithJava7Runner == null) {
			testRunInNewJVMModuleWithJava7Runner = newTestRunInNewJVMModuleWithJava7Runner();
		}
		return testRunInNewJVMModuleWithJava7Runner;
	}
	
	public abstract AbstractTestRunInNewJVMModule<T> newTestRunInNewJVMModule();
	
	public abstract AbstractTestRunLocallyModule<T> newTestRunLocallyModule();
	
	public abstract AbstractTestRunInNewJVMModuleWithJava7Runner<T> newTestRunInNewJVMModuleWithJava7Runner();	

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

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public R processItem(final TestWrapper testWrapper, ProcessorSocket<TestWrapper, R> socket) {
		socket.allowOnlyForcedTracks();
		socket.forceTrack(testWrapper.toString());
		++testCounter;
//		Log.out(this, "Now processing: '%s'.", testWrapper);

		if (testErrorCounter <= maxErrors) {
			TestStatistics testStatistics = new TestStatistics();

			currentState = UNDEFINED_COVERAGE;

			T projectData;
			if (alwaysUseJava7) {
				projectData = runTestInJVMWithJava7(testWrapper, testStatistics, false);
			} else if (alwaysUseSeparateJVM) {
				projectData = runTestInJVM(testWrapper, testStatistics, false);
			} else {
				projectData = runTestLocally(testWrapper, testStatistics);
			}

			// check for successful test execution
			boolean errorOccurred = testErrorOccurred(testWrapper, testStatistics, true) || !isCorrectData(projectData);
			testErrorOccurred |= errorOccurred;

			if (errorOccurred) {
				++testErrorCounter;
			}

			boolean testResultError = testResultErrorOccurred(testWrapper, testStatistics, true);

			if (testStatistics.getErrorMsg() != null) {
				Log.err(this, testStatistics.getErrorMsg());
			}

			if (statisticsContainer != null) {
				statisticsContainer.addStatistics(testStatistics);
			}

			//don't produce reports for wrong test data or tests with unexpected outcome
			if (testResultError || !isCorrectData(projectData)) {
				return null;
			} else {
				return generateReport(testWrapper, testStatistics, projectData);
			}
		} else {
			// skip execution if too many errors occured
			if (statisticsContainer != null) {
				TestStatistics testStatistics = new TestStatistics();
				testStatistics.addStatisticsElement(StatisticsData.SKIPPED, 1);
				statisticsContainer.addStatistics(testStatistics);
			}
			return null;
		}
	}

	public abstract R generateReport(TestWrapper testWrapper, TestStatistics testStatistics, T data);

	private boolean testResultErrorOccurred(final TestWrapper testWrapper, TestStatistics testStatistics, boolean log) {
		// check for "correct" (intended) test execution result
		String testName = testWrapper.toString();
		if (testStatistics.couldBeFinished()) {
			if (!testStatistics.coverageGenerationFailed()) {
				if (knownFailingtests != null) {
					if (testStatistics.wasSuccessful()) {
						if (knownFailingtests.contains(testName)) {
							if (log) {
								testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
										"Test '" + testName + "' was successful but should fail.");
								testStatistics.addStatisticsElement(StatisticsData.WRONG_TEST_RESULT, 1);
							}
							return true;
						}
					} else {
						if (knownFailingtests.contains(testName)) {
							++failedTestCounter;
						} else {
							if (log) {
								testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
										"Test '" + testName + "' failed but should be successful.");
								testStatistics.addStatisticsElement(StatisticsData.WRONG_TEST_RESULT, 1);
							}
							return true;
						}
					}
				}
			}
		}

		return false;
	}
	
	private boolean testErrorOccurred(final TestWrapper testWrapper, TestStatistics testStatistics, boolean log) {
		// check for "correct" (intended) test execution
		String testName = testWrapper.toString();
		if (testStatistics.couldBeFinished()) {
			if (testStatistics.coverageGenerationFailed()) {
				if (log) {
					testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
							"Test '" + testName + "' coverage generation failed.");
				}
				return true;
			}
		} else {
			if (testStatistics.timeoutOccurred()) {
				if (log) {
					testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
							"Test '" + testName + "' had a timeout.");
				}
			}
			if (testStatistics.exceptionOccured()) {
				if (log) {
					testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
							"Test '" + testName + "' had an exception.");
				}
			}
			if (testStatistics.coverageGenerationFailed()) {
				if (log) {
					testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
							"Test '" + testName + "' coverage generation failed.");
				}
			}
			if (testStatistics.wasInterrupted()) {
				if (log) {
					testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
							"Test '" + testName + "' had an exception.");
				}
			}
			return true;
		}

		return false;
	}

	private boolean isCorrectData(T projectData) {
		return projectData != null && 
				currentState == CORRECT_EXECUTION;
	}
	
	private T runTestLocally(final TestWrapper testWrapper, 
			final TestStatistics testStatistics) {
		T projectData = null;
		currentState = UNDEFINED_COVERAGE;
		
		projectData = runTestWithRunner(testWrapper, testStatistics, getTestRunLocallyModule());
		
		if(!isCorrectData(projectData) || testResultErrorOccurred(testWrapper, testStatistics, false)) {
			projectData = runTestInJVM(testWrapper, testStatistics, true);
		}
		
		return projectData;
	}
	
	private T runTestInJVM(final TestWrapper testWrapper, 
			final TestStatistics testStatistics, boolean error) {
		T projectData = null;
		currentState = UNDEFINED_COVERAGE;

		if (error) {
			Log.out(this, "Running test in separate JVM due to error: %s", testWrapper);
		}
		projectData = runTestWithRunner(testWrapper, testStatistics, getTestRunInNewJVMModule());
		testStatistics.addStatisticsElement(StatisticsData.SEPARATE_JVM, 1);

		if(!isCorrectData(projectData) || testResultErrorOccurred(testWrapper, testStatistics, false)) {
			projectData = runTestInJVMWithJava7(testWrapper, testStatistics, true);
		}

		return projectData;
	}
	
	private T runTestInJVMWithJava7(final TestWrapper testWrapper, 
			final TestStatistics testStatistics, boolean error) {
		T projectData = null;
		currentState = UNDEFINED_COVERAGE;

		if (error) {
			Log.out(this, "Running test in separate JVM with Java 7 due to error: %s", testWrapper);
		}
		projectData = runTestWithRunner(testWrapper, testStatistics, getTestRunInNewJVMModuleWithJava7Runner());
		testStatistics.addStatisticsElement(StatisticsData.SEPARATE_JVM, 1);

		return projectData;
	}
	
	private T runTestWithRunner(TestWrapper testWrapper, TestStatistics testStatistics, AbstractProcessor<TestWrapper, Pair<TestStatistics, T>> testrunner) {
		T projectData = null;
//		FileUtils.delete(dataFile);
		//(try to) run the testS
		Pair<TestStatistics, T> testResult = testrunner.submit(testWrapper).getResult();
		testStatistics.mergeWith(testResult.first());
		
		//see if the test was executed and finished execution normally
		if (testResult.first().couldBeFinished()) {
			if (testResult.first().coverageGenerationFailed() 
					|| testResult.second() == null) {
				currentState = WRONG_COVERAGE;
			} else {
				projectData = testResult.second();
				currentState = CORRECT_EXECUTION;
			}
		} else {
			currentState = UNFINISHED_EXECUTION;
		}
		return projectData;
	}

	public abstract R getErrorReport();
	
	@Override
	public R getResultFromCollectedItems() {
		// in the end, check if number of failing tests is correct (if given)
		if (testErrorCounter > maxErrors) {
			Log.err(this, "Some tests were not successfully executed! (> %d)", maxErrors);
			return getErrorReport();
		}
		if (knownFailingtests != null) {
			if (knownFailingtests.size() > failedTestCounter) {
				Log.err(this, "Not all specified failing tests have been executed! Expected: %d, Actual: %d", 
						knownFailingtests.size(), failedTestCounter);
				return getErrorReport();
			}
		}
		return super.getResultFromCollectedItems();
	}

	@Override
	public boolean finalShutdown() {
		if (testRunLocallyModule != null) {
			testRunLocallyModule.finalShutdown();
		}
		if (testRunInNewJVMModule != null) {
			testRunInNewJVMModule.finalShutdown();
		}
		if (testRunInNewJVMModuleWithJava7Runner != null) {
			testRunInNewJVMModuleWithJava7Runner.finalShutdown();
		}
		return super.finalShutdown();
	}

}
