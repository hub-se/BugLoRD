package se.de.hu_berlin.informatik.benchmark.ranking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.benchmark.ranking.Ranking;
import se.de.hu_berlin.informatik.benchmark.ranking.RankingMetric;

public class NormalizedRanking<T> extends SimpleRanking<T> {

    public enum NormalizationStrategy {
        ZeroOne,
        ReciprocalRank,
    }

    /** Holds the strategy to use */
    private final NormalizationStrategy strategy;

    public NormalizedRanking(Boolean ascending, final NormalizationStrategy strategy, final Ranking<T> toNormalize) {
        this(ascending, strategy);
        addAll(toNormalize.getElementMap());
    }
    
    public NormalizedRanking(Boolean ascending, final NormalizationStrategy strategy) {
        super(ascending);
        this.strategy = strategy;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public double getRankingValue(final T node) {
    	return normalizeSuspiciousness(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RankingMetric<T> getRankingMetrics(final T node) {
        final RankingMetric<T> metric = super.getRankingMetrics(node);
        final double susNorm = this.normalizeSuspiciousness(metric);
        return new SimpleRankingMetric<T>(metric.getElement(), metric.getBestRanking(), metric.getRanking(), metric.getWorstRanking(), susNorm, getElementMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ranking<T> merge(final Ranking<T> other) {
    	final Ranking<T> merged = new NormalizedRanking<T>(this.isAscending(), strategy);
    	merged.addAll(this.getElementMap());
        merged.addAll(other.getElementMap());
        return merged;
    }

    private double normalizeSuspiciousness(final RankingMetric<T> metric) {
        switch (this.strategy) {
        case ReciprocalRank:
            return 1.0d / metric.getWorstRanking();
        case ZeroOne:
        	return getZeroOneSuspiciousness(metric.getRankingValue());
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }
    
    private double normalizeSuspiciousness(final T node) {
    	switch (this.strategy) {
        case ReciprocalRank:
            return 1.0d / super.getRankingMetrics(node).getWorstRanking();
        case ZeroOne:
        	return getZeroOneSuspiciousness(super.getRankingValue(node));
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }
    
    private double normalizeSuspiciousness(final T node, double rankingValue) {
    	switch (this.strategy) {
        case ReciprocalRank:
            return 1.0d / super.getRankingMetrics(node).getWorstRanking();
        case ZeroOne:
        	return getZeroOneSuspiciousness(rankingValue);
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }

	private double getZeroOneSuspiciousness(final double curSusp) {
		final double suspMax = super.getBestFiniteRankingValue();
		final double suspMin = super.getWorstFiniteRankingValue();

		if (Double.isInfinite(curSusp)) {
		    if (curSusp < 0) {
		        return 0.0d;
		    } else {
		        return 1.0d;
		    }
		} else if (Double.isNaN(curSusp)) {
		    return Double.NaN;
		} else if (Double.compare(suspMax, suspMin) == 0) {
			return 0.5d;
		} else {
			return (curSusp - suspMin) / (suspMax - suspMin);
		}
	}

	@Override
	public double getBestRankingValue() {
		return normalizeSuspiciousness(getBestRankingElement(), super.getBestRankingValue());
	}

	@Override
	public double getWorstRankingValue() {
		return normalizeSuspiciousness(getWorstRankingElement(), super.getWorstRankingValue());
	}

	@Override
	public double getBestFiniteRankingValue() {
		return normalizeSuspiciousness(getBestFiniteRankingElement(), super.getBestFiniteRankingValue());
	}

	@Override
	public double getWorstFiniteRankingValue() {
		return normalizeSuspiciousness(getWorstFiniteRankingElement(), super.getWorstFiniteRankingValue());
	}
    
	@Override
	public List<RankedElement<T>> getSortedRankedElements(boolean ascending) {
		return sortRankedElements(ascending);
	}
	
	private List<RankedElement<T>> sortRankedElements(boolean ascending) {
		List<RankedElement<T>> rankedNodes = new ArrayList<>(getElementMap().size());
		//fill the list with elements with normalized ranking values
		for (Entry<T, Double> entry : getElementMap().entrySet()) {
			rankedNodes.add(new SimpleRankedElement<>(entry.getKey(), normalizeSuspiciousness(entry.getKey(), entry.getValue())));
		}
		//sort the list
		return sortRankedElementList(ascending, rankedNodes);
	}
    
}
