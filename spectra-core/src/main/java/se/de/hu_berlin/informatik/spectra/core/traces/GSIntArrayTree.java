package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.List;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CloneableIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.IntArrayWrapper;

public class GSIntArrayTree extends GSArrayTree<int[],IntArrayWrapper> {
	
	private static final IntArrayWrapper SUCC_END = new IntArrayWrapper(new int[] {-1});
	private static final IntArrayWrapper BAD_INDEX = new IntArrayWrapper(new int[] {-3});
	private static final IntArrayWrapper END_NODE = new IntArrayWrapper(new int[] {-2});

	@Override
	IntArrayWrapper getSequenceEndMarker() {
		return END_NODE;
	}

	@Override
	IntArrayWrapper getBadIndexMarker() {
		return BAD_INDEX;
	}

	@Override
	IntArrayWrapper getSuccessfulEndMarker() {
		return SUCC_END;
	}

	@Override
	IntArrayWrapper getRepresentation(int[] element) {
		return new IntArrayWrapper(element);
	}

	@Override
	int[][] newArray(int size) {
		return new int[size][];
	}

	@Override
	GSArrayTreeNode<int[], IntArrayWrapper> newTreeNode(GSArrayTree<int[], IntArrayWrapper> treeReference,
			CloneableIterator<int[]> unprocessedIterator, int i) {
		return new GSIntArrayTreeNode(treeReference, unprocessedIterator, i);
	}

	@Override
	GSArrayTreeNode<int[], IntArrayWrapper> newTreeNode(GSArrayTree<int[], IntArrayWrapper> treeReference,
			int[][] remainingSequence, List<GSArrayTreeNode<int[], IntArrayWrapper>> existingEdges) {
		return new GSIntArrayTreeNode(treeReference, remainingSequence, existingEdges);
	}

}
