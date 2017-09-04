package se.de.hu_berlin.informatik.sbfl.spectra.modules;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.jacoco.core.runtime.AgentOptions;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.testlister.mining.TestMinerProcessor;
import se.de.hu_berlin.informatik.sbfl.RunTestsAndGenSpectra;
import se.de.hu_berlin.informatik.sbfl.RunTestsAndGenSpectra.CmdOptions;
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
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

public class RunTestAndGenSpectraProcessor extends AbstractConsumingProcessor<OptionParser> {

	private static final boolean TEST_DEBUG_OUTPUT = false;

	@Override
	public void consumeItem(OptionParser options) throws UnsupportedOperationException {
		boolean cobertura = options.hasOption(CmdOptions.USE_COBERTURA);
		Path coberturaDataFile = null;
		if (cobertura) {
			if (System.getProperty("net.sourceforge.cobertura.datafile") == null) {
				Log.abort(RunTestsAndGenSpectra.class, "Please include property '-Dnet.sourceforge.cobertura.datafile=.../cobertura.ser' in the application's call.");
			}
			coberturaDataFile = Paths.get(System.getProperty("net.sourceforge.cobertura.datafile"));
//			Log.out(RunTestsAndGenSpectra.class, "Cobertura data file: '%s'.", coberturaDataFile);
		} else {
			if (!options.hasOption(CmdOptions.ORIGINAL_CLASSES_DIRS)) {
				Log.abort(RunTestsAndGenSpectra.class, "Option '%s' not set.", CmdOptions.ORIGINAL_CLASSES_DIRS.asArg());
			}
		}
		
		String[] pathsToBinaries = options.getOptionValues(CmdOptions.ORIGINAL_CLASSES_DIRS);
		
//		Log.out(RunTestsAndGenSpectra.class, "Project dir: '%s'.", Paths.get(options.getOptionValue(CmdOptions.PROJECT_DIR)));
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
		
		String java7RunnerJar = options.getOptionValue(CmdOptions.JAVA7_RUNNER);
		
		int maxErrors = options.getOptionValueAsInt(CmdOptions.MAX_ERRORS, 0);
		
		final StatisticsCollector<StatisticsData> statisticsContainer = new StatisticsCollector<>(StatisticsData.class);
		
		final String[] failingtests = options.getOptionValues(CmdOptions.FAILING_TESTS);
		
		final String javaHome = options.getOptionValue(CmdOptions.JAVA_HOME_DIR, null);
		
		String testClassPath = options.hasOption(CmdOptions.TEST_CLASS_PATH) ? options.getOptionValue(CmdOptions.TEST_CLASS_PATH) : null;
		
		// assemble the necessary directories for running the tests
		List<URL> cpURLs = new ArrayList<>();
		
		@SuppressWarnings("unused")
		Path instrumentedDir = getPathAndAddToURLs(options.getOptionValue(CmdOptions.INSTRUMENTED_DIR), cpURLs);
		@SuppressWarnings("unused")
		Path testClassDir = getPathAndAddToURLs(options.getOptionValue(CmdOptions.TEST_CLASS_DIR), cpURLs);
		for (String binaryPathString : pathsToBinaries) {
			getPathAndAddToURLs(binaryPathString, cpURLs);
		}
		
		if (testClassPath != null) {
//			Log.out(RunTestsAndGenSpectra.class, testClassPath);
			String[] cpArray = testClassPath.split(File.pathSeparator);
			for (String cpElement : cpArray) {
				try {
					cpURLs.add(new File(cpElement).getAbsoluteFile().toURI().toURL());
				} catch (MalformedURLException e) {
					Log.err(RunTestsAndGenSpectra.class, e, "Could not parse URL from '%s'.", cpElement);
				}
//				break;
			}
		}
		
		List<URL> systemClassPathElements = new ClassPathParser().parseSystemClasspath().getUniqueClasspathElements();
		List<URL> testRelatedElements = new ArrayList<>();
		for (URL url : systemClassPathElements) {
			String path = url.getPath().toLowerCase();
			if (path.contains("junit") || 
					path.contains("cobertura")) {
//				Log.out(this, "added '%s' to start of classpath.", path);
				testRelatedElements.add(url);
			}
		}
		
		ClassPathParser classPathParser = new ClassPathParser();
		// TODO: check if necessary..
//		for (URL url : testRelatedElements) {
//			classPathParser.addElementToClassPath(url);
//		}
		for (URL url : cpURLs) {
			classPathParser.addElementToClassPath(url);
		}
		String testClasspath = classPathParser.getClasspath();
		
//		Log.out(RunTestsAndGenSpectra.class, classpath);
		
		// exclude junit classes to be able to extract the tests
		ClassLoader testClassLoader = 
				new ParentLastClassLoader(cpURLs, false
						, "junit.runner", "junit.framework", "org.junit", "org.hamcrest", "java.lang", "java.util"
						);

//		preLoadClasses(instrumentedDir, pathsToBinaries, testClassDir, testClassLoader, false);
		
//		preLoadClasses(instrumentedDir, pathsToBinaries, testClassDir, testClassLoader, true);
		
//		preLoadPrivateInnerClasses(instrumentedDir, pathsToBinaries, testClassDir, testClassLoader, true);
//		
//		preLoadClasses(instrumentedDir, pathsToBinaries, testClassDir, Thread.currentThread().getContextClassLoader(), true);
		
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
					new TestMinerProcessor(testClassLoader, false));
		} else { //has option "t"
			testFile = options.isFile(CmdOptions.TEST_LIST, true);
			
			linker.append(
					new FileLineProcessor<TestWrapper>(testClassLoader, new StringProcessor<TestWrapper>() {
						private TestWrapper testWrapper;
						@Override public boolean process(String testNameAndClass) {
							//format: test.class::testName
							final String[] test = testNameAndClass.split("::");
							if (test.length != 2) {
								Log.err(JaCoCoToSpectra.class, "Wrong test identifier format: '" + testNameAndClass + "'.");
								return false;
							} else {
								testWrapper = new TestWrapper(test[0], test[1], testClassLoader);
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
		
		ClassLoader testAndInstrumentClassLoader = testClassLoader;
		
		if (cobertura) {
			linker.append(
					new CoberturaTestRunAndReportModule(coberturaDataFile, outputDir, projectDir.toFile(), srcDir.toString(), options.hasOption(CmdOptions.FULL_SPECTRA), TEST_DEBUG_OUTPUT, 
							options.hasOption(CmdOptions.TIMEOUT) ? Long.valueOf(options.getOptionValue(CmdOptions.TIMEOUT)) : null,
									options.hasOption(CmdOptions.REPEAT_TESTS) ? Integer.valueOf(options.getOptionValue(CmdOptions.REPEAT_TESTS)) : 1,
											testClasspath, 
											javaHome,
											java7RunnerJar,
											options.hasOption(CmdOptions.SEPARATE_JVM),
											options.hasOption(CmdOptions.JAVA7),
											maxErrors,
											failingtests, statisticsContainer, testAndInstrumentClassLoader)
//					.asPipe(instrumentedClassesLoader)
					.asPipe().enableTracking().allowOnlyForcedTracks(),
					new CoberturaAddReportToProviderAndGenerateSpectraModule(true, null/*outputDir + File.separator + "fail"*/, statisticsContainer));
		} else {
			linker.append(
					new JaCoCoTestRunAndReportModule(Paths.get(outputDir, "__jacoco.exec").toAbsolutePath(), outputDir, projectDir.toFile(), srcDir.toString(), pathsToBinaries, port, TEST_DEBUG_OUTPUT, 
							options.hasOption(CmdOptions.TIMEOUT) ? Long.valueOf(options.getOptionValue(CmdOptions.TIMEOUT)) : null,
									options.hasOption(CmdOptions.REPEAT_TESTS) ? Integer.valueOf(options.getOptionValue(CmdOptions.REPEAT_TESTS)) : 1,
//											new ClassPathParser().parseSystemClasspath().getClasspath() + File.pathSeparator +
											testClasspath, 
											javaHome,
											java7RunnerJar,
											options.hasOption(CmdOptions.SEPARATE_JVM),
											options.hasOption(CmdOptions.JAVA7),
											maxErrors,
											failingtests, statisticsContainer, testAndInstrumentClassLoader)
//					.asPipe(instrumentedClassesLoader)
					.asPipe().enableTracking().allowOnlyForcedTracks(),
					new JaCoCoAddReportToProviderAndGenerateSpectraModule(true, null/*outputDir + File.separator + "fail"*/, options.hasOption(CmdOptions.FULL_SPECTRA), statisticsContainer));
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
	}
	
	private static Path getPathAndAddToURLs(String stringPath, List<URL> cpURLs) {
		Path path = null;
		if (stringPath != null) {
			try {
				path = new File(stringPath).toPath().toAbsolutePath();
				if (path.toFile().exists()) {
					cpURLs.add(path.toFile().toURI().toURL());
				} else {
					Log.err(RunTestsAndGenSpectra.class, "Path '%s' does not exist and will not be added to the class loader.", path);
				}
			} catch (MalformedURLException e) {
				Log.err(RunTestsAndGenSpectra.class, e, "Could not parse URL from '%s'.", stringPath);
			}
		}
		return path;
	}
	
//	private static void preLoadPrivateInnerClasses(Path instrumentedDir, String[] pathsToBinaries, Path testClassDir,
//			ClassLoader testClassLoader, boolean initialize) {
//		ClassLoader normalClassLoader = Thread.currentThread().getContextClassLoader();
//		Thread.currentThread().setContextClassLoader(testClassLoader);
//		
//		if (instrumentedDir != null) {
//			loadPrivateInnerClassesInDirectory(instrumentedDir, testClassLoader, initialize);
//		}
//		
//		for (String binaryPathString : pathsToBinaries) {
//			Path binaryPath = Paths.get(binaryPathString).toAbsolutePath();
//			if (binaryPath.toFile().exists()) {
//				if (binaryPath.toFile().isDirectory()) {
//					loadPrivateInnerClassesInDirectory(binaryPath, testClassLoader, initialize);
//				} else {
//					Log.warn(RunTestsAndGenSpectra.class, "Class file '%s' will not be loaded.", binaryPath);
//				}
//			}
////			else {
////				Log.err(RunTestsAndGenSpectra.class, "Path '%s' does not exist and will be ignored.", binaryPath);
////			}
//		}
//		
//		if (testClassDir != null) {
//			loadPrivateInnerClassesInDirectory(testClassDir, testClassLoader, initialize);
//		}
//        
//		Thread.currentThread().setContextClassLoader(normalClassLoader);
//	}
//	
//	private static void loadPrivateInnerClassesInDirectory(Path directory, ClassLoader classLoader, boolean initialize) {
//		List<Path> instrumentedClassFiles = new SearchFileOrDirToListProcessor("**.class", true)
//				.searchForFiles().submit(directory).getResult();
//		for (Path instrumentedClassFile : instrumentedClassFiles) {
////			Log.out(RunTestsAndGenSpectra.class, "%s'", instrumentedClassFile);
//			String relativePath = directory.relativize(instrumentedClassFile.toAbsolutePath()).toString();
////			Log.out(RunTestsAndGenSpectra.class, "%s'", relativePath);			
//			int pos = relativePath.indexOf('$');
//			if (pos != -1) {
//				String outerClassName = relativePath.substring(0, pos).replace(File.separatorChar, '.');
//				String innerClassName = relativePath.substring(0, relativePath.length() - 6).replace(File.separatorChar, '.');
//				try {
//					Class<?> outerClazz = Class.forName(outerClassName, initialize, classLoader);
//					Class<?> innerClazz = Class.forName(innerClassName, initialize, classLoader);
//					
//					innerClazz.
//
//					// constructor of inner class as first argument need instance of
//					// Outer class, so we need to select such constructor
//					Constructor<?> constructor = null;
//					try {
//						constructor = innerClazz.getDeclaredConstructor(outerClazz);
//					} catch (NoSuchMethodException e) {
//						Log.err(RunTestsAndGenSpectra.class, "Class '%s' has no declared constructor.", innerClassName);
//					} catch (SecurityException e) {
//						Log.err(RunTestsAndGenSpectra.class, e, "Class '%s': Constructor could not be accessed.", outerClassName);
//					}
//					
////					if (constructor == null) {
////						try {
////							constructor = innerClazz.getConstructor(outerClazz);
////						} catch (NoSuchMethodException e) {
////							Log.err(RunTestsAndGenSpectra.class, "Class '%s' has no constructor.", innerClassName);
////						} catch (SecurityException e) {
////							Log.err(RunTestsAndGenSpectra.class, e, "Class '%s': Constructor could not be accessed.", outerClassName);
////						}
////					}
//					
//					if (constructor == null) {
//						try {
//							Constructor<?>[] declaredConstructors = innerClazz.getDeclaredConstructors();
//							if (declaredConstructors.length > 0) {
//								constructor = declaredConstructors[0];
//							}
//						} catch (SecurityException e) {
//							Log.err(RunTestsAndGenSpectra.class, e, "Class '%s': Constructors could not be accessed.", outerClassName);
//						}
//					}
//					
//					if (constructor != null) {
//						//we need to make constructor accessible 
//						constructor.setAccessible(true);
//					}
//				} catch (ClassNotFoundException e) {
//					Log.err(RunTestsAndGenSpectra.class, "Class '%s' not found.", innerClassName);
//				} catch (Error e) {
//					Log.err(RunTestsAndGenSpectra.class, e, "Class '%s' could not be loaded.", innerClassName);
//				}
//			}
//		}
//	}

//	private static void preLoadClasses(Path instrumentedDir, String[] pathsToBinaries, Path testClassDir,
//			ClassLoader testClassLoader, boolean initialize) {
//		ClassLoader normalClassLoader = Thread.currentThread().getContextClassLoader();
//		Thread.currentThread().setContextClassLoader(testClassLoader);
//		
//		if (instrumentedDir != null) {
//			loadClassesInDirectory(instrumentedDir, testClassLoader, initialize);
//		}
//		
//		for (String binaryPathString : pathsToBinaries) {
//			Path binaryPath = Paths.get(binaryPathString).toAbsolutePath();
//			if (binaryPath.toFile().exists()) {
//				if (binaryPath.toFile().isDirectory()) {
//					loadClassesInDirectory(binaryPath, testClassLoader, initialize);
//				} else {
//					Log.warn(RunTestsAndGenSpectra.class, "Class file '%s' will not be loaded.", binaryPath);
//				}
//			}
////			else {
////				Log.err(RunTestsAndGenSpectra.class, "Path '%s' does not exist and will be ignored.", binaryPath);
////			}
//		}
//		
//		if (testClassDir != null) {
//			loadClassesInDirectory(testClassDir, testClassLoader, initialize);
//		}
//        
//		Thread.currentThread().setContextClassLoader(normalClassLoader);
//	}
//
//	private static void loadClassesInDirectory(Path directory, ClassLoader classLoader, boolean initialize) {
//		List<Path> instrumentedClassFiles = new SearchFileOrDirToListProcessor("**.class", true)
//				.searchForFiles().submit(directory).getResult();
//		for (Path instrumentedClassFile : instrumentedClassFiles) {
////			Log.out(RunTestsAndGenSpectra.class, "%s'", instrumentedClassFile);
//			String relativePath = directory.relativize(instrumentedClassFile.toAbsolutePath()).toString();
////			Log.out(RunTestsAndGenSpectra.class, "%s'", relativePath);
//			String className = relativePath.substring(0, relativePath.length() - 6).replace(File.separatorChar, '.');
//			try {
//				Class.forName(className, initialize, classLoader);
//			} catch (ClassNotFoundException e) {
//				Log.err(RunTestsAndGenSpectra.class, "Class '%s' not found.", className);
//			} catch (Error e) {
//				Log.err(RunTestsAndGenSpectra.class, e, "Class '%s' could not be loaded.", className);
//			}
//		}
//	}
	
//	private static boolean hasTestMethods(Class<?> klass) {
//        Method[] methods = klass.getMethods();
//        for(Method m:methods) {
//            if (m.isAnnotationPresent(org.junit.Test.class)) {
//                return true;
//            }
//        }
//        return false;
//    }
	


//	private String getFullClassName(String classFileName) throws IOException {           
//		File file = new File(classFileName);
//
//		FileChannel roChannel = new RandomAccessFile(file, "r").getChannel(); 
//		ByteBuffer bb = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int)roChannel.size());         
//
//		Class<?> clazz = getClass().getClassLoader().defineClass((String)null, bb, (ProtectionDomain)null);
//		return clazz.getName();
//	}

}
