/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.jacoco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.cli.Option;
import org.jacoco.agent.AgentJar;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;
import se.de.hu_berlin.informatik.sbfl.RunTestsAndGenSpectra;
import se.de.hu_berlin.informatik.sbfl.spectra.cobertura.CoberturaToSpectra;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.basics.ExecuteMainClassInNewJVM;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;


/**
 * Computes SBFL rankings or hit traces from a list of tests or a list of test classes
 * with the support of the stardust API.
 * Instruments given classes with JaCoCo and may list all tests of given test classes
 * at the beginning for convenience.
 * 
 * @author Simon Heiden
 */
final public class JaCoCoToSpectra {

	private JaCoCoToSpectra() {
		//disallow instantiation
	}

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		JAVA_HOME_DIR("java", "javaHomeDir", true, "Path to a Java home directory (at least v1.8). Set if you encounter any version problems. "
				+ "If not set, the default JRE is used.", false),
		CLASS_PATH("cp", "classPath", true, "An additional class path which may be needed for the execution of tests. "
				+ "Will be appended to the regular class path if this option is set.", false),
		AGENT_PORT("p", "port", true, "The port to use for connecting to the JaCoCo Java agent. Default: " + AgentOptions.DEFAULT_PORT, false),
		TIMEOUT("tm", "timeout", true, "A timeout (in seconds) for the execution of each test. Tests that run "
				+ "longer than the timeout will abort and will count as failing.", false),
		REPEAT_TESTS("r", "repeatTests", true, "Execute each test a set amount of times to (hopefully) "
				+ "generate correct coverage data. Default is '1'.", false),
		FULL_SPECTRA("f", "fullSpectra", false, "Set this if a full spectra should be generated with all executable statements. Otherwise, only "
				+ "these statements are included that are executed by at least one test case.", false),
		SEPARATE_JVM("jvm", "separateJvm", false, "Set this if each test shall be run in a separate JVM.", false),
		TEST_LIST("t", "testList", true, "File with all tests to execute.", 0),
		TEST_CLASS_LIST("tcl", "testClassList", true, "File with a list of test classes from which all tests shall be executed.", 0),
		INSTRUMENT_CLASSES(Option.builder("c").longOpt("classes").required()
				.hasArgs().desc("A list of classes/directories to instrument with JaCoCo.").build()),
		PROJECT_DIR("pd", "projectDir", true, "Path to the directory of the project under test.", true),
		SOURCE_DIR("sd", "sourceDir", true, "Relative path to the main directory containing the sources from the project directory.", true),
		TEST_CLASS_DIR("td", "testClassDir", true, "Relative path to the main directory containing the needed test classes from the project directory.", true),
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
	 *  set this to true to instrument the classes first, before execution;
	 *  if this is set to false, then the "on-the-fly" instrumentation of JaCoCo will be used
	 */
	public final static boolean OFFLINE_INSTRUMENTATION = true;

	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(final String[] args) {

		final OptionParser options = OptionParser.getOptions("JaCoCoToSpectra", false, CmdOptions.class, args);

		String projectDir = options.getOptionValue(CmdOptions.PROJECT_DIR);
		String sourceDir = options.getOptionValue(CmdOptions.SOURCE_DIR);
		String testClassDir = options.getOptionValue(CmdOptions.TEST_CLASS_DIR);
		
		String outputDir = options.getOptionValue(CmdOptions.OUTPUT);

		final String[] classesToInstrument = options.getOptionValues(CmdOptions.INSTRUMENT_CLASSES);

		final String javaHome = options.getOptionValue(CmdOptions.JAVA_HOME_DIR, null);
		
		String testClassPath = options.getOptionValue(CmdOptions.CLASS_PATH, null);
		
		boolean useFullSpectra = options.hasOption(CmdOptions.FULL_SPECTRA);
		boolean useSeparateJVM = options.hasOption(CmdOptions.SEPARATE_JVM);
		
		String testClassList = options.getOptionValue(CmdOptions.TEST_CLASS_LIST);
		String testList = options.getOptionValue(CmdOptions.TEST_LIST);
		
		Long timeout = options.getOptionValueAsLong(CmdOptions.TIMEOUT);
		int testRepeatCount = options.getOptionValueAsInt(CmdOptions.REPEAT_TESTS, 1);
		
		Integer agentPort = options.getOptionValueAsInt(CmdOptions.AGENT_PORT);
		
		generateSpectra(
				projectDir, sourceDir, testClassDir, outputDir,
				testClassPath, testClassList, testList, javaHome, 
				useFullSpectra, useSeparateJVM, timeout, testRepeatCount,
				agentPort, null, classesToInstrument);

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
	 * @param failingtests
	 * a list of known failing tests to compare the test execution results with;
	 * if {@code null}, then it will just be ignored
	 * @param testRepeatCount
	 * number of times to execute each test case; 1 by default if {@code null}
	 * @param agentPort
	 * port to use by the java agent
	 * @param pathsToBinaries
	 * a list of paths to class files or directories with class files
	 */
	public static void generateSpectra(String projectDirOptionValue, String sourceDirOptionValue,
			String testClassDirOptionValue, String outputDirOptionValue, String testClassPath,
			String testClassList, String testList, final String javaHome, boolean useFullSpectra, boolean useSeparateJVM, Long timeout,
			int testRepeatCount, Integer agentPort, List<String> failingtests, final String... pathsToBinaries) {
		final Path projectDir = FileUtils.checkIfAnExistingDirectory(null, projectDirOptionValue);
		final Path testClassDir = FileUtils.checkIfAnExistingDirectory(projectDir, testClassDirOptionValue);
		final Path sourceDir = FileUtils.checkIfAnExistingDirectory(projectDir, sourceDirOptionValue);
		final Path outputDirPath = FileUtils.checkIfNotAnExistingFile(null, outputDirOptionValue);
		
		if (projectDir == null) {
			Log.abort(CoberturaToSpectra.class, "Project directory '%s' does not exist or is not a directory.", projectDirOptionValue);
		}
		if (testClassDir == null) {
			Log.abort(CoberturaToSpectra.class, "Test class directory '%s' does not exist or is not a directory.", testClassDirOptionValue);
		}
		if (sourceDir == null) {
			Log.abort(CoberturaToSpectra.class, "Source directory '%s' does not exist or is not a directory.", sourceDirOptionValue);
		}
		if (outputDirPath == null) {
			Log.abort(CoberturaToSpectra.class, "Output path '%s' points to an existing file.", projectDirOptionValue);
		}
		
		int port = AgentOptions.DEFAULT_PORT;
		if (agentPort != null) {
			port = agentPort;
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
//					Log.out(JaCoCoToSpectra.class, "filtered out '%s'.", path);
//					continue;
//				}
				reducedtestClassPath.addElementToClassPath(element);
			}
			testClassPath = reducedtestClassPath.getClasspath();
		}


		/* #====================================================================================
		 * # (offline) instrumentation
		 * #==================================================================================== */
		
		if (OFFLINE_INSTRUMENTATION) {
			//build arguments for instrumentation
			String[] instrArgs = { 
					Instrument.CmdOptions.OUTPUT.asArg(), Paths.get(outputDir).toAbsolutePath().toString()};

			if (testClassPath != null) {
				instrArgs = Misc.addToArrayAndReturnResult(instrArgs, 
						Instrument.CmdOptions.CLASS_PATH.asArg(), testClassPath);
			}

			if (pathsToBinaries != null) {
				instrArgs = Misc.addToArrayAndReturnResult(instrArgs, Instrument.CmdOptions.INSTRUMENT_CLASSES.asArg());
				instrArgs = Misc.joinArrays(instrArgs, pathsToBinaries);
			}

			//we need to run the tests in a new jvm that uses the given Java version
			int instrumentationResult = new ExecuteMainClassInNewJVM(//javaHome,
					null,
					Instrument.class, 
					//classPath,
					systemClassPath + (testClassPath != null ? File.pathSeparator + testClassPath : ""),
					projectDir.toAbsolutePath().toFile())
					.submit(instrArgs)
					.getResult();

			if (instrumentationResult != 0) {
				Log.abort(JaCoCoToSpectra.class, "Instrumentation failed.");
			}
		}
		
//		/* #====================================================================================
//		 * # generate class path for test execution
//		 * #==================================================================================== */
//
//		//generate modified class path with instrumented classes
//		final ClassPathParser cpParser = new ClassPathParser()
//				.parseSystemClasspath();
//		if (OFFLINE_INSTRUMENTATION) {
//			//append instrumented classes directory
//			cpParser.addElementToClassPath(instrumentedDir.toAbsolutePath().toFile());
//		}
//		cpParser
//		//append a given class path for any files that are needed to run the tests
//		.addClassPathToClassPath(testClassPath)
//		//append test class directory
//		.addElementToClassPath(testClassDir.toAbsolutePath().toFile());
//		//append binaries
//		for (final String item : pathsToBinaries) {
//			cpParser.addElementAtStartOfClassPath(Paths.get(item).toAbsolutePath().toFile());
//		}
////		cpParser.addElementAtStartOfClassPath(instrumentedDir.toAbsolutePath().toFile());
//		String testAndInstrumentClassPath = cpParser.getClasspath();

		File jacocoAgentJar = null; 
		try {
			jacocoAgentJar = AgentJar.extractToTempLocation();
		} catch (IOException e) {
			Log.abort(JaCoCoToSpectra.class, e, "Could not create JaCoCo agent jar file.");
		}
		
		if (OFFLINE_INSTRUMENTATION) {
			if (testClassPath == null) {
				testClassPath = "";
			}
			testClassPath += (jacocoAgentJar != null ? File.pathSeparator + jacocoAgentJar.getAbsolutePath() : "");
//			testAndInstrumentClassPath += (jacocoAgentJar != null ? File.pathSeparator + jacocoAgentJar.getAbsolutePath() : "");
		}
		
		/* #====================================================================================
		 * # run tests and generate spectra
		 * #==================================================================================== */
		
		//build arguments for the "real" application (running the tests...)
		String[] newArgs = { 
				RunTestsAndGenSpectra.CmdOptions.PROJECT_DIR.asArg(), projectDir.toAbsolutePath().toString(), 
				RunTestsAndGenSpectra.CmdOptions.SOURCE_DIR.asArg(), sourceDirOptionValue,
				RunTestsAndGenSpectra.CmdOptions.OUTPUT.asArg(), Paths.get(outputDir).toAbsolutePath().toString(),
				RunTestsAndGenSpectra.CmdOptions.TEST_CLASS_DIR.asArg(), testClassDir.toAbsolutePath().toString(),
				RunTestsAndGenSpectra.CmdOptions.ORIGINAL_CLASSES_DIRS.asArg()};
		
		newArgs = Misc.joinArrays(newArgs, pathsToBinaries);
		
		if (javaHome != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, javaHome);
		}
		
