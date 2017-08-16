/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;

import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.JaCoCoToSpectra;
import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.JaCoCoToSpectra.CmdOptions;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class JaCoCoToSpectraTest extends TestSettings {

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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		FileUtils.delete(Paths.get(extraTestOutput));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		FileUtils.delete(Paths.get(extraTestOutput));
	}
	
	@Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private static String extraTestOutput = "target" + File.separator + "testoutputExtra";
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.JaCoCoToSpectra#main(java.lang.String[])}.
	 */
	@Test
	public void testMainRankingGeneration() {
		final String[] args = {
				CmdOptions.PROJECT_DIR.asArg(), ".", 
//				CmdOptions.CLASS_PATH.asArg(), getStdResourcesDir() + File.separator + "lib" + File.separator + "junit-4.11.jar",
				CmdOptions.SOURCE_DIR.asArg(), getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src", 
				CmdOptions.TEST_CLASS_DIR.asArg(), getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin",
				CmdOptions.TEST_LIST.asArg(), getStdResourcesDir() + File.separator + "all_testsSimple.txt",
				CmdOptions.INSTRUMENT_CLASSES.asArg(), getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin",
				CmdOptions.OUTPUT.asArg(),  extraTestOutput + File.separator + "report",
				CmdOptions.AGENT_PORT.asArg(), "8000"};
		JaCoCoToSpectra.main(args);
		assertTrue(Files.exists(Paths.get(extraTestOutput, "report", "spectraCompressed.zip")));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "report", "ranking.trc")));
	}
	
//	/**
//	 * Test method for {@link se.de.hu_berlin.informatik.c2r.JaCoCoToSpectra#main(java.lang.String[])}.
//	 */
////	@Test
//	public void testMainRankingGenerationSeparateJVM() {
//		final String[] args = {
//				CmdOptions.PROJECT_DIR.asArg(), ".", 
//				CmdOptions.SOURCE_DIR.asArg(), getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src", 
//				CmdOptions.TEST_CLASS_DIR.asArg(), getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin",
//				CmdOptions.TEST_LIST.asArg(), getStdResourcesDir() + File.separator + "all_testsSimple.txt",
//				CmdOptions.SEPARATE_JVM.asArg(),
//				CmdOptions.INSTRUMENT_CLASSES.asArg(), getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin",
//				CmdOptions.OUTPUT.asArg(),  extraTestOutput + File.separator + "report2"};
//		JaCoCoToSpectra.main(args);
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "report2", "spectraCompressed.zip")));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "report2", "ranking.trc")));
//	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.JaCoCoToSpectra#generateRankingForDefects4JElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateRankingForDefects4JElement() {
		JaCoCoToSpectra.generateRankingForDefects4JElement(null, ".",
				getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src", 
				getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin", 
				null, 
				getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin", 
				getStdResourcesDir() + File.separator + "testclassesSimple.txt", 
				extraTestOutput + File.separator + "reportTestClass",
				8001, null, 2, false, false);

		Path spectraZipFile = Paths.get(extraTestOutput, "reportTestClass", "spectraCompressed.zip");
		assertTrue(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportTestClass", "ranking.trc")));
		
		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
		assertFalse(spectra.getTraces().isEmpty());
		assertEquals(spectra.getTraces().size()-2, spectra.getSuccessfulTraces().size());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.JaCoCoToSpectra#generateRankingForDefects4JElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateRankingForDefects4JElementFullSpectra() {
		JaCoCoToSpectra.generateRankingForDefects4JElement(null, ".",
				getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src", 
				getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin", 
				null, 
				getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin", 
				getStdResourcesDir() + File.separator + "testclassesSimple.txt", 
				extraTestOutput + File.separator + "reportTestClass2",
				8301, null, null, true, false);

		Path spectraZipFile = Paths.get(extraTestOutput, "reportTestClass2", "spectraCompressed.zip");
		assertTrue(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportTestClass2", "ranking.trc")));
		
		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
		assertFalse(spectra.getTraces().isEmpty());
		assertEquals(spectra.getTraces().size()-2, spectra.getSuccessfulTraces().size());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.JaCoCoToSpectra#generateRankingForDefects4JElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateRankingForDefects4JElementWrongTestClass() {
//		exception.expect(Abort.class);
		JaCoCoToSpectra.generateRankingForDefects4JElement(null, ".",
				getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src", 
				getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin", 
				null,
				getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin",
				getStdResourcesDir() + File.separator + "wrongTestClassesSimple.txt", 
				extraTestOutput + File.separator + "reportTestClass3",
				8303, null, null, false, false);
		
		Path spectraZipFile = Paths.get(extraTestOutput, "reportTestClass3", "spectraCompressed.zip");
		assertTrue(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportTestClass3", "ranking.trc")));
		
		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
		assertFalse(spectra.getTraces().isEmpty());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.JaCoCoToSpectra#generateRankingForDefects4JElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateRankingForDefects4JElementWithTimeOut() {
		JaCoCoToSpectra.generateRankingForDefects4JElement(null, ".",
				getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src", 
				getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin", 
				null, 
				getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin",
				getStdResourcesDir() + File.separator + "testclassesSimple.txt", 
				extraTestOutput + File.separator + "reportTestClass4",
				8340, -1L, 1, false, false);
		
		Path spectraZipFile = Paths.get(extraTestOutput, "reportTestClass4", "spectraCompressed.zip");
		assertTrue(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportTestClass4", "ranking.trc")));
//		
//		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
//		assertTrue(spectra.getTraces().isEmpty());
	}

}
