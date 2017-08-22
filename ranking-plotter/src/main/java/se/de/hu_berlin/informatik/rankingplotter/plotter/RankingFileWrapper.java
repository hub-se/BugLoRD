/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.SignificanceLevel;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper.ModificationType;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.experiments.ranking.MarkedRanking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.RankingMetric;
import se.de.hu_berlin.informatik.utils.miscellaneous.MathUtils;

/**
 * Helper class that stores percentage values and other helper structures
 * that are parsed from a combined ranking file.
 * 
 * @author Simon Heiden
 */
public class RankingFileWrapper implements Comparable<RankingFileWrapper> {
	
	private static final boolean COUNT_ALL_CHANGES = false;

	/**
	 * Stores the percentage value of the SBFL ranking.
	 */
	private double SBFL;
	
	private long unsignificant_changes = 0;
	private long low_significance_changes = 0;
	private long medium_significance_changes = 0;
	private long high_significance_changes = 0;
	private long crucial_significance_changes = 0;
	
	private long unsignificant_changes_sum = 0;
	private long low_significance_changes_sum = 0;
	private long medium_significance_changes_sum = 0;
	private long high_significance_changes_sum = 0;
	private long crucial_significance_changes_sum = 0;
	
	private long mod_changes = 0;
	private long mod_deletes = 0;
	private long mod_inserts = 0;
	private long mod_unknowns = 0;
	
	private long mod_changes_sum = 0;
	private long mod_deletes_sum = 0;
	private long mod_inserts_sum = 0;
	private long mod_unknowns_sum = 0;

	private long allCount = 0;
	private long allSum = 0;
	
	private List<Integer> allRankings = null;
	
	private int min_rank = Integer.MAX_VALUE;

	private long min_rank_count = 0;
	private long minRankSum = 0;
	
	private List<Integer> allMinRankings = null;

	private Map<Integer,Integer> hitAtXMap;
	
	final private String project;
	final private int bugId;
	
	private int[] changedLinesRankings = null;
	
	private MarkedRanking<SourceCodeBlock, List<ChangeWrapper>> ranking;
	
	/**
	 * Creates a new {@link RankingFileWrapper} object with the given parameters.
	 * @param project
	 * the project name
	 * @param bugId
	 * the bug id
	 * @param combinedRanking
	 * the combined ranking
	 * @param sBFL
	 * the percentage value of the SBFL ranking
	 * @param changeInformation 
	 * a map containing all modifications
	 * @param strategy
	 * which strategy to use. May take the lowest or the highest ranking 
	 * of a range of equal-value rankings or may compute the average
	 */
	public RankingFileWrapper(String project, int bugId, Ranking<SourceCodeBlock> combinedRanking, double sBFL,
			Map<String, List<ChangeWrapper>> changeInformation,
			ParserStrategy strategy) {
		super();
		this.project = project;
		this.bugId = bugId;
		this.SBFL = sBFL;
		
		//you can add different values for X here
		hitAtXMap = new HashMap<>();
		hitAtXMap.put(1,0);
		hitAtXMap.put(5,0);
		hitAtXMap.put(10,0);
		hitAtXMap.put(20,0);
		hitAtXMap.put(30,0);
		hitAtXMap.put(50,0);
		hitAtXMap.put(100,0);
		
		if (combinedRanking != null) {
			this.ranking = new MarkedRanking<>(combinedRanking);
		}
		
		if (this.ranking != null) {
			parseModLinesFile(changeInformation, strategy);
		}
	}
	
	public void throwAwayRanking() {
		ranking = null;
	}
	
