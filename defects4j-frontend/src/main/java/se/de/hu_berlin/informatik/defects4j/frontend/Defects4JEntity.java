package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class Defects4JEntity implements DefectEntity {
	
	private final static String SEP = File.separator;
	
	private static final Prop prop = new Prop();
	
	private final boolean buggyVersion;
	private final int bugID;
	private final String project;
	private String mainDir;
	private String projectDir;
	private String workDir;
	
	private boolean isInExecutionMode = false;
	
	private Path mainSrcDir = null;
	private Path testSrcDir = null;
	private Path mainBinDir = null;
	private Path testBinDir = null;
	private String classPath = null;
	private String testClassPath = null;
	private List<Path> testClasses = null;
	
	final private static Defects4JEntity dummy = new Defects4JEntity();
	
	public static Defects4JEntity getBuggyDefects4JEntity(String project, String bugId) {
		return new Defects4JEntity(project, bugId, true);
	}
	
	public static Defects4JEntity getFixedDefects4JEntity(String project, String bugId) {
		return new Defects4JEntity(project, bugId, false);
	}
	
	public static Defects4JEntity getProjectEntity(String project) {
		return new Defects4JEntity(project);
	}
	
	public static Defects4JEntity getDummyEntity() {
		return dummy;
	}
	
	/**
	 * @param project
	 * a project identifier, serving as a directory name
	 * @param bugID
	 * id of the bug
	 * @param buggy
	 * whether to use the buggy or the fixed version of the bug with the given id
	 */
	private Defects4JEntity(String project, String bugID, boolean buggy) {
		try {
			this.bugID = Integer.parseInt(bugID);
		} catch(NumberFormatException e) {
			throw e;
		}
		this.project = project;
		
		validateProjectAndBugID(project, this.bugID, true);
		
		this.buggyVersion = buggy;
		
		switchToArchiveDir();
	}
	
	/**
	 * @param project
	 * a project identifier, serving as a directory name
	 */
	private Defects4JEntity(String project) {
		this.project = project;
		this.bugID = 0;
		
		validateProject(project, false);
		
		this.buggyVersion = true;
		
		switchToArchiveDir();
	}
	
	private Defects4JEntity() {
		this.project = "dummy";
		this.bugID = 0;
		this.buggyVersion = true;
		
		switchToArchiveDir();
	}
	
	public static boolean validateProject(String project, boolean abortOnError) {
		for (final String element : Prop.getAllProjects()) {
			if (element.equals(project)) {
				return true;
			}
		}
		
		if (abortOnError) {
			Log.abort(Prop.class, "Chosen project has to be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.");
		}
		return false;
	}
	
	public static boolean validateProjectAndBugID(String project, int parsedID, boolean abortOnError) {
		if (parsedID < 1) {
			if (abortOnError)
				Log.abort(Prop.class, "Bug ID is negative.");
			else
				return false;
		}

		switch (project) {
		case "Lang":
			if (parsedID > 65)
				if (abortOnError)
					Log.abort(Prop.class, "Bug ID may only range from 1 to 65 for project Lang.");
				else
					return false;
			break;
		case "Math":
			if (parsedID > 106)
				if (abortOnError)
					Log.abort(Prop.class, "Bug ID may only range from 1 to 106 for project Math.");
				else
					return false;
			break;
		case "Chart":
			if (parsedID > 26)
				if (abortOnError)
					Log.abort(Prop.class, "Bug ID may only range from 1 to 26 for project Chart.");
				else
					return false;
			break;
		case "Time":
			if (parsedID > 27)
				if (abortOnError)
					Log.abort(Prop.class, "Bug ID may only range from 1 to 27 for project Time.");
				else
					return false;
			break;
		case "Closure":
			if (parsedID > 133)
				if (abortOnError)
					Log.abort(Prop.class, "Bug ID may only range from 1 to 133 for project Closure.");
				else
					return false;
			break;
		default:
			if (abortOnError)
				Log.abort(Prop.class, "Chosen project has to be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.");
			else
				return false;
			break;
		}
		return true;
	}
	
	public static Prop getProperties() {
		return prop;
	}
	
	public void switchToExecutionDir() {
		mainDir = prop.executionDir;
		setProjectAndBugDirAfterSwitch();
		isInExecutionMode = true;
	}
	
	public void switchToArchiveDir() {
		mainDir = prop.archiveDir;
		setProjectAndBugDirAfterSwitch();
		isInExecutionMode = false;
	}
	
	public boolean isInExecutionMode() {
		return isInExecutionMode;
	}
	
	private void setProjectAndBugDirAfterSwitch() {
		projectDir = mainDir + SEP + project;
		if (buggyVersion) {
			workDir = projectDir + SEP + bugID + "b";
		} else {
			workDir = projectDir + SEP + bugID + "f";
		}
	}
	
	public int getBugId() {
		return bugID;
	}
	
	public String getProject() {
		return project;
	}
	
	
	private boolean checkoutBug(boolean deleteExisting) {
		if (deleteExisting) {
			deleteAll();
		} else if (getWorkDir().toFile().exists()) {
			return false;
		}
		new File(projectDir).mkdirs();
		prop.executeCommand(new File(projectDir), 
				prop.defects4jExecutable, "checkout", 
				"-p", getProject(), "-v", getBugId() + "b", "-w", workDir);
		return true;
	}

	
	public String getInfo() {
		return prop.executeCommandWithOutput(new File(projectDir), false, 
				prop.defects4jExecutable, "info", "-p", getProject(), "-b", String.valueOf(getBugId()));
	}
	
	private String getD4JExport(boolean buggyVersion, String option) {
		return prop.executeCommandWithOutput(new File(workDir), false, 
				prop.defects4jExecutable, "export", "-p", option);
	}


	
	
	@Override
	public boolean resetAndInitialize(boolean deleteExisting) {
		return checkoutBug(deleteExisting);
	}

	@Override
	public boolean deleteAll() {
		tryDeleteExecutionDirectory(true);
		deleteArchiveDirectory();
		return true;
	}
	
	public Path getMainDir() {
		return Paths.get(mainDir);
	}
	
	public Path getProjectDir() {
		return Paths.get(projectDir);
	}
	
	@Override
	public Path getWorkDir() {
		return Paths.get(workDir);
	}

	@Override
	public Path getMainSourceDir() {
		if (mainSrcDir == null) {
			mainSrcDir = Paths.get(getD4JExport(buggyVersion, "dir.src.classes"));
		}
		return mainSrcDir;
	}

	@Override
	public Path getTestSourceDir() {
		if (testSrcDir == null) {
			testSrcDir = Paths.get(getD4JExport(buggyVersion, "dir.src.tests"));
		}
		return testSrcDir;
	}

	@Override
	public Path getMainBinDir() {
		if (mainBinDir == null) {
			mainBinDir = Paths.get(getD4JExport(buggyVersion, "dir.bin.classes"));
		}
		return mainBinDir;
	}

	@Override
	public Path getTestBinDir() {
		if (testBinDir == null) {
			testBinDir = Paths.get(getD4JExport(buggyVersion, "dir.bin.tests"));
		}
		return testBinDir;
	}

	@Override
	public String getClassPath() {
		if (classPath == null) {
			classPath = getD4JExport(buggyVersion, "cp.classes");
		}
		return classPath;
	}

	@Override
	public String getTestClassPath() {
		if (testClassPath == null) {
			testClassPath = getD4JExport(buggyVersion, "cp.test");
		}
		return testClassPath;
	}

	@Override
	public List<String> getTestCases() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Path> getTestClasses() throws UnsupportedOperationException {
		if (testClasses == null) {
			String list;
			if (prop.relevant) {
				list = getD4JExport(buggyVersion, "tests.relevant");
			} else {
				list = getD4JExport(buggyVersion, "tests.all");
			}
			String[] array = list.split(System.lineSeparator());
			testClasses = new ArrayList<>(array.length);
			for (String item : array) {
				testClasses.add(Paths.get(item));
			}
		}
		return testClasses;
	}

	@Override
	public void removeUnnecessaryFiles() {
		/* #====================================================================================
		 * # clean up unnecessary directories (binary classes)
		 * #==================================================================================== */
		FileUtils.delete(Paths.get(workDir + Prop.SEP + getMainBinDir()));
		FileUtils.delete(Paths.get(workDir + Prop.SEP + getTestBinDir()));
		/* #====================================================================================
		 * # clean up unnecessary directories (doc files, svn/git files)
		 * #==================================================================================== */
		FileUtils.delete(Paths.get(workDir + Prop.SEP + "doc"));
		FileUtils.delete(Paths.get(workDir + Prop.SEP + ".git"));
		FileUtils.delete(Paths.get(workDir + Prop.SEP + ".svn"));
	}

	@Override
	public boolean compile() {
		if (!(new File(workDir + SEP + ".defects4j.config")).exists()) {
			Log.abort(Defects4JEntity.class, "Defects4J config file doesn't exist: '%s'.", workDir + SEP + ".defects4j.config");
		}
		prop.executeCommand(new File(workDir), prop.defects4jExecutable, "compile");
		return true;
	}

	
	
	
	
	/**
	 * Deletes the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical or if forced to...
	 * @param force
	 * whether to force deletion, even if the execution directory is equal to the archive directory
	 */
	public void tryDeleteExecutionDirectory(boolean force) {
		boolean temp = isInExecutionMode();
		switchToArchiveDir();
		File archiveProjectDir = new File(projectDir);
		switchToExecutionDir();
		File executionProjectDir = new File(projectDir);
		
		if (force || !archiveProjectDir.equals(executionProjectDir)) {
			FileUtils.delete(new File(workDir));
		}
		
		if (!temp) {
			switchToArchiveDir();
		}
	}
	
	/**
	 * Deletes the buggy or fixed version archive directory.
	 */
	public void deleteArchiveDirectory() {
		boolean temp = isInExecutionMode();
		switchToArchiveDir();

		FileUtils.delete(new File(workDir));
		
		if (temp) {
			switchToExecutionDir();
		}
	}
	
	/**
	 * Moves the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical...
	 */
	public void tryMovingExecutionDirToArchive() {
		boolean temp = isInExecutionMode();
		switchToArchiveDir();
		File archiveProjectDir = new File(projectDir);
		File archiveWorkDir = new File(workDir);
		switchToExecutionDir();
		File executionProjectDir = new File(projectDir);
		File executionWorkDir = new File(workDir);
		if (!archiveProjectDir.equals(executionProjectDir)) {
			FileUtils.delete(archiveWorkDir);
			try {
				FileUtils.copyFileOrDir(executionWorkDir, archiveWorkDir);
			} catch (IOException e) {
				Log.abort(this, "IOException while trying to copy directory '%s' to '%s'.",
						executionWorkDir, archiveWorkDir);
			}
			FileUtils.delete(executionWorkDir);
		}
		
		if (!temp) {
			switchToArchiveDir();
		}
	}

}
