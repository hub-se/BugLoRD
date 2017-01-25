package se.de.hu_berlin.informatik.benchmark.ranking;

import java.util.Map.Entry;

import se.de.hu_berlin.informatik.benchmark.ranking.SimpleNormalizedRanking.NormalizationStrategy;

public interface NormalizedRanking<T> extends Ranking<T> {

    /**
	 * Combines two rankings, using the given combiner to combine
	 * two single data points with identical identifiers. If a
	 * data point doesn't exist on one of the rankings, its
	 * ranking value is regarded as being zero. The rankings are
	 * normalized before they are combined.
	 * @param <T>
	 * the type of the ranking elements
	 * @param ranking1
	 * the first ranking
	 * @param ranking2
	 * the second ranking
	 * @param combiner
	 * the combiner
	 * @param strategy
	 * the normalization strategy to use
	 * @return
	 * the combined ranking (new instance obtained from ranking 1)
	 */
	public static <T> Ranking<T> combine(Ranking<T> ranking1, Ranking<T> ranking2, 
			RankingCombiner<Double> combiner, NormalizationStrategy strategy) {
		NormalizedRanking<T> normalizedRanking1 = new SimpleNormalizedRanking<>(ranking1, strategy);
		NormalizedRanking<T> normalizedRanking2 = new SimpleNormalizedRanking<>(ranking2, strategy);
		Ranking<T> combinedRanking = new SimpleRanking<>(ranking1.isAscending());
		for (Entry<T, Double> element1 : normalizedRanking1.getElementMap().entrySet()) {
			double ranking = normalizedRanking2.getRankingValue(element1.getKey());
			if (Double.isNaN(ranking)) {
				ranking = 0;
			}
			combinedRanking.add(
					element1.getKey(), 
					combiner.combine(element1.getValue(), ranking));
		}
		
		for (Entry<T, Double> element2 : normalizedRanking2.getElementMap().entrySet()) {
			if (!combinedRanking.hasRanking(element2.getKey())) {
				double ranking = normalizedRanking1.getRankingValue(element2.getKey());
				if (Double.isNaN(ranking)) {
					ranking = 0;
				}
				combinedRanking.add(
						element2.getKey(), 
						combiner.combine(ranking, element2.getValue()));
			}
		}
		
		return combinedRanking;
	}
	
}
