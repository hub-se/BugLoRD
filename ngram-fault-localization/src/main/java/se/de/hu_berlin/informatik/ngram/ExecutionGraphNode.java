package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An execution graph node  consists structurally of three components.
 * The nodeId of this node in the spectra and two sets containing IDs
 * of other nodes that are executed before and after this node in the traces.
 */
public class ExecutionGraphNode {
    private final int index;
    private ConcurrentHashMap<Integer, Integer> InNodes;
    private ConcurrentHashMap<Integer, Integer> OutNodes;
    private ISpectra spectra;
    private AtomicInteger blockID;

    /**
     * Constructs the node
     *
     * @param index   the integer index of this node
     * @param spectra
     */
    protected ExecutionGraphNode(int index, ISpectra spectra) {
        this.spectra = spectra;
        this.index = index;
        InNodes = new ConcurrentHashMap<>();
        OutNodes = new ConcurrentHashMap<>();
        blockID = new AtomicInteger(-1);
    }

    int getBlockID() {
        return blockID.intValue();
    }

    public void setBlockID(int blockID) {
        this.blockID.lazySet(blockID);
    }

    public int getIndex() {
        return index;
    }

    public Set<Integer> getInNodes() {
        return InNodes.keySet(index);
    }

    public Set<Integer> getOutNodes() {
        return OutNodes.keySet(index);
    }


    public boolean checkInNode(Integer n) {
        return InNodes.contains(n);
    }

    public void addInNode(Integer n) {
        InNodes.computeIfAbsent(n, v -> new Integer(index));
    }

    public int getInDegree() {
        return InNodes.size();
    }

    public boolean checkOutNode(Integer n) {
        return OutNodes.contains(n);
    }

    public void addOutNode(Integer n) {
        OutNodes.computeIfAbsent(n, v -> new Integer(index));
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
                ", blockId=" + blockID.intValue() +
                ", InNodes=" + InNodes.keySet(index) +
                ", OutNodes=" + OutNodes.keySet(index) +
                ", EF=" + spectra.getNode(index).getEF() +
                ", EP=" + spectra.getNode(index).getEP() +
                "}";
    }
}
