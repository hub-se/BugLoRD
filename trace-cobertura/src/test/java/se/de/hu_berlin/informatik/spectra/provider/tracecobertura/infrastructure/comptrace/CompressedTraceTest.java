/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedLongArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedArrayQueue.Type;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue.MyBufferedIntIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.CompressedIdTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.EfficientCompressedIntegerTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.IntTraceIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.ReplaceableCloneableIntIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs.EfficientCompressedLongTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs.LongTraceIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs.ReplaceableCloneableLongIterator;


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
		
		System.out.println(compressedIdTrace);
//		printWithIterator(compressedIdTrace.baseIterator());
		
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
		
		EfficientCompressedIntegerTrace compressedIdTrace = new EfficientCompressedIntegerTrace(queue, true);
		
		Assert.assertEquals(161, compressedIdTrace.size());
		Assert.assertEquals(99, compressedIdTrace.getMaxStoredValue());
		Assert.assertEquals(51, compressedIdTrace.getCompressedTrace().size());
		
		System.out.println(compressedIdTrace);
		printWithIterator(compressedIdTrace.baseIterator());
		
//		Thread.sleep(5000);
		IntTraceIterator iterator = compressedIdTrace.iterator();
		
		int counter = 0;
		int i = 0;
		while (iterator.hasNext() && i < 20) {
			Assert.assertEquals(i, iterator.next());
			Assert.assertEquals(i++, iterator.next());
			counter += 2;
		}
		Assert.assertEquals(99, iterator.next());
		++counter;
		i = 0;
		while (iterator.hasNext() && i < 20) {
			Assert.assertEquals(i, iterator.next());
			Assert.assertEquals(i++, iterator.next());
			counter += 2;
		}
		i = 0;
		while (iterator.hasNext() && i < 20) {
			Assert.assertEquals(i, iterator.next());
			Assert.assertEquals(i, iterator.next());
			Assert.assertEquals(i++, iterator.next());
			counter += 3;
		}
		i = 0;
		while (iterator.hasNext() && i < 10) {
			Assert.assertEquals(i, iterator.next());
			Assert.assertEquals(i++, iterator.next());
			counter += 2;
		}
		
		queue.clear();
		Assert.assertEquals(compressedIdTrace.size(), counter);
	}
	
	@Test
	public void testBackwardsLongIterationWithAlternations() throws Exception {
		BufferedLongArrayQueue queue = new BufferedLongArrayQueue(outputDir, "test2rAL", 5);
		
		for (int i = 0; i < 20; ++i) {
			queue.add(1);
			queue.add(i % 2 == 0 ? 0 : 2);
		}
		queue.add(99);
		for (int i = 0; i < 20; ++i) {
			queue.add(1);
			queue.add(i % 2 == 0 ? 0 : 2);
		}
		for (int i = 0; i < 20; ++i) {
			queue.add(i);
			queue.add(i);
			queue.add(i);
		}
		for (int i = 0; i < 10; ++i) {
			queue.add(1);
			queue.add(i % 2 == 0 ? 0 : 2);
		}
		queue.sleep();
		
		EfficientCompressedLongTrace compressedIdTrace = new EfficientCompressedLongTrace(queue, true);
		
		Assert.assertEquals(161, compressedIdTrace.size());
		Assert.assertEquals(99, compressedIdTrace.getMaxStoredValue());
//		Assert.assertEquals(30, compressedIdTrace.getCompressedTrace().size());
		
//		Thread.sleep(5000);

		printWithIterator(compressedIdTrace.iterator());
		printWithIterator(compressedIdTrace.reverseIterator());
		
//		System.out.println(compressedIdTrace.toString());
		
		List<Long> list = storeInList(compressedIdTrace.iterator());
		List<Long> reverseList = storeInList(compressedIdTrace.reverseIterator());
		
		Collections.reverse(reverseList);
		
		Assert.assertEquals(compressedIdTrace.size(), list.size());
		Assert.assertEquals(compressedIdTrace.size(), reverseList.size());
		
		Iterator<Long> listIterator = list.iterator();
		Iterator<Long> reverseListIterator = reverseList.iterator();
		
		while (listIterator.hasNext()) {
			Assert.assertEquals(listIterator.next(), reverseListIterator.next());
		}
		
//		int i = 0;
//		while (iterator.hasNext() && i < 20) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		Assert.assertEquals(99, iterator.next());
//		i = 0;
//		while (iterator.hasNext() && i < 20) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		i = 0;
//		while (iterator.hasNext() && i < 20) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		i = 0;
//		while (iterator.hasNext() && i < 10) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		
		queue.clear();
	}
	
	@Test
	public void testBackwardsIntegerIterationWithAlternations() throws Exception {
		BufferedIntArrayQueue queue = new BufferedIntArrayQueue(outputDir, "test2rA", 5);
		
		for (int i = 0; i < 20; ++i) {
			queue.add(1);
			queue.add(i % 2 == 0 ? 0 : 2);
		}
		queue.add(99);
		for (int i = 0; i < 20; ++i) {
			queue.add(1);
			queue.add(i % 2 == 0 ? 0 : 2);
		}
		for (int i = 0; i < 20; ++i) {
			queue.add(i);
			queue.add(i);
			queue.add(i);
		}
		for (int i = 0; i < 10; ++i) {
			queue.add(1);
			queue.add(i % 2 == 0 ? 0 : 2);
		}
		queue.sleep();
		
		EfficientCompressedIntegerTrace compressedIdTrace = new EfficientCompressedIntegerTrace(queue, true);
		
		System.out.println(compressedIdTrace.toString());
		printWithIterator(compressedIdTrace.baseIterator());
		
		Assert.assertEquals(161, compressedIdTrace.size());
		Assert.assertEquals(99, compressedIdTrace.getMaxStoredValue());
//		Assert.assertEquals(30, compressedIdTrace.getCompressedTrace().size());
		
//		Thread.sleep(5000);
		
//		compressedIdTrace.sleep();
//		Thread.sleep(15000);

		printWithIterator(compressedIdTrace.iterator());
		printWithIterator(compressedIdTrace.reverseIterator());
		
		System.out.println(compressedIdTrace.toString());
		
		List<Integer> list = storeInList(compressedIdTrace.iterator());
		List<Integer> reverseList = storeInList(compressedIdTrace.reverseIterator());
		
		Collections.reverse(reverseList);
		
		Assert.assertEquals(compressedIdTrace.size(), list.size());
		Assert.assertEquals(compressedIdTrace.size(), reverseList.size());
		
		Iterator<Integer> listIterator = list.iterator();
		Iterator<Integer> reverseListIterator = reverseList.iterator();
		
		while (listIterator.hasNext()) {
			Assert.assertEquals(listIterator.next(), reverseListIterator.next());
		}
		
//		int i = 0;
//		while (iterator.hasNext() && i < 20) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		Assert.assertEquals(99, iterator.next());
//		i = 0;
//		while (iterator.hasNext() && i < 20) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		i = 0;
//		while (iterator.hasNext() && i < 20) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		i = 0;
//		while (iterator.hasNext() && i < 10) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		
		queue.clear();
	}

	@Test
	public void testBackwardsIntegerIteration() throws Exception {
		BufferedIntArrayQueue queue = new BufferedIntArrayQueue(outputDir, "test2r", 5);
		
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
		
		EfficientCompressedIntegerTrace compressedIdTrace = new EfficientCompressedIntegerTrace(queue, true);
		
		Assert.assertEquals(161, compressedIdTrace.size());
		Assert.assertEquals(99, compressedIdTrace.getMaxStoredValue());
		Assert.assertEquals(51, compressedIdTrace.getCompressedTrace().size());
		
//		Thread.sleep(5000);

		printWithIterator(compressedIdTrace.iterator());
		printWithIterator(compressedIdTrace.reverseIterator());
		
//		System.out.println(compressedIdTrace.toString());
		
		List<Integer> list = storeInList(compressedIdTrace.iterator());
		List<Integer> reverseList = storeInList(compressedIdTrace.reverseIterator());
		
		Collections.reverse(reverseList);
		
		Assert.assertEquals(compressedIdTrace.size(), list.size());
		Assert.assertEquals(compressedIdTrace.size(), reverseList.size());
		
		Iterator<Integer> listIterator = list.iterator();
		Iterator<Integer> reverseListIterator = reverseList.iterator();
		
		while (listIterator.hasNext()) {
			Assert.assertEquals(listIterator.next(), reverseListIterator.next());
		}
		
//		int i = 0;
//		while (iterator.hasNext() && i < 20) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		Assert.assertEquals(99, iterator.next());
//		i = 0;
//		while (iterator.hasNext() && i < 20) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		i = 0;
//		while (iterator.hasNext() && i < 20) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		i = 0;
//		while (iterator.hasNext() && i < 10) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		
		queue.clear();
	}
	
	private static void printWithIterator(ReplaceableCloneableIntIterator iterator) {
		StringBuilder builder = new StringBuilder();
		while (iterator.hasNext()) {
			builder.append(iterator.next() + ", ");
		}
		System.out.println(builder.toString());
	}
	
	private static List<Integer> storeInList(ReplaceableCloneableIntIterator iterator) {
		List<Integer> result = new ArrayList<>();
		while (iterator.hasNext()) {
			result.add(iterator.next());
		}
		return result;
	}
	
	
	@Test
	public void testBackwardsLongIteration() throws Exception {
		BufferedLongArrayQueue queue = new BufferedLongArrayQueue(outputDir, "test2lr", 5);
		
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
		
		EfficientCompressedLongTrace compressedIdTrace = new EfficientCompressedLongTrace(queue, true);
		
		Assert.assertEquals(161, compressedIdTrace.size());
		Assert.assertEquals(99, compressedIdTrace.getMaxStoredValue());
		Assert.assertEquals(51, compressedIdTrace.getCompressedTrace().size());
		
//		Thread.sleep(5000);
		
		System.out.println(compressedIdTrace);
//		printWithIterator(compressedIdTrace.baseIterator());

		printWithIterator(compressedIdTrace.iterator());
		printWithIterator(compressedIdTrace.reverseIterator());
		
		List<Long> list = storeInList(compressedIdTrace.iterator());
		List<Long> reverseList = storeInList(compressedIdTrace.reverseIterator());
		
		Collections.reverse(reverseList);
		
		Assert.assertEquals(compressedIdTrace.size(), list.size());
		Assert.assertEquals(compressedIdTrace.size(), reverseList.size());
		
		Iterator<Long> listIterator = list.iterator();
		Iterator<Long> reverseListIterator = reverseList.iterator();
		
		while (listIterator.hasNext()) {
			Assert.assertEquals(listIterator.next(), reverseListIterator.next());
		}
		
//		int i = 0;
//		while (iterator.hasNext() && i < 20) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		Assert.assertEquals(99, iterator.next());
//		i = 0;
//		while (iterator.hasNext() && i < 20) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		i = 0;
//		while (iterator.hasNext() && i < 20) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		i = 0;
//		while (iterator.hasNext() && i < 10) {
//			Assert.assertEquals(i, iterator.next());
//			Assert.assertEquals(i++, iterator.next());
//		}
//		
		queue.clear();
	}
	
	private static void printWithIterator(ReplaceableCloneableLongIterator iterator) {
		StringBuilder builder = new StringBuilder();
		while (iterator.hasNext()) {
			builder.append(iterator.next() + ", ");
		}
		System.out.println(builder.toString());
	}
	
	private static List<Long> storeInList(ReplaceableCloneableLongIterator iterator) {
		List<Long> result = new ArrayList<>();
		while (iterator.hasNext()) {
			result.add(iterator.next());
		}
		return result;
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
		
		EfficientCompressedIntegerTrace compressedIdTrace = new EfficientCompressedIntegerTrace(queue, true);
		
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
		
		EfficientCompressedLongTrace compressedIdTrace = new EfficientCompressedLongTrace(queue, true);
		
		Assert.assertEquals(20, compressedIdTrace.size());
		Assert.assertEquals(11, compressedIdTrace.getMaxStoredValue());
		Assert.assertEquals(4, compressedIdTrace.getCompressedTrace().size());
		
//		compressedIdTrace.sleep();
//		Thread.sleep(15000);
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
		
		ReplaceableCloneableLongIterator iterator2 = compressedIdTrace.baseIterator();
		
		Assert.assertEquals(1, iterator2.next());
		Assert.assertEquals(2, iterator2.next());
		Assert.assertEquals(3, iterator2.next());
		Assert.assertEquals(4, iterator2.next());
		
		queue.clear();
	}
	
}
