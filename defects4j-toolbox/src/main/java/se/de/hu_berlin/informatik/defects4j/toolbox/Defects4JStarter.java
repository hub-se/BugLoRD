/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.toolbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.Tarantula;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;
import se.de.hu_berlin.informatik.utils.tm.modules.ExecuteCommandInSystemEnvironmentAndReturnOutputModule;
import se.de.hu_berlin.informatik.utils.tm.modules.ExecuteCommandInSystemEnvironmentModule;

/**
 * 
 * 
 * @author SimHigh
 */
public class Defects4JStarter {

	private final static String PROP_FILE_NAME = "defects4jProperties.ini";
	
	private final static String PROP_D4J_DIR = "defects4j_dir";
	private final static String PROP_EXECUTION_DIR = "execution_dir";
	private final static String PROP_ARCHIVE_DIR = "archive_dir";
	private final static String PROP_LOG_DIR = "log_dir";
	private final static String PROP_ONLY_RELEVANT_TESTS = "only_relevant_tests";
	private final static String PROP_KENLM_DIR = "kenlm_dir";
	private final static String PROP_TMP_DIR = "tmp_dir";
	private final static String PROP_JAVA7_DIR = "java7_dir";
	private final static String PROP_JAVA7_HOME = "java7_home";
	private final static String PROP_JAVA7_JRE = "java7_jre";
	private final static String PROP_LOCALIZERS = "localizers";
	
	private final static String SEP = File.separator;
	
	private static String java7BinDir = null;
	private static String java7home = null;
	private static String java7jre = null;
	
	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "Defects4JStarter -p project -b bugID"; 
		final String tool_usage = "Defects4JStarter";
		final OptionParser options = new OptionParser(tool_usage, args);

        options.add("p", "project", true, "A project of the Defects4J benchmark. Should be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.", true);
        options.add("b", "bugID", true, "A number indicating the id of a buggy project version. Value ranges differ based on the project.", true);
		
		
        options.parseCommandLine();
        
