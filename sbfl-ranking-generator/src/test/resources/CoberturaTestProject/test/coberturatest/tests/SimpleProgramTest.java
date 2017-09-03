/**
 * 
 */
package coberturatest.tests;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cobertura.test.SimpleProgram;

/**
 * @author SimHigh
 *
 */
public class SimpleProgramTest {

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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link coberturatest.SimpleProgram#add(int, int)}.
	 */
	@Test
	public void testAdd() throws Exception {
		Assert.assertEquals(SimpleProgram.add(2, 3), 5);
		Assert.assertEquals(SimpleProgram.add(2, 4), 6);
	}
	
	/**
	 * Test method for {@link coberturatest.SimpleProgram#add(int, int)}.
	 */
	@Test
	public void testAddWrong() throws Exception {
		Assert.assertEquals(SimpleProgram.add(2, 3), 5);
		Assert.assertEquals(SimpleProgram.add(2, 2), 6);
	}
	
	/**
	 * Test method for {@link coberturatest.SimpleProgram#add2times(int, int)}.
	 */
	@Test
	public void testAdd2times() throws Exception {
		Assert.assertEquals(SimpleProgram.add2times(2, 3), 10);
		Assert.assertEquals(SimpleProgram.add2times(2, 4), 12);
	}
	
	/**
	 * Test method for {@link coberturatest.SimpleProgram#subtract(int, int)}.
	 */
	@Test
	public void testSubtract() throws Exception {
		Assert.assertEquals(SimpleProgram.subtract(2, 3), -1);
		Assert.assertEquals(SimpleProgram.subtract(2, 4), -2);
	}

	/**
	 * Test method for {@link coberturatest.SimpleProgram.InnerStaticClass#multiply(int, int)}.
	 */
	@Test
	public void testMultiply() throws Exception {
		Assert.assertEquals(SimpleProgram.InnerStaticClass.multiply(2, 3), 6);
	}

	/**
	 * Test method for {@link coberturatest.SimpleProgram.InnerClass#divide(int, int)}.
	 */
	@Test
	public void testDivide() throws Exception {
		Assert.assertEquals(new SimpleProgram().new InnerClass().divide(6, 3), 2);
	}
	
	public static class InnerTestClass {
		
		/**
		 * Test method for {@link coberturatest.SimpleProgram#add(int, int)}.
		 */
		@Test
		public void testAddInner() throws Exception {
			Assert.assertEquals(SimpleProgram.add(2, 3), 5);
			Assert.assertEquals(SimpleProgram.add(2, 4), 6);
		}
		
	}
	
	private static class InnerPrivateTestClass {
		
		/**
		 * Test method for {@link coberturatest.SimpleProgram#add(int, int)}.
		 */
		@Test
		public void testAddInnerPrivate() throws Exception {
			Assert.assertEquals(SimpleProgram.add(2, 3), 5);
			Assert.assertEquals(SimpleProgram.add(2, 4), 6);
		}
		
	}
	
	private static class InnerPrivateTestClassWithNonPublicConstructor {
		
		InnerPrivateTestClassWithNonPublicConstructor() {
			//exists in Closure Test case...
		}
		
		/**
		 * Test method for {@link coberturatest.SimpleProgram#add(int, int)}.
		 */
		@Test
		public void testAddInnerPrivateNonPublic() throws Exception {
			Assert.assertEquals(SimpleProgram.add(2, 3), 5);
			Assert.assertEquals(SimpleProgram.add(2, 4), 6);
		}
		
	}
	
	public static class InnerPublicTestClassWithNonPublicConstructor {
		
		InnerPublicTestClassWithNonPublicConstructor() {
			//does that work?
		}
		
		/**
		 * Test method for {@link coberturatest.SimpleProgram#add(int, int)}.
		 */
		@Test
		public void testAddInnerNonPublic() throws Exception {
			Assert.assertEquals(SimpleProgram.add(2, 3), 5);
			Assert.assertEquals(SimpleProgram.add(2, 4), 6);
		}
		
	}
	
}
