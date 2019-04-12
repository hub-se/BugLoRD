package se.de.hu_berlin.informatik.spectra.core.traces;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.BufferedMap;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CompressedTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.TraceIterator;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;


/**
 * @author Simon
 *
 */
public class RawTraceCollectorTest extends TestSettings {

	/**
     */
	@BeforeClass
	public static void setUpBeforeClass() {
	}

	/**
     */
	@AfterClass
	public static void tearDownAfterClass() {
	}

	/**
     */
	@Before
	public void setUp() {
	}

	/**
     */
	@After
	public void tearDown() {
	}

	@Test
	public void testAddRawTraceToPool() {
		Path outputDir = Paths.get(getStdTestDir()).resolve("test1");
		RawTraceCollector collector = new RawTraceCollector(outputDir);
		collector.addRawTraceToPool(1, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true, outputDir, "t1");
		collector.addRawTraceToPool(2, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true, outputDir, "t2");
		
		//                             0    1    2    4    4    1    2    4    4    1    2    1      3
		int[] traceArray = new int[] {1,2, 3,4, 5,6, 5,7, 5,7, 3,4, 5,6, 5,7, 5,7, 3,4, 5,6, 3,4, 5,6,7,8};
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3");
		
//		Thread.sleep(20000);
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + Arrays.toString(traceArray));
		
		System.out.println(rawTrace.getCompressedTrace());
		System.out.println(mapToString(rawTrace.getRepetitionMarkers()));
		System.out.println(mapToString(rawTrace.getChild().getRepetitionMarkers()));
		for (Integer integer : rawTrace) {
			System.out.print(integer + ", ");
		}
		System.out.println();
		for (TraceIterator<Integer> iterator = rawTrace.iterator(); iterator.hasNext();) {
			if (iterator.isStartOfRepetition()) {
				System.out.print("s:");
			}
			if (iterator.isEndOfRepetition()) {
				System.out.print("e:");
			}
//			System.out.print(iterator.index + ":");
//			System.out.print(iterator.childIterator.index + ":");
//			System.out.print(iterator.childIterator.childIterator.index + ":");
			Integer integer = iterator.next();
			System.out.print(integer + ", ");
		}
		System.out.println();
		
//		for (TraceIterator<Integer> iterator = rawTrace.iterator(1); iterator.hasNext();) {
//			if (iterator.isStartOfRepetition()) {
//				System.out.print("s:");
//			}
//			if (iterator.isEndOfRepetition()) {
//				System.out.print("e:");
//			}
////			System.out.print(iterator.index + ":");
////			System.out.print(iterator.childIterator.index + ":");
////			System.out.print(iterator.childIterator.childIterator.index + ":");
//			Integer integer = iterator.next();
//			System.out.print(integer + ", ");
//		}
//		System.out.println();
		
		ExecutionTrace executionTrace = collector.getExecutionTraces(3, true).get(0);
		collector.getIndexer().getSequences();
		
		System.out.println(Arrays.toString(executionTrace.reconstructFullMappedTrace(collector.getIndexer())));
		
		StringBuilder result = new StringBuilder("[ ");
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result.append(Arrays.toString(collector.getIndexer().getSequence(j))).append(" ");
		}
		result.append("]");
		System.out.println(result);
		
		System.out.println(executionTrace.getCompressedTrace());
		System.out.println(mapToString(executionTrace.getRepetitionMarkers()));
		System.out.println(mapToString(executionTrace.getChild().getRepetitionMarkers()));
		for (Integer integer : executionTrace) {
			System.out.print(integer + ", ");
		}
		System.out.println();
		
		System.out.println(executionTrace.getCompressedTrace());
		
		System.out.println(collector.getGsTree());
	}
	

	@Test
	public void testAddRawTraceToPool2() {
		Path outputDir = Paths.get(getStdTestDir()).resolve("test2");
		RawTraceCollector collector = new RawTraceCollector(outputDir);
		collector.addRawTraceToPool(1, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true, outputDir, "t1");
		collector.addRawTraceToPool(2, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true, outputDir, "t2");
		
		int[] traceArray = new int[] {1,2, 3,4, 1,2, 3,4, 1,2, 3,4, 3,4, 5,6, 5,6, 5,7, 3,4, 5,6, 3,4, 5,6,7,8};
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3");
		
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + Arrays.toString(traceArray));
		
//		System.out.println(Arrays.toString(rawTrace.reconstructFullTrace()));
		System.out.println(rawTrace.getCompressedTrace());
		System.out.println(mapToString(rawTrace.getRepetitionMarkers()));
		System.out.println(mapToString(rawTrace.getChild().getRepetitionMarkers()));
		for (TraceIterator<Integer> iterator = rawTrace.iterator(); iterator.hasNext();) {
			if (iterator.isStartOfRepetition()) {
				System.out.print("s:");
			}
			if (iterator.isEndOfRepetition()) {
				System.out.print("e:");
			}
//			System.out.print(iterator.index + ":");
//			System.out.print(iterator.childIterator.index + ":");
//			System.out.print(iterator.childIterator.childIterator.index + ":");
			Integer integer = iterator.next();
			System.out.print(integer + ", ");
		}
		System.out.println();
		
