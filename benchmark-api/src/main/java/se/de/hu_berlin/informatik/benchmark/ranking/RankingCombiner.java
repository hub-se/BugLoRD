package se.de.hu_berlin.informatik.benchmark.ranking;

@FunctionalInterface
public interface RankingCombiner<T> {

	public T combine(T ranking1, T ranking2);
	
}
