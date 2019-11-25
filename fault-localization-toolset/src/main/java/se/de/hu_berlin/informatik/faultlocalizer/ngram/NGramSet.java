package se.de.hu_berlin.informatik.faultlocalizer.ngram;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NGramSet {
    private final int maxLength;
    //final private int numOfCores = Runtime.getRuntime().availableProcessors();
    final private int numOfCores = 16;
    private double minSupport;
    private LinearExecutionHitTrace hitTrace;
    private List<NGram> result;
    private int minEF;
    private double failedTestCount;
    private LinkedHashMap<Integer, Double> confidence;
    private boolean dynaSup;
    private ConcurrentHashMap<ArrayList<Integer>, NGram> nGramHashSet;
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
        nGramHashSet = new ConcurrentHashMap<>(hitTrace.getBlockCount());
        involvedTest = new HashMap<>(hitTrace.getBlockCount(), 1.0f);
        failedTest = new HashMap<>(hitTrace.getBlockCount(), 1.0f);
        relevant = new HashSet<>();
        //first step is the build the relevant block set
        try {
            buildNGramSet();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        result = new ArrayList<>(nGramHashSet.values());
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
            ArrayList<Integer> blockIDs = entry.getBlockIDs();
            for (int i = 0; i < blockCount; i++) {
                int tmp = blockIDs.get(i);
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
            ArrayList<Integer> blockIDs = entry.getBlockIDs();

            for (int i = 0; i < blockCount; i++) {
                int tmp = blockIDs.get(i);

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
        initInvolvementMap();
        createNGrams();
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
        ArrayList<Integer> lastNGram = new ArrayList<>(nMax);

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
            lastNGram.add(i, tmp);
        }

        // beginning to create additional n-gram by transition  of 1 element at a time.
        // we only create new n-gram if it  contains a 1-gram that was executed by all failed test.
        // that means that "distToLastFailedNode" must be smaller than "nMax".
        if (distToLastFailedNode < nMax) {

            updateMinSup(nMax, distToLastFailedNode, confOfLastRelNode);
            checkThenAdd(nMax, lastNGram, confOfLastRelNode, distToLastFailedNode);
        }

        while (blockIt.hasNext()) {
            ArrayList<Integer> nGram = new ArrayList<>(nMax);

            // copy the tail of the last n-gram
            for (int j = 0; j < nMax - 1; j++) {
                nGram.add(j, lastNGram.get(j + 1));
            }

            //get the next element and reset the distance if a new failed node is found.
            int tmp = blockIt.next();

            if (relevant.contains(tmp)) {
                distToLastFailedNode = 0;
                confOfLastRelNode = computeConfOfSingleBlock(tmp);

            } else distToLastFailedNode++;

            nGram.add(nMax - 1, tmp);

            // again, we only save this ngram if it contains a relevant block
            if (distToLastFailedNode < nMax) {
                updateMinSup(nMax, distToLastFailedNode, confOfLastRelNode);
                checkThenAdd(nMax, lastNGram, confOfLastRelNode, distToLastFailedNode);
            }
            lastNGram = nGram;
        }
    }

    private void updateMinSup(int nMax, int distToLastFailedNode, double confOfLastRelNode) {
        if (dynaSup) {
            minEF = computeMinEF(1.0 - (confOfLastRelNode / (nMax - distToLastFailedNode + 1)));
        }
    }

    private void checkThenAdd(int nMax, ArrayList<Integer> lastNGram, double confOfLastRelNode, int distance) {

        if (nGramHashSet.get(lastNGram) == null) {
            double EF = getIntersectionCount2(lastNGram, failedTest, nMax);
            //if minSup is fulfilled and the new confidence is big enough
            if ((EF > 0) && (EF >= minEF)) {
                double ET = getIntersectionCount2(lastNGram, involvedTest, nMax);
                if (!dynaSup) {
                    nGramHashSet.computeIfAbsent(lastNGram, v -> new NGram(nMax, EF, ET, lastNGram));
                } else if ((EF / ET) >= Math.pow(confOfLastRelNode, distance + 1)) {
                    nGramHashSet.computeIfAbsent(lastNGram, v -> new NGram(nMax, EF, ET, lastNGram));
                }
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
                            ArrayList<Integer> tmp = new ArrayList<>(1);
                            tmp.add(b);
                            nGramHashSet.computeIfAbsent(tmp, v -> new NGram(1, EF, ET, tmp));
                        }
                    }
                }
                involvedTest.computeIfAbsent(b, v -> new HashSet<>());
                involvedTest.get(b).add(testId);
            });
        });
    }

    private double getIntersectionCount(ArrayList<Integer> ngram, HashMap<Integer, HashSet<Integer>> map, int maxN) {
        ArrayList<HashSet<Integer>> allSet = new ArrayList<>(maxN);
        HashSet<Integer> intersect;

        for (int i = 0; i < maxN; i++) {
            if (map.get(ngram.get(i)) == null) return 0.0;
            allSet.add(map.get(ngram.get(i)));
        }
        allSet.sort(new SizeComparator());

        intersect = (HashSet) allSet.get(0).clone();

        for (int i = 1; i < maxN; i++) {
            intersect.retainAll(allSet.get(i));
            if (intersect.size() == 0) break;
        }


        return intersect.size();
    }

    private double getIntersectionCount2(ArrayList<Integer> ngram, HashMap<Integer, HashSet<Integer>> map, int maxN) {
        ArrayList<HashSet<Integer>> allSet = new ArrayList<>(maxN);
        for (int i = 0; i < maxN; i++) {
            if (map.get(ngram.get(i)) == null) return 0.0;
            allSet.add(map.get(ngram.get(i)));
        }
        allSet.sort(new SizeComparator());
        final int length = allSet.get(0).size();
        if (length == 0) return 0;
        final int[] tmp = new int[length];
        final int[] i = new int[1];


        allSet.get(0).forEach(e -> {
            tmp[i[0]] = e;
            i[0]++;
        });
        for (int j = 1; j < maxN; j++) {
            for (int n = 0; n < length; n++) {
                if (!allSet.get(j).contains(tmp[n])) tmp[n] = -1;
            }
        }
        double count = 0;
        for (int n = 0; n < length; n++) {
            if (tmp[n] != -1) count++;
        }
        return count;
    }


    class SizeComparator implements Comparator<HashSet<?>> {

        @Override
        public int compare(HashSet<?> o1, HashSet<?> o2) {
            return Integer.compare(o1.size(), o2.size());
        }
    }
}
