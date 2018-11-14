package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

@CoverageIgnore
public class ExecutionTraceCollector {

	// the statements are stored as "class_id:statement_counter"
	public static final String SPLIT_CHAR = ":";
	
	private static final transient Lock globalExecutionTraceCollectorLock = new ReentrantLock();
	
	// shouldn't need to be thread-safe, as each thread only accesses its own trace
	private static Map<Long,List<int[]>> executionTraces = new ConcurrentHashMap<>();
	
	public static Map<Integer, int[]> classesToCounterArrayMap = new ConcurrentHashMap<Integer, int[]>();

	public static void initializeCounterArrayForClass(int classId, int countersCnt) {
		classesToCounterArrayMap.put(classId, new int[countersCnt]);
	}
	
	/**
	 * @return
	 * the collection of execution traces for all executed threads;
	 * the statements in the traces are stored as "class_id:statement_counter";
	 * also resets the internal map
	 */
	public static Map<Long,List<int[]>> getAndResetExecutionTraces() {
		globalExecutionTraceCollectorLock.lock();
		try {
			Map<Long, List<int[]>> traces = executionTraces;
			executionTraces = new ConcurrentHashMap<>();
			return traces;
		} finally {
			globalExecutionTraceCollectorLock.unlock();
		}
	}

	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param classId
	 * the unique id of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void addStatementToExecutionTraceAndIncrementCounter(int classId, int counterId) {
		addStatementToExecutionTrace(classId, counterId);
		incrementCounter(classId, counterId);
	}

	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param classId
	 * the unique id of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void variableAddStatementToExecutionTraceAndIncrementCounter(int classId, int counterId) {
		variableAddStatementToExecutionTrace(classId, counterId);
		incrementCounter(classId, counterId);
	}

	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param classId
	 * the unique id of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void jumpAddStatementToExecutionTraceAndIncrementCounter(int classId, int counterId) {
		jumpAddStatementToExecutionTrace(classId, counterId);
		incrementCounter(classId, counterId);
	}

	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param classId
	 * the unique id of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void switchAddStatementToExecutionTraceAndIncrementCounter(int classId, int counterId) {
		switchAddStatementToExecutionTrace(classId, counterId);
		incrementCounter(classId, counterId);
	}

	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param classId
	 * the unique id of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void addStatementToExecutionTrace(int classId, int counterId) {
		// get an id for the current thread
		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO
		
		// get the respective execution trace
		List<int[]> trace = executionTraces.get(threadId);
		if (trace == null) {
			trace = new ArrayList<>();
			executionTraces.put(threadId, trace);
		}
		
//		System.out.println("size: " + TouchCollector.registeredClasses.size());
//		for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
//			System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
//		}
		
//		System.out.println(classId + ":" + counterId);
		
		// add the statement to the trace
		trace.add(new int[] {classId, counterId});
	}
	
	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param classId
	 * the unique id of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void variableAddStatementToExecutionTrace(int classId, int counterId) {
		if (counterId == 0) {
			// this marks a fake jump! (ignore)
			return;
		}
		// get an id for the current thread
		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO
		
		// get the respective execution trace
		List<int[]> trace = executionTraces.get(threadId);
		if (trace == null) {
			trace = new ArrayList<>();
			executionTraces.put(threadId, trace);
		}
		
//		System.out.println("size: " + TouchCollector.registeredClasses.size());
//		for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
//			System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
//		}
		
//		System.out.println(classId + ":" + counterId + " (from variable)");
		
		// add the statement to the trace
		trace.add(new int[] {classId, counterId, 0});
	}
	
	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param classId
	 * the unique id of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void jumpAddStatementToExecutionTrace(int classId, int counterId) {
		// get an id for the current thread
		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO
		
		// get the respective execution trace
		List<int[]> trace = executionTraces.get(threadId);
		if (trace == null) {
			trace = new ArrayList<>();
			executionTraces.put(threadId, trace);
		}
		
//		System.out.println("size: " + TouchCollector.registeredClasses.size());
//		for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
//			System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
//		}
		
//		System.out.println(classId + ":" + counterId + " (from variable)");
		
		// add the statement to the trace
		trace.add(new int[] {classId, counterId, 1});
	}
	
	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param classId
	 * the unique id of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void switchAddStatementToExecutionTrace(int classId, int counterId) {
		// get an id for the current thread
		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO
		
		// get the respective execution trace
		List<int[]> trace = executionTraces.get(threadId);
		if (trace == null) {
			trace = new ArrayList<>();
			executionTraces.put(threadId, trace);
		}
		
//		System.out.println("size: " + TouchCollector.registeredClasses.size());
//		for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
//			System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
//		}
		
//		System.out.println(classId + ":" + counterId + " (from variable)");
		
		// add the statement to the trace
		trace.add(new int[] {classId, counterId, 2});
	}
	
	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param classId
	 * the unique id of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void incrementCounter(int classId, int counterId) {
		globalExecutionTraceCollectorLock.lock();
		try {
			++classesToCounterArrayMap.get(classId)[counterId];
		} finally {
			globalExecutionTraceCollectorLock.unlock();
		}
	}
	
	public static int[] getAndResetCounterArrayForClass(int classId) {
		globalExecutionTraceCollectorLock.lock();
		try {
//			String key = clazz.getName().replace('.','/');
			int[] counters = classesToCounterArrayMap.get(classId);
			if (counters != null) {
				classesToCounterArrayMap.put(classId, new int[counters.length]);
			}
			return counters;
		} finally {
			globalExecutionTraceCollectorLock.unlock();
		}
	}
	
}
