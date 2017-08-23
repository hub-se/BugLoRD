package se.de.hu_berlin.informatik.benchmark.api.ibugs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.AbstractEntity;
import se.de.hu_berlin.informatik.benchmark.api.ibugs.parser.IBugsTestResultParser;
import se.de.hu_berlin.informatik.benchmark.api.ibugs.parser.IBugsTestSuiteWrapper;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.SystemUtils;

public class IBugsEntity extends AbstractEntity {

	// this is the identifier for the iBugsEntity
	private final String fixedId;
	private final boolean buggyVersion;
	private final String FIX_TAG; // this is either pre-fix or post-fix
	private final String project;
	private final String versionRoot; // looks like
										// projectRoot/versions/28919/pre-fix/
	private final String targetRoot; // looks like
										// versionRoot/org.aspectj/modules/aj-build/dist/tools/lib/
	private final String harnessTestResults; // looks like
												// versionRoot/org.aspectj/modules/tests/harnessresults.xml

	// the project root needs to have the build.xml that is the target for the
	// ant calls
	// it is also used to find the testresults.xml file for this specific entity
	private final File projectRoot;

	// TODO this has to find a better place!
	public static final String CP_SEPERATOR = ":";

	private IBugs utils = new IBugs();

	/**
	 * @return the versionRoot
	 */
	public String getVersionRoot() {
		return versionRoot;
	}

	public static IBugsEntity getBuggyIBugsEntity(String aProject, String aProjectRoot, String aFixedId) {
		return new IBugsEntity(aFixedId, aProject, aProjectRoot, true);
	}

	public static IBugsEntity getFixedIBugsEntity(String aProject, String aProjectRoot, String aFixedId) {
		return new IBugsEntity(aFixedId, aProject, aProjectRoot, false);
	}

	/**
	 * @return the fixedId
	 */
	public String getFixedId() {
		return fixedId;
	}

	/**
	 * @return the buggyVersion
	 */
	public boolean isBuggyVersion() {
		return buggyVersion;
	}

	/**
	 * @return the project
	 */
	public String getProject() {
		return project;
	}

	/**
	 * @return the targetRoot
	 */
	public String getTargetRoot() {
		return targetRoot;
	}

	/**
	 * @return the projectRoot
	 */
	public File getProjectRoot() {
		return projectRoot;
	}

	/**
	 * @return the cpSeperator
	 */
	public static String getCpSeperator() {
		return CP_SEPERATOR;
	}

	private IBugsEntity(String aFixedId, String aProject, String aProjectRoot, boolean aBuggy) {
		super(new IBugsDirectoryProvider(aProject, aProjectRoot, aFixedId, aBuggy));

		if (aFixedId == null || aFixedId.length() == 0) {
			Log.err(this, "Invalid fixed id!");
		}

		fixedId = aFixedId;
		buggyVersion = aBuggy;

		if (aBuggy) {
			FIX_TAG = IBugs.PRE_FIX;
		} else {
			FIX_TAG = IBugs.POST_FIX;
		}

		project = aProject;
		projectRoot = new File(aProjectRoot);

		if (!projectRoot.exists()) {
			Log.err(this, "The root directory of the project does not exist: " + aProject);
		}

		versionRoot = projectRoot + "/" + IBugs.VERSION_SUBDIR + "/" + fixedId + "/" + FIX_TAG + "/";
		targetRoot = versionRoot + "org.aspectj/modules/aj-build/dist/tools/lib/"; // this
																					// is
																					// a
																					// constant
																					// because
																					// the
																					// ant
																					// file
																					// specifies
																					// it
		harnessTestResults = versionRoot + "org.aspectj/modules/tests/harnessresults.xml";
	}

	public boolean clean() {

		String antCmd = "";

		if (buggyVersion) {
			antCmd = utils.generatePreCleanCmd(fixedId);
			Log.out(this, "Cleaning pre fixed repository using command: " + antCmd);
		} else {
			antCmd = utils.generatePostCleanCmd(fixedId);
			Log.out(this, "Cleaning post fixed repository using command: " + antCmd);
		}

		String[] args = antCmd.split(" ");
		SystemUtils.executeCommand(projectRoot, true, args);

		return true;
	}

