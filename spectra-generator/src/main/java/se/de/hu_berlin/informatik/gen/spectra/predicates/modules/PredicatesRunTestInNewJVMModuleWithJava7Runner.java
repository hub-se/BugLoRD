package se.de.hu_berlin.informatik.gen.spectra.predicates.modules;

import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunTestInNewJVMModuleWithJava7RunnerAndServer;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.java7.testrunner.UnitTestRunnerNoServer;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;

import java.io.File;
import java.nio.file.Path;

public class PredicatesRunTestInNewJVMModuleWithJava7Runner extends AbstractRunTestInNewJVMModuleWithJava7RunnerAndServer<ProjectData> {

    public PredicatesRunTestInNewJVMModuleWithJava7Runner(String testOutput, boolean debugOutput, Long timeout, int repeatCount, String instrumentedClassPath, Path dataFile, String javaHome, File projectDir, String[] customJvmArgs, String... properties) {
        super(testOutput, debugOutput, timeout, repeatCount, instrumentedClassPath, dataFile, javaHome, projectDir, customJvmArgs, properties);
    }

    @Override
    public boolean prepareBeforeRunningTest() {
        return true;
    }

    @Override //fake second part
    public Pair<TestStatistics, ProjectData> getResultAfterTest(TestWrapper testWrapper, int executionResult) {
        TestStatistics statistics = new TestStatistics();
        ProjectData projectData = new ProjectData();
        //see if the test was executed and finished execution normally
        if (executionResult == UnitTestRunnerNoServer.TEST_SUCCESSFUL ||
                executionResult == UnitTestRunnerNoServer.TEST_FAILED) {
            statistics.addStatisticsElement(StatisticsData.COULD_BE_FINISHED, 1);
            statistics.addStatisticsElement(StatisticsData.IS_SUCCESSFUL, executionResult == UnitTestRunnerNoServer.TEST_SUCCESSFUL);
        } else if (executionResult == UnitTestRunnerNoServer.TEST_TIMEOUT) {
            statistics.addStatisticsElement(StatisticsData.TIMEOUT_OCCURRED, 1);
            listener.resetListener();
        } else if (executionResult == UnitTestRunnerNoServer.TEST_EXCEPTION) {
            statistics.addStatisticsElement(StatisticsData.EXCEPTION_OCCURRED, 1);
            listener.resetListener();
        } else {
            listener.resetListener();
        }

        return new Pair<>(statistics, projectData);
    }
}
