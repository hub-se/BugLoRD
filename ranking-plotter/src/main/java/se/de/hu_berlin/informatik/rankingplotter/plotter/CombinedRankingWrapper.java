/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

/**
 * Helper class to store the combined ranking based on some lambda values.
 * 
 * @author Simon Heiden
 */
public class CombinedRankingWrapper implements Comparable<CombinedRankingWrapper> {

	/**
	 * Stores the combined ranking.
	 */
	private double combinedRanking = 0;
	
	/**
	 * Creates a {@link CombinedRankingWrapper} object with the given parameters.
	 * Computes a combined ranking which is normalized so that the rankings range from 0 to 1.
	 * If the original maximal NLFL ranking is below 1, then the ranking remains unchanged.
	 * The ranking is computed with the formula
	 * <br>{@code lambda * SBFL-ranking + (1-lambda) * (lamda2 * global NLFL-ranking + (1-lamda2) local NLFL ranking)}. 
	 * @param SBFLRanking
	 * the SBFL ranking
	 * @param NLFLRanking
	 * the global NLFL ranking
	 * @param localNLFLRanking
	 * the local NLFL ranking
	 * @param lambda
	 * value such that {@code 0 <= lambda <= 1}
	 * @param max_globalranking
	 * the maximum global NLFL ranking
	 * @param lambda2
	 * value such that {@code 0 <= lambda2 <= 1}
	 * @param max_localranking
	 * the maximum local NLFL ranking
	 */
	public CombinedRankingWrapper(final double SBFLRanking, final double NLFLRanking, final double localNLFLRanking, 
			final double lambda, final double max_globalranking, 
			final double lambda2, final double max_localranking) {
		if (lambda == 0) {
			this.combinedRanking = (lambda2 * NLFLRanking / max_globalranking 
							+ (1.0-lambda2) * localNLFLRanking / max_localranking);
		} else {
			this.combinedRanking = lambda * SBFLRanking 
					+ (1.0-lambda) * (lambda2 * NLFLRanking / max_globalranking 
							+ (1.0-lambda2) * localNLFLRanking / max_localranking);
		}
	}
	
	/**
	 * Creates a {@link CombinedRankingWrapper} object with the given parameters.
	 * Computes a combined ranking which is normalized so that the rankings range from 0 to 1.
	 * If the original maximal NLFL ranking is below 1, then the ranking remains unchanged.
	 * The ranking is computed with the formula
	 * <br>{@code lambda * SBFL-ranking + (1-lambda) * global NLFL-ranking}. 
	 * @param SBFLRanking
	 * the SBFL ranking
	 * @param NLFLRanking
	 * the global NLFL ranking
	 * @param lambda
	 * value such that {@code 0 <= lambda <= 1}
	 * @param max_ranking
	 * the maximum global NLFL ranking
	 */
	public CombinedRankingWrapper(final double SBFLRanking, final double NLFLRanking,
			final double lambda, final double max_ranking) {
		if (lambda == 0) {
			this.combinedRanking = (NLFLRanking / max_ranking);
		} else {
			this.combinedRanking = lambda * SBFLRanking
					+ (1.0-lambda) * (NLFLRanking / max_ranking);
		}
	}
	
	/**
	 * @return 
	 * the combined ranking
	 */
	public double getCombinedRanking() {
		return this.combinedRanking;
	}


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final CombinedRankingWrapper o) {
		return Double.compare(o.getCombinedRanking(), this.getCombinedRanking());
	}
}
