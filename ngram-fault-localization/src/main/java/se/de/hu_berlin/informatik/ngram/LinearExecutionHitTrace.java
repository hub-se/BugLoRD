package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LinearExecutionHitTrace {
    private ArrayList<LinearExecutionTestTrace> TestTrace;
    private ISpectra<SourceCodeBlock, ?> spectra;
    private ConcurrentHashMap<Integer, ExecutionGraphNode> nodeSeq;
    private ConcurrentHashMap<Integer, LinkedHashSet<Integer>> blockMap;

    public LinearExecutionHitTrace(ISpectra<SourceCodeBlock, ?> spectra) {
        TestTrace = new ArrayList<>();
        nodeSeq = new ConcurrentHashMap<>();
        blockMap = new ConcurrentHashMap<>();
        this.spectra = spectra;
        initGraphNode();
        generateLinearBlockTrace();
    }


    private boolean hasMultiIn(ExecutionGraphNode node) {
        return (node.getInDegree() > 1);
    }

    private boolean hasMultiOut(ExecutionGraphNode node) {
        return (node.getOutDegree() > 1);
    }

    private boolean isLoop(ExecutionGraphNode node) {
        try {
            return node.checkInNode(node.getIndex());
        } catch (NullPointerException e) {
            System.out.println("IsLoop:  Input-Node is NULL!");
            return false;
        }
    }

    public ISpectra getSpectra() {
        return spectra;
    }

    public String getShortIdentifier(int index) {
        return spectra.getNode(index).getIdentifier().getShortIdentifier();
    }

    public String getIdentifier(int index) {
        return spectra.getNode(index).getIdentifier().toString();
    }

    public ArrayList<LinearExecutionTestTrace> getTestTrace() {
        return TestTrace;
    }


    public ConcurrentHashMap<Integer, ExecutionGraphNode> getNodeSeq() {
        return nodeSeq;
    }

    public ConcurrentHashMap<Integer, LinkedHashSet<Integer>> getBlockMap() {
        return blockMap;
    }

    public int getBlockCount() {
        HashSet<Integer> set = new HashSet<>();
        TestTrace.forEach(t -> {
            t.getTraces().forEach(e -> {
                e.getBlockSeq().forEach(b -> set.add(b.getIndex()));
            });
        });
        return set.size();
    }

    public LinearExecutionTestTrace getTrace(int index) {
        Iterator it = TestTrace.iterator();
        return TestTrace.get(index);
    }

    public List<LinearExecutionTestTrace> getFailedTest() {
        return getTestTrace().stream().filter(t -> t.isSuccessful() == false).collect(Collectors.toList());
    }

    public int getFailedTestCount() {
        return spectra.getFailingTraces().size();
    }

    public HashSet<Integer> getAllBlocks() {
        HashSet<Integer> allBlocks = new HashSet<>();
        getTestTrace().forEach(t -> {
            allBlocks.addAll(t.getInvolvedBlocks());
        });
        return allBlocks;
    }

    @Override
    public String toString() {
        return "LEBHitTrace{" + "TestTrace=" + TestTrace + '}';
    }

    private void initGraphNode() {
        for (ITrace<SourceCodeBlock> test : spectra.getTraces()) {

            int lastId = -1;
            for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
                Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(spectra.getIndexer());

                while (nodeIdIterator.hasNext()) {
                    int nodeIndex = nodeIdIterator.next();
                    // nodeIndex is seen for the first time
                    getNodeSeq().computeIfAbsent(
                            nodeIndex,
                            k -> new ExecutionGraphNode(
                                    nodeIndex, spectra));
                    //skip repetition
                    if (getNodeSeq().get(nodeIndex).checkInNode(nodeIndex)) continue;


                    if (lastId == -1) {
                        lastId = nodeIndex;

                    } else {
                        getNodeSeq().get(nodeIndex).addInNode(lastId);
                        getNodeSeq().get(lastId).addOutNode(nodeIndex);
                        lastId = nodeIndex;
                    }

                }
            }
        }
    }

    private void generateLinearBlockTrace() {

        for (ITrace<SourceCodeBlock> test : spectra.getTraces()) {
            LinearExecutionTestTrace testTrace = new LinearExecutionTestTrace(test.getIndex(), spectra, test.isSuccessful());

            generateLEBFromExeTrace(test, testTrace);
            getTestTrace().add(testTrace);
        }
    }

    private void generateLEBFromExeTrace(ITrace<SourceCodeBlock> test, LinearExecutionTestTrace testTrace) {
        for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
            LinearBlockSequence innerTrace = new LinearBlockSequence(spectra);
            Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(spectra.getIndexer());
            HashSet<Integer> visited = new HashSet<>();
            LinearExecutionBlock currentBlock = new LinearExecutionBlock(spectra);
            int lastNode = -1;
            while (nodeIdIterator.hasNext()) {

                int nodeIndex = nodeIdIterator.next();
                //skip repetition
                if (visited.contains(nodeIndex)) {
                    // store current block if the index will be skipped and there is no other element left
                    if (!nodeIdIterator.hasNext() && !currentBlock.isEmpty()) {
                        innerTrace.getBlockSeq().add(currentBlock);
                    }
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
                        currentBlock = new LinearExecutionBlock(nodeIndex, spectra);
                        //add new block to the map
                        blockMap.computeIfAbsent(nodeIndex, v -> new LinkedHashSet<>());

                    }
                }

                //add node's data to the block
                if (currentBlock.isEmpty()) {
                    currentBlock.setIndex(nodeIndex);
                    //add new block to the map
                    blockMap.computeIfAbsent(nodeIndex, v -> new LinkedHashSet<>());

                }
                currentBlock.adNodeToBlock(nodeSeq.get(nodeIndex));
                lastNode = nodeIndex;
                //add Node to the BlockMap
                blockMap.get(currentBlock.getIndex()).add(nodeIndex);

                //close block if it has more than 1 outputs
                if (hasMultiOut(node)) {
                    //save and close current block
                    innerTrace.getBlockSeq().add(currentBlock);
                    //if there is a next node, create new block
                    if (nodeIdIterator.hasNext()) {
                        currentBlock = new LinearExecutionBlock(spectra);
                    }
                }
                //save the block if there is no element left
                if (!nodeIdIterator.hasNext()) {
                    innerTrace.getBlockSeq().add(currentBlock);
                }

            }
            testTrace.getTraces().add(innerTrace);
            //                //Log.out(this, innerTrace.blockSeq.toString());

            //check if any block is missing
