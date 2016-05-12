/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.sandbox;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.javatokenizer.tokenizelines.LineParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;
import se.de.hu_berlin.informatik.utils.tm.modules.FileLineProcessorModule;
import se.de.hu_berlin.informatik.utils.tm.modules.MultiFileWriterModule;

/**
 * Tokenizes the specified lines in all files provided in the provided trace file and writes the
 * tokenized lines (sentences) and the according file paths with line numbers to the specified output files.
 * 
 * @author Simon Heiden
 */
public class TokenizeLines {

	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "TokenizeLines -i src-path rel_path:line-file -o path:line-output sentence-output [-m] [-l] [-c [order]]"; 
		final String tool_usage = "TokenizeLines";
		final OptionParser options = new OptionParser(tool_usage, args);
		
		final Option cont_opt = new Option("c", "getContext", true, "Whether each sentence should be preceeded by <#order> tokens, where <#order> is an optional argument.");
        cont_opt.setOptionalArg(true);
        cont_opt.setType(Integer.class);
        
        options.add("l", "useLookAhead", false, "Whether each sentence should be succeeded by all tokens of the following line.");
        options.add("m", "startFromMethods", false, "Limits the context to within methods. (Currently, the context strats from the last opening curly bracket within the context.)");
        
        final Option input_opt = Option.builder("i").required().longOpt("input")
        		.numberOfArgs(2).desc("Path to main source directory and path to trace file with format: rel_path:line")
        		.build();
        final Option output_opt = Option.builder("o").required().longOpt("output")
        		.numberOfArgs(2).desc("Paths to output files with format: path:line-output, and matching tokenized sentences")
        		.build();

        options.add(input_opt);
        options.add(output_opt);
        options.add(cont_opt);
        
        options.parseCommandLine();
        
        return options;
	}
	
	/**
	 * @param args
	 * -i src-path rel_path:line-file -o path:line-output sentence-output [-m] [-l] [-c [order]]
	 */
	public static void main(String[] args) {		
		
        OptionParser options = getOptions(args);
        
		//source path such that src_path/rel_path_to_file
        final String src_path = options.getCmdLine().getOptionValues('i')[0];
		
		//file with file names and line numbers (format: relative/path/To/File:lineNumber)
        final Path lineFile = Paths.get(options.getCmdLine().getOptionValues('i')[1]);
		
        final Path path_line_output = Paths.get(options.getCmdLine().getOptionValues('o')[0]);
		if (path_line_output.toFile().isDirectory()) {
			options.printHelp(1);
		}
		
		final Path sentence_output = Paths.get(options.getCmdLine().getOptionValues('o')[1]);
		if (sentence_output.toFile().isDirectory()) {
			options.printHelp(1);
		}
		
		final Map<String, List<Integer>> map = new HashMap<>();
		
		ModuleLinker linker = new ModuleLinker();
		
		linker.link(new FileLineProcessorModule(new LineParser(map)),
				new JavaParserLinesModule(src_path, options.getCmdLine().hasOption('c'), 
						options.getCmdLine().hasOption('m'), 
						Integer.parseInt(options.getCmdLine().getOptionValue('c', "10")),
						options.getCmdLine().hasOption('l')),
				new MultiFileWriterModule<List<List<String>>>(true, path_line_output, sentence_output))
			.submitAndStart(lineFile);
		
	}

}
