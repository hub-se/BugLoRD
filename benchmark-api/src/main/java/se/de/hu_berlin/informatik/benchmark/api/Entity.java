package se.de.hu_berlin.informatik.benchmark.api;

import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;

public interface Entity {
	
	/**
	 * Resets the entity to a fresh checked-out/initialized state.
	 * This may include a repository checkout and the previous
	 * deletion of already existing files and directories.
	 * @param executionMode
	 * whether execution directories should be used
	 * @param deleteExisting
	 * whether to delete already existing directories
	 * @return
	 * true if successful, false otherwise
	 */
	public boolean resetAndInitialize(boolean executionMode, boolean deleteExisting);
	
	
	public boolean isInitialized();

	public boolean compile(boolean executionMode);
	
	public boolean deleteAllButData();
	
	public boolean deleteData();
	
	default public boolean deleteAll() {
		deleteAllButData();
		deleteData();
		FileUtils.delete(getEntityDir(true));
		FileUtils.delete(getEntityDir(false));
		return true;
	}
	
	public DirectoryProvider getDirectoryProvider();
	
	default public Path getBenchmarkDir(boolean executionMode) {
		return getDirectoryProvider().getBenchmarkDir(executionMode);
	}
	
	default public Path getEntityDir(boolean executionMode) {
		return getDirectoryProvider().getEntityDir(executionMode);
	}
	
	default public Path getWorkDir(boolean executionMode) {
		return getDirectoryProvider().getWorkDir(executionMode);
	}
	
	default public Path getWorkDataDir() {
		return getDirectoryProvider().getWorkDataDir();
	}
	
	public void removeUnnecessaryFiles(boolean executionMode) throws UnsupportedOperationException;

	
	
	default public Path getMainSourceDir(boolean executionMode) throws UnsupportedOperationException {
		return getDirectoryProvider().getMainSourceDir(executionMode);
	}
	
	default public Path getTestSourceDir(boolean executionMode) throws UnsupportedOperationException {
		return getDirectoryProvider().getTestSourceDir(executionMode);
	}
	
	default public Path getMainBinDir(boolean executionMode) throws UnsupportedOperationException {
		return getDirectoryProvider().getMainBinDir(executionMode);
	}
	
	default public Path getTestBinDir(boolean executionMode) throws UnsupportedOperationException {
		return getDirectoryProvider().getTestBinDir(executionMode);
	}

	/**
	 * Deletes the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical or if forced to...
	 * @param force
	 * whether to force deletion, even if the execution directory is equal to the archive directory
	 */
	default public void tryDeleteExecutionDirectory(boolean force) {
		getDirectoryProvider().tryDeleteExecutionDirectory(force);
	}
	
	/**
	 * Deletes the buggy or fixed version archive directory.
	 */
	default public void deleteArchiveDirectory() {
		getDirectoryProvider().deleteArchiveDirectory();
	}
	
	/**
	 * Moves the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical...
	 */
	default public void tryMovingExecutionDirToArchive() {
		getDirectoryProvider().tryMovingExecutionDirToArchive();
	}
	
	
	public String getClassPath(boolean executionMode) throws UnsupportedOperationException;
	
	public String getTestClassPath(boolean executionMode) throws UnsupportedOperationException;
	
	
	public List<String> getTestCases(boolean executionMode) throws UnsupportedOperationException;
	
	public List<Path> getTestClasses(boolean executionMode) throws UnsupportedOperationException;
	
	public String getUniqueIdentifier();

}
