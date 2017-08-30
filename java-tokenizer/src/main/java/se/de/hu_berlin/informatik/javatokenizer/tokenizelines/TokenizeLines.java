/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenizelines;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.Option;
import se.de.hu_berlin.informatik.javatokenizer.modules.TraceFileMergerModule;
import se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize.TokenizationStrategy;
import se.de.hu_berlin.informatik.utils.files.processors.FileLineProcessor;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.miscellaneous.ComparablePair;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.Processor;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.ModuleLinker;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;

/**
 * Tokenizes the specified lines in all files provided in the provided trace
 * file and writes the tokenized lines (sentences) and the according file paths
 * with line numbers to the specified output files.
 * 
 * @author Simon Heiden
 */
public class TokenizeLines {

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		CONTEXT(Option.builder("c").longOpt("getContext").hasArg(true)
				.desc(
						"Whether each sentence should be preceeded by <#order> tokens, where <#order> is an optional argument.")
				.optionalArg(true).type(Integer.class).build()),
		STRATEGY("strat", "strategy", TokenizationStrategy.class, TokenizationStrategy.SYNTAX, 
				"The tokenization strategy to use.", false),
		SOURCE_PATH("s", "srcPath", true, "Path to main source directory.", true),
		TRACE_FILE("t", "traceFile", true,
				"Path to trace file or directory with trace files (will get merged) with format: "
						+ "'package:relative/path/To/File:methodName:startline#:endline#'.",
				true),
		LOOK_AHEAD("l", "useLookAhead", false,
				"Whether each sentence should be succeeded by all tokens of the following line.", false),
		START_METHODS("m", "startFromMethods", false,
				"Limits the context to within methods. (Currently, the context "
						+ "starts from the last opening curly bracket within the context for syntactic tokenization.)",
				false),
		OVERWRITE("w", "overwrite", false, "Set flag if files and directories should be overwritten.", false),
		OUTPUT("o", "output", true, "Path to output file with tokenized sentences.", true),
		ABSTRACTION_DEPTH("d", "abstractionDepth", true,
				"Set the depth of the mapping process, where '0' means total abstraction, positive values "
						+ "mean a higher depth, and '-1' means maximum depth. Default is: " + MAPPING_DEPTH_DEFAULT,
				false),
		INCLUDE_PARENT("p", "includeParent", false, "Whether to include information about the parent nodes in the tokens.", false),
		PRE_TOKEN_COUNT("pre", "preTokenCount", true,
				"The number of tokens to include that precede the actual line. Default is: 0.", false),
		POST_TOKEN_COUNT("post", "postTokenCount", true,
				"The number of tokens to include that succeed the actual line. Default is: 0.", false);

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

	private final static String MAPPING_DEPTH_DEFAULT = "3";

	public final static String CONTEXT_TOKEN = "<_con_end_>";

