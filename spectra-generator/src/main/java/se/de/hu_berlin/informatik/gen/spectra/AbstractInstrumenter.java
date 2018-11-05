package se.de.hu_berlin.informatik.gen.spectra;

import java.nio.file.Path;

public abstract class AbstractInstrumenter {
	
	protected Path projectDir;
	protected String instrumentedDir;
	protected String testClassPath;
	protected String[] pathsToBinaries;

	public AbstractInstrumenter(final Path projectDir, final String instrumentedDir,
			String testClassPath, String... pathsToBinaries) {
		this.projectDir = projectDir;
		this.instrumentedDir = instrumentedDir;
		this.testClassPath = testClassPath;
		this.pathsToBinaries = pathsToBinaries;
	}

	public Path getProjectDir() {
		return projectDir;
	}

	public void setProjectDir(Path projectDir) {
		this.projectDir = projectDir;
	}

	public String getInstrumentedDir() {
		return instrumentedDir;
	}

	public void setInstrumentedDir(String instrumentedDir) {
		this.instrumentedDir = instrumentedDir;
	}

	public String getTestClassPath() {
		return testClassPath;
	}

	public void setTestClassPath(String testClassPath) {
		this.testClassPath = testClassPath;
	}

	public String[] getPathsToBinaries() {
		return pathsToBinaries;
	}

	public void setPathsToBinaries(String[] pathsToBinaries) {
		this.pathsToBinaries = pathsToBinaries;
	}
	
	
	public abstract int instrumentClasses();

}
