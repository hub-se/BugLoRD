package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;


import org.apache.oro.text.regex.Pattern;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoberturaClassWriter;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.DetectDuplicatedCodeClassVisitor;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.DetectIgnoredCodeClassVisitor;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.IOUtil;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.CoberturaStatementEncoding;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Class that is responsible for the whole process of instrumentation of a single class.
 * <p>
 * The class is instrumented in tree passes:
 * <ol>
 * <li>Read only: {@link DetectDuplicatedCodeClassVisitor} - we look for the same ASM code snippets
 * rendered in different places of destination code</li>
 * <li>Read only: {@link BuildClassMapClassVisitor} - finds all touch-points and other interesting
 * information that are in the class and store it in {@link ClassMap}.
 * <li>Real instrumentation: {@link InjectCodeClassInstrumenter}. Uses {#link ClassMap} to inject
 * code into the class</li>
 * </ol>
 *
 * @author piotr.tabor@gmail.com
 */
public class CoberturaInstrumenter {
    private static final Logger logger = LoggerFactory
            .getLogger(CoberturaInstrumenter.class);

    /**
     * During the instrumentation process we are feeling {@link ProjectData}, to generate from
     * it the *.ser file.
     * <p>
     * We now (1.10+) don't need to generate the file (it is not necessery for reporting), but we still
     * do it for backward compatibility (for example maven-cobertura-plugin expects it). We should avoid
     * this some day.
     */
    private ProjectData projectData;

    /**
     * The root directory for instrumented classes. If it is null, the instrumented classes are overwritten.
     */
    private File destinationDirectory;

    /**
     * List of patterns to know that we don't want trace lines that are calls to some methods
     */
    private Collection<org.apache.oro.text.regex.Pattern> ignoreRegexes = new Vector<>();

    /**
     * Methods annotated by this annotations will be ignored during coverage measurement
     */
    private Set<String> ignoreMethodAnnotations = new HashSet<>();

    /**
     * Classes annotated by this annotations will be ignored during
     * instrumentation
     */
    private Set<String> ignoreClassAnnotations = new HashSet<>();

    /**
     * If true: Getters, Setters and simple initialization will be ignored by coverage measurement
     */
    private boolean ignoreTrivial;

    /**
     * If true: The process is interrupted when first error occured.
     */
    private boolean failOnError;

    /**
     * Setting to true causes cobertura to use more strict threadsafe model that is significantly
     * slower, but guarantees that number of hits counted for each line will be precise in multithread-environment.
     * <p>
     * The option does not change measured coverage.
     * <p>
     * In implementation it means that AtomicIntegerArray will be used instead of int[].
     */
    private boolean threadsafeRigorous;

    private final boolean collectExecutionTrace;

    private static int currentClassIndex = -1;

    public CoberturaInstrumenter(boolean collectExecutionTrace) {
        this.collectExecutionTrace = collectExecutionTrace;
    }

    /**
     * Analyzes and instruments class given by path.
     *
     * <p>Also the {@link #projectData} structure is filled with information about the found touch-points</p>
     *
     * @param file                   - path to class that should be instrumented
     * @param statementsToInstrument - set of encoded statements that should actually be part of instrumentation
     * @return instrumentation result structure or null in case of problems
     */
    public InstrumentationResult instrumentClass(File file, Set<Integer> statementsToInstrument) {
        InputStream inputStream = null;
        try {
//			logger.debug("Working on file:" + file.getAbsolutePath());
            inputStream = new FileInputStream(file);
            return instrumentClass(inputStream, statementsToInstrument);
        } catch (Throwable t) {
            logger.warn("Unable to instrument file " + file.getAbsolutePath(),
                    t);
            if (failOnError) {
                throw new RuntimeException(
                        "Warning detected and failOnError is true", t);
            } else {
                return null;
            }
        } finally {
            IOUtil.closeInputStream(inputStream);
        }
    }

    /**
     * Analyzes and instruments class given by inputStream
     *
     * <p>Also the {@link #projectData} structure is filled with information about the found touch-points</p>
     *
     * @param inputStream            - source of class to instrument
     * @param statementsToInstrument - set of encoded statements that should actually be part of instrumentation
     * @return instrumentation result structure or null in case of problems
     * @throws IOException if anything happens
     */
    public InstrumentationResult instrumentClass(InputStream inputStream, Set<Integer> statementsToInstrument)
            throws IOException {
        ClassReader cr0 = new ClassReader(inputStream);
        ClassWriter cw0 = new ClassWriter(0);
        DetectIgnoredCodeClassVisitor detectIgnoredCv = new DetectIgnoredCodeClassVisitor(
                cw0, ignoreTrivial, ignoreMethodAnnotations);
        DetectDuplicatedCodeClassVisitor cv0 = new DetectDuplicatedCodeClassVisitor(
                detectIgnoredCv);
        cr0.accept(cv0, 0);

        ClassReader cr = new ClassReader(cw0.toByteArray());
        ClassWriter cw = new ClassWriter(0);
        int classId = ++currentClassIndex;
        if (classId > Math.pow(2, CoberturaStatementEncoding.CLASS_ID_BITS) - 1) {
            throw new IllegalStateException("Class ID too high! Encoding error: " + classId);
        }
		BuildClassMapClassVisitor cv = new BuildClassMapClassVisitor(cw,
                ignoreRegexes, ignoreClassAnnotations,
                cv0.getDuplicatesLinesCollector(),
                detectIgnoredCv.getIgnoredMethodNamesAndSignatures(),
                classId);

        cr.accept(cv, ClassReader.EXPAND_FRAMES);

//		if (logger.isDebugEnabled()) {
//			logger
//					.debug("=============== Detected duplicated code =============");
//			Map<Integer, Map<Integer, Integer>> l = cv0
//					.getDuplicatesLinesCollector();
//			for (Map.Entry<Integer, Map<Integer, Integer>> m : l.entrySet()) {
//				if (m.getValue() != null) {
//					for (Map.Entry<Integer, Integer> pair : m.getValue()
//							.entrySet()) {
//						logger.debug(cv.getClassMap().getClassName() + ":"
//								+ m.getKey() + " " + pair.getKey() + "->"
//								+ pair.getValue());
//					}
//				}
//			}
//			logger
//					.debug("=============== End of detected duplicated code ======");
//		}

        //TODO(ptab): Don't like the idea, but we have to be compatible (hope to remove the line in future release)
//		logger
//				.debug("Migrating classmap in projectData to store in *.ser file: "
//						+ cv.getClassMap().getClassName());

        ClassData classData = cv.getClassMap().applyOnProjectData(projectData,
                cv.shouldBeInstrumented());

        if (cv.shouldBeInstrumented()) {
            /*
             *  BuildClassMapClassInstrumenter and DetectDuplicatedCodeClassVisitor has not modificated bytecode,
             *  so we can use any bytecode representation of that class.
             */
            ClassReader cr2 = new ClassReader(cw0.toByteArray());
            ClassWriter cw2 = new CoberturaClassWriter(
                    ClassWriter.COMPUTE_FRAMES);
            // assigns counter IDs to touch points
            int[][] counterIDs2LineNumbers = cv.getClassMap().assignCounterIds();
            // set a mapping structure in the class data to map counter IDs to actual line numbers
            classData.setCounterId2LineNumbers(counterIDs2LineNumbers);

//			logger.debug("Assigned " + cv.getClassMap().getMaxCounterId()
//					+ " counters (" + counterIDs2LineNumbers.length + ") to class:" + cv.getClassMap().getClassName());
            InjectCodeClassInstrumenter cv2 = new InjectCodeClassInstrumenter(
                    cw2, ignoreRegexes, threadsafeRigorous, cv.getClassMap(),
                    cv0.getDuplicatesLinesCollector(), detectIgnoredCv
                    .getIgnoredMethodNamesAndSignatures(),
                    statementsToInstrument, collectExecutionTrace);
            cr2.accept(new CheckClassAdapter(cv2), ClassReader.SKIP_FRAMES);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            CheckClassAdapter.verify(new ClassReader(cw2.toByteArray()), false,
                    pw);
//			logger.debug(sw.toString());

            return new InstrumentationResult(cv.getClassMap().getClassName(),
                    cw2.toByteArray());
        } else {
//			logger.debug("Class shouldn't be instrumented: "
//					+ cv.getClassMap().getClassName());
            return null;
        }
    }

    /**
     * Analyzes and instruments class given by file.
     *
     * <p>If the {@link #destinationDirectory} is null, then the file is overwritten,
     * otherwise the class is stored into the {@link #destinationDirectory}</p>
     *
     * <p>Also the {@link #projectData} structure is filled with information about the found touch-points</p>
     *
     * @param file                   - source of class to instrument
     * @param statementsToInstrument - set of encoded statements that should actually be part of instrumentation
     */
    public void addInstrumentationToSingleClass(File file, Set<Integer> statementsToInstrument) {
//		logger.debug("Instrumenting class " + file.getAbsolutePath());

        InstrumentationResult instrumentationResult = instrumentClass(file, statementsToInstrument);
        if (instrumentationResult != null) {
            OutputStream outputStream = null;
            try {
                // If destinationDirectory is null, then overwrite
                // the original, uninstrumented file.
                File outputFile = (destinationDirectory == null)
                        ? file
                        : new File(destinationDirectory,
                        instrumentationResult.className.replace('.',
                                File.separatorChar)
                                + ".class");
//				logger.debug("Writing instrumented class into:"
//						+ outputFile.getAbsolutePath());
                if (destinationDirectory != null && outputFile.exists()) {
                	logger.warn("Instrumented class does already exist: " 
                			+ outputFile.getAbsolutePath());
                }

                File parentFile = outputFile.getParentFile();
                if (parentFile != null) {
                    parentFile.mkdirs();
                }

                outputStream = new FileOutputStream(outputFile);
                outputStream.write(instrumentationResult.content);
            } catch (Throwable t) {
                logger.warn("Unable to write instrumented file "
                        + file.getAbsolutePath(), t);
                return;
            } finally {
                IOUtil.closeOutputStream(outputStream);
            }
        }
    }

    // ----------------- Getters and setters -------------------------------------

    /*
     * Gets the root directory for instrumented classes. If it is null, the instrumented classes are overwritten.
     */
    public File getDestinationDirectory() {
        return destinationDirectory;
    }

    /*
     * Sets the root directory for instrumented classes. If it is null, the instrumented classes are overwritten.
     */
    public void setDestinationDirectory(File destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    /*
     * Gets list of patterns to know that we don't want trace lines that are calls to some methods
     */
    public Collection<Pattern> getIgnoreRegexes() {
        return ignoreRegexes;
    }

    /*
     * Sets list of patterns to know that we don't want trace lines that are calls to some methods
     */
    public void setIgnoreRegexes(Collection<org.apache.oro.text.regex.Pattern> ignoreRegexes) {
        this.ignoreRegexes = ignoreRegexes;
    }

    public void setIgnoreTrivial(boolean ignoreTrivial) {
        this.ignoreTrivial = ignoreTrivial;
    }

    public void setIgnoreMethodAnnotations(Set<String> ignoreMethodAnnotations) {
        this.ignoreMethodAnnotations = ignoreMethodAnnotations;
    }

    public void setIgnoreClassAnnotations(Set<String> ignoreClassAnnotations) {
        this.ignoreClassAnnotations = ignoreClassAnnotations;
    }

    public void setThreadsafeRigorous(boolean threadsafeRigorous) {
        this.threadsafeRigorous = threadsafeRigorous;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /*
     * Sets {@link ProjectData} that will be filled with information about touch points inside instrumented classes
     *
     * @param projectData2 the project data
     */
    public void setProjectData(ProjectData projectData2) {
        this.projectData = projectData2;
    }

    /**
     * Result of instrumentation is a pair of two fields:
     * <ul>
     * <li> {@link #content} - bytecode of the instrumented class
     * <li> {@link #className} - className of class being instrumented
     * </ul>
     */
    public static class InstrumentationResult {
        private final String className;
        private final byte[] content;

        public InstrumentationResult(String className, byte[] content) {
            this.className = className;
            this.content = content;
        }

        public String getClassName() {
            return className;
        }

        public byte[] getContent() {
            return content;
        }
    }
}
