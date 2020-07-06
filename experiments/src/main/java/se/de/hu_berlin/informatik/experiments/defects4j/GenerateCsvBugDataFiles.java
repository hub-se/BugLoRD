package se.de.hu_berlin.informatik.experiments.defects4j;

import org.apache.commons.cli.Option;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBase.Defects4JProject;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.modification.Modification;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.ToolSpecific;
import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingUtils;
import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingUtils.SourceCodeBlockRankingMetrics;
import se.de.hu_berlin.informatik.spectra.core.Node.NodeType;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranch;
import se.de.hu_berlin.informatik.utils.experiments.ranking.MarkedRanking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.RankingMetric;
import se.de.hu_berlin.informatik.utils.experiments.ranking.SimpleRanking;
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

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates csv files that store information about SBFL bug data.
 *
 * @author SimHigh
 */
public class GenerateCsvBugDataFiles {

    public enum CmdOptions implements OptionWrapperInterface {
        /* add options here according to your needs */
        SUFFIX("s", "suffix", true, "A ranking directory suffix, if existing.", false),
        SUFFIX2("s2", "suffix2", true, "A ranking directory suffix for a statement level ranking, if existing.", false),

        LOCALIZERS(Option.builder("l").longOpt("localizers").required(false).hasArgs().desc(
                "A list of localizers (e.g. 'Tarantula', 'Jaccard', ...). If not set, "
                        + "the localizers will be retrieved from the properties file.")
                .build()),
        SPECTRA_TOOL("st", "spectraTool", ToolSpecific.class, ToolSpecific.TRACE_COBERTURA,
                "What tool has been used to compute the rankings?.", false),
        OUTPUT("o", "output", true, "Path to output directory in which csv files will be stored.", true),
        FILL_EMPTY_LINES("f", "fill", false, "Whether empty lines between statements in the same method should be filled up.", false),
        LOC("loc", "loc", false, "Whether to generate LOC for each ranking/bug.", false);

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
     * @param args command line arguments
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
        String suffix2 = options.getOptionValue(CmdOptions.SUFFIX2, null);

        String[] localizers = options.getOptionValues(CmdOptions.LOCALIZERS);
        if (localizers == null) {
            localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
        }

        if (localizers.length < 1) {
            Log.abort(GenerateCsvBugDataFiles.class, "No localizers given.");
        }

        ToolSpecific toolSpecific = options.getOptionValue(CmdOptions.SPECTRA_TOOL,
                ToolSpecific.class, ToolSpecific.TRACE_COBERTURA, true);

        if (options.hasOption(CmdOptions.LOC)) {
        	// bug size data
        	{
        		PipeLinker linker = new PipeLinker().append(
        				new ThreadedProcessor<>(options.getNumberOfThreads(),
        						new RankingLOCProcessor(suffix, suffix2, localizers[0], toolSpecific)),
        				new AbstractProcessor<String, List<String>>() {

        					final Map<String, String> map = new HashMap<>();

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
        		for (Defects4JProject project : Defects4J.getAllProjects()) {
        			String[] ids = Defects4J.getAllActiveBugIDs(project);
        			for (String id : ids) {
        				linker.submit(new Defects4JBuggyFixedEntity(project, id));
        			}
        		}
        		linker.shutdown();
        	}
        }

        // bug data
        for (String localizer : localizers) {
            Log.out(GenerateCsvBugDataFiles.class, "Processing %s.", localizer);
            PipeLinker linker2 = new PipeLinker().append(
                    new ThreadedProcessor<>(options.getNumberOfThreads(),
                            new GenStatisticsProcessor(suffix, suffix2, localizer, toolSpecific, options.hasOption(CmdOptions.FILL_EMPTY_LINES))),
                    new AbstractProcessor<Pair<String, String[]>, List<String>>() {

                        final Map<String, String> map = new HashMap<>();

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

                            String[] titleArray = {"BugID", "Line", "EF", "EP", "NF", "NP", "BestRanking",
                                    "WorstRanking", "MinWastedEffort", "MaxWastedEffort", "Suspiciousness",
                                    "MinFiles", "MaxFiles", "MinMethods", "MaxMethods"};
                            map.put("", CSVUtils.toCsvLine(titleArray));
                            return Misc.sortByKeyToValueList(map);
                        }
                    },
                    new ListToFileWriter<List<String>>(output.resolve("faultData").resolve(localizer + ".csv"), true));

            // iterate over all projects
            for (Defects4JProject project : Defects4J.getAllProjects()) {
                String[] ids = Defects4J.getAllActiveBugIDs(project);
                for (String id : ids) {
                    linker2.submit(new Defects4JBuggyFixedEntity(project, id));
                }
            }
            linker2.shutdown();

        }

        Log.out(GenerateCsvBugDataFiles.class, "All done!");

    }

