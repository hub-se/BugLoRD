package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings.getStdResourcesDir;

class NGramSetTest {
    @org.junit.jupiter.api.Test
    void generateNGSet() {
        long start = System.currentTimeMillis();
        Path output1 = Paths.get(getStdResourcesDir(), "spectraCompressed.zip");
        ISpectra<SourceCodeBlock, ?> input = SpectraFileUtils.loadBlockCountSpectraFromZipFile(output1);
        System.out.println("Time in total for loading the spectra: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        start = System.currentTimeMillis();
        System.out.println("number of test: " + input.getTraces().size());
        LinearExecutionHitTrace hitTrace = new LinearExecutionHitTrace(input);
        NGramSet nGrams = new NGramSet(hitTrace, 2, 0.9);
        System.out.println("ngram set size : " + nGrams.getResult().size());
        System.out.println("Time in total for NGRAM methods: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");

        if (1 == 2) {
            nGrams.getResult().forEach(e -> {
                double EF = e.getEF();
                double ET = e.getET();
                double conf = e.getConfidence();

                System.out.print("[" + Arrays.toString(e.getBlockIDs()) + "] ");
                System.out.println("EF: " + EF + ", ET: " + ET + ", CONFIDENCE: " + conf);
            });
        } else nGrams.getResultAsText().forEach(e -> System.out.println(e));


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
        HashSet<Integer> c2 = (HashSet) sSet.get(2).clone();
        c1.retainAll(c2);
        System.out.println(c1);
        System.out.println(sSet);

    }

    class SizeComarator implements Comparator<HashSet<?>> {

        @Override
        public int compare(HashSet<?> o1, HashSet<?> o2) {
            return Integer.valueOf(o1.size()).compareTo(o2.size());
        }
    }

}