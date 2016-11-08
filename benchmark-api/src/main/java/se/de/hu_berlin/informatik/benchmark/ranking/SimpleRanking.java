/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.benchmark.ranking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class used to create a ranking of nodes with corresponding suspiciousness set.
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public class SimpleRanking<T> implements Ranking<T>, Iterable<T> {

    /** Holds the nodes with their corresponding ranking values */
    private final Map<T, Double> nodes = new HashMap<>();

    /** caches the actual ranking for each node */
    private Map<T, Integer> __cacheRanking;
    /** caches the best ranking for each node */
    private Map<T, Integer> __cacheBestRanking;
    /** caches the worst ranking for each node */
    private Map<T, Integer> __cacheWorstRanking;
    
    final private boolean ascending;

    private T maxKey = null;
	private double max = Double.NEGATIVE_INFINITY;

	private T minKey = null;
	private double min = Double.POSITIVE_INFINITY;

	private T maxFiniteKey = null;
	private double maxFinite = Double.NEGATIVE_INFINITY;

	private T minFiniteKey = null;
	private double minFinite = Double.POSITIVE_INFINITY;

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
    
//    /**
//     * Create a new ranking with ascending ordering.
//     */
//    public SimpleRanking() {
//       this(true);
//    }
    
    @Override
    public boolean isAscending() {
    	return this.ascending;
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
    public boolean add(final T node, final double suspiciousness) {
//        final double s = Double.isNaN(suspiciousness) ? Double.NEGATIVE_INFINITY : suspiciousness;
    	if (hasRanking(node)) {
    		//do not add the element if already in the ranking
    		return false;
    	}
//        this.rankedNodes.add(new SimpleRankedElement<T>(node, suspiciousness));
        this.nodes.put(node, suspiciousness);
        //since we don't remove ranked elements, we can compute these values here once
        if (suspiciousness > max) {
        	max = suspiciousness;
        	maxKey = node;
        }
        if (suspiciousness < min) {
        	min = suspiciousness;
        	minKey = node;
        }
        if (Double.isFinite(suspiciousness)) {
        	if (suspiciousness > maxFinite) {
            	maxFinite = suspiciousness;
            	maxFiniteKey = node;
            }
            if (suspiciousness < minFinite) {
            	minFinite = suspiciousness;
            	minFiniteKey = node;
            }
        }
        
        this.outdateRankingCache();
        return true;
    }
    
//    /**
//     * Adds all nodes in the given collection.
//     * @param rankedElements
//     * the collection of nodes
//     */
//    @Override
//    public void addAll(Collection<RankedElement<T>> rankedElements) {
//        for (RankedElement<T> rankedElement : rankedElements) {
//        	add(rankedElement.getElement(), rankedElement.getRankingValue());
//        }
//    }
    

    @Override
    public void addAll(Map<T, Double> elementMap) {
        for (Entry<T, Double> rankedElement : elementMap.entrySet()) {
        	add(rankedElement.getKey(), rankedElement.getValue());
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
    	this.updateRankingCache();
    	Integer ranking = this.__cacheRanking.get(node);
    	if (ranking == null) {
    		throw new IllegalArgumentException(
            		String.format("The ranking does not contain element '%s'.", node));
    	}
    	return ranking - 1;
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
        final Integer ranking = this.__cacheRanking.get(node);
        final Integer worstRanking = this.__cacheWorstRanking.get(node);
        assert bestRanking != null;
        assert ranking != null;
        assert worstRanking != null;
        final double nodeSuspiciousness = this.nodes.get(node);
        return new SimpleRankingMetric<T>(node, bestRanking, ranking, worstRanking, nodeSuspiciousness, nodes);
    }

    /**
     * Outdates the ranking cache
     */
    @Override
    public void outdateRankingCache() {
    	this.__cacheRanking = null;
        this.__cacheBestRanking = null;
        this.__cacheWorstRanking = null;
    }

    /**
     * Checks whether the ranking cache is outdated or not
     *
     * @return true if the cache is outdated, false otherwise
     */
    protected boolean isRankingCacheOutdated() {
        return this.__cacheBestRanking == null 
        		|| this.__cacheWorstRanking == null 
        		|| this.__cacheRanking == null;
    }

    /**
     * Updates the cached worst case and best case ranking if necessary
     */
    protected void updateRankingCache() {
        if (!this.isRankingCacheOutdated()) {
            return;
        }
        
        //we have to use an internal method version here or we will possibly get 
        //problems with recursions if the public version gets overridden
        List<RankedElement<T>> sortedRankedElements = getSortedRankedElementsInternal();

        // update best case and actual rankings
        this.__cacheBestRanking = new HashMap<>();
        this.__cacheRanking = new HashMap<>();
        Integer bestRanking = null;
        int position = 0;
        Double preSuspiciousness = null;
        for (final RankedElement<T> element : sortedRankedElements) {
            position++;
            if (preSuspiciousness == null || preSuspiciousness.compareTo(element.getRankingValue()) != 0) {
                bestRanking = position;
                preSuspiciousness = element.getRankingValue();
            }
            this.__cacheRanking.put(element.getElement(), position);
            this.__cacheBestRanking.put(element.getElement(), bestRanking);
        }

        // update worst case
        this.__cacheWorstRanking = new HashMap<>();
        Integer worstRanking = null;
        position = sortedRankedElements.size() + 1;
        preSuspiciousness = null;
        for (ListIterator<RankedElement<T>> iterator = sortedRankedElements.listIterator(sortedRankedElements.size()); iterator.hasPrevious();) {
        	RankedElement<T> element = iterator.previous();
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
        merged.addAll(this.getElementMap());
        merged.addAll(other.getElementMap());
        return merged;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        // mimic RankedElement iterator but pass node objects to the outside
        final Iterator<RankedElement<T>> rankedIterator = getSortedRankedElements().iterator();
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
    @Override
    public void save(final String filename) throws IOException {
        Ranking.save(this, filename);
    }

	@Override
	public double getRankingValue(T element) {
		Double rank = nodes.get(element);
		if (rank == null) {
			return Double.NaN;
		}
		return rank;
	}

	@Override
	public List<RankedElement<T>> getSortedRankedElements() {
		return getSortedRankedElements(ascending);
	}
	
	private List<RankedElement<T>> getSortedRankedElementsInternal() {
		return sortRankedElements(ascending);
	}
	
	@Override
	public List<RankedElement<T>> getSortedRankedElements(boolean ascending) {
		return sortRankedElements(ascending);
	}
	
	private List<RankedElement<T>> sortRankedElements(boolean ascending) {
		List<RankedElement<T>> rankedNodes = new ArrayList<>(nodes.size());
		//fill the list with elements 
		for (Entry<T, Double> entry : nodes.entrySet()) {
			rankedNodes.add(new SimpleRankedElement<>(entry.getKey(), entry.getValue()));
		}
		//sort the list
		return sortRankedElementList(ascending, rankedNodes);
	}
	
	protected List<RankedElement<T>> sortRankedElementList(boolean ascending, final List<RankedElement<T>> rankedNodes) {
		//sort the list
		if (ascending) {
			rankedNodes.sort(new Comparator<RankedElement<T>>() {
				@Override
				public int compare(RankedElement<T> o1, RankedElement<T> o2) {
					if (Double.isNaN(o1.getRankingValue())) {
						if (Double.isNaN(o2.getRankingValue())) {
							//two NaN values are to be regarded equal as ranking values...
							// TODO: we need to ensure all elements have a total order.
							return Integer.compare(o1.hashCode(), o2.hashCode());
						}
						//being a ranking value, NaN are always regarded as being less than other values...
						return -1;
					} else if (Double.isNaN(o2.getRankingValue())) {
						//being a ranking value, NaN are always regarded as being less than other values...
						return 1;
					}
					final int compareTo = Double.compare(o1.getRankingValue(), o2.getRankingValue());
					if (compareTo != 0) {
						return compareTo;
					}
					// TODO: we need to ensure all elements have a total order.
					return Integer.compare(o1.hashCode(), o2.hashCode());
				}
			});
		} else {
			rankedNodes.sort(new Comparator<RankedElement<T>>() {
				@Override
				public int compare(RankedElement<T> o2, RankedElement<T> o1) {
					if (Double.isNaN(o1.getRankingValue())) {
						if (Double.isNaN(o2.getRankingValue())) {
							//two NaN values are to be regarded equal as ranking values...
							// TODO: we need to ensure all elements have a total order.
							return Integer.compare(o1.hashCode(), o2.hashCode());
						}
						//being a ranking value, NaN are always regarded as being less than other values...
						return -1;
					} else if (Double.isNaN(o2.getRankingValue())) {
						//being a ranking value, NaN are always regarded as being less than other values...
						return 1;
					}
					final int compareTo = Double.compare(o1.getRankingValue(), o2.getRankingValue());
					if (compareTo != 0) {
						return compareTo;
					}
					// TODO: we need to ensure all elements have a total order.
					return Integer.compare(o1.hashCode(), o2.hashCode());
				}
			});
		}
		
		return rankedNodes;
	}

	@Override
	public SimpleRanking<T> newInstance(boolean ascending) {
		return new SimpleRanking<>(ascending);
	}

	@Override
	public boolean hasRanking(T element) {
		return nodes.containsKey(element);
	}

	@Override
	public Map<T, Double> getElementMap() {
		return nodes;
	}

	private double getMaxRankingValue() {
		return max;
	}

	private double getMinRankingValue() {
		return min;
	}
	
	@Override
	public double getBestRankingValue() {
		if (nodes.isEmpty()) {
			return Double.NaN;
		}

		if (ascending) {
			return getMinRankingValue();
		} else {
			return getMaxRankingValue();
		}
	}

	@Override
	public double getWorstRankingValue() {
		if (nodes.isEmpty()) {
			return Double.NaN;
		}

		if (ascending) {
			return getMaxRankingValue();
		} else {
			return getMinRankingValue();
		}
	}

	private double getMaxFiniteRankingValue() {
		if (Double.isFinite(maxFinite)) {
			return maxFinite;
		} else {
			return Double.NaN;
		}
	}

	private double getMinFiniteRankingValue() {
		if (Double.isFinite(minFinite)) {
			return minFinite;
		} else {
			return Double.NaN;
		}
	}
	
	@Override
	public double getBestFiniteRankingValue() {
		if (nodes.isEmpty()) {
			return Double.NaN;
		}

		if (ascending) {
			return getMinFiniteRankingValue();
		} else {
			return getMaxFiniteRankingValue();
		}
	}

	@Override
	public double getWorstFiniteRankingValue() {
		if (nodes.isEmpty()) {
			return Double.NaN;
		}
		
		if (ascending) {
			return getMaxFiniteRankingValue();
		} else {
			return getMinFiniteRankingValue();
		}
	}

	private T getMaxRankingElement() {
		return maxKey;
	}

	private T getMinRankingElement() {
		return minKey;
	}
	
	@Override
	public T getBestRankingElement() {
		if (ascending) {
			return getMinRankingElement();
		} else {
			return getMaxRankingElement();
		}
	}

	@Override
	public T getWorstRankingElement() {
		if (ascending) {
			return getMaxRankingElement();
		} else {
			return getMinRankingElement();
		}
	}
	
	private T getMaxFiniteRankingElement() {
		return maxFiniteKey;
	}

	private T getMinFiniteRankingElement() {
		return minFiniteKey;
	}
	
	@Override
	public T getBestFiniteRankingElement() {
		if (ascending) {
			return getMinFiniteRankingElement();
		} else {
			return getMaxFiniteRankingElement();
		}
	}

	@Override
	public T getWorstFiniteRankingElement() {
		if (ascending) {
			return getMaxFiniteRankingElement();
		} else {
			return getMinFiniteRankingElement();
		}
	}
    

}
