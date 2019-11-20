package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An execution graph node  consists structurally of three components.
 * The nodeId of this node in the spectra and two sets containing IDs
 * of other nodes that are executed before and after this node in the traces.
 */
public class ExecutionGraphNode {
    private final int index;
    private Set<Integer> InNodes;
    private Set<Integer> OutNodes;
    private ISpectra spectra;
    private int blockID = -1;

    /**
     * Constructs the node
     *
     * @param index   the integer index of this node
     * @param spectra
     */
    protected ExecutionGraphNode(int index, ISpectra spectra) {
        this.spectra = spectra;
        this.index = index;
        ConcurrentHashMap<Integer, Integer> in = new ConcurrentHashMap<>();
        ConcurrentHashMap<Integer, Integer> out = new ConcurrentHashMap<>();
        InNodes = in.keySet(index);
        OutNodes = out.keySet(index);
    }

    public int getBlockID() {
        return blockID;
    }

    public void setBlockID(int blockID) {
        this.blockID = blockID;
    }

    public int getIndex() {
        return index;
    }

    public Set<Integer> getInNodes() {
        return InNodes;
    }

    public Set<Integer> getOutNodes() {
        return OutNodes;
    }


    public boolean checkInNode(Integer n) {
        return InNodes.contains(n);
    }

    public boolean addInNode(Integer n) {
        return InNodes.add(n);
    }

    public int getInDegree() {
        return InNodes.size();
    }

    public boolean checkOutNode(Integer n) {
        return OutNodes.contains(n);
    }

    public boolean addOutNode(Integer n) {
        return OutNodes.add(n);
    }

    public int getOutDegree() {
        return OutNodes.size();
    }

    public INode getSpectraNode() {
        return spectra.getNode(index);
    }

    @Override
    public String toString() {
        return "\n\t\t\t{" +
                "nodeId=" + index +
                ", blockId=" + blockID +
                ", InNodes=" + InNodes +
                ", OutNodes=" + OutNodes +
                ", EF=" + spectra.getNode(index).getEF() +
                ", EP=" + spectra.getNode(index).getEP() +
                "}";
    }
}
