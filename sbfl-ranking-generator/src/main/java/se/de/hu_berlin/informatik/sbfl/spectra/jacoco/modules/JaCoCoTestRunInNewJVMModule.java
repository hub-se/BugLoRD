/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.jacoco.modules;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Path;
import org.apache.commons.cli.Option;
import org.jacoco.agent.AgentJar;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.jacoco.core.tools.ExecFileLoader;

import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.junittestutils.testrunner.running.ExtendedTestRunModule;
import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.JaCoCoToSpectra;
import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.SerializableExecFileLoader;
import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.modules.JaCoCoTestRunInNewJVMModule.TestRunner.CmdOptions;
import se.de.hu_berlin.informatik.sbfl.spectra.modules.AbstractTestRunInNewJVMModuleWithServer;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.SimpleServerFramework;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.basics.ExecuteMainClassInNewJVM;

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
public class JaCoCoTestRunInNewJVMModule extends AbstractTestRunInNewJVMModuleWithServer<SerializableExecFileLoader> {

	final private ExecuteMainClassInNewJVM executeModule;

	final private String[] args;
	
	public JaCoCoTestRunInNewJVMModule(final String testOutput, 
			final boolean debugOutput, final Long timeout, final int repeatCount, 
			String instrumentedClassPath, final String javaHome, File projectDir, int port) {
		super(testOutput);

		int freePort = SimpleServerFramework.getFreePort(port);
		
		if (JaCoCoToSpectra.OFFLINE_INSTRUMENTATION) {
			this.executeModule = new ExecuteMainClassInNewJVM(
					//javaHome,
					null, 
					TestRunner.class,
					instrumentedClassPath,
					projectDir,
					"-Djacoco-agent.dumponexit=false", 
					"-Djacoco-agent.output=tcpserver",
					"-Djacoco-agent.excludes=*",
					"-Djacoco-agent.port=" + freePort
					)
					.setEnvVariable("LC_ALL","en_US.UTF-8")
					.setEnvVariable("TZ", "America/Los_Angeles");
		} else {
			File jacocoAgentJar = null; 
			try {
				jacocoAgentJar = AgentJar.extractToTempLocation();
			} catch (IOException e) {
				Log.abort(JaCoCoToSpectra.class, e, "Could not create JaCoCo agent jar file.");
			}
			
			this.executeModule = new ExecuteMainClassInNewJVM(
					//javaHome,
					null, 
					TestRunner.class,
					instrumentedClassPath,
					projectDir,
					"-javaagent:" + jacocoAgentJar.getAbsolutePath() 
					+ "=dumponexit=false,"
					+ "output=tcpserver,"
					+ "excludes=se.de.hu_berlin.informatik.*:org.junit.*,"
					+ "port=" + freePort
					)
					.setEnvVariable("LC_ALL","en_US.UTF-8")
					.setEnvVariable("TZ", "America/Los_Angeles");
		}
		
		int arrayLength = 10;
		if (timeout != null) {
			++arrayLength;
			++arrayLength;
		}
		if (!debugOutput) {
			++arrayLength;
		}

		args = new String[arrayLength];
		
		args[0] = CmdOptions.TEST_CLASS.asArg();
		args[2] = CmdOptions.TEST_NAME.asArg();

		int argCounter = 3;
		args[++argCounter] = TestRunner.CmdOptions.OUTPUT.asArg();
		args[++argCounter] = getStatisticsResultFile().toString();

		args[++argCounter] = TestRunner.CmdOptions.PORT.asArg();
		args[++argCounter] = String.valueOf(getServerPort());
		
		args[++argCounter] = TestRunner.CmdOptions.AGENT_PORT.asArg();
		args[++argCounter] = String.valueOf(freePort);
		
		if (timeout != null) {
			args[++argCounter] = TestRunner.CmdOptions.TIMEOUT.asArg();
			args[++argCounter] = String.valueOf(timeout.longValue());
		}
		if (!debugOutput) {
			args[++argCounter] = OptionParser.DefaultCmdOptions.SILENCE_ALL.asArg();
		}
	}
	
	@Override
	public boolean prepareBeforeRunningTest() {
		//not necessary
		return true;
	}
	
	@Override
	public String[] getArgs(String testClassName, String testMethodName) {	
		args[1] = testClassName;
		args[3] = testMethodName;
		return args;
	}

	@Override
	public ExecuteMainClassInNewJVM getMain() {
		return executeModule;
	}

	public final static class TestRunner {

		private TestRunner() {
			//disallow instantiation
		}

