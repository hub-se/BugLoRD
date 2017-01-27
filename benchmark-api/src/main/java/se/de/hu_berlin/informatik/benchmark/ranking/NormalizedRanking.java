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
        ZeroToOneRankingValue,
        ZeroToOneRank,
        ZeroToOneRankWorst,
        ZeroToOneRankBest,
        ZeroToOneRankMean,
        
        ReciprocalRank,
        ReciprocalRankWorst,
        ReciprocalRankBest,
        ReciprocalRankMean
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
        final double susNormalized = this.normalizeSuspiciousness(metric);
        return new SimpleRankingMetric<T>(metric.getElement(), 
        		metric.getBestRanking(), metric.getRanking(), metric.getWorstRanking(), 
        		susNormalized, getElements().size());
    }

    private double normalizeSuspiciousness(final RankingMetric<T> metric) {
        switch (this.strategy) {
        case ZeroToOneRankingValue:
        	return getZeroOneSuspiciousness(metric.getRankingValue());
        	
        case ZeroToOneRank:
            return getZeroOneRank(metric.getRanking());
    	case ZeroToOneRankBest:
            return getZeroOneRank(metric.getBestRanking());
        case ZeroToOneRankWorst:
            return getZeroOneRank(metric.getWorstRanking());
        case ZeroToOneRankMean:
            return getZeroOneRank(metric.getMeanRanking());
            
    	case ReciprocalRank:
            return getReciprocalRank(metric.getRanking());
    	case ReciprocalRankBest:
            return getReciprocalRank(metric.getBestRanking());
        case ReciprocalRankWorst:
            return getReciprocalRank(metric.getWorstRanking());
        case ReciprocalRankMean:
            return getReciprocalRank(metric.getMeanRanking());
            
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }
    
    private double normalizeSuspiciousness(final T node) {
    	switch (this.strategy) {
    	case ZeroToOneRankingValue:
        	return getZeroOneSuspiciousness(ranking.getRankingValue(node));
        default:
            return normalizeSuspiciousness(ranking.getRankingMetrics(node));
        }
    }
    
    private double normalizeSuspiciousness(final T node, double rankingValue) {
    	switch (this.strategy) {
    	case ZeroToOneRankingValue:
        	return getZeroOneSuspiciousness(rankingValue);
    	default:
    		return normalizeSuspiciousness(node);
        }
    }

	private double getZeroOneSuspiciousness(final double curSusp) {
		final double suspMax;
		final double suspMin;
		if (ranking.isAscending()) {
			suspMax = ranking.getWorstFiniteRankingValue();
			suspMin = ranking.getBestFiniteRankingValue();
		} else {
			suspMax = ranking.getBestFiniteRankingValue();
			suspMin = ranking.getWorstFiniteRankingValue();	
		}

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
	
	private double getZeroOneRank(final double rank) {
		int size = ranking.getElements().size();
		if (size == 0) {
			return Double.NaN;
		} else  if (size == 1) {
			return 0.5d;
		} else {
			if (ranking.isAscending()) {
				return (double) (rank - 1) / (double) (size - 1);
			} else {
				return 1 - ((double) (rank - 1) / (double) (size - 1));
			}
		}
	}
	
//	private double getReciprocalSuspiciousness(final double curSusp) {
//		final double suspMax;
//		final double suspMin;
//		if (ranking.isAscending()) {
//			suspMax = ranking.getWorstFiniteRankingValue();
//			suspMin = ranking.getBestFiniteRankingValue();
//		} else {
//			suspMax = ranking.getBestFiniteRankingValue();
//			suspMin = ranking.getWorstFiniteRankingValue();	
//		}
//		
//		if (Double.isInfinite(curSusp)) {
//			if (curSusp < 0) {
//				return 0.0d;
//			} else {
//				return 1.0d;
//			}
//		} else if (Double.isNaN(curSusp)) {
//			return Double.NaN;
//		} else if (Double.compare(suspMax, suspMin) == 0) {
//			return 0.5d;
//		} else {
//			return (curSusp - suspMin) / (suspMax - suspMin);
//		}
//		
//		if (ranking.isAscending()) {
//			return 1.0d / (ranking.getElements().size() + 1 - rank);
//		} else {
//			return 1.0d / rank;
//		}
//	}
	
	private double getReciprocalRank(final double rank) {
		if (ranking.isAscending()) {
			return 1.0d / (ranking.getElements().size() + 1 - rank);
		} else {
			return 1.0d / rank;
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
