package se.de.hu_berlin.informatik.spectra.core.traces;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence.TraceIterator;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An execution trace consists structurally of a list of executed nodes (or references to node lists)
 * and a list of tuples that mark repeated sequences in the trace.
 */
public class ExecutionTrace {

    private InputSequence trace;
    private Set<Integer> terminals;

    private byte[] traceByteArray;

    private SequenceIndexerCompressed sequenceIndexer;

    public byte[] getTraceByteArray() {
        return traceByteArray;
    }

    public ExecutionTrace(byte[] traceByteArray, SequenceIndexerCompressed sequenceIndexer) {
        this.traceByteArray = traceByteArray;
        this.sequenceIndexer = sequenceIndexer;
    }

    public ExecutionTrace(byte[] traceByteArrayWithGrammar) {
        this.traceByteArray = traceByteArrayWithGrammar;
    }

    private InputSequence getTrace() {
        // lazy instantiation
        if (this.trace == null) {
            try {
                this.trace = getInputSequenceFromByteArray();
            } catch (IOException | ClassNotFoundException e) {
                Log.abort(this, e, "Cannot convert to input sequence.");
            }
        }
        return this.trace;
    }

    private InputSequence getInputSequenceFromByteArray() throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(traceByteArray);
        ObjectInputStream objIn = new ObjectInputStream(byteIn);
        if (sequenceIndexer == null || sequenceIndexer.getExecutionTraceInputGrammar() == null) {
            // grammar should be included
            InputSequence inputSequence = InputSequence.readFrom(objIn);
            return inputSequence;
        } else {
            // grammar should be shared
            InputSequence inputSequence = InputSequence.readFrom(objIn, sequenceIndexer.getExecutionTraceInputGrammar());
            return inputSequence;
        }
    }

    public long size() {
        return getTrace().getLength();
    }

    /**
     * Constructs the full execution trace. Usually, you should NOT be using this. Use an iterator instead!
     *
     * @param indexer indexer that is used to connect the element IDs in the execution trace to the respective sub traces
     *                that contain node IDs
     * @return array that contains all executed node IDs
     */
    public int[] reconstructFullMappedTrace(SequenceIndexerCompressed indexer) {
        Iterator<Integer> indexedFullTrace = mappedIterator(indexer);
        List<Integer> fullTrace = new ArrayList<>();
        while (indexedFullTrace.hasNext()) {
            fullTrace.add(indexedFullTrace.next());
        }
        return fullTrace.stream().mapToInt(i -> i).toArray();
    }

    /**
     * iterates over all node IDs in the execution trace.
     *
     * @param sequenceIndexer indexer that is used to connect the element IDs in the execution trace to the respective sub traces
     *                        that contain node IDs
     * @return iterator
     */
    public Iterator<Integer> mappedIterator(SequenceIndexerCompressed sequenceIndexer) {
        return new Iterator<Integer>() {

            final TraceIterator iterator = ExecutionTrace.this.iterator();
            int[] currentSubTraceIdSequence;
            int subTraceSequenceIndex = 0; // outer index
            int[] currentNodeIdSequence;
            int subTraceIndex = 0; // inner index

            @Override
            public boolean hasNext() {
                if (sequenceIndexer.getSubTraceIdSequences() == null) {
                    return hasNextFlat();
                } else {
                    return hasNextNotFlat();
                }
            }

            private boolean hasNextFlat() {
                // need new sub trace? look for the next valid sub trace
                if (currentNodeIdSequence == null || subTraceIndex >= currentNodeIdSequence.length) {
                    // we're at the end of the current sub trace! (or there is none)
                    currentNodeIdSequence = null;
                    // get the next valid sub trace
                    while (iterator.hasNext()) {
                        currentNodeIdSequence = sequenceIndexer.getNodeIdSequence(iterator.next());
                        if (currentNodeIdSequence.length > 0) {
                            // found a "good" sequence
                            subTraceIndex = 0;
                            break;
                        }
                        currentNodeIdSequence = null;
                    }
                }

                // if we found a valid sub trace, it should be non-null
                return currentNodeIdSequence != null;
            }

            private boolean hasNextNotFlat() {
                // need new sub trace? look for the next valid sub trace
                while (currentNodeIdSequence == null || subTraceIndex >= currentNodeIdSequence.length) {
                    if (currentSubTraceIdSequence == null || subTraceSequenceIndex >= currentSubTraceIdSequence.length) {
                        // we're at the end of the current sub trace sequence! (or there is none)
                        currentSubTraceIdSequence = null;
                        // get the next valid sub trace sequence
                        while (iterator.hasNext()) {
                            currentSubTraceIdSequence = sequenceIndexer.getSubTraceIdSequence(iterator.next());
                            if (currentSubTraceIdSequence.length > 0) {
//                            	System.out.println("seq start");
                                // found a "good" sequence
                                subTraceSequenceIndex = 0;
                                break;
                            }
                            currentSubTraceIdSequence = null;
                        }
                    }

                    // found no sub trace sequence?
                    if (currentSubTraceIdSequence == null) {
                        return false;
                    }

                    // we're in a valid sub trace sequence, so get the next valid sub trace!
                    while (subTraceSequenceIndex < currentSubTraceIdSequence.length) {
                        // get the next sub trace from the current sub trace sequence
                        currentNodeIdSequence = sequenceIndexer.getNodeIdSequence(currentSubTraceIdSequence[subTraceSequenceIndex++]);
                        if (currentNodeIdSequence.length > 0) {
                            // found a "good" sequence
//                        	System.out.println("sub seq start");
                            subTraceIndex = 0;
                            break;
                        }
                        currentNodeIdSequence = null;
                    }

                    // if we didn't find a valid sub trace, we repeat the loop
                }

                // if we found a valid sub trace, it should be non-null
                return currentNodeIdSequence != null;
            }

            @Override
            public Integer next() {
                return currentNodeIdSequence[subTraceIndex++];
            }
        };
    }

    /**
     * iterates over all node IDs in the execution trace, starting from the end of the trace.
     *
     * @param sequenceIndexer indexer that is used to connect the element IDs in the execution trace to the respective sub traces
     *                        that contain node IDs
     * @return iterator
     */
    public Iterator<Integer> mappedReverseIterator(SequenceIndexerCompressed sequenceIndexer) {
        return new Iterator<Integer>() {

            final TraceIterator iterator = ExecutionTrace.this.reverseIterator();
            int[] currentSubTraceIdSequence;
            int subTraceSequenceIndex = -1; // outer index
            int[] currentNodeIdSequence;
            int subTraceIndex = -1; // inner index

            @Override
            public boolean hasNext() {
                if (sequenceIndexer.getSubTraceIdSequences() == null) {
                    return hasNextFlat();
                } else {
                    return hasNextNotFlat();
                }
            }

            private boolean hasNextFlat() {
                // need new sub trace? look for the next valid sub trace
                if (currentNodeIdSequence == null || subTraceIndex < 0) {
                    // we're at the end of the current sub trace! (or there is none)
                    currentNodeIdSequence = null;
                    // get the next valid sub trace
                    while (iterator.hasPrevious()) {
                        currentNodeIdSequence = sequenceIndexer.getNodeIdSequence(iterator.previous());
                        if (currentNodeIdSequence.length > 0) {
                            // found a "good" sequence
                            subTraceIndex = currentNodeIdSequence.length - 1;
                            break;
                        }
                        currentNodeIdSequence = null;
                    }
                }

                // if we found a valid sub trace, it should be non-null
                return currentNodeIdSequence != null;
            }

            private boolean hasNextNotFlat() {
                // need new sub trace? look for the next valid sub trace
                while (currentNodeIdSequence == null || subTraceIndex < 0) {
                    if (currentSubTraceIdSequence == null || subTraceSequenceIndex < 0) {
                        // we're at the end of the current sub trace sequence! (or there is none)
                        currentSubTraceIdSequence = null;
                        // get the next valid sub trace sequence
                        while (iterator.hasPrevious()) {
                            currentSubTraceIdSequence = sequenceIndexer.getSubTraceIdSequence(iterator.previous());
                            if (currentSubTraceIdSequence.length > 0) {
                                // found a "good" sequence
                                subTraceSequenceIndex = currentSubTraceIdSequence.length - 1;
                                break;
                            }
                            currentSubTraceIdSequence = null;
                        }
                    }

                    // found no sub trace sequence?
                    if (currentSubTraceIdSequence == null) {
                        return false;
                    }

                    // we're in a valid sub trace sequence, so get the next valid sub trace!
                    while (subTraceSequenceIndex >= 0) {
                        // get the next sub trace from the current sub trace sequence
                        currentNodeIdSequence = sequenceIndexer.getNodeIdSequence(currentSubTraceIdSequence[subTraceSequenceIndex--]);
                        if (currentNodeIdSequence.length > 0) {
                            // found a "good" sequence
                            subTraceIndex = currentNodeIdSequence.length - 1;
                            break;
                        }
                        currentNodeIdSequence = null;
                    }

                    // if we didn't find a valid sub trace, we repeat the loop
                }

                // if we found a valid sub trace, it should be non-null
                return currentNodeIdSequence != null;
            }

            @Override
            public Integer next() {
                return currentNodeIdSequence[subTraceIndex--];
            }
        };
    }

    public TraceIterator iterator() {
        return getTrace().iterator();
    }

    public TraceIterator reverseIterator() {
        return getTrace().iterator(getTrace().getLength());
    }

    public Set<Integer> getTerminals() {
        if (terminals == null) {
            terminals = getTrace().computeTerminals();
        }
        return terminals;
    }

    @Override
    public String toString() {
        return getTrace().toString();
    }
}
