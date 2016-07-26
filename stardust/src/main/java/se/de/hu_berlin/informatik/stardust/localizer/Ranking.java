/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import se.de.hu_berlin.informatik.stardust.traces.INode;

/**
 * Class used to create a ranking of nodes with corresponding suspiciousness set.
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Ranking<T> implements Iterable<INode<T>> {

    /** Holds the actual ranking */
    protected final TreeSet<RankedElement<T>> rankedNodes = new TreeSet<>(); // NOCS

    /** Holds the nodes with their corresponding suspiciousness */
    protected final Map<INode<T>, Double> nodes = new HashMap<>();

    /** caches the best ranking for each node */
    private Map<INode<T>, Integer> __cacheBestRanking;
    /** caches the worst ranking for each node */
    private Map<INode<T>, Integer> __cacheWorstRanking;

    /**
     * Create a new ranking.
     */
    public Ranking() {
        super();
    }

    /**
     * Adds a node with its suspiciousness to the ranking.
     *
     * @param node
     *            the node to add to the ranking
     * @param suspiciousness
     *            the determined suspiciousness of the node
     */
    public void rank(final INode<T> node, final double suspiciousness) {
        final double s = Double.isNaN(suspiciousness) ? Double.NEGATIVE_INFINITY : suspiciousness;
        this.rankedNodes.add(new RankedElement<T>(node, s));
        this.nodes.put(node, s);
        this.outdateRankingCache();
    }

    /**
     * Returns the suspiciousness of the given node.
     *
     * @param node
     *            the node to get the suspiciousness of
     * @return suspiciousness
     */
    public double getSuspiciousness(final INode<T> node) {
        return this.nodes.get(node);
    }

    /**
     * Computes the wasted effort metric of a node in the ranking.
     *
     * This is equal to the number of nodes that are ranked higher than the given node.
     *
     * @param node
     *            the node to compute the metric for.
     * @return number of nodes ranked higher as the given node.
     */
    public int wastedEffort(final INode<T> node) {
        int position = 0;
        for (final RankedElement<T> element : this.rankedNodes) {
            if (node.equals(element.node)) {
                return position;
            }
            position++;
        }
        throw new IllegalArgumentException(String.format("The ranking does not contain node '%s'.", node.toString()));
    }

    /**
     * Returns all ranking metrics for a given node.
     *
     * @param node
     *            the node to get the metrics for
     * @return metrics
     */
    public RankingMetric<T> getRankingMetrics(final INode<T> node) {
        this.updateRankingCache();
        final Integer bestRanking = this.__cacheBestRanking.get(node);
        final Integer worstRanking = this.__cacheWorstRanking.get(node);
        assert bestRanking != null;
        assert worstRanking != null;
        final double nodeSuspiciousness = this.nodes.get(node);
        return new RankingMetric<T>(node, bestRanking, worstRanking, nodeSuspiciousness, nodes);
    }

    /**
     * Outdates the ranking cache
     */
    protected void outdateRankingCache() {
        this.__cacheBestRanking = null;
        this.__cacheWorstRanking = null;
    }

    /**
     * Checks whether the ranking cache is outdated or not
     *
     * @return true if the cache is outdated, false otherwise
     */
    protected boolean isRankingCacheOutdated() {
        return this.__cacheBestRanking == null || this.__cacheWorstRanking == null;
    }

    /**
     * Updates the cached worst case and best case ranking if necessary
     */
    protected void updateRankingCache() {
        if (!this.isRankingCacheOutdated()) {
            return;
        }

        // update best case
        this.__cacheBestRanking = new HashMap<>();
        Integer bestRanking = null;
        int position = 0;
        Double preSuspiciousness = null;
        for (final RankedElement<T> element : this.rankedNodes) {
            position++;
            if (preSuspiciousness == null || preSuspiciousness.compareTo(element.suspicousness) != 0) {
                bestRanking = position;
                preSuspiciousness = element.suspicousness;
            }
            this.__cacheBestRanking.put(element.node, bestRanking);
        }

        // update worst case
        this.__cacheWorstRanking = new HashMap<>();
        Integer worstRanking = null;
        position = this.rankedNodes.size() + 1;
        preSuspiciousness = null;
        for (final RankedElement<T> element : this.rankedNodes.descendingSet()) {
            position--;
            if (preSuspiciousness == null || preSuspiciousness.compareTo(element.suspicousness) != 0) {
                worstRanking = position;
                preSuspiciousness = element.suspicousness;
            }
            this.__cacheWorstRanking.put(element.node, worstRanking);
        }
    }

    /**
     * Creates a new ranking with this ranking and the other ranking merged together.
     *
     * @param other
     *            the other ranking to merge with this ranking
     * @return merged ranking
     */
    public Ranking<T> merge(final Ranking<T> other) {
        final Ranking<T> merged = new Ranking<T>();
        merged.nodes.putAll(this.nodes);
        merged.rankedNodes.addAll(this.rankedNodes);
        merged.nodes.putAll(other.nodes);
        merged.rankedNodes.addAll(other.rankedNodes);
        merged.outdateRankingCache();
        return merged;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<INode<T>> iterator() {
        // mimic RankedElement iterator but pass node objects to the outside
        final Iterator<RankedElement<T>> rankedIterator = this.rankedNodes.iterator();
        return new Iterator<INode<T>>() {

            @Override
            public boolean hasNext() {
                return rankedIterator.hasNext();
            }

            @Override
            public INode<T> next() {
                return rankedIterator.next().node;
            }

            @Override
            public void remove() {
                rankedIterator.remove();
            }
        };
    }

    /**
     * Saves the ranking result to a given file.
     *
     * @param filename
     *            the file name to save the ranking to
     * @throws IOException
     */
    public void save(final String filename) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filename);
            for (final RankedElement<T> el : this.rankedNodes) {
                writer.write(String.format("%s: %f\n", el.node.toString(), el.suspicousness));
            }
        } catch (final Exception e) {
            throw new RuntimeException("Saving the ranking failed.", e);
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    

}
