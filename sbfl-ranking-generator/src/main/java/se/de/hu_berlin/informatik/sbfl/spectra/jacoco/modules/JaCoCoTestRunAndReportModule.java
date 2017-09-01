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
import java.util.List;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.jacoco.core.tools.ExecFileLoader;

import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.junittestutils.data.TestWrapper;
import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.SerializableExecFileLoader;
import se.de.hu_berlin.informatik.sbfl.spectra.modules.AbstractTestRunAndReportModule;
import se.de.hu_berlin.informatik.sbfl.spectra.modules.AbstractTestRunInNewJVMModule;
import se.de.hu_berlin.informatik.stardust.provider.jacoco.JaCoCoReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class JaCoCoTestRunAndReportModule extends AbstractTestRunAndReportModule<SerializableExecFileLoader, JaCoCoReportWrapper> {

	final public static JaCoCoReportWrapper ERROR_WRAPPER = new JaCoCoReportWrapper(null, null, false);

	final private JaCoCoTestRunInNewJVMModule testRunnerNewJVM;

	// location of Java class files
	private List<File> classfiles = new ArrayList<File>();

	final private int port;

	public JaCoCoTestRunAndReportModule(final Path dataFile, final String testOutput, File projectDir, final String srcDir, String[] originalClasses, int port,
			final boolean debugOutput, Long timeout, final int repeatCount, String instrumentedClassPath,
			final String javaHome, boolean useSeparateJVMalways, String[] failingtests,
			final StatisticsCollector<StatisticsData> statisticsContainer, ClassLoader cl) {
		super(testOutput, debugOutput, timeout, repeatCount, useSeparateJVMalways, failingtests, statisticsContainer, cl);
		
//		this.sourcefiles.add(new File(srcDir));
		for (String classDirFile : originalClasses) {
			this.classfiles.add(new File(classDirFile));
		}

		this.port = port;

		this.testRunnerNewJVM = new JaCoCoTestRunInNewJVMModule(testOutput, debugOutput, timeout, repeatCount,
				instrumentedClassPath, javaHome, projectDir);

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
	public AbstractTestRunInNewJVMModule<SerializableExecFileLoader> getTestRunInNewJVMModule() {
		return testRunnerNewJVM;
	}


	@Override
	public JaCoCoReportWrapper generateReport(TestWrapper testWrapper, TestStatistics testStatistics,
			SerializableExecFileLoader data) {
		if (data.getExecFileLoader() != null) {
			// generate the report
			IBundleCoverage bundle = null;
			try {
				bundle = analyze(data.getExecFileLoader().getExecutionDataStore());
			} catch (IOException e) {
				Log.abort(this, e, "Analysis failed.");
			}
			// try {
			// writeReports(bundle, execFileLoader);
			// } catch (IOException e) {
			// Log.abort(this, e, "Writing reports failed.");
			// }

			return new JaCoCoReportWrapper(bundle, testWrapper.toString(), testStatistics.wasSuccessful());
		} else {
			return null;
		}
	}


	@Override
	public boolean prepareBeforeRunningTest() {
		//no preparation needed
		return true;
	}


	@Override
	public SerializableExecFileLoader getCoverageDataAftertest() {
		ExecFileLoader loader;
		// get execution data
		try {
			loader = dump(port);
		} catch (IOException e) {
			loader = null;
		}
		return new SerializableExecFileLoader(loader);
	}

	@Override
	public JaCoCoReportWrapper getErrorReport() {
		return ERROR_WRAPPER;
	}


}
