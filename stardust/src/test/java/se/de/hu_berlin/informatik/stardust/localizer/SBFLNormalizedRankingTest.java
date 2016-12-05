/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer;

import se.de.hu_berlin.informatik.benchmark.ranking.SimpleNormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.stardust.localizer.SBFLNormalizedRanking;
import se.de.hu_berlin.informatik.stardust.spectra.IMutableTrace;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SBFLNormalizedRankingTest {

    private ISpectra<String> data;
    private double smallDelta = 0.00001;

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
        final SBFLRanking<String> ranking = new SBFLRanking<>();

        ranking.add(this.data.getNode("S1"), 0.0);
        ranking.add(this.data.getNode("S2"), 0.2);
        ranking.add(this.data.getNode("S3"), 0.3);
        ranking.add(this.data.getNode("S4"), 1.0);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ZeroToOne);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getNode("S1")), smallDelta);
        Assert.assertEquals(0.2d, n.getRankingValue(this.data.getNode("S2")), smallDelta);
        Assert.assertEquals(0.3d, n.getRankingValue(this.data.getNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getNode("S4")), smallDelta);
    }

    @Test
    public void getZeroOneDivideByTwo() {
        final SBFLRanking<String> ranking = new SBFLRanking<>();

        ranking.add(this.data.getNode("S1"), 0.0);
        ranking.add(this.data.getNode("S2"), 0.5);
        ranking.add(this.data.getNode("S3"), 1.0);
        ranking.add(this.data.getNode("S4"), 2.0);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ZeroToOne);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getNode("S1")), smallDelta);
        Assert.assertEquals(0.25d, n.getRankingValue(this.data.getNode("S2")), smallDelta);
        Assert.assertEquals(0.5d, n.getRankingValue(this.data.getNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getNode("S4")), smallDelta);
    }

    @Test
    public void getZeroOneWithNegativeSusp() {
        final SBFLRanking<String> ranking = new SBFLRanking<>();

        ranking.add(this.data.getNode("S1"), -1.0);
        ranking.add(this.data.getNode("S2"), 0);
        ranking.add(this.data.getNode("S3"), 0.5);
        ranking.add(this.data.getNode("S4"), 1.0);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ZeroToOne);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getNode("S1")), smallDelta);
        Assert.assertEquals(0.5d, n.getRankingValue(this.data.getNode("S2")), smallDelta);
        Assert.assertEquals(0.75d, n.getRankingValue(this.data.getNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getNode("S4")), smallDelta);
    }

    @Test
    public void getZeroOneWithInfinity() {
        final SBFLRanking<String> ranking = new SBFLRanking<>();

        ranking.add(this.data.getNode("S1"), Double.NEGATIVE_INFINITY);
        ranking.add(this.data.getNode("S2"), 0);
        ranking.add(this.data.getNode("S3"), 0.5);
        ranking.add(this.data.getNode("S4"), Double.POSITIVE_INFINITY);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ZeroToOne);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getNode("S1")), smallDelta);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getNode("S2")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getNode("S4")), smallDelta);
    }

    @Test
    public void getZeroOneWithMultipleInfinity() {
        final SBFLRanking<String> ranking = new SBFLRanking<>();

        ranking.add(this.data.getNode("S1"), Double.NEGATIVE_INFINITY);
        ranking.add(this.data.getNode("S2"), Double.NEGATIVE_INFINITY);
        ranking.add(this.data.getNode("S3"), 0);
        ranking.add(this.data.getNode("S4"), 0.5);
        ranking.add(this.data.getNode("S5"), Double.POSITIVE_INFINITY);
        ranking.add(this.data.getNode("S6"), Double.POSITIVE_INFINITY);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ZeroToOne);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getNode("S1")), smallDelta);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getNode("S2")), smallDelta);
        Assert.assertEquals(0.0d, n.getRankingValue(this.data.getNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getNode("S4")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getNode("S5")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getNode("S6")), smallDelta);
    }

    @Test
    public void getReciprocalNoModification() {
        final SBFLRanking<String> ranking = new SBFLRanking<>();

        ranking.add(this.data.getNode("S1"), 0.25);
        ranking.add(this.data.getNode("S2"), 1.0d / 3.0d);
        ranking.add(this.data.getNode("S3"), 0.5);
        ranking.add(this.data.getNode("S4"), 1.0);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ReciprocalRank);
        Assert.assertEquals(0.25d, n.getRankingValue(this.data.getNode("S1")), smallDelta);
        Assert.assertEquals(1.0d / 3.0d, n.getRankingValue(this.data.getNode("S2")), smallDelta);
        Assert.assertEquals(0.5d, n.getRankingValue(this.data.getNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getNode("S4")), smallDelta);
    }

    @Test
    public void getReciprocalDivideByTwo() {
        final SBFLRanking<String> ranking = new SBFLRanking<>();

        ranking.add(this.data.getNode("S1"), 0.0);
        ranking.add(this.data.getNode("S2"), 0.5);
        ranking.add(this.data.getNode("S3"), 1.0);
        ranking.add(this.data.getNode("S4"), 2.0);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ReciprocalRank);
        Assert.assertEquals(0.25d, n.getRankingValue(this.data.getNode("S1")), smallDelta);
        Assert.assertEquals(1.0d / 3.0d, n.getRankingValue(this.data.getNode("S2")), smallDelta);
        Assert.assertEquals(0.5d, n.getRankingValue(this.data.getNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getNode("S4")), smallDelta);
    }

    @Test
    public void getReciprocalWithNegativeSusp() {
        final SBFLRanking<String> ranking = new SBFLRanking<>();

        ranking.add(this.data.getNode("S1"), -1.0);
        ranking.add(this.data.getNode("S2"), 0);
        ranking.add(this.data.getNode("S3"), 0.5);
        ranking.add(this.data.getNode("S4"), 1.0);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ReciprocalRank);
        Assert.assertEquals(0.25d, n.getRankingValue(this.data.getNode("S1")), smallDelta);
        Assert.assertEquals(1.0d / 3.0d, n.getRankingValue(this.data.getNode("S2")), smallDelta);
        Assert.assertEquals(0.5d, n.getRankingValue(this.data.getNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getNode("S4")), smallDelta);
    }

    @Test
    public void getReciprocalWithInfinity() {
        final SBFLRanking<String> ranking = new SBFLRanking<>();

        ranking.add(this.data.getNode("S1"), Double.NEGATIVE_INFINITY);
        ranking.add(this.data.getNode("S2"), 0);
        ranking.add(this.data.getNode("S3"), 0.5);
        ranking.add(this.data.getNode("S4"), Double.POSITIVE_INFINITY);

        final SBFLNormalizedRanking<String> n = new SBFLNormalizedRanking<>(ranking, NormalizationStrategy.ReciprocalRank);
        Assert.assertEquals(0.25d, n.getRankingValue(this.data.getNode("S1")), smallDelta);
        Assert.assertEquals(1.0d / 3.0d, n.getRankingValue(this.data.getNode("S2")), smallDelta);
        Assert.assertEquals(0.5d, n.getRankingValue(this.data.getNode("S3")), smallDelta);
        Assert.assertEquals(1.0d, n.getRankingValue(this.data.getNode("S4")), smallDelta);
    }


    private ISpectra<String> data() {
        final Spectra<String> s = new Spectra<>();
        final IMutableTrace<String> t1 = s.addTrace("trace1", false);
        t1.setInvolvement("S1", true);
        t1.setInvolvement("S2", true);
        t1.setInvolvement("S3", true);
        t1.setInvolvement("S4", true);
        t1.setInvolvement("S5", true);
        t1.setInvolvement("S6", true);
        return s;
    }
}
