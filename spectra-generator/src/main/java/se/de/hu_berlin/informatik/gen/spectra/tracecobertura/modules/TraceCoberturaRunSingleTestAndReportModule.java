/**
 * 
 */
package se.de.hu_berlin.informatik.gen.spectra.tracecobertura.modules;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunSingleTestAndReportModule;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunTestInNewJVMModule;
import se.de.hu_berlin.informatik.gen.spectra.modules.AbstractRunTestLocallyModule;
import se.de.hu_berlin.informatik.gen.spectra.tracecobertura.modules.sub.TraceCoberturaRunTestInNewJVMModule;
import se.de.hu_berlin.informatik.gen.spectra.tracecobertura.modules.sub.TraceCoberturaRunTestInNewJVMModuleWithJava7Runner;
import se.de.hu_berlin.informatik.gen.spectra.tracecobertura.modules.sub.TraceCoberturaRunTestLocallyModule;
import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Arguments;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ArgumentsBuilder;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.CoverageDataFileHandler;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.TouchCollector;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.NativeReport;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.ProjectData;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.report.TraceCoberturaReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.ClassPathParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class TraceCoberturaRunSingleTestAndReportModule extends AbstractRunSingleTestAndReportModule<ProjectData,TraceCoberturaReportWrapper> {

	final public static TraceCoberturaReportWrapper ERROR_WRAPPER = new TraceCoberturaReportWrapper(null, null, false);
	
	final private Path dataFile;
	private Map<Class<?>, Integer> registeredClasses;
	private Arguments reportArguments;
	private ProjectData initialProjectData;
	private ProjectData resetProjectData;
	private String testOutput;
	private ClassLoader cl;
	private boolean debugOutput;
	private String javaHome;
	private Long timeout;
	private int repeatCount;
	private String instrumentedClassPath;
	private boolean fullSpectra;
	private File projectDir;
	private String java7RunnerJar;
	boolean isFirst = true;

	public TraceCoberturaRunSingleTestAndReportModule(final Path dataFile, final String testOutput, final File projectDir, final String srcDir, 
			final boolean fullSpectra, final boolean debugOutput, Long timeout, final int repeatCount,
			String instrumentedClassPath, final String javaHome, final String java7RunnerJar, boolean useSeparateJVMalways, 
			boolean alwaysUseJava7, int maxErrors, String[] failingtests,
			final StatisticsCollector<StatisticsData> statisticsContainer, ClassLoader cl) {
		super(testOutput, debugOutput, timeout, repeatCount, useSeparateJVMalways, alwaysUseJava7, 
				maxErrors, failingtests, statisticsContainer, cl);
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
		String baseDir = null;
		validateDataFile(this.dataFile.toString());
		validateAndCreateDestinationDirectory(testOutput);

		ArgumentsBuilder builder = new ArgumentsBuilder();
		builder.setDataFile(this.dataFile.toString());
		builder.setDestinationDirectory(testOutput);
		builder.addSources(srcDir, baseDir == null);

		reportArguments = builder.build();
		
		initialProjectData = CoverageDataFileHandler.loadCoverageData(dataFile.toFile());

//		//try to get access to necessary fields from Cobertura with reflection...
//		try {
//			Field registeredClassesField = TouchCollector.class.getDeclaredField("registeredClasses");
//			registeredClassesField.setAccessible(true);
//			registeredClasses = (Map<Class<?>, Integer>) registeredClassesField.get(null);
//		} catch (Exception e) {
//			//if reflection doesn't work, get the classes from the data file
//			Collection<ClassData> classes = initialProjectData.getClasses();
//			registeredClasses = new HashMap<>();
//			for (ClassData classData : classes) {
//				try {
//					if (cl == null) {
//						registeredClasses.put(Class.forName(classData.getName()), 0);
//					} else {
//						registeredClasses.put(Class.forName(classData.getName(), true, cl), 0);
//					}
//				} catch (ClassNotFoundException e1) {
//					Log.err(this, "Class '%s' not found for registration.", classData.getName());
//				}
//			}
//		}
		
		registeredClasses = TouchCollector.registeredClasses;

		//in the original data file, all (executable) lines are contained, even though they are not executed at all;
		//so if we want to not have the full spectra, we have to reset this data here
		if (!this.fullSpectra) {
			initialProjectData.reset();
//			TouchCollector.resetTouchesOnRegisteredClasses();
		}

		//initialize/reset the project data
		resetProjectData = ProjectData.resetGlobalProjectDataAndGetResetAndWipeDataFile(dataFile.toFile());
		
		//turn off auto saving (removes the shutdown hook inside of Cobertura)
		ProjectData.turnOffAutoSave();
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
	public TraceCoberturaReportWrapper generateReport(TestWrapper testWrapper, TestStatistics testStatistics,
			ProjectData data) {
		if (fullSpectra && isFirst) {
			data.merge(initialProjectData);
			isFirst = false;
		} else {
			// we need to merge project data here to get the 
			// counter ID to line number maps for the class data
			data.merge(resetProjectData);
		}

		//generate the report
		NativeReport report = new NativeReport(data, reportArguments
				.getDestinationDirectory(), reportArguments.getSources(),
				reportArguments.getEncoding());

		return new TraceCoberturaReportWrapper(report, 
				testWrapper.toString(), testStatistics.wasSuccessful());
	}

	@Override
	public TraceCoberturaReportWrapper getErrorReport() {
		return ERROR_WRAPPER;
	}

	@Override
	public AbstractRunTestLocallyModule<ProjectData> newTestRunLocallyModule() {
		return new TraceCoberturaRunTestLocallyModule(dataFile, testOutput, fullSpectra, 
				debugOutput, timeout, repeatCount, cl, registeredClasses);
	}
	
	@Override
	public AbstractRunTestInNewJVMModule<ProjectData> newTestRunInNewJVMModule() {
		return new TraceCoberturaRunTestInNewJVMModule(testOutput, debugOutput, timeout, 
				repeatCount, instrumentedClassPath + File.pathSeparator + new ClassPathParser().parseSystemClasspath().getClasspath(), 
				dataFile, javaHome, projectDir);
	}

	@Override
	public AbstractRunTestInNewJVMModule<ProjectData> newTestRunInNewJVMModuleWithJava7Runner() {
		//remove as much irrelevant classes as possible from class path (does not work this way...) TODO
//		ClassPathParser systemClasspath = new ClassPathParser(true).parseSystemClasspath();
//		systemClasspath.removeElementsOtherThan("java7-test-runner", "ant-", "junit-4.12");
		String testClassPath = instrumentedClassPath + File.pathSeparator;
		if (java7RunnerJar == null) {
			testClassPath += new ClassPathParser().parseSystemClasspath().getClasspath();
		} else {
			testClassPath += java7RunnerJar;
			Log.out(this, java7RunnerJar);
		}
		return new TraceCoberturaRunTestInNewJVMModuleWithJava7Runner(testOutput, 
				debugOutput, timeout, repeatCount, testClassPath,
				// + File.pathSeparator + systemClasspath.getClasspath(), 
				dataFile, javaHome, projectDir);
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
