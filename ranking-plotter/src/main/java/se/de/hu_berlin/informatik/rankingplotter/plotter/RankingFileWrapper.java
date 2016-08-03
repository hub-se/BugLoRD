/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.SignificanceLevel;
import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Helper class that stores percentage values and other helper structures
 * that are parsed from a combined ranking file.
 * 
 * @author Simon Heiden
 */
public class RankingFileWrapper implements Comparable<RankingFileWrapper> {
	
	/**
	 * Stores the path to the combined ranking file.
	 */
	private File rankingFile;
	
	/**
	 * Stores the percentage value of the SBFL ranking.
	 */
	private int SBFL;
	
	/**
	 * Stores the percentage value of the global NLFL ranking.
	 */
	private int globalNLFL;
	
	/**
	 * Stores the percentage value of the local NLFL ranking.
	 */
	private int localNLFL;
	
	/**
	 * An array with all rankings  (in order of appearance in the ranking file).
	 */
	private Double[] rankings = null;
	
	/**
	 * A map that links line numbers to a list of corresponding changes.
	 */
	private Map<Integer, List<ChangeWrapper>> lineNumberToModMap;

	/**
	 * A file containing data about modified lines (usually ending with ".modlines").
	 */
	private File modLinesFile;
	
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
	
	private Map<Double, Integer> firstAppearance = null;
	private Map<Double, Integer> lastAppearance = null;

	private long all = 0;
	private long allSum = 0;
	
	/**
	 * Creates a new {@link RankingFileWrapper} object with the given parameters.
	 * @param rankingFile
	 * the path to a combined ranking file
	 * @param sBFL
	 * the percentage value of the SBFL ranking
	 * @param globalNLFL
	 * the percentage value of the global NLFL ranking
	 * @param localNLFL
	 * the percentage value of the local NLFL ranking
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
	public RankingFileWrapper(File rankingFile, int sBFL, int globalNLFL, int localNLFL,
			boolean parseRankings, ParserStrategy strategy, boolean computeAverages, boolean ignoreZeroAndBelow) {
		super();
		this.rankingFile = rankingFile;
		this.SBFL = sBFL;
		this.globalNLFL = globalNLFL;
		this.localNLFL = localNLFL;
		
		if (rankingFile != null) {
			if (parseRankings) {
				this.rankings = parseRankings(this, rankingFile, ignoreZeroAndBelow);
			}

			setModLinesFile(".modlines");
			this.lineNumberToModMap = 
					parseModLinesFile(modLinesFile, parseRankings, strategy, computeAverages, ignoreZeroAndBelow);
		}
	}
	
	/**
	 * Parses the modified lines file.
	 * @param modLinesFile
	 * the modified lines file
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
	private Map<Integer, List<ChangeWrapper>> parseModLinesFile(File modLinesFile, boolean parseRankings, 
			ParserStrategy strategy, boolean computeAverages, boolean ignoreZeroAndBelow) {
		Map<Integer, List<ChangeWrapper>> lineToModMap = new HashMap<>();
		String line = null;
		try (final BufferedReader fileReader = Files.newBufferedReader(modLinesFile.toPath(), StandardCharsets.UTF_8)) {
			if (parseRankings) {
				firstAppearance = new HashMap<>();
				lastAppearance = new HashMap<>();
				for (int i = 0; i < rankings.length; ++i) {
					firstAppearance.putIfAbsent(rankings[i], i+1);
					lastAppearance.put(rankings[i], i+1);
				}
			}
			
			while((line = fileReader.readLine()) != null) {
				String[] attributes = null;
				try {
					//format: 0         1          2           3            4             5                 6
					//  | rank_pos | ranking | start_line | end_line | entity type | change type | significance level |
					attributes = line.split(ChangeChecker.SEPARATION_CHAR);
					assert attributes.length == 7;
				} catch (AssertionError e) {
					Log.abort(this, "Processed line is in wrong format. Maybe due to containing "
							+ "an additional separation char '" + ChangeChecker.SEPARATION_CHAR + "'.\n"
							+ e.getMessage());
				}
				
				int rank_pos = Integer.parseInt(attributes[0]);
				
				//continue with the next line if the parsed rank corresponds
				//to a line that is zero or below zero
				if (ignoreZeroAndBelow && rank_pos > rankings.length) {
					continue;
				}
				
				if (parseRankings) {
					switch(strategy) {
					case BEST_CASE:
						//use the best value if the corresponding ranking spans
						//over multiple lines
						rank_pos = firstAppearance.get(rankings[rank_pos-1]);
						break;
					case AVERAGE_CASE:
						//use the middle value if the corresponding ranking spans
						//over multiple lines
						rank_pos = (int)((lastAppearance.get(rankings[rank_pos-1]) 
								+ firstAppearance.get(rankings[rank_pos-1])) / 2.0);
						break;
					case WORST_CASE:
						//use the worst value if the corresponding ranking spans
						//over multiple lines
						rank_pos = lastAppearance.get(rankings[rank_pos-1]);
						break;
					case NO_CHANGE:
						//use the parsed line number
						break;
					default:
						Log.err(this, "Unknown strategy!");
					}
				}
				ChangeWrapper change = new ChangeWrapper(
						Integer.parseInt(attributes[2]), Integer.parseInt(attributes[3]),
						attributes[4], attributes[5], attributes[6]);
				if (lineToModMap.containsKey(rank_pos)) {
					lineToModMap.get(rank_pos).add(change);
				} else {
					List<ChangeWrapper> list = new ArrayList<>();
					list.add(change);
					lineToModMap.put(rank_pos, list);
				}
			}
		} catch (IOException e) {
			Log.abort(this, "IOException while processing %s.", modLinesFile.toString());
		} catch (NumberFormatException e) {
			Log.abort(this, "Could not parse line number from line '%s'.", line);
		}
		
		for (Entry<Integer, List<ChangeWrapper>> entry : lineToModMap.entrySet()) {
			int rank_pos = entry.getKey();
			SignificanceLevel significance = getHighestSignificanceLevel(entry.getValue());
		
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

	/**
	 * Parses the rankings from the ranking file to an array.
	 * @param o
	 * this object (only for error message purposes)
	 * @param rankingFile
	 * the ranking file
	 * @param ignoreZeroAndBelow
	 * whether to ignore ranking values that are zero or below zero 
	 * (rankings will not be added to the array)
	 * @return
	 * an array with all rankings (in order of appearance in the ranking file)
	 */
	private static Double[] parseRankings(Object o, File rankingFile, boolean ignoreZeroAndBelow) {
		String line = null;
		List<Double> rankings = new ArrayList<>();
		try (final BufferedReader fileReader = Files.newBufferedReader(rankingFile.toPath(), StandardCharsets.UTF_8)) {
			while((line = fileReader.readLine()) != null) {
				double ranking = Double.parseDouble(line.substring(line.lastIndexOf(':')+2, line.length()));
				//break the loop if rankings equal to and below zero shall be ignored
				if (ignoreZeroAndBelow && ranking <= 0) {
					break;
				}
				rankings.add(ranking);
			}
		} catch (IOException e) {
			Log.abort(o, "IOException while processing %s.", rankingFile.toString());
		} catch (NumberFormatException e) {
			Log.abort(o, "Could not parse ranking from line '%s'.", line);
		}
		return rankings.toArray(new Double[rankings.size()]);
	}
	
