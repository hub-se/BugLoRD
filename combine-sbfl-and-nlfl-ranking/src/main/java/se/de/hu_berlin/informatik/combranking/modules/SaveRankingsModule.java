/**
 * 
 */
package se.de.hu_berlin.informatik.combranking.modules;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import se.de.hu_berlin.informatik.combranking.Rankings;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
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
				
		Integer[] sBFLpercentages = {10, 25, 50, 75, 90};
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

		for (int i = 0; i < sBFLpercentages.length; ++i) {
			Path temp = Paths.get(outputDir.toString() + File.separator + 
					lineFile.getFileName().toString().replaceAll("[.]", "_") + File.separator +
					"SBFL_" + sBFLpercentages[i]);
			temp.toFile().mkdirs();
			for (int j = 0; j < globalNLFLpercentages.length; ++j) {
				saveRankingWithLambda(map, 
						(double)sBFLpercentages[i]/100, globalrankingNormalize, 
						(double)globalNLFLpercentages[j]/100, localrankingNormalize,
						temp,
						"GlobNLFL_" + globalNLFLpercentages[j] + "_LocNLFL_" + String.valueOf(100-globalNLFLpercentages[j]));
			}
		}

		
//		for (int i = 0; i < percentages.length; ++i) {
//			Path temp = Paths.get(outputDir.toString() + File.separator + 
//					lineFile.getFileName().toString().replaceAll("[.]", "_"));
//			temp.toFile().mkdirs();
//			saveRankingWithLambda(map, 
//					(double)percentages[i]/100, globalrankingNormalize,
//					temp,
//					"SBFL_" + percentages[i] + "_NLFL_" + String.valueOf(100-percentages[i]));
//		}
		
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

//	/**
//	 * Saves a new ranking file with a combined ranking. The ranking is computed with the formula
//	 * <br>{@code lambda * SBFL-ranking + (1-lambda) * global NLFL-ranking}. 
//	 * @param map
//	 * containing {@link Rankings} objects that are linked to "relative/path/To/File:lineNumber"-lines
//	 * @param lambda
//	 * value such that {@code 0 <= lambda <= 1}
//	 * @param globalrankingNormalize
//	 * a value to normalize the global NLFL ranking
//	 * @param output
//	 * path to output directory
//	 * @param filename
//	 * output filename (prefix, will be extended by ".crnk")
//	 */
//	private void saveRankingWithLambda(final Map<String, Rankings> map, 
//			final double lambda, final double globalrankingNormalize, final Path output, final String filename) {		
//		for (Entry<String, Rankings> entry : map.entrySet()) {
//			entry.getValue().setCombinedRanking(lambda, globalrankingNormalize);
//		}
//		
//		List<String> lines = sortByValue(map);
//		try {
//			Files.write(Paths.get(output.toString() + File.separator + filename + ".crnk"), lines, StandardCharsets.UTF_8);
//		} catch (IOException e) {
//			Misc.err(this, "Could not save rankings in path %s.", output.toString() + File.separator + filename + ".crnk");
//		}
//	}
	
	/**
	 * Saves a new ranking file with a combined ranking, including a local NLFL ranking. The ranking is computed with the formula
	 * <br>{@code lambda * SBFL-ranking + (1-lambda) * (lamda2 * global NLFL-ranking + (1-lamda2) local NLFL ranking)}. 
	 * @param map
	 * containing {@link Rankings} objects that are linked to "relative/path/To/File:lineNumber"-lines
	 * @param lambda
	 * value such that {@code 0 <= lambda <= 1}
	 * @param globalrankingNormalize
	 * a value to normalize the global NLFL ranking
	 * @param lambda2
	 * value such that {@code 0 <= lambda2 <= 1}
	 * @param localrankingNormalize
	 * a value to normalize the local NLFL ranking
	 * @param output
	 * path to output directory
	 * @param filename
	 * output filename (prefix, will be extended by ".crnk")
	 */
	private void saveRankingWithLambda(final Map<String, Rankings> map, 
			final double lambda, final double globalrankingNormalize, 
			final double lambda2, final double localrankingNormalize, final Path output, final String filename) {		
		for (Entry<String, Rankings> entry : map.entrySet()) {
			entry.getValue().setCombinedRanking(lambda, globalrankingNormalize, lambda2, localrankingNormalize);
		}
		
		List<String> lines = sortByValue(map);
		try {
			Files.write(Paths.get(output.toString() + File.separator + filename + ".crnk"), lines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			Log.err(this, "Could not save rankings in path %s.", output.toString() + File.separator + filename + ".crnk");
		}
	}

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
