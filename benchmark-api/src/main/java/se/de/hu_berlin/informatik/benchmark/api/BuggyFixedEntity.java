package se.de.hu_berlin.informatik.benchmark.api;

import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public interface BuggyFixedEntity extends Entity {
	
	public static final String CHANGES_FILE_NAME = ".changes";
	public static final String CHANGES_FILE_NAME_HUMAN = ".changes_human";
	
	default public BuggyFixedEntity getBuggyEntity(BuggyFixedEntity entity) {
		return entity.getBuggyVersion();
	}
	
	default public BuggyFixedEntity getFixedEntity(BuggyFixedEntity entity) {
		return entity.getFixedVersion();
	}
	
	public BuggyFixedEntity getBuggyVersion();
	
	public BuggyFixedEntity getFixedVersion();

//	public List<String> getModifiedClasses();
	
	
	public Map<String, List<ChangeWrapper>> getAllChanges(boolean executionModeBug, boolean executionModeFix);
	
	default public boolean saveAllChangesToFile(boolean executionModeBug, boolean executionModeFix) {
		Map<String, List<ChangeWrapper>> changes = getAllChanges(executionModeBug, executionModeFix);
		if (changes == null) {
			Log.err(this, "Acquiring changes was not successful. Nothing will be saved.");
			return false;
		}
		ChangeWrapper.storeChanges(changes, getWorkDataDir().resolve(CHANGES_FILE_NAME));
		ChangeWrapper.storeChangesHumanReadable(changes, getWorkDataDir().resolve(CHANGES_FILE_NAME_HUMAN));
		return true;
	}
	
	default public Map<String, List<ChangeWrapper>> loadChangesFromFile() {
		return ChangeWrapper.readChangesFromFile(getWorkDataDir().resolve(CHANGES_FILE_NAME));
	}
	
}
