package se.de.hu_berlin.informatik.spectra.core.traces;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.IntArrayWrapper;

public class GSIntArrayTreeIndexer extends GSArrayTreeIndexer<int[], IntArrayWrapper> {

	public GSIntArrayTreeIndexer(GSArrayTree<int[], IntArrayWrapper> tree) {
		super(tree);
	}

	@Override
	GSIntArrayTreeNode[][] newSequencesArray(int size) {
		return new GSIntArrayTreeNode[size][];
	}

	@Override
	GSIntArrayTreeNode[] newArray(int size) {
		return new GSIntArrayTreeNode[size];
	}

}
