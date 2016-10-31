package se.de.hu_berlin.informatik.benchmark.api;

import se.de.hu_berlin.informatik.benchmark.Bug;

public interface BuggyEntity extends Entity {

	public Bug getBug() throws IllegalStateException;
	
	public boolean setBug(Bug bug);
	
}
