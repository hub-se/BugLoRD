package se.de.hu_berlin.informatik.junittestutils.testrunner;

import java.nio.file.Path;
import java.nio.file.Paths;

import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.junittestutils.testrunner.running.ExtendedTestRunModule;

/**
 * Runs a single Test.
 * 
 * @author Simon
 */
public class UnitTestRunner {

//	public static enum CmdOptions implements OptionWrapperInterface {
//		/* add options here according to your needs */
//		TEST_CLASS("t", "testClass", true, "Name of a test class (full.qualifier.ClassName).", true),
//		TEST_METHOD("m", "testMethod", true, "Test method identifier (someTestName).", true),
//		// should be given as -cp when running the jar file
////		TEST_CLASS_PATH("cp", "classPath", true, "The class path which is needed for the execution of tests.", true),
//		OUTPUT("o", "output", true, "Path to the test statistics result file.", true);
//        
//		/* the following code blocks should not need to be changed */
//		final private OptionWrapper option;
//
//		//adds an option that is not part of any group
//		CmdOptions(final String opt, final String longOpt, 
//				final boolean hasArg, final String description, final boolean required) {
//			this.option = new OptionWrapper(
//					Option.builder(opt).longOpt(longOpt).required(required).
//					hasArg(hasArg).desc(description).build(), NO_GROUP);
//		}
//		
//		//adds an option that is part of the group with the specified index (positive integer)
//		//a negative index means that this option is part of no group
//		//this option will not be required, however, the group itself will be
//		CmdOptions(final String opt, final String longOpt, 
//				final boolean hasArg, final String description, int groupId) {
//			this.option = new OptionWrapper(
//					Option.builder(opt).longOpt(longOpt).required(false).
//					hasArg(hasArg).desc(description).build(), groupId);
//		}
//		
//		//adds the given option that will be part of the group with the given id
//		CmdOptions(Option option, int groupId) {
//			this.option = new OptionWrapper(option, groupId);
//		}
//		
//		//adds the given option that will be part of no group
//		CmdOptions(Option option) {
//			this(option, NO_GROUP);
//		}
//
//		@Override public String toString() { return option.getOption().getOpt(); }
//		@Override public OptionWrapper getOptionWrapper() { return option; }
//	}
	
	/**
	 * @param args
	 * test.Class::testMethod path/to/outputFile.csv
	 */
	public static void main(String[] args) {

//		OptionParser options = OptionParser.getOptions("UnitTestRunner", false, CmdOptions.class, args);
//
//		String testClass = options.getOptionValue(CmdOptions.TEST_CLASS);
//		String testMethod = options.getOptionValue(CmdOptions.TEST_METHOD);
//		
//		Path output = options.isFile(CmdOptions.OUTPUT, false);
		
		if (args.length != 2) {
			System.err.println("Wrong number of arguments.");
			System.exit(1);
		}
		
		String[] testClassAndMethod = args[0].split("::");
		if (testClassAndMethod.length != 2) {
			System.err.println("Wrong test identifier format.");
			System.exit(1);
		}
		
		String testClass = testClassAndMethod[0];
		String testMethod = testClassAndMethod[1];
		
		Path output = Paths.get(args[1]);
		if (output.toFile().isDirectory()) {
			System.err.println(output + " is a directory.");
			System.exit(1);
		}
		
		TestStatistics result = new ExtendedTestRunModule(output.getParent().toString(), true, 600L, null)
				.submit(new TestWrapper(testClass, testMethod))
				.getResult();
		
		result.saveToCSV(output);
		
		if (result.exceptionOccured()) {
			System.err.println("Test execution exception.");
			System.exit(1);
		} else {
			System.exit(0);
		}
	}
	
}
