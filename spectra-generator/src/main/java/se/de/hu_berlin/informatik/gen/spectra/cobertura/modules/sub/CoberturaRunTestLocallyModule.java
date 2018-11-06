/**
 * 
 */
package se.de.hu_berlin.informatik.gen.spectra.cobertura.modules.sub;

import java.nio.file.Path;
import java.util.Map;

import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunTestLocallyModule;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.LockableProjectData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.TouchCollector;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;
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
public class CoberturaRunTestLocallyModule extends AbstractRunTestLocallyModule<ProjectData> {

	private Map<Class<?>, Integer> registeredClasses;

	public CoberturaRunTestLocallyModule(final Path dataFile, final String testOutput, final boolean fullSpectra, 
			final boolean debugOutput, final Long timeout, final int repeatCount, ClassLoader cl, 
			Map<Class<?>, Integer> registeredClasses) {
		super(testOutput, debugOutput, timeout, repeatCount, cl);
		this.registeredClasses = registeredClasses;
	}
	
	@Override
	public Pair<TestStatistics, ProjectData> getResultAfterTest(TestWrapper testWrapper, TestStatistics testResult) {
		ProjectData projectData = new LockableProjectData();
		TouchCollector.applyTouchesOnProjectData(projectData, false);
		if (testResult.couldBeFinished()) {
			return new Pair<>(testResult, projectData);
		} else {
			return new Pair<>(testResult, null);
		}
	}

	@Override
	public boolean prepareBeforeRunningTest() {
		//sadly, we have to check if the coverage data has properly been reset...
		boolean isResetted = false;
		int maxTryCount = 3;
		int tryCount = 0;
		LockableProjectData projectData2 = null;
		while (!isResetted && tryCount < maxTryCount) {
			++tryCount;
			projectData2 = new LockableProjectData();
			TouchCollector.resetTouchesOnProjectData(projectData2);
//			LockableProjectData.resetLines(projectData2);
			if (!LockableProjectData.containsCoveredLines(projectData2)) {
				isResetted = true;
			}
		}
		return isResetted;
	}

}
