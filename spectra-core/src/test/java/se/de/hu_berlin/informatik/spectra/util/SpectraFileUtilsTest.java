/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

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
import se.de.hu_berlin.informatik.spectra.core.traces.RawTraceCollector;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.CoberturaSpectraProviderFactory;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.xml.CoberturaCountXMLProvider;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.xml.CoberturaXMLProvider;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.SingleLinkedQueue;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author SimHigh
 *
 */
public class SpectraFileUtilsTest extends TestSettings {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
//		deleteTestOutputs();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
//		Log.off();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
//		deleteTestOutputs();
	}

	@Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
	
	@Rule
	public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.stardust.util.SpectraUtils.
	 * @throws Exception
	 * if a trace can't be added
	 */
	@Test
	public void testSpectraReadingAndWriting() throws Exception {
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
	 * Test method for {@link se.de.hu_berlin.informatik.stardust.util.SpectraUtils.
     * @throws Exception
	 * if a trace can't be added
	 */
	@Test
	public void testBlockSpectraReadingAndWriting() throws Exception {
		final CoberturaXMLProvider<HitTrace<SourceCodeBlock>> c = CoberturaSpectraProviderFactory.getHitSpectraFromXMLProvider(true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large", true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large2", true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/simple-coverage.xml", "simple", false);
        ISpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> spectra = c.loadSpectra();
        
        Collection<? extends ITrace<SourceCodeBlock>> failingTraces = spectra.getFailingTraces();
        assertNotNull(failingTraces);
        assertTrue(failingTraces.size() == 1);
        ITrace<SourceCodeBlock> trace = spectra.getTrace("simple");
        assertNotNull(trace);
        assertFalse(trace.isSuccessful());
        
        SingleLinkedQueue<Integer> rawTrace = new SingleLinkedQueue<>();
        rawTrace.add(0);
        rawTrace.add(1);
        rawTrace.add(2);
        rawTrace.add(12);
        rawTrace.add(0);
        rawTrace.add(1);
        
        RawTraceCollector traceCollector = new RawTraceCollector(Paths.get(getStdTestDir()));
        
        traceCollector.addRawTraceToPool(trace.getIndex(), 0, rawTrace, false);
        List<ExecutionTrace> executionTraces = traceCollector.getExecutionTraces(trace.getIndex(), false);
        for (ExecutionTrace eTrace : executionTraces) {
        	trace.addExecutionTrace(eTrace);
        }
        
        spectra.setIndexer(traceCollector.getIndexer());
        
        try {
			traceCollector.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Path output1 = Paths.get(getStdTestDir(), "spectra_block.zip");
		SpectraFileUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra, output1, true, false, true);
		Log.out(this, "saved...");
		
		ISpectra<SourceCodeBlock, ?> spectra2 = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, output1);
		Log.out(this, "loaded...");
		failingTraces = spectra2.getFailingTraces();
        assertNotNull(failingTraces);
        assertTrue(failingTraces.size() == 1);
        trace = spectra2.getTrace("simple");
        assertNotNull(trace);
        assertFalse(trace.isSuccessful());
        // check the correct execution trace
        assertFalse(trace.getExecutionTraces().isEmpty());
        ExecutionTrace executionTrace1 = trace.getExecutionTraces().iterator().next();
        
//        for (int i = 0; i < spectra2.getIndexer().getSequences().length; i++) {
//        	Log.out(this, Arrays.toString(spectra2.getIndexer().getSequences()[i]));
//		}
        
        int[] trace1 = executionTrace1.reconstructFullMappedTrace(spectra2.getIndexer());
        assertEquals(6, trace1.length);

        assertEquals(0, trace1[0]);
        assertEquals(1, trace1[1]);
        assertEquals(2, trace1[2]);
        assertEquals(12, trace1[3]);
        assertEquals(0, trace1[4]);
        assertEquals(1, trace1[5]);
        
        assertEquals(spectra, spectra2);
		
		Path output2 = Paths.get(getStdTestDir(), "spectra2_block.zip");
		SpectraFileUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra2, output2, true, false, true);
		Log.out(this, "saved indexed...");
		ISpectra<SourceCodeBlock, ?> spectra3 = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, output2);
		Log.out(this, "loaded...");
		assertEquals(spectra2, spectra3);
		 
		Path output3 = Paths.get(getStdTestDir(), "spectra3_block.zip");
		SpectraFileUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra2, output3, true, false, false);
		Log.out(this, "saved non-indexed...");
		ISpectra<SourceCodeBlock, ?> spectra4 = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, output3);
		Log.out(this, "loaded...");
		assertEquals(spectra2, spectra4);
		
		assertTrue(output1.toFile().exists());
		assertTrue(output2.toFile().exists());
		assertTrue(output1.toFile().length() == output2.toFile().length());
		assertTrue(output3.toFile().exists());
		assertTrue(output3.toFile().length() > output2.toFile().length());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.stardust.util.SpectraUtils.
     * @throws Exception
	 * if a trace can't be added
	 */
	@Test
	public void testBlockCountSpectraReadingAndWriting() throws Exception {
		final CoberturaCountXMLProvider<CountTrace<SourceCodeBlock>> c = CoberturaSpectraProviderFactory.getCountSpectraFromXMLProvider(true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large", true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large2", true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/simple-coverage.xml", "simple", false);
        ISpectra<SourceCodeBlock, ? super CountTrace<SourceCodeBlock>> spectra = c.loadSpectra();
        
        Collection<? super CountTrace<SourceCodeBlock>> failingTraces = spectra.getFailingTraces();
        assertNotNull(failingTraces);
        assertTrue(failingTraces.size() == 1);
        ITrace<SourceCodeBlock> trace = spectra.getTrace("simple");
        assertNotNull(trace);
        assertFalse(trace.isSuccessful());
        assertEquals(2, trace.getInvolvedNodes().size());
		
		Path output1 = Paths.get(getStdTestDir(), "count_spectra_block.zip");
		SpectraFileUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra, output1, true, false, true);
		Log.out(this, "saved...");
		
		CountSpectra<SourceCodeBlock> spectra2 = SpectraFileUtils.loadCountSpectraFromZipFile(SourceCodeBlock.DUMMY, output1);
		Log.out(this, "loaded...");
		failingTraces = spectra2.getFailingTraces();
        assertNotNull(failingTraces);
        assertTrue(failingTraces.size() == 1);
        trace = spectra2.getTrace("simple");
        assertNotNull(trace);
        assertFalse(trace.isSuccessful());
        
        assertEquals(spectra, spectra2);
		
		Path output2 = Paths.get(getStdTestDir(), "count_spectra2_block.zip");
		SpectraFileUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra2, output2, true, false, true);
		Log.out(this, "saved indexed...");
		ISpectra<SourceCodeBlock, ?> spectra3 = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, output2);
		Log.out(this, "loaded...");
		assertEquals(spectra2, spectra3);
		 
		Path output3 = Paths.get(getStdTestDir(), "count_spectra3_block.zip");
		SpectraFileUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra2, output3, true, false, false);
		Log.out(this, "saved non-indexed...");
		ISpectra<SourceCodeBlock, ?> spectra4 = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, output3);
		Log.out(this, "loaded...");
		assertEquals(spectra2, spectra4);
		
		assertTrue(output1.toFile().exists());
		assertTrue(output2.toFile().exists());
		assertTrue(output1.toFile().length() == output2.toFile().length());
		assertTrue(output3.toFile().exists());
		assertTrue(output3.toFile().length() > output2.toFile().length());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.stardust.util.SpectraUtils.
     * @throws Exception
	 * if a trace can't be added
	 */
	@Test
	public void testBlockSpectraCsvWriting() throws Exception {
        ISpectra<SourceCodeBlock, ?> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(Paths.get(getStdResourcesDir(), "spectra.zip"));
        
		Path output1 = Paths.get(getStdTestDir(), "spectra_block.csv");
		SpectraFileUtils.saveSpectraToCsvFile(SourceCodeBlock.DUMMY, spectra, output1, false, true);
		Log.out(this, "saved...");
		
		Path output2 = Paths.get(getStdTestDir(), "spectra2_block.csv");
		SpectraFileUtils.saveSpectraToCsvFile(SourceCodeBlock.DUMMY, spectra, output2, true, true);
		Log.out(this, "saved...");
		
		assertTrue(output1.toFile().exists());
		assertTrue(output2.toFile().exists());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.stardust.util.SpectraUtils.
     * @throws Exception
	 * if a trace can't be added
	 */
	@Test
	public void testSparseBlockSpectraReadingAndWriting() throws Exception {
		final CoberturaXMLProvider<HitTrace<SourceCodeBlock>> c = CoberturaSpectraProviderFactory.getHitSpectraFromXMLProvider(true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large", true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large2", true);
        c.addData(getStdResourcesDir() + "/fk/stardust/provider/simple-coverage.xml", "simple", false);

        ISpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> spectra = c.loadSpectra();
        
        Collection<? extends ITrace<SourceCodeBlock>> failingTraces = spectra.getFailingTraces();
        assertNotNull(failingTraces);
        assertTrue(failingTraces.size() == 1);
        ITrace<SourceCodeBlock> trace = spectra.getTrace("simple");
        assertNotNull(trace);
        assertFalse(trace.isSuccessful());
		
		Path output1 = Paths.get(getStdTestDir(), "spectra_block_sp.zip");
		SpectraFileUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra, output1, true, true, true);
		Log.out(this, "saved...");
		
		ISpectra<SourceCodeBlock, ?> spectra2 = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, output1);
		Log.out(this, "loaded...");
		failingTraces = spectra2.getFailingTraces();
        assertNotNull(failingTraces);
        assertTrue(failingTraces.size() == 1);
        trace = spectra2.getTrace("simple");
        assertNotNull(trace);
        assertFalse(trace.isSuccessful());
        
        assertEquals(spectra, spectra2);
		
		Path output2 = Paths.get(getStdTestDir(), "spectra2_block_sp.zip");
		SpectraFileUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra2, output2, true, true, true);
		Log.out(this, "saved indexed...");
		ISpectra<SourceCodeBlock, ?> spectra3 = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, output2);
		Log.out(this, "loaded...");
		assertEquals(spectra2, spectra3);
		
		Path output3 = Paths.get(getStdTestDir(), "spectra3_block_sp.zip");
		SpectraFileUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra2, output3, true, false, true);
		Log.out(this, "saved non-indexed...");
		ISpectra<SourceCodeBlock, ?> spectra4 = SpectraFileUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, output3);
		Log.out(this, "loaded...");
		assertEquals(spectra2, spectra4);
		
		assertTrue(output1.toFile().exists());
		assertTrue(output2.toFile().exists());
		assertTrue(output1.toFile().length() == output2.toFile().length());
		assertTrue(output3.toFile().exists());
		assertTrue(output3.toFile().length() <= output2.toFile().length());
	}
	
	//TODO:doesn't seem to work for some kind of reasons... dunno why
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.stardust.util.SpectraUtils.
	 * 
	 * @throws IOException 
	 */
	@Test
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
