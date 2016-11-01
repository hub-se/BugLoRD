package se.de.hu_berlin.informatik.benchmark.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public interface DirectoryProvider {
	
	public final static String SEP = File.separator;
	
	public String getBenchmarkArchiveDir();
	
	public String getBenchmarkExecutionDir();
	
	default public boolean deleteAllEntityDirectories() {
		tryDeleteExecutionDirectory(true);
		deleteArchiveDirectory();
		return true;
	}
	
	default public boolean deleteAllEntityDirectoriesAndData() {
		deleteAllEntityDirectories();
		deleteWorkDataDirectory();
		return true;
	}
	
	/**
	 * Deletes the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical or if forced to...
	 * @param force
	 * whether to force deletion, even if the execution directory is equal to the archive directory
	 */
	default public void tryDeleteExecutionDirectory(boolean force) {
		File archiveMainDir = new File(getBenchmarkArchiveDir());
		File executionmainDir = new File(getBenchmarkExecutionDir());
		
		if (force || !archiveMainDir.equals(executionmainDir)) {
			FileUtils.delete(getWorkDir(true));
		}
	}
	
	/**
	 * Deletes the buggy or fixed version archive directory.
	 */
	default public void deleteArchiveDirectory()  {
		FileUtils.delete(getWorkDir(false));
	}
	
	/**
	 * Deletes the work data directory.
	 */
	default public void deleteWorkDataDirectory() {
		FileUtils.delete(getWorkDataDir());
	}
	
	/**
	 * Moves the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical...
	 */
	default public void tryMovingExecutionDirToArchive() {
		File archiveMainDir = new File(getBenchmarkArchiveDir());
		File executionMainDir = new File(getBenchmarkExecutionDir());
		if (!archiveMainDir.equals(executionMainDir)) {
			deleteArchiveDirectory();
			try {
				FileUtils.copyFileOrDir(getWorkDir(true).toFile(), getWorkDir(false).toFile());
			} catch (IOException e) {
				Log.abort(this, "IOException while trying to copy directory '%s' to '%s'.",
						getWorkDir(true), getWorkDir(false));
			}
			FileUtils.delete(getWorkDir(true));
		}
	}
	
	default public Path getBenchmarkDir(boolean executionMode) {
		if (executionMode) {
			return Paths.get(getBenchmarkExecutionDir());
		} else {
			return Paths.get(getBenchmarkArchiveDir());
		}
	}
	
	default public Path getEntityDir(boolean executionMode) {
		Path result = getBenchmarkDir(executionMode).resolve(getRelativeEntityPath());
//		result.toFile().mkdirs();
		return result;
	}
	
	public Path getRelativeEntityPath();

	default public Path getWorkDir(boolean executionMode) {
		Path result = getEntityDir(executionMode).resolve(getRelativeEntityPath());
//		result.toFile().mkdirs();
		return result;
	}
	
	default public Path getWorkDataDir() {
		Path result = getEntityDir(false).resolve(".BugLoRD_data");
//		result.toFile().mkdirs();
		return result;
	}
	
	
	public Path getMainSourceDir(boolean executionMode) throws UnsupportedOperationException;
	
	public Path getTestSourceDir(boolean executionMode) throws UnsupportedOperationException;
	
	public Path getMainBinDir(boolean executionMode) throws UnsupportedOperationException;
	
	public Path getTestBinDir(boolean executionMode) throws UnsupportedOperationException;
	
	
}
