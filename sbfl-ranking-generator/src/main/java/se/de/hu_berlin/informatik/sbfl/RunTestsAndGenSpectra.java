package se.de.hu_berlin.informatik.sbfl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.cli.Option;
import org.jacoco.core.runtime.AgentOptions;
import org.junit.runner.Request;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.sbfl.spectra.cobertura.modules.CoberturaAddReportToProviderAndGenerateSpectraModule;
import se.de.hu_berlin.informatik.sbfl.spectra.cobertura.modules.CoberturaTestRunAndReportModule;
import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.JaCoCoToSpectra;
import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.modules.JaCoCoAddReportToProviderAndGenerateSpectraModule;
import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.modules.JaCoCoTestRunAndReportModule;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.FilterSpectraModule;
import se.de.hu_berlin.informatik.stardust.spectra.manipulation.SaveSpectraModule;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.FileLineProcessor;
import se.de.hu_berlin.informatik.utils.files.processors.FileLineProcessor.StringProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.ParentLastClassLoader;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

public class RunTestsAndGenSpectra {

	private RunTestsAndGenSpectra() {
		//disallow instantiation
	}

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		JAVA_HOME_DIR("java", "javaHomeDir", true, "Path to a Java home directory (at least v1.8). Set if you encounter any version problems. "
				+ "If not set, the default JRE is used.", false),
		CLASS_PATH("cp", "classPath", true, "A class path which may be needed for the execution of tests.", false),
		USE_COBERTURA("cob", "cobertura", false, "Whether to use Cobertura to generate coverage.", false),
		ORIGINAL_CLASSES(Option.builder("oc").longOpt("originalClasses")
        		.hasArgs().desc("The original (not instrumented) classes.")
        		.build()),
		AGENT_PORT("p", "port", true, "The port to use for connecting to the JaCoCo Java agent. Default: " + AgentOptions.DEFAULT_PORT, false),
		TEST_LIST("t", "testList", true, "File with all tests to execute.", 0),
		TEST_CLASS_LIST("tcl", "testClassList", true, "File with a list of test classes from which all tests shall be executed.", 0),
		TIMEOUT("tm", "timeout", true, "A timeout (in seconds) for the execution of each test. Tests that run "
				+ "longer than the timeout will abort and will count as failing.", false),
		REPEAT_TESTS("r", "repeatTests", true, "Execute each test a set amount of times to (hopefully) "
				+ "generate correct coverage data. Default is '1'.", false),
		FULL_SPECTRA("f", "fullSpectra", false, "Set this if a full spectra should be generated with all executable statements. Otherwise, only "
				+ "these statements are included that are executed by at least one test case.", false),
		SEPARATE_JVM("jvm", "separateJvm", false, "Set this if each test shall be run in a separate JVM.", false),
		PROJECT_DIR("pd", "projectDir", true, "Path to the directory of the project under test.", true),
		SOURCE_DIR("sd", "sourceDir", true, "Relative path to the main directory containing the sources from the project directory.", true),
		OUTPUT("o", "output", true, "Path to output directory.", true),
		FAILING_TESTS(Option.builder("ft").longOpt("failingTests")
				.hasArgs().desc("A list of tests that should (only) fail. format: qualified.class.name::testMethodName").build());

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

		final OptionParser options = OptionParser.getOptions("RunTestsAndGenSpectra", false, CmdOptions.class, args);

		boolean cobertura = options.hasOption(CmdOptions.USE_COBERTURA);
		Path coberturaDataFile = null;
		if (cobertura) {
			if (System.getProperty("net.sourceforge.cobertura.datafile") == null) {
				Log.abort(RunTestsAndGenSpectra.class, "Please include property '-Dnet.sourceforge.cobertura.datafile=.../cobertura.ser' in the application's call.");
			}
			coberturaDataFile = Paths.get(System.getProperty("net.sourceforge.cobertura.datafile"));
			Log.out(RunTestsAndGenSpectra.class, "Cobertura data file: '%s'.", coberturaDataFile);
		} else {
			if (!options.hasOption(CmdOptions.ORIGINAL_CLASSES)) {
				Log.abort(RunTestsAndGenSpectra.class, "Option '%s' not set.", CmdOptions.ORIGINAL_CLASSES.asArg());
			}
		}
		
		final Path projectDir = options.isDirectory(CmdOptions.PROJECT_DIR, true);
		final Path srcDir = options.isDirectory(projectDir, CmdOptions.SOURCE_DIR, true);
		final String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();
		
