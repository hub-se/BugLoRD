package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.List;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CloneableIterator;

@Deprecated
public class GSIntTree extends GSArrayTree<Integer,Integer> {
	
	private static final Integer SUCC_END = -1;
	private static final Integer BAD_INDEX = -3;
	private static final Integer END_NODE = -2;

	@Override
	Integer getSequenceEndMarker() {
		return END_NODE;
	}

	@Override
	Integer getBadIndexMarker() {
		return BAD_INDEX;
	}

	@Override
	Integer getSuccessfulEndMarker() {
		return SUCC_END;
	}

	@Override
	Integer getRepresentation(Integer element) {
		return element;
	}

	@Override
	Integer[] newArray(int size) {
		return new Integer[size];
	}

	@Override
	GSArrayTreeNode<Integer, Integer> newTreeNode(GSArrayTree<Integer, Integer> treeReference2,
			CloneableIterator<Integer> unprocessedIterator, int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	GSArrayTreeNode<Integer, Integer> newTreeNode(GSArrayTree<Integer, Integer> treeReference2,
			Integer[] remainingSequence, List<GSArrayTreeNode<Integer, Integer>> existingEdges) {
		// TODO Auto-generated method stub
		return null;
	}

}
