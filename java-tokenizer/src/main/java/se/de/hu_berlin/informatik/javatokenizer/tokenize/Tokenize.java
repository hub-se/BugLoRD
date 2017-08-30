/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.files.processors.SearchFileOrDirProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.Processor;
import se.de.hu_berlin.informatik.utils.processors.basics.ListsToChunksCollector;
import se.de.hu_berlin.informatik.utils.processors.basics.ThreadedProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.ModuleLinker;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.Pipe;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;

/**
 * Tokenizes an input file or an entire directory (recursively) of Java source code files. 
 * May be run threaded when given a directory as an input. If the according flag is set, only
 * method bodies are tokenized.
 * 
 * @author Simon Heiden
 */
public class Tokenize {

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		ABSTRACTION_DEPTH("d", "abstractionDepth", true, "Set the depth of the mapping process, where '0' means total abstraction, positive values "
				+ "mean a higher depth, and '-1' means maximum depth. Default is: " + MAPPING_DEPTH_DEFAULT, false),
		INPUT("i", "input", true, "Path to input file/directory.", true),
		OUTPUT("o", "output", true, "Path to output file (or directory, if input is a directory).", true),
		STRATEGY("strat", "strategy", TokenizationStrategy.class, TokenizationStrategy.SYNTAX, 
				"The tokenization strategy to use.", false),
		CONTINUOUS("c", "continuous", false, "Set flag if output should be continuous.", false),
		METHODS_ONLY("m", "methodsOnly", false, "Set flag if only method bodies should be tokenized. (Doesn't work for files that are not parseable.)", false),
		INCLUDE_PARENT("p", "includeParent", false, "Whether to include information about the parent nodes in the tokens.", false),
		OVERWRITE("w", "overwrite", false, "Set flag if files and directories should be overwritten.", false);

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

