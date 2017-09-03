/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.jacoco.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.jacoco.core.tools.ExecFileLoader;

import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.SerializableExecFileLoader;
import se.de.hu_berlin.informatik.sbfl.spectra.modules.AbstractTestRunInNewJVMModuleWithJava7Runner;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

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
public class JaCoCoTestRunInNewJVMModuleWithJava7Runner extends AbstractTestRunInNewJVMModuleWithJava7Runner<SerializableExecFileLoader> {
	
	private File dataFile;

	public JaCoCoTestRunInNewJVMModuleWithJava7Runner(final String testOutput, 
			final boolean debugOutput, final Long timeout, final int repeatCount, 
			String instrumentedClassPath, final Path dataFile, final String javaHome, File projectDir, String... properties) {
		super(testOutput, debugOutput, timeout, repeatCount, instrumentedClassPath, 
				dataFile, javaHome, projectDir, (String[])properties);
		this.dataFile = dataFile.toFile();
	}
	
	@Override
	public boolean prepareBeforeRunningTest() {
		try {
			new ExecFileLoader().save(dataFile, false);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public SerializableExecFileLoader getDataForExecutedTest() {
		if (dataFile.exists()) {
			ExecFileLoader loader = new ExecFileLoader();
			// get execution data
			try {
				loader.load(dataFile);
				return new SerializableExecFileLoader(loader);
			} catch (IOException e) {
				return null;
			}
		} else {
			Log.err(this, "JaCoCo data file does not exist: %s", dataFile);
			return null;
		}
	}

}
