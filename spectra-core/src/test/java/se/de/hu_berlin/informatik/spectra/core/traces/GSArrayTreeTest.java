package se.de.hu_berlin.informatik.spectra.core.traces;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Simon
 *
 */
public class GSArrayTreeTest {

	/**
     */
	@BeforeClass
	public static void setUpBeforeClass() {
	}

	/**
     */
	@AfterClass
	public static void tearDownAfterClass() {
	}

	/**
     */
	@Before
	public void setUp() {
	}

	/**
     */
	@After
	public void tearDown() {
	}
	
	private int s(int numbers) {
		return numbers;
	}

	/**
	 * Test method for {@link se.de.hu_berlin.informatik.spectra.core.traces.GSTree#addSequence(int[])}.
	 */
	@Test
	public void testAddSequenceIntArray() {
		IntGSArrayTree tree = new IntGSArrayTree();
		
		tree.addSequence(new int[] {});
		tree.addSequence(new int[] {s(1), s(2), s(11), s(3), s(4), s(6)});
		tree.addSequence(new int[] {s(1), s(2), s(11), s(3), s(4), s(7), s(5)});
		tree.addSequence(new int[] {s(1), s(2), s(11), s(7), s(5), s(6)});
		tree.addSequence(new int[] {s(1), s(2), s(11), s(7), s(5), s(6), s(9), s(8), s(10)});
		tree.addSequence(new int[] {s(1), s(2), s(11), s(7)});
		tree.addSequence(new int[] {s(1), s(2), s(10), s(7)});
		// add a trace that contains a previously determined starting element (1);
		// this should stop the addition of this sequence after adding [12,14] 
		// and add the sequence [1,2,14,15] instead
		tree.addSequence(new int[] {s(12), s(14), s(1), s(2), s(14), s(15)});
		// add a trace that contains both 1 and 12 as previously identified starting elements;
		// this should add the sequences [12,14], [1,3], [12,7]
		tree.addSequence(new int[] {s(12), s(14), s(1), s(3), s(12), s(7)});
		
		System.out.print(tree);
		
//		#
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
	
	
}
