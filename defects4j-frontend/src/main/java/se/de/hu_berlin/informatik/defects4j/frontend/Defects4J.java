package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class Defects4J {
	
	private final static String SEP = File.separator;
	
	private Prop prop = null;
	
	public Defects4J(String project, String bugID) {
		prop = new Prop(project, bugID, true);
//		prop.switchToExecutionMode();
//		new File(prop.projectDir).mkdirs();
//		prop.switchToArchiveMode();
//		new File(prop.projectDir).mkdirs();
	}
	
	public Prop getProperties() {
		return prop;
	}
	
	public void switchToExecutionMode() {
		prop.switchToExecutionMode();
	}
	
	public void switchToArchiveMode() {
		prop.switchToArchiveMode();
	}
	
	public void checkoutBug(boolean buggyVersion) {
		if (buggyVersion) {
			prop.executeCommand(new File(prop.projectDir), 
					prop.defects4jExecutable, "checkout", 
					"-p", prop.getProject(), "-v", prop.getBugID() + "b", "-w", prop.buggyWorkDir);
		} else {
			prop.executeCommand(new File(prop.projectDir), 
					prop.defects4jExecutable, "checkout", 
					"-p", prop.getProject(), "-v", prop.getBugID() + "f", "-w", prop.fixedWorkDir);
		}
	}

	public String getInfo() {
		return prop.executeCommandWithOutput(new File(prop.projectDir), false, 
				prop.defects4jExecutable, "info", "-p", prop.getProject(), "-b", String.valueOf(prop.getBugID()));
	}
	
	private String getD4JExport(boolean buggyVersion, String option) {
		if (buggyVersion) {
			return prop.executeCommandWithOutput(new File(prop.buggyWorkDir), false, 
					prop.defects4jExecutable, "export", "-p", option);
		} else {
			return prop.executeCommandWithOutput(new File(prop.fixedWorkDir), false, 
					prop.defects4jExecutable, "export", "-p", option);
		}
	}
	
	public String getMainSrcDir(boolean buggyVersion) {
		return getD4JExport(buggyVersion, "dir.src.classes");
	}
	
	public String getMainBinDir(boolean buggyVersion) {
		return getD4JExport(buggyVersion, "dir.bin.classes");
	}
	
	public String getTestBinDir(boolean buggyVersion) {
		return getD4JExport(buggyVersion, "dir.bin.tests");
	}
	
	public String getTestCP(boolean buggyVersion) {
		return getD4JExport(buggyVersion, "cp.test");
	}
	
	public String getTests(boolean buggyVersion) {
		if (prop.relevant) {
			return getD4JExport(buggyVersion, "tests.relevant");
		} else {
			return getD4JExport(buggyVersion, "tests.all");
		}
	}
	
	public void compile(boolean buggyVersion) {
		if (buggyVersion) {
			if (!(new File(prop.buggyWorkDir + SEP + ".defects4j.config")).exists()) {
				Log.abort(Defects4J.class, "Defects4J config file doesn't exist: '%s'.", prop.buggyWorkDir + SEP + ".defects4j.config");
			}
			prop.executeCommand(new File(prop.buggyWorkDir), 
					prop.defects4jExecutable, "compile");
		} else {
			if (!(new File(prop.fixedWorkDir + SEP + ".defects4j.config")).exists()) {
				Log.abort(Defects4J.class, "Defects4J config file doesn't exist: '%s'.", prop.buggyWorkDir + SEP + ".defects4j.config");
			}
			prop.executeCommand(new File(prop.fixedWorkDir), 
					prop.defects4jExecutable, "compile");
		}
	}
	
	/**
	 * Deletes the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical or if forced to...
	 * @param buggyVersion
	 * whether to delete the buggy version's directory
	 * @param force
	 * whether to force deletion, even if the execution directory is equal to the archive directory
	 */
	public void tryDeleteExecutionDirectory(boolean buggyVersion, boolean force) {
		prop.tryDeleteExecutionDirectory(buggyVersion, force);
	}
	
	/**
	 * Deletes the buggy or fixed version archive directory.
	 * @param buggyVersion
	 * whether to delete the buggy version's directory
	 */
	public void deleteArchiveDirectory(boolean buggyVersion) {
		prop.deleteArchiveDirectory(buggyVersion);
	}
	
	/**
	 * Moves the buggy or fixed version execution directory if archive and execution directory 
	 * aren't identical...
	 * @param buggyVersion
	 * whether to move the buggy version's directory
	 */
	public void tryMovingExecutionDirToArchive(boolean buggyVersion) {
		prop.tryMovingExecutionDirToArchive(buggyVersion);
	}
	
}
