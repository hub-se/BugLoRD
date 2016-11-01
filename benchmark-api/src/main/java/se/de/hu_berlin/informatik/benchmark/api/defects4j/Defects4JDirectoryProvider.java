package se.de.hu_berlin.informatik.benchmark.api.defects4j;

import java.nio.file.Path;
import java.nio.file.Paths;

import se.de.hu_berlin.informatik.benchmark.api.AbstractDirectoryProvider;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;

public class Defects4JDirectoryProvider extends AbstractDirectoryProvider {
	
	private final boolean buggyVersion;
	private final int bugID;
	private final String project;
	
	private String projectDir;
	
	/**
	 * @param project
	 * a project identifier, serving as a directory name
	 * @param bugID
	 * id of the bug
	 * @param buggy
	 * whether to use the buggy or the fixed version of the bug with the given id
	 */
	public Defects4JDirectoryProvider(String project, String bugID, boolean buggy) {
		try {
			this.bugID = Integer.parseInt(bugID);
		} catch(NumberFormatException e) {
			throw e;
		}
		this.project = project;
		
		this.buggyVersion = buggy;
		
		switchToArchiveDir();
	}
	
	/**
	 * @param project
	 * a project identifier, serving as a directory name
	 * @param bugID
	 * id of the bug
	 * @param buggy
	 * whether to use the buggy or the fixed version of the bug with the given id
	 */
	public Defects4JDirectoryProvider(String project, int bugID, boolean buggy) {
		this.bugID = bugID;
		this.project = project;
		
		this.buggyVersion = buggy;
		
		switchToArchiveDir();
	}
	
	@Override
	public void setDirsCorrectlyAfterSwitch() {
		projectDir = getBenchmarkDir() + SEP + project;
		if (buggyVersion) {
			setWorkDir(projectDir + SEP + bugID + "b");
		} else {
			setWorkDir(projectDir + SEP + bugID + "f");
		}
	}
	
	public Path getProjectDir() {
		return Paths.get(projectDir);
	}


	@Override
	public Path computeMainSourceDir() {
		return Paths.get(Defects4J.getD4JExport(getWorkDir().toString(), buggyVersion, "dir.src.classes"));
	}

	@Override
	public Path computeTestSourceDir() {
		return Paths.get(Defects4J.getD4JExport(getWorkDir().toString(), buggyVersion, "dir.src.tests"));
	}

	@Override
	public Path computeMainBinDir() {
		return Paths.get(Defects4J.getD4JExport(getWorkDir().toString(), buggyVersion, "dir.bin.classes"));
	}

	@Override
	public Path computeTestBinDir() {
		return Paths.get(Defects4J.getD4JExport(getWorkDir().toString(), buggyVersion, "dir.bin.tests"));
	}

	@Override
	public String getBenchmarkArchiveDir() {
		return Defects4J.getValueOf(Defects4JProperties.ARCHIVE_DIR);
	}

	@Override
	public String getBenchmarkExecutionDir() {
		return Defects4J.getValueOf(Defects4JProperties.EXECUTION_DIR);
	}
	

}
