/**
 * 
 */
package se.de.hu_berlin.informatik.combranking.modules;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import se.de.hu_berlin.informatik.combranking.Rankings;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.fileoperations.csv.CSVUtils;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * Saves the rankings in multiple result files.
 * 
 * @author Simon Heiden
 *
 */
public class SaveRankingsModule extends AModule<Map<String, Rankings>,Object> {

	private Path lineFile;
	private Path outputDir;
	private String[] sBFLPercentages;
	private String[] globalNLFLPercentages;
	
	/**
	 * Creates a new {@link SaveRankingsModule} object with the given parameters.
	 * @param lineFile
	 * file with file names and line numbers (format: relative/path/To/File:lineNumber)
	 * @param outputDir
	 * path to output directory
	 * @param sBFLPercentages
	 * an array of percentage values that determine the weighting 
	 * of the SBFL ranking to the NLFL ranking
	 * @param globalNLFLPercentages
	 * an array of percentage values that determine the weighting 
	 * of the global NLFL ranking to the local NLFL ranking
	 */
	public SaveRankingsModule(Path lineFile, Path outputDir, String[] sBFLPercentages, String[] globalNLFLPercentages) {
		super(true);
		this.lineFile = lineFile;
		this.outputDir = outputDir;
		this.sBFLPercentages = sBFLPercentages;
		this.globalNLFLPercentages = globalNLFLPercentages;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public Object processItem(Map<String, Rankings> map) {
				
		Integer[] sBFLpercentages = {0, 10, 20, 50, 75, 90, 100};
		Integer[] globalNLFLpercentages = {100};
		if (sBFLPercentages != null) {
			final List<Integer> temp = new ArrayList<>();
			for (int i = 0; i < sBFLPercentages.length; ++i) {
				temp.add(Integer.parseInt(sBFLPercentages[i]));
			}
			sBFLpercentages = temp.toArray(new Integer[0]);
		}
		
		if (globalNLFLPercentages != null) {
			final List<Integer> temp = new ArrayList<>();
			for (int i = 0; i < globalNLFLPercentages.length; ++i) {
				temp.add(Integer.parseInt(globalNLFLPercentages[i]));
			}
			globalNLFLpercentages = temp.toArray(new Integer[0]);
		}
		
//		double globalrankingNormalize = computeMaxGlobalRanking(map);
		double globalrankingNormalize = 10;
		//if the maximal ranking is below 1, then just set it to 1
		globalrankingNormalize = globalrankingNormalize > 1 ? globalrankingNormalize : 1;

//		double localrankingNormalize = computeMaxLocalRanking(map);
		double localrankingNormalize = 10;
		//if the maximal ranking is below 1, then just set it to 1
		localrankingNormalize = localrankingNormalize > 1 ? localrankingNormalize : 1;

		List<String[]> combinedRankingsList = new ArrayList<>(sBFLpercentages.length * globalNLFLpercentages.length + 1);
		String[] identifiers = new String[map.entrySet().size() + 1];
		identifiers[0] = lineFile.toAbsolutePath().toString();
		int k = 0;
		for (Entry<String, Rankings> entry : map.entrySet()) {
			identifiers[++k] = entry.getKey();
		}
		combinedRankingsList.add(identifiers);
		
		for (int i = 0; i < sBFLpercentages.length; ++i) {
			for (int j = 0; j < globalNLFLpercentages.length; ++j) {
				String[] combinedRankings = new String[map.entrySet().size() + 1];
				//first entry: SBFL_percentage:global_NLFL_percentage
				combinedRankings[0] = sBFLpercentages[i] + ":" + globalNLFLpercentages[j];
				k = 0;
				for (Entry<String, Rankings> entry : map.entrySet()) {
					//following entries: combined_ranking_values
					combinedRankings[++k] = 
							String.valueOf(entry.getValue().setCombinedRanking(
									(double)sBFLpercentages[i]/100, globalrankingNormalize, 
									(double)globalNLFLpercentages[j]/100, localrankingNormalize));
				}
				combinedRankingsList.add(combinedRankings);
			}
		}
		
		List<String> lines = CSVUtils.toMirroredCsv(combinedRankingsList);

		Path output = Paths.get(outputDir.toString(), 
				lineFile.getFileName().toString().replaceAll("[.]", "_"),
				"combined_rankings.crnk");
		new ListToFileWriterModule<List<String>>(output, true).submit(lines);
		
		return null;
	}

//	/**
//	 * Computes the maximum global NLFL ranking of all {@link Rankings} objects in the given map.
//	 * @param map
//	 * containing {@link Rankings} objects that are linked to "relative/path/To/File:lineNumber"-lines
//	 */
//	private static double computeMaxGlobalRanking(final Map<String, Rankings> map) {
//		double max = 0;
//		for (Entry<String, Rankings> entry : map.entrySet()) {
//			max = entry.getValue().getGlobalNLFLRanking() > max ? entry.getValue().getGlobalNLFLRanking() : max;
//		}
//		return max;
//	}
//	
//	/**
//	 * Computes the maximum local NLFL ranking of all {@link Rankings} objects in the given map.
//	 * @param map
//	 * containing {@link Rankings} objects that are linked to "relative/path/To/File:lineNumber"-lines
//	 */
//	private static double computeMaxLocalRanking(final HashMap<String, Rankings> map) {
//		double max = 0;
//		for (Entry<String, Rankings> entry : map.entrySet()) {
//			max = entry.getValue().getlocalNLFLRanking() > max ? entry.getValue().getlocalNLFLRanking() : max;
//		}
//		return max;
//	}
	
	/**
	 * Sorts the given map based on combined ranking values.
	 * @param map
	 * containing {@link Rankings} objects that are linked to "relative/path/To/File:lineNumber"-lines
	 * @return
	 * list of {@link String}s with sorted lines
	 */
	private static List<String> sortByValue(final Map<String, Rankings> map) {
		List<String> result = new ArrayList<>();
		Stream <Entry<String,Rankings>> st = map.entrySet().stream();

		st.sorted(Comparator.comparing(e -> e.getValue()))
		.forEachOrdered(e ->result.add(e.getKey() + ": " + e.getValue().getCombinedRanking()));

		return result;
	}
}
