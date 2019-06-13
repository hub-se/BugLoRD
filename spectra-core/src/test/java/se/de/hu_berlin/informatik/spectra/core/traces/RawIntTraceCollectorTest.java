package se.de.hu_berlin.informatik.spectra.core.traces;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CompressedTraceBase;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.TraceIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.BufferedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.BufferedMap;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;


/**
 * @author Simon
 *
 */
public class RawIntTraceCollectorTest extends TestSettings {

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

	private Integer[] s(int... numbers) {
		Integer[] result = new Integer[numbers.length];
		for (int i = 0; i < numbers.length; ++i) {
			result[i] =numbers[i];
		}
		return result;
	}
	
	private int[][] rt(int... numbers) {
		int[][] result = new int[numbers.length][];
		for (int i = 0; i < numbers.length; ++i) {
			result[i] = new int[] {0,numbers[i]};
		}
		return result;
	}
	
	private int[][] getSubTraceIdSequences(RawIntTraceCollector collector) {
		int[][] subTraceIdSequences;
		ArraySequenceIndexer<Integer, Integer> indexer = collector.getIndexer();
		
		subTraceIdSequences = new int[indexer.getSequences().length][];
		for (int i = 0; i < indexer.getSequences().length; i++) {
			Iterator<Integer> sequenceIterator = indexer.getSequenceIterator(i);
			SingleLinkedArrayQueue<Integer> traceOfSubTraceIDs = new SingleLinkedArrayQueue<>(100);
			
			while (sequenceIterator.hasNext()) {
				traceOfSubTraceIDs.add(sequenceIterator.next());
			}
			
			subTraceIdSequences[i] = new int[traceOfSubTraceIDs.size()];
			for (int j = 0; j < subTraceIdSequences[i].length; ++j) {
				subTraceIdSequences[i][j] = traceOfSubTraceIDs.remove();
			}
		}
		return subTraceIdSequences;
	}
	
	private int[][] getNodeIdSequences(Map<Integer, BufferedArrayQueue<int[]>> idToSubTraceMap) {
		// indexer.getSequences() will generate all sequences of sub trace IDs that exist in the GS tree
		int[][] nodeIdSequences = new int[idToSubTraceMap.size()+1][];

		// id 0 marks an empty sub trace... should not really happen, but just in case it does... :/
		nodeIdSequences[0] = new int[] {};
		for (int i = 1; i < idToSubTraceMap.size() + 1; i++) {
			BufferedArrayQueue<int[]> list = idToSubTraceMap.get(i);
			Iterator<int[]> sequenceIterator = list.iterator();
			SingleLinkedArrayQueue<Integer> traceOfNodeIDs = new SingleLinkedArrayQueue<>(100);
			
			while (sequenceIterator.hasNext()) {
				int[] statement = sequenceIterator.next();
				traceOfNodeIDs.add(statement[1]);
			}
			
			nodeIdSequences[i] = new int[traceOfNodeIDs.size()];
			for (int j = 0; j < nodeIdSequences[i].length; ++j) {
				nodeIdSequences[i][j] = traceOfNodeIDs.remove();
			}
		}
		
		return nodeIdSequences;
	}
	
	private BufferedMap<BufferedArrayQueue<int[]>> generateIdToSubtraceMap(Path outputDir, int max, String filePrefix) {
		BufferedMap<BufferedArrayQueue<int[]>> idToSubTraceMap = new BufferedMap<>(outputDir.toFile(), filePrefix);
        for (int i = 1; i <= max; ++i) {
        	idToSubTraceMap.put(i,asList(outputDir, rt(i+10)));
        }
		return idToSubTraceMap;
	}
	
	private BufferedArrayQueue<int[]> asList(Path outputDir, int[][] rt) {
		BufferedArrayQueue<int[]> list = new BufferedArrayQueue<>(outputDir.toFile(), 
				String.valueOf(UUID.randomUUID()), rt.length);
		for (int[] statement : rt) {
			list.add(statement);
		}
		return list;
	}

	private void checkMappedTrace(Integer[] traceArray, int[] fullMappedTrace) {
		Assert.assertEquals(traceArray.length, fullMappedTrace.length);
		for (int i = 0; i < traceArray.length; i++) {
			Assert.assertEquals(traceArray[i]+10, fullMappedTrace[i]);
		}
	}

