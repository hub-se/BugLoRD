/**
 *
 */
package se.de.hu_berlin.informatik.gen.spectra.spectra;

import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;
import se.de.hu_berlin.informatik.gen.spectra.main.TraceCoberturaSpectraGenerator;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.Node.NodeType;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Simon
 *
 */
public class TraceCoberturaToSpectraTest extends TestSettings {

    private static final String GC = "-XX:+UseG1GC";

	private static final String MAX_SMALL_HEAP = "-Xmx1g";

	private static final String MAX_HEAP = "-Xmx2g";

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
//		FileUtils.delete(Paths.get(extraTestOutput));
    }

    /**
     */
    @After
    public void tearDown() {
//		FileUtils.delete(Paths.get(extraTestOutput));
    }

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private static final String extraTestOutput = "target" + File.separator + "testoutputTraceCobertura";

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
        long startTime = new Date().getTime();
        new TraceCoberturaSpectraGenerator.Builder()
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
                .setCustomJvmArgs(new String[] {MAX_HEAP, GC})
                .setCustomSmallJvmArgs(new String[] {MAX_SMALL_HEAP, GC})
                .run();
        long endTime = new Date().getTime();

        System.out.println("Execution time: " + Misc.getFormattedTimerString(endTime - startTime));

        Path spectraZipFile = Paths.get(extraTestOutput, outputDirName, "spectraCompressed.zip");
        if (successful) {
            assertTrue(Files.exists(spectraZipFile));
            checkTraceSpectra(spectraZipFile);
        } else {
            assertFalse(Files.exists(spectraZipFile));
        }
    }

    private void checkTraceSpectra(Path spectraZipFile) {
        boolean debug = false;
        ISpectra<SourceCodeBlock, ?> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
        assertNotNull(spectra);
        // iterate over all test cases
        for (ITrace<SourceCodeBlock> test : spectra.getTraces()) {
            if (debug) {
                // iterate over execution traces
                for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
                    System.out.println(test.getIdentifier() + " -> eTrace:");
                    Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(spectra.getIndexer());
                    while (nodeIdIterator.hasNext()) {
                        int nodeIndex = nodeIdIterator.next();
                        System.out.println(nodeIndex + ": " + spectra.getNode(nodeIndex).getIdentifier());
                    }
                }
            }
            Set<Integer> involvedInExecutionTraces = new HashSet<>();
            // iterate over execution traces
            for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
                // iterate over all nodes using the indexer
                Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(spectra.getIndexer());
                while (nodeIdIterator.hasNext()) {
                    Integer nodeId = nodeIdIterator.next();
                    // check if all nodes in the execution trace are contained in the test case
                    assertTrue(test.getInvolvedNodes().contains(nodeId));
                    involvedInExecutionTraces.add(nodeId);
                }
            }

            for (Integer nodeIndex : test.getInvolvedNodes()) {
                if (debug) {
                    if (!involvedInExecutionTraces.contains(nodeIndex)) {
                        System.out.println("test " + test.getIdentifier() + ": node ID " + nodeIndex +
                                " was not found in execution trace: " + spectra.getNode(nodeIndex).getIdentifier());
                    }
                } else {
                    // check if all nodes in the test case are contained in the execution trace
                    assertTrue("test " + test.getIdentifier() + ": node ID " + nodeIndex +
                                    " was not found in execution trace: " + spectra.getNode(nodeIndex).getIdentifier(),
                            involvedInExecutionTraces.contains(nodeIndex));
                }
            }

            for (int subTraceId = 0; subTraceId < spectra.getIndexer().getNodeIdSequences().size(); ++subTraceId) {
                int special_counter = 0;
                int special_counter_at_pos = 0;

                boolean isFirst = true;
                Iterator<Integer> nodeIdIterator = spectra.getIndexer().getNodeIdSequenceIterator(subTraceId);
                while (nodeIdIterator.hasNext()) {
                    int nodeId = nodeIdIterator.next();
                    INode<SourceCodeBlock> node = spectra.getNode(nodeId);
                    if (!node.getIdentifier().getNodeType().equals(NodeType.NORMAL)) {
                        ++special_counter;
                        if (isFirst || !nodeIdIterator.hasNext()) {
                            ++special_counter_at_pos;
                        }
                    }
                    isFirst = false;
                }
                if (special_counter > 2 || special_counter != special_counter_at_pos) {
                    nodeIdIterator = spectra.getIndexer().getNodeIdSequenceIterator(subTraceId);
                    while (nodeIdIterator.hasNext()) {
                        int nodeIndex = nodeIdIterator.next();
                        System.out.println(nodeIndex + ": " + spectra.getNode(nodeIndex).getIdentifier());
                    }
                }
                // check if all sub traces contain at most 2 special nodes (branch/switch/jump)
                assertTrue("special node count: " + special_counter, special_counter <= 2);

                // check if all sub traces contain at most 2 special nodes (branch/switch/jump)
                assertEquals("special nodes not at the start or the end.", special_counter, special_counter_at_pos);
            }
        }
    }

    private void testOnProjectWithTestClassList(TestProject project, String outputDirName,
                                                long timeout, int testrepeatCount, boolean fullSpectra,
                                                boolean separateJVM, boolean useJava7, boolean successful, String testClassListPath) {
        long startTime = new Date().getTime();
        new TraceCoberturaSpectraGenerator.Builder()
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
                .setCustomJvmArgs(new String[] {MAX_HEAP, GC})
                .setCustomSmallJvmArgs(new String[] {MAX_SMALL_HEAP, GC})
                .run();
        long endTime = new Date().getTime();

        System.out.println("Execution time: " + Misc.getFormattedTimerString(endTime - startTime));

        Path spectraZipFile = Paths.get(extraTestOutput, outputDirName, "spectraCompressed.zip");
        if (successful) {
            assertTrue(Files.exists(spectraZipFile));
            checkTraceSpectra(spectraZipFile);
        } else {
            assertFalse(Files.exists(spectraZipFile));
        }
    }

    private void testOnProjectWithTestList(TestProject project, String outputDirName,
                                           long timeout, int testrepeatCount, boolean fullSpectra,
                                           boolean separateJVM, boolean useJava7, boolean successful, String testListPath) {
        Path spectraZipFile = Paths.get(extraTestOutput, outputDirName, "spectraCompressed.zip");
        FileUtils.delete(spectraZipFile);
        long startTime = new Date().getTime();
        new TraceCoberturaSpectraGenerator.Builder()
                .setProjectDir(project.getProjectMainDir())
                .setSourceDir(project.getSrcDir())
                .setTestClassDir(project.getBinTestDir())
                .setTestClassPath(project.getTestCP())
                .setPathsToBinaries(project.getBinTestDir(), project.getBinDir())
                .setOutputDir(extraTestOutput + File.separator + outputDirName)
//		.setOutputDir(Paths.get("src","test","resources","Lang10b").toString())
                .setTestList(testListPath)
                .setFailingTests(project.getFailingTests())
                .useFullSpectra(fullSpectra)
                .useSeparateJVM(separateJVM)
                .useJava7only(useJava7)
                .setTimeout(timeout)
                .setTestRepeatCount(testrepeatCount)
                .setCustomJvmArgs(new String[] {MAX_HEAP, GC})
                .setCustomSmallJvmArgs(new String[] {MAX_SMALL_HEAP, GC})
                .run();
        long endTime = new Date().getTime();

        System.out.println("Execution time: " + Misc.getFormattedTimerString(endTime - startTime));

        if (successful) {
            assertTrue(Files.exists(spectraZipFile));
            checkTraceSpectra(spectraZipFile);
        } else {
            assertFalse(Files.exists(spectraZipFile));
        }
    }

    private void testOnProjectWithFailedtests(TestProject project, String outputDirName,
                                              long timeout, int testrepeatCount, boolean fullSpectra,
                                              boolean separateJVM, boolean useJava7, boolean successful, List<String> failedtests) {
        Path spectraZipFile = Paths.get(extraTestOutput, outputDirName, "spectraCompressed.zip");
        FileUtils.delete(spectraZipFile);
        long startTime = new Date().getTime();
        new TraceCoberturaSpectraGenerator.Builder()
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
                .setCustomJvmArgs(new String[] {MAX_HEAP, GC})
                .setCustomSmallJvmArgs(new String[] {MAX_SMALL_HEAP, GC})
                .run();
        long endTime = new Date().getTime();

        System.out.println("Execution time: " + Misc.getFormattedTimerString(endTime - startTime));

        if (successful) {
            assertTrue(Files.exists(spectraZipFile));
            checkTraceSpectra(spectraZipFile);
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
        testNormalExecution(new TestProjects.Mockito12b(), "reportMockito12b", true);
    }

    /**
     * Test method for .
     */
//	@Test
    public void testGenerateRankingForClosure() {
        testNormalExecution(new TestProjects.Closure101b(), "reportClosure101b", true);
    }

    /**
     * Test method for .
     */
//	@Test
    public void testGenerateRankingForLang8() {
        testNormalExecution(new TestProjects.Lang8b(), "reportLang8b", true);
    }

    /**
     * Test method for .
     */
//	@Test
    public void testGenerateRankingForLang10() {
        testNormalExecution(new TestProjects.Lang10b(), "reportLang10b", true);
    }

    //	@Test
    public void testGenerateRankingForLang10TestList() {
        testOnProjectWithTestList(new TestProjects.Lang10b(), "reportLang10bTestList",
                10000L, 1, false, false, false, true, "lang10tests.txt");
    }

    @Test
    public void testGenerateRankingForLang10TestListSmall() {
        // org.apache.commons.lang3.time.FastDateParser, counter ID 166, line 399
        testOnProjectWithTestList(new TestProjects.Lang10b(), "reportLang10bTestListSmall",
                10000L, 1, false, false, false, true, "lang10testsSmall.txt");
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
    public void testGenerateRankingForCoberturaTestProjectTestListThreads() {
        testOnProjectWithTestList(new TestProjects.CoberturaTestProject(), "reportCoberturaTestProjectTestListThreads",
                10L, 1, true, false, false, true, "all_testsSimpleThreads.txt");
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
