package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.SequiturUtils;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.OutputSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@CoverageIgnore
public class ExecutionTraceCollector {

//	public final static int EXECUTION_TRACE_CHUNK_SIZE = 150000;
//	public final static int MAP_CHUNK_SIZE = 150000;
//	public static final int SUBTRACE_ARRAY_SIZE = 500;

    public static final int NEW_SUBTRACE_ID = 0;

    private static final transient Lock globalExecutionTraceCollectorLock = new ReentrantLock();

    // shouldn't need to be thread-safe, as each thread only accesses its own trace (thread id -> sequence of sub trace ids)
    private static Map<Long, OutputSequence> executionTraces = new ConcurrentHashMap<>();
//    private static SharedOutputGrammar grammar = new SharedOutputGrammar();

    private static final int[][] classesToCounterArrayMap = new int[(int)Math.pow(2, CoberturaStatementEncoding.CLASS_ID_BITS)][];

//    private static Set<Thread> currentThreads = new HashSet<>();

    public static void initializeCounterArrayForClass(int classId, int countersCnt) {
        classesToCounterArrayMap[classId] = new int[countersCnt];
    }

//	private static Path tempDir;
//	
//	static {
//		try {
//			Path path = Paths.get(System.getProperty("user.dir")).resolve("execTracesTmp");
//			path.toFile().mkdirs();
//			tempDir = Files.createTempDirectory(path.toAbsolutePath(), "exec");
//		} catch (IOException e) {
//			e.printStackTrace();
//			tempDir = null;
//		}
//	}

    private static long counter = 0;

    /**
     * @return the collection of execution traces for all executed threads;
     * the statements in the traces are stored as "class_id:statement_counter";
     * also resets the internal map and collects potentially remaining sub traces.
     */
    public static List<Pair<Long, byte[]>> getAndResetExecutionTraces() {
        globalExecutionTraceCollectorLock.lock();
        try {
//            processAllRemainingSubTraces();
        	
        	Map<Long, OutputSequence> tempMap = executionTraces;
//        	grammar.lock();
//        	SharedOutputGrammar tempGrammar = grammar;
        	
        	executionTraces = new ConcurrentHashMap<>();
//            grammar = new SharedOutputGrammar();

            int threadCounter = 0;
//            StringBuilder sb = new StringBuilder();
//            sb.append(String.format("%n#statements: %,d%n", counter));
            List<Pair<Long, byte[]>> traces = new ArrayList<>(tempMap.size());
            for (Entry<Long, OutputSequence> entry : tempMap.entrySet()) {
            	byte[] bytes = SequiturUtils.convertToByteArray(entry.getValue(), true);
                traces.add(new Pair<>(entry.getKey(), bytes));
                ++threadCounter;

//                sb.append(String.format(" %,d -> %,d (%.2f%%)%n", 
//                		entry.getValue().getLength(), bytes.length / 4, 
//                		-100.00 + 100.0 * (double) (bytes.length / 4) / (double) entry.getValue().getLength()));
            }
//            byte[] grammarByteArray = SequiturUtils.convertToByteArray(tempGrammar);
            
            //System.out.println(String.format("executed statements: %,d, threads: %,d", counter, threadCounter));
            counter = 0;
//            if (sb.length() != 0) {
//            	System.out.print(sb.toString());
//            }
            
//            SharedInputGrammar sharedInputGrammar = SequiturUtils.convertToInputGrammar(grammarByteArray);
//            for (byte[] trace : traces.values()) {
//            	TraceIterator traceIterator = SequiturUtils.getInputSequenceFromByteArray(trace, sharedInputGrammar).iterator();
//            }
            
//            return new Pair<List<Pair<Long, byte[]>>, byte[]>(traces, grammarByteArray);
            return traces;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            globalExecutionTraceCollectorLock.unlock();
        }
    }


    private static OutputSequence getNewCollector(long threadId) {
        return new OutputSequence();
    }


    /**
     * Marks the beginning of a new sub trace by adding a special indicator to the trace.
     *
     * @param trace the output sequence to append statements to
     */
    public static void startNewSubTrace(OutputSequence trace) {

        if (++counter % 1000000 == 0) {
            System.out.print('.');
            if (counter % 100000000 == 0)
                System.out.println(String.format("%,d", counter));
        }

        // add an indicator to the trace that represents a visited catch block
        trace.append(NEW_SUBTRACE_ID);

    }


//    private static void processAllRemainingSubTraces() {
//
//        for (Thread thread : currentThreads) {
//            if (thread.equals(Thread.currentThread())) {
//                continue;
//            }
//            boolean done = false;
//            while (!done) {
//                if (thread.isAlive()) {
//                    System.err.println("Thread " + thread.getId() + " is still alive. Waiting 5 seconds for it to die...");
//                    try {
//                        thread.join(5000); // wait 5 seconds for threads to die... TODO
//                        if (thread.isAlive()) {
//                            System.err.println("(At least) thread " + thread.getId() + " remains alive...");
//                            //						thread.interrupt();
//                            break;
//                        }
//                        done = true;
//                    } catch (InterruptedException e) {
//                        // try again
//                    }
//                } else {
//                    break;
//                }
//            }
//        }
//
//        currentThreads.clear();
//
////		globalExecutionTraceCollectorLock.lock();
////		try {
////			// store execution traces
////			Iterator<Entry<Long, OutputSequence<Integer>>> iterator2 = executionTraces.entrySet().iterator();
////			while (iterator2.hasNext()) {
////				try {
////					iterator2.next().getValue().sleep();
////				} catch (Exception e) {
////					e.printStackTrace();
////					// something went wrong...
////					iterator2.remove();
////				}
////			}
////			
////		} finally {
////			globalExecutionTraceCollectorLock.unlock();
////		}
//
//    }


