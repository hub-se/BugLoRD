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
    private ConcurrentHashMap<Integer, LinkedHashSet<Integer>> block2NodeMap;
    private ConcurrentHashMap<Integer, Integer> node2BlockMap;

    public LinearExecutionHitTrace(ISpectra<SourceCodeBlock, ?> spectra) {
        TestTrace = new ArrayList<>(spectra.getTraces().size());
        nodeSeq = new ConcurrentHashMap<>(spectra.getNodes().size(), 1.0f);
        block2NodeMap = new ConcurrentHashMap<>(spectra.getNodes().size());
        node2BlockMap = new ConcurrentHashMap<>(spectra.getNodes().size(), 1.0f);
        this.spectra = spectra;
        //long start = System.currentTimeMillis();
        //System.out.println("beginning init graph...");
        initGraphNode();
        //System.out.println("time for init the LEB graph: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        //start = System.currentTimeMillis();
        //System.out.println("beginning creating LEB traces...");
        generateLinearBlockTrace();
        //System.out.println("creating LEB traces done in: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
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

    public double getEF(int index) {
        return spectra.getNode(index).getEF();
    }

    public double getEP(int index) {
        return spectra.getNode(index).getEP();
    }

    public ArrayList<LinearExecutionTestTrace> getTestTrace() {
        return TestTrace;
    }


    public ConcurrentHashMap<Integer, ExecutionGraphNode> getNodeSeq() {
        return nodeSeq;
    }

    public ConcurrentHashMap<Integer, LinkedHashSet<Integer>> getBlock2NodeMap() {
        return block2NodeMap;
    }

    public int getBlockCount() {
        return block2NodeMap.size();
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

    public ConcurrentHashMap.KeySetView<Integer, LinkedHashSet<Integer>> getAllBlocks() {
        return block2NodeMap.keySet();
    }

    @Override
    public String toString() {
        return "LEBHitTrace{" + "TestTrace=" + TestTrace + '}';
    }

    private void initGraphNode() {
        for (ITrace<SourceCodeBlock> test : spectra.getTraces()) {

            int c = 1;
            for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
                int lastId = -1;
                long start = System.currentTimeMillis();
                //System.out.println("beginning init traces for test no: " + test.getIndex() + "-" + c + ", node count: " + test.involvedNodesCount());
                Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(spectra.getIndexer());
                ExecutionGraphNode current, last;
                while (nodeIdIterator.hasNext()) {
                    int nodeIndex = nodeIdIterator.next();
                    // nodeIndex is seen for the first time
                    getNodeSeq().computeIfAbsent(
                            nodeIndex,
                            k -> new ExecutionGraphNode(nodeIndex, spectra));
                    current = getNodeSeq().get(nodeIndex);
                    if (lastId != -1) {
                        last = getNodeSeq().get(lastId);

                        if (!current.checkInNode(lastId)) {
                            current.addInNode(lastId);
                        }
                        if (!last.checkOutNode(nodeIndex)) {
                            last.addOutNode(nodeIndex);
                        }
                    }
                    // fix for cases where the same node doesn't is always the starting one
                    current.addInNode(lastId);
                    lastId = nodeIndex;

                }
                //System.out.println("init test " + test.getIndex() + "-" + c + " done in " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
                c++;
            }

        }
    }

    private void generateLinearBlockTrace() {

        for (ITrace<SourceCodeBlock> test : spectra.getTraces()) {

            LinearExecutionTestTrace testTrace = new LinearExecutionTestTrace(test.getIndex(), spectra, test.isSuccessful());
            //long start = System.currentTimeMillis();
            //System.out.println("beginning creating traces for test no: " + test.getIndex() + ", node count: " + test.involvedNodesCount());
            generateLEBFromExeTrace(test, testTrace);
            getTestTrace().add(testTrace);
            //System.out.println("test no: " + test.getIndex() + " done in: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        }
    }

    private void generateLEBFromExeTrace(ITrace<SourceCodeBlock> test, LinearExecutionTestTrace testTrace) {
        for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
            LinearBlockSequence innerTrace = new LinearBlockSequence();
            Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(spectra.getIndexer());
            HashSet<Integer> visited = new HashSet<>(test.getInvolvedNodes().size(), 1.0f);

            int currentBlock = -2;
            int lastBlock = -2;
            int lastNode = -2;

            while (nodeIdIterator.hasNext()) {

                int nodeIndex = nodeIdIterator.next();
                ExecutionGraphNode node = nodeSeq.get(nodeIndex);

                //skip repetition
                if (visited.contains(nodeIndex)) {

                    // repetition found
                    // check if node  is head of a block
                    if (block2NodeMap.get(nodeIndex) != null) {
                        // skip if last Block is the same
                        if ((currentBlock == nodeIndex) || (nodeIndex == lastBlock)) continue;
                        // we see a different block-> add to blockseq
                        innerTrace.addBlock(nodeIndex);
                        if (hasMultiOut(node)) {
                            currentBlock = -2;
                        } else {
                            currentBlock = nodeIndex;
                        }
                        lastBlock = nodeIndex;
                        lastNode = nodeIndex;
                    } else {
                        // skipp all nodes in this block
                        if (hasMultiOut(node)) {
                            currentBlock = -2;
                        }
                        lastNode = nodeIndex;
                        continue;
                    }

                } else {
                    visited.add(nodeIndex);
                    // check if we are in a new block
                    if (currentBlock == -2) {
                        // init the new block
                        currentBlock = addCurrentBlock(innerTrace, nodeIndex, node);
                        lastNode = nodeIndex;
                        continue;
                    } else {
                        // we do not have a new block, check if we have to create one
                        if ((node.getBlockID() != currentBlock) && (isLoop(node) || hasMultiIn(node) || !node.checkInNode(lastNode))) {
                            currentBlock = addCurrentBlock(innerTrace, nodeIndex, node);
                            lastBlock = nodeIndex;
                        } else {
                            // just a normal node within a block
                            // check if node is globally new
                            if (node.getBlockID() == -1) {
                                node.setBlockID(currentBlock);
                                block2NodeMap.get(currentBlock).add(nodeIndex);
                                if (hasMultiOut(node)) {
                                    lastBlock = currentBlock;
                                    currentBlock = -2;
                                }
                                lastNode = nodeIndex;
                            } else {
                                //block was seen somewhere else, skip to next one
                                if (hasMultiOut(node)) {
                                    lastBlock = currentBlock;
                                    currentBlock = -2;
                                }
                                lastNode = nodeIndex;
                                continue;
                            }
                        }
                    }
                }

            }
            testTrace.getTraces().add(innerTrace);
        }
    }

    private int addCurrentBlock(LinearBlockSequence innerTrace, int nodeIndex, ExecutionGraphNode node) {
        int currentBlock;
        currentBlock = nodeIndex;
        // block is globally new
        if (node.getBlockID() == -1) {
            node.setBlockID(nodeIndex);
            block2NodeMap.computeIfAbsent(nodeIndex, v -> new LinkedHashSet<>());
            block2NodeMap.get(nodeIndex).add(nodeIndex);
        }
        innerTrace.addBlock(nodeIndex);
        //check if we have to create new block, due multi out edges
        if (hasMultiOut(node)) {
            currentBlock = -2;
        }
        return currentBlock;
    }


}
