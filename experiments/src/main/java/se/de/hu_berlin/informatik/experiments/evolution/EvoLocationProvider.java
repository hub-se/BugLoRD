package se.de.hu_berlin.informatik.experiments.evolution;

import se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm.LocationSelectionStrategy;

public interface EvoLocationProvider<T,L> {

	/**
	 * Should produce a mutation location based on the given item
	 * and the given strategy.
	 * @param item
	 * the item to produce a location for
	 * @param strategy
	 * the strategy to use to produce the location
	 * @return
	 * the produced location
	 */
	public L getNextLocation(T item, LocationSelectionStrategy strategy);
	
}
