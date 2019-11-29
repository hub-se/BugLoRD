package se.de.hu_berlin.informatik.spectra.core.traces;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipException;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedMap;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.EfficientCompressedIntegerTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.TraceIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.ReplaceableCloneableIterator;
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

	private int[] s(int... numbers) {
//		int[] result = new int[numbers.length];
//		for (int i = 0; i < numbers.length; ++i) {
//			result[i] =numbers[i];
//		}
		return numbers;
	}
	
	private int[][] rt(int... numbers) {
		int[][] result = new int[numbers.length][];
		for (int i = 0; i < numbers.length; ++i) {
			result[i] = new int[] {0,numbers[i],0};
		}
		return result;
	}
	
	private int[][] getSubTraceIdSequences(RawIntTraceCollector collector) {
		int[][] subTraceIdSequences;
		IntArraySequenceIndexer indexer = collector.getIndexer();
		
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
	
	private EfficientCompressedIntegerTrace[] getNodeIdSequences(Path outputDir, Map<Integer, EfficientCompressedIntegerTrace> idToSubTraceMap) {
		// indexer.getSequences() will generate all sequences of sub trace IDs that exist in the GS tree
		EfficientCompressedIntegerTrace[] nodeIdSequences = new EfficientCompressedIntegerTrace[idToSubTraceMap.size()+1];

		// id 0 marks an empty sub trace... should not really happen... :/
		nodeIdSequences[0] = null;
		for (int i = 1; i < idToSubTraceMap.size() + 1; i++) {
			EfficientCompressedIntegerTrace list = idToSubTraceMap.get(i);
			ReplaceableCloneableIterator sequenceIterator = list.iterator();
			BufferedIntArrayQueue traceOfNodeIDs = new BufferedIntArrayQueue(outputDir.toFile(), 
					String.valueOf(UUID.randomUUID()), 100);
			
			while (sequenceIterator.hasNext()) {
				int statement = sequenceIterator.next();
				traceOfNodeIDs.add(CoberturaStatementEncoding.getCounterId(statement));
			}
			
			nodeIdSequences[i] = new EfficientCompressedIntegerTrace(traceOfNodeIDs, false);
			
//			nodeIdSequences[i] = new int[traceOfNodeIDs.size()];
//			for (int j = 0; j < nodeIdSequences[i].length; ++j) {
//				nodeIdSequences[i][j] = traceOfNodeIDs.remove();
//			}
		}
		
		return nodeIdSequences;
	}
	
	private Map<Integer,EfficientCompressedIntegerTrace> generateIdToSubtraceMap(Path outputDir, int max, String filePrefix) {
//		Map<Integer,BufferedArrayQueue<int[]>> idToSubTraceMap = new BufferedMap<>(outputDir.toFile(), filePrefix);
		Map<Integer, EfficientCompressedIntegerTrace> idToSubTraceMap = new HashMap<>();
        for (int i = 1; i <= max; ++i) {
        	idToSubTraceMap.put(i,asList(outputDir, rt(i+10)));
        }
		return idToSubTraceMap;
	}
	
	private EfficientCompressedIntegerTrace asList(Path outputDir, int[][] rt) {
		BufferedIntArrayQueue list = new BufferedIntArrayQueue(outputDir.toFile(), 
				String.valueOf(UUID.randomUUID()), rt.length);
		for (int[] statement : rt) {
			list.add(CoberturaStatementEncoding.generateUniqueRepresentationForStatement(statement[0], statement[1]));
		}
		return new EfficientCompressedIntegerTrace(list, false);
	}

	private void checkMappedTrace(int[] traceArray, int[] fullMappedTrace) {
		Assert.assertEquals(traceArray.length, fullMappedTrace.length);
		for (int i = 0; i < traceArray.length; i++) {
			Assert.assertEquals(traceArray[i]+10, fullMappedTrace[i]);
		}
	}

	@Test
	public void testAddRawTraceToPool() throws ZipException {
		Path outputDir = Paths.get(getStdTestDir()).resolve("test1");
		RawIntTraceCollector collector = new RawIntTraceCollector(outputDir);
		
		// sub trace id -> sub trace
        Map<Integer, EfficientCompressedIntegerTrace> idToSubTraceMap = generateIdToSubtraceMap(outputDir, 8, "test1");
        
		collector.addRawTraceToPool(1, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t1", 
				generateIdToSubtraceMap(outputDir, 8, "test1"));
		collector.addRawTraceToPool(2, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t2", 
				generateIdToSubtraceMap(outputDir, 8, "test1"));
		
		//                    0    1    2    4    4    1    2    4    4    1    2    1      3      ????
		int[] traceArray = s(1,2, 3,4, 5,6, 5,7, 5,7, 3,4, 5,6, 5,7, 5,7, 3,4, 5,6, 3,4, 5,6,7,8);
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3", 
				generateIdToSubtraceMap(outputDir, 8, "test1"));
		
//		Thread.sleep(20000);
		System.out.println(collector.getGsTree());
		
		EfficientCompressedIntegerTrace rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + arrayToString(traceArray));
		
