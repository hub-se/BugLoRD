package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public abstract class GSArrayTreeIndexer<T,K> implements ArraySequenceIndexer<T,K> {

	// array of all existing sequences, extracted from the tree
	private GSArrayTreeNode<T,K>[][] sequences;
	private final GSArrayTree<T,K> tree;
	
	
	public GSArrayTreeIndexer(GSArrayTree<T,K> tree) {
		this.tree = Objects.requireNonNull(tree);
	}
	
	@Override
	public GSArrayTreeNode<T,K>[][] getSequences() {
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
	public int getSequenceIdForEndNode(GSArrayTreeNode<T,K> endNode) {
		if (endNode == null) {
			System.err.println("End node is null.");
			return GSArrayTree.BAD_INDEX;
		}
		if (endNode instanceof GSArrayTreeEndNode) {
			if (!isIndexed()) {
				generateSequenceIndex();
			}
			return ((GSArrayTreeEndNode<T,K>) endNode).getIndex();
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
	
	abstract GSArrayTreeNode<T,K>[][] newSequencesArray(int size);
	
	abstract GSArrayTreeNode<T,K>[] newArray(int size);
	
//	private int currentIndex = 0;
	
	@Override
	public void generateSequenceIndex() {
		int suffixCount = tree.countAllSuffixes();
		Log.out(this, "Number of sequences: %d", suffixCount);
		sequences = newSequencesArray(suffixCount);
		
//		currentIndex = 0;
		// iterate over all different branches (starting with the same element)
		for (GSArrayTreeNode<T,K> node : tree.getBranches().values()) {
			collectAllSuffixes(newArray(0), node);
		}
	}
	
	private void collectAllSuffixes(GSArrayTreeNode<T,K>[] sequence, GSArrayTreeNode<T,K> node) {
		if (node instanceof GSArrayTreeEndNode) {
			sequences[((GSArrayTreeEndNode<T,K>) node).getIndex()] = sequence;
			return;
		}
		
		GSArrayTreeNode<T,K>[] concatenation = newArray(sequence.length + 1);
		System.arraycopy(sequence,0,concatenation,0,sequence.length);
		concatenation[sequence.length] = node;

		for (GSArrayTreeNode<T,K> edge : node.getEdges()) {
			collectAllSuffixes(concatenation, edge);
		}
	}

	@Override
	public T[] getSequence(int index) {
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

	private T[] generateSequence(GSArrayTreeNode<T,K>[] gsTreeNodes) {
		int length = 0;
		for (GSArrayTreeNode<T,K> gsTreeNode : gsTreeNodes) {
			length += gsTreeNode.getSequence().length;
		}
		T[] sequence = tree.newArray(length);
		int j = 0;
		for (GSArrayTreeNode<T,K> gsTreeNode : gsTreeNodes) {
			for (int i = 0; i < gsTreeNode.getSequence().length; i++) {
				sequence[j++] = gsTreeNode.getSequence()[i];
			}
		}
		return sequence;
	}
	
	@Override
	public Iterator<T> getSequenceIterator(int index) {
		if (index == GSArrayTree.BAD_INDEX) {
			throw new IllegalStateException("Bad sequence index!");
		}
		if (!isIndexed()) {
			generateSequenceIndex();
		}
		if (index < 0 || index >= tree.getEndNodeCount()) {
			throw new IllegalStateException("Index out of range: " + index);
		}
		
		return new SequenceIterator<>(sequences[index]);
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
		for (GSArrayTreeNode<T,K> gsTreeNode : sequences[index]) {
			length += gsTreeNode.getSequence().length;
		}
		return length;
	}
	
	private final static class SequenceIterator<T,K> implements Iterator<T> {

		private final GSArrayTreeNode<T,K>[] gsTreeNodes;
		private int nodeIndex = 0;
		private int sequenceIndex = 0;

		public SequenceIterator(GSArrayTreeNode<T,K>[] gsTreeNodes) {
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
		public T next() {
			T next = gsTreeNodes[nodeIndex].getSequence()[sequenceIndex++];
			if (sequenceIndex >= gsTreeNodes[nodeIndex].getSequence().length) {
				++nodeIndex;
				sequenceIndex = 0;
			}
			return next;
		}
		
	}

	@Override
	public T[][] getMappedSequences() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeFromSequences(T element) {
		// this needs to be done AFTER all traces have been indexed
		// TODO make sure!
		if (!isIndexed()) {
			generateSequenceIndex();
		}
		
		K rep = tree.getRepresentation(element);
		
		// iterate over all sequences (it would suffice to iterate over each single node in the tree TODO)
        for (GSArrayTreeNode<T, K>[] sequence : sequences) {
            boolean found = false;
            for (GSArrayTreeNode<T, K> tkgsArrayTreeNode : sequence) {
                if (tkgsArrayTreeNode.contains(rep)) {
                    // sequence contains the node at least once
                    found = true;
                }
                if (found) {
                    // sequence contains the node, so generate a new sequence and replace the old
                    List<T> newSequence = new ArrayList<>(tkgsArrayTreeNode.getSequence().length - 1);
                    for (int k = 0; k < tkgsArrayTreeNode.getSequence().length; k++) {
                        if (!tree.getRepresentation(tkgsArrayTreeNode.getSequence()[k]).equals(rep)) {
                            newSequence.add(tkgsArrayTreeNode.getSequence()[k]);
                        }
                    }
                    T[] seq = tree.newArray(newSequence.size());
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
