/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class ConvertChangesXMLFiles {
	
	private final static String[] projects = { "Closure", "Time", "Math", "Lang", "Chart", "Mockito" };

	public static String[] getAllBugIDs(String project) {
		int maxID = getMaxBugID(project);
		String[] result = new String[maxID];
		for (int i = 0; i < maxID; ++i) {
			result[i] = String.valueOf(i + 1);
		}
		return result;
	}

	public static int getMaxBugID(String project) {
		int maxID;
		switch (project) {
		case "Lang":
			maxID = 65;
			break;
		case "Math":
			maxID = 106;
			break;
		case "Chart":
			maxID = 26;
			break;
		case "Time":
			maxID = 27;
			break;
		case "Closure":
			maxID = 133;
			break;
		case "Mockito":
			maxID = 38;
			break;
		default:
			maxID = 0;
			break;
		}
		return maxID;
	}
	
	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {
		
		Path mainPath = Paths.get("d4j-faults");
		
		//iterate over all projects
		for (String project : projects) {
			Path projectPath = mainPath.resolve(project);
			if (!projectPath.toFile().exists()) {
				Log.out(ConvertChangesXMLFiles.class, "%s does not exist!", projectPath);
				continue;
			}
			String[] ids = getAllBugIDs(project); 
			for (String id : ids) {
				Path xmlPath = projectPath.resolve("bugdiagnosis_" + project + "-" + id + ".xml");
				if (!xmlPath.toFile().exists()) {
					Log.out(ConvertChangesXMLFiles.class, "%s does not exist!", projectPath);
					continue;
				}
				
				
			}
		}
		
		Log.out(ConvertChangesXMLFiles.class, "All done!");
		
	}
	
}
