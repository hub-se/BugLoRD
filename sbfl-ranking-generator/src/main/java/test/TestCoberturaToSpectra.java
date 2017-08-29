/**
 * 
 */
package test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.Option;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.instrument.CodeInstrumentationTask;
import test.TestRunTestsAndGenSpectra;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.miscellaneous.TestSettings;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.basics.ExecuteMainClassInNewJVM;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;


/**
 * Computes SBFL rankings or hit traces from a list of tests or a list of test classes
 * with the support of the stardust API.
 * Instruments given classes with Cobertura and may list all tests of given test classes
 * at the beginning for convenience.
 * 
 * @author Simon Heiden
 */
final public class TestCoberturaToSpectra extends TestSettings {

	private TestCoberturaToSpectra() {
		//disallow instantiation
	}

	private static String extraTestOutput = "target" + File.separator + "testoutputExtra";
	
	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(final String[] args) {
		
		String absProjectPath = new File(getStdResourcesDir() + File.separator + "Mockito12b").getAbsolutePath();

		String testClassPath = absProjectPath + "/lib/junit-4.11.jar" + File.pathSeparator
				+ absProjectPath + "/target/classes" + File.pathSeparator
				+ absProjectPath + "/target/test-classes" + File.pathSeparator
				+ absProjectPath + "/lib/asm-all-5.0.4.jar" + File.pathSeparator
				+ absProjectPath + "/lib/assertj-core-2.1.0.jar" + File.pathSeparator
				+ absProjectPath + "/lib/cglib-and-asm-1.0.jar" + File.pathSeparator
				+ absProjectPath + "/lib/cobertura-2.0.3.jar" + File.pathSeparator
				+ absProjectPath + "/lib/fest-assert-1.3.jar" + File.pathSeparator
				+ absProjectPath + "/lib/fest-util-1.1.4.jar" + File.pathSeparator
				+ absProjectPath + "/lib/hamcrest-all-1.3.jar" + File.pathSeparator
				+ absProjectPath + "/lib/hamcrest-core-1.1.jar" + File.pathSeparator
				+ absProjectPath + "/lib/objenesis-2.1.jar" + File.pathSeparator
				+ absProjectPath + "/lib/objenesis-2.2.jar" + File.pathSeparator
				+ absProjectPath + "/lib/powermock-reflect-1.2.5.jar";
		
		String projectDir = absProjectPath;
		String sourceDir = "src";
		String testClassDir = "target" + File.separator + "test-classes";
		
		String outputDir = extraTestOutput + File.separator + "reportCoberturaTestClassMockito12b";

		final String[] classesToInstrument = new String[] {"target" + File.separator + "classes" };

		String testClassList = getStdResourcesDir() + File.separator + "Mockito12b" + File.separator + "testClasses.txt";
		
		generateSpectra(
				projectDir, sourceDir, testClassDir, outputDir,
				testClassPath, testClassList, null, null, 
				true, false, null, 1, 
				null, classesToInstrument);

	}

