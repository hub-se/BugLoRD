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
		@Override public void removeUnnecessaryFiles() throws UnsupportedOperationException {
			throw new UnsupportedOperationException(); }
		@Override public boolean compile() { return false; }
		@Override public List<Path> computeTestClasses() throws UnsupportedOperationException {
			throw new UnsupportedOperationException(); }
		@Override public String computeTestClassPath() throws UnsupportedOperationException {
			throw new UnsupportedOperationException(); }
		@Override public List<String> computeTestCases() throws UnsupportedOperationException {
			throw new UnsupportedOperationException(); }
		@Override public String computeClassPath() throws UnsupportedOperationException {
			throw new UnsupportedOperationException(); }
		@Override public String getUniqueIdentifier() { return "dummy-entity"; }
		@Override public boolean initialize() { return false; }
	};
	
	
	public static AbstractEntity getDummyEntity() {
		return dummy;
	}
	
	public AbstractEntity(DirectoryProvider directoryProvider) {
		this.directoryProvider = directoryProvider;
	}
	
	
	@Override
	public boolean resetAndInitialize(boolean deleteExisting) {
		if (deleteExisting) {
			deleteAll();
		}
		boolean result = initialize();
		isInitialized = result;
		return result;
	}
	
	abstract public boolean initialize();

	@Override
	public boolean deleteAll() {
		tryDeleteExecutionDirectory(true);
		deleteArchiveDirectory();
		isInitialized = false;
		return true;
	}
	

	@Override
	public String getClassPath() throws UnsupportedOperationException {
		if (classPath == null) {
			classPath = computeClassPath();
		}
		return classPath;
	}

	abstract public String computeClassPath() throws UnsupportedOperationException;

	@Override
	public String getTestClassPath() throws UnsupportedOperationException {
		if (testClassPath == null) {
			testClassPath = computeTestClassPath();
		}
		return testClassPath;
	}
	
	abstract public String computeTestClassPath() throws UnsupportedOperationException;

	@Override
	public List<String> getTestCases() throws UnsupportedOperationException {
		if (testCases == null) {
			testCases = computeTestCases();
		}
		return testCases;
	}

	abstract public List<String> computeTestCases() throws UnsupportedOperationException;

	@Override
	public List<Path> getTestClasses() throws UnsupportedOperationException{
		if (testClasses == null) {
			testClasses = computeTestClasses();
		}
		return testClasses;
	}

	abstract public List<Path> computeTestClasses() throws UnsupportedOperationException;


	@Override
	public DirectoryProvider getDirectoryProvider() {
		return directoryProvider;
	}

	@Override
	public boolean isInitialized() {
		return isInitialized;
	}

}
