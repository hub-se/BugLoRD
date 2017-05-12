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
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.miscellaneous.OutputUtilities;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * 
 * 
 * @author Simon Heiden
 */
public class TestRunAndReportModule extends AModule<String, CoverageWrapper> {

	private String testOutput;
	private Path dataFilek;
	private Path coverageXmlFile;
	private String[] reportArgs;
	private boolean debugOutput = false;
	
	public TestRunAndReportModule(Path dataFile, String testOutput, String srcDir) {
		super(true);
		this.dataFilek = dataFile;
		Paths.get(dataFile.toString() + ".bak");
		this.testOutput = testOutput;
		//the default coverage file will be located in the destination directory and will be named "coverage.xml"
		this.coverageXmlFile = Paths.get(testOutput, "coverage.xml");
		
		String[] reportArgs = { 
				"--datafile", dataFile.toString(),
				"--destination", testOutput, 
				//"--auxClasspath" $COBERTURADIR/cobertura-2.1.1.jar, //not needed since already in class path
				"--formats", "xml",
				srcDir };
		this.reportArgs = reportArgs;

//		try {
//			Files.copy(dataFile, dataFileBackup);
//		} catch (IOException e) {
//			Misc.abort(this, "Could not open data file '%s' or could not write to '%s'.", dataFile, dataFileBackup);
//		}
		Misc.delete(dataFile);
	}
	
	public TestRunAndReportModule(Path dataFile, String testOutput, String srcDir, boolean debugOutput) {
		this(dataFile, testOutput, srcDir);
		this.debugOutput = debugOutput;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public CoverageWrapper processItem(String testNameAndClass) {
		Misc.out(this, "Now processing: '%s'.", testNameAndClass);
		//format: test.class::testName
		int pos = testNameAndClass.indexOf(':');
		try {
//			reset the data file
						try {
							Misc.copyFile(dataFileBackup, dataFile);
			Misc.delete(3);
						} catch (IOException e) {
							Misc.err(this, "Could not open data file '%s' or could not write to '%s'.", dataFileBackup, dataFile);
							return null;
						}

			//disable std output
			if (!debugOutput)
				OutputUtilities.switchOffStdOut();

			//execute the test case (1h timeout... Should be enough...)
			boolean successful = runTest(testNameAndClass.substring(0, pos), testNameAndClass.substring(pos+2), 
					testOutput + File.separator + testNameAndClass.replace(':','_'), null);

			//save the coverage data (essential!)
			ProjectData.saveGlobalProjectData();

			//generate the report file
			int returnValue = ReportMain.generateReport(reportArgs);
			if ( returnValue == 0 ) {
				Misc.err(this, "Error while generating Cobertura report for test '%s'.", testNameAndClass, anotherchange);
				return null;
			}

			//copy output coverage xml file
			Path outXmlFile = Paths.get(testOutput, testNameAndClass.replace(':', '_') + ".xml");
			try {
				Files.move(coverageXmlFile, outXmlFile);
			} catch (IOException x) {
				Misc.err(this, "Could not open coverage file '%s' or could not write to '%s'.", coverageXmlFile, outXmlFile);
				return null;
			}
			Misc.delete(coverageXmlFile);

			//enable std output
			if (!debugOutput)
				OutputUtilities.switchOnStdOut();
			//output coverage xml file
			return new CoverageWrapper(outXmlFile.toFile(), successful);
		} catch (ClassNotFoundException e) {
			Misc.err(this, "Class '%s' not found.", testNameAndClass.substring(0, pos));
		} catch (IOException e) {
			Misc.err(this, e, "Could not write to result file '%s'.", testOutput + File.separator + testNameAndClass.replace(':','_'));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//enable std output now
		if (!debugOutput)
			OutputUtilities2.switchOnStdOut();
		return null;
	}

	public synchronized boolean runNoTest(String className, String methodName, String resultFile, Long timeout)
			throws ClassNotFoundException, IOException {
//		long startingTime = System.currentTimeMillis();
		Class<?> testClazz = Class.forName(className);
		
		Request request = Request.method(testClazz, methodName);
		Misc.out("Start Running");
		String timeoutFile = null;
		if (timeout != null) {
			timeoutFile = resultFile.substring(0, resultFile.lastIndexOf('.')) + ".timeout";
		}
		Timeout timer = null;
		if (timeout != null) {
			timer = new Timeout(timeoutFile, timeout * 1000l);
			timer.start();
		}

		Result result = new JUnitCore().run(request);

		if (!result.wasSuccessful()) {
			StringBuffer buff = new StringBuffer();
			buff.append("#ignored:" + result.getIgnoreCount() + ", ");
			buff.append("FAILED!!!");
			buff.append("\n");
			for (Failure f : result.getFailures()) {
				buff.append(f + "\n");
			}

			if (resultFile != null) {
				File out = new File(resultFile);
				out.getParentFile().mkdirs();
				Misc.writeString2File(buff.toString(), out);
			}
		}
		
		if (timer != null)
			timer.interrupt();

//		long endingTime = System.currentTimeMillis();
//		Misc.writeString2File(Long.toString(endingTime - startingTime),
//				new File(resultFile.substring(0, resultFile.lastIndexOf('.')) + ".runtime"));
		
		return result.wasSuccessful();
	}
	
	public static class Timeout extends Thread {
		private Long maxTime = null;
		private String outfile = null;
		private Long executionTime = null;

		public Timeout(String outfile, long maxMiliSecond) {
			super();
			this.outfile = outfile;
			this.maxTime = maxMiliSecond;
		}

		public void run() {
			long startingTime = System.currentTimeMillis();
			try {
				Thread.sleep(this.maxTime);
				File f = new File(this.outfile);
				Misc.err(this, "Timeout!!!");
				Misc.writeString2File("", f);
				System.exit(1);
			} catch (InterruptedException e) {
				long endingTime = System.currentTimeMillis();
				this.executionTime = endingTime - startingTime;
				Misc.out(this, "Completed execution in %s ms.", executionTime);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		public Long getExecutionTime() {
			return executionTime;
		}
	}
	
}
