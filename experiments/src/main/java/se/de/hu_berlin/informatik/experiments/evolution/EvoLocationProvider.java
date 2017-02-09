package se.de.hu_berlin.informatik.experiments.evolution;

import se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm.LocationSelectionStrategy;

public interface EvoLocationProvider<T,L> {

	public L getNextLocation(T item, LocationSelectionStrategy strategy);
	
}
