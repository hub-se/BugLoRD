package se.de.hu_berlin.informatik.defects4j.frontend;

import java.util.List;

public interface BuggyFixedBenchmarkEntity extends BenchmarkEntity {
	
	default public BuggyFixedBenchmarkEntity getBuggyEntity(BuggyFixedBenchmarkEntity entity) {
		return entity.getBuggyVersion();
	}
	
	default public BuggyFixedBenchmarkEntity getFixedEntity(BuggyFixedBenchmarkEntity entity) {
		return entity.getFixedVersion();
	}
	
	public BuggyFixedBenchmarkEntity getBuggyVersion();
	
	public BuggyFixedBenchmarkEntity getFixedVersion();

	public List<String> getModifiedSources();
	
}