//		for (TraceIterator<Integer> iterator = rawTrace.iterator(1); iterator.hasNext();) {
//			if (iterator.isStartOfRepetition()) {
//				System.out.print("s:");
//			}
//			if (iterator.isEndOfRepetition()) {
//				System.out.print("e:");
//			}
////			System.out.print(iterator.index + ":");
////			System.out.print(iterator.childIterator.index + ":");
////			System.out.print(iterator.childIterator.childIterator.index + ":");
//			Integer integer = iterator.next();
//			System.out.print(integer + ", ");
//		}
		System.out.println();
		
		ExecutionTrace executionTrace = collector.getExecutionTraces(3, true).get(0);
		
		System.out.println(Arrays.toString(executionTrace.reconstructFullMappedTrace(collector.getIndexer())));
		
		StringBuilder result = new StringBuilder("[ ");
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result.append(Arrays.toString(collector.getIndexer().getSequence(j))).append(" ");
		}
		result.append("]");
		System.out.println(result);
		
//		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace()));
		System.out.println(executionTrace.getCompressedTrace());
		System.out.println(mapToString(executionTrace.getRepetitionMarkers()));
		System.out.println(mapToString(executionTrace.getChild().getRepetitionMarkers()));
		for (Integer integer : executionTrace) {
			System.out.print(integer + ", ");
		}
		System.out.println();
		
		System.out.println(executionTrace.getCompressedTrace());
		
		System.out.println(collector.getGsTree());
		try {
			collector.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Test
	public void testAddRawTraceToPool3() {
		Path outputDir = Paths.get(getStdTestDir()).resolve("test3");
		RawTraceCollector collector = new RawTraceCollector(outputDir);
		collector.addRawTraceToPool(1, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true, outputDir, "t1");
		collector.addRawTraceToPool(2, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true, outputDir, "t2");
		
		int[] traceArray = new int[] {1,2, 3,4, 5,6, 5,7, 3,4, 5,6, 3,4, 5,6,7,8, 5,6,7,8, 5,6,7,8, 5,6,7,8, 5,6,7,8};
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3");
		
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + Arrays.toString(traceArray));
		
//		System.out.println(Arrays.toString(rawTrace.reconstructFullTrace()));
		System.out.println(rawTrace.getCompressedTrace());
		System.out.println(mapToString(rawTrace.getRepetitionMarkers()));
		System.out.println(mapToString(rawTrace.getChild().getRepetitionMarkers()));
		for (TraceIterator<Integer> iterator = rawTrace.iterator(); iterator.hasNext();) {
			if (iterator.isStartOfRepetition()) {
				System.out.print("s:");
			}
			if (iterator.isEndOfRepetition()) {
				System.out.print("e:");
			}
			Integer integer = iterator.next();
			System.out.print(integer + ", ");
		}
		System.out.println();
		System.out.println(rawTrace.computeStartingElements().toString());
//		for (TraceIterator<Integer> iterator = rawTrace.iterator(1); iterator.hasNext();) {
//			if (iterator.isStartOfRepetition()) {
//				System.out.print("s:");
//			}
//			if (iterator.isEndOfRepetition()) {
//				System.out.print("e:");
//			}
//			Integer integer = iterator.next();
//			System.out.print(integer + ", ");
//		}
//		System.out.println();
		
		ExecutionTrace executionTrace = collector.getExecutionTraces(3, true).get(0);
		
		System.out.println(Arrays.toString(executionTrace.reconstructFullMappedTrace(collector.getIndexer())));
		
		StringBuilder result = new StringBuilder("[ ");
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result.append(Arrays.toString(collector.getIndexer().getSequence(j))).append(" ");
		}
		result.append("]");
		System.out.println(result);
		
//		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace()));
		System.out.println(executionTrace.getCompressedTrace());
		System.out.println(mapToString(executionTrace.getRepetitionMarkers()));
		System.out.println(mapToString(executionTrace.getChild().getRepetitionMarkers()));
		for (Integer integer : executionTrace) {
			System.out.print(integer + ", ");
		}
		System.out.println();
		
		System.out.println(executionTrace.getCompressedTrace());
		
		System.out.println(collector.getGsTree());
		try {
			collector.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testAddRawTraceToPool4() {
		Path outputDir = Paths.get(getStdTestDir()).resolve("test4");
		RawTraceCollector collector = new RawTraceCollector(outputDir);
		collector.addRawTraceToPool(1, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true, outputDir, "t1");
		collector.addRawTraceToPool(2, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true, outputDir, "t2");
		
		int[] traceArray = new int[] {1,2,3, 4,5,6,5,6, 4,5,6,5,6,5,6, 7,8};
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3");
		
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + Arrays.toString(traceArray));
		
//		System.out.println(Arrays.toString(rawTrace.reconstructFullTrace()));
		System.out.println(rawTrace.getCompressedTrace());
		System.out.println(mapToString(rawTrace.getRepetitionMarkers()));
		System.out.println(mapToString(rawTrace.getChild().getRepetitionMarkers()));
		for (TraceIterator<Integer> iterator = rawTrace.iterator(); iterator.hasNext();) {
			if (iterator.isStartOfRepetition()) {
				System.out.print("s:");
			}
			if (iterator.isEndOfRepetition()) {
				System.out.print("e:");
			}
			Integer integer = iterator.next();
			System.out.print(integer + ", ");
		}
		System.out.println();
		System.out.println(rawTrace.computeStartingElements().toString());
//		for (TraceIterator<Integer> iterator = rawTrace.iterator(1); iterator.hasNext();) {
//			if (iterator.isStartOfRepetition()) {
//				System.out.print("s:");
//			}
//			if (iterator.isEndOfRepetition()) {
//				System.out.print("e:");
//			}
//			Integer integer = iterator.next();
//			System.out.print(integer + ", ");
//		}
//		System.out.println();
		
		ExecutionTrace executionTrace = collector.getExecutionTraces(3, true).get(0);
		
		System.out.println(Arrays.toString(executionTrace.reconstructFullMappedTrace(collector.getIndexer())));
		
		StringBuilder result = new StringBuilder("[ ");
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result.append(Arrays.toString(collector.getIndexer().getSequence(j))).append(" ");
		}
		result.append("]");
		System.out.println(result);
		
