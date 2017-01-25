package se.de.hu_berlin.informatik.benchmark.ranking;

@FunctionalInterface
public interface RankingManipulator<T> {

	public T manipulate(T ranking);
	
}
