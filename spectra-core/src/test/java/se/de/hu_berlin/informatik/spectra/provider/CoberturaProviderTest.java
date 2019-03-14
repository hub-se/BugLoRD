/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider;

import org.junit.Assert;
import org.junit.Test;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.CoberturaSpectraProviderFactory;
import se.de.hu_berlin.informatik.spectra.provider.cobertura.xml.CoberturaXMLProvider;

public class CoberturaProviderTest {

    @Test
    public void loadSimpleCoverage() {
        final CoberturaXMLProvider<HitTrace<SourceCodeBlock>> c = CoberturaSpectraProviderFactory.getHitSpectraFromXMLProvider(true);
        c.addData("src/test/resources/fk/stardust/provider/simple-coverage.xml", "simple", true);
        final ISpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> s = c.loadSpectra();
        checkSimpleNodes(s);
        checkSimpleTraceNormal(s);
    }

	private static void checkSimpleNodes(final ISpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> s) {
		// assert loaded count is correct
        Assert.assertEquals(s.getNodes().size(), 3);
        Assert.assertEquals(s.getTraces().size(), 1);

        // assert we have nodes
        Assert.assertTrue(s.hasNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "<init>()V", 3)));
        Assert.assertTrue(s.hasNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "main([Ljava/lang/String;)V", 9)));
        Assert.assertTrue(s.hasNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "main([Ljava/lang/String;)V", 10)));
	}

	private static void checkSimpleTraceNormal(final ISpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> s) {
		// assert trace has correct involvement loaded
        final ITrace<SourceCodeBlock> t = s.getTraces().iterator().next();
        Assert.assertFalse(t.isInvolved(s.getOrCreateNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "<init>()V", 3))));
        Assert.assertTrue(t.isInvolved(s.getOrCreateNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "main([Ljava/lang/String;)V", 9))));
        Assert.assertTrue(t.isInvolved(s.getOrCreateNode(new SourceCodeBlock("cobertura", "cobertura/CoverageTest.java", "main([Ljava/lang/String;)V", 10))));
        
        //assert that trace is loaded as 'successful'
        Assert.assertTrue(t.isSuccessful());
	}
	
    @Test
    public void loadLargeCoverage() {
        final CoberturaXMLProvider<HitTrace<SourceCodeBlock>> c = CoberturaSpectraProviderFactory.getHitSpectraFromXMLProvider(true);
        c.addData("src/test/resources/fk/stardust/provider/large-coverage.xml", "large", true);
        final ISpectra<SourceCodeBlock, ? super HitTrace<SourceCodeBlock>> s = c.loadSpectra();


//        List<INode<SourceCodeLine>> nodes = s.getNodes();
//        SourceCodeLine[] array = new SourceCodeLine[nodes.size()];
//        for (int i = 0; i < array.length; ++i) {
//        	array[i] = nodes.get(i).getIdentifier();
//        }
//        Arrays.sort(array);
//        for (SourceCodeLine line : array) {
//        	System.out.println(line);
//        }
        // assert loaded count is correct
        Assert.assertEquals(16245, s.getNodes().size());
        Assert.assertEquals(1, s.getTraces().size());

        // assert we have 3563 involved nodes
        // (match count of the regex 'hits="[^0]' on large-coverage.xml divided by 2, as all hits are mentioned twice)
        int count = 0;
        final ITrace<SourceCodeBlock> t = s.getTraces().iterator().next();
        for (final INode<SourceCodeBlock> node : s.getNodes()) {
            if (t.isInvolved(node)) {
                count++;
            }
        }
        Assert.assertEquals(count, 3563);
    }
}
