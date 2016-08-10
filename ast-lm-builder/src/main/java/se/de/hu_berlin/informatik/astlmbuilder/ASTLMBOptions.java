package se.de.hu_berlin.informatik.astlmbuilder;

import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

/**
 * The options for the ast language model builder are defined in this class as
 * well as the default values.
 */
public class ASTLMBOptions {

	public final static String OUTPUT_FILE = "o";
	public final static String INPUT_DIR = "i";

	public final static String NGRAM_ORDER = "n";
	public final static String NGRAM_ORDER_DEFAULT = "6";
	
	public final static String GRANULARITY = "g";
	public final static String GRAN_NORMAL = "normal";
	public final static String GRAN_ALL = "all";
	
	public final static String SINGLE_TOKENS = "s";

	public final static String ENTRY_POINT = "e";
	public final static String ENTRY_METHOD = "method";
	public final static String ENTRY_ROOT = "root";
	
	public final static String CREATE_ARPA_TEXT = "t";
	
	public final static String THREAD_COUNT = "tc";
	public final static String THREAD_COUNT_DEFAULT = "8";
	
	public final static String MAPPING_DEPTH = "d";
	public final static String MAPPING_DEPTH_DEFAULT = "-1";

	/**
	 * Parses the options from the command line.
	 * 
	 * @param args
	 *            the application's arguments
	 * @return an {@link OptionParser} object that provides access to all parsed
	 *         options and their values or their default values if no specific
	 *         values were set.
	 */
	public static OptionParser getOptions(String[] args) {
		final String tool_usage = "Abstract Syntax Tree Language Model Builder";
		final OptionParser options = new OptionParser(tool_usage, args);

		options.add(OUTPUT_FILE, "output", true, "Path to output file (the language model). "
				+ "Depending on the output format, either the extension '.bin' or '.arpa' will be added.", true);
		options.add(INPUT_DIR, "input", true,
				"Path to the directory with all files that should be used for training the language model", true);
		options.add(GRANULARITY, "granularity", true,
				"Granularity of the tokens. Allowed parameters are \"" + GRAN_NORMAL + "\" and \"" + GRAN_ALL + "\"",
				false);
		options.add(ENTRY_POINT, "entry", true,
				"Determines the point in the abstract syntax tree that starts the generation of sequences" + 
				" which will be inserted into the language model. Allowed parameters are \"" + ENTRY_METHOD + "\" and \"" +
				ENTRY_ROOT + "\"",
				false);
		
		options.add( SINGLE_TOKENS, "genSingleTokens", false, "If set, each AST node will produce a single token "
				+ "instead of possibly producing multiple tokens.", false);
		
		options.add( CREATE_ARPA_TEXT, "textoutput", false, "If set, additionally to the binary file, a human readable " + 
		        "text file in arpa format will be created.", false);

		options.add( NGRAM_ORDER, "ngramorder", true,
				"Set the order of the ngram for the language model. Default is: " + NGRAM_ORDER_DEFAULT, false );
		
		options.add( THREAD_COUNT, "threadCount", true,
				"Set the number of threads that should be working on the training of the language model. Default is: " +
				THREAD_COUNT_DEFAULT, false);
		
		options.add( MAPPING_DEPTH, "mappingDepth", true,
				"Set the depth of the mapping process, where '0' means total abstraction, positive values "
				+ "mean a higher depth, and '-1' means maximum depth. Default is: " +
				MAPPING_DEPTH_DEFAULT, false);
		
		options.parseCommandLine();

		return options;
	}
}
