package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A Linear Execution Block (LEB)  is a directed path within the execution graph
 * such that the in-degree of each vertex is 0 or 1.
 * Structurally a LEB consists of a BlockSequenceID and a sequence of ExeGraphNode of the spectra.
 */
public class LinearExecutionBlock {
    private Queue<ExecutionGraphNode> nodeSequence;
    private int index;
    private ISpectra spectra;


    /**
     * Constructs the node
     * since every nodes in this block were always executed together,
     * we can securely clone the data of the first node.
     * Because the Ids of the original nodes are unique, we also can re-use them.
     *
     * @param index      the integer index of this node
     * @param spectra
     */
    protected LinearExecutionBlock(int index, ISpectra spectra) {
        this.spectra = spectra;
        this.index = index;
        nodeSequence = new LinkedList<>();
    }
    protected LinearExecutionBlock(ISpectra spectra) {
        this.spectra = spectra;
        nodeSequence = new LinkedList<>();
    }



    public Queue<ExecutionGraphNode> getNodeSequence() {
        return nodeSequence;
    }


    public void adNodeToBlock(ExecutionGraphNode node){
        nodeSequence.add(node);
    }
    public int getSize(){
        return nodeSequence.size();
    }
    @Override
    public String toString(){
        return "\n\t\tBlockID: "+getIndex() + "\n\t\t\tNodeIDs: "+ getNodeSequence();
    }

    public int getIndex() {
        return index;
    }
    public void setIndex(int index){
        this.index = index;
    }
    boolean isEmpty(){
        return nodeSequence.size() == 0;
    }
}