    protected static Ranking<SourceCodeBlock> generateStatementLevelRanking(Entity bug, ToolSpecific spectraTool, 
    		String suffix, String suffixStatement, String rankingIdentifier) {
        // generate a statement level ranking!
        Ranking<SourceCodeBlock> ranking = null;

        switch (spectraTool) {
            case BRANCH_SPECTRA:
                // convert the given branch level ranking to a statement level ranking
                Ranking<ProgramBranch> branchRanking = RankingUtils.getRanking(ProgramBranch.DUMMY, bug, suffix, rankingIdentifier);
                if (branchRanking == null) {
                    Log.abort(GenerateCsvBugDataFiles.class, "Found no branch level ranking with identifier '%s'.", rankingIdentifier);
                }
                
                // use statement level ranking (if any) for ranking within branches
                Ranking<SourceCodeBlock> statementRanking = null;
                if (suffixStatement != null) {
                	statementRanking = RankingUtils.getRanking(SourceCodeBlock.DUMMY, bug, suffixStatement, rankingIdentifier);
                	if (statementRanking == null) {
                        Log.abort(GenerateCsvBugDataFiles.class, "Found no statement level ranking with identifier '%s'.", rankingIdentifier);
                    }
                }
                
                ranking = new SimpleRanking<>(false);

                if (statementRanking == null) {
                	// add new statements to the statement level ranking, using the scores of the branches
                    Iterator<ProgramBranch> iterator = branchRanking.iterator();
                    while (iterator.hasNext()) {
                        ProgramBranch branch = iterator.next();
                        double rankingValue = branchRanking.getRankingValue(branch);
                        for (SourceCodeBlock block : branch) {
                            if (!ranking.hasRanking(block)) {
                                ranking.add(block, rankingValue);
                            }
                        }
                    }
                } else {
                	AtomicInteger currentHighestCore = new AtomicInteger(1000000);

                	// add new statements to the statement level ranking, using the scores of the branches
                	Iterator<ProgramBranch> iterator = branchRanking.iterator();
                	List<ProgramBranch> branchesWithSameScore = new ArrayList<>();
                	double lastScore = Double.NaN;
                	while (iterator.hasNext()) {
                		ProgramBranch branch = iterator.next();
                		double rankingValue = branchRanking.getRankingValue(branch);
                		if (rankingValue == lastScore) {
                			branchesWithSameScore.add(branch);
                		} else {
                			processCollectedBranches(rankingIdentifier, ranking, statementRanking, currentHighestCore,
									branchesWithSameScore);
                			branchesWithSameScore.add(branch);
                			lastScore = rankingValue;
                		}
                	}
                	// process any unprocessed branches, if existing
                	processCollectedBranches(rankingIdentifier, ranking, statementRanking, currentHighestCore,
							branchesWithSameScore);
                }
                break;
            case COBERTURA:
            case JACOCO:
            case TRACE_COBERTURA:
                // use statement level rankings for common SBFL scores
                ranking = RankingUtils.getRanking(SourceCodeBlock.DUMMY, bug, suffix, rankingIdentifier);
                break;
            default:
                throw new UnsupportedOperationException("option '" + spectraTool + "' not supported.");
        }

        if (ranking == null) {
            Log.abort(GenerateCsvBugDataFiles.class, "Found no ranking with identifier '%s'.", rankingIdentifier);
        }
        return ranking;
    }

