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
	public boolean compile() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeUnnecessaryFiles() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUniqueIdentifier() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean initialize() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String computeClassPath() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String computeTestClassPath() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> computeTestCases() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Path> computeTestClasses() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
