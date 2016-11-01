/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer;

import java.util.Iterator;
import java.util.ListIterator;

import se.de.hu_berlin.informatik.benchmark.ranking.RankedElement;
import se.de.hu_berlin.informatik.benchmark.ranking.Ranking;
import se.de.hu_berlin.informatik.benchmark.ranking.RankingMetric;
import se.de.hu_berlin.informatik.stardust.spectra.INode;

public class NormalizedRanking<T> extends SBFLRanking<T> {

    public enum NormalizationStrategy {
        ZeroOne,
        ReciprocalRank,
    }

    /** Holds the strategy to use */
    private final NormalizationStrategy strategy;
    private Double __suspMax;
    private Double __suspMin;

    public NormalizedRanking(final Ranking<INode<T>> toNormalize, final NormalizationStrategy strategy) {
        super();
        this.strategy = strategy;
        addAll(toNormalize.getRankedElements());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public double getRankingValue(final INode<T> node) {
        return this.getRankingMetrics(node).getRankingValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RankingMetric<INode<T>> getRankingMetrics(final INode<T> node) {
        final RankingMetric<INode<T>> metric = super.getRankingMetrics(node);
        final double susNorm = this.normalizeSuspiciousness(metric);
        return new SBFLRankingMetric<T>(metric.getElement(), metric.getBestRanking(), metric.getRanking(), metric.getWorstRanking(), susNorm, getElementMap());
    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected void outdateRankingCache() {
        super.outdateRankingCache();
        this.__suspMax = null;
        this.__suspMin = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isRankingCacheOutdated() {
        return super.isRankingCacheOutdated() || this.__suspMax == null || this.__suspMin == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateRankingCache() {
        if (!this.isRankingCacheOutdated()) {
            return;
        }
        super.updateRankingCache();
        this.updateSuspMinMax();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ranking<INode<T>> merge(final Ranking<INode<T>> other) {
        // FIXME: (potentially?) incorrect, need to return instance of NormalizedRanking
        return super.merge(other);
    }

    private double normalizeSuspiciousness(final RankingMetric<INode<T>> metric) {
        final double curSusp = metric.getRankingValue();
        switch (this.strategy) {
        case ReciprocalRank:
            return 1.0d / metric.getWorstRanking();
        case ZeroOne:
            this.updateRankingCache();

            if (Double.isInfinite(curSusp)) {
                if (curSusp < 0) {
                    return 0.0d;
                } else {
                    return 1.0d;
                }
            }
            if (Double.isNaN(curSusp)) {
                return 0.0d;
            }
            if (this.__suspMax.compareTo(this.__suspMin) == 0) {
                return 0.5d;
            }
            return (curSusp - this.__suspMin) / (this.__suspMax - this.__suspMin);
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }

    private void updateSuspMinMax() {
        // max susp
        double suspMax;
        Iterator<RankedElement<INode<T>>> iterator = getRankedElements().iterator();
        RankedElement<INode<T>> max = iterator.next();
        while (max != null && (Double.isNaN(max.getRankingValue()) || Double.isInfinite(max.getRankingValue()))) {
        	max = iterator.next();
        }
        if (max == null) {
            suspMax = 1.0d;
        } else {
            suspMax = max.getRankingValue();
        }
        assert !Double.isInfinite(suspMax) && !Double.isNaN(suspMax);

        // min susp
        double suspMin;
        ListIterator<RankedElement<INode<T>>> revIterator = getRankedElements().listIterator(getRankedElements().size());
        RankedElement<INode<T>> min = revIterator.previous();
        while (min != null && (Double.isNaN(min.getRankingValue()) || Double.isInfinite(min.getRankingValue()))) {
            min = revIterator.previous();
        }
        if (min == null) {
            suspMin = 1.0d;
        } else {
            suspMin = min.getRankingValue();
        }
        assert !Double.isInfinite(suspMin) && !Double.isNaN(suspMin);
        this.__suspMax = suspMax;
        this.__suspMin = suspMin;
    }
    
}