		public static enum CmdOptions implements OptionWrapperInterface {
			/* add options here according to your needs */
			TEST_CLASS("c", "testClass", true, "The name of the class that the test can be found in.", true),
			TEST_NAME("t", "testName", true, "The name of the test to run.", true),
			TIMEOUT("tm", "timeout", true, "A timeout (in seconds) for the execution of each test. Tests that run "
					+ "longer than the timeout will abort and will count as failing.", false),
			PORT("p", "port", true, "The port to connect to and send the project data.", false),
			AGENT_PORT("ap", "agentPort", true, "The port to use for connecting to the JaCoCo Java agent.", true),
			OUTPUT("o", "output", true, "Path to result statistics file.", true);

			/* the following code blocks should not need to be changed */
			final private OptionWrapper option;

			//adds an option that is not part of any group
			CmdOptions(final String opt, final String longOpt, 
					final boolean hasArg, final String description, final boolean required) {
				this.option = new OptionWrapper(
						Option.builder(opt).longOpt(longOpt).required(required).
						hasArg(hasArg).desc(description).build(), NO_GROUP);
			}

			//adds an option that is part of the group with the specified index (positive integer)
			//a negative index means that this option is part of no group
			//this option will not be required, however, the group itself will be
			CmdOptions(final String opt, final String longOpt, 
					final boolean hasArg, final String description, final int groupId) {
				this.option = new OptionWrapper(
						Option.builder(opt).longOpt(longOpt).required(false).
						hasArg(hasArg).desc(description).build(), groupId);
			}

			//adds the given option that will be part of the group with the given id
			CmdOptions(final Option option, final int groupId) {
				this.option = new OptionWrapper(option, groupId);
			}

			//adds the given option that will be part of no group
			CmdOptions(final Option option) {
				this(option, NO_GROUP);
			}

			@Override public String toString() { return option.getOption().getOpt(); }
			@Override public OptionWrapper getOptionWrapper() { return option; }
		}

		/**
		 * @param args
		 * command line arguments
		 * @throws Exception 
		 * if any error happens...
		 */
		public static void main(final String[] args) throws Exception {

			final OptionParser options = OptionParser.getOptions("TestRunner", false, CmdOptions.class, args);
			
			final Path outputFile = options.isFile(CmdOptions.OUTPUT, false);

			final String testClazz = options.getOptionValue(CmdOptions.TEST_CLASS);
			final String testName = options.getOptionValue(CmdOptions.TEST_NAME);

			Integer port = options.getOptionValueAsInt(CmdOptions.PORT);
			if (port == null) {
				Log.abort(TestRunner.class, "Given port '%s' can not be parsed as an integer.", options.getOptionValue(CmdOptions.PORT));
			}
			
			Integer agentPort = options.getOptionValueAsInt(CmdOptions.AGENT_PORT);
			if (agentPort == null) {
				Log.abort(TestRunner.class, "Given agent port '%s' can not be parsed as an integer.", options.getOptionValue(CmdOptions.AGENT_PORT));
			}
			
			ExtendedTestRunModule testRunner = new ExtendedTestRunModule(outputFile.getParent().toString(), 
					true, options.hasOption(CmdOptions.TIMEOUT) ? Long.valueOf(options.getOptionValue(CmdOptions.TIMEOUT)) : null, null);
			
			TestStatistics statistics = testRunner
					.submit(new TestWrapper(testClazz, testName))
					.getResult();

			testRunner.finalShutdown();
			
			ExecFileLoader loader = null;
			// see if the test was executed and finished execution normally
			if (statistics.couldBeFinished()) {
				// wait for some milliseconds
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// do nothing
				}
				// get execution data
				try {
					loader = dump(agentPort);
				} catch (IOException e) {
					loader = null;
					statistics.addStatisticsElement(StatisticsData.COVERAGE_GENERATION_FAILED, 1);
					Log.err(
							TestRunner.class, e,
							testClazz + ": Could not request execution data after running test " + testName + ".");
				}
			}

			boolean successful = SimpleServerFramework.sendToServer(
					new SerializableExecFileLoader(loader), port, 3,
					(r) -> {
						if (r.equals(SEND_AGAIN)) {
							return true;
						} else {
							return false;
						}
					},
					(t,r) -> {
						if (t.getExecFileLoader() == null && r.equals(DATA_IS_NULL) ||
								t.getExecFileLoader() != null && r.equals(DATA_IS_NOT_NULL)) {
							return true;
						} else {
							return false;
						}
					});

			if (!successful) {
				System.exit(1);
			}

			statistics.saveToCSV(outputFile);
			
			Runtime.getRuntime().exit(0);
		}
		
		private static ExecFileLoader dump(final int port) throws IOException {
			final ExecFileLoader loader = new ExecFileLoader();
			final Socket socket = tryConnect(port);
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

		private static Socket tryConnect(final int port) throws IOException {
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
					Log.err(TestRunner.class, "%s.", e.getMessage());
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException x) {
						throw new InterruptedIOException();
					}
				}
			}
		}
		
	}
}
