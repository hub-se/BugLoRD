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

import se.de.hu_berlin.informatik.javatokenizer.modules.TokenizeLinesModule;
import se.de.hu_berlin.informatik.javatokenizer.modules.TraceFileMergerModule;
import se.de.hu_berlin.informatik.utils.fileoperations.FileLineProcessorModule;
import se.de.hu_berlin.informatik.utils.fileoperations.StringListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;

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
//		final String tool_usage = "TokenizeLines -s src-path -t traceFile -o sentence-output [-m] [-l] [-c [order]] [-w]"; 
		final String tool_usage = "TokenizeLines";
		final OptionParser options = new OptionParser(tool_usage, args);
		
		final Option cont_opt = new Option("c", "getContext", true, "Whether each sentence should be preceeded by <#order> tokens, where <#order> is an optional argument.");
        cont_opt.setOptionalArg(true);
        cont_opt.setType(Integer.class);
        
        options.add("s", "srcPath", true, "Path to main source directory.", true);
        options.add("t", "traceFile", true, "Path to trace file or directory with trace files (will get merged) with format: 'relative/path/To/File:line#'.", true);
        options.add("l", "useLookAhead", false, "Whether each sentence should be succeeded by all tokens of the following line.");
        options.add("m", "startFromMethods", false, "Limits the context to within methods. (Currently, the context starts from the last opening curly bracket within the context.)");
        options.add("w", "overwrite", false, "Set flag if files and directories should be overwritten.");
        options.add("o", "output", true, "Path to output file with tokenized sentences.", true);

        options.add(cont_opt);
        
        options.parseCommandLine();
        
        return options;
	}
	
	/**
	 * @param args
	 * -s src-path -t traceFile -o sentence-output [-m] [-l] [-c [order]] [-w]
	 */
	public static void main(String[] args) {		
		
		OptionParser options = getOptions(args);
        
		//source path such that src_path/rel_path_to_file
        String src_path = options.isDirectory('s', true).toString();
		
		//file with file names and line numbers (format: relative/path/To/File:line#)
        Path lineFile = Paths.get(options.getOptionValue('t'));
		
        Path sentence_output = options.isFile('o', false);
		
		Path allTracesMerged = lineFile;
		
		if (lineFile.toFile().isDirectory()) {
			allTracesMerged = Paths.get(lineFile.toString(), "all.trc.mrg");

			new ModuleLinker().link(
					new TraceFileMergerModule(), 
					new StringListToFileWriterModule<>(allTracesMerged , true))
			.submitAndStart(lineFile);
		}
		
		Map<String, Set<Integer>> map = new HashMap<>();
		//maps trace file lines to sentences
		Map<String,String> sentenceMap = new HashMap<>();
		
		ModuleLinker linker = new ModuleLinker();
		
		linker.link(new FileLineProcessorModule<Map<String, List<Integer>>>(new LineParser(map)),
				new TokenizeLinesModule(
						sentenceMap, src_path, allTracesMerged, 
						options.hasOption('c'), 
						options.hasOption('m'), 
						Integer.parseInt(options.getOptionValue('c', "10")), 
						options.hasOption('l')),
				new FileLineProcessorModule<List<String>>(new LineMatcher(sentenceMap), true),
				new StringListToFileWriterModule<List<String>>(sentence_output, options.hasOption('w')))
			.submitAndStart(allTracesMerged);
		
	}

}
