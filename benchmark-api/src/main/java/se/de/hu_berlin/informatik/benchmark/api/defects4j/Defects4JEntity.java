package se.de.hu_berlin.informatik.benchmark.api.defects4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.de.hu_berlin.informatik.benchmark.api.AbstractEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J.Defects4JProperties;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class Defects4JEntity extends AbstractEntity {
		
	private final boolean buggyVersion;
	private final int bugID;
	private final String project;

	private static Map<String, Defects4JEntity> ENTITY_CACHE = new ConcurrentHashMap<String, Defects4JEntity>();
	
	public static Defects4JEntity getBuggyDefects4JEntity(String project, String bugId) {
		return ENTITY_CACHE.computeIfAbsent(project + bugId +"b", k -> new Defects4JEntity(project, bugId, true));
	}
	
	public static Defects4JEntity getFixedDefects4JEntity(String project, String bugId) {
		return ENTITY_CACHE.computeIfAbsent(project + bugId +"f", k -> new Defects4JEntity(project, bugId, false));
	}
	
	public static Defects4JEntity getProjectEntity(String project) {
		return new Defects4JEntity(project);
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
		super(new Defects4JDirectoryProvider(project, bugID, buggy));
		try {
			this.bugID = Integer.parseInt(bugID);
		} catch(NumberFormatException e) {
			throw e;
		}
		this.project = project;
		
		Defects4J.validateProjectAndBugID(project, this.bugID, true);
		
		this.buggyVersion = buggy;
	}
	
	/**
	 * @param project
	 * a project identifier, serving as a directory name
	 */
	private Defects4JEntity(String project) {
		super(new Defects4JDirectoryProvider(project, 0, true));
		this.project = project;
		this.bugID = 0;
		
		Defects4J.validateProject(project, false);
		
		this.buggyVersion = true;
	}
	
	private Defects4JEntity() {
		super(new Defects4JDirectoryProvider("dummy", 0, true));
		this.project = "dummy";
		this.bugID = 0;
		this.buggyVersion = true;
	}
	
	public Path getProjectDir(boolean executionMode) {
		return ((Defects4JDirectoryProvider)getDirectoryProvider()).getProjectDir(executionMode);
	}
	
	
	public int getBugId() {
		return bugID;
	}
	
	public String getProject() {
		return project;
	}
	
	
	private boolean checkoutBug(boolean executionMode) {
		if (getWorkDir(executionMode).toFile().exists()) {
			return false;
		}
		getEntityDir(executionMode).toFile().mkdirs();

		String version = null;
		if (buggyVersion) {
			version = getBugId() + "b";
		} else {
			version = getBugId() + "f";
		}
		
		Defects4J.executeCommand(getEntityDir(executionMode).toFile(), true,
				Defects4J.getDefects4JExecutable(), "checkout", 
				"-p", getProject(), "-v", version, "-w", getWorkDir(executionMode).toString());
		return true;
	}

	
	public String getInfo(boolean executionMode) {
		return Defects4J.executeCommandWithOutput(getEntityDir(executionMode).toFile(), false, 
				Defects4J.getDefects4JExecutable(), "info", "-p", getProject(), "-b", String.valueOf(getBugId()));
	}


	@Override
	public void removeUnnecessaryFiles(boolean executionMode) {
		/* #====================================================================================
		 * # clean up unnecessary directories (binary classes)
		 * #==================================================================================== */
		Path mainBinDir = getWorkDir(executionMode).resolve(getMainBinDir(executionMode));
		if (!isWorkDir(mainBinDir, executionMode)) {
//			Log.out(Defects4JEntity.class, "deleting '%s'...", mainBinDir);
			FileUtils.delete(mainBinDir);
		}
		Path testBinDir = getWorkDir(executionMode).resolve(getTestBinDir(executionMode));
		if (!isWorkDir(testBinDir, executionMode)) {
//			Log.out(Defects4JEntity.class, "deleting '%s'...", testBinDir);
			FileUtils.delete(testBinDir);
		}
		/* #====================================================================================
		 * # clean up unnecessary directories (doc files, svn/git files)
		 * #==================================================================================== */
//		Log.out(Defects4JEntity.class, "deleting '%s'...", getWorkDir(executionMode).resolve("doc"));
		FileUtils.delete(getWorkDir(executionMode).resolve("doc"));
//		Log.out(Defects4JEntity.class, "deleting '%s'...", getWorkDir(executionMode).resolve(".git"));
		FileUtils.delete(getWorkDir(executionMode).resolve(".git"));
//		Log.out(Defects4JEntity.class, "deleting '%s'...", getWorkDir(executionMode).resolve(".svn"));
		FileUtils.delete(getWorkDir(executionMode).resolve(".svn"));
	}
	
	private boolean isWorkDir(Path dir, boolean executionMode) {
		return dir.equals(getWorkDir(executionMode));
	}

	@Override
	public boolean compile(boolean executionMode) {
		if (!getWorkDir(executionMode).resolve(".defects4j.config").toFile().exists()) {
			Log.abort(Defects4JEntity.class, "Defects4J config file doesn't exist: '%s'.", 
					getWorkDir(executionMode).resolve(".defects4j.config"));
		}
		Defects4J.executeCommand(getWorkDir(executionMode).toFile(), true,
				Defects4J.getDefects4JExecutable(), "compile");
		return true;
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
	public List<String> getFailingTests(boolean executionMode) throws UnsupportedOperationException {
		if (buggyVersion) {
			return getTriggeringTestsFromInfo(executionMode);
		} else {
			return Collections.emptyList();
		}
	}
	
	/**
	 * Returns a String which contains all failing (triggering) tests (one per line).
	 * <p> line format: {@code qualified.class.name::testIdentifier}
	 * <p> example: {@code org.apache.commons.lang3.math.NumberUtilsTest::TestLang747}
	 * @param executionMode
	 * whether the execution directory should be used to make the necessary system call
	 */
	private List<String> getTriggeringTestsFromInfo(boolean executionMode) {
		/* #====================================================================================
		 * # collect bug info
		 * #==================================================================================== */
		String infoOutput = getInfo(executionMode);
		
		return parseInfoStringForTriggeringTests(infoOutput);
	}
	
	/**
	 * Parses a Defects4J info string and returns a String which contains all 
	 * failing (triggering) tests (one per line).
	 * <p> line format: {@code qualified.class.name::testIdentifier}
	 * <p> example: {@code org.apache.commons.lang3.math.NumberUtilsTest::TestLang747}
	 * @param info
	 * the info string
	 * @return
	 * failing/triggering tests, separated by new lines
	 */
	private List<String> parseInfoStringForTriggeringTests(String info) {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new StringReader(info))) {
			String line = null;
			boolean triggeringTestLine = false;
			while ((line = reader.readLine()) != null) {
				if (line.equals("Root cause in triggering tests:")) {
					triggeringTestLine = true;
					continue;
				}
				if (triggeringTestLine && line.startsWith(" - ")) {
					lines.add(line.substring(3));
				} else if (triggeringTestLine && line.startsWith("--------------------------------------------------------------------------------")) {
					triggeringTestLine = false;
				}
			}
		} catch (IOException e) {
			Log.abort(this, "IOException while reading info string.");
		}
		
		return lines;
	}

	@Override
	public boolean initialize(boolean executionMode) {
		return checkoutBug(executionMode);
	}

	@Override
	public String computeClassPath(boolean executionMode) {
		return Defects4J.getD4JExport(getWorkDir(executionMode).toString(), "cp.classes");
	}

	@Override
	public String computeTestClassPath(boolean executionMode) {
		return Defects4J.getD4JExport(getWorkDir(executionMode).toString(), "cp.test");
	}

	@Override
	public List<String> computeTestCases(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Path> computeTestClasses(boolean executionMode) {
		String list;
		if (Boolean.parseBoolean(Defects4J.getValueOf(Defects4JProperties.ONLY_RELEVANT_TESTS))) {
			list = Defects4J.getD4JExport(getWorkDir(executionMode).toString(), "tests.relevant");
		} else {
			list = Defects4J.getD4JExport(getWorkDir(executionMode).toString(), "tests.all");
		}
		String[] array = list.split(System.lineSeparator());
		List<Path> testClasses = new ArrayList<>(array.length);
		for (String item : array) {
			testClasses.add(Paths.get(item));
		}
		return testClasses;
	}

}
