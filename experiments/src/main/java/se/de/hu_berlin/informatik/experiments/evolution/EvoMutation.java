package se.de.hu_berlin.informatik.experiments.evolution;

@FunctionalInterface
public interface EvoMutation<T,L> {

	/**
	 * Mutates the target object based on the given location.
	 * @param target
	 * the target object to mutate
	 * @param location
	 * the location at which to mutate the target object
	 * @return
	 * the mutated object
	 */
	public T applyTo(T target, L location);
	
}
