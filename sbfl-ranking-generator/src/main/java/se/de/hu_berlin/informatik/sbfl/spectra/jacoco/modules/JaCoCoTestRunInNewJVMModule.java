/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.jacoco.modules;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.apache.commons.cli.Option;
import org.jacoco.agent.AgentJar;
import org.jacoco.core.runtime.AgentOptions;
import se.de.hu_berlin.informatik.sbfl.StatisticsData;
import se.de.hu_berlin.informatik.sbfl.TestStatistics;
import se.de.hu_berlin.informatik.sbfl.TestWrapper;
import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.JaCoCoToSpectra;
import se.de.hu_berlin.informatik.sbfl.spectra.modules.TestRunModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
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
public class JaCoCoTestRunInNewJVMModule extends AbstractProcessor<TestWrapper, TestStatistics> {

	final private ExecuteMainClassInNewJVM executeModule;

	final private Path resultOutputFile;
	final private String resultOutputFileString;
	final private String testOutput;
	final private String[] args;
	
	public JaCoCoTestRunInNewJVMModule(final String testOutput, 
			final boolean debugOutput, final Long timeout, final int repeatCount, 
			String instrumentedClassPath, final Path dataFile, final String javaHome, File projectDir, int port) {
		super();
		this.testOutput = testOutput;
		this.resultOutputFile = 
				Paths.get(this.testOutput).resolve("__testResult.stats.csv").toAbsolutePath();
		this.resultOutputFileString = resultOutputFile.toString();

		File jacocoAgentJar = null; 
		try {
			jacocoAgentJar = AgentJar.extractToTempLocation();
		} catch (IOException e) {
			Log.abort(JaCoCoToSpectra.class, e, "Could not create JaCoCo agent jar file.");
		}
		
		
		// get a port that is not yet used...
		port = getFreePort(port);
		if (port == -1) {
			Log.abort(JaCoCoToSpectra.class, "Could not find an unused port...");
		}
		Log.out(JaCoCoToSpectra.class, "Using port %d.", port);
		
		if (JaCoCoToSpectra.OFFLINE_INSTRUMENTATION) {
			this.executeModule = new ExecuteMainClassInNewJVM(
					javaHome, 
					TestRunner.class,
					instrumentedClassPath,
					projectDir,
//					"-Djacoco.datafile=" + dataFile.toAbsolutePath().toString()
//					,
					"-Djacoco-agent.dumponexit=true", 
					"-Djacoco-agent.output=file",
					"-Djacoco-agent.excludes=*",
					"-Djacoco-agent.destfile=" + dataFile.toAbsolutePath().toString(),
					"-Djacoco-agent.port=" + port
					)
					.setEnvVariable("TZ", "America/Los_Angeles");
		} else {
			this.executeModule = new ExecuteMainClassInNewJVM(
					javaHome, 
					TestRunner.class,
					instrumentedClassPath,
					projectDir,
//					"-Djacoco.datafile=" + dataFile.toAbsolutePath().toString()
//					,
					"-javaagent:" + jacocoAgentJar.getAbsolutePath() 
					+ "=dumponexit=true,"
					+ "output=file,"
					+ "destfile=" + dataFile.toAbsolutePath().toString() + ","
					+ "excludes=se.de.hu_berlin.informatik.*:org.junit.*:sun.util.*:org.apache.commons.cli.*:sun.text.*,"
					+ "port=" + port
					)
					.setEnvVariable("TZ", "America/Los_Angeles");
		}
		
		int arrayLength = 6;
		if (timeout != null) {
			++arrayLength;
			++arrayLength;
		}
		if (!debugOutput) {
			++arrayLength;
		}
		
		args = new String[arrayLength];
		
		int argCounter = 3;
		args[++argCounter] = TestRunner.CmdOptions.OUTPUT.asArg();
		args[++argCounter] = resultOutputFileString;
		
		if (timeout != null) {
			args[++argCounter] = TestRunner.CmdOptions.TIMEOUT.asArg();
			args[++argCounter] = String.valueOf(timeout.longValue());
		}
		if (!debugOutput) {
			args[++argCounter] = OptionParser.DefaultCmdOptions.SILENCE_ALL.asArg();
		}
	}
	
	private static int getFreePort(final int startPort) {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(AgentOptions.DEFAULT_ADDRESS);
		} catch (UnknownHostException e1) {
			// should not happen
			return -1;
		}
		// port between 0 and 65535 !
		Random random = new Random();
		int currentPort = startPort;
		int count = 0;
		while (true) {
			if (count > 1000) {
				return -1;
			}
			++count;
			try {
				new Socket(inetAddress, currentPort).close();
			} catch (final IOException e) {
				// found a free port
				break;
			} catch (IllegalArgumentException e) {
				// should only happen on first try (if argument wrong)
			}
			currentPort = random.nextInt(60536) + 5000;
		}
		return currentPort;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	@Override
	public TestStatistics processItem(final TestWrapper testWrapper, ProcessorSocket<TestWrapper, TestStatistics> socket) {
		socket.forceTrack(testWrapper.toString());
//		Log.out(this, "Now processing: '%s'.", testWrapper);
		int result = -1;

		int argCounter = -1;
		args[++argCounter] = TestRunner.CmdOptions.TEST_CLASS.asArg();
		args[++argCounter] = testWrapper.getTestClassName();
		args[++argCounter] = TestRunner.CmdOptions.TEST_NAME.asArg();
		args[++argCounter] = testWrapper.getTestMethodName();

		result = executeModule.submit(args).getResult();
		
		if (result != 0) {
			Log.err(JaCoCoTestRunInNewJVMModule.class, testWrapper + ": Running test in separate JVM failed.");
			return new TestStatistics(testWrapper + ": Running test in separate JVM failed.");
		}

		return new TestStatistics(Statistics.loadAndMergeFromCSV(StatisticsData.class, resultOutputFile));
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

			TestRunModule testRunner = new TestRunModule(outputFile.getParent().toString(), 
					true, options.hasOption(CmdOptions.TIMEOUT) ? Long.valueOf(options.getOptionValue(CmdOptions.TIMEOUT)) : null, null);
			
//			// For instrumentation and runtime we need a IRuntime instance
//			// to collect execution data:
//			final IRuntime runtime = new LoggerRuntime();
//			
//			// Now we're ready to run our instrumented class and need to startup the
//			// runtime first:
//			final RuntimeData data = new RuntimeData();
//			runtime.startup(data);
			
			TestStatistics statistics = testRunner
					.submit(new TestWrapper(testClazz, testName))
					.getResult();

			testRunner.finalShutdown();
			
//			// At the end of test execution we collect execution data and shutdown
//			// the runtime:
//			final ExecutionDataStore executionData = new ExecutionDataStore();
//			final SessionInfoStore sessionInfos = new SessionInfoStore();
//			data.collect(executionData, sessionInfos, true);
//			runtime.shutdown();
//			save(jacocoDataFile, executionData, sessionInfos);
			
//			try {
//				ExecFileLoader loader = dump(port);
//				loader.save(jacocoDataFile, false);
//			} catch (IOException e) {
//				Log.err(TestRunner.class, e, "Could not save coverage data.");
//			}

			statistics.saveToCSV(outputFile);
		}
		
	}
}
