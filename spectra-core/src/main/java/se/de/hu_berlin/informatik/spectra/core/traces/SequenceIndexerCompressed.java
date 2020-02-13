package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.Collection;
import java.util.Iterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.SharedInputGrammar;
import se.de.hu_berlin.informatik.spectra.util.CachedMap;

public interface SequenceIndexerCompressed {

	public void removeFromSequences(int index);
	
	public void removeFromSequences(Collection<Integer> nodeIndicesToRemove);

	CachedMap<int[]> getNodeIdSequences();

	int[] getNodeIdSequence(int subTraceIndex);

	SharedInputGrammar getExecutionTraceInputGrammar();

	public Iterator<Integer> getNodeIdSequenceIterator(int subTraceId);

	byte[] getGrammarByteArray();

}
