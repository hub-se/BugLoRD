package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings.getStdResourcesDir;

class LinearExecutionHitTraceTest {
    @org.junit.jupiter.api.Test
    void generateLinearExecutionHitTraceTest() {
        Path output1 = Paths.get(getStdResourcesDir(), "spectraCompressed.zip");

        ISpectra<SourceCodeBlock, ?> input = SpectraFileUtils.loadBlockCountSpectraFromZipFile(output1);
        LinearExecutionHitTrace hitTrace = new LinearExecutionHitTrace(input);

        System.out.println("# of Blocks: " + hitTrace.getBlockCount() + " blockMap-size: " + hitTrace.getBlockMap().size());
        System.out.println(hitTrace.getBlockMap());

        //check if any block is missing
        HashSet<Integer> temp = new HashSet<>();
        hitTrace.getTestTrace().forEach(t -> {
            t.getTraces().forEach(e -> {
                e.getBlockSeq().forEach(b -> temp.add(b.getIndex()));
            });
        });

        hitTrace.getBlockMap().keySet().forEach((integer -> {
            if (!temp.contains(integer))
                System.out.println("not found " + integer + " , ");
        }));

        //check if there is a node that is in more than 1 block
        HashMap<Integer, HashSet<Integer>> inverted = new HashMap<>();

        hitTrace.getTestTrace().forEach(e -> {
            e.getTraces().forEach(t -> {
                t.getBlockSeq().forEach(b -> {
                    b.getNodeSequence().forEach(n -> {
                        inverted.putIfAbsent(n.getIndex(), new HashSet<>());
                        inverted.get(n.getIndex()).add(b.getIndex());
                    });
                });
            });
        });

        inverted.forEach((k, v) -> {
            if (v.size() > 1) System.out.println("Node: " + k + " comes in these blocks: " + v);
        });
        hitTrace.getTestTrace().forEach(testTrace -> testTrace.getTraces().forEach(innerTrace -> {
            //check if there is any corrupted block
            innerTrace.getBlockSeq().forEach(
                    e -> {
                        assertTrue(e != null, "Block is null");
                        assertTrue(e.getIndex() != -1, "there is un-initiated block.");
                    }
            );
            //check if there is any duplicate
            assertTrue(innerTrace.allNodes().stream().filter(
                    e -> Collections.frequency(
                            innerTrace.allNodes(), e) > 1).count() == 0,
                    "There is duplicated element.");
        }));

    }
}