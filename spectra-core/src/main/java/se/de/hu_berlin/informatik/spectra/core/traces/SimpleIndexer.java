package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.Map;

public class SimpleIndexer implements SequenceIndexer {

	// array of all existing sequences
	private int[][] sequences;
	
	
	public SimpleIndexer(int[][] sequences) {
		this.sequences = sequences;
	}
	
	@Override
	public int[][] getSequences() {
		return sequences;
	}
	
	@Override
	public Map<GSTreeNode,Integer> getEndNodeToSequenceIdMap() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getSequenceIdForEndNode(GSTreeNode endNode) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int[] getSequenceForEndNode(GSTreeNode endNode) {
		throw new UnsupportedOperationException();
	}
	
}
