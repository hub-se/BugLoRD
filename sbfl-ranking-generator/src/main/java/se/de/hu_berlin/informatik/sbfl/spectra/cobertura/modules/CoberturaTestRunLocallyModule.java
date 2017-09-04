/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.cobertura.modules;

import java.nio.file.Path;
import java.util.Map;

import net.sourceforge.cobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.sbfl.spectra.modules.AbstractTestRunLocallyModule;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.coverage.LockableProjectData;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.coverage.MyTouchCollector;
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
public class CoberturaTestRunLocallyModule extends AbstractTestRunLocallyModule<ProjectData> {

	private Map<Class<?>, Integer> registeredClasses;

	public CoberturaTestRunLocallyModule(final Path dataFile, final String testOutput, final boolean fullSpectra, 
			final boolean debugOutput, final Long timeout, final int repeatCount, ClassLoader cl, 
			Map<Class<?>, Integer> registeredClasses) {
		super(testOutput, debugOutput, timeout, repeatCount, cl);
		this.registeredClasses = registeredClasses;
	}
	
	@Override
	public Pair<TestStatistics, ProjectData> getResultAfterTest(TestWrapper testWrapper, TestStatistics testResult) {
		ProjectData projectData = new LockableProjectData();
		MyTouchCollector.applyTouchesOnProjectData2(registeredClasses, projectData);
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
			MyTouchCollector.resetTouchesOnProjectData2(registeredClasses, projectData2);
//			LockableProjectData.resetLines(projectData2);
			if (!LockableProjectData.containsCoveredLines(projectData2)) {
				isResetted = true;
			}
		}
		return isResetted;
	}

}
