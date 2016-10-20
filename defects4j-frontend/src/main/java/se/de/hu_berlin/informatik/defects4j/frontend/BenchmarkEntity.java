package se.de.hu_berlin.informatik.defects4j.frontend;

import java.nio.file.Path;
import java.util.List;

public interface BenchmarkEntity {
	
	/**
	 * Resets the entity to a fresh checked-out/initialized state.
	 * This may include a repository checkout and the previous
	 * deletion of already existing files and directories.
	 * @param deleteExisting
	 * whether to delete already existing directories
	 * @return
	 * true if successful, false otherwise
	 */
	public boolean resetAndInitialize(boolean deleteExisting);

	public boolean compile();
	
	public boolean deleteAll();
	
	public BenchmarkDirectoryProvider getDirectoryProvider();
	
	default public Path getBenchmarkDir() {
		return getDirectoryProvider().getBenchmarkDir();
	}
	
	default public Path getWorkDir() {
		return getDirectoryProvider().getWorkDir();
	}
	
	public void removeUnnecessaryFiles() throws UnsupportedOperationException;

	
	
	default public Path getMainSourceDir() throws UnsupportedOperationException {
		return getDirectoryProvider().getMainSourceDir();
	}
	
	default public Path getTestSourceDir() throws UnsupportedOperationException {
		return getDirectoryProvider().getTestSourceDir();
	}
	
	default public Path getMainBinDir() throws UnsupportedOperationException {
		return getDirectoryProvider().getMainBinDir();
	}
	
	default public Path getTestBinDir() throws UnsupportedOperationException {
		return getDirectoryProvider().getTestBinDir();
	}
	
	default public void switchToExecutionDir() {
		getDirectoryProvider().switchToExecutionDir();
	}
	
	default public void switchToArchiveDir() {
		getDirectoryProvider().switchToArchiveDir();
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
	
	
	public String getClassPath() throws UnsupportedOperationException;
	
	public String getTestClassPath() throws UnsupportedOperationException;
	
	
	public List<String> getTestCases() throws UnsupportedOperationException;
	
	public List<Path> getTestClasses() throws UnsupportedOperationException;
	

}
