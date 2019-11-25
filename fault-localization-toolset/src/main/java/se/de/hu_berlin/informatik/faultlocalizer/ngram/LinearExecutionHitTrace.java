package se.de.hu_berlin.informatik.faultlocalizer.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LinearExecutionHitTrace {
    final private int numOfCores = 16;
    private ArrayList<LinearExecutionTestTrace> TestTrace;
    private ISpectra<SourceCodeBlock, ?> spectra;
    private ConcurrentHashMap<Integer, ExecutionGraphNode> nodeSeq;
    private ConcurrentHashMap<Integer, LinkedHashSet<Integer>> block2NodeMap;
    private int traceCount;

    public LinearExecutionHitTrace(ISpectra<SourceCodeBlock, ?> spectra) {
        traceCount = spectra.getTraces().size();
        TestTrace = new ArrayList<>(traceCount);
        nodeSeq = new ConcurrentHashMap<>(spectra.getNodes().size());

        this.spectra = spectra;
        try {
            initGraphNode();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        block2NodeMap = new ConcurrentHashMap<>(nodeSeq.size());
        try {
            generateLinearBlockTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private boolean hasMultiIn(ExecutionGraphNode node) {
        return (node.getInDegree() > 1);
    }

    private boolean hasMultiOut(ExecutionGraphNode node) {
        return (node.getOutDegree() > 1);
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

    private void initGraphNode() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores);
        //CountDownLatch latch = new CountDownLatch(traceCount);
        for (ITrace<SourceCodeBlock> test : spectra.getTraces()) {
            executorService.submit(() -> {
                for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
                    Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(spectra.getIndexer());
                    ExecutionGraphNode current, last;
                    int lastId = -1;
                    while (nodeIdIterator.hasNext()) {
                        int nodeIndex = nodeIdIterator.next();
                        // nodeIndex is seen for the first time
                        // check for repetition
                        getNodeSeq().computeIfAbsent(
                                nodeIndex,
                                k -> new ExecutionGraphNode(nodeIndex, spectra));
                        current = getNodeSeq().get(nodeIndex);
                        synchronized (current) {
                            if (lastId != -1) {
                                last = getNodeSeq().get(lastId);

                                last.addOutNode(nodeIndex);
                            }
                            // fix for cases where the same node doesn't is always the starting one
                            current.addInNode(lastId);
                        }
                        lastId = nodeIndex;
                    }
                }
                //latch.countDown();
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        //latch.await();
    }

    private void generateLinearBlockTrace() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores);
        //CountDownLatch latch = new CountDownLatch(traceCount);
        for (ITrace<SourceCodeBlock> test : spectra.getTraces()) {
            executorService.submit(() -> {
                LinearExecutionTestTrace testTrace = new LinearExecutionTestTrace(test.getIndex(), test.getIdentifier(), spectra, test.isSuccessful());
                generateLEBFromExeTrace(test, testTrace);
                getTestTrace().add(testTrace);
                //latch.countDown();
            });

        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        //latch.await();
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
                synchronized (node) {
                    // check if we have a globally new node
                    if (node.getBlockID() == -1) {
                        //check if we have to create a new block
                        if ((currentBlock == -2) || hasMultiIn(node)) {
                            //add to block2NodeMap if not exists
                            block2NodeMap.computeIfAbsent(nodeIndex, v -> new LinkedHashSet<>());
                            synchronized (block2NodeMap) {
                                block2NodeMap.get(nodeIndex).add(nodeIndex);
                            }
                            // init block and add to block sequence
                            node.setBlockID(nodeIndex);
                            innerTrace.addBlock(nodeIndex);
                            currentBlock = nodeIndex;
                            lastBlock = nodeIndex;

                        } else {
                            //add this node to the current block
                            synchronized (block2NodeMap) {
                                block2NodeMap.get(currentBlock).add(nodeIndex);
                            }
                            node.setBlockID(currentBlock);
                        }
                        //check if we have to close this block
                        if (hasMultiOut(node)) {
                            currentBlock = -2;
                        }
                        continue;
                    }


                }

                //we have seen this node some where

                //check if we are at the beginning of a block
                if ((currentBlock == -2)) {
                    //check for repetition
                    if (lastBlock != nodeIndex) innerTrace.addBlock(nodeIndex);
                    currentBlock = nodeIndex;
                    lastBlock = nodeIndex;
                }
                // otherwise, just a inner node, keep on  skipping

                //check if we have to close this block
                if (hasMultiOut(node)) {
                    currentBlock = -2;
                }

            }
            testTrace.getTraces().add(innerTrace);
        }
    }


}
