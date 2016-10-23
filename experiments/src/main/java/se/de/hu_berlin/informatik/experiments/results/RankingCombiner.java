package se.de.hu_berlin.informatik.experiments.results;

public interface RankingCombiner<T> {

	public T combine(T ranking1, T ranking2);
	
}
