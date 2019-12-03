package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.EfficientCompressedIntegerTrace;

@CoverageIgnore
public class ExecutionTraceCollector {

	public final static int EXECUTION_TRACE_CHUNK_SIZE = 100000;
	public final static int MAP_CHUNK_SIZE = 500000;
	public static final int SUBTRACE_ARRAY_SIZE = 300;
	
	public static final int NEW_SUBTRACE_ID = 0;
	
	private static final transient Lock globalExecutionTraceCollectorLock = new ReentrantLock();

	// shouldn't need to be thread-safe, as each thread only accesses its own trace (thread id -> sequence of sub trace ids)
	private static Map<Long,EfficientCompressedIntegerTrace> executionTraces = new ConcurrentHashMap<>();

	private static int[][] classesToCounterArrayMap = new int[2048][];
	
	private static Set<Thread> currentThreads = new HashSet<>();

	public static void initializeCounterArrayForClass(int classId, int countersCnt) {
		classesToCounterArrayMap[classId] = new int[countersCnt];
	}
	
	private static Path tempDir;
	
	static {
		try {
			Path path = Paths.get(System.getProperty("user.dir")).resolve("execTracesTmp");
			path.toFile().mkdirs();
			tempDir = Files.createTempDirectory(path.toAbsolutePath(), "exec");
		} catch (IOException e) {
			e.printStackTrace();
			tempDir = null;
		}
	}
	
	
	/**
	 * @return
	 * the collection of execution traces for all executed threads;
	 * the statements in the traces are stored as "class_id:statement_counter";
	 * also resets the internal map and collects potentially remaining sub traces.
	 */
	public static Map<Long,EfficientCompressedIntegerTrace> getAndResetExecutionTraces() {
		globalExecutionTraceCollectorLock.lock();
		try {
			processAllRemainingSubTraces();
			Map<Long, EfficientCompressedIntegerTrace> traces = executionTraces;
			executionTraces = new ConcurrentHashMap<>();
			return traces;
		} finally {
			globalExecutionTraceCollectorLock.unlock();
		}
	}
	

	private static EfficientCompressedIntegerTrace getNewCollector(long threadId) {
		// do not delete buffered trace files on exit, due to possible necessary serialization
		return new EfficientCompressedIntegerTrace(tempDir.toAbsolutePath().toFile(), 
				"exec_trc_" + threadId + "-", 
				EXECUTION_TRACE_CHUNK_SIZE, MAP_CHUNK_SIZE, false, true);
	}
	
	
	/**
	 * Marks the beginning of a new sub trace by adding a special indicator to the trace.
	 */
	public static void startNewSubTrace() {
		// get an id for the current thread
		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO

		// get the thread's execution trace
		EfficientCompressedIntegerTrace trace = executionTraces.get(threadId);
		if (trace == null) {
			trace = getNewCollector(threadId);
			executionTraces.put(threadId, trace);
		}

		// add an indicator to the trace that represents a visited catch block
		trace.add(NEW_SUBTRACE_ID);

	}

	
	private static void processAllRemainingSubTraces() {
		
		for (Thread thread : currentThreads) {
			if (thread.equals(Thread.currentThread())) {
				continue;
			}
			boolean done = false;
			while (!done) {
				if (thread.isAlive()) {
					System.err.println("Thread " + thread.getId() + " is still alive. Waiting 20 seconds for it to die...");
					try {
						thread.join(20000); // wait 20 seconds for threads to die... TODO
						if (thread.isAlive()) {
							System.err.println("(At least) thread " + thread.getId() + " remains alive...");
							//						thread.interrupt();
							break;
						}
						done = true;
					} catch (InterruptedException e) {
						// try again
					}
				} else {
					break;
				}
			}
		}
		
		currentThreads.clear();
		
		globalExecutionTraceCollectorLock.lock();
		try {
			// store execution traces
			Iterator<Entry<Long, EfficientCompressedIntegerTrace>> iterator2 = executionTraces.entrySet().iterator();
			while (iterator2.hasNext()) {
				try {
					iterator2.next().getValue().sleep();
				} catch (Exception e) {
					e.printStackTrace();
					// something went wrong...
					iterator2.remove();
				}
			}
			
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
		addStatementToExecutionTrace(classId, counterId, CoberturaStatementEncoding.NORMAL_ID);
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
		addStatementToExecutionTrace(classId, counterId, CoberturaStatementEncoding.BRANCH_ID);
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
		addStatementToExecutionTrace(classId, counterId, CoberturaStatementEncoding.JUMP_ID);
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
//		processLastSubTrace();
		addStatementToExecutionTrace(classId, counterId, CoberturaStatementEncoding.SWITCH_ID);
		incrementCounter(classId, counterId);
	}
	

	private static void addStatementToExecutionTrace(int classId, int counterId, int specialIndicatorId) {
		if (counterId == AbstractCodeProvider.FAKE_COUNTER_ID) {
			// this marks a fake jump! (ignore)
			return;
		}
		
		// get an id for the current thread
		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO

		// get the thread's execution trace
		EfficientCompressedIntegerTrace trace = executionTraces.get(threadId);
		if (trace == null) {
			trace = getNewCollector(threadId);
			executionTraces.put(threadId, trace);
		}
		
		// add the statement to the execution trace
		trace.add(CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId, counterId));
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
//		globalExecutionTraceCollectorLock.lock();
//		try {
//		++classesToCounterArrayMap.get(classId)[counterId];
		++classesToCounterArrayMap[classId][counterId];
//		} finally {
//			globalExecutionTraceCollectorLock.unlock();
//		}
	}
	
	public static int[] getAndResetCounterArrayForClass(int classId) {
		globalExecutionTraceCollectorLock.lock();
		try {
//			String key = clazz.getName().replace('.','/');
			
//			int[] counters = classesToCounterArrayMap.get(classId);
//			if (counters != null) {
//				classesToCounterArrayMap.put(classId, new int[counters.length]);
//			}
			
			int[] counters = classesToCounterArrayMap[classId];
			if (counters != null) {
				classesToCounterArrayMap[classId] = new int[counters.length];
			}
			return counters;
		} finally {
			globalExecutionTraceCollectorLock.unlock();
		}
	}
	
}