	/**
	 * @return
	 * the combined ranking file
	 */
	public File getRankingFile() {
		return rankingFile;
	}
	
	/**
	 * @return
	 * the file containing data about modified lines (usually ending with ".modlines")
	 */
	public File getModLinesFile() {
		return modLinesFile;
	}
	
	/**
	 * Sets the file containing data about modified lines. Will abort the application if
	 * no file exists.
	 * @param extension
	 * the extension of the modified lines file (usually ".modlines")
	 */
	private void setModLinesFile(String extension) {
		Path temp = Paths.get(rankingFile.toString() + extension);
		if (!temp.toFile().exists()) {
			Log.abort(this, "File with modified lines '%s' doesn't exist.", temp.toString());
		}
		modLinesFile = temp.toFile();
	}

	/**
	 * @return 
	 * the percentage value of the SBFL ranking
	 */
	public int getSBFLPercentage() {
		return SBFL;
	}

	/**
	 * @return
	 * the percentage value of the global NLFL ranking
	 */
	public int getGlobalNLFLPercentage() {
		return globalNLFL;
	}

	/**
	 * @return
	 * the percentage value of the local NLFL ranking
	 */
	public int getLocalNLFLPercentage() {
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
		int result = Integer.compare(o.getSBFLPercentage(), this.getSBFLPercentage());
		if (result == 0) { //if SBFL values are equal
			//result = Integer.compare(this.getGlobalNLFL(), o.getGlobalNLFL());
			//if (result == 0) { //if global NLFL values are equal
				return Integer.compare(this.getLocalNLFLPercentage(), o.getLocalNLFLPercentage());
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
	public Double[] getRankings() {
		return rankings;
	}

	/**
	 * @return
	 * a map that links line numbers to a list of corresponding changes
	 */
	public Map<Integer, List<ChangeWrapper>> getLineToModMap() {
		return lineNumberToModMap;
	}

	public long getAll() {
		return all;
	}
	
	public void addToAll(long all) {
		this.all += all;
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
}
