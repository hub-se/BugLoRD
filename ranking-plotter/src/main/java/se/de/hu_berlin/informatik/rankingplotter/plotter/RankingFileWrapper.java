/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SignificanceLevel;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;

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
	 * Stores the percentage value of the global NLFL ranking.
	 */
	private double globalNLFL;
	
	/**
	 * Stores the percentage value of the local NLFL ranking.
	 */
	private double localNLFL;
	
	/**
	 * An array with all rankings  (in order of appearance in the ranking file).
	 */
	private double[] rankings = null;
	
	/**
	 * A map that links line numbers to a list of corresponding changes.
	 */
	private Map<Integer, List<ChangeWrapper>> lineNumberToModMap;

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
	
	private Map<Double, Integer> firstAppearance = null;
	private Map<Double, Integer> lastAppearance = null;

	private long all = 0;
	private long allSum = 0;
	
	private int min_rank = Integer.MAX_VALUE;

	private long min_rank_count = 0;
	private long minRankSum = 0;

	private Map<Integer,Integer> hitAtXMap;
	
	/**
	 * Creates a new {@link RankingFileWrapper} object with the given parameters.
	 * @param map
	 * the map of identifiers with corresponding rankings
	 * @param sBFL
	 * the percentage value of the SBFL ranking
	 * @param globalNLFL
	 * the percentage value of the global NLFL ranking
	 * @param changeInformation 
	 * a map containing all modifications
	 * @param parseRankings
	 * determines if the ranking file should be parsed at all
	 * (leaves the array of rankings to be null)
	 * @param strategy
	 * which strategy to use. May take the lowest or the highest ranking 
	 * of a range of equal-value rankings or may compute the average
	 * @param computeAverages
	 * whether to prepare computation of averages of multiple rankings
	 * @param ignoreZeroAndBelow
	 * whether to ignore ranking values that are zero or below zero
	 */
	public RankingFileWrapper(Map<String, Rankings> map, double sBFL, double globalNLFL,
			Map<String, List<ChangeWrapper>> changeInformation, boolean parseRankings, 
			ParserStrategy strategy, boolean computeAverages, boolean ignoreZeroAndBelow) {
		super();
		this.SBFL = sBFL;
		this.globalNLFL = globalNLFL;
		this.localNLFL = 100 - globalNLFL;
		
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
		
		if (map != null) {
			List<String> identifiers = null;
			if (parseRankings) {
				Pair<List<String>,List<Double>> pair = parseRankings(map, ignoreZeroAndBelow);
				identifiers = pair.getFirst();
				this.rankings = new double[identifiers.size()];
				for (int i = 0; i < rankings.length; ++i) {
					this.rankings[i] = pair.getSecond().get(i); 
				}
			}
			
			this.lineNumberToModMap = 
					parseModLinesFile(identifiers, changeInformation, parseRankings, strategy, computeAverages, ignoreZeroAndBelow);
		}
	}
	
	/**
	 * Parses the rankings from the ranking file to an array.
	 * @param map 
	 * the map of identifiers with corresponding rankings
	 * @param ignoreZeroAndBelow
	 * whether to ignore ranking values that are zero or below zero 
	 * (rankings will not be added to the array)
	 * @return
	 * list of identifiers and rankings, both sorted
	 */
	private Pair<List<String>,List<Double>> parseRankings(Map<String, Rankings> map, boolean ignoreZeroAndBelow) {
		//TODO: implement ignoring zero
		for (Entry<String, Rankings> entry : map.entrySet()) {
			entry.getValue().setCombinedRanking(SBFL/100, 10, globalNLFL/100, 10);
		}

		return sortByValue(map);
	}
	
	/**
	 * Sorts the given map based on combined ranking values.
	 * @param map
	 * containing {@link Rankings} objects that are linked to "relative/path/To/File:lineNumber"-lines
	 * @return
	 * list of identifiers and rankings, both sorted
	 */
	private static Pair<List<String>, List<Double>> sortByValue(final Map<String, Rankings> map) {
		Pair<List<String>,List<Double>> result = new Pair<>(new ArrayList<>(), new ArrayList<>());

		map.entrySet().stream().sorted(Comparator.comparing(e -> e.getValue()))
		.forEachOrdered(e -> { 
			result.getFirst().add(e.getKey()); 
			result.getSecond().add(e.getValue().getCombinedRanking()); 
		});

		return result;
	}
	
	/**
	 * Parses the modified lines file.
	 * @param identifiers 
	 * the list of identifiers
	 * @param changeInformation 
	 * a map containing all modifications
	 * @param parseRankings
	 * determines if the ranking file was parsed
	 * @param strategy
	 * which strategy to use. May take the lowest or the highest ranking 
	 * of a range of equal-value rankings or may compute the average
	 * @param computeAverages
	 * whether to prepare computation of averages of multiple rankings
	 * @param ignoreZeroAndBelow
	 * whether to ignore ranking values that are zero or below zero
	 * @return
	 * a map that links line numbers to a list of corresponding changes
	 */
	private Map<Integer, List<ChangeWrapper>> parseModLinesFile(List<String> identifiers, Map<String, List<ChangeWrapper>> changeInformation, boolean parseRankings, 
			ParserStrategy strategy, boolean computeAverages, boolean ignoreZeroAndBelow) {
		Map<Integer, List<ChangeWrapper>> lineToModMap = new HashMap<>();

		min_rank = Integer.MAX_VALUE;

		if (parseRankings) {
			firstAppearance = new HashMap<>();
			lastAppearance = new HashMap<>();
			for (int i = 0; i < rankings.length; ++i) {
				firstAppearance.putIfAbsent(rankings[i], i+1);
				lastAppearance.put(rankings[i], i+1);
			}
		}

		List<String[]> attributeList = new ArrayList<>();

		int lineCounter = 0;
		for (String identifier : identifiers) {
			++lineCounter;
			//format: path:line_number
			String[] path_line = identifier.split(":");

			if (changeInformation.containsKey(path_line[0])) {
				int lineNumber = Integer.parseInt(path_line[1]);
				List<ChangeWrapper> changes = changeInformation.get(path_line[0]);

				for (ChangeWrapper entry : changes) {
					//is the ranked line inside of a changed statement?
					if (lineNumber >= entry.getStart() && lineNumber <= entry.getEnd()) {
						attributeList.add(new String[] { 
								String.valueOf(lineCounter),
								String.valueOf(entry.getStart()), String.valueOf(entry.getEnd()), 
								entry.getEntityType(), entry.getChangeType(), 
								entry.getSignificance().toString(), entry.getModificationType() });
					}
				}
			}
		}

		for (String[] attributes : attributeList) {
			//format: 0           1          2            3             4                5                 6
			//  | rank_pos | start_line | end_line | entity type | change type | significance level | modification |

			int original_rank_pos = Integer.parseInt(attributes[0]);
			int rank_pos = original_rank_pos;

			//TODO fix that? is that correct? probably not...
			//continue with the next line if the parsed rank corresponds
			//to a line that is zero or below zero
			if (ignoreZeroAndBelow && rank_pos > rankings.length) {
				continue;
			}

			if (parseRankings) {
				double orig_rank = rankings[original_rank_pos-1];
				switch(strategy) {
				case BEST_CASE:
					//use the best value if the corresponding ranking spans
					//over multiple lines
					rank_pos = firstAppearance.get(orig_rank);
					firstAppearance.put(orig_rank, firstAppearance.get(orig_rank) + 1);
					break;
				case AVERAGE_CASE:
					//use the middle value if the corresponding ranking spans
					//over multiple lines
					rank_pos = (int)((lastAppearance.get(orig_rank) + firstAppearance.get(orig_rank)) / 2.0);
					break;
				case WORST_CASE:
					//use the worst value if the corresponding ranking spans
					//over multiple lines
					rank_pos = lastAppearance.get(orig_rank);
					lastAppearance.put(orig_rank, lastAppearance.get(orig_rank) - 1);
					break;
				case NO_CHANGE:
					//use the parsed line number
					break;
				}
			}

			min_rank = (rank_pos < min_rank ? rank_pos : min_rank);

			ChangeWrapper change = new ChangeWrapper(
					Integer.parseInt(attributes[1]), Integer.parseInt(attributes[2]),
					attributes[3], attributes[4], attributes[5], attributes[6], rank_pos);
			if (lineToModMap.containsKey(original_rank_pos)) {
				lineToModMap.get(original_rank_pos).add(change);
			} else {
				List<ChangeWrapper> list = new ArrayList<>();
				list.add(change);
				lineToModMap.put(original_rank_pos, list);
			}
		}
		

		//We don't need these any more in the future if computing the averages
		//TODO is that correct? 
		if (computeAverages) {
			this.firstAppearance = null;
			this.lastAppearance = null;
			this.rankings = null;
		}


		for (Entry<Integer, List<ChangeWrapper>> entry : lineToModMap.entrySet()) {
			int rank_pos = entry.getValue().get(0).getRankPos();
			SignificanceLevel significance = getHighestSignificanceLevel(entry.getValue());
			String modType = getModificationType(entry.getValue());
			
			for (Entry<Integer,Integer> hitEntry : hitAtXMap.entrySet()) {
				if (rank_pos <= hitEntry.getKey()) {
					hitAtXMap.put(hitEntry.getKey(), hitEntry.getValue() + 1);
				}
			}
		
			if (computeAverages) {
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
				case ChangeWrapper.MOD_UNKNOWN:
					mod_unknowns_sum += rank_pos;
					++mod_unknowns;
					break;
				case ChangeWrapper.MOD_CHANGE:
					mod_changes_sum += rank_pos;
					++mod_changes;
					break;
				case ChangeWrapper.MOD_DELETE:
					mod_deletes_sum += rank_pos;
					++mod_deletes;
					break;
				case ChangeWrapper.MOD_INSERT:
					mod_inserts_sum += rank_pos;
					++mod_inserts;
					break;
				}
			}
		}
		
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
	
	private static String getModificationType(List<ChangeWrapper> changes) {
		// importance order: delete > change > insert > unknown
		String modificationType = ChangeWrapper.MOD_UNKNOWN;
		for (ChangeWrapper change : changes) {
			if (change.getModificationType().equals(ChangeWrapper.MOD_INSERT)) {
				modificationType = ChangeWrapper.MOD_INSERT;
			}
		}
		for (ChangeWrapper change : changes) {
			if (change.getModificationType().equals(ChangeWrapper.MOD_CHANGE)) {
				modificationType = ChangeWrapper.MOD_CHANGE;
			}
		}
		for (ChangeWrapper change : changes) {
			if (change.getModificationType().equals(ChangeWrapper.MOD_DELETE)) {
				modificationType = ChangeWrapper.MOD_DELETE;
			}
		}
		return modificationType;
	}

	/**
	 * @return 
	 * the percentage value of the SBFL ranking
	 */
	public double getSBFLPercentage() {
		return SBFL;
	}

	/**
	 * @return
	 * the percentage value of the global NLFL ranking
	 */
	public double getGlobalNLFLPercentage() {
		return globalNLFL;
	}

	/**
	 * @return
	 * the percentage value of the local NLFL ranking
	 */
	public double getLocalNLFLPercentage() {
		return localNLFL;
	}
	
	/**
	 * @return 
	 * the percentage value of the SBFL ranking
	 */
	public double getSBFL() {
		return (double)SBFL / 100.0;
	}

	/**
	 * @return
	 * the percentage value of the global NLFL ranking
	 */
	public double getGlobalNLFL() {
		return (double)globalNLFL / 100.0;
	}

	/**
	 * @return
	 * the percentage value of the local NLFL ranking
	 */
	public double getLocalNLFL() {
		return (double)localNLFL / 100.0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final RankingFileWrapper o) {
		int result = Double.compare(o.getSBFLPercentage(), this.getSBFLPercentage());
		if (result == 0) { //if SBFL values are equal
			//result = Integer.compare(this.getGlobalNLFL(), o.getGlobalNLFL());
			//if (result == 0) { //if global NLFL values are equal
				return Double.compare(this.getLocalNLFLPercentage(), o.getLocalNLFLPercentage());
			//} else {
			//	return result;
			//}
		} else {
			return result;
		}
	}

	/**
	 * @return
	 * an array with all rankings (in order of appearance in the ranking file)
	 */
	public double[] getRankings() {
		return rankings;
	}

	/**
	 * @return
	 * a map that links line numbers to a list of corresponding changes
	 */
	public Map<Integer, List<ChangeWrapper>> getLineToModMap() {
		return lineNumberToModMap;
	}

	public long getMinRank() {
		return min_rank;
	}
	
	public long getMinRankCount() {
		return min_rank_count;
	}
	
	public void addToMinRankCount(int min_rank_count) {
		this.min_rank_count += min_rank_count;
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

	public Map<Double, Integer> getFirstAppearance() {
		return firstAppearance;
	}

	public void setFirstAppearance(Map<Double, Integer> firstAppearance) {
		this.firstAppearance = firstAppearance;
	}

	public Map<Double, Integer> getLastAppearance() {
		return lastAppearance;
	}

	public void setLastAppearance(Map<Double, Integer> lastAppearance) {
		this.lastAppearance = lastAppearance;
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
}
