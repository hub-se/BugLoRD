/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.cobertura.modules;

import java.io.File;
import java.nio.file.Path;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
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
public class CoberturaTestRunInNewJVMModuleWithJava7Runner extends AbstractTestRunInNewJVMModuleWithJava7Runner<ProjectData> {
	
	private File dataFile;

	public CoberturaTestRunInNewJVMModuleWithJava7Runner(final String testOutput, 
			final boolean debugOutput, final Long timeout, final int repeatCount, 
			String instrumentedClassPath, final Path dataFile, final String javaHome, File projectDir) {
		super(testOutput, debugOutput, timeout, repeatCount, instrumentedClassPath, 
				dataFile, javaHome, projectDir, 
				"-Dnet.sourceforge.cobertura.datafile=" + dataFile.toAbsolutePath().toString());
		this.dataFile = dataFile.toFile();
	}
	
	@Override
	public boolean prepareBeforeRunningTest() {
		CoverageDataFileHandler.saveCoverageData(new ProjectData(), dataFile);
		return true;
	}

	@Override
	public ProjectData getDataForExecutedTest() {
		if (dataFile.exists()) {
			return CoverageDataFileHandler.loadCoverageData(dataFile);
		} else {
			Log.err(this, "Cobertura data file does not exist: %s", dataFile);
			return null;
		}
	}

}
