/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.core.traces;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CompressedTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.TraceIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CloneableIterator;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;


/**
 * @author Simon
 *
 */
public class RawTraceCollectorTest extends TestSettings {

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

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.RawTraceCollector#addRawTraceToPool(java.lang.String, java.util.List)}.
	 */
	@Test
	public void testAddRawTraceToPool() throws Exception {
		RawTraceCollector collector = new RawTraceCollector(Paths.get(getStdTestDir()).resolve("test1"));
		collector.addRawTraceToPool(1, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true);
		collector.addRawTraceToPool(2, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true);
		
		//                             0    1    2    4    4    1    2    4    4    1    2    1      3
		int[] traceArray = new int[] {1,2, 3,4, 5,6, 5,7, 5,7, 3,4, 5,6, 5,7, 5,7, 3,4, 5,6, 3,4, 5,6,7,8};
		collector.addRawTraceToPool(3, 0, traceArray, true);
		
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + Arrays.toString(traceArray));
		
		System.out.println(Arrays.toString(rawTrace.reconstructFullTrace()));
		System.out.println(Arrays.toString(rawTrace.getCompressedTrace()));
		System.out.println(mapToString(rawTrace.getRepetitionMarkers()));
		System.out.println(mapToString(rawTrace.getChild().getRepetitionMarkers()));
		for (TraceIterator<Integer> iterator = rawTrace.iterator(); iterator.hasNext();) {
			Integer integer = iterator.next();
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
		
		System.out.println(Arrays.toString(executionTrace.reconstructFullMappedTrace(collector.getIndexer())));
		
		String result = "[ ";
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result += Arrays.toString(collector.getIndexer().getSequence(j)) + " ";
		}
		result += "]";
		System.out.println(result);
		
		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace()));
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
		System.out.println(mapToString(executionTrace.getRepetitionMarkers()));
		System.out.println(mapToString(executionTrace.getChild().getRepetitionMarkers()));
		for (CloneableIterator<Integer> iterator = executionTrace.iterator(); iterator.hasNext();) {
			Integer integer = iterator.next();
			System.out.print(integer + ", ");
		}
		System.out.println();
		
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
		
		System.out.println(collector.getGsTree());
	}
	
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.RawTraceCollector#addRawTraceToPool(java.lang.String, java.util.List)}.
	 */
	@Test
	public void testAddRawTraceToPool2() throws Exception {
		RawTraceCollector collector = new RawTraceCollector(Paths.get(getStdTestDir()).resolve("test2"));
		collector.addRawTraceToPool(1, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true);
		collector.addRawTraceToPool(2, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true);
		
		int[] traceArray = new int[] {1,2, 3,4, 1,2, 3,4, 1,2, 3,4, 3,4, 5,6, 5,6, 5,7, 3,4, 5,6, 3,4, 5,6,7,8};
		collector.addRawTraceToPool(3, 0, traceArray, true);
		
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + Arrays.toString(traceArray));
		
		System.out.println(Arrays.toString(rawTrace.reconstructFullTrace()));
		System.out.println(Arrays.toString(rawTrace.getCompressedTrace()));
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
		
		String result = "[ ";
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result += Arrays.toString(collector.getIndexer().getSequence(j)) + " ";
		}
		result += "]";
		System.out.println(result);
		
		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace()));
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
		System.out.println(mapToString(executionTrace.getRepetitionMarkers()));
		System.out.println(mapToString(executionTrace.getChild().getRepetitionMarkers()));
		for (CloneableIterator<Integer> iterator = executionTrace.iterator(); iterator.hasNext();) {
			Integer integer = iterator.next();
			System.out.print(integer + ", ");
		}
		System.out.println();
		
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
		
		System.out.println(collector.getGsTree());
		try {
			collector.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.RawTraceCollector#addRawTraceToPool(java.lang.String, java.util.List)}.
	 */
	@Test
	public void testAddRawTraceToPool3() throws Exception {
		RawTraceCollector collector = new RawTraceCollector(Paths.get(getStdTestDir()).resolve("test3"));
		collector.addRawTraceToPool(1, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true);
		collector.addRawTraceToPool(2, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true);
		
		int[] traceArray = new int[] {1,2, 3,4, 5,6, 5,7, 3,4, 5,6, 3,4, 5,6,7,8, 5,6,7,8, 5,6,7,8, 5,6,7,8, 5,6,7,8};
		collector.addRawTraceToPool(3, 0, traceArray, true);
		
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + Arrays.toString(traceArray));
		
		System.out.println(Arrays.toString(rawTrace.reconstructFullTrace()));
		System.out.println(Arrays.toString(rawTrace.getCompressedTrace()));
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
		
		String result = "[ ";
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result += Arrays.toString(collector.getIndexer().getSequence(j)) + " ";
		}
		result += "]";
		System.out.println(result);
		
		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace()));
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
		System.out.println(mapToString(executionTrace.getRepetitionMarkers()));
		System.out.println(mapToString(executionTrace.getChild().getRepetitionMarkers()));
		for (CloneableIterator<Integer> iterator = executionTrace.iterator(); iterator.hasNext();) {
			Integer integer = iterator.next();
			System.out.print(integer + ", ");
		}
		System.out.println();
		
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
		
		System.out.println(collector.getGsTree());
		try {
			collector.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.RawTraceCollector#addRawTraceToPool(java.lang.String, java.util.List)}.
	 */
	@Test
	public void testAddRawTraceToPool4() throws Exception {
		RawTraceCollector collector = new RawTraceCollector(Paths.get(getStdTestDir()).resolve("test4"));
		collector.addRawTraceToPool(1, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true);
		collector.addRawTraceToPool(2, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true);
		
		int[] traceArray = new int[] {1,2,3, 4,5,6,5,6, 4,5,6,5,6,5,6, 7,8};
		collector.addRawTraceToPool(3, 0, traceArray, true);
		
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + Arrays.toString(traceArray));
		
		System.out.println(Arrays.toString(rawTrace.reconstructFullTrace()));
		System.out.println(Arrays.toString(rawTrace.getCompressedTrace()));
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
		
		String result = "[ ";
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result += Arrays.toString(collector.getIndexer().getSequence(j)) + " ";
		}
		result += "]";
		System.out.println(result);
		
		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace()));
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
		System.out.println(mapToString(executionTrace.getRepetitionMarkers()));
		if (executionTrace.getChild() != null) {
			System.out.println(mapToString(executionTrace.getChild().getRepetitionMarkers()));
		}
		for (CloneableIterator<Integer> iterator = executionTrace.iterator(); iterator.hasNext();) {
			Integer integer = iterator.next();
			System.out.print(integer + ", ");
		}
		System.out.println();
		
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
		
		System.out.println(collector.getGsTree());
		
		try {
			collector.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.RawTraceCollector#addRawTraceToPool(java.lang.String, java.util.List)}.
	 */
	@Test
	public void testAddRawTraceToPool5() throws Exception {
		RawTraceCollector collector = new RawTraceCollector(Paths.get(getStdTestDir()).resolve("test5"));
		collector.addRawTraceToPool(1, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true);
		collector.addRawTraceToPool(2, 0, new int[] {1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8}, true);
		
		int[] traceArray = new int[] {1,2,3, 4,5,6,7,7,5,6,7,7,7, 4,5,6,7,5,6,7,7,5,6,7,7,7,7, 9,8};
		collector.addRawTraceToPool(3, 0, traceArray, true);
		
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + Arrays.toString(traceArray));
		
		System.out.println(Arrays.toString(rawTrace.reconstructFullTrace()));
		System.out.println(Arrays.toString(rawTrace.getCompressedTrace()));
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
		
		String result = "[ ";
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result += Arrays.toString(collector.getIndexer().getSequence(j)) + " ";
		}
		result += "]";
		System.out.println(result);
		
		System.out.println(Arrays.toString(executionTrace.reconstructFullTrace()));
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
		System.out.println(mapToString(executionTrace.getRepetitionMarkers()));
		if (executionTrace.getChild() != null) {
			System.out.println(mapToString(executionTrace.getChild().getRepetitionMarkers()));
		}
		for (CloneableIterator<Integer> iterator = executionTrace.iterator(); iterator.hasNext();) {
			Integer integer = iterator.next();
			System.out.print(integer + ", ");
		}
		System.out.println();
		
		System.out.println(Arrays.toString(executionTrace.getCompressedTrace()));
		
		System.out.println(collector.getGsTree());
		try {
			collector.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String mapToString(Map<Integer, int[]> map) {
		if (map == null) {
			return "null";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("[ ");
		for (Entry<Integer, int[]> entry : map.entrySet()) {
			builder.append("[" + entry.getKey() + ": " + entry.getValue()[0] + ", " + entry.getValue()[1] + "] ");
		}
		builder.append("]");
		return builder.toString();
	}

}
