package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.Iterator;

public interface IntArraySequenceIndexer {

    public IntGSArrayTreeNode[][] getSequences();

//	public Map<GSTreeNode,Integer> getEndNodeToSequenceIdMap();

    public int getSequenceIdForEndNode(IntGSArrayTreeNode endNode);

//	public int[] getSequenceForEndNode(GSTreeNode endNode);

    int[] getSequence(int index);

    Iterator<Integer> getSequenceIterator(int index);

    int[][] getMappedSequences();

    public void removeFromSequences(int element);

    public void reset();

    public boolean isIndexed();

    public void generateSequenceIndex();

    public int getSequenceLength(int index);

}
