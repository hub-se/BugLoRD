/**
 * 
 */
package se.de.hu_berlin.informatik.aspectj.frontend;

import java.io.IOException;
import org.jdom.JDOMException;

import se.de.hu_berlin.informatik.aspectj.frontend.evaluation.sbfl.CreateRankingsFromSpectra;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;


/**
 * 
 * @author Simon Heiden
 */
public class ExperimentRunner {

	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) { 
		final String tool_usage = "ExperimentRunner";
		final OptionParser options = new OptionParser(tool_usage, true, args);

//        options.add(Option.builder(Prop.OPT_PROJECT).longOpt("projects").required().hasArgs()
//        		.desc("A list of projects to consider of the Defects4J benchmark. "
//        		+ "Should be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'.").build());
//        options.add(Option.builder(Prop.OPT_BUG_ID).longOpt("bugIDs").required().hasArgs()
//        		.desc("A list of numbers indicating the ids of buggy project versions to consider. "
//        		+ "Value ranges differ based on the project. Set this to 'all' to "
//        		+ "iterate over all bugs in a project.").build());
//        
//		
//        options.add(Option.builder(Prop.OPT_LOCALIZERS).longOpt("localizers").optionalArg(true).hasArgs()
//        		.desc("A list of identifiers of Cobertura localizers (e.g. 'Tarantula', 'Jaccard', ...).")
//				.build());
        
        options.parseCommandLine();
        
        return options;
	}

	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {
		
		OptionParser options = getOptions(args);	
		
//		String[] projects = options.getOptionValues(Prop.OPT_PROJECT);
//		String[] ids = options.getOptionValues(Prop.OPT_BUG_ID);
//		String[] localizers = options.getOptionValues(Prop.OPT_LOCALIZERS);
//		boolean all = ids[0].equals("all");

		int threadCount = options.getNumberOfThreads();

		try {
			new CreateRankingsFromSpectra(threadCount, false).run();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
}
