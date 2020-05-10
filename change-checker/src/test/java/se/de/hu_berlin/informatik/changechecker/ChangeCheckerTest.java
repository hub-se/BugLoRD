package se.de.hu_berlin.informatik.changechecker;

import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

import java.io.File;

/**
 * @author Simon
 */
public class ChangeCheckerTest extends TestSettings {

    /**
     *
     */
    @BeforeClass
    public static void setUpBeforeClass() {
    }

    /**
     *
     */
    @AfterClass
    public static void tearDownAfterClass() {
        deleteTestOutputs();
    }

    /**
     *
     */
    @Before
    public void setUp() {
    }

    /**
     *
     */
    @After
    public void tearDown() {
        deleteTestOutputs();
    }

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    /**
     * Test method for {@link se.de.hu_berlin.informatik.changechecker.ChangeChecker#main(java.lang.String[])}.
     */
    @Test
    public void testMain() {
        String[] args = {
                "-l", getStdResourcesDir() + File.separator + "TestRunAndReportModule.java",
                "-r", getStdResourcesDir() + File.separator + "TestRunAndReportModule_changed.java"};
        ChangeChecker.main(args);
//		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tests.out")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.changechecker.ChangeChecker#main(java.lang.String[])}.
     */
    @Test
    public void testMain3() {
        String[] args = {
                "-l", getStdResourcesDir() + File.separator + "MinMaxCategoryRenderer.java",
                "-r", getStdResourcesDir() + File.separator + "MinMaxCategoryRenderer_fixed.java"};
        ChangeChecker.main(args);
//		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tests.out")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.changechecker.ChangeChecker#main(java.lang.String[])}.
     */
    @Test
    public void testMain4() {
        String[] args = {
                "-l", getStdResourcesDir() + File.separator + "FinalMockCandidateFilter.java",
                "-r", getStdResourcesDir() + File.separator + "FinalMockCandidateFilter_fixed.java"};
        ChangeChecker.main(args);
//		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tests.out")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.changechecker.ChangeChecker#main(java.lang.String[])}.
     */
    @Test
    public void testMainCompressed() {
        String[] args = {
                "-l", getStdResourcesDir() + File.separator + "TestRunAndReportModule.java",
                "-r", getStdResourcesDir() + File.separator + "TestRunAndReportModule_changed.java",
                "-c"};
        ChangeChecker.main(args);
//		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tests.out")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.changechecker.ChangeChecker#main(java.lang.String[])}.
     */
    @Test
    public void testMain2() {
        String[] args = {
                "-l", getStdResourcesDir() + File.separator + "MustBeReachingVariableDef.java",
                "-r", getStdResourcesDir() + File.separator + "MustBeReachingVariableDef_changed.java"};
        ChangeChecker.main(args);
//		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tests.out")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.changechecker.ChangeChecker#main(java.lang.String[])}.
     */
    @Test
    public void testMain2Compressed() {
        String[] args = {
                "-l", getStdResourcesDir() + File.separator + "MustBeReachingVariableDef.java",
                "-r", getStdResourcesDir() + File.separator + "MustBeReachingVariableDef_changed.java",
                "-c"};
        ChangeChecker.main(args);
//		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tests.out")));
    }

}
