package se.de.hu_berlin.informatik.benchmark.api;

import java.io.File;
import java.nio.file.Path;

public interface DirectoryProvider {
	
	public final static String SEP = File.separator;
	
	public String getBenchmarkArchiveDir();
	
	public String getBenchmarkExecutionDir();
	
	public void switchToExecutionDir();
	
	public void switchToArchiveDir();
	
	public void setDirsCorrectlyAfterSwitch();
	
	public boolean isInExecutionMode();
	
	public boolean deleteAllEntityDirectories();
	
	/**
	 * Deletes the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical or if forced to...
	 * @param force
	 * whether to force deletion, even if the execution directory is equal to the archive directory
	 */
	public void tryDeleteExecutionDirectory(boolean force);
	
	/**
	 * Deletes the buggy or fixed version archive directory.
	 */
	public void deleteArchiveDirectory();
	
	/**
	 * Moves the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical...
	 */
	public void tryMovingExecutionDirToArchive();
	
	public Path getBenchmarkDir();
	
	public Path getWorkDir();
	
	default public Path getWorkDataDir() {
		Path result = getWorkDir().resolve(".BugLoRD_data");
		result.toFile().mkdirs();
		return result;
	}
	
	
	public Path getMainSourceDir() throws UnsupportedOperationException;
	
	public Path getTestSourceDir() throws UnsupportedOperationException;
	
	public Path getMainBinDir() throws UnsupportedOperationException;
	
	public Path getTestBinDir() throws UnsupportedOperationException;
	
	
}
