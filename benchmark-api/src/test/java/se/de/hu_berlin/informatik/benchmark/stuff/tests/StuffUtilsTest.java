/**
 * 
 */
package se.de.hu_berlin.informatik.benchmark.stuff.tests;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;

import se.de.hu_berlin.informatik.benchmark.stuff.StuffUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author SimHigh
 *
 */
public class StuffUtilsTest extends TestSettings {

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
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	
	private static <T extends Comparable<T>> boolean bucketsAreEqual(List<T>[] buckets1, List<T>[] buckets2) {
		if (buckets1.length != buckets2.length) {
			return false;
		}
		for (int i = 0; i < buckets1.length; ++i) {
			if (buckets1[i].size() != buckets2[i].size()) {
				return false;
			}
			Iterator<T> it1 = buckets1[i].iterator();
			Iterator<T> it2 = buckets2[i].iterator();
			while (it1.hasNext()) {
				if (it1.next().compareTo(it2.next()) != 0) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.benchmark.stuff.StuffUtils#drawFromArrayIntoNBuckets(T[], int, long)}.
	 */
	@Test
	public void testDrawFromArrayIntoNBuckets() throws Exception {
		String[] array = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13" };
		
		List<String>[] buckets1 = StuffUtils.drawFromArrayIntoNBuckets(array, 3, 123456789L);
		List<String>[] buckets2 = StuffUtils.drawFromArrayIntoNBuckets(array, 3, 123456789L);
		
//		for (List<String> bucket : buckets) {
//			Log.out(this, bucket.toString());
//		}
		
		assertTrue(bucketsAreEqual(buckets1, buckets2));
		
		List<String>[] buckets3 = StuffUtils.drawFromArrayIntoNBuckets(array, 3);
		
		assertFalse(bucketsAreEqual(buckets1, buckets3));
	}

}
