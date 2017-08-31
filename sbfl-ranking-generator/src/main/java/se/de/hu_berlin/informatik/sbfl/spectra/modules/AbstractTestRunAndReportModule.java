/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.modules;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import se.de.hu_berlin.informatik.junittestutils.testlister.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.testlister.data.TestStatistics;
import se.de.hu_berlin.informatik.junittestutils.testlister.data.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.testlister.running.ExtendedTestRunModule;
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
	
	final private ExtendedTestRunModule testRunner;

	final private boolean alwaysUseSeparateJVM;
	
	private int testCounter = 0;
	
	final private Set<String> knownFailingtests;
	private int failedTestCounter = 0;
	private boolean failingTestErrorOccurred = false;

	public AbstractTestRunAndReportModule(final String testOutput, 
			final boolean debugOutput, Long timeout, final int repeatCount,
			boolean useSeparateJVMalways, String[] failingtests,
			final StatisticsCollector<StatisticsData> statisticsContainer, ClassLoader cl) {
		super();
		if (failingtests == null) {
			knownFailingtests = null;
		} else {
			knownFailingtests = new HashSet<>();
			addKnownFailingTests(failingtests);
		}
		
		this.statisticsContainer = statisticsContainer;

		this.alwaysUseSeparateJVM = useSeparateJVMalways;

		if (this.alwaysUseSeparateJVM) {
			this.testRunner = null;
		} else {
			this.testRunner = new ExtendedTestRunModule(testOutput, debugOutput, timeout, repeatCount, cl);
		}
		
	}
	
	public abstract AbstractTestRunInNewJVMModule<T> getTestRunInNewJVMModule();

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
		
		TestStatistics testStatistics = new TestStatistics();

		currentState = UNDEFINED_COVERAGE;
		
		T projectData;
		if (alwaysUseSeparateJVM) {
			projectData = runTestInNewJVM(testWrapper, testStatistics);
		} else {
			projectData = runTestLocallyOrElseInJVM(testWrapper, testStatistics);
		}
		
		// check for "correct" (intended) test execution
		failingTestErrorOccurred |= testResultErrorOccurred(testWrapper, testStatistics, true);

		if (testStatistics.getErrorMsg() != null) {
			Log.err(this, testStatistics.getErrorMsg());
		}
		
		if (statisticsContainer != null) {
			statisticsContainer.addStatistics(testStatistics);
		}

		if (isCorrectData(projectData)) {
			return generateReport(testWrapper, testStatistics, projectData);
		} else {
			return null;
		}
	}

	public abstract R generateReport(TestWrapper testWrapper, TestStatistics testStatistics, T data);

	private boolean testResultErrorOccurred(final TestWrapper testWrapper, TestStatistics testStatistics, boolean log) {
		// check for "correct" (intended) test execution
		if (knownFailingtests != null) {
			String testName = testWrapper.toString();
			if (testStatistics.couldBeFinished()) {
				if (testStatistics.hasWrongCoverage()) {
					if (log) {
						testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
								"Test '" + testName + "' had wrong coverage.");
					}
					return true;
				} else {
					if (testStatistics.wasSuccessful()) {
						if (knownFailingtests.contains(testName)) {
							if (log) {
								testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
										"Test '" + testName + "' was successful but should fail.");
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
							}
							return true;
						}
					}
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
				if (testStatistics.hasWrongCoverage()) {
					if (log) {
						testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
								"Test '" + testName + "' had wrong coverage.");
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
		}
		return false;
	}

	private boolean isCorrectData(T projectData) {
		return projectData != null && 
				currentState != WRONG_COVERAGE && 
				currentState != UNDEFINED_COVERAGE && 
				currentState != UNFINISHED_EXECUTION;
	}
	
	private T runTestLocallyOrElseInJVM(final TestWrapper testWrapper, 
			final TestStatistics testStatistics) {
		
		T projectData = runTestLocally(testWrapper, testStatistics);
		
		if(!isCorrectData(projectData) || testResultErrorOccurred(testWrapper, testStatistics, false)) {
			currentState = UNDEFINED_COVERAGE;
			
			Log.out(this, "Running test in separate JVM due to error: %s", testWrapper);
			projectData = runTestInNewJVM(testWrapper, testStatistics);
		}
		
		return projectData;
	}
	
	public abstract boolean prepareBeforeRunningTest();
	
	public abstract T getCoverageDataAftertest();
	
	private T runTestLocally(final TestWrapper testWrapper, 
			final TestStatistics testStatistics) {
		boolean preparationSucceeded = prepareBeforeRunningTest();
		
		if (!preparationSucceeded) {
			currentState = WRONG_COVERAGE;
			TestStatistics testResult = new TestStatistics();
			testResult.addStatisticsElement(StatisticsData.WRONG_COVERAGE, 1);
			testStatistics.mergeWith(testResult);
			testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
					"Coverage data not empty before running test " + testCounter + ".");
			return null;
		}
		
		T projectData = null;

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
			
			if (testResult.hasWrongCoverage()) {
				currentState = WRONG_COVERAGE;
			} else {
				projectData = getCoverageDataAftertest();
				if (projectData == null) {
					currentState = WRONG_COVERAGE;
					testResult.addStatisticsElement(StatisticsData.WRONG_COVERAGE, 1);
					Log.err(this, testWrapper + ": Could not get coverage data after running test no. " + testCounter + ".");
					testStatistics.addStatisticsElement(
							StatisticsData.ERROR_MSG,
							testWrapper + ": Could not get coverage data after running test no. " + testCounter + ".");
				} else {
					currentState = CORRECT_EXECUTION;
				}
			}
//			Log.out(this, "Project data for: " + testWrapper + System.lineSeparator() + projectDataToString(projectData, false));
		} else {
			currentState = UNFINISHED_EXECUTION;
		}
		
		return projectData;
	}
	
	private T runTestInNewJVM(TestWrapper testWrapper, TestStatistics testStatistics) {
		T projectData = null;
//		FileUtils.delete(dataFile);
		//(try to) run the test in new JVM and get the statistics
		Pair<TestStatistics, T> testResult = getTestRunInNewJVMModule().submit(testWrapper).getResult();
		testStatistics.mergeWith(testResult.first());
		testStatistics.addStatisticsElement(StatisticsData.SEPARATE_JVM, 1);
		
		//see if the test was executed and finished execution normally
		if (testResult.first().couldBeFinished()) {
			if (testResult.first().hasWrongCoverage()) {
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
		if (knownFailingtests != null) {
			if (failingTestErrorOccurred) {
				Log.err(this, "Some test execution had the wrong result!");
				return getErrorReport();
			}
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
		if (testRunner != null) {
			testRunner.finalShutdown();
		}
		if (getTestRunInNewJVMModule() != null) {
			getTestRunInNewJVMModule().finalShutdown();
		}
		return super.finalShutdown();
	}

}
