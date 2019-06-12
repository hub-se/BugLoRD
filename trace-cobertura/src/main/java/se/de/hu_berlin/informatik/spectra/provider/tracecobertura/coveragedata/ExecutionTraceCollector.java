package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

@CoverageIgnore
public class ExecutionTraceCollector {

	public final static int CHUNK_SIZE = 250000;
	
//	private static ExecutorService executorService = new ThreadPoolExecutor(1, 1,
//			0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
//			new ThreadFactory() {
//
//		ThreadFactory factory = Executors.defaultThreadFactory();
//
//		// int counter = 0;
//		@Override
//		public Thread newThread(Runnable r) {
//			// ++counter;
//			// Log.out(this, "Creating Thread no. %d for %s.",
//			// counter, r);
//			Thread thread = factory.newThread(r);
//			return thread;
//		}
//	}) {
//
//		protected void afterExecute(Runnable r, Throwable t) {
//			super.afterExecute(r, t);
//			if (t == null && r instanceof Future<?>) {
//				try {
//					Future<?> future = (Future<?>) r;
//					if (future.isDone()) {
//						future.get();
//					}
//				} catch (CancellationException ce) {
//					t = ce;
//				} catch (ExecutionException ee) {
//					t = ee.getCause();
//				} catch (InterruptedException ie) {
//					Thread.currentThread().interrupt();
//				}
//			}
//			if (t != null) {
//				System.err.println(t);
//				t.printStackTrace();
//			}
//		}
//	};

	private static final transient Lock globalExecutionTraceCollectorLock = new ReentrantLock();

	// shouldn't need to be thread-safe, as each thread only accesses its own trace (thread id -> sequence of sub trace ids)
	private static Map<Long,BufferedArrayQueue<Integer>> executionTraces = new ConcurrentHashMap<>();
	// stores (sub trace id -> subTrace)
	private static BufferedMap<BufferedArrayQueue<int[]>> existingSubTraces = null;
	// stores (sub trace wrapper -> sub trace id) to retrieve subtrace ids
	// the integer array in the wrapper has to contain start and ending node of the sub trace
	// if the sub trace is longer than one statement
	private static Map<Long,Integer> subTraceIdMap = new ConcurrentHashMap<>();
	private static volatile int currentId = 0; 
	// lock for getting/generating sub trace ids (ensures that sub trace ids are unique)
	private static final transient Lock idLock = new ReentrantLock();
	// stores currently built up execution trace parts for each thread (thread id -> sub trace)
	private static Map<Long,BufferedArrayQueue<int[]>> currentSubTraces = new ConcurrentHashMap<>();
	
	public static final Map<Integer, int[]> classesToCounterArrayMap = new HashMap<>();

	private static final int SUBTRACE_ARRAY_SIZE = 500;

	public static void initializeCounterArrayForClass(int classId, int countersCnt) {
		classesToCounterArrayMap.put(classId, new int[countersCnt]);
	}
	
	private static Path tempDir;
	
