/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.modules;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;

import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.miscellaneous.SystemUtils;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;
import se.de.hu_berlin.informatik.utils.statistics.Statistics;

/**
 * Runs a single test by executing a system command, using the given java home directory.
 * The coverage data has to be stored in a file which is read after executing 
 * the system command successfully (returned 0).
 * 
 * <p>
 * if the test can't be run at all, this information is given in the returned
 * statistics, together with an error message.
 * 
 * @author Simon Heiden
 */
public abstract class AbstractTestRunWithCommandAndJavaVersion<T extends Serializable>
		extends AbstractProcessor<TestWrapper, Pair<TestStatistics, T>> {

	final private Path resultOutputFile;
	
	final private Path dataFilePath;
	private File projectDir;
	private String javaBinDir;
	private String javaHomeDir;
	private String javaJREDir;

	public AbstractTestRunWithCommandAndJavaVersion(Path resultOutputFile,
			Path dataFilePath, final String javaHome, File projectDir) {
		super();
		this.resultOutputFile = resultOutputFile;

		this.dataFilePath = dataFilePath;

		this.javaHomeDir = javaHome;
		this.javaBinDir = javaHome + File.separator + "bin";
		this.javaJREDir = javaHome + File.separator + "jre";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.
	 * Object)
	 */
	@Override
	public Pair<TestStatistics, T> processItem(final TestWrapper testWrapper,
			ProcessorSocket<TestWrapper, Pair<TestStatistics, T>> socket) {
		socket.forceTrack(testWrapper.toString());
		// Log.out(this, "Now processing: '%s'.", testWrapper);
		int result = -1;

		result = SystemUtils.executeCommandInJavaEnvironment(projectDir, 
				javaBinDir, javaHomeDir, javaJREDir, false, 
				getCommandAndArgs(testWrapper.getTestClassName(), testWrapper.getTestMethodName()));

		if (result != 0) {
//			Log.err(this, testWrapper + ": Running test in separate JVM failed.");
			TestStatistics statistics = new TestStatistics(testWrapper + ": Running test in separate JVM failed.");
			statistics.addStatisticsElement(StatisticsData.COVERAGE_GENERATION_FAILED, 1);
			return new Pair<>(statistics, null);
		}

		T data = getFromFile(dataFilePath);

		// Log.out(this, "returning valid item...");
		return new Pair<>(new TestStatistics(Statistics.loadAndMergeFromCSV(StatisticsData.class, resultOutputFile)),
				data);
	}

	public abstract String[] getCommandAndArgs(String testClassName, String testMethodName);

	public abstract T getFromFile(Path dataFilePath);

}
