
package se.de.hu_berlin.informatik.junittestutils.testlister;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.basics.CollectionSequencer;
import se.de.hu_berlin.informatik.utils.processors.basics.ItemCollector;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.ModuleLinker;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.testlister.mining.TestClassLineProcessor;
import se.de.hu_berlin.informatik.utils.files.processors.FileLineProcessor;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.ParentLastClassLoader;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;

/**
 * Collects all JUnit tests from a list of classes provided by an input file.
 * 
 * @author Simon Heiden
 */
public class UnitTestLister {

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		INPUT("i", "input", true, "Path to input file with list of test classes.", true),
		OUTPUT("o", "output", true, "Path to output file with list of all tests (format: 'test-class::test').", true),
		TEST_CLASS_PATH("cp", "classPath", true, "A class path which may be needed for the execution of tests.", false);
        
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

		OptionParser options = OptionParser.getOptions("UnitTestLister", false, CmdOptions.class, args);

		Path input = options.isFile(CmdOptions.INPUT, true);
		Path output = options.isFile(CmdOptions.OUTPUT, false);
		
		String testClassPath = options.hasOption(CmdOptions.TEST_CLASS_PATH) ? options.getOptionValue(CmdOptions.TEST_CLASS_PATH) : null;
		
		List<TestWrapper> items = getAllTestsFromTestClassList(input, testClassPath);
		
		new ListToFileWriter<>(output, true)
		.submit(items);
	}

	/**
	 * Tries to mine all valid test methods from the test classes given in the
	 * input file (as a list of class names). Calls {@link #getAllTestsFromTestClassList(Path, ClassLoader)}
	 * internally.
	 * @param input
	 * file with test class names
	 * @param testClassPath
	 * the class path necessary to run the tests/find the test classes; may be null
	 * @return
	 * a list of tests, given in wrapper objects for easier execution
	 */
	public static List<TestWrapper> getAllTestsFromTestClassList(Path input, String testClassPath) {
		// assemble the necessary directories for running the tests
		List<URL> cpURLs = new ArrayList<>();
				
		if (testClassPath != null) {
//			Log.out(UnitTestLister.class, testClassPath);
			String[] cpArray = testClassPath.split(File.pathSeparator);
			for (String cpElement : cpArray) {
				try {
					cpURLs.add(new File(cpElement).getAbsoluteFile().toURI().toURL());
				} catch (MalformedURLException e) {
					Log.err(UnitTestLister.class, e, "Could not parse URL from '%s'.", cpElement);
				}
//				break;
			}
		}
		
		// exclude junit classes to be able to extract the tests
		ClassLoader testClassLoader = 
				new ParentLastClassLoader(cpURLs, false
						, "junit.runner", "junit.framework", "org.junit", "org.hamcrest", "java.lang", "java.util"
						);
		
		return getAllTestsFromTestClassList(input, testClassLoader);
	}

	/**
	 * Tries to mine all valid test methods from the test classes given in the
	 * input file (as a list of class names). Uses the given ClassLoader instance
	 * to find the test classes.
	 * @param input
	 * file with test class names
	 * @param testClassLoader
	 * the class loader to use to find the test classes
	 * @return
	 * a list of tests, given in wrapper objects for easier execution
	 */
	public static List<TestWrapper> getAllTestsFromTestClassList(Path input, ClassLoader testClassLoader) {
		ItemCollector<TestWrapper> collector = new ItemCollector<>();
		new ModuleLinker().append(
				new FileLineProcessor<List<TestWrapper>>(new TestClassLineProcessor(testClassLoader), true),
				new CollectionSequencer<>(),
				collector)
		.submit(input);
		
		List<TestWrapper> items = collector.getCollectedItems();
		return items;
	}
	
}