	/**
	 * Generates a spectra by executing the specified tests.
	 * @param projectDirOptionValue
	 * main project directory
	 * @param sourceDirOptionValue
	 * the source directory, relative to the project directory
	 * @param testClassDirOptionValue
	 * the class binary directory, relative to the project directory
	 * @param outputDirOptionValue
	 * the output directory in which the spectra shall be stored
	 * that shall be instrumented (will generate coverage data)
	 * @param testClassPath
	 * the class path needed to execute the tests
	 * @param testClassList
	 * path to a file that contains a list of all test classes to consider
	 * @param testList
	 * path to a file that contains a list of all tests to consider
	 * @param javaHome
	 * a Java version to use (path to the home directory)
	 * @param useFullSpectra
	 * whether a full spectra should be created (in contrast to ignoring
	 * uncovered elements)
	 * @param useSeparateJVM
	 * whether a separate JVM shall be used for each test to run
	 * @param timeout
	 * timeout (in seconds) for each test execution; {@code null} if no timeout
	 * shall be used
	 * @param testRepeatCount
	 * number of times to execute each test case; 1 by default if {@code null}
	 * @param failingtests
	 * a list of known failing tests to compare the test execution results with;
	 * if {@code null}, then it will just be ignored
	 * @param pathsToBinaries
	 * a list of paths to class files or directories with class files
	 */
	public static void generateSpectra(String projectDirOptionValue, String sourceDirOptionValue,
			String testClassDirOptionValue, String outputDirOptionValue,
			String testClassPath, String testClassList, String testList,
			final String javaHome, boolean useFullSpectra, boolean useSeparateJVM,
			Long timeout, int testRepeatCount, List<String> failingtests, String... pathsToBinaries) {
		final Path projectDir = FileUtils.checkIfAnExistingDirectory(null, projectDirOptionValue);
		final Path testClassDir = FileUtils.checkIfAnExistingDirectory(projectDir, testClassDirOptionValue);
		final Path sourceDir = FileUtils.checkIfAnExistingDirectory(projectDir, sourceDirOptionValue);
		final Path outputDirPath = FileUtils.checkIfNotAnExistingFile(null, outputDirOptionValue);
		
		if (projectDir == null) {
			Log.abort(TestCoberturaToSpectra.class, "Project directory '%s' does not exist or is not a directory.", projectDirOptionValue);
		}
		if (testClassDir == null) {
			Log.abort(TestCoberturaToSpectra.class, "Test class directory '%s' does not exist or is not a directory.", testClassDirOptionValue);
		}
		if (sourceDir == null) {
			Log.abort(TestCoberturaToSpectra.class, "Source directory '%s' does not exist or is not a directory.", sourceDirOptionValue);
		}
		if (outputDirPath == null) {
			Log.abort(TestCoberturaToSpectra.class, "Output path '%s' points to an existing file.", projectDirOptionValue);
		}
		
		
		final String outputDir = outputDirPath.toAbsolutePath().toString();
		final Path instrumentedDir = Paths.get(outputDir, "instrumented").toAbsolutePath();
		
		String systemClassPath = new ClassPathParser().parseSystemClasspath().getClasspath();
		
		if (testClassPath != null) {
			List<URL> testClassPathList = new ClassPathParser().addClassPathToClassPath(testClassPath).getUniqueClasspathElements();
			
			ClassPathParser reducedtestClassPath = new ClassPathParser();
			for (URL element : testClassPathList) {
//				String path = element.getPath().toLowerCase();
//				if (//path.contains("junit") || 
//						path.contains("cobertura")) {
//					Log.out(CoberturaToSpectra.class, "filtered out '%s'.", path);
//					continue;
//				}
				reducedtestClassPath.addElementToClassPath(element);
			}
			testClassPath = reducedtestClassPath.getClasspath();
		}


//		/* #====================================================================================
//		 * # instrumentation
//		 * #==================================================================================== */
//		
//		//build arguments for instrumentation
//		String[] instrArgs = { 
//				Instrument.CmdOptions.OUTPUT.asArg(), Paths.get(outputDir).toAbsolutePath().toString()};
//
//		if (testClassPath != null) {
//			instrArgs = Misc.addToArrayAndReturnResult(instrArgs, 
//					Instrument.CmdOptions.CLASS_PATH.asArg(), testClassPath);
//		}
//
//		if (pathsToBinaries != null) {
//			instrArgs = Misc.addToArrayAndReturnResult(instrArgs, Instrument.CmdOptions.INSTRUMENT_CLASSES.asArg());
//			instrArgs = Misc.joinArrays(instrArgs, pathsToBinaries);
//		}

		final File coberturaDataFile = Paths.get(outputDir, "cobertura.ser").toAbsolutePath().toFile();

//		//we need to run the tests in a new jvm that uses the given Java version
//		int instrumentationResult = new ExecuteMainClassInNewJVM(javaHome, 
//				Instrument.class, 
//				//classPath,
//				systemClassPath + (testClassPath != null ? File.pathSeparator + testClassPath : ""),
//				projectDir.toFile(), 
//				"-Dnet.sourceforge.cobertura.datafile=" + coberturaDataFile.getAbsolutePath().toString())
//				.submit(instrArgs)
//				.getResult();
//
//		if (instrumentationResult != 0) {
//			Log.abort(TestCoberturaToSpectra.class, "Instrumentation failed.");
//		}

		
		/* #====================================================================================
		 * # generate class path for test execution
		 * #==================================================================================== */

		//generate modified class path with instrumented classes
		final ClassPathParser cpParser = new ClassPathParser()
				//append instrumented classes directory
				.addElementToClassPath(instrumentedDir.toAbsolutePath().toFile())
				//append a given class path for any files that are needed to run the tests
				.addClassPathToClassPath(testClassPath)
				//append test class directory
				.addElementToClassPath(testClassDir.toAbsolutePath().toFile());
		//append binaries
		for (final String item : pathsToBinaries) {
			cpParser.addElementAtStartOfClassPath(Paths.get(item).toAbsolutePath().toFile());
		}
		cpParser.addClassPathToClassPath(systemClassPath);
//		cpParser.addElementAtStartOfClassPath(instrumentedDir.toAbsolutePath().toFile());
		String testAndInstrumentClassPath = cpParser.getClasspath();
		
		/* #====================================================================================
		 * # run tests and generate spectra
		 * #==================================================================================== */
		
		//build arguments for the "real" application (running the tests...)
		String[] newArgs = { 
				TestRunTestsAndGenSpectra.CmdOptions.USE_COBERTURA.asArg(),
				TestRunTestsAndGenSpectra.CmdOptions.PROJECT_DIR.asArg(), projectDirOptionValue, 
				TestRunTestsAndGenSpectra.CmdOptions.SOURCE_DIR.asArg(), sourceDirOptionValue,
				TestRunTestsAndGenSpectra.CmdOptions.OUTPUT.asArg(), Paths.get(outputDir).toAbsolutePath().toString(),
				TestRunTestsAndGenSpectra.CmdOptions.TEST_CLASS_DIR.asArg(), testClassDir.toAbsolutePath().toString(),
				TestRunTestsAndGenSpectra.CmdOptions.ORIGINAL_CLASSES_DIRS.asArg()};
		
		newArgs = Misc.joinArrays(newArgs, pathsToBinaries);
		
		if (javaHome != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, javaHome);
		}
		
