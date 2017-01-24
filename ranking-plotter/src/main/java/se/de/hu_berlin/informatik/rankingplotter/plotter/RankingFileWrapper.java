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
import se.de.hu_berlin.informatik.benchmark.ranking.MarkedRanking;
import se.de.hu_berlin.informatik.benchmark.ranking.RankingMetric;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper.ModificationType;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;

/**
 * Helper class that stores percentage values and other helper structures
 * that are parsed from a combined ranking file.
 * 
 * @author Simon Heiden
 */
public class RankingFileWrapper implements Comparable<RankingFileWrapper> {
	
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
	
	private MarkedRanking<String, List<ChangeWrapper>> ranking;
	
	/**
	 * Creates a new {@link RankingFileWrapper} object with the given parameters.
	 * @param project
	 * the project name
	 * @param bugId
	 * the bug id
	 * @param ranking
	 * the combined ranking
	 * @param sBFL
	 * the percentage value of the SBFL ranking
	 * @param changeInformation 
	 * a map containing all modifications
	 * @param strategy
	 * which strategy to use. May take the lowest or the highest ranking 
	 * of a range of equal-value rankings or may compute the average
	 */
	public RankingFileWrapper(String project, int bugId, MarkedRanking<String, List<ChangeWrapper>> ranking, double sBFL,
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
		
		this.ranking = ranking;
		
		if (this.ranking != null) {
			parseModLinesFile(changeInformation, strategy);
		}
	}
	
	public void throwAwayRanking() {
		ranking = null;
	}
	
//	/**
//	 * Sorts the given map based on combined ranking values.
//	 * @param map
//	 * containing {@link Rankings} objects that are linked to "relative/path/To/File:lineNumber"-lines
//	 * @return
//	 * list of identifiers and rankings, both sorted
//	 */
//	private static Pair<List<String>, List<Double>> sortByValue(final Map<String, Rankings> map) {
//		Pair<List<String>,List<Double>> result = new Pair<>(new ArrayList<>(), new ArrayList<>());
//
//		map.entrySet().stream().sorted(Comparator.comparing(e -> e.getValue()))
//		.forEachOrdered(e -> { 
//			result.getFirst().add(e.getKey()); 
//			result.getSecond().add(e.getValue().getCombinedRanking()); 
//		});
//
//		return result;
//	}
	
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
//		Map<String, List<ChangeWrapper>> lineToModMap = new HashMap<>();

		min_rank = Integer.MAX_VALUE;

		for (String element : ranking.getElementMap().keySet()) {
			SourceCodeBlock block = SourceCodeBlock.getNewBlockFromString(element);
			
			//see if the respective file was changed
			if (changeInformation.containsKey(block.getClassName())) {
				List<ChangeWrapper> changes = changeInformation.get(block.getClassName());

				List<ChangeWrapper> list = null;
				for (ChangeWrapper change : changes) {
					//is the ranked block part of a changed statement?
					if (block.getEndLineNumber() >= change.getStart() && block.getStartLineNumber() <= change.getEnd()) {
						if (list == null) {
							list = new ArrayList<>(1);
						}
						list.add(change);
					}
				}
				if (list != null) {
					ranking.markElementWith(element, list);
				}
			}
		}

//		Map<String, Integer> lineToRankMap = new HashMap<>();
		changedLinesRankings = new int[ranking.getMarkedElements().size()];
		int counter = 0;
		for (String changedElement : ranking.getMarkedElements()) {
			RankingMetric<String> metric = ranking.getRankingMetrics(changedElement);
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
				rank_pos = (int)((metric.getBestRanking() + metric.getWorstRanking()) / 2.0);
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
//			lineToRankMap.put(changedElement.getKey(), rank_pos);
			
			changedLinesRankings[counter++] = rank_pos;
			SignificanceLevel significance = getHighestSignificanceLevel(changes);
			EnumSet<ChangeWrapper.ModificationType> modTypes = getModificationTypes(changes);
			
			for (Entry<Integer,Integer> hitEntry : hitAtXMap.entrySet()) {
				if (rank_pos <= hitEntry.getKey()) {
					hitAtXMap.put(hitEntry.getKey(), hitEntry.getValue() + 1);
				}
			}
		

			switch(significance) {
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

			for (ChangeWrapper.ModificationType mod : modTypes) {
				switch(mod) {
				case UNKNOWN:
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
				}
			}

		}
		
		ranking.outdateRankingCache();
	}
	
	public static SignificanceLevel getHighestSignificanceLevel(List<ChangeWrapper> changes) {
		SignificanceLevel significance = SignificanceLevel.NONE;
		for (ChangeWrapper change : changes) {
			if (change.getSignificance().value() > significance.value()) {
				significance = change.getSignificance();
			}
		}
		return significance;
	}
	
	public static EnumSet<ChangeWrapper.ModificationType> getModificationTypes(List<ChangeWrapper> changes) {
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
	public MarkedRanking<String, List<ChangeWrapper>> getRanking() {
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
		if (allRankings.isEmpty()) {
			return 0;
		}
		allRankings.sort(null);
		int size = allRankings.size();
		if (size % 2 == 0) {
			//even number of elements
			return (double)(allRankings.get(size/2 - 1) + allRankings.get(size/2)) / 2.0;
		} else {
			//odd number of elements
			return (double)allRankings.get(size/2);
		}
	}
	
	public double getMeanFirstRank() {
		return (double)minRankSum / (double)min_rank_count;
	}
	
	public double getMedianFirstRank() {
		if (allMinRankings.isEmpty()) {
			return 0;
		}
		allMinRankings.sort(null);
		int size = allMinRankings.size();
		if (size % 2 == 0) {
			//even number of elements
			return (double)(allMinRankings.get(size/2 - 1) + allMinRankings.get(size/2)) / 2.0;
		} else {
			//odd number of elements
			return (double)allMinRankings.get(size/2);
		}
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