		if (OFFLINE_INSTRUMENTATION) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.INSTRUMENTED_DIR.asArg(), instrumentedDir.toAbsolutePath().toString());
		}

		if (testClassList != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.TEST_CLASS_LIST.asArg(), String.valueOf(testClassList));
		} else if (testList != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.TEST_LIST.asArg(), String.valueOf(testList));
		} else {
			Log.abort(CoberturaToSpectra.class, "No test (class) list options given.");
		}
		
		if (testClassPath != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.TEST_CLASS_PATH.asArg(), testClassPath);
		}
		
		if (useFullSpectra) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.FULL_SPECTRA.asArg());
		}
		
		if (useSeparateJVM) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.SEPARATE_JVM.asArg());
		}
		
		if (timeout != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.TIMEOUT.asArg(), String.valueOf(timeout));
		}
		
		if (testRepeatCount > 1) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.REPEAT_TESTS.asArg(), String.valueOf(testRepeatCount));
		}
		
		if (agentPort != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.AGENT_PORT.asArg(), String.valueOf(port));
		}
		
		if (failingtests != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.FAILING_TESTS.asArg());
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
		ExecuteMainClassInNewJVM testRunner;
		if (useSeparateJVM) {
			testRunner = new ExecuteMainClassInNewJVM(//javaHome,
					null,
					RunTestsAndGenSpectra.class,
//					testClassPath + File.pathSeparator + 
					systemClassPath,
//					testAndInstrumentClassPath,
//					reducedSystemCP.getClasspath(),
//					new ClassPathParser().parseSystemClasspath().getClasspath(),
					projectDir.toFile(),
					"-XX:+UseNUMA", "-XX:+UseConcMarkSweepGC"//, "-Xmx2G"
					);
		} else {
			// get a port that is not yet used...
			port = getFreePort(port);
			if (port == -1) {
				Log.abort(JaCoCoToSpectra.class, "Could not find an unused port...");
			}
			Log.out(JaCoCoToSpectra.class, "Using port %d.", port);
			
			if (OFFLINE_INSTRUMENTATION) {
				testRunner = new ExecuteMainClassInNewJVM(//javaHome,
						null,
						RunTestsAndGenSpectra.class,
//						testClassPath + File.pathSeparator + 
						systemClassPath,
//						testAndInstrumentClassPath,
//						reducedSystemCP.getClasspath(),
//						new ClassPathParser().parseSystemClasspath().getClasspath(),
						projectDir.toFile(), 
						"-Djacoco-agent.dumponexit=false", 
						"-Djacoco-agent.output=tcpserver",
						"-Djacoco-agent.excludes=*",
						"-Djacoco-agent.port=" + port,
						"-XX:+UseNUMA", "-XX:+UseConcMarkSweepGC"//, "-Xmx2G"
						);
			} else {
				testRunner = new ExecuteMainClassInNewJVM(//javaHome,
						null,
						RunTestsAndGenSpectra.class,
//						testClassPath + File.pathSeparator + 
						systemClassPath,
//						testAndInstrumentClassPath,
						projectDir.toFile(),
						"-javaagent:" + jacocoAgentJar.getAbsolutePath() 
						+ "=dumponexit=false,"
						+ "output=tcpserver,"
						+ "excludes=se.de.hu_berlin.informatik.*:org.junit.*,"
						+ "port=" + port,
						"-XX:+UseNUMA", "-XX:+UseConcMarkSweepGC"//, "-Xmx2G"
						);
			}
		}
		testRunner
		.setEnvVariable("LC_ALL","en_US.UTF-8")
		.setEnvVariable("TZ", "America/Los_Angeles")
		.submit(newArgs);

		
		/* #====================================================================================
		 * # delete instrumented classes
		 * #==================================================================================== */
		
		if (OFFLINE_INSTRUMENTATION) {
			FileUtils.delete(instrumentedDir);
		}
	}
	
	private static int getFreePort(final int startPort) {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(AgentOptions.DEFAULT_ADDRESS);
		} catch (UnknownHostException e1) {
			// should not happen
			return -1;
		}
		// port between 0 and 65535 !
		Random random = new Random();
		int currentPort = startPort;
		int count = 0;
		while (true) {
			if (count > 1000) {
				return -1;
			}
			++count;
			try {
				new Socket(inetAddress, currentPort).close();
			} catch (final IOException e) {
				// found a free port
				break;
			} catch (IllegalArgumentException e) {
				// should only happen on first try (if argument wrong)
			}
			currentPort = random.nextInt(60536) + 5000;
		}
		return currentPort;
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

		private static Instrumenter instrumenter;

		private static List<File> source = new ArrayList<File>();

		/**
		 * @param args
		 * command line arguments
		 */
		public static void main(final String[] args) {

			final OptionParser options = OptionParser.getOptions("Instrument", false, CmdOptions.class, args);

			final String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();

			final Path instrumentedDir = Paths.get(outputDir, "instrumented").toAbsolutePath();
			final String[] classesToInstrument = options.getOptionValues(CmdOptions.INSTRUMENT_CLASSES);

			for (String file : classesToInstrument) {
				source.add(new File(file).getAbsoluteFile());
			}

			final File absoluteDest = instrumentedDir.toFile().getAbsoluteFile();
			instrumenter = new Instrumenter(new OfflineInstrumentationAccessGenerator());
			int total = 0;
			for (final File s : source) {
				if (s.isFile()) {
					try {
						total += instrument(s, new File(absoluteDest, s.getName()));
						//						Log.out(Instrument.class, "Instrumented %s.", s);
					} catch (IOException e) {
						Log.err(Instrument.class, e, "Could not instrument '%s' with target '%s'.", s, new File(absoluteDest, s.getName()));
					}
				} else {
					try {
						total += instrumentRecursive(s, absoluteDest);
					} catch (IOException e) {
						Log.err(Instrument.class, e, "Could not instrument folder '%s' with target '%s'.", s, absoluteDest);
					}
				}
			}
			Log.out(Instrument.class, "%s classes instrumented to %s.",
					Integer.valueOf(total), absoluteDest);
		}

		private static int instrumentRecursive(final File src, final File dest)
				throws IOException {
			int total = 0;
			if (src.isDirectory()) {
				for (final File child : src.listFiles()) {
					total += instrumentRecursive(child,
							new File(dest, child.getName()));
				}
			} else {
				total += instrument(src, dest);
				//				Log.out(Instrument.class, "Instrumented %s.", src);
			}
			return total;
		}

		private static int instrument(final File src, final File dest) throws IOException {
			dest.getParentFile().mkdirs();
			final InputStream input = new FileInputStream(src);
			try {
				final OutputStream output = new FileOutputStream(dest);
				try {
					return instrumenter.instrumentAll(input, output,
							src.getAbsolutePath());
				} finally {
					output.close();
				}
			} catch (final IOException e) {
				dest.delete();
				throw e;
			} finally {
				input.close();
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
		private Integer agentPort;
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
		
		public Builder setAgentPort(int agentPort) {
			this.agentPort = agentPort;
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
					agentPort, failingTests, (String[]) classesToInstrument);
		}
		
	}

}
