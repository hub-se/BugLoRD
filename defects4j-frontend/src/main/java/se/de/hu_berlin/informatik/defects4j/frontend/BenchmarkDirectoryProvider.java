package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import se.de.hu_berlin.informatik.defects4j.frontend.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public abstract class BenchmarkDirectoryProvider implements DirectoryProvider {
	
	private String mainDir;
	private String workDir;
	
	private boolean isInExecutionMode = false;
	

	@Override
	public void switchToExecutionDir() {
		mainDir = Defects4J.getValueOf(Defects4JProperties.EXECUTION_DIR);
		setDirsCorrectlyAfterSwitch();
		isInExecutionMode = true;
	}
	
	@Override
	public void switchToArchiveDir() {
		mainDir = Defects4J.getValueOf(Defects4JProperties.ARCHIVE_DIR);
		setDirsCorrectlyAfterSwitch();
		isInExecutionMode = false;
	}
	
	@Override
	public boolean isInExecutionMode() {
		return isInExecutionMode;
	}
	
	@Override
	public boolean deleteAllEntityDirectories() {
		tryDeleteExecutionDirectory(true);
		deleteArchiveDirectory();
		return true;
	}

	@Override
	public void tryDeleteExecutionDirectory(boolean force) {
		boolean temp = isInExecutionMode();
		switchToArchiveDir();
		File archiveMainDir = new File(mainDir);
		switchToExecutionDir();
		File executionmainDir = new File(mainDir);
		
		if (force || !archiveMainDir.equals(executionmainDir)) {
			FileUtils.delete(new File(workDir));
		}
		
		if (!temp) {
			switchToArchiveDir();
		}
	}

	@Override
	public void deleteArchiveDirectory() {
		boolean temp = isInExecutionMode();
		switchToArchiveDir();

		FileUtils.delete(new File(workDir));
		
		if (temp) {
			switchToExecutionDir();
		}
	}
	
	@Override
	public void tryMovingExecutionDirToArchive() {
		boolean temp = isInExecutionMode();
		switchToArchiveDir();
		File archiveMainDir = new File(mainDir);
		File archiveWorkDir = new File(workDir);
		switchToExecutionDir();
		File executionMainDir = new File(mainDir);
		File executionWorkDir = new File(workDir);
		if (!archiveMainDir.equals(executionMainDir)) {
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

	@Override
	public Path getBenchmarkDir() {
		return Paths.get(mainDir);
	}
	
	@Override
	public Path getWorkDir() {
		return Paths.get(workDir);
	}
	
	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}
}
