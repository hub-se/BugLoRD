package se.de.hu_berlin.informatik.astlmbuilder.reader;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;

/**
 * The options for the ast language model reader are defined in this class as
 * well as the default values.
 */
public class ASTLMROptions {

	public enum ASTLMRCmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		INPUT("i", "input", true, "Path to the file that stores the language model. If it has the suffix " + BINARY_SUFFIX +
				" it will be loaded as a binary. The arpa style is assumed otherwise.", true),
        
		LM_ORDER("n", "ngramorder", true, "The order of the language model.", false);	

		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;
		
		//adds an option that is not part of any group
		ASTLMRCmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).
					hasArg(hasArg).desc(description).build(), NO_GROUP);
		}
		
		//adds an option that is part of the group with the specified index (positive integer)
		//a negative index means that this option is part of no group
		//this option will not be required, however, the group itself will be
		ASTLMRCmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).
					hasArg(hasArg).desc(description).build(), groupId);
		}
		
		//adds the given option that will be part of the group with the given id
		ASTLMRCmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}
		
		//adds the given option that will be part of no group
		ASTLMRCmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override public String toString() { return option.getOption().getOpt(); }
		@Override public OptionWrapper getOptionWrapper() { return option; }
	}
	
	public final static String BINARY_SUFFIX = ".bin";
	
	public final static String LM_ORDER_DEFAULT = "6";
}