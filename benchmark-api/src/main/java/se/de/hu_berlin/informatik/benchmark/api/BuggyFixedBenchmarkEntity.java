package se.de.hu_berlin.informatik.benchmark.api;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;

public interface BuggyFixedBenchmarkEntity extends BenchmarkEntity {
	
	public static final String CHANGES_FILE_NAME = ".changes";
	public static final String CHANGES_FILE_NAME_HUMAN = ".changes_human";
	
	default public BuggyFixedBenchmarkEntity getBuggyEntity(BuggyFixedBenchmarkEntity entity) {
		return entity.getBuggyVersion();
	}
	
	default public BuggyFixedBenchmarkEntity getFixedEntity(BuggyFixedBenchmarkEntity entity) {
		return entity.getFixedVersion();
	}
	
	public BuggyFixedBenchmarkEntity getBuggyVersion();
	
	public BuggyFixedBenchmarkEntity getFixedVersion();

	public List<String> getModifiedClasses();
	
	public List<ChangeWrapper> getChanges(String className);
	
//	public List<ChangeWrapper> getChanges(Path pathToClassFile);
	
	default public Map<String, List<ChangeWrapper>> getAllChanges() {
		Map<String, List<ChangeWrapper>> map = new HashMap<>();
		for (String clazz : getModifiedClasses()) {
			map.put(clazz, getChanges(clazz));
		}
		return map;
	}
	
	default public void saveAllChangesToFile() {
		Map<String, List<ChangeWrapper>> changes = getAllChanges();
		ChangeWrapper.storeChanges(changes, getWorkDataDir().resolve(CHANGES_FILE_NAME));
		ChangeWrapper.storeChangesHumanReadable(changes, getWorkDataDir().resolve(CHANGES_FILE_NAME_HUMAN));
	}
	
	default public Map<String, List<ChangeWrapper>> loadChangesFromFile() {
		return ChangeWrapper.readChangesFromFile(getWorkDataDir().resolve(CHANGES_FILE_NAME));
	}
	
}