	/**
	 * Returns the list of changes relevant to the given {@link SourceCodeBlock}.
	 * @param ignoreRefactorings
	 * whether to ignore changes that are refactorings
	 * @param block
	 * the block to check
	 * @param changesMap
	 * the map of all existing changes
	 * @return
	 * list of changes relevant to the given block; {@code null} if no changes match
	 */
	public static List<ChangeWrapper> getModifications(boolean ignoreRefactorings, 
			SourceCodeBlock block, Map<String, List<ChangeWrapper>> changesMap) {
		List<ChangeWrapper> list = null;
		//see if the respective file was changed
		List<ChangeWrapper> changes = changesMap.get(block.getFilePath());
		if (changes != null) {
			for (ChangeWrapper change : changes) {
				
				if (ignoreRefactorings) {
					//no semantic change like changes to a comment or something like that? then proceed...
					if (change.getModificationType() == ModificationType.PROB_NO_CHANGE) {
						continue;
					}
					//no change at all?...
					if (change.getModificationType() == ModificationType.NO_CHANGE) {
						continue;
					}
				}
				
				List<Integer> deltas = change.getIncludedDeltas();
				if (deltas == null) {
					continue;
				}
				
				//is the ranked block part of a changed statement?
				for (int deltaLine : change.getIncludedDeltas()) {
					if (block.getStartLineNumber() <= deltaLine && deltaLine <= block.getEndLineNumber()) {
						if (list == null) {
							list = new ArrayList<>(1);
						}
						list.add(change);
						break;
					}
				}
			}
		}
		return list;
	}
	
	/**
	 * Parses the modified lines file.
	 * @param changeInformation 
	 * a map containing all modifications (the keys are the file names/paths)
	 * @param strategy
	 * which strategy to use. May take the lowest or the highest ranking 
	 * of a range of equal-value rankings or may compute the average
	 */
	private void parseModLinesFile(Map<String, List<ChangeWrapper>> changeInformation, 
			ParserStrategy strategy) {
		min_rank = Integer.MAX_VALUE;

		for (SourceCodeBlock rankedElement : ranking.getElements()) {
			List<ChangeWrapper> list = getModifications(true, rankedElement, changeInformation);
			//found changes for this line? then mark the line with the change(s)... 
			if (list != null) {
				ranking.markElementWith(rankedElement, list);
			}
		}

		List<Integer> changedLinesRankingsList = new ArrayList<>();
		for (SourceCodeBlock changedElement : ranking.getMarkedElements()) {
			RankingMetric<SourceCodeBlock> metric = ranking.getRankingMetrics(changedElement);
			List<ChangeWrapper> changes = ranking.getMarker(changedElement);

			int original_rank_pos = metric.getRanking();
			int rank_pos = original_rank_pos;

			switch(strategy) {
			case BEST_CASE:
				//use the best value if the corresponding ranking spans
				//over multiple lines
				rank_pos = metric.getBestRanking();
				break;
			case AVERAGE_CASE:
				//use the middle value if the corresponding ranking spans
				//over multiple lines
				rank_pos = (int) Math.rint(metric.getMeanRanking());
				break;
			case WORST_CASE:
				//use the worst value if the corresponding ranking spans
				//over multiple lines
				rank_pos = metric.getWorstRanking();
				break;
			case NO_CHANGE:
				//use the parsed line number
				break;
			}

			min_rank = (rank_pos < min_rank ? rank_pos : min_rank);

			if (COUNT_ALL_CHANGES) {
				countAllChanges(changedLinesRankingsList, changes, rank_pos);
			} else {
				countOneChange(changedLinesRankingsList, changes, rank_pos);
			}

		}
		
		ranking.outdateRankingCache();
		
		changedLinesRankings = new int[changedLinesRankingsList.size()];
		for (int i = 0; i < changedLinesRankingsList.size(); ++i) {
			changedLinesRankings[i] = changedLinesRankingsList.get(i);
		}
	}

