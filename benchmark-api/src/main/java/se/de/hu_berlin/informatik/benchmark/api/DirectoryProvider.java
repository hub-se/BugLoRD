package se.de.hu_berlin.informatik.benchmark.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * An interface that contains a collection of methods to provide 
 * directory paths for a benchmark entity.
 * 
 * @author Simon Heiden
 * 
 * @see Entity
 */
public interface DirectoryProvider {
	
	/**
	 * @return
	 * the path to the main archive directory
	 */
	public String getBenchmarkArchiveDir();
	
	/**
	 * @return
	 * the path to the main execution directory
	 */
	public String getBenchmarkExecutionDir();
	
	/**
	 * Deletes all entity related directories, excluding
	 * the data directory.
	 * @return
	 * true if successful; false otherwise
	 */
	default public boolean deleteAllEntityDirectories() {
		tryDeleteExecutionDirectory(true);
		deleteArchiveDirectory();
		return true;
	}
	
	/**
	 * Deletes all entity related directories, including
	 * the data directory.
	 * @return
	 * true if successful; false otherwise
	 */
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
	 * Deletes the entity archive directory.
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
	 * Moves the execution directory if archive and execution directory 
	 * aren't identical.
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
	
	/**
	 * @param executionMode
	 * whether to use the execution directory version
	 * @return
	 * the path to the main benchmark directory
	 */
	default public Path getBenchmarkDir(boolean executionMode) {
		if (executionMode) {
			return Paths.get(getBenchmarkExecutionDir());
		} else {
			return Paths.get(getBenchmarkArchiveDir());
		}
	}
	
	/**
	 * @param executionMode
	 * whether to use the execution directory version
	 * @return
	 * the path to the entity directory
	 */
	default public Path getEntityDir(boolean executionMode) {
		Path result = getBenchmarkDir(executionMode).resolve(getRelativeEntityPath());
//		result.toFile().mkdirs();
		return result;
	}
	
	/**
	 * @return
	 * the relative entity path, starting from the main benchmark directory
	 */
	public Path getRelativeEntityPath();
	
	/**
	 * @return
	 * an identifier for the entity; will be used as a directory name
	 */
	public String getEntityIdentifier();

	/**
	 * @param executionMode
	 * whether to use the execution directory version
	 * @return
	 * the path to the working directory which is the path: 
	 * benchmarkDir/relativeEntityPath/entityIdentifier
	 */
	default public Path getWorkDir(boolean executionMode) {
		Path result = getEntityDir(executionMode).resolve(getEntityIdentifier());
//		result.toFile().mkdirs();
		return result;
	}
	
	/**
	 * @return
	 * the path to the data directory related to the entity
	 */
	default public Path getWorkDataDir() {
		Path result = getEntityDir(false).resolve(BugLoRDConstants.DATA_DIR_NAME);
//		result.toFile().mkdirs();
		return result;
	}
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be returned
	 * @return
	 * the path to the main source directory
	 * @throws UnsupportedOperationException
	 * if this operation is not implemented
	 */
	public Path getMainSourceDir(boolean executionMode) throws UnsupportedOperationException;
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be returned
	 * @return
	 * the path to the main test source directory
	 * @throws UnsupportedOperationException
	 * if this operation is not implemented
	 */
	public Path getTestSourceDir(boolean executionMode) throws UnsupportedOperationException;
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be returned
	 * @return
	 * the path to the main binaries directory
	 * @throws UnsupportedOperationException
	 * if this operation is not implemented
	 */
	public Path getMainBinDir(boolean executionMode) throws UnsupportedOperationException;
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be returned
	 * @return
	 * the path to the main test binaries directory
	 * @throws UnsupportedOperationException
	 * if this operation is not implemented
	 */
	public Path getTestBinDir(boolean executionMode) throws UnsupportedOperationException;
		
}
