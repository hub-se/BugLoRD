package se.de.hu_berlin.informatik.stardust.localizer;

import java.util.Map;

import se.de.hu_berlin.informatik.stardust.traces.INode;

/**
 * Holds all ranking information for a node.
 * @param <T>
 * the type of the node
 */
public class RankingMetric<T> {

    /** The node the ranking metris belong to */
    private final INode<T> node;
    /** the best possible ranking of the node */
    private final int bestRanking;
    /** the worst possible ranking of the node */
    private final int worstRanking;
    /** The suspiciousness of the node */
    private final double suspiciousness;
    
    /** Holds the nodes with their corresponding suspiciousness */
    private final Map<INode<T>, Double> nodes;

    /**
     * Create the ranking metric for a certain node.
     *
     * @param node
     *            The node the ranking metris belong to
     * @param bestRanking
     *            the best possible ranking of the node
     * @param worstRanking
     *            the worst possible ranking of the node
     * @param suspiciousness
     *            The suspiciousness of the node
     * @param nodes
     * 			  the nodes with their corresponding suspiciousness
     */
    protected RankingMetric(final INode<T> node, final int bestRanking, final int worstRanking,
            final double suspiciousness, Map<INode<T>, Double> nodes) {
        this.node = node;
        this.bestRanking = bestRanking;
        this.worstRanking = worstRanking;
        this.suspiciousness = suspiciousness;
        this.nodes = nodes;
    }

    /**
     * Returns the node this metrics belong to
     *
     * @return node
     */
    public INode<T> getNode() {
        return node;
    }

    /**
     * Returns the best possible ranking of the node
     *
     * @return bestRanking
     */
    public int getBestRanking() {
        return bestRanking;
    }

    /**
     * Returns the worst possible ranking of the node
     *
     * @return worstRanking
     */
    public int getWorstRanking() {
        return worstRanking;
    }

    /**
     * Returns the minimum wasted effort that is necessary to find this node with the current ranking.
     *
     * @return minWastedEffort
     */
    public double getMinWastedEffort() {
        return new Double(bestRanking - 1) / new Double(nodes.size());
    }

    /**
     * Returns the maximum wasted effort that is necessary to find this node with the current ranking.
     *
     * @return maxWastedEffort
     */
    public double getMaxWastedEffort() {
        return new Double(worstRanking - 1) / new Double(nodes.size());
    }

    /**
     * Returns the suspiciousness of the node.
     *
     * @return suspiciousness
     */
    public double getSuspiciousness() {
        return suspiciousness;
    }


}
