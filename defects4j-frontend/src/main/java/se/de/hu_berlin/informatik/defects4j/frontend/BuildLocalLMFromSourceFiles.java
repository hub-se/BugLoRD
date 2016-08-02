/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.fileoperations.SearchForFilesOrDirsModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;

/**
 * Builds a local language model of order 10 from the source files 
 * of a defects4J project (buggy) version.
 * 
 * @author SimHigh
 */
public class BuildLocalLMFromSourceFiles {
	
	private final static String SEP = File.separator;
	
	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "BuildLocalLMFromSourceFiles -p project -b bugID"; 
		final String tool_usage = "BuildLocalLMFromSourceFiles";
		final OptionParser options = new OptionParser(tool_usage, args);

        options.add(Prop.OPT_PROJECT, "project", true, "A project of the Defects4J benchmark. "
        		+ "Should be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.", true);
        options.add(Prop.OPT_BUG_ID, "bugID", true, "A number indicating the id of a buggy project version. "
        		+ "Value ranges differ based on the project.", true);
        
        options.parseCommandLine();
        
        return options;
	}
	
	
	/**
	 * @param args
	 * -p project -b bugID
	 */
	public static void main(String[] args) {
		
		OptionParser options = getOptions(args);	
		
		String project = options.getOptionValue(Prop.OPT_PROJECT);
		String id = options.getOptionValue(Prop.OPT_BUG_ID);
		int parsedID = Integer.parseInt(id);
		
		Prop.validateProjectAndBugID(project, parsedID, true);
		
		String buggyID = id + "b";
		String fixedID = id + "f";
		
		//this is important!!
		Prop prop = new Prop().loadProperties(project, buggyID, fixedID);
		
		File executionBuggyVersionDir = Paths.get(prop.executionBuggyWorkDir).toFile();
		executionBuggyVersionDir.mkdirs();
		File archiveBuggyWorkDir = Paths.get(prop.archiveBuggyWorkDir).toFile();
		
		if (!archiveBuggyWorkDir.exists()) {
			Misc.abort(BuildLocalLMFromSourceFiles.class, "Archive buggy project version directory doesn't exist: '" + prop.archiveBuggyWorkDir + "'.");
		}
			
		/* #====================================================================================
		 * # tokenize java source files and build local LM
		 * #==================================================================================== */
		String buggyMainSrcDir = prop.executeCommandWithOutput(archiveBuggyWorkDir, false, 
				prop.defects4jExecutable, "export", "-p", "dir.src.classes");
		Misc.out("main source directory: <" + buggyMainSrcDir + ">");
		
		File localLMDir = Paths.get(executionBuggyVersionDir.toString(), "_localLM").toFile();
		localLMDir.mkdirs();
		String tokenOutputDir = localLMDir + SEP + "tokens";
		Tokenize.tokenizeDefects4JElement(prop.archiveBuggyWorkDir + SEP + buggyMainSrcDir, tokenOutputDir);
		
		//generate a file that contains a list of all token files (needed by SRILM)
		new ModuleLinker().link(
				new SearchForFilesOrDirsModule("**/*.{tkn}", false, true, true),
				new ListToFileWriterModule<List<Path>>(Paths.get(tokenOutputDir, "list"), true))
		.submit(Paths.get(tokenOutputDir));
		
		//make batch counts with SRILM
		String countsDir = localLMDir + SEP + "counts";
		Paths.get(countsDir).toFile().mkdirs();
		prop.executeCommand(localLMDir, prop.sriLMmakeBatchCountsExecutable, 
				tokenOutputDir + SEP + "list", "10", "/bin/cat", countsDir, "-order", "10", "-unk");
		
		//merge batch counts with SRILM
		prop.executeCommand(localLMDir, prop.sriLMmergeBatchCountsExecutable, countsDir);
		
		//estimate language model of order 10 with SRILM
		String localLM = localLMDir + SEP + "temp.arpa";
		prop.executeCommand(localLMDir, prop.sriLMmakeBigLMExecutable, "-read", 
				countsDir + SEP + "*.gz", "-lm", localLM, "-order", "10", "-unk");
		
		//build binary with kenLM
		String localLMbinary = archiveBuggyWorkDir + SEP + "local.binary";
		prop.executeCommand(executionBuggyVersionDir, prop.kenLMbuildBinaryExecutable,
				localLM, localLMbinary);
		
		//delete the temporary local LM files (only binary is being kept)
		Misc.delete(localLMDir);
	}
	
}
