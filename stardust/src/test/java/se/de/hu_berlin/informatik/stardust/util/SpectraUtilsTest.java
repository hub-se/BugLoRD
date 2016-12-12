/**
 * 
 */
package se.de.hu_berlin.informatik.stardust.util;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.provider.CoberturaProvider;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author SimHigh
 *
 */
public class SpectraUtilsTest extends TestSettings {

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
		deleteTestOutputs();
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
		deleteTestOutputs();
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
		final CoberturaProvider c = new CoberturaProvider();
        c.addTraceFile(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large", true);
        c.addTraceFile(getStdResourcesDir() + "/fk/stardust/provider/simple-coverage.xml", "simple", false);
        final ISpectra<SourceCodeBlock> s = c.loadSpectra();
		
		Path output1 = Paths.get(getStdTestDir(), "spectra.zip");
		SpectraUtils.saveSpectraToZipFile(s, output1, true, false);
		Log.out(this, "saved...");
		
		ISpectra<String> spectra = SpectraUtils.loadStringSpectraFromZipFile(output1);
		Log.out(this, "loaded...");
		
		Path output2 = Paths.get(getStdTestDir(), "spectra2.zip");
		SpectraUtils.saveSpectraToZipFile(spectra, output2, true, false);
		Log.out(this, "saved...");
		
		assertTrue(output1.toFile().exists());
		assertTrue(output2.toFile().exists());
		assertTrue(output1.toFile().length() != output2.toFile().length());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.stardust.util.SpectraUtils.
     * @throws Exception
	 * if a trace can't be added
	 */
	@Test
	public void testBlockSpectraReadingAndWriting() throws Exception {
		final CoberturaProvider c = new CoberturaProvider();
        c.addTraceFile(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large", true);
        c.addTraceFile(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large2", true);
        c.addTraceFile(getStdResourcesDir() + "/fk/stardust/provider/simple-coverage.xml", "simple", false);
        ISpectra<SourceCodeBlock> spectra = c.loadSpectra();
        
        List<ITrace<SourceCodeBlock>> failingTraces = spectra.getFailingTraces();
        assertNotNull(failingTraces);
        assertTrue(failingTraces.size() == 1);
        assertEquals("simple", failingTraces.get(0).getIdentifier());
		
		Path output1 = Paths.get(getStdTestDir(), "spectra_block.zip");
		SpectraUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra, output1, true, false, true);
		Log.out(this, "saved...");
		
		spectra = SpectraUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, output1);
		Log.out(this, "loaded...");
		failingTraces = spectra.getFailingTraces();
        assertNotNull(failingTraces);
        assertTrue(failingTraces.size() == 1);
        assertEquals("simple", failingTraces.get(0).getIdentifier());
		
		Path output2 = Paths.get(getStdTestDir(), "spectra2_block.zip");
		SpectraUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra, output2, true, false, true);
		Log.out(this, "saved indexed...");
		Path output3 = Paths.get(getStdTestDir(), "spectra3_block.zip");
		SpectraUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra, output3, true, false, false);
		Log.out(this, "saved non-indexed...");
		
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
	public void testSparseBlockSpectraReadingAndWriting() throws Exception {
		final CoberturaProvider c = new CoberturaProvider();
        c.addTraceFile(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large", true);
        c.addTraceFile(getStdResourcesDir() + "/fk/stardust/provider/large-coverage.xml", "large2", true);
        c.addTraceFile(getStdResourcesDir() + "/fk/stardust/provider/simple-coverage.xml", "simple", false);

        ISpectra<SourceCodeBlock> spectra = c.loadSpectra();
        
        List<ITrace<SourceCodeBlock>> failingTraces = spectra.getFailingTraces();
        assertNotNull(failingTraces);
        assertTrue(failingTraces.size() == 1);
        assertEquals("simple", failingTraces.get(0).getIdentifier());
		
		Path output1 = Paths.get(getStdTestDir(), "spectra_block.zip");
		SpectraUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra, output1, true, true, true);
		Log.out(this, "saved...");
		
		spectra = SpectraUtils.loadSpectraFromZipFile(SourceCodeBlock.DUMMY, output1);
		Log.out(this, "loaded...");
		failingTraces = spectra.getFailingTraces();
        assertNotNull(failingTraces);
        assertTrue(failingTraces.size() == 1);
        assertEquals("simple", failingTraces.get(0).getIdentifier());
		
		Path output2 = Paths.get(getStdTestDir(), "spectra2_block.zip");
		SpectraUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra, output2, true, true, true);
		Log.out(this, "saved indexed...");
		Path output3 = Paths.get(getStdTestDir(), "spectra3_block.zip");
		SpectraUtils.saveSpectraToZipFile(SourceCodeBlock.DUMMY, spectra, output3, true, false, true);
		Log.out(this, "saved non-indexed...");
		
		assertTrue(output1.toFile().exists());
		assertTrue(output2.toFile().exists());
		assertTrue(output1.toFile().length() == output2.toFile().length());
		assertTrue(output3.toFile().exists());
		assertTrue(output3.toFile().length() != output2.toFile().length());
	}
	
	//TODO:doesn't seem to work for some kind of reasons... dunno why
//	/**
//	 * Test method for {@link se.de.hu_berlin.informatik.stardust.util.SpectraUtils.
//	 * 
//	 * @throws IOException 
//	 */
//	@Test
//	public void testBugMinerSpectraReadingAndWriting() throws IOException {	
//		Path spectraZipFile = Paths.get(getStdResourcesDir(), "28919-traces-compressed.zip");
//		ISpectra<SourceCodeBlock> spectra = SpectraUtils.loadSpectraFromBugMinerZipFile2(spectraZipFile);
//
//		Log.out(this, "loaded...");
//		Path output1 = Paths.get(getStdTestDir(), "spectra.zip");
//		SpectraUtils.saveSpectraToZipFile(spectra, output1, true);
//
//		Log.out(this, "saved...");
//		assertTrue(output1.toFile().exists());
//	}
}
