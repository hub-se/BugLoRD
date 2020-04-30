/**
 *
 */
package se.de.hu_berlin.informatik.gen.spectra.spectra;

import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;
import se.de.hu_berlin.informatik.gen.spectra.main.JaCoCoSpectraGenerator;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Simon
 *
 */
public class JaCoCoToSpectraTest extends TestSettings {

    /**
     */
    @BeforeClass
    public static void setUpBeforeClass() {
    }

    /**
     */
    @AfterClass
    public static void tearDownAfterClass() {
    }

    /**
     */
    @Before
    public void setUp() {
        FileUtils.delete(Paths.get(extraTestOutput));
    }

    /**
     */
    @After
    public void tearDown() {
        FileUtils.delete(Paths.get(extraTestOutput));
    }

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private static final String extraTestOutput = "target" + File.separator + "testoutputJaCoCo";

    private void testNormalExecution(TestProject project, String outputDirName, boolean successful) {
        testOnProject(project, outputDirName, 10L, 1, false, false, false, successful);
    }

    private void testSepJVMExecution(TestProject project, String outputDirName, boolean successful) {
        testOnProject(project, outputDirName, 10L, 1, false, true, false, successful);
    }

    private void testSepJVMJava7Execution(TestProject project, String outputDirName, boolean successful) {
        testOnProject(project, outputDirName, 10L, 1, false, true, true, successful);
    }

    private void testTimeoutExecution(TestProject project, String outputDirName) {
        testOnProject(project, outputDirName, -1L, 1, false, false, false, false);
    }

    private void testOnProject(TestProject project, String outputDirName,
                               long timeout, int testrepeatCount, boolean fullSpectra,
                               boolean separateJVM, boolean useJava7, boolean successful) {
        new JaCoCoSpectraGenerator.Builder()
                .setProjectDir(project.getProjectMainDir())
                .setSourceDir(project.getSrcDir())
                .setTestClassDir(project.getBinTestDir())
                .setTestClassPath(project.getTestCP())
                .setPathsToBinaries(project.getBinDir())
                .setOutputDir(extraTestOutput + File.separator + outputDirName)
                .setTestClassList(project.getTestClassListPath())
                .setFailingTests(project.getFailingTests())
                .useFullSpectra(fullSpectra)
                .useSeparateJVM(separateJVM)
                .useJava7only(useJava7)
                .setTimeout(timeout)
                .setTestRepeatCount(testrepeatCount)
                .run();

        Path spectraZipFile = Paths.get(extraTestOutput, outputDirName, "spectraCompressed.zip");
        if (successful) {
            assertTrue(Files.exists(spectraZipFile));
        } else {
            assertFalse(Files.exists(spectraZipFile));
        }
    }

    private void testOnProjectWithTestClassList(TestProject project, String outputDirName,
                                                long timeout, int testrepeatCount, boolean fullSpectra,
                                                boolean separateJVM, boolean useJava7, boolean successful, String testClassListPath) {
        new JaCoCoSpectraGenerator.Builder()
                .setProjectDir(project.getProjectMainDir())
                .setSourceDir(project.getSrcDir())
                .setTestClassDir(project.getBinTestDir())
                .setTestClassPath(project.getTestCP())
                .setPathsToBinaries(project.getBinDir())
                .setOutputDir(extraTestOutput + File.separator + outputDirName)
                .setTestClassList(testClassListPath)
                .setFailingTests(project.getFailingTests())
                .useFullSpectra(fullSpectra)
                .useSeparateJVM(separateJVM)
                .useJava7only(useJava7)
                .setTimeout(timeout)
                .setTestRepeatCount(testrepeatCount)
                .run();

        Path spectraZipFile = Paths.get(extraTestOutput, outputDirName, "spectraCompressed.zip");
        if (successful) {
            assertTrue(Files.exists(spectraZipFile));
        } else {
            assertFalse(Files.exists(spectraZipFile));
        }
    }

    private void testOnProjectWithTestList(TestProject project, String outputDirName,
                                           long timeout, int testrepeatCount, boolean fullSpectra,
                                           boolean separateJVM, boolean useJava7, boolean successful, String testListPath) {
        new JaCoCoSpectraGenerator.Builder()
                .setProjectDir(project.getProjectMainDir())
                .setSourceDir(project.getSrcDir())
                .setTestClassDir(project.getBinTestDir())
                .setTestClassPath(project.getTestCP())
                .setPathsToBinaries(project.getBinDir())
                .setOutputDir(extraTestOutput + File.separator + outputDirName)
                .setTestList(testListPath)
                .setFailingTests(project.getFailingTests())
                .useFullSpectra(fullSpectra)
                .useSeparateJVM(separateJVM)
                .useJava7only(useJava7)
                .setTimeout(timeout)
                .setTestRepeatCount(testrepeatCount)
                .run();

        Path spectraZipFile = Paths.get(extraTestOutput, outputDirName, "spectraCompressed.zip");
        if (successful) {
            assertTrue(Files.exists(spectraZipFile));
        } else {
            assertFalse(Files.exists(spectraZipFile));
        }
    }