	private void countAllChanges(List<Integer> changedLinesRankingsList, List<ChangeWrapper> changes, int rank_pos) {
		//if a line touched multiple changes, count them all
		//TODO: is that really a good idea?
		for (ChangeWrapper change : changes) {
			changedLinesRankingsList.add(rank_pos);

			for (Entry<Integer,Integer> hitEntry : hitAtXMap.entrySet()) {
				if (rank_pos <= hitEntry.getKey()) {
					hitAtXMap.put(hitEntry.getKey(), hitEntry.getValue() + 1);
				}
			}


			switch(change.getSignificance()) {
			case NONE:
				unsignificant_changes_sum += rank_pos;
				++unsignificant_changes;
				break;
			case LOW:
				low_significance_changes_sum += rank_pos;
				++low_significance_changes;
				break;
			case MEDIUM:
				medium_significance_changes_sum += rank_pos;
				++medium_significance_changes;
				break;
			case HIGH:
				high_significance_changes_sum += rank_pos;
				++high_significance_changes;
				break;
			case CRUCIAL:
				crucial_significance_changes_sum += rank_pos;
				++crucial_significance_changes;
				break;
			}
			allSum += rank_pos;
			++allCount;

			switch(change.getModificationType()) {
			case PROB_NO_CHANGE:
				mod_unknowns_sum += rank_pos;
				++mod_unknowns;
				break;
			case CHANGE:
				mod_changes_sum += rank_pos;
				++mod_changes;
				break;
			case DELETE:
				mod_deletes_sum += rank_pos;
				++mod_deletes;
				break;
			case INSERT:
				mod_inserts_sum += rank_pos;
				++mod_inserts;
				break;
			case NO_CHANGE:
				break;
			}

		}
	}
	
	private void countOneChange(List<Integer> changedLinesRankingsList, List<ChangeWrapper> changes, int rank_pos) {
		//if a line touched multiple changes, count only the most "important"...
		changedLinesRankingsList.add(rank_pos);

		for (Entry<Integer,Integer> hitEntry : hitAtXMap.entrySet()) {
			if (rank_pos <= hitEntry.getKey()) {
				hitAtXMap.put(hitEntry.getKey(), hitEntry.getValue() + 1);
			}
		}

		switch(getHighestSignificanceLevel(changes)) {
		case NONE:
			unsignificant_changes_sum += rank_pos;
			++unsignificant_changes;
			break;
		case LOW:
			low_significance_changes_sum += rank_pos;
			++low_significance_changes;
			break;
		case MEDIUM:
			medium_significance_changes_sum += rank_pos;
			++medium_significance_changes;
			break;
		case HIGH:
			high_significance_changes_sum += rank_pos;
			++high_significance_changes;
			break;
		case CRUCIAL:
			crucial_significance_changes_sum += rank_pos;
			++crucial_significance_changes;
			break;
		}
		allSum += rank_pos;
		++allCount;
		
		ModificationType modificationType = getMostImportantType(changes);

		switch(modificationType) {
		case PROB_NO_CHANGE:
			mod_unknowns_sum += rank_pos;
			++mod_unknowns;
			break;
		case CHANGE:
			mod_changes_sum += rank_pos;
			++mod_changes;
			break;
		case DELETE:
			mod_deletes_sum += rank_pos;
			++mod_deletes;
			break;
		case INSERT:
			mod_inserts_sum += rank_pos;
			++mod_inserts;
			break;
		case NO_CHANGE:
			break;
		}

	}
	
	private static ModificationType getMostImportantType(List<ChangeWrapper> changes) {
		EnumSet<ChangeWrapper.ModificationType> types = getModificationTypes(changes);
		if (types.contains(ModificationType.CHANGE)) {
			return ModificationType.CHANGE;
		} else if (types.contains(ModificationType.DELETE)) {
			return ModificationType.DELETE;
		} else if (types.contains(ModificationType.INSERT)) {
			return ModificationType.INSERT;
		} else if (types.contains(ModificationType.PROB_NO_CHANGE)) {
			return ModificationType.PROB_NO_CHANGE;
		} else {
			return ModificationType.NO_CHANGE;
		}
	}

	private static SignificanceLevel getHighestSignificanceLevel(List<ChangeWrapper> changes) {
		SignificanceLevel significance = SignificanceLevel.NONE;
		for (ChangeWrapper change : changes) {
			if (change.getSignificance().value() > significance.value()) {
				significance = change.getSignificance();
			}
		}
		return significance;
	}
	
