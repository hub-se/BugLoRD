package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.Iterator;

public interface SequenceIndexer {

	public GSTreeNode[][] getSequences();
	
//	public Map<GSTreeNode,Integer> getEndNodeToSequenceIdMap();
	
	public int getSequenceIdForEndNode(GSTreeNode endNode);
	
//	public int[] getSequenceForEndNode(GSTreeNode endNode);

	int[] getSequence(int index);

	Iterator<Integer> getSequenceIterator(int index);

	int[][] getMappedSequences();

	public void removeFromSequences(int index);
	
}
