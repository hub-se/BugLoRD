package test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.jacoco.core.runtime.AgentOptions;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import se.de.hu_berlin.informatik.sbfl.RunTestsAndGenSpectra;
import se.de.hu_berlin.informatik.sbfl.StatisticsData;
import se.de.hu_berlin.informatik.sbfl.TestStatistics;
import se.de.hu_berlin.informatik.sbfl.TestWrapper;
import se.de.hu_berlin.informatik.sbfl.RunTestsAndGenSpectra.CmdOptions;
import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.JaCoCoToSpectra;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.files.processors.SearchFileOrDirToListProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.ParentLastClassLoader;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.processors.AbstractConsumingProcessor;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

public class TestRunTestAndGenSpectraProcessor extends AbstractConsumingProcessor<OptionParser> {
	
	protected static final boolean USE_TEST_ADAPTER = true;
	private static final boolean TEST_DEBUG_OUTPUT = true;

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
		
		String testClassPath = options.hasOption(CmdOptions.TEST_CLASS_PATH) ? options.getOptionValue(CmdOptions.TEST_CLASS_PATH) : null;
		
		// assemble the necessary directories for running the tests
		List<URL> cpURLs = new ArrayList<>();
		
		Path instrumentedDir = getPathAndAddToURLs(options.getOptionValue(CmdOptions.INSTRUMENTED_DIR), cpURLs);
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
		
		// exclude junit classes to be able to extract the tests
		ClassLoader testClassLoader = 
				new ParentLastClassLoader(cpURLs, false
						, "junit.runner", "junit.framework", "org.junit"
//						, "org.hamcrest", "java.lang", "java.util"
						);
		
		
		Project project = new Project();
		JUnitTask junitTask = (JUnitTask) project.createTask("org.apache.tools.ant.taskdefs.optional.junit.JUnitTask");
		try {
			junitTask = new JUnitTask();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		junitTask.setProject(project);
		String[] methods = { "org.mockitousage.bugs.CaptorAnnotationAutoboxingTest::shouldAutoboxSafely" };
		JUnitTest test = new JUnitTest("org.mockitousage.bugs.CaptorAnnotationAutoboxingTest", true, true, false, methods);
		test.addFormatter(new FormatterElement());
		junitTask.addTest(test);
		junitTask.execute();
		
		Log.out(this, "%d, %d", test.errorCount(), test.failureCount());
		
//		ClassLoader testClassLoader = ClassLoaders.excludingClassLoader()
//				.withCodeSourceUrls(cpURLs)
//                .without("junit", "org.junit")
//                .build();
//		Thread.currentThread().setContextClassLoader(testClassLoader);
		
//		preLoadClasses(instrumentedDir, pathsToBinaries, testClassDir, testClassLoader, false);
		
//		preLoadClasses(instrumentedDir, pathsToBinaries, testClassDir, testClassLoader, true);
		
//		preLoadPrivateInnerClasses(instrumentedDir, pathsToBinaries, testClassDir, testClassLoader, true);
//		preLoadPrivateInnerClasses(null, null, testClassDir, testClassLoader, true);
//		
//		preLoadClasses(instrumentedDir, pathsToBinaries, testClassDir, Thread.currentThread().getContextClassLoader(), true);
		
//		Log.out(RunTestsAndGenSpectra.class, Misc.listToString(cpURLs));
		
		String className = "org.mockitousage.bugs.CaptorAnnotationAutoboxingTest";
		
		String testMethodName = "init";
		
		TestWrapper testWrapper = null;
//		try {
//			//TODO what happens, if a test class tries to load some instrumented class that was not loaded before?...
//			// current procedure: pre-load all instrumented classes, etc....
//			Class<?> testClazz = Class.forName(className, true, testClassLoader);
////			Class<?> testClazz = Class.forName(className);
//
//			testWrapper = new TestWrapper(testClassLoader, testClazz, testMethodName);
//		} catch (ClassNotFoundException e) {
//			Log.err(this, "Class '%s' not found.", className);
//		}
//		
//		runTest(testWrapper, outputDir + File.separatorChar + "result.txt", null);
		
		
//		testMethodName = "shouldAutoboxSafely";
//		
//		testWrapper = null;
//		try {
//			//TODO what happens, if a test class tries to load some instrumented class that was not loaded before?...
//			// current procedure: pre-load all instrumented classes, etc....
//			Class<?> testClazz = Class.forName(className, true, testClassLoader);
////			Class<?> testClazz = Class.forName(className);
//
//			testWrapper = new TestWrapper(testClassLoader, testClazz, testMethodName);
//		} catch (ClassNotFoundException e) {
//			Log.err(this, "Class '%s' not found.", className);
//		}
//		
//		runTest(testWrapper, outputDir + File.separatorChar + "result.txt", null);
		
	}
	
