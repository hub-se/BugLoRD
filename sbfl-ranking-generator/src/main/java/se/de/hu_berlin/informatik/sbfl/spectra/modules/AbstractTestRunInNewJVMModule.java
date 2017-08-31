/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.modules;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.nio.file.Path;
import se.de.hu_berlin.informatik.junittestutils.testlister.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.testlister.data.TestStatistics;
import se.de.hu_berlin.informatik.junittestutils.testlister.data.TestWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.miscellaneous.SimpleServerFramework;
import se.de.hu_berlin.informatik.utils.miscellaneous.SimpleServerFramework.ServerSideListener;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.basics.ExecuteMainClassInNewJVM;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;
import se.de.hu_berlin.informatik.utils.statistics.Statistics;

/**
 * Runs a single test inside a new JVM and generates statistics. A timeout may be set
 * such that each executed test that runs longer than this timeout will
 * be aborted and will count as failing.
 * 
 * <p> if the test can't be run at all, this information is given in the
 * returned statistics, together with an error message.
 * 
 * @author Simon Heiden
 */
public abstract class AbstractTestRunInNewJVMModule<T extends Serializable> extends AbstractProcessor<TestWrapper, Pair<TestStatistics, T>> {

	public static Byte DATA_IS_NULL = Byte.valueOf((byte) 0);
	public static Byte DATA_IS_NOT_NULL = Byte.valueOf((byte) 1);

	final private ServerSocket server;
	final private ServerSideListener<T, Byte> listener;
	final private Object receiveLock = new Object();
	final private int port;
	
	final private String testClassOptionKey;
	final private String testMethodOptionKey;

	final private Path resultOutputFile;

	public AbstractTestRunInNewJVMModule(Path resultOutputFile, 
			String testClassOptionKey, String testMethodOptionKey) {
		super();
		this.resultOutputFile = resultOutputFile;
		this.testClassOptionKey = testClassOptionKey;
		this.testMethodOptionKey = testMethodOptionKey;
		
		server = SimpleServerFramework.startServer();
		
		if (server != null) {
			this.port = server.getLocalPort();
			Log.out(this, "Server started with port %d...", port);
			this.listener = SimpleServerFramework.<T,Byte>startServerListener(server, receiveLock,
					(t) -> {
						if (t == null) {
							return DATA_IS_NULL;
						} else {
							return DATA_IS_NOT_NULL;
						}
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
	

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public Pair<TestStatistics, T> processItem(final TestWrapper testWrapper, 
			ProcessorSocket<TestWrapper, Pair<TestStatistics, T>> socket) {
		socket.forceTrack(testWrapper.toString());
//		Log.out(this, "Now processing: '%s'.", testWrapper);
		int result = -1;

		String[] args = getArgs();
		int argCounter = -1;
		args[++argCounter] = testClassOptionKey;
		args[++argCounter] = testWrapper.getTestClassName();
		args[++argCounter] = testMethodOptionKey;
		args[++argCounter] = testWrapper.getTestMethodName();

		result = getMain().submit(args).getResult();
		
//		Log.out(this, "waiting for data...");
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
			Log.err(this, testWrapper + ": Could not retrieve project data.");
			return new Pair<>(
					new TestStatistics(testWrapper + ": Could not retrieve project data."),
					null);
		}
		
		if (result != 0) {
			// reset for next time...
			listener.resetListener();
			Log.err(this, testWrapper + ": Running test in separate JVM failed.");
			return new Pair<>(
					new TestStatistics(testWrapper + ": Running test in separate JVM failed."),
					null);
		}

//		Log.out(this, "returning valid item...");
		return new Pair<>(
				new TestStatistics(Statistics.loadAndMergeFromCSV(StatisticsData.class, resultOutputFile)),
				listener.getLastData());
	}
	
	public abstract String[] getArgs();
	
	public abstract ExecuteMainClassInNewJVM getMain();

	@Override
	public boolean finalShutdown() {
//		Log.out(this, "Shutting down...");
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
