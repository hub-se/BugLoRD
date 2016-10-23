package se.de.hu_berlin.informatik.benchmark.ranking;

import java.util.SortedSet;

/**
 * Class used to store element and ranking value in order 
 * to use the {@link SortedSet} interface for actual element
 * ordering.
 * @param <T>
 * The type of the element
 */
public interface RankedElement<T> extends Comparable<RankedElement<T>> {
   
	public T getElement();
	
	public String getIdentifier();
	
	public double getRankingValue();

}
