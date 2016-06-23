/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.c2r.Cob2Instr2Coverage2Ranking;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

/**
 * Checks out a Defects4J buggy project version. Will delete existing
 * versions if executed again.
 * 
 * @author SimHigh
 */
public class CheckoutAndGenerateSBFLRankings {
	
private final static String SEP = File.separator;
	
	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "Defects4JStarter -p project -b bugID [-l loc1 loc2 ...]"; 
		final String tool_usage = "Defects4JStarter";
		final OptionParser options = new OptionParser(tool_usage, args);

        options.add("p", "project", true, "A project of the Defects4J benchmark. "
        		+ "Should be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.", true);
        options.add("b", "bugID", true, "A number indicating the id of a buggy project version. "
        		+ "Value ranges differ based on the project.", true);
        
//        options.add("r", "onlyRelevant", false, "Set if only relevant tests shall be executed.");
		
        options.add(Option.builder("l").longOpt("localizers").optionalArg(true).hasArgs()
        		.desc("A list of identifiers of Cobertura localizers (e.g. 'Tarantula', 'Jaccard', ...).")
				.build());
        
        options.parseCommandLine();
        
        return options;
	}
	
	
	/**
	 * @param args
	 * -p project -b bugID [-l loc1 loc2 ...]
	 */
	public static void main(String[] args) {
		
		OptionParser options = getOptions(args);	
		
		String project = options.getOptionValue('p');
		String id = options.getOptionValue('b');
		int parsedID = Integer.parseInt(id);
		
		Prop.validateProjectAndBugID(project, parsedID);
		
		String buggyID = id + "b";
		
		//this is important!!
		Prop.loadProperties(project, buggyID);
		
		File executionProjectDir = Paths.get(Prop.projectDir).toFile();
		executionProjectDir.mkdirs();
		File executionBuggyVersionDir = Paths.get(Prop.workDir).toFile();
		
		//delete existing directory, if any
		Misc.delete(Paths.get(Prop.workDir));
		
		/* #====================================================================================
		 * # checkout buggy version
		 * #==================================================================================== */
		Prop.executeCommand(executionProjectDir, 
				Prop.defects4jExecutable, "checkout", "-p", project, "-v", buggyID, "-w", Prop.workDir);
		
		/* #====================================================================================
		 * # collect bug info
		 * #==================================================================================== */
		String infoFile = Prop.workDir + SEP + ".info";
		String processOutput = Prop.executeCommandWithOutput(executionBuggyVersionDir, false, 
				Prop.defects4jExecutable, "info", "-p", project, "-b", id);
		
		try {
			Misc.writeString2File(processOutput, Paths.get(infoFile).toFile());
		} catch (IOException e) {
			Misc.abort("IOException while trying to write to file '" + infoFile + "'.");
		}
		
		String mainSrcDir = Prop.executeCommandWithOutput(executionBuggyVersionDir, false, 
				Prop.defects4jExecutable, "export", "-p", "dir.src.classes");
		Misc.out("main source directory: <" + mainSrcDir + ">");
		String mainBinDir = Prop.executeCommandWithOutput(executionBuggyVersionDir, false, 
				Prop.defects4jExecutable, "export", "-p", "dir.bin.classes");
		Misc.out("main binary directory: <" + mainBinDir + ">");
		String testBinDir = Prop.executeCommandWithOutput(executionBuggyVersionDir, false,
				Prop.defects4jExecutable, "export", "-p", "dir.bin.tests");
		Misc.out("test binary directory: <" + testBinDir + ">");
		
		String testCP = Prop.executeCommandWithOutput(executionBuggyVersionDir, false, 
				Prop.defects4jExecutable, "export", "-p", "cp.test");
		Misc.out("test class path: <" + testCP + ">");
		
		/* #====================================================================================
		 * # compile buggy version
		 * #==================================================================================== */
		if (!Paths.get(Prop.workDir + SEP + ".defects4j.config").toFile().exists()) {
			Misc.abort("Defects4J config file doesn't exist.");
		}
		Prop.executeCommand(executionBuggyVersionDir, Prop.defects4jExecutable, "compile");
		
		/* #====================================================================================
		 * # generate coverage traces via cobertura and calculate rankings
		 * #==================================================================================== */
		String testClassesFile = Prop.workDir + SEP + "test_classes.txt";
		if (Prop.relevant) {
			Prop.executeCommand(executionBuggyVersionDir, 
					Prop.defects4jExecutable, "export", "-p", "tests.relevant", "-o", testClassesFile);
		} else {
			Prop.executeCommand(executionBuggyVersionDir, 
					Prop.defects4jExecutable, "export", "-p", "tests.all", "-o", testClassesFile);
		}
		
		String rankingDir = Prop.workDir + SEP + "ranking";
		String[] localizers = options.getOptionValues('l');
		Cob2Instr2Coverage2Ranking.generateRankingForDefects4JElement(
				Prop.workDir, mainSrcDir, testBinDir, testCP, 
				Prop.workDir + SEP + mainBinDir, testClassesFile, 
				rankingDir, localizers);
		
		/* #====================================================================================
		 * # clean up unnecessary directories (binary classes, doc files, svn/git files)
		 * #==================================================================================== */
		Misc.delete(Paths.get(Prop.workDir + SEP + mainBinDir));
		Misc.delete(Paths.get(Prop.workDir + SEP + testBinDir));
		Misc.delete(Paths.get(Prop.workDir + SEP + "doc"));
		Misc.delete(Paths.get(Prop.workDir + SEP + ".git"));
		Misc.delete(Paths.get(Prop.workDir + SEP + ".svn"));
		
		/* #====================================================================================
		 * # move to archive directory, in case it differs from the execution directory
		 * #==================================================================================== */
		File archiveDir = Paths.get(Prop.archiveDir).toFile();
		if (!archiveDir.equals(executionBuggyVersionDir)) {
			try {
				Misc.copyFileOrDir(executionBuggyVersionDir, archiveDir);
			} catch (IOException e) {
				Misc.abort((Object)null, "IOException while trying to copy directory '%s' to '%s'.",
						executionBuggyVersionDir, archiveDir);
			}
			Misc.delete(executionBuggyVersionDir);
		}
	}
	
}
