package se.de.hu_berlin.informatik.benchmark.api.ibugs;

import java.io.File;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class IBugs {

	// these are also used for the directory provider
	public static final String PRE_FIX = "pre-fix";
	public static final String POST_FIX = "post-fix";
	public static final String ASPECTJ_PROJECT_NAME = "AspectJ";
	public static final String DEFAULT_PROJECT = ASPECTJ_PROJECT_NAME;
	public static final String LIST_SEPARATOR = ",";
	public static final String USE_ALL_IDS = "all";

	public static final String ANT_COMMAND_CHECKOUT = " checkoutversion -DfixId=";
	public static final String ANT_COMMAND_BUILD = " buildversion -DfixId=";
	public static final String ANT_COMMAND_BUILD_BASE = " -Dtag=";
	public static final String ANT_COMMAND_PRE_BUILD = ANT_COMMAND_BUILD_BASE + PRE_FIX;
	public static final String ANT_COMMAND_POST_BUILD = ANT_COMMAND_BUILD_BASE + POST_FIX;
	public static final String ANT_COMMAND_BUILD_TESTS = " buildtests -DfixId=";
	public static final String ANT_COMMAND_RUN_JUNIT_TESTS = " runjunittests -DfixId=";
	public static final String ANT_COMMAND_RUN_HARNESS_TESTS = " runharnesstests -DfixId=";

	public static final String ANT_COMMAND_GEN_TEST_SCRIPT = " gentestscript -DfixId=";
	public static final String ANT_COMMAND_GEN_TEST_FILE_NAME = " -DtestFileName=";
	public static final String ANT_COMMAND_GEN_TEST_NAME = " -DtestName=";
	public static final String ANT_COMMAND_GEN_TEST_VMARGS = " -DjvmArgs=";
	
	// some values from the properties file that are not final
	public static final String PROPERTIES_FILE_NAME = "properties.xml";
	public static String ANT_EXE = "ant"; // this needs to be set after reading the properties
	public static String VERSION_SUBDIR = "versions";

	/**
	 * Returns some fixed ids that are listed as valid values in the
	 * ConvertRankings class. This list is incomplete and only contains
	 * 136 out of 350 possible ids.
	 * 
	 * @return Some valid fixed ids as strings
	 */
	public static String[] getAllFixedIdsForAspectJ() {
		// converted from the ConvertRankings class
		return new String[] { "28919", "28974", "29186", "29959", "30168", "32463", "33635", "34925", "36430", "36803",
				"37576", "37739", "38131", "39626", "39974", "40192", "40257", "40380", "40824", "42539", "42993",
				"43033", "43194", "43709", "44117", "46298", "47318", "49657", "50776", "51320", "51929", "52394",
				"54421", "54965", "55341", "57436", "57666", "58520", "59596", "59895", "61411", "62227", "64069",
				"64331", "67592", "69011", "70008", "71377", "71878", "72150", "72528", "72531", "72671", "73433",
				"74238", "77799", "81846", "81863", "82134", "82218", "83563", "86789", "87376", "88652", "96371",
				"99168", "100227", "104218", "107299", "109016", "109614", "113257", "114875", "115251", "115275",
				"116626", "116949", "118192", "118715", "118781", "119353", "119451", "119539", "119543", "120351",
				"120474", "122370", "122728", "123695", "124654", "124808", "125480", "125699", "125810", "128128",
				"128237", "128655", "128744", "129566", "130837", "130869", "131505", "131932", "131933", "132130",
				"135001", "136665", "138143", "138219", "138223", "138286", "141956", "142165", "145086", "145693",
				"145950", "146546", "147701", "148409", "150671", "151673", "151845", "152257", "152388", "152589",
				"152631", "153490", "153535", "153845", "154332", "155148", "155972", "156904", "156962", "158412",
				"161217" };
	}
	
	/**
	 * Get all known fixed ids for the specified project name
	 * @param aProjectName The name of the project
	 * @return all known ids
	 */
	public static String[] getAllFixedIdsForProject( String aProjectName ) {
		if( aProjectName.equalsIgnoreCase( ASPECTJ_PROJECT_NAME) ) {
			return getAllFixedIdsForAspectJ();
		}
		
		// we only know fixed ids for aspectj for now
		return new String[0];
	}

	/**
	 * Generates a checkout command that can be used in combination with ant and
	 * the proper build.xml to download a prefix and postfix version of a buggy
	 * repository
	 * 
	 * @param aFixedId
	 * The id of the bug that should be checked out
	 * @return a string that can be used to download a buggy repository
	 */
	public String generateCheckoutCmd(String aFixedId) {
		return ANT_EXE + ANT_COMMAND_CHECKOUT + aFixedId;
	}

	/**
	 * Generates a build command to build all files of a given version that was
	 * checked out before. This targets the pre fix repository.
	 * 
	 * @param aFixedId
	 *            The id of the bug for the repository that should be build
	 * @return a string that can be used to build a buggy repository
	 */
	public String generatePreBuildCmd(String aFixedId) {
		return ANT_EXE + ANT_COMMAND_BUILD + aFixedId + ANT_COMMAND_PRE_BUILD;
	}

	/**
	 * Generates a build command to build all files of a given version that was
	 * checked out before. This targets the post fix repository.
	 * 
	 * @param aFixedId
	 *            The id of the bug for the repository that should be build
	 * @return a string that can be used to build a buggy repository
	 */
	public String generatePostBuildCmd(String aFixedId) {
		return ANT_EXE + ANT_COMMAND_BUILD + aFixedId + ANT_COMMAND_POST_BUILD;
	}

	/**
	 * Generates a build command to build all test classes of a given version
	 * that was checked out before. This targets the pre fix repository.
	 * 
	 * @param aFixedId
	 *            The id of the bug for the repository that should be build
	 * @return a string that can be used to build a buggy repository
	 */
	public String generatePreBuildTestsCmd(String aFixedId) {
		return ANT_EXE + ANT_COMMAND_BUILD_TESTS + aFixedId + ANT_COMMAND_PRE_BUILD;
	}

	/**
	 * Generates a build command to build all test classes of a given version
	 * that was checked out before. This targets the post fix repository.
	 * 
	 * @param aFixedId
	 *            The id of the bug for the repository that should be build
	 * @return a string that can be used to build a buggy repository
	 */
	public String generatePostBuildTestsCmd(String aFixedId) {
		return ANT_EXE + ANT_COMMAND_BUILD_TESTS + aFixedId + ANT_COMMAND_POST_BUILD;
	}

	/**
	 * Generates a build command to build run the jUnit test classes of a given
	 * version that was checked out before. This targets the pre fix repository.
	 * 
	 * @param aFixedId
	 *            The id of the bug for the repository that should be build
	 * @return a string that can be used to build a buggy repository
	 */
	public String generateRunJUnitTestsPreFixCmd(String aFixedId) {
		return ANT_EXE + ANT_COMMAND_RUN_JUNIT_TESTS + aFixedId + ANT_COMMAND_PRE_BUILD;
	}

	/**
	 * Generates a build command to build run the jUnit test classes of a given
	 * version that was checked out before. This targets the post fix
	 * repository.
	 * 
	 * @param aFixedId
	 *            The id of the bug for the repository that should be build
	 * @return a string that can be used to build a buggy repository
	 */
	public String generateRunJUnitTestsPostFixCmd(String aFixedId) {
		return ANT_EXE + ANT_COMMAND_RUN_JUNIT_TESTS + aFixedId + ANT_COMMAND_POST_BUILD;
	}

	/**
	 * AspectJ has a set of harness tests for the compiler. These tests are
	 * described in xml-files, the main file being
	 * 'org.aspectj/modules/tests/ajcTests.xml'. This task executes all tests in
	 * 'org.aspectj/modules/tests/ajcTests.xml' and
	 * 'org.aspectj/modules/tests/jimTests.xml'
	 * 
	 * This targets the pre fix repository.
	 * 
	 * @param aFixedId
	 *            The id of the bug for the repository that should be build
	 * @return a string that can be used to build a buggy repository
	 */
	public String generateRunHarnessTestsPreFixCmd(String aFixedId) {
		return ANT_EXE + ANT_COMMAND_RUN_HARNESS_TESTS + aFixedId + ANT_COMMAND_PRE_BUILD;
	}

	/**
	 * AspectJ has a set of harness tests for the compiler. These tests are
	 * described in xml-files, the main file being
	 * 'org.aspectj/modules/tests/ajcTests.xml'. This task executes all tests in
	 * 'org.aspectj/modules/tests/ajcTests.xml' and
	 * 'org.aspectj/modules/tests/jimTests.xml'
	 * 
	 * This targets the post fix repository.
	 * 
	 * @param aFixedId
	 *            The id of the bug for the repository that should be build
	 * @return a string that can be used to build a buggy repository
	 */
	public String generateRunHarnessTestsPostFixCmd(String aFixedId) {
		return ANT_EXE + ANT_COMMAND_RUN_HARNESS_TESTS + aFixedId + ANT_COMMAND_POST_BUILD;
	}

	/**
	 * This is not yet tested and should be used carefully.
	 * 
	 * 
	 * This is a convenience task that allows you to run a single test out of
	 * AspectJ's test suite. Tests are identified using the test name and the
	 * name of the file where the test is specified" (relative to the directory
	 * of the version). You can find a list of all tests that are" executed by
	 * AspectJ in a file called 'testresults.xml' in the root folder for each
	 * version. You can refer to these files to find test and file names for
	 * most of the tests in AspectJ. This target handles both JUnit and harness
	 * tests from AspectJ. Upon successful execution," you will find an ant
	 * script in file 'org.aspectj/modules/tests/runtest.xml'. You can use this"
	 * script to execute the test by typing 'ant -f runtest.xml' in folder
	 * 'org.aspectj/modules/test'." If the task fails to locate the test, an
	 * invalid script may be generated. If you cannot execute" the generated
	 * script, make sure that you specified an exisiting test."
	 *
	 * @param aFixedId
	 *            The id of the bug for the repository that should be build
	 * @param aTargetPreFix
	 *            If true the pre fix repository will be targeted else the post
	 *            fix one is used
	 * @param aTestFileName
	 *            the name of the test file (relative to the base directory of
	 *            the version, e.g. 'org.aspectj/modules/tests/ajcTests.xml'
	 * @param aTestName
	 *            the name of the test to execute
	 * @param aVMArgs
	 *            Arguments that are to be passed to the virtual machine when
	 *            executing a test. This is useful for dynamic analyses that
	 *            require special parameters.
	 * @return a string that can be used to generate a test script or null if
	 *         something went wrong
	 */
	public String generateTestScriptCmd(String aFixedId, boolean aTargetPreFix, String aTestFileName, String aTestName,
			String aVMArgs) {
		String result = ANT_EXE + ANT_COMMAND_GEN_TEST_SCRIPT + aFixedId;

		if (aTargetPreFix) {
			result += ANT_COMMAND_PRE_BUILD;
		} else {
			result += ANT_COMMAND_POST_BUILD;
		}

		if (aTestFileName != null && aTestFileName.length() > 0) {
			File testFile = new File(aTestFileName);
			if (!testFile.exists()) {
				Log.err(this, "The test file " + aTestFileName + " does not exist");
				return null;
			} else {
				// saver would be to use the absolute path of the file object
				// but the description mentions
				// that the path should be relative to the base directory
				result +=  ANT_COMMAND_GEN_TEST_FILE_NAME + aTestFileName;
			}

			result += " " + aTestFileName.trim();
		} else {
			Log.err(this, "Provide a valid test file for the generation of the test script");
			return null;
		}

		if (aTestName != null && aTestName.length() > 0) {
			result += ANT_COMMAND_GEN_TEST_NAME + aTestName;
		} else {
			Log.err(this, "Provide a valid test name for the generation of the test script");
			return null;
		}

		if (aVMArgs != null && aVMArgs.length() > 0) {
			// if this argument is not set it will just not be added to the
			// command since it is not mandatory I think
			result += ANT_COMMAND_GEN_TEST_VMARGS + aVMArgs;
		}

		return result;
	}

}
