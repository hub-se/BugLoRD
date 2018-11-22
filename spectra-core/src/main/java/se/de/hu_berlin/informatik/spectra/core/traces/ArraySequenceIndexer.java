package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.Iterator;

public interface ArraySequenceIndexer<T,K> {

	public GSArrayTreeNode<T,K>[][] getSequences();
	
//	public Map<GSTreeNode,Integer> getEndNodeToSequenceIdMap();
	
	public int getSequenceIdForEndNode(GSArrayTreeNode<T,K> endNode);
	
//	public int[] getSequenceForEndNode(GSTreeNode endNode);

	T[] getSequence(int index);

	Iterator<T> getSequenceIterator(int index);

	T[][] getMappedSequences();

	public void removeFromSequences(T element);
	
}
