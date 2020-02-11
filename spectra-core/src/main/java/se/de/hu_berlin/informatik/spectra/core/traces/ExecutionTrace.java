package se.de.hu_berlin.informatik.spectra.core.traces;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence.TraceIterator;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * An execution trace consists structurally of a list of executed nodes (or references to node lists)
 * and a list of tuples that mark repeated sequences in the trace.
 *
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
		// lazy instanciation
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
	 * @param indexer
	 * indexer that is used to connect the element IDs in the execution trace to the respective sub traces
	 * that contain node IDs
	 * @return
	 * array that contains all executed node IDs
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
	 * @param sequenceIndexer
	 * indexer that is used to connect the element IDs in the execution trace to the respective sub traces
	 * that contain node IDs
	 * @return
	 * iterator
	 */
	public Iterator<Integer> mappedIterator(SequenceIndexerCompressed sequenceIndexer) {
		return new Iterator<Integer>(){
			
			final TraceIterator iterator = ExecutionTrace.this.iterator();
			int[] currentSequence;
			int subTraceIndex = 0;

			@Override
			public boolean hasNext() {
				if (currentSequence == null || subTraceIndex >= currentSequence.length) {
					currentSequence = null;
					while (iterator.hasNext()) {
						currentSequence = sequenceIndexer.getNodeIdSequence(iterator.next());
						if (currentSequence.length > 0) {
							// found a "good" sequence
							subTraceIndex = 0;
							break;
						}
						currentSequence = null;
					}
					
					// found no sequence?
                    return currentSequence != null;
				}
				
				return true;
			}

			@Override
			public Integer next() {
				return currentSequence[subTraceIndex++];
			}
		};
	}
	
	/**
	 * iterates over all node IDs in the execution trace, starting from the end of the trace. 
	 * @param sequenceIndexer
	 * indexer that is used to connect the element IDs in the execution trace to the respective sub traces
	 * that contain node IDs
	 * @return
	 * iterator
	 */
	public Iterator<Integer> mappedReverseIterator(SequenceIndexerCompressed sequenceIndexer) {
		return new Iterator<Integer>(){
			
			final TraceIterator iterator = ExecutionTrace.this.reverseIterator();
			int[] currentSequence;
			int subTraceIndex = -1;

			@Override
			public boolean hasNext() {
				if (currentSequence == null || subTraceIndex < 0) {
					currentSequence = null;
					while (iterator.hasPrevious()) {
						currentSequence = sequenceIndexer.getNodeIdSequence(iterator.previous());
						if (currentSequence.length > 0) {
							// found a "good" sequence
							subTraceIndex = currentSequence.length - 1;
							break;
						}
						currentSequence = null;
					}
					
					// found no sequence?
                    return currentSequence != null;
				}
				
				return true;
			}

			@Override
			public Integer next() {
				return currentSequence[subTraceIndex--];
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
