package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LinearExecutionHitTrace {
    private ArrayList<LinearExecutionTestTrace> TestTrace;
    private ISpectra<SourceCodeBlock, ?> spectra;
    private ConcurrentHashMap<Integer, ExecutionGraphNode> nodeSeq;
    private ConcurrentHashMap<Integer, LinkedHashSet<Integer>> block2NodeMap;

    public LinearExecutionHitTrace(ISpectra<SourceCodeBlock, ?> spectra) {
        TestTrace = new ArrayList<>(spectra.getTraces().size());
        nodeSeq = new ConcurrentHashMap<>(spectra.getNodes().size());
        block2NodeMap = new ConcurrentHashMap<>(spectra.getNodes().size());
        this.spectra = spectra;
        long start = System.currentTimeMillis();
        //System.out.println("beginning init graph...");
        initGraphNode();
        System.out.println("time for init the LEB graph: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        start = System.currentTimeMillis();
        //System.out.println("beginning creating LEB traces...");
        generateLinearBlockTrace();
        System.out.println("creating LEB traces done in: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
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
        long maxTime = 0;
        int maxTestIndex = 0;
        String id = new String();
        for (ITrace<SourceCodeBlock> test : spectra.getTraces()) {
            for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
                int lastId = -1;
                long start = System.currentTimeMillis();
                //System.out.println("beginning init traces for test no: " + test.getIndex() + "-" + c + ", node count: " + test.involvedNodesCount());
                Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(spectra.getIndexer());
                ExecutionGraphNode current, last;
                while (nodeIdIterator.hasNext()) {
                    int nodeIndex = nodeIdIterator.next();
                    // nodeIndex is seen for the first time
                    // check for repetition
                    getNodeSeq().computeIfAbsent(
                            nodeIndex,
                            k -> new ExecutionGraphNode(nodeIndex, spectra));
                    current = getNodeSeq().get(nodeIndex);
                    if (lastId != -1) {
                        last = getNodeSeq().get(lastId);
                        last.addOutNode(nodeIndex);
                    }
                    // fix for cases where the same node doesn't is always the starting one
                    current.addInNode(lastId);
                    lastId = nodeIndex;

                }
                long tmp = System.currentTimeMillis() - start;
                if (tmp > maxTime) {
                    maxTime = tmp;
                    maxTestIndex = test.getIndex();
                    id = test.getIdentifier();
                }
            }
        }
        System.out.println("slowest test: " + maxTestIndex + ", done in " + (maxTime / 1000.0) + "s, for nodes count: " + spectra.getTrace(id).involvedNodesCount());
    }

    private void generateLinearBlockTrace() {
        int c = 1;
        long maxTime = 0;
        int maxTestIndex = 0;
        for (ITrace<SourceCodeBlock> test : spectra.getTraces()) {

            LinearExecutionTestTrace testTrace = new LinearExecutionTestTrace(test.getIndex(), test.getIdentifier(), spectra, test.isSuccessful());
            long start = System.currentTimeMillis();
            //System.out.println("beginning creating traces for test no: " + test.getIndex() + ", node count: " + test.involvedNodesCount());
            generateLEBFromExeTrace(test, testTrace);
            getTestTrace().add(testTrace);
            long tmp = (System.currentTimeMillis() - start);
            if (tmp > maxTime) {
                maxTime = tmp;
                maxTestIndex = test.getIndex();
            }

            //System.out.println("test no: " + test.getIndex() + " done in: " + ( / 1000.0) + "s");
        }
        System.out.println("slowest test : " + maxTestIndex + ", blocks generation done in " + (maxTime / 1000.0) + "s, for block count: " + getTrace(maxTestIndex).getInvolvedBlocks().size());
    }

    private void generateLEBFromExeTrace(ITrace<SourceCodeBlock> test, LinearExecutionTestTrace testTrace) {
        for (ExecutionTrace executionTrace : test.getExecutionTraces()) {

            LinearBlockSequence innerTrace = new LinearBlockSequence();
            Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(spectra.getIndexer());

            int currentBlock = -2;
            int lastBlock = -2;

            while (nodeIdIterator.hasNext()) {

                int nodeIndex = nodeIdIterator.next();
                ExecutionGraphNode node = nodeSeq.get(nodeIndex);

                // check if we have a globally new node
                if (node.getBlockID() == -1) {

                    //check if we have to create a new block
                    if ((currentBlock == -2) || hasMultiIn(node) || isLoop(node)) {
                        //add to block2NodeMap if not exists
                        block2NodeMap.computeIfAbsent(nodeIndex, v -> new LinkedHashSet<>());
                        block2NodeMap.get(nodeIndex).add(nodeIndex);

                        // init block and add to block sequence
                        node.setBlockID(nodeIndex);
                        innerTrace.addBlock(nodeIndex);
                        currentBlock = nodeIndex;
                        lastBlock = nodeIndex;

                    } else {
                        //add this node to the current block
                        block2NodeMap.get(currentBlock).add(nodeIndex);
                        node.setBlockID(currentBlock);
                    }

                } else {
                    //we have seen this node some where

                    //check if we are at the beginning of a block
                    if (node.getBlockID() == nodeIndex || (currentBlock == -2)) {
                        //check for repetition
                        if (lastBlock != nodeIndex) innerTrace.addBlock(nodeIndex);
                        currentBlock = nodeIndex;
                        lastBlock = nodeIndex;
                    }
                    // otherwise, just a inner node, keep on  skipping
                }
                //check if we have to close this block
                if (hasMultiOut(node)) {
                    currentBlock = -2;
                }

            }
            testTrace.getTraces().add(innerTrace);
        }
    }


}
