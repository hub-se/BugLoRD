package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class GenerateLinearBlock {
    private GenerateLinearBlock() {
    }
    private static boolean hasMultiIn(ExecutionGraphNode node) {
        return (node.getInDegree() > 1);
    }

    private static boolean hasMultiOut(ExecutionGraphNode node) {
        return (node.getOutDegree() > 1);
    }
    private static boolean isLoop(ExecutionGraphNode node) {
        return node.checkInNode(node.getIndex());
    }
    public static void initGraphNode(ISpectra<SourceCodeBlock, ?> input, ConcurrentHashMap<Integer, ExecutionGraphNode> nodeSeq) {
        for (ITrace<SourceCodeBlock> test : input.getTraces()) {
            int lastId = -1;
            for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
                Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(input.getIndexer());

                while (nodeIdIterator.hasNext()) {
                    int nodeIndex = nodeIdIterator.next();
                    // nodeIndex is seen for the first time
                    nodeSeq.computeIfAbsent(
                            nodeIndex,
                            k -> new ExecutionGraphNode(
                                    nodeIndex, input));
                    //skip repetition
                    if (nodeSeq.get(nodeIndex).checkInNode(nodeIndex)) continue;


                    if (lastId == -1) {
                        lastId = nodeIndex;

                    } else {
                        nodeSeq.get(nodeIndex).addInNode(lastId);
                        nodeSeq.get(lastId).addOutNode(nodeIndex);
                        lastId = nodeIndex;
                    }

                }
            }
        }
    }
    public static void generateLinearBlockTrace(ISpectra<SourceCodeBlock, ?> input, ConcurrentHashMap<Integer, ExecutionGraphNode> nodeSeq, LinearExecutionHitTrace hitTrace){

        for(ITrace<SourceCodeBlock> test : input.getTraces()){
            LinearExecutionTestTrace testTrace = new LinearExecutionTestTrace(test.getIndex(),input);

            for (ExecutionTrace executionTrace : test.getExecutionTraces()){
                LinearBlockSequence innerTrace = new LinearBlockSequence(input);
                Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(input.getIndexer());
                HashSet<Integer> visited = new HashSet<>();
                LinearExecutionBlock currentBlock = new LinearExecutionBlock(input);
                int lastNode = -1;
                while (nodeIdIterator.hasNext()) {

                    int nodeIndex = nodeIdIterator.next();
                    //skip repetition
                    if (visited.contains(nodeIndex)) {
                        // store current block if the index will be skipped and there is no other element left
                        if (!nodeIdIterator.hasNext() && !currentBlock.isEmpty()) innerTrace.getBlockSeq().add(currentBlock);
                        continue;
                    } else visited.add(nodeIndex);

                    ExecutionGraphNode node = nodeSeq.get(nodeIndex);

                    // Block is not empty, check if it can hold the current node
                    // There are 4 scenarios:
                    // 1) add node to the block as normal
                    // 2) add node and close this block because this node has multiple outputs
                    // 3) do not add this node but create a new block and then add it afterwards
                    //    the reason is that this node has multiple inputs or it is a loop
                    // 4) last node is not a direct predecessor, current node must be added to a new block


                    // check if node must be added in a new block
                    // that is only the case if:
                    // - node  has a loop or
                    // - node has more than 1 inputs
                    // - lastNode is not a direct predecessor

                    if (isLoop(node) || hasMultiIn(node) || !node.checkInNode(lastNode)) {
                        // if block is not empty, close block and create a new one
                        if (!currentBlock.isEmpty()) {
                            innerTrace.getBlockSeq().add(currentBlock);
                            currentBlock = new LinearExecutionBlock(nodeIndex,input);
                        }
                    }

                    //add node's data to the block
                    if (currentBlock.isEmpty()) {
                        currentBlock.setIndex(nodeIndex);

                    }
                    currentBlock.adNodeToBlock(nodeSeq.get(nodeIndex));
                    lastNode = nodeIndex;

                    //close block if it has more than 1 outputs
                    if (hasMultiOut(node)) {
                        //save and close current block
                        innerTrace.getBlockSeq().add(currentBlock);
                        //if there is a next node, create new block
                        if (nodeIdIterator.hasNext()) {
                            currentBlock = new LinearExecutionBlock(input);
                        }
                    }
                    //save the block if there is no element left
                    if (!nodeIdIterator.hasNext()) {
                        innerTrace.getBlockSeq().add(currentBlock);
                    }

                }
                testTrace.getTraces().add(innerTrace);

            }
            hitTrace.getTestTrace().put(testTrace.getTestID(), testTrace);
        }
    }
}
