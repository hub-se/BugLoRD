package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings.getStdResourcesDir;

class NGramSetTest {
    @org.junit.jupiter.api.Test
    void generateNGSet() {
        long start = System.currentTimeMillis();
        //Path output1 = Paths.get(getStdResourcesDir(), "Math-84b.zip");
        Path output1 = Paths.get(getStdResourcesDir(), "spectraCompressed.zip");
        // Path output1 = Paths.get(getStdResourcesDir(), "Chart-26b.zip");
        //Path output1 = Paths.get(getStdResourcesDir(), "Closure-93b.zip");
        //Path output1 = Paths.get(getStdResourcesDir(), "Chart-4b.zip");
        ISpectra<SourceCodeBlock, ?> input = SpectraFileUtils.loadBlockCountSpectraFromZipFile(output1);
        System.out.println("Time in total for loading the spectra: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        System.out.println("number of test: " + input.getTraces().size());
        System.out.println("number of failed test: " + input.getFailingTraces().size());
        System.out.println("number of nodes: " + input.getNodes().size());
        start = System.currentTimeMillis();
        LinearExecutionHitTrace hitTrace = new LinearExecutionHitTrace(input);
        System.out.println("Total time for init LEB methods: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        System.out.println("number of blocks: " + hitTrace.getBlock2NodeMap().size());
        start = System.currentTimeMillis();

        NGramSet nGrams = new NGramSet(hitTrace, 3, 0.4, true);


        System.out.println("ngram set size : " + nGrams.getResult().size());
        System.out.println("ranking list size : " + nGrams.getConfidence().size());

        System.out.println("Total time for NGRAM methods: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        //playingAround(nGrams);
        printResult(nGrams);
        //printMap(nGrams.getConfidence());

    }

    @org.junit.jupiter.api.Test
    void checkCons() {
        checkConsistency("Chart-26b.zip", 4);
        //checkConsistency("Math-84b.zip", 10);
        //checkConsistency("Chart-4b.zip", 10);
        //checkConsistency("spectraCompressed.zip", 10);
    }

    private void checkConsistency(String file, int max) {
        Path output = Paths.get(getStdResourcesDir(), file);
        long start = System.currentTimeMillis();
        ISpectra<SourceCodeBlock, ?> input = SpectraFileUtils.loadBlockCountSpectraFromZipFile(output);
        System.out.println("Time in total for loading the spectra: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        System.out.println("number of test: " + input.getTraces().size());
        System.out.println("number of failed test: " + input.getFailingTraces().size());
        System.out.println("number of nodes: " + input.getNodes().size());
        int blockCount = 0, nGramCount = 0;
        start = System.currentTimeMillis();
        LinearExecutionHitTrace hitTrace = new LinearExecutionHitTrace(input);
        System.out.println("Total time for init LEB methods: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        System.out.println("number of blocks: " + hitTrace.getBlock2NodeMap().size());
        start = System.currentTimeMillis();

        NGramSet nGrams = new NGramSet(hitTrace, 3, 0.4, true);


        System.out.println("ngram set size : " + nGrams.getResult().size());
        System.out.println("ranking list size : " + nGrams.getConfidence().size());

        System.out.println("Total time for NGRAM methods: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        blockCount = hitTrace.getBlock2NodeMap().size();
        nGramCount = nGrams.getResult().size();
        System.out.println("the number of " + file + ", blockCount: "
                + blockCount + ", NgramCount: " + nGramCount);


        for (int i = 1; i <= max; i++) {
            List<NGram> tmp = new LinkedList<>();

            System.out.println("\niteration: " + i);
            NGramSet nGrams2 = new NGramSet(hitTrace, 3, 0.4, true);
            int newCount = nGrams2.getResult().size();
            if (nGramCount > newCount) {
                tmp = nGrams.getResult();
                tmp.removeAll(nGrams2.getResult());
            } else {
                if (nGramCount < newCount) {
                    tmp = nGrams2.getResult();
                    tmp.removeAll(nGrams.getResult());
                }
            }

            if (tmp.size() > 0) {
                System.out.println("the Ngram set was changing in " + file + ", old size: " + nGramCount + " new size: " + newCount + " vs diff size: "
                        + tmp.size() + " iteration = " + i);
                tmp.forEach(nGram -> System.out.println("\t" + nGram.toString()));
            }

        }

    }

    private void printResult(NGramSet nGrams) {
        if (1 == 2) {
            nGrams.getResult().forEach(e -> {
                double EF = e.getEF();
                double ET = e.getET();
                double conf = e.getConfidence();

                System.out.print("[" + e.getBlockIDs().toString() + "] ");
                System.out.println("EF: " + EF + ", ET: " + ET + ", CONFIDENCE: " + conf);
            });
        } else nGrams.getResultAsText().forEach(e -> System.out.println(e));
    }

    private void playingAround(NGramSet nGrams) {

        HashMap<Double, Integer> freqs = new HashMap<>();
        for (NGram nGram : nGrams.getResult()) {
            if (freqs.containsKey(nGram.getConfidence())) {
                Integer old = freqs.get(nGram.getConfidence());
                freqs.replace(nGram.getConfidence(), old + 1);
            } else freqs.put(nGram.getConfidence(), new Integer(1));
        }
        LinkedHashMap<Double, Double> weighted = new LinkedHashMap<>();
        freqs.entrySet().stream().forEach(x -> weighted.put(x.getKey(), x.getKey() * x.getValue() / freqs.size()));

        LinkedHashMap<Double, Double> sorted = new LinkedHashMap<>();
        weighted.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sorted.put(x.getKey(), x.getValue()));
        printMap(sorted);
    }

    private void printMap(HashMap<?, ?> sorted) {
        DecimalFormat df = new DecimalFormat("####,####.#####");
        sorted.forEach((key, value) -> System.out.println("[ " + key + " : " + df.format(value).concat(" ]")));
    }

    @org.junit.jupiter.api.Test
    void TestHasSetList() {
        HashSet<Integer> s1 = new HashSet<>();
        HashSet<Integer> s2 = new HashSet<>();
        HashSet<Integer> s3 = new HashSet<>();

        for (int i = 0; i < 4; i++) s1.add(i);
        for (int i = 3; i < 5; i++) s2.add(i);
        for (int i = 4; i < 7; i++) s3.add(i);

        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3);

        ConcurrentHashMap<Integer, HashSet<Integer>> sSet = new ConcurrentHashMap<>();
        sSet.put(1, s1);
        sSet.put(2, s2);
        sSet.put(3, s3);
        System.out.println(sSet);


        HashSet<Integer> c1 = (HashSet) sSet.get(1).clone();
        System.out.println("c1, before filtering: " + c1);
        for (int i = 2; i < sSet.size() + 1; i++) {
            HashSet<Integer> c = sSet.get(i);
            if (c == null) {
                System.out.println("nullpointer!: ");
            }
            System.out.println("check set: " + c);
            c1.retainAll(c);
            System.out.println("c1, after filtering: " + c1);
            if (c1 == null) break;
        }

        System.out.println("c1, at the end: " + c1);
        System.out.println("is c1 null? : " + (c1 == null));
        System.out.println("c1 size: " + c1.size());
        System.out.println("\n\noriginal: " + sSet);
        ArrayList<HashSet<Integer>> allSet = new ArrayList<>();
        sSet.values().forEach(e -> {
            allSet.add(e);
        });

        Collections.sort(allSet, new SizeComparator());
        System.out.println("copy to  an array: " + allSet);
        HashSet<Integer> d1 = (HashSet) allSet.get(0).clone();
        System.out.println("clone of the first set: " + d1);
        for (int i = 1; i < allSet.size(); i++) {
            HashSet<Integer> temp = allSet.get(i);
            System.out.println("check : " + temp);
            d1.retainAll(temp);
            if (d1 == null) break;
        }
        System.out.println("is left after filtering: " + d1);
        System.out.println("allset: " + allSet);
        System.out.println("sset: " + sSet);
    }

    @org.junit.jupiter.api.Test
    void TestHasHMap() {
        HashMap<ArrayList<Integer>, String> map = new HashMap<>();
        ArrayList<Integer> a = new ArrayList<>();
        ArrayList<Integer> b = new ArrayList<>();
        a.add(1);
        b.add(1);
        map.put(a, "A");
        System.out.println(map.containsKey(b));

    }

    class SizeComparator implements Comparator<HashSet<?>> {

        @Override
        public int compare(HashSet<?> o1, HashSet<?> o2) {
            return Integer.compare(o1.size(), o2.size());
        }
    }

}