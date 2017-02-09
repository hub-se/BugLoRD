package se.de.hu_berlin.informatik.experiments.evolution;

import se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm.MutationSelectionStrategy;

public interface EvoMutationProvider<L,T> {

	public EvoMutation<L,T> getMutation(MutationSelectionStrategy strategy);
	
}