        return options;
	}
	
	
	/**
	 * @param args
	 * asdadsadasdasd
	 */
	public static void main(String[] args) {
		
		OptionParser options = getOptions(args);
		
		Properties props = loadProperties();
		
//		props.setProperty(PROP_D4J_DIR, "bla 2");
//		
//		storeProperties(props);		
		
		//get via options TODO
		String project = options.getOptionValue('p');
		String id = options.getOptionValue('b');
		int parsedID = Integer.parseInt(id);
		if (parsedID < 1) {
			Misc.abort("Bug ID is negative.");
		}
		
		switch (project) {
		case "Lang":
			if (parsedID > 65)
				Misc.abort("Bug ID may only range from 1 to 65 for project Lang.");
			break;
		case "Math":
			if (parsedID > 106)
				Misc.abort("Bug ID may only range from 1 to 106 for project Math.");
			break;
		case "Chart":
			if (parsedID > 26)
				Misc.abort("Bug ID may only range from 1 to 26 for project Chart.");
			break;
		case "Time":
			if (parsedID > 27)
				Misc.abort("Bug ID may only range from 1 to 27 for project Time.");
			break;
		case "Closure":
			if (parsedID > 133)
				Misc.abort("Bug ID may only range from 1 to 133 for project Closure.");
			break;
		default:
			Misc.abort("Chosen project has to be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.");
			break;	
		}
		
		String buggyID = id + "b";
		String fixedID = id + "f";
		
		String executionDir = props.getProperty(PROP_EXECUTION_DIR, ".");
		String archiveDir = props.getProperty(PROP_ARCHIVE_DIR, "." + SEP + "archive");
		String projectDir = executionDir + SEP + project;
		String workDir = projectDir + SEP + buggyID;
		
		
		String defects4jExecutable = props.getProperty(PROP_D4J_DIR, ".") + "/defects4j";
		java7BinDir = props.getProperty(PROP_JAVA7_DIR, ".");
		java7home = props.getProperty(PROP_JAVA7_HOME, ".");
		java7jre = props.getProperty(PROP_JAVA7_JRE, ".");
		
		File executionProjectDir = Paths.get(projectDir).toFile();
		executionProjectDir.mkdirs();
		File executionBuggyVersionDIR = Paths.get(workDir).toFile();
		
		Misc.delete(Paths.get(workDir));
		
		/* #====================================================================================
		 * # checkout buggy version
		 * #==================================================================================== */
		executeCommand(executionProjectDir, defects4jExecutable, "checkout", "-p", project, "-v", buggyID, "-w", workDir);
		
		/* #====================================================================================
		 * # collect bug info
		 * #==================================================================================== */
		String infoFile = workDir + SEP + ".info";
		String processOutput = executeCommandWithOutput(executionBuggyVersionDIR, false, defects4jExecutable, "info", "-p", project, "-b", id);
		
		try {
			Misc.writeString2File(processOutput, Paths.get(infoFile).toFile());
		} catch (IOException e) {
			Misc.abort("IOException while trying to write to file '" + infoFile + "'.");
		}
		
		String mainSrcDir = executeCommandWithOutput(executionBuggyVersionDIR, false, defects4jExecutable, "export", "-p", "dir.src.classes");
		Misc.out("main source directory: <" + mainSrcDir + ">");
		String mainBinDir = executeCommandWithOutput(executionBuggyVersionDIR, false, defects4jExecutable, "export", "-p", "dir.bin.classes");
		Misc.out("main binary directory: <" + mainBinDir + ">");
		String testBinDir = executeCommandWithOutput(executionBuggyVersionDIR, false,defects4jExecutable, "export", "-p", "dir.bin.tests");
		Misc.out("test binary directory: <" + testBinDir + ">");
		
		String testCP = executeCommandWithOutput(executionBuggyVersionDIR, false, defects4jExecutable, "export", "-p", "cp.test");
		Misc.out("test class path: <" + testCP + ">");
		
		/* #====================================================================================
		 * # compile buggy version
		 * #==================================================================================== */
		if (!Paths.get(workDir + SEP + ".defects4j.config").toFile().exists()) {
			Misc.abort("Defects4J config file doesn't exist.");
		}
		executeCommand(executionBuggyVersionDIR, defects4jExecutable, "compile");
		
		/* #====================================================================================
		 * # generate coverage traces via cobertura and calculate rankings
		 * #==================================================================================== */
		boolean relevant = props.getProperty(PROP_ONLY_RELEVANT_TESTS, "true").equals("true") ? true : false;
		String testClassesFile = workDir + SEP + "test_classes.txt";
		if (relevant) {
			executeCommand(executionBuggyVersionDIR, defects4jExecutable, "export", "-p", "tests.relevant", "-o", testClassesFile);
		} else {
			executeCommand(executionBuggyVersionDIR, defects4jExecutable, "export", "-p", "tests.all", "-o", testClassesFile);
		}
		
		String rankingDir = workDir + SEP + "ranking";
		//TODO: set via option and/or property file
		String[] localizers = props.getProperty(PROP_LOCALIZERS, "Tarantula").split(" ");
		Cob2Instr2Coverage2Ranking.generateRankingForDefects4JElement(
				workDir, mainSrcDir, testBinDir, testCP, workDir + SEP + mainBinDir, testClassesFile, rankingDir, localizers);
		
		/* #====================================================================================
		 * # clean up unnecessary directories
		 * #==================================================================================== */
		Misc.delete(Paths.get(workDir + SEP + mainBinDir));
		Misc.delete(Paths.get(workDir + SEP + testBinDir));
		Misc.delete(Paths.get(workDir + SEP + "doc"));
		Misc.delete(Paths.get(workDir + SEP + ".git"));
		Misc.delete(Paths.get(workDir + SEP + ".svn"));
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
	private static void executeCommand(File executionDir, String... commandArgs) {
		int executionResult = new ExecuteCommandInSystemEnvironmentModule(executionDir, java7BinDir)
				.setEnvVariable("JAVA_HOME", java7home)
				.setEnvVariable("JRE_HOME", java7jre)
				.submit(commandArgs).getResult();
		
		if (executionResult != 0) {
			Misc.abort("Error while executing command: " + Misc.arrayToString(commandArgs, " ", "", ""));
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
	 */
	private static String executeCommandWithOutput(File executionDir, boolean returnErrorOutput, String... commandArgs) {
		return new ExecuteCommandInSystemEnvironmentAndReturnOutputModule(executionDir, returnErrorOutput, java7BinDir)
				.setEnvVariable("JAVA_HOME", java7home)
				.setEnvVariable("JRE_HOME", java7jre)
				.submit(commandArgs).getResult();
	}
	
	/**
	 * Loads properties from the property file.
	 * @return
	 * a Properties object containing all loaded properties
	 */
	private static Properties loadProperties() {
//		File homeDir = new File(System.getProperty("user.home"));
		File propertyFile = new File(PROP_FILE_NAME);

		Properties props = new Properties();

		if (propertyFile.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(propertyFile);
				props.load(fis);
			} catch (FileNotFoundException e) {
				Misc.abort("No property file found: '" + propertyFile + "'.");
			} catch (IOException e) {
				Misc.abort("IOException while reading property file: '" + propertyFile + "'.");
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
		return props;
	}
	
	
//	private static void storeProperties(Properties props) {
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
}
