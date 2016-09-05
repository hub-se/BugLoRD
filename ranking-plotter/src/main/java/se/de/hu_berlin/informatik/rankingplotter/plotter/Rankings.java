/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

/**
 * Helper class to store and process SBFL and global and local NLFL rankings of a
 * specific line in a specific source file.
 * 
 * @author Simon Heiden
 */
public class Rankings implements Comparable<Rankings> {

	/**
	 * Stores the SBFL ranking.
	 */
	private double SBFLRanking;
	
	/**
	 * Stores the global NLFL ranking.
	 */
	private double NLFLRanking;
	
	/**
	 * Stores the local NLFL ranking.
	 */
	private double localNLFLRanking;
	
	/**
	 * Stores the combined ranking.
	 */
	private double combinedRanking = 0;
	
	private String identifier;
	
	/**
	 * Creates a {@link Rankings} object with the given parameters.
	 * @param identifier
	 * the element's identifier
	 * @param SBFLRanking
	 * the SBFL ranking
	 * @param NLFLRanking
	 * the global NLFL ranking
	 * @param localNLFLRanking
	 * the local NLFL ranking
	 */
	public Rankings(final String identifier, final double SBFLRanking, final double NLFLRanking, final double localNLFLRanking) {
		this.identifier = identifier;
		this.SBFLRanking = SBFLRanking;
		this.NLFLRanking = NLFLRanking;
		this.localNLFLRanking = localNLFLRanking;
	}
	
	/**
	 * Creates a {@link Rankings} object with the given parameters.
	 * @param identifier
	 * the element's identifier
	 * @param SBFLRanking
	 * the SBFL ranking
	 * @param NLFLRanking
	 * the global NLFL ranking
	 */
	public Rankings(final String identifier, final double SBFLRanking, final double NLFLRanking) {
		this.identifier = identifier;
		this.SBFLRanking = SBFLRanking;
		this.NLFLRanking = NLFLRanking;
		this.localNLFLRanking = 0;
	}
	
	/**
	 * Creates a {@link Rankings} object with the given parameters.
	 * @param identifier
	 * the element's identifier
	 * @param SBFLRanking
	 * the SBFL ranking
	 */
	public Rankings(final String identifier, final double SBFLRanking) {
		this.identifier = identifier;
		this.SBFLRanking = SBFLRanking;
		this.NLFLRanking = 0;
		this.localNLFLRanking = 0;
	}
	
	/**
	 * @return 
	 * the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * @return 
	 * the SBFL ranking
	 */
	public double getSBFLRanking() {
		return SBFLRanking;
	}

	/**
	 * @param sBFLRanking
	 * the SBFL ranking to set
	 */
	public void setSBFLRanking(final double sBFLRanking) {
		SBFLRanking = sBFLRanking;
	}

	/**
	 * @return 
	 * the global NLFL ranking
	 */
	public double getGlobalNLFLRanking() {
		return NLFLRanking;
	}

	/**
	 * @param nLFLRanking 
	 * the global NLFL ranking to set
	 */
	public void setGlobalNLFLRanking(final double nLFLRanking) {
		NLFLRanking = nLFLRanking;
	}
	
	/**
	 * @return 
	 * the local NLFL ranking
	 */
	public double getlocalNLFLRanking() {
		return localNLFLRanking;
	}

	/**
	 * @param nLFLRanking 
	 * the local NLFL ranking to set
	 */
	public void setlocalNLFLRanking(final double nLFLRanking) {
		localNLFLRanking = nLFLRanking;
	}
	
	/**
	 * Computes a combined ranking which is normalized so that the rankings range from 0 to 1.
	 * If the original maximal NLFL ranking is below 1, then the ranking remains unchanged.
	 * The ranking is computed with the formula
	 * <br>{@code lambda * SBFL-ranking + (1-lambda) * global NLFL-ranking}. 
	 * @param lambda
	 * value such that {@code 0 <= lambda <= 1}
	 * @param max_ranking
	 * the maximum global NLFL ranking
	 * @return
	 * the new combined ranking
	 */
	public double setCombinedRanking(final double lambda, final double max_ranking) {
		if (lambda == 0) {
			this.combinedRanking = (this.NLFLRanking / max_ranking);
		} else {
			this.combinedRanking = lambda * this.SBFLRanking
					+ (1.0-lambda) * (this.NLFLRanking / max_ranking);
		}
		return this.combinedRanking;
	}
	
	/**
	 * Computes a combined ranking which is normalized so that the rankings range from 0 to 1.
	 * If the original maximal NLFL ranking is below 1, then the ranking remains unchanged.
	 * The ranking is computed with the formula
	 * <br>{@code lambda * SBFL-ranking + (1-lambda) * (lamda2 * global NLFL-ranking + (1-lamda2) local NLFL ranking)}. 
	 * @param lambda
	 * value such that {@code 0 <= lambda <= 1}
	 * @param max_globalranking
	 * the maximum global NLFL ranking
	 * @param lambda2
	 * value such that {@code 0 <= lambda2 <= 1}
	 * @param max_localranking
	 * the maximum local NLFL ranking
	 * @return
	 * the new combined ranking
	 */
	public double setCombinedRanking(final double lambda, final double max_globalranking, 
			final double lambda2, final double max_localranking) {
		if (lambda == 0) {
			this.combinedRanking = (lambda2 * this.NLFLRanking / max_globalranking 
							+ (1.0-lambda2) * this.localNLFLRanking / max_localranking);
		} else {
			this.combinedRanking = lambda * this.SBFLRanking 
					+ (1.0-lambda) * (lambda2 * this.NLFLRanking / max_globalranking 
							+ (1.0-lambda2) * this.localNLFLRanking / max_localranking);
		}
		return this.combinedRanking;
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
	public int compareTo(final Rankings o) {
		return Double.compare(o.getCombinedRanking(), this.getCombinedRanking());
	}
}
