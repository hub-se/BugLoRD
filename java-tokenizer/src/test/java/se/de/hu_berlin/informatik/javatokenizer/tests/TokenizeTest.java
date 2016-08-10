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
//		deleteTestOutputs();
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSrcFolderSyntax() {
		String[] args = {
				"-i", getStdResourcesDir(),  
				"-o", getStdTestDir() + File.separator + "out",
				"-t", "5",
				"-w" };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "out", "1")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSrcFolderSemantic() {
		String[] args = {
				"-i", getStdResourcesDir(),  
				"-o", getStdTestDir() + File.separator + "outSemantic",
				"-strat", "SEMANTIC",
				"-m",
				"-t", "5",
				"-w" };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "outSemantic", "1")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSrcFolderMethods() {
		String[] args = {
				"-i", getStdResourcesDir(),  
				"-o", getStdTestDir() + File.separator + "out_methods",
				"-t", "5",
				"-m",
				"-w" };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "out_methods", "1")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSingleFileSyntax() {
		String[] args = {
				"-i", getStdResourcesDir() + File.separator + "Tokenize.txt",  
				"-o", getStdTestDir() + File.separator + "test.tkn",
				"-w" };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "test.tkn")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSingleFileSemantic() {
		String[] args = {
				"-i", getStdResourcesDir() + File.separator + "test.java",  
				"-o", getStdTestDir() + File.separator + "testSem.tkn",
				"-strat", "SEMANTIC",
				"-m",
				"-w" };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "testSem.tkn")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSingleFileSemantic2() {
		String[] args = {
				"-i", getStdResourcesDir() + File.separator + "smallTest.java",  
				"-o", getStdTestDir() + File.separator + "smallTest.tkn",
				"-strat", "SEMANTIC",
				"-d", "1",
				"-w" };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "smallTest.tkn")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize#main(java.lang.String[])}.
	 */
	@Test
	public void testMainSingleFileMethods() {
		String[] args = {
				"-i", getStdResourcesDir() + File.separator + "LocalizedFormats.java",  
				"-o", getStdTestDir() + File.separator + "test_methods.tkn",
				"-m",
				"-w" };
		Tokenize.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "test_methods.tkn")));
	}

}
