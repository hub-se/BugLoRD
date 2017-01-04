/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.LineData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.NativeReport;
import se.de.hu_berlin.informatik.c2r.StatisticsData;
import se.de.hu_berlin.informatik.c2r.TestStatistics;
import se.de.hu_berlin.informatik.c2r.TestWrapper;
import se.de.hu_berlin.informatik.stardust.provider.ReportWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.OutputStreamManipulationUtilities;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;
import se.de.hu_berlin.informatik.utils.tracking.TrackingStrategy;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class TestRunAndReportModule extends AbstractModule<TestWrapper, ReportWrapper> {

	final private String testOutput;
//	final private Path dataFile;
//	final private Path dataFileBackup;
//	final private Path coverageXmlFile;
	final private Arguments reportArguments;
//	final private ReportFormat reportFormat;
	final private boolean debugOutput;
	final private Long timeout;
	
	final private StatisticsCollector<StatisticsData> statisticsContainer;
	
	private ProjectData projectData;
	private ProjectData initialProjectData;
//	private Field globalProjectData = null;
//	private Lock globalProjectDataLock = null;
	
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
			final StatisticsCollector<StatisticsData> statisticsContainer) {
		super(true);
		this.statisticsContainer = statisticsContainer;
//		this.dataFile = dataFile;
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
		
//		//try to get access to necessary fields from Cobertura with reflection...
//		try {
//			globalProjectData = ProjectData.class.getDeclaredField("globalProjectData");
//			globalProjectData.setAccessible(true);
//			resetProjectData();
//			Field projectDataLock = ProjectData.class.getDeclaredField("globalProjectDataLock");
//			projectDataLock.setAccessible(true);
//			globalProjectDataLock = (Lock) projectDataLock.get(null);
//		} catch (Exception e) {
//			globalProjectData = null;
//			globalProjectDataLock = null;
//		}
	}
	
//	private void resetProjectData() throws IllegalArgumentException, IllegalAccessException {
//		globalProjectData.set(null, new ProjectData());
//	}
	
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
			TestStatistics testStatistics = null;
			ProjectData lastProjectData = null;
			int iterationCounter = -1;
			boolean isEqual = false;
			
			while (!isEqual) {
				++iterationCounter;
				
//				//reset (delete) the data file
//				if (globalProjectData == null) {
//					FileUtils.delete(dataFile);
//				}
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
				testStatistics = testRunner.submit(testWrapper).getResult();

				//see if the test was executed
				if (!testStatistics.couldBeExecuted()) {
					projectData = null;
					if (testStatistics.getErrorMsg() != null) {
						Log.err(this, testStatistics.getErrorMsg());
					}
					break;
				}

				//disable std output
				if (!debugOutput) {
					System.out.flush();
//					System.err.flush();
					OutputStreamManipulationUtilities.switchOffStdOut();
//					OutputStreamManipulationUtilities.switchOffStdErr();
				}

//				/*
//				 * Wait for some time for all writing to finish, here?
//				 */
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					//do nothing
//				}

//				//calculate and save the coverage data (essential!)
//				//saving the data to a file would not really be necessary, but we have to reset the global
//				//project data and there doesn't seem to be any other way to reset it...
//				//UPDATE: We try to get the necessary private static fields first!
//				//If this succeeds, we are able to avoid writing the data file...
//				if (globalProjectData == null) {
//					//gets a reference to the current global project data, such that it 
//					//doesn't have to be loaded from the data file again, afterwards
//					projectData = ProjectData.getGlobalProjectData();
//					
//					ProjectData.saveGlobalProjectData();
//				} else {
					//gets a reference to the current global project data
//					projectData = ProjectData.getGlobalProjectData();
					//TODO: is this really safe to not use the global project data?
					projectData = new ProjectData();
					
					if (!isEqual(ProjectData.getGlobalProjectData(), projectData)) {
						Log.err(this, testWrapper + ": Global project data was updated.");
						testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, testWrapper + ": Global project data was updated.");
					}
					
//					//reset the project data
//					globalProjectDataLock.lock();
//					try {
//						resetProjectData();
//					} finally {
//						globalProjectDataLock.unlock();
//					}

//					/*
//					 * Now sleep a bit in case there is a thread still holding a reference to the "old"
//					 * globalProjectData. We want it to finish its updates.
//					 * (Is 1000 ms really enough in this case?)
//					 */
//					try {
//						Thread.sleep(200);
//					} catch (InterruptedException e) {
//						//do nothing
//					}

					TouchCollector.applyTouchesOnProjectData(projectData);
