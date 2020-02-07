package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.Collection;
import java.util.Iterator;

import de.unisb.cs.st.sequitur.input.SharedInputGrammar;

public interface SequenceIndexerCompressed {

	public void removeFromSequences(int index);
	
	public void removeFromSequences(Collection<Integer> nodeIndicesToRemove);

	int[][] getNodeIdSequences();

	int[] getNodeIdSequence(int subTraceIndex);

	SharedInputGrammar<Integer> getExecutionTraceInputGrammar();

	public Iterator<Integer> getNodeIdSequenceIterator(int subTraceId);

	byte[] getGrammarByteArray();

}
