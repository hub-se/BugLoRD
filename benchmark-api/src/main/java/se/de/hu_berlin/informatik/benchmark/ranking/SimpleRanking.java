/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.benchmark.ranking;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Class used to create a ranking of nodes with corresponding suspiciousness set.
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public class SimpleRanking<T> implements Ranking<T>, Iterable<T> {

    /** Holds the actual ranking */
    protected final TreeSet<RankedElement<T>> rankedNodes = new TreeSet<>(); // NOCS

    /** Holds the nodes with their corresponding suspiciousness */
    protected final Map<T, Double> nodes = new HashMap<>();

    /** caches the best ranking for each node */
    private Map<T, Integer> __cacheBestRanking;
    /** caches the worst ranking for each node */
    private Map<T, Integer> __cacheWorstRanking;
    
    final private boolean ascending;

    /**
     * Create a new ranking.
     * <p> Ascending means that lower values get ranked first/best.
	 * <p> Descending means that higher values get ranked first/best.
     * @param ascending
     * if the ranking values should be ordered 
     * ascendingly, or descendingly otherwise
     */
    public SimpleRanking(boolean ascending) {
        super();
        this.ascending = ascending;
    }
    
    /**
     * Create a new ranking with ascending ordering.
     */
    public SimpleRanking() {
       this(true);
    }
    
    @Override
    public boolean isAscending() {
    	return isAscending();
    }

    /**
     * Adds a node with its suspiciousness to the ranking.
     *
     * @param node
     *            the node to add to the ranking
     * @param suspiciousness
     *            the determined suspiciousness of the node
     */
    @Override
    public void add(final T node, final double suspiciousness) {
        final double s = Double.isNaN(suspiciousness) ? Double.NEGATIVE_INFINITY : suspiciousness;
        this.rankedNodes.add(new SimpleRankedElement<T>(node, s));
        this.nodes.put(node, s);
        this.outdateRankingCache();
    }
    
    /**
     * Adds all nodes in the given collection.
     * @param rankedElements
     * the collection of nodes
     */
    @Override
    public void addAll(Collection<RankedElement<T>> rankedElements) {
        for (RankedElement<T> rankedElement : rankedElements) {
        	add(rankedElement.getElement(), rankedElement.getRankingValue());
        }
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
    @Override
    public int wastedEffort(final T node) {
        int position = 0;
        for (final RankedElement<T> element : getRankedElements()) {
            if (node.equals(element.getElement())) {
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
    @Override
    public RankingMetric<T> getRankingMetrics(final T node) {
        this.updateRankingCache();
        final Integer bestRanking = this.__cacheBestRanking.get(node);
        final Integer worstRanking = this.__cacheWorstRanking.get(node);
        assert bestRanking != null;
        assert worstRanking != null;
        final double nodeSuspiciousness = this.nodes.get(node);
        return new SimpleRankingMetric<T>(node, bestRanking, worstRanking, nodeSuspiciousness, nodes);
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
        for (final RankedElement<T> element : getRankedElements(ascending)) {
            position++;
            if (preSuspiciousness == null || preSuspiciousness.compareTo(element.getRankingValue()) != 0) {
                bestRanking = position;
                preSuspiciousness = element.getRankingValue();
            }
            this.__cacheBestRanking.put(element.getElement(), bestRanking);
        }

        // update worst case
        this.__cacheWorstRanking = new HashMap<>();
        Integer worstRanking = null;
        position = this.rankedNodes.size() + 1;
        preSuspiciousness = null;
        for (final RankedElement<T> element : getRankedElements(!ascending)) {
            position--;
            if (preSuspiciousness == null || preSuspiciousness.compareTo(element.getRankingValue()) != 0) {
                worstRanking = position;
                preSuspiciousness = element.getRankingValue();
            }
            this.__cacheWorstRanking.put(element.getElement(), worstRanking);
        }
    }

    /**
     * Creates a new ranking with this ranking and the other ranking merged together.
     *
     * @param other
     *            the other ranking to merge with this ranking
     * @return merged ranking
     */
    @Override
    public Ranking<T> merge(final Ranking<T> other) {
        final Ranking<T> merged = new SimpleRanking<T>(ascending);
        merged.addAll(this.getRankedElements());
        merged.addAll(other.getRankedElements());
        return merged;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        // mimic RankedElement iterator but pass node objects to the outside
        final Iterator<RankedElement<T>> rankedIterator = getRankedElements().iterator();
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return rankedIterator.hasNext();
            }

            @Override
            public T next() {
                return rankedIterator.next().getElement();
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
     * 			  in case of not being able to write to the given path
     */
    public void save(final String filename) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filename);
            for (final RankedElement<T> el : getRankedElements()) {
                writer.write(String.format("%s: %f\n", el.getIdentifier(), el.getRankingValue()));
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

	@Override
	public double getRankingValue(T element) {
		Double rank = nodes.get(element);
		if (rank == null) {
			return 0;
		}
		return rank;
	}

	@Override
	public NavigableSet<RankedElement<T>> getRankedElements() {
		if (ascending) {
			return rankedNodes;
		} else {
			return rankedNodes.descendingSet();
		}
	}
	
	private NavigableSet<RankedElement<T>> getRankedElements(boolean ascending) {
		if (ascending) {
			return rankedNodes;
		} else {
			return rankedNodes.descendingSet();
		}
	}

	@Override
	public SimpleRanking<T> newInstance(boolean ascending) {
		return new SimpleRanking<>(ascending);
	}

	@Override
	public boolean hasRanking(T element) {
		return nodes.containsKey(element);
	}
    

}
