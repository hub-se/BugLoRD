package se.de.hu_berlin.informatik.defects4j.frontend;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Defects4JDirectoryProvider extends BenchmarkDirectoryProvider {
	
	private final boolean buggyVersion;
	private final int bugID;
	private final String project;
	
	private String projectDir;
	
	private Path mainSrcDir = null;
	private Path testSrcDir = null;
	private Path mainBinDir = null;
	private Path testBinDir = null;
	
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
	public Path getMainSourceDir() {
		if (mainSrcDir == null) {
			mainSrcDir = Paths.get(Defects4J.getD4JExport(getWorkDir().toString(), buggyVersion, "dir.src.classes"));
		}
		return mainSrcDir;
	}

	@Override
	public Path getTestSourceDir() {
		if (testSrcDir == null) {
			testSrcDir = Paths.get(Defects4J.getD4JExport(getWorkDir().toString(), buggyVersion, "dir.src.tests"));
		}
		return testSrcDir;
	}

	@Override
	public Path getMainBinDir() {
		if (mainBinDir == null) {
			mainBinDir = Paths.get(Defects4J.getD4JExport(getWorkDir().toString(), buggyVersion, "dir.bin.classes"));
		}
		return mainBinDir;
	}

	@Override
	public Path getTestBinDir() {
		if (testBinDir == null) {
			testBinDir = Paths.get(Defects4J.getD4JExport(getWorkDir().toString(), buggyVersion, "dir.bin.tests"));
		}
		return testBinDir;
	}
	

}
