package se.de.hu_berlin.informatik.benchmark.ranking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import se.de.hu_berlin.informatik.benchmark.ranking.Ranking;
import se.de.hu_berlin.informatik.benchmark.ranking.RankingMetric;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class NormalizedRanking<T> implements Ranking<T> {

	final private Ranking<T> ranking;
	
    public static enum NormalizationStrategy {
        ZeroToOne,
        ReciprocalRank,
    }

    /** Holds the strategy to use */
    private final NormalizationStrategy strategy;

    /**
     * Creates a view on the given ranking. Changes to this ranking are
     * visible in the given ranking. The same holds for the other direction.
     * @param toNormalize
     * the ranking to normalize
     * @param strategy
     * the strategy to use
     */
    public NormalizedRanking(final Ranking<T> toNormalize, final NormalizationStrategy strategy) {
        super();
        if (toNormalize == null) {
        	Log.warn(this, "Given ranking is null. Creating new ascending ranking.");
        	this.ranking = new SimpleRanking<>(true);
        } else {
        	this.ranking = toNormalize;
        }
        this.strategy = strategy;
    }
    
    /**
     * Creates a new normalized ranking, backed by a SimpleRanking.
     * @param ascending
     * if the ranking values should be ordered 
     * ascendingly, or descendingly otherwise
     * @param strategy
     * the strategy to use
     */
    public NormalizedRanking(boolean ascending, final NormalizationStrategy strategy) {
        super();
        this.ranking = new SimpleRanking<>(ascending);
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
        final RankingMetric<T> metric = ranking.getRankingMetrics(node);
        final double susNorm = this.normalizeSuspiciousness(metric);
        return new SimpleRankingMetric<T>(metric.getElement(), metric.getBestRanking(), metric.getRanking(), metric.getWorstRanking(), susNorm, getElements().size());
    }

    private double normalizeSuspiciousness(final RankingMetric<T> metric) {
        switch (this.strategy) {
        case ReciprocalRank:
            return 1.0d / metric.getWorstRanking();
        case ZeroToOne:
        	return getZeroOneSuspiciousness(metric.getRankingValue());
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }
    
    private double normalizeSuspiciousness(final T node) {
    	switch (this.strategy) {
        case ReciprocalRank:
            return 1.0d / ranking.getRankingMetrics(node).getWorstRanking();
        case ZeroToOne:
        	return getZeroOneSuspiciousness(ranking.getRankingValue(node));
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }
    
    private double normalizeSuspiciousness(final T node, double rankingValue) {
    	switch (this.strategy) {
        case ReciprocalRank:
            return 1.0d / ranking.getRankingMetrics(node).getWorstRanking();
        case ZeroToOne:
        	return getZeroOneSuspiciousness(rankingValue);
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }

	private double getZeroOneSuspiciousness(final double curSusp) {
		final double suspMax = ranking.getBestFiniteRankingValue();
		final double suspMin = ranking.getWorstFiniteRankingValue();

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
		return normalizeSuspiciousness(getBestRankingElement(), ranking.getBestRankingValue());
	}

	@Override
	public double getWorstRankingValue() {
		return normalizeSuspiciousness(getWorstRankingElement(), ranking.getWorstRankingValue());
	}

	@Override
	public double getBestFiniteRankingValue() {
		return normalizeSuspiciousness(getBestFiniteRankingElement(), ranking.getBestFiniteRankingValue());
	}

	@Override
	public double getWorstFiniteRankingValue() {
		return normalizeSuspiciousness(getWorstFiniteRankingElement(), ranking.getWorstFiniteRankingValue());
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
		return Ranking.sortRankedElementList(ascending, rankedNodes);
	}

	@Override
	public NormalizedRanking<T> newInstance(boolean ascending) {
		return new NormalizedRanking<T>(ascending, strategy);
	}
    
	@Override
	public boolean isAscending() {
		return ranking.isAscending();
	}

	@Override
	public boolean add(T element, double rankingValue) {
		return ranking.add(element, rankingValue);
	}

	@Override
	public void addAllFromRanking(Ranking<T> ranking) {
		this.ranking.addAllFromRanking(ranking);
	}

	@Override
	public Ranking<T> merge(Ranking<T> other) {
		return ranking.merge(other);
	}

	@Override
	public T getBestRankingElement() {
		return ranking.getBestRankingElement();
	}

	@Override
	public T getBestFiniteRankingElement() {
		return ranking.getBestFiniteRankingElement();
	}

	@Override
	public T getWorstRankingElement() {
		return ranking.getWorstRankingElement();
	}

	@Override
	public T getWorstFiniteRankingElement() {
		return ranking.getWorstFiniteRankingElement();
	}

	@Override
	public int wastedEffort(T element) throws IllegalArgumentException {
		return ranking.wastedEffort(element);
	}

	@Override
	public List<RankedElement<T>> getSortedRankedElements() {
		return ranking.getSortedRankedElements();
	}

	@Override
	public Map<T, Double> getElementMap() {
		return ranking.getElementMap();
	}

	@Override
	public Set<T> getElements() {
		return ranking.getElements();
	}

	@Override
	public boolean hasRanking(T element) {
		return ranking.hasRanking(element);
	}

	@Override
	public void outdateRankingCache() {
		ranking.outdateRankingCache();
	}
    
}
