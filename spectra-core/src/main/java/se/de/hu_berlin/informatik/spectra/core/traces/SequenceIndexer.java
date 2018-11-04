package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.Map;

public interface SequenceIndexer {

	public int[][] getSequences();
	
	public Map<GSTreeNode,Integer> getEndNodeToSequenceIdMap();
	
	public int getSequenceIdForEndNode(GSTreeNode endNode);
	
	public int[] getSequenceForEndNode(GSTreeNode endNode);

	int[] getSequence(int index);
	
}