	private static EnumSet<ChangeWrapper.ModificationType> getModificationTypes(List<ChangeWrapper> changes) {
		EnumSet<ChangeWrapper.ModificationType> set = EnumSet.noneOf(ChangeWrapper.ModificationType.class);
		for (ChangeWrapper change : changes) {
			if (change.getModificationType() == ModificationType.INSERT) {
				set.add(ModificationType.INSERT);
				break;
			}
		}
		for (ChangeWrapper change : changes) {
			if (change.getModificationType() == ModificationType.CHANGE) {
				set.add(ModificationType.CHANGE);
				break;
			}
		}
		for (ChangeWrapper change : changes) {
			if (change.getModificationType() == ModificationType.DELETE) {
				set.add(ModificationType.DELETE);
				break;
			}
		}
		return set;
	}

	/**
	 * @return 
	 * the percentage value of the SBFL ranking not divided by 100
	 */
	public double getSBFLPercentage() {
		return SBFL;
	}
	
	/**
	 * @return 
	 * the percentage value of the SBFL ranking
	 */
	public double getSBFL() {
		return (double)SBFL / 100.0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final RankingFileWrapper o) {
		return Double.compare(o.getSBFLPercentage(), this.getSBFLPercentage());
	}

	/**
	 * @return
	 * all rankings
	 */
	public MarkedRanking<SourceCodeBlock, List<ChangeWrapper>> getRanking() {
		return ranking;
	}
	
	public List<Integer> getAllRanks() {
		return allRankings;
	}
	
	public List<Integer> getAllMinRanks() {
		return allMinRankings;
	}

	public Integer getMinRank() {
		return min_rank < Integer.MAX_VALUE ? min_rank : null;
	}
	
	public long getMinRankCount() {
		return min_rank_count;
	}
	
	public void addToMinRankCount(int min_rank_count) {
		this.min_rank_count += min_rank_count;
	}
	
	public int[] getChangedLinesRankings() {
		return changedLinesRankings;
	}
	
	public long getAll() {
		return allCount;
	}
	
	public void addToAll(long all) {
		this.allCount += all;
	}
	
	public long getModChanges() {
		return mod_changes;
	}
	
	public void addToModChanges(long mod_changes) {
		this.mod_changes += mod_changes;
	}
	
	public long getModDeletes() {
		return mod_deletes;
	}
	
	public void addToModDeletes(long mod_deletes) {
		this.mod_deletes += mod_deletes;
	}
	
	public long getModInserts() {
		return mod_inserts;
	}
	
	public void addToModInserts(long mod_inserts) {
		this.mod_inserts += mod_inserts;
	}
	
	public long getModUnknowns() {
		return mod_unknowns;
	}
	
	public void addToModUnknowns(long mod_unknowns) {
		this.mod_unknowns += mod_unknowns;
	}
	
	public long getModChangesSum() {
		return mod_changes_sum;
	}

	public void addToModChangesSum(long mod_changes_sum) {
		this.mod_changes_sum += mod_changes_sum;
	}
	
	public long getModDeletesSum() {
		return mod_deletes_sum;
	}

	public void addToModDeletesSum(long mod_deletes_sum) {
		this.mod_deletes_sum += mod_deletes_sum;
	}
	
	public long getModInsertsSum() {
		return mod_inserts_sum;
	}

	public void addToModInsertsSum(long mod_inserts_sum) {
		this.mod_inserts_sum += mod_inserts_sum;
	}
	
	public long getModUnknownsSum() {
		return mod_unknowns_sum;
	}

	public void addToModUnknownsSum(long mod_unknowns_sum) {
		this.mod_unknowns_sum += mod_unknowns_sum;
	}
	
	
	
	public long getUnsignificantChanges() {
		return unsignificant_changes;
	}
	
	public void addToUnsignificantChanges(long unsignificant_changes) {
		this.unsignificant_changes += unsignificant_changes;
	}
	
	public long getLowSignificanceChanges() {
		return low_significance_changes;
	}
	
	public void addToLowSignificanceChanges(long low_significance_changes) {
		this.low_significance_changes += low_significance_changes;
	}
	
	public long getMediumSignificanceChanges() {
		return medium_significance_changes;
	}
	
	public void addToMediumSignificanceChanges(long medium_significance_changes) {
		this.medium_significance_changes += medium_significance_changes;
	}

	public long getHighSignificanceChanges() {
		return high_significance_changes;
	}
	
