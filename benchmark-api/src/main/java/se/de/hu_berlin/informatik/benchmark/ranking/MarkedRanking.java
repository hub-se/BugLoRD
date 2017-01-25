/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.benchmark.ranking;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class used to create a ranking of nodes with corresponding suspiciousness set.
 * The elements in the ranking may be marked with objects of type {@code K}.
 *
 * @param <T>
 * type used to identify nodes in the system
 * @param <K>
 * the type of markers
 */
public class MarkedRanking<T,K> implements Ranking<T> {

	final private Ranking<T> ranking;
	private Map<T,K> markerMap = new HashMap<>();
	
    /**
     * Create a new marked ranking.
     * <p> Ascending means that lower values get ranked first/best.
	 * <p> Descending means that higher values get ranked first/best.
     * @param ascending
     * if the ranking values should be ordered 
     * ascendingly, or descendingly otherwise
     */
    public MarkedRanking(boolean ascending) {
        super();
        this.ranking = new SimpleRanking<>(ascending);
    }
    
    /**
     * Creates a view on the given ranking. Changes to this ranking are
     * visible in the given ranking. The same holds for the other direction.
     * @param ranking
     * the ranking
     */
    public MarkedRanking(Ranking<T> ranking) {
        super();
        this.ranking = ranking;
    }

	public boolean markElementWith(T element, K marker) {
		if (hasRanking(element) && !isMarked(element)) {
			markerMap.put(element, marker);
			return true;
		} else {
			return false;
		}
	}

	public Collection<T> getMarkedElements() {
		return markerMap.keySet();
	}

	public K getMarker(T element) {
		return markerMap.get(element);
	}

	public boolean isMarked(T element) {
		return markerMap.containsKey(element);
	}

	@Override
	public MarkedRanking<T,K> newInstance(boolean ascending) {
		return new MarkedRanking<>(ascending);
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public MarkedRanking<T,K> merge(final Ranking<T> other) {
        final MarkedRanking<T,K> merged = newInstance(this.isAscending());
        merged.addAllFromRanking(this);
        merged.addAllFromRanking(other);
        
        for (T markedElement : this.getMarkedElements()) {
        	merged.markElementWith(markedElement, this.getMarker(markedElement));
        }
        
        if (other instanceof MarkedRanking) {
        	MarkedRanking<T,?> otherRanking = (MarkedRanking<T, ?>)other;
        	for (T markedElement : otherRanking.getMarkedElements()) {
            	merged.markElementWith(markedElement, (K) otherRanking.getMarker(markedElement));
            }
        }
        
        return merged;
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

	@Override
	public double getRankingValue(T element) {
		return ranking.getRankingValue(element);
	}

	@Override
	public double getBestRankingValue() {
		return ranking.getBestRankingValue();
	}

	@Override
	public double getBestFiniteRankingValue() {
		return ranking.getBestFiniteRankingValue();
	}

	@Override
	public double getWorstRankingValue() {
		return ranking.getWorstRankingValue();
	}

	@Override
	public double getWorstFiniteRankingValue() {
		return ranking.getWorstFiniteRankingValue();
	}

	@Override
	public RankingMetric<T> getRankingMetrics(T element) {
		return ranking.getRankingMetrics(element);
	}

	@Override
	public List<RankedElement<T>> getSortedRankedElements(boolean ascending) {
		return ranking.getSortedRankedElements(ascending);
	}

}
