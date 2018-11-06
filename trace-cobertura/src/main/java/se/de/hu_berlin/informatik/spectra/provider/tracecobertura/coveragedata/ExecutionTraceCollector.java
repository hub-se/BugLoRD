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
	
	// assign each class an id
//	private static Map<String,Integer> classNameToIdMap = new HashMap<>();
//	private static Map<Integer,String> idToClassNameMap = new HashMap<>();
//	private static AtomicInteger currentIndex = new AtomicInteger(0);
//	private static Long firstThreadId = null;
//	private static boolean synchronize = false;
	
	// shouldn't need to be thread-safe, as each thread only accesses its own trace
	private static Map<Long,List<String>> executionTraces = new HashMap<>();

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
	public static void addStatementToExecutionTrace(String className, int counterId) {
		// get an id for the current thread
		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO
		
//		// try to do as less synchronization as possible
//		if (firstThreadId == null) {
//			// first entry of a thread
//			synchronized (ExecutionTraceCollector.class) {
//				// make sure to synchronize in case of another thread getting here
//				if (firstThreadId == null) {
//					// we are sure that we are still the first thread to get here
//					firstThreadId = threadId;
//				} else if (threadId != firstThreadId.longValue()) {
//					// we are another thread (very unlikely, if at all possible)
//					synchronize = true;
//				}
//			}
//		} else if (threadId != firstThreadId.longValue()) {
//			// we have seen another thread, so better start synchronization...
//			synchronize = true;
//		}
//
//		// get the class id (synchronize, if necessary)
//		Integer classId = null;
//		if (synchronize) {
//			synchronized (ExecutionTraceCollector.class) {
//				classId = getClassId(className);
//			}
//		} else {
//			// theoretically, this could still be reached by 
//			// another thread that is not yet synchronized;
//			// it's very unlikely, though
//			classId = getClassId(className);
//		}
		
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
		
//		System.out.println(className);
		
		// add the statement to the trace
		trace.add(String.valueOf(TouchCollector.registeredClassesStringsToIdMap.get(className)) 
				+ SPLIT_CHAR + String.valueOf(counterId));
	}

//	private static Integer getClassId(String className) {
//		Integer classId;
//		// get the id for the class; generate a new one if none does exist
//		classId = classNameToIdMap.get(className);
//		if (classId == null) {
//			classId = currentIndex.getAndIncrement();
//			idToClassNameMap.put(classId, className);
//			classNameToIdMap.put(className, classId);
//		}
//		return classId;
//	}
	
	/**
	 * @return
	 * the map of generated IDs for class names, as used by cobertura;
	 * also resets the internal structures
	 */
	public static Map<Integer, String> getAndResetIdToClassNameMap() {
		synchronized (ExecutionTraceCollector.class) {
//			Map<Integer, String> map = idToClassNameMap;
//			classNameToIdMap = new HashMap<>();
//			idToClassNameMap = new HashMap<>();
//			currentIndex.set(0);
//			firstThreadId = null;
//			synchronize = false;
			return TouchCollector.registeredClassesIdToStringsMap;
		}
	}
	
}
