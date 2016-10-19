/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.experiments.calls;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.constants.Defects4JConstants;
import se.de.hu_berlin.informatik.defects4j.frontend.Defects4JEntity;
import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.fileoperations.SearchForFilesOrDirsModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * Runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerQueryAndCombineRankingsEH extends EHWithInputAndReturn<Defects4JEntity,Defects4JEntity> {
	
	public static class Factory extends EHWithInputAndReturnFactory<Defects4JEntity,Defects4JEntity> {

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
		public EHWithInputAndReturn<Defects4JEntity, Defects4JEntity> newFreshInstance() {
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
	public Defects4JEntity processInput(Defects4JEntity buggyEntity) {
		Log.out(this, "Processing project '%s', bug %s.", buggyEntity.getProject(), buggyEntity.getBugId());
		buggyEntity.switchToArchiveDir();
		
		/* #====================================================================================
		 * # query sentences to the LM via kenLM,
		 * # combine the generated rankings
		 * #==================================================================================== */

		File archiveBuggyVersionDir = buggyEntity.getWorkDir().toFile();
		
		if (!archiveBuggyVersionDir.exists()) {
			Log.abort(this, "Archive buggy project version directory doesn't exist: '" + buggyEntity.getWorkDir() + "'.");
		}
		
		//TODO: delete all directories or only for the given localizers?
		List<Path> oldCombinedRankingFolders = new SearchForFilesOrDirsModule("**/ranking/*/*", true)
				.searchForDirectories().skipSubTreeAfterMatch()
				.submit(buggyEntity.getWorkDir())
				.getResult();

		for (Path directory : oldCombinedRankingFolders) {
			FileUtils.delete(directory);
		}

		//if the global LM name contains '_dxy_' (xy being a number), then we assume that the AST based
		//semantic tokenizer has been used to generate the LM and we use the respecting method to tokenize
		//the needed lines in the source files
		String depth = null;
		if (globalLM == null) {
			globalLM = Defects4JEntity.getProperties().globalLM;
		}
		String lmFileName = Paths.get(globalLM).getFileName().toString();
		
		if (!(new File(globalLM)).exists()) {
			Log.err(this, "Given global LM doesn't exist: '" + globalLM + "'.");
			Log.err(this, "Error while querying sentences and/or combining rankings. Skipping project '"
					+ buggyEntity.getProject() + "', bug '" + buggyEntity.getBugId() + "'.");
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
		String srcDirFile = buggyEntity.getWorkDir() + Prop.SEP + Defects4JConstants.FILENAME_SRCDIR;
		String buggyMainSrcDir = null;
		
		try {
			buggyMainSrcDir = Misc.replaceNewLinesInString(FileUtils.readFile2String(Paths.get(srcDirFile)), "");
		} catch (IOException e) {
			Log.warn(this, "No file '%s' containing the source directory path. Generating new one...", srcDirFile);
		}
		
		if (buggyMainSrcDir == null) {
			buggyMainSrcDir = buggyEntity.getMainSourceDir().toString();
			try {
				FileUtils.writeString2File(buggyMainSrcDir, new File(srcDirFile));
			} catch (IOException e1) {
				Log.err(this, "IOException while trying to write to file '%s'.", srcDirFile);
			}
		}
		

		/* #====================================================================================
		 * # generate the sentences and query them to the language models
		 * #==================================================================================== */
		
		List<Path> rankingFiles = new ArrayList<>();
		for (String localizer : Defects4JEntity.getProperties().localizers.split(" ")) {
			localizer = localizer.toLowerCase();
			Path temp = buggyEntity.getWorkDir().resolve(Paths.get("ranking", localizer, "ranking.rnk"));
			if (!temp.toFile().exists() || temp.toFile().isDirectory()) {
				Log.err(this, "'%s' is either not a valid localizer or it is missing the needed ranking file.", localizer);
				Log.err(this, "Error while querying sentences and/or combining rankings. Skipping project '"
						+ buggyEntity.getProject() + "', bug '" + buggyEntity.getBugId() + "'.");
				return null;
			}
			rankingFiles.add(temp);
		}

		List<Path> traceFiles = new SearchForFilesOrDirsModule("**/ranking/*.{trc}", true).searchForFiles()
				.submit(buggyEntity.getWorkDir())
				.getResult();
		
		String traceFile = null;
		boolean foundSingleTraceFile = false;
		String sentenceOutput = buggyEntity.getWorkDir() + Prop.SEP + "ranking" + Prop.SEP + Defects4JConstants.FILENAME_SENTENCE_OUT;
		String globalRankingFile = buggyEntity.getWorkDir() + Prop.SEP + "ranking" + Prop.SEP + Defects4JConstants.FILENAME_LM_RANKING;
		
		//if a single trace file has been found, then compute the global and local rankings only once
		if (traceFiles.size() == 1) {
			foundSingleTraceFile = true;
			traceFile = traceFiles.get(0).toAbsolutePath().toString();
			Log.out(this, "Processing: " + traceFile);
			
			if (depth != null) {
				if (lmFileName.contains("single")) {
					TokenizeLines.tokenizeLinesDefects4JElementSemanticSingle(archiveBuggyVersionDir + Prop.SEP + buggyMainSrcDir,
							traceFile, sentenceOutput, "10", depth);
				} else {
					TokenizeLines.tokenizeLinesDefects4JElementSemantic(archiveBuggyVersionDir + Prop.SEP + buggyMainSrcDir,
							traceFile, sentenceOutput, "10", depth);
				}
			} else {
				TokenizeLines.tokenizeLinesDefects4JElement(archiveBuggyVersionDir + Prop.SEP + buggyMainSrcDir,
						traceFile, sentenceOutput, "10");
			}
			
			Defects4JEntity.getProperties().executeCommand(null, "/bin/sh", "-c", Defects4JEntity.getProperties().kenLMqueryExecutable 
					+ " -n -c " + globalLM + " < " + sentenceOutput + " > " + globalRankingFile);

		}
		
		if (!foundSingleTraceFile) {
			//iterate over all ranking files
			for (Path rankingFile : rankingFiles) {
				Log.out(this, "Processing: " + rankingFile);
				//if none or multiple trace files have been found, use the respective SBFL files
				//instead of a trace file. This queries the sentences to the LMs for each ranking file...

				traceFile = rankingFile.toAbsolutePath().toString();

				if (depth != null) {
					TokenizeLines.tokenizeLinesDefects4JElementSemantic(archiveBuggyVersionDir + Prop.SEP + buggyMainSrcDir,
							traceFile, sentenceOutput, "10", depth);
				} else {
					TokenizeLines.tokenizeLinesDefects4JElement(archiveBuggyVersionDir + Prop.SEP + buggyMainSrcDir,
							traceFile, sentenceOutput, "10");
				}

				Defects4JEntity.getProperties().executeCommand(null, "/bin/sh", "-c", Defects4JEntity.getProperties().kenLMqueryExecutable 
						+ " -n -c " + Defects4JEntity.getProperties().globalLM + " < " + sentenceOutput + " > " + globalRankingFile);
			}
		}

		return buggyEntity;
	}

}

