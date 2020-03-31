package se.de.hu_berlin.informatik.spectra.core.traces;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.EfficientCompressedIntegerTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.TraceIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.TraceReverseIterator;

import java.util.Collection;
import java.util.Iterator;

public interface SequenceIndexerCompressed {

    public void removeFromSequences(int index);

    public void removeFromSequences(Collection<Integer> nodeIndicesToRemove);

    int[][] getSubTraceIdSequences();

    EfficientCompressedIntegerTrace[] getNodeIdSequences();

    int[] getSubTraceIdSequence(int index);

    EfficientCompressedIntegerTrace getNodeIdSequence(int subTraceIndex);

    /**
     * Iterates over the sequence of sub trace IDs
     * with the given index and each node ID in the
     * respective indexed sub trace.
     *
     * @param subTraceSequenceIndex an index of a sequence of sub trace IDs
     * @return an iterator over all sub traces in the
     * specified sequence of sub trace IDs
     */
    Iterator<Integer> getFullSequenceIterator(int subTraceSequenceIndex);

    /**
     * Iterates over the sequence of sub trace IDs
     * with the given index and each node ID in the
     * respective indexed sub trace, starting from the end
     * of the sequence.
     *
     * @param subTraceSequenceIndex an index of a sequence of sub trace IDs
     * @return an iterator over all sub traces in the
     * specified sequence of sub trace IDs
     */
    Iterator<Integer> getFullSequenceReverseIterator(int subTraceSequenceIndex);

    /**
     * Iterates over the sub trace with the given index.
     * The sub trace contains spectra node IDs.
     *
     * @param subTraceIndex an index of a sub trace
     * @return an iterator over the specified sub trace
     */
    TraceIterator getNodeIdSequenceIterator(int subTraceIndex);

    /**
     * Iterates over the sub trace with the given index,
     * starting from the end of the sequence.
     * The sub trace contains spectra node IDs.
     *
     * @param subTraceIndex an index of a sub trace
     * @return an iterator over the specified sub trace
     */
    TraceReverseIterator getNodeIdSequenceReverseIterator(int subTraceIndex);

    /**
     * Iterates over the sequence of sub trace IDs
     * with the given index.
     *
     * @param subTraceSequenceIndex an index of a sequence of sub trace IDs
     * @return an iterator over the specified sequence of sub trace IDs
     */
    Iterator<Integer> getSubTraceIDSequenceIterator(int subTraceSequenceIndex);

    /**
     * Iterates over the sequence of sub trace IDs
     * with the given index, starting from the end
     * of the sequence.
     *
     * @param subTraceSequenceIndex an index of a sequence of sub trace IDs
     * @return an iterator over the specified sequence of sub trace IDs
     */
    Iterator<Integer> getSubTraceIDSequenceReverseIterator(int subTraceSequenceIndex);

}