		int port = AgentOptions.DEFAULT_PORT;
		if (options.hasOption(CmdOptions.AGENT_PORT)) {
			try {
				port = Integer.valueOf(options.getOptionValue(CmdOptions.AGENT_PORT));
			} catch (NumberFormatException e) {
				Log.abort(JaCoCoToSpectra.class, "Could not parse given agent port: %s.", options.getOptionValue(CmdOptions.AGENT_PORT));
			}
		}
		
		final StatisticsCollector<StatisticsData> statisticsContainer = new StatisticsCollector<>(StatisticsData.class);
		
		final String[] failingtests = options.getOptionValues(CmdOptions.FAILING_TESTS);
		
		final String javaHome = options.getOptionValue(CmdOptions.JAVA_HOME_DIR, null);
		String testAndInstrumentClassPath = options.hasOption(CmdOptions.CLASS_PATH) ? options.getOptionValue(CmdOptions.CLASS_PATH) : null;
		
		List<URL> cpURLs = new ArrayList<>();
		
		if (testAndInstrumentClassPath != null) {
//			Log.out(RunTestsAndGenSpectra.class, testAndInstrumentClassPath);
			String[] cpArray = testAndInstrumentClassPath.split(File.pathSeparator);
			for (String cpElement : cpArray) {
				try {
					cpURLs.add(new File(cpElement).toURI().toURL());
				} catch (MalformedURLException e) {
					Log.err(RunTestsAndGenSpectra.class, e, "Could not parse URL from '%s'.", cpElement);
				}
//				break;
			}
		}
		
		// exclude junit classes to be able to extract the tests
		ClassLoader testClassLoader = 
//				Thread.currentThread().getContextClassLoader(); 
				new ParentLastClassLoader(cpURLs, false, "org.junit", "junit.framework", "org.hamcrest", "java.lang", "java.util");
		
//		Thread.currentThread().setContextClassLoader(testClassLoader);
		
//		Log.out(RunTestsAndGenSpectra.class, Misc.listToString(cpURLs));

		PipeLinker linker = new PipeLinker();
		
