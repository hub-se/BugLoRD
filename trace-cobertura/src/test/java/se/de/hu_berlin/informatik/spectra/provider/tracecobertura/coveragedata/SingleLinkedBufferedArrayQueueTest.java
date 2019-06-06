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

	/*
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/*
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/*
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/*
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	private File outputDir = new File("target" + File.separator + "bufferedArrayQueueTest");
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedArrayQueueFileStringInt() throws Exception {
		BufferedArrayQueue<Integer> queue = new BufferedArrayQueue<Integer>(outputDir, "test", 5);
		
		for (int i = 0; i < 50; ++i) {
			queue.add(i);
		}
		queue.sleep();
		
//		Thread.sleep(5000);
		Iterator<Integer> iterator = queue.iterator();
		
		int i = 0;
		while (iterator.hasNext()) {
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		
		queue.clear();
	}
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedArrayQueueFileStringInt2() throws Exception {
		BufferedArrayQueue<Integer> queue = new BufferedArrayQueue<Integer>(outputDir, "test2", 5);
		
		for (int i = 0; i < 50; ++i) {
			queue.add(i);
		}
		queue.sleep();
		
//		Assert.assertEquals(2, queue.get(2).intValue());
		Assert.assertEquals(30, queue.get(30).intValue());
		
		queue.clear(10);
		queue.sleep();
		
		queue.clear(13);
		queue.sleep();
		
		Assert.assertEquals(23, queue.get(0).intValue());
		Assert.assertEquals(25, queue.get(2).intValue());
		Assert.assertEquals(40, queue.get(17).intValue());
		queue.sleep();
		
		Iterator<Integer> iterator = queue.iterator();
		
		int i = 23;
		while (iterator.hasNext()) {
//			System.out.println(iterator.next());
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		queue.sleep();
		
		queue.remove();
		queue.element();
		
		Assert.assertEquals(26, queue.get(2).intValue());
		Assert.assertEquals(41, queue.get(17).intValue());
		
		queue.clear();
	}
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedArrayQueueFileStringInt3() throws Exception {
		BufferedArrayQueue<int[]> queue = new BufferedArrayQueue<int[]>(outputDir, "test2", 5);
		
		for (int i = 0; i < 50; ++i) {
			queue.add(new int[] {i});
		}
		
		queue.clear(23);
		
		Iterator<int[]> iterator = queue.iterator();
		
		int i = 23;
		while (iterator.hasNext()) {
			Assert.assertEquals(i++, iterator.next()[0]);
		}
		
		queue.clear();
		
	}

}