	private static void processCollectedBranches(String rankingIdentifier, Ranking<SourceCodeBlock> ranking,
			Ranking<SourceCodeBlock> statementRanking, AtomicInteger currentHighestCore,
			List<ProgramBranch> branchesWithSameScore) {
		Set<SourceCodeBlock> containedBlocks = new HashSet<>();
		// process any previously collected branches
		for (ProgramBranch programBranch : branchesWithSameScore) {
			for (SourceCodeBlock block : programBranch) {
				if (!ranking.hasRanking(block)) {
					// collect new blocks to add to the ranking
					containedBlocks.add(block);
				}
			}
		}
		Ranking<SourceCodeBlock> statementRankingPart = new SimpleRanking<>(false);
		for (SourceCodeBlock sourceCodeBlock : containedBlocks) {
			// fetch statement level ranking score
			double value = statementRanking.getRankingValue(sourceCodeBlock);
			if (!Double.isNaN(value)) {
				statementRankingPart.add(sourceCodeBlock, value);
			} else {
				statementRankingPart.add(sourceCodeBlock, Double.NEGATIVE_INFINITY);
				Log.warn(GenerateCsvBugDataFiles.class, "Found no entry for block '%s' in statement level ranking with identifier '%s'.", 
						sourceCodeBlock.getShortIdentifier(), rankingIdentifier);
			}
		}
		// iterate over the part of the statement level ranking and add to main ranking with the respective scores
		Iterator<SourceCodeBlock> rankingPartIterator = statementRankingPart.iterator();
		double lastPartRankingScore = Double.NaN;
		while (rankingPartIterator.hasNext()) {
			SourceCodeBlock next = rankingPartIterator.next();
			double nextValue = statementRankingPart.getRankingValue(next);
			// assign scores based on statement level ranking placements
			if (lastPartRankingScore == nextValue) {
				ranking.add(next, currentHighestCore.get());
			} else {
				ranking.add(next, currentHighestCore.decrementAndGet());
				lastPartRankingScore = nextValue;
			}
		}
		
		// reset for next time
		branchesWithSameScore.clear();
	}

    private static class GenStatisticsProcessor extends AbstractProcessor<BuggyFixedEntity<?>, Pair<String, String[]>> {

        final private String rankingIdentifier;
        private final String suffix;
        private final String suffix2;
        private ToolSpecific spectraTool;
        private boolean fillEmptyLines;

        /**
         * @param suffix            a suffix to append to the ranking directory (may be null)
         * @param suffix2           a suffix to append to the ranking directory (may be null, statement level ranking)
         * @param rankingIdentifier a fault localizer identifier or an lm ranking file name
         * @param spectraTool       the tool used to generate the rankings (statement-/ branch-level ...)
         * @param fillEmptyLines    whether to fill empty lines between statements within the same method
         */
        private GenStatisticsProcessor(String suffix, String suffix2, String rankingIdentifier,
                                       ToolSpecific spectraTool, boolean fillEmptyLines) {
            this.suffix = suffix;
            this.suffix2 = suffix2;
            this.rankingIdentifier = rankingIdentifier;
            this.spectraTool = spectraTool;
            this.fillEmptyLines = fillEmptyLines;
        }