	@Override
	public boolean compile(boolean executionMode) {

		// a clean is needed in some versions
		clean();

		String antCmd = "";

		if (buggyVersion) {
			antCmd = utils.generatePreBuildCmd(fixedId);
			Log.out(this, "Compiling classes for pre fixed repository using command: " + antCmd);
		} else {
			antCmd = utils.generatePostBuildCmd(fixedId);
			Log.out(this, "Compiling classes for post fixed repository using command: " + antCmd);
		}

		String[] args = antCmd.split(" ");
		// compile two times because of a container and order bug
		SystemUtils.executeCommand(projectRoot, true, args);
		SystemUtils.executeCommand(projectRoot, true, args);

		// lets save some calls and always build the tests together with the
		// normal build
		compileTests(executionMode);

		return true;
	}

	/**
	 * Builds the test classes for the repository.
	 * 
	 * @param executionMode
	 * currently not used. Specified the archive or execution directory for d4j
	 * @return true
	 */
	public boolean compileTests(boolean executionMode) {
		String antCmd = "";

		if (buggyVersion) {
			antCmd = utils.generatePreBuildTestsCmd(fixedId);
			Log.out(this, "Compiling test classes for pre fixed repository using command: " + antCmd);
		} else {
			antCmd = utils.generatePostBuildTestsCmd(fixedId);
			Log.out(this, "Compiling test classes for post fixed repository using command: " + antCmd);
		}

		String[] args = antCmd.split(" ");

		// compile two times because of a container and order bug
		SystemUtils.executeCommand(projectRoot, true, args);
		SystemUtils.executeCommand(projectRoot, true, args);

		// this looks a bit strange but running the junittests creates files
		// that store important data like
		// the classpath
		runJUnitTests();

		return true;
	}

	/**
	 * There is a special set of tests called harness tests that can be executed
	 * @return true
	 */
	public boolean runHarnessTests() {
		String antCmd = "";

		// this only makes sense if the repository was built before and the
		// target directory exists
		File targetDir = new File(targetRoot);
		if (!targetDir.exists()) {
			Log.err(
					this, "The target directory does not exists making the execution of tests impossible: "
							+ targetDir.getAbsolutePath());

			// TODO decide if I should just trigger the clean building and
			// proceed
			return false;
		}

		if (buggyVersion) {
			antCmd = utils.generateRunHarnessTestsPreFixCmd(fixedId);
			Log.out(this, "Running harness tests for pre fixed repository using command: " + antCmd);
		} else {
			antCmd = utils.generateRunHarnessTestsPostFixCmd(fixedId);
			Log.out(this, "Running harness tests for post fixed repository using command: " + antCmd);
		}

		String[] args = antCmd.split(" ");
		SystemUtils.executeCommand(projectRoot, true, args);

		// parse the result file and print the content
		parseHarnessTestResultFile();

		return true;
	}

	/**
	 * Searches for the files that is created after the harness tests are run
	 * and reports the content. The file itself is not in a valid xml format and
	 * rather consists of several lines of output data.
	 * 
	 * This method is only public for debugging reasons and should not be used
	 * in a normal test szenario since it is already executed after the harness
	 * tests.
	 */
	public void parseHarnessTestResultFile() {
		File harnessTestResultsFile = new File(harnessTestResults);
		if (!harnessTestResultsFile.exists()) {
			Log.err(this, "The result file for the execution of the harness tests was not created...");
		}

		// the content is not really in an appealing format so the idea is to
		// parse the start of each line
		// I think this should originally create a valid xml files but it
		// somehow did not
		FileReader is = null;
		BufferedReader reader = null;

		int passes = 0;
		int fails = 0;
		// this could be extended to save more if needed
		// counting the skipps is a bit harder

		try {
			is = new FileReader(harnessTestResultsFile);
			reader = new BufferedReader(is);

			String oneLine = null;

			while ((oneLine = reader.readLine()) != null) {
				oneLine = oneLine.trim();

				if (oneLine.length() > 0) {
					if (oneLine.startsWith("PASS:")) {
						++passes;
					} else if (oneLine.startsWith("FAIL:")) {
						++fails;
					}
				}
			}

		} catch (FileNotFoundException e) {
			Log.err(this, e);
		} catch (IOException e) {
			Log.err(this, e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// nothing to do
				}
			}
		}

