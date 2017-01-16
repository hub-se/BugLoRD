/**
 * 
 */
package se.de.hu_berlin.informatik.changechecker;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class ChangeWrapperTest extends TestSettings {
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
		deleteTestOutputs();
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
		deleteTestOutputs();
	}
	
	@Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.changechecker.ChangeWrapper#storeChanges(java.util.Map, java.nio.file.Path)}.
	 */
	@Test
	public void testStoreAndReadChanges() throws Exception {
		List<ChangeWrapper> list = ChangeChecker
				.checkForChanges(new File(getStdResourcesDir() + File.separator + "MustBeReachingVariableDef.java"), 
						new File(getStdResourcesDir() + File.separator + "MustBeReachingVariableDef_changed.java"));
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
