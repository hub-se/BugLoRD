package se.de.hu_berlin.informatik.benchmark.api.defects4j;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.utils.experiments.cv.CrossValidationUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class Defects4JBase {

	public final static String SEP = File.separator;

	public final static String PROP_FILE_NAME = "defects4jProperties.ini";

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

	public static Defects4JEntity[] getAllBugs(String project) {
		int maxID = getMaxBugID(project);
		Defects4JEntity[] result = new Defects4JEntity[maxID];
		for (int i = 0; i < maxID; ++i) {
			result[i] = Defects4JEntity.getBuggyDefects4JEntity(project, String.valueOf(i + 1));
		}
		return result;
	}

	public static String[] getAllProjects() {
		return projects;
	}

	public static boolean validateProject(String project, boolean abortOnError) {
		for (final String element : Defects4JBase.getAllProjects()) {
			if (element.equals(project)) {
				return true;
			}
		}

		if (abortOnError) {
			Log.abort(
					Defects4JBase.class,
					"Chosen project has to be either 'Lang', 'Chart', 'Time', 'Closure', 'Mockito' or 'Math'.");
		}
		return false;
	}

	public static boolean validateProjectAndBugID(String project, int parsedID, boolean abortOnError) {
		if (!validateProject(project, abortOnError)) {
			return false;
		}

		if (parsedID < 1) {
			if (abortOnError)
				Log.abort(Defects4JBase.class, "Bug ID is negative.");
			else
				return false;
		}

		int maxID = getMaxBugID(project);
		if (parsedID > maxID) {
			if (abortOnError) {
				Log.abort(Defects4JBase.class, "Bug ID may only range from 1 to %d for project %s.", maxID, project);
			} else {
				return false;
			}
		}

		return true;
	}

	public static List<BuggyFixedEntity<?>>[] generateNBuckets(BuggyFixedEntity<?>[] array, int n, Long seed,
			Path csvOutput) {
		List<BuggyFixedEntity<?>>[] buckets = CrossValidationUtils.drawFromArrayIntoNBuckets(array, n, seed);

		CrossValidationUtils.generateFileFromBuckets(buckets, k -> k.getUniqueIdentifier(), csvOutput);

		return buckets;
	}

	public static List<BuggyFixedEntity<?>>[] readBucketsFromFile(Path csvFile) {
		List<BuggyFixedEntity<?>>[] buckets = CrossValidationUtils.getBucketsFromFile(csvFile, k -> parseIdentifier(k));

		return buckets;
	}

	private static BuggyFixedEntity<?> parseIdentifier(String k) {
		String[] items = k.split(Defects4JBuggyFixedEntity.SEPARATOR_CHAR);
		if (items.length != 2) {
			Log.err(Defects4JBase.class, "'%s' is not a parseable identifier for a Defects4J entity.", k);
			return null;
		}
		return new Defects4JBuggyFixedEntity(items[0], items[1]);
	}

}
