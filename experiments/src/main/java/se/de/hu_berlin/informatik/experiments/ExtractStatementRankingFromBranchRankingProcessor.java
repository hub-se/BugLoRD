package se.de.hu_berlin.informatik.experiments;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.modification.Modification;
import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.spectra.core.branch.ProgramBranch;
import se.de.hu_berlin.informatik.utils.experiments.ranking.MarkedRanking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.RankingMetric;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;
import se.de.hu_berlin.informatik.utils.processors.sockets.ProcessorSocket;

import java.util.*;

public class ExtractStatementRankingFromBranchRankingProcessor extends AbstractProcessor<BuggyFixedEntity<?>, Pair<String, String[]>> {

    final private String rankingIdentifier;
    private final String suffix;

    /**
     * @param suffix
     * a suffix to append to the ranking directory (may be null)
     * @param rankingIdentifier
     * a fault localizer identifier or an lm ranking file name
     */
    public ExtractStatementRankingFromBranchRankingProcessor(String suffix, String rankingIdentifier) {
        this.suffix = suffix;
        this.rankingIdentifier = rankingIdentifier;
    }

    @Override
    public Pair<String, String[]> processItem(BuggyFixedEntity<?> entity, ProcessorSocket<BuggyFixedEntity<?>, Pair<String, String[]>> socket) {
        Log.out(ExtractStatementRankingFromBranchRankingProcessor.class, "Processing %s.", entity);
        Entity bug = entity.getBuggyVersion();

        Map<String, List<Modification>> changeInformation = entity.loadChangesFromFile();

        Ranking<ProgramBranch> programBranchRanking = RankingDuc.getRanking(bug, suffix, rankingIdentifier);
        if (programBranchRanking == null) {
            Log.abort(this, "Found no ranking with identifier '%s'.", rankingIdentifier);
        }

        MarkedRanking<ProgramBranch, List<Modification>> markedProgramBranchRanking = new MarkedRanking<>(programBranchRanking);

        List<Modification> ignoreList = new ArrayList<>();
        for (ProgramBranch programBranch : markedProgramBranchRanking.getElements()) {
            List<Modification> list = new ArrayList<>();
            for (SourceCodeBlock block : programBranch.getElements()) {
                List<Modification> modifications = Modification.getModifications(
                        block.getFilePath(), block.getStartLineNumber(), block.getEndLineNumber(), true,
                        changeInformation, ignoreList);
                if(modifications != null){
                    list.addAll(modifications);
                }
                // found changes for this line? then mark the line with the
                // change(s)...
            }
            if (list != null && !list.isEmpty()) {
                markedProgramBranchRanking.markElementWith(programBranch, list);
            }
        }

        // BugID, Line, EF, EP, NF, NP, BestRanking, WorstRanking,
        // MinWastedEffort, MaxWastedEffort, Suspiciousness,
        // MinFiles, MaxFiles, MinMethods, MaxMethods

        String bugIdentifier = bug.getUniqueIdentifier();

        int count = 0;
        HashSet<SourceCodeBlock> extractedStatements = new HashSet<>(); //avoid duplicate statements when extracting statements from branches

        for (ProgramBranch changedElement : markedProgramBranchRanking.getMarkedElements()) {

            RankingMetric<ProgramBranch> metric = Objects.requireNonNull(programBranchRanking).getRankingMetrics(changedElement);
            RankingDuc.ProgramBranchRankingMetrics scbMetric = RankingDuc.getProgramBranchRankingMetrics(programBranchRanking, changedElement);

            for(SourceCodeBlock statement : changedElement.getElements()){

                if(extractedStatements.contains(statement)){
                    continue;
                }else{
                    extractedStatements.add(statement);
                }

                String[] line = new String[15];

                line[0] = bugIdentifier;
                line[1] = statement.getShortIdentifier();
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

        }

        return null;
    }

}
