/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.javatokenizer.modules.SemanticTokenizerParserModule;
import se.de.hu_berlin.informatik.javatokenizer.modules.SyntacticTokenizerParserModule;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.fileoperations.ThreadedFileWalkerModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.IOutputPathGenerator;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.miscellaneous.OutputPathGenerator;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;

/**
 * Tokenizes an input file or an entire directory (recursively) of Java source code files. 
 * May be run threaded when given a directory as an input. If the according flag is set, only
 * method bodies are tokenized.
 * 
 * @author Simon Heiden
 */
public class Tokenize {
	
	public final static String STRAT_SYNTAX = "SYNTAX";
	public final static String STRAT_SEMANTIC = "SEMANTIC";
	
	public enum TokenizationStrategy { SYNTAX(0), SEMANTIC(1);
		private final int id;
		private TokenizationStrategy(int id) {
			this.id = id;
		}

		@Override
		public String toString() {
			switch(id) {
			case 0:
				return STRAT_SYNTAX;
			case 1:
				return STRAT_SEMANTIC;
			default:
				return STRAT_SYNTAX;
			}
		}
	}

	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "Tokenize -i input-file/dir -o output-file/dir -t [#threads] [-c] [-m] [-w]"; 
		final String tool_usage = "Tokenize";
		final OptionParser options = new OptionParser(tool_usage, args);
		
		options.add("i", "input", true, "Path to input file/directory.", true);
		options.add("o", "output", true, "Path to output file (or directory, if input is a directory).", true);
		
		options.add("strat", "strategy", true, "The tokenization strategy to use. ('SYNTAX' (default) or 'SEMANTIC')");
		
		options.add("c", "continuous", false, "Set flag if output should be continuous.");
		options.add("m", "methodsOnly", false, "Set flag if only method bodies should be tokenized. (Doesn't work for files that are not parseable.)");
		options.add("w", "overwrite", false, "Set flag if files and directories should be overwritten.");
		
		final Option thread_opt = new Option("t", "threaded", true, "Set flag if threads should be used. (Works only if input is a directory.)");
		thread_opt.setOptionalArg(true);
		thread_opt.setType(Integer.class);
		
		options.add(thread_opt);
        
        options.parseCommandLine();
        
        return options;
	}
	
	/**
	 * @param args
	 * -i input-file/dir -o output-file/dir -t [#threads] [-c] [-m] [-w]
	 */
	public static void main(String[] args) {		
		
		OptionParser options = getOptions(args);
        
		Path input = Paths.get(options.getOptionValue('i'));
		Path output = Paths.get(options.getOptionValue('o'));
		
		TokenizationStrategy strategy = TokenizationStrategy.SYNTAX;
		if (options.hasOption("strat")) {
			switch(options.getOptionValue("strat")) {
			case STRAT_SYNTAX:
				strategy = TokenizationStrategy.SYNTAX;
				break;
			case STRAT_SEMANTIC:
				strategy = TokenizationStrategy.SEMANTIC;
				break;
			default:
				Misc.abort((Object)null, "Unknown strategy: '%s'", options.getOptionValue("strat"));
			}
		}
		
		if ((input.toFile().isDirectory())) {
			int threadCount = 1;
			if (options.hasOption('t')) {
				//parse number of threads
				threadCount = Integer.parseInt(options.getOptionValue('t', "10"));
			} 
			
			boolean done = false;
			
			final String pattern = "**/*.{java}";
			final String extension = ".tkn";
			IOutputPathGenerator<Path> generator = new OutputPathGenerator(output, extension, options.hasOption('w'));

			ThreadedFileWalkerModule threadWalker = null;
			switch (strategy) {
			case SYNTAX:
				threadWalker = new ThreadedFileWalkerModule(false, false, true, pattern, threadCount, 
						SyntacticTokenizeCall.class, options.hasOption('m'), !options.hasOption('c'), generator);
				break;
			case SEMANTIC:
				threadWalker = new ThreadedFileWalkerModule(false, false, true, pattern, threadCount, 
						SemanticTokenizeCall.class, options.hasOption('m'), !options.hasOption('c'), generator);
				break;
			default:
				Misc.abort((Object)null, "Unimplemented strategy: '%s'", strategy);
			}
			//create a new threaded FileWalker object with the given matching pattern, the maximum thread count and stuff
			//tokenize the files
			done = threadWalker.submit(input).getResult();

			if (done) {
				System.out.println("All jobs finished!");
				return;
			} else {
				System.err.println("Timeout reached or Exception thrown! Could not finish all jobs!");
				System.exit(1);
			}
		} else {
			if (output.toFile().isDirectory()) {
				options.printHelp(1, "o");
			}
			//Input is only one file. Don't create a threaded file walker, etc. 
			ModuleLinker linker = new ModuleLinker();

			AModule<Path, List<String>> parser = null;
			switch (strategy) {
			case SYNTAX:
				parser = new SyntacticTokenizerParserModule(options.hasOption('m'), !options.hasOption('c'));
				break;
			case SEMANTIC:
				parser = new SemanticTokenizerParserModule(options.hasOption('m'), !options.hasOption('c'));
				break;
			default:
				Misc.abort((Object)null, "Unimplemented strategy: '%s'", strategy);
			}
			
			linker.link(parser, new ListToFileWriterModule<List<String>>(output, options.hasOption('w')))
				.submit(Paths.get(options.getOptionValue('i')));
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
				"-i", inputDir,
				"-t", "20",
				"-c",
				"-o", outputDir};
		
		main(args);
	}
	
	/**
	 * Convenience method for easier use in a special case.
	 * @param inputDir
	 * the input directory, containing the Java source files
	 * @param outputDir
	 * the output directory for the token files
	 */
	public static void tokenizeDefects4JElementSemantic(
			String inputDir, String outputDir) {
		String[] args = { 
				"-i", inputDir,
				"-t", "20",
				"-strat", "SEMANTIC",
				"-c",
				"-o", outputDir};
		
		main(args);
	}
}
