/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4J;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JConstants;
import se.de.hu_berlin.informatik.benchmark.ranking.Ranking;
import se.de.hu_berlin.informatik.benchmark.ranking.SimpleRanking;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD;
import se.de.hu_berlin.informatik.experiments.defects4j.BugLoRD.BugLoRDProperties;
import se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerQueryAndCombineRankingsEH extends EHWithInputAndReturn<BuggyFixedEntity,BuggyFixedEntity> {
	
	public static class Factory extends EHWithInputAndReturnFactory<BuggyFixedEntity,BuggyFixedEntity> {

		final private String globalLM;
		
		/**
		 * Initializes a {@link Factory} object.
		 * @param globalLM
		 * the path to the global lm binary
		 */
		public Factory(String globalLM) {
			super(ExperimentRunnerQueryAndCombineRankingsEH.class);
			this.globalLM = globalLM;
		}

		@Override
		public EHWithInputAndReturn<BuggyFixedEntity, BuggyFixedEntity> newFreshInstance() {
			return new ExperimentRunnerQueryAndCombineRankingsEH(globalLM);
		}
	}
	
	private String globalLM;
	
	/**
	 * Initializes a {@link ExperimentRunnerQueryAndCombineRankingsEH} object with the given parameters.
	 * @param globalLM
	 * the path to the global lm binary
	 */
	public ExperimentRunnerQueryAndCombineRankingsEH(String globalLM) {
		super();
		this.globalLM = globalLM;
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	@Override
	public BuggyFixedEntity processInput(BuggyFixedEntity buggyEntity) {
		Log.out(this, "Processing %s.", buggyEntity);
		
		/* #====================================================================================
		 * # query sentences to the LM via kenLM,
		 * # combine the generated rankings
		 * #==================================================================================== */

		File archiveBuggyVersionDir = buggyEntity.getWorkDir(false).toFile();
		
		if (!archiveBuggyVersionDir.exists()) {
			Log.err(this, "Work data directory doesn't exist: '" + buggyEntity.getWorkDataDir() + "'.");
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
		
		/* #====================================================================================
		 * # preparation
		 * #==================================================================================== */
		String buggyMainSrcDir = buggyEntity.getMainSourceDir(false).toString();
		

		/* #====================================================================================
		 * # generate the sentences and query them to the language models
		 * #==================================================================================== */
		
		List<Path> rankingFiles = new ArrayList<>();
		for (String localizer : BugLoRD.getValueOf(BugLoRDProperties.LOCALIZERS).split(" ")) {
			localizer = localizer.toLowerCase(Locale.getDefault());
			Path temp = buggyEntity.getWorkDataDir().resolve(
					Paths.get(Defects4JConstants.DIR_NAME_RANKING, localizer, Defects4JConstants.FILENAME_RANKING_FILE));
			if (!temp.toFile().exists() || temp.toFile().isDirectory()) {
				Log.err(this, "'%s' is either not a valid localizer or it is missing the needed ranking file.", localizer);
				Log.err(this, "Error while querying sentences and/or combining rankings. Skipping '"
						+ buggyEntity + "'.");
				return null;
			}
			rankingFiles.add(temp);
		}

		String traceFile = buggyEntity.getWorkDataDir()
				.resolve(Defects4JConstants.DIR_NAME_RANKING)
				.resolve(Defects4JConstants.FILENAME_TRACE_FILE)
				.toString();
		String sentenceOutput = buggyEntity.getWorkDataDir()
				.resolve(Defects4JConstants.DIR_NAME_RANKING)
				.resolve(Defects4JConstants.FILENAME_SENTENCE_OUT)
				.toString();
		String globalRankingFile = buggyEntity.getWorkDataDir()
				.resolve(Defects4JConstants.DIR_NAME_RANKING)
				.resolve(Defects4JConstants.FILENAME_LM_RANKING)
				.toString();
		
		Log.out(this, "Processing: " + traceFile);

		if (depth != null) {
			if (lmFileName.contains("single")) {
				TokenizeLines.tokenizeLinesDefects4JElementSemanticSingle(archiveBuggyVersionDir + Defects4J.SEP + buggyMainSrcDir,
						traceFile, sentenceOutput, "10", depth);
			} else {
				TokenizeLines.tokenizeLinesDefects4JElementSemantic(archiveBuggyVersionDir + Defects4J.SEP + buggyMainSrcDir,
						traceFile, sentenceOutput, "10", depth);
			}
		} else {
			TokenizeLines.tokenizeLinesDefects4JElement(archiveBuggyVersionDir + Defects4J.SEP + buggyMainSrcDir,
					traceFile, sentenceOutput, "10");
		}

		Defects4J.executeCommand(null, "/bin/sh", "-c", BugLoRD.getKenLMQueryExecutable() 
				+ " -n -c " + globalLM + " < " + sentenceOutput + " > " + globalRankingFile + "_tmp");

		createCompleteRanking(Paths.get(traceFile), Paths.get(globalRankingFile + "_tmp"), globalRankingFile);

		return buggyEntity;
	}

	private void createCompleteRanking(Path traceFile, Path globalRankingFile, String output) {
		Ranking<String> ranking = new SimpleRanking<>(false);
		try (BufferedReader traceFileReader = Files.newBufferedReader(traceFile , StandardCharsets.UTF_8);
				BufferedReader rankingFileReader = Files.newBufferedReader(globalRankingFile , StandardCharsets.UTF_8)) {
			String traceLine;
			String rankingLine;
			while ((traceLine = traceFileReader.readLine()) != null && (rankingLine = rankingFileReader.readLine()) != null) {
				ranking.add(traceLine, Double.valueOf(rankingLine));
			}
		} catch (IOException e) {
			Log.err(this, e, "Could not read trace file or lm ranking file.");
			return;
		}
		
		try {
			Ranking.save(ranking, output);
		} catch (IOException e) {
			Log.err(this, e, "Could not write lm ranking to '%s'.", output);
		}
	}

}

