/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.NativeReport;
import se.de.hu_berlin.informatik.c2r.StatisticsData;
import se.de.hu_berlin.informatik.c2r.TestStatistics;
import se.de.hu_berlin.informatik.c2r.TestWrapper;
import se.de.hu_berlin.informatik.stardust.provider.ReportWrapper;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.OutputStreamManipulationUtilities;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsContainer;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;
import se.de.hu_berlin.informatik.utils.tracking.TrackingStrategy;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class TestRunAndReportModule extends AbstractModule<TestWrapper, ReportWrapper> {

	final private String testOutput;
	final private Path dataFile;
//	final private Path dataFileBackup;
//	final private Path coverageXmlFile;
	final private Arguments reportArguments;
//	final private ReportFormat reportFormat;
	final private boolean debugOutput;
	final private Long timeout;
	
	final private StatisticsContainer<StatisticsData> statisticsContainer;
	
	private ProjectData projectData;
	private ProjectData initialProjectData;
	private Field globalProjectData = null;
	private Lock globalProjectDataLock = null;
	
	final private boolean fullSpectra;

	final private TestRunModule testRunner;

	public TestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir) {
		this(dataFile, testOutput, srcDir, false, false, null, 1);
	}
	
	public TestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir, 
			final boolean fullSpectra, final boolean debugOutput, Long timeout, final int repeatCount) {
		this(dataFile, testOutput, srcDir, fullSpectra, debugOutput, timeout, repeatCount, null);
	}
	
	public TestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir, 
			final boolean fullSpectra, final boolean debugOutput, Long timeout, final int repeatCount,
			final StatisticsContainer<StatisticsData> statisticsContainer) {
		super(true);
		this.statisticsContainer = statisticsContainer;
		this.dataFile = dataFile;
//		this.dataFileBackup = Paths.get(dataFile.toString() + ".bak");
		this.testOutput = testOutput;
		//the default coverage file will be located in the destination directory and will be named "coverage.xml"
//		this.coverageXmlFile = Paths.get(testOutput, "coverage.xml");

		String baseDir = null;
//		String format = "xml";
		validateDataFile(dataFile.toString());
		validateAndCreateDestinationDirectory(this.testOutput);
		
		ArgumentsBuilder builder = new ArgumentsBuilder();
		builder.setDataFile(dataFile.toString());
		builder.setDestinationDirectory(this.testOutput);
		builder.addSources(srcDir, baseDir == null);
		
//		this.reportFormat = ReportFormat.getFromString(format);
		reportArguments = builder.build();
		
		this.fullSpectra = fullSpectra;
		
		//in the original data file, all (executable) lines are contained, even though they are not executed at all;
		//so if we want to have the full spectra, we have to make a backup and load it again for each run test
		if (this.fullSpectra) {
			initialProjectData = CoverageDataFileHandler.loadCoverageData(dataFile.toFile());
		} else {
			initialProjectData = new ProjectData();
		}
		
		this.debugOutput = debugOutput;
		this.timeout = timeout;
		
		this.testRunner = new TestRunModule(this.testOutput, debugOutput, this.timeout, repeatCount);
		
		//disable std output
		if (!debugOutput) {
			System.out.flush();
			OutputStreamManipulationUtilities.switchOffStdOut();
		}
		
		//initialize/reset the project data
		ProjectData.saveGlobalProjectData();
		//turn off auto saving (removes the shutdown hook inside of Cobertura)
		ProjectData.turnOffAutoSave();
		
		//enable std output
		if (!debugOutput) {
			System.out.flush();
			OutputStreamManipulationUtilities.switchOnStdOut();
		}
		
		//try to get access to necessary fields from Cobertura with reflection...
		try {
			globalProjectData = ProjectData.class.getDeclaredField("globalProjectData");
			globalProjectData.setAccessible(true);
			Field projectDataLock = ProjectData.class.getDeclaredField("globalProjectDataLock");
			projectDataLock.setAccessible(true);
			globalProjectDataLock = (Lock) projectDataLock.get(null);
		} catch (Exception e) {
			globalProjectData = null;
			globalProjectDataLock = null;
		}
	}
	
	private void resetProjectData() throws IllegalArgumentException, IllegalAccessException {
		globalProjectData.set(null, new ProjectData());
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

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public ReportWrapper processItem(final TestWrapper testWrapper) {
		try {
			//reset (delete) the data file
			if (globalProjectData == null) {
				FileUtils.delete(dataFile);
			}
			//TODO: we don't need to use the backup file anymore, do we?
//			//restore the original data file for the full spectra
//			if (fullSpectra) {
//				try {
//					Files.copy(dataFileBackup, dataFile);
//				} catch (IOException e) {
//					Log.err(this, "Could not open data file '%s' or could not write to '%s'.", dataFileBackup, dataFile);
//					return null;
//				}
//			}

			//(try to) run the test and get the statistics
			TestStatistics testStatistics = testRunner.submit(testWrapper).getResult();
			if (statisticsContainer != null) {
				statisticsContainer.addStatistics(testStatistics);
			}
			
			//see if the test was executed
			if (!testStatistics.couldBeExecuted()) {
				Log.err(this, testStatistics.getErrorMsg());
				return null;
			}
			
			//disable std output
			if (!debugOutput) {
				System.out.flush();
				OutputStreamManipulationUtilities.switchOffStdOut();
			}
			
			//gets a reference to the current project data, such that it 
			//doesn't have to be loaded from the data file again, afterwards
			projectData = ProjectData.getGlobalProjectData();
			
			//calculate and save the coverage data (essential!)
			//saving the data to a file would not really be necessary, but we have to reset the global
			//project data and there doesn't seem to be any other way to reset it...
			//UPDATE: We try to get the necessary private static fields first!
			//If this succeeds, we are able to avoid writing the data file...
			if (globalProjectData == null) {
				ProjectData.saveGlobalProjectData();
			} else {
				//reset the project data
				globalProjectDataLock.lock();
				try {
					resetProjectData();
				} finally {
					globalProjectDataLock.unlock();
				}

				/*
				 * Now sleep a bit in case there is a thread still holding a reference to the "old"
				 * globalProjectData. We want it to finish its updates.
				 * (Is 1 second really enough in this case?)
				 */
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

				TouchCollector.applyTouchesOnProjectData(projectData);
			}
			
			//generate the report
			NativeReport report = null;
			int returnValue = 0;
			try {
				//TODO: create the complexity calculator only once? does this work?
				ComplexityCalculator complexityCalculator = 
						new ComplexityCalculator(reportArguments.getSources());
				complexityCalculator.setEncoding(reportArguments.getEncoding());
				complexityCalculator.setCalculateMethodComplexity(
						reportArguments.isCalculateMethodComplexity());

				report = new NativeReport(projectData, reportArguments
						.getDestinationDirectory(), reportArguments.getSources(),
						complexityCalculator, reportArguments.getEncoding());
			} catch(Exception e) {
				returnValue = 1;
			}
			
			//enable std output
			if (!debugOutput) {
				System.out.flush();
				OutputStreamManipulationUtilities.switchOnStdOut();
			}
			
			if ( returnValue != 0 ) {
				Log.err(this, "Error while generating Cobertura report for test '%s'.", testWrapper);
				return null;
			}

			return new ReportWrapper(report, initialProjectData, testWrapper.toString(), testStatistics.wasSuccessful());
		} catch (Exception e) {
			Log.err(this, e);
		}
		
		return null;
	}

	@Override
	public boolean finalShutdown() {
		testRunner.finalShutdown();
		return super.finalShutdown();
	}

	@Override
	public AbstractModule<TestWrapper, ReportWrapper> enableTracking() {
		super.enableTracking();
		delegateTrackingTo(testRunner);
		return this;
	}

	@Override
	public AbstractModule<TestWrapper, ReportWrapper> enableTracking(int stepWidth) {
		super.enableTracking(stepWidth);
		delegateTrackingTo(testRunner);
		return this;
	}

	@Override
	public AbstractModule<TestWrapper, ReportWrapper> enableTracking(TrackingStrategy tracker) {
		super.enableTracking(tracker);
		delegateTrackingTo(testRunner);
		return this;
	}

	@Override
	public AbstractModule<TestWrapper, ReportWrapper> enableTracking(boolean useProgressBar) {
		super.enableTracking(useProgressBar);
		delegateTrackingTo(testRunner);
		return this;
	}

	@Override
	public AbstractModule<TestWrapper, ReportWrapper> enableTracking(boolean useProgressBar, int stepWidth) {
		super.enableTracking(useProgressBar, stepWidth);
		delegateTrackingTo(testRunner);
		return this;
	}
	
}
