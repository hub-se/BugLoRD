package se.de.hu_berlin.informatik.experiments.results;

import java.util.HashMap;
import java.util.Map;

import se.de.hu_berlin.informatik.benchmark.ranking.RankedElement;
import se.de.hu_berlin.informatik.benchmark.ranking.Ranking;

public class RankingUtils {

	/**
	 * Combines two rankings, using the given combiner to combine
	 * two single data points with identical identifiers. If a
	 * data point doesn't exist on one of the rankings, it's
	 * ranking value is regarded as being zero.
	 * @param ranking1
	 * the first ranking
	 * @param ranking2
	 * the second ranking
	 * @param combiner
	 * the combiner
	 * @return
	 * the combined ranking (new instance obtained from ranking 1)
	 */
	public static <T> Ranking<T> combineRankings(
			Ranking<T> ranking1, Ranking<T> ranking2, 
			RankingCombiner<Double> combiner) {
		Ranking<T> combinedRanking = ranking1.newInstance();
		for (RankedElement<T> element1 : ranking1.getRankedElements()) {
			combinedRanking.add(
					element1.getElement(), 
					combiner.combine(
							element1.getRankingValue(), 
							ranking2.getRankingValue(element1.getElement())));
		}
		
		for (RankedElement<T> element2 : ranking2.getRankedElements()) {
			if (!combinedRanking.hasRanking(element2.getElement())) {
				combinedRanking.add(
						element2.getElement(), 
						combiner.combine(
								ranking1.getRankingValue(element2.getElement()), 
								element2.getRankingValue()));
			}
		}
		
		return combinedRanking;
	}
	
	 /**
     * Computes the wasted effort metric of an element in the ranking.
     * This is equal to the number of nodes that are ranked higher than the given element.
     * @param element
     * the node to compute the metric for.
     * @return 
     * number of nodes ranked higher as the given node.
     * @param <T>
     * the type of the elements
     */
    public static <T> int wastedEffort(final Ranking<T> ranking, final T element) {
        int position = 0;
        for (final RankedElement<T> element2 : ranking.getRankedElements()) {
            if (element.equals(element2.getElement())) {
                return position;
            }
            position++;
        }
        throw new IllegalArgumentException(
        		String.format("The ranking does not contain node '%s'.", element.toString()));
    }
    
    /**
     * Computes a Map with all wasted efforts for all contained ranking elements.
     * @param ranking
     * the ranking
     * @return
     * the map with all computed wasted effort data
     */
    public static <T> Map<T, Integer> wastedEfforts(final Ranking<T> ranking) {
    	Map<T, Integer> wastedEffortMap = new HashMap<>();
    	int position = 0;
        for (final RankedElement<T> element : ranking.getRankedElements()) {
        	wastedEffortMap.put(element.getElement(), position);
            position++;
        }
        return wastedEffortMap;
    }
    
    public static <T> double meanRankingValue(final Ranking<T> ranking) {
        double value = 0.0;
        for (final RankedElement<T> element : ranking.getRankedElements()) {
        	value += element.getRankingValue();
        }
        return value / ranking.getRankedElements().size();
    }
	
}
