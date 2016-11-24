package se.de.hu_berlin.informatik.benchmark.api;

import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public interface BuggyFixedEntity {
	
//	default public Entity getBuggyEntity(BuggyFixedEntity entity) {
//		return entity.getBuggyVersion();
//	}
//	
//	default public Entity getFixedEntity(BuggyFixedEntity entity) {
//		return entity.getFixedVersion();
//	}
	
	public Entity getBuggyVersion();
	
	public Entity getFixedVersion();

//	public List<String> getModifiedClasses();
	
	
	public Map<String, List<ChangeWrapper>> getAllChanges(
			boolean executionModeBug, boolean resetBug, boolean deleteBugAfterwards,
			boolean executionModeFix, boolean resetFix, boolean deleteFixAfterwards);
	
	default public boolean getAndSaveAllChangesToFile(
			boolean executionModeBug, boolean resetBug, boolean deleteBugAfterwards,
			boolean executionModeFix, boolean resetFix, boolean deleteFixAfterwards) {
		Map<String, List<ChangeWrapper>> changes = getAllChanges(
				executionModeBug, resetBug, deleteBugAfterwards, 
				executionModeFix, resetFix, deleteFixAfterwards);
		if (changes == null) {
			Log.err(this, "Acquiring changes was not successful. Nothing will be saved.");
			return false;
		}
		ChangeWrapper.storeChanges(changes, getBuggyVersion().getWorkDataDir().resolve(BugLoRDConstants.CHANGES_FILE_NAME));
		ChangeWrapper.storeChangesHumanReadable(changes, getBuggyVersion().getWorkDataDir().resolve(BugLoRDConstants.CHANGES_FILE_NAME_HUMAN));
		return true;
	}
	
	default public Map<String, List<ChangeWrapper>> loadChangesFromFile() {
		return ChangeWrapper.readChangesFromFile(getBuggyVersion().getWorkDataDir().resolve(BugLoRDConstants.CHANGES_FILE_NAME));
	}

	public default boolean requireBug(boolean executionMode) {
		Entity bug = getBuggyVersion();
		if (!getBuggyVersion().getWorkDir(executionMode).toFile().exists()) {
			bug.resetAndInitialize(executionMode, true);
			return false;
		}
		return true;
	}
	
	public default boolean requireFix(boolean executionMode) {
		Entity fix = getFixedVersion();
		if (!getFixedVersion().getWorkDir(executionMode).toFile().exists()) {
			fix.resetAndInitialize(executionMode, true);
			return false;
		}
		return true;
	}
	
}
