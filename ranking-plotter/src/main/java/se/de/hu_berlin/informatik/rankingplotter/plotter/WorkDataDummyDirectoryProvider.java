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
	public void setDirsCorrectlyAfterSwitch() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path computeMainSourceDir() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path computeTestSourceDir() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path computeMainBinDir() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path computeTestBinDir() throws UnsupportedOperationException {
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

}
