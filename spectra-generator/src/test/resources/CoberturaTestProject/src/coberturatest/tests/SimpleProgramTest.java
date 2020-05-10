/**
 *
 */
package coberturatest.tests;

import cobertura.test.SimpleProgram;
import org.junit.*;

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
        Assert.assertEquals(SimpleProgram.add(12, 3), 15);
        Assert.assertEquals(SimpleProgram.add(-1, 4), 3);
    }

    @Test
    public void testWeirdStuff() throws Exception {
        Assert.assertEquals(null, SimpleProgram.toBooleanObject((String) null));
        Assert.assertEquals(null, SimpleProgram.toBooleanObject(""));
        Assert.assertEquals(Boolean.FALSE, SimpleProgram.toBooleanObject("false"));
        Assert.assertEquals(Boolean.FALSE, SimpleProgram.toBooleanObject("no"));
        Assert.assertEquals(Boolean.FALSE, SimpleProgram.toBooleanObject("off"));
        Assert.assertEquals(Boolean.FALSE, SimpleProgram.toBooleanObject("FALSE"));
        Assert.assertEquals(Boolean.FALSE, SimpleProgram.toBooleanObject("NO"));
        Assert.assertEquals(Boolean.FALSE, SimpleProgram.toBooleanObject("OFF"));
        Assert.assertEquals(null, SimpleProgram.toBooleanObject("oof"));
        Assert.assertEquals(Boolean.TRUE, SimpleProgram.toBooleanObject("true"));
        Assert.assertEquals(Boolean.TRUE, SimpleProgram.toBooleanObject("yes"));
        Assert.assertEquals(Boolean.TRUE, SimpleProgram.toBooleanObject("on"));
        Assert.assertEquals(Boolean.TRUE, SimpleProgram.toBooleanObject("TRUE"));
        Assert.assertEquals(Boolean.TRUE, SimpleProgram.toBooleanObject("ON"));
        Assert.assertEquals(Boolean.TRUE, SimpleProgram.toBooleanObject("YES"));
        Assert.assertEquals(Boolean.TRUE, SimpleProgram.toBooleanObject("TruE"));
        Assert.assertEquals(Boolean.TRUE, SimpleProgram.toBooleanObject("TruE"));

        Assert.assertEquals(Boolean.TRUE, SimpleProgram.toBooleanObject("y"));
        Assert.assertEquals(Boolean.TRUE, SimpleProgram.toBooleanObject("Y"));
        Assert.assertEquals(Boolean.TRUE, SimpleProgram.toBooleanObject("t"));
        Assert.assertEquals(Boolean.TRUE, SimpleProgram.toBooleanObject("T"));
        Assert.assertEquals(Boolean.FALSE, SimpleProgram.toBooleanObject("f"));
        Assert.assertEquals(Boolean.FALSE, SimpleProgram.toBooleanObject("F"));
        Assert.assertEquals(Boolean.FALSE, SimpleProgram.toBooleanObject("n"));
        Assert.assertEquals(Boolean.FALSE, SimpleProgram.toBooleanObject("N"));
        Assert.assertEquals(null, SimpleProgram.toBooleanObject("z"));

        Assert.assertEquals(null, SimpleProgram.toBooleanObject("ab"));
        Assert.assertEquals(null, SimpleProgram.toBooleanObject("yoo"));
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
        Assert.assertEquals(SimpleProgram.subtract(3, 4), -1);
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