		//adds an option that may have arguments from a given set (Enum)
		<T extends Enum<T>> CmdOptions(final String opt, final String longOpt, 
				Class<T> valueSet, T defaultValue, final String description, final boolean required) {
			if (defaultValue == null) {
				this.option = new OptionWrapper(
						Option.builder(opt).longOpt(longOpt).required(required).
						hasArgs().desc(description + " Possible arguments: " +
								Misc.enumToString(valueSet) + ".").build(), NO_GROUP);
			} else {
				this.option = new OptionWrapper(
						Option.builder(opt).longOpt(longOpt).required(required).
						hasArg(true).desc(description + " Possible arguments: " +
								Misc.enumToString(valueSet) + ". Default: " + 
								defaultValue.toString() + ".").build(), NO_GROUP);
			}
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

	private final static String MAPPING_DEPTH_DEFAULT = "3";

	public enum TokenizationStrategy { 
		SYNTAX, 
		SEMANTIC,
		SEMANTIC_LONG;

		@Override
		public String toString() {
			switch(this) {
			case SYNTAX:
				return "syntax";
			case SEMANTIC:
				return "semantic";
			case SEMANTIC_LONG:
				return "semantic_long";
			default:
				throw new UnsupportedOperationException("Not implemented.");
			}
		}
	}

	/**
	 * @param args
	 * -i input-file/dir -o output-file/dir -t [#threads] [-c] [-m] [-w]
	 */
	public static void main(String[] args) {		

		OptionParser options = OptionParser.getOptions("Tokenize", true, CmdOptions.class, args);

		Path input = Paths.get(options.getOptionValue(CmdOptions.INPUT));
		Path output = Paths.get(options.getOptionValue(CmdOptions.OUTPUT));

		int depth = Integer
				.parseInt(options.getOptionValue(CmdOptions.ABSTRACTION_DEPTH, MAPPING_DEPTH_DEFAULT));

		TokenizationStrategy strategy = options.getOptionValue(CmdOptions.STRATEGY, TokenizationStrategy.class, TokenizationStrategy.SYNTAX, true);

		if ((input.toFile().isDirectory())) {
			int threadCount = options.getNumberOfThreads(3);

			final String pattern = "**/*.{java}";
			final String extension = ".tkn";

			Pipe<Path,List<String>> threadProcessorPipe = null;
			switch (strategy) {
			case SYNTAX:
				threadProcessorPipe = new ThreadedProcessor<>(threadCount,
						new SyntacticTokenizerParser(options.hasOption(CmdOptions.METHODS_ONLY), !options.hasOption(CmdOptions.CONTINUOUS))).asPipe();
				break;
			case SEMANTIC:
				threadProcessorPipe = new ThreadedProcessor<>(threadCount,
						new SemanticTokenizerParser(options.hasOption(CmdOptions.METHODS_ONLY), !options.hasOption(CmdOptions.CONTINUOUS), 
								false, depth, options.hasOption(CmdOptions.INCLUDE_PARENT))).asPipe();
				break;
			case SEMANTIC_LONG:
				threadProcessorPipe = new ThreadedProcessor<>(threadCount,
						new SemanticTokenizerParser(options.hasOption(CmdOptions.METHODS_ONLY), !options.hasOption(CmdOptions.CONTINUOUS), 
								true, depth, options.hasOption(CmdOptions.INCLUDE_PARENT))).asPipe();
				break;
			default:
				Log.abort(Tokenize.class, "Unimplemented strategy: '%s'", strategy);
			}
			
			threadProcessorPipe.enableTracking(100);
			
			//starting from methods? Then use a pipe to collect the method strings and write them
			//to files in larger chunks, seeing that very small files are being created usually...
			//TODO create option to set the minimum number of lines in an output file

			new PipeLinker().append(
					new SearchFileOrDirProcessor(pattern).includeRootDir().searchForFiles(),
					threadProcessorPipe,
					new ListsToChunksCollector<String>(options.hasOption(CmdOptions.METHODS_ONLY) ? 10000 : 1),
					new ListToFileWriter<List<String>>(output, options.hasOption(CmdOptions.OVERWRITE), true, extension))
			.submitAndShutdown(input);

		} else {
			if (output.toFile().isDirectory()) {
				options.printHelp(CmdOptions.OUTPUT);
			}
			//Input is only one file. Don't create a threaded file walker, etc. 
			ModuleLinker linker = new ModuleLinker();

			Processor<Path, List<String>> parser = null;
			switch (strategy) {
			case SYNTAX:
				parser = new SyntacticTokenizerParser(options.hasOption(CmdOptions.METHODS_ONLY), !options.hasOption(CmdOptions.CONTINUOUS));
				break;
			case SEMANTIC:
				parser = new SemanticTokenizerParser(options.hasOption(CmdOptions.METHODS_ONLY), !options.hasOption(CmdOptions.CONTINUOUS), 
						false, depth, options.hasOption(CmdOptions.INCLUDE_PARENT));
				break;
			case SEMANTIC_LONG:
				parser = new SemanticTokenizerParser(options.hasOption(CmdOptions.METHODS_ONLY), !options.hasOption(CmdOptions.CONTINUOUS), 
						true, depth, options.hasOption(CmdOptions.INCLUDE_PARENT));
				break;
			default:
				Log.abort(Tokenize.class, "Unimplemented strategy: '%s'", strategy);
			}

			linker.append(parser, new ListToFileWriter<List<String>>(output, options.hasOption(CmdOptions.OVERWRITE)))
			.submit(Paths.get(options.getOptionValue(CmdOptions.INPUT)));
		}
	}

	/**
	 * Convenience method for easier use in a special case.
	 * @param inputDir
	 * the input directory, containing the Java source files
	 * @param outputDir
	 * the output directory for the token files
	 */
	public static void tokenizeDefects4JElement(
			String inputDir, String outputDir) {
		String[] args = { 
				CmdOptions.INPUT.asArg(), inputDir,
				CmdOptions.METHODS_ONLY.asArg(),
				CmdOptions.OUTPUT.asArg(), outputDir};

		main(args);
	}

	/**
	 * Convenience method for easier use in a special case.
	 * @param inputDir
	 * the input directory, containing the Java source files
	 * @param outputDir
	 * the output directory for the token files
	 * @param abstractionDepth
	 * the abstraction depth to use by the AST based tokenizer
	 * @param includeParent
	 * whether to include information about the parent node
	 */
	public static void tokenizeDefects4JElementSemantic(
			String inputDir, String outputDir, String abstractionDepth, boolean includeParent) {
		String[] args = { 
				CmdOptions.INPUT.asArg(), inputDir,
				CmdOptions.STRATEGY.asArg(), "SEMANTIC",
				CmdOptions.METHODS_ONLY.asArg(),
				CmdOptions.ABSTRACTION_DEPTH.asArg(), abstractionDepth,
				CmdOptions.OUTPUT.asArg(), outputDir};
		if (includeParent) {
			args = Misc.addToArrayAndReturnResult(args, CmdOptions.INCLUDE_PARENT.asArg()); 
		}
		main(args);
	}
}