//                HashSet<Integer> temp = new HashSet<>();
//                innerTrace.allNodes().forEach(node -> temp.add(node.getIndex()));
//                visited.forEach(id-> {
//                    if (!temp.contains(id))
//                        System.out.println("not found " + id + " , ");
//                });


//
            // print all looped nodes
//                innerTrace.getBlockSeq().forEach(block -> block.getNodeSequence().
//                        forEach(id -> {
//                            if (isLoop(nodeSeq.get(id.getIndex())))
//                                System.out.println(
//                                    nodeSeq.get(id.getIndex()).toString()
//                                    +"\n"+
//                                    block.toString());
//                        }));
//                //check if  the number of nodes is correct
//                assertTrue(
//                        test.getInvolvedNodes().size() == innerTrace.allNodes().size(),
//                        "involved count: "
//                                + test.getInvolvedNodes().size()
//                                + " innertrace node count: "
//                                + innerTrace.allNodes().size());
//
//                //members of the same block should also have the same properties
//                innerTrace.getBlockSeq().forEach(block -> block.getNodeSequence().
//                        forEach(id -> assertTrue(
//                                input.getNode(block.getIndex()).getIdentifier().getNodeType()
//                                        == input.getNode(id.getIndex()).getIdentifier().getNodeType(),
//                                "Block  " + block.getIndex() + " : NodeType = "
//                                        + input.getNode(block.getIndex()).getIdentifier().getNodeType() +
//                                        " vs Node: " + id + " : NodeType = "
//                                        + input.getNode(id.getIndex()).getIdentifier().getNodeType() +
//                                        "\n" + block.toString()
//                        )));
//
//                //members of the same block must also have the same properties
//                innerTrace.blockSeq.forEach(block -> block.getnIds().
//                        forEach(id -> assertTrue(
//                                input.getNode(block.blockId).getEP()
//                                        == input.getNode(id).getEP(),
//                                "Block  " + block.blockId + " : EP = "
//                                        + input.getNode(block.blockId).getEF() +
//                                        " vs Node: " + id + " : EP = "
//                                        + input.getNode(id).getEP())));
//


        }
    }


}
