/**
 *
 */
package se.de.hu_berlin.informatik.gen.spectra.predicates.modules;

import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunSingleTestAndReportModule;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunTestInNewJVMModule;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunTestLocallyModule;
import se.de.hu_berlin.informatik.gen.spectra.predicates.extras.PredicateReportWrapper;
import se.de.hu_berlin.informatik.gen.spectra.predicates.extras.Profile;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.*;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

public class PredicatesRunSingleTestAndReportModule extends AbstractRunSingleTestAndReportModule<ProjectData, PredicateReportWrapper> {

    final public static PredicateReportWrapper ERROR_WRAPPER = new PredicateReportWrapper(null, null, false, null);

    final private Path dataFile;
    private final Map<Class<?>, Integer> registeredClasses;
    private final Arguments reportArguments;
    private final String testOutput;
    private final ClassLoader cl;
    private final boolean debugOutput;
    private final String javaHome;
    private final Long timeout;
    private final int repeatCount;
    private final String instrumentedClassPath;
    private final boolean fullSpectra;
    private final File projectDir;
    private final String java7RunnerJar;
    boolean isFirst = true;

    public PredicatesRunSingleTestAndReportModule(final Path dataFile, final String testOutput, final File projectDir, final String srcDir,
                                                      final boolean fullSpectra, final boolean debugOutput, Long timeout, final int repeatCount,
                                                      String instrumentedClassPath, final String javaHome, final String java7RunnerJar, boolean useSeparateJVMalways,
                                                      boolean alwaysUseJava7, int maxErrors, String[] failingtests,
                                                      final StatisticsCollector<StatisticsData> statisticsContainer, ClassLoader cl,
                                                      String[] customJvmArgs) {
        super(testOutput, debugOutput, timeout, repeatCount, useSeparateJVMalways, alwaysUseJava7,
                maxErrors, failingtests, statisticsContainer, cl, customJvmArgs);
        this.testOutput = testOutput;
        this.projectDir = projectDir;
        this.fullSpectra = fullSpectra;
        this.debugOutput = debugOutput;
        this.timeout = timeout;
        this.repeatCount = repeatCount;
        this.instrumentedClassPath = instrumentedClassPath;
        this.javaHome = javaHome;
        this.java7RunnerJar = java7RunnerJar;
        this.cl = cl;

        this.dataFile = dataFile;
//		String baseDir = null;
        //validateDataFile(this.dataFile.toString());
        validateAndCreateDestinationDirectory(testOutput);


        ArgumentsBuilder builder = new ArgumentsBuilder();
        //builder.setDataFile(this.dataFile.toString());
        builder.setDestinationDirectory(testOutput);
        builder.addSources(srcDir, true);

        reportArguments = builder.build();

        //initialProjectData = CoverageDataFileHandler.loadCoverageData(dataFile.toFile());

        registeredClasses = TouchCollector.registeredClasses;

    }

    private void validateDataFile(String value) {
        File dataFile = new File(value);
        if (!dataFile.exists()) {
            Log.abort(this, "Error: data file " + dataFile.getAbsolutePath()
                    + " does not exist");
        }
        if (!dataFile.isFile()) {
            Log.abort(this, "Error: data file " + dataFile.getAbsolutePath()
                    + " must be a regular file");
        }
    }

    private void validateAndCreateDestinationDirectory(String value) {
        File destinationDir = new File(value);
        if (destinationDir.exists() && !destinationDir.isDirectory()) {
            Log.abort(this, "Error: destination directory " + destinationDir
                    + " already exists but is not a directory");
        }
        destinationDir.mkdirs();
    }

    @Override
    public PredicateReportWrapper generateReport(TestWrapper testWrapper, TestStatistics testStatistics,
                                                      ProjectData data) {

        //generate the report
        NativeReport report = new NativeReport(data, reportArguments
                .getDestinationDirectory(), reportArguments.getSources(),
                reportArguments.getEncoding());
        Profile profile = new Profile(new ArrayList<>(Output.Triggers.keySet()),testStatistics.wasSuccessful());

        return new PredicateReportWrapper(report,
                testWrapper.toString(), testStatistics.wasSuccessful(), profile);
    }

    @Override
    public PredicateReportWrapper getErrorReport() {
        return ERROR_WRAPPER;
    }

    @Override
    public AbstractRunTestLocallyModule<ProjectData> newTestRunLocallyModule() {
        return new PredicatesRunTestLocallyModule(dataFile, testOutput, fullSpectra,
                debugOutput, timeout, repeatCount, cl, registeredClasses);
    }

    @Override
    public AbstractRunTestInNewJVMModule<ProjectData> newTestRunInNewJVMModule() {
        return new PredicatesRunTestInNewJVMModule(testOutput, debugOutput, timeout,
                repeatCount, instrumentedClassPath + File.pathSeparator + new ClassPathParser().parseSystemClasspath().getClasspath(),
                dataFile, null, projectDir, getCustomSmallJvmArgs());
    }

    @Override
    public AbstractRunTestInNewJVMModule<ProjectData> newTestRunInNewJVMModuleWithJava7Runner() {

        String testClassPath = instrumentedClassPath + File.pathSeparator;
        if (java7RunnerJar == null) {
            testClassPath += new ClassPathParser().parseSystemClasspath().getClasspath();
        } else {
            testClassPath += java7RunnerJar;
            Log.out(this, testClassPath);
        }
        return new PredicatesRunTestInNewJVMModuleWithJava7Runner(testOutput,
                debugOutput, timeout, repeatCount, testClassPath,
                // + File.pathSeparator + systemClasspath.getClasspath(),
                dataFile, javaHome, projectDir, getCustomSmallJvmArgs());
    }

    @Override
    public ProjectData transformTestResultFromSeparateJVM(ProjectData projectData) {
        // we do not actually need to transform the IDs in the execution traces, since
        // the id to class map is project data specific;
        // it might have other IDs than the local project data objects, but that should ok!
        return projectData;
    }

    @Override
    public ProjectData transformTestResultFromSeparateJVMWithJava7(ProjectData projectData) {
        // we do not actually need to transform the IDs in the execution traces, since
        // the id to class map is project data specific;
        // it might have other IDs than the local project data objects, but that should ok!
        return projectData;
    }

}
