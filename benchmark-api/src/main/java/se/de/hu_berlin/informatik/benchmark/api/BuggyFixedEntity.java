package se.de.hu_berlin.informatik.benchmark.api;

import se.de.hu_berlin.informatik.benchmark.modification.Modification;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

import java.util.List;
import java.util.Map;

public interface BuggyFixedEntity<T extends Entity> {

//	default public Entity getBuggyEntity(BuggyFixedEntity entity) {
//		return entity.getBuggyVersion();
//	}
//	
//	default public Entity getFixedEntity(BuggyFixedEntity entity) {
//		return entity.getFixedVersion();
//	}

    public T getBuggyVersion();

    public T getFixedVersion();

//	public List<String> getModifiedClasses();


    public Map<String, List<Modification>> getAllChanges(
            boolean executionModeBug, boolean resetBug, boolean deleteBugAfterwards,
            boolean executionModeFix, boolean resetFix, boolean deleteFixAfterwards);

    default public boolean getAndSaveAllChangesToFile(
            boolean executionModeBug, boolean resetBug, boolean deleteBugAfterwards,
            boolean executionModeFix, boolean resetFix, boolean deleteFixAfterwards) {
        Map<String, List<Modification>> changes = getAllChanges(
                executionModeBug, resetBug, deleteBugAfterwards,
                executionModeFix, resetFix, deleteFixAfterwards);
        if (changes == null) {
            Log.err(this, "Acquiring changes was not successful. Nothing will be saved.");
            return false;
        }
        Modification.storeChanges(changes, getBuggyVersion().getWorkDataDir().resolve(BugLoRDConstants.CHANGES_FILE_NAME));
        Modification.storeChangesHumanReadable(changes, getBuggyVersion().getWorkDataDir().resolve(BugLoRDConstants.CHANGES_FILE_NAME_HUMAN));
        return true;
    }

    default public Map<String, List<Modification>> loadChangesFromFile() {
        return Modification.readChangesFromFile(getBuggyVersion().getWorkDataDir().resolve(BugLoRDConstants.CHANGES_FILE_NAME));
    }

    public default boolean requireBug(boolean executionMode) {
        T bug = getBuggyVersion();
        if (!getBuggyVersion().getWorkDir(executionMode).toFile().exists()) {
            bug.resetAndInitialize(executionMode, true);
            return false;
        }
        return true;
    }

    public default boolean requireFix(boolean executionMode) {
        T fix = getFixedVersion();
        if (!getFixedVersion().getWorkDir(executionMode).toFile().exists()) {
            fix.resetAndInitialize(executionMode, true);
            return false;
        }
        return true;
    }

    /**
     * Should return a List of Strings which contains all modified source files with one file per line.
     * <p> line format: {@code qualified.class.name}
     * <p> example: {@code com.google.javascript.jscomp.FlowSensitiveInlineVariables}
     *
     * @param executionMode whether the execution directory should be used to make the necessary system call
     * @return the list of modified classes
     */
    public List<String> getModifiedClasses(boolean executionMode) throws UnsupportedOperationException;

    /**
     * @return an identifier for this entity that is unique
     */
    public String getUniqueIdentifier();

}