		Log.out(this, "Results from running the harness tests:");
		Log.out(this, "\tPass: " + passes);
		Log.out(this, "\tFail: " + fails);
	}

	/**
	 * There is a special set of tests called harness tests that can be executed
	 * @return true
	 */
	public boolean runJUnitTests() {
		// TODO remove this information once I reworked the building of the test
		// cases
		Log.out(
				this, "Running the junit test suites will most likely fail because of an ant error when building "
						+ "the test jars. The harness tests are much better and stable.");

		String antCmd = "";

		if (buggyVersion) {
			antCmd = utils.generateRunJUnitTestsPreFixCmd(fixedId);
			Log.out(this, "Running jUnit tests for pre fixed repository using command: " + antCmd);
		} else {
			antCmd = utils.generateRunJUnitTestsPostFixCmd(fixedId);
			Log.out(this, "Running jUnit tests for post fixed repository using command: " + antCmd);
		}

		String[] args = antCmd.split(" ");
		SystemUtils.executeCommand(projectRoot, true, args);

		return true;
	}

	/**
	 * This will probably never be implemented and used
	 * @return true
	 */
	public boolean genTestScript() {
		Log.out(this, "The generation of the test scripts is currently not implemented");
		return true;
	}

	@Override
	public void removeUnnecessaryFiles(boolean executionMode) throws UnsupportedOperationException {
		// hm there are none I guess
	}

	@Override
	public String getUniqueIdentifier() {
		return toString();
	}

	/**
	 * If this is the buggy entity a checkout will be performed that downloads
	 * the pre and the post fix version for the fixed id.
	 */
	@Override
	public boolean initialize(boolean executionMode) {
		checkoutRepository();
		return true;
	}

	/**
	 * Starts a task to checkout the repository for this entity
	 */
	public void checkoutRepository() {
		String antCmd = "";

		if (buggyVersion) {

			// first check if the destination directory already exists
			// not using the delete method from the normal directory provider
			// because this is not defects4j with archive and execution dir
			File entity_root = getDirectoryProvider().getEntityDir(true).toFile();
			if (entity_root.exists()) {
				// delete from the parent to delete the fix id directory as well
				FileUtils.delete(entity_root.getParentFile());
			}

			antCmd = utils.generateCheckoutCmd(fixedId);
		} else {
			// only the entity that represents the buggy version performs the
			// checkout because it downloads
			// always both versions
			return;
		}

		Log.out(this, "Checking out pre and postfix repository using command: " + antCmd);

		String[] args = antCmd.split(" ");
		SystemUtils.executeCommand(projectRoot, true, args);
	}

	@Override
	public String computeClassPath(boolean executionMode) throws UnsupportedOperationException {
		// the same for the normal usage and the test cases
		return findAllCPJars();
	}

	@Override
	public String computeTestClassPath(boolean executionMode) throws UnsupportedOperationException {
		// the same for the normal usage and the test cases
		return findAllCPJars();
	}

	@Override
	public List<String> computeTestCases(boolean executionMode) throws UnsupportedOperationException {
		// TODO throws unsupported operation exception in defects4j but should
		// be a list of test cases I guess

		return null;
	}

	/**
	 * Parses the testresults.xml file that was generated previously and makes
	 * the data available as a IBugsTestSuiteWrapper object.
	 * 
	 * @return a wrapper object containing the data from the xml file
	 */
	public IBugsTestSuiteWrapper parseTestResultsFile() {

		IBugsTestResultParser parser = new IBugsTestResultParser();
		IBugsTestSuiteWrapper testResults = parser.parseTestResultXML(versionRoot);

		// return relevant data
		return testResults;
	}

	@Override
	public List<Path> computeTestClasses(boolean executionMode) throws UnsupportedOperationException {
		Log.out(this, "This is not how iBugs works :(");

		// The junit tests are all named *ModuleTests.java and stored inside the
		// testsrc directories of all modules
		// the harness tests are all listed in the apjTests.xml and jimTests.xml
		// files

		return null;
	}

	/**
	 * Just a temporary solution that collects the jars that are also mentioned
	 * in the ant script
	 * 
	 * @return a sufficient class path for executing the harness tests
	 */
	public String getClasspathForHarnessTests() {
		return findAllCPJars();
	}

	public String toString() {
		String sep = "-";
		String buggy = buggyVersion ? IBugs.PRE_FIX : IBugs.POST_FIX;
		return project + sep + fixedId + sep + buggy;
	}

	/**
	 * Finds all jars in the lib directory of the current version directory and
	 * returns them in class path format
	 * 
	 * @return a class path string containing all jars that will be needed or
	 * null if the library directory could not be found
	 */
	public String findAllCPJars() {
		String result = "";

		// as dirty as it gets but this is the simulation of the fileset copy
		// from ant

		// this needs to exist
		File libDir = new File(versionRoot + "org.aspectj/modules/lib");
		if (!libDir.exists()) {
			Log.err(this, "The lib directory " + libDir.getAbsolutePath() + " is missing...");
			return null;
		}

		// and this needs to exist as well
		File buildDir = new File(versionRoot + "org.aspectj/modules/aj-build/jars");
		if (!buildDir.exists()) {
			Log.err(this, "The build directory " + buildDir.getAbsolutePath() + " is missing...");
			return null;
		}

		File targetDir = new File(targetRoot);
		if (!targetDir.exists()) {
			Log.err(this, "The target directory " + targetDir.getAbsolutePath() + " is missing...");
		}

		// init the classpath with the most important jars
		Collection<String> jarsFound = getJarsToInstrument();
		// followed by the non aspectJ libs
		findAllCPJarsRec(libDir, jarsFound);
		findAllCPJarsRec(buildDir, jarsFound);

		result = convertCollectionToClasspath(jarsFound);

		// TODO I could parse all the .classpath files... maybe if something
		// throws errors

		return result;
	}

	/**
	 * After building the files of a repository the jars that are relevant to
	 * monitoring are placed in a specified directory. This method returns the
	 * absolute path of all created jars.
	 * 
	 * @return A collection of absolute path names of relevant jars for
	 * monitoring or null if something is wrong
	 */
	public Collection<String> getJarsToInstrument() {
		// by definition all built jars are stored here
		// org.aspectj\modules\aj-build\dist\tools\lib
		Collection<String> result = new ArrayList<String>();

		File targetRootDir = new File(targetRoot);
		if (!targetRootDir.exists()) {
			Log.err(this, "The target directory for the built jars does not exist: " + targetRootDir.getAbsolutePath());
			return null;
		}

		if (!targetRootDir.isDirectory()) {
			Log.err(this, "The target directory is not a directory? " + targetRootDir);
			return null;
		}

		String[] allFiles = targetRootDir.list();

		// we are only interested in jars. A FileFilter would also work.
		for (String s : allFiles) {
			if (s.endsWith(".jar")) {
				result.add(targetRoot + s);
			}
		}

		return result;
	}

	/**
	 * Searches for all .jar files in the given directory and adds them to the
	 * collection
	 * 
	 * @param aFile
	 * The root directory with some jars
	 * @param aJarsFound
	 * A collection storing all the paths to the found jars
	 */
	private void findAllCPJarsRec(File aFile, Collection<String> aJarsFound) {
		if (aFile.getName().endsWith(".jar")) {
			aJarsFound.add(aFile.getAbsolutePath());
			return;
		}

		if (aFile.isDirectory()) {
			for (File innerFile : aFile.listFiles()) {
				findAllCPJarsRec(innerFile, aJarsFound);
			}
		}
	}

	/**
	 * Iterates over all entries in the collection and builds a classpath from
	 * it
	 * 
	 * @param aCollectionOfStrings
	 * A collection containing the absolute paths of files
	 * @return A class path with all entries from the collection
	 */
	private String convertCollectionToClasspath(Collection<String> aCollectionOfStrings) {
		// could also be done with a StringBuilder
		String result = "";

		Iterator<String> it = aCollectionOfStrings.iterator();

		// first element has no leading seperator
		if (it.hasNext()) {
			result = it.next();
		}

		// all following entries are seperated by a ;
		while (it.hasNext()) {
			result += ";" + it.next();
		}

		return result;
	}

	@Override
	public List<String> getFailingTests(boolean executionMode) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
