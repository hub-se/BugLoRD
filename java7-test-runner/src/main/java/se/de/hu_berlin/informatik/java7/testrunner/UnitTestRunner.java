package se.de.hu_berlin.informatik.java7.testrunner;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.TouchCollector;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Runs a single Test.
 *
 * @author Simon
 */
public class UnitTestRunner {

    final public static int TEST_SUCCESSFUL = 0;
    final public static int TEST_FAILED = 1;
    final public static int TEST_TIMEOUT = 2;
    final public static int TEST_EXCEPTION = 3;

    final private static ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();

    /**
     * @param args test.Class testMethod path/to/outputFile.csv port [timeout]
     */
    public static void main(String[] args) {

        if (args.length < 4) {
            System.err.println("Wrong number of arguments.");
            System.exit(1);
        }

        String testClass = args[0];
        String testMethod = args[1];

        Path output = Paths.get(args[2]);
        if (output.toFile().isDirectory()) {
            System.err.println(output + " is a directory.");
            System.exit(1);
        }

        int port = Integer.valueOf(args[3]);

        long timeout = 600L;
        if (args.length > 4) {
            timeout = Long.parseLong(args[4]);
        }

        if (System.getProperty("net.sourceforge.cobertura.datafile") != null) {
            // initialize!
            ProjectData.getGlobalProjectData();
            //turn off auto saving (removes the shutdown hook inside of Cobertura)
            ProjectData.turnOffAutoSave();
            // reset hits, if any class was already registered (should not be the case, actually)
            TouchCollector.resetTouchesOnRegisteredClasses();
        }

        TestWrapper testWrapper = new TestWrapper(testClass, testMethod);
        int result = runTest(testWrapper, output.getParent() + File.separator + testWrapper.toString().replace(':', '_'), timeout);

        if (System.getProperty("net.sourceforge.cobertura.datafile") != null) {
            ProjectData projectData = null;
            //see if the test was executed and finished execution normally
            if (result == TEST_SUCCESSFUL || result == TEST_FAILED) {
                // wait for some milliseconds
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // do nothing
                }
                projectData = new ProjectData();

                TouchCollector.applyTouchesOnProjectData(projectData);
            }

            // only send if not null...
            boolean successful = projectData != null
                    && SimpleServerFramework.sendToServer(projectData, port, 3);

            //result.saveToCSV(output);
            //ProjectData.saveGlobalProjectData();
            if (!successful) {
                Runtime.getRuntime().exit(TEST_EXCEPTION);
            }
        }

        System.exit(result);
    }

    private static int runTest(final TestWrapper testWrapper, final String resultFile, final Long timeout) {
//		Log.out(this, "Start Running " + testWrapper);

        FutureTask<JUnitTest> task = testWrapper.getTest(testOutputStream);
        int executionResult = TEST_EXCEPTION;

        JUnitTest test = null;
        boolean timeoutOccured = false, wasInterrupted = false, exceptionThrown = false;
        boolean couldBeFinished = false;
        String errorMsg = null;
        Set<Thread> threadSetStart = Thread.getAllStackTraces().keySet();
        try {
            if (task == null) {
                throw new ExecutionException("Could not get test from TestWrapper (null).", null);
            }
            if (timeout != null && timeout <= 0) {
                throw new TimeoutException();
            }

            Thread thread = new Thread(task);
            thread.start();

            if (timeout == null) {
                test = task.get();
            } else {
                test = task.get(timeout, TimeUnit.SECONDS);
            }
            couldBeFinished = true;
        } catch (InterruptedException e) {
            errorMsg = testWrapper + ": Test execution interrupted!";
            wasInterrupted = true;
            cancelTask(task);
        } catch (ExecutionException | CancellationException e) {
            if (e.getCause() != null) {
                errorMsg = testWrapper + ": Test execution exception! -> " + e.getCause();
                e.getCause().printStackTrace();
            } else {
                errorMsg = testWrapper + ": Test execution exception!";
            }
            exceptionThrown = true;
            if (task != null) {
                cancelTask(task);
            }
        } catch (TimeoutException e) {
            executionResult = TEST_TIMEOUT;
            errorMsg = testWrapper + ": Time out! ";
            timeoutOccured = true;
            cancelTask(task);
        }

        Set<Thread> threadSetEnd = Thread.getAllStackTraces().keySet();

        for (Thread thread : threadSetEnd) {
            if (!threadSetStart.contains(thread)) {
                try {
                    thread.join(60000);
                } catch (InterruptedException e) {
                    // meh
                }
                if (thread.isAlive()) {
                    System.err.println("Thread " + thread.getId() + " is still alive after running the test " + testWrapper.toString());
                    //					thread.interrupt();
                    break;
                }
            }
        }

        boolean wasSuccessful = false;
        if (test != null) {
            //boolean timeoutOccured = test.runCount() == 0 && test.errorCount() == 0 && test.failureCount() == 0 && test.skipCount() == 0;
            //boolean errorOccured = test.errorCount() > 0;
            //couldBeFinished = test.runCount() > 0 && test.skipCount() == 0;

            wasSuccessful = couldBeFinished
                    && !timeoutOccured && !wasInterrupted && !exceptionThrown
                    && test.failureCount() == 0 && test.errorCount() == 0;

            if (couldBeFinished) {
                if (wasSuccessful) {
                    executionResult = TEST_SUCCESSFUL;
                } else {
                    executionResult = TEST_FAILED;
                }
            }
        }

        if (resultFile != null) {
            final StringBuilder buff = new StringBuilder();
            if (test == null) {
                if (errorMsg != null) {
                    buff.append(errorMsg).append(System.lineSeparator());
                }
            } else if (!wasSuccessful) {
                buff.append("#ignored:").append(test.skipCount()).append(", ").append("FAILED!!!").append(System.lineSeparator());
                buff.append(testOutputStream.toString());
//				for (final Failure f : result.getFailures()) {
//					buff.append(f.toString() + System.lineSeparator());
//				}
            }

            if (buff.length() != 0) {
                final File out = new File(resultFile);
                try {
                    writeString2File(buff.toString(), out);
                } catch (IOException e) {
                    System.err.println("IOException while trying to write to file '" + out + "'.");
                }
            }
        }

        testOutputStream.reset();

        if (executionResult != TEST_SUCCESSFUL && executionResult != TEST_FAILED) {
            System.err.println("Test execution failed.");
        }

        return executionResult;
    }

    private static void cancelTask(FutureTask<?> task) {
        while (!task.isDone())
            task.cancel(false);
    }

    /**
     * Writes a String to the provided file. If the file does not exist, it will be created.
     *
     * @param string the string to write
     * @param file   the output file
     * @throws IOException if the file is a directory or can not be opened or written to
     */
    private static void writeString2File(final String string, final File file) throws IOException {
        if (!file.exists()) {
            ensureParentDir(file);
            file.createNewFile();
        }
        try (final PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            writer.println(string);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Ensure that the given file has a parent directory. Creates all
     * directories on the way to the parent directory if they not exist.
     *
     * @param file the file
     */
    private static void ensureParentDir(final File file) {
        final File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

}
