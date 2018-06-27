/**
 * 
 */
package se.de.hu_berlin.informatik.gen.spectra;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerationFactory.Strategy;
import se.de.hu_berlin.informatik.gen.spectra.internal.RunAllTestsAndGenSpectra;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.processors.basics.ExecuteMainClassInNewJVM;


/**
 * Computes SBFL rankings or hit traces from a list of tests or a list of test classes
 * with the support of the stardust API.
 * Instruments given classes with Cobertura and may list all tests of given test classes
 * at the beginning for convenience.
 * 
 * @author Simon Heiden
 */
public abstract class AbstractSpectraGenerator {

	/**
	 * Generates a spectra by executing the specified tests.
	 * @param factory
	 * a factory that provides tool-specific methods to generate coverage/run tests, etc.
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
	 * @param useJava7
	 * whether a separate JVM shall be used for each test to run, using Java 7
	 * @param timeout
	 * timeout (in seconds) for each test execution; {@code null} if no timeout
	 * shall be used
	 * @param testRepeatCount
	 * number of times to execute each test case; 1 by default if {@code null}
	 * @param maxErrors
	 * the maximum of test execution errors to tolerate
	 * @param agentPort
	 * port to use by the java agent
	 * @param failingtests
	 * a list of known failing tests to compare the test execution results with;
	 * if {@code null}, then it will just be ignored
	 * @param pathsToBinaries
	 * a list of paths to class files or directories with class files
	 */
	private static void generateSpectra(AbstractSpectraGenerationFactory<?,?,?> factory, 
			String projectDirOptionValue, String sourceDirOptionValue,
			String testClassDirOptionValue, String outputDirOptionValue,
			String testClassPath, String testClassList, String testList,
			final String javaHome, boolean useFullSpectra, boolean useSeparateJVM, boolean useJava7,
			Long timeout, int testRepeatCount, int maxErrors, Integer agentPort, List<String> failingtests, 
			String... pathsToBinaries) {
		final Path projectDir = FileUtils.checkIfAnExistingDirectory(null, projectDirOptionValue);
		final Path testClassDir = FileUtils.checkIfAnExistingDirectory(projectDir, testClassDirOptionValue);
		final Path sourceDir = FileUtils.checkIfAnExistingDirectory(projectDir, sourceDirOptionValue);
		final Path outputDirPath = FileUtils.checkIfNotAnExistingFile(null, outputDirOptionValue);
		
		if (projectDir == null) {
			Log.abort(AbstractSpectraGenerator.class, "Project directory '%s' does not exist or is not a directory.", projectDirOptionValue);
		}
		if (testClassDir == null) {
			Log.abort(AbstractSpectraGenerator.class, "Test class directory '%s' does not exist or is not a directory.", testClassDirOptionValue);
		}
		if (sourceDir == null) {
			Log.abort(AbstractSpectraGenerator.class, "Source directory '%s' does not exist or is not a directory.", sourceDirOptionValue);
		}
		if (outputDirPath == null) {
			Log.abort(AbstractSpectraGenerator.class, "Output path '%s' points to an existing file.", projectDirOptionValue);
		}
		
		
		final String outputDir = outputDirPath.toAbsolutePath().toString();
		final Path instrumentedDir = Paths.get(outputDir, "instrumented").toAbsolutePath();
		
//		String systemClassPath = new ClassPathParser().parseSystemClasspath().getClasspath();
		
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

		/* #====================================================================================
		 * # instrument the class files
		 * #==================================================================================== */
		
		int instrumentationResult = factory
				.getInstrumenter(projectDir, instrumentedDir.toString(), testClassPath, pathsToBinaries)
				.instrumentClasses();

		if (instrumentationResult != 0) {
			Log.abort(AbstractSpectraGenerator.class, "Instrumentation failed.");
		}
		
		// any additions to the class path necessary for running the tests / retrieving the coverage data?
		String[] additionalElements = factory.getElementsToAddToTestClassPathForMainTestRunner();
		
		if (additionalElements != null && additionalElements.length > 0) {
			if (testClassPath == null) {
				testClassPath = "";
			} else {
				testClassPath += File.pathSeparator;
			}
			for (int i = 0; i < additionalElements.length; i++) {
				String path = additionalElements[i];
				testClassPath += path;
				if (i + 1 < additionalElements.length) {
					testClassPath += File.pathSeparator;
				}
			}
		}

		/* #====================================================================================
		 * # run tests and generate spectra
		 * #==================================================================================== */
		
		runTestsAndGenerateSpectra(
				factory, projectDirOptionValue, sourceDirOptionValue, testClassPath, testClassList, testList, javaHome,
				useFullSpectra, useSeparateJVM, useJava7, timeout, testRepeatCount, maxErrors, agentPort, failingtests,
				projectDir, testClassDir, outputDir, instrumentedDir, pathsToBinaries);

		
		/* #====================================================================================
		 * # delete instrumented classes
		 * #==================================================================================== */
		
		FileUtils.delete(instrumentedDir);
	}

