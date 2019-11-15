package se.de.hu_berlin.informatik.spectra.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.count.CountSpectra;
import se.de.hu_berlin.informatik.spectra.core.count.CountTrace;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.core.traces.RawIntTraceCollector;
import se.de.hu_berlin.informatik.spectra.core.traces.SimpleIntIndexerCompressed;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.CoberturaSpectraProviderFactory;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.xml.CoberturaCountXMLProvider;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.xml.CoberturaXMLProvider;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedLongArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.EfficientCompressedIntegerTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs.EfficientCompressedLongTrace;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author SimHigh
 *
 */
public class SpectraFileUtilsTest extends TestSettings {

	/**
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
	}

	/**
	 */
	@AfterClass
	public static void tearDownAfterClass() {
//		deleteTestOutputs();
	}

	/**
	 */
	@Before
	public void setUp() {
//		Log.off();
	}

	/**
	 */
	@After
	public void tearDown() {
//		deleteTestOutputs();
	}

	@Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
	
	@Rule
	public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();

	private int[] s(int... numbers) {
		return numbers;
	}
	
	private int[][] rt(int... numbers) {
		int[][] result = new int[numbers.length][];
		for (int i = 0; i < numbers.length; ++i) {
			result[i] = new int[] {0,numbers[i],0};
		}
		return result;
	}
	
	private EfficientCompressedLongTrace asList(Path outputDir, int[][] rt) {
		BufferedLongArrayQueue list = new BufferedLongArrayQueue(
				outputDir.toFile(), String.valueOf(UUID.randomUUID()), rt.length);
		for (int[] statement : rt) {
			list.add(CoberturaStatementEncoding.generateUniqueRepresentationForStatement(statement[0], statement[1], statement[2]));
		}
		return new EfficientCompressedLongTrace(list, true);
	}
	
	private EfficientCompressedIntegerTrace c(Path outputDir, int... numbers) {
		BufferedIntArrayQueue list = new BufferedIntArrayQueue(
				outputDir.toFile(), String.valueOf(UUID.randomUUID()), numbers.length);
		for (int id : numbers) {
			list.add(id);
		}
		return new EfficientCompressedIntegerTrace(list, true);
	}
	
	/**
	 */
	@Test
	public void testSpectraReadingAndWriting() {
		final CoberturaXMLProvider<HitTrace<SourceCodeBlock>> c = CoberturaSpectraProviderFactory.getHitSpectraFromXMLProvider(true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large", true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/simple-coverage.xml", "simple", false);
        final ISpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> s = c.loadSpectra();
		
		Path output1 = Paths.get(getStdTestDir(), "spectra.zip");
		SpectraFileUtils.saveSpectraToZipFile(s, output1, true, false);
		assertTrue(output1.toFile().exists());
		Log.out(this, "saved...");
		
		ISpectra<String, ?> spectra = SpectraFileUtils.loadStringSpectraFromZipFile(output1);
		Log.out(this, "loaded...");
		assertEquals(s.getTraces().size(), spectra.getTraces().size());
		assertEquals(s.getFailingTraces().size(), spectra.getFailingTraces().size());
		assertEquals(s.getNodes().size(), spectra.getNodes().size());
		
		Path output2 = Paths.get(getStdTestDir(), "spectra2.zip");
		SpectraFileUtils.saveSpectraToZipFile(spectra, output2, true, false);
		assertTrue(output2.toFile().exists());
		Log.out(this, "saved...");
		ISpectra<String, ?> spectra2 = SpectraFileUtils.loadStringSpectraFromZipFile(output2);
		Log.out(this, "loaded...");
		
		assertEquals(spectra, spectra2);
	}
	
