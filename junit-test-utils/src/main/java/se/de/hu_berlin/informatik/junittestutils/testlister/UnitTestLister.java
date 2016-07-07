
package se.de.hu_berlin.informatik.junittestutils.testlister;

import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.utils.fileoperations.FileLineProcessorModule;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.ModuleLinker;

/**
 * Collects all JUnit tests from a list of classes provided by an input file.
 * 
 * @author Simon Heiden
 */
public class UnitTestLister {

	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
//		final String tool_usage = "TestLister -i input-file -o output-file"; 
		final String tool_usage = "TestLister";
		final OptionParser options = new OptionParser(tool_usage, args);

		options.add("i", "input", true, "Path to input file with list of test classes.", true);
		options.add("o", "output", true, "Path to output file with list of all tests (format: 'test-class::test').", true);
        
        options.parseCommandLine();
        
        return options;
	}
	
	/**
	 * @param args
	 * -i input-file -o output-file
	 */
	public static void main(String[] args) {

		OptionParser options = getOptions(args);

		Path input = options.isFile('i', true);
		Path output = options.isFile('o', false);
		
		new ModuleLinker().link(
				new FileLineProcessorModule<List<String>>(new TestClassLineProcessor(), true), 
				new ListToFileWriterModule<List<String>>(output, true))
		.submit(input);
		
	}
	
}
