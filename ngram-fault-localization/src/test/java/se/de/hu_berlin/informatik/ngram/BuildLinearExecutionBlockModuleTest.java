package se.de.hu_berlin.informatik.ngram;

import org.junit.jupiter.api.Test;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.IntTraceIterator;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings.getStdResourcesDir;

class BuildLinearExecutionBlockModuleTest {

    public static <T> Stream<List<T>> sliding(List<T> list, int size) {
        if (size > list.size())
            return Stream.empty();
        return IntStream.range(0, list.size() - size + 1)
                .mapToObj(start -> list.subList(start, start + size));

    }

    @Test
    void reduceTraceToBlock() {
        Path output1 = Paths.get(getStdResourcesDir(), "spectraCompressed.zip");

        ISpectra<SourceCodeBlock, ?> input = SpectraFileUtils.loadBlockCountSpectraFromZipFile(output1);
        BuildLinearExecutionBlockModule dummy = new BuildLinearExecutionBlockModule();
        ISpectra<SourceCodeBlock, ?> result = dummy.processItem(input);

        double failingTestCount = result.getFailingTraces().size();
        ConcurrentHashMap<Integer, HashSet<Integer>> involvedTest = new ConcurrentHashMap<>();
        ConcurrentHashMap<Integer, HashSet<Integer>> failedTest = new ConcurrentHashMap<>();
        HashSet<Integer> relevant = new HashSet<>();
        // max length for n-gram
        int maxN = 2;
        int minsup = 1;
        //remember failing test id and create relevant 1-gram
        result.getFailingTraces().forEach(t -> {
            int testID = t.getIndex();
            t.getInvolvedNodes().forEach(n -> {
                INode<SourceCodeBlock> node = result.getNode(n);
                involvedTest.computeIfAbsent(n, v -> new HashSet<>());
                involvedTest.get(n).add(testID);
                failedTest.computeIfAbsent(n, v -> new HashSet<>());
                failedTest.get(n).add(testID);
                if (node.getEF() == failingTestCount) relevant.add(n);
            });
        });

        result.getSuccessfulTraces().forEach(t -> {
            t.getInvolvedNodes().forEach(n -> {
                INode<SourceCodeBlock> node = result.getNode(n);
                involvedTest.computeIfAbsent(n, v -> new HashSet<>());
                involvedTest.get(n).add(t.getIndex());

            });
        });

        System.out.println(relevant.size());
        System.out.println(involvedTest.size());
        HashSet<int[]> ngramSet = new HashSet<>();

        result.getTraces().forEach(t -> {
            for (ExecutionTrace trace : t.getExecutionTraces()) {
                if (trace.size() < maxN) continue;
                int[] lastNGram = new int[maxN];
                int lastFailedNode = maxN;
                //array to get EF/Support and ET/involvement values for each n-gram
                int[] stats = new int[2];
                IntTraceIterator eTraceIt = trace.iterator();
                for (int i = 0; i < maxN; i++) {
                    int tmp = eTraceIt.next();
                    if (relevant.contains(tmp)) lastFailedNode = i;
                    lastNGram[i] = tmp;
                }
                if (lastFailedNode < maxN) {
                    stats = getEFAndET(lastNGram, involvedTest, failedTest, maxN);
                    //if minSup is fulfilled
                    if (stats[0] >= minsup) ngramSet.add(lastNGram);
                }

                while (eTraceIt.hasNext()) {
                    int[] nGram = new int[maxN];
                    for (int j = 0; j < maxN - 1; j++) {
                        nGram[j] = lastNGram[j + 1];
                    }
                    int tmp = eTraceIt.next();
                    if (relevant.contains(tmp)) {
                        lastFailedNode = 0;
                    } else lastFailedNode++;
                    nGram[maxN - 1] = tmp;
                    if (lastFailedNode < maxN) {
                        stats = getEFAndET(nGram, involvedTest, failedTest, maxN);
                        //if minSup is fulfilled
                        if (stats[0] >= minsup) ngramSet.add(nGram);
                    }
                    lastNGram = nGram;

                }


            }

        });
        System.out.println("ngram set size : " + ngramSet.size());
        relevant.forEach(e -> {
            double EF = result.getNode(e).getEF();
            double ET = EF + result.getNode(e).getEP();
            double conf = EF / ET;

            System.out.print("[" + e + "] ");
            System.out.println("EF: " + EF + ", ET: " + ET + ", CONFIDENCE: " + conf);
        });
    }

    public int[] getEFAndET(int[] ngram, ConcurrentHashMap<Integer, HashSet<Integer>> involvement, ConcurrentHashMap<Integer, HashSet<Integer>> failedTest, int maxN) {
        HashSet<Integer> testTraceIds = involvement.get(ngram[0]);
        HashSet<Integer> failedID = failedTest.get(ngram[0]);
        int EF = 0, ET = 0;

        for (int i = 1; i < maxN && failedID != null && testTraceIds != null; i++) {
            if (failedTest.get(ngram[i]) != null) {
                failedID.retainAll(failedTest.get(ngram[i]));
            } else break;

            if (involvement.get(ngram[i]) == null) {
                System.out.println("involvement is null. something is worng here, node: " + ngram[i]);
                break;

            } else testTraceIds.retainAll(involvement.get(ngram[i]));
        }
        if (failedID != null) EF = failedID.size();
        if (testTraceIds != null) ET = testTraceIds.size();
        double conf = ET == 0 ? 0 : (double) EF / ET;
        System.out.print("[");
        for (int i = 0; i < maxN; i++) System.out.print(ngram[i] + " ");
        System.out.println("] EF: " + EF + ", ET: " + ET + ", CONFIDENCE: " + conf);
        return new int[]{EF, ET};

    }
}