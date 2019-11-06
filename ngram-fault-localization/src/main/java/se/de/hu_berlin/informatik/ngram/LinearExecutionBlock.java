package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.Node;

import java.util.List;

/**
 * A Linear Execution Block (LEB)  is a directed path within the execution graph
 * such that the in-degree of each vertex is 0 or 1.
 * Structurally a LEB consists of a BlockSequenceID and a sequence of ExeGraphNode of the spectra.
 */
public class LinearExecutionBlock extends Node {
    private List<Integer> blockSequence;

    /**
     * Constructs the node
     * since every nodes in this block were always executed together,
     * we can securely clone the data of the first node.
     * Because the Ids of the original nodes are unique, we also can re-use them.
     *
     * @param index      the integer index of this node
     * @param identifier the identifier of this node
     * @param spectra
     */
    protected LinearExecutionBlock(int index, Object identifier, ISpectra spectra) {
        super(index, identifier, spectra);
        adNodeToBlock(index);
    }

    public void adNodeToBlock(int index) {
        blockSequence.add(index);
    }


    public List<Integer> getBlockSequence() {
        return blockSequence;
    }


    public void adNodeToBlock(ExecutionGraphNode node){
        blockSequence.add(node.getNodeId());
    }
    public int getSize(){
        return blockSequence.size();
    }
}
