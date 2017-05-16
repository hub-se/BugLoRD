/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.lm;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.utils.files.processors.StringListToFileWriter;
import se.de.hu_berlin.informatik.utils.files.processors.SearchFileOrDirToListProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.basics.ThreadedListProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.ModuleLinker;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;
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
		NGRAM_ORDER(Option.builder("n").longOpt("orders").required().hasArgs()
				.desc("A list of numbers indicating the orders of the n-gram models to build (1 <= n <= 10).").build()),
		GEN_BINARY("b", "binary", false, "Whether to generate a kenLM binary from the ARPA-format file.", false),
		KEEP_ARPA("k", "keepArpa", false,
				"Whether to keep the ARPA-format file in addition to the kenLM binary, if the binary is created.",
				false);

		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;

		// adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description,
				final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).hasArg(hasArg).desc(description).build(),
					NO_GROUP);
		}

		// adds an option that is part of the group with the specified index
		// (positive integer)
		// a negative index means that this option is part of no group
		// this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description,
				int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).hasArg(hasArg).desc(description).build(),
					groupId);
		}

		// adds the given option that will be part of the group with the given
		// id
		CmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}

		// adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override
		public String toString() {
			return option.getOption().getOpt();
		}

		@Override
		public OptionWrapper getOptionWrapper() {
			return option;
		}
	}

	/**
	 * @param args
	 * -i input
	 */
	public static void main(String[] args) {
		
		OptionParser options = OptionParser.getOptions("BuildLanguageModel", true, CmdOptions.class, args);	
		
		Path inputDir = options.isDirectory(CmdOptions.INPUT, true);
		Path output = options.isFile(CmdOptions.OUTPUT, false);
		String _orders[];
		List<Integer> orders = new ArrayList<>();
		try {
			_orders = options.getOptionValues(CmdOptions.NGRAM_ORDER);
			for (String order : _orders) {
				orders.add(Integer.parseInt(order));
			}
		} catch(NumberFormatException e) {
			Log.abort(BuildLanguageModel.class, "Given orders have to be integers.");
		}
		for (int order : orders) {
			if (order < 1 || order > 10) {
				Log.abort(BuildLanguageModel.class, "Given order '%s' has to be in a range of 1 to 10.", options.getOptionValue(CmdOptions.NGRAM_ORDER));
			}
		}
		
		//generate a file that contains a list of all token files (needed by SRILM)
		Path listFile = inputDir.resolve("file.list");
		
		if (!listFile.toFile().exists()) {
			new ModuleLinker().append(
					new SearchFileOrDirToListProcessor("**/*.{tkn}", true).searchForFiles(),
					new StringListToFileWriter<List<Path>>(listFile, true))
			.submit(inputDir);
		}
		
		new PipeLinker(options)
		.append(new ThreadedListProcessor<>(options.getNumberOfThreads(), new LMBuilder(inputDir, output)))
		.submitAndShutdown(orders);
	}

}
