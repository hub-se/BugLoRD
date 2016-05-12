/**
 * 
 */
package se.de.hu_berlin.informatik.junittestutils.tests;

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

import se.de.hu_berlin.informatik.junittestutils.testlister.UnitTestLister;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class UnitTestListerTest extends TestSettings {

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

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.junittestutils.testlister.UnitTestLister#main(java.lang.String[])}.
	 */
	@Test
	public void testMain() {
		String[] args = { 
				"-i", getStdResourcesDir() + File.separator + "test.in",  
				"-o", getStdTestDir() + File.separator + "tests.out" };
		UnitTestLister.main(args);
		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tests.out")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.junittestutils.testlister.UnitTestLister#main(java.lang.String[])}.
	 */
	@Test
	public void testMainTestClassNotFound() {
		String[] args = { 
				"-i", getStdResourcesDir() + File.separator + "testWrong.in",  
				"-o", getStdTestDir() + File.separator + "testsWrong.out" };
		exit.expectSystemExitWithStatus(1);
		UnitTestLister.main(args);
	}

}
