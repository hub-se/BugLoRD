/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.io.File;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedArrayQueue.Type;


/**
 * @author Simon
 *
 */
public class CompressedTraceTest {

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

	private File outputDir = new File("target" + File.separator + "CompressedTraceTest");
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedArrayQueueFileStringLong() throws Exception {
		BufferedArrayQueue<Long> queue = new BufferedArrayQueue<Long>(outputDir, "test", 5, Type.LONG);
		
		for (long i = 0; i < 20; ++i) {
			queue.add(i);
			queue.add(i);
		}
		queue.add(99L);
		for (long i = 0; i < 20; ++i) {
			queue.add(i);
			queue.add(i);
		}
		for (long i = 0; i < 20; ++i) {
			queue.add(i);
			queue.add(i);
			queue.add(i);
		}
		for (long i = 0; i < 10; ++i) {
			queue.add(i);
			queue.add(i);
		}
		queue.sleep();
		
		CompressedIdTrace compressedIdTrace = new CompressedIdTrace(queue, true);
		
		Assert.assertEquals(161, compressedIdTrace.size());
		Assert.assertEquals(99, compressedIdTrace.getMaxStoredValue());
		Assert.assertEquals(51, compressedIdTrace.getCompressedTrace().size());
		
//		Thread.sleep(5000);
		Iterator<Long> iterator = compressedIdTrace.iterator();
		
		int i = 0;
		while (iterator.hasNext() && i < 20) {
			Assert.assertEquals(i, iterator.next().intValue());
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		Assert.assertEquals(99, iterator.next().intValue());
		i = 0;
		while (iterator.hasNext() && i < 20) {
			Assert.assertEquals(i, iterator.next().intValue());
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		i = 0;
		while (iterator.hasNext() && i < 20) {
			Assert.assertEquals(i, iterator.next().intValue());
			Assert.assertEquals(i, iterator.next().intValue());
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		i = 0;
		while (iterator.hasNext() && i < 10) {
			Assert.assertEquals(i, iterator.next().intValue());
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		
		queue.clear();
	}
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedArrayQueueFileStringInt() throws Exception {
		BufferedArrayQueue<Integer> queue = new BufferedArrayQueue<Integer>(outputDir, "test2", 5, Type.INTEGER);
		
		for (int i = 0; i < 20; ++i) {
			queue.add(i);
			queue.add(i);
		}
		queue.add(99);
		for (int i = 0; i < 20; ++i) {
			queue.add(i);
			queue.add(i);
		}
		for (int i = 0; i < 20; ++i) {
			queue.add(i);
			queue.add(i);
			queue.add(i);
		}
		for (int i = 0; i < 10; ++i) {
			queue.add(i);
			queue.add(i);
		}
		queue.sleep();
		
		CompressedIntegerIdTrace compressedIdTrace = new CompressedIntegerIdTrace(queue, true);
		
		Assert.assertEquals(161, compressedIdTrace.size());
		Assert.assertEquals(99, compressedIdTrace.getMaxStoredValue());
		Assert.assertEquals(51, compressedIdTrace.getCompressedTrace().size());
		
//		Thread.sleep(5000);
		Iterator<Integer> iterator = compressedIdTrace.iterator();
		
		int i = 0;
		while (iterator.hasNext() && i < 20) {
			Assert.assertEquals(i, iterator.next().intValue());
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		Assert.assertEquals(99, iterator.next().intValue());
		i = 0;
		while (iterator.hasNext() && i < 20) {
			Assert.assertEquals(i, iterator.next().intValue());
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		i = 0;
		while (iterator.hasNext() && i < 20) {
			Assert.assertEquals(i, iterator.next().intValue());
			Assert.assertEquals(i, iterator.next().intValue());
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		i = 0;
		while (iterator.hasNext() && i < 10) {
			Assert.assertEquals(i, iterator.next().intValue());
			Assert.assertEquals(i++, iterator.next().intValue());
		}
		
		queue.clear();
	}
	
}
