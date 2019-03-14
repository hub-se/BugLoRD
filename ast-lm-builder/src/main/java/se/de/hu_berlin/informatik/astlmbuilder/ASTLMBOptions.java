package se.de.hu_berlin.informatik.astlmbuilder;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;


/**
 * The options for the ast language model builder are defined in this class as
 * well as the default values.
 */
public class ASTLMBOptions {

	public final static String NGRAM_ORDER_DEFAULT = "6";

	public final static String GRAN_NORMAL = "normal";
	public final static String GRAN_ALL = "all";

	public final static String ENTRY_METHOD = "method";
	public final static String ENTRY_ROOT = "root";

	public final static String GRAN_DEFAULT = GRAN_NORMAL;
	public final static String ENTRY_DEFAULT = ENTRY_METHOD;
	public final static String THREAD_COUNT_DEFAULT = "6";
	public final static String MAPPING_DEPTH_DEFAULT = "2";
	public final static String MAX_LIST_MEMBERS_DEFAULT = "-1";
	
	public enum ASTLMBCmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		INPUT("i", "input", true, "Path to the directory storing all source files that are used for training the language model", true),
        
		OUTPUT("o", "output", true, "Path to the output file that will be generated. Can either be in arpa or binary format.", true),
        
        NGRAM_ORDER("n", "order", true, "The order of the n-gram model to build.", false),
        
        GRANULARITY("g", "granularity", true, "Granularity of the tokens. Allowed parameters are \"" + GRAN_NORMAL +
        			"\" and \"" + GRAN_ALL + "\". Default is " + GRAN_DEFAULT, false),
        
        ENTRY_POINT("ep", "entryPoint", false, "Determines the point in the abstract syntax tree that starts the generation of sequences" + 
					" which will be inserted into the language model. Allowed parameters are \"" + ENTRY_METHOD + "\" and \"" +
					ENTRY_ROOT + "\". Default is " + ENTRY_DEFAULT, false),
        
        THREAD_COUNT("tc", "threadCount", true, "Set the number of threads that should be working on the training of the language model. Default is: " +
				THREAD_COUNT_DEFAULT, false),
        
        MAPPING_DEPTH("d", "depth", true, "Set the depth of the mapping process, where '0' means total abstraction, positive values "
				+ "mean a higher depth, and '-1' means maximum depth. Default is: " + MAPPING_DEPTH_DEFAULT, false ),
        
        MAX_LIST_MEMBERS("mlm", "maxListMembers", true, "Set the maximum number of list elements that will be included when generating tokens"
				+ ", where '-1' means that all list members will be included. "
				+ "Default is: " + MAX_LIST_MEMBERS_DEFAULT, false),
			
		CREATE_ARPA_TEXT("t", "arpa", false,
				"If set, the keywords for the node types will be in a human readable format instead of short keywords that are " +
				"optimized for memory and performance.", false),
		
		HUMAN_READABLE_KEYWORDS("hrkw", "humanReadableKeyWords", false,
				"Uses keywords that can be read by humans instead of short ones.", false);
		

		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;
		
		//adds an option that is not part of any group
		ASTLMBCmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).
					hasArg(hasArg).desc(description).build(), NO_GROUP);
		}
		
		//adds an option that is part of the group with the specified index (positive integer)
		//a negative index means that this option is part of no group
		//this option will not be required, however, the group itself will be
		ASTLMBCmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).
					hasArg(hasArg).desc(description).build(), groupId);
		}
		
		//adds the given option that will be part of the group with the given id
		ASTLMBCmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}
		
		//adds the given option that will be part of no group
		ASTLMBCmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override public String toString() { return option.getOption().getOpt(); }
		@Override public OptionWrapper getOptionWrapper() { return option; }
	}

}
