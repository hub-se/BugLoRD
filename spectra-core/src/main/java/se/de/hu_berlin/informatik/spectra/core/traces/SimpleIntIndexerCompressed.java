package se.de.hu_berlin.informatik.spectra.core.traces;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.SequiturUtils;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.SharedInputGrammar;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.SharedOutputGrammar;
import se.de.hu_berlin.informatik.spectra.util.CachedMap;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class SimpleIntIndexerCompressed implements SequenceIndexerCompressed {

	// mapping: sub trace ID -> sequence of spectra node IDs
	private CachedMap<int[]> nodeIdSequences;

	private SharedInputGrammar executionTraceInputGrammar;

	private byte[] storedGrammar;

	// constructor used when loading indexer from zip file
	public SimpleIntIndexerCompressed(byte[] storedGrammar, CachedMap<int[]> nodeIdSequences) {
		this.storedGrammar = storedGrammar;
		this.nodeIdSequences = nodeIdSequences;
	}
	
	// constructor used before storing in zip file
	public SimpleIntIndexerCompressed(SharedOutputGrammar executionTraceGrammar, 
			CachedMap<int[]> existingSubTraces) throws IOException {
//		System.out.println(String.format(
//				"- #sub traces: %,d", existingSubTraces.size()));

		// might be null if grammar is null
		this.storedGrammar = SequiturUtils.convertToByteArray(executionTraceGrammar);
		if (storedGrammar != null) {
			System.out.println(String.format(
					"- execution trace grammar size: %,d", storedGrammar.length/4));
		}

		this.nodeIdSequences = existingSubTraces;

		// id 0 marks an empty sub trace... should not really happen, but just in case it does... :/
		if (!this.nodeIdSequences.containsKey(0)) {
			this.nodeIdSequences.put(0, new int[0]);
		}
		
		// gather/print some statistics (loads from zip file)
		printStatistics();
	}

	private void printStatistics() {
		int min = Integer.MAX_VALUE;
		int max = 0;
		long sum = 0;
		
		// id 0 marks an empty sub trace
		for (int i = 1; i < this.nodeIdSequences.size(); i++) {
			int[] seq = this.nodeIdSequences.get(i);
			int length = seq.length;
			min = Math.min(min, length);
			max = Math.max(max, length);
			sum += length;
		}

		int count = this.nodeIdSequences.size()-1;
		
		Log.out(this, "Statistics:%n"
				+ "- %-30s %,d%n"
				+ "- %-30s %,d%n"
				+ "- %-30s %,d%n"
				+ "- %-30s %,d%n"
				+ "- %-30s %.2f%n", 
				"number of sequences:", count,
				"total size (int):", sum, 
				"minimum node sequence length:", min, 
				"maximum node sequence length:", max,
				"mean node sequence length:", count > 0 ? (float)sum/(float)count : 0);
	}

//	// constructor used before storing in zip file
//	public SimpleIntIndexerCompressed(SharedOutputGrammar executionTraceGrammar, int[][] nodeIdSequences) throws IOException {
//		// might be null if grammar is null
//		this.storedGrammar = SequiturUtils.convertToByteArray(executionTraceGrammar);
//		this.nodeIdSequences = nodeIdSequences;
//	}
	
	// constructor used when grammar is included in execution traces
	public SimpleIntIndexerCompressed(CachedMap<int[]> nodeIdSequences) {
		this.nodeIdSequences = nodeIdSequences;
	}
	
	@Override
	public CachedMap<int[]> getNodeIdSequences() {
		return nodeIdSequences;
	}
	
	@Override
	public int[] getNodeIdSequence(int subTraceIndex) {
		if (subTraceIndex >= nodeIdSequences.size()) {
			return null;
		}
		return nodeIdSequences.get(subTraceIndex);
	}
	
	@Override
	public void removeFromSequences(int nodeId) {
		// iterate over all sub traces
		// TODO: sub trace with id 0 is the empty sub trace. Should not exist, regularly
		for (int i = 1; i < nodeIdSequences.size(); i++) {
			int[] sequence = nodeIdSequences.get(i);
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
				// this needs to rewrite the entire zip archive!
				nodeIdSequences.put(i, newSequence);
			}
		}
	}

	@Override
	public void removeFromSequences(Collection<Integer> nodeIndicesToRemove) {
		// iterate over all sub traces
		// TODO: sub trace with id 0 is the empty sub trace. Should not exist, regularly
		for (int i = 1; i < nodeIdSequences.size(); i++) {
			int[] sequence = nodeIdSequences.get(i);
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
				// this needs to rewrite the entire zip archive!
				nodeIdSequences.put(i, newSequence);
			}
		}
	}
	
	@Override
	public byte[] getGrammarByteArray() {
		return storedGrammar;
	}

	@Override
	public SharedInputGrammar getExecutionTraceInputGrammar() {
		if (executionTraceInputGrammar == null && storedGrammar != null) {
			try {
				executionTraceInputGrammar = SequiturUtils.convertToInputGrammar(storedGrammar);
			} catch (IOException e) {
				Log.abort(this, e, "Could not convert grammar.");
			}
		}
		return executionTraceInputGrammar;
	}

	@Override
	public Iterator<Integer> getNodeIdSequenceIterator(int subTraceId) {
		return Arrays.stream(nodeIdSequences.get(subTraceId)).iterator();
	}
}
