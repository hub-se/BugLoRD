/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.StringListToFileWriter;
import se.de.hu_berlin.informatik.utils.files.processors.SearchFileOrDirToListProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.ModuleLinker;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;

/**
 * Builds a language model of order n from tokenized files.
 * 
 * @author SimHigh
 */
public class BuildLanguageModel {
	
	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		INPUT("i", "input", true, "The directory that contains the tokenized files.", true),
        OUTPUT("o", "output", true, "The output path + prefix for the generated files.", true),
        NGRAM_ORDER("n", "order", true, "The order of the n-gram model to build.", true),
        GEN_BINARY("b", "binary", false, "Whether to genrate a kenLM binary from the ARPA-format file.", false),
        KEEP_ARPA("k", "keepArpa", false, "Whether to keep the ARPA-format file in addition to the kenLM binary, if the binary is created.", false);

		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;

		//adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).
					hasArg(hasArg).desc(description).build(), NO_GROUP);
		}
		
		//adds an option that is part of the group with the specified index (positive integer)
		//a negative index means that this option is part of no group
		//this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).
					hasArg(hasArg).desc(description).build(), groupId);
		}
		
		//adds the given option that will be part of the group with the given id
		CmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}
		
		//adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override public String toString() { return option.getOption().getOpt(); }
		@Override public OptionWrapper getOptionWrapper() { return option; }
	}
	
	/**
	 * @param args
	 * -i input
	 */
	public static void main(String[] args) {
		
		OptionParser options = OptionParser.getOptions("BuildLanguageModel", false, CmdOptions.class, args);	
		
		Path inputDir = options.isDirectory(CmdOptions.INPUT, true);
		Path output = options.isFile(CmdOptions.OUTPUT, false);
		int order = 0;
		try {
			order = Integer.parseInt(options.getOptionValue(CmdOptions.NGRAM_ORDER));
		} catch(NumberFormatException e) {
			Log.abort(BuildLanguageModel.class, "Given order '%s' has to be an integer.", options.getOptionValue(CmdOptions.NGRAM_ORDER));
		}
		if (order < 1 || order > 10) {
			Log.abort(BuildLanguageModel.class, "Given order '%s' has to be in a range of 1 to 10.", options.getOptionValue(CmdOptions.NGRAM_ORDER));
		}
		
		Path temporaryFilesDir = inputDir.resolve("_tempLMDir_");
		FileUtils.delete(temporaryFilesDir);
		
		//generate a file that contains a list of all token files (needed by SRILM)
		Path listFile = inputDir.resolve("file.list");
		new ModuleLinker().append(
				new SearchFileOrDirToListProcessor("**/*.{tkn}", true).searchForFiles(),
				new StringListToFileWriter<List<Path>>(listFile, true))
		.submit(inputDir);
		
		//make batch counts with SRILM
		String countsDir = temporaryFilesDir + Defects4J.SEP + "counts";
		Paths.get(countsDir).toFile().mkdirs();
		Defects4J.executeCommand(temporaryFilesDir.toFile(), BugLoRD.getSRILMMakeBatchCountsExecutable(), 
				listFile.toString(), "10", "/bin/cat", countsDir, "-order", String.valueOf(order), "-unk");
		
		//merge batch counts with SRILM
		Defects4J.executeCommand(temporaryFilesDir.toFile(), BugLoRD.getSRILMMergeBatchCountsExecutable(), countsDir);
		
		//estimate language model of order n with SRILM
		String arpalLM = output + ".arpa";
		Defects4J.executeCommand(temporaryFilesDir.toFile(), BugLoRD.getSRILMMakeBigLMExecutable(), "-read", 
				countsDir + Defects4J.SEP + "*.gz", "-lm", arpalLM, "-order", String.valueOf(order), "-unk");
		
		if (options.hasOption(CmdOptions.GEN_BINARY)) {
			//build binary with kenLM
			String binaryLM = output + ".binary";
			Defects4J.executeCommand(temporaryFilesDir.toFile(), BugLoRD.getKenLMBinaryExecutable(),
					arpalLM, binaryLM);
			if (!options.hasOption(CmdOptions.KEEP_ARPA)) {
				FileUtils.delete(new File(arpalLM));
			}
		}
		
		//delete the temporary LM files
		FileUtils.delete(temporaryFilesDir);
	}
	
}
