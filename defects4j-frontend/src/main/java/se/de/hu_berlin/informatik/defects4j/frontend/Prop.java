package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.tm.modules.ExecuteCommandInSystemEnvironmentAndReturnOutputModule;
import se.de.hu_berlin.informatik.utils.tm.modules.ExecuteCommandInSystemEnvironmentModule;

public class Prop {

	public final static String SEP = File.separator;
	public final static String PATH_MARK = "#";
	
	public final static String FILENAME_INFO = ".info";
	public final static String FILENAME_INFO_MOD_SOURCES = ".info.mod";
	public final static String FILENAME_SRCDIR = ".srcDir";
	public final static String FILENAME_TEST_CLASSES = "test_classes.txt";
	public final static String FILENAME_SENTENCE_OUT = ".sentences";
	public final static String FILENAME_LM_RANKING = ".global";
	public final static String FILENAME_MOD_LINES = ".modifiedLines";
	public final static String EXTENSION_MOD_LINES = ".modlines";
	
	public final static String PROP_FILE_NAME = "defects4jProperties.ini";

	public final static String PROP_D4J_DIR = "defects4j_dir";
	public final static String PROP_EXECUTION_DIR = "execution_dir";
	public final static String PROP_ARCHIVE_DIR = "archive_dir";
	public final static String PROP_PLOT_DIR = "plot_dir";
	public final static String PROP_SPECTRA_ARCHIVE_DIR = "spectraArchive_dir";
	public final static String PROP_ONLY_RELEVANT_TESTS = "only_relevant_tests";
	public final static String PROP_KENLM_DIR = "kenlm_dir";
	public final static String PROP_SRILM_DIR = "srilm_dir";
	public final static String PROP_GLOBAL_LM = "global_lm_binary";
	public final static String PROP_JAVA7_DIR = "java7_dir";
	public final static String PROP_JAVA7_HOME = "java7_home";
	public final static String PROP_JAVA7_JRE = "java7_jre";
	public final static String PROP_PROGRESS_FILE = "progress_file";
	public final static String PROP_PERCENTAGES = "ranking_percentages";
	public final static String PROP_LOCALIZERS = "localizers";
	
	public final static String OPT_PROJECT = "p";
	public final static String OPT_BUG_ID = "b";
	public final static String OPT_LOCALIZERS = "l";
	
	public final static String OPT_LM = "lm";

	public String plotMainDir;
	public String spectraArchiveDir;
	
	public boolean relevant;
	
	public String defects4jExecutable;
	public String sriLMmakeBatchCountsExecutable;
	public String sriLMmergeBatchCountsExecutable;
	public String sriLMmakeBigLMExecutable;
	public String kenLMbuildBinaryExecutable;
	public String kenLMqueryExecutable;
	
	public String progressFile;
	public String percentages;
	public String localizers;
	
	public String globalLM;
	
	public String java7BinDir;
	public String java7home;
	public String java7jre;
	
	private int bugID = 0;
	private String project = null;
	
	private Properties props = null;

	public String mainDir;
	public String projectDir;
	public String buggyWorkDir;
	public String fixedWorkDir;
	
	private boolean isInExecutionMode = false;
	
	public Prop(String project, String bugID, boolean checkValidity) {
		loadProperties(project, bugID, checkValidity);
		switchToExecutionMode();
		new File(projectDir).mkdirs();
		switchToArchiveMode();
		new File(projectDir).mkdirs();
	}
	
	public Prop() {
		loadProperties();
	}
	
