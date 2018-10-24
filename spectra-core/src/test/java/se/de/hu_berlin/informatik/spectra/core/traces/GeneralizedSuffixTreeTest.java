/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.core.traces;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Simon
 *
 */
public class GeneralizedSuffixTreeTest {

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
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.GeneralizedSuffixTree#addSequence(int[])}.
	 */
	@Test
	public void testAddSequenceIntArray() throws Exception {
		GeneralizedSuffixTree tree = new GeneralizedSuffixTree();
		
		tree.addSequence(new int[] {});
		tree.addSequence(new int[] {1, 2, 11, 3, 4, 6});
		tree.addSequence(new int[] {1, 2, 11, 3, 4, 7, 5});
		tree.addSequence(new int[] {1, 2, 11, 7, 5, 6});
		tree.addSequence(new int[] {1, 2, 11, 7, 5, 6, 9, 8, 10});
		tree.addSequence(new int[] {1, 2, 11, 7});
		tree.addSequence(new int[] {1, 2, 10, 7});
		
		System.out.print(tree);
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.GeneralizedSuffixTree#addSequence(int[])}.
	 */
	@Test
	public void testAddSequenceIntArray2() throws Exception {
		GeneralizedSuffixTree tree = new GeneralizedSuffixTree();
		
		tree.addSequence(new int[] {});
		tree.addSequence(new int[] {1, 2, 11, 3, 4, 6});
		tree.addSequence(new int[] {1, 2, 11, 3, 4, 7, 5});
		tree.addSequence(new int[] {1, 2, 11, 7, 5, 6});
		tree.addSequence(new int[] {1, 2, 11, 7, 5, 6, 9, 8, 10});
		tree.addSequence(new int[] {1, 2, 11, 7});
		tree.addSequence(new int[] {1, 2, 10, 7});
		
		// new starting element 2
		// searches for sequences starting with 2 in previously inserted sequences
		tree.addSequence(new int[] {2, 11, 7, 6});
		
		System.out.print(tree);
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.GeneralizedSuffixTree#addSequence(int[])}.
	 */
	@Test
	public void testAddSequenceIntArray3() throws Exception {
		GeneralizedSuffixTree tree = new GeneralizedSuffixTree();
		
//		tree.addSequence(new int[] {});
		tree.addSequence(new int[] {1, 2, 11, 3, 4, 6});
		tree.addSequence(new int[] {1, 2, 11, 3, 4, 7, 5});
		tree.addSequence(new int[] {1, 2, 11, 7, 5, 6});
		tree.addSequence(new int[] {1, 2, 11, 7, 5, 6, 9, 8, 10});
		tree.addSequence(new int[] {1, 2, 11, 7});
		tree.addSequence(new int[] {1, 2, 10, 7});
		
		// new starting element 11
		// searches for sequences starting with 11 in previously inserted sequences
		// removes a complete node in the tree (that started with 11)
		tree.addSequence(new int[] {11, 7});
		
		System.out.print(tree);
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.GeneralizedSuffixTree#checkIfMatch(int[], int, int)}.
	 */
	@Test
	public void testCheckIfMatchIntArrayIntInt() throws Exception {
		GeneralizedSuffixTree tree = new GeneralizedSuffixTree();

		tree.addSequence(new int[] {});
		tree.addSequence(new int[] {1, 2, 11, 3, 4, 6});
		tree.addSequence(new int[] {1, 2, 11, 3, 4, 7, 5});
		tree.addSequence(new int[] {1, 2, 11, 7, 5, 6});
		tree.addSequence(new int[] {1, 2, 11, 7, 5, 6, 9, 8, 10});
		tree.addSequence(new int[] {1, 2, 11, 7});
		tree.addSequence(new int[] {1, 2, 10, 7});
		
		Assert.assertTrue(tree.checkIfMatch(new int[] {1, 2, 11, 3, 4, 7, 5}));
		Assert.assertFalse(tree.checkIfMatch(new int[] {2, 11, 3, 4, 7, 5}));
		Assert.assertFalse(tree.checkIfMatch(new int[] {1, 2, 11, 3, 4, 7}));
		Assert.assertTrue(tree.checkIfMatch(new int[] {0, 1, 2, 11, 3, 4, 6, 7}, 1, 7));

		System.out.print(tree);
	}

	
	
}