	@Test
	public void testAddRawTraceToPool() {
		Path outputDir = Paths.get(getStdTestDir()).resolve("test1");
		RawIntTraceCollector collector = new RawIntTraceCollector(outputDir);
		
		// sub trace id -> sub trace
        BufferedMap<BufferedArrayQueue<int[]>> idToSubTraceMap = generateIdToSubtraceMap(outputDir, 8, "test1");
        
		collector.addRawTraceToPool(1, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t1", 
				generateIdToSubtraceMap(outputDir, 8, "test1"));
		collector.addRawTraceToPool(2, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t2", 
				generateIdToSubtraceMap(outputDir, 8, "test1"));
		
		//                        0    1    2    4    4    1    2    4    4    1    2    1      3      ????
		Integer[] traceArray = s(1,2, 3,4, 5,6, 5,7, 5,7, 3,4, 5,6, 5,7, 5,7, 3,4, 5,6, 3,4, 5,6,7,8);
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3", 
				generateIdToSubtraceMap(outputDir, 8, "test1"));
		
//		Thread.sleep(20000);
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + arrayToString(traceArray));
		
//		System.out.println(rawTrace.getCompressedTrace());
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
		
		int[][] subTraceIdSequences = getSubTraceIdSequences(collector);
		int[][] nodeIdSequences = getNodeIdSequences(idToSubTraceMap);
		SimpleIntIndexer simpleIndexer = new SimpleIntIndexer(subTraceIdSequences, nodeIdSequences);
		
		
		int[] fullMappedTrace = executionTrace.reconstructFullMappedTrace(simpleIndexer);
		checkMappedTrace(traceArray, fullMappedTrace);
		System.out.println(Arrays.toString(fullMappedTrace));
		
		StringBuilder result = new StringBuilder("[ ");
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result.append(arrayToString(collector.getIndexer().getSequence(j))).append(" ");
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
		
		try {
			collector.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testAddRawTraceToPool2() {
		Path outputDir = Paths.get(getStdTestDir()).resolve("test2");
		RawIntTraceCollector collector = new RawIntTraceCollector(outputDir);
		
		// sub trace id -> sub trace
        BufferedMap<BufferedArrayQueue<int[]>> idToSubTraceMap = generateIdToSubtraceMap(outputDir, 8, "test2");
        
		collector.addRawTraceToPool(1, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t1", 
				generateIdToSubtraceMap(outputDir, 8, "test2"));
		collector.addRawTraceToPool(2, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t2", 
				generateIdToSubtraceMap(outputDir, 8, "test2"));
		
		Integer[] traceArray = s(1,2, 3,4, 1,2, 3,4, 1,2, 3,4, 3,4, 5,6, 5,6, 5,7, 3,4, 5,6, 3,4, 5,6,7,8);
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3", 
				generateIdToSubtraceMap(outputDir, 8, "test2"));
		
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + arrayToString(traceArray));
		
//		System.out.println(rawTrace.getCompressedTrace());
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
		
		int[][] subTraceIdSequences = getSubTraceIdSequences(collector);
		int[][] nodeIdSequences = getNodeIdSequences(idToSubTraceMap);
		SimpleIntIndexer simpleIndexer = new SimpleIntIndexer(subTraceIdSequences, nodeIdSequences);
		
		int[] fullMappedTrace = executionTrace.reconstructFullMappedTrace(simpleIndexer);
		checkMappedTrace(traceArray, fullMappedTrace);
		System.out.println(Arrays.toString(fullMappedTrace));
		
		StringBuilder result = new StringBuilder("[ ");
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result.append(arrayToString(collector.getIndexer().getSequence(j))).append(" ");
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
		RawIntTraceCollector collector = new RawIntTraceCollector(outputDir);
		
		// sub trace id -> sub trace
        BufferedMap<BufferedArrayQueue<int[]>> idToSubTraceMap = generateIdToSubtraceMap(outputDir, 8, "test3");
        
		collector.addRawTraceToPool(1, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t1", 
				generateIdToSubtraceMap(outputDir, 8, "test3"));
		collector.addRawTraceToPool(2, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t2", 
				generateIdToSubtraceMap(outputDir, 8, "test3"));
		
		Integer[] traceArray = s(1,2, 3,4, 5,6, 5,7, 3,4, 5,6, 3,4, 5,6,7,8, 5,6,7,8, 5,6,7,8, 5,6,7,8, 5,6,7,8);
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3", 
				generateIdToSubtraceMap(outputDir, 8, "test3"));
		
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + arrayToString(traceArray));
		
