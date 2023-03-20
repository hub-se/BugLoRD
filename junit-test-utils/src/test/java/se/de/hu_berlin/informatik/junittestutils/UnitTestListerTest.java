package se.de.hu_berlin.informatik.junittestutils;

import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.testlister.UnitTestLister;
import se.de.hu_berlin.informatik.junittestutils.testlister.UnitTestLister.CmdOptions;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Simon
 */
public class UnitTestListerTest extends TestSettings {

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

//    @Rule
//    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
//
//    @Rule
//    public final ExpectedException exception = ExpectedException.none();

    /**
     * Test method for {@link se.de.hu_berlin.informatik.junittestutils.testlister.UnitTestLister#main(java.lang.String[])}.
     */
    @Test
    public void testMain() {
//		Log.off();
        String[] args = {
                CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "test.in",
                CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "tests.out"};
        UnitTestLister.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "tests.out")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.junittestutils.testlister.UnitTestLister#main(java.lang.String[])}.
     */
    @Test
    public void testMainTestClassNotFound() {
//		Log.off();
        String[] args = {
                CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "testWrong.in",
                CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "testsWrong.out"};
        UnitTestLister.main(args);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "testsWrong.out")));
    }

    /**
     * Test method for {@link se.de.hu_berlin.informatik.junittestutils.testlister.UnitTestLister#getAllTestsFromTestClassList(java.nio.file.Path, java.lang.String)}.
     */
    @Test
    public void testGetAllTestsFromTestClassListPathString() {
        Path correctIn = Paths.get(getStdResourcesDir(), "test.in");
        List<TestWrapper> list = UnitTestLister.getAllTestsFromTestClassList(correctIn, (String) null);
        assertEquals(3, list.size());

        Path wrongIn = Paths.get(getStdResourcesDir(), "testWrong.in");
        List<TestWrapper> listWrong = UnitTestLister.getAllTestsFromTestClassList(wrongIn, (String) null);
        assertEquals(3, listWrong.size());
    }

}
