package se.de.hu_berlin.informatik.experiments.evolution;

public interface EvoResult<T,F> extends Comparable<F> {

	/**
	 * @return
	 * the fitness of the item
	 */
	public F getFitness();
	
	/**
	 * @return
	 * the item
	 */
	public T getItem();
	
	/**
	 * Cleans up any traces of this item in case it is
	 * not part of the population any more. This may clean up
	 * directory structures or nullify object pointers, etc.
	 * @return
	 * true if successful; false otherwise
	 */
	public boolean cleanUp();
}
