package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedLongArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.EfficientCompressedIntegerTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs.EfficientCompressedLongTrace;

@CoverageIgnore
public class ExecutionTraceCollector {

	public final static int EXECUTION_TRACE_CHUNK_SIZE = 300000;
	public final static int MAP_CHUNK_SIZE = 500000;
	public static final int SUBTRACE_ARRAY_SIZE = 100;
	
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

	
//	private static class SubTraceCollector {
//		
//		private BlockingQueue<BufferedArrayQueue<int[]>> queue = new ArrayBlockingQueue<>(100);
//		
//		private Thread handlerThread;
//		private volatile boolean done = false;
//		
//		public SubTraceCollector(final long threadId) {
//			handlerThread = new Thread() {
//				@Override
//				public void run() {
//					while (!queue.isEmpty() || !done) {
////						System.out.println(threadId);
//						BufferedArrayQueue<int[]> subTrace = queue.poll();
//						if (subTrace == null) {
//							try {
//								Thread.sleep(50);
//							} catch (InterruptedException e) {
//								// do nothing
//							}
//						} else {
//							processSubtraceForThreadId(threadId, subTrace);
//						}
//					}
//				};
//			};
//			
//			handlerThread.start();
//		}
//		
//		public void submitTrace(BufferedArrayQueue<int[]> subTrace) {
//			boolean worked = false;
//			while (!worked) {
//				try {
//					queue.put(subTrace);
//					worked = true;
//				} catch (InterruptedException e) {
//					// try again
//				}
//			}
//		}
//		
//		public void setDoneAndWaitForFinish() {
//			done = true;
//			while (handlerThread.isAlive()) {
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					// do nothing
//				}
//			}
//		}
//	}
//	
//	private static Map<Long,SubTraceCollector> subTraceCollectorThreads = new ConcurrentHashMap<>();
	
	private static final transient Lock globalExecutionTraceCollectorLock = new ReentrantLock();

	// shouldn't need to be thread-safe, as each thread only accesses its own trace (thread id -> sequence of sub trace ids)
	private static Map<Long,EfficientCompressedIntegerTrace> executionTraces = new ConcurrentHashMap<>();
	// stores (sub trace id -> subTrace)
	private static Map<Integer,BufferedLongArrayQueue> existingSubTraces = new ConcurrentHashMap<>();
	private static Map<Integer,EfficientCompressedLongTrace> existingCompressedSubTraces = new ConcurrentHashMap<>();
	// stores (sub trace wrapper -> sub trace id) to retrieve subtrace ids
	// the integer array in the wrapper has to contain start and ending node of the sub trace
	// if the sub trace is longer than one statement
	private static Map<Long,Integer> subTraceIdMap = new ConcurrentHashMap<>();
	private static volatile int currentId = 0; 
	// lock for getting/generating sub trace ids (ensures that sub trace ids are unique)
	private static final transient Lock idLock = new ReentrantLock();
	// stores currently built up execution trace parts for each thread (thread id -> sub trace)
	private static Map<Long,BufferedLongArrayQueue> currentSubTraces = new ConcurrentHashMap<>();
	
	private static Queue<BufferedLongArrayQueue> unusedSubTraceCache = new ConcurrentLinkedQueue<>();
	
//	public static final Map<Integer, int[]> classesToCounterArrayMap = new ConcurrentHashMap<>();

	private static int[][] classesToCounterArrayMap = new int[2048][];
	
	private static Set<Thread> currentThreads = new HashSet<>();

