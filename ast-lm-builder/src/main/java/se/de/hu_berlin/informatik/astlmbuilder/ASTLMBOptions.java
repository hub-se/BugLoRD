package se.de.hu_berlin.informatik.astlmbuilder;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.utils.optionparser.IOptions;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;

/**
 * The options for the ast language model builder are defined in this class as
 * well as the default values.
 */
public class ASTLMBOptions {

	public final static String NGRAM_ORDER_DEFAULT = "6";
	public final static String GRAN_NORMAL = "normal";
	public final static String GRAN_ALL = "all";
	public final static String SINGLE_TOKENS = "s";
	public final static String ENTRY_METHOD = "method";
	public final static String ENTRY_ROOT = "root";
	public final static String MAPPING_DEPTH_DEFAULT = "2";
	public final static String SERIALIZATION_DEPTH_DEFAULT = "0";
	public final static String SERIALIZATION_MAX_CHILDREN_DEFAULT = "5"; // five may already be a bit to much

	public static enum CmdOptions implements IOptions {
		/* add options here according to your needs */
		OUTPUT("o", "output", true, "Path to output file (the language model). "
				+ "Depending on the output format, either the extension '.bin' or '.arpa' will be added.", true),
		INPUT("i", "input", true,
				"Path to the directory with all files that should be used for training the language model", true),
		GRANULARITY("g", "granularity", true,
				"Granularity of the tokens. Allowed parameters are \"" + GRAN_NORMAL + "\" and \"" + GRAN_ALL + "\"", false),
		ENTRY_POINT("e", "entry", true,
				"Determines the point in the abstract syntax tree that starts the generation of sequences" + 
				" which will be inserted into the language model. Allowed parameters are \"" + ENTRY_METHOD + "\" and \"" +
				ENTRY_ROOT + "\"", false),
		SINGLE_TOKENS("s", "genSingleTokens", false, "If set, each AST node will produce a single token "
				+ "instead of possibly producing multiple tokens.", false),
		CREATE_ARPA_TEXT("t", "textoutput", false, "If set, additionally to the binary file, a human readable " + 
		        "text file in arpa format will be created.", false),
		NGRAM_ORDER("n", "ngramorder", true,
				"Set the order of the ngram for the language model. Default is: " + NGRAM_ORDER_DEFAULT, false ),
		MAPPING_DEPTH("d", "mappingDepth", true,
				"Set the depth of the mapping process, where '0' means total abstraction, positive values "
				+ "mean a higher depth, and '-1' means maximum depth. Default is: " +
				MAPPING_DEPTH_DEFAULT, false),
		SERIALIZATION_DEPTH("sd", "seriDepth", true,
				"Set the depth of the serialization process, where '0' means no serialization at all, positive values "
				+ "mean a higher depth, and '-1' means maximum depth. Default is: " +
				MAPPING_DEPTH_DEFAULT, false),
		SERIALIZATION_MAX_CHILDREN("smc", "seriMaxChildren", true,
				"Set the maximum number of children that will be included into the serialization process"
				+ ", where '-1' means that all children will always be included. "
				+ "Default is: " +
				SERIALIZATION_MAX_CHILDREN_DEFAULT, false);

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
	
}
