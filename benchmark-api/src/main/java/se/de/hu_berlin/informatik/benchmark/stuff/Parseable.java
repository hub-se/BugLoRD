package se.de.hu_berlin.informatik.benchmark.stuff;

public interface Parseable<T> {
	
	public String createIdentifier();
	
	public T parseFromString(String identifier);

}
