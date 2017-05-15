/**
 * 
 */
package se.de.hu_berlin.informatik.changechecker;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class ChangeCheckerTest extends TestSettings {

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
	 * Test method for {@link se.de.hu_berlin.informatik.changechecker.ChangeChecker#main(java.lang.String[])}.
	 */
	@Test
	public void testMain() {
		String[] args = { 
				"-l", getStdResourcesDir() + File.separator + "TestRunAndReportModule.java",  
				"-r", getStdResourcesDir() + File.separator + "TestRunAndReportModule_changed.java" };
		ChangeChecker.main(args);
//		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tests.out")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.changechecker.ChangeChecker#main(java.lang.String[])}.
	 */
	@Test
	public void testMainCompressed() {
		String[] args = { 
				"-l", getStdResourcesDir() + File.separator + "TestRunAndReportModule.java",  
				"-r", getStdResourcesDir() + File.separator + "TestRunAndReportModule_changed.java",
				"-c" };
		ChangeChecker.main(args);
//		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tests.out")));
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.changechecker.ChangeChecker#main(java.lang.String[])}.
	 */
	@Test
	public void testMain2() {
		String[] args = { 
				"-l", getStdResourcesDir() + File.separator + "MustBeReachingVariableDef.java",  
				"-r", getStdResourcesDir() + File.separator + "MustBeReachingVariableDef_changed.java" };
		ChangeChecker.main(args);
//		assertTrue(Files.exists(Paths.get(getStdTestDir(), "tests.out")));
	}
	
}
