/**
 * 
 */
package se.de.hu_berlin.informatik.benchmark.api.ibugs;

/**
 * Just a simple test class to see if the execution of the harness tests works and if the results are usable.
 * This is not supposed to be executed automatically since it requires the path to the
 * file that should be parsed.
 * 
 * @author Roy Lieck
 *
 */
public class TestParseHarnessTestResults {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestParseHarnessTestResults testCPB = new TestParseHarnessTestResults();
		testCPB.doAction( args );
	}
	
	/**
	 * Non static entry method
	 * 
	 * @param args Arguments
	 */
	public void doAction( String[] args ) {
		// no checks for valid arguments since I know how i want to call it
		String projectName = "AspectJ";
		String projectRoot = args[0];
		String fixedId = args[1];
		IBugsBuggyFixedEntity buggyFixedE = new IBugsBuggyFixedEntity( projectName, projectRoot, fixedId );
		
		IBugsEntity buggyE = (IBugsEntity) buggyFixedE.getBuggyVersion();
		buggyE.parseHarnessTestResultFile();
	}

}