	/**
	 * Loads properties from the property file.
	 * @param project
	 * a project identifier, serving as a directory name
	 * @param bugID
	 * id of the bug
	 * @param checkValidity
	 * check the validity of the combination of project and bug ID
	 * @return
	 * a Properties object containing all loaded properties
	 */
	public Prop loadProperties(String project, String bugID, boolean checkValidity) {
//		File homeDir = new File(System.getProperty("user.home"));
		try {
			this.bugID = Integer.parseInt(bugID);
		} catch(NumberFormatException e) {
			this.bugID = 0;
		}
		this.project = project;
		
		if (checkValidity) {
			validateProjectAndBugID(project, this.bugID, true);
		}
		
		File propertyFile = new File(Prop.PROP_FILE_NAME);

		props = new Properties();

		if (propertyFile.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(propertyFile);
				props.load(fis);
			} catch (FileNotFoundException e) {
				Log.abort(this, "No property file found: '" + propertyFile + "'.");
			} catch (IOException e) {
				Log.abort(this, "IOException while reading property file: '" + propertyFile + "'.");
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
				} catch (IOException e) {
					// nothing to do
				}
			}
		}
		
		switchToArchiveMode();
		
		plotMainDir = props.getProperty(Prop.PROP_PLOT_DIR, "." + SEP + "plots");
		spectraArchiveDir = props.getProperty(Prop.PROP_SPECTRA_ARCHIVE_DIR, "." + SEP + "spectraArchive");
		
		defects4jExecutable = props.getProperty(Prop.PROP_D4J_DIR, ".") + SEP + "defects4j";
		sriLMmakeBatchCountsExecutable = props.getProperty(Prop.PROP_SRILM_DIR, ".") + SEP + "make-batch-counts";
		sriLMmergeBatchCountsExecutable = props.getProperty(Prop.PROP_SRILM_DIR, ".") + SEP + "merge-batch-counts";
		sriLMmakeBigLMExecutable = props.getProperty(Prop.PROP_SRILM_DIR, ".") + SEP + "make-big-lm";
		kenLMbuildBinaryExecutable = props.getProperty(Prop.PROP_KENLM_DIR, ".") + SEP + "build_binary";
		kenLMqueryExecutable = props.getProperty(Prop.PROP_KENLM_DIR, ".") + SEP + "query";
		
		globalLM = props.getProperty(Prop.PROP_GLOBAL_LM, ".");
		
		progressFile = props.getProperty(Prop.PROP_PROGRESS_FILE, ".");
		percentages = props.getProperty(Prop.PROP_PERCENTAGES, "0 5 10 15 20 25 30 35 40 45 50 55 60 65 70 75 80 85 90 95");
		localizers = props.getProperty(Prop.PROP_LOCALIZERS, "Op2 GP13 Tarantula Ochiai Jaccard");
		
		java7BinDir = props.getProperty(Prop.PROP_JAVA7_DIR, ".");
		java7home = props.getProperty(Prop.PROP_JAVA7_HOME, ".");
		java7jre = props.getProperty(Prop.PROP_JAVA7_JRE, ".");
		
		relevant = props.getProperty(Prop.PROP_ONLY_RELEVANT_TESTS, "true").equals("true") ? true : false;
		
		return this;
	}
	
	public void switchToExecutionMode() {
		mainDir = props.getProperty(Prop.PROP_EXECUTION_DIR, ".");
		setProjectAndBugDirAfterSwitch();
		isInExecutionMode = true;
	}
	
	public void switchToArchiveMode() {
		mainDir = props.getProperty(Prop.PROP_ARCHIVE_DIR, "." + SEP + "archive");
		setProjectAndBugDirAfterSwitch();
		isInExecutionMode = false;
	}
	
	public boolean isInExecutionMode() {
		return isInExecutionMode;
	}
	
	private void setProjectAndBugDirAfterSwitch() {
		projectDir = mainDir + SEP + project;
		buggyWorkDir = projectDir + SEP + bugID + "b";
		fixedWorkDir = projectDir + SEP + bugID + "f";
	}
	
	public int getBugID() {
		return bugID;
	}
	
	public String getProject() {
		return project;
	}
	
	/**
	 * Loads the basic porperties without generating special paths for specific bugs.
	 * @return
	 * a Properties object containing all loaded properties
	 */
	public Prop loadProperties() {
		return loadProperties("", "", false);
	}
	
