package se.de.hu_berlin.informatik.benchmark.api.ibugs;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.AbstractEntity;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.SystemUtils;

public class IBugsEntity extends AbstractEntity {

	// this is the identifier for the iBugsEntity
	private final String fixedId;
	private final boolean buggyVersion;
	private final String project;
	
	// the project root needs to have the build.xml that is the target for the ant calls
	private final File projectRoot;

	private IBugs utils = new IBugs();

	public static IBugsEntity getBuggyIBugsEntity( String aProject, String aProjectRoot, String aFixedId ) {
		return new IBugsEntity( aFixedId, aProject, aProjectRoot, true );
	}
	
	public static IBugsEntity getFixedIBugsEntity( String aProject, String aProjectRoot, String aFixedId ) {
		return new IBugsEntity( aFixedId, aProject, aProjectRoot, false );
	}
	
	private IBugsEntity(String aFixedId, String aProject, String aProjectRoot, boolean aBuggy ) {
		super( new IBugsDirectoryProvider( aProject, aProjectRoot, aFixedId, aBuggy ) );
		
		if( aFixedId == null || aFixedId.length() == 0 ) {
			Log.err( this, "Invalid fixed id!" );
		}
		
		fixedId = aFixedId;
		buggyVersion = aBuggy;
		project = aProject;
		projectRoot = new File( aProjectRoot );
		
		if ( !projectRoot.exists() ) {
			Log.err( this, "The root directory of the project does not exist: " + aProject );
		}
	}

	@Override
	public boolean compile(boolean executionMode) {
		String antCmd = "";
		
		if( buggyVersion ) {
			antCmd = utils.generatePreBuildCmd(fixedId);
			Log.out(this, "Compiling classes for pre fixed repository using command: " + antCmd );
		} else {
			antCmd = utils.generatePostBuildCmd( fixedId );
			Log.out(this, "Compiling classes for post fixed repository using command: " + antCmd );
		}
		
		String[] args = antCmd.split( " " );	
		SystemUtils.executeCommand( projectRoot, args );
		
		// this would be the save way if the string array does not work
//		SystemUtils.executeCommand( projectRoot, "ant", "buildversion", "-DfixId=" + fixedId, IBugs.ANT_COMMAND_PRE_BUILD);
		
		// currently this will always build both the normal and the test classes
		compileTests( executionMode );
		
		return true;
	}

	/**
	 * Builds the test classes for the repository.
	 * 
	 * @param executionMode currently not used. Specified the archive or execution directory for d4j
	 * @return true
	 */
	public boolean compileTests( boolean executionMode ) {
		String antCmd = "";
		
		if( buggyVersion ) {
			antCmd = utils.generatePreBuildTestsCmd(fixedId);
			Log.out(this, "Compiling test classes for pre fixed repository using command: " + antCmd );
		} else {
			antCmd = utils.generatePostBuildTestsCmd( fixedId );
			Log.out(this, "Compiling test classes for post fixed repository using command: " + antCmd );
		}
		
		String[] args = antCmd.split( " " );	
		SystemUtils.executeCommand( projectRoot, args );
		
		return true;
	}
	
	/**
	 * There is a special set of tests called harness tests that can be executed
	 * @return true
	 */
	public boolean runHarnessTests() {
		String antCmd = "";
		
		if( buggyVersion ) {
			antCmd = utils.generateRunHarnessTestsPreFixCmd(fixedId);
			Log.out(this, "Running harness tests for pre fixed repository using command: " + antCmd );
		} else {
			antCmd = utils.generateRunHarnessTestsPostFixCmd(fixedId);
			Log.out(this, "Running harness tests for post fixed repository using command: " + antCmd );
		}
		
		String[] args = antCmd.split( " " );	
		SystemUtils.executeCommand( projectRoot, args );
		
		return true;
	}
	
	/**
	 * There is a special set of tests called harness tests that can be executed
	 * @return true
	 */
	public boolean runJUnitTests() {
		String antCmd = "";
		
		if( buggyVersion ) {
			antCmd = utils.generateRunJUnitTestsPreFixCmd(fixedId);
			Log.out(this, "Running jUnit tests for pre fixed repository using command: " + antCmd );
		} else {
			antCmd = utils.generateRunJUnitTestsPostFixCmd(fixedId);
			Log.out(this, "Running jUnit tests for post fixed repository using command: " + antCmd );
		}
		
		String[] args = antCmd.split( " " );	
		SystemUtils.executeCommand( projectRoot, args );
		
		return true;
	}
	
	/**
	 * This will probably never be implemented and used
	 * @return true
	 */
	public boolean genTestScript() {
		Log.out( this, "The generation of the test scripts is currently not implemented" );
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
	 * If this is the buggy entity a checkout will be performed that downloads the  pre
	 * and the post fix version for the fixed id.
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
		
		if( buggyVersion ) {
			
			// first check if the destination directory already exists
			// not using the delete method from the normal directory provider because this is not defects4j with archive and execution dir
			File entity_root = getDirectoryProvider().getEntityDir( true ).toFile();
			if( entity_root.exists() ) {
				// delete from the parent to delete the fix id directory as well
				FileUtils.delete(entity_root.getParentFile());
			}
			
			antCmd = utils.generateCheckoutCmd( fixedId );
		} else {
			// only the entity that represents the buggy version performs the checkout because it downloads
			// always both versions
			return;
		}

		Log.out(this, "Checking out pre and postfix repository using command: " + antCmd );
		
		String[] args = antCmd.split( " " );	
		SystemUtils.executeCommand( projectRoot, args );
	}

	@Override
	public String computeClassPath(boolean executionMode) throws UnsupportedOperationException {
		// TODO find a way to get the class path for a repository
		return null;
	}

	@Override
	public String computeTestClassPath(boolean executionMode) throws UnsupportedOperationException {
		// TODO find a way to get the class path for the execution of the test cases
		return null;
	}

	@Override
	public List<String> computeTestCases(boolean executionMode) throws UnsupportedOperationException {
		// TODO throws unsupported operation exception in defects4j but should be a list of test cases I guess
		return null;
	}

	@Override
	public List<Path> computeTestClasses(boolean executionMode) throws UnsupportedOperationException {
		// TODO find a way to get the list of tests that should be executed
		// they are in the xml files I guess
		return null;
	}
	
	public String toString() {
		String sep = "-";
		String buggy = buggyVersion ? IBugs.PRE_FIX : IBugs.POST_FIX;
		return project + sep + fixedId + sep + buggy;
	}

}
