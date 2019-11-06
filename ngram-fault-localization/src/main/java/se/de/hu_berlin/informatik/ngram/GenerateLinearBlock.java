package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GenerateLinearBlock extends AbstractProcessor<ISpectra<SourceCodeBlock,?>, ISpectra<SourceCodeBlock,?>> {
    public GenerateLinearBlock() {
        super();
    }
    @Override
    public ISpectra<SourceCodeBlock,?> processItem(final ISpectra<SourceCodeBlock,?> input){
        ConcurrentHashMap<Integer, ExecutionGraphNode> nodeSeq = new ConcurrentHashMap<>();
        //first step is to initiate In- and Out-degree of the graph-nodes
        for (ITrace<SourceCodeBlock> test : input.getTraces()){
            for (ExecutionTrace executionTrace : test.getExecutionTraces()){
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
        class Block{
            public List<Integer> nIds = new LinkedList<>();
            public int blockId = -1;
        }
        class LEBInnerTrace{
            public List<Block> blockSeq;
            public int innerID;

            public LEBInnerTrace() {
                blockSeq = new LinkedList<>();
            }
        }
        class LEBTrace{
            public int testID;
            public Collection<LEBInnerTrace> traces;

        }
        class LEBHitTrace{
            public HashMap<Integer, LEBTrace> TestTrace = new HashMap<>();
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
                Block currentBlock = new Block();

                boolean initBlock = false;
                while (nodeIdIterator.hasNext()) {
                    int nodeIndex = nodeIdIterator.next();
                    if (visited.contains(nodeIndex)) {
                        continue;
                    } else visited.add(nodeIndex);
                    ExecutionGraphNode node = nodeSeq.get(nodeIndex);
                    int inDegree = node.getInDegree();
                    int outDegree = node.getOutDegree();
                    boolean addToNewBlock = false;
                    boolean closeThisBlock = false;
                    boolean isLooped = node.checkInNode(nodeIndex);


                    if (isLooped) {
                        addToNewBlock = true;
                    }
                    if (outDegree > 1) closeThisBlock = true;
                    if (inDegree > 1) addToNewBlock = true;


                    if (addToNewBlock) {
                        innerTrace.blockSeq.add(currentBlock);
                        Block leb = new Block();
                        leb.blockId = nodeIndex;
                        leb.nIds.add(nodeIndex);
                        currentBlock = leb;
                    } else {
                        currentBlock.nIds.add(nodeIndex);
                        //init block if necessary
                        currentBlock.blockId = currentBlock.blockId == -1 ? nodeIndex : -1;

                        if (closeThisBlock) {
                            innerTrace.blockSeq.add(currentBlock);
                            Block leb = new Block();
                            currentBlock = leb;
                        }
                    }



                }
                i++;
                linearBlockTrace.TestTrace.put(testTrace.testID, testTrace);
            }
        }
        return input;
    }
}
