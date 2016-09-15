/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class Cob2Instr2Coverage2RankingTest extends TestSettings {

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

	private static String extraTestOutput = "target" + File.separator + "testoutputExtra";
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainRankingGeneration() {
		String[] args = {
				"-pd", ".", 
				"-sd", "src" + File.separator + "main" + File.separator + "java", 
				"-td", "target" + File.separator + "test-classes",
				"-t", getStdResourcesDir() + File.separator + "all_tests.txt",
				"-c", "target" + File.separator + "classes",
				"-l", "tarantula", "jaccard", "GP13", "Wong2", "Op2",
				"-o",  extraTestOutput + File.separator + "report" };
		Cob2Instr2Coverage2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(extraTestOutput, "report", "tarantula", "ranking.rnk")));
		assertTrue(Files.exists(Paths.get(extraTestOutput, "report", "jaccard", "ranking.rnk")));
		assertTrue(Files.exists(Paths.get(extraTestOutput, "report", "gp13", "ranking.rnk")));
		assertTrue(Files.exists(Paths.get(extraTestOutput, "report", "op2", "ranking.rnk")));
		assertTrue(Files.exists(Paths.get(extraTestOutput, "report", "wong2", "ranking.rnk")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainRankingGenerationTestClassFile() {
		String[] args = {
				"-pd", ".", 
				"-sd", "src" + File.separator + "main" + File.separator + "java", 
				"-td", "target" + File.separator + "test-classes",
				"-tc", getStdResourcesDir() + File.separator + "testclasses.txt",
				"-c", "target" + File.separator + "classes",
				"-l", "tarantula", "jaccard",
				"-o", extraTestOutput + File.separator + "reportTestClass" };
		Cob2Instr2Coverage2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportTestClass", "tarantula", "ranking.rnk")));
		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportTestClass", "jaccard", "ranking.rnk")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking#main(java.lang.String[])}.
	 */
	@Test
	public void testMainTraceGeneration() {
		String[] args = {
				"-pd", ".", 
				"-sd", "src" + File.separator + "main" + File.separator + "java", 
				"-td", "target" + File.separator + "test-classes",
				"-t", getStdResourcesDir() + File.separator + "all_tests.txt",
				"-c", "target" + File.separator + "classes",
				"-ht",
				"-o", extraTestOutput + File.separator + "reportTraces" };
		Cob2Instr2Coverage2Ranking.main(args);
		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportTraces", 
				"se.de.hu_berlin.informatik.c2r.tests.Coverage2RankingTest__testMainRankingGenerationNoLocalizers.xml.trc")));
		assertTrue(Files.exists(Paths.get(extraTestOutput, "reportTraces", 
				"se.de.hu_berlin.informatik.c2r.tests.Coverage2RankingTest__testMainTraceGeneration.xml.trc")));
	}
	
//	/**
//	 * Test method for {@link se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking#main(java.lang.String[])}.
//	 */
//	@Test
//	public void testMainRankingGeneration() {
//		String[] args = {
//				"-pd", ".." + File.separator + "java-tokenizer", 
//				"-sd", "src" + File.separator + "main" + File.separator + "java", 
//				"-td", "target" + File.separator + "test-classes",
//				"-t", getStdResourcesDir() + File.separator + "all_tests.txt",
//				"-c", ".." + File.separator + "java-tokenizer" + File.separator + "target" + File.separator + "classes",
//				"-l", "tarantula", "jaccard",
//				"-o", getStdTestDir() + File.separator + "report" };
//		Cob2Instr2Coverage2Ranking.main(args);
//		assertTrue(true);
//	}
//	
//	/**
//	 * Test method for {@link se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking#main(java.lang.String[])}.
//	 */
//	@Test
//	public void testMainRankingGenerationTestClassFile() {
//		String[] args = {
//				"-pd", ".." + File.separator + "java-tokenizer", 
//				"-sd", "src" + File.separator + "main" + File.separator + "java", 
//				"-td", "target" + File.separator + "test-classes",
//				"-tc", getStdResourcesDir() + File.separator + "testclasses.txt",
//				"-c", ".." + File.separator + "java-tokenizer" + File.separator + "target" + File.separator + "classes",
//				"-l", "tarantula", "jaccard",
//				"-o", getStdTestDir() + File.separator + "reportTestClass" };
//		Cob2Instr2Coverage2Ranking.main(args);
//		assertTrue(true);
//	}
//	
//	/**
//	 * Test method for {@link se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking#main(java.lang.String[])}.
//	 */
//	@Test
//	public void testMainTraceGeneration() {
//		String[] args = {
//				"-pd", ".." + File.separator + "java-tokenizer", 
//				"-sd", "src" + File.separator + "main" + File.separator + "java", 
//				"-td", "target" + File.separator + "test-classes",
//				"-t", getStdResourcesDir() + File.separator + "all_tests.txt",
//				"-c", ".." + File.separator + "java-tokenizer" + File.separator + "target" + File.separator + "classes",
//				"-ht",
//				"-o", getStdTestDir() + File.separator + "reportTraces" };
//		Cob2Instr2Coverage2Ranking.main(args);
//		assertTrue(true);
//	}

}
