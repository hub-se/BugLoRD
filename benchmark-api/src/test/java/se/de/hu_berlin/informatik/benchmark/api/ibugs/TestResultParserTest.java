package se.de.hu_berlin.informatik.benchmark.api.ibugs;

import java.util.List;

import se.de.hu_berlin.informatik.benchmark.api.ibugs.parser.IBugsTestResultWrapper;
import se.de.hu_berlin.informatik.benchmark.api.ibugs.parser.IBugsTestSuiteWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Just a simple test class to see if the parser works and if the results are usable.
 * This is not supposed to be executed automatically since it requires the path to the
 * file that should be parsed.
 * 
 * @author Roy Lieck
 *
 */
public class TestResultParserTest {

	/**
	 * @param args args
	 */
	public static void main(String[] args) {
		TestResultParserTest trpt = new TestResultParserTest();
		trpt.doAction(args);	
	}
	
	/**
	 * Non static entry method
	 * @param args args
	 */
	public void doAction( String[] args ) {
		// no checks for valid arguments since I know how i want to call it
		String projectName = "AspectJ";
		String projectRoot = args[0];
		String fixedId = args[1];
		IBugsBuggyFixedEntity buggyFixedE = new IBugsBuggyFixedEntity( projectName, projectRoot, fixedId );

		IBugsTestSuiteWrapper ibtswb = buggyFixedE.getBuggyVersion().parseTestResultsFile();
		IBugsTestSuiteWrapper ibtswf = buggyFixedE.getFixedVersion().parseTestResultsFile();
		
		Log.out( this, "Passing: " + ibtswb.getPassing() + " / " + ibtswf.getPassing() );
		Log.out( this, "Failing: " + ibtswb.getFailing() + " / " + ibtswf.getFailing() );
		Log.out( this, "Size: " + ibtswb.getSize() + " / " + ibtswf.getSize() );
		Log.out( this, "Size (No error): " + ibtswb.getAllTests().size() + " / " + ibtswf.getAllTests().size() );
		Log.out( this, "Size (Error): " + ibtswb.getAllTestsWithErrors().size() + " / " + ibtswf.getAllTestsWithErrors().size() );
		printRndEntry( ibtswb.getAllTests() );
		printRndEntry( ibtswb.getAllTestsWithErrors() );
		printRndEntry( ibtswf.getAllTests() );
		printRndEntry( ibtswf.getAllTestsWithErrors() );
	}
	
	/**
	 * Prints the attributes of a random entry from the list
	 * @param aTestResultList bla
	 */
	private void printRndEntry( List<IBugsTestResultWrapper> aTestResultList ) {
		int rndIdx = (int) (Math.random() * aTestResultList.size() );
		IBugsTestResultWrapper trw = aTestResultList.get( rndIdx );
		Log.out( this, "RndIdx(" + rndIdx + "):" + trw.toString() );
	}

}