	private static void runTestsAndGenerateSpectra(AbstractSpectraGenerationFactory<?, ?, ?> factory, String projectDirOptionValue,
			String sourceDirOptionValue, String testClassPath, String testClassList, String testList,
			final String javaHome, boolean useFullSpectra, boolean useSeparateJVM, boolean useJava7, Long timeout,
			int testRepeatCount, int maxErrors, Integer agentPort, List<String> failingtests, final Path projectDir,
			final Path testClassDir, final String outputDir, final Path instrumentedDir, String... pathsToBinaries) {
		
		String[] newArgs = getArgs(factory.getStrategy(), factory.getSpecificArgsForMainTestRunner(),
				projectDirOptionValue, sourceDirOptionValue, testClassDir, testClassPath, outputDir, instrumentedDir,
				testClassList, testList, javaHome, useFullSpectra, useSeparateJVM, useJava7, timeout, testRepeatCount,
				maxErrors, agentPort, failingtests, pathsToBinaries);
		
		String systemClassPath = new ClassPathParser().parseSystemClasspath().getClasspath();
		
		//we need to run the tests in a new jvm that uses the given Java version
		new ExecuteMainClassInNewJVM(
				null,
				RunAllTestsAndGenSpectra.class,
				systemClassPath,
				projectDir.toFile(), 
				factory.getPropertiesForMainTestRunner(instrumentedDir, useJava7)
				)
		.setEnvVariable("LC_ALL","en_US.UTF-8")
		.setEnvVariable("TZ", "America/Los_Angeles")
		.submit(newArgs);
	}

	private static String[] getArgs(Strategy strategy, String[] specificArgs, String projectDirOptionValue, String sourceDirOptionValue, final Path testClassDir,
			String testClassPath, final String outputDir, final Path instrumentedDir, String testClassList,
			String testList, final String javaHome, boolean useFullSpectra, boolean useSeparateJVM, boolean useJava7,
			Long timeout, int testRepeatCount, int maxErrors, Integer agentPort, List<String> failingtests, String... pathsToBinaries) {
		//build arguments for the "real" application (running the tests...)
		String[] newArgs = {
				RunAllTestsAndGenSpectra.CmdOptions.PROJECT_DIR.asArg(), projectDirOptionValue, 
				RunAllTestsAndGenSpectra.CmdOptions.SOURCE_DIR.asArg(), sourceDirOptionValue,
				RunAllTestsAndGenSpectra.CmdOptions.OUTPUT.asArg(), Paths.get(outputDir).toAbsolutePath().toString(),
				RunAllTestsAndGenSpectra.CmdOptions.TEST_CLASS_DIR.asArg(), testClassDir.toAbsolutePath().toString(),
				RunAllTestsAndGenSpectra.CmdOptions.ORIGINAL_CLASSES_DIRS.asArg()};
		
		newArgs = Misc.joinArrays(newArgs, pathsToBinaries);
		
		if (javaHome != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.JAVA_HOME_DIR.asArg(), javaHome);
		}
		
		newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.STRATEGY.asArg(), strategy.toString());
		
		if (specificArgs != null) {
			newArgs = Misc.joinArrays(newArgs, specificArgs);
		}
		
		newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.INSTRUMENTED_DIR.asArg(), instrumentedDir.toAbsolutePath().toString());

		if (testClassList != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.TEST_CLASS_LIST.asArg(), String.valueOf(testClassList));
		} else if (testList != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.TEST_LIST.asArg(), String.valueOf(testList));
		} else {
			Log.abort(AbstractSpectraGenerator.class, "No test (class) list options given.");
		}
		
		if (testClassPath != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.TEST_CLASS_PATH.asArg(), testClassPath);
		}
		
		if (useFullSpectra) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.FULL_SPECTRA.asArg());
		}
		
		if (useJava7) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.JAVA7.asArg());
		}
		
		if (useSeparateJVM) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.SEPARATE_JVM.asArg());
		}
		
		if (timeout != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.TIMEOUT.asArg(), String.valueOf(timeout));
		}
		
		if (testRepeatCount > 1) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.REPEAT_TESTS.asArg(), String.valueOf(testRepeatCount));
		}
		
		if (maxErrors != 0) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.MAX_ERRORS.asArg(), String.valueOf(maxErrors));
		}
		
		if (agentPort != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.AGENT_PORT.asArg(), String.valueOf(agentPort.intValue()));
		}

		if (failingtests != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunAllTestsAndGenSpectra.CmdOptions.FAILING_TESTS.asArg());
			for (String failingTest : failingtests) {
				newArgs = Misc.addToArrayAndReturnResult(newArgs, failingTest);
			}
		}
		return newArgs;
	}

	public abstract static class AbstractBuilder {
		
		protected String projectDir;
		protected String sourceDir;
		protected String testClassDir;
		protected String outputDir;
		protected String[] classesToInstrument;
		protected String javaHome;
		protected String testClassPath;
		protected boolean useFullSpectra = true;
		protected boolean useSeparateJVM = false;
		protected String testClassList;
		protected String testList;
		protected Long timeout;
		protected int testRepeatCount = 1;
		protected List<String> failingTests;
		protected boolean useJava7;
		protected int maxErrors = 0;
		
		public AbstractBuilder setProjectDir(String projectDir) {
			this.projectDir = projectDir;
			return this;
		}

		
		public AbstractBuilder setSourceDir(String sourceDir) {
			this.sourceDir = sourceDir;
			return this;
		}

		
		public AbstractBuilder setTestClassDir(String testClassDir) {
			this.testClassDir = testClassDir;
			return this;
		}

		
		public AbstractBuilder setOutputDir(String outputDir) {
			this.outputDir = outputDir;
			return this;
		}

		
		public AbstractBuilder setPathsToBinaries(String... classesToInstrument) {
			this.classesToInstrument = classesToInstrument;
			return this;
		}

		
		public AbstractBuilder setJavaHome(String javaHome) {
			this.javaHome = javaHome;
			return this;
		}

		
		public AbstractBuilder setTestClassPath(String testClassPath) {
			this.testClassPath = testClassPath;
			return this;
		}

		
		public AbstractBuilder useFullSpectra(boolean useFullSpectra) {
			this.useFullSpectra = useFullSpectra;
			return this;
		}

		
		public AbstractBuilder useSeparateJVM(boolean useSeparateJVM) {
			this.useSeparateJVM = useSeparateJVM;
			return this;
		}

		public AbstractBuilder useJava7only(boolean useJava7) {
			this.useJava7 = useJava7;
			return this;
		}
		
		public AbstractBuilder setTestClassList(String testClassList) {
			this.testClassList = testClassList;
			return this;
		}

		
		public AbstractBuilder setTestList(String testList) {
			this.testList = testList;
			return this;
		}

		
		public AbstractBuilder setTimeout(Long timeout) {
			this.timeout = timeout;
			return this;
		}

		
		public AbstractBuilder setTestRepeatCount(int testRepeatCount) {
			this.testRepeatCount = testRepeatCount;
			return this;
		}
		
		public AbstractBuilder setMaxErrors(int maxErrors) {
			this.maxErrors  = maxErrors;
			return this;
		}
		
		public AbstractBuilder setFailingTests(List<String> failingTests) {
			this.failingTests = failingTests;
			return this;
		}

		public abstract void run();
		
		public void run(AbstractSpectraGenerationFactory<?,?,?> factory, Integer agentPort) {
			generateSpectra(
					factory, projectDir, sourceDir, testClassDir, outputDir,
					testClassPath, testClassList, testList, javaHome, 
					useFullSpectra, useSeparateJVM, useJava7, timeout, testRepeatCount, 
					maxErrors, agentPort, failingTests, (String[]) classesToInstrument);
		}
		
	}

}
