package se.de.hu_berlin.informatik.aspectj.frontend.evaluation.sbfl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.ibugs.IBugsFaultLocationCollection;
import se.de.hu_berlin.informatik.benchmark.Bug;
import se.de.hu_berlin.informatik.benchmark.FaultInformation.Suspiciousness;
import se.de.hu_berlin.informatik.benchmark.FileWithFaultLocations;
import se.de.hu_berlin.informatik.benchmark.LineWithFaultInformation;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;

/**
 * @author Simon
 *
 */
public class IBugsFaultLocationsTest extends TestSettings {

	/**
     */
	@BeforeClass
	public static void setUpBeforeClass() {
	}

	/**
     */
	@AfterClass
	public static void tearDownAfterClass() {
//		deleteTestOutputs();
	}
	
	private IBugsFaultLocationCollection p;

	/**
	 * @throws java.lang.Exception
     * in case of an exception...
	 */
	@Before
	public void setUp() throws Exception {
		this.p = new IBugsFaultLocationCollection(getStdResourcesDir() + "/fk/stardust/evaluation/ibugs/rflSample01.xml");
	}
	
	/**
     */
	@After
	public void tearDown() {
		this.p = null;
//		deleteTestOutputs();
	}


    @Test
    public void testNothingInside() {
        final int bugId = 102710;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(0, this.p.getBug(bugId).getFaultyFiles().size());
    }

    @Test
    public void testEmptyFile() {
        final int bugId = 102711;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(0, this.p.getBug(bugId).getFaultyFiles().size());
    }

    @Test
    public void testValidFilenameButNoLines() {
        final int bugId = 102712;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFaultyFiles().size());
        Assert.assertEquals(0, this.p.getBug(bugId).getFaultyFiles().get(0).getFaultyLines().size());
    }

    @Test
    public void testInvalidFilenameButLines() {
        final int bugId = 102713;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(0, this.p.getBug(bugId).getFaultyFiles().size());
    }

    @Test
    public void testNoFileExtension() {
        final int bugId = 102714;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(0, this.p.getBug(bugId).getFaultyFiles().size());
    }

    @Test
    public void testSingleFileSingleLine() {
        final int bugId = 102715;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFaultyFiles().size());

        final FileWithFaultLocations firstFile = this.p.getBug(bugId).getFaultyFiles().get(0);
        Assert.assertEquals(1, firstFile.getFaultyLines().size());

        final LineWithFaultInformation firstLine = firstFile.getFaultyLines().get(0);
        Assert.assertEquals(1182, firstLine.getLineNo());
        Assert.assertEquals(Suspiciousness.HIGH, firstLine.getSuspiciousness());
        Assert.assertEquals("is now on else if", firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineEmptyComment() {
        final int bugId = 102716;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFaultyFiles().size());

        final FileWithFaultLocations firstFile = this.p.getBug(bugId).getFaultyFiles().get(0);
        Assert.assertEquals(1, firstFile.getFaultyLines().size());

        final LineWithFaultInformation firstLine = firstFile.getFaultyLines().get(0);
        Assert.assertEquals(1182, firstLine.getLineNo());
        Assert.assertEquals(Suspiciousness.HIGH, firstLine.getSuspiciousness());
        Assert.assertEquals("", firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineMissingComment() {
        final int bugId = 102717;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFaultyFiles().size());

        final FileWithFaultLocations firstFile = this.p.getBug(bugId).getFaultyFiles().get(0);
        Assert.assertEquals(1, firstFile.getFaultyLines().size());

        final LineWithFaultInformation firstLine = firstFile.getFaultyLines().get(0);
        Assert.assertEquals(1182, firstLine.getLineNo());
        Assert.assertEquals(Suspiciousness.HIGH, firstLine.getSuspiciousness());
        Assert.assertNull(firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineInvalidSuspiciousness() {
        final int bugId = 102718;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFaultyFiles().size());

        final FileWithFaultLocations firstFile = this.p.getBug(bugId).getFaultyFiles().get(0);
        Assert.assertEquals(1, firstFile.getFaultyLines().size());

        final LineWithFaultInformation firstLine = firstFile.getFaultyLines().get(0);
        Assert.assertEquals(1182, firstLine.getLineNo());
        Assert.assertEquals(Suspiciousness.UNKNOWN, firstLine.getSuspiciousness());
        Assert.assertEquals("added new else if branch", firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineMissingSuspiciousness() {
        final int bugId = 102719;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFaultyFiles().size());

        final FileWithFaultLocations firstFile = this.p.getBug(bugId).getFaultyFiles().get(0);
        Assert.assertEquals(1, firstFile.getFaultyLines().size());

        final LineWithFaultInformation firstLine = firstFile.getFaultyLines().get(0);
        Assert.assertEquals(1182, firstLine.getLineNo());
        Assert.assertEquals(Suspiciousness.UNKNOWN, firstLine.getSuspiciousness());
        Assert.assertEquals("added new else if branch", firstLine.getComment());
    }

    @Test
    public void testSingleFileSingleLineInvalidLineNumber() {
        final int bugId = 102720;
        Assert.assertTrue(this.p.hasBug(bugId));
        Assert.assertEquals(1, this.p.getBug(bugId).getFaultyFiles().size());

        final FileWithFaultLocations firstFile = this.p.getBug(bugId).getFaultyFiles().get(0);
        Assert.assertEquals(0, firstFile.getFaultyLines().size());
    }

    @Test
    public void testMultiFileMultiLine() {
        final int bugId = 102721;
        Assert.assertTrue(this.p.hasBug(bugId));
        final Bug bug = this.p.getBug(bugId);
        Assert.assertEquals(2, bug.getFaultyFiles().size());
        Assert.assertTrue(bug.hasFile("org/aspectj/weaver/patterns/PointcutRewriter.java"));
        Assert.assertTrue(bug
                .hasFile("org/aspectj/ajdt/internal/compiler/ast/ValidateAtAspectJAnnotationsVisitor.java"));
        Assert.assertFalse(bug.hasFile("nonexisting.java"));

        final FileWithFaultLocations firstFile = bug.getFile("org/aspectj/weaver/patterns/PointcutRewriter.java");
        Assert.assertEquals(2, firstFile.getFaultyLines().size());

        final FileWithFaultLocations secondFile = bug
                .getFile("org/aspectj/ajdt/internal/compiler/ast/ValidateAtAspectJAnnotationsVisitor.java");
        Assert.assertEquals(1, secondFile.getFaultyLines().size());

    }


}
