package se.de.hu_berlin.informatik.experiments.evolution;

public interface EvoMutation<L,T> {

	public L getLocation();
	
	public T mutate(T target, L location);
	
}
