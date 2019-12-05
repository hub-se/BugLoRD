package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class IntGSArrayTreeIndexer implements IntArraySequenceIndexer {

	// array of all existing sequences, extracted from the tree
	private IntGSArrayTreeNode[][] sequences;
	private final IntGSArrayTree tree;
	
	
	public IntGSArrayTreeIndexer(IntGSArrayTree tree) {
		this.tree = Objects.requireNonNull(tree);
	}
	
	@Override
	public IntGSArrayTreeNode[][] getSequences() {
		if (!isIndexed()) {
			generateSequenceIndex();
		}
		return sequences;
	}
	
	@Override
	public void reset() {
		sequences = null;
	}
	
//	@Override
//	public Map<GSTreeNode,Integer> getEndNodeToSequenceIdMap() {
//		if (sequences == null || endNodeToSequenceIdMap == null) {
//			generateSequenceIndex();
//		}
//		return endNodeToSequenceIdMap;
//	}
	
	@Override
	public int getSequenceIdForEndNode(IntGSArrayTreeNode endNode) {
		if (endNode == null) {
			System.err.println("End node is null.");
			return GSArrayTree.BAD_INDEX;
		}
		if (endNode instanceof IntGSArrayTreeEndNode) {
			if (!isIndexed()) {
				generateSequenceIndex();
			}
			return ((IntGSArrayTreeEndNode) endNode).getIndex();
		} else {
			System.err.println("End node is not an end node.");
			return GSArrayTree.BAD_INDEX;
		}
	}

	@Override
	public boolean isIndexed() {
		return sequences != null;
	}
	
//	@Override
//	public int[] getSequenceForEndNode(GSTreeNode endNode) {
//		if (sequences == null || endNodeToSequenceIdMap == null) {
//			generateSequenceIndex();
//		}
//		Integer id = endNodeToSequenceIdMap.get(endNode);
//		if (id == null) {
//			return null;
//		}
//		return sequences[id];
//	}
	
	IntGSArrayTreeNode[][] newSequencesArray(int size) {
		return new IntGSArrayTreeNode[size][];
	}

	IntGSArrayTreeNode[] newArray(int size) {
		return new IntGSArrayTreeNode[size];
	}
	
//	private int currentIndex = 0;
	
	private int maxNodeSequenceLength = 0;
	private int minNodeSequenceLength = Integer.MAX_VALUE;
	private int maxSequenceLength = 0;
	private int minSequenceLength = Integer.MAX_VALUE;
	private long nodeSequenceLengthSum = 0;
	private long sequenceLengthSum = 0;
	
	@Override
	public void generateSequenceIndex() {
		maxSequenceLength = 0;
		minSequenceLength = Integer.MAX_VALUE;
		sequenceLengthSum = 0;
		
		maxNodeSequenceLength = 0;
		minNodeSequenceLength = Integer.MAX_VALUE;
		nodeSequenceLengthSum = 0;
		
//		currentIndex = 0;
		int suffixCount = tree.countAllSuffixes();
		// initialize sequence array
		sequences = newSequencesArray(suffixCount);
		
		// iterate over all different branches (starting with the same element)
		for (IntGSArrayTreeNode node : tree.getBranches().values()) {
			collectAllSuffixes(newArray(0), node);
		}
		
		Log.out(this, "Statistics:%n"
				+ "- %-30s %,d%n"
				+ "- %-30s %,d%n"
				+ "- %-30s %,d%n"
				+ "- %-30s %,d%n"
				+ "- %-30s %.2f%n"
				+ "- %-30s %,d%n"
				+ "- %-30s %,d%n"
				+ "- %-30s %.2f", 
				"number of sequences:", suffixCount, "starting elements:", tree.getBranches().size(), 
				"minimum node sequence length:", minNodeSequenceLength, "maximum node sequence length:", maxNodeSequenceLength,
				"mean node sequence length:", suffixCount > 0 ? (float)nodeSequenceLengthSum/(float)suffixCount : 0,
				"minimum sequence length:", minSequenceLength, "maximum sequence length:", maxSequenceLength,
				"mean sequence length:", suffixCount > 0 ? (float)sequenceLengthSum/(float)suffixCount : 0);
	}
	
	private void collectAllSuffixes(IntGSArrayTreeNode[] sequence, IntGSArrayTreeNode node) {
		if (node instanceof IntGSArrayTreeEndNode) {
			sequences[((IntGSArrayTreeEndNode) node).getIndex()] = sequence;
			
			maxNodeSequenceLength = Math.max(maxNodeSequenceLength, sequence.length);
			minNodeSequenceLength = Math.min(minNodeSequenceLength, sequence.length);
			nodeSequenceLengthSum += sequence.length;
			
			int length = 0;
			for (IntGSArrayTreeNode seqNode : sequence) {
				length += seqNode.getSequence().length;
			}
			maxSequenceLength = Math.max(maxSequenceLength, length);
			minSequenceLength = Math.min(minSequenceLength, length);
			sequenceLengthSum += length;
			return;
		}
		
		IntGSArrayTreeNode[] concatenation = newArray(sequence.length + 1);
		System.arraycopy(sequence,0,concatenation,0,sequence.length);
		concatenation[sequence.length] = node;

		for (IntGSArrayTreeNode edge : node.getEdges()) {
			collectAllSuffixes(concatenation, edge);
		}
	}

	@Override
	public int[] getSequence(int index) {
		if (index == GSArrayTree.BAD_INDEX) {
			throw new IllegalStateException("Bad sequence index!");
		}
		if (!isIndexed()) {
			generateSequenceIndex();
		}
		if (index < 0 || index >= tree.getEndNodeCount()) {
			throw new IllegalStateException("Index out of range: " + index);
		}
		return generateSequence(sequences[index]);
	}

	private int[] generateSequence(IntGSArrayTreeNode[] gsTreeNodes) {
		int length = 0;
		for (IntGSArrayTreeNode gsTreeNode : gsTreeNodes) {
			length += gsTreeNode.getSequence().length;
		}
		int[] sequence = tree.newArray(length);
		int j = 0;
		for (IntGSArrayTreeNode gsTreeNode : gsTreeNodes) {
			for (int i = 0; i < gsTreeNode.getSequence().length; i++) {
				sequence[j++] = gsTreeNode.getSequence()[i];
			}
		}
		return sequence;
	}
	
	@Override
	public Iterator<Integer> getSequenceIterator(int index) {
		if (index == GSArrayTree.BAD_INDEX) {
			throw new IllegalStateException("Bad sequence index!");
		}
		if (!isIndexed()) {
			generateSequenceIndex();
		}
		if (index < 0 || index >= tree.getEndNodeCount()) {
			throw new IllegalStateException("Index out of range: " + index);
		}
		
		return new SequenceIterator(sequences[index]);
	}
	
	@Override
	public int getSequenceLength(int index) {
		if (index == GSArrayTree.BAD_INDEX) {
			throw new IllegalStateException("Bad sequence index!");
		}
		if (!isIndexed()) {
			generateSequenceIndex();
		}
		int length = 0;
		for (IntGSArrayTreeNode gsTreeNode : sequences[index]) {
			length += gsTreeNode.getSequence().length;
		}
		return length;
	}
	
	private final static class SequenceIterator implements Iterator<Integer> {

		private final IntGSArrayTreeNode[] gsTreeNodes;
		private int nodeIndex = 0;
		private int sequenceIndex = 0;

		public SequenceIterator(IntGSArrayTreeNode[] gsTreeNodes) {
			this.gsTreeNodes = Objects.requireNonNull(gsTreeNodes);
		}

		@Override
		public boolean hasNext() {
			while (nodeIndex < gsTreeNodes.length && gsTreeNodes[nodeIndex].getSequence().length == 0) {
				// skip empty nodes
				++nodeIndex;
			}
			return nodeIndex < gsTreeNodes.length && 
					sequenceIndex < gsTreeNodes[nodeIndex].getSequence().length;
		}

		@Override
		public Integer next() {
			int next = gsTreeNodes[nodeIndex].getSequence()[sequenceIndex++];
			if (sequenceIndex >= gsTreeNodes[nodeIndex].getSequence().length) {
				++nodeIndex;
				sequenceIndex = 0;
			}
			return next;
		}
		
	}

	@Override
	public int[][] getMappedSequences() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeFromSequences(int element) {
		// this needs to be done AFTER all traces have been indexed
		// TODO make sure!
		if (!isIndexed()) {
			generateSequenceIndex();
		}

		// iterate over all sequences (it would suffice to iterate over each single node in the tree TODO)
        for (IntGSArrayTreeNode[] sequence : sequences) {
            boolean found = false;
            for (IntGSArrayTreeNode tkgsArrayTreeNode : sequence) {
                if (tkgsArrayTreeNode.contains(element)) {
                    // sequence contains the node at least once
                    found = true;
                }
                if (found) {
                    // sequence contains the node, so generate a new sequence and replace the old
                    List<Integer> newSequence = new ArrayList<>(tkgsArrayTreeNode.getSequence().length - 1);
                    for (int k = 0; k < tkgsArrayTreeNode.getSequence().length; k++) {
                        if (tkgsArrayTreeNode.getSequence()[k] != element) {
                            newSequence.add(tkgsArrayTreeNode.getSequence()[k]);
                        }
                    }
                    int[] seq = tree.newArray(newSequence.size());
                    for (int l = 0; l < seq.length; l++) {
                        seq[l] = newSequence.get(l);
                    }
                    tkgsArrayTreeNode.setSequence(seq);
                }
                found = false;
            }
        }
	}
	
}
