package se.de.hu_berlin.informatik.spectra.core.traces;

public class IntGSArrayTreeEndNode extends IntGSArrayTreeNode {

    private int index;

    public IntGSArrayTreeEndNode(int index) {
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
