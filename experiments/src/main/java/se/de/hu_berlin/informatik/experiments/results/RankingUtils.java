package se.de.hu_berlin.informatik.experiments.results;

import se.de.hu_berlin.informatik.benchmark.ranking.Ranking;

public class RankingUtils {

	
	 /**
     * Computes the wasted effort metric of an element in the ranking.
     * This is equal to the number of nodes that are ranked higher than the given element.
     * @param ranking
     * the ranking
     * @param element
     * the node to compute the metric for.
     * @return 
     * number of nodes ranked higher as the given node.
     * @param <T>
     * the type of the elements
     */
    public static <T> int wastedEffort(final Ranking<T> ranking, final T element) {
        return ranking.wastedEffort(element);
    }
    
    public static <T> double meanRankingValue(final Ranking<T> ranking) {
        double value = 0.0;
        int count = 0;
        for (T element : ranking.getElements()) {
        	double rankingValue = ranking.getRankingValue(element);
        	if (Double.isNaN(rankingValue)) {
        		continue;
        	}
        	value += rankingValue;
        	++count;
        }
        return value / count;
    }
	
}
