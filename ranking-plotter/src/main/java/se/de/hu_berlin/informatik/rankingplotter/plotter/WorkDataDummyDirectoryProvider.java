package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.benchmark.api.AbstractDirectoryProvider;

public class WorkDataDummyDirectoryProvider extends AbstractDirectoryProvider {

	private Path workDataDir;

	public WorkDataDummyDirectoryProvider(Path workDataDir) {
		super();
		this.workDataDir = workDataDir;
	}

	@Override
	public Path computeMainSourceDir(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path computeTestSourceDir(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path computeMainBinDir(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path computeTestBinDir(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getBenchmarkArchiveDir() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getBenchmarkExecutionDir() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path getWorkDataDir() {
		return workDataDir;
	}

	@Override
	public Path getRelativeEntityPath() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getEntityIdentifier() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
