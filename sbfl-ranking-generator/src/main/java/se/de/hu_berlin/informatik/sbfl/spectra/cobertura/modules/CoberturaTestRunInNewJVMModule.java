/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.cobertura.modules;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.apache.commons.cli.Option;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import se.de.hu_berlin.informatik.junittestutils.testlister.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.testlister.data.TestStatistics;
import se.de.hu_berlin.informatik.junittestutils.testlister.data.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.testlister.running.ExtendedTestRunModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
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
public class CoberturaTestRunInNewJVMModule extends AbstractProcessor<TestWrapper, Pair<TestStatistics, ProjectData>> {

	private static Byte DATA_IS_NULL = Byte.valueOf((byte) 0);
	private static Byte DATA_IS_NOT_NULL = Byte.valueOf((byte) 1);
	public static ProjectData SHUTDOWN_NOTICE = new ProjectData();
	
	final private ServerSocket server;
	final private Thread serverListenerThread;
	
	private ProjectData receivedProjectData = null;
	private volatile boolean hasNewData = false;
	private volatile boolean serverErrorOccurred = false;
	private volatile boolean isShutdown = false;
	
	final private Object receiveLock = new Object();
	
	final private ExecuteMainClassInNewJVM executeModule;

	final private Path resultOutputFile;
	final private String resultOutputFileString;
	final private String testOutput;
	final private String[] args;
	final private int port;
	
	public CoberturaTestRunInNewJVMModule(final String testOutput, 
			final boolean debugOutput, final Long timeout, 
			String instrumentedClassPath, final Path dataFile, final String javaHome, File projectDir) {
		this(testOutput, debugOutput, timeout, 1, instrumentedClassPath, dataFile, javaHome, projectDir);
	}

	public CoberturaTestRunInNewJVMModule(final String testOutput, 
			final boolean debugOutput, final Long timeout, final int repeatCount, 
			String instrumentedClassPath, final Path dataFile, final String javaHome, File projectDir) {
		super();
		this.testOutput = testOutput;
		this.resultOutputFile = 
				Paths.get(this.testOutput).resolve("__testResult.stats.csv").toAbsolutePath();
		this.resultOutputFileString = resultOutputFile.toString();

		this.executeModule = new ExecuteMainClassInNewJVM(
				javaHome, 
				TestRunner.class,
				instrumentedClassPath,
				projectDir, 
				"-Dnet.sourceforge.cobertura.datafile=" + dataFile.toAbsolutePath().toString())
				.setEnvVariable("TZ", "America/Los_Angeles");
		
		int arrayLength = 8;
		if (timeout != null) {
			++arrayLength;
			++arrayLength;
		}
		if (!debugOutput) {
			++arrayLength;
		}
		
		this.port = getFreePort(new Random().nextInt(60536) + 5000);
		
		server = startServer(port);
		Log.out(this, "Server started with port %d...", port);
		
		if (server != null) {
			serverListenerThread = new Thread(new ServerSideListener(server));
			serverListenerThread.start();
		} else {
			serverListenerThread = null;
		}
		
		args = new String[arrayLength];
		
		int argCounter = 3;
		args[++argCounter] = TestRunner.CmdOptions.OUTPUT.asArg();
		args[++argCounter] = resultOutputFileString;
		
		args[++argCounter] = TestRunner.CmdOptions.PORT.asArg();
		args[++argCounter] = String.valueOf(port);
		
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
			inetAddress = InetAddress.getByName(null);
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
	public Pair<TestStatistics, ProjectData> processItem(final TestWrapper testWrapper, ProcessorSocket<TestWrapper, Pair<TestStatistics, ProjectData>> socket) {
		socket.forceTrack(testWrapper.toString());
//		Log.out(this, "Now processing: '%s'.", testWrapper);
		int result = -1;

		int argCounter = -1;
		args[++argCounter] = TestRunner.CmdOptions.TEST_CLASS.asArg();
		args[++argCounter] = testWrapper.getTestClassName();
		args[++argCounter] = TestRunner.CmdOptions.TEST_NAME.asArg();
		args[++argCounter] = testWrapper.getTestMethodName();

		result = executeModule.submit(args).getResult();
		
//		Log.out(this, "waiting for data...");
		// wait for new data if necessary (should be available)
		synchronized (receiveLock) {
			while (!hasNewData && !serverErrorOccurred) {
				try {
					receiveLock.wait();
				} catch (InterruptedException e) {
					// try again
				}
			}
			hasNewData = false;
		}
		
		if (serverErrorOccurred) {
			// reset for next time...
			serverErrorOccurred = false;
			Log.err(CoberturaTestRunInNewJVMModule.class, testWrapper + ": Could not retrieve project data.");
			return new Pair<>(
					new TestStatistics(testWrapper + ": Could not retrieve project data."),
					null);
		}
		
		if (result != 0) {
			Log.err(CoberturaTestRunInNewJVMModule.class, testWrapper + ": Running test in separate JVM failed.");
			return new Pair<>(
					new TestStatistics(testWrapper + ": Running test in separate JVM failed."),
					null);
		}

//		Log.out(this, "returning valid item...");
		return new Pair<>(
				new TestStatistics(Statistics.loadAndMergeFromCSV(StatisticsData.class, resultOutputFile)),
				receivedProjectData);
	}
	
	public ServerSocket startServer(int port) {
	    try {
	        ServerSocket socket = new ServerSocket(port);
	        return socket;
	    } catch (Exception e) {
	        System.err.println("Server Error: " + e.getMessage());
	        System.err.println("Localized: " + e.getLocalizedMessage());
	        System.err.println("Stack Trace: " + e.getStackTrace());
	        System.err.println("To String: " + e.toString());
	    }
	    
	    return null;
	}

	public static void sendToServer(ProjectData projectData, int port) {
	    try {
	        // Create the socket
	        Socket clientSocket = new Socket((String)null, port);
//	        Log.out("client", "Client Socket initialized...");
	        // Create the input & output streams to the server
	        ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
	        ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());

	        boolean succeeded = false;
	        while (!succeeded) {
//	        	Log.out("client", "writing data to port %d...", port);
	        	/* Send the Message Object to the server */
	        	outToServer.writeObject(projectData);            

	        	/* Retrieve the Message Object from server */
	        	Byte inFromServerMsg = (Byte)inFromServer.readObject();
	        	
	        	/* Print out the received Message */
//	        	Log.out("client", "Message from server: " + inFromServerMsg);
		        
	        	if (projectData == null && inFromServerMsg.equals(DATA_IS_NULL) ||
	        			projectData != null && inFromServerMsg.equals(DATA_IS_NOT_NULL)) {
	        		succeeded = true;
	        	}
	        }

	        clientSocket.close();

	    } catch (Exception e) {
	        System.err.println("Client Error: " + e.getMessage());
	        System.err.println("Localized: " + e.getLocalizedMessage());
	        System.err.println("Stack Trace: " + e.getStackTrace());
	    }
	}
	
