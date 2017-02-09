package se.de.hu_berlin.informatik.experiments.evolution;

public interface EvoRecombiner<T> {

	public T recombine(T parent1, T parent2);
	
}
