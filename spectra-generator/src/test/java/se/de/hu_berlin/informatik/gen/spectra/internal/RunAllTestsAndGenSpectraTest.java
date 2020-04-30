/**
 *
 */
package se.de.hu_berlin.informatik.gen.spectra.internal;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerationFactory.Strategy;
import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerator;
import se.de.hu_berlin.informatik.gen.spectra.spectra.TestProject;
import se.de.hu_berlin.informatik.gen.spectra.spectra.TestProjects;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;


/**
 * @author Simon
 *
 */
public class RunAllTestsAndGenSpectraTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    private static final String extraTestOutput = "target" + File.separator + "testoutputTraceCobertura";

    /**
     * Test method for {@link se.de.hu_berlin.informatik.gen.spectra.internal.RunAllTestsAndGenSpectra#main(java.lang.String[])}.
     */
//	@Test
    public void testMain() throws Exception {
        FileUtils.copyFileOrDir(Paths.get("src", "test", "resources", "Lang10b", "cobertura_original.ser").toFile(),
                Paths.get("src", "test", "resources", "Lang10b", "cobertura.ser").toFile(), StandardCopyOption.REPLACE_EXISTING);
        TestProject project = new TestProjects.Lang10b();
        String args[] = getArgs(Strategy.TRACE_COBERTURA, null,
                project.getProjectMainDir(), project.getSrcDir(), Paths.get(project.getBinTestDir()), project.getTestCP(),
                extraTestOutput + File.separator + "LangTestMain", Paths.get(project.getProjectMainDir(), "instrumented"),
                null, Paths.get("src", "test", "resources", "Lang10b", "lang10testsSmall.txt").toString(), null, true, false, false, 5000000L, 0,
                0, null, project.getFailingTests(), project.getBinDir());

        RunAllTestsAndGenSpectra.main(args);
    }

    private static String[] getArgs(Strategy strategy, String[] specificArgs, String projectDirOptionValue, String sourceDirOptionValue, final Path testClassDir,
                                    String testClassPath, final String outputDir, final Path instrumentedDir, String testClassList,
                                    String testList, final String javaHome, boolean useFullSpectra, boolean useSeparateJVM, boolean useJava7,
                                    Long timeout, int testRepeatCount, int maxErrors, Integer agentPort, List<String> failingtests, String... pathsToBinaries) {
        //build arguments for the "real" application (running the tests...)
        String[] newArgs = {
                RunAllTestsAndGenSpectra.CmdOptions.PROJECT_DIR.asArg(), projectDirOptionValue,
                RunAllTestsAndGenSpectra.CmdOptions.SOURCE_DIR.asArg(), sourceDirOptionValue,
                RunAllTestsAndGenSpectra.CmdOptions.OUTPUT.asArg(), Paths.get(outputDir).toAbsolutePath().toString(),
                RunAllTestsAndGenSpectra.CmdOptions.TEST_CLASS_DIR.asArg(), testClassDir.toAbsolutePath().toString(),
                RunAllTestsAndGenSpectra.CmdOptions.ORIGINAL_CLASSES_DIRS.asArg()};

        newArgs = Misc.joinArrays(newArgs, pathsToBinaries);

        if (javaHome != null) {
            newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.JAVA_HOME_DIR.asArg(), javaHome);
        }

        newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.STRATEGY.asArg(), strategy.toString());

        if (specificArgs != null) {
            newArgs = Misc.joinArrays(newArgs, specificArgs);
        }

        newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.INSTRUMENTED_DIR.asArg(), instrumentedDir.toAbsolutePath().toString());

        if (testClassList != null) {
            newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.TEST_CLASS_LIST.asArg(), testClassList);
        } else if (testList != null) {
            newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.TEST_LIST.asArg(), testList);
        } else {
            Log.abort(AbstractSpectraGenerator.class, "No test (class) list options given.");
        }

        if (testClassPath != null) {
            newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.TEST_CLASS_PATH.asArg(), testClassPath);
        }

        if (useFullSpectra) {
            newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.FULL_SPECTRA.asArg());
        }

        if (useJava7) {
            newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.JAVA7.asArg());
        }

        if (useSeparateJVM) {
            newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.SEPARATE_JVM.asArg());
        }

        if (timeout != null) {
            newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.TIMEOUT.asArg(), String.valueOf(timeout));
        }

        if (testRepeatCount > 1) {
            newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.REPEAT_TESTS.asArg(), String.valueOf(testRepeatCount));
        }

        if (maxErrors != 0) {
            newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.MAX_ERRORS.asArg(), String.valueOf(maxErrors));
        }

        if (agentPort != null) {
            newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.AGENT_PORT.asArg(), String.valueOf(agentPort.intValue()));
        }

        if (failingtests != null) {
            newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.FAILING_TESTS.asArg());
            for (String failingTest : failingtests) {
                newArgs = Misc.addToArrayAndReturnResult(newArgs, failingTest);
            }
        }
        return newArgs;
    }

}