	private TestStatistics runTest(final TestWrapper testWrapper, final String resultFile, final Long timeout) {
		long startingTime = System.currentTimeMillis();
//		Log.out(this, "Start Running " + testWrapper);

		FutureTask<Result> task = testWrapper.getTest();
		
		Result result = null;
		boolean timeoutOccured = false, wasInterrupted = false, exceptionThrown = false;
		boolean couldBeFinished = true;
		String errorMsg = null;
		try {
			if (task == null) {
				throw new ExecutionException("Could not get test from TestWrapper (null).", null);
			}
			new Thread(task).run();
			
			if (timeout == null) {
				result = task.get();
			} else {
				result = task.get(timeout, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			errorMsg = testWrapper + ": Test execution interrupted!";
			wasInterrupted = true;
			couldBeFinished = false;
			cancelTask(task);
		} catch (ExecutionException | CancellationException e) {
			if (e.getCause() != null) {
				errorMsg = testWrapper + ": Test execution exception! -> " + e.getCause();
				e.getCause().printStackTrace();
			} else {
				errorMsg = testWrapper + ": Test execution exception!";
			}
			Log.err(this, e, errorMsg);
			exceptionThrown = true;
			couldBeFinished = false;
			if (task != null) {
				cancelTask(task);
			}
		} catch (TimeoutException e) {
			errorMsg = testWrapper + ": Time out! ";
			timeoutOccured = true;
			couldBeFinished = false;
			cancelTask(task);
		}
		if (result != null) {
			if (result.wasSuccessful()) {
				Log.out(this, "was successful!");
			} else {
				Log.out(this, "was not successful!");
				for (final Failure f : result.getFailures()) {
					Log.err(this, f.getException(), f.toString());
				}
			}
		}

		if (resultFile != null) {
			final StringBuilder buff = new StringBuilder();
			if (result == null) {
				if (timeoutOccured) {
					buff.append(testWrapper + " TIMEOUT!!!" + System.lineSeparator());
				} else if (wasInterrupted) {
					buff.append(testWrapper + " INTERRUPTED!!!" + System.lineSeparator());
				} else if (exceptionThrown) {
					buff.append(testWrapper + " EXECUTION EXCEPTION!!!" + System.lineSeparator());
				}
			} else if (!result.wasSuccessful()) {
				buff.append("#ignored:" + result.getIgnoreCount() + ", " + "FAILED!!!" + System.lineSeparator());
				for (final Failure f : result.getFailures()) {
					buff.append(f.toString() + System.lineSeparator());
				}
			}
			
			if (buff.length() != 0) {
				final File out = new File(resultFile);
				try {
					FileUtils.writeString2File(buff.toString(), out);
				} catch (IOException e) {
					Log.err(this, e, "IOException while trying to write to file '%s'", out);
				}
			}
		}

		long endingTime = System.currentTimeMillis();
//		Misc.writeString2File(Long.toString(endingTime - startingTime),
//				new File(resultFile.substring(0, resultFile.lastIndexOf('.')) + ".runtime"));
		
		long duration = (endingTime - startingTime);
		if (result == null) {
			return new TestStatistics(duration, false, timeoutOccured, 
					exceptionThrown, wasInterrupted, false, errorMsg);
		} else {
			if (result.getIgnoreCount() > 0) {
				couldBeFinished = false;
			}
			return new TestStatistics(duration, result.wasSuccessful(), 
					timeoutOccured, exceptionThrown, wasInterrupted, couldBeFinished, errorMsg);
		}
	}
	
	private void cancelTask(FutureTask<Result> task) {
		while(!task.isDone())
			task.cancel(false);
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
	
	private static void preLoadPrivateInnerClasses(Path instrumentedDir, String[] pathsToBinaries, Path testClassDir,
			ClassLoader testClassLoader, boolean initialize) {
		ClassLoader normalClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(testClassLoader);
		
		if (instrumentedDir != null) {
			loadPrivateInnerClassesInDirectory(instrumentedDir, testClassLoader, initialize);
		}
		
		if (pathsToBinaries != null) {
		for (String binaryPathString : pathsToBinaries) {
			Path binaryPath = Paths.get(binaryPathString).toAbsolutePath();
			if (binaryPath.toFile().exists()) {
				if (binaryPath.toFile().isDirectory()) {
					loadPrivateInnerClassesInDirectory(binaryPath, testClassLoader, initialize);
				} else {
					Log.warn(RunTestsAndGenSpectra.class, "Class file '%s' will not be loaded.", binaryPath);
				}
			}
//			else {
//				Log.err(RunTestsAndGenSpectra.class, "Path '%s' does not exist and will be ignored.", binaryPath);
//			}
		}
		}
		
		if (testClassDir != null) {
			loadPrivateInnerClassesInDirectory(testClassDir, testClassLoader, initialize);
		}
        
		Thread.currentThread().setContextClassLoader(normalClassLoader);
	}
	
	private static void loadPrivateInnerClassesInDirectory(Path directory, ClassLoader classLoader, boolean initialize) {
		List<Path> instrumentedClassFiles = new SearchFileOrDirToListProcessor("**.class", true)
				.searchForFiles().submit(directory).getResult();
		for (Path instrumentedClassFile : instrumentedClassFiles) {
//			Log.out(RunTestsAndGenSpectra.class, "%s'", instrumentedClassFile);
			String relativePath = directory.relativize(instrumentedClassFile.toAbsolutePath()).toString();
			if ("org.mockitousage.bugs.CaptorAnnotationAutoboxingTest$Fun".equals(relativePath.substring(0, relativePath.length() - 6).replace(File.separatorChar, '.'))) {
//			Log.out(RunTestsAndGenSpectra.class, "%s'", relativePath);			
			int pos = relativePath.indexOf('$');
			if (pos != -1) {
				String outerClassName = relativePath.substring(0, pos).replace(File.separatorChar, '.');
				String innerClassName = relativePath.substring(0, relativePath.length() - 6).replace(File.separatorChar, '.');
				try {
					Class<?> outerClazz = Class.forName(outerClassName, initialize, classLoader);
					Class<?> innerClazz = Class.forName(innerClassName, initialize, classLoader);
					

					// constructor of inner class as first argument need instance of
					// Outer class, so we need to select such constructor
					Constructor<?> constructor = null;
					try {
						constructor = innerClazz.getDeclaredConstructor(outerClazz);
					} catch (NoSuchMethodException e) {
						Log.err(RunTestsAndGenSpectra.class, "Class '%s' has no declared constructor.", innerClassName);
					} catch (SecurityException e) {
						Log.err(RunTestsAndGenSpectra.class, e, "Class '%s': Constructor could not be accessed.", outerClassName);
					}
					
					if (constructor == null) {
						try {
							constructor = innerClazz.getConstructor(outerClazz);
						} catch (NoSuchMethodException e) {
							Log.err(RunTestsAndGenSpectra.class, "Class '%s' has no constructor.", innerClassName);
						} catch (SecurityException e) {
							Log.err(RunTestsAndGenSpectra.class, e, "Class '%s': Constructor could not be accessed.", outerClassName);
						}
					}
					
					if (constructor == null) {
						try {
							Constructor<?>[] declaredConstructors = innerClazz.getDeclaredConstructors();
							if (declaredConstructors.length > 0) {
								constructor = declaredConstructors[0];
							} else {
								Log.err(RunTestsAndGenSpectra.class, "Class '%s' has no declared constructors.", innerClassName);
							}
						} catch (SecurityException e) {
							Log.err(RunTestsAndGenSpectra.class, e, "Class '%s': Constructors could not be accessed.", outerClassName);
						}
					}
					
					if (constructor != null) {
						//we need to make constructor accessible 
						constructor.setAccessible(true);
					}
				} catch (ClassNotFoundException e) {
					Log.err(RunTestsAndGenSpectra.class, "Class '%s' not found.", innerClassName);
				} catch (Error e) {
					Log.err(RunTestsAndGenSpectra.class, e, "Class '%s' could not be loaded.", innerClassName);
				}
			}
			}
		}
	}

	private static void preLoadClasses(Path instrumentedDir, String[] pathsToBinaries, Path testClassDir,
			ClassLoader testClassLoader, boolean initialize) {
		ClassLoader normalClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(testClassLoader);
		
		if (instrumentedDir != null) {
			loadClassesInDirectory(instrumentedDir, testClassLoader, initialize);
		}
		
		for (String binaryPathString : pathsToBinaries) {
			Path binaryPath = Paths.get(binaryPathString).toAbsolutePath();
			if (binaryPath.toFile().exists()) {
				if (binaryPath.toFile().isDirectory()) {
					loadClassesInDirectory(binaryPath, testClassLoader, initialize);
				} else {
					Log.warn(RunTestsAndGenSpectra.class, "Class file '%s' will not be loaded.", binaryPath);
				}
			}
//			else {
//				Log.err(RunTestsAndGenSpectra.class, "Path '%s' does not exist and will be ignored.", binaryPath);
//			}
		}
		
		if (testClassDir != null) {
			loadClassesInDirectory(testClassDir, testClassLoader, initialize);
		}
        
		Thread.currentThread().setContextClassLoader(normalClassLoader);
	}

	private static void loadClassesInDirectory(Path directory, ClassLoader classLoader, boolean initialize) {
		List<Path> instrumentedClassFiles = new SearchFileOrDirToListProcessor("**.class", true)
				.searchForFiles().submit(directory).getResult();
		for (Path instrumentedClassFile : instrumentedClassFiles) {
//			Log.out(RunTestsAndGenSpectra.class, "%s'", instrumentedClassFile);
			String relativePath = directory.relativize(instrumentedClassFile.toAbsolutePath()).toString();
//			Log.out(RunTestsAndGenSpectra.class, "%s'", relativePath);
			String className = relativePath.substring(0, relativePath.length() - 6).replace(File.separatorChar, '.');
			try {
				Class.forName(className, initialize, classLoader);
			} catch (ClassNotFoundException e) {
				Log.err(RunTestsAndGenSpectra.class, "Class '%s' not found.", className);
			} catch (Error e) {
				Log.err(RunTestsAndGenSpectra.class, e, "Class '%s' could not be loaded.", className);
			}
		}
	}
	
	private static boolean hasTestMethods(Class<?> klass) {
        Method[] methods = klass.getMethods();
        for(Method m:methods) {
            if (m.isAnnotationPresent(org.junit.Test.class)) {
                return true;
            }
        }
        return false;
    }
	


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
