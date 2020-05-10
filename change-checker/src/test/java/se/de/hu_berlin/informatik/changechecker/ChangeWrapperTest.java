package se.de.hu_berlin.informatik.changechecker;

import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author Simon
 */
public class ChangeWrapperTest extends TestSettings {
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
     * Test method for {@link se.de.hu_berlin.informatik.changechecker.ChangeWrapper#storeChanges(java.util.Map, java.nio.file.Path)}.
     */
    @Test
    public void testStoreAndReadChanges() {
        List<ChangeWrapper> list = ChangeCheckerUtils
                .checkForChanges(new File(getStdResourcesDir() + File.separator + "MustBeReachingVariableDef.java"),
                        new File(getStdResourcesDir() + File.separator + "MustBeReachingVariableDef_changed.java"),
                        false, false);
        Map<String, List<ChangeWrapper>> map = new HashMap<>();
        map.put("MustBeReachingVariableDef", list);

        Path dest = Paths.get(getStdTestDir(), "test.out");
        ChangeWrapper.storeChanges(map, dest);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "test.out")));

        Map<String, List<ChangeWrapper>> map2 = ChangeWrapper.readChangesFromFile(dest);
        Path dest2 = Paths.get(getStdTestDir(), "test2.out");
        ChangeWrapper.storeChanges(map2, dest2);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "test2.out")));

        Map<String, List<ChangeWrapper>> map3 = ChangeWrapper.readChangesFromFile(dest2);
        Path dest3 = Paths.get(getStdTestDir(), "test3.out");
        ChangeWrapper.storeChanges(map3, dest3);
        assertTrue(Files.exists(Paths.get(getStdTestDir(), "test3.out")));
    }

}