//		System.out.println(rawTrace.getCompressedTrace());
		System.out.println(mapToString(rawTrace.getRepetitionMarkers(0)));
		System.out.println(mapToString(rawTrace.getRepetitionMarkers(1)));
		TraceIterator traceIterator = rawTrace.iterator();
		while (traceIterator.hasNext()) {
			System.out.print(traceIterator.next() + ", ");
		}
		System.out.println();
		for (TraceIterator iterator = rawTrace.iterator(); iterator.hasNext();) {
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
		
		ExecutionTrace executionTrace = collector.calculateExecutionTraces(3, true).get(0);
		collector.getIndexer().getSequences();
		
		int[][] subTraceIdSequences = getSubTraceIdSequences(collector);
		EfficientCompressedIntegerTrace[] nodeIdSequences = getNodeIdSequences(outputDir, idToSubTraceMap);
		SimpleIntIndexerCompressed simpleIndexer = new SimpleIntIndexerCompressed(subTraceIdSequences, nodeIdSequences);
		
		
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
		System.out.println(mapToString(executionTrace.getRepetitionMarkers(0)));
		System.out.println(mapToString(executionTrace.getRepetitionMarkers(1)));
		TraceIterator eTraceIterator = executionTrace.iterator();
		while (eTraceIterator.hasNext()) {
			System.out.print(eTraceIterator.next() + ", ");
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
	public void testAddRawTraceToPool2() throws ZipException {
		Path outputDir = Paths.get(getStdTestDir()).resolve("test2");
		RawIntTraceCollector collector = new RawIntTraceCollector(outputDir);
		
		// sub trace id -> sub trace
		Map<Integer, EfficientCompressedIntegerTrace> idToSubTraceMap = generateIdToSubtraceMap(outputDir, 8, "test2");
        
		collector.addRawTraceToPool(1, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t1", 
				generateIdToSubtraceMap(outputDir, 8, "test2"));
		collector.addRawTraceToPool(2, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t2", 
				generateIdToSubtraceMap(outputDir, 8, "test2"));
		
		int[] traceArray = s(1,2, 3,4, 1,2, 3,4, 1,2, 3,4, 3,4, 5,6, 5,6, 5,7, 3,4, 5,6, 3,4, 5,6,7,8);
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3", 
				generateIdToSubtraceMap(outputDir, 8, "test2"));
		
		System.out.println(collector.getGsTree());
		
		EfficientCompressedIntegerTrace rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + arrayToString(traceArray));
		
//		System.out.println(rawTrace.getCompressedTrace());
		System.out.println(mapToString(rawTrace.getRepetitionMarkers(0)));
		System.out.println(mapToString(rawTrace.getRepetitionMarkers(1)));
		TraceIterator traceIterator = rawTrace.iterator();
		while (traceIterator.hasNext()) {
			System.out.print(traceIterator.next() + ", ");
		}
		System.out.println();
		for (TraceIterator iterator = rawTrace.iterator(); iterator.hasNext();) {
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
		
		ExecutionTrace executionTrace = collector.calculateExecutionTraces(3, true).get(0);
		collector.getIndexer().getSequences();
		
		int[][] subTraceIdSequences = getSubTraceIdSequences(collector);
		EfficientCompressedIntegerTrace[] nodeIdSequences = getNodeIdSequences(outputDir, idToSubTraceMap);
		SimpleIntIndexerCompressed simpleIndexer = new SimpleIntIndexerCompressed(subTraceIdSequences, nodeIdSequences);
		
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
		System.out.println(mapToString(executionTrace.getRepetitionMarkers(0)));
		System.out.println(mapToString(executionTrace.getRepetitionMarkers(1)));
		TraceIterator eTraceIterator = executionTrace.iterator();
		while (eTraceIterator.hasNext()) {
			System.out.print(eTraceIterator.next() + ", ");
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
	public void testAddRawTraceToPool3() throws ZipException {
		Path outputDir = Paths.get(getStdTestDir()).resolve("test3");
		RawIntTraceCollector collector = new RawIntTraceCollector(outputDir);
		
		// sub trace id -> sub trace
		Map<Integer, EfficientCompressedIntegerTrace> idToSubTraceMap = generateIdToSubtraceMap(outputDir, 8, "test3");
        
		collector.addRawTraceToPool(1, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t1", 
				generateIdToSubtraceMap(outputDir, 8, "test3"));
		collector.addRawTraceToPool(2, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t2", 
				generateIdToSubtraceMap(outputDir, 8, "test3"));
		
		int[] traceArray = s(1,2, 3,4, 5,6, 5,7, 3,4, 5,6, 3,4, 5,6,7,8, 5,6,7,8, 5,6,7,8, 5,6,7,8, 5,6,7,8);
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3", 
				generateIdToSubtraceMap(outputDir, 8, "test3"));
		
		System.out.println(collector.getGsTree());
		
		EfficientCompressedIntegerTrace rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + arrayToString(traceArray));
		
//		System.out.println(rawTrace.getCompressedTrace());
		System.out.println(mapToString(rawTrace.getRepetitionMarkers(0)));
		System.out.println(mapToString(rawTrace.getRepetitionMarkers(1)));
		TraceIterator traceIterator = rawTrace.iterator();
		while (traceIterator.hasNext()) {
			System.out.print(traceIterator.next() + ", ");
		}
		System.out.println();
		for (TraceIterator iterator = rawTrace.iterator(); iterator.hasNext();) {
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
		
		ExecutionTrace executionTrace = collector.calculateExecutionTraces(3, true).get(0);
		collector.getIndexer().getSequences();
		
		int[][] subTraceIdSequences = getSubTraceIdSequences(collector);
		EfficientCompressedIntegerTrace[] nodeIdSequences = getNodeIdSequences(outputDir, idToSubTraceMap);
		SimpleIntIndexerCompressed simpleIndexer = new SimpleIntIndexerCompressed(subTraceIdSequences, nodeIdSequences);
		
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
		System.out.println(mapToString(executionTrace.getRepetitionMarkers(0)));
		System.out.println(mapToString(executionTrace.getRepetitionMarkers(1)));
		TraceIterator eTraceIterator = executionTrace.iterator();
		while (eTraceIterator.hasNext()) {
			System.out.print(eTraceIterator.next() + ", ");
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
	public void testAddRawTraceToPool4() throws ZipException {
		Path outputDir = Paths.get(getStdTestDir()).resolve("test4");
		RawIntTraceCollector collector = new RawIntTraceCollector(outputDir);
		
		// sub trace id -> sub trace
		Map<Integer, EfficientCompressedIntegerTrace> idToSubTraceMap = generateIdToSubtraceMap(outputDir, 8, "test4");
        
		collector.addRawTraceToPool(1, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t1", 
				generateIdToSubtraceMap(outputDir, 8, "test4"));
		collector.addRawTraceToPool(2, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t2", 
				generateIdToSubtraceMap(outputDir, 8, "test4"));
		
		int[] traceArray = s(1,2,3, 4,5,6,5,6, 4,5,6,5,6,5,6, 7,8);
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3", 
				generateIdToSubtraceMap(outputDir, 8, "test4"));
		
		System.out.println(collector.getGsTree());
		
		EfficientCompressedIntegerTrace rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + arrayToString(traceArray));
		
//		System.out.println(rawTrace.getCompressedTrace());
		System.out.println(mapToString(rawTrace.getRepetitionMarkers(0)));
		System.out.println(mapToString(rawTrace.getRepetitionMarkers(1)));
		TraceIterator traceIterator = rawTrace.iterator();
		while (traceIterator.hasNext()) {
			System.out.print(traceIterator.next() + ", ");
		}
		System.out.println();
		for (TraceIterator iterator = rawTrace.iterator(); iterator.hasNext();) {
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
		
		ExecutionTrace executionTrace = collector.calculateExecutionTraces(3, true).get(0);
		collector.getIndexer().getSequences();
		
		int[][] subTraceIdSequences = getSubTraceIdSequences(collector);
		EfficientCompressedIntegerTrace[] nodeIdSequences = getNodeIdSequences(outputDir, idToSubTraceMap);
		SimpleIntIndexerCompressed simpleIndexer = new SimpleIntIndexerCompressed(subTraceIdSequences, nodeIdSequences);
		
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
		System.out.println(mapToString(executionTrace.getRepetitionMarkers(0)));
		System.out.println(mapToString(executionTrace.getRepetitionMarkers(1)));
		TraceIterator eTraceIterator = executionTrace.iterator();
		while (eTraceIterator.hasNext()) {
			System.out.print(eTraceIterator.next() + ", ");
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
	public void testAddRawTraceToPool5() throws ZipException {
		Path outputDir = Paths.get(getStdTestDir()).resolve("test5");
		RawIntTraceCollector collector = new RawIntTraceCollector(outputDir);
		
		// sub trace id -> sub trace
		Map<Integer, EfficientCompressedIntegerTrace> idToSubTraceMap = generateIdToSubtraceMap(outputDir, 9, "test5");
        
		collector.addRawTraceToPool(1, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t1", 
				generateIdToSubtraceMap(outputDir, 9, "test5"));
		collector.addRawTraceToPool(2, 0, s(1,2, 3,4,5,6, 3,4,5,6, 3,4,5,6,7,8), true, outputDir, "t2", 
				generateIdToSubtraceMap(outputDir, 9, "test5"));
		
		int[] traceArray = s(1,2,3, 4,5,6,7,7,5,6,7,7,7, 4,5,6,7,5,6,7,7,5,6,7,7,7,7, 9,8);
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3", 
				generateIdToSubtraceMap(outputDir, 9, "test5"));
		
		System.out.println(collector.getGsTree());
		
		EfficientCompressedIntegerTrace rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + arrayToString(traceArray));
		
//		System.out.println(rawTrace.getCompressedTrace());
		System.out.println(mapToString(rawTrace.getRepetitionMarkers(0)));
		System.out.println(mapToString(rawTrace.getRepetitionMarkers(1)));
		TraceIterator traceIterator = rawTrace.iterator();
		while (traceIterator.hasNext()) {
			System.out.print(traceIterator.next() + ", ");
		}
		System.out.println();
		for (TraceIterator iterator = rawTrace.iterator(); iterator.hasNext();) {
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
		
		ExecutionTrace executionTrace = collector.calculateExecutionTraces(3, true).get(0);
		collector.getIndexer().getSequences();
		
		int[][] subTraceIdSequences = getSubTraceIdSequences(collector);
		EfficientCompressedIntegerTrace[] nodeIdSequences = getNodeIdSequences(outputDir, idToSubTraceMap);
		SimpleIntIndexerCompressed simpleIndexer = new SimpleIntIndexerCompressed(subTraceIdSequences, nodeIdSequences);
		
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
		System.out.println(mapToString(executionTrace.getRepetitionMarkers(0)));
		System.out.println(mapToString(executionTrace.getRepetitionMarkers(1)));
		TraceIterator eTraceIterator = executionTrace.iterator();
		while (eTraceIterator.hasNext()) {
			System.out.print(eTraceIterator.next() + ", ");
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
	public void testAddRawTraceToPool6() throws ZipException {
		Path outputDir = Paths.get(getStdTestDir()).resolve("test6");
		RawIntTraceCollector collector = new RawIntTraceCollector(outputDir);
		
		// sub trace id -> sub trace
		Map<Integer, EfficientCompressedIntegerTrace> idToSubTraceMap = generateIdToSubtraceMap(outputDir, 3, "test6");
        
		collector.addRawTraceToPool(1, 0, s(1,2,3, 1,2,3, 1,2,3), true, outputDir, "t1", 
				generateIdToSubtraceMap(outputDir, 3, "test6"));
		collector.addRawTraceToPool(2, 0, s(1,2,3, 1,2,3), true, outputDir, "t2", 
				generateIdToSubtraceMap(outputDir, 3, "test6"));
		
		int[] traceArray = s(1,2,3, 1,2,3);
		collector.addRawTraceToPool(3, 0, traceArray, true, outputDir, "t3", 
				generateIdToSubtraceMap(outputDir, 3, "test6"));
		
		System.out.println(collector.getGsTree());
		
		EfficientCompressedIntegerTrace rawTrace = collector.getRawTraces(3).get(0);
		
		System.out.println(traceArray.length + ", " + arrayToString(traceArray));
		
//		System.out.println(rawTrace.getCompressedTrace());
		System.out.println(mapToString(rawTrace.getRepetitionMarkers(0)));
//		System.out.println(mapToString(rawTrace.getRepetitionMarkers(1)));
		TraceIterator traceIterator = rawTrace.iterator();
		while (traceIterator.hasNext()) {
			System.out.print(traceIterator.next() + ", ");
		}
		System.out.println();
		for (TraceIterator iterator = rawTrace.iterator(); iterator.hasNext();) {
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
		
		ExecutionTrace executionTrace = collector.calculateExecutionTraces(3, true).get(0);
		collector.getIndexer().getSequences();
		
		int[][] subTraceIdSequences = getSubTraceIdSequences(collector);
		EfficientCompressedIntegerTrace[] nodeIdSequences = getNodeIdSequences(outputDir, idToSubTraceMap);
		SimpleIntIndexerCompressed simpleIndexer = new SimpleIntIndexerCompressed(subTraceIdSequences, nodeIdSequences);
		
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
		System.out.println(mapToString(executionTrace.getRepetitionMarkers(0)));
//		System.out.println(mapToString(executionTrace.getRepetitionMarkers(1)));
		TraceIterator eTraceIterator = executionTrace.iterator();
		while (eTraceIterator.hasNext()) {
			System.out.print(eTraceIterator.next() + ", ");
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

	private String arrayToString(int[] traceArray) {
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
