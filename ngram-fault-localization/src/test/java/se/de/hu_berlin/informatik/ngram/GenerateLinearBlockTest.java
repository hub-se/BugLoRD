package se.de.hu_berlin.informatik.ngram;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.de.hu_berlin.informatik.ngram.GenerateLinearBlock.generateLinearBlockTrace;
import static se.de.hu_berlin.informatik.ngram.GenerateLinearBlock.initGraphNode;
import static se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings.getStdResourcesDir;

class GenerateLinearBlockTest {


    @org.junit.jupiter.api.Test
    void generateLinearExecutionBlockTest() {
        Path output1 = Paths.get(getStdResourcesDir(), "spectraCompressed.zip");

        ISpectra<SourceCodeBlock, ?> input = SpectraFileUtils.loadBlockCountSpectraFromZipFile(output1);
        ConcurrentHashMap<Integer, ExecutionGraphNode> nodeSeq = new ConcurrentHashMap<>();
        // first step is to initiate In- and Out-degree of the graph-nodes
        initGraphNode(input, nodeSeq);
        // second steps is to generate the linear execution blocks


        LinearExecutionHitTrace hitTrace = new LinearExecutionHitTrace(input);
        generateLinearBlockTrace(input, nodeSeq, hitTrace);
        //System.out.println(hitTrace.toString());
        hitTrace.getTestTrace().values().forEach(testTrace->testTrace.getTraces().forEach(innerTrace->{
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



//                //Log.out(this, innerTrace.blockSeq.toString());
//
//                //check if any block is missing
//                HashSet<Integer> temp = new HashSet<>();
//                innerTrace.allNodes().forEach(integer -> temp.add(integer));
//                visited.forEach(integer -> {
//                    if (!temp.contains(integer))
//                        System.out.println("not found " + integer + " , ");
//                });
//

//
//                // print all looped nodes
////                innerTrace.blockSeq.forEach(block -> block.getnIds().
////                        forEach(id -> {
////                            if (isLoop(nodeSeq.get(id)))
////                                System.out.println(
////                                    nodeSeq.get(id).toString()
////                                    +"\n"+
////                                    block.toString());
////                        }));
//                //check if  the number of nodes is correct
//                assertTrue(
//                        test.getInvolvedNodes().size() == innerTrace.allNodes().size(),
//                        "involved count: "
//                                + test.getInvolvedNodes().size()
//                                + " innertrace node count: "
//                                + innerTrace.allNodes().size());
//
//                //members of the same block must also have the same properties
//                innerTrace.blockSeq.forEach(block -> block.getnIds().
//                        forEach(id -> assertTrue(
//                                input.getNode(block.blockId).getEF()
//                                        == input.getNode(id).getEF(),
//                                "Block  " + block.blockId + " : EF = "
//                                        + input.getNode(block.blockId).getEF() +
//                                        " vs Node: " + id + " : EF = "
//                                        + input.getNode(id).getEF())));
//
//                //members of the same block must also have the same properties
//                innerTrace.blockSeq.forEach(block -> block.getnIds().
//                        forEach(id -> assertTrue(
//                                input.getNode(block.blockId).getEP()
//                                        == input.getNode(id).getEP(),
//                                "Block  " + block.blockId + " : EP = "
//                                        + input.getNode(block.blockId).getEF() +
//                                        " vs Node: " + id + " : EP = "
//                                        + input.getNode(id).getEP())));
//


