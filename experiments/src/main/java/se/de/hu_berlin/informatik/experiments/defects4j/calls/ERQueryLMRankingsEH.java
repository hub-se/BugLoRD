/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;

import se.de.hu_berlin.informatik.benchmark.api.BugLoRDConstants;
import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.Entity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ERQueryLMRankingsEH extends AbstractProcessor<BuggyFixedEntity<?>,BuggyFixedEntity<?>> {
	
	private String globalLM;
	private String suffix;
	
	private final static Object fileLock = new Object();
	
	/**
	 * Initializes a {@link ERQueryLMRankingsEH} object with the given parameters.
	 * @param suffix 
	 * a suffix to append to the ranking directory (may be null)
	 * @param globalLM
	 * the path to the global lm binary
	 */
	public ERQueryLMRankingsEH(String suffix, String globalLM) {
		super();
		this.suffix = suffix;
		this.globalLM = globalLM;
	}

	@Override
	public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		buggyEntity.requireBug(true);
		
		Entity bug = buggyEntity.getBuggyVersion();
		
		/* #====================================================================================
		 * # query sentences to the LM via kenLM,
		 * # combine the generated rankings
		 * #==================================================================================== */

		File buggyVersionDir = bug.getWorkDir(true).toFile();
		
		if (!buggyVersionDir.exists()) {
			Log.err(this, "Work directory doesn't exist: '" + buggyVersionDir + "'.");
			Log.err(this, "Error while querying sentences and/or combining rankings. Skipping '"
					+ buggyEntity + "'.");
			return null;
		}

		//if the global LM name contains '_dxy_' (xy being a number), then we assume that the AST based
		//semantic tokenizer has been used to generate the LM and we use the respecting method to tokenize
		//the needed lines in the source files
		String depth = null;
		if (globalLM == null) {
			globalLM = BugLoRD.getValueOf(BugLoRDProperties.GLOBAL_LM_BINARY);
		}
		String lmFileName = Paths.get(globalLM).getFileName().toString();
		
		if (!(new File(globalLM)).exists()) {
			Log.err(this, "Given global LM doesn't exist: '" + globalLM + "'.");
			Log.err(this, "Error while querying sentences and/or combining rankings. Skipping '"
					+ buggyEntity + "'.");
			return null;
		}
		
		int pos = lmFileName.indexOf("_d");
		if (pos != -1) {
			int pos2  = lmFileName.indexOf("_", pos+2);
			if (pos2 != -1) {
				depth = lmFileName.substring(pos+2, pos2);
			}
		}
		
		lmFileName = FileUtils.getFileWithoutExtension(lmFileName);
		
		/* #====================================================================================
		 * # preparation
		 * #==================================================================================== */
		String buggyMainSrcDir = bug.getMainSourceDir(true).toString();
		

		/* #====================================================================================
		 * # generate the sentences and query them to the language models
		 * #==================================================================================== */

		String traceFile = bug.getWorkDataDir()
				.resolve(suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
				.resolve(BugLoRDConstants.FILENAME_TRACE_FILE)
				.toString();
		String sentenceOutput = bug.getWorkDataDir()
				.resolve(suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
				.resolve(BugLoRDConstants.FILENAME_SENTENCE_OUT)
				.toString();
		String lmRankingDir = bug.getWorkDataDir()
				.resolve(suffix == null ? BugLoRDConstants.DIR_NAME_RANKING : BugLoRDConstants.DIR_NAME_RANKING + "_" + suffix)
				.resolve(BugLoRDConstants.DIR_NAME_LM_RANKING)
//				.resolve(BugLoRDConstants.FILENAME_LM_RANKING)
				.toString();
		new File(lmRankingDir).mkdirs();
		
		Log.out(this, "Processing: " + traceFile);
		File allLMRankingFileNames = new File(BugLoRDConstants.LM_RANKING_FILENAMES_FILE);

		for (int pre = 0; pre <= BugLoRDConstants.MAX_PRE_TOKENS_COUNT; pre += 1) {
			for (int post = 0; post <= BugLoRDConstants.MAX_POST_TOKENS_COUNT; post += 1) {
				if (depth != null) {
					TokenizeLines.tokenizeLinesDefects4JElementSemantic(
							buggyVersionDir + Defects4J.SEP + buggyMainSrcDir,
							traceFile, sentenceOutput, "10", depth, BugLoRDConstants.INCLUDE_PARENT_IN_TOKEN, 
							String.valueOf(pre), String.valueOf(post));
				} else {
					TokenizeLines.tokenizeLinesDefects4JElement(
							buggyVersionDir + Defects4J.SEP + buggyMainSrcDir,
							traceFile, sentenceOutput, "10");
				}
				String lmRankingFile = getLMRankingFileName(lmFileName, pre, post);

				Defects4J.executeCommand(null, true, "/bin/sh", "-c", BugLoRD.getKenLMQueryExecutable() 
						+ " -n -c " + globalLM + " < " + sentenceOutput + " > " + lmRankingDir + File.separator + lmRankingFile);
				
				try {
					// first check without synchronization
					if (!FileUtils.isLineInFile(lmRankingFile, allLMRankingFileNames)) {
						// only allow access to file for one thread at a time 
						synchronized (fileLock) {
							// weird double check, but necessary if multiple threads run in here
							if (!FileUtils.isLineInFile(lmRankingFile, allLMRankingFileNames)) {
								FileUtils.appendString2File(lmRankingFile, allLMRankingFileNames);
							}
						}
					}
				} catch (IOException e) {
					Log.err(this, e, "Could not read/write lm ranking filename file: ", allLMRankingFileNames);
				}
			}
		}
		
//		//move this to the plotting step?
//		Ranking<String> lmRanking = createCompleteRanking(Paths.get(traceFile), Paths.get(kenLMoutputFile));
//
//		try {
//			Ranking.save(lmRanking, globalRankingFile);
//		} catch (IOException e) {
//			Log.err(this, e, "Could not write lm ranking to '%s'.", globalRankingFile);
//		}
		
		//delete unnecessary directories
//		bug.deleteAllButData();
		
		return buggyEntity;
	}
	
	private static String getLMRankingFileName(String prefix, int pre, int post) {
		prefix = prefix.toLowerCase(Locale.getDefault());
		return prefix + "_" + pre + "_" + post + ".lmr";
	}

//	private Ranking<String> createCompleteRanking(Path traceFile, Path globalRankingFile) {
//		Ranking<String> ranking = new SimpleRanking<>(false);
//		try (BufferedReader traceFileReader = Files.newBufferedReader(traceFile , StandardCharsets.UTF_8);
//				BufferedReader rankingFileReader = Files.newBufferedReader(globalRankingFile , StandardCharsets.UTF_8)) {
//			String traceLine;
//			String rankingLine;
//			while ((traceLine = traceFileReader.readLine()) != null && (rankingLine = rankingFileReader.readLine()) != null) {
//				double rankingValue;
//				if (rankingLine.equals("nan")) {
//					rankingValue = Double.NaN;
//				} else {
//					rankingValue = Double.valueOf(rankingLine);
//				}
//				ranking.add(traceLine, rankingValue);
//			}
//		} catch (IOException e) {
//			Log.abort(this, e, "Could not read trace file or lm ranking file.");
//			return ranking;
//		}
//		
//		return ranking;
//	}

}

