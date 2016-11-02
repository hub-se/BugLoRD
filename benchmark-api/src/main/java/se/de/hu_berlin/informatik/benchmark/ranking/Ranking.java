package se.de.hu_berlin.informatik.benchmark.ranking;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public interface Ranking<T> {

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
     */
    public void add(final T element, final double rankingValue);
    
//    /**
//     * Adds all elements in the given collection.
//     * @param rankedElements
//     * the collection of elements
//     */
//    public void addAll(Collection<RankedElement<T>> rankedElements);
    
    /**
     * Adds all elements in the given Map.
     * @param elementMap
     * a map of ranked elements
     */
	public void addAll(Map<T, Double> elementMap);

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
    
    public double getWorstRankingValue();

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
     * Returns all ranking metrics for a given node.
     * @param element
     * the element to get the metrics for
     * @return 
     * metrics
     */
    public RankingMetric<T> getRankingMetrics(final T element);
    
    public List<RankedElement<T>> getSortedRankedElements();
    
    public Map<T, Double> getElementMap();
    
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
                writer.write(String.format("%s:%f\n", el.getIdentifier(), el.getRankingValue()));
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
    public static Ranking<String> load(final Path file, boolean ascending, RankingNaNStrategy.Strategy nanStrategy, 
    		RankingPosInfStrategy.Strategy posInfStrategy, RankingNegInfStrategy.Strategy negInfStrategy) {
    	Ranking<String> ranking = new SimpleRanking<>(ascending);
    	List<String> nanIdentifiers = new ArrayList<>();
    	List<String> posInfIdentifiers = new ArrayList<>();
    	List<String> negInfIdentifiers = new ArrayList<>();
    	try (final BufferedReader reader = Files.newBufferedReader(file , StandardCharsets.UTF_8)) {
			//get the maximal ranking value that is NOT infinity
			String rankingline = null;
			while((rankingline = reader.readLine()) != null) {
				final int pos = rankingline.lastIndexOf(':');
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
    		RankingNaNStrategy.Strategy nanStrategy, 
    		RankingPosInfStrategy.Strategy posInfStrategy, 
    		RankingNegInfStrategy.Strategy negInfStrategy) {
    	Ranking<T> ranking = new SimpleRanking<>(originalRanking.isAscending());
    	List<T> nanIdentifiers = new ArrayList<>();
    	List<T> posInfIdentifiers = new ArrayList<>();
    	List<T> negInfIdentifiers = new ArrayList<>();

    	for (Entry<T, Double> element : originalRanking.getElementMap().entrySet()) {
    		double rankingValue = element.getValue();
    		if (Double.isNaN(rankingValue)) {
    			nanIdentifiers.add(element.getKey());
    		} else if (rankingValue == Double.POSITIVE_INFINITY) {
    			posInfIdentifiers.add(element.getKey());
    		} else if (rankingValue == Double.NEGATIVE_INFINITY) {
    			negInfIdentifiers.add(element.getKey());
    		} else {
    			ranking.add(element.getKey(), rankingValue);
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
			RankingCombiner<Double> combiner) {
		Ranking<T> combinedRanking = ranking1.newInstance(ranking1.isAscending());
		for (Entry<T, Double> element1 : ranking1.getElementMap().entrySet()) {
			double ranking = ranking2.getRankingValue(element1.getKey());
			if (Double.isNaN(ranking)) {
				ranking = 0;
			}
			combinedRanking.add(
					element1.getKey(), 
					combiner.combine(element1.getValue(), ranking));
		}
		
		for (Entry<T, Double> element2 : ranking2.getElementMap().entrySet()) {
			if (!combinedRanking.hasRanking(element2.getKey())) {
				double ranking = ranking1.getRankingValue(element2.getKey());
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

	void outdateRankingCache();



	
	
}
