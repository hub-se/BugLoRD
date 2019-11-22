package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings.getStdResourcesDir;

class LinearExecutionHitTraceTest {
    @org.junit.jupiter.api.Test
    void generateLinearExecutionHitTraceTest() {
        long start = System.currentTimeMillis();
        //Path output1 = Paths.get(getStdResourcesDir(), "Math-84b.zip");
        //Path output1 = Paths.get(getStdResourcesDir(), "spectraCompressed.zip");
        //Path output1 = Paths.get(getStdResourcesDir(), "Chart-26b.zip");
        Path output1 = Paths.get(getStdResourcesDir(), "Chart-4b.zip");
        ISpectra<SourceCodeBlock, ?> input = SpectraFileUtils.loadBlockCountSpectraFromZipFile(output1);
        //checkSpectra(input);
        System.out.println("Time in total for loading the spectra: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        System.out.println("number of test: " + input.getTraces().size());
        System.out.println("number of failed test: " + input.getFailingTraces().size());
        System.out.println("number of nodes: " + input.getNodes().size());
        start = System.currentTimeMillis();
        //checkOutSlowPart(input);
        LinearExecutionHitTrace hitTrace = new LinearExecutionHitTrace(input);
        System.out.println("Total time for init LEB methods: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        System.out.println("# of Blocks: " + hitTrace.getBlockCount() + " blockMap-size: " + hitTrace.getBlock2NodeMap().size());
        System.out.println("# of nodes in nodeseq: " + hitTrace.getNodeSeq().size());
//        System.out.println(input.getNode(33166).getEF() + " "+input.getNode(33166).getEP());
        //System.out.println(hitTrace.getBlock2NodeMap());
        //       System.out.println(hitTrace.getNodeSeq().get(33947));
//        System.out.println(hitTrace.getBlock2NodeMap().get(1720));
//        System.out.println(hitTrace.getNodeSeq().get(54690));
//        System.out.println(hitTrace.getBlock2NodeMap().get(44746));
//        System.out.println(hitTrace.getNodeSeq().get(15361));
//        System.out.println(hitTrace.getBlock2NodeMap().get(6264));
//        System.out.println(hitTrace.getNodeSeq().get(46324));
//        System.out.println(hitTrace.getNodeSeq().get(12085));
//        System.out.println(hitTrace.getNodeSeq().get(12067));

        getDiffFromNodeSeq(input, hitTrace);
        getDiffFromBlockMap(input, hitTrace);
        //checkNodes(input, hitTrace);
        isBlockMissing(hitTrace);
        checkMultiUsedNode(hitTrace);
        checkCorruption(hitTrace);
        checkStats(input, hitTrace);

    }

    private void checkOutSlowPart(ISpectra<SourceCodeBlock, ?> input) {
        input.getTraces().forEach(t -> {
            int i = t.getIndex();
            if (i == 58) {
                int count = 0;
                HashSet<Integer> set = new HashSet<>(43963362);
                for (ExecutionTrace e : t.getExecutionTraces()) {
                    System.out.println("test 58 execution trace size: " + e.size());
                    Iterator<Integer> nodeIdIterator = e.mappedIterator(input.getIndexer());
                    while (nodeIdIterator.hasNext()) {
                        count++;
                        set.add(nodeIdIterator.next());
                    }
                }
                System.out.println("test 58 execution trace node count: " + count);
                System.out.println("test 58 execution trace node count, set size: " + set.size());
                return;
            }
        });
    }

    private void getDiffFromBlockMap(ISpectra<SourceCodeBlock, ?> input, LinearExecutionHitTrace hitTrace) {
        HashSet<Integer> allNodes = new HashSet<>();
        hitTrace.getAllBlocks().forEach(b -> {
            hitTrace.getBlock2NodeMap().get(b).forEach(n -> {
                allNodes.add(n);
            });
        });
        System.out.println("getDiffFromBlockMap: Is-Nodes count: " + allNodes.size());

        HashSet<Integer> diff = new HashSet<>();
        input.getTraces().forEach(t -> t.getInvolvedNodes().forEach(n -> diff.add(n)));
        System.out.println("Should-Nodes count: " + diff.size());
        diff.removeAll(allNodes);
        System.out.println("diff count: " + diff.size());
        if (diff.size() != 0) {
            System.out.println("Diff: ");
            System.out.println(diff);
        }
    }

    private void getDiffFromNodeSeq(ISpectra<SourceCodeBlock, ?> input, LinearExecutionHitTrace hitTrace) {
        HashSet<Integer> allNodes = new HashSet<>();
        hitTrace.getNodeSeq().forEach((k, v) -> allNodes.add(k));
        System.out.println("getDiffFromNodeSeq: Is-Nodes count: " + hitTrace.getNodeSeq().size());

        HashSet<Integer> diff = new HashSet<>();
        input.getTraces().forEach(t -> t.getInvolvedNodes().forEach(n -> diff.add(n)));
        System.out.println("Should-Nodes count: " + diff.size());
        diff.removeAll(allNodes);
        System.out.println("diff count: " + diff.size());
        if (diff.size() != 0) {
            System.out.println("Diff: ");
            System.out.println(diff);
        }
    }

    private void checkSpectra(ISpectra<SourceCodeBlock, ?> input) {
        HashSet<Integer> allNodes = new HashSet<>();
        input.getTraces().forEach(t -> t.getInvolvedNodes().forEach(n -> allNodes.add(n)));
        System.out.println("checSpectra: Is-Nodes count from traces: " + allNodes.size());

        HashSet<Integer> diff = new HashSet<>();
        input.getNodes().forEach(n -> {
            diff.add(n.getIndex());
        });
        System.out.println("Should-Nodes count from getnodes(): " + diff.size());
        diff.removeAll(allNodes);
        System.out.println("diff count: " + diff.size());
        if (diff.size() != 0) {
            System.out.println("Diff: ");
            System.out.println(diff);
        }

    }

    private void checkNodes(ISpectra<SourceCodeBlock, ?> input, LinearExecutionHitTrace hitTrace) {
        HashSet<Integer> allNodes = new HashSet<>();
        hitTrace.getNodeSeq().forEach((k, v) -> allNodes.add(k));
        System.out.println("checkNodes: Is-Nodes count: " + hitTrace.getNodeSeq().size());
        HashSet<Integer> diff = new HashSet<>();
        input.getNodes().forEach(e -> diff.add(e.getIndex()));
        System.out.println("Should-Nodes count: " + diff.size());
        diff.removeAll(allNodes);
        System.out.println("diff count: " + diff.size());
        if (diff.size() != 0) {
            System.out.println("Diff: ");
            System.out.println(diff);
        }
    }

    private void checkStats(ISpectra<SourceCodeBlock, ?> input, LinearExecutionHitTrace hitTrace) {
        hitTrace.getBlock2NodeMap().forEach((k, v) -> {
            double ef = input.getNode(k).getEF();
            double ep = input.getNode(k).getEP();

            v.forEach(i -> {
                assertTrue(ef == input.getNode(i).getEF(), "EF is different! Block: " + k);
                assertTrue(ep == input.getNode(i).getEP(), "EP is different! Block: " + k);
            });
        });
    }

    private void checkCorruption(LinearExecutionHitTrace hitTrace) {
        hitTrace.getTestTrace().forEach(testTrace -> testTrace.getTraces().forEach(innerTrace -> {
            //check if there is any corrupted block
            innerTrace.getBlockSeq().forEach(
                    e -> {
                        assertTrue(e != null, "Block is null");
                        assertTrue(e != -1, "there is un-initiated block in test: " + testTrace.getTestID());
                    }
            );
        }));
    }

    private void checkMultiUsedNode(LinearExecutionHitTrace hitTrace) {
        //check if there is a node that is in more than 1 block
        HashMap<Integer, HashSet<Integer>> inverted = new HashMap<>();

        hitTrace.getTestTrace().forEach(e -> {
            e.getTraces().forEach(t -> {
                t.getBlockSeq().forEach(b -> {
                    if (hitTrace.getBlock2NodeMap().get(b) == null)
                        System.out.println("not found block: " + b + "  in test no: " + e.getTestID());
                    hitTrace.getBlock2NodeMap().get(b).forEach(n -> {
                        inverted.putIfAbsent(n, new HashSet<>());
                        inverted.get(n).add(b);
                    });
                });
            });
        });

        inverted.forEach((k, v) -> {
            if (v.size() > 1) System.out.println("Node: " + k + " comes in these blocks: " + v);
        });
    }

    private void isBlockMissing(LinearExecutionHitTrace hitTrace) {
        //check if any block is missing
        HashSet<Integer> temp = new HashSet<>();
        hitTrace.getTestTrace().forEach(t -> {
            t.getTraces().forEach(e -> {
                e.getBlockSeq().forEach(b -> temp.add(b));
            });
        });

        hitTrace.getBlock2NodeMap().keySet().forEach((integer -> {
            if (!temp.contains(integer))
                System.out.println("not found " + integer + " , ");
        }));
    }
}