		newArgs = Misc.addToArrayAndReturnResult(newArgs, TestRunTestsAndGenSpectra.CmdOptions.INSTRUMENTED_DIR.asArg(), instrumentedDir.toAbsolutePath().toString());

		if (testClassList != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, TestRunTestsAndGenSpectra.CmdOptions.TEST_CLASS_LIST.asArg(), String.valueOf(testClassList));
		} else if (testList != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, TestRunTestsAndGenSpectra.CmdOptions.TEST_LIST.asArg(), String.valueOf(testList));
		} else {
			Log.abort(TestCoberturaToSpectra.class, "No test (class) list options given.");
		}
		
		if (testClassPath != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, TestRunTestsAndGenSpectra.CmdOptions.TEST_CLASS_PATH.asArg(), testClassPath);
		}
		
		if (useFullSpectra) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, TestRunTestsAndGenSpectra.CmdOptions.FULL_SPECTRA.asArg());
		}
		
		if (useSeparateJVM) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, TestRunTestsAndGenSpectra.CmdOptions.SEPARATE_JVM.asArg());
		}
		
		if (timeout != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, TestRunTestsAndGenSpectra.CmdOptions.TIMEOUT.asArg(), String.valueOf(timeout));
		}
		
		if (testRepeatCount > 1) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, TestRunTestsAndGenSpectra.CmdOptions.REPEAT_TESTS.asArg(), String.valueOf(testRepeatCount));
		}
		
		if (failingtests != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, TestRunTestsAndGenSpectra.CmdOptions.FAILING_TESTS.asArg());
			for (String failingTest : failingtests) {
				newArgs = Misc.addToArrayAndReturnResult(newArgs, failingTest);
			}
		}
		
