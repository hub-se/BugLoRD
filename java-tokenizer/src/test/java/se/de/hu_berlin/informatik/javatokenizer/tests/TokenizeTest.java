/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize;
import se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize.CmdOptions;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class TokenizeTest extends TestSettings {

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
		deleteTestOutputs();
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSrcFolderSyntax() {
		String[] args = {
				CmdOptions.INPUT.asArg(), getStdResourcesDir(),  
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "out",
				CmdOptions.OVERWRITE.asArg() };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "out", "1")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSrcFolderSemantic() {
		String[] args = {
				CmdOptions.INPUT.asArg(), getStdResourcesDir(),  
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "outSemantic",
				CmdOptions.STRATEGY.asArg(), "SEMANTIC",
				CmdOptions.METHODS_ONLY.asArg(),
				CmdOptions.OVERWRITE.asArg() };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "outSemantic", "1")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSrcFolderMethods() {
		String[] args = {
				CmdOptions.INPUT.asArg(), getStdResourcesDir(),  
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "out_methods",
				CmdOptions.METHODS_ONLY.asArg(),
				CmdOptions.OVERWRITE.asArg() };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "out_methods", "1")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSingleFileSyntax() {
		String[] args = {
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "Tokenize.txt",  
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "test.tkn",
				CmdOptions.OVERWRITE.asArg() };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "test.tkn")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSingleFileSemantic() {
		String[] args = {
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "test.java",  
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "testSem.tkn",
				CmdOptions.STRATEGY.asArg(), "SEMANTIC",
				CmdOptions.METHODS_ONLY.asArg(),
				CmdOptions.OVERWRITE.asArg() };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "testSem.tkn")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSingleFileSemantic2() {
		String[] args = {
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "smallTest.java",  
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "smallTest.tkn",
				CmdOptions.STRATEGY.asArg(), "SEMANTIC",
				CmdOptions.MAPPING_DEPTH.asArg(), "2",
				CmdOptions.OVERWRITE.asArg() };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "smallTest.tkn")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSingleFileMethods() {
		String[] args = {
				CmdOptions.INPUT.asArg(), getStdResourcesDir() + File.separator + "LocalizedFormats.java",  
				CmdOptions.OUTPUT.asArg(), getStdTestDir() + File.separator + "test_methods.tkn",
				CmdOptions.METHODS_ONLY.asArg(),
				CmdOptions.OVERWRITE.asArg() };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "test_methods.tkn")));
	}

}
