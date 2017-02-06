package se.de.hu_berlin.informatik.benchmark.ranking;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import se.de.hu_berlin.informatik.benchmark.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public interface Ranking<T> extends Iterable<T> {
	
	final public static char RANKING_SEPARATOR = ':'; 
	
	/**
	 * Holds strategies for assigning different ranking
	 * values to special ranking values like NaN, positive
	 * infinity and negative infinity.
	 */
	public enum RankingStrategy {
		/** Replaces the ranking value with 0.0 */
		ZERO,
		/** Replaces the ranking value with positive infinity */
		INFINITY,
		/** Replaces the ranking value with negative infinity */
		NEGATIVE_INFINITY,
		/** Replaces the ranking value with the best finite
		 * ranking value in the ranking +/- 1 (depending on the
		 * ranking being ascending or descending) */
		BEST,
		/** Replaces the ranking value with the worst finite
		 * ranking value in the ranking +/- 1 (depending on the
		 * ranking being ascending or descending) */
		WORST,
		/** Replaces the ranking value with NaN */
		NAN
	}
	
	/**
     * {@inheritDoc}
     */
    @Override
    public default Iterator<T> iterator() {
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
	 * Gets a new instance with ascending or descending ordering.
	 * <p> Ascending means that lower values get ranked first/best.
	 * <p> Descending means that higher values get ranked first/best.
	 * @param ascending
	 * true for ascending, false for descending ordering
	 * @return
	 * a new Ranking object instance
	 */
	public Ranking<T> newInstance(boolean ascending);
	
	/**
	 * <p> Ascending means that lower values get ranked first/best.
	 * <p> Descending means that higher values get ranked first/best.
	 * @return
	 * whether the ranking order is ascending; else descending
	 */
	public boolean isAscending();
	
	/**
     * Adds an element with its ranking value to the ranking.
     * @param element
     * the element to add to the ranking
     * @param rankingValue
     * the determined ranking value of the element
	 * @return 
	 * true if successful, false otherwise
     */
    public boolean add(final T element, final double rankingValue);
    
//    /**
//     * Adds all elements in the given collection.
//     * @param rankedElements
//     * the collection of elements
//     */
//    public void addAll(Collection<RankedElement<T>> rankedElements);
    
    /**
     * Adds all elements from the given ranking to this ranking.
     * @param ranking
     * a map of ranked elements
     */
	public void addAllFromRanking(final Ranking<T> ranking);

    /**
     * Creates a new ranking with this ranking and the other ranking merged together.
     * @param other
     * the other ranking to merge with this ranking
     * @return 
     * merged ranking
     */
    public Ranking<T> merge(final Ranking<T> other);
    
    /**
     * Returns the suspiciousness of the given node.
     * @param element
     * the node to get the suspiciousness of
     * @return 
     * ranking value, or NaN if ranking doesn't exist
     */
    public double getRankingValue(final T element);
    
    
    public double getBestRankingValue();
    
    public double getBestFiniteRankingValue();
    
    public double getWorstRankingValue();
    
    public double getWorstFiniteRankingValue();
    
    public T getBestRankingElement();
    
    public T getBestFiniteRankingElement();
    
    public T getWorstRankingElement();
    
    public T getWorstFiniteRankingElement();

    /**
     * Computes the wasted effort metric of an element in the ranking.
     * This is equal to the number of elements that are ranked higher 
     * than the given element.
     * @param element
     * the element to compute the metric for.
     * @return 
     * number of elements ranked higher as the given node.
     * @throws IllegalArgumentException
     * if the given element does not exist in the ranking
     */
    public int wastedEffort(final T element) throws IllegalArgumentException;

    /**
     * Returns all ranking metrics for a given node. This method
     * has to iterate over all ranked elements and is potentially slow.
     * Additionally, the metrics change potentially, when new elements are 
     * added to the ranking and the metrics have to be computed again
     * if this is the case.
     * @param element
     * the element to get the metrics for
     * @return 
     * metrics
     */
    public RankingMetric<T> getRankingMetrics(final T element);
    
    /**
     * This method may be slow and should be used with care. If suitable,
     * and if the order of returned elements is unimportant,
     * use {@link #getElements()} to access all ranked elements.
     * @return
     * sorted list of ranked elements in the ranking order
     * that is associated with this ranking (ascending or descending)
     */
    public List<RankedElement<T>> getSortedRankedElements();
    
    /**
     * This method may be slow and should be used with care. If suitable,
     * and if the order of returned elements is unimportant,
     * use {@link #getElements()} to access all ranked elements.
     * @param ascending
	 * true for ascending, false for descending ordering
     * @return
     * sorted list of ranked elements
     */
    public List<RankedElement<T>> getSortedRankedElements(boolean ascending);
    
    public static <T> List<RankedElement<T>> sortRankedElementList(boolean ascending, final List<RankedElement<T>> rankedNodes) {
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
    
    /**
     * @return
     * the map of elements, linked to their ranking values;
     * not sorted
     */
    public Map<T, Double> getElementMap();
    
    /**
     * @return
     * a collection of elements contained in this ranking
     */
    public Set<T> getElements();
    
    /**
     * @param element
     * the element
     * @return
     * whether this element is contained in this ranking
     */
    public boolean hasRanking(T element);
    
    /**
     * Saves the ranking result to a given file.
     * @param ranking
     * the ranking to save
     * @param filename
     * the file name to save the ranking to
     * @throws IOException
     * in case of not being able to write to the given path
     * @param <T>
     * the type of the ranked element
     */
    public static <T> void save(Ranking<T> ranking, final String filename) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filename);
            for (final RankedElement<T> el : ranking.getSortedRankedElements()) {
                writer.write(String.format("%s" + RANKING_SEPARATOR + "%f\n", el.getIdentifier(), el.getRankingValue()));
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
    
    /**
     * Saves this ranking in human readable form to the given file.
     * Best ranked elements are stored first.
     * @param filename
     * the file to use to store the ranking
     * @throws IOException
     * if saving to the file is not possible
     */
    default public void save(final String filename) throws IOException {
    	save(this, filename);
    }
    
    /**
     * Loads a ranking object from a given file.
     * <p> Ascending means that lower values get ranked first/best.
	 * <p> Descending means that higher values get ranked first/best.
     * @param file
     * the ranking file
     * @param ascending
     * true for ascending, false for descending ordering
     * @param nanStrategy
     * a strategy that assigns some value to NaN ranking values
     * @param posInfStrategy
     * a strategy that assigns some value to positive infinity ranking values
     * @param negInfStrategy
     * a strategy that assigns some value to negative infinity ranking values
     * @return
     * the ranking
     */
    public static Ranking<String> load(final Path file, boolean ascending, 
    		RankingStrategy nanStrategy, RankingStrategy posInfStrategy, RankingStrategy negInfStrategy) {
    	Ranking<String> ranking = new SimpleRanking<>(ascending);
    	List<String> nanIdentifiers = new ArrayList<>();
    	List<String> posInfIdentifiers = new ArrayList<>();
    	List<String> negInfIdentifiers = new ArrayList<>();
    	try (final BufferedReader reader = Files.newBufferedReader(file , StandardCharsets.UTF_8)) {
			//get the maximal ranking value that is NOT infinity
			String rankingline = null;
			while((rankingline = reader.readLine()) != null) {
				final int pos = rankingline.lastIndexOf(RANKING_SEPARATOR);
				if (pos == -1) {
					Log.abort(Ranking.class, "Entry '%s' not valid in '%s'.", rankingline, file.toAbsolutePath());
				}
				double rankingValue = Double.parseDouble(rankingline.substring(pos+1, rankingline.length()));
				if (Double.isNaN(rankingValue)) {
					nanIdentifiers.add(rankingline.substring(0, pos));
				} else if (rankingValue == Double.POSITIVE_INFINITY) {
					posInfIdentifiers.add(rankingline.substring(0, pos));
				} else if (rankingValue == Double.NEGATIVE_INFINITY) {
					negInfIdentifiers.add(rankingline.substring(0, pos));
				} else {
					ranking.add(rankingline.substring(0, pos), rankingValue);
				}
			}
		} catch (IOException e) {
			Log.abort(Ranking.class, e, "Could not open/read the ranking file '%s'.", file.toAbsolutePath());
		} catch (NumberFormatException e) {
			Log.abort(Ranking.class, e, "Ranking value not valid in '%s'.", file.toAbsolutePath());
		}
    	
    	double nanValue;
    	switch(nanStrategy) {
		case BEST:
			if (ranking.isAscending()) {
				nanValue = ranking.getBestRankingValue() - 1;
			} else {
				nanValue = ranking.getBestRankingValue() + 1;
			}
			break;
		case INFINITY:
			nanValue = Double.POSITIVE_INFINITY;
			break;
		case NAN:
			nanValue = Double.NaN;
			break;
		case NEGATIVE_INFINITY:
			nanValue = Double.NEGATIVE_INFINITY;
			break;
		case WORST:
			if (ranking.isAscending()) {
				nanValue = ranking.getWorstRankingValue() + 1;
			} else {
				nanValue = ranking.getWorstRankingValue() - 1;
			}
			break;
		case ZERO:
			nanValue = 0.0;
			break;
		default:
			nanValue = Double.NaN;
			break;
    	}
    	
    	double posInfValue;
    	switch(posInfStrategy) {
		case BEST:
			if (ranking.isAscending()) {
				posInfValue = ranking.getBestRankingValue() - 1;
			} else {
				posInfValue = ranking.getBestRankingValue() + 1;
			}
			break;
		case INFINITY:
			posInfValue = Double.POSITIVE_INFINITY;
			break;
		case NAN:
			posInfValue = Double.NaN;
			break;
		case NEGATIVE_INFINITY:
			posInfValue = Double.NEGATIVE_INFINITY;
			break;
		case WORST:
			if (ranking.isAscending()) {
				posInfValue = ranking.getWorstRankingValue() + 1;
			} else {
				posInfValue = ranking.getWorstRankingValue() - 1;
			}
			break;
		case ZERO:
			posInfValue = 0.0;
			break;
		default:
			posInfValue = Double.POSITIVE_INFINITY;
			break;
    	}
    	
    	double negInfValue;
    	switch(negInfStrategy) {
		case BEST:
			if (ranking.isAscending()) {
				negInfValue = ranking.getBestRankingValue() - 1;
			} else {
				negInfValue = ranking.getBestRankingValue() + 1;
			}
			break;
		case INFINITY:
			negInfValue = Double.POSITIVE_INFINITY;
			break;
		case NAN:
			negInfValue = Double.NaN;
			break;
		case NEGATIVE_INFINITY:
			negInfValue = Double.NEGATIVE_INFINITY;
			break;
		case WORST:
			if (ranking.isAscending()) {
				negInfValue = ranking.getWorstRankingValue() + 1;
			} else {
				negInfValue = ranking.getWorstRankingValue() - 1;
			}
			break;
		case ZERO:
			negInfValue = 0.0;
			break;
		default:
			negInfValue = Double.NEGATIVE_INFINITY;
			break;
    	}
    	
    	for (String identifier : negInfIdentifiers) {
    		ranking.add(identifier, negInfValue);
    	}
    	for (String identifier : posInfIdentifiers) {
    		ranking.add(identifier, posInfValue);
    	}
    	for (String identifier : nanIdentifiers) {
    		ranking.add(identifier, nanValue);
    	}
    	
    	return ranking;
    }
    
    /**
     * Applies the given strategies to the ranking and returns a
     * ranking with the strategies applied.
     * @param <T>
     * the type of raking element identifiers
     * @param originalRanking
     * the ranking
     * @param nanStrategy
     * a strategy that assigns some value to NaN ranking values
     * @param posInfStrategy
     * a strategy that assigns some value to positive infinity ranking values
     * @param negInfStrategy
     * a strategy that assigns some value to negative infinity ranking values
     * @return
     * a ranking with applied strategies
     */
    public static <T> Ranking<T> getRankingWithStrategies(Ranking<T> originalRanking, 
    		RankingStrategy nanStrategy, RankingStrategy posInfStrategy, RankingStrategy negInfStrategy) {
    	Ranking<T> ranking = new SimpleRanking<>(originalRanking.isAscending());
    	List<T> nanIdentifiers = new ArrayList<>();
    	List<T> posInfIdentifiers = new ArrayList<>();
    	List<T> negInfIdentifiers = new ArrayList<>();

    	for (T element : originalRanking.getElements()) {
    		double rankingValue = originalRanking.getRankingValue(element);
    		if (Double.isNaN(rankingValue)) {
    			nanIdentifiers.add(element);
    		} else if (rankingValue == Double.POSITIVE_INFINITY) {
    			posInfIdentifiers.add(element);
    		} else if (rankingValue == Double.NEGATIVE_INFINITY) {
    			negInfIdentifiers.add(element);
    		} else {
    			ranking.add(element, rankingValue);
    		}
    	}
    	
    	double nanValue;
    	switch(nanStrategy) {
		case BEST:
			if (ranking.isAscending()) {
				nanValue = ranking.getBestRankingValue() - 1;
			} else {
				nanValue = ranking.getBestRankingValue() + 1;
			}
			break;
		case INFINITY:
			nanValue = Double.POSITIVE_INFINITY;
			break;
		case NAN:
			nanValue = Double.NaN;
			break;
		case NEGATIVE_INFINITY:
			nanValue = Double.NEGATIVE_INFINITY;
			break;
		case WORST:
			if (ranking.isAscending()) {
				nanValue = ranking.getWorstRankingValue() + 1;
			} else {
				nanValue = ranking.getWorstRankingValue() - 1;
			}
			break;
		case ZERO:
			nanValue = 0.0;
			break;
		default:
			nanValue = Double.NaN;
			break;
    	}
    	
    	double posInfValue;
    	switch(posInfStrategy) {
		case BEST:
			if (ranking.isAscending()) {
				posInfValue = ranking.getBestRankingValue() - 1;
			} else {
				posInfValue = ranking.getBestRankingValue() + 1;
			}
			break;
		case INFINITY:
			posInfValue = Double.POSITIVE_INFINITY;
			break;
		case NAN:
			posInfValue = Double.NaN;
			break;
		case NEGATIVE_INFINITY:
			posInfValue = Double.NEGATIVE_INFINITY;
			break;
		case WORST:
			if (ranking.isAscending()) {
				posInfValue = ranking.getWorstRankingValue() + 1;
			} else {
				posInfValue = ranking.getWorstRankingValue() - 1;
			}
			break;
		case ZERO:
			posInfValue = 0.0;
			break;
		default:
			posInfValue = Double.POSITIVE_INFINITY;
			break;
    	}
    	
    	double negInfValue;
    	switch(negInfStrategy) {
		case BEST:
			if (ranking.isAscending()) {
				negInfValue = ranking.getBestRankingValue() - 1;
			} else {
				negInfValue = ranking.getBestRankingValue() + 1;
			}
			break;
		case INFINITY:
			negInfValue = Double.POSITIVE_INFINITY;
			break;
		case NAN:
			negInfValue = Double.NaN;
			break;
		case NEGATIVE_INFINITY:
			negInfValue = Double.NEGATIVE_INFINITY;
			break;
		case WORST:
			if (ranking.isAscending()) {
				negInfValue = ranking.getWorstRankingValue() + 1;
			} else {
				negInfValue = ranking.getWorstRankingValue() - 1;
			}
			break;
		case ZERO:
			negInfValue = 0.0;
			break;
		default:
			negInfValue = Double.NEGATIVE_INFINITY;
			break;
    	}
    	
    	for (T identifier : negInfIdentifiers) {
    		ranking.add(identifier, negInfValue);
    	}
    	for (T identifier : posInfIdentifiers) {
    		ranking.add(identifier, posInfValue);
    	}
    	for (T identifier : nanIdentifiers) {
    		ranking.add(identifier, nanValue);
    	}
    	
    	return ranking;
    }

    /**
	 * Combines two rankings, using the given combiner to combine
	 * two single data points with identical identifiers. If a
	 * data point doesn't exist on one of the rankings, it's
	 * ranking value is regarded as being zero.
	 * @param <T>
	 * the type of the ranking elements
	 * @param ranking1
	 * the first ranking
	 * @param ranking2
	 * the second ranking
	 * @param combiner
	 * the combiner
	 * @return
	 * the combined ranking (new instance obtained from ranking 1)
	 */
	public static <T> Ranking<T> combine(Ranking<T> ranking1, Ranking<T> ranking2, 
			BinaryOperator<Double> combiner) {
		Ranking<T> combinedRanking = ranking1.newInstance(ranking1.isAscending());
		for (T element1 : ranking1.getElements()) {
			double ranking = ranking2.getRankingValue(element1);
			if (Double.isNaN(ranking)) {
				ranking = 0;
			}
			combinedRanking.add(
					element1, combiner.apply(ranking1.getRankingValue(element1), ranking));
		}
		
		for (T element2 : ranking2.getElements()) {
			if (!combinedRanking.hasRanking(element2)) {
				double ranking = ranking1.getRankingValue(element2);
				if (Double.isNaN(ranking)) {
					ranking = 0;
				}
				combinedRanking.add(
						element2, combiner.apply(ranking, ranking2.getRankingValue(element2)));
			}
		}
		
		return combinedRanking;
	}
	
	/**
	 * Combines two rankings, using the given combiner to combine
	 * two single data points with identical identifiers. If a
	 * data point doesn't exist on one of the rankings, its
	 * ranking value is regarded as being zero. The rankings are
	 * normalized with the given strategy before they are combined.
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
			BinaryOperator<Double> combiner, NormalizationStrategy strategy) {
		NormalizedRanking<T> normalizedRanking1 = new NormalizedRanking<>(ranking1, strategy);
		NormalizedRanking<T> normalizedRanking2 = new NormalizedRanking<>(ranking2, strategy);
		
		return Ranking.combine(normalizedRanking1, normalizedRanking2, combiner);
	}

	public void outdateRankingCache();
	
	/**
	 * Manipulates the given ranking and returns the result.
	 * @param <T>
	 * the type of the ranking elements
	 * @param ranking
	 * the ranking to manipulate
	 * @param manipulator
	 * the manipulator
	 * @return
	 * the manipulated ranking (new instance obtained from the given ranking)
	 */
	public static <T> Ranking<T> manipulate(Ranking<T> ranking, 
			UnaryOperator<Double> manipulator) {
		Ranking<T> manipulatedRanking = ranking.newInstance(ranking.isAscending());
		for (T element : ranking.getElements()) {
			manipulatedRanking.add(
					element, manipulator.apply(ranking.getRankingValue(element)));
		}
		
		return manipulatedRanking;
	}
	
	
}
