package se.de.hu_berlin.informatik.benchmark.ranking;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.SortedSet;

public interface Ranking<T> {

	public Ranking<T> newInstance();
	
	/**
     * Adds an element with its ranking value to the ranking.
     * @param element
     * the element to add to the ranking
     * @param rankingValue
     * the determined ranking value of the element
     */
    public void add(final T element, final double rankingValue);
    
    /**
     * Adds all elements in the given collection.
     * @param rankedElements
     * the collection of element
     */
    public void addAll(Collection<RankedElement<T>> rankedElements);

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
     * ranking value, or 0 if ranking doesn't exist
     */
    public double getRankingValue(final T element);

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
    
    public SortedSet<RankedElement<T>> getRankedElements();
    
    /**
     * Saves the ranking result to a given file.
     *
     * @param filename
     *            the file name to save the ranking to
     * @throws IOException
     * 			  in case of not being able to write to the given path
     * @param <T>
     * the type of the ranked element
     */
    public static <T> void save(Ranking<T> ranking, final String filename) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filename);
            for (final RankedElement<T> el : ranking.getRankedElements()) {
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

	public boolean hasRanking(T element);
}
