/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;


/**
 * @author Simon
 *
 */
public class CachedMapTest extends TestSettings {

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

	@Test
	public void cachedMapTest() {
		Path output1 = Paths.get(getStdTestDir(), "cacheMap", "map.zip");
		Path output2 = Paths.get(getStdTestDir(), "cacheMap", "mapTarget.zip");
		FileUtils.delete(output1);
		FileUtils.delete(output2);
		
		CachedMap<int[]> map = new CachedIntArrayMap(output1, 5, "test", true);
		Map<Integer, int[]> checkMap = new HashMap<>();
		
		Random rand = new Random(12315415);
		
		map.put(17, new int[] {1,2,3});
		
		map.get(17);
		
		for (int i = 0; i < 1000; ++i) {
			int length = rand.nextInt(100);
			int[] array = new int[length];
			for (int j = 0; j < length; ++j) {
				array[j] = rand.nextInt();
			}
			int key = rand.nextInt();
			map.put(key, array);
			checkMap.put(key, array);
		}
		
		map.put(17, new int[] {1,2,3});
		map.remove(17);
		
		checkIfEqual(map, checkMap);
		
		map.moveMapContentsTo(output2);
		
		assertTrue(output2.toFile().exists());
		
		CachedMap<int[]> map2 = new CachedIntArrayMap(output2, 5, "test", true);
		
		checkIfEqual(map2, checkMap);
	}

	private void checkIfEqual(CachedMap<int[]> map, Map<Integer, int[]> checkMap) {
		assertEquals(checkMap.size(), map.size());
		
		for (Entry<Integer, int[]> entry : checkMap.entrySet()) {
			assertTrue(map.containsKey(entry.getKey()));
			
			assertArrayEquals("key: " + entry.getKey() + ", length: " + entry.getValue().length, entry.getValue(), map.get(entry.getKey()));
		}
	}
	
}