	public static void initializeCounterArrayForClass(int classId, int countersCnt) {
//		classesToCounterArrayMap.put(classId, new int[countersCnt]);
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
	
	/**
	 * @return
	 * The map of ids to actual sub traces; also resets the internal map
	 */
	public static Map<Integer, EfficientCompressedLongTrace> getAndResetIdToSubtraceMap() {
		globalExecutionTraceCollectorLock.lock();
		try {
			// process all remaining sub traces. Just to be safe!
			processAllRemainingSubTraces();
			// sub trace ids that stay consistent throughout the entire time!!??? TODO
			Map<Integer, EfficientCompressedLongTrace> traceMap = existingCompressedSubTraces;
			// reset id counter and map!
			currentId = 0;
//			existingSubTraces = null;
			existingSubTraces = new ConcurrentHashMap<>();
			existingCompressedSubTraces = new ConcurrentHashMap<>();
			subTraceIdMap.clear();
			return traceMap;
		} finally {
			globalExecutionTraceCollectorLock.unlock();
		}
	}
	
	private static EfficientCompressedIntegerTrace getNewCollector(long threadId) {
		// do not delete buffered trace files on exit, due to possible necessary serialization
		return new EfficientCompressedIntegerTrace(tempDir.toAbsolutePath().toFile(), 
				"exec_trc_" + threadId + "-", 
				EXECUTION_TRACE_CHUNK_SIZE, MAP_CHUNK_SIZE, false);
	}
	
	
	private static int getOrCreateIdForSubTrace(BufferedLongArrayQueue subTrace) {
//		if (subTrace == null || subTrace.isEmpty()) {
//			// id 0 indicates empty sub trace
//			return 0;
//		}

		long subTraceId = CoberturaStatementEncoding.generateUniqueRepresentationForSubTrace(subTrace);
		Integer id = subTraceIdMap.get(subTraceId);
		if (id == null) {
			idLock.lock();
			try {
				id = subTraceIdMap.get(subTraceId);
				// check again while locked!
				if (id == null) {
					// 
					// starts with id 1
					id = ++currentId;
					// new sub trace, so store new id and store sub trace
					subTraceIdMap.put(subTraceId, id);
//					if (existingSubTraces == null) {
//						existingSubTraces = new ConcurrentHashMap<>();
//					}
//					System.out.println("new :" + currentId 
//							//+ ":" + subTrace.toString()
//							);
					subTrace.sleep();
					existingSubTraces.put(id, subTrace);
				}
			} finally {
				idLock.unlock();
			}
		} else {
//			System.out.println("old: " + id
//					//+ ":" + subTrace.toString()
//					);
			subTrace.clear();
			unusedSubTraceCache.add(subTrace);
		}

		return id;
	}
	
	
//	private static BufferedMap<BufferedArrayQueue<int[]>> getNewSubTraceMap() {
//		// do not delete buffered map on exit, due to possible necessary serialization
//		return new BufferedMap<>(tempDir.toAbsolutePath().toFile(), 
//				String.valueOf(UUID.randomUUID()), MAP_CHUNK_SIZE, false);
//	}
	
	private static BufferedLongArrayQueue getNewSubtrace() {
		BufferedLongArrayQueue subTrace = unusedSubTraceCache.poll();
		if (subTrace == null) {
			return new BufferedLongArrayQueue(tempDir.toAbsolutePath().toFile(), 
					"sub_trc_" + String.valueOf(UUID.randomUUID()), 
					SUBTRACE_ARRAY_SIZE, false);
		} else {
			return subTrace;
		}
	}

	
	/**
	 * This method should be called after each decision point.
	 * After the instrumented program has finished execution, 
	 * {@link ExecutionTraceCollector#processAllRemainingSubTraces()}
	 * should be called to collect the remaining sub traces.
	 */
	public static void processLastSubTrace() {
		// get an id for the current thread
		Thread currentThread = Thread.currentThread();
		currentThreads.add(currentThread);
		long threadId = currentThread.getId(); // may be reused, once the thread is killed TODO
//		System.out.println(threadId);
//		submitSubTraceToCollectorThread(threadId, currentSubTraces.remove(threadId));

		processSubtraceForThreadId(threadId, currentSubTraces.remove(threadId));
	}

//	private static void submitSubTraceToCollectorThread(long threadId, BufferedArrayQueue<int[]> subTrace) {
//		if (subTrace == null) {
//			return;
//		}
//		SubTraceCollector collector = subTraceCollectorThreads.get(threadId);
//		if (collector == null) {
//			collector = new SubTraceCollector(threadId);
//			subTraceCollectorThreads.put(threadId, collector);
//		}
//		collector.submitTrace(subTrace);
//	}

	private static void processSubtraceForThreadId(long threadId, BufferedLongArrayQueue subTrace) {
		if (subTrace == null) {
			// sub trace contains no nodes
			return;
		}
//		if (subTrace.isEmpty()) {
//			throw new IllegalStateException("Processing an empty sub trace...");
//		}
		
//		// do more expensive operations in a separate thread?
//		return executorService.submit(new SubTraceProcessor(threadId, subTrace));
		
		// get the respective execution trace
		EfficientCompressedIntegerTrace trace = executionTraces.get(threadId);
		if (trace == null) {
			trace = getNewCollector(threadId);
			executionTraces.put(threadId, trace);
		}
		
		// get or create id for sub trace
//		System.out.println(queueToString(subTrace));
		int id = getOrCreateIdForSubTrace(subTrace);
//		System.out.println(id + ": " + queueToString(subTrace));
		
		// add the sub trace's id to the trace
		trace.add(id);
					
		
//		System.out.println("size: " + TouchCollector.registeredClasses.size());
//		for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
//			System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
//		}
//
//		System.out.println(classId + ":" + counterId);
	}
	
//	private static String queueToString(BufferedArrayQueue<int[]> traceArray) {
//		if (traceArray == null) {
//			return "null";
//		}
//		StringBuilder builder = new StringBuilder();
//		builder.append("[ ");
//		ReplaceableCloneableIterator<int[]> iterator = traceArray.iterator();
//		for (; iterator.hasNext();) {
//			builder.append(arrayToString(iterator.next())).append(", ");
//		}
//		builder.setLength(builder.length() > 2 ? builder.length()-2 : builder.length()-1);
//		builder.append(" ]");
//		return builder.toString();
//	}
//	
//	private static String arrayToString(int[] traceArray) {
//		if (traceArray == null) {
//			return "null";
//		}
//		StringBuilder builder = new StringBuilder();
//		builder.append("[ ");
//		for (int entry : traceArray) {
//			builder.append(entry).append(", ");
//		}
//		builder.setLength(builder.length() > 2 ? builder.length()-2 : builder.length()-1);
//		builder.append(" ]");
//		return builder.toString();
//	}
	
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
			Iterator<Entry<Long, BufferedLongArrayQueue>> iterator = currentSubTraces.entrySet().iterator();
			//		Future<?> future = null;
			while (iterator.hasNext()) {
				Entry<Long, BufferedLongArrayQueue> entry = iterator.next();
				processSubtraceForThreadId(entry.getKey(), entry.getValue());
				//			submitSubTraceToCollectorThread(entry.getKey(), entry.getValue());

				// clear the current sub trace
				iterator.remove();
			}

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
			
			// reduce the size of sub traces that contain repetitions
			Iterator<Entry<Integer, BufferedLongArrayQueue>> iterator3 = existingSubTraces.entrySet().iterator();
			while (iterator3.hasNext()) {
				Entry<Integer, BufferedLongArrayQueue> entry = iterator3.next();
				BufferedLongArrayQueue queue = entry.getValue();
//				queue.deleteOnExit();
				try {
					EfficientCompressedLongTrace subTrace = new EfficientCompressedLongTrace(queue, false);
					//				System.out.println(subTrace.toString());
					subTrace.sleep();
					subTrace.lock();
					existingCompressedSubTraces.put(entry.getKey(), subTrace);
				} catch (Exception e) {
					e.printStackTrace();
					// something went wrong...
					iterator3.remove();
				}
			}
			
			existingSubTraces.clear();
			existingSubTraces = new ConcurrentHashMap<>();
		} finally {
			globalExecutionTraceCollectorLock.unlock();
		}
		
//		Iterator<Entry<Long, SubTraceCollector>> iterator2 = subTraceCollectorThreads.entrySet().iterator();
////		Future<?> future = null;
//		while (iterator2.hasNext()) {
//			Entry<Long, SubTraceCollector> entry = iterator2.next();
////			processSubtraceForThreadId(entry.getKey(), entry.getValue());
//			entry.getValue().setDoneAndWaitForFinish();
//			
//			// clear the current sub trace
//			iterator2.remove();
//		}
		
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

		// get the respective sub trace
		BufferedLongArrayQueue subTrace = currentSubTraces.get(threadId);
		if (subTrace == null) {
			subTrace = getNewSubtrace();
			currentSubTraces.put(threadId, subTrace);
		}

//		System.out.println("size: " + TouchCollector.registeredClasses.size());
//		for (Entry<String, Integer> entry : TouchCollector.registeredClassesStringsToIdMap.entrySet()) {
//			System.out.println("key: " + entry.getKey() + ", id: " + entry.getValue());
//		}

//		if (classId == 142) {
//		System.out.println(classId + ":" + counterId + ":" + specialIndicatorId);
//		}

		// add the statement to the sub trace
		subTrace.add(CoberturaStatementEncoding
				.generateUniqueRepresentationForStatement(classId, counterId, specialIndicatorId));
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
