package se.de.hu_berlin.informatik.experiments.evolution;

import java.util.Collection;
import se.de.hu_berlin.informatik.experiments.evolution.EvolutionaryAlgorithm.MutationSelectionStrategy;

public interface EvoMutationProvider<T,L> {

	/**
	 * Produces a mutation that can be applied to an object of type T, given a location of type L.
	 * Uses the given strategy to pick a mutation.
	 * @param strategy
	 * the strategy to use when choosing a mutation
	 * @return
	 * the mutation
	 */
	public EvoMutation<T,L> getNextMutation(MutationSelectionStrategy strategy);
	
	/**
	 * Adds the given mutation to the collection of possible mutations,
	 * @param mutation
	 * the mutation to add
	 * @return
	 * true if successful; false otherwise
	 */
	public boolean addMutation(EvoMutation<T,L> mutation);
	
	/**
	 * Adds the given mutations to the collection of possible mutations,
	 * @param mutations
	 * the mutations to add
	 * @return
	 * true if successful; false otherwise
	 */
	default public boolean addMutations(Collection<EvoMutation<T,L>> mutations) {
		boolean result = true;
		for (EvoMutation<T,L> mutation : mutations) {
			result &= addMutation(mutation);
		}
		return result;
	}
	
	/**
	 * Adds the given mutations to the collection of possible mutations,
	 * @param mutations
	 * the mutations to add
	 * @return
	 * true if successful; false otherwise
	 */
	default public boolean addMutations(@SuppressWarnings("unchecked") EvoMutation<T,L>... mutations) {
		boolean result = true;
		for (EvoMutation<T,L> mutation : mutations) {
			result &= addMutation(mutation);
		}
		return result;
	}
	
	/**
	 * @return
	 * the collection of mutations in the pool
	 */
	public Collection<EvoMutation<T,L>> getMutations();
	
}
