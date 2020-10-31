package se.de.hu_berlin.informatik.faultlocalizer.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import java.util.ArrayList; //PT
import java.util.List; //PT

public class LinearExecutionHitTrace {
    final private int numOfCores = Runtime.getRuntime().availableProcessors();
    private ArrayList<LinearExecutionTestTrace> TestTrace;
    private ISpectra<SourceCodeBlock, ?> spectra;
    private ConcurrentHashMap<Integer, ExecutionGraphNode> nodeSeq;
    private ConcurrentHashMap<Integer, LinkedHashSet<Integer>> block2NodeMap;
    private AtomicInteger traceCount;

    public LinearExecutionHitTrace(ISpectra<SourceCodeBlock, ?> spectra) {

        nodeSeq = new ConcurrentHashMap<>(spectra.getNodes().size());
        traceCount = new AtomicInteger(0);
        this.spectra = spectra;
        TestTrace = new ArrayList<>(spectra.getTraces().size());
        long start = System.currentTimeMillis();

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

    public List<LinearExecutionTestTrace> getSuccessfulTest() {
        return getTestTrace().stream().filter(t -> t.isSuccessful() == true).collect(Collectors.toList());
    }

    public int getFailedTestCount() {
        return spectra.getFailingTraces().size();
    }
    
  /*  //by Philipp Thamm to calculate the Cross-Entropy
   * public int getSuccessfulTestCount() {
   *     return spectra.getSuccessfulTraces().size();
   * }
   */
    public ConcurrentHashMap.KeySetView<Integer, LinkedHashSet<Integer>> getAllBlocks() {
        return block2NodeMap.keySet();
    }

    @Override
    public String toString() {
        return "LEBHitTrace{" + "TestTrace=" + TestTrace + '}';
    }

    private void initGraphNode() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores);


        for (ITrace<SourceCodeBlock> test : spectra.getTraces()) {
            executorService.submit(() -> {
                for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
                    traceCount.getAndIncrement();
                    Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(spectra.getIndexer());
                    ExecutionGraphNode current, last = null;
                    int lastId = -1;
                    while (nodeIdIterator.hasNext()) {
                        int nodeIndex = nodeIdIterator.next();
                        // nodeIndex is seen for the first time
                        // check for repetition
                        current = getNodeSeq().computeIfAbsent(
                                nodeIndex,
                                k -> new ExecutionGraphNode(nodeIndex, spectra));
                        
                        if (last != null) {
                            last.addOutNode(nodeIndex);
                        }
                        // fix for cases where the same node doesn't is always the starting one
                        current.addInNode(lastId);
                        lastId = nodeIndex;
                        last = current;
                    }
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.HOURS);

    }

    private void generateLinearBlockTrace() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores);
        for (ITrace<SourceCodeBlock> test : spectra.getTraces()) {
            executorService.submit(() -> {
                LinearExecutionTestTrace testTrace = new LinearExecutionTestTrace(test.getIndex(), test.getIdentifier(), spectra, test.isSuccessful());
                generateLEBFromExeTrace(test, testTrace);
                getTestTrace().add(testTrace);
            });

        }
        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.HOURS);

    }

    private void generateLEBFromExeTrace(ITrace<SourceCodeBlock> test, LinearExecutionTestTrace testTrace) {
        for (ExecutionTrace executionTrace : test.getExecutionTraces()) {

            LinearBlockSequence innerTrace = new LinearBlockSequence();
            Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(spectra.getIndexer());
            HashMap<Integer, HashSet<Integer>> predessor = new HashMap<>();
            int currentBlock = -2;
            int lastBlock = -2;

            while (nodeIdIterator.hasNext()) {

                int nodeIndex = nodeIdIterator.next();
                ExecutionGraphNode node = nodeSeq.get(nodeIndex);

                synchronized (node) {
                    // check if we have a globally new node
                    if (node.getBlockID() == -1) {
                        //check if we have to create a new block
                        if ((currentBlock == -2) || hasMultiIn(node) || isLoop(node)) {
                            //add to block2NodeMap if not exists
                            block2NodeMap.computeIfAbsent(nodeIndex, v -> new LinkedHashSet<>());
                            synchronized (block2NodeMap) {
                                block2NodeMap.get(nodeIndex).add(nodeIndex);
                            }
                            // init block and add to block sequence
                            predessor.computeIfAbsent(nodeIndex, v -> new HashSet<>());
                            predessor.get(nodeIndex).add(lastBlock);
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

                //we have seen this node somewhere

                //check if we are at the beginning of a block
                if ((currentBlock == -2) || (node.getBlockID() == nodeIndex)) {
                    //check for repetition
                    predessor.computeIfAbsent(nodeIndex, v -> new HashSet<>());
                    if ((lastBlock != nodeIndex) && (!predessor.get(nodeIndex).contains(lastBlock)))
                        innerTrace.addBlock(nodeIndex);
                    predessor.get(nodeIndex).add(lastBlock);
                    currentBlock = nodeIndex;
                    lastBlock = nodeIndex;
                }
                // otherwise, just an inner node, keep on skipping

                //check if we have to close this block
                if (hasMultiOut(node)) {
                    currentBlock = -2;
                }

            }
            testTrace.getTraces().add(innerTrace);
        }
    }
    
    public String successfulToString() {
    	TestTrace.forEach(t -> {
    		if (t.isSuccessful()) {
        		return "LEBHitTrace{" + "SuccessfulTestTrace=" + t + '}';
        	}
        });
    }
    
    public String failedToString() {
    	TestTrace.forEach(t -> {
    		if (!(t.isSuccessful())) {
        		return "LEBHitTrace{" + "SuccessfulTestTrace=" + t + '}';
        	}
        });
    }


}
