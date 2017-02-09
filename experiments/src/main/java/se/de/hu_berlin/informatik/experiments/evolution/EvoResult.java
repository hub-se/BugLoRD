package se.de.hu_berlin.informatik.experiments.evolution;

public interface EvoResult<T,F> extends Comparable<F> {

	public F getFitness();
	
	public T getItem();
	
	public boolean cleanUp();
}
