package se.de.hu_berlin.informatik.settings;

import java.io.File;

/**
 * @author SimHigh
 *
 */
public class TestSettings {

	private static String stdResourcesDir = "src" + File.separator + "main" + File.separator + "resources";
	private static String stdTestDir = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "tests";
	
	public static String getStdResourcesDir() {
		return stdResourcesDir;
	}
	
	public static void setStdResourcesDir(String stdResourcesDir) {
		TestSettings.stdResourcesDir = stdResourcesDir;
	}
	
	public static String getStdTestDir() {
		return stdTestDir;
	}
	
	public static void setStdTestDir(String stdTestDir) {
		TestSettings.stdTestDir = stdTestDir;
	}
	
	
}
