package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class LinearBlockSequence {
    private LinkedList<LinearExecutionBlock> blockSeq;
    private ISpectra spectra;

    public LinearBlockSequence(ISpectra spectra) {
        blockSeq = new LinkedList<>();
        this.spectra=spectra;
    }

    public LinkedList<LinearExecutionBlock> getBlockSeq() {
        return blockSeq;
    }

    //counting all contained nodes
    public int getAllNodeSize() {
        return blockSeq.stream().mapToInt(LinearExecutionBlock::getSize).sum();
    }

    public int getBlockSeqSize() {
        return blockSeq.size();
    }
    public List<ExecutionGraphNode> allNodes(){
        return blockSeq.stream().
                flatMap(block->block.getNodeSequence().stream()).collect(Collectors.toList());
    }
    @Override
    public String toString(){
        return "\t" +blockSeq;
    }
}
