package se.de.hu_berlin.informatik.benchmark.api;

import java.nio.file.Path;
import java.util.List;

public abstract class AbstractEntity implements Entity {
		
	final private DirectoryProvider directoryProvider;
	
	private boolean isInitialized = false;
	
	private String classPath = null;
	private String testClassPath = null;
	private List<Path> testClasses = null;
	private List<String> testCases = null;

	final private static AbstractEntity dummy = new AbstractEntity(null) {
		@Override public void removeUnnecessaryFiles(boolean executionMode) throws UnsupportedOperationException {
			throw new UnsupportedOperationException(); }
		@Override public boolean compile(boolean executionMode) { return false; }
		@Override public List<Path> computeTestClasses(boolean executionMode) throws UnsupportedOperationException {
			throw new UnsupportedOperationException(); }
		@Override public String computeTestClassPath(boolean executionMode) throws UnsupportedOperationException {
			throw new UnsupportedOperationException(); }
		@Override public List<String> computeTestCases(boolean executionMode) throws UnsupportedOperationException {
			throw new UnsupportedOperationException(); }
		@Override public String computeClassPath(boolean executionMode) throws UnsupportedOperationException {
			throw new UnsupportedOperationException(); }
		@Override public String getUniqueIdentifier() { return "dummy-entity"; }
		@Override public boolean initialize(boolean executionMode) { return false; }
		@Override public List<String> getFailingTests(boolean executionMode) throws UnsupportedOperationException {
			throw new UnsupportedOperationException(); }
	};
	
	
	public static AbstractEntity getDummyEntity() {
		return dummy;
	}
	
	public AbstractEntity(DirectoryProvider directoryProvider) {
		this.directoryProvider = directoryProvider;
	}
	
	
	@Override
	public boolean resetAndInitialize(boolean executionMode, boolean deleteExisting) {
		if (deleteExisting) {
			deleteAllButData();
		}
		boolean result = initialize(executionMode);
		isInitialized = result;
		return result;
	}
	
	abstract public boolean initialize(boolean executionMode);

	@Override
	public boolean deleteAllButData() {
		getDirectoryProvider().deleteAllEntityDirectories();
		isInitialized = false;
		return true;
	}
	

	@Override
	public boolean deleteData() {
		getDirectoryProvider().deleteWorkDataDirectory();
		return true;
	}

	@Override
	public String getClassPath(boolean executionMode) throws UnsupportedOperationException {
		if (classPath == null) {
			classPath = computeClassPath(executionMode);
		}
		return classPath;
	}

	abstract public String computeClassPath(boolean executionMode) throws UnsupportedOperationException;

	@Override
	public String getTestClassPath(boolean executionMode) throws UnsupportedOperationException {
		if (testClassPath == null) {
			testClassPath = computeTestClassPath(executionMode);
		}
		return testClassPath;
	}
	
	abstract public String computeTestClassPath(boolean executionMode) throws UnsupportedOperationException;

	@Override
	public List<String> getTestCases(boolean executionMode) throws UnsupportedOperationException {
		if (testCases == null) {
			testCases = computeTestCases(executionMode);
		}
		return testCases;
	}

	abstract public List<String> computeTestCases(boolean executionMode) throws UnsupportedOperationException;

	@Override
	public List<Path> getTestClasses(boolean executionMode) throws UnsupportedOperationException{
		if (testClasses == null) {
			testClasses = computeTestClasses(executionMode);
		}
		return testClasses;
	}

	abstract public List<Path> computeTestClasses(boolean executionMode) throws UnsupportedOperationException;

	@Override
	public DirectoryProvider getDirectoryProvider() {
		return directoryProvider;
	}

	@Override
	public boolean isInitialized() {
		return isInitialized;
	}

}
