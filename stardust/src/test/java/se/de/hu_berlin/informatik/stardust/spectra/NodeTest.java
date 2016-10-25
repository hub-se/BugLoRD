/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.spectra;

import org.junit.Assert;
import org.junit.Test;

import fk.stardust.test.data.SimpleSpectraProvider;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;

public class NodeTest {

    /**
     * Test data taken from Table 1 from:
     * 
     * Lee Naish, Hua Jie Lee, and Kotagiri Ramamohanarao. 2011. A model for spectra-based software diagnosis. ACM
     * Trans. Softw. Eng. Methodol. 20, 3, Article 11 (August 2011), 32 pages. DOI=10.1145/2000791.2000795
     * http://doi.acm.org/10.1145/2000791.2000795
     * 
     * @throws Exception
     * 
     * @see http://dl.acm.org/citation.cfm?id=2000795
     */
    @Test
    public void computeINFSMetricsForSimpleSpectra() throws Exception {
        final ISpectra<String> s = new SimpleSpectraProvider().loadSpectra();

        Assert.assertTrue(s.hasNode("S1"));
        Assert.assertTrue(s.hasNode("S2"));
        Assert.assertTrue(s.hasNode("S3"));

        Assert.assertEquals(s.getNode("S1").getNP(), 0);
        Assert.assertEquals(s.getNode("S1").getNF(), 1);
        Assert.assertEquals(s.getNode("S1").getEP(), 3);
        Assert.assertEquals(s.getNode("S1").getEF(), 1);

        Assert.assertEquals(s.getNode("S2").getNP(), 2);
        Assert.assertEquals(s.getNode("S2").getNF(), 0);
        Assert.assertEquals(s.getNode("S2").getEP(), 1);
        Assert.assertEquals(s.getNode("S2").getEF(), 2);

        Assert.assertEquals(s.getNode("S3").getNP(), 1);
        Assert.assertEquals(s.getNode("S3").getNF(), 1);
        Assert.assertEquals(s.getNode("S3").getEP(), 2);
        Assert.assertEquals(s.getNode("S3").getEF(), 1);
    }

    @Test
    public void computeForSpectraWithoutTraces() {
        final ISpectra<String> s = new Spectra<>();
        final INode<String> n = s.getNode("sampleNode");
        Assert.assertEquals(n.getNP(), 0);
        Assert.assertEquals(n.getNF(), 0);
        Assert.assertEquals(n.getEP(), 0);
        Assert.assertEquals(n.getEF(), 0);
    }
}
