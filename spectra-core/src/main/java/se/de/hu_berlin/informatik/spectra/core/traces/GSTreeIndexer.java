package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.HashMap;
import java.util.Map;

public class GSTreeIndexer implements SequenceIndexer {

	// mapping from tree end nodes (suffixes) to the indices of the sequences in the below array
	private Map<GSTreeNode,Integer> endNodeToSequenceIdMap;
	// array of all existing sequences, extracted from the tree
	private int[][] sequences;
	private GSTree tree;
	
	
	public GSTreeIndexer(GSTree tree) {
		this.tree = tree;
	}
	
	@Override
	public int[][] getSequences() {
		if (sequences == null || endNodeToSequenceIdMap == null) {
			generateSequenceIndex();
		}
		return sequences;
	}
	
	@Override
	public Map<GSTreeNode,Integer> getEndNodeToSequenceIdMap() {
		if (sequences == null || endNodeToSequenceIdMap == null) {
			generateSequenceIndex();
		}
		return endNodeToSequenceIdMap;
	}
	
	@Override
	public int getSequenceIdForEndNode(GSTreeNode endNode) {
		if (endNode == null) {
			return GSTree.BAD_INDEX;
		}
		if (endNodeToSequenceIdMap == null) {
			generateSequenceIndex();
		}
		return endNodeToSequenceIdMap.get(endNode);
	}
	
	@Override
	public int[] getSequenceForEndNode(GSTreeNode endNode) {
		if (sequences == null || endNodeToSequenceIdMap == null) {
			generateSequenceIndex();
		}
		Integer id = endNodeToSequenceIdMap.get(endNode);
		if (id == null) {
			return null;
		}
		return sequences[id];
	}
	
	private int currentIndex = 0;
	
	private void generateSequenceIndex() {
		int suffixCount = tree.countAllSuffixes();
		
		sequences = new int[suffixCount][];
		endNodeToSequenceIdMap = new HashMap<>();
		
		currentIndex = 0;
		// iterate over all different branches (starting with the same element)
		for (GSTreeNode node : tree.getBranches().values()) {
			collectAllSuffixes(new int[] {}, node);
		}
	}
	
	private void collectAllSuffixes(int[] sequence, GSTreeNode node) {
		if (node.getFirstElement() == GSTree.SEQUENCE_END) {
			sequences[currentIndex] = sequence;
			endNodeToSequenceIdMap.put(node, currentIndex);
			++currentIndex;
			return;
		}
		
		int[] concatenation = new int[sequence.length + node.getSequence().length];
		System.arraycopy(sequence,0,concatenation,0,sequence.length);
		System.arraycopy(node.getSequence(),0,concatenation,sequence.length,node.getSequence().length);

		for (GSTreeNode edge : node.getEdges()) {
			collectAllSuffixes(concatenation, edge);
		}
	}

	@Override
	public int[] getSequence(int index) {
		if (index == GSTree.BAD_INDEX) {
			throw new IllegalStateException("Bad sequence index!");
		}
		if (sequences == null || endNodeToSequenceIdMap == null) {
			generateSequenceIndex();
		}
		return sequences[index];
	}
	
}
