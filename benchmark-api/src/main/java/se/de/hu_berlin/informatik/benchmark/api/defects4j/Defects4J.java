package se.de.hu_berlin.informatik.benchmark.api.defects4j;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.utils.experiments.cv.CrossValidationUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.SystemUtils;
import se.de.hu_berlin.informatik.utils.properties.PropertyLoader;
import se.de.hu_berlin.informatik.utils.properties.PropertyTemplate;

public final class Defects4J {

	public static enum Defects4JProperties implements PropertyTemplate {
		EXECUTION_DIR("execution_dir", "/path/to/../execution_dir",
				"you can set an execution directory that differs from the archive directory.",
				"this is for example useful if you work on a unix server with a lot of RAM, such that",
				"you can, for the most part, directly work in the main memory (/dev/shm/...).",
				"will normally get deleted if nothing unexpected happens during execution.",
				"If equal to the archive directory, this will of course not be deleted."),
		ARCHIVE_DIR("archive_dir", "/path/to/../archive_dir",
				"the archive directory holds all generated project data in the end",
				"set the archive directory and the execution directory to the same paths if",
				"you are not sure what to do."),
		JAVA7_DIR("java7_dir", "/path/to/../jdk1.7.0_79/bin",
				"the projects in the Defects4J benchmark need Java 1.7 to work properly.",
				"you have to set path to the binaries here."),
		JAVA7_HOME("java7_home", "/path/to/../jdk1.7.0_79",
				"the projects in the Defects4J benchmark need Java 1.7 to work properly.",
				"set the path to the java home directory here."),
		JAVA7_JRE("java7_jre", "/path/to/../jdk1.7.0_79/jre",
				"the projects in the Defects4J benchmark need Java 1.7 to work properly.",
				"set the path to a proper JRE here."),
		ONLY_RELEVANT_TESTS("only_relevant_tests", "true", "whether only relevant tests shall be considered"),
		PLOT_DIR("plot_dir", "/path/to/../plot_dir_for_specific_LM",
				"specify the main directory to where the generated plot data shall be saved"),
		SPECTRA_ARCHIVE_DIR("spectraArchive_dir", "/path/to/../spectraArchive",
				"set the path to the archive of spectra directory, if it exists"),
		CHANGES_ARCHIVE_DIR("changesArchive_dir", "/path/to/../changesArchive",
				"set the path to the archive of changes directory, if it exists"),
		D4J_DIR("defects4j_dir", "/path/to/../defects4j/framework/bin", "path to the defects4j framework");

		final private String[] descriptionLines;
		final private String identifier;
		final private String placeHolder;

		private String value = null;

		Defects4JProperties(String identifier, String placeHolder, String... descriptionLines) {
			this.identifier = identifier;
			this.placeHolder = placeHolder;
			this.descriptionLines = descriptionLines;
		}

		@Override
		public String getPropertyIdentifier() {
			return identifier;
		}

		@Override
		public String getPlaceHolder() {
			return placeHolder;
		}

		@Override
		public String[] getHelpfulDescription() {
			return descriptionLines;
		}

		@Override
		public void setPropertyValue(String value) {
			this.value = value;
		}

		@Override
		public String getValue() {
			return value;
		}
	}

	public final static String SEP = File.separator;

	public final static String PROP_FILE_NAME = "defects4jProperties.ini";

	private final static String[] projects = { "Closure", "Time", "Math", "Lang", "Chart", "Mockito" };

	private static Properties props = PropertyLoader
			.loadProperties(new File(Defects4J.PROP_FILE_NAME), Defects4JProperties.class);

	// suppress default constructor (class should not be instantiated)
	private Defects4J() {
		throw new AssertionError();
	}

	public static Properties getProperties() {
		return props;
	}

	public static String getValueOf(Defects4JProperties property) {
		return property.getValue();
	}

	public static String getDefects4JExecutable() {
		return getValueOf(Defects4JProperties.D4J_DIR) + SEP + "defects4j";
	}

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
		for (final String element : Defects4J.getAllProjects()) {
			if (element.equals(project)) {
				return true;
			}
		}

		if (abortOnError) {
			Log.abort(
					Defects4J.class,
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
				Log.abort(Defects4J.class, "Bug ID is negative.");
			else
				return false;
		}

		int maxID = getMaxBugID(project);
		if (parsedID > maxID) {
			if (abortOnError) {
				Log.abort(Defects4J.class, "Bug ID may only range from 1 to %d for project %s.", maxID, project);
			} else {
				return false;
			}
		}

		return true;
	}

	public static String getD4JExport(String workDir, String option) {
		return executeCommandWithOutput(
				new File(workDir), false, Defects4J.getDefects4JExecutable(), "export", "-p", option);
	}

	/**
	 * Executes a given command in the system's environment, while additionally
	 * using a given Java 1.7 environment, which is required for defects4J to
	 * function correctly and to compile the projects. Will abort the program in
	 * case of an error in the executed process.
	 * @param executionDir
	 * an execution directory in which the command shall be executed
	 * @param abortOnError
	 * whether to abort if the command cannot be executed
	 * @param commandArgs
	 * the command to execute, given as an array
	 */
	public static void executeCommand(File executionDir, boolean abortOnError, String... commandArgs) {
		SystemUtils.executeCommandInJavaEnvironment(
				executionDir, Defects4JProperties.JAVA7_DIR.getValue(), Defects4JProperties.JAVA7_HOME.getValue(),
				Defects4JProperties.JAVA7_JRE.getValue(), abortOnError, (String[]) commandArgs);
	}

	/**
	 * Executes a given command in the system's environment, while additionally
	 * using a given Java 1.7 environment, which is required for defects4J to
	 * function correctly and to compile the projects. Returns either the
	 * process' output to standard out or to error out.
	 * @param executionDir
	 * an execution directory in which the command shall be executed
	 * @param returnErrorOutput
	 * whether to output the error output channel instead of standard out
	 * @param commandArgs
	 * the command to execute, given as an array
	 * @return the process' output to standard out or to error out
	 */
	public static String executeCommandWithOutput(File executionDir, boolean returnErrorOutput, String... commandArgs) {
		return SystemUtils.executeCommandWithOutputInJavaEnvironment(
				executionDir, returnErrorOutput, Defects4JProperties.JAVA7_DIR.getValue(),
				Defects4JProperties.JAVA7_HOME.getValue(), Defects4JProperties.JAVA7_JRE.getValue(),
				(String[]) commandArgs);
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
			Log.err(Defects4J.class, "'%s' is not a parseable identifier for a Defects4J entity.", k);
			return null;
		}
		return new Defects4JBuggyFixedEntity(items[0], items[1]);
	}

}
