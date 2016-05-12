/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenize;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.javatokenizer.modules.TokenizerParserModule;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;
import se.de.hu_berlin.informatik.utils.tm.modules.FileWriterModule;
import se.de.hu_berlin.informatik.utils.tm.modules.ThreadedFileWalkerModule;

/**
 * Tokenizes an input file or an entire directory (recursively) of Java source code files. 
 * May be run threaded when given a directory as an input. If the according flag is set, only
 * method bodies are tokenized.
 * 
 * @author Simon Heiden
 */
public class Tokenize {

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
		
		if ((input.toFile().isDirectory())) {
			int threadCount = 1;
			if (options.hasOption('t')) {
				//parse number of threads
				threadCount = Integer.parseInt(options.getOptionValue('t', "10"));
			} 
			
			boolean done = false;
			
			final String pattern = "**/*.{java}";
			final String extension = ".tkn";
			//create a new threaded FileWalker object with the given matching pattern, the maximum thread count and stuff
			if (options.hasOption('m')) {
				//tokenize method bodies only
				done = new ThreadedFileWalkerModule(false, false, true, pattern, output, extension, threadCount, 
						options.hasOption('w'), TokenizeMethodsCall.class, 
						!options.hasOption('c'))
						.submitAndStart(input)
						.getResult();
			} else {
				//tokenize the complete files
				done = new ThreadedFileWalkerModule(false, false, true, pattern, output, extension, threadCount, 
						options.hasOption('w'), TokenizeCall.class, 
						!options.hasOption('c'))
						.submitAndStart(input)
						.getResult();
			}
			
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
			AModule<Path,List<String>> parser;
			if (options.hasOption('m')) {
				//tokenize methodies only
				parser = new TokenizerParserModule(true, !options.hasOption('c'));
			} else {
				//tokenize the complete file
				parser = new TokenizerParserModule(false, !options.hasOption('c'));
			}
			linker.link(parser, new FileWriterModule<List<String>>(output, options.hasOption('w')))
				.submitAndStart(Paths.get(options.getOptionValue('i')));
		}
	}
}
