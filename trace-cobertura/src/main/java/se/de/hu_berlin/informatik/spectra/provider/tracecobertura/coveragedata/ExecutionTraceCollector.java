package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

@CoverageIgnore
public class ExecutionTraceCollector {

	// the statements are stored as "class_id:statement_counter"
	public static final String SPLIT_CHAR = ":";
	
	// shouldn't need to be thread-safe, as each thread only accesses its own trace
	private static Map<Long,List<String>> executionTraces = new HashMap<>();
	
	public static Map<String, int[]> registeredClassesToCounterArrayMap = new HashMap<String, int[]>();

	public static void initializeCounterArrayForClass(String clazz, int countersCnt) {
		registeredClassesToCounterArrayMap.put(clazz, new int[countersCnt]);
	}
	
	/**
	 * @return
	 * the collection of execution traces for all executed threads;
	 * the statements in the traces are stored as "class_id:statement_counter";
	 * also resets the internal map
	 */
	public static Map<Long,List<String>> getAndResetExecutionTraces() {
		synchronized (ExecutionTraceCollector.class) {
			Map<Long, List<String>> traces = executionTraces;
			executionTraces = new HashMap<>();
			return traces;
		}
	}

	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param className
	 * the name of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void addStatementToExecutionTraceAndIncrementCounter(String className, int counterId) {
		addStatementToExecutionTrace(className, counterId);
		incrementCounter(className, counterId);
	}
	
	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param className
	 * the name of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void variableAddStatementToExecutionTraceAndIncrementCounter(String className, int counterId) {
		variableAddStatementToExecutionTrace(className, counterId);
		incrementCounter(className, counterId);
	}
	
	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param className
	 * the name of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void addStatementToExecutionTrace(String className, int counterId) {
		// get an id for the current thread
		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO
		
		// get the respective execution trace
		List<String> trace = executionTraces.get(threadId);
		if (trace == null) {
			trace = new ArrayList<>();
			executionTraces.put(threadId, trace);
		}
		
//		System.out.println("size: " + TouchCollector.registeredClasses.size());
//		for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
//			System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
//		}
		
//		System.out.println(className + ":" + counterId);
		
		// add the statement to the trace
		trace.add(String.valueOf(TouchCollector.registeredClassesStringsToIdMap.get(className)) 
				+ SPLIT_CHAR + String.valueOf(counterId));
	}
	
	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param className
	 * the name of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void variableAddStatementToExecutionTrace(String className, int counterId) {
		// get an id for the current thread
		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO
		
		// get the respective execution trace
		List<String> trace = executionTraces.get(threadId);
		if (trace == null) {
			trace = new ArrayList<>();
			executionTraces.put(threadId, trace);
		}
		
//		System.out.println("size: " + TouchCollector.registeredClasses.size());
//		for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
//			System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
//		}
		
//		System.out.println(className + ":" + counterId + " (from variable)");
		
		// add the statement to the trace
		trace.add(String.valueOf(TouchCollector.registeredClassesStringsToIdMap.get(className)) 
				+ SPLIT_CHAR + String.valueOf(counterId) + SPLIT_CHAR + "0");
	}
	
	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * @param className
	 * the name of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void incrementCounter(String className, int counterId) {
		++registeredClassesToCounterArrayMap.get(className)[counterId];
	}
	
	/**
	 * @return
	 * the map of generated IDs for class names, as used by cobertura;
	 * also resets the internal structures
	 */
	public static Map<Integer, String> getIdToClassNameMap() {
		synchronized (ExecutionTraceCollector.class) {
			return TouchCollector.registeredClassesIdToStringsMap;
		}
	}
	
	public static int[] getAndResetCounterArrayForClass(Class<?> clazz) {
		synchronized (ExecutionTraceCollector.class) {
			String key = clazz.getName().replace('.','/');
			int[] counters = registeredClassesToCounterArrayMap.get(key);
			if (counters != null) {
				registeredClassesToCounterArrayMap.put(key, new int[counters.length]);
			}
			return counters;
		}
	}
	
}
