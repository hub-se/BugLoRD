package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ArgumentsBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Add coverage instrumentation to existing classes.
 * </p>
 *
 * <h3>What does that mean, exactly?</h3>
 * <p>
 * It means Cobertura will look at each class you give it.  It
 * loads the bytecode into memory.  For each line of source,
 * Cobertura adds a few extra instructions.  These instructions
 * do the following:
 * </p>
 *
 * <ol>
 * <li>Get an instance of the ProjectData class.</li>
 * <li>Call a method in this ProjectData class that increments
 * a counter for this line of code.
 * </ol>
 */
@Deprecated
public class InstrumentMain {
    private static final LoggerWrapper logger = new LoggerWrapper();
    public static URLClassLoader urlClassLoader;

    public static int instrument(String[] args) {
        Header.print(System.out);

        long startTime = System.currentTimeMillis();

        try {
            args = CommandLineBuilder.preprocessCommandLineArguments(args);
        } catch (Exception ex) {
            System.err.println("Error: Cannot process arguments: "
                    + ex.getMessage());
            return 1;
        }
        try {
            new Cobertura(createArgumentsFromCMDParams(args).build())
                    .instrumentCode().saveProjectData();
        } catch (Throwable throwable) {
            System.err.println(String.format(
                    "Failed while instrumenting code: %s", throwable
                            .getMessage()));
            throwable.printStackTrace();
            // This should probably return 1, but the old code didn't exit
            // here, so we won't either...
        }

        long stopTime = System.currentTimeMillis();
        logger.info("Instrument time: " + (stopTime - startTime) + "ms");
        return 0;
    }

    public static void main(String[] args) {
        int returnValue = instrument(args);
        if (returnValue != 0) {
            System.exit(returnValue);
        }
    }

    private static ArgumentsBuilder createArgumentsFromCMDParams(String[] args) {
        ArgumentsBuilder builder = new ArgumentsBuilder();

        // Parse parameters
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--basedir":
                    String baseDir = args[++i];
                    builder.setBaseDirectory(baseDir);
                    break;
                case "--datafile":
                    builder.setDataFile(args[++i]);
                    break;
                case "--destination":
                    builder.setDestinationDirectory(args[++i]);
                    break;
                case "--ignore":
                    builder.addIgnoreRegex(args[++i]);
                    break;
                case "--ignoreMethodAnnotation":
                    builder.addIgnoreMethodAnnotation(args[++i]);
                    break;
                case "--ignoreClassAnnotation":
                    builder.addIgnoreClassAnnotation(args[++i]);
                    break;
                case "--ignoreTrivial":
                    builder.ignoreTrivial(true);
                    break;
                case "--collectExecutionTraces":
                    builder.collectExecutionTraces(true);
                    break;
                case "--includeClasses":
                    builder.addIncludeClassesRegex(args[++i]);
                    break;
                case "--excludeClasses":
                    builder.addExcludeClassesRegex(args[++i]);
                    break;
                case "--failOnError":
                    builder.failOnError(true);
                    logger.setFailOnError(true);
                    break;
                case "--threadsafeRigorous":
                    builder.threadsafeRigorous(true);
                    break;
                case "--auxClasspath":
                    addElementsToJVM(args[++i]);
                    break;
                case "--listOfFilesToInstrument":
                    builder.listOfFilesToInstrument(args[++i]);
                    break;
                default:
                    builder.addFileToInstrument(args[i]);
                    break;
            }
        }
        return builder;
    }

    private static void addElementsToJVM(String classpath) {
        List<URL> urlsArray = new ArrayList<>();
        String[] classpathParsed = classpath.split(File.pathSeparator);

        for (String element : classpathParsed) {
            File f = null;
            try {
                f = new File(element);
                urlsArray.add(f.toURI().toURL());
            } catch (MalformedURLException e) {
                logger.debug("Warning - could not convert file: " + element
                        + " to a URL.", e);
            }
        }
        urlClassLoader = new URLClassLoader(urlsArray.toArray(new URL[0]));
    }

    // TODO: Preserved current behaviour, but this code is failing on WARN, not error
    private static class LoggerWrapper {
        private final Logger logger = LoggerFactory
                .getLogger(InstrumentMain.class);
        private boolean failOnError = false;

        public void setFailOnError(boolean failOnError) {
            this.failOnError = failOnError;
        }

        @SuppressWarnings("unused")
        public void debug(String message) {
            logger.debug(message);
        }

        public void debug(String message, Throwable t) {
            logger.debug(message, t);
        }

        public void info(String message) {
            logger.debug(message);
        }

        @SuppressWarnings("unused")
        public void warn(String message, Throwable t) {
            logger.warn(message, t);
            if (failOnError) {
                throw new RuntimeException(
                        "Warning detected and failOnError is true", t);
            }
        }
    }
}