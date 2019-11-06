package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings.getStdResourcesDir;

class GenerateLinearBlockTest {
    @org.junit.jupiter.api.Test
    void generateLinearExecutionBlockTest() {
        Path output1 = Paths.get(getStdResourcesDir(), "spectraCompressed.zip");

        ISpectra<SourceCodeBlock, ?> input = SpectraFileUtils.loadBlockCountSpectraFromZipFile(output1);
        ConcurrentHashMap<Integer, ExecutionGraphNode> nodeSeq = new ConcurrentHashMap<>();
        //first step is to initiate In- and Out-degree of the graph-nodes
        for (ITrace<SourceCodeBlock> test : input.getTraces()) {
            for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
                Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(input.getIndexer());
                int lastId = 0;
                while (nodeIdIterator.hasNext()) {
                    int nodeIndex = nodeIdIterator.next();
                    //nodeIndex is the first node
                    nodeSeq.computeIfAbsent(nodeIndex,
                            k -> new ExecutionGraphNode(
                                    nodeIndex, input.getNode(nodeIndex).getIdentifier(), input));
                    //if not first node
                    if (lastId != 0) {
                        nodeSeq.get(nodeIndex).addInNode(lastId);
                        nodeSeq.get(lastId).addOutNode(nodeIndex);
                    }
                    lastId = nodeIndex;
                }
            }
        }
        //second steps is to generate the linear execution blocks
        //just for now, we dont use the half defined classes
        class Block {
            public int blockId = -1;
            public Queue<Integer> nIds;

            public Block() {
                nIds = new LinkedList<>();
            }

            public Queue<Integer> getnIds() {
                return nIds;
            }

            @Override
            public String toString() {
                return "\n\t\tBlockId = " + blockId +
                        "\n\t\t\tNodeIDs " + nIds;
            }

            public int getSize() {
                return nIds.size();
            }
        }
        class LEBInnerTrace {
            public int innerID;
            public Queue<Block> blockSeq;

            public LEBInnerTrace() {
                blockSeq = new LinkedList<>();
            }

            @Override
            public String toString() {
                return "blockSeqID = " + innerID +
                        "\t" + blockSeq +
                        "\n\n";
            }

            public int getSize() {
                return  blockSeq.stream().mapToInt(Block::getSize).sum();
            }
            public  List<Integer> allNodes(){
                return blockSeq.stream().flatMap(block -> block.getnIds().stream()).collect(Collectors.toList());
            }
        }
        class LEBTrace {
            public int testID;
            public Collection<LEBInnerTrace> traces;

            @Override
            public String toString() {
                return "LEBTrace{" +
                        "testID=" + testID +
                        ", traces=" + traces +
                        '}';
            }
        }
        class LEBHitTrace {
            public HashMap<Integer, LEBTrace> TestTrace = new HashMap<>();

            @Override
            public String toString() {
                return "LEBHitTrace{" +
                        "TestTrace=" + TestTrace +
                        '}';
            }
        }
        // create new HitTrace
        LEBHitTrace linearBlockTrace = new LEBHitTrace();
        for (ITrace<SourceCodeBlock> test : input.getTraces()) {
            //create linear execution trace for each test
            LEBTrace testTrace = new LEBTrace();
            testTrace.testID = test.getIndex();
            int i = 0;
            for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
                // for each thread-trace of the test, create a block trace
                LEBInnerTrace innerTrace = new LEBInnerTrace();
                innerTrace.innerID = i;
                Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(input.getIndexer());
                HashSet<Integer> visited = new HashSet<>();
                Block currentBlock = null;

                boolean lastBlockNotAdded = false;
                int lastNode = -1;
                while (nodeIdIterator.hasNext()) {


                    int nodeIndex = nodeIdIterator.next();

                    if (visited.contains(nodeIndex)) {
                        continue;
                    } else visited.add(nodeIndex);
                    ExecutionGraphNode node = nodeSeq.get(nodeIndex);
                    lastBlockNotAdded = false;
                    //init first block
                    if (lastNode == -1 || currentBlock == null) {
                        currentBlock = new Block();
                        currentBlock.blockId = nodeIndex;
                        currentBlock.nIds.add(nodeIndex);
                        lastNode = nodeIndex;
                        //node contains loop, block must end here
                        if (node.checkInNode(nodeIndex)) {
                            innerTrace.blockSeq.add(currentBlock);
                            currentBlock = null;
                            continue;
                        }

                        continue;
                    } else {
                        if (currentBlock != null) {
                            //current node has more than 1 parent -> must create new block
                            if (node.getInDegree() > 1) {
                                innerTrace.blockSeq.add(currentBlock);
                                currentBlock = new Block();
                                currentBlock.blockId = nodeIndex;
                                currentBlock.nIds.add(nodeIndex);
                                lastNode = nodeIndex;
                                continue;
                            }
                            //current node has more than 1 child -> block will end here
                            if (node.getOutDegree() > 1) {
                                currentBlock.nIds.add(nodeIndex);
                                innerTrace.blockSeq.add(currentBlock);
                                currentBlock = null;
                                lastNode = nodeIndex;
                                continue;
                            }
                            currentBlock.getnIds().add(nodeIndex);
                            lastBlockNotAdded = true;
                            lastNode = nodeIndex;
                        } else System.err.println("CurrentBlock is NULL.");
                    }

                }
                i++;
                //trace ended before creating new block therefore the last block bas to be added manually.
                if (lastBlockNotAdded) {
                    innerTrace.blockSeq.add(currentBlock);
                }

//                HashSet<Integer> temp = new HashSet<>();
//                innerTrace.allNodes().forEach(integer -> temp.add(integer));
//                visited.forEach(integer -> {if (!temp.contains(integer)) System.out.print("not found "+integer + " , ");});
//                Log.out(this, "Test: " + test.getIdentifier() + "\n\t" + "Trace: \n" + innerTrace.toString());
                //Log.out(this, "Test: " + test.getIdentifier() + "\n\t" + "Trace: \n" + innerTrace.innerID);
                assertTrue(test.getInvolvedNodes().size() == innerTrace.allNodes().size(),"involved count: " +test.getInvolvedNodes().size() + " innertrace node count: "+ innerTrace.allNodes().size());

                linearBlockTrace.TestTrace.put(testTrace.testID, testTrace);


            }
        }
    }
}