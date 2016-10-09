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

import se.de.hu_berlin.informatik.astlmbuilder.ASTLMBOptions.CmdOptions;
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
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files",  
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "out.lm",
				CmdOptions.GRANULARITY.asArg(), "all",
				CmdOptions.ENTRY_POINT.asArg(), "root",
				CmdOptions.CREATE_ARPA_TEXT.asArg(),
				CmdOptions.NGRAM_ORDER.asArg(), "3"};
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
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files" + File.separator + "StringUtils.java",  
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "outSingle.lm",
				CmdOptions.GRANULARITY.asArg(), "all",
				CmdOptions.ENTRY_POINT.asArg(), "all",
				CmdOptions.CREATE_ARPA_TEXT.asArg(),
				CmdOptions.NGRAM_ORDER.asArg(), "6"};
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
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files" + File.separator + "smallTest.java",  
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "small.lm",
				CmdOptions.GRANULARITY.asArg(), "all",
				CmdOptions.ENTRY_POINT.asArg(), "all",
//				CmdOptions.SINGLE_TOKENS.asArg(),
				CmdOptions.CREATE_ARPA_TEXT.asArg(),
				CmdOptions.NGRAM_ORDER.asArg(), "6"};
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
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files",  
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "out3.lm",
				CmdOptions.GRANULARITY.asArg(), "all",
				CmdOptions.ENTRY_POINT.asArg(), "root",
				CmdOptions.CREATE_ARPA_TEXT.asArg(),
				CmdOptions.SINGLE_TOKENS.asArg(),
				CmdOptions.NGRAM_ORDER.asArg(), "3"};
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
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "training_files",  
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "out2.lm",
				CmdOptions.GRANULARITY.asArg(), "normal",
				CmdOptions.ENTRY_POINT.asArg(), "method",
				CmdOptions.CREATE_ARPA_TEXT.asArg(),
				CmdOptions.NGRAM_ORDER.asArg(), "3"};
		ASTLMBuilder.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "out2.lm.bin")));
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "out2.lm.arpa")));
	}

}
