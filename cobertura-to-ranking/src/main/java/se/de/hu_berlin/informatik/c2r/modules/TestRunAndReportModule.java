/**
 * 
 */
package se.de.hu_berlin.informatik.c2r.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.ReportMain;
import se.de.hu_berlin.informatik.c2r.CoverageWrapper;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.OutputStreamManipulationUtilities;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;
import se.de.hu_berlin.informatik.utils.tracking.ProgressTracker;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class TestRunAndReportModule extends AbstractModule<String, CoverageWrapper> {

	final private String testOutput;
	final private Path dataFile;
	final private Path coverageXmlFile;
	final private String[] reportArgs;
	final private boolean debugOutput;
	
	final private ProgressTracker tracker = new ProgressTracker(false);
	
	public TestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir) {
		this(dataFile, testOutput, srcDir, false);
	}
	
	public TestRunAndReportModule(final Path dataFile, final String testOutput, final String srcDir, 
			final boolean debugOutput) {
		super(true);
		this.dataFile = dataFile;
		Paths.get(dataFile.toString() + ".bak");
		this.testOutput = testOutput;
		//the default coverage file will be located in the destination directory and will be named "coverage.xml"
		this.coverageXmlFile = Paths.get(testOutput, "coverage.xml");
		
		this.reportArgs = new String[] { 
				"--datafile", dataFile.toString(),
				"--destination", testOutput, 
				//"--auxClasspath" $COBERTURADIR/cobertura-2.1.1.jar, //not needed since already in class path
				"--format", "xml",
				srcDir };

//		try {
//			Files.copy(dataFile, dataFileBackup);
//		} catch (IOException e) {
//			Misc.abort(this, "Could not open data file '%s' or could not write to '%s'.", dataFile, dataFileBackup);
//		}
		FileUtils.delete(dataFile);
		
		this.debugOutput = debugOutput;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public CoverageWrapper processItem(final String testNameAndClass) {
		tracker.track("..." + testNameAndClass);
		System.out.flush();
//		Log.out(this, "Now processing: '%s'.", testNameAndClass);
		
		//format: test.class::testName
		final int pos = testNameAndClass.indexOf(':');
		try {
			//reset the data file
			//			try {
			//				Misc.copyFile(dataFileBackup, dataFile);
			FileUtils.delete(dataFile);
			//			} catch (IOException e) {
			//				Misc.err(this, "Could not open data file '%s' or could not write to '%s'.", dataFileBackup, dataFile);
			//				return null;
			//			}

			//disable std output
			if (!debugOutput) {
				OutputStreamManipulationUtilities.switchOffStdOut();
			}

			//execute the test case (1h timeout... Should be enough...)
			final boolean successful = runTest(testNameAndClass.substring(0, pos), testNameAndClass.substring(pos+2), 
					testOutput + File.separator + testNameAndClass.replace(':','_'), null);

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
			return new CoverageWrapper(outXmlFile.toFile(), successful);
		} catch (ClassNotFoundException e) {
			Log.err(this, "Class '%s' not found.", testNameAndClass.substring(0, pos));
		} catch (IOException e) {
			Log.err(this, e, "Could not write to result file '%s'.", testOutput + File.separator + testNameAndClass.replace(':','_'));
		} catch (Exception e) {
			Log.err(this, e);
		}
		
		//enable std output
		if (!debugOutput) {
			OutputStreamManipulationUtilities.switchOnStdOut();
		}
		
		return null;
	}

	public boolean runTest(final String className, 
			final String methodName, final String resultFile, final Long timeout)
			throws ClassNotFoundException, IOException {
//		long startingTime = System.currentTimeMillis();
		final Class<?> testClazz = Class.forName(className);
		
		final Request request = Request.method(testClazz, methodName);
		Log.out(this, "Start Running");
		
		Timeout timer = null;
		if (timeout != null) {
			final String timeoutFile = resultFile.substring(0, resultFile.lastIndexOf('.')) + ".timeout";
			timer = new Timeout(timeoutFile, timeout * 1000l);
			timer.start();
		}

		final Result result = new JUnitCore().run(request);

		if (!result.wasSuccessful()) {
			final StringBuilder buff = new StringBuilder();
			buff.append("#ignored:" + result.getIgnoreCount() + ", " + "FAILED!!!" + System.lineSeparator());
			for (final Failure f : result.getFailures()) {
				buff.append(f.toString() + System.lineSeparator());
			}

			if (resultFile != null) {
				final File out = new File(resultFile);
				out.getParentFile().mkdirs();
				FileUtils.writeString2File(buff.toString(), out);
			}
		}
		
		if (timer != null) {
			timer.interrupt();
		}

//		long endingTime = System.currentTimeMillis();
//		Misc.writeString2File(Long.toString(endingTime - startingTime),
//				new File(resultFile.substring(0, resultFile.lastIndexOf('.')) + ".runtime"));
		
		return result.wasSuccessful();
	}
	
	//a timeout will lead to a call of System.exit(), which will abort the entire application
	//TODO: change this behavior
	public static class Timeout extends Thread {
		final private Long maxTime;
		final private String outfile;
		
		private Long executionTime;

		public Timeout(final String outfile, final long maxMiliSecond) {
			super();
			this.outfile = outfile;
			this.maxTime = maxMiliSecond;
		}

		public void run() {
			final long startingTime = System.currentTimeMillis();
			try {
				Thread.sleep(this.maxTime);
				final File f = new File(this.outfile);
				Log.err(this, "Timeout!!!");
				FileUtils.writeString2File("", f);
				System.exit(1);
			} catch (InterruptedException e) {
				final long endingTime = System.currentTimeMillis();
				this.executionTime = endingTime - startingTime;
				Log.out(this, "Completed execution in %s ms.", executionTime);
			} catch (IOException e) {
				Log.err(this, e);
			}

		}

		public Long getExecutionTime() {
			return executionTime;
		}
	}
	
}