    private void testOnProjectWithFailedtests(TestProject project, String outputDirName,
                                              long timeout, int testrepeatCount, boolean fullSpectra,
                                              boolean separateJVM, boolean useJava7, boolean successful, List<String> failedtests) {
        new JaCoCoSpectraGenerator.Builder()
                .setProjectDir(project.getProjectMainDir())
                .setSourceDir(project.getSrcDir())
                .setTestClassDir(project.getBinTestDir())
                .setTestClassPath(project.getTestCP())
                .setPathsToBinaries(project.getBinDir())
                .setOutputDir(extraTestOutput + File.separator + outputDirName)
                .setTestClassList(project.getTestClassListPath())
                .setFailingTests(failedtests)
                .useFullSpectra(fullSpectra)
                .useSeparateJVM(separateJVM)
                .useJava7only(useJava7)
                .setTimeout(timeout)
                .setTestRepeatCount(testrepeatCount)
                .run();

        Path spectraZipFile = Paths.get(extraTestOutput, outputDirName, "spectraCompressed.zip");
        if (successful) {
            assertTrue(Files.exists(spectraZipFile));
        } else {
            assertFalse(Files.exists(spectraZipFile));
        }
    }


    /**
     * Test method for .
     */
//	@Test
    public void testGenerateRankingForTime() {
        testNormalExecution(new TestProjects.Time3b(), "reportTime3b", true);
    }

    /**
     * Test method for .
     */
//	@Test
    public void testGenerateRankingForMockito() {
        testNormalExecution(new TestProjects.Mockito12b(), "reportCoberturaMockito12b", true);
    }

    /**
     * Test method for .
     */
//	@Test
    public void testGenerateRankingForClosure() {
        testNormalExecution(new TestProjects.Closure101b(), "reportCoberturaClosure101b", true);
    }

    /**
     * Test method for .
     */
//	@Test
    public void testGenerateRankingForLang8() {
        testNormalExecution(new TestProjects.Lang8b(), "reportCoberturaLang8b", true);
    }

    /**
     * Test method for .
     */
    @Test
    public void testGenerateRankingForCoberturaTestProjectTestList() {
        testOnProjectWithTestList(new TestProjects.CoberturaTestProject(), "reportCoberturaTestProjectTestList",
                10L, 1, false, false, false, true, "all_testsSimple.txt");
    }

    /**
     * Test method for .
     */
    @Test
    public void testGenerateRankingForCoberturaTestProject() {
        testNormalExecution(new TestProjects.CoberturaTestProject(), "reportCoberturaTestProject", true);

        Path spectraZipFile = Paths.get(extraTestOutput, "reportCoberturaTestProject", "spectraCompressed.zip");
        ISpectra<SourceCodeBlock, ?> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
        assertFalse(spectra.getTraces().isEmpty());
        assertEquals(spectra.getTraces().size() - 1, spectra.getSuccessfulTraces().size());
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.gen.spectra.main.CoberturaSpectraGenerator#main(java.lang.String[])}.
     */
    @Test
    public void testMainRankingGenerationSeparateJVMForCoberturaTestProject() {
        testSepJVMExecution(new TestProjects.CoberturaTestProject(), "reportCoberturaTestProjectSepJVM", true);
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.gen.spectra.main.CoberturaSpectraGenerator#main(java.lang.String[])}.
     */
    @Test
    public void testMainRankingGenerationJava7ForCoberturaTestProject() {
        testSepJVMJava7Execution(new TestProjects.CoberturaTestProject(), "reportCoberturaTestProjectSepJVMJava7", true);
    }

    /**
     * Test method for .
     */
    @Test
    public void testGenerateRankingForCoberturaTestProjectWithWrongFailedTestCases() {
        ArrayList<String> failingTests = new ArrayList<>();
        failingTests.add("coberturatest.tests.SimpleProgramTest::testAdd");
        testOnProjectWithFailedtests(new TestProjects.CoberturaTestProject(), "reportCoberturaTestProjectWrongFailedtestCases",
                10L, 1, false, false, false, false, failingTests);
    }


    /**
     * Test method for .
     */
    @Test
    public void testGenerateRankingForCoberturaTestProjectWrongTestClass() {
        TestProjects.CoberturaTestProject project = new TestProjects.CoberturaTestProject();
        testOnProjectWithTestClassList(project, "reportCoberturaTestProjectWrongtestClass",
                10L, 1, false, false, false, true, project.getProjectMainDir() + File.separator + "wrongTestClassesSimple.txt");

        Path spectraZipFile = Paths.get(extraTestOutput, "reportCoberturaTestProjectWrongtestClass", "spectraCompressed.zip");

        ISpectra<SourceCodeBlock, ?> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
        assertFalse(spectra.getTraces().isEmpty());
    }

    /**
     * Test method for .
     */
    @Test
    public void testGenerateRankingForCoberturaTestProjectWithTimeOut() {
        testTimeoutExecution(new TestProjects.CoberturaTestProject(), "reportCoberturaTestProjectTimeOut");
    }

}
