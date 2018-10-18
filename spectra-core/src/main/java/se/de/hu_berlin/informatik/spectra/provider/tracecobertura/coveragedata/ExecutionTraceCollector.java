package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutionTraceCollector {

	// the statements are stored as "class_id:statement_counter"
	public static final String SPLIT_CHAR = ":";
	
	// assign each class an id
	private static Map<String,Integer> classNameToIdMap = new ConcurrentHashMap<>();
	private static Map<Integer,String> idToClassNameMap = new HashMap<>();
	private static AtomicInteger currentIndex = new AtomicInteger(0);
	
	// shouldn't need to be thread-safe, as each thread only accesses its own trace
	private static Map<Long,List<String>> executionTraces = new HashMap<>();

	/**
	 * @return
	 * the collection of execution traces for all executed threads;
	 * the statements in the traces are stored as "class_id:statement_counter";
	 * also resets the internal map
	 */
	public static Map<Long,List<String>> getAndResetExecutionTraces() {
		Map<Long, List<String>> traces = executionTraces;
		executionTraces = new HashMap<>();
		return traces;
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

		// get the id for the class; generate a new one if none does exist
		int classId = classNameToIdMap.computeIfAbsent(className, 
				k -> {
					int index = currentIndex.getAndIncrement();
					idToClassNameMap.put(index, k);
					return index;
				});
		
		// get the respective execution trace
		List<String> trace = executionTraces.computeIfAbsent(threadId,
				k -> {
					return new ArrayList<>();
				});
		
		// add the statement to the trace
		trace.add(String.valueOf(classId) + SPLIT_CHAR + String.valueOf(counterId));
	}
	
	/**
	 * @return
	 * the map of generated IDs for class names, as used by cobertura;
	 * also resets the internal structures
	 */
	public static Map<Integer, String> getAndResetIdToClassNameMap() {
		Map<Integer, String> map = idToClassNameMap;
		classNameToIdMap = new ConcurrentHashMap<>();
		idToClassNameMap = new HashMap<>();
		currentIndex = new AtomicInteger(0);
		return map;
	}
	
}