//		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace()));
		System.out.println(executionTrace.getCompressedTrace());
		System.out.println(mapToString(executionTrace.getRepetitionMarkers()));
		if (executionTrace.getChild() != null) {
			System.out.println(mapToString(executionTrace.getChild().getRepetitionMarkers()));
		}
		for (Integer integer : executionTrace) {
			System.out.print(integer + ", ");
		}
		System.out.println();
		
		System.out.println(executionTrace.getCompressedTrace());
		
		System.out.println(collector.getGsTree());
		
		try {
			collector.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testAddRawTraceToPool5() {
		Path outputDir = Paths.get(getStdTestDir()).resolve("test5");
		RawTraceCollector collector = new RawTraceCollector(outputDir);
		collector.addRawTraceToPool(1, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true, outputDir, "t1");
		collector.addRawTraceToPool(2, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true, outputDir, "t2");
		
		int[] traceArray = new int[] {1,2,3, 4,5,6,7,7,5,6,7,7,7, 4,5,6,7,5,6,7,7,5,6,7,7,7,7, 9,8};
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3");
		
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + Arrays.toString(traceArray));
		
//		System.out.println(Arrays.toString(rawTrace.reconstructFullTrace()));
		System.out.println(rawTrace.getCompressedTrace());
		System.out.println(mapToString(rawTrace.getRepetitionMarkers()));
		System.out.println(mapToString(rawTrace.getChild().getRepetitionMarkers()));
		System.out.println(mapToString(rawTrace.getChild().getChild().getRepetitionMarkers()));
		for (TraceIterator<Integer> iterator = rawTrace.iterator(); iterator.hasNext();) {
			if (iterator.isStartOfRepetition()) {
				System.out.print("s:");
			}
			if (iterator.isEndOfRepetition()) {
				System.out.print("e:");
			}
			Integer integer = iterator.next();
			System.out.print(integer + ", ");
		}
		System.out.println();
		System.out.println(rawTrace.computeStartingElements().toString());
//		for (TraceIterator<Integer> iterator = rawTrace.iterator(1); iterator.hasNext();) {
//			if (iterator.isStartOfRepetition()) {
//				System.out.print("s:");
//			}
//			if (iterator.isEndOfRepetition()) {
//				System.out.print("e:");
//			}
//			Integer integer = iterator.next();
//			System.out.print(integer + ", ");
//		}
//		System.out.println();
		
		ExecutionTrace executionTrace = collector.getExecutionTraces(3, true).get(0);
		
		System.out.println(Arrays.toString(executionTrace.reconstructFullMappedTrace(collector.getIndexer())));
		
		StringBuilder result = new StringBuilder("[ ");
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result.append(Arrays.toString(collector.getIndexer().getSequence(j))).append(" ");
		}
		result.append("]");
		System.out.println(result);
		
//		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace()));
		System.out.println(executionTrace.getCompressedTrace());
		System.out.println(mapToString(executionTrace.getRepetitionMarkers()));
		if (executionTrace.getChild() != null) {
			System.out.println(mapToString(executionTrace.getChild().getRepetitionMarkers()));
		}
		for (Integer integer : executionTrace) {
			System.out.print(integer + ", ");
		}
		System.out.println();
		
		System.out.println(executionTrace.getCompressedTrace());
		
		System.out.println(collector.getGsTree());
		try {
			collector.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String mapToString(BufferedMap<int[]> map) {
		if (map == null) {
			return "null";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("[ ");
		Iterator<Entry<Integer, int[]>> entrySetIterator = map.entrySetIterator();
		while (entrySetIterator.hasNext()) {
			Entry<Integer, int[]> entry = entrySetIterator.next();
			builder.append("[").append(entry.getKey()).append(": ").append(entry.getValue()[0]).append(", ").append(entry.getValue()[1]).append("] ");
		}
		builder.append("]");
		return builder.toString();
	}

}
