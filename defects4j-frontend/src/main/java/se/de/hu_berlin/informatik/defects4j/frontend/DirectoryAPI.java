package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.io.IOException;

import se.de.hu_berlin.informatik.defects4j.frontend.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public interface DirectoryAPI {
	
	public void setBenchmarkDir(String path);
	

	default public void switchToExecutionDir() {
		setBenchmarkDir(Defects4J.getValueOf(Defects4JProperties.EXECUTION_DIR));
		setProjectAndBugDirAfterSwitch();
		setExecutionMode(true);
	}
	
	default public void switchToArchiveDir() {
		setBenchmarkDir(Defects4J.getValueOf(Defects4JProperties.ARCHIVE_DIR));
		setProjectAndBugDirAfterSwitch();
		setExecutionMode(false);
	}
	
	public void setExecutionMode(boolean executionMode);
	
	public boolean isInExecutionMode();
	
	default private void setProjectAndBugDirAfterSwitch() {
		projectDir = mainDir + SEP + project;
		if (buggyVersion) {
			workDir = projectDir + SEP + bugID + "b";
		} else {
			workDir = projectDir + SEP + bugID + "f";
		}
	}
	
	default public boolean deleteAll() {
		tryDeleteExecutionDirectory(true);
		deleteArchiveDirectory();
		return true;
	}
	
	/**
	 * Deletes the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical or if forced to...
	 * @param force
	 * whether to force deletion, even if the execution directory is equal to the archive directory
	 */
	default public void tryDeleteExecutionDirectory(boolean force) {
		boolean temp = isInExecutionMode();
		switchToArchiveDir();
		File archiveProjectDir = new File(projectDir);
		switchToExecutionDir();
		File executionProjectDir = new File(projectDir);
		
		if (force || !archiveProjectDir.equals(executionProjectDir)) {
			FileUtils.delete(new File(workDir));
		}
		
		if (!temp) {
			switchToArchiveDir();
		}
	}
	
	/**
	 * Deletes the buggy or fixed version archive directory.
	 */
	default public void deleteArchiveDirectory() {
		boolean temp = isInExecutionMode();
		switchToArchiveDir();

		FileUtils.delete(new File(workDir));
		
		if (temp) {
			switchToExecutionDir();
		}
	}
	
	/**
	 * Moves the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical...
	 */
	default public void tryMovingExecutionDirToArchive() {
		boolean temp = isInExecutionMode();
		switchToArchiveDir();
		File archiveProjectDir = new File(projectDir);
		File archiveWorkDir = new File(workDir);
		switchToExecutionDir();
		File executionProjectDir = new File(projectDir);
		File executionWorkDir = new File(workDir);
		if (!archiveProjectDir.equals(executionProjectDir)) {
			FileUtils.delete(archiveWorkDir);
			try {
				FileUtils.copyFileOrDir(executionWorkDir, archiveWorkDir);
			} catch (IOException e) {
				Log.abort(this, "IOException while trying to copy directory '%s' to '%s'.",
						executionWorkDir, archiveWorkDir);
			}
			FileUtils.delete(executionWorkDir);
		}
		
		if (!temp) {
			switchToArchiveDir();
		}
	}
}
