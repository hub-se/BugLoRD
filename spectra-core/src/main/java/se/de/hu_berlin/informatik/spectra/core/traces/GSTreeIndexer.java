package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class GSTreeIndexer implements SequenceIndexer {

	// array of all existing sequences, extracted from the tree
	private GSTreeNode[][] sequences;
	private final GSTree tree;
	
	
	public GSTreeIndexer(GSTree tree) {
		this.tree = tree;
	}
	
	@Override
	public GSTreeNode[][] getSequences() {
		if (sequences == null) {
			generateSequenceIndex();
		}
		return sequences;
	}
	
//	@Override
//	public Map<GSTreeNode,Integer> getEndNodeToSequenceIdMap() {
//		if (sequences == null || endNodeToSequenceIdMap == null) {
//			generateSequenceIndex();
//		}
//		return endNodeToSequenceIdMap;
//	}
	
	@Override
	public int getSequenceIdForEndNode(GSTreeNode endNode) {
		if (endNode == null) {
			System.err.println("End node is null.");
			return GSTree.BAD_INDEX;
		}
		if (endNode.getSequence()[0] != GSTree.SEQUENCE_END) {
			System.err.println("End node is not an end node.");
			return GSTree.BAD_INDEX;
		}
		if (sequences == null) {
			generateSequenceIndex();
		}
		return endNode.getSequence()[1];
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
	
	private int currentIndex = 0;
	
	private void generateSequenceIndex() {
		int suffixCount = tree.countAllSuffixes();
		Log.out(this, "Number of sequences: %d", suffixCount);
		sequences = new GSTreeNode[suffixCount][];
		
		currentIndex = 0;
		// iterate over all different branches (starting with the same element)
		for (GSTreeNode node : tree.getBranches().values()) {
			collectAllSuffixes(new GSTreeNode[] {}, node);
		}
	}
	
	private void collectAllSuffixes(GSTreeNode[] sequence, GSTreeNode node) {
		if (node.getFirstElement() == GSTree.SEQUENCE_END) {
			sequences[currentIndex] = sequence;
			node.getSequence()[1] = currentIndex;
			++currentIndex;
			return;
		}
		
		GSTreeNode[] concatenation = new GSTreeNode[sequence.length + 1];
		System.arraycopy(sequence,0,concatenation,0,sequence.length);
		concatenation[sequence.length] = node;

		for (GSTreeNode edge : node.getEdges()) {
			collectAllSuffixes(concatenation, edge);
		}
	}

	@Override
	public int[] getSequence(int index) {
		if (index == GSTree.BAD_INDEX) {
			throw new IllegalStateException("Bad sequence index!");
		}
		if (sequences == null) {
			generateSequenceIndex();
		}
		if (index < 0 || index >= tree.getEndNodeCount()) {
			throw new IllegalStateException("Index out of range: " + index);
		}
		return generateSequence(sequences[index]);
	}

	private int[] generateSequence(GSTreeNode[] gsTreeNodes) {
		int length = 0;
		for (GSTreeNode gsTreeNode : gsTreeNodes) {
			length += gsTreeNode.getSequence().length;
		}
		int[] sequence = new int[length];
		int j = 0;
		for (GSTreeNode gsTreeNode : gsTreeNodes) {
			for (int i = 0; i < gsTreeNode.getSequence().length; i++) {
				sequence[j++] = gsTreeNode.getSequence()[i];
			}
		}
		return sequence;
	}
	
	@Override
	public Iterator<Integer> getSequenceIterator(int index) {
		if (index == GSTree.BAD_INDEX) {
			throw new IllegalStateException("Bad sequence index!");
		}
		if (sequences == null) {
			generateSequenceIndex();
		}
		if (index < 0 || index >= tree.getEndNodeCount()) {
			throw new IllegalStateException("Index out of range: " + index);
		}
		
		return new SequenceIterator(sequences[index]);
	}
	
	private final static class SequenceIterator implements Iterator<Integer> {

		private final GSTreeNode[] gsTreeNodes;
		private int nodeIndex = 0;
		private int sequenceIndex = 0;

		public SequenceIterator(GSTreeNode[] gsTreeNodes) {
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
	public void removeFromSequences(int index) {
		// this needs to be done AFTER all traces have been indexed
		// TODO make sure!
		if (sequences == null) {
			generateSequenceIndex();
		}
		
		// iterate over all sequences (it would suffice to iterate over each single node in the tree TODO)
        for (GSTreeNode[] sequence : sequences) {
            boolean found = false;
            for (GSTreeNode gsTreeNode : sequence) {
                if (gsTreeNode.contains(index)) {
                    // sequence contains the node at least once
                    found = true;
                }
                if (found) {
                    // sequence contains the node, so generate a new sequence and replace the old
                    List<Integer> newSequence = new ArrayList<>(gsTreeNode.getSequence().length - 1);
                    for (int k = 0; k < gsTreeNode.getSequence().length; k++) {
                        if (gsTreeNode.getSequence()[k] != index) {
                            newSequence.add(gsTreeNode.getSequence()[k]);
                        }
                    }
                    gsTreeNode.setSequence(newSequence.stream().mapToInt(k -> k).toArray());
                }
                found = false;
            }
        }
	}
	
}
