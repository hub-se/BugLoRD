package se.de.hu_berlin.informatik.spectra.core.traces;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;

import se.de.hu_berlin.informatik.spectra.core.branch.SimpleIntGSArrayTree;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;


/**
 * @author Simon
 */
public class SimpleIntGSArrayTreeTest {

    /**
     *
     */
    @BeforeClass
    public static void setUpBeforeClass() {
    }

    /**
     *
     */
    @AfterClass
    public static void tearDownAfterClass() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
    }

    /**
     *
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
    	int startIndex = 1;
    	AtomicInteger idGen = new AtomicInteger(startIndex);
        SimpleIntGSArrayTree tree = new SimpleIntGSArrayTree();

        Assert.assertEquals(startIndex, tree.addSequence(idGen, new int[]{}));
        Assert.assertEquals(startIndex, tree.addSequence(idGen, new int[]{}));
        Assert.assertEquals(startIndex+1, tree.addSequence(idGen, new int[]{s(1), s(2), s(11), s(3), s(4), s(6)}));
        Assert.assertEquals(startIndex+1, tree.addSequence(idGen, new int[]{s(1), s(2), s(11), s(3), s(4), s(6)}));
        Assert.assertEquals(startIndex, tree.addSequence(idGen, new int[]{}));
        Assert.assertEquals(startIndex, tree.addSequence(idGen, new int[]{}));
        Assert.assertEquals(startIndex+1, tree.addSequence(idGen, new int[]{s(1), s(2), s(11), s(3), s(4), s(6)}));
        Assert.assertEquals(startIndex+2, tree.addSequence(idGen, new int[]{s(1), s(2), s(11), s(3), s(4), s(7), s(5)}));
        Assert.assertEquals(startIndex+3, tree.addSequence(idGen, new int[]{s(1), s(2), s(11), s(7), s(5), s(6)}));
        Assert.assertEquals(startIndex+4, tree.addSequence(idGen, new int[]{s(1), s(2), s(11), s(7), s(5), s(6), s(9), s(8), s(10)}));
        Assert.assertEquals(startIndex+5, tree.addSequence(idGen, new int[]{s(1), s(2), s(11), s(7)}));
        Assert.assertEquals(startIndex+6, tree.addSequence(idGen, new int[]{s(1), s(2), s(10), s(7)}));
        // add a trace that contains a previously determined starting element (1);
        // this should stop the addition of this sequence after adding [12,14] 
        // and add the sequence [1,2,14,15] instead
        Assert.assertEquals(startIndex+7, tree.addSequence(idGen, new int[]{s(12), s(14), s(1), s(2), s(14), s(15)}));
        // add a trace that contains both 1 and 12 as previously identified starting elements;
        // this should add the sequences [12,14], [1,3], [12,7]
        Assert.assertEquals(startIndex+8, tree.addSequence(idGen, new int[]{s(12), s(14), s(1), s(3), s(12), s(7)}));
        Assert.assertEquals(startIndex+9, tree.addSequence(idGen, new int[]{s(1), s(2), s(10), s(7), s(29)}));
        Assert.assertEquals(startIndex+10, tree.addSequence(idGen, new int[]{s(1), s(2), s(11), s(33)}));
        Assert.assertEquals(startIndex+5, tree.addSequence(idGen, new int[]{s(1), s(2), s(11), s(7)}));
        Assert.assertEquals(startIndex+6, tree.addSequence(idGen, new int[]{s(1), s(2), s(10), s(7)}));
        
        Assert.assertEquals(startIndex+11, tree.addSequence(idGen, new int[]{s(1), s(1)}));
        Assert.assertEquals(startIndex+11, tree.addSequence(idGen, new int[]{s(1), s(1)}));

        Assert.assertEquals(12, tree.getNumberOfSequences());
        
        System.out.print(tree);
        System.out.println();
        
        for (Pair<Integer, int[]> pair : tree) {
        	System.out.println(pair.first() + ": " + Arrays.toString(pair.second()));	
		}

//        GS Tree: 11 sequences
//        >(#1)
//        >>1,2,>11,>3,4,>6,(#2)
//        >>1,2,>11,>3,4,>7,5,(#3)
//        >>1,2,>11,>7,(#6)
//        >>1,2,>11,>7,>5,6,(#4)
//        >>1,2,>11,>7,>5,6,>9,8,10,(#5)
//        >>1,2,>11,>33,(#11)
//        >>1,2,>10,7,(#7)
//        >>1,2,>10,7,>29,(#10)
//        >>12,14,1,>2,14,15,(#8)
//        >>12,14,1,>3,12,7,(#9)
    }


}
