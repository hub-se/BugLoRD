/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.plot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.sbfl.spectra.jacoco.JaCoCoToSpectra;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsAPI;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsOptions;
import se.de.hu_berlin.informatik.utils.statistics.StatisticsCollector;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;

/**
 * Generates plots of the experiments' results.
 * 
 * @author SimHigh
 */
public class HyperbolicEvoCrossValidation {
	
	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		PROJECTS(Option.builder("p").longOpt("projects").hasArgs()
        		.desc("A list of projects to consider of the Defects4J benchmark. "
        		+ "Should be either 'Lang', 'Chart', 'Time', 'Closure', 'Mockito', 'Math' or "
        		+ "'super' for the super directory. Set this to 'all' to "
        		+ "iterate over all projects (and the super directory).").build()),

        SUFFIX("s", "suffix", true, "A suffix to append to the ranking directory.", false),
        
        LOCALIZERS(Option.builder("l").longOpt("localizers").required(false)
				.hasArgs().desc("A list of localizers (e.g. 'Tarantula', 'Jaccard', ...). If not set, "
						+ "the localizers will be retrieved from the properties file.").build()),
        
        CROSS_VALIDATION_SEED("cv", "cvSeed", true, "A seed to use for generating the buckets.", false),
        BUCKET_COUNT("bc", "bucketCount", true, "The number of buckets to create (default: 10).", false),

        OUTPUT("o", "outputDir", true, "Main plot output directory.", false);
        
		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;

		//adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).
					hasArg(hasArg).desc(description).build(), NO_GROUP);
		}
		
		//adds an option that is part of the group with the specified index (positive integer)
		//a negative index means that this option is part of no group
		//this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).
					hasArg(hasArg).desc(description).build(), groupId);
		}

		//adds an option that may have arguments from a given set (Enum)
		<T extends Enum<T>> CmdOptions(final String opt, final String longOpt, 
				Class<T> valueSet, T defaultValue, final String description, final boolean required) {
			if (defaultValue == null) {
				this.option = new OptionWrapper(
						Option.builder(opt).longOpt(longOpt).required(required).
						hasArgs().desc(description + " Possible arguments: " +
								Misc.enumToString(valueSet) + ".").build(), NO_GROUP);
			} else {
				this.option = new OptionWrapper(
						Option.builder(opt).longOpt(longOpt).required(required).
						hasArg(true).desc(description + " Possible arguments: " +
								Misc.enumToString(valueSet) + ". Default: " + 
								defaultValue.toString() + ".").build(), NO_GROUP);
			}
		}
		
		//adds the given option that will be part of the group with the given id
		CmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}
		
		//adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override public String toString() { return option.getOption().getOpt(); }
		@Override public OptionWrapper getOptionWrapper() { return option; }
	}
	
	public enum StatisticsData implements StatisticsAPI {
		RESULT_MSG("result messages", StatisticType.STRING, StatisticsOptions.CONCAT);

		final private String label;
		final private StatisticType type;
		final private StatisticsOptions[] options;
		private StatisticsData(String label, StatisticType type, StatisticsOptions... options) {
			this.label = label;
			this.type = type;
			this.options = options;
		}
		
		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public StatisticType getType() {
			return type;
		}

		@Override
		public StatisticsOptions[] getOptions() {
			return options;
		}
	}
	
	public static void main(String[] args) {
		
		OptionParser options = OptionParser.getOptions("HyperbolicEvoCrossValidation", true, CmdOptions.class, args);
		
		String[] projects = options.getOptionValues(CmdOptions.PROJECTS);
		boolean allProjects = false;
		if (projects != null) {
			allProjects = projects[0].equals("all");
		} else {
			projects = new String[0];
		}
		
		String output = options.getOptionValue(CmdOptions.OUTPUT, null);
		if (output != null && (new File(output)).isFile()) {
			Log.abort(HyperbolicEvoCrossValidation.class, "Given output path '%s' is a file.", output);
		}
		
		String[] localizers = options.getOptionValues(CmdOptions.LOCALIZERS);
		if (localizers == null) {
			localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
		}
			
		int threadCount = options.getNumberOfThreads();

		if (allProjects) {
			projects = Defects4J.getAllProjects();
		}
		
		String suffix = options.getOptionValue(CmdOptions.SUFFIX, null);
		
		String seedOption = options.getOptionValue(CmdOptions.CROSS_VALIDATION_SEED, "1234567890");

		Long seed = Long.valueOf(seedOption);
		int bc = Integer.valueOf(options.getOptionValue(CmdOptions.BUCKET_COUNT, "10"));
		for (String project : projects) {
			StatisticsCollector<StatisticsData> statContainer = new StatisticsCollector<>(StatisticsData.class);
			new HyperbolicBucketsEH(suffix, seed, bc, project, output, localizers, threadCount)
			.submit(statContainer);
			
			String stats = statContainer.printStatistics();
			try {
				FileUtils.writeStrings2File(Paths.get(output, project + "_stats").toFile(), stats);
			} catch (IOException e) {
				Log.err(HyperbolicEvoCrossValidation.class, "Can not write statistics to '%s'.", Paths.get(output, project + "_stats"));
			}
		}

	}
	
}
