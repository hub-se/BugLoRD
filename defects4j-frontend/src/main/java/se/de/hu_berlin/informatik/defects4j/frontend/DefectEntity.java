package se.de.hu_berlin.informatik.defects4j.frontend;

import java.nio.file.Path;
import java.util.List;

public interface DefectEntity {
	
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
	
	public Path getWorkDir();
	
	public void removeUnnecessaryFiles() throws UnsupportedOperationException;
	
	
	
	public Path getMainSourceDir() throws UnsupportedOperationException;
	
	public Path getTestSourceDir() throws UnsupportedOperationException;
	
	public Path getMainBinDir() throws UnsupportedOperationException;
	
	public Path getTestBinDir() throws UnsupportedOperationException;
	
	
	
	public String getClassPath() throws UnsupportedOperationException;
	
	public String getTestClassPath() throws UnsupportedOperationException;
	
	
	public List<String> getTestCases() throws UnsupportedOperationException;
	
	public List<Path> getTestClasses() throws UnsupportedOperationException;
	

}
