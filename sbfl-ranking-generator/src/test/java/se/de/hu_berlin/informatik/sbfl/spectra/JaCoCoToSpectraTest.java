/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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
//		FileUtils.delete(Paths.get(extraTestOutput));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
//		FileUtils.delete(Paths.get(extraTestOutput));
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
				CmdOptions.OUTPUT.asArg(),  extraTestOutput + File.separator + "reportJaCoCo",
				CmdOptions.AGENT_PORT.asArg(), "8000"};
		JaCoCoToSpectra.main(args);
		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCo", "spectraCompressed.zip")));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCo", "ranking.trc")));
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
//				CmdOptions.OUTPUT.asArg(),  extraTestOutput + File.separator + "reportJaCoCo2"};
//		JaCoCoToSpectra.main(args);
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCo2", "spectraCompressed.zip")));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCo2", "ranking.trc")));
//	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.spectra.cobertura.CoberturaToSpectra#generateRankingForDefects4JElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateRankingForDefects4JElementMockito() {
		String testCP = getStdResourcesDir() + File.separator + "Mockito12b/lib/junit-4.11.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/target/classes" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/target/test-classes" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/asm-all-5.0.4.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/assertj-core-2.1.0.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/cglib-and-asm-1.0.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/cobertura-2.0.3.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/fest-assert-1.3.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/fest-util-1.1.4.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/hamcrest-all-1.3.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/hamcrest-core-1.1.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/objenesis-2.1.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/objenesis-2.2.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Mockito12b/lib/powermock-reflect-1.2.5.jar";
		
		ArrayList<String> failingTests = new ArrayList<>();
		failingTests.add("org.mockito.internal.util.reflection.GenericMasterTest::shouldDealWithNestedGenerics");
		failingTests.add("org.mockitousage.annotation.CaptorAnnotationBasicTest::shouldUseAnnotatedCaptor");
		failingTests.add("org.mockitousage.annotation.CaptorAnnotationBasicTest::shouldUseCaptorInOrdinaryWay");
		failingTests.add("org.mockitousage.annotation.CaptorAnnotationBasicTest::shouldCaptureGenericList");
		failingTests.add("org.mockitousage.annotation.CaptorAnnotationBasicTest::shouldUseGenericlessAnnotatedCaptor");
		failingTests.add("org.mockitousage.annotation.CaptorAnnotationTest::shouldScreamWhenWrongTypeForCaptor");
		failingTests.add("org.mockitousage.annotation.CaptorAnnotationTest::testNormalUsage");
		failingTests.add("org.mockitousage.annotation.CaptorAnnotationTest::shouldScreamWhenMoreThanOneMockitoAnnotaton");
		failingTests.add("org.mockitousage.annotation.CaptorAnnotationTest::shouldScreamWhenInitializingCaptorsForNullClass");
		failingTests.add("org.mockitousage.annotation.CaptorAnnotationTest::shouldLookForAnnotatedCaptorsInSuperClasses");

		new JaCoCoToSpectra.Builder()
		.setProjectDir(".")
		.setSourceDir(getStdResourcesDir() + File.separator + "Mockito12b" + File.separator + "src")
		.setTestClassDir(getStdResourcesDir() + File.separator + "Mockito12b" + File.separator + "target" + File.separator + "test-classes")
		.setTestClassPath(testCP)
		.setPathsToBinaries(getStdResourcesDir() + File.separator + "Mockito12b" + File.separator + "target" + File.separator + "classes")
		.setOutputDir(extraTestOutput + File.separator + "reportJaCoCoTestClassMockito12b")
		.setTestClassList(getStdResourcesDir() + File.separator + "Mockito12b" + File.separator + "testClasses.txt")
		.setFailingTests(failingTests)
		.useFullSpectra(true)
		.useSeparateJVM(false)
		.setTimeout(null)
		.setTestRepeatCount(1)
		.setAgentPort(8219)
		.run();

		Path spectraZipFile = Paths.get(extraTestOutput, "reportJaCoCoTestClassMockito12b", "spectraCompressed.zip");
		assertTrue(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCoTestClass", "ranking.trc")));
		
//		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
//		assertFalse(spectra.getTraces().isEmpty());
//		assertEquals(spectra.getTraces().size()-2, spectra.getSuccessfulTraces().size());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.spectra.cobertura.CoberturaToSpectra#generateRankingForDefects4JElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateRankingForDefects4JElementClosure() {
		String testCP = getStdResourcesDir() + File.separator + "Closure101b/build/classes" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Closure101b/lib/ant_deploy.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Closure101b/lib/args4j_deploy.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Closure101b/lib/google_common_deploy.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Closure101b/lib/hamcrest-core-1.1.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Closure101b/lib/junit.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Closure101b/lib/libtrunk_rhino_parser_jarjared.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Closure101b/lib/protobuf_deploy.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Closure101b/lib/ant.jar" + File.pathSeparator
				+ getStdResourcesDir() + File.separator + "Closure101b/build/test";
		
		ArrayList<String> failingTests = new ArrayList<>();
		failingTests.add("com.google.javascript.jscomp.CommandLineRunnerTest::testProcessClosurePrimitives");

		new JaCoCoToSpectra.Builder()
		.setProjectDir(".")
		.setSourceDir(getStdResourcesDir() + File.separator + "Closure101b" + File.separator + "src")
		.setTestClassDir(getStdResourcesDir() + File.separator + "Closure101b" + File.separator + "build" + File.separator + "test")
		.setTestClassPath(testCP)
		.setPathsToBinaries(getStdResourcesDir() + File.separator + "Closure101b" + File.separator + "build" + File.separator + "classes")
		.setOutputDir(extraTestOutput + File.separator + "reportJaCoCoTestClassClosure101b")
		.setTestClassList(getStdResourcesDir() + File.separator + "Closure101b" + File.separator + "testClasses.txt")
		.setFailingTests(failingTests)
		.useFullSpectra(true)
		.useSeparateJVM(false)
		.setTimeout(5L)
		.setTestRepeatCount(1)
		.setAgentPort(8221)
		.run();

		Path spectraZipFile = Paths.get(extraTestOutput, "reportJaCoCoTestClassClosure101b", "spectraCompressed.zip");
		assertTrue(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCoTestClass", "ranking.trc")));
		
//		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
//		assertFalse(spectra.getTraces().isEmpty());
//		assertEquals(spectra.getTraces().size()-2, spectra.getSuccessfulTraces().size());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.spectra.cobertura.CoberturaToSpectra#generateRankingForDefects4JElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateRankingForDefects4JElementTestList() {
		new JaCoCoToSpectra.Builder()
		.setProjectDir(".")
		.setSourceDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src")
		.setTestClassDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin")
		.setPathsToBinaries(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin")
		.setOutputDir(extraTestOutput + File.separator + "reportJaCoCoTestClass9")
		.setTestList(getStdResourcesDir() + File.separator + "all_testsSimple.txt")
		.useFullSpectra(false)
		.useSeparateJVM(false)
		.setTimeout(null)
		.setTestRepeatCount(2)
		.setAgentPort(8201)
		.run();

		Path spectraZipFile = Paths.get(extraTestOutput, "reportJaCoCoTestClass9", "spectraCompressed.zip");
		assertTrue(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCoTestClass", "ranking.trc")));
		
		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
		assertFalse(spectra.getTraces().isEmpty());
		assertEquals(2, spectra.getSuccessfulTraces().size());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.spectra.cobertura.CoberturaToSpectra#generateRankingForDefects4JElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateRankingForDefects4JElementTestListFullSpectra() {
		new JaCoCoToSpectra.Builder()
		.setProjectDir(".")
		.setSourceDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src")
		.setTestClassDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin")
		.setPathsToBinaries(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin")
		.setOutputDir(extraTestOutput + File.separator + "reportJaCoCoTestClass10")
		.setTestList(getStdResourcesDir() + File.separator + "all_testsSimple.txt")
		.useFullSpectra(true)
		.useSeparateJVM(false)
		.setTimeout(null)
		.setTestRepeatCount(2)
		.setAgentPort(8301)
		.run();

		Path spectraZipFile = Paths.get(extraTestOutput, "reportJaCoCoTestClass10", "spectraCompressed.zip");
		assertTrue(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCoTestClass", "ranking.trc")));
		
		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
		assertFalse(spectra.getTraces().isEmpty());
		assertEquals(2, spectra.getSuccessfulTraces().size());
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.JaCoCoToSpectra#generateRankingForDefects4JElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateRankingForDefects4JElement() {
		new JaCoCoToSpectra.Builder()
		.setProjectDir(".")
		.setSourceDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src")
		.setTestClassDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin")
		.setPathsToBinaries(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin")
		.setOutputDir(extraTestOutput + File.separator + "reportJaCoCoTestClass")
		.setTestClassList(getStdResourcesDir() + File.separator + "testclassesSimple.txt")
		.useFullSpectra(false)
		.setTimeout(600L)
		.setAgentPort(8001)
		.setTestRepeatCount(2)
		.run();

		Path spectraZipFile = Paths.get(extraTestOutput, "reportJaCoCoTestClass", "spectraCompressed.zip");
		assertTrue(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCoTestClass", "ranking.trc")));
		
		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
		assertFalse(spectra.getTraces().isEmpty());
		assertEquals(spectra.getTraces().size()-2, spectra.getSuccessfulTraces().size());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.spectra.cobertura.CoberturaToSpectra#generateRankingForDefects4JElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateRankingForDefects4JElementWithFailedTestCases() {
		ArrayList<String> failingTests = new ArrayList<>();
		failingTests.add("coberturatest.tests.SimpleProgramTest::testAddWrong");
		new JaCoCoToSpectra.Builder()
		.setProjectDir(".")
		.setSourceDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src")
		.setTestClassDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin")
		.setPathsToBinaries(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin")
		.setOutputDir(extraTestOutput + File.separator + "reportJaCoCoTestClass7")
		.setTestClassList(getStdResourcesDir() + File.separator + "testclassesSimple.txt")
		.setFailingTests(failingTests)
		.useFullSpectra(false)
		.useSeparateJVM(false)
		.setTimeout(null)
		.setAgentPort(8001)
		.setTestRepeatCount(2)
		.run();

		Path spectraZipFile = Paths.get(extraTestOutput, "reportJaCoCoTestClass7", "spectraCompressed.zip");
		assertTrue(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCoTestClass", "ranking.trc")));
		
		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
		assertFalse(spectra.getTraces().isEmpty());
		assertEquals(spectra.getTraces().size()-2, spectra.getSuccessfulTraces().size());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.spectra.cobertura.CoberturaToSpectra#generateRankingForDefects4JElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateRankingForDefects4JElementWithWrongFailedTestCases() {
		ArrayList<String> failingTests = new ArrayList<>();
		failingTests.add("coberturatest.tests.SimpleProgramTest::testAdd");
		new JaCoCoToSpectra.Builder()
		.setProjectDir(".")
		.setSourceDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src")
		.setTestClassDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin")
		.setPathsToBinaries(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin")
		.setOutputDir(extraTestOutput + File.separator + "reportJaCoCoTestClass8")
		.setTestClassList(getStdResourcesDir() + File.separator + "testclassesSimple.txt")
		.setFailingTests(failingTests)
		.useFullSpectra(false)
		.useSeparateJVM(false)
		.setTimeout(null)
		.setAgentPort(8001)
		.setTestRepeatCount(2)
		.run();

		Path spectraZipFile = Paths.get(extraTestOutput, "reportJaCoCoTestClass8", "spectraCompressed.zip");
		assertFalse(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCoTestClass", "ranking.trc")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.JaCoCoToSpectra#generateRankingForDefects4JElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateRankingForDefects4JElementFullSpectra() {
		new JaCoCoToSpectra.Builder()
		.setProjectDir(".")
		.setSourceDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src")
		.setTestClassDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin")
		.setPathsToBinaries(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin")
		.setOutputDir(extraTestOutput + File.separator + "reportJaCoCoTestClass2")
		.setTestClassList(getStdResourcesDir() + File.separator + "testclassesSimple.txt")
		.useFullSpectra(true)
		.setTimeout(600L)
		.setAgentPort(8301)
		.setTestRepeatCount(2)
		.run();

		Path spectraZipFile = Paths.get(extraTestOutput, "reportJaCoCoTestClass2", "spectraCompressed.zip");
		assertTrue(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCoTestClass2", "ranking.trc")));
		
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
		new JaCoCoToSpectra.Builder()
		.setProjectDir(".")
		.setSourceDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src")
		.setTestClassDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin")
		.setPathsToBinaries(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin")
		.setOutputDir(extraTestOutput + File.separator + "reportJaCoCoTestClass3")
		.setTestClassList(getStdResourcesDir() + File.separator + "wrongTestClassesSimple.txt")
		.useFullSpectra(false)
		.setTimeout(null)
		.setAgentPort(8303)
		.setTestRepeatCount(1)
		.run();
		
		Path spectraZipFile = Paths.get(extraTestOutput, "reportJaCoCoTestClass3", "spectraCompressed.zip");
		assertTrue(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCoTestClass3", "ranking.trc")));
		
		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
		assertFalse(spectra.getTraces().isEmpty());
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.sbfl.JaCoCoToSpectra#generateRankingForDefects4JElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateRankingForDefects4JElementWithTimeOut() {
		new JaCoCoToSpectra.Builder()
		.setProjectDir(".")
		.setSourceDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "src")
		.setTestClassDir(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "test-bin")
		.setPathsToBinaries(getStdResourcesDir() + File.separator + "CoberturaTestProject" + File.separator + "bin")
		.setOutputDir(extraTestOutput + File.separator + "reportJaCoCoTestClass4")
		.setTestClassList(getStdResourcesDir() + File.separator + "testclassesSimple.txt")
		.useFullSpectra(false)
		.setTimeout(-1L)
		.setAgentPort(8340)
		.setTestRepeatCount(1)
		.run();
		
		Path spectraZipFile = Paths.get(extraTestOutput, "reportJaCoCoTestClass4", "spectraCompressed.zip");
		assertTrue(Files.exists(spectraZipFile));
//		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportJaCoCoTestClass4", "ranking.trc")));
//		
//		ISpectra<SourceCodeBlock> spectra = SpectraFileUtils.loadBlockSpectraFromZipFile(spectraZipFile);
//		assertTrue(spectra.getTraces().isEmpty());
	}

}