	public void addToHighSignificanceChanges(long high_significance_changes) {
		this.high_significance_changes += high_significance_changes;
	}
	
	public long getCrucialSignificanceChanges() {
		return crucial_significance_changes;
	}
	
	public void addToCrucialSignificanceChanges(long crucial_significance_changes) {
		this.crucial_significance_changes += crucial_significance_changes;
	}

	public long getMinRankSum() {
		return minRankSum;
	}

	public void addToMinRankSum(int minRank) {
		if (allMinRankings == null) {
			allMinRankings = new ArrayList<>();
		}
		allMinRankings.add(minRank);
		this.minRankSum += minRank;
	}
	
	public long getAllSum() {
		return allSum;
	}

	public void addToAllSum(long allSum) {
		this.allSum += allSum;
	}
	
	public void addToAllRankings(int[] changedLineRankings) {
		if (allRankings == null) {
			allRankings = new ArrayList<>();
		}
		for (int rank : changedLineRankings) {
			allRankings.add(rank);
		}
	}
	
	public long getUnsignificantChangesSum() {
		return unsignificant_changes_sum;
	}

	public void addToUnsignificantChangesSum(long unsignificant_changes_sum) {
		this.unsignificant_changes_sum += unsignificant_changes_sum;
	}
	
	public long getLowSignificanceChangesSum() {
		return low_significance_changes_sum;
	}

	public void addToLowSignificanceChangesSum(long low_significance_changes_sum) {
		this.low_significance_changes_sum += low_significance_changes_sum;
	}
	
	public long getMediumSignificanceChangesSum() {
		return medium_significance_changes_sum;
	}

	public void addToMediumSignificanceChangesSum(long medium_significance_changes_sum) {
		this.medium_significance_changes_sum += medium_significance_changes_sum;
	}
	
	public long getHighSignificanceChangesSum() {
		return high_significance_changes_sum;
	}

	public void addToHighSignificanceChangesSum(long high_significance_changes_sum) {
		this.high_significance_changes_sum += high_significance_changes_sum;
	}
	
	public long getCrucialSignificanceChangesSum() {
		return crucial_significance_changes_sum;
	}

	public void addToCrucialSignificanceChangesSum(long crucial_significance_changes_sum) {
		this.crucial_significance_changes_sum += crucial_significance_changes_sum;
	}


	
	public double getMeanRank() {
		return (double)allSum / (double)allCount;
	}
	
	public double getMedianRank() {
		return MathUtils.getMedian(allRankings);
	}
	
	public double getMeanFirstRank() {
		return (double)minRankSum / (double)min_rank_count;
	}
	
	public double getMedianFirstRank() {
		return MathUtils.getMedian(allMinRankings);
	}
	
	public double getUnsignificantChangesAverage() {
		return (double)unsignificant_changes_sum / (double)unsignificant_changes;
	}
	
	public double getLowSignificanceChangesAverage() {
		return (double)low_significance_changes_sum / (double)low_significance_changes;
	}

	public double getMediumSignificanceChangesAverage() {
		return (double)medium_significance_changes_sum / (double)medium_significance_changes;
	}
	
	public double getHighSignificanceChangesAverage() {
		return (double)high_significance_changes_sum / (double)high_significance_changes;
	}
	
	public double getCrucialSignificanceChangesAverage() {
		return (double)crucial_significance_changes_sum / (double)crucial_significance_changes;
	}
	
	
	public double getModChangesAverage() {
		return (double)mod_changes_sum / (double)mod_changes;
	}
	
	public double getModDeletesAverage() {
		return (double)mod_deletes_sum / (double)mod_deletes;
	}
	
	public double getModInsertsAverage() {
		return (double)mod_inserts_sum / (double)mod_inserts;
	}
	
	public double getModUnknownsAverage() {
		return (double)mod_unknowns_sum / (double)mod_unknowns;
	}
	
	
	public Map<Integer,Integer> getHitAtXMap() {
		return hitAtXMap;
	}

	public String getProject() {
		return project;
	}

	public int getBugId() {
		return bugId;
	}
}
