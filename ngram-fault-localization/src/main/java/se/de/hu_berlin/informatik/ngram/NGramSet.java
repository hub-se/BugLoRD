package se.de.hu_berlin.informatik.ngram;

import java.util.*;

public class NGramSet {
    private final int maxLength;
    private final double minSupport;
    private LinearExecutionHitTrace hitTrace;
    private HashSet<NGram> nGramHashSet;
    private List<NGram> result;
    private HashSet<Integer> relevant;
    private int minEF;
    private double failedTestCount;

    public NGramSet(LinearExecutionHitTrace hitTrace, int maxLength, double minSupport) {
        this.hitTrace = hitTrace;
        this.maxLength = maxLength;
        this.minSupport = minSupport;
        minEF = (int) (minSupport * hitTrace.getFailedTestCount());
        failedTestCount = hitTrace.getFailedTestCount();
        nGramHashSet = new HashSet<>();
        //first step is the build the relevant block set
        relevant = initRelevantSet();
        buildNGramSet();
        result = new ArrayList<>(nGramHashSet);
        Collections.sort(result, Collections.reverseOrder());
    }

    public List<NGram> getResult() {
        return result;
    }

    public List<String> getResultAsText() {
        HashSet<Integer> visitedNode = new HashSet<>();
        List rText = new LinkedList();
        HashMap<Integer, LinkedHashSet<Integer>> blockMap = hitTrace.getBlockMap();
        result.forEach(entry -> {
            int blockCount = entry.length;
            int[] blockIDs = entry.getBlockIDs();
            StringBuilder string = new StringBuilder();
            //string.append("SEQ: " + Arrays.toString(blockIDs) + "\n");
            for (int i = 0; i < blockCount; i++) {
                int tmp = blockIDs[i];
                if (visitedNode.contains(tmp)) continue;
                visitedNode.add(tmp);
                LinkedHashSet<Integer> nodes = blockMap.get(tmp);
                nodes.forEach(n -> {
                    string.append(hitTrace.getShortIdentifier(n));
                    string.append(" EF: " + entry.getEF() + ", ET: " + entry.getET() + ", Confidence: " + entry.getConfidence() + "\n");
                });
            }
            if (string.length() > 0) rText.add(string.toString());
        });
        return rText;
    }

    public int getMinEF() {
        return minEF;
    }

    private void buildNGramSet() {
        //first step is to find the relevant 1-gram
        int failedTestCount = hitTrace.getFailedTestCount();
        HashMap<Integer, HashSet<Integer>> involvedTest = new HashMap<>();
        HashMap<Integer, HashSet<Integer>> failedTest = new HashMap<>();
        initInvolvementMap(involvedTest, failedTest);
        createNGrams(involvedTest, failedTest);
    }

    private void createNGrams(HashMap<Integer, HashSet<Integer>> involvedTest,
                              HashMap<Integer, HashSet<Integer>> failedTest) {
        hitTrace.getTestTrace().forEach(testTrace -> {
            testTrace.getTraces().forEach(seq -> {
                for (int i = 2; i <= maxLength; i++) {
                    calcNGramStats(seq.getBlockSeq(), i, involvedTest, failedTest);
                }
            });
        });
    }

