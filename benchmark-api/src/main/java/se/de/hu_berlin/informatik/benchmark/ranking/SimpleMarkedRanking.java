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
import java.util.Map;

/**
 * Class used to create a ranking of nodes with corresponding suspiciousness set.
 * The elements in the ranking may be marked with objects of type {@code K}.
 *
 * @param <T>
 * type used to identify nodes in the system
 * @param <K>
 * the type of markers
 */
public class SimpleMarkedRanking<T,K> extends SimpleRanking<T> implements MarkedRanking<T,K> {

	private Map<T,K> markerMap = new HashMap<>();
	
    /**
     * Create a new marked ranking.
     * <p> Ascending means that lower values get ranked first/best.
	 * <p> Descending means that higher values get ranked first/best.
     * @param ascending
     * if the ranking values should be ordered 
     * ascendingly, or descendingly otherwise
     */
    public SimpleMarkedRanking(boolean ascending) {
        super(ascending);
    }
    
    /**
     * Constructs a new simple marked ranking using the given ranking
     * (especially, it uses the exact element map of the given map).
     * @param ranking
     * the ranking
     */
    public SimpleMarkedRanking(Ranking<T> ranking) {
        super(ranking);
    }

	@Override
	public boolean markElementWith(T element, K marker) {
		if (hasRanking(element) && !isMarked(element)) {
			markerMap.put(element, marker);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Collection<T> getMarkedElements() {
		return markerMap.keySet();
	}

	@Override
	public K getMarker(T element) {
		return markerMap.get(element);
	}

	@Override
	public boolean isMarked(T element) {
		return markerMap.containsKey(element);
	}
    
    

}