//	public static void storeProperties(Properties props) {
//		// write the updated properties file to the file system
//		FileOutputStream fos = null;
//		File propertyFile = new File(PROP_FILE_NAME);
//		
//		try {
//			fos = new FileOutputStream(propertyFile);
//			props.store(fos, "property file for Defects4J benchmark experiments");
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (fos != null) {
//				try {
//					fos.close();
//				} catch (IOException e) {
//					// nothing to do
//				}
//			}
//		}
//	}
	
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
	
	public static String[] getAllBugIDs(String project) {
		int maxID = 0;
		switch (project) {
		case "Lang":
			maxID = 65;			
			break;
		case "Math":
			maxID = 106;
			break;
		case "Chart":
			maxID = 26;
			break;
		case "Time":
			maxID = 27;
			break;
		case "Closure":
			maxID = 133;
			break;
		default:
			maxID = 0;
			break;	
		}
		String[] result = new String[maxID];
		for (int i = 0; i < maxID; ++i) {
			result[i] = String.valueOf(i + 1);
		}
		return result;
	}
	
	public static String[] getAllProjects() {
		String[] result = 
			{ "Lang", "Chart", "Time", "Math", "Closure" };
		return result;
	}

	/**
	 * Executes a given command in the system's environment, while additionally using a given Java 1.7 environment,
	 * which is required for defects4J to function correctly and to compile the projects. Will abort the
	 * program in case of an error in the executed process.
	 * @param executionDir
	 * an execution directory in which the command shall be executed
	 * @param commandArgs
	 * the command to execute, given as an array
	 */
	public void executeCommand(File executionDir, String... commandArgs) {
		int executionResult = new ExecuteCommandInSystemEnvironmentModule(executionDir, java7BinDir)
				.setEnvVariable("JAVA_HOME", java7home)
				.setEnvVariable("JRE_HOME", java7jre)
				.submit(commandArgs).getResult();
		
		if (executionResult != 0) {
			Log.abort(Defects4J.class, "Error while executing command: " + Misc.arrayToString(commandArgs, " ", "", ""));
		}
	}
	
	/**
	 * Executes a given command in the system's environment, while additionally using a given Java 1.7 environment,
	 * which is required for defects4J to function correctly and to compile the projects. Returns either the process'
	 * output to standard out or to error out.
	 * @param executionDir
	 * an execution directory in which the command shall be executed
	 * @param returnErrorOutput
	 * whether to output the error output channel instead of standeard out
	 * @param commandArgs
	 * the command to execute, given as an array
	 * @return
	 * the process' output to standard out or to error out
	 */
	public String executeCommandWithOutput(File executionDir, boolean returnErrorOutput, String... commandArgs) {
		return new ExecuteCommandInSystemEnvironmentAndReturnOutputModule(executionDir, returnErrorOutput, java7BinDir)
				.setEnvVariable("JAVA_HOME", java7home)
				.setEnvVariable("JRE_HOME", java7jre)
				.submit(commandArgs).getResult();
	}
	
	/**
	 * Deletes the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical or if forced to...
	 * @param buggyVersion
	 * whether to delete the buggy version's directory
	 * @param force
	 * whether to force deletion, even if the execution directory is equal to the archive directory
	 */
	public void tryDeleteExecutionDirectory(boolean buggyVersion, boolean force) {
		boolean temp = isInExecutionMode();
		switchToArchiveMode();
		File archiveProjectDir = new File(projectDir);
		switchToExecutionMode();
		File executionProjectDir = new File(projectDir);
		
		if (force || !archiveProjectDir.equals(executionProjectDir)) {
			if (buggyVersion) {
				Misc.delete(new File(buggyWorkDir));
			} else {
				Misc.delete(new File(fixedWorkDir));
			}
		}
		
		if (!temp) {
			switchToArchiveMode();
		}
	}
	
	/**
	 * Deletes the buggy or fixed version archive directory.
	 * @param buggyVersion
	 * whether to delete the buggy version's directory
	 */
	public void deleteArchiveDirectory(boolean buggyVersion) {
		boolean temp = isInExecutionMode();
		switchToArchiveMode();

		if (buggyVersion) {
			Misc.delete(new File(buggyWorkDir));
		} else {
			Misc.delete(new File(fixedWorkDir));
		}
		
		if (temp) {
			switchToExecutionMode();
		}
	}
	
	/**
	 * Moves the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical...
	 * @param buggyVersion
	 * whether to move the buggy version's directory
	 */
	public void tryMovingExecutionDirToArchive(boolean buggyVersion) {
		boolean temp = isInExecutionMode();
		switchToArchiveMode();
		File archiveProjectDir = new File(projectDir);
		File archiveBuggyVersionDir = new File(buggyWorkDir);
		File archiveFixedVersionDir = new File(fixedWorkDir);
		switchToExecutionMode();
		File executionProjectDir = new File(projectDir);
		File executionBuggyVersionDir = new File(buggyWorkDir);
		File executionFixedVersionDir = new File(fixedWorkDir);
		if (!archiveProjectDir.equals(executionProjectDir)) {
			if (buggyVersion) {
				Misc.delete(archiveBuggyVersionDir);
				try {
					Misc.copyFileOrDir(executionBuggyVersionDir, archiveBuggyVersionDir);
				} catch (IOException e) {
					Log.abort(this, "IOException while trying to copy directory '%s' to '%s'.",
							executionBuggyVersionDir, archiveBuggyVersionDir);
				}
				Misc.delete(executionBuggyVersionDir);
			} else {
				Misc.delete(archiveFixedVersionDir);
				try {
					Misc.copyFileOrDir(executionFixedVersionDir, archiveFixedVersionDir);
				} catch (IOException e) {
					Log.abort(this, "IOException while trying to copy directory '%s' to '%s'.",
							executionFixedVersionDir, archiveFixedVersionDir);
				}
				Misc.delete(executionFixedVersionDir);
			}
		}
		
		if (!temp) {
			switchToArchiveMode();
		}
	}
}
