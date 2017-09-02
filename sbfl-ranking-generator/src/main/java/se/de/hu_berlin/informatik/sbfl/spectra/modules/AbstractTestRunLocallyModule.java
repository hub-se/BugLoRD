/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.modules;

import java.io.Serializable;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.junittestutils.testrunner.running.ExtendedTestRunModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;

/**
 * Runs a single test inside a new JVM and generates statistics. A timeout may
 * be set such that each executed test that runs longer than this timeout will
 * be aborted and will count as failing.
 * 
 * <p>
 * if the test can't be run at all, this information is given in the returned
 * statistics, together with an error message.
 * 
 * @author Simon Heiden
 */
public abstract class AbstractTestRunLocallyModule<T extends Serializable>
		extends AbstractProcessor<TestWrapper, Pair<TestStatistics, T>> {

	private ExtendedTestRunModule testRunner;

	public AbstractTestRunLocallyModule(final String testOutput, 
			final boolean debugOutput, final Long timeout, final int repeatCount, ClassLoader cl) {
		super();

		this.testRunner = new ExtendedTestRunModule(testOutput, debugOutput, timeout, repeatCount, cl);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.
	 * Object)
	 */
	@Override
	public Pair<TestStatistics, T> processItem(final TestWrapper testWrapper,
			ProcessorSocket<TestWrapper, Pair<TestStatistics, T>> socket) {
		socket.forceTrack(testWrapper.toString());
		// Log.out(this, "Now processing: '%s'.", testWrapper);

		boolean preparationSucceeded = prepareBeforeRunningTest();

		if (preparationSucceeded) {
			//(try to) run the test and get the statistics
			TestStatistics testResult = testRunner.submit(testWrapper).getResult();

			return getResultAfterTest(testWrapper, testResult);
		} else {
			TestStatistics statistics = new TestStatistics();
			statistics.addStatisticsElement(StatisticsData.COVERAGE_GENERATION_FAILED, 1);
			statistics.addStatisticsElement(StatisticsData.ERROR_MSG, testWrapper + ": Test preparation failed.");
			
			return new Pair<>(statistics, null);
		}
	}

	public abstract Pair<TestStatistics, T> getResultAfterTest(final TestWrapper testWrapper, TestStatistics testResult);
	
	public abstract boolean prepareBeforeRunningTest();
	
	@Override
	public boolean finalShutdown() {
		if (testRunner != null) {
			testRunner.finalShutdown();
		}
		return super.finalShutdown();
	}

}