    private void calcNGramStats(LinkedList<LinearExecutionBlock> seq, int nMax,
                                HashMap<Integer, HashSet<Integer>> involvedTest,
                                HashMap<Integer, HashSet<Integer>> failedTest) {

        // stop is this sequence is too short
        if (seq.size() < nMax) return;

        Iterator<LinearExecutionBlock> blockIt = seq.iterator();
        int[] lastNGram = new int[nMax];

        //array to get EF/Support and ET/involvement values for each n-gram
        double[] stats = new double[2];

        //distance to the node that was executed by all failed tests
        int distToLastFailedNode = nMax;

        //init the first nMax-Gram
        for (int i = 0; i < nMax; i++) {
            int tmp = blockIt.next().getIndex();
            if (relevant.contains(tmp)) distToLastFailedNode = i;
            lastNGram[i] = tmp;
        }

        // we only create new n-gram if it  contains a 1-gram that was executed by all failed test.
        // that means that "distToLastFailedNode" must be smaller than "nMax"
        if (distToLastFailedNode < nMax) {
            stats = getEFAndET(lastNGram, involvedTest, failedTest, nMax);
            //if minSup is fulfilled
            if (stats[0] >= minEF) {
                nGramHashSet.add(new NGram(nMax, stats[0], stats[1], lastNGram));
            }
        }

        while (blockIt.hasNext()) {
            int[] nGram = new int[nMax];
            // copy the tail of the last n-gram
            for (int j = 0; j < nMax - 1; j++) {
                nGram[j] = lastNGram[j + 1];
            }

            //get the next element and reset the distance if a new failed node is found.
            int tmp = blockIt.next().getIndex();
            if (relevant.contains(tmp)) {
                distToLastFailedNode = 0;
            } else distToLastFailedNode++;

            nGram[nMax - 1] = tmp;

            // again, we only save this ngram if it contains a relevant block
            if (distToLastFailedNode < nMax) {
                stats = getEFAndET(lastNGram, involvedTest, failedTest, nMax);
                //if minSup is fulfilled
                if (stats[0] >= minEF) {
                    nGramHashSet.add(new NGram(nMax, stats[0], stats[1], lastNGram));
                }
            }
            lastNGram = nGram;
        }
    }

    private void initInvolvementMap(HashMap<Integer, HashSet<Integer>> involvedTest,
                                    HashMap<Integer, HashSet<Integer>> failedTest) {
        hitTrace.getTestTrace().forEach(t -> {
            boolean isFailed = !t.isSuccessful();
            int testId = t.getTestID();
            t.getInvolvedBlocks().forEach(b -> {
                if (isFailed) {
                    failedTest.computeIfAbsent(b, v -> new HashSet<Integer>());
                    failedTest.get(b).add(testId);
                }
                involvedTest.computeIfAbsent(b, v -> new HashSet<Integer>());
                involvedTest.get(b).add(testId);
            });
        });
    }

    private HashSet<Integer> initRelevantSet() {
        HashSet<Integer> relevant = new HashSet<>();
        int failedTestCount = hitTrace.getFailedTestCount();

        List<LinearExecutionTestTrace> failedTraces = hitTrace.getFailedTest();
        failedTraces.forEach(t -> {
            HashSet<Integer> blocks = t.getInvolvedBlocks();
            blocks.forEach(n -> {
                double EF, ET;
                EF = hitTrace.getEF(n);
                if (relevant.contains(n) || EF != (double) failedTestCount) return;
                relevant.add(n);
                ET = hitTrace.getEP(n) + EF;
                nGramHashSet.add(new NGram(1, EF, ET, new int[]{n}));
            });
        });
        return relevant;
    }

    private double[] getEFAndET(int[] ngram, HashMap<Integer,
            HashSet<Integer>> involvement,
                                HashMap<Integer, HashSet<Integer>> failedTest, int maxN) {

        double EF = getSetsIntersectSize(ngram, failedTest, maxN);
        double ET = getSetsIntersectSize(ngram, involvement, maxN);

        return new double[]{EF, ET};

    }

    private double getSetsIntersectSize(int[] ngram, HashMap<Integer, HashSet<Integer>> hashSetMap, int maxN) {

        if (hashSetMap.get(ngram[0]) == null) return 0.0;
        HashSet<Integer> intersect = (HashSet) hashSetMap.get(ngram[0]).clone();

        for (int i = 1; i < maxN; i++) {
            if (hashSetMap.get(ngram[i]) == null) return 0.0;
            intersect.retainAll(hashSetMap.get(ngram[i]));
            if (intersect.size() == 0) break;
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
