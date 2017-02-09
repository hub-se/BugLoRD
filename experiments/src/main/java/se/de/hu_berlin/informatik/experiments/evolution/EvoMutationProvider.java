package se.de.hu_berlin.informatik.experiments.evolution;

public interface EvoMutationProvider<L,T> {

	public EvoMutation<L,T> getRandomMutation();
	
}