	/**
	 * @throws ZipException 
	 */
	@Test
	public void testBlockSpectraReadingAndWriting() throws ZipException {
		final CoberturaXMLProvider<HitTrace<SourceCodeBlock>> c = CoberturaSpectraProviderFactory.getHitSpectraFromXMLProvider(true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large", true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large2", true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/simple-coverage.xml", "simple", false);
        ISpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> spectra = c.loadSpectra();
        
        Collection<? extends ITrace<SourceCodeBlock>> failingTraces = spectra.getFailingTraces();
        assertNotNull(failingTraces);
		assertEquals(1, failingTraces.size());
        ITrace<SourceCodeBlock> trace = spectra.getTrace("simple");
        assertNotNull(trace);
        assertFalse(trace.isSuccessful());
        
        Path outputDir = Paths.get(getStdTestDir());
        
        // mapping: tree indexer sequence ID -> sequence of sub trace IDs
    	int[][] subTraceIdSequences = new int[][] {s(1,2,3)};
    	
    	// mapping: sub trace ID -> sequence of spectra node IDs
    	EfficientCompressedIntegerTrace[] nodeIdSequences = 
    			new EfficientCompressedIntegerTrace[] {
    					c(outputDir), c(outputDir, 1,2,3), c(outputDir, 4,5,6), c(outputDir, 7,8,9)};
    	
        // sub trace id array
    	int[] rawTrace = new int[] {1,2,3,1,2,3};
        
        RawIntTraceCollector traceCollector = new RawIntTraceCollector(outputDir);
        
        // sub trace id -> sub trace
        Map<Integer, EfficientCompressedLongTrace> idToSubTraceMap = new HashMap<>();
        idToSubTraceMap.put(1,asList(outputDir, rt(5,6,7)));
        idToSubTraceMap.put(2,asList(outputDir, rt(8,9,10)));
        idToSubTraceMap.put(3,asList(outputDir, rt(11,12,13)));
        
        traceCollector.addRawTraceToPool(trace.getIndex(), 0, rawTrace, false, outputDir, "t1", idToSubTraceMap);
        traceCollector.getIndexer().getSequences();
        
//        System.out.println(traceCollector.getGsTree());
        
        List<ExecutionTrace> executionTraces = traceCollector.calculateExecutionTraces(trace.getIndex(), false);
        for (ExecutionTrace eTrace : executionTraces) {
        	System.out.println(eTrace.getCompressedTrace());
        	trace.addExecutionTrace(eTrace);
        }
        assertFalse(trace.getExecutionTraces().isEmpty());
        
        spectra.setIndexer(new SimpleIntIndexerCompressed(subTraceIdSequences, nodeIdSequences));
        
        int[] trace1 = executionTraces.get(0).reconstructFullMappedTrace(spectra.getIndexer());
        
        Log.out(this, Arrays.toString(trace1));
        assertEquals(18, trace1.length);

        assertArrayEquals(s(1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9), trace1);
        
//        try {
//			traceCollector.finalize();
//		} catch (Throwable e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		Path output1 = Paths.get(getStdTestDir(), "spectra_block.zip");
		SpectraFileUtils.saveSpectraToZipFile(spectra, output1, true, false, true);
		Log.out(this, "saved...");
		
		ISpectra<SourceCodeBlock, ?> spectra2 = SpectraFileUtils.loadBlockSpectraFromZipFile(output1);
		Log.out(this, "loaded...");
		assertNotNull(spectra2.getIndexer());
		failingTraces = spectra2.getFailingTraces();
        assertNotNull(failingTraces);
		assertEquals(1, failingTraces.size());
        trace = spectra2.getTrace("simple");
        assertNotNull(trace);
        assertFalse(trace.isSuccessful());
        // check the correct execution trace
        assertFalse(trace.getExecutionTraces().isEmpty());
        ExecutionTrace executionTrace1 = trace.getExecutionTraces().iterator().next();
        
//        for (int i = 0; i < spectra2.getIndexer().getSequences().length; i++) {
//        	Log.out(this, Arrays.toString(spectra2.getIndexer().getSequences()[i]));
//		}
        
        trace1 = executionTrace1.reconstructFullMappedTrace(spectra2.getIndexer());
        assertEquals(18, trace1.length);

        assertArrayEquals(s(1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9), trace1);
        
        assertEquals(spectra, spectra2);
		
		Path output2 = Paths.get(getStdTestDir(), "spectra2_block.zip");
		SpectraFileUtils.saveSpectraToZipFile(spectra2, output2, true, false, true);
		Log.out(this, "saved indexed...");
		ISpectra<SourceCodeBlock, ?> spectra3 = SpectraFileUtils.loadBlockSpectraFromZipFile(output2);
		Log.out(this, "loaded...");
		assertEquals(spectra2, spectra3);
		 
		Path output3 = Paths.get(getStdTestDir(), "spectra3_block.zip");
		SpectraFileUtils.saveSpectraToZipFile(spectra2, output3, true, false, false);
		Log.out(this, "saved non-indexed...");
		ISpectra<SourceCodeBlock, ?> spectra4 = SpectraFileUtils.loadBlockSpectraFromZipFile(output3);
		Log.out(this, "loaded...");
		assertEquals(spectra2, spectra4);
		
		assertTrue(output1.toFile().exists());
		assertTrue(output2.toFile().exists());
		assertEquals(output1.toFile().length(), output2.toFile().length());
		assertTrue(output3.toFile().exists());
		assertTrue(output3.toFile().length() > output2.toFile().length());
	}
	
	@Test
	public void testBlockSpectraReadingAndWriting2() throws ZipException {
		Path output1 = Paths.get(getStdResourcesDir(), "Chart-22b.zip");
		ISpectra<SourceCodeBlock, ?> spectra2 = SpectraFileUtils.loadBlockSpectraFromZipFile(output1);
		Log.out(this, "loaded...");
		assertNotNull(spectra2.getIndexer());
		Collection<? extends ITrace<SourceCodeBlock>> failingTraces = spectra2.getFailingTraces();
        assertNotNull(failingTraces);
		assertEquals(6, failingTraces.size());
//        trace = spectra2.getTrace("simple");
//        assertNotNull(trace);
//        assertFalse(trace.isSuccessful());
//        // check the correct execution trace
//        assertFalse(trace.getExecutionTraces().isEmpty());
//        ExecutionTrace executionTrace1 = trace.getExecutionTraces().iterator().next();
//        
////        for (int i = 0; i < spectra2.getIndexer().getSequences().length; i++) {
////        	Log.out(this, Arrays.toString(spectra2.getIndexer().getSequences()[i]));
////		}
//        
//        trace1 = executionTrace1.reconstructFullMappedTrace(spectra2.getIndexer());
//        assertEquals(18, trace1.length);
//
//        assertArrayEquals(s(1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9), trace1);
//        
//        assertEquals(spectra, spectra2);
//		
		Path output2 = Paths.get(getStdTestDir(), "Chart22_block.zip");
		SpectraFileUtils.saveSpectraToZipFile(spectra2, output2, true, false, true);
		Log.out(this, "saved indexed...");
		ISpectra<SourceCodeBlock, ?> spectra3 = SpectraFileUtils.loadBlockSpectraFromZipFile(output2);
		Log.out(this, "loaded...");
		assertEquals(spectra2, spectra3);
//		 
//		Path output3 = Paths.get(getStdTestDir(), "spectra3_block.zip");
//		SpectraFileUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra2, output3, true, false, false);
//		Log.out(this, "saved non-indexed...");
//		ISpectra<SourceCodeBlock, ?> spectra4 = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, output3);
//		Log.out(this, "loaded...");
//		assertEquals(spectra2, spectra4);
//		
//		assertTrue(output1.toFile().exists());
//		assertTrue(output2.toFile().exists());
//		assertEquals(output1.toFile().length(), output2.toFile().length());
//		assertTrue(output3.toFile().exists());
//		assertTrue(output3.toFile().length() > output2.toFile().length());
	}

	/**
	 */
	@Test
	public void testBlockCountSpectraReadingAndWriting() {
		final CoberturaCountXMLProvider<CountTrace<SourceCodeBlock>> c = CoberturaSpectraProviderFactory.getCountSpectraFromXMLProvider(true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large", true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large2", true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/simple-coverage.xml", "simple", false);
        ISpectra<SourceCodeBlock, ? super CountTrace<SourceCodeBlock>> spectra = c.loadSpectra();
        
        Collection<? super CountTrace<SourceCodeBlock>> failingTraces = spectra.getFailingTraces();
        assertNotNull(failingTraces);
		assertEquals(1, failingTraces.size());
        ITrace<SourceCodeBlock> trace = spectra.getTrace("simple");
        assertNotNull(trace);
        assertFalse(trace.isSuccessful());
        assertEquals(2, trace.getInvolvedNodes().size());
		
		Path output1 = Paths.get(getStdTestDir(), "count_spectra_block.zip");
		SpectraFileUtils.saveSpectraToZipFile(spectra, output1, true, false, true);
		Log.out(this, "saved...");
		
		CountSpectra<SourceCodeBlock> spectra2 = SpectraFileUtils.loadBlockCountSpectraFromZipFile(output1);
		Log.out(this, "loaded...");
		failingTraces = spectra2.getFailingTraces();
        assertNotNull(failingTraces);
		assertEquals(1, failingTraces.size());
        trace = spectra2.getTrace("simple");
        assertNotNull(trace);
        assertFalse(trace.isSuccessful());
        
        assertEquals(spectra, spectra2);
		
		Path output2 = Paths.get(getStdTestDir(), "count_spectra2_block.zip");
		SpectraFileUtils.saveSpectraToZipFile(spectra2, output2, true, false, true);
		Log.out(this, "saved indexed...");
		ISpectra<SourceCodeBlock, ?> spectra3 = SpectraFileUtils.loadBlockSpectraFromZipFile(output2);
		Log.out(this, "loaded...");
		assertEquals(spectra2, spectra3);
		 
		Path output3 = Paths.get(getStdTestDir(), "count_spectra3_block.zip");
		SpectraFileUtils.saveSpectraToZipFile(spectra2, output3, true, false, false);
		Log.out(this, "saved non-indexed...");
		ISpectra<SourceCodeBlock, ?> spectra4 = SpectraFileUtils.loadBlockSpectraFromZipFile(output3);
		Log.out(this, "loaded...");
		assertEquals(spectra2, spectra4);
		
		assertTrue(output1.toFile().exists());
		assertTrue(output2.toFile().exists());
		assertEquals(output1.toFile().length(), output2.toFile().length());
		assertTrue(output3.toFile().exists());
		assertTrue(output3.toFile().length() > output2.toFile().length());
	}
	
	/**
	 */
	@Test
	public void testBlockSpectraCsvWriting() {
        ISpectra<SourceCodeBlock, ?> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(Paths.get(getStdResourcesDir(), "spectra.zip"));
        
		Path output1 = Paths.get(getStdTestDir(), "spectra_block.csv");
		SpectraFileUtils.saveSpectraToCsvFile(spectra, output1, false, true);
		Log.out(this, "saved...");
		
		Path output2 = Paths.get(getStdTestDir(), "spectra2_block.csv");
		SpectraFileUtils.saveSpectraToCsvFile(spectra, output2, true, true);
		Log.out(this, "saved...");
		
		assertTrue(output1.toFile().exists());
		assertTrue(output2.toFile().exists());
	}
	
	/**
	 */
	@Test
	public void testSparseBlockSpectraReadingAndWriting() {
		final CoberturaXMLProvider<HitTrace<SourceCodeBlock>> c = CoberturaSpectraProviderFactory.getHitSpectraFromXMLProvider(true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large", true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large2", true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/simple-coverage.xml", "simple", false);

        ISpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> spectra = c.loadSpectra();
        
        Collection<? extends ITrace<SourceCodeBlock>> failingTraces = spectra.getFailingTraces();
        assertNotNull(failingTraces);
		assertEquals(1, failingTraces.size());
        ITrace<SourceCodeBlock> trace = spectra.getTrace("simple");
        assertNotNull(trace);
        assertFalse(trace.isSuccessful());
		
		Path output1 = Paths.get(getStdTestDir(), "spectra_block_sp.zip");
		SpectraFileUtils.saveSpectraToZipFile(spectra, output1, true, true, true);
		Log.out(this, "saved...");
		
		ISpectra<SourceCodeBlock, ?> spectra2 = SpectraFileUtils.loadBlockSpectraFromZipFile(output1);
		Log.out(this, "loaded...");
		failingTraces = spectra2.getFailingTraces();
        assertNotNull(failingTraces);
		assertEquals(1, failingTraces.size());
        trace = spectra2.getTrace("simple");
        assertNotNull(trace);
        assertFalse(trace.isSuccessful());
        
        assertEquals(spectra, spectra2);
		
		Path output2 = Paths.get(getStdTestDir(), "spectra2_block_sp.zip");
		SpectraFileUtils.saveSpectraToZipFile(spectra2, output2, true, true, true);
		Log.out(this, "saved indexed...");
		ISpectra<SourceCodeBlock, ?> spectra3 = SpectraFileUtils.loadBlockSpectraFromZipFile(output2);
		Log.out(this, "loaded...");
		assertEquals(spectra2, spectra3);
		
		Path output3 = Paths.get(getStdTestDir(), "spectra3_block_sp.zip");
		SpectraFileUtils.saveSpectraToZipFile(spectra2, output3, true, false, true);
		Log.out(this, "saved non-indexed...");
		ISpectra<SourceCodeBlock, ?> spectra4 = SpectraFileUtils.loadBlockSpectraFromZipFile(output3);
		Log.out(this, "loaded...");
		assertEquals(spectra2, spectra4);
		
		assertTrue(output1.toFile().exists());
		assertTrue(output2.toFile().exists());
		assertEquals(output1.toFile().length(), output2.toFile().length());
		assertTrue(output3.toFile().exists());
		assertTrue(output3.toFile().length() <= output2.toFile().length());
	}
	
	//TODO:doesn't seem to work for some kind of reasons... dunno why
	/**
	 * @throws IOException if
	 */
//	@Test
	public void testBugMinerSpectraReadingAndWriting() throws IOException {	
		Path spectraZipFile = Paths.get(getStdResourcesDir(), "28919-traces-compressed.zip");
		ISpectra<SourceCodeBlock, HitTrace<SourceCodeBlock>> spectra = SpectraFileUtils.loadSpectraFromBugMinerZipFile2(spectraZipFile);

		Log.out(this, "loaded...");
		Path output1 = Paths.get(getStdTestDir(), "spectra.zip");
		SpectraFileUtils.saveSpectraToZipFile(spectra, output1, true, true);

		Log.out(this, "saved...");
		assertTrue(output1.toFile().exists());
	}
}
