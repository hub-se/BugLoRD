package se.de.hu_berlin.informatik.gen.spectra.predicates.modules;

import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunTestLocallyModule;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.TouchCollector;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;

import java.nio.file.Path;
import java.util.Map;

public class PredicatesRunTestLocallyModule extends AbstractRunTestLocallyModule<ProjectData> {

    String testOutput;

    public PredicatesRunTestLocallyModule(final Path dataFile, final String testOutput, final boolean fullSpectra,
                                              final boolean debugOutput, final Long timeout, final int repeatCount, ClassLoader cl,
                                              Map<Class<?>, Integer> registeredClasses) {
        super(testOutput, debugOutput, timeout, repeatCount, cl);
        this.testOutput = testOutput;
    }

    @Override
    public Pair<TestStatistics, ProjectData> getResultAfterTest(TestWrapper testWrapper, TestStatistics testResult) {
        ProjectData projectData = new ProjectData();
        TouchCollector.applyTouchesOnProjectData(projectData);

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
        while (!isResetted && tryCount < maxTryCount) {
            ++tryCount;
            isResetted = TouchCollector.resetTouchesOnRegisteredClasses();
        }

        Output.resetTriggers();
        return isResetted;
    }

}
