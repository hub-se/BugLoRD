/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.ReportMain;
import se.de.hu_berlin.informatik.c2r.TestStatistics;
import se.de.hu_berlin.informatik.stardust.provider.CoverageWrapper;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.OutputStreamManipulationUtilities;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class TestRunAndReportModule extends AbstractModule<String, CoverageWrapper> {

	final private String testOutput;
	final private Path dataFile;
	final private Path dataFileBackup;
	final private Path coverageXmlFile;
	final private String[] reportArgs;
	final private boolean debugOutput;
	final private Long timeout;
	
	final private boolean fullSpectra;

	final private TestRunModule testRunner;

	public TestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir) {
		this(dataFile, testOutput, srcDir, false, false, null);
	}
	
	public TestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir, 
			final boolean fullSpectra, final boolean debugOutput, Long timeout) {
		super(true);
		this.dataFile = dataFile;
		this.dataFileBackup = Paths.get(dataFile.toString() + ".bak");
		this.testOutput = testOutput;
		//the default coverage file will be located in the destination directory and will be named "coverage.xml"
		this.coverageXmlFile = Paths.get(testOutput, "coverage.xml");
		
		this.reportArgs = new String[] { 
				"--datafile", dataFile.toString(),
				"--destination", testOutput, 
				//"--auxClasspath" $COBERTURADIR/cobertura-2.1.1.jar, //not needed since already in class path
				"--format", "xml",
				srcDir };
		this.fullSpectra = fullSpectra;
		
		//in the original data file, all lines are contained, even though they are not executed at all;
		//so if we want to have the full spectra, we have to make a backup and load it again for each run test
		if (this.fullSpectra) {
			try {
				FileUtils.delete(dataFileBackup);
				Files.copy(this.dataFile, dataFileBackup);
			} catch (IOException e) {
				Log.abort(this, "Could not open data file '%s' or could not write to '%s'.", dataFile, dataFileBackup);
			}
			FileUtils.delete(this.dataFile);
		}
		
		this.debugOutput = debugOutput;
		this.timeout = timeout;
		
		this.testRunner = new TestRunModule(this.testOutput, this.timeout);
		
		//initialize the project data
		ProjectData.saveGlobalProjectData();
		//turn off auto saving (removes the shutdown hook)
		ProjectData.turnOffAutoSave();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public CoverageWrapper processItem(final String testNameAndClass) {

		try {
			//reset the data file
			FileUtils.delete(dataFile);
			//restore the original data file for the full spectra
			if (fullSpectra) {
				try {
					Files.copy(dataFileBackup, dataFile);
				} catch (IOException e) {
					Log.err(this, "Could not open data file '%s' or could not write to '%s'.", dataFileBackup, dataFile);
					return null;
				}
			}

			//disable std output
			if (!debugOutput) {
				OutputStreamManipulationUtilities.switchOffStdOut();
			}

			//(try to) run the test and get the statistics
			TestStatistics testStatistics = testRunner.submit(testNameAndClass).getResult();
			
			//see if the test was executed
			if (!testStatistics.couldBeExecuted()) {
				//enable std output
				if (!debugOutput) {
					OutputStreamManipulationUtilities.switchOnStdOut();
				}
				Log.err(this, testStatistics.getErrorMsg());
				return null;
			}
			
			//save the coverage data (essential!)
			ProjectData.saveGlobalProjectData();

			//generate the report file
			final int returnValue = ReportMain.generateReport(reportArgs);
			if ( returnValue != 0 ) {
				//enable std output
				if (!debugOutput) {
					OutputStreamManipulationUtilities.switchOnStdOut();
				}
				Log.err(this, "Error while generating Cobertura report for test '%s'.", testNameAndClass);
				FileUtils.delete(coverageXmlFile);
				return null;
			}

			//copy output coverage xml file
			final Path outXmlFile = Paths.get(testOutput, testNameAndClass.replace(':', '_') + ".xml");
			try {
				Files.move(coverageXmlFile, outXmlFile);
			} catch (IOException e) {
				//enable std output
				if (!debugOutput) {
					OutputStreamManipulationUtilities.switchOnStdOut();
				}
				Log.err(this, "Could not open coverage file '%s' or could not write to '%s'.", coverageXmlFile, outXmlFile);
				FileUtils.delete(coverageXmlFile);
				return null;
			}
			FileUtils.delete(coverageXmlFile);

			//enable std output
			if (!debugOutput) {
				OutputStreamManipulationUtilities.switchOnStdOut();
			}
			//output coverage xml file
			return new CoverageWrapper(outXmlFile.toFile(), testNameAndClass, testStatistics.wasSuccessful());
		} catch (IOException e) {
			Log.err(this, e, "Could not write to result file '%s'.", testOutput, testNameAndClass.replace(':', '_') + ".xml");
		} catch (Exception e) {
			Log.err(this, e);
		}
		
		//enable std output
		if (!debugOutput) {
			OutputStreamManipulationUtilities.switchOnStdOut();
		}
		
		return null;
	}

	@Override
	public boolean finalShutdown() {
		testRunner.finalShutdown();
		return super.finalShutdown();
	}
	
}
