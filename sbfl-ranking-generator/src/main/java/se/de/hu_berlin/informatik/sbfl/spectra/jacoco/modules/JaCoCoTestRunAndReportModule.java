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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.jacoco.core.tools.ExecFileLoader;

import se.de.hu_berlin.informatik.sbfl.StatisticsData;
import se.de.hu_berlin.informatik.sbfl.TestStatistics;
import se.de.hu_berlin.informatik.sbfl.TestWrapper;
import se.de.hu_berlin.informatik.sbfl.spectra.modules.TestRunModule;
import se.de.hu_berlin.informatik.stardust.provider.jacoco.JaCoCoReportWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class JaCoCoTestRunAndReportModule extends AbstractProcessor<TestWrapper, JaCoCoReportWrapper> {

	final private String testOutput;
	// final private Arguments reportArguments;
	final private Long timeout;

	final private StatisticsCollector<StatisticsData> statisticsContainer;

	final private static ExecFileLoader UNDEFINED_COVERAGE_DUMMY = new ExecFileLoader();
	final private static ExecFileLoader UNFINISHED_EXECUTION_DUMMY = new ExecFileLoader();
	final private static ExecFileLoader WRONG_COVERAGE_DUMMY = new ExecFileLoader();
	
	final public static JaCoCoReportWrapper ERROR_WRAPPER = new JaCoCoReportWrapper(null, null, false);

	final private File dataFile;
	
	final private TestRunModule testRunner;
	final private JaCoCoTestRunInNewJVMModule testRunnerNewJVM;

	final private boolean alwaysUseSeparateJVM;

	private int testCounter = 0;

	// location of Java class files
	List<File> classfiles = new ArrayList<File>();

	// location of the source files
//	List<File> sourcefiles = new ArrayList<File>();
	
	final private int port;
	
	final private Set<String> knownFailingtests;
	private int failedTestCounter = 0;
	private boolean failingTestErrorOccurred = false;

	public JaCoCoTestRunAndReportModule(final Path dataFile, final String testOutput, File projectDir, final String srcDir, String[] originalClasses, int port,
			final boolean debugOutput, Long timeout, final int repeatCount, String instrumentedClassPath,
			final String javaHome, boolean useSeparateJVMalways, String[] failingtests,
			final StatisticsCollector<StatisticsData> statisticsContainer, ClassLoader cl) {
		super();
		if (failingtests == null) {
			knownFailingtests = null;
		} else {
			knownFailingtests = new HashSet<>();
			addKnownFailingTests(failingtests);
		}

		this.statisticsContainer = statisticsContainer;
		this.testOutput = testOutput;
		
		this.dataFile = dataFile.toFile();

//		this.sourcefiles.add(new File(srcDir));
		for (String classDirFile : originalClasses) {
			this.classfiles.add(new File(classDirFile));
		}

		this.timeout = timeout;
		this.port = port;

		this.alwaysUseSeparateJVM = instrumentedClassPath != null && useSeparateJVMalways;

		if (this.alwaysUseSeparateJVM) {
			this.testRunner = null;
			this.testRunnerNewJVM = new JaCoCoTestRunInNewJVMModule(this.testOutput, debugOutput, this.timeout, repeatCount,
					instrumentedClassPath, this.dataFile.toPath(), javaHome, projectDir, port);
		} else {
			this.testRunner = new TestRunModule(this.testOutput, debugOutput, this.timeout, repeatCount, cl);
			this.testRunnerNewJVM = null;
		}

		FileUtils.delete(this.dataFile);

	}
	
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

	/*
	 * (non-Javadoc)
	 * @see
	 * se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.
	 * Object)
	 */
	@Override
	public JaCoCoReportWrapper processItem(final TestWrapper testWrapper,
			ProcessorSocket<TestWrapper, JaCoCoReportWrapper> socket) {
		socket.allowOnlyForcedTracks();
		socket.forceTrack(testWrapper.toString());
		++testCounter;
		// Log.out(this, "Now processing: '%s'.", testWrapper);

		TestStatistics testStatistics = new TestStatistics();
		ExecFileLoader projectData = UNDEFINED_COVERAGE_DUMMY;

		if (alwaysUseSeparateJVM) {
			projectData = runTestInNewJVM(testWrapper, testStatistics);
			// Log.out(this, testStatistics.toString());
		} else {
			projectData = runTestLocally(testWrapper, testStatistics);
		}
			
		// check for "correct" (intended) test execution
		if (knownFailingtests != null) {
			if (testStatistics.couldBeFinished()) {
				String testName = testWrapper.toString();
				if (testStatistics.wasSuccessful()) {
					if (knownFailingtests.contains(testName)) {
						testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
								"Test '" + testName + "' was successful but should fail.");
						failingTestErrorOccurred = true;
					}
				} else {
					if (knownFailingtests.contains(testName)) {
						++failedTestCounter;
					} else {
						testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
								"Test '" + testName + "' failed but should be successful.");
						failingTestErrorOccurred = true;
					}
				}
			}
		}

		if (testStatistics.getErrorMsg() != null) {
			Log.err(this, testStatistics.getErrorMsg());
		}

		if (statisticsContainer != null) {
			statisticsContainer.addStatistics(testStatistics);
		}

		if (isNormalData(projectData)) {			
			return generateReport(testWrapper, testStatistics, projectData);
		} else {
			return null;
		}
	}

	private static boolean isNormalData(ExecFileLoader projectData) {
		return projectData != null && projectData != WRONG_COVERAGE_DUMMY && projectData != UNDEFINED_COVERAGE_DUMMY
				&& projectData != UNFINISHED_EXECUTION_DUMMY;
	}

	// private ExecFileLoader runTestLocallyOrInJVM(final TestWrapper
	// testWrapper,
	// TestStatistics testStatistics, ExecFileLoader lastProjectData) {
	// ExecFileLoader projectData;
	// if (lastProjectData == WRONG_COVERAGE_DUMMY) {
	// projectData = runTestInNewJVM(testWrapper, testStatistics);
	// } else {
	// projectData = runTestLocally(testWrapper, testStatistics);
	// }
	//
	//// //see if the test was executed and finished execution normally
	//// if (isNormalData(lastProjectData) && isNormalData(projectData)) {
	//// boolean isEqual = LockableProjectData.containsSameCoverage(projectData,
	// lastProjectData);
	//// if (!isEqual) {
	//// testStatistics.addStatisticsElement(StatisticsData.DIFFERENT_COVERAGE,
	// 1);
	//// testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG,
	//// testWrapper + ": Repeated test execution generated different
	// coverage.");
	//// projectData.merge(lastProjectData);
	//// }
	//// }
	//
	// return projectData;
	// }

	private ExecFileLoader runTestLocally(final TestWrapper testWrapper, final TestStatistics testStatistics) {
		// (try to) run the test and get the statistics
		TestStatistics testResult = testRunner.submit(testWrapper).getResult();
		testStatistics.mergeWith(testResult);

		ExecFileLoader loader = UNDEFINED_COVERAGE_DUMMY;
		// see if the test was executed and finished execution normally
		if (testResult.couldBeFinished()) {
			// wait for some milliseconds
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// do nothing
			}
			// get execution data
			try {
				loader = dump(port);
			} catch (IOException e) {
				loader = UNDEFINED_COVERAGE_DUMMY;
				Log.err(
						this, e,
						testWrapper + ": Could not request execution data after running test no. " + testCounter + ".");
				testStatistics.addStatisticsElement(
						StatisticsData.ERROR_MSG,
						testWrapper + ": Could not request execution data after running test no. " + testCounter + ".");
			}
		} else {
			loader = UNFINISHED_EXECUTION_DUMMY;
		}

		return loader;
	}

	private ExecFileLoader dump(final int port) throws IOException {
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

	private Socket tryConnect(final int port) throws IOException {
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
				Log.err(this, "%s.", e.getMessage());
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException x) {
					throw new InterruptedIOException();
				}
			}
		}
	}

	private ExecFileLoader runTestInNewJVM(TestWrapper testWrapper, TestStatistics testStatistics) {
		ExecFileLoader loader;
		// (try to) run the test in new JVM and get the statistics
		TestStatistics testResult = testRunnerNewJVM.submit(testWrapper).getResult();
		testStatistics.mergeWith(testResult);
		testStatistics.addStatisticsElement(StatisticsData.SEPARATE_JVM, 1);

		loader = UNDEFINED_COVERAGE_DUMMY;
		// see if the test was executed and finished execution normally
		if (testResult.couldBeFinished()) {
			// get execution data
			if (dataFile.exists()) {
				try {
					loader = new ExecFileLoader();
					loader.load(dataFile);
				} catch (IOException e) {
					loader = UNDEFINED_COVERAGE_DUMMY;
					Log.err(
							this, e,
							testWrapper + ": Could not request execution data after running test no. " + testCounter + ".");
					testStatistics.addStatisticsElement(
							StatisticsData.ERROR_MSG,
							testWrapper + ": Could not request execution data after running test no. " + testCounter + ".");
				}
			} else {
				loader = UNDEFINED_COVERAGE_DUMMY;
				Log.err(this, testWrapper + ": Data file does not exist after running test no. " + testCounter + ".");
				testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, 
						testWrapper + ": Data file does not exist after running test no. " + testCounter + ".");
			}
		} else {
			loader = UNFINISHED_EXECUTION_DUMMY;
		}
		FileUtils.delete(dataFile);
		
		return loader;
	}

	private JaCoCoReportWrapper generateReport(final TestWrapper testWrapper, TestStatistics testStatistics,
			ExecFileLoader execFileLoader) {
		// generate the report
		IBundleCoverage bundle = null;
		try {
			bundle = analyze(execFileLoader.getExecutionDataStore());
		} catch (IOException e) {
			Log.abort(this, e, "Analysis failed.");
		}
		// try {
		// writeReports(bundle, execFileLoader);
		// } catch (IOException e) {
		// Log.abort(this, e, "Writing reports failed.");
		// }

		return new JaCoCoReportWrapper(bundle, testWrapper.toString(), testStatistics.wasSuccessful());
	}

	private IBundleCoverage analyze(final ExecutionDataStore data) throws IOException {
		final CoverageBuilder builder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(data, builder);
		for (final File f : classfiles) {
			analyzer.analyzeAll(f);
		}
		printNoMatchWarning(builder.getNoMatchClasses());
		return builder.getBundle("JaCoCo Report");
	}

	private void printNoMatchWarning(final Collection<IClassCoverage> nomatch) {
		if (!nomatch.isEmpty()) {
			Log.err(this, "Some classes do not match with execution data.");
			Log.err(this, "For report generation, the same class files must be used as at runtime.");
			for (final IClassCoverage c : nomatch) {
				Log.err(this, "Execution data for class %s does not match.", c.getName());
			}
		}
	}

