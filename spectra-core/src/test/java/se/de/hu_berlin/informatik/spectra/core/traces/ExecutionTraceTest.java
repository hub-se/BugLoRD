package se.de.hu_berlin.informatik.spectra.core.traces;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.SequiturUtils;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.input.InputSequence;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.sequitur.output.OutputSequence;
import se.de.hu_berlin.informatik.spectra.util.CachedIntArrayMap;
import se.de.hu_berlin.informatik.spectra.util.CachedMap;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class ExecutionTraceTest extends TestSettings {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    private int[] s(int... ints) {
        return ints;
    }

    @Test
    public void mappedIterator() throws IOException {

        Path outputDir = Paths.get(getStdTestDir(), "eTraceCacheMap");
        CachedMap<int[]> nodeIdSequences = new CachedIntArrayMap(outputDir.resolve("nodeIdSequences.zip"),
                0, SpectraFileUtils.NODE_ID_SEQUENCES_DIR, true);
        nodeIdSequences.put(1, s(7,8,9));
        nodeIdSequences.put(2, s(12,13,14));
        nodeIdSequences.put(3, s(23,22));
        CachedMap<int[]> subTraceIdSequences = new CachedIntArrayMap(outputDir.resolve("subTraceIdSequences.zip"),
                0, SpectraFileUtils.SUB_TRACE_ID_SEQUENCES_DIR, true);
        subTraceIdSequences.put(1, s(1,1,2));
        subTraceIdSequences.put(2, s(3,3,2));

        SequenceIndexerCompressed sequenceIndexer = new SimpleIntIndexerCompressed(nodeIdSequences, subTraceIdSequences);
        OutputSequence eTrace = new OutputSequence();
        int[] testTrace = s(1,2,1,2);
        for (int i : testTrace) {
            eTrace.append(i);
        }
        byte[] traceByteArray = SequiturUtils.convertToByteArray(eTrace, true);

        ExecutionTrace trace = new ExecutionTrace(traceByteArray, sequenceIndexer);
        InputSequence.TraceIterator traceIterator = trace.iterator();
        for (int i : testTrace) {
            Assert.assertTrue(traceIterator.hasNext());
            Assert.assertEquals(i, traceIterator.next());
        }

        int[] testMappedTrace = s(7,8,9,7,8,9,12,13,14,23,22,23,22,12,13,14, 7,8,9,7,8,9,12,13,14,23,22,23,22,12,13,14);
        Iterator<Integer> mappedIterator = trace.mappedIterator(sequenceIndexer);
        for (int i : testMappedTrace) {
            Assert.assertTrue("element: " + i, mappedIterator.hasNext());
            Assert.assertEquals(i, mappedIterator.next().intValue());
        }

        Iterator<Integer> mappedReverseIterator = trace.mappedReverseIterator(sequenceIndexer);
        for (int i = testMappedTrace.length - 1; i >= 0; --i) {
            Assert.assertTrue("element: " + testMappedTrace[i], mappedReverseIterator.hasNext());
            Assert.assertEquals(testMappedTrace[i], mappedReverseIterator.next().intValue());
        }
    }

    @Test
    public void mappedReverseIterator() throws IOException {

        Path outputDir = Paths.get(getStdTestDir(), "eTraceCacheMap");
        CachedMap<int[]> nodeIdSequences = new CachedIntArrayMap(outputDir.resolve("nodeIdSequences.zip"),
                0, SpectraFileUtils.NODE_ID_SEQUENCES_DIR, true);
        nodeIdSequences.put(1, s(7,8,9));
        nodeIdSequences.put(2, s(12,13,14));
        nodeIdSequences.put(3, s(23,22));
        CachedMap<int[]> subTraceIdSequences = new CachedIntArrayMap(outputDir.resolve("subTraceIdSequences.zip"),
                0, SpectraFileUtils.SUB_TRACE_ID_SEQUENCES_DIR, true);
        subTraceIdSequences.put(1, s(1,1,2));
        subTraceIdSequences.put(2, s(3,3,2));

        SequenceIndexerCompressed sequenceIndexer = new SimpleIntIndexerCompressed(nodeIdSequences, subTraceIdSequences);
        OutputSequence eTrace = new OutputSequence();
        int[] testTrace = s(1,2,1,2);
        for (int i : testTrace) {
            eTrace.append(i);
        }
        byte[] traceByteArray = SequiturUtils.convertToByteArray(eTrace, true);

        ExecutionTrace trace = new ExecutionTrace(traceByteArray, sequenceIndexer);
        InputSequence.TraceIterator traceIterator = trace.iterator();
        for (int i : testTrace) {
            Assert.assertTrue(traceIterator.hasNext());
            Assert.assertEquals(i, traceIterator.next());
        }

        int[] testMappedTrace = s(7,8,9,7,8,9,12,13,14,23,22,23,22,12,13,14, 7,8,9,7,8,9,12,13,14,23,22,23,22,12,13,14);
        Iterator<Integer> mappedIterator = trace.mappedIterator(sequenceIndexer);
        for (int i : testMappedTrace) {
            Assert.assertTrue("element: " + i, mappedIterator.hasNext());
            Assert.assertEquals(i, mappedIterator.next().intValue());
        }

        Iterator<Integer> mappedReverseIterator = trace.mappedReverseIterator(sequenceIndexer);
        for (int i = testMappedTrace.length - 1; i >= 0; --i) {
            Assert.assertTrue("element: " + testMappedTrace[i], mappedReverseIterator.hasNext());
            Assert.assertEquals(testMappedTrace[i], mappedReverseIterator.next().intValue());
        }
    }

    @Test
    public void mappedIterator2() throws IOException {

        Path outputDir = Paths.get(getStdTestDir(), "eTraceCacheMap2");
        CachedMap<int[]> nodeIdSequences = new CachedIntArrayMap(outputDir.resolve("nodeIdSequences.zip"),
                0, SpectraFileUtils.NODE_ID_SEQUENCES_DIR, true);
        nodeIdSequences.put(1, s());
        nodeIdSequences.put(2, s(12,13,14));
        nodeIdSequences.put(3, s(23,22));
        CachedMap<int[]> subTraceIdSequences = new CachedIntArrayMap(outputDir.resolve("subTraceIdSequences.zip"),
                0, SpectraFileUtils.SUB_TRACE_ID_SEQUENCES_DIR, true);
        subTraceIdSequences.put(1, s(1,1,2));
        subTraceIdSequences.put(2, s(3,3,2));

        SequenceIndexerCompressed sequenceIndexer = new SimpleIntIndexerCompressed(nodeIdSequences, subTraceIdSequences);
        OutputSequence eTrace = new OutputSequence();
        int[] testTrace = s(1,2,1,2);
        for (int i : testTrace) {
            eTrace.append(i);
        }
        byte[] traceByteArray = SequiturUtils.convertToByteArray(eTrace, true);

        ExecutionTrace trace = new ExecutionTrace(traceByteArray, sequenceIndexer);
        InputSequence.TraceIterator traceIterator = trace.iterator();
        for (int i : testTrace) {
            Assert.assertTrue(traceIterator.hasNext());
            Assert.assertEquals(i, traceIterator.next());
        }

        int[] testMappedTrace = s(12,13,14,23,22,23,22,12,13,14, 12,13,14,23,22,23,22,12,13,14);
        Iterator<Integer> mappedIterator = trace.mappedIterator(sequenceIndexer);
        for (int i : testMappedTrace) {
            Assert.assertTrue("element: " + i, mappedIterator.hasNext());
            Assert.assertEquals(i, mappedIterator.next().intValue());
        }

        Iterator<Integer> mappedReverseIterator = trace.mappedReverseIterator(sequenceIndexer);
        for (int i = testMappedTrace.length - 1; i >= 0; --i) {
            Assert.assertTrue("element: " + testMappedTrace[i], mappedReverseIterator.hasNext());
            Assert.assertEquals(testMappedTrace[i], mappedReverseIterator.next().intValue());
        }
    }

    @Test
    public void flatMappedIterator() throws IOException {

        Path outputDir = Paths.get(getStdTestDir(), "eTraceCacheMapFlat");
        CachedMap<int[]> nodeIdSequences = new CachedIntArrayMap(outputDir.resolve("nodeIdSequences.zip"),
                0, SpectraFileUtils.NODE_ID_SEQUENCES_DIR, true);
        nodeIdSequences.put(1, s(7,8,9));
        nodeIdSequences.put(2, s(12,13,14));
        nodeIdSequences.put(3, s(23,22));

        SequenceIndexerCompressed sequenceIndexer = new SimpleIntIndexerCompressed(nodeIdSequences, null);
        OutputSequence eTrace = new OutputSequence();
        int[] testTrace = s(1,2,3,1,2,3);
        for (int i : testTrace) {
            eTrace.append(i);
        }
        byte[] traceByteArray = SequiturUtils.convertToByteArray(eTrace, true);

        ExecutionTrace trace = new ExecutionTrace(traceByteArray, sequenceIndexer);
        InputSequence.TraceIterator traceIterator = trace.iterator();
        for (int i : testTrace) {
            Assert.assertTrue(traceIterator.hasNext());
            Assert.assertEquals(i, traceIterator.next());
        }

        int[] testMappedTrace = s(7,8,9,12,13,14,23,22, 7,8,9,12,13,14,23,22);
        Iterator<Integer> mappedIterator = trace.mappedIterator(sequenceIndexer);
        for (int i : testMappedTrace) {
            Assert.assertTrue("element: " + i, mappedIterator.hasNext());
            Assert.assertEquals(i, mappedIterator.next().intValue());
        }

        Iterator<Integer> mappedReverseIterator = trace.mappedReverseIterator(sequenceIndexer);
        for (int i = testMappedTrace.length - 1; i >= 0; --i) {
            Assert.assertTrue("element: " + testMappedTrace[i], mappedReverseIterator.hasNext());
            Assert.assertEquals(testMappedTrace[i], mappedReverseIterator.next().intValue());
        }
    }

    @Test
    public void flatMappedIterator2() throws IOException {

        Path outputDir = Paths.get(getStdTestDir(), "eTraceCacheMapFlat2");
        CachedMap<int[]> nodeIdSequences = new CachedIntArrayMap(outputDir.resolve("nodeIdSequences.zip"),
                0, SpectraFileUtils.NODE_ID_SEQUENCES_DIR, true);
        nodeIdSequences.put(1, s(7,8,9));
        nodeIdSequences.put(2, s());
        nodeIdSequences.put(3, s(23,22));

        SequenceIndexerCompressed sequenceIndexer = new SimpleIntIndexerCompressed(nodeIdSequences, null);
        OutputSequence eTrace = new OutputSequence();
        int[] testTrace = s(1,2,3,1,2,3);
        for (int i : testTrace) {
            eTrace.append(i);
        }
        byte[] traceByteArray = SequiturUtils.convertToByteArray(eTrace, true);

        ExecutionTrace trace = new ExecutionTrace(traceByteArray, sequenceIndexer);
        InputSequence.TraceIterator traceIterator = trace.iterator();
        for (int i : testTrace) {
            Assert.assertTrue(traceIterator.hasNext());
            Assert.assertEquals(i, traceIterator.next());
        }

        int[] testMappedTrace = s(7,8,9,23,22, 7,8,9,23,22);
        Iterator<Integer> mappedIterator = trace.mappedIterator(sequenceIndexer);
        for (int i : testMappedTrace) {
            Assert.assertTrue("element: " + i, mappedIterator.hasNext());
            Assert.assertEquals(i, mappedIterator.next().intValue());
        }

        Iterator<Integer> mappedReverseIterator = trace.mappedReverseIterator(sequenceIndexer);
        for (int i = testMappedTrace.length - 1; i >= 0; --i) {
            Assert.assertTrue("element: " + testMappedTrace[i], mappedReverseIterator.hasNext());
            Assert.assertEquals(testMappedTrace[i], mappedReverseIterator.next().intValue());
        }
    }

}