    /**
     * This method should be called for each executed statement. Therefore,
     * access to this class has to be ensured for ALL instrumented classes.
     *
     * @param classId   the unique id of the class, as used by cobertura
     * @param counterId the cobertura counter id, necessary to retrieve the exact line in the class
     * @param trace     the output sequence to append statements to
     */
    public static void addStatementToExecutionTraceAndIncrementCounter(int classId, int counterId, OutputSequence trace) {
        addStatementToExecutionTrace(classId, counterId, CoberturaStatementEncoding.NORMAL_ID, trace);
        incrementCounter(classId, counterId);
    }

    /**
     * This method should be called for each executed statement. Therefore,
     * access to this class has to be ensured for ALL instrumented classes.
     *
     * @param classId   the unique id of the class, as used by cobertura
     * @param counterId the cobertura counter id, necessary to retrieve the exact line in the class
     * @param trace     the output sequence to append statements to
     */
    public static void variableAddStatementToExecutionTraceAndIncrementCounter(int classId, int counterId, OutputSequence trace) {
        addStatementToExecutionTrace(classId, counterId, CoberturaStatementEncoding.BRANCH_ID, trace);
        incrementCounter(classId, counterId);
    }

    /**
     * This method should be called for each executed statement. Therefore,
     * access to this class has to be ensured for ALL instrumented classes.
     *
     * @param classId   the unique id of the class, as used by cobertura
     * @param counterId the cobertura counter id, necessary to retrieve the exact line in the class
     * @param trace     the output sequence to append statements to
     */
    public static void jumpAddStatementToExecutionTraceAndIncrementCounter(int classId, int counterId, OutputSequence trace) {
        addStatementToExecutionTrace(classId, counterId, CoberturaStatementEncoding.JUMP_ID, trace);
        incrementCounter(classId, counterId);
    }

    /**
     * This method should be called for each executed statement. Therefore,
     * access to this class has to be ensured for ALL instrumented classes.
     *
     * @param classId   the unique id of the class, as used by cobertura
     * @param counterId the cobertura counter id, necessary to retrieve the exact line in the class
     * @param trace     the output sequence to append statements to
     */
    public static void switchAddStatementToExecutionTraceAndIncrementCounter(int classId, int counterId, OutputSequence trace) {
//		processLastSubTrace();
        addStatementToExecutionTrace(classId, counterId, CoberturaStatementEncoding.SWITCH_ID, trace);
        incrementCounter(classId, counterId);
    }


    private static void addStatementToExecutionTrace(int classId, int counterId, int specialIndicatorId, OutputSequence trace) {
        if (counterId == AbstractCodeProvider.FAKE_COUNTER_ID) {
            // this marks a fake jump! (ignore)
            return;
        }

        if (++counter % 1000000 == 0) {
            System.out.print('.');
            if (counter % 100000000 == 0)
                System.out.println(String.format("%,d", counter));
        }

        // add the statement to the execution trace
        trace.append(CoberturaStatementEncoding.generateUniqueRepresentationForStatement(classId, counterId));
    }


    /**
     * This method gets called once at the start of each instrumented method.
     * The returned reference is stored in a local variable and used throughout the method.
     *
     * @return output sequence for the current thread
     */
    public static OutputSequence getOutputSequence() {
        Thread currentThread = Thread.currentThread();
		long threadId = currentThread.getId();
        // get the thread's execution trace
        OutputSequence trace = executionTraces.get(threadId);
        if (trace == null) {
        	 globalExecutionTraceCollectorLock.lock();
             try {
            	 trace = getNewCollector(threadId);
            	 executionTraces.put(threadId, trace);
//            	 currentThreads.add(currentThread);
             } finally {
            	 globalExecutionTraceCollectorLock.unlock();
			}
        }
        return trace;
    }

    /**
     * This method should be called for each executed statement. Therefore,
     * access to this class has to be ensured for ALL instrumented classes.
     *
     * @param classId   the unique id of the class, as used by cobertura
     * @param counterId the cobertura counter id, necessary to retrieve the exact line in the class
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
