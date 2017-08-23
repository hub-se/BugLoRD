/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.ibugs;

import java.util.Collection;

import se.de.hu_berlin.informatik.benchmark.api.ibugs.IBugs;
import se.de.hu_berlin.informatik.benchmark.api.ibugs.IBugsBuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.ibugs.IBugsOptions.IBugsCmdOptions;
import se.de.hu_berlin.informatik.experiments.ibugs.calls.ERIBugsBuild;
import se.de.hu_berlin.informatik.experiments.ibugs.calls.ERIBugsBuildTests;
import se.de.hu_berlin.informatik.experiments.ibugs.calls.ERIBugsCheckoutBugAndFix;
import se.de.hu_berlin.informatik.experiments.ibugs.calls.ERIBugsGenTestScript;
import se.de.hu_berlin.informatik.experiments.ibugs.calls.ERIBugsRunHarness;
import se.de.hu_berlin.informatik.experiments.ibugs.calls.ERIBugsRunJUnit;
import se.de.hu_berlin.informatik.experiments.ibugs.utils.BugDataFromRDWrapper;
import se.de.hu_berlin.informatik.experiments.ibugs.utils.IBugsPropertiesXMLParser;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.processors.basics.ThreadedProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;
import se.de.hu_berlin.informatik.utils.threaded.SemaphoreThreadLimit;
import se.de.hu_berlin.informatik.utils.threaded.ThreadLimit;

/**
 * Main class to work with a data set from iBugs.
 * To work with the experiment runner one first needs to adjust the properties.xml
 * file inside the iBugs project directory. Especially the additional property
 * of the path to the ant exectuable needs to be set.
 * 
 * An example version of a valid properties.xml can be found in the resource directory.
 * 
 * @author Roy Lieck
 *
 */
public class ExperimentRunnerIBugs {
	
	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {
		ExperimentRunnerIBugs erib = new ExperimentRunnerIBugs();
		erib.doAction( args );
	}
	
	/**
	 * Non static entry method.
	 * 
	 * @param args Program arguments
	 */
	public void doAction( String[] args ) {
		OptionParser options = OptionParser.getOptions("ExperimentRunnerIBugs", true, IBugsCmdOptions.class, args);
		
		int threadCount = options.getNumberOfThreads();
		Log.out( this, "Using %d parallel threads.", threadCount);
		
		PipeLinker linker = new PipeLinker();
		ThreadLimit limit = new SemaphoreThreadLimit(threadCount);

		// read the path to the ant executable from the properties file and set it for future use
		IBugsPropertiesXMLParser parser = new IBugsPropertiesXMLParser();
		parser.parseXMLProps( options );

		appendAllModulesToLinker( options, linker, threadCount, limit );
		submitAllIds( options, linker);

		linker.shutdown();
	}
	
	/**
	 * Adds all modules to the linker that are needed to execute the targets specified in
	 * the option parser object.
	 * 
	 * @param aOP The options from the command line
	 * @param aLinker The linker for the modules
	 * @param aThreadCount The number of threads
	 * @param aThreadLimit The thread limit object
	 */
	private void appendAllModulesToLinker( OptionParser aOP, PipeLinker aLinker, int aThreadCount, ThreadLimit aThreadLimit ) {
		if ( aOP.hasOption( IBugsCmdOptions.CHECKOUT ) ) {
			// this will checkout the repositories
			aLinker.append(new ThreadedProcessor<>(aThreadCount, aThreadLimit, 
					new ERIBugsCheckoutBugAndFix()));
			// a debug level would be nice here
			Log.out( this, "Added the checkout module" );
		}
		
		if ( aOP.hasOption( IBugsCmdOptions.BUILD ) ) {
			// this will build the normal and the test classes together
			aLinker.append(new ThreadedProcessor<>(aThreadCount, aThreadLimit, 
					new ERIBugsBuild()));
			Log.out( this,  "Added the build module" );
		}
		
		if ( aOP.hasOption( IBugsCmdOptions.BUILD_TESTS ) ) {
			// this will only build the test classes
			aLinker.append(new ThreadedProcessor<>(aThreadCount, aThreadLimit, 
					new ERIBugsBuildTests()));
			Log.out( this,  "Added the build tests module" );
		}
		
		if ( aOP.hasOption( IBugsCmdOptions.GEN_TEST_SCRIPT ) ) {
			// this will generate different test scripts
			aLinker.append(new ThreadedProcessor<>(aThreadCount, aThreadLimit, 
					new ERIBugsGenTestScript()));
			Log.out( this,  "Added the generate test script module" );
		}
		
		if ( aOP.hasOption( IBugsCmdOptions.RUN_JUNIT ) ) {
			// this will run the junit tests
			aLinker.append(new ThreadedProcessor<>(aThreadCount, aThreadLimit, 
					new ERIBugsRunJUnit()));
			Log.out( this,  "Added the junit test execution module" );
		}
		
		if ( aOP.hasOption( IBugsCmdOptions.RUN_HARNESS ) ) {
			// this will run the harness tests. Whatever they may be
			aLinker.append(new ThreadedProcessor<>(aThreadCount, aThreadLimit, 
					new ERIBugsRunHarness()));
			Log.out( this,  "Added the harness test execution module" );
		}
	}
	
	/**
	 * Submits all fix ids to the linker that are specified in the options
	 * 
	 * @param aOP The options from the command line
	 * @param aLinker The linker for the modules
	 */
	private void submitAllIds( OptionParser aOP, PipeLinker aLinker ) {
		String projectName = IBugs.DEFAULT_PROJECT;
		String projectRoot = aOP.getOptionValue( IBugsCmdOptions.PROJECT_ROOT_DIR );
		
		if( aOP.hasOption( IBugsCmdOptions.PROJECT ) ) {
			projectName = aOP.getOptionValue( IBugsCmdOptions.PROJECT );
		}
		
		// submit all fixed ids
		String[] ids = aOP.getOptionValue( IBugsCmdOptions.FIX_ID ).split( IBugs.LIST_SEPARATOR );
		if ( ids[0].equalsIgnoreCase( IBugs.USE_ALL_IDS) ) {
			
			// first try to get all valid fix ids from the repository descriptor file
			IBugsPropertiesXMLParser parser = new IBugsPropertiesXMLParser();
			Collection<BugDataFromRDWrapper> allValidFixIds = parser.parseRepoDescriptor( projectRoot );
			
			if( allValidFixIds == null || allValidFixIds.size() == 0 ) {
				// this is an incomplete list which we will fallback in case something
				// is wrong with the bug ids in the repository descriptor
				Log.out( this, "Could not extract the fix ids from the repository descriptor. Using an incomplete list instead." );
				ids = IBugs.getAllFixedIdsForProject( projectName );
				
				for( String fixId : ids ) {
					aLinker.submit(new IBugsBuggyFixedEntity(projectName, projectRoot, fixId ));
				}
				
			} else {
				// use the complete list
				Log.out( this, "Found " + allValidFixIds.size() + " fix ids in the repository descriptor to checkout" );
				
				for( BugDataFromRDWrapper fixId : allValidFixIds ) {
					aLinker.submit(new IBugsBuggyFixedEntity(projectName, projectRoot, fixId.getBugId() ));
				}
			}
			
		} else {
			// the target is not to checkout all repositories but only a few that are specified already
			for( String fixId : ids ) {
				aLinker.submit(new IBugsBuggyFixedEntity(projectName, projectRoot, fixId ));
			}
		}
	}
	
}
