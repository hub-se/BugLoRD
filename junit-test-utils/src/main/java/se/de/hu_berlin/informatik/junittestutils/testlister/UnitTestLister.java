
package se.de.hu_berlin.informatik.junittestutils.testlister;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.utils.fileoperations.FileLineProcessorModule;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.optionparser.IOptions;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;

/**
 * Collects all JUnit tests from a list of classes provided by an input file.
 * 
 * @author Simon Heiden
 */
public class UnitTestLister {

	public static enum CmdOptions implements IOptions {
		/* add options here according to your needs */
		INPUT("i", "input", true, "Path to input file with list of test classes.", true),
		OUTPUT("o", "output", true, "Path to output file with list of all tests (format: 'test-class::test').", true);
        
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
	
	/**
	 * @param args
	 * -i input-file -o output-file
	 */
	public static void main(String[] args) {

		OptionParser options = OptionParser.getOptions("TestLister", false, CmdOptions.class, args);

		Path input = options.isFile(CmdOptions.INPUT, true);
		Path output = options.isFile(CmdOptions.OUTPUT, false);
		
		new ModuleLinker().link(
				new FileLineProcessorModule<List<String>>(new TestClassLineProcessor(), true), 
				new ListToFileWriterModule<List<String>>(output, true))
		.submit(input);
		
	}
	
}