//	@SuppressWarnings("unused")
//	private void writeReports(final IBundleCoverage bundle, final ExecFileLoader loader) throws IOException {
//		Log.out(this, "Analyzing %s classes.%n", Integer.valueOf(bundle.getClassCounter().getTotalCount()));
//		final IReportVisitor visitor = createReportVisitor();
//		visitor.visitInfo(loader.getSessionInfoStore().getInfos(), loader.getExecutionDataStore().getContents());
//		visitor.visitBundle(bundle, getSourceLocator());
//		visitor.visitEnd();
//	}
//
//	private IReportVisitor createReportVisitor() throws IOException, IOException {
//		final List<IReportVisitor> visitors = new ArrayList<IReportVisitor>();
//
//		// if (xml != null) {
//		// final XMLFormatter formatter = new XMLFormatter();
//		// visitors.add(formatter.createVisitor(new
//		// FileOutputStream("test.xml")));
//		// }
//		//
//		// if (csv != null) {
//		// final CSVFormatter formatter = new CSVFormatter();
//		// visitors.add(formatter.createVisitor(new
//		// FileOutputStream("test.csv")));
//		// }
//		//
//		// if (html != null) {
//		final HTMLFormatter formatter = new HTMLFormatter();
//		visitors.add(formatter.createVisitor(new FileMultiReportOutput(new File("test_html"))));
//		// }
//
//		return new MultiReportVisitor(visitors);
//	}
//
//	private ISourceFileLocator getSourceLocator() {
//		final MultiSourceFileLocator multi = new MultiSourceFileLocator(4);
//		for (final File f : sourcefiles) {
//			multi.add(new DirectorySourceFileLocator(f, null, 4));
//		}
//		return multi;
//	}
	
	@Override
	public JaCoCoReportWrapper getResultFromCollectedItems() {
		// in the end, check if number of failing tests is correct (if given)
		if (knownFailingtests != null) {
			if (failingTestErrorOccurred) {
				Log.err(this, "Some test execution had the wrong result!");
				return ERROR_WRAPPER;
			}
			if (knownFailingtests.size() > failedTestCounter) {
				Log.err(this, "Not all specified failing tests have been executed! Expected: %d, Actual: %d", 
						knownFailingtests.size(), failedTestCounter);
				return ERROR_WRAPPER;
			}
		}
		return super.getResultFromCollectedItems();
	}

	@Override
	public boolean finalShutdown() {
		if (testRunner != null) {
			testRunner.finalShutdown();
		}
		return super.finalShutdown();
	}

}
