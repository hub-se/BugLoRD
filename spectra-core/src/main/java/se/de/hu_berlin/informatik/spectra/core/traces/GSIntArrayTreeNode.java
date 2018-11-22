package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.List;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CloneableIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.IntArrayWrapper;

public class GSIntArrayTreeNode extends GSArrayTreeNode<int[], IntArrayWrapper> {

	public GSIntArrayTreeNode(GSArrayTree<int[], IntArrayWrapper> treeReference,
			CloneableIterator<int[]> unprocessedIterator, int i) {
		super(treeReference, unprocessedIterator, i);
	}

	public GSIntArrayTreeNode(GSArrayTree<int[], IntArrayWrapper> treeReference, int[][] remainingSequence,
			List<GSArrayTreeNode<int[], IntArrayWrapper>> existingEdges) {
		super(treeReference, remainingSequence, existingEdges);
	}
	
}