	private class ServerSideListener implements Runnable {

		final private ServerSocket serverSocket;
		
		public ServerSideListener(ServerSocket serverSocket) {
			this.serverSocket = serverSocket;
		}
		
		@Override
		public void run() {
			listenOnSocket(serverSocket);
		}
		
		private void listenOnSocket(ServerSocket serverSocket) {
			while (!isShutdown) {
				try {
					// Create the Client Socket
					Socket clientSocket = serverSocket.accept();
//					Log.out(this, "Server Socket Extablished...");
					// Create input and output streams to client
					ObjectOutputStream outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
					ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());

					/* Retrieve information */
					receivedProjectData = (ProjectData)inFromClient.readObject();

					// tell any waiting threads that there is new data...
					synchronized (receiveLock) {
						hasNewData = true;
						receiveLock.notifyAll();
					}

					/* Send a message object back */
					if (receivedProjectData == null) {
						outToClient.writeObject(DATA_IS_NULL);
					} else {
						outToClient.writeObject(DATA_IS_NOT_NULL);
					}

				} catch (Exception e) {
					// tell any waiting threads that there is an error...
					synchronized (receiveLock) {
						serverErrorOccurred = true;
						receiveLock.notifyAll();
					}
					System.err.println("Server Error: " + e.getMessage());
					System.err.println("Localized: " + e.getLocalizedMessage());
					System.err.println("Stack Trace: " + e.getStackTrace());
					System.err.println("To String: " + e.toString());
				}
			}
		}
		
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
		 */
		public static void main(final String[] args) {

			if (System.getProperty("net.sourceforge.cobertura.datafile") == null) {
				Log.abort(TestRunner.class, "Please include property '-Dnet.sourceforge.cobertura.datafile=.../cobertura.ser' in the application's call.");
			}

			final OptionParser options = OptionParser.getOptions("TestRunner", false, CmdOptions.class, args);

			final Path outputFile = options.isFile(CmdOptions.OUTPUT, false);
//			final Path coberturaDataFile = Paths.get(System.getProperty("net.sourceforge.cobertura.datafile"));
//			Log.out(TestRunner.class, "Cobertura data file: '%s'.", System.getProperty("net.sourceforge.cobertura.datafile"));

			final String className = options.getOptionValue(CmdOptions.TEST_CLASS);
			final String testName = options.getOptionValue(CmdOptions.TEST_NAME);
			
			Integer port = options.getOptionValueAsInt(CmdOptions.PORT);
			if (port == null) {
				Log.abort(TestRunner.class, "Given port '%s' can not be parsed as an integer.", options.getOptionValue(CmdOptions.PORT));
			}
			
			ExtendedTestRunModule testRunner = new ExtendedTestRunModule(outputFile.getParent().toString(), 
					true, options.hasOption(CmdOptions.TIMEOUT) ? Long.valueOf(options.getOptionValue(CmdOptions.TIMEOUT)) : null, null);
			
			//initialize/reset the project data
			ProjectData.saveGlobalProjectData();
			//turn off auto saving (removes the shutdown hook inside of Cobertura)
			ProjectData.turnOffAutoSave();
			
			ProjectData projectData = null;

			//(try to) run the test and get the statistics
			TestStatistics statistics = testRunner
					.submit(new TestWrapper(className, testName))
					.getResult();

			//see if the test was executed and finished execution normally
			if (statistics.couldBeFinished()) {
				// wait for some milliseconds
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// do nothing
				}
				projectData = new ProjectData();

				TouchCollector.applyTouchesOnProjectData(projectData);
			}

			testRunner.finalShutdown();
			
			sendToServer(projectData, port);

			statistics.saveToCSV(outputFile);
		}
		
		

	}

	@Override
	public boolean finalShutdown() {
//		Log.out(this, "Shutting down...");
		if (serverListenerThread != null) {
			isShutdown = true;
			sendToServer(SHUTDOWN_NOTICE, port);
			while (serverListenerThread.isAlive()) {
				try {
					serverListenerThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return super.finalShutdown();
	}
	
	

}
