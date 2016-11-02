/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.SignificanceLevel;
import se.de.hu_berlin.informatik.benchmark.ranking.Ranking;
import se.de.hu_berlin.informatik.benchmark.ranking.RankingMetric;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper.ModificationType;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;

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
	
	/**
	 * A map that links element identifiers to a list of corresponding changes.
	 */
	private Map<String, List<ChangeWrapper>> lineNumberToModMap;

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

	private long all = 0;
	private long allSum = 0;
	
	private int min_rank = Integer.MAX_VALUE;

	private long min_rank_count = 0;
	private long minRankSum = 0;

	private Map<Integer,Integer> hitAtXMap;
	
	final private String project;
	final private int bugId;
	
	private int[] changedLinesRankings = null;
	
	private Ranking<String> ranking;
	
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
	public RankingFileWrapper(String project, int bugId, Ranking<String> ranking, double sBFL,
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
		hitAtXMap.put(Integer.MAX_VALUE,0);
		
		this.ranking = ranking;
		
		if (this.ranking != null) {
		this.lineNumberToModMap = 
				parseModLinesFile(changeInformation, strategy);
		}
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
	 * a map containing all modifications
	 * @param strategy
	 * which strategy to use. May take the lowest or the highest ranking 
	 * of a range of equal-value rankings or may compute the average
	 * @return
	 * a map that links element identifiers to a list of corresponding changes
	 */
	private Map<String, List<ChangeWrapper>> parseModLinesFile(Map<String, List<ChangeWrapper>> changeInformation, 
			ParserStrategy strategy) {
		Map<String, List<ChangeWrapper>> lineToModMap = new HashMap<>();

		min_rank = Integer.MAX_VALUE;

		for (String element : ranking.getElementMap().keySet()) {
			String identifier = element;
			//format: path:line_number
			String[] path_line = identifier.split(":");

			if (changeInformation.containsKey(path_line[0])) {
				int lineNumber = Integer.parseInt(path_line[1]);
				List<ChangeWrapper> changes = changeInformation.get(path_line[0]);

				for (ChangeWrapper entry : changes) {
					//is the ranked line inside of a changed statement?
					if (lineNumber >= entry.getStart() && lineNumber <= entry.getEnd()) {
						lineToModMap.computeIfAbsent(identifier, k -> new ArrayList<>(1)).add(entry);
					}
				}
			}
		}

//		Map<String, Integer> lineToRankMap = new HashMap<>();
		changedLinesRankings = new int[lineToModMap.keySet().size()];
		int counter = 0;
		for (Entry<String, List<ChangeWrapper>> changedElement : lineToModMap.entrySet()) {
			RankingMetric<String> metric = ranking.getRankingMetrics(changedElement.getKey());

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
			SignificanceLevel significance = getHighestSignificanceLevel(changedElement.getValue());
			ChangeWrapper.ModificationType modType = getModificationType(changedElement.getValue());
			
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
			++all;

			switch(modType) {
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
		
		ranking.outdateRankingCache();
		
		return lineToModMap;
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
	
	private static ChangeWrapper.ModificationType getModificationType(List<ChangeWrapper> changes) {
		// importance order: delete > change > insert > unknown
		ChangeWrapper.ModificationType modificationType = ModificationType.UNKNOWN;
		for (ChangeWrapper change : changes) {
			if (change.getModificationType() == ModificationType.INSERT) {
				modificationType = ModificationType.INSERT;
			}
		}
		for (ChangeWrapper change : changes) {
			if (change.getModificationType() == ModificationType.CHANGE) {
				modificationType = ModificationType.CHANGE;
			}
		}
		for (ChangeWrapper change : changes) {
			if (change.getModificationType() == ModificationType.DELETE) {
				modificationType = ModificationType.DELETE;
			}
		}
		return modificationType;
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
		return Double.compare(o.getSBFL(), this.getSBFL());
	}

	/**
	 * @return
	 * all rankings
	 */
	public Ranking<String> getRanking() {
		return ranking;
	}

	/**
	 * @return
	 * a map that links line numbers to a list of corresponding changes
	 */
	public Map<String, List<ChangeWrapper>> getLineToModMap() {
		return lineNumberToModMap;
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
		return all;
	}
	
	public void addToAll(long all) {
		this.all += all;
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
	
	public double getModChangesSum() {
		return mod_changes_sum;
	}

	public void addToModChangesSum(double mod_changes_sum) {
		this.mod_changes_sum += mod_changes_sum;
	}
	
	public double getModDeletesSum() {
		return mod_deletes_sum;
	}

	public void addToModDeletesSum(double mod_deletes_sum) {
		this.mod_deletes_sum += mod_deletes_sum;
	}
	
	public double getModInsertsSum() {
		return mod_inserts_sum;
	}

	public void addToModInsertsSum(double mod_inserts_sum) {
		this.mod_inserts_sum += mod_inserts_sum;
	}
	
	public double getModUnknownsSum() {
		return mod_unknowns_sum;
	}

	public void addToModUnknownsSum(double mod_unknowns_sum) {
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

	public double getMinRankSum() {
		return minRankSum;
	}

	public void addToMinRankSum(double minRankSum) {
		this.minRankSum += minRankSum;
	}
	
	public double getAllSum() {
		return allSum;
	}

	public void addToAllSum(double allSum) {
		this.allSum += allSum;
	}
	
	public double getUnsignificantChangesSum() {
		return unsignificant_changes_sum;
	}

	public void addToUnsignificantChangesSum(double unsignificant_changes_sum) {
		this.unsignificant_changes_sum += unsignificant_changes_sum;
	}
	
	public double getLowSignificanceChangesSum() {
		return low_significance_changes_sum;
	}

	public void addToLowSignificanceChangesSum(double low_significance_changes_sum) {
		this.low_significance_changes_sum += low_significance_changes_sum;
	}
	
	public double getMediumSignificanceChangesSum() {
		return medium_significance_changes_sum;
	}

	public void addToMediumSignificanceChangesSum(double medium_significance_changes_sum) {
		this.medium_significance_changes_sum += medium_significance_changes_sum;
	}
	
	public double getHighSignificanceChangesSum() {
		return high_significance_changes_sum;
	}

	public void addToHighSignificanceChangesSum(double high_significance_changes_sum) {
		this.high_significance_changes_sum += high_significance_changes_sum;
	}
	
	public double getCrucialSignificanceChangesSum() {
		return crucial_significance_changes_sum;
	}

	public void addToCrucialSignificanceChangesSum(double crucial_significance_changes_sum) {
		this.crucial_significance_changes_sum += crucial_significance_changes_sum;
	}


	
	public double getAllAverage() {
		return (double)allSum / (double)all;
	}
	
	public double getMeanFirstRank() {
		return (double)minRankSum / (double)min_rank_count;
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
