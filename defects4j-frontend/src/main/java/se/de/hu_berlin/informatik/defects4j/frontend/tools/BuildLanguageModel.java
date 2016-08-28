/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend.tools;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.fileoperations.SearchForFilesOrDirsModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;

/**
 * Builds a language model of order n from tokenized files.
 * 
 * @author SimHigh
 */
public class BuildLanguageModel {
	
	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "BuildLanguageModel -i input"; 
		final String tool_usage = "BuildLanguageModel";
		final OptionParser options = new OptionParser(tool_usage, args);

        options.add("i", "input", true, "The directory that contains the tokenized files.", true);
        options.add("o", "output", true, "The output path + prefix for the generated files.", true);
        options.add("n", "order", true, "The order of the n-gram model to build.", true);
        options.add("b", "binary", false, "Whether to genrate a kenLM binary from the ARPA-format file.");
        options.add("k", "keepArpa", false, "Whether to keep the ARPA-format file in addition to the kenLM binary, if the binary is created.");

        options.parseCommandLine();
        
        return options;
	}
	
	
	/**
	 * @param args
	 * -i input
	 */
	public static void main(String[] args) {
		
		OptionParser options = getOptions(args);	
		
		Path inputDir = options.isDirectory('i', true);
		Path output = options.isFile('o', false);
		int order = 0;
		try {
			order = Integer.parseInt(options.getOptionValue('n'));
		} catch(NumberFormatException e) {
			Log.abort(BuildLanguageModel.class, "Given order '%s' has to be an integer.", options.getOptionValue('n'));
		}
		if (order < 1 || order > 10) {
			Log.abort(BuildLanguageModel.class, "Given order '%s' has to be in a range of 1 to 10.", options.getOptionValue('n'));
		}
		
		Path temporaryFilesDir = inputDir.resolve("_tempLMDir_");
		Misc.delete(temporaryFilesDir);
		
		Prop prop = new Prop();
		
		//generate a file that contains a list of all token files (needed by SRILM)
		Path listFile = inputDir.resolve("file.list");
		new ModuleLinker().link(
				new SearchForFilesOrDirsModule(false, true, "**/*.{tkn}", false, true),
				new ListToFileWriterModule<List<Path>>(listFile, true))
		.submit(inputDir);
		
		//make batch counts with SRILM
		String countsDir = temporaryFilesDir + Prop.SEP + "counts";
		Paths.get(countsDir).toFile().mkdirs();
		prop.executeCommand(temporaryFilesDir.toFile(), prop.sriLMmakeBatchCountsExecutable, 
				listFile.toString(), "10", "/bin/cat", countsDir, "-order", String.valueOf(order), "-unk");
		
		//merge batch counts with SRILM
		prop.executeCommand(temporaryFilesDir.toFile(), prop.sriLMmergeBatchCountsExecutable, countsDir);
		
		//estimate language model of order n with SRILM
		String arpalLM = output + ".arpa";
		prop.executeCommand(temporaryFilesDir.toFile(), prop.sriLMmakeBigLMExecutable, "-read", 
				countsDir + Prop.SEP + "*.gz", "-lm", arpalLM, "-order", String.valueOf(order), "-unk");
		
		if (options.hasOption('b')) {
			//build binary with kenLM
			String binaryLM = output + ".binary";
			prop.executeCommand(temporaryFilesDir.toFile(), prop.kenLMbuildBinaryExecutable,
					arpalLM, binaryLM);
			if (!options.hasOption('k')) {
				Misc.delete(new File(arpalLM));
			}
		}
		
		//delete the temporary LM files
		Misc.delete(temporaryFilesDir);
	}
	
}
