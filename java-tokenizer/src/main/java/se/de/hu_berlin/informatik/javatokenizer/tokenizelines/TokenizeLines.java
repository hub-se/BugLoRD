/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenizelines;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.Option;
import se.de.hu_berlin.informatik.javatokenizer.modules.TraceFileMergerModule;
import se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize;
import se.de.hu_berlin.informatik.javatokenizer.tokenize.Tokenize.TokenizationStrategy;
import se.de.hu_berlin.informatik.utils.fileoperations.FileLineProcessorModule;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.IOptions;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.tm.ITransmitterProvider;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;

/**
 * Tokenizes the specified lines in all files provided in the provided trace file and writes the
 * tokenized lines (sentences) and the according file paths with line numbers to the specified output files.
 * 
 * @author Simon Heiden
 */
public class TokenizeLines {
	
	public static enum CmdOptions implements IOptions {
		/* add options here according to your needs */
		CONTEXT(Option.builder("c").longOpt("getContext").hasArg(true)
				.desc("Whether each sentence should be preceeded by <#order> tokens, where <#order> is an optional argument.")
				.optionalArg(true).type(Integer.class).build()),
		STRATEGY("strat", "strategy", true, "The tokenization strategy to use. ('SYNTAX' (default) or 'SEMANTIC')", false),
		SINGLE_TOKEN("st", "genSingleTokens", false, "If set, each AST node will produce a single token "
				+ "instead of possibly producing multiple tokens. (Only for semantic tokenization.)", false),
		SOURCE_PATH("s", "srcPath", true, "Path to main source directory.", true),
		TRACE_FILE("t", "traceFile", true, "Path to trace file or directory with trace files (will get merged) with format: 'relative/path/To/File:line#'.", true),
		LOOK_AHEAD("l", "useLookAhead", false, "Whether each sentence should be succeeded by all tokens of the following line.", false),
		START_METHODS("m", "startFromMethods", false, "Limits the context to within methods. (Currently, the context "
				+ "starts from the last opening curly bracket within the context.)", false),
		OVERWRITE("w", "overwrite", false, "Set flag if files and directories should be overwritten.", false),
		OUTPUT("o", "output", true, "Path to output file with tokenized sentences.", true),
		MAPPING_DEPTH("d", "mappingDepth", true, "Set the depth of the mapping process, where '0' means total abstraction, positive values "
				+ "mean a higher depth, and '-1' means maximum depth. Default is: " + MAPPING_DEPTH_DEFAULT, false);
		
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

	private final static String MAPPING_DEPTH_DEFAULT = "3";
	
	/**
	 * @param args
	 * -s src-path -t traceFile -o sentence-output [-m] [-l] [-c [order]] [-w]
	 */
	public static void main(String[] args) {		
		
		OptionParser options = OptionParser.getOptions("TokenizeLines", false, CmdOptions.class, args);
        
		//source path such that src_path/rel_path_to_file
        String src_path = options.isDirectory(CmdOptions.SOURCE_PATH, true).toString();
		
		//file with file names and line numbers (format: relative/path/To/File:line#)
        Path lineFile = Paths.get(options.getOptionValue(CmdOptions.TRACE_FILE));
		
        Path sentence_output = options.isFile(CmdOptions.OUTPUT, false);
		
		Path allTracesMerged = lineFile;
		
		if (lineFile.toFile().isDirectory()) {
			allTracesMerged = Paths.get(lineFile.toString(), "all.trc.mrg");

			new ModuleLinker().append(
					new TraceFileMergerModule(), 
					new ListToFileWriterModule<>(allTracesMerged , true))
			.submit(lineFile);
		}
		
		TokenizationStrategy strategy = TokenizationStrategy.SYNTAX;
		if (options.hasOption(CmdOptions.STRATEGY)) {
			switch(options.getOptionValue(CmdOptions.STRATEGY)) {
			case Tokenize.STRAT_SYNTAX:
				strategy = TokenizationStrategy.SYNTAX;
				break;
			case Tokenize.STRAT_SEMANTIC:
				strategy = TokenizationStrategy.SEMANTIC;
				break;
			default:
				Log.abort(TokenizeLines.class, "Unknown strategy: '%s'", options.getOptionValue(CmdOptions.STRATEGY));
			}
		}
		
		int depth = Integer
				.parseInt(options.getOptionValue(CmdOptions.MAPPING_DEPTH, MAPPING_DEPTH_DEFAULT));
		
		Map<String, Set<Integer>> map = new HashMap<>();
		//maps trace file lines to sentences
		Map<String,String> sentenceMap = new HashMap<>();
		
		ModuleLinker linker = new ModuleLinker();
		
		ITransmitterProvider<Map<String, Set<Integer>>, Path> parser = null;
		switch (strategy) {
		case SYNTAX:
			parser = new SyntacticTokenizeLines(
					sentenceMap, src_path, allTracesMerged, 
					options.hasOption(CmdOptions.CONTEXT), 
					options.hasOption(CmdOptions.START_METHODS), 
					Integer.parseInt(options.getOptionValue(CmdOptions.CONTEXT, "10")), 
					options.hasOption(CmdOptions.LOOK_AHEAD));
			break;
		case SEMANTIC:
			parser = new SemanticTokenizeLines(
					sentenceMap, src_path, allTracesMerged, 
					options.hasOption(CmdOptions.CONTEXT), 
					options.hasOption(CmdOptions.START_METHODS), 
					Integer.parseInt(options.getOptionValue(CmdOptions.CONTEXT, "10")), 
					options.hasOption(CmdOptions.STRATEGY), 
					depth);
			break;
		default:
			Log.abort(TokenizeLines.class, "Unimplemented strategy: '%s'", strategy);
		}
		
		linker.append(
				new FileLineProcessorModule<Map<String, Set<Integer>>>(new LineParser(map)),
				parser,
				new FileLineProcessorModule<List<String>>(new LineMatcher(sentenceMap), true),
				new ListToFileWriterModule<List<String>>(sentence_output, options.hasOption(CmdOptions.OVERWRITE)))
			.submit(allTracesMerged);
		
	}

