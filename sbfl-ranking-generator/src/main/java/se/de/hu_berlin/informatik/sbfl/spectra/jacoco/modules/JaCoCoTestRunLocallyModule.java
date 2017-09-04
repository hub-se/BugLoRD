/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.jacoco.modules;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.jacoco.core.tools.ExecFileLoader;

import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.SerializableExecFileLoader;
import se.de.hu_berlin.informatik.sbfl.spectra.modules.AbstractTestRunLocallyModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;

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
public class JaCoCoTestRunLocallyModule extends AbstractTestRunLocallyModule<SerializableExecFileLoader> {

	private int port;

	public JaCoCoTestRunLocallyModule(final String testOutput, 
			final boolean debugOutput, final Long timeout, final int repeatCount, ClassLoader cl, int port) {
		super(testOutput, debugOutput, timeout, repeatCount, cl);
		this.port = port;
	}
	
	@Override
	public boolean prepareBeforeRunningTest() {
		//no preparation needed
		return true;
	}
	
	@Override
	public Pair<TestStatistics, SerializableExecFileLoader> getResultAfterTest(TestWrapper testWrapper, TestStatistics testResult) {
		if (testResult.couldBeFinished()) {
			ExecFileLoader loader;
			// get execution data
			try {
				loader = dump(port, true);
			} catch (IOException e) {
				loader = null;
			}
			return new Pair<>(testResult, new SerializableExecFileLoader(loader));
		} else {
			// dump execution data
			try {
				dump(port, false);
			} catch (IOException e) {
				// don't care
			}
			return new Pair<>(testResult, null);
		}
	}
	
	private ExecFileLoader dump(final int port, boolean log) throws IOException {
		final ExecFileLoader loader = new ExecFileLoader();
		final Socket socket = tryConnect(port, log);
		try {
			final RemoteControlWriter remoteWriter = new RemoteControlWriter(socket.getOutputStream());
			final RemoteControlReader remoteReader = new RemoteControlReader(socket.getInputStream());
			remoteReader.setSessionInfoVisitor(loader.getSessionInfoStore());
			remoteReader.setExecutionDataVisitor(loader.getExecutionDataStore());

			remoteWriter.visitDumpCommand(true, true);
			remoteReader.read();
		} finally {
			socket.close();
		}
		return loader;
	}

	private Socket tryConnect(final int port, boolean log) throws IOException {
		int count = 0;
		InetAddress inetAddress = InetAddress.getByName(AgentOptions.DEFAULT_ADDRESS);
		while (true) {
			try {
				// Log.out(this, "Connecting to %s:%s.", address,
				// Integer.valueOf(port));
				return new Socket(inetAddress, port);
			} catch (final IOException e) {
				if (++count > 2) {
					throw e;
				}
				if (log) {
					Log.err(this, "%s.", e.getMessage());
				}
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException x) {
					throw new InterruptedIOException();
				}
			}
		}
	}

}
