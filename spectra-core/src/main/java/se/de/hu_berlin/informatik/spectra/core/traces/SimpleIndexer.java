package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimpleIndexer implements SequenceIndexer {

	// array of all existing sequences
	private int[][] sequences;
	
	
	public SimpleIndexer(int[][] sequences) {
		this.sequences = sequences;
	}
	
	@Override
	public int[][] getMappedSequences() {
		return sequences;
	}
	
	@Override
	public int[] getSequence(int index) {
		if (index >= sequences.length) {
			return new int[] {};
		}
		return sequences[index];
	}
	
//	@Override
//	public Map<GSTreeNode,Integer> getEndNodeToSequenceIdMap() {
//		throw new UnsupportedOperationException();
//	}
	
	@Override
	public int getSequenceIdForEndNode(GSTreeNode endNode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GSTreeNode[][] getSequences() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Integer> getSequenceIterator(final int index) {
		return new Iterator<Integer>() {
            private int pos = 0;

            public boolean hasNext() {
               return pos < sequences[index].length;
            }

            public Integer next() {
               return sequences[index][pos++];
            }
        };
	}

	@Override
	public void removeFromSequences(int index) {
		// iterate over all sequences
		for (int i = 0; i < sequences.length; i++) {
			int[] sequence = sequences[i];
			boolean found = false;
			for (int j = 0; j < sequence.length; j++) {
				if (sequence[j] == index) {
					// sequence contains the node at least once 
					found = true;
					break;
				}
			}
			if (found) {
				// sequence contains the node, so generate a new sequence and replace the old
				List<Integer> newSequence = new ArrayList<>(sequence.length - 1);
				for (int j = 0; j < sequence.length; j++) {
					if (sequence[j] != index) {
						newSequence.add(sequence[j]);
					}
				}
				sequences[i] = newSequence.stream().mapToInt(k -> k).toArray();
			}
		}
	}
	
//	@Override
//	public int[] getSequenceForEndNode(GSTreeNode endNode) {
//		throw new UnsupportedOperationException();
//	}
	
}
