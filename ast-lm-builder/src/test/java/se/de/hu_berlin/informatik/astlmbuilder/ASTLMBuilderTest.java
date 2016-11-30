/**
 * 
 */
package se.de.hu_berlin.informatik.astlmbuilder;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.astlmbuilder.ASTLMBOptions.ASTLMBCmdOptions;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class ASTLMBuilderTest extends TestSettings {

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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
//		deleteTestOutputs();
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.astlmbuilder.ASTLMBuilder#main(java.lang.String[])}.
	 */
	@Test
	public void testMain() throws Exception {
		String[] args = {
				ASTLMBCmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files",  
				ASTLMBCmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "out.lm",
				ASTLMBCmdOptions.GRANULARITY.asArg(), "all",
				ASTLMBCmdOptions.ENTRY_POINT.asArg(), "root",
				ASTLMBCmdOptions.CREATE_ARPA_TEXT.asArg(),
				ASTLMBCmdOptions.NGRAM_ORDER.asArg(), "3"};
		ASTLMBuilder.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "out.lm.bin")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "out.lm.arpa")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.astlmbuilder.ASTLMBuilder#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSingleFile() throws Exception {
		String[] args = {
				ASTLMBCmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files" + File.separator + "StringUtils.java",  
				ASTLMBCmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "outSingle.lm",
				ASTLMBCmdOptions.GRANULARITY.asArg(), "all",
				ASTLMBCmdOptions.ENTRY_POINT.asArg(), "all",
				ASTLMBCmdOptions.CREATE_ARPA_TEXT.asArg(),
				ASTLMBCmdOptions.NGRAM_ORDER.asArg(), "6"};
		ASTLMBuilder.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "outSingle.lm.bin")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "outSingle.lm.arpa")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.astlmbuilder.ASTLMBuilder#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSingleFileSmallTest() throws Exception {
		String[] args = {
				ASTLMBCmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files" + File.separator + "smallTest.java",  
				ASTLMBCmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "small.lm",
				ASTLMBCmdOptions.GRANULARITY.asArg(), "all",
				ASTLMBCmdOptions.ENTRY_POINT.asArg(), "all",
//				ASTLMBCmdOptions.SINGLE_TOKENS.asArg(),
				ASTLMBCmdOptions.CREATE_ARPA_TEXT.asArg(),
				ASTLMBCmdOptions.NGRAM_ORDER.asArg(), "6"};
		ASTLMBuilder.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "small.lm.bin")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "small.lm.arpa")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.astlmbuilder.ASTLMBuilder#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSingleTokens() throws Exception {
		String[] args = {
				ASTLMBCmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files",  
				ASTLMBCmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "out3.lm",
				ASTLMBCmdOptions.GRANULARITY.asArg(), "all",
				ASTLMBCmdOptions.ENTRY_POINT.asArg(), "root",
				ASTLMBCmdOptions.CREATE_ARPA_TEXT.asArg(),
				ASTLMBCmdOptions.SINGLE_TOKENS.asArg(),
				ASTLMBCmdOptions.NGRAM_ORDER.asArg(), "3"};
		ASTLMBuilder.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "out3.lm.bin")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "out3.lm.arpa")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.astlmbuilder.ASTLMBuilder#main(java.lang.String[])}.
	 */
	@Test
	public void testMainNormalGranularityMethods() throws Exception {
		String[] args = {
				ASTLMBCmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files",  
				ASTLMBCmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "out2.lm",
				ASTLMBCmdOptions.GRANULARITY.asArg(), "normal",
				ASTLMBCmdOptions.ENTRY_POINT.asArg(), "method",
				ASTLMBCmdOptions.CREATE_ARPA_TEXT.asArg(),
				ASTLMBCmdOptions.NGRAM_ORDER.asArg(), "3"};
		ASTLMBuilder.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "out2.lm.bin")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "out2.lm.arpa")));
	}

}
