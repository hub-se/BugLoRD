package se.de.hu_berlin.informatik.spectra.core.traces;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import de.unisb.cs.st.sequitur.input.InputSequence;
import de.unisb.cs.st.sequitur.input.SharedInputGrammar;
import de.unisb.cs.st.sequitur.output.SharedOutputGrammar;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class SimpleIntIndexerCompressed implements SequenceIndexerCompressed {

	// mapping: sub trace ID -> sequence of spectra node IDs
	private int[][] nodeIdSequences;
	
	private SharedOutputGrammar<Integer> executionTraceOutputGrammar;
	private SharedInputGrammar<Integer> executionTraceInputGrammar;

	private byte[] storedGrammar;

	// constructor used when loading indexer from zip file
	public SimpleIntIndexerCompressed(byte[] storedGrammar, int[][] nodeIdSequences) {
		this.storedGrammar = storedGrammar;
		this.nodeIdSequences = nodeIdSequences;
	}
	
	// constructor used before storing in zip file
	public SimpleIntIndexerCompressed(SharedOutputGrammar<Integer> executionTraceGrammar, 
			Map<Integer, byte[]> existingSubTraces, SharedInputGrammar<Integer> subTraceGrammar) throws IOException {
		this.executionTraceOutputGrammar = executionTraceGrammar;
		this.nodeIdSequences = new int[existingSubTraces.size()+1][];

		// id 0 marks an empty sub trace... should not really happen, but just in case it does... :/
		this.nodeIdSequences[0] = new int[0];
		for (int i = 1; i < existingSubTraces.size() + 1; i++) {
			InputSequence<Integer> inputSequence = getInputSequenceFromByteArray(existingSubTraces.get(i), subTraceGrammar);
			int[] nodeIdSequence = new int[(int) inputSequence.getLength()];
			ListIterator<Integer> iterator = inputSequence.iterator();
			for (int j = 0; iterator.hasNext(); ++j) {
				nodeIdSequence[j] = iterator.next();
			}
			
			this.nodeIdSequences[i] = nodeIdSequence;
		}
	}
	
	// constructor used before storing in zip file
	public SimpleIntIndexerCompressed(SharedOutputGrammar<Integer> executionTraceGrammar, int[][] nodeIdSequences) {
		this.executionTraceOutputGrammar = executionTraceGrammar;
		this.nodeIdSequences = nodeIdSequences;
	}
	
	// constructor used when grammar is included in execution traces
	public SimpleIntIndexerCompressed(int[][] nodeIdSequences) {
		this.nodeIdSequences = nodeIdSequences;
	}
	
	private InputSequence<Integer> getInputSequenceFromByteArray(byte[] bytes,
			SharedInputGrammar<Integer> inGrammar) throws IOException {
		ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
		ObjectInputStream objIn = new ObjectInputStream(byteIn);
		InputSequence<Integer> inputSequence = InputSequence.readFrom(objIn, inGrammar);
		return inputSequence;
	}
	
	@Override
	public int[][] getNodeIdSequences() {
		return nodeIdSequences;
	}
	
	@Override
	public int[] getNodeIdSequence(int subTraceIndex) {
		if (subTraceIndex >= nodeIdSequences.length) {
			return null;
		}
		return nodeIdSequences[subTraceIndex];
	}
	
	@Override
	public void removeFromSequences(int nodeId) {
		// iterate over all sub traces
		// TODO: sub trace with id 0 is the empty sub trace. Should not exist, regularly
		for (int i = 1; i < nodeIdSequences.length; i++) {
			int[] sequence = nodeIdSequences[i];
			int foundCounter = 0;
			for (int id : sequence) {
				if (id == nodeId) {
					++foundCounter;
				}
			}
			if (foundCounter > 0) {
				// sequence contains the node, so generate a new sequence and replace the old
				int[] newSequence = new int[sequence.length - foundCounter];
				int j = 0;
				for (int id : sequence) {
					if (id != nodeId) {
						newSequence[j++] = id;
					}
				}
				nodeIdSequences[i] = newSequence;
			}
		}
	}

	@Override
	public void removeFromSequences(Collection<Integer> nodeIndicesToRemove) {
		// iterate over all sub traces
		// TODO: sub trace with id 0 is the empty sub trace. Should not exist, regularly
		for (int i = 1; i < nodeIdSequences.length; i++) {
			int[] sequence = nodeIdSequences[i];
			int foundCounter = 0;
			for (int id : sequence) {
				if (nodeIndicesToRemove.contains(id)) {
					++foundCounter;
				}
			}
			if (foundCounter > 0) {
				// sequence contains the node, so generate a new sequence and replace the old
				int[] newSequence = new int[sequence.length - foundCounter];
				int j = 0;
				for (int id : sequence) {
					if (!nodeIndicesToRemove.contains(id)) {
						newSequence[j++] = id;
					}
				}
				nodeIdSequences[i] = newSequence;
			}
		}
	}
	
	@Override
	public byte[] getGrammarByteArray() {
		if (storedGrammar == null && executionTraceOutputGrammar != null) {
			try {
				storedGrammar = SpectraFileUtils.convertToByteArray(executionTraceOutputGrammar);
			} catch (ClassNotFoundException | IOException e) {
				Log.abort(this, e, "Could not convert grammar.");
			}
		}
		return storedGrammar;
	}

	@Override
	public SharedInputGrammar<Integer> getExecutionTraceInputGrammar() {
		if (executionTraceInputGrammar == null) {
			if (storedGrammar != null) {
				try {
					executionTraceInputGrammar = SpectraFileUtils.convertToInputGrammar(storedGrammar);
				} catch (ClassNotFoundException | IOException e) {
					Log.abort(this, e, "Could not convert grammar.");
				}
			} else if (executionTraceOutputGrammar != null) {
				try {
					storedGrammar = SpectraFileUtils.convertToByteArray(executionTraceOutputGrammar);
					executionTraceInputGrammar = SpectraFileUtils.convertToInputGrammar(storedGrammar);
				} catch (ClassNotFoundException | IOException e) {
					Log.abort(this, e, "Could not convert grammar.");
				}
			}
		}
		return executionTraceInputGrammar;
	}

	@Override
	public Iterator<Integer> getNodeIdSequenceIterator(int subTraceId) {
		return Arrays.stream(nodeIdSequences[subTraceId]).iterator();
	}
}
