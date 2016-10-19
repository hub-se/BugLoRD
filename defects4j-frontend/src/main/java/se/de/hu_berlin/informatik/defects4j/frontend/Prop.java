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
	
	public final static String PROP_FILE_NAME = "defects4jProperties.ini";
	
	private final static String[] projects = { "Lang", "Chart", "Time", "Math", "Closure" };

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

	public String executionDir;
	public String archiveDir;
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
	
	private Properties props = null;
	
	
	public Prop() {
		loadProperties();
	}
	
	/**
	 * Loads properties from the property file.
	 * @return
	 * a Properties object containing all loaded properties
	 */
	public Prop loadProperties() {
//		File homeDir = new File(System.getProperty("user.home"));
		
		
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
		} else {
			Log.abort(this, "No property file exists: '" + propertyFile + "'.");
		}
		
		executionDir = props.getProperty(Prop.PROP_EXECUTION_DIR, ".");
		archiveDir = props.getProperty(Prop.PROP_ARCHIVE_DIR, "." + SEP + "archive");
		
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
	
	public static Defects4JEntity[] getAllBugs(String project) {
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
		Defects4JEntity[] result = new Defects4JEntity[maxID];
		for (int i = 0; i < maxID; ++i) {
			result[i] = Defects4JEntity.getBuggyDefects4JEntity(project, String.valueOf(i + 1));
		}
		return result;
	}
	
	public static String[] getAllProjects() {
		return projects;
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
			Log.abort(Defects4JEntity.class, "Error while executing command: " + Misc.arrayToString(commandArgs, " ", "", ""));
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
	
}
