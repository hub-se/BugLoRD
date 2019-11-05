package se.de.hu_berlin.informatik.gen.spectra.internal;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.gen.spectra.AbstractSpectraGenerationFactory;
import se.de.hu_berlin.informatik.gen.spectra.internal.RunAllTestsAndGenSpectra.CmdOptions;
import se.de.hu_berlin.informatik.gen.spectra.main.JaCoCoSpectraGenerator;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.testlister.mining.TestMinerProcessor;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.FileLineProcessor;
import se.de.hu_berlin.informatik.utils.files.processors.FileLineProcessor.StringProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.ParentLastClassLoader;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;
import se.de.hu_berlin.informatik.utils.processors.basics.StringsToFileWriter;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

public class RunTestsAndGenSpectraProcessor<T extends Serializable,R,S> extends AbstractConsumingProcessor<OptionParser> {

	public static final boolean TEST_DEBUG_OUTPUT = true;
	private final AbstractSpectraGenerationFactory<T, R, S> factory;
	
	
	public RunTestsAndGenSpectraProcessor(AbstractSpectraGenerationFactory<T,R,S> factory) {
		Objects.requireNonNull(factory, "Factory is null.");
		this.factory = factory;
	}

	@Override
	public void consumeItem(OptionParser options) throws UnsupportedOperationException {

		String[] pathsToBinaries = options.getOptionValues(CmdOptions.ORIGINAL_CLASSES_DIRS);
		
//		Log.out(RunTestsAndGenSpectra.class, "Project dir: '%s'.", Paths.get(options.getOptionValue(CmdOptions.PROJECT_DIR)));

		final String outputDir = options.isDirectory(CmdOptions.OUTPUT, false).toString();

		final StatisticsCollector<StatisticsData> statisticsContainer = new StatisticsCollector<>(StatisticsData.class);
		
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
					Log.err(RunAllTestsAndGenSpectra.class, e, "Could not parse URL from '%s'.", cpElement);
				}
//				break;
			}
		}
		
		List<URL> systemClassPathElements = new ClassPathParser().parseSystemClasspath().getUniqueClasspathElements();
		List<URL> testRelatedElements = new ArrayList<>();
		for (URL url : systemClassPathElements) {
			String path = url.getPath().toLowerCase();
//			Log.out(this, "in classpath: %s", path);
			// TODO: this is tool-specific...
			if (path.contains("ant-junit") || path.contains("junit-4.12") 
					|| path.contains("tracecobertura") || path.contains("trace-cobertura")
					) {
				Log.out(this, "added '%s' to start of classpath.", path);
				testRelatedElements.add(url);
			}
		}
		
		ClassPathParser classPathParser = new ClassPathParser();
		// TODO: check if necessary..
//		for (URL url : testRelatedElements) {
//			classPathParser.addElementToClassPath(url);
//		}
		for (URL url : cpURLs) {
//			String path = url.getPath().toLowerCase();
//			Log.out(this, "in cpURLs: %s", path);
			classPathParser.addElementToClassPath(url);
		}
		String changedTestClassPath = classPathParser.getClasspath();
		
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
		// collect the tests
		if (options.hasOption(CmdOptions.TEST_CLASS_LIST)) { //has option "tc"
			testFile = options.isFile(CmdOptions.TEST_CLASS_LIST, true);
			
			Path testOutputFile = Paths.get(outputDir, "minedTests.txt");
			linker.append(
                    new FileLineProcessor<>(new StringProcessor<String>() {
                        private final Set<String> seenClasses = new HashSet<>();
                        private String clazz = null;

                        @Override
                        public boolean process(String clazz) {
                            // only consider top-level classes
                            // child classes will be searched for tests anyway
                            int pos = clazz.indexOf('$');
                            if (pos != -1) {
                                clazz = clazz.substring(0, pos);
                            }
                            // ignore duplicates
                            if (!seenClasses.contains(clazz)) {
                                seenClasses.add(clazz);
                                this.clazz = clazz;
                            } else {
                                this.clazz = null;
                            }
                            return true;
                        }

                        @Override
                        public String getLineResult() {
                            String temp = clazz;
                            clazz = null;
                            return temp;
                        }
                    }),
					new TestMinerProcessor(testClassLoader, false),
					new StringsToFileWriter<TestWrapper>(testOutputFile , true));
		} else { //has option "t"
			testFile = options.isFile(CmdOptions.TEST_LIST, true);
			
			linker.append(
                    new FileLineProcessor<>(testClassLoader, new StringProcessor<TestWrapper>() {
                        private TestWrapper testWrapper;

                        @Override
                        public boolean process(String testNameAndClass) {
                        	if (testNameAndClass.startsWith("#")) {
                        		return false;
                        	}
                            //format: test.class::testName
                            final String[] test = testNameAndClass.split("::");
                            if (test.length != 2) {
                                Log.err(JaCoCoSpectraGenerator.class, "Wrong test identifier format: '" + testNameAndClass + "'.");
                                return false;
                            } else {
                                testWrapper = new TestWrapper(test[0], test[1], testClassLoader);
                            }
                            return true;
                        }

                        @Override
                        public TestWrapper getLineResult() {
                            TestWrapper temp = testWrapper;
                            testWrapper = null;
                            return temp;
                        }
                    }));
		}
		
		// need a special class loader to run the tests...
		ClassLoader testAndInstrumentClassLoader = testClassLoader;
		
		// run tests and collect reports based on used coverage tool
		linker.append(
				factory.getTestRunnerModule(options, testAndInstrumentClassLoader, changedTestClassPath, statisticsContainer)
//				.asPipe(instrumentedClassesLoader)
				.asPipe().enableTracking().allowOnlyForcedTracks(),
				factory.getReportToSpectraProcessor(options, statisticsContainer),
				// save the resulting spectra + reduced/filtered spectra
				factory.getSpectraProcessor(options))
		.submitAndShutdown(testFile);
		
		// print some statistics and stuff...
		EnumSet<StatisticsData> stringDataEnum = EnumSet.noneOf(StatisticsData.class);
		stringDataEnum.add(StatisticsData.ERROR_MSG);
		stringDataEnum.add(StatisticsData.FAILED_TEST_COVERAGE);
		String statsWithoutStringData = statisticsContainer.printStatistics(EnumSet.complementOf(stringDataEnum));
		
		Log.out(this, statsWithoutStringData);
		
		String stats = statisticsContainer.printStatistics(stringDataEnum);
		try {
			FileUtils.writeStrings2File(Paths.get(outputDir, testFile.getFileName() + "_stats").toFile(), statsWithoutStringData, stats);
		} catch (IOException e) {
			Log.err(JaCoCoSpectraGenerator.class, "Can not write statistics to '%s'.", Paths.get(outputDir, testFile.getFileName() + "_stats"));
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
					Log.err(RunAllTestsAndGenSpectra.class, "Path '%s' does not exist and will not be added to the class loader.", path);
				}
			} catch (MalformedURLException e) {
				Log.err(RunAllTestsAndGenSpectra.class, e, "Could not parse URL from '%s'.", stringPath);
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
