/**
 * 
 */
package se.de.hu_berlin.informatik.gen.spectra.modules;

import java.io.Serializable;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.miscellaneous.SimpleServerFramework;
import se.de.hu_berlin.informatik.utils.miscellaneous.SimpleServerFramework.ServerSideListener;
import se.de.hu_berlin.informatik.utils.statistics.Statistics;

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
public abstract class AbstractRunTestInNewJVMModuleWithServer<T extends Serializable>
		extends AbstractRunTestInNewJVMModule<T> {

	final private ServerSideListener<T, Byte> listener;
	final private int port;
	
	public AbstractRunTestInNewJVMModuleWithServer(final String testOutput) {
		super(testOutput);

		listener = SimpleServerFramework.startServer();

		if (listener != null) {
			this.port = listener.getServerPort();
//			Log.out(this, "Server started with port %d...", port);
		} else {
			this.port = -1;
			Log.abort(this, "Unable to establich server.");
		}

	}

	protected int getServerPort() {
		return this.port;
	}
	
	@Override
	public Pair<TestStatistics, T> getResultAfterTest(final TestWrapper testWrapper, int result) {
		if (result != 0) {
			// 
			// reset for next time...
			listener.resetListener();
//			Log.err(this, testWrapper + ": Running test in separate JVM failed.");
			TestStatistics statistics = new TestStatistics();
			statistics.addStatisticsElement(StatisticsData.ERROR_MSG, testWrapper + ": Running test in separate JVM failed.");
			statistics.addStatisticsElement(StatisticsData.COVERAGE_GENERATION_FAILED, 1);
			return new Pair<>(statistics, null);
		}

		T data = listener.getNewData();

		// Log.out(this, "returning valid item...");
		return new Pair<>(new TestStatistics(Statistics.loadAndMergeFromCSV(StatisticsData.class, getStatisticsResultFile())),
				data);
	}

	@Override
	public boolean finalShutdown() {
		// Log.out(this, "Shutting down...");
		if (listener != null) {
			listener.shutDown();
		}
		return super.finalShutdown();
	}

}
