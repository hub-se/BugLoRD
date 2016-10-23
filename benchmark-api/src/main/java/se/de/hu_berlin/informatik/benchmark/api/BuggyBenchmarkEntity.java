package se.de.hu_berlin.informatik.benchmark.api;

import se.de.hu_berlin.informatik.benchmark.Bug;

public interface BuggyBenchmarkEntity extends BenchmarkEntity {

	public Bug getBug() throws IllegalStateException;
	
	public boolean setBug(Bug bug);
	
}