//				}
					

				if (lastProjectData != null) {
					isEqual = isEqual(projectData, lastProjectData);
					if (!isEqual) {
						Log.warn(this, testWrapper + ": Repeated test execution generated different coverage.");
						testStatistics.addStatisticsElement(StatisticsData.ERROR_MSG, testWrapper + ": Repeated test execution generated different coverage.");
					}
				}
				
				lastProjectData = projectData;
				
				//enable std output
				if (!debugOutput) {
					System.out.flush();
//					System.err.flush();
					OutputStreamManipulationUtilities.switchOnStdOut();
//					OutputStreamManipulationUtilities.switchOnStdErr();
				}
			}
			
			if (testStatistics.couldBeExecuted()) {
				testStatistics.addStatisticsElement(StatisticsData.REPORT_ITERATIONS, Double.valueOf(iterationCounter));
			}

			if (statisticsContainer != null) {
				statisticsContainer.addStatistics(testStatistics);
			}
			
			if (projectData != null) {
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

				if ( returnValue != 0 ) {
					Log.err(this, "Error while generating Cobertura report for test '%s'.", testWrapper);
					return null;
				}

				return new ReportWrapper(report, initialProjectData, testWrapper.toString(), testStatistics.wasSuccessful());
			}
		} catch (Exception e) {
			Log.err(this, e);
		}
		
		return null;
	}

	private boolean isEqual(ProjectData projectData2, ProjectData lastProjectData) {
		// loop over all packages
        @SuppressWarnings("unchecked")
        SortedSet<PackageData> packages = projectData2.getPackages();
		Iterator<PackageData> itPackages = packages.iterator();
        @SuppressWarnings("unchecked")
        SortedSet<PackageData> packagesLast = lastProjectData.getPackages();
		Iterator<PackageData> itPackagesLast = packagesLast.iterator();
		if (packages.size() != packagesLast.size()) {
			Log.err(this, "Unequal amount of stored packages.");
			return false;
		}
		while (itPackages.hasNext()) {
			PackageData packageData = itPackages.next();
			PackageData packageDataLast = itPackagesLast.next();
			
			if (!packageData.getName().equals(packageDataLast.getName())) {
				Log.err(this, "Package names don't match.");
				return false;
			}
			
			// loop over all classes of the package
			@SuppressWarnings("unchecked")
			Collection<SourceFileData> sourceFiles = packageData.getSourceFiles();
			Iterator<SourceFileData> itSourceFiles = sourceFiles.iterator();
			@SuppressWarnings("unchecked")
			Collection<SourceFileData> sourceFilesLast = packageDataLast.getSourceFiles();
			Iterator<SourceFileData> itSourceFilesLast = sourceFilesLast.iterator();
			if (sourceFiles.size() != sourceFilesLast.size()) {
				Log.err(this, "Unequal amount of stored source files for package '%s'.", packageData.getName());
				return false;
			}
			while (itSourceFiles.hasNext()) {
				SourceFileData fileData = itSourceFiles.next();
				SourceFileData fileDataLast = itSourceFilesLast.next();
				
				if (!fileData.getName().equals(fileDataLast.getName())) {
					Log.err(this, "Source file names don't match for package '%s'.", packageData.getName());
					return false;
				}
				@SuppressWarnings("unchecked")
				SortedSet<ClassData> classes = fileData.getClasses();
				Iterator<ClassData> itClasses = classes.iterator();
				@SuppressWarnings("unchecked")
				SortedSet<ClassData> classesLast = fileDataLast.getClasses();
				Iterator<ClassData> itClassesLast = classesLast.iterator();
				if (classes.size() != classesLast.size()) {
					Log.err(this, "Unequal amount of stored classes for file '%s'.", fileData.getName());
					return false;
				}
				while (itClasses.hasNext()) {
					ClassData classData = itClasses.next();
					ClassData classDataLast = itClassesLast.next();
					
					if (!classData.getName().equals(classDataLast.getName())) {
						Log.err(this, "Class names don't match for file '%s'.", fileData.getName());
						return false;
					}
					if (!classData.getSourceFileName().equals(classDataLast.getSourceFileName())) {
						Log.err(this, "Source file names don't match for file '%s'.", fileData.getName());
						return false;
					}
					
	                // loop over all methods of the class
	                SortedSet<String> sortedMethods = new TreeSet<>();
	        		sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
	        		Iterator<String> itMethods = sortedMethods.iterator();
	        		SortedSet<String> sortedMethodsLast = new TreeSet<>();
	        		sortedMethodsLast.addAll(classDataLast.getMethodNamesAndDescriptors());
	        		Iterator<String> itMethodsLast = sortedMethodsLast.iterator();
	        		if (sortedMethods.size() != sortedMethodsLast.size()) {
	        			Log.err(this, "Unequal amount of stored methods for class '%s'.", classData.getName());
	        			return false;
	        		}
	        		while (itMethods.hasNext()) {
	        			final String methodNameAndSig = itMethods.next();
	        			final String methodNameAndSigLast = itMethodsLast.next();
	        			if (!methodNameAndSig.equals(methodNameAndSigLast)) {
	        				Log.err(this, "Methods don't match for class '%s'.", classData.getName());
	        				return false;
	        			}

	                    // loop over all lines of the method
	                    SortedSet<CoverageData> sortedLines = new TreeSet<>();
	            		sortedLines.addAll(classData.getLines(methodNameAndSig));
	            		Iterator<CoverageData> itLines = classData.getLines(methodNameAndSig).iterator();
	            		SortedSet<CoverageData> sortedLinesLast = new TreeSet<>();
	            		sortedLinesLast.addAll(classDataLast.getLines(methodNameAndSigLast));
	            		Iterator<CoverageData> itLinesLast = classDataLast.getLines(methodNameAndSigLast).iterator();
	            		if (sortedLines.size() != sortedLinesLast.size()) {
	            			Log.err(this, "Unequal amount of stored lines for method '%s'.", methodNameAndSig);
		        			return false;
		        		}
	            		while (itLines.hasNext()) {
	            			LineData lineData = (LineData) itLines.next();
	            			LineData lineDataLast = (LineData) itLinesLast.next();
	            			
	            			if (lineData.getLineNumber() != lineDataLast.getLineNumber()) {
	            				Log.err(this, "Line numbers don't match for method '%s'.", methodNameAndSig);
	            				return false;
	            			}
	            		}
	        		}
				}
			}
		}
		return true;
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
