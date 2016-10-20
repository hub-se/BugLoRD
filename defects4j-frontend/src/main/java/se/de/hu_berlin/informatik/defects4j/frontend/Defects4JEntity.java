package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.defects4j.frontend.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class Defects4JEntity implements BuggyFixedBenchmarkEntity {
		
	private final boolean buggyVersion;
	private final int bugID;
	private final String project;

	private boolean isInExecutionMode = false;
	
	private String classPath = null;
	private String testClassPath = null;
	private List<Path> testClasses = null;

	final private Defects4JDirectoryProvider directoryProvider;
	
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
	
	@Override
	public BuggyFixedBenchmarkEntity getBuggyVersion() {
		if (buggyVersion) {
			return this;
		} else {
			return getBuggyDefects4JEntity(project, String.valueOf(bugID));
		}
	}

	@Override
	public BuggyFixedBenchmarkEntity getFixedVersion() {
		if (buggyVersion) {
			return getFixedDefects4JEntity(project, String.valueOf(bugID));
		} else {
			return this;
		}
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
		
		directoryProvider = new Defects4JDirectoryProvider(this.project, this.bugID, this.buggyVersion);
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
		
		directoryProvider = new Defects4JDirectoryProvider(this.project, this.bugID, this.buggyVersion);
	}
	
	private Defects4JEntity() {
		this.project = "dummy";
		this.bugID = 0;
		this.buggyVersion = true;
		
		directoryProvider = new Defects4JDirectoryProvider(this.project, this.bugID, this.buggyVersion);
	}
	
	public static boolean validateProject(String project, boolean abortOnError) {
		for (final String element : Defects4J.getAllProjects()) {
			if (element.equals(project)) {
				return true;
			}
		}
		
		if (abortOnError) {
			Log.abort(Defects4J.class, "Chosen project has to be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.");
		}
		return false;
	}
	
	public static boolean validateProjectAndBugID(String project, int parsedID, boolean abortOnError) {
		if (parsedID < 1) {
			if (abortOnError)
				Log.abort(Defects4J.class, "Bug ID is negative.");
			else
				return false;
		}

		switch (project) {
		case "Lang":
			if (parsedID > 65)
				if (abortOnError)
					Log.abort(Defects4J.class, "Bug ID may only range from 1 to 65 for project Lang.");
				else
					return false;
			break;
		case "Math":
			if (parsedID > 106)
				if (abortOnError)
					Log.abort(Defects4J.class, "Bug ID may only range from 1 to 106 for project Math.");
				else
					return false;
			break;
		case "Chart":
			if (parsedID > 26)
				if (abortOnError)
					Log.abort(Defects4J.class, "Bug ID may only range from 1 to 26 for project Chart.");
				else
					return false;
			break;
		case "Time":
			if (parsedID > 27)
				if (abortOnError)
					Log.abort(Defects4J.class, "Bug ID may only range from 1 to 27 for project Time.");
				else
					return false;
			break;
		case "Closure":
			if (parsedID > 133)
				if (abortOnError)
					Log.abort(Defects4J.class, "Bug ID may only range from 1 to 133 for project Closure.");
				else
					return false;
			break;
		default:
			if (abortOnError)
				Log.abort(Defects4J.class, "Chosen project has to be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.");
			else
				return false;
			break;
		}
		return true;
	}
	
//	public static Defects4JProp getProperties() {
//		return prop;
//	}
	
	public Path getProjectDir() {
		return directoryProvider.getProjectDir();
	}
	
	public boolean isInExecutionMode() {
		return isInExecutionMode;
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
		getProjectDir().toFile().mkdirs();
		BugLoRD.executeCommand(getProjectDir().toFile(), 
				Defects4J.getDefects4JExecutable(), "checkout", 
				"-p", getProject(), "-v", getBugId() + "b", "-w", getWorkDir().toString());
		return true;
	}

	
	public String getInfo() {
		return BugLoRD.executeCommandWithOutput(getProjectDir().toFile(), false, 
				Defects4J.getDefects4JExecutable(), "info", "-p", getProject(), "-b", String.valueOf(getBugId()));
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
	

	@Override
	public String getClassPath() {
		if (classPath == null) {
			classPath = Defects4J.getD4JExport(getWorkDir().toString(), buggyVersion, "cp.classes");
		}
		return classPath;
	}

	@Override
	public String getTestClassPath() {
		if (testClassPath == null) {
			testClassPath = Defects4J.getD4JExport(getWorkDir().toString(), buggyVersion, "cp.test");
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
			if (Boolean.parseBoolean(Defects4J.getValueOf(Defects4JProperties.ONLY_RELEVANT_TESTS))) {
				list = Defects4J.getD4JExport(getWorkDir().toString(), buggyVersion, "tests.relevant");
			} else {
				list = Defects4J.getD4JExport(getWorkDir().toString(), buggyVersion, "tests.all");
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
		FileUtils.delete(getWorkDir().resolve(getMainBinDir()));
		FileUtils.delete(getWorkDir().resolve(getTestBinDir()));
		/* #====================================================================================
		 * # clean up unnecessary directories (doc files, svn/git files)
		 * #==================================================================================== */
		FileUtils.delete(getWorkDir().resolve("doc"));
		FileUtils.delete(getWorkDir().resolve(".git"));
		FileUtils.delete(getWorkDir().resolve(".svn"));
	}

	@Override
	public boolean compile() {
		if (!getWorkDir().resolve(".defects4j.config").toFile().exists()) {
			Log.abort(Defects4JEntity.class, "Defects4J config file doesn't exist: '%s'.", getWorkDir().resolve(".defects4j.config"));
		}
		BugLoRD.executeCommand(getWorkDir().toFile(), Defects4J.getDefects4JExecutable(), "compile");
		return true;
	}


	@Override
	public BenchmarkDirectoryProvider getDirectoryProvider() {
		return directoryProvider;
	}

	@Override
	public String toString() {
		return "Project: " + project + ", bugID: " + bugID;
	}

	@Override
	public String getUniqueIdentifier() {
		if (buggyVersion) {
			return project + "-" + bugID + "b";
		} else {
			return project + "-" + bugID + "f";
		}
	}

	@Override
	public List<String> getModifiedSources() {
		/* #====================================================================================
		 * # collect bug info
		 * #==================================================================================== */
		String infoOutput = getInfo();
		
		return parseInfoString(infoOutput);
	}
	
	/**
	 * Parses a Defects4J info string and returns a String which contains all modified
	 * source files with one file per line.
	 * @param info
	 * the info string
	 * @return
	 * modified source files, separated by new lines
	 */
	private List<String> parseInfoString(String info) {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new StringReader(info))) {
			String line = null;
			boolean modifiedSourceLine = false;
			while ((line = reader.readLine()) != null) {
				if (line.equals("List of modified sources:")) {
					modifiedSourceLine = true;
					continue;
				}
				if (modifiedSourceLine && line.startsWith(" - ")) {
					lines.add(line.substring(3));
				} else {
					modifiedSourceLine = false;
				}
			}
		} catch (IOException e) {
			Log.abort(this, "IOException while reading info string.");
		}
		
		return lines;
	}

}