	/**
	 * Convenience method for easier use in a special case.
	 * @param inputDir
	 * the input main source directory, containing the Java source files
	 * @param traceFile
	 * the trace file based on which the sentences shall be generated
	 * (may be an SBFL ranking file)
	 * @param outputFile
	 * the output file for the generated sentences
	 * @param contextLength
	 * the length of the context of the generated sentences
	 */
	public static void tokenizeLinesDefects4JElement(
			String inputDir, String traceFile, String outputFile, String contextLength) {
		String[] args = { 
				CmdOptions.SOURCE_PATH.asArg(), inputDir,
				CmdOptions.TRACE_FILE.asArg(), traceFile,
				CmdOptions.CONTEXT.asArg(), contextLength,
				CmdOptions.OUTPUT.asArg(), outputFile,
				CmdOptions.OVERWRITE.asArg()};
		
		main(args);
	}
	
	/**
	 * Convenience method for easier use in a special case.
	 * @param inputDir
	 * the input main source directory, containing the Java source files
	 * @param traceFile
	 * the trace file based on which the sentences shall be generated
	 * (may be an SBFL ranking file)
	 * @param outputFile
	 * the output file for the generated sentences
	 * @param contextLength
	 * the length of the context of the generated sentences
	 * @param abstractionDepth
	 * the abstraction depth to use by the AST based tokenizer
	 */
	public static void tokenizeLinesDefects4JElementSemantic(
			String inputDir, String traceFile, String outputFile, String contextLength, String abstractionDepth) {
		String[] args = { 
				CmdOptions.SOURCE_PATH.asArg(), inputDir,
				CmdOptions.TRACE_FILE.asArg(), traceFile,
				CmdOptions.STRATEGY.asArg(), "SEMANTIC",
				CmdOptions.CONTEXT.asArg(), contextLength,
				CmdOptions.MAPPING_DEPTH.asArg(), abstractionDepth,
				CmdOptions.OUTPUT.asArg(), outputFile,
				CmdOptions.OVERWRITE.asArg()};
		
		main(args);
	}
	
	/**
	 * Convenience method for easier use in a special case.
	 * @param inputDir
	 * the input main source directory, containing the Java source files
	 * @param traceFile
	 * the trace file based on which the sentences shall be generated
	 * (may be an SBFL ranking file)
	 * @param outputFile
	 * the output file for the generated sentences
	 * @param contextLength
	 * the length of the context of the generated sentences
	 * @param abstractionDepth
	 * the abstraction depth to use by the AST based tokenizer
	 */
	public static void tokenizeLinesDefects4JElementSemanticSingle(
			String inputDir, String traceFile, String outputFile, String contextLength, String abstractionDepth) {
		String[] args = { 
				CmdOptions.SOURCE_PATH.asArg(), inputDir,
				CmdOptions.TRACE_FILE.asArg(), traceFile,
				CmdOptions.STRATEGY.asArg(), "SEMANTIC",
				CmdOptions.SINGLE_TOKEN.asArg(),
				CmdOptions.CONTEXT.asArg(), contextLength,
				CmdOptions.MAPPING_DEPTH.asArg(), abstractionDepth,
				CmdOptions.OUTPUT.asArg(), outputFile,
				CmdOptions.OVERWRITE.asArg()};
		
		main(args);
	}
	
}
