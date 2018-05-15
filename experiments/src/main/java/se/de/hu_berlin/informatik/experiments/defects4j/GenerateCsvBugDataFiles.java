/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.modification.Modification;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingUtils;
import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingUtils.SourceCodeBlockRankingMetrics;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.experiments.ranking.MarkedRanking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.RankingMetric;
import se.de.hu_berlin.informatik.utils.files.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.files.processors.ListToFileWriter;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.basics.ThreadedProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;
import se.de.hu_berlin.informatik.utils.processors.sockets.pipe.PipeLinker;

/**
 * Generates csv files that store information about SBFL bug data.
 * 
 * @author SimHigh
 */
public class GenerateCsvBugDataFiles {

	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		SUFFIX("s", "suffix", true, "A ranking directory suffix, if existing.", false),

		LOCALIZERS(Option.builder("l").longOpt("localizers").required(false).hasArgs().desc(
				"A list of localizers (e.g. 'Tarantula', 'Jaccard', ...). If not set, "
						+ "the localizers will be retrieved from the properties file.")
				.build()),
		OUTPUT("o", "output", true, "Path to output directory in which csv files will be stored.", true);

		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;

		// adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description,
				final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).hasArg(hasArg).desc(description).build(),
					NO_GROUP);
		}

		// adds an option that is part of the group with the specified index
		// (positive integer)
		// a negative index means that this option is part of no group
		// this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, final boolean hasArg, final String description,
				int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).hasArg(hasArg).desc(description).build(),
					groupId);
		}

		// adds an option that may have arguments from a given set (Enum)
		<T extends Enum<T>> CmdOptions(final String opt, final String longOpt, Class<T> valueSet, T defaultValue,
				final String description, final boolean required) {
			if (defaultValue == null) {
				this.option = new OptionWrapper(Option.builder(opt).longOpt(longOpt).required(required).hasArgs()
						.desc(description + " Possible arguments: " + Misc.enumToString(valueSet) + ".").build(),
						NO_GROUP);
			} else {
				this.option = new OptionWrapper(
						Option.builder(opt).longOpt(longOpt).required(required).hasArg(true).desc(
								description + " Possible arguments: " + Misc.enumToString(valueSet) + ". Default: "
										+ defaultValue.toString() + ".")
								.build(),
						NO_GROUP);
			}
		}

		// adds the given option that will be part of the group with the given
		// id
		CmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}

		// adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override
		public String toString() {
			return option.getOption().getOpt();
		}

		@Override
		public OptionWrapper getOptionWrapper() {
			return option;
		}
	}

	/**
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {

		OptionParser options = OptionParser.getOptions("GenerateCsvBugDataFiles", true, CmdOptions.class, args);

		// AbstractEntity mainEntity = Defects4JEntity.getDummyEntity();
		//
		// File archiveMainDir = mainEntity.getBenchmarkDir(false).toFile();
		//
		// if (!archiveMainDir.exists()) {
		// Log.abort(GenerateSpectraArchive.class,
		// "Archive main directory doesn't exist: '" +
		// mainEntity.getBenchmarkDir(false) + "'.");
		// }

		// get the output path (does not need to exist)
		Path output = options.isDirectory(CmdOptions.OUTPUT, false);

		String suffix = options.getOptionValue(CmdOptions.SUFFIX, null);

		String[] localizers = options.getOptionValues(CmdOptions.LOCALIZERS);
		if (localizers == null) {
			localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
		}

		if (localizers.length < 1) {
			Log.abort(GenerateCsvBugDataFiles.class, "No localizers given.");
		}

		// bug size data
		{
			PipeLinker linker = new PipeLinker().append(
					new ThreadedProcessor<>(options.getNumberOfThreads(),
							new RankingLOCProcessor(suffix, localizers[0])),
					new AbstractProcessor<String, List<String>>() {

						Map<String, String> map = new HashMap<>();

						@Override
						public List<String> processItem(String item) {
							map.put(item.split(",")[0], item);
							return null;
						}

						@Override
						public List<String> getResultFromCollectedItems() {

							// BugID, Line, EF, EP, NF, NP, BestRanking,
							// WorstRanking, MinWastedEffort, MaxWastedEffort,
							// Suspiciousness

							map.put("", "BugID,LOC");
							return Misc.sortByKeyToValueList(map);
						}
					}, new ListToFileWriter<List<String>>(output.resolve("bugsize").resolve("bugsize.csv"), true));

			// iterate over all projects
			for (String project : Defects4J.getAllProjects()) {
				String[] ids = Defects4J.getAllBugIDs(project);
				for (String id : ids) {
					linker.submit(new Defects4JBuggyFixedEntity(project, id));
				}
			}
			linker.shutdown();
		}

		// bug data
		for (String localizer : localizers) {
			Log.out(GenerateCsvBugDataFiles.class, "Processing %s.", localizer);
			PipeLinker linker2 = new PipeLinker().append(
					new ThreadedProcessor<>(options.getNumberOfThreads(),
							new GenStatisticsProcessor(suffix, localizer)),
					new AbstractProcessor<Pair<String, String[]>, List<String>>() {

						Map<String, String> map = new HashMap<>();

						@Override
						public List<String> processItem(Pair<String, String[]> item) {
							map.put(item.first(), CSVUtils.toCsvLine(item.second()));
							return null;
						}

						@Override
						public List<String> getResultFromCollectedItems() {

							// BugID, Line, EF, EP, NF, NP, BestRanking,
							// WorstRanking, MinWastedEffort, MaxWastedEffort,
							// Suspiciousness,
							// MinFiles, MaxFiles, MinMethods, MaxMethods

							String[] titleArray = { "BugID", "Line", "EF", "EP", "NF", "NP", "BestRanking",
									"WorstRanking", "MinWastedEffort", "MaxWastedEffort", "Suspiciousness",
									"MinFiles", "MaxFiles", "MinMethods", "MaxMethods" };
							map.put("", CSVUtils.toCsvLine(titleArray));
							return Misc.sortByKeyToValueList(map);
						}
					},
					new ListToFileWriter<List<String>>(output.resolve("faultData").resolve(localizer + ".csv"), true));

			// iterate over all projects
			for (String project : Defects4J.getAllProjects()) {
				String[] ids = Defects4J.getAllBugIDs(project);
				for (String id : ids) {
					linker2.submit(new Defects4JBuggyFixedEntity(project, id));
				}
			}
			linker2.shutdown();

		}

		Log.out(GenerateCsvBugDataFiles.class, "All done!");

	}

	private static class GenStatisticsProcessor extends AbstractProcessor<BuggyFixedEntity<?>, Pair<String, String[]>> {

		final private String rankingIdentifier;
		private String suffix;

		/**
		 * @param suffix
		 * a suffix to append to the ranking directory (may be null)
		 * @param rankingIdentifier
		 * a fault localizer identifier or an lm ranking file name
		 */
		private GenStatisticsProcessor(String suffix, String rankingIdentifier) {
			this.suffix = suffix;
			this.rankingIdentifier = rankingIdentifier;
		}

		@Override
		public Pair<String, String[]> processItem(BuggyFixedEntity<?> entity, ProcessorSocket<BuggyFixedEntity<?>, Pair<String, String[]>> socket) {
			Log.out(GenerateCsvBugDataFiles.class, "Processing %s.", entity);
			Entity bug = entity.getBuggyVersion();

			Map<String, List<Modification>> changeInformation = entity.loadChangesFromFile();

			Ranking<SourceCodeBlock> ranking = RankingUtils.getRanking(bug, suffix, rankingIdentifier);
			if (ranking == null) {
				Log.abort(this, "Found no ranking with identifier '%s'.", rankingIdentifier);
			}

			MarkedRanking<SourceCodeBlock, List<Modification>> markedRanking = new MarkedRanking<>(ranking);

			List<Modification> ignoreList = new ArrayList<>();
			for (SourceCodeBlock block : markedRanking.getElements()) {
				List<Modification> list = Modification.getModifications(
						block.getFilePath(), block.getStartLineNumber(), block.getEndLineNumber(), true,
						changeInformation, ignoreList);
				// found changes for this line? then mark the line with the
				// change(s)...
				if (list != null && !list.isEmpty()) {
					markedRanking.markElementWith(block, list);
				}
			}

			// BugID, Line, EF, EP, NF, NP, BestRanking, WorstRanking,
			// MinWastedEffort, MaxWastedEffort, Suspiciousness,
			// MinFiles, MaxFiles, MinMethods, MaxMethods

			String bugIdentifier = bug.getUniqueIdentifier();

			int count = 0;
			for (SourceCodeBlock changedElement : markedRanking.getMarkedElements()) {
				String[] line = new String[15];
				RankingMetric<SourceCodeBlock> metric = ranking.getRankingMetrics(changedElement);
				SourceCodeBlockRankingMetrics scbMetric = RankingUtils.getSourceCodeBlockRankingMetrics(ranking, changedElement);
				
				// List<ChangeWrapper> changes =
				// markedRanking.getMarker(changedElement);

				line[0] = bugIdentifier;
				line[1] = changedElement.getShortIdentifier();
				line[2] = "0"; // TODO: no info about spectra in rankings...
				line[3] = "0";
				line[4] = "0";
				line[5] = "0";
				line[6] = Integer.toString(metric.getBestRanking());
				line[7] = Integer.toString(metric.getWorstRanking());
				line[8] = Double.toString(metric.getMinWastedEffort());
				line[9] = Double.toString(metric.getMaxWastedEffort());
				line[10] = Double.toString(metric.getRankingValue());
				
				line[11] = Integer.toString(scbMetric.getMinFiles());
				line[12] = Integer.toString(scbMetric.getMaxFiles());
				line[13] = Integer.toString(scbMetric.getMinMethods());
				line[14] = Integer.toString(scbMetric.getMaxMethods());

				socket.produce(new Pair<>(bugIdentifier + count, line));
				++count;
			}

			return null;
		}
	}

	private static class RankingLOCProcessor extends AbstractProcessor<BuggyFixedEntity<?>, String> {

		final private String rankingIdentifier;
		private String suffix;

		/**
		 * @param suffix
		 * a suffix to append to the ranking directory (may be null)
		 * @param rankingIdentifier
		 * a fault localizer identifier or an lm ranking file name
		 */
		private RankingLOCProcessor(String suffix, String rankingIdentifier) {
			this.suffix = suffix;
			this.rankingIdentifier = rankingIdentifier;
		}

		@Override
		public String processItem(BuggyFixedEntity<?> entity, ProcessorSocket<BuggyFixedEntity<?>, String> socket) {
			Log.out(GenerateCsvBugDataFiles.class, "Processing %s for general data.", entity);
			Entity bug = entity.getBuggyVersion();

			Ranking<SourceCodeBlock> ranking = RankingUtils.getRanking(bug, suffix, rankingIdentifier);
			if (ranking == null) {
				Log.abort(this, "Found no ranking with identifier '%s'.", rankingIdentifier);
			}

			// BugID, Line, IF, IS, NF, NS, BestRanking, WorstRanking,
			// MinWastedEffort, MaxWastedEffort, Suspiciousness

			String bugIdentifier = bug.getUniqueIdentifier();

			return bugIdentifier + "," + Integer.toString(ranking.getElements().size());
		}
	}

}
