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
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue.MyBufferedIntIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedLongArrayQueue.MyBufferedLongIterator;


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
		BufferedIntArrayQueue queue = new BufferedIntArrayQueue(outputDir, "test2", 5);
		
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
		IntTraceIterator iterator = compressedIdTrace.iterator();
		
		int i = 0;
		while (iterator.hasNext() && i < 20) {
			Assert.assertEquals(i, iterator.next());
			Assert.assertEquals(i++, iterator.next());
		}
		Assert.assertEquals(99, iterator.next());
		i = 0;
		while (iterator.hasNext() && i < 20) {
			Assert.assertEquals(i, iterator.next());
			Assert.assertEquals(i++, iterator.next());
		}
		i = 0;
		while (iterator.hasNext() && i < 20) {
			Assert.assertEquals(i, iterator.next());
			Assert.assertEquals(i, iterator.next());
			Assert.assertEquals(i++, iterator.next());
		}
		i = 0;
		while (iterator.hasNext() && i < 10) {
			Assert.assertEquals(i, iterator.next());
			Assert.assertEquals(i++, iterator.next());
		}
		
		queue.clear();
	}
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedBufferedArrayQueueFileStringInt2() throws Exception {
		BufferedIntArrayQueue queue = new BufferedIntArrayQueue(outputDir, "test3", 5);
		
		queue.add(1);
		queue.add(2);
		queue.add(3);
		queue.add(3);
		queue.add(3);
		queue.add(4);
		
		queue.add(1);
		queue.add(2);
		queue.add(3);
		queue.add(4);
		queue.add(3);
		queue.add(4);
		
		queue.add(1);
		queue.add(2);
		queue.add(3);
		queue.add(4);
		queue.add(4);
		queue.add(4);
		queue.add(4);
		queue.add(4);
		
		queue.sleep();
		
		CompressedIntegerIdTrace compressedIdTrace = new CompressedIntegerIdTrace(queue, true);
		
		Assert.assertEquals(20, compressedIdTrace.size());
		Assert.assertEquals(11, compressedIdTrace.getMaxStoredValue());
		Assert.assertEquals(4, compressedIdTrace.getCompressedTrace().size());
		
//		Thread.sleep(5000);
		IntTraceIterator iterator = compressedIdTrace.iterator();
		
		Assert.assertEquals(1, iterator.next());
		Assert.assertEquals(2, iterator.next());
		Assert.assertEquals(3, iterator.next());
		Assert.assertEquals(3, iterator.next());
		Assert.assertEquals(3, iterator.next());
		Assert.assertEquals(4, iterator.next());
		
		Assert.assertEquals(1, iterator.next());
		Assert.assertEquals(2, iterator.next());
		Assert.assertEquals(3, iterator.next());
		Assert.assertEquals(4, iterator.next());
		Assert.assertEquals(3, iterator.next());
		Assert.assertEquals(4, iterator.next());
		
		Assert.assertEquals(1, iterator.next());
		Assert.assertEquals(2, iterator.next());
		Assert.assertEquals(3, iterator.next());
		Assert.assertEquals(4, iterator.next());
		Assert.assertEquals(4, iterator.next());
		Assert.assertEquals(4, iterator.next());
		Assert.assertEquals(4, iterator.next());
		Assert.assertEquals(4, iterator.next());
		
		MyBufferedIntIterator iterator2 = compressedIdTrace.getCompressedTrace().iterator();
		
		Assert.assertEquals(1, iterator2.next());
		Assert.assertEquals(2, iterator2.next());
		Assert.assertEquals(3, iterator2.next());
		Assert.assertEquals(4, iterator2.next());
		
		queue.clear();
	}
	
	/*
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedBufferedArrayQueue#SingleLinkedBufferedArrayQueue(java.io.File, java.lang.String, int)}.
	 */
	@Test
	public void testSingleLinkedLongBufferedArrayQueue() throws Exception {
		BufferedLongArrayQueue queue = new BufferedLongArrayQueue(outputDir, "test4", 5);
		
		queue.add(1);
		queue.add(2);
		queue.add(3);
		queue.add(3);
		queue.add(3);
		queue.add(4);
		
		queue.add(1);
		queue.add(2);
		queue.add(3);
		queue.add(4);
		queue.add(3);
		queue.add(4);
		
		queue.add(1);
		queue.add(2);
		queue.add(3);
		queue.add(4);
		queue.add(4);
		queue.add(4);
		queue.add(4);
		queue.add(4);
		
		queue.sleep();
		
		CompressedLongIdTrace compressedIdTrace = new CompressedLongIdTrace(queue, true);
		
		Assert.assertEquals(20, compressedIdTrace.size());
		Assert.assertEquals(11, compressedIdTrace.getMaxStoredValue());
		Assert.assertEquals(4, compressedIdTrace.getCompressedTrace().size());
		
//		Thread.sleep(5000);
		LongTraceIterator iterator = compressedIdTrace.iterator();
		
		Assert.assertEquals(1, iterator.next());
		Assert.assertEquals(2, iterator.next());
		Assert.assertEquals(3, iterator.next());
		Assert.assertEquals(3, iterator.next());
		Assert.assertEquals(3, iterator.next());
		Assert.assertEquals(4, iterator.next());
		
		Assert.assertEquals(1, iterator.next());
		Assert.assertEquals(2, iterator.next());
		Assert.assertEquals(3, iterator.next());
		Assert.assertEquals(4, iterator.next());
		Assert.assertEquals(3, iterator.next());
		Assert.assertEquals(4, iterator.next());
		
		Assert.assertEquals(1, iterator.next());
		Assert.assertEquals(2, iterator.next());
		Assert.assertEquals(3, iterator.next());
		Assert.assertEquals(4, iterator.next());
		Assert.assertEquals(4, iterator.next());
		Assert.assertEquals(4, iterator.next());
		Assert.assertEquals(4, iterator.next());
		Assert.assertEquals(4, iterator.next());
		
		MyBufferedLongIterator iterator2 = compressedIdTrace.getCompressedTrace().iterator();
		
		Assert.assertEquals(1, iterator2.next());
		Assert.assertEquals(2, iterator2.next());
		Assert.assertEquals(3, iterator2.next());
		Assert.assertEquals(4, iterator2.next());
		
		queue.clear();
	}
	
}