//		Log.out(CoberturaToSpectra.class, systemClassPath);
//		
//		List<File> systemCPList = new ClassPathParser().parseSystemClasspath().getUniqueClasspathElements();
//		
//		ClassPathParser reducedSystemCP = new ClassPathParser();
//		for (File element : systemCPList) {
//			String path = element.toString().toLowerCase();
//			if (//path.contains("junit") || 
//					path.contains("mockito")) {
//				Log.out(CoberturaToSpectra.class, "filtered out '%s'.", path);
//				continue;
//			}
//			reducedSystemCP.addElementToClassPath(element);
//		}
		
		//we need to run the tests in a new jvm that uses the given Java version
		new ExecuteMainClassInNewJVM(javaHome, 
				TestRunTestsAndGenSpectra.class,
//				testAndInstrumentClassPath,
//				+ File.pathSeparator + 
				systemClassPath,
//				reducedSystemCP.getClasspath(),
//				new ClassPathParser().parseSystemClasspath().getClasspath(),
				projectDir.toFile(), 
				"-Dnet.sourceforge.cobertura.datafile=" + coberturaDataFile.getAbsolutePath().toString(), 
				"-XX:+UseNUMA", "-XX:+UseConcMarkSweepGC"//, "-Xmx2G"
				)
		.setEnvVariable("CLASSPATH", testAndInstrumentClassPath)
		.setEnvVariable("TZ", "America/Los_Angeles")
		.submit(newArgs);

		
		/* #====================================================================================
		 * # delete instrumented classes
		 * #==================================================================================== */
		
