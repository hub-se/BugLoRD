package se.de.hu_berlin.informatik.benchmark.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public abstract class AbstractDirectoryProvider implements DirectoryProvider {
	
	private String mainDir;
	private String workDir;
	
	private Path mainSrcDir = null;
	private Path testSrcDir = null;
	private Path mainBinDir = null;
	private Path testBinDir = null;
	
	private boolean isInExecutionMode = false;
	

	@Override
	public void switchToExecutionDir() {
		mainDir = getBenchmarkExecutionDir();
		setDirsCorrectlyAfterSwitch();
		isInExecutionMode = true;
	}
	
	@Override
	public void switchToArchiveDir() {
		mainDir = getBenchmarkArchiveDir();
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
	
	protected void setWorkDir(String workDir) {
		this.workDir = workDir;
	}
	
	
	
	@Override
	public Path getMainSourceDir() {
		if (mainSrcDir == null) {
			mainSrcDir = computeMainSourceDir();
		}
		return mainSrcDir;
	}
	
	abstract public Path computeMainSourceDir();

	@Override
	public Path getTestSourceDir() {
		if (testSrcDir == null) {
			testSrcDir = computeTestSourceDir();
		}
		return testSrcDir;
	}
	
	abstract public Path computeTestSourceDir();

	@Override
	public Path getMainBinDir() {
		if (mainBinDir == null) {
			mainBinDir = computeMainBinDir();
		}
		return mainBinDir;
	}
	
	abstract public Path computeMainBinDir();

	@Override
	public Path getTestBinDir() {
		if (testBinDir == null) {
			testBinDir = computeTestBinDir();
		}
		return testBinDir;
	}
	
	abstract public Path computeTestBinDir();
}
