package se.de.hu_berlin.informatik.ngram;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedLongArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.CompressedIntegerTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs.CompressedLongTrace;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings.getStdResourcesDir;

class ExecutionGraphNodeTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();

    public static <T> Stream<List<T>> sliding(List<T> list, int size) {
        if (size > list.size())
            return Stream.empty();
        return IntStream.range(0, list.size() - size + 1)
                .mapToObj(start -> list.subList(start, start + size));
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    private int[] s(int... numbers) {
        return numbers;
    }

    private int[][] rt(int... numbers) {
        int[][] result = new int[numbers.length][];
        for (int i = 0; i < numbers.length; ++i) {
            result[i] = new int[]{0, numbers[i], 0};
        }
        return result;
    }

    private CompressedLongTrace asList(Path outputDir, int[][] rt) {
        BufferedLongArrayQueue list = new BufferedLongArrayQueue(
                outputDir.toFile(), String.valueOf(UUID.randomUUID()), rt.length);
        for (int[] statement : rt) {
            list.add(CoberturaStatementEncoding.generateUniqueRepresentationForStatement(statement[0], statement[1], statement[2]));
        }
        return new CompressedLongTrace(list, true);
    }

    private CompressedIntegerTrace c(Path outputDir, int... numbers) {
        BufferedIntArrayQueue list = new BufferedIntArrayQueue(
                outputDir.toFile(), String.valueOf(UUID.randomUUID()), numbers.length);
        for (int id : numbers) {
            list.add(id);
        }
        return new CompressedIntegerTrace(list, true);
    }

//    @org.junit.jupiter.api.Test
//    void getNode() {
//        Path output1 = Paths.get(getStdResourcesDir(), "spectraCompressed.zip");
//
//        ISpectra<SourceCodeBlock, ?> spectra = SpectraFileUtils.loadBlockCountSpectraFromZipFile(output1);
//        Log.out(this, "spectra nodes count " + spectra.getNodes().size() + "\n \n");
//        Collection<INode<SourceCodeBlock>> nodes = spectra.getNodes();
//        Iterator<INode<SourceCodeBlock>> nodesIt = nodes.iterator();
//        if (nodesIt.hasNext()) {
//            Log.out(this, "test getnode()");
//            INode<SourceCodeBlock> dummy = nodesIt.next();
//            ExcGraphNode<SourceCodeBlock> testNode = new ExcGraphNode<SourceCodeBlock>(dummy);
//            Log.out(this, testNode.getNode().toString());
//            assertEquals(dummy, testNode.getNode());
//        }
//    }


    @org.junit.jupiter.api.Test
    void initNodesInAndOutDegreesSingleThread() {
        Path output1 = Paths.get(getStdResourcesDir(), "spectraCompressed.zip");

        ISpectra<SourceCodeBlock, ?> spectra = SpectraFileUtils.loadBlockCountSpectraFromZipFile(output1);
        Log.out(this, "spectra nodes count " + spectra.getNodes().size() + "\n");
        Collection<?> traces = spectra.getTraces();
        Log.out(this, "there are " + traces.size() + " traces.\n");

        ExecutionTrace trace;
        int i = 0;
        int count = 0;
        long start = System.currentTimeMillis();
        ConcurrentHashMap<Integer, ExecutionGraphNode> nodeSeq = new ConcurrentHashMap<>();
        HashSet<Integer> notInnerNodeInLEB = new HashSet<>();
        HashSet<Integer> loopLEB = new HashSet<>();
        HashSet<Integer> nodeInLEB = new HashSet<>();
        for (ITrace<SourceCodeBlock> test : spectra.getTraces()) {
            Log.out(this, "trace index at " + (i + 1) + ", now  get execution traces");
            Log.out(this, "\tnumber of contained execution traces  = " + test.getExecutionTraces().size());
            Log.out(this, "\tnumber of involved nodes in this trace: " + test.getInvolvedNodes().size());

            for (ExecutionTrace executionTrace : test.getExecutionTraces()) {
                System.out.println("\t\t\t" + test.getIdentifier() + "'s number of nodes: " + executionTrace.size() + "\n");
                //System.out.println(executionTrace.toString());
                Iterator<Integer> nodeIdIterator = executionTrace.mappedIterator(spectra.getIndexer());

                int lastId = 0;

                while (nodeIdIterator.hasNext()) {
                    int nodeIndex = nodeIdIterator.next();
                    nodeSeq.computeIfAbsent(nodeIndex, k -> new ExecutionGraphNode(nodeIndex, spectra.getNode(nodeIndex).getIdentifier(), spectra));
                    //if not first node
                    if (lastId != 0) {
                        nodeSeq.get(nodeIndex).addInNode(lastId);
                        nodeSeq.get(lastId).addOutNode(nodeIndex);
                    }

                    count++;

                    //System.out.println(nodeIndex + ": " + spectra.getNode(nodeIndex).getIdentifier());
                    //System.out.print(nodeIndex + "; ");
                    lastId = nodeIndex;
                }
                //System.out.println("counting Iterator steps = " + count);


            }
            i++;
        }
        Log.out(this, "nodeSeq contains: "+nodeSeq.size() +" and # of steps: "+count);
        nodeSeq.forEach((integer, node) -> {

            if (node.checkInNode(integer)) {
                System.out.println("looped single LEB found: " + integer + " 's parents: "
                        + Arrays.toString(node.getInNodes().toArray())
                        + " and children: " + Arrays.toString(node.getOutNodes().toArray()) +"\n\t" +
                        " nodeEF = "+node.getEF());
                loopLEB.add(integer);
            } else {
                if (node.getInDegree() <= 1) {


                    nodeInLEB.add(integer);

                    if (node.getInDegree() == 0){
                        System.out.println("starting node found: " + integer + " 's parents: "
                                + Arrays.toString(node.getInNodes().toArray())
                                + " and children: " + Arrays.toString(node.getOutNodes().toArray()));
                    }

                } else  {
//                    System.out.println("single LEB with in and out degree > 1 found: " + integer + " 's parents: "
////                            + Arrays.toString(node.getInNodes().toArray())
////                            + " and children: " + Arrays.toString(node.getOutNodes().toArray()));
                    notInnerNodeInLEB.add(integer);
                }

            }
        });
        System.out.println("Time in total: " + ((System.currentTimeMillis() - start) / 1000.0) + "s");
        Log.out(this,"there are "+notInnerNodeInLEB.size() +" not inner node of LEBs and "+loopLEB.size() +" looped and "+ nodeInLEB.size() + " inner nodes of LEBs");
        System.out.println("bool = expression : " + (( 1 + 1 == 5)?3:4));
        assertEquals(nodeSeq.size(), notInnerNodeInLEB.size() + nodeInLEB.size() + loopLEB.size());
    }


}