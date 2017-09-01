/**
 * 
 */
package se.de.hu_berlin.informatik.sbfl.spectra.cobertura.modules;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.NativeReport;
import se.de.hu_berlin.informatik.junittestutils.data.StatisticsData;
import se.de.hu_berlin.informatik.junittestutils.data.TestStatistics;
import se.de.hu_berlin.informatik.junittestutils.data.TestWrapper;
import se.de.hu_berlin.informatik.sbfl.spectra.modules.AbstractTestRunAndReportModule;
import se.de.hu_berlin.informatik.sbfl.spectra.modules.AbstractTestRunInNewJVMModule;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.CoberturaReportWrapper;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.coverage.LockableProjectData;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.coverage.MyTouchCollector;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class CoberturaTestRunAndReportModule extends AbstractTestRunAndReportModule<ProjectData,CoberturaReportWrapper> {

	final public static CoberturaReportWrapper ERROR_WRAPPER = new CoberturaReportWrapper(null, null, null, false);
	
	final private File dataFile;
	private ProjectData initialProjectData;

	final private CoberturaTestRunInNewJVMModule testRunnerNewJVM;
	
	private Map<Class<?>, Integer> registeredClasses;

	private Arguments reportArguments;

	@SuppressWarnings("unchecked")
	public CoberturaTestRunAndReportModule(final Path dataFile, final String testOutput, final File projectDir, final String srcDir, 
			final boolean fullSpectra, final boolean debugOutput, Long timeout, final int repeatCount,
			String instrumentedClassPath, final String javaHome, boolean useSeparateJVMalways, String[] failingtests,
			final StatisticsCollector<StatisticsData> statisticsContainer, ClassLoader cl) {
		super(testOutput, debugOutput, timeout, repeatCount, useSeparateJVMalways, failingtests, statisticsContainer, cl);

		this.dataFile = dataFile.toFile();
		String baseDir = null;
		validateDataFile(this.dataFile.toString());
		validateAndCreateDestinationDirectory(testOutput);

		ArgumentsBuilder builder = new ArgumentsBuilder();
		builder.setDataFile(this.dataFile.toString());
		builder.setDestinationDirectory(testOutput);
		builder.addSources(srcDir, baseDir == null);

		reportArguments = builder.build();

		this.testRunnerNewJVM = new CoberturaTestRunInNewJVMModule(testOutput, debugOutput, timeout, repeatCount, 
				instrumentedClassPath, this.dataFile.toPath(), javaHome, projectDir);
		

		//try to get access to necessary fields from Cobertura with reflection...
		try {
			Field registeredClassesField = TouchCollector.class.getDeclaredField("registeredClasses");
			registeredClassesField.setAccessible(true);
			registeredClasses = (Map<Class<?>, Integer>) registeredClassesField.get(null);
		} catch (Exception e) {
			//if reflection doesn't work, get the classes from the data file
			Collection<ClassData> classes;
			if (fullSpectra) {
				classes = initialProjectData.getClasses();
			} else {
				classes = CoverageDataFileHandler.loadCoverageData(dataFile.toFile()).getClasses();
			}
			registeredClasses = new HashMap<>();
			for (ClassData classData : classes) {
				try {
					if (cl == null) {
						registeredClasses.put(Class.forName(classData.getName()), 0);
					} else {
						registeredClasses.put(Class.forName(classData.getName(), true, cl), 0);
					}
				} catch (ClassNotFoundException e1) {
					Log.err(this, "Class '%s' not found for registration.", classData.getName());
				}
			}
		}
		
		//in the original data file, all (executable) lines are contained, even though they are not executed at all;
		//so if we want to have the full spectra, we have to make a backup and load it again for each run test
		if (fullSpectra) {
			initialProjectData = CoverageDataFileHandler.loadCoverageData(this.dataFile);
		} else {
			initialProjectData = new LockableProjectData();
			MyTouchCollector.resetTouchesOnProjectData2(registeredClasses, initialProjectData);
		}
		
		//initialize/reset the project data
		ProjectData.saveGlobalProjectData();
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
	public AbstractTestRunInNewJVMModule<ProjectData> getTestRunInNewJVMModule() {
		return testRunnerNewJVM;
	}

	@Override
	public CoberturaReportWrapper generateReport(TestWrapper testWrapper, TestStatistics testStatistics,
			ProjectData data) {
		//generate the report
		ComplexityCalculator complexityCalculator = null;
//			= new ComplexityCalculator(reportArguments.getSources());
//			complexityCalculator.setEncoding(reportArguments.getEncoding());
//			complexityCalculator.setCalculateMethodComplexity(
//					reportArguments.isCalculateMethodComplexity());

		NativeReport report = new NativeReport(data, reportArguments
				.getDestinationDirectory(), reportArguments.getSources(),
				complexityCalculator, reportArguments.getEncoding());

		return new CoberturaReportWrapper(report, initialProjectData, 
				testWrapper.toString(), testStatistics.wasSuccessful());
	}

	@Override
	public boolean prepareBeforeRunningTest() {
		//sadly, we have to check if the coverage data has properly been reset...
		boolean isResetted = false;
		int maxTryCount = 3;
		int tryCount = 0;
		LockableProjectData projectData2 = null;
		while (!isResetted && tryCount < maxTryCount) {
			++tryCount;
			projectData2 = new LockableProjectData();
			MyTouchCollector.resetTouchesOnProjectData2(registeredClasses, projectData2);
//			LockableProjectData.resetLines(projectData2);
			if (!LockableProjectData.containsCoveredLines(projectData2)) {
				isResetted = true;
			}
		}
		return isResetted;
	}

	@Override
	public ProjectData getCoverageDataAftertest() {
		ProjectData projectData = new LockableProjectData();
		MyTouchCollector.applyTouchesOnProjectData2(registeredClasses, projectData);
		return projectData;
	}

	@Override
	public CoberturaReportWrapper getErrorReport() {
		return ERROR_WRAPPER;
	}

}
