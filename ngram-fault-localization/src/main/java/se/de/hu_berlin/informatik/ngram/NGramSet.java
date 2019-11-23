package se.de.hu_berlin.informatik.ngram;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NGramSet {
    private final int maxLength;
    private double minSupport;
    private LinearExecutionHitTrace hitTrace;
    final private int numOfCores = Runtime.getRuntime().availableProcessors();
    private List<NGram> result;
    private int minEF;
    private double failedTestCount;
    private LinkedHashMap<Integer, Double> confidence;
    private boolean dynaSup;
    private Set<NGram> nGramHashSet;
    private HashMap<Integer, HashSet<Integer>> involvedTest;
    private HashMap<Integer, HashSet<Integer>> failedTest;
    private HashSet<Integer> relevant;

    public NGramSet(LinearExecutionHitTrace hitTrace, int maxLength, double minSupport) {
        this.hitTrace = hitTrace;
        this.maxLength = maxLength;
        this.minSupport = minSupport;
        minEF = computeMinEF(minSupport);
        startNgramSet(hitTrace);
    }


    public NGramSet(LinearExecutionHitTrace hitTrace, int maxLength, boolean dynaSup) {
        this.hitTrace = hitTrace;
        this.maxLength = maxLength;
        this.dynaSup = dynaSup;
        startNgramSet(hitTrace);
    }

    private void startNgramSet(LinearExecutionHitTrace hitTrace) {
        failedTestCount = hitTrace.getFailedTestCount();
        nGramHashSet = ConcurrentHashMap.newKeySet();
        involvedTest = new HashMap<>(hitTrace.getBlockCount(), 1.0f);
        failedTest = new HashMap<>(hitTrace.getBlockCount(), 1.0f);
        relevant = new HashSet<>();
        //first step is the build the relevant block set
        try {
            buildNGramSet();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        result = new ArrayList<>(nGramHashSet);
        Collections.sort(result, Collections.reverseOrder());
        mapResult2Nodes();
    }

    private int computeMinEF(double minSupport) {
        return (int) (minSupport * failedTestCount);
    }

    public double getConfidence(int nodeIndex) {
        if (confidence.get(nodeIndex) != null) return confidence.get(nodeIndex);
        else return 0.0;
    }

    public HashMap<Integer, Double> getConfidence() {
        return confidence;
    }

    private void mapResult2Nodes() {
        confidence = new LinkedHashMap<>(result.size());
        ConcurrentHashMap<Integer, LinkedHashSet<Integer>> blockMap = hitTrace.getBlock2NodeMap();
        HashSet<Integer> visitedNode = new HashSet<>(blockMap.size());

        result.forEach(entry -> {
            int blockCount = entry.length;
            int[] blockIDs = entry.getBlockIDs();
            for (int i = 0; i < blockCount; i++) {
                int tmp = blockIDs[i];
                if (visitedNode.contains(tmp)) continue;
                visitedNode.add(tmp);
                LinkedHashSet<Integer> nodes = blockMap.get(tmp);
                nodes.forEach(n -> {
                    confidence.put(n, entry.getConfidence());
                });
            }
        });
    }

    public List<NGram> getResult() {
        return result;
    }

    public List<String> getResultAsText() {
        ConcurrentHashMap<Integer, LinkedHashSet<Integer>> blockMap = hitTrace.getBlock2NodeMap();
        HashSet<Integer> visitedNode = new HashSet<>(blockMap.size());
        List rText = new LinkedList();
        result.forEach(entry -> {

            int blockCount = entry.length;
            int[] blockIDs = entry.getBlockIDs();

            for (int i = 0; i < blockCount; i++) {
                int tmp = blockIDs[i];

                if (visitedNode.contains(tmp)) continue;

                visitedNode.add(tmp);

                LinkedHashSet<Integer> nodes = blockMap.get(tmp);

                nodes.forEach(n -> {
                    StringBuilder string = new StringBuilder();
                    string.append(hitTrace.getShortIdentifier(n));
                    string.append(" EF: " + entry.getEF() + ", ET: " + entry.getET() + ", Confidence: " + entry.getConfidence());
                    rText.add(string.toString());
                });
            }

        });
        return rText;
    }

    public int getMinEF() {
        return minEF;
    }

    private void buildNGramSet() throws InterruptedException {
        //first step is to find the relevant 1-gram
        long start = System.currentTimeMillis();
        initInvolvementMap();
        System.out.println("init involvement done in: " + (System.currentTimeMillis() - start) / 1000.0 + " s");
        start = System.currentTimeMillis();
        createNGrams();
        System.out.println("create ngramSet done in: " + (System.currentTimeMillis() - start) / 1000.0 + " s");
    }

    private void createNGrams() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores);
        hitTrace.getTestTrace().forEach(testTrace -> {
            testTrace.getTraces().forEach(seq -> {
                executorService.submit(() -> {
                    for (int i = 2; i <= maxLength; i++) {
                        calcNGramStats(seq.getBlockSeq(), i);
                    }
                });
            });
        });
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    private void calcNGramStats(LinkedList<Integer> seq, int nMax) {

        // stop is this sequence is too short
        if (seq.size() < nMax) return;

        Iterator<Integer> blockIt = seq.iterator();
        int[] lastNGram = new int[nMax];

        //array to get EF/Support and ET/involvement values for each n-gram
        double[] stats = new double[2];

        //distance to the node that was executed by all failed tests
        int distToLastFailedNode = nMax + 1;
        double confOfLastRelNode = 0.0;

        //init the first nMax-Gram
        for (int i = 0; i < nMax; i++) {
            int tmp = blockIt.next();
            if (relevant.contains(tmp)) {
                distToLastFailedNode = i;
                confOfLastRelNode = computeConfOfSingleBlock(tmp);
            }
            lastNGram[i] = tmp;
        }

        // beginning to create additional n-gram by transition  of 1 element at a time.
        // we only create new n-gram if it  contains a 1-gram that was executed by all failed test.
        // that means that "distToLastFailedNode" must be smaller than "nMax".
        if (distToLastFailedNode < nMax) {

            stats = getEFAndET(lastNGram, nMax);
            updateMinSup(nMax, distToLastFailedNode, confOfLastRelNode);
            checkThenAdd(nMax, lastNGram, stats, confOfLastRelNode, distToLastFailedNode);
        }

        while (blockIt.hasNext()) {
            int[] nGram = new int[nMax];

            // copy the tail of the last n-gram
            for (int j = 0; j < nMax - 1; j++) {
                nGram[j] = lastNGram[j + 1];
            }

            //get the next element and reset the distance if a new failed node is found.
            int tmp = blockIt.next();

            if (relevant.contains(tmp)) {
                distToLastFailedNode = 0;
                confOfLastRelNode = computeConfOfSingleBlock(tmp);

            } else distToLastFailedNode++;

            nGram[nMax - 1] = tmp;

            // again, we only save this ngram if it contains a relevant block
            if (distToLastFailedNode < nMax) {
                stats = getEFAndET(lastNGram, nMax);
                updateMinSup(nMax, distToLastFailedNode, confOfLastRelNode);
                checkThenAdd(nMax, lastNGram, stats, confOfLastRelNode, distToLastFailedNode);
            }
            lastNGram = nGram;
        }
    }

    private void updateMinSup(int nMax, int distToLastFailedNode, double confOfLastRelNode) {
        if (dynaSup) {
            minEF = computeMinEF(1.0 - Math.pow(confOfLastRelNode, nMax - distToLastFailedNode + 1));
        }
    }

    private void checkThenAdd(int nMax, int[] lastNGram, double[] stats, double confOfLastRelNode, int distance) {
        //if minSup is fulfilled and the new confidence is big enough
        if ((stats[0] > 0) && (stats[0] >= minEF)) {
            if (!dynaSup) {
                nGramHashSet.add(new NGram(nMax, stats[0], stats[1], lastNGram));
            } else if ((stats[0] / stats[1]) >= Math.pow(confOfLastRelNode, distance + 1)) {
                nGramHashSet.add(new NGram(nMax, stats[0], stats[1], lastNGram));
            }
        }

    }

    private double computeConfOfSingleBlock(int tmp) {

        return (double) failedTest.get(tmp).size() / involvedTest.get(tmp).size();
    }


    private void initInvolvementMap() {
        hitTrace.getTestTrace().forEach(t -> {
            boolean isFailed = !t.isSuccessful();
            int testId = t.getTestID();
            t.getInvolvedBlocks().forEach(b -> {
                if (isFailed) {
                    failedTest.computeIfAbsent(b, v -> new HashSet<>());
                    failedTest.get(b).add(testId);
                    double EF, ET;
                    EF = hitTrace.getEF(b);
                    if (EF == failedTestCount) {
                        if (!relevant.contains(b)) {
                            relevant.add(b);
                            ET = hitTrace.getEP(b) + EF;
                            nGramHashSet.add(new NGram(1, EF, ET, new int[]{b}));
                        }
                    }
                }
                involvedTest.computeIfAbsent(b, v -> new HashSet<>());
                involvedTest.get(b).add(testId);
            });
        });
    }


    private double[] getEFAndET(int[] ngram, int maxN) {

        double EF = getSetsIntersectSize(ngram, true, maxN);
        double ET = getSetsIntersectSize(ngram, false, maxN);

        return new double[]{EF, ET};

    }

    private double getSetsIntersectSize(int[] ngram, boolean isEF, int maxN) {
        ArrayList<HashSet<Integer>> allSet = new ArrayList<>(maxN);
        HashSet<Integer> intersect;
        if (isEF) {
            for (int i : ngram
            ) {
                if (failedTest.get(i) == null) return 0.0;
                allSet.add(failedTest.get(i));
            }
            allSet.sort(new SizeComparator());

            intersect = (HashSet) allSet.get(0).clone();

            for (int i = 1; i < maxN; i++) {
                intersect.retainAll(failedTest.get(ngram[i]));
                if (intersect.size() == 0) break;
            }
        } else {
            for (int i : ngram
            ) {
                if (involvedTest.get(i) == null) return 0.0;
                allSet.add(involvedTest.get(i));
            }
            allSet.sort(new SizeComparator());

            intersect = (HashSet) allSet.get(0).clone();

            for (int i = 1; i < maxN; i++) {
                intersect.retainAll(involvedTest.get(ngram[i]));
                if (intersect.size() == 0) break;
            }
        }


        return intersect.size();
    }


    class SizeComparator implements Comparator<HashSet<?>> {

        @Override
        public int compare(HashSet<?> o1, HashSet<?> o2) {
            return Integer.compare(o1.size(), o2.size());
        }
    }
}