//		System.out.println(rawTrace.getCompressedTrace());
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
		
		int[][] subTraceIdSequences = getSubTraceIdSequences(collector);
		int[][] nodeIdSequences = getNodeIdSequences(idToSubTraceMap);
		SimpleIntIndexer simpleIndexer = new SimpleIntIndexer(subTraceIdSequences, nodeIdSequences);
		
		int[] fullMappedTrace = executionTrace.reconstructFullMappedTrace(simpleIndexer);
		checkMappedTrace(traceArray, fullMappedTrace);
		System.out.println(Arrays.toString(fullMappedTrace));
		
		StringBuilder result = new StringBuilder("[ ");
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result.append(arrayToString(collector.getIndexer().getSequence(j))).append(" ");
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
		RawIntTraceCollector collector = new RawIntTraceCollector(outputDir);
		
		// sub trace id -> sub trace
        BufferedMap<BufferedArrayQueue<int[]>> idToSubTraceMap = generateIdToSubtraceMap(outputDir, 8, "test4");
        
		collector.addRawTraceToPool(1, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t1", 
				generateIdToSubtraceMap(outputDir, 8, "test4"));
		collector.addRawTraceToPool(2, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t2", 
				generateIdToSubtraceMap(outputDir, 8, "test4"));
		
		Integer[] traceArray = s(1,2,3, 4,5,6,5,6, 4,5,6,5,6,5,6, 7,8);
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3", 
				generateIdToSubtraceMap(outputDir, 8, "test4"));
		
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + arrayToString(traceArray));
		
//		System.out.println(rawTrace.getCompressedTrace());
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
		
		int[][] subTraceIdSequences = getSubTraceIdSequences(collector);
		int[][] nodeIdSequences = getNodeIdSequences(idToSubTraceMap);
		SimpleIntIndexer simpleIndexer = new SimpleIntIndexer(subTraceIdSequences, nodeIdSequences);
		
		int[] fullMappedTrace = executionTrace.reconstructFullMappedTrace(simpleIndexer);
		checkMappedTrace(traceArray, fullMappedTrace);
		System.out.println(Arrays.toString(fullMappedTrace));
		
		StringBuilder result = new StringBuilder("[ ");
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result.append(arrayToString(collector.getIndexer().getSequence(j))).append(" ");
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
		RawIntTraceCollector collector = new RawIntTraceCollector(outputDir);
		
		// sub trace id -> sub trace
        BufferedMap<BufferedArrayQueue<int[]>> idToSubTraceMap = generateIdToSubtraceMap(outputDir, 9, "test5");
        
		collector.addRawTraceToPool(1, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t1", 
				generateIdToSubtraceMap(outputDir, 9, "test5"));
		collector.addRawTraceToPool(2, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t2", 
				generateIdToSubtraceMap(outputDir, 9, "test5"));
		
		Integer[] traceArray = s(1,2,3, 4,5,6,7,7,5,6,7,7,7, 4,5,6,7,5,6,7,7,5,6,7,7,7,7, 9,8);
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3", 
				generateIdToSubtraceMap(outputDir, 9, "test5"));
		
		System.out.println(collector.getGsTree());
		
		CompressedTraceBase<Integer, ?> rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + arrayToString(traceArray));
		
//		System.out.println(rawTrace.getCompressedTrace());
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
		
		int[][] subTraceIdSequences = getSubTraceIdSequences(collector);
		int[][] nodeIdSequences = getNodeIdSequences(idToSubTraceMap);
		SimpleIntIndexer simpleIndexer = new SimpleIntIndexer(subTraceIdSequences, nodeIdSequences);
		
		int[] fullMappedTrace = executionTrace.reconstructFullMappedTrace(simpleIndexer);
		checkMappedTrace(traceArray, fullMappedTrace);
		System.out.println(Arrays.toString(fullMappedTrace));
		
		StringBuilder result = new StringBuilder("[ ");
		for (int j = 0; j < collector.getIndexer().getSequences().length; j++) {
			result.append(arrayToString(collector.getIndexer().getSequence(j))).append(" ");
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

	private String arrayToString(Integer[] traceArray) {
		if (traceArray == null) {
			return "null";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("[ ");
		for (int entry : traceArray) {
			builder.append(entry).append(", ");
		}
		builder.setLength(builder.length() > 2 ? builder.length()-2 : builder.length()-1);
		builder.append(" ]");
		return builder.toString();
	}
	
}
