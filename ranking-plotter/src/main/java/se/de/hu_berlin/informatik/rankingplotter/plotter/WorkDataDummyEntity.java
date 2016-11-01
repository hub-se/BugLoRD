package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.AbstractBuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;

public class WorkDataDummyEntity extends AbstractBuggyFixedEntity{

	public WorkDataDummyEntity(Path workDataDir) {
		super(new WorkDataDummyDirectoryProvider(workDataDir));
	}

	@Override
	public BuggyFixedEntity getBuggyVersion() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public BuggyFixedEntity getFixedVersion() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean compile(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeUnnecessaryFiles(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUniqueIdentifier() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean initialize(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String computeClassPath(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String computeTestClassPath(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> computeTestCases(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Path> computeTestClasses(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