        @Override
        public Pair<String, String[]> processItem(BuggyFixedEntity<?> entity, ProcessorSocket<BuggyFixedEntity<?>, Pair<String, String[]>> socket) {
            Log.out(GenerateCsvBugDataFiles.class, "Processing %s.", entity);
            Entity bug = entity.getBuggyVersion();

            Map<String, List<Modification>> changeInformation = entity.loadChangesFromFile();

            Ranking<SourceCodeBlock> ranking = generateStatementLevelRanking(bug, spectraTool, suffix, suffix2, rankingIdentifier);

            if (fillEmptyLines) {
                // fill up empty lines between statements within the same method;
                // helps with the correct marking of changes to ranked elements
                fillEmptylines(ranking);
            }

            ranking = removeDuplicateLines(ranking);

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
                RankingMetric<SourceCodeBlock> metric = Objects.requireNonNull(ranking).getRankingMetrics(changedElement);
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
        private final String suffix;
        private final String suffix2;
        private ToolSpecific spectraTool;

        /**
         * @param suffix            a suffix to append to the ranking directory (may be null)
         * @param suffix2            a suffix to append to the ranking directory (may be null, statement level ranking)
         * @param rankingIdentifier a fault localizer identifier or an lm ranking file name
         * @param spectraTool       the tool used to generate the rankings (statement-/ branch-level ...)
         */
        private RankingLOCProcessor(String suffix, String suffix2, String rankingIdentifier, ToolSpecific spectraTool) {
            this.suffix = suffix;
            this.suffix2 = suffix2;
            this.rankingIdentifier = rankingIdentifier;
            this.spectraTool = spectraTool;
        }

        @Override
        public String processItem(BuggyFixedEntity<?> entity, ProcessorSocket<BuggyFixedEntity<?>, String> socket) {
            Log.out(GenerateCsvBugDataFiles.class, "Processing %s for general data.", entity);
            Entity bug = entity.getBuggyVersion();

            Ranking<SourceCodeBlock> ranking = generateStatementLevelRanking(bug, spectraTool, suffix, suffix2, rankingIdentifier);

            ranking = removeDuplicateLines(ranking);

            // BugID, Line, IF, IS, NF, NS, BestRanking, WorstRanking,
            // MinWastedEffort, MaxWastedEffort, Suspiciousness

            String bugIdentifier = bug.getUniqueIdentifier();

            return bugIdentifier + "," + Objects.requireNonNull(ranking).getElements().size();
        }
    }

    protected static void fillEmptylines(Ranking<SourceCodeBlock> ranking) {
        //get lines in the ranking and sort them
        Collection<SourceCodeBlock> nodes = ranking.getElements();
        SourceCodeBlock[] array = new SourceCodeBlock[nodes.size()];
        int counter = -1;
        for (SourceCodeBlock node : nodes) {
            array[++counter] = node;
        }
        Arrays.sort(array);

        SourceCodeBlock lastLine = new SourceCodeBlock("", "", "", -1, NodeType.NORMAL);
        //iterate over all lines
        List<SourceCodeBlock> nodesOnSameLine = new ArrayList<>(3);
        for (SourceCodeBlock line : array) {
            //see if we are inside the same method in the same package
            if (line.getMethodName().equals(lastLine.getMethodName())
                    && line.getFilePath().equals(lastLine.getFilePath())) {
                //set the end line number of the last covered line to be equal 
                //to the line before the next covered line
                if (line.getStartLineNumber() == lastLine.getStartLineNumber()) {
                    nodesOnSameLine.add(line);
                } else {
                    for (SourceCodeBlock block : nodesOnSameLine) {
                        // set end line for all nodes on the same line
                        block.setLineNumberEnd(line.getStartLineNumber() - 1);
                    }
                    nodesOnSameLine.clear();
                }
            } else {
                nodesOnSameLine.clear();
            }

            //next line...
            lastLine = line;
        }
    }

    protected static Ranking<SourceCodeBlock> removeDuplicateLines(Ranking<SourceCodeBlock> ranking) {
        Ranking<SourceCodeBlock> result = new SimpleRanking<>(false);

        Set<String> seenLines = new HashSet<>();
        // add new statements to the statement level ranking, using the scores of the branches
        Iterator<SourceCodeBlock> iterator = ranking.iterator();
        while (iterator.hasNext()) {
            SourceCodeBlock block = iterator.next();
            String lineRep = block.getFilePath() + block.getStartLineNumber();
            if (!seenLines.contains(lineRep)) {
                seenLines.add(lineRep);
                double rankingValue = ranking.getRankingValue(block);
                result.add(block, rankingValue);
            }
        }
        return result;
    }

}
