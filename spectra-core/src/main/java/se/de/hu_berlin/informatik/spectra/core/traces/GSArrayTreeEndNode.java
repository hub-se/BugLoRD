package se.de.hu_berlin.informatik.spectra.core.traces;

public class GSArrayTreeEndNode<T, K> extends GSArrayTreeNode<T, K> {

    private int index;

    public GSArrayTreeEndNode(int index) {
        super();
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
