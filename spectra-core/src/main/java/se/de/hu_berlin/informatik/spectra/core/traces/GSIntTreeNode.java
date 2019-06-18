package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.List;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CloneableIterator;

public class GSIntTreeNode extends GSArrayTreeNode<Integer, Integer> {

	public GSIntTreeNode(GSArrayTree<Integer, Integer> treeReference,
			CloneableIterator<Integer> unprocessedIterator, int i) {
		super(treeReference, unprocessedIterator, i);
	}

	public GSIntTreeNode(GSArrayTree<Integer, Integer> treeReference, Integer[] remainingSequence,
			List<GSArrayTreeNode<Integer, Integer>> existingEdges) {
		super(treeReference, remainingSequence, existingEdges);
	}
	
}