//		FileUtils.delete(instrumentedDir);
	}

	public final static class Instrument {

		private Instrument() {
			//disallow instantiation
		}

		public static enum CmdOptions implements OptionWrapperInterface {
			/* add options here according to your needs */
			CLASS_PATH("cp", "classPath", true, "An additional class path which may be needed for the execution of tests. "
					+ "Will be appended to the regular class path if this option is set.", false),
			INSTRUMENT_CLASSES(Option.builder("c").longOpt("classes").required()
					.hasArgs().desc("A list of classes/directories to instrument with Cobertura.").build()),
			OUTPUT("o", "output", true, "Path to output directory.", true);

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
					final boolean hasArg, final String description, final int groupId) {
				this.option = new OptionWrapper(
						Option.builder(opt).longOpt(longOpt).required(false).
						hasArg(hasArg).desc(description).build(), groupId);
			}

			//adds the given option that will be part of the group with the given id
			CmdOptions(final Option option, final int groupId) {
				this.option = new OptionWrapper(option, groupId);
			}

			//adds the given option that will be part of no group
			CmdOptions(final Option option) {
				this(option, NO_GROUP);
			}

			@Override public String toString() { return option.getOption().getOpt(); }
			@Override public OptionWrapper getOptionWrapper() { return option; }
		}

		/**
		 * @param args
		 * command line arguments
		 */
		public static void main(final String[] args) {

			if (System.getProperty("net.sourceforge.cobertura.datafile") == null) {
				Log.abort(Instrument.class, "Please include property '-Dnet.sourceforge.cobertura.datafile=.../cobertura.ser' in the application's call.");
			}

			final OptionParser options = OptionParser.getOptions("Instrument", false, CmdOptions.class, args);

			final String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();
			final Path coberturaDataFile = Paths.get(System.getProperty("net.sourceforge.cobertura.datafile"));
//			Log.out(Instrument.class, "Cobertura data file: '%s'.", coberturaDataFile);

			final Path instrumentedDir = Paths.get(outputDir, "instrumented").toAbsolutePath();
			final String[] classesToInstrument = options.getOptionValues(CmdOptions.INSTRUMENT_CLASSES);

//			String[] instrArgs = { 
//					"--datafile", coberturaDataFile.toString(),
//					"--destination", instrumentedDir.toString(), 
//					//"--auxClasspath" $COBERTURADIR/cobertura-2.1.1.jar, //not needed since already in class path
//			};
//
//			//add class path for files that can't be found during instrumentation
//			if (options.hasOption(CmdOptions.CLASS_PATH)) {
//				final String[] auxCP = { "--auxClasspath", options.getOptionValue(CmdOptions.CLASS_PATH) };
//				instrArgs = Misc.joinArrays(instrArgs, auxCP);
//			}
//
//			//add the classes (or dirs of classes) to instrument to the end of the argument array
//			instrArgs = Misc.joinArrays(instrArgs, classesToInstrument);
//
//			//instrument the classes
//			final int returnValue = InstrumentMain.instrument(instrArgs);
//			if ( returnValue != 0 ) {
//				Log.abort(Instrument.class, "Error while instrumenting class files.");
//			}
			
			Arguments instrumentationArguments;
			
			ArgumentsBuilder builder = new ArgumentsBuilder();
			builder.setDataFile(coberturaDataFile.toString());
			builder.setDestinationDirectory(instrumentedDir.toString());
			builder.threadsafeRigorous(true);
			for (String file : classesToInstrument) {
				builder.addFileToInstrument(file);
			}

			instrumentationArguments = builder.build();
			
			CodeInstrumentationTask instrumentationTask = new CodeInstrumentationTask();
			try {
				ProjectData projectData = new ProjectData();
				instrumentationTask.instrument(instrumentationArguments, projectData);
				CoverageDataFileHandler.saveCoverageData(projectData, instrumentationArguments.getDataFile());
			} catch (Throwable e) {
				Log.abort(Instrument.class, e, "Error while instrumenting class files.");
			}

		}

	}
	
	public static class Builder {
		
		private String projectDir;
		private String sourceDir;
		private String testClassDir;
		private String outputDir;
		private String[] classesToInstrument;
		private String javaHome;
		private String testClassPath;
		private boolean useFullSpectra = true;
		private boolean useSeparateJVM = false;
		private String testClassList;
		private String testList;
		private Long timeout;
		private int testRepeatCount = 1;
		private List<String> failingTests;
		
		public Builder setProjectDir(String projectDir) {
			this.projectDir = projectDir;
			return this;
		}

		
		public Builder setSourceDir(String sourceDir) {
			this.sourceDir = sourceDir;
			return this;
		}

		
		public Builder setTestClassDir(String testClassDir) {
			this.testClassDir = testClassDir;
			return this;
		}

		
		public Builder setOutputDir(String outputDir) {
			this.outputDir = outputDir;
			return this;
		}

		
		public Builder setPathsToBinaries(String... classesToInstrument) {
			this.classesToInstrument = classesToInstrument;
			return this;
		}

		
		public Builder setJavaHome(String javaHome) {
			this.javaHome = javaHome;
			return this;
		}

		
		public Builder setTestClassPath(String testClassPath) {
			this.testClassPath = testClassPath;
			return this;
		}

		
		public Builder useFullSpectra(boolean useFullSpectra) {
			this.useFullSpectra = useFullSpectra;
			return this;
		}

		
		public Builder useSeparateJVM(boolean useSeparateJVM) {
			this.useSeparateJVM = useSeparateJVM;
			return this;
		}

		
		public Builder setTestClassList(String testClassList) {
			this.testClassList = testClassList;
			return this;
		}

		
		public Builder setTestList(String testList) {
			this.testList = testList;
			return this;
		}

		
		public Builder setTimeout(Long timeout) {
			this.timeout = timeout;
			return this;
		}

		
		public Builder setTestRepeatCount(int testRepeatCount) {
			this.testRepeatCount = testRepeatCount;
			return this;
		}
		
		public Builder setFailingTests(List<String> failingTests) {
			this.failingTests = failingTests;
			return this;
		}

		public void run() {
			generateSpectra(
					projectDir, sourceDir, testClassDir, outputDir,
					testClassPath, testClassList, testList, javaHome, 
					useFullSpectra, useSeparateJVM, timeout, testRepeatCount, 
					failingTests, (String[]) classesToInstrument);
		}
		
	}

}
