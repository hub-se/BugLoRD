/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking;

import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.SBFLNormalizedRanking;
import se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking.NodeRanking;
import se.de.hu_berlin.informatik.spectra.core.hit.HitSpectra;
import se.de.hu_berlin.informatik.spectra.core.hit.HitTrace;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SBFLNormalizedRankingTest {

    private HitSpectra<String> data;
    private final double smallDelta = 0.00001;

    @Before
    public void before() {
        this.data = this.data();
    }

    @After
    public void after() {
        this.data = null;
    }

    @Test
    public void getZeroOneNoModification() {
        final NodeRanking<String> ranking = new NodeRanking<>();

        ranking.add(this.data.getOrCreateNode("S1"), 0.0);
        ranking.add(this.data.getOrCreateNode("S2"), 0.2);
        ranking.add(this.data.getOrCreateNode("S3"), 0.3);
        ranking.add(this.data.getOrCreateNode("S4"), 1.0);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ZeroToOneRankingValue);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getOrCreateNode("S1")), smallDelta);
        Assert.assertEquals(0.2d, n.getRankingValue(this.data.getOrCreateNode("S2")), smallDelta);
        Assert.assertEquals(0.3d, n.getRankingValue(this.data.getOrCreateNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getOrCreateNode("S4")), smallDelta);
    }

    @Test
    public void getZeroOneDivideByTwo() {
        final NodeRanking<String> ranking = new NodeRanking<>();

        ranking.add(this.data.getOrCreateNode("S1"), 0.0);
        ranking.add(this.data.getOrCreateNode("S2"), 0.5);
        ranking.add(this.data.getOrCreateNode("S3"), 1.0);
        ranking.add(this.data.getOrCreateNode("S4"), 2.0);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ZeroToOneRankingValue);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getOrCreateNode("S1")), smallDelta);
        Assert.assertEquals(0.25d, n.getRankingValue(this.data.getOrCreateNode("S2")), smallDelta);
        Assert.assertEquals(0.5d, n.getRankingValue(this.data.getOrCreateNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getOrCreateNode("S4")), smallDelta);
    }

    @Test
    public void getZeroOneWithNegativeSusp() {
        final NodeRanking<String> ranking = new NodeRanking<>();

        ranking.add(this.data.getOrCreateNode("S1"), -1.0);
        ranking.add(this.data.getOrCreateNode("S2"), 0);
        ranking.add(this.data.getOrCreateNode("S3"), 0.5);
        ranking.add(this.data.getOrCreateNode("S4"), 1.0);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ZeroToOneRankingValue);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getOrCreateNode("S1")), smallDelta);
        Assert.assertEquals(0.5d, n.getRankingValue(this.data.getOrCreateNode("S2")), smallDelta);
        Assert.assertEquals(0.75d, n.getRankingValue(this.data.getOrCreateNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getOrCreateNode("S4")), smallDelta);
    }

    @Test
    public void getZeroOneWithInfinity() {
        final NodeRanking<String> ranking = new NodeRanking<>();

        ranking.add(this.data.getOrCreateNode("S1"), Double.NEGATIVE_INFINITY);
        ranking.add(this.data.getOrCreateNode("S2"), 0);
        ranking.add(this.data.getOrCreateNode("S3"), 0.5);
        ranking.add(this.data.getOrCreateNode("S4"), Double.POSITIVE_INFINITY);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ZeroToOneRankingValue);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getOrCreateNode("S1")), smallDelta);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getOrCreateNode("S2")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getOrCreateNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getOrCreateNode("S4")), smallDelta);
    }

    @Test
    public void getZeroOneWithMultipleInfinity() {
        final NodeRanking<String> ranking = new NodeRanking<>();

        ranking.add(this.data.getOrCreateNode("S1"), Double.NEGATIVE_INFINITY);
        ranking.add(this.data.getOrCreateNode("S2"), Double.NEGATIVE_INFINITY);
        ranking.add(this.data.getOrCreateNode("S3"), 0);
        ranking.add(this.data.getOrCreateNode("S4"), 0.5);
        ranking.add(this.data.getOrCreateNode("S5"), Double.POSITIVE_INFINITY);
        ranking.add(this.data.getOrCreateNode("S6"), Double.POSITIVE_INFINITY);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ZeroToOneRankingValue);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getOrCreateNode("S1")), smallDelta);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getOrCreateNode("S2")), smallDelta);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getOrCreateNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getOrCreateNode("S4")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getOrCreateNode("S5")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getOrCreateNode("S6")), smallDelta);
    }

    @Test
    public void getReciprocalNoModification() {
        final NodeRanking<String> ranking = new NodeRanking<>();

        ranking.add(this.data.getOrCreateNode("S1"), 0.25);
        ranking.add(this.data.getOrCreateNode("S2"), 1.0d / 3.0d);
        ranking.add(this.data.getOrCreateNode("S3"), 0.5);
        ranking.add(this.data.getOrCreateNode("S4"), 1.0);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ReciprocalRankWorst);
        Assert.assertEquals(0.25d, n.getRankingValue(this.data.getOrCreateNode("S1")), smallDelta);
        Assert.assertEquals(1.0d / 3.0d, n.getRankingValue(this.data.getOrCreateNode("S2")), smallDelta);
        Assert.assertEquals(0.5d, n.getRankingValue(this.data.getOrCreateNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getOrCreateNode("S4")), smallDelta);
    }

    @Test
    public void getReciprocalDivideByTwo() {
        final NodeRanking<String> ranking = new NodeRanking<>();

        ranking.add(this.data.getOrCreateNode("S1"), 0.0);
        ranking.add(this.data.getOrCreateNode("S2"), 0.5);
        ranking.add(this.data.getOrCreateNode("S3"), 1.0);
        ranking.add(this.data.getOrCreateNode("S4"), 2.0);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ReciprocalRankWorst);
        Assert.assertEquals(0.25d, n.getRankingValue(this.data.getOrCreateNode("S1")), smallDelta);
        Assert.assertEquals(1.0d / 3.0d, n.getRankingValue(this.data.getOrCreateNode("S2")), smallDelta);
        Assert.assertEquals(0.5d, n.getRankingValue(this.data.getOrCreateNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getOrCreateNode("S4")), smallDelta);
    }

    @Test
    public void getReciprocalWithNegativeSusp() {
        final NodeRanking<String> ranking = new NodeRanking<>();

        ranking.add(this.data.getOrCreateNode("S1"), -1.0);
        ranking.add(this.data.getOrCreateNode("S2"), 0);
        ranking.add(this.data.getOrCreateNode("S3"), 0.5);
        ranking.add(this.data.getOrCreateNode("S4"), 1.0);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ReciprocalRankWorst);
        Assert.assertEquals(0.25d, n.getRankingValue(this.data.getOrCreateNode("S1")), smallDelta);
        Assert.assertEquals(1.0d / 3.0d, n.getRankingValue(this.data.getOrCreateNode("S2")), smallDelta);
        Assert.assertEquals(0.5d, n.getRankingValue(this.data.getOrCreateNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getOrCreateNode("S4")), smallDelta);
    }

    @Test
    public void getReciprocalWithInfinity() {
        final NodeRanking<String> ranking = new NodeRanking<>();

        ranking.add(this.data.getOrCreateNode("S1"), Double.NEGATIVE_INFINITY);
        ranking.add(this.data.getOrCreateNode("S2"), 0);
        ranking.add(this.data.getOrCreateNode("S3"), 0.5);
        ranking.add(this.data.getOrCreateNode("S4"), Double.POSITIVE_INFINITY);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ReciprocalRankWorst);
        Assert.assertEquals(0.25d, n.getRankingValue(this.data.getOrCreateNode("S1")), smallDelta);
        Assert.assertEquals(1.0d / 3.0d, n.getRankingValue(this.data.getOrCreateNode("S2")), smallDelta);
        Assert.assertEquals(0.5d, n.getRankingValue(this.data.getOrCreateNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getOrCreateNode("S4")), smallDelta);
    }


    private HitSpectra<String> data() {
        final HitSpectra<String> s = new HitSpectra<>(null);
        final HitTrace<String> t1 = s.addTrace("trace1", 1, false);
        t1.setInvolvement("S1", true);
        t1.setInvolvement("S2", true);
        t1.setInvolvement("S3", true);
        t1.setInvolvement("S4", true);
        t1.setInvolvement("S5", true);
        t1.setInvolvement("S6", true);
        return s;
    }
}
