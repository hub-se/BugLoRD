/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.modules;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
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
public abstract class AbstractTestRunInNewJVMModuleWithServer<T extends Serializable>
		extends AbstractTestRunInNewJVMModule<T> {

	public static Byte DATA_IS_NULL = Byte.valueOf((byte) 0);
	public static Byte DATA_IS_NOT_NULL = Byte.valueOf((byte) 1);

	public static Byte SEND_AGAIN = Byte.valueOf((byte) 2);

	final private ServerSocket server;
	final private ServerSideListener<T, Byte> listener;
	final private Object receiveLock = new Object();
	final private int port;
	
	public AbstractTestRunInNewJVMModuleWithServer(final String testOutput) {
		super(testOutput);

		server = SimpleServerFramework.startServer();

		if (server != null) {
			this.port = server.getLocalPort();
//			Log.out(this, "Server started with port %d...", port);
			this.listener = SimpleServerFramework.<T, Byte>startServerListener(server, receiveLock, (t) -> {
				if (t == null || t.equals(null)) {
					return DATA_IS_NULL;
				} else {
					return DATA_IS_NOT_NULL;
				}
			}, () -> {
				return SEND_AGAIN;
			});
			if (this.listener == null) {
				Log.abort(this, "Unable to start server listener thread.");
			}
		} else {
			this.port = -1;
			this.listener = null;
			Log.abort(this, "Unable to establich server socket.");
		}

	}

	protected int getServerPort() {
		return this.port;
	}
	
	@Override
	public Pair<TestStatistics, T> getResultAfterTest(final TestWrapper testWrapper, int result) {
		if (result != 0) {
			// reset for next time...
			listener.resetListener();
//			Log.err(this, testWrapper + ": Running test in separate JVM failed.");
			TestStatistics statistics = new TestStatistics(testWrapper + ": Running test in separate JVM failed.");
			statistics.addStatisticsElement(StatisticsData.COVERAGE_GENERATION_FAILED, 1);
			return new Pair<>(statistics, null);
		}

		// Log.out(this, "waiting for data...");
		// wait for new data if necessary (should already be available)
		synchronized (receiveLock) {
			while (!listener.hasNewData() && !listener.serverErrorOccurred()) {
				try {
					receiveLock.wait();
				} catch (InterruptedException e) {
					// try again
				}
			}
		}

		if (listener.serverErrorOccurred()) {
			// reset for next time...
			listener.resetListener();
			TestStatistics statistics = new TestStatistics(testWrapper + ": Could not retrieve coverage data.");
			statistics.addStatisticsElement(StatisticsData.COVERAGE_GENERATION_FAILED, 1);
			return new Pair<>(statistics, null);
		}

		// Log.out(this, "returning valid item...");
		return new Pair<>(new TestStatistics(Statistics.loadAndMergeFromCSV(StatisticsData.class, getStatisticsResultFile())),
				listener.getLastData());
	}

	@Override
	public boolean finalShutdown() {
		// Log.out(this, "Shutting down...");
		if (listener != null) {
			listener.shutDown();
		}
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				Log.abort(this, e, "Could not close server socket.");
			}
		}
		return super.finalShutdown();
	}

}
