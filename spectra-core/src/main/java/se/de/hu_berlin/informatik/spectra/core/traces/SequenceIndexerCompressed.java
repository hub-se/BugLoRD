package se.de.hu_berlin.informatik.spectra.core.traces;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.SharedInputGrammar;
import se.de.hu_berlin.informatik.spectra.util.CachedMap;

import java.util.Collection;
import java.util.Iterator;

public interface SequenceIndexerCompressed {

    public void removeFromSequences(int index);

    public void removeFromSequences(Collection<Integer> nodeIndicesToRemove);

    CachedMap<int[]> getNodeIdSequences();

    int[] getNodeIdSequence(int subTraceIndex);

    CachedMap<int[]> getSubTraceIdSequences();

    int[] getSubTraceIdSequence(int subTraceIndex);

    SharedInputGrammar getExecutionTraceInputGrammar();

    public Iterator<Integer> getNodeIdSequenceIterator(int subTraceId);

    public Iterator<Integer> getSubTraceIdSequenceIterator(int subTraceId);

    byte[] getGrammarByteArray();

}
