package se.de.hu_berlin.informatik.astlmbuilder.reader;

import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

/**
 * The options for the ast language model reader are defined in this class as
 * well as the default values.
 */
public class ASTLMROptions {

	public final static String INPUT_DIR = "i";

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

		options.add(INPUT_DIR, "input", true,
				"Path to the directory with all files that should be used for training the language model", true);
		
		options.parseCommandLine();

		return options;
	}
}