	static {
		try {
			Path path = Paths.get(System.getProperty("user.dir")).resolve("execTracesTmp");
			path.toFile().mkdirs();
			tempDir = Files.createTempDirectory(path.toAbsolutePath(), "exec");
			existingSubTraces = getNewSubTraceMap();
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
	public static Map<Long,BufferedArrayQueue<Integer>> getAndResetExecutionTraces() {
		globalExecutionTraceCollectorLock.lock();
		try {
			processAllRemainingSubTraces();
			Map<Long, BufferedArrayQueue<Integer>> traces = executionTraces;
			executionTraces = new ConcurrentHashMap<>();
			return traces;
		} finally {
			globalExecutionTraceCollectorLock.unlock();
		}
	}
	
	/**
	 * @return
	 * The map of ids to actual sub traces; also resets the internal map
	 */
	public static BufferedMap<BufferedArrayQueue<int[]>> getAndResetIdToSubtraceMap() {
		globalExecutionTraceCollectorLock.lock();
		try {
			// process all remaining sub traces. Just to be safe!
			processAllRemainingSubTraces();
			// sub trace ids that stay consistent throughout the entire time!!??? TODO
			BufferedMap<BufferedArrayQueue<int[]>> traceMap = existingSubTraces;
			// reset id counter and map!
			currentId = 0;
//			existingSubTraces = null;
			existingSubTraces = getNewSubTraceMap();
			subTraceIdMap.clear();
			return traceMap;
		} finally {
			globalExecutionTraceCollectorLock.unlock();
		}
	}
	
	private static BufferedArrayQueue<Integer> getNewCollector(long threadId) {
		// do not delete buffered trace files on exit, due to possible necessary serialization
		return new BufferedArrayQueue<>(tempDir.toAbsolutePath().toFile(), 
				threadId + "-" + String.valueOf(UUID.randomUUID()), CHUNK_SIZE, false);
	}
	
	
	private static int getOrCreateIdForSubTrace(BufferedArrayQueue<int[]> subTrace) {
		if (subTrace == null || subTrace.isEmpty()) {
			// id 0 indicates empty sub trace
			return 0;
		}

		idLock.lock();
		try {
			long wrapper = generateUniqueRepresentationForSubTrace(subTrace);
			Integer id = subTraceIdMap.get(wrapper);
			if (id == null) {
				// starts with id 1
				id = ++currentId;
				// new sub trace, so store new id and store sub trace
				subTraceIdMap.put(wrapper, currentId);
				if (existingSubTraces == null) {
					existingSubTraces = getNewSubTraceMap();
				}
				subTrace.sleep();
				existingSubTraces.put(currentId, subTrace);
//				System.out.println(currentId + ":" + wrapper.toString());
			}

			return id;
		} finally {
			idLock.unlock();
		}
	}
	
	// store the starting and ending statements in a single long value!
	// should represent a sub trace uniquely!
	// uses 10 bit for the class identifier (max 1024);
	// uses 22 bit for the counter id (max 4,194,304);
	// a counter with id 0 can not occur!
	public static long generateUniqueRepresentationForSubTrace(BufferedArrayQueue<int[]> subTrace) {
		if (subTrace.isEmpty()) {
			return 0;
		} else if (subTrace.size() == 1) {
			return ((long)subTrace.peek()[0] << 22) 
					+ subTrace.peek()[1];
		} else {
			return ((((long)subTrace.peek()[0] << 22) 
					+ subTrace.peek()[1] << 10) 
					+ subTrace.peekLast()[0] << 22) 
					+ subTrace.peekLast()[1];
		}
	}
	
	private static BufferedMap<BufferedArrayQueue<int[]>> getNewSubTraceMap() {
		// do not delete buffered map on exit, due to possible necessary serialization
		return new BufferedMap<>(tempDir.toAbsolutePath().toFile(), 
				String.valueOf(UUID.randomUUID()), 2*CHUNK_SIZE, false);
	}
	
	private static BufferedArrayQueue<int[]> getNewSubtrace() {
		return new BufferedArrayQueue<>(tempDir.toAbsolutePath().toFile(), 
				String.valueOf(UUID.randomUUID()), SUBTRACE_ARRAY_SIZE, false);
	}

	
	/**
	 * This method should be called after each decision point.
	 * After the instrumented program has finished execution, 
	 * {@link ExecutionTraceCollector#processAllRemainingSubTraces()}
	 * should be called to collect the remaining sub traces.
	 */
	public static void processLastSubTrace() {
		// get an id for the current thread
		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO

		processLastSubtraceForThreadId(threadId, currentSubTraces.remove(threadId));
	}

	private static void processLastSubtraceForThreadId(long threadId, BufferedArrayQueue<int[]> subTrace) {
//		// do more expensive operations in a separate thread?
//		return executorService.submit(new SubTraceProcessor(threadId, subTrace));
		
//		// get the respective execution trace
//		BufferedArrayQueue<Integer> trace = executionTraces.get(threadId);
//		if (trace == null) {
//			trace = getNewCollector(threadId);
//			executionTraces.put(threadId, trace);
//		}
		
		// get or create id for sub trace
//		System.out.println(queueToString(subTrace));
		int id = getOrCreateIdForSubTrace(subTrace);
//		System.out.println(id + ": " + queueToString(subTrace));
		
//		// add the sub trace's id to the trace
//		trace.add(id);
					
		
//		System.out.println("size: " + TouchCollector.registeredClasses.size());
//		for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
//			System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
//		}
//
//		System.out.println(classId + ":" + counterId);
	}
	
	private static String queueToString(BufferedArrayQueue<int[]> traceArray) {
		if (traceArray == null) {
			return "null";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("[ ");
		ReplaceableCloneableIterator<int[]> iterator = traceArray.iterator();
		for (; iterator.hasNext();) {
			builder.append(arrayToString(iterator.next())).append(", ");
		}
		builder.setLength(builder.length() > 2 ? builder.length()-2 : builder.length()-1);
		builder.append(" ]");
		return builder.toString();
	}
	
	private static String arrayToString(int[] traceArray) {
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
	
//	private static class SubTraceProcessor implements Runnable {
//		
//		private final long threadId;
//		private final List<int[]> subTrace;
//
//		public SubTraceProcessor(long threadId, List<int[]> subTrace) {
//			this.threadId = threadId;
//			this.subTrace = subTrace;
//		}
//
//		@Override
//		public void run() {
////			// get the respective execution trace
////			BufferedArrayQueue<Integer> trace = executionTraces.get(threadId);
////			if (trace == null) {
////				trace = getNewCollector(threadId);
////				executionTraces.put(threadId, trace);
////			}
//			
//			// get or create id for sub trace
//			int id = getOrCreateIdForSubTrace(subTrace);
//			
////			// add the sub trace's id to the trace
////			trace.add(id);
//						
//			
////			System.out.println("size: " + TouchCollector.registeredClasses.size());
////			for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
////				System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
////			}
////
////			System.out.println(classId + ":" + counterId);
//		}
//		
//	}
	
	private static void processAllRemainingSubTraces() {
		Iterator<Entry<Long, BufferedArrayQueue<int[]>>> iterator = currentSubTraces.entrySet().iterator();
//		Future<?> future = null;
		while (iterator.hasNext()) {
			Entry<Long, BufferedArrayQueue<int[]>> entry = iterator.next();
			processLastSubtraceForThreadId(entry.getKey(), entry.getValue());
			// clear the current sub trace
			iterator.remove();
		}
		
//		if (future != null) {
//			boolean terminated = false;
//			while (!terminated) {
//				try {
//					// Log.out(this, "awaiting termination...");
//					future.get();
//					terminated = true;
//				} catch (InterruptedException e) {
//					// try again...
//				} catch (ExecutionException e) {
//					e.printStackTrace();
//					terminated = true;
//				}
//			}
//		}
		
//		executorService.shutdown();
//
//		// await termination
//		boolean result = false;
//		boolean terminated = false;
//		while (!terminated) {
//			try {
//				// Log.out(this, "awaiting termination...");
//				result = executorService.awaitTermination(7, TimeUnit.DAYS);
//				terminated = true;
//			} catch (InterruptedException e) {
//				// try again...
//			}
//		}
//
//		if (!result) {
//			System.err.println("Timeout reached or Exception thrown! Could not finish all jobs!");
//			executorService.shutdownNow();
//		}
	}
	
//	/**
//	 * This method should be called for each executed statement. Therefore, 
//	 * access to this class has to be ensured for ALL instrumented classes.
//	 * 
//	 * @param classId
//	 * the unique id of the class, as used by cobertura
//	 * @param counterId
//	 * the cobertura counter id, necessary to retrieve the exact line in the class
//	 */
//	public static void addDecisionStatementToExecutionTrace(int classId, int counterId) {
//		// get an id for the current thread
//		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO
//		
//		// get the respective execution trace
//		BufferedArrayQueue<int[]> trace = executionTraces.get(threadId);
//		if (trace == null) {
//			trace = getNewCollector(threadId);
//			executionTraces.put(threadId, trace);
//		}
//		
////		System.out.println("size: " + TouchCollector.registeredClasses.size());
////		for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
////			System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
////		}
//		
////		System.out.println(classId + ":" + counterId);
//		
//		// add the statement to the trace
//		trace.add(new int[] {classId, counterId, 3});
//	}
	
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
		processLastSubTrace();
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
		
		// get the respective sub trace
		BufferedArrayQueue<int[]> subTrace = currentSubTraces.get(threadId);
		if (subTrace == null) {
			subTrace = getNewSubtrace();
			currentSubTraces.put(threadId, subTrace);
		}
		
//		System.out.println("size: " + TouchCollector.registeredClasses.size());
//		for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
//			System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
//		}
		
//		System.out.println(classId + ":" + counterId);
		
		// add the statement to the sub trace
		subTrace.add(new int[] {classId, counterId});
	}

	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * Seems to mark false branches in if-statements...
	 * 
	 * @param classId
	 * the unique id of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void variableAddStatementToExecutionTrace(int classId, int counterId) {
		if (counterId == AbstractCodeProvider.FAKE_COUNTER_ID) {
			// this marks a fake jump! (ignore)
			return;
		}
		// get an id for the current thread
		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO

		// get the respective sub trace
		BufferedArrayQueue<int[]> subTrace = currentSubTraces.get(threadId);
		if (subTrace == null) {
			subTrace = getNewSubtrace();
			currentSubTraces.put(threadId, subTrace);
		}

//				System.out.println("size: " + TouchCollector.registeredClasses.size());
//				for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
//					System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
//				}

//				System.out.println(classId + ":" + counterId);

		// add the statement to the sub trace
		subTrace.add(new int[] {classId, counterId, 0});
	}
	
	/**
	 * This method should be called for each executed statement. Therefore, 
	 * access to this class has to be ensured for ALL instrumented classes.
	 * 
	 * Seems to mark true branches in if-statements...
	 * 
	 * @param classId
	 * the unique id of the class, as used by cobertura
	 * @param counterId
	 * the cobertura counter id, necessary to retrieve the exact line in the class
	 */
	public static void jumpAddStatementToExecutionTrace(int classId, int counterId) {
		if (counterId == AbstractCodeProvider.FAKE_COUNTER_ID) {
			// this marks a fake jump! (ignore)
			return;
		}
		
		// get an id for the current thread
		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO

		// get the respective sub trace
		BufferedArrayQueue<int[]> subTrace = currentSubTraces.get(threadId);
		if (subTrace == null) {
			subTrace = getNewSubtrace();
			currentSubTraces.put(threadId, subTrace);
		}

//				System.out.println("size: " + TouchCollector.registeredClasses.size());
//				for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
//					System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
//				}

//				System.out.println(classId + ":" + counterId);

		// add the statement to the sub trace
		subTrace.add(new int[] {classId, counterId, 1});
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
		if (counterId == AbstractCodeProvider.FAKE_COUNTER_ID) {
			// this marks a fake jump! (ignore)
			return;
		}
		
		// get an id for the current thread
		long threadId = Thread.currentThread().getId(); // may be reused, once the thread is killed TODO

		// get the respective sub trace
		BufferedArrayQueue<int[]> subTrace = currentSubTraces.get(threadId);
		if (subTrace == null) {
			subTrace = getNewSubtrace();
			currentSubTraces.put(threadId, subTrace);
		}

//				System.out.println("size: " + TouchCollector.registeredClasses.size());
//				for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
//					System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
//				}

//				System.out.println(classId + ":" + counterId);

		// add the statement to the sub trace
		subTrace.add(new int[] {classId, counterId, 2});
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