		Path testFile = null;
		if (options.hasOption(CmdOptions.TEST_CLASS_LIST)) { //has option "tc"
			testFile = options.isFile(CmdOptions.TEST_CLASS_LIST, true);
			
			linker.append(
					new FileLineProcessor<String>(new StringProcessor<String>() {
						private String clazz = null;
						@Override public boolean process(String clazz) {
							this.clazz = clazz;
							return true;
						}
						@Override public String getLineResult() {
							String temp = clazz;
							clazz = null;
							return temp;
						}
					}),
					new AbstractProcessor<String, TestWrapper>() {
						@Override
						public TestWrapper processItem(String className, ProcessorSocket<String, TestWrapper> socket) {
							try {
								Class<?> testClazz = Class.forName(className, true, testClassLoader);
//								Class<?> testClazz = Class.forName(className);
								
								JUnit4TestAdapter tests = new JUnit4TestAdapter(testClazz);
								for (Test t : tests.getTests()) {
									if (t.toString().startsWith("initializationError(")) {
										Log.err(this, "Test could not be initialized: %s", t.toString());
										continue;
									}
//									socket.produce(new TestWrapper(testClassLoader, t, testClazz));
									socket.produce(new TestWrapper(null, t, testClazz));
								}
								
//								BlockJUnit4ClassRunner runner = new BlockJUnit4ClassRunner(testClazz);
//								List<FrameworkMethod> list = runner.getTestClass().getAnnotatedMethods(org.junit.Test.class);
//								
//								for (FrameworkMethod method : list) {
//									producer.produce(new TestWrapper(instrumentedClassesLoader, testClazz, method));
//								}
//							} catch (InitializationError e) {
//								Log.err(this, e, "Test adapter could not be initialized with class '%s'.", className);
							} 
							catch (ClassNotFoundException e) {
								Log.err(this, "Class '%s' not found.", className);
							}
							return null;
						}
					});
		} else { //has option "t"
			testFile = options.isFile(CmdOptions.TEST_LIST, true);
			
			linker.append(
					new FileLineProcessor<TestWrapper>(new StringProcessor<TestWrapper>() {
						private TestWrapper testWrapper;
						@Override public boolean process(String testNameAndClass) {
							//format: test.class::testName
							final String[] test = testNameAndClass.split("::");
							if (test.length != 2) {
								Log.err(JaCoCoToSpectra.class, "Wrong test identifier format: '" + testNameAndClass + "'.");
								return false;
							} else {
								Class<?> testClazz = null;
								try {
									testClazz = Class.forName(test[0], true, testClassLoader);
//									testClazz = Class.forName(test[0]);
								} catch (ClassNotFoundException e) {
									Log.err(JaCoCoToSpectra.class, "Class '%s' not found.", test[0]);
									return false;
								}
								Request request = Request.method(testClazz, test[1]);
//								testWrapper = new TestWrapper(testClassLoader, request, test[0], test[1]);
								testWrapper = new TestWrapper(null, request, test[0], test[1]);
							}
							return true;
						}
						@Override public TestWrapper getLineResult() {
							TestWrapper temp = testWrapper;
							testWrapper = null;
							return temp;
						}
					}));
		}
		
		
		if (cobertura) {
			linker.append(
					new CoberturaTestRunAndReportModule(coberturaDataFile, outputDir, srcDir.toString(), options.hasOption(CmdOptions.FULL_SPECTRA), false, 
							options.hasOption(CmdOptions.TIMEOUT) ? Long.valueOf(options.getOptionValue(CmdOptions.TIMEOUT)) : null,
									options.hasOption(CmdOptions.REPEAT_TESTS) ? Integer.valueOf(options.getOptionValue(CmdOptions.REPEAT_TESTS)) : 1,
//											testAndInstrumentClassPath + File.pathSeparator + 
											new ClassPathParser().parseSystemClasspath().getClasspath(), 
											javaHome, options.hasOption(CmdOptions.SEPARATE_JVM), failingtests, statisticsContainer, testClassLoader)
//					.asPipe(instrumentedClassesLoader)
					.asPipe().enableTracking().allowOnlyForcedTracks(),
					new CoberturaAddReportToProviderAndGenerateSpectraModule(true, null/*outputDir + File.separator + "fail"*/));
		} else {
			linker.append(
					new JaCoCoTestRunAndReportModule(outputDir, srcDir.toString(), options.getOptionValues(CmdOptions.ORIGINAL_CLASSES), port, false, 
							options.hasOption(CmdOptions.TIMEOUT) ? Long.valueOf(options.getOptionValue(CmdOptions.TIMEOUT)) : null,
									options.hasOption(CmdOptions.REPEAT_TESTS) ? Integer.valueOf(options.getOptionValue(CmdOptions.REPEAT_TESTS)) : 1,
//											testAndInstrumentClassPath + File.pathSeparator + 
											new ClassPathParser().parseSystemClasspath().getClasspath(), 
											javaHome, false,
											//options.hasOption(CmdOptions.SEPARATE_JVM), 
											failingtests, statisticsContainer, testClassLoader)
//					.asPipe(instrumentedClassesLoader)
					.asPipe().enableTracking().allowOnlyForcedTracks(),
					new JaCoCoAddReportToProviderAndGenerateSpectraModule(true, null/*outputDir + File.separator + "fail"*/, options.hasOption(CmdOptions.FULL_SPECTRA)));
		}
		
		linker.append(
//				new BuildCoherentSpectraModule(),
				new SaveSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY, Paths.get(outputDir, BugLoRDConstants.SPECTRA_FILE_NAME)),
//				new TraceFileModule<SourceCodeBlock>(outputDir),
				new FilterSpectraModule<SourceCodeBlock>(INode.CoverageType.EF_EQUALS_ZERO),
				new SaveSpectraModule<SourceCodeBlock>(SourceCodeBlock.DUMMY, Paths.get(outputDir, BugLoRDConstants.FILTERED_SPECTRA_FILE_NAME)))
		.submitAndShutdown(testFile);
		
		EnumSet<StatisticsData> stringDataEnum = EnumSet.noneOf(StatisticsData.class);
		stringDataEnum.add(StatisticsData.ERROR_MSG);
		stringDataEnum.add(StatisticsData.FAILED_TEST_COVERAGE);
		String statsWithoutStringData = statisticsContainer.printStatistics(EnumSet.complementOf(stringDataEnum));
		
		Log.out(JaCoCoToSpectra.class, statsWithoutStringData);
		
		String stats = statisticsContainer.printStatistics(stringDataEnum);
		try {
			FileUtils.writeStrings2File(Paths.get(outputDir, testFile.getFileName() + "_stats").toFile(), statsWithoutStringData, stats);
		} catch (IOException e) {
			Log.err(JaCoCoToSpectra.class, "Can not write statistics to '%s'.", Paths.get(outputDir, testFile.getFileName() + "_stats"));
		}
		
		// we have to specifically call exit(0) here, because for some applications under test,
		// this application does not end due to some reason... (e.g. Mockito causes problems)
		Runtime.getRuntime().exit(0);
	}

}
