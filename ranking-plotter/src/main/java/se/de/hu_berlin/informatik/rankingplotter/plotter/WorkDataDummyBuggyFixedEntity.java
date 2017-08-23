package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.AbstractBuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.AbstractDirectoryProvider;
import se.de.hu_berlin.informatik.benchmark.api.AbstractEntity;

public class WorkDataDummyBuggyFixedEntity extends AbstractBuggyFixedEntity<WorkDataDummyBuggyFixedEntity.WorkDataDummyEntity> {

	public WorkDataDummyBuggyFixedEntity(Path workDataDir) {
		super(new WorkDataDummyEntity(workDataDir), null);
	}
	
	public static class WorkDataDummyEntity extends AbstractEntity {

		public WorkDataDummyEntity(Path workDataDir) {
			super(new WorkDataDummyDirectoryProvider(workDataDir));
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

		@Override
		public List<String> getFailingTests(boolean executionMode) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

	}
	
	public static class WorkDataDummyDirectoryProvider extends AbstractDirectoryProvider {

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

	@Override
	public String getUniqueIdentifier() {
		return "dummy";
	}

	@Override
	public List<String> getModifiedClasses(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
}