	/**
	 * @param args
	 * -s src-path -t traceFile -o sentence-output [-m] [-l] [-c [order]] [-w]
	 */
	public static void main(String[] args) {

		OptionParser options = OptionParser.getOptions("TokenizeLines", false, CmdOptions.class, args);

		// source path such that src_path/rel_path_to_file
		String src_path = options.isDirectory(CmdOptions.SOURCE_PATH, true).toString();

		// file with file names and line numbers (format:
		// package:relative/path/To/File:methodName:startline#:endline#)
		Path lineFile = Paths.get(options.getOptionValue(CmdOptions.TRACE_FILE));

		Path sentence_output = options.isFile(CmdOptions.OUTPUT, false);

		Path allTracesMerged = lineFile;

		if (lineFile.toFile().isDirectory()) {
			allTracesMerged = Paths.get(sentence_output.toAbsolutePath().getParent().toString(), "all.trc.mrg");

			new ModuleLinker().append(new TraceFileMergerModule(), new ListToFileWriter<>(allTracesMerged, true))
					.submit(lineFile);
		}

		TokenizationStrategy strategy = options.getOptionValue(CmdOptions.STRATEGY, TokenizationStrategy.class, TokenizationStrategy.SYNTAX, true);

		Map<String, Set<ComparablePair<Integer, Integer>>> map = new FileLineProcessor<>(new LineParser())
				.submit(allTracesMerged).getResult();

		int pre = Integer.parseInt(options.getOptionValue(CmdOptions.PRE_TOKEN_COUNT, "0"));
		int post = Integer.parseInt(options.getOptionValue(CmdOptions.POST_TOKEN_COUNT, "0"));

		Processor<Map<String, Set<ComparablePair<Integer, Integer>>>, Map<String, String>> parser = null;
		switch (strategy) {
		case SYNTAX:
			parser = new SyntacticTokenizeLines(src_path, options.hasOption(CmdOptions.CONTEXT),
					options.hasOption(CmdOptions.START_METHODS),
					Integer.parseInt(options.getOptionValue(CmdOptions.CONTEXT, "10")),
					options.hasOption(CmdOptions.LOOK_AHEAD));
			break;
		case SEMANTIC: {
			int depth = Integer.parseInt(options.getOptionValue(CmdOptions.ABSTRACTION_DEPTH, MAPPING_DEPTH_DEFAULT));

			parser = new SemanticTokenizeLines(src_path, options.hasOption(CmdOptions.CONTEXT),
					options.hasOption(CmdOptions.START_METHODS),
					Integer.parseInt(options.getOptionValue(CmdOptions.CONTEXT, "10")), false, depth, options.hasOption(CmdOptions.INCLUDE_PARENT), pre, post);
		}
			break;
		case SEMANTIC_LONG: {
			int depth = Integer.parseInt(options.getOptionValue(CmdOptions.ABSTRACTION_DEPTH, MAPPING_DEPTH_DEFAULT));

			parser = new SemanticTokenizeLines(src_path, options.hasOption(CmdOptions.CONTEXT),
					options.hasOption(CmdOptions.START_METHODS),
					Integer.parseInt(options.getOptionValue(CmdOptions.CONTEXT, "10")), true, depth, options.hasOption(CmdOptions.INCLUDE_PARENT), pre, post);
		}
			break;
		default:
			Log.abort(TokenizeLines.class, "Unimplemented strategy: '%s'", strategy);
		}
		// maps trace file lines to sentences
		Map<String, String> sentenceMap = parser.submit(map).getResult();

		new ModuleLinker().append(
				new FileLineProcessor<List<String>>(new LineMatcher(sentenceMap), true),
				new ListToFileWriter<List<String>>(sentence_output, options.hasOption(CmdOptions.OVERWRITE)))
				.submit(allTracesMerged);

	}

	/**
	 * Convenience method for easier use in a special case.
	 * @param inputDir
	 * the input main source directory, containing the Java source files
	 * @param traceFile
	 * the trace file based on which the sentences shall be generated (may be an
	 * SBFL ranking file)
	 * @param outputFile
	 * the output file for the generated sentences
	 * @param contextLength
	 * the length of the context of the generated sentences
	 */
	public static void tokenizeLinesDefects4JElement(String inputDir, String traceFile, String outputFile,
			String contextLength) {
		String[] args = { CmdOptions.SOURCE_PATH.asArg(), inputDir, CmdOptions.TRACE_FILE.asArg(), traceFile,
				CmdOptions.CONTEXT.asArg(), contextLength, CmdOptions.OUTPUT.asArg(), outputFile,
				CmdOptions.OVERWRITE.asArg() };

		main(args);
	}

	/**
	 * Convenience method for easier use in a special case.
	 * @param inputDir
	 * the input main source directory, containing the Java source files
	 * @param traceFile
	 * the trace file based on which the sentences shall be generated (may be an
	 * SBFL ranking file)
	 * @param outputFile
	 * the output file for the generated sentences
	 * @param contextLength
	 * the length of the context of the generated sentences
	 * @param abstractionDepth
	 * the abstraction depth to use by the AST based tokenizer
	 * @param includeParent
	 * whether to include information about the parent node
	 * @param preTokenCount
	 * the number of tokens to include that precede the actual line
	 * @param postTokenCount
	 * the number of tokens to include that succeed the actual line
	 */
	public static void tokenizeLinesDefects4JElementSemantic(String inputDir, String traceFile, String outputFile,
			String contextLength, String abstractionDepth, boolean includeParent, String preTokenCount, String postTokenCount) {
		String[] args = { CmdOptions.SOURCE_PATH.asArg(), inputDir, CmdOptions.TRACE_FILE.asArg(), traceFile,
				CmdOptions.STRATEGY.asArg(), TokenizationStrategy.SEMANTIC.toString(), CmdOptions.CONTEXT.asArg(),
				contextLength, CmdOptions.ABSTRACTION_DEPTH.asArg(), abstractionDepth, CmdOptions.OUTPUT.asArg(),
				outputFile, CmdOptions.OVERWRITE.asArg(), CmdOptions.PRE_TOKEN_COUNT.asArg(), preTokenCount, 
				CmdOptions.POST_TOKEN_COUNT.asArg(), postTokenCount };
		if (includeParent) {
			args = Misc.addToArrayAndReturnResult(args, CmdOptions.INCLUDE_PARENT.asArg()); 
		}

		main(args);
	}

}
