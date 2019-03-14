/**
 * 
 */
package se.de.hu_berlin.informatik.gen.spectra.modules;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.basics.ExecuteMainClassInNewJVM;
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
public abstract class AbstractRunTestInNewJVMModule<T extends Serializable>
		extends AbstractProcessor<TestWrapper, Pair<TestStatistics, T>> {

	private final Path resultOutputFile;

	public AbstractRunTestInNewJVMModule(final String testOutput) {
		super();
		this.resultOutputFile = 
				Paths.get(testOutput).resolve("__testResult.stats.csv").toAbsolutePath();
	}
	
	protected Path getStatisticsResultFile() {
		return resultOutputFile;
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
		int result = -1;

		String[] args = getArgs(testWrapper.getTestClassName(), testWrapper.getTestMethodName());
		
		boolean preparationSucceeded = prepareBeforeRunningTest();

		if (preparationSucceeded) {
			result = getMain().submit(args).getResult();

			return getResultAfterTest(testWrapper, result);
		} else {
			TestStatistics statistics = new TestStatistics();
			statistics.addStatisticsElement(StatisticsData.COVERAGE_GENERATION_FAILED, 1);
			statistics.addStatisticsElement(StatisticsData.ERROR_MSG, testWrapper + ": Test preparation failed.");
			
			return new Pair<>(statistics, null);
		}
	}

	public abstract Pair<TestStatistics, T> getResultAfterTest(final TestWrapper testWrapper, int executionResult);
	
	public abstract boolean prepareBeforeRunningTest();

	public abstract String[] getArgs(String testClassName, String testMethodName);

	public abstract ExecuteMainClassInNewJVM getMain();

}
