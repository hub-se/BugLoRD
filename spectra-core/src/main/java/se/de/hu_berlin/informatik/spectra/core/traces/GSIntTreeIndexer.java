package se.de.hu_berlin.informatik.spectra.core.traces;

public class GSIntTreeIndexer extends GSArrayTreeIndexer<Integer, Integer> {

	public GSIntTreeIndexer(GSArrayTree<Integer, Integer> tree) {
		super(tree);
	}

	@Override
	GSIntTreeNode[][] newSequencesArray(int size) {
		return new GSIntTreeNode[size][];
	}

	@Override
	GSIntTreeNode[] newArray(int size) {
		return new GSIntTreeNode[size];
	}

}
