/**
 * 
 */
package se.de.hu_berlin.informatik.aspectj.frontend.evaluation.sbfl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs.IBugsFaultLocations;
import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs.IBugsFaultLocations.Bug;
import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs.IBugsFaultLocations.File;
import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs.IBugsFaultLocations.Line;
import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs.IBugsFaultLocations.Suspiciousness;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class IBugsFaultLocationsTest extends TestSettings {

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
	
	private IBugsFaultLocations p;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.p = new IBugsFaultLocations(getStdResourcesDir() + "/fk/stardust/evaluation/ibugs/rflSample01.xml");
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		this.p = null;
//		deleteTestOutputs();
	}


    @Test
    public void testNothingInside() {
        final int bugId = 102710;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(0, this.p.getBug(bugId).getFiles().size());
    }

    @Test
    public void testEmptyFile() {
        final int bugId = 102711;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(0, this.p.getBug(bugId).getFiles().size());
    }

    @Test
    public void testValidFilenameButNoLines() {
        final int bugId = 102712;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());
        Assert.assertEquals(0, this.p.getBug(bugId).getFiles().get(0).getLines().size());
    }

    @Test
    public void testInvalidFilenameButLines() {
        final int bugId = 102713;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(0, this.p.getBug(bugId).getFiles().size());
    }

    @Test
    public void testNoFileExtension() {
        final int bugId = 102714;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(0, this.p.getBug(bugId).getFiles().size());
    }

    @Test
    public void testSingleFileSingleLine() {
        final int bugId = 102715;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());

        final File firstFile = this.p.getBug(bugId).getFiles().get(0);
        Assert.assertEquals(1, firstFile.getLines().size());

        final Line firstLine = firstFile.getLines().get(0);
        Assert.assertEquals(1182, firstLine.getLine());
        Assert.assertEquals(Suspiciousness.HIGH, firstLine.getSuspiciousness());
        Assert.assertEquals("is now on else if", firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineEmptyComment() {
        final int bugId = 102716;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());

        final File firstFile = this.p.getBug(bugId).getFiles().get(0);
        Assert.assertEquals(1, firstFile.getLines().size());

        final Line firstLine = firstFile.getLines().get(0);
        Assert.assertEquals(1182, firstLine.getLine());
        Assert.assertEquals(Suspiciousness.HIGH, firstLine.getSuspiciousness());
        Assert.assertEquals("", firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineMissingComment() {
        final int bugId = 102717;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());

        final File firstFile = this.p.getBug(bugId).getFiles().get(0);
        Assert.assertEquals(1, firstFile.getLines().size());

        final Line firstLine = firstFile.getLines().get(0);
        Assert.assertEquals(1182, firstLine.getLine());
        Assert.assertEquals(Suspiciousness.HIGH, firstLine.getSuspiciousness());
        Assert.assertNull(firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineInvalidSuspiciousness() {
        final int bugId = 102718;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());

        final File firstFile = this.p.getBug(bugId).getFiles().get(0);
        Assert.assertEquals(1, firstFile.getLines().size());

        final Line firstLine = firstFile.getLines().get(0);
        Assert.assertEquals(1182, firstLine.getLine());
        Assert.assertEquals(Suspiciousness.UNKNOWN, firstLine.getSuspiciousness());
        Assert.assertEquals("added new else if branch", firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineMissingSuspiciousness() {
        final int bugId = 102719;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());

        final File firstFile = this.p.getBug(bugId).getFiles().get(0);
        Assert.assertEquals(1, firstFile.getLines().size());

        final Line firstLine = firstFile.getLines().get(0);
        Assert.assertEquals(1182, firstLine.getLine());
        Assert.assertEquals(Suspiciousness.UNKNOWN, firstLine.getSuspiciousness());
        Assert.assertEquals("added new else if branch", firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineInvalidLineNumber() {
        final int bugId = 102720;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFiles().size());

        final File firstFile = this.p.getBug(bugId).getFiles().get(0);
        Assert.assertEquals(0, firstFile.getLines().size());
    }

    @Test
    public void testMultiFileMultiLine() {
        final int bugId = 102721;
        Assert.assertTrue(this.p.hasBug(bugId));
        final Bug bug = this.p.getBug(bugId);
        Assert.assertEquals(2, bug.getFiles().size());
        Assert.assertTrue(bug.hasFile("org/aspectj/weaver/patterns/PointcutRewriter.java"));
        Assert.assertTrue(bug
                .hasFile("org/aspectj/ajdt/internal/compiler/ast/ValidateAtAspectJAnnotationsVisitor.java"));
        Assert.assertFalse(bug.hasFile("nonexisting.java"));

        final File firstFile = bug.getFile("org/aspectj/weaver/patterns/PointcutRewriter.java");
        Assert.assertEquals(2, firstFile.getLines().size());

        final File secondFile = bug
                .getFile("org/aspectj/ajdt/internal/compiler/ast/ValidateAtAspectJAnnotationsVisitor.java");
        Assert.assertEquals(1, secondFile.getLines().size());

    }


}
