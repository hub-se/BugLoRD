package se.de.hu_berlin.informatik.experiments.defects4j;

import org.apache.commons.cli.Option;
import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.experiments.defects4j.calls.GenCombinedRankingsEH;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.processors.basics.ThreadedListProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates combined rankings for specific percentages.
 *
 * @author SimHigh
 */
public class GenerateCombinedRankings {

    public enum CmdOptions implements OptionWrapperInterface {
        /* add options here according to your needs */

        SUFFIX("s", "suffix", true, "A suffix to append to the plot sub-directory.", false),

        LOCALIZERS(Option.builder("l").longOpt("localizers").required(false)
                .hasArgs().desc("A list of localizers (e.g. 'Tarantula', 'Jaccard', ...). If not set, "
                        + "the localizers will be retrieved from the properties file.").build()),
        PERCENTAGES(Option.builder("pc").longOpt("percentages").hasArgs()
                .desc("Generate different combined rankings. Takes as arguments a "
                        + "list of percentage values (percentage of SBFL ranking).").build()),

        NORMALIZED("n", "normalized", NormalizationStrategy.class, NormalizationStrategy.ReciprocalRankWorst,
                "Indicates whether the ranking should be normalized before combination.", false),

        LM("lm", "globalLM", true, "A specific LM ranking identifier.", false);

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

        @Override
        public String toString() {
            return option.getOption().getOpt();
        }

        @Override
        public OptionWrapper getOptionWrapper() {
            return option;
        }
    }


    public static void main(String[] args) {

        OptionParser options = OptionParser.getOptions("GenerateCombinedRankings", true, CmdOptions.class, args);

        NormalizationStrategy normStrategy = null;
        if (options.hasOption(CmdOptions.NORMALIZED)) {
            normStrategy = options.getOptionValue(CmdOptions.NORMALIZED,
                    NormalizationStrategy.class, NormalizationStrategy.ReciprocalRankWorst, true);
        }

        String[] localizers = options.getOptionValues(CmdOptions.LOCALIZERS);
        if (localizers == null) {
            localizers = BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ");
        }

        int threadCount = options.getNumberOfThreads();

        String suffix = options.getOptionValue(CmdOptions.SUFFIX, null);

        List<BuggyFixedEntity<?>> entities = new ArrayList<>();
        //iterate over all projects
        for (String project : Defects4J.getAllProjects()) {
            String[] ids = Defects4J.getAllBugIDs(project);
            for (String id : ids) {
                entities.add(new Defects4JBuggyFixedEntity(project, id));
            }
        }

        String globalLMidentifier = options.getOptionValue(CmdOptions.LM, null);

        String[] percentages = options.getOptionValues(CmdOptions.PERCENTAGES);

        if (globalLMidentifier != null) {
            new ThreadedListProcessor<>(threadCount,
                    new GenCombinedRankingsEH(suffix, globalLMidentifier, localizers, percentages, normStrategy))
                    .submit(entities);
        } else {
            List<String> allRankingFileNames = getAllLMRankingFileIdentifiers();

            for (String lmRankingFileName : allRankingFileNames) {
                new ThreadedListProcessor<>(threadCount,
                        new GenCombinedRankingsEH(suffix, lmRankingFileName, localizers, percentages, normStrategy))
                        .submit(entities);
            }
        }

    }

    public static List<String> getAllLMRankingFileIdentifiers() {
        File allLMRankingFileNamesFile = new File(BugLoRDConstants.LM_RANKING_FILENAMES_FILE);

        return FileUtils.readFile2List(allLMRankingFileNamesFile.toPath());
    }

}
