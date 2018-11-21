/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.io.File;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Simon
 *
 */
public class SingleLinkedBufferedArrayQueueTest {

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

	private File outputDir = new File("target" + File.separator + "bufferedArrayQueueTest");
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedArrayQueueFileStringInt() throws Exception {
		SingleLinkedBufferedArrayQueue<Integer> queue = new SingleLinkedBufferedArrayQueue<Integer>(outputDir, "test", 5);
		
		for (int i = 0; i < 50; ++i) {
			queue.add(i);
		}
		
		Iterator<Integer> iterator = queue.iterator();
		
		int i = 0;
		while (iterator.hasNext()) {
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedArrayQueueFileStringInt2() throws Exception {
		SingleLinkedBufferedArrayQueue<Integer> queue = new SingleLinkedBufferedArrayQueue<Integer>(outputDir, "test2", 5);
		
		for (int i = 0; i < 50; ++i) {
			queue.add(i);
		}
		
		queue.clear(23);
		
		Iterator<Integer> iterator = queue.iterator();
		
		int i = 23;
		while (iterator.hasNext()) {
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedArrayQueueFileStringInt3() throws Exception {
		SingleLinkedBufferedArrayQueue<int[]> queue = new SingleLinkedBufferedArrayQueue<int[]>(outputDir, "test2", 5);
		
		for (int i = 0; i < 50; ++i) {
			queue.add(new int[] {i});
		}
		
		queue.clear(23);
		
		Iterator<int[]> iterator = queue.iterator();
		
		int i = 23;
		while (iterator.hasNext()) {
			Assert.assertEquals(i++, iterator.next()[0]);
		}
		
	}

}
