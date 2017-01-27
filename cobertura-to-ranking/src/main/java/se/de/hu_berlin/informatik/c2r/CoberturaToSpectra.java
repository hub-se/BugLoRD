/**
 * 
 */
package se.de.hu_berlin.informatik.c2r;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

import org.apache.commons.cli.Option;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.instrument.CodeInstrumentationTask;
import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.c2r.modules.AddReportToProviderAndGenerateSpectraModule;
import se.de.hu_berlin.informatik.c2r.modules.SaveFilteredSpectraModule;
import se.de.hu_berlin.informatik.c2r.modules.SaveSpectraModule;
import se.de.hu_berlin.informatik.c2r.modules.TestRunAndReportModule;
import se.de.hu_berlin.informatik.c2r.modules.TraceFileModule;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.fileoperations.FileLineProcessorPipe;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.tm.modules.ExecuteMainClassInNewJVMModule;
import se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.StringProcessor;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.AbstractPipe;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;


/**
 * Computes SBFL rankings or hit traces from a list of tests or a list of test classes
 * with the support of the stardust API.
 * Instruments given classes with Cobertura and may list all tests of given test classes
 * at the beginning for convenience.
 * 
 * @author Simon Heiden
 */
final public class CoberturaToSpectra {

	private CoberturaToSpectra() {
		//disallow instantiation
	}

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		JAVA_HOME_DIR("java", "javaHomeDir", true, "Path to a Java home directory (at least v1.8). Set if you encounter any version problems. "
				+ "If not set, the default JRE is used.", false),
		CLASS_PATH("cp", "classPath", true, "An additional class path which may be needed for the execution of tests. "
				+ "Will be appended to the regular class path if this option is set.", false),
		TIMEOUT("tm", "timeout", true, "A timeout (in seconds) for the execution of each test. Tests that run "
				+ "longer than the timeout will abort and will count as failing.", false),
		REPEAT_TESTS("r", "repeatTests", true, "Execute each test a set amount of times to (hopefully) "
				+ "generate correct coverage data. Default is '1'.", false),
		FULL_SPECTRA("f", "fullSpectra", false, "Set this if a full spectra should be generated with all executable statements. Otherwise, only "
				+ "these statements are included that are executed by at least one test case.", false),
		SEPARATE_JVM("jvm", "separateJvm", false, "Set this if each test shall be run in a separate JVM.", false),
		TEST_LIST("t", "testList", true, "File with all tests to execute.", 0),
		TEST_CLASS_LIST("tc", "testClassList", true, "File with a list of test classes from which all tests shall be executed.", 0),
		INSTRUMENT_CLASSES(Option.builder("c").longOpt("classes").required()
				.hasArgs().desc("A list of classes/directories to instrument with Cobertura.").build()),
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
	 * @param args
	 * command line arguments
	 */
	public static void main(final String[] args) {

		final OptionParser options = OptionParser.getOptions("CoberturaToSpectra", false, CmdOptions.class, args);

		final Path projectDir = options.isDirectory(CmdOptions.PROJECT_DIR, true);
		options.isDirectory(projectDir, CmdOptions.SOURCE_DIR, true);
		final Path testClassDir = options.isDirectory(projectDir, CmdOptions.TEST_CLASS_DIR, true);
		final String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();

		//		if (!options.hasOption("ht") && options.getOptionValues('l') == null) {
		//			Misc.err("No localizers given. Only generating the compressed spectra.");
		//		}

		final Path instrumentedDir = Paths.get(outputDir, "instrumented").toAbsolutePath();

		final String[] classesToInstrument = options.getOptionValues(CmdOptions.INSTRUMENT_CLASSES);

		final String javaHome = options.getOptionValue(CmdOptions.JAVA_HOME_DIR, null);



		//generate modified class path with instrumented classes at the beginning
		final ClassPathParser cpParser = new ClassPathParser()
				.parseSystemClasspath()
				.addElementAtStartOfClassPath(testClassDir.toAbsolutePath().toFile());
		for (final String item : classesToInstrument) {
			cpParser.addElementAtStartOfClassPath(Paths.get(item).toAbsolutePath().toFile());
		}
		cpParser.addElementAtStartOfClassPath(instrumentedDir.toAbsolutePath().toFile());
		String classPath = cpParser.getClasspath();

		//append a given class path for any files that are needed to run the tests
		classPath += options.hasOption(CmdOptions.CLASS_PATH) ? File.pathSeparator + options.getOptionValue(CmdOptions.CLASS_PATH) : "";



//		String allTestsFile;
//		if (options.hasOption(CmdOptions.TEST_CLASS_LIST)) {
//			//mine all tests from test classes given in input file
//			allTestsFile = Paths.get(outputDir + File.separator + "all_tests.txt").toAbsolutePath().toString();
//			final String[] testlisterArgs = {
//					UnitTestLister.CmdOptions.INPUT.asArg(), options.getOptionValue(CmdOptions.TEST_CLASS_LIST),
//					UnitTestLister.CmdOptions.OUTPUT.asArg(), allTestsFile
//			};
//			//we need the test classes in the class path, so we have to start a new java process
//			final int result = new ExecuteMainClassInNewJVMModule(javaHome, 
//					UnitTestLister.class,
//					classPath, null,
//					"-XX:+UseNUMA")
//					.submit(testlisterArgs)
//					.getResult();
//
//			if (result != 0) {
//				Log.abort(CoberturaToSpectra.class, "Error while mining tests from test class file.");
//			}
//		} else { //has option "t"
//			allTestsFile = options.isFile(CmdOptions.TEST_LIST, true).toAbsolutePath().toString();
//		}


		
		//build arguments for instrumentation
		String[] instrArgs = { 
				Instrument.CmdOptions.OUTPUT.asArg(), Paths.get(outputDir).toAbsolutePath().toString()};

		if (options.hasOption(CmdOptions.CLASS_PATH)) {
			instrArgs = Misc.addToArrayAndReturnResult(instrArgs, 
					Instrument.CmdOptions.CLASS_PATH.asArg(), options.getOptionValue(CmdOptions.CLASS_PATH));
		}

		if (options.getOptionValues(CmdOptions.INSTRUMENT_CLASSES) != null) {
			instrArgs = Misc.addToArrayAndReturnResult(instrArgs, Instrument.CmdOptions.INSTRUMENT_CLASSES.asArg());
			instrArgs = Misc.joinArrays(instrArgs, options.getOptionValues(CmdOptions.INSTRUMENT_CLASSES));
		}

		final File coberturaDataFile = Paths.get(outputDir, "cobertura.ser").toAbsolutePath().toFile();

		//sadly, we have no other choice but to start a new java process with the updated class path and the cobertura data file.
		//updating the class path on the fly is really messy...
		int instrumentationResult = new ExecuteMainClassInNewJVMModule(javaHome, 
				Instrument.class, 
				classPath, projectDir.toFile(), 
				"-Dnet.sourceforge.cobertura.datafile=" + coberturaDataFile.getAbsolutePath().toString())
				.submit(instrArgs)
				.getResult();

		if (instrumentationResult != 0) {
			Log.abort(CoberturaToSpectra.class, "Instrumentation failed.");
		}
		

		
		//build arguments for the "real" application (running the tests...)
		String[] newArgs = { 
				RunTestsAndGenSpectra.CmdOptions.PROJECT_DIR.asArg(), options.getOptionValue(CmdOptions.PROJECT_DIR), 
				RunTestsAndGenSpectra.CmdOptions.SOURCE_DIR.asArg(), options.getOptionValue(CmdOptions.SOURCE_DIR),
				RunTestsAndGenSpectra.CmdOptions.OUTPUT.asArg(), Paths.get(outputDir).toAbsolutePath().toString(),
				RunTestsAndGenSpectra.CmdOptions.CLASS_PATH.asArg(), classPath};
		
		if (javaHome != null) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, javaHome);
		}

		if (options.hasOption(CmdOptions.TEST_CLASS_LIST)) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.TEST_CLASS_LIST.asArg(), String.valueOf(options.getOptionValue(CmdOptions.TEST_CLASS_LIST)));
		} else if (options.hasOption(CmdOptions.TEST_LIST)) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.TEST_LIST.asArg(), String.valueOf(options.getOptionValue(CmdOptions.TEST_LIST)));
		}
		
		if (options.hasOption(CmdOptions.FULL_SPECTRA)) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.FULL_SPECTRA.asArg());
		}
		
		if (options.hasOption(CmdOptions.SEPARATE_JVM)) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.SEPARATE_JVM.asArg());
		}
		
		if (options.hasOption(CmdOptions.TIMEOUT)) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.TIMEOUT.asArg(), String.valueOf(options.getOptionValue(CmdOptions.TIMEOUT)));
		}
		
		if (options.hasOption(CmdOptions.REPEAT_TESTS)) {
			newArgs = Misc.addToArrayAndReturnResult(newArgs, RunTestsAndGenSpectra.CmdOptions.REPEAT_TESTS.asArg(), String.valueOf(options.getOptionValue(CmdOptions.REPEAT_TESTS)));
		}
		
		//sadly, we have no other choice but to start a new java process with the updated class path and the cobertura data file.
		//updating the class path on the fly is really messy...
		new ExecuteMainClassInNewJVMModule(javaHome, 
				RunTestsAndGenSpectra.class,
				classPath, projectDir.toFile(), 
				"-Dnet.sourceforge.cobertura.datafile=" + coberturaDataFile.getAbsolutePath().toString(), 
				"-XX:+UseNUMA", "-XX:+UseConcMarkSweepGC"//, "-Xmx2G"
				)
		.setEnvVariable("TZ", "America/Los_Angeles")
		.submit(newArgs);

		FileUtils.delete(instrumentedDir);

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
			Log.out(Instrument.class, "Cobertura data file: '%s'.", coberturaDataFile);

			final Path instrumentedDir = Paths.get(outputDir, "instrumented").toAbsolutePath();
			final String[] classesToInstrument = options.getOptionValues(CmdOptions.INSTRUMENT_CLASSES);

			String[] instrArgs = { 
					"--datafile", coberturaDataFile.toString(),
					"--destination", instrumentedDir.toString(), 
					//"--auxClasspath" $COBERTURADIR/cobertura-2.1.1.jar, //not needed since already in class path
			};

			//add class path for files that can't be found during instrumentation
			if (options.hasOption(CmdOptions.CLASS_PATH)) {
				final String[] auxCP = { "--auxClasspath", options.getOptionValue(CmdOptions.CLASS_PATH) };
				instrArgs = Misc.joinArrays(instrArgs, auxCP);
			}

			//add the classes (or dirs of classes) to instrument to the end of the argument array
			instrArgs = Misc.joinArrays(instrArgs, classesToInstrument);

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

	public final static class RunTestsAndGenSpectra {

		private RunTestsAndGenSpectra() {
			//disallow instantiation
		}

		public static enum CmdOptions implements OptionWrapperInterface {
			/* add options here according to your needs */
			JAVA_HOME_DIR("java", "javaHomeDir", true, "Path to a Java home directory (at least v1.8). Set if you encounter any version problems. "
					+ "If not set, the default JRE is used.", false),
			CLASS_PATH("cp", "classPath", true, "An additional class path which may be needed for the execution of tests. "
					+ "Will be appended to the regular class path if this option is set.", false),
			TEST_LIST("t", "testList", true, "File with all tests to execute.", 0),
			TEST_CLASS_LIST("tc", "testClassList", true, "File with a list of test classes from which all tests shall be executed.", 0),
			TIMEOUT("tm", "timeout", true, "A timeout (in seconds) for the execution of each test. Tests that run "
					+ "longer than the timeout will abort and will count as failing.", false),
			REPEAT_TESTS("r", "repeatTests", true, "Execute each test a set amount of times to (hopefully) "
					+ "generate correct coverage data. Default is '1'.", false),
			FULL_SPECTRA("f", "fullSpectra", false, "Set this if a full spectra should be generated with all executable statements. Otherwise, only "
					+ "these statements are included that are executed by at least one test case.", false),
			SEPARATE_JVM("jvm", "separateJvm", false, "Set this if each test shall be run in a separate JVM.", false),
			PROJECT_DIR("pd", "projectDir", true, "Path to the directory of the project under test.", true),
			SOURCE_DIR("sd", "sourceDir", true, "Relative path to the main directory containing the sources from the project directory.", true),
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
				Log.abort(RunTestsAndGenSpectra.class, "Please include property '-Dnet.sourceforge.cobertura.datafile=.../cobertura.ser' in the application's call.");
			}

			final OptionParser options = OptionParser.getOptions("RunTestsAndGenSpectra", false, CmdOptions.class, args);

			final Path projectDir = options.isDirectory(CmdOptions.PROJECT_DIR, true);
			final Path srcDir = options.isDirectory(projectDir, CmdOptions.SOURCE_DIR, true);
			final String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();
			final Path coberturaDataFile = Paths.get(System.getProperty("net.sourceforge.cobertura.datafile"));
			Log.out(RunTestsAndGenSpectra.class, "Cobertura data file: '%s'.", coberturaDataFile);
			
			final StatisticsCollector<StatisticsData> statisticsContainer = new StatisticsCollector<>(StatisticsData.class);
			

			PipeLinker linker = new PipeLinker();
			
			Path testFile = null;
			if (options.hasOption(CmdOptions.TEST_CLASS_LIST)) { //has option "tc"
				testFile = options.isFile(CmdOptions.TEST_CLASS_LIST, true);
				
				linker.append(
						new FileLineProcessorPipe<String>(new StringProcessor<String>() {
							private String clazz;
							@Override public boolean process(String clazz) {
								this.clazz = clazz;
								return true;
							}
							@Override public String getResult() {
								return clazz;
							}
						}),
						new AbstractPipe<String, TestWrapper>(true) {
							@Override
							public TestWrapper processItem(String className) {
								try {
									Class<?> testClazz = Class.forName(className);		
									
									JUnit4TestAdapter tests = new JUnit4TestAdapter(testClazz);
									for (Test t : tests.getTests()) {
										submitProcessedItem(new TestWrapper(t, testClazz));
									}
								} catch (ClassNotFoundException e) {
									Log.err(this, "Class '%s' not found.", className);
								}
								return null;
							}
						});
			} else { //has option "t"
				testFile = options.isFile(CmdOptions.TEST_LIST, true);
				
				linker.append(
						new FileLineProcessorPipe<TestWrapper>(new StringProcessor<TestWrapper>() {
							private TestWrapper testWrapper;
							@Override public boolean process(String testNameAndClass) {
								//format: test.class::testName
								final String[] test = testNameAndClass.split("::");
								if (test.length != 2) {
									Log.err(CoberturaToSpectra.class, "Wrong test identifier format: '" + testNameAndClass + "'.");
									return false;
								} else {
									testWrapper = new TestWrapper(test[0], test[1]);
								}
								return true;
							}
							@Override public TestWrapper getResult() {
								return testWrapper;
							}
						}));
			}
			
			linker.append(
					new TestRunAndReportModule(coberturaDataFile, outputDir, srcDir.toString(), options.hasOption(CmdOptions.FULL_SPECTRA), false, 
							options.hasOption(CmdOptions.TIMEOUT) ? Long.valueOf(options.getOptionValue(CmdOptions.TIMEOUT)) : null,
									options.hasOption(CmdOptions.REPEAT_TESTS) ? Integer.valueOf(options.getOptionValue(CmdOptions.REPEAT_TESTS)) : 1,
											options.hasOption(CmdOptions.CLASS_PATH) ? options.getOptionValue(CmdOptions.CLASS_PATH) : null,
													options.hasOption(CmdOptions.JAVA_HOME_DIR) ? options.getOptionValue(CmdOptions.JAVA_HOME_DIR) : null,
															options.hasOption(CmdOptions.SEPARATE_JVM), statisticsContainer)
					.enableTracking(),
					new AddReportToProviderAndGenerateSpectraModule(true, outputDir + File.separator + "fail"),
					new SaveSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY, Paths.get(outputDir, BugLoRDConstants.SPECTRA_FILE_NAME)),
					new SaveFilteredSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY, Paths.get(outputDir, BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME)),
					new TraceFileModule(outputDir))
			.submitAndShutdown(testFile);
			
			EnumSet<StatisticsData> stringDataEnum = EnumSet.noneOf(StatisticsData.class);
			stringDataEnum.add(StatisticsData.ERROR_MSG);
			stringDataEnum.add(StatisticsData.FAILED_TEST_COVERAGE);
			String statsWithoutStringData = statisticsContainer.printStatistics(EnumSet.complementOf(stringDataEnum));
			
			Log.out(CoberturaToSpectra.class, statsWithoutStringData);
			
			String stats = statisticsContainer.printStatistics(stringDataEnum);
			try {
				FileUtils.writeStrings2File(Paths.get(outputDir, testFile.getFileName() + "_stats").toFile(), statsWithoutStringData, stats);
			} catch (IOException e) {
				Log.err(CoberturaToSpectra.class, "Can not write statistics to '%s'.", Paths.get(outputDir, testFile.getFileName() + "_stats"));
			}
		}

	}


	/**
	 * Convenience method for easier use in a special case.
	 * @param workDir
	 * directory of a buggy Defects4J project version
	 * @param mainSrcDir
	 * path to main source directory
	 * @param testBinDir
	 * path to main directory of binary test classes
	 * @param testCP
	 * class path needed to execute tests
	 * @param mainBinDir
	 * path to main directory of binary program classes
	 * @param testClassesFile
	 * path to a file that contains a list of all test classes to consider
	 * @param rankingDir
	 * output path of generated rankings
	 * @param timeout
	 * timeout (in seconds) for each test execution
	 * @param repeatCount
	 * number of times to execute each test case
	 * @param fullSpectra
	 * whether a full spectra should be created
	 * @param alwaysUseSeparateJVM
	 * whether a separate JVM shall be used for each test to run
	 */
	public static void generateRankingForDefects4JElement(
			final String workDir, final String mainSrcDir, final String testBinDir, 
			final String testCP, final String mainBinDir, final String testClassesFile, 
			final String rankingDir, final Long timeout, final Integer repeatCount, 
			final boolean fullSpectra, final boolean alwaysUseSeparateJVM) {
		String[] args = { 
				CmdOptions.PROJECT_DIR.asArg(), workDir, 
				CmdOptions.SOURCE_DIR.asArg(), mainSrcDir,
				CmdOptions.TEST_CLASS_DIR.asArg(), testBinDir,
				CmdOptions.INSTRUMENT_CLASSES.asArg(), mainBinDir,
				CmdOptions.TEST_CLASS_LIST.asArg(), testClassesFile,
				CmdOptions.OUTPUT.asArg(), rankingDir};
		
		if (fullSpectra) {
			args = Misc.addToArrayAndReturnResult(args, CmdOptions.FULL_SPECTRA.asArg());
		}
		
		if (alwaysUseSeparateJVM) {
			args = Misc.addToArrayAndReturnResult(args, CmdOptions.SEPARATE_JVM.asArg());
		}
		
		if (testCP != null) {
			args = Misc.addToArrayAndReturnResult(args, CmdOptions.CLASS_PATH.asArg(), testCP);
		}
		
		if (timeout != null) {
			args = Misc.addToArrayAndReturnResult(args, CmdOptions.TIMEOUT.asArg(), String.valueOf(timeout));
		}
		
		if (repeatCount != null) {
			args = Misc.addToArrayAndReturnResult(args, CmdOptions.REPEAT_TESTS.asArg(), String.valueOf(repeatCount));
		}

		main(args);
	}

}
