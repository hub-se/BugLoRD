/**
 * 
 */
package se.de.hu_berlin.informatik.spectra.core.traces;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.IntArrayWrapper;


/**
 * @author Simon
 *
 */
public class GSArrayTreeTest {

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
	
	private int[] s(int... numbers) {
		return numbers;
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.GSTree#addSequence(int[])}.
	 */
	@Test
	public void testAddSequenceIntArray() throws Exception {
		GSArrayTree<int[],IntArrayWrapper> tree = new GSIntArrayTree();
		
		tree.addSequence(new int[][] {});
		tree.addSequence(new int[][] {s(1), s(2), s(11), s(3), s(4), s(6)});
		tree.addSequence(new int[][] {s(1), s(2), s(11), s(3), s(4), s(7), s(5)});
		tree.addSequence(new int[][] {s(1), s(2), s(11), s(7), s(5), s(6)});
		tree.addSequence(new int[][] {s(1), s(2), s(11), s(7), s(5), s(6), s(9), s(8), s(10)});
		tree.addSequence(new int[][] {s(1), s(2), s(11), s(7)});
		tree.addSequence(new int[][] {s(1), s(2), s(10), s(7)});
		// add a trace that contains a previously determined starting element (1);
		// this should stop the addition of this sequence after adding [12,14] 
		// and add the sequence [1,2,14,15] instead
		tree.addSequence(new int[][] {s(12), s(14), s(1), s(2), s(14), s(15)});
		// add a trace that contains both 1 and 12 as previously identified starting elements;
		// this should add the sequences [12,14], [1,3], [12,7]
		tree.addSequence(new int[][] {s(12), s(14), s(1), s(3), s(12), s(7)});
		
		System.out.print(tree);
		
//		#1,#2,#11,#3,4,#6,#
//		#1,#2,#11,#3,4,#7,5,#
//		#1,#2,#11,#7,#5,6,#
//		#1,#2,#11,#7,#5,6,#9,8,10,#
//		#1,#2,#11,#7,#
//		#1,#2,#10,7,#
//		#1,#2,#14,15,#
//		#1,#3,#
//		#12,#14,#
//		#12,#7,#
	}
	
	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.GSTree#addSequence(int[])}.
	 */
	@Test
	public void testAddSequenceIntArray2() throws Exception {
		GSArrayTree<int[],IntArrayWrapper> tree = new GSIntArrayTree();
		
//		tree.addSequence(new int[][] {});
		tree.addSequence(new int[][] {s(1,2), s(2), s(11), s(3), s(4), s(6)});
		tree.addSequence(new int[][] {s(1), s(2), s(11), s(3), s(4), s(7), s(5)});
		tree.addSequence(new int[][] {s(1), s(2), s(11), s(7), s(5), s(6), s(2)});
		tree.addSequence(new int[][] {s(1), s(2), s(11,2), s(7), s(5), s(6), s(9), s(8), s(10)});
		tree.addSequence(new int[][] {s(1), s(2), s(11), s(7)});
		tree.addSequence(new int[][] {s(1), s(2), s(10), s(2), s(7), s(2,2), s(2), s(3), s(4)});
		
		// new starting element s(2)
		// searches for sequences starting with s(2) in previously inserted sequences 
		// no it doesn't any more!!
		tree.addSequence(new int[][] {s(2), s(11), s(7), s(6)});
		
		System.out.print(tree);
	}
	
	
}
