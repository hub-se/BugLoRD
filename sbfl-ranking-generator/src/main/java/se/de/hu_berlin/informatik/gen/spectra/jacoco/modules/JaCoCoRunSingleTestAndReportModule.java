/**
 * 
 */
package se.de.hu_berlin.informatik.gen.spectra.jacoco.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jacoco.agent.AgentJar;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;

import se.de.hu_berlin.informatik.gen.spectra.jacoco.JaCoCoSpectraGenerationFactory;
import se.de.hu_berlin.informatik.gen.spectra.jacoco.modules.sub.JaCoCoRunTestInNewJVMModule;
import se.de.hu_berlin.informatik.gen.spectra.jacoco.modules.sub.JaCoCoRunTestInNewJVMModuleWithJava7Runner;
import se.de.hu_berlin.informatik.gen.spectra.jacoco.modules.sub.JaCoCoRunTestLocallyModule;
import se.de.hu_berlin.informatik.gen.spectra.main.JaCoCoSpectraGenerator;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunSingleTestAndReportModule;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunTestInNewJVMModule;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunTestInNewJVMModuleWithJava7Runner;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunTestLocallyModule;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.stardust.provider.jacoco.report.JaCoCoReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.miscellaneous.SimpleServerFramework;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class JaCoCoRunSingleTestAndReportModule extends AbstractRunSingleTestAndReportModule<SerializableExecFileLoader, JaCoCoReportWrapper> {

	final public static JaCoCoReportWrapper ERROR_WRAPPER = new JaCoCoReportWrapper(null, null, false);

	// location of Java class files
	private List<File> classfiles = new ArrayList<File>();

	private Path dataFile;
	private String testOutput;
	private File projectDir;
	private int port;
	private boolean debugOutput;
	private Long timeout;
	private int repeatCount;
	private String instrumentedClassPath;
	private String javaHome;
	private ClassLoader cl;
	private String java7RunnerJar;

	public JaCoCoRunSingleTestAndReportModule(final Path dataFile, final String testOutput, File projectDir, final String srcDir, String[] originalClasses, int port,
			final boolean debugOutput, Long timeout, final int repeatCount, String instrumentedClassPath,
			final String javaHome, final String java7RunnerJar, boolean useSeparateJVMalways, boolean alwaysUseJava7, int maxErrors, String[] failingtests,
			final StatisticsCollector<StatisticsData> statisticsContainer, ClassLoader cl) {
		super(testOutput, debugOutput, timeout, repeatCount, useSeparateJVMalways, alwaysUseJava7, 
				maxErrors, failingtests, statisticsContainer, cl);
		this.dataFile = dataFile;
		this.testOutput = testOutput;
		this.projectDir = projectDir;
		this.port = port;
		this.debugOutput = debugOutput;
		this.timeout = timeout;
		this.repeatCount = repeatCount;
		this.instrumentedClassPath = instrumentedClassPath;
		this.javaHome = javaHome;
		this.java7RunnerJar = java7RunnerJar;
		this.cl = cl;
		
//		this.sourcefiles.add(new File(srcDir));
		for (String classDirFile : originalClasses) {
			this.classfiles.add(new File(classDirFile));
		}

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

	@Override
	public JaCoCoReportWrapper getErrorReport() {
		return ERROR_WRAPPER;
	}

	@Override
	public AbstractRunTestLocallyModule<SerializableExecFileLoader> newTestRunLocallyModule() {
		return new JaCoCoRunTestLocallyModule(testOutput, 
				debugOutput, timeout, repeatCount, cl, port);
	}
	
	@Override
	public AbstractRunTestInNewJVMModule<SerializableExecFileLoader> newTestRunInNewJVMModule() {
		return new JaCoCoRunTestInNewJVMModule(testOutput, debugOutput, timeout, repeatCount,
				instrumentedClassPath + File.pathSeparator + new ClassPathParser().parseSystemClasspath().getClasspath(), 
				javaHome, projectDir, port+1);
	}

	@Override
	public AbstractRunTestInNewJVMModuleWithJava7Runner<SerializableExecFileLoader> newTestRunInNewJVMModuleWithJava7Runner() {
		int freePort = SimpleServerFramework.getFreePort(port+2);

		String testClassPath = instrumentedClassPath + File.pathSeparator;
		
		String[] properties;
		if (JaCoCoSpectraGenerationFactory.OFFLINE_INSTRUMENTATION) {
			properties = Misc.createArrayFromItems(
					"-Djacoco-agent.dumponexit=true", 
					"-Djacoco-agent.output=file",
					"-Djacoco-agent.destfile=" + dataFile.toAbsolutePath().toString(),
					"-Djacoco-agent.excludes=*",
					"-Djacoco-agent.port=" + freePort);
		} else {
			File jacocoAgentJar = null; 
			try {
				jacocoAgentJar = AgentJar.extractToTempLocation();
			} catch (IOException e) {
				Log.abort(JaCoCoSpectraGenerator.class, e, "Could not create JaCoCo agent jar file.");
			}
			
			testClassPath += jacocoAgentJar.getAbsolutePath() + File.pathSeparator;

			properties = Misc.createArrayFromItems(
					"-javaagent:" + jacocoAgentJar.getAbsolutePath() 
					+ "=dumponexit=true,"
					+ "output=file,"
					+ "destfile=" + dataFile.toAbsolutePath().toString() + ","
					+ "excludes=se.de.hu_berlin.informatik.*:org.junit.*,"
					+ "port=" + freePort);
		}
		
		//remove as much irrelevant classes as possible from class path TODO
//		ClassPathParser systemClasspath = new ClassPathParser().parseSystemClasspath();
//		systemClasspath.removeElementsOtherThan("java7-test-runner", "ant-", "junit-4.12");
		if (java7RunnerJar == null) {
			testClassPath += new ClassPathParser().parseSystemClasspath().getClasspath();
		} else {
			testClassPath += java7RunnerJar;
		}
		return new JaCoCoRunTestInNewJVMModuleWithJava7Runner(testOutput, 
				debugOutput, timeout, repeatCount, testClassPath,
				// + File.pathSeparator + systemClasspath.getClasspath(), 
				dataFile, javaHome, projectDir, (String[])properties);
	}



}
