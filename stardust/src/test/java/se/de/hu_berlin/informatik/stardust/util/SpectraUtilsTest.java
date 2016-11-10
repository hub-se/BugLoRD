/**
 * 
 */
package se.de.hu_berlin.informatik.stardust.util;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;

import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
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
		Log.off();
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
	 */
	@Test
	public void testSpectraReadingAndWriting() {
		Path spectraZipFile = Paths.get(getStdResourcesDir(), "spectraCompressed.zip");
		ISpectra<String> spectra = SpectraUtils.loadSpectraFromZipFile(spectraZipFile);
		
		Log.out(this, "loaded...");
		Path output1 = Paths.get(getStdTestDir(), "spectra.zip");
		SpectraUtils.saveSpectraToZipFile(spectra, output1, true);
		
		Log.out(this, "saved...");
		spectra = SpectraUtils.loadSpectraFromZipFile(output1);
		
		Log.out(this, "loaded...");
		Path output2 = Paths.get(getStdTestDir(), "spectra2.zip");
		SpectraUtils.saveSpectraToZipFile(spectra, output2, true);
		Log.out(this, "saved...");
		
		assertTrue(output1.toFile().exists());
		assertTrue(output2.toFile().exists());
		assertTrue(output1.toFile().length() == output2.toFile().length());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.stardust.util.SpectraUtils.
	 * @throws IOException 
	 */
	@Test
	public void testBugMinerSpectraReadingAndWriting() throws IOException {	
		Path spectraZipFile = Paths.get(getStdResourcesDir(), "28919-traces-compressed.zip");
		ISpectra<String> spectra = SpectraUtils.loadSpectraFromBugMinerZipFile(spectraZipFile);

		Log.out(this, "loaded...");
		Path output1 = Paths.get(getStdTestDir(), "spectra.zip");
		SpectraUtils.saveSpectraToZipFile(spectra, output1, true);

		Log.out(this, "saved...");
		assertTrue(output1.toFile().exists());
	}
}
