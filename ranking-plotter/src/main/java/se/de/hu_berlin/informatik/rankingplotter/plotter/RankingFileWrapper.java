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

import se.de.hu_berlin.informatik.rankingplotter.modules.PercentageParserModule.ParserStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

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
	 * A map that links line numbers to some kind of modification identifier ('a', 'c' or 'd' (and 'n')).
	 */
	private Map<Integer, String> lineNumberToModMap;

	/**
	 * A file containing data about modified lines (usually ending with ".modlines").
	 */
	private File modLinesFile;
	
	private long appends = 0;
	private long changes = 0;
	private long deletes = 0;
	private long neighbors = 0;
	
	private long appendsSum = 0;
	private long changesSum = 0;
	private long deletesSum = 0;
	private long neighborsSum = 0;
	
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
	 * a map that links line numbers to some kind of modification identifier ('a', 'c' or 'd' (and 'n'))
	 */
	private Map<Integer, String> parseModLinesFile(File modLinesFile, boolean parseRankings, 
			ParserStrategy strategy, boolean computeAverages, boolean ignoreZeroAndBelow) {
		Map<Integer, String> map = new HashMap<>();
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
				int pos = line.indexOf(':');
				if (pos == -1) {
					break;
				}
				int rank = Integer.parseInt(line.substring(0, pos));
				
				//continue with the next line if the parsed rank corresponds
				//to a line that is zero or below zero
				if (ignoreZeroAndBelow && rank > rankings.length) {
					continue;
				}
				
				if (parseRankings) {
					switch(strategy) {
					case BEST_CASE:
						//use the best value if the corresponding ranking spans
						//over multiple lines
						rank = firstAppearance.get(rankings[rank-1]);
						break;
					case AVERAGE_CASE:
						//use the middle value if the corresponding ranking spans
						//over multiple lines
						rank = (int)((lastAppearance.get(rankings[rank-1]) 
								+ firstAppearance.get(rankings[rank-1])) / 2.0);
						break;
					case WORST_CASE:
						//use the worst value if the corresponding ranking spans
						//over multiple lines
						rank = lastAppearance.get(rankings[rank-1]);
						break;
					case NO_CHANGE:
						//use the parsed line number
						break;
					default:
						Misc.err(this, "Unknown strategy!");
					}
				}
				map.put(rank, line.substring(pos+1));

				if (computeAverages) {
					switch(line.substring(pos+1)) {
					case "a":
						appendsSum += rank;
						++appends;
						allSum += rank;
						++all;
						break;
					case "c":
						changesSum += rank;
						++changes;
						allSum += rank;
						++all;
						break;
					case "d":
						deletesSum += rank;
						++deletes;
						allSum += rank;
						++all;
						break;
					case "n":
						neighborsSum += rank;
						++neighbors;
						break;
					default:
						Misc.err(this, "Unknown modification identifier '%s'.", line.substring(pos+1));
						break;
					}
				}
			}
		} catch (IOException e) {
			Misc.abort(this, "IOException while processing %s.", modLinesFile.toString());
		} catch (NumberFormatException e) {
			Misc.abort(this, "Could not parse line number from line '%s'.", line);
		}
		
		return map;
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
			Misc.abort(o, "IOException while processing %s.", rankingFile.toString());
		} catch (NumberFormatException e) {
			Misc.abort(o, "Could not parse ranking from line '%s'.", line);
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
			Misc.abort(this, "File with modified lines '%s' doesn't exist.", temp.toString());
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
	 * a map that links line numbers to some kind of modification identifier ('a', 'c' or 'd')
	 */
	public Map<Integer, String> getLineToModMap() {
		return lineNumberToModMap;
	}

	public long getAll() {
		return all;
	}
	
	public void addToAll(long all) {
		this.all += all;
	}
	
	public long getAppends() {
		return appends;
	}
	
	public void addToAppends(long appends) {
		this.appends += appends;
	}

	public long getChanges() {
		return changes;
	}

	public void addToChanges(long changes) {
		this.changes += changes;
	}

	public long getDeletes() {
		return deletes;
	}

	public void addToDeletes(long deletes) {
		this.deletes += deletes;
	}

	public long getNeighbors() {
		return neighbors;
	}

	public void addToNeighbors(long neighbors) {
		this.neighbors += neighbors;
	}

	public double getAllSum() {
		return allSum;
	}

	public void addToAllSum(double allSum) {
		this.allSum += allSum;
	}
	
	public double getAppendsSum() {
		return appendsSum;
	}

	public void addToAppendsSum(double appendsSum) {
		this.appendsSum += appendsSum;
	}

	public double getChangesSum() {
		return changesSum;
	}

	public void addToChangesSum(double changesSum) {
		this.changesSum += changesSum;
	}

	public double getDeletesSum() {
		return deletesSum;
	}

	public void addToDeletesSum(double deletesSum) {
		this.deletesSum += deletesSum;
	}

	public double getNeighborsSum() {
		return neighborsSum;
	}

	public void addToNeighborsSum(double neighborsSum) {
		this.neighborsSum += neighborsSum;
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
	
	public double getAppendsAverage() {
		return (double)appendsSum / (double)appends;
	}
	
	public double getChangesAverage() {
		return (double)changesSum / (double)changes;
	}

	public double getDeletesAverage() {
		return (double)deletesSum / (double)deletes;
	}
	
	public double getNeighborsAverage() {
		return (double)neighborsSum / (double)neighbors;
	}
}
