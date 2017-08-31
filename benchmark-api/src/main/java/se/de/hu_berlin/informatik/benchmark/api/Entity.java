package se.de.hu_berlin.informatik.benchmark.api;

import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.utils.files.FileUtils;

/**
 * This interface describes an entity of some given benchmark. Usually, this
 * should be a Java project with all needed source files and the means to
 * use it properly. This includes
 * <pre>
 * - obtaining relevant directories related to the specific entity
 * - obtaining respective class paths
 * - proper initialization (including checkout, preparations, etc.)
 * - compilation of source files to byte code class files
 * - deletion of relevant directories after usage
 * - obtaining unique identifiers for the given entity
 * </pre>
 * 
 * @author Simon Heiden
 */
public interface Entity {
	
	/**
	 * Resets the entity to a fresh checked-out/initialized state.
	 * This may include a repository checkout and the previous
	 * deletion of already existing files and directories as well
	 * as other necessary preparations.
	 * @param executionMode
	 * whether execution directories should be used
	 * @param deleteExisting
	 * whether to delete already existing directories
	 * @return
	 * true if successful, false otherwise
	 */
	public boolean resetAndInitialize(boolean executionMode, boolean deleteExisting);
	
	
	/**
	 * @return
	 * whether the given entity has already been initialized
	 */
	public boolean isInitialized();

	/**
	 * Compiles the source files in the given entity.
	 * @param executionMode
	 * whether the source files are located in the execution directory
	 * @return
	 * true if successful; false otherwise
	 */
	public boolean compile(boolean executionMode);
	
	/**
	 * Deletes all connected directories apart from the data directory.
	 * @return
	 * true if successful; false otherwise
	 */
	public boolean deleteAllButData();
	
	/**
	 * Deletes the data directory.
	 * @return
	 * true if successful; false otherwise
	 */
	public boolean deleteData();
	
	/**
	 * Deletes all connected directories and also the data directory
	 * of this entity. Uses the other methods in this interface internally 
	 * for the actual deletion.
	 * @return
	 * true if successful; false otherwise.
	 */
	default public boolean deleteAll() {
		deleteAllButData();
		deleteData();
		FileUtils.delete(getEntityDir(true));
		FileUtils.delete(getEntityDir(false));
		return true;
	}
	
	/**
	 * @return
	 * the directory provider of this entity
	 */
	public DirectoryProvider getDirectoryProvider();
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be returned
	 * @return
	 * the main path in which all entity directories should be located
	 */
	default public Path getBenchmarkDir(boolean executionMode) {
		return getDirectoryProvider().getBenchmarkDir(executionMode);
	}
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be returned
	 * @return
	 * the path to the entity directory
	 */
	default public Path getEntityDir(boolean executionMode) {
		return getDirectoryProvider().getEntityDir(executionMode);
	}
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be returned
	 * @return
	 * the actual working directory which should be the main path to the project, usually
	 */
	default public Path getWorkDir(boolean executionMode) {
		return getDirectoryProvider().getWorkDir(executionMode);
	}
	
	/**
	 * @return
	 * the data directory of this entity
	 */
	default public Path getWorkDataDir() {
		return getDirectoryProvider().getWorkDataDir();
	}
	
	/**
	 * Removes unnecessary files that are not relevant to future analysis.
	 * @param executionMode
	 * whether the execution directory version should be used
	 * @throws UnsupportedOperationException
	 * if this operation is not implemented
	 */
	public void removeUnnecessaryFiles(boolean executionMode) throws UnsupportedOperationException;

	
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be returned
	 * @return
	 * the path to the main source directory
	 * @throws UnsupportedOperationException
	 * if this operation is not implemented
	 */
	default public Path getMainSourceDir(boolean executionMode) throws UnsupportedOperationException {
		return getDirectoryProvider().getMainSourceDir(executionMode);
	}
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be returned
	 * @return
	 * the path to the main test source directory
	 * @throws UnsupportedOperationException
	 * if this operation is not implemented
	 */
	default public Path getTestSourceDir(boolean executionMode) throws UnsupportedOperationException {
		return getDirectoryProvider().getTestSourceDir(executionMode);
	}
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be returned
	 * @return
	 * the path to the main binaries directory
	 * @throws UnsupportedOperationException
	 * if this operation is not implemented
	 */
	default public Path getMainBinDir(boolean executionMode) throws UnsupportedOperationException {
		return getDirectoryProvider().getMainBinDir(executionMode);
	}
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be returned
	 * @return
	 * the path to the main test binaries directory
	 * @throws UnsupportedOperationException
	 * if this operation is not implemented
	 */
	default public Path getTestBinDir(boolean executionMode) throws UnsupportedOperationException {
		return getDirectoryProvider().getTestBinDir(executionMode);
	}

	/**
	 * Deletes the execution directory if archive and execution directory 
	 * aren't identical or if forced to.
	 * @param force
	 * whether to force deletion, even if the execution directory is equal to the archive directory
	 */
	default public void tryDeleteExecutionDirectory(boolean force) {
		getDirectoryProvider().tryDeleteExecutionDirectory(force);
	}
	
	/**
	 * Deletes the archive directory.
	 */
	default public void deleteArchiveDirectory() {
		getDirectoryProvider().deleteArchiveDirectory();
	}
	
	/**
	 * Moves the execution directory to the archive directory if archive and execution directory 
	 * aren't identical.
	 */
	default public void tryMovingExecutionDirToArchive() {
		getDirectoryProvider().tryMovingExecutionDirToArchive();
	}
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be used
	 * @return
	 * the class path that is needed to execute the project
	 * @throws UnsupportedOperationException
	 * if this operation is not implemented
	 */
	public String getClassPath(boolean executionMode) throws UnsupportedOperationException;
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be used
	 * @return
	 * the class path that is needed to execute the project's tests
	 * @throws UnsupportedOperationException
	 * if this operation is not implemented
	 */
	public String getTestClassPath(boolean executionMode) throws UnsupportedOperationException;
	
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be used
	 * @return
	 * the list of test case identifiers that may be executed
	 * @throws UnsupportedOperationException
	 * if this operation is not implemented
	 */
	public List<String> getTestCases(boolean executionMode) throws UnsupportedOperationException;
	
	/**
	 * @param executionMode
	 * whether the execution directory version should be used
	 * @return
	 * the list of test classes that are given in the project
	 * @throws UnsupportedOperationException
	 * if this operation is not implemented
	 */
	public List<Path> getTestClasses(boolean executionMode) throws UnsupportedOperationException;

	/**
	 * Should return a list of Strings which contains all failing (triggering) tests (one per line).
	 * <p> line format: {@code qualified.class.name::testIdentifier}
	 * <p> example: {@code org.apache.commons.lang3.math.NumberUtilsTest::TestLang747}
	 * @param executionMode
	 * whether the execution directory should be used to make the necessary system call
	 * @return
	 * the list of failing tests
	 */
	abstract public List<String> getFailingTests(boolean executionMode) throws UnsupportedOperationException;
	
	/**
	 * @return
	 * an identifier for this entity that is unique
	 */
	public String getUniqueIdentifier();

}
