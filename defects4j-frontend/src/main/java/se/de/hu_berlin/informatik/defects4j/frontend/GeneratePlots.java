/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend;

import java.io.File;
import java.util.Arrays;
import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.defects4j.frontend.plot.PlotAverageCall;
import se.de.hu_berlin.informatik.defects4j.frontend.plot.PlotAverageIgnoreZeroCall;
import se.de.hu_berlin.informatik.defects4j.frontend.plot.PlotSingleElementCall;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.threaded.ExecutorServiceProvider;
import se.de.hu_berlin.informatik.utils.tm.modules.ThreadedListProcessorModule;

/**
 * Generates plots of the experiments' results.
 * 
 * @author SimHigh
 */
public class GeneratePlots {
	
//	private final static String SEP = File.separator;
	
	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
		final String tool_usage = "GeneratePlots";
		final OptionParser options = new OptionParser(tool_usage, args);

		options.add(Option.builder(Prop.OPT_PROJECT).longOpt("projects").hasArgs()
        		.desc("A list of projects to consider of the Defects4J benchmark. "
        		+ "Should be either 'Lang', 'Chart', 'Time', 'Closure' or 'Math'. Set this to 'all' to "
        		+ "iterate over all projects.").build());
        options.add(Option.builder(Prop.OPT_BUG_ID).longOpt("bugIDs").hasArgs()
        		.desc("A list of numbers indicating the ids of buggy project versions to consider. "
        		+ "Value ranges differ based on the project. Set this to 'all' to "
        		+ "iterate over all bugs in a project.").build());
 
        final Option thread_opt = new Option("t", "threads", true, "Number of threads to run "
        		+ "experiments in parallel. (Default is 1.)");
		thread_opt.setOptionalArg(true);
		thread_opt.setType(Integer.class);
		options.add(thread_opt);
		
//        options.add(Option.builder(Prop.OPT_LOCALIZERS).longOpt("localizers").required().hasArgs()
//        		.desc("A list of identifiers of Cobertura localizers (e.g. 'Tarantula', 'Jaccard', ...) "
//        				+ "for which plots shall be generated.")
//				.build());
        
        options.add("s", "singleElementPlots", false, "Whether to plot single plots for each Defects4J element "
        		+ "that show the ranks of faulty code lines for the given localizer(s).");
        options.add("a", "averagePlots", false, "Whether to plot average plots for each Defects4J project.");
        options.add("az", "averagePlotsNoZero", false, "Whether to plot average plots for each Defects4J project "
        		+ "and ignore data points with a ranking of zero or below.");
        
        options.add("strat", "parserStrategy", true, "What strategy should be used when encountering a range of"
				+ "equal rankings. Options are: 'BEST', 'WORST', 'NOCHANGE' and 'AVERAGE'. Default is 'AVERAGE'.");
        
        options.add("o", "outputDir", true, "Main plot output directory.", false);
        
        options.parseCommandLine();
        
        return options;
	}
	
	
	/**
	 * @param args
	 * -p project -b bugID
	 */
	public static void main(String[] args) {
		
		OptionParser options = getOptions(args);	
		
		String[] projects = options.getOptionValues(Prop.OPT_PROJECT);
		boolean allProjects = false;
		if (projects != null) {
			allProjects = projects[0].equals("all");
		} else {
			projects = new String[0];
		}
		
		String[] ids = options.getOptionValues(Prop.OPT_BUG_ID);
		boolean allIDs = false;
		if (ids != null) {
			allIDs = ids[0].equals("all");
		} else {
			ids = new String[0];
		}
		
		String output = options.getOptionValue('o', null);
		if (output != null && (new File(output)).isFile()) {
			Log.abort(GeneratePlots.class, "Given output path '%s' is a file.", output);
		}
		
		ParserStrategy strategy = ParserStrategy.AVERAGE_CASE;
		if (options.hasOption("strat")) {
			switch(options.getOptionValue("strat")) {
			case Plotter.STRAT_BEST:
				strategy = ParserStrategy.BEST_CASE;
				break;
			case Plotter.STRAT_WORST:
				strategy = ParserStrategy.WORST_CASE;
				break;
			case Plotter.STRAT_AVERAGE:
				strategy = ParserStrategy.AVERAGE_CASE;
				break;
			case Plotter.STRAT_NOCHANGE:
				strategy = ParserStrategy.NO_CHANGE;
				break;
			default:
				Log.abort(GeneratePlots.class, "Unknown strategy: '%s'", options.getOptionValue("strat"));
			}
		}
		
		//this is important!!
		Prop prop = new Prop().loadProperties();
		
//		String[] localizers = options.getOptionValues(Prop.OPT_LOCALIZERS);
		String[] localizers = prop.localizers.split(" ");
				
		int threadCount = 1;
		if (options.hasOption('t')) {
			//parse number of threads
			threadCount = Integer.parseInt(options.getOptionValue('t', "1"));
		}

		ExecutorServiceProvider executor = new ExecutorServiceProvider(threadCount);
		
		if (allProjects) {
			projects = Prop.getAllProjects();
		}
		
		if (options.hasOption("s")) {	
			//iterate over all projects
			for (String project : projects) {
				if (allIDs) {
					ids = Prop.getAllBugIDs(project); 
				}

				for (String localizer : localizers) {
					String[] temp = { localizer };
					new ThreadedListProcessorModule<String>(executor.getExecutorService(), 
							PlotSingleElementCall.class, project, temp, output)
					.submit(Arrays.asList(ids));
				}
			}
		}
		
		if (options.hasOption("a")) {
			for (String localizer : localizers) {
				String[] temp = { localizer };
				new ThreadedListProcessorModule<String>(executor.getExecutorService(), 
						PlotAverageCall.class, strategy, temp, output)
				.submit(Arrays.asList(projects));
			}
		}
		
		if (options.hasOption("az")) {
			for (String localizer : localizers) {
				String[] temp = { localizer };
				new ThreadedListProcessorModule<String>(executor.getExecutorService(), 
						PlotAverageIgnoreZeroCall.class, strategy, temp, output)
				.submit(Arrays.asList(projects));
			}
		}
		
		executor.shutdownAndWaitForTermination();
	}
	
}
