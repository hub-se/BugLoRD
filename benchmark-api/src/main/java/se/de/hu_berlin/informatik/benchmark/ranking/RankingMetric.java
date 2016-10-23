package se.de.hu_berlin.informatik.benchmark.ranking;

public interface RankingMetric<T> {

	/**
     * Returns the element this metrics belong to.
     * @return element
     */
    public T getElement();

    /**
     * Returns the best possible ranking of the element.
     * @return bestRanking
     */
    public int getBestRanking();

    /**
     * Returns the worst possible ranking of the element.
     * @return worstRanking
     */
    public int getWorstRanking();

    /**
     * Returns the minimum wasted effort that is necessary 
     * to find this element with the current ranking.
     * @return minWastedEffort
     */
    public double getMinWastedEffort();

    /**
     * Returns the maximum wasted effort that is necessary 
     * to find this element with the current ranking.
     * @return maxWastedEffort
     */
    public double getMaxWastedEffort();

    /**
     * Returns the ranking value of the element.
     * @return ranking value
     */
    public double getRankingValue();
    
}
