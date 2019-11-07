package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings.getStdResourcesDir;

class GenerateLinearBlockTest {
    private boolean hasMultiIn(ExecutionGraphNode node) {
        return (node.getInDegree() > 1);
    }

    private boolean hasMultiOut(ExecutionGraphNode node) {
        return (node.getOutDegree() > 1);
    }

    @org.junit.jupiter.api.Test
    void generateLinearExecutionBlockTest() {
        Path output1 = Paths.get(getStdResourcesDir(), "spectraCompressed.zip");

        ISpectra<SourceCodeBlock, ?> input = SpectraFileUtils.loadBlockCountSpectraFromZipFile(output1);
        ConcurrentHashMap<Integer, ExecutionGraphNode> nodeSeq = new ConcurrentHashMap<>();
        // first step is to initiate In- and Out-degree of the graph-nodes
        initGraphNode(input, nodeSeq);
        // second steps is to generate the linear execution blocks
        // just for now, we dont use the half defined classes
        class Block {
            public int blockId;
            public Queue<Integer> nIds;

            public Block() {
                blockId = -1;
                nIds = new LinkedList<>();
            }

            public Block(int blockId) {
                this.blockId = blockId;
                nIds = new LinkedList<>();
            }

            public int getBlockId() {
                return blockId;
            }

            public void setBlockId(int blockId) {
                this.blockId = blockId;
            }

            public Queue<Integer> getnIds() {
                return nIds;
            }

            public void addNode(int id) {
                nIds.add(id);
            }

            @Override
            public String toString() {
                return "\n\t\tBlockId = " + blockId + "\n\t\t\tNodeIDs " + nIds;
            }

            public int getSize() {
                return nIds.size();
            }

            public boolean isEmpty() {
                return getSize() == 0;
            }
        }
        class LEBInnerTrace {
            public int innerID;

            public Queue<Block> getBlockSeq() {
                return blockSeq;
            }

            public Queue<Block> blockSeq;

            public LEBInnerTrace() {
                blockSeq = new LinkedList<>();
            }

            @Override
            public String toString() {
                return "blockSeqID = " + innerID + "\t" + blockSeq + "\n\n";
            }

            public int getSize() {
                return blockSeq.stream().mapToInt(Block::getSize).sum();
            }

            public List<Integer> allNodes() {
                return blockSeq.stream()
                        .flatMap(block -> block.getnIds().stream())
                        .collect(Collectors.toList());
            }

        }
        class LEBTrace {
            public int testID;
            public Collection<LEBInnerTrace> traces;

            @Override
            public String toString() {
                return "LEBTrace{" + "testID=" + testID + ", traces=" + traces + '}';
            }
        }
        class LEBHitTrace {
            public HashMap<Integer, LEBTrace> TestTrace = new HashMap<>();

            @Override
            public String toString() {
                return "LEBHitTrace{" + "TestTrace=" + TestTrace + '}';
            }
        }
        // create new HitTrace
        LEBHitTrace linearBlockTrace = new LEBHitTrace();
        for (ITrace<SourceCodeBlock> test : input.getTraces()) {
            // create linear execution trace for each test
            LEBTrace testTrace = new LEBTrace();
            testTrace.testID = test.getIndex();
            int i = 0;
            for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
                // for each thread-trace of the test, create a block trace
                LEBInnerTrace innerTrace = new LEBInnerTrace();
                innerTrace.innerID = i;
                Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(input.getIndexer());
                HashSet<Integer> visited = new HashSet<>();
                Block currentBlock = new Block();
                int lastnode = -1;
                while (nodeIdIterator.hasNext()) {

                    int nodeIndex = nodeIdIterator.next();
                    //skip repetition
                    if (visited.contains(nodeIndex)) {
                        // store current block if the index will be skipped and there is no other element left
                        if (!nodeIdIterator.hasNext() && !currentBlock.isEmpty()) innerTrace.blockSeq.add(currentBlock);

                        continue;
                    } else visited.add(nodeIndex);

                    ExecutionGraphNode node = nodeSeq.get(nodeIndex);

                    //Block is not empty, check if it can hold the current node
                    //there are 4 scenarios:
                    // 1) add node to the block as normal
                    // 2) add node and close this block because the node has multiple outputs
                    // 3) do not add this node but instead create a new block and add it
                    //    because this node has multiple inputs or it is a loop
                    // 4) last node is not a direct predecessor, current node must be added to a new block


                    // check if node must be added in a new block
                    // that is only the case if:
                    // - node  has a loop or
                    // - node has more than 1 inputs
                    // - lastNode is not a direct predecessor

                    if (isLoop(node) || hasMultiIn(node)||!node.checkInNode(lastnode)) {
                        // if block is not empty, close block and create a new one
                        if (!currentBlock.isEmpty()){
                            innerTrace.blockSeq.add(currentBlock);
                            currentBlock = new Block(nodeIndex);;
                        }
                    }

                    //add node's data to the block
                    if(currentBlock.isEmpty()) {
                        currentBlock.setBlockId(nodeIndex);

                    }
                    currentBlock.addNode(nodeIndex);
                    lastnode = nodeIndex;

                    //close block if it has more than 1 outputs
                    if (hasMultiOut(node)) {
                        //save and close current block
                        innerTrace.blockSeq.add(currentBlock);
                        //if there is a next node, create new block
                        if (nodeIdIterator.hasNext()) {
                            currentBlock = new Block();
                        }
                    }
                    //save the block if there is no element left
                    if (!nodeIdIterator.hasNext()){
                        innerTrace.blockSeq.add(currentBlock);
                    }

                }
                i++;


                //check if there is any corrupted block
                innerTrace.blockSeq.forEach(
                        e -> {
                            assertTrue(e != null, "Block is null");
                            assertTrue(e.getBlockId() != -1, "there is un-initiated block.");
                        }
                );
                //Log.out(this, innerTrace.blockSeq.toString());

                //check if any block is missing
                HashSet<Integer> temp = new HashSet<>();
                innerTrace.allNodes().forEach(integer -> temp.add(integer));
                visited.forEach(integer -> {
                    if (!temp.contains(integer))
                        System.out.println("not found " + integer + " , ");
                });
                Log.out(this, "Test: " + test.getIdentifier() + "\n\t" + "Trace: \n" + innerTrace.toString());
//                 Log.out(this, "Test: " + test.getIdentifier() + "\n\t" + "Trace: \n" + innerTrace.innerID);
//                Log.out(this,nodeSeq.get(13821).toString() );
//                Log.out(this, nodeSeq.get(13962).toString());


                Log.out(this, "appears too often: " + Arrays.toString(innerTrace.allNodes().stream().filter(e -> Collections.frequency(innerTrace.allNodes(), e) > 1).toArray()));


                //check if  the number of nodes is correct
                assertTrue(
                        test.getInvolvedNodes().size() == innerTrace.allNodes().size(),
                        "involved count: "
                                + test.getInvolvedNodes().size()
                                + " innertrace node count: "
                                + innerTrace.allNodes().size());

                //members of the same block must also have the same properties
                innerTrace.blockSeq.forEach(block -> block.getnIds().
                        forEach(id -> assertTrue(
                                input.getNode(block.blockId).getEF()
                                        == input.getNode(id).getEF(),
                                "Block  " + block.blockId + " : EF = "
                                        + input.getNode(block.blockId).getEF() +
                                        " vs Node: " + id + " : EF = "
                                        + input.getNode(id).getEF())));

                //members of the same block must also have the same properties
                innerTrace.blockSeq.forEach(block -> block.getnIds().
                        forEach(id -> assertTrue(
                                input.getNode(block.blockId).getEP()
                                        == input.getNode(id).getEP(),
                                "Block  " + block.blockId + " : EP = "
                                        + input.getNode(block.blockId).getEF() +
                                        " vs Node: " + id + " : EP = "
                                        + input.getNode(id).getEP())));
                linearBlockTrace.TestTrace.put(testTrace.testID, testTrace);
            }
        }
    }

    private void initGraphNode(ISpectra<SourceCodeBlock, ?> input, ConcurrentHashMap<Integer, ExecutionGraphNode> nodeSeq) {
        for (ITrace<SourceCodeBlock> test : input.getTraces()) {
            int lastId = -1;
            for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
                Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(input.getIndexer());

                while (nodeIdIterator.hasNext()) {
                    int nodeIndex = nodeIdIterator.next();
                    if (nodeIndex == 13962 && lastId == 13824){
                        Log.out(this,"lastID: "+lastId + ", current Node ID: "+nodeIndex);
                    }
                    // nodeIndex is seen for the first time
                    nodeSeq.computeIfAbsent(
                            nodeIndex,
                            k -> new ExecutionGraphNode(
                                    nodeIndex, input.getNode(nodeIndex).getIdentifier(), input));
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

    private boolean isLoop(ExecutionGraphNode node) {
        return node.checkInNode(node.getIndex());
    }
}
