/**
 * 
 */
package se.de.hu_berlin.informatik.defects4j.frontend.tools.calls;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.combranking.CombineSBFLandNLFLRanking;
import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.javatokenizer.tokenizelines.TokenizeLines;
import se.de.hu_berlin.informatik.utils.fileoperations.FileLineProcessorModule;
import se.de.hu_berlin.informatik.utils.fileoperations.SearchForFilesOrDirsModule;
import se.de.hu_berlin.informatik.utils.fileoperations.ThreadedFileWalkerModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.StringsToListProcessor;

/**
 * {@link Callable} object that runs a single experiment.
 * 
 * @author Simon Heiden
 */
public class ExperimentRunnerQueryAndCombineRankingsCall extends CallableWithPaths<String, Boolean> {
	
	final private String project;
	private String globalLM;
	
	/**
	 * Initializes a {@link ExperimentRunnerQueryAndCombineRankingsCall} object with the given parameters.
	 * @param project
	 * the id of the project under consideration
	 * @param globalLM
	 * the path to the global lm binary
	 */
	public ExperimentRunnerQueryAndCombineRankingsCall(String project, String globalLM) {
		super();
		this.project = project;
		this.globalLM = globalLM;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		String id = getInput();
		
		Prop prop = new Prop(project, id, true);
		prop.switchToArchiveMode();
		
		/* #====================================================================================
		 * # query sentences to the LM via kenLM,
		 * # combine the generated rankings
		 * #==================================================================================== */

		File archiveBuggyVersionDir = new File(prop.buggyWorkDir);
		
		if (!archiveBuggyVersionDir.exists()) {
			Log.abort(this, "Archive buggy project version directory doesn't exist: '" + prop.buggyWorkDir + "'.");
		}
		
		//TODO: delete all directories or only for the given localizers?
		List<Path> oldCombinedRankingFolders = new SearchForFilesOrDirsModule(true, false, "**/ranking/*/*", true, true)
				.submit(Paths.get(prop.buggyWorkDir))
				.getResult();

		for (Path directory : oldCombinedRankingFolders) {
			Misc.delete(directory);
		}

		//if the global LM name contains '_dxy_' (xy being a number), then we assume that the AST based
		//semantic tokenizer has been used to generate the LM and we use the respecting method to tokenize
		//the needed lines in the source files
		String depth = null;
		if (globalLM == null) {
			globalLM = prop.globalLM;
		}
		String lmFileName = Paths.get(globalLM).getFileName().toString();
		
		if (!(new File(globalLM)).exists()) {
			Log.err(this, "Given global LM doesn't exist: '" + globalLM + "'.");
			Log.err(this, "Error while querying sentences and/or combining rankings. Skipping project '"
					+ project + "', bug '" + id + "'.");
			return false;
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
		String srcDirFile = prop.buggyWorkDir + Prop.SEP + Prop.FILENAME_SRCDIR;
		String buggyMainSrcDir = null;
		
		try {
			buggyMainSrcDir = Misc.replaceNewLinesInString(Misc.readFile2String(Paths.get(srcDirFile)), "");
		} catch (IOException e) {
			Log.err(this, "IOException while trying to read file '%s'.", srcDirFile);
		}
		
		if (buggyMainSrcDir == null) {
			buggyMainSrcDir = prop.executeCommandWithOutput(archiveBuggyVersionDir, false, 
					prop.defects4jExecutable, "export", "-p", "dir.src.classes");

			try {
				Misc.writeString2File(buggyMainSrcDir, new File(srcDirFile));
			} catch (IOException e1) {
				Log.err(this, "IOException while trying to write to file '%s'.", srcDirFile);
			}
		}
		

		/* #====================================================================================
		 * # generate the sentences and query them to the language models
		 * #==================================================================================== */
		
		List<Path> rankingFiles = new ArrayList<>();
		for (String localizer : prop.localizers.split(" ")) {
			localizer = localizer.toLowerCase();
			Path temp = Paths.get(prop.buggyWorkDir, "ranking", localizer, "ranking.rnk");
			if (!temp.toFile().exists() || temp.toFile().isDirectory()) {
				Log.err(this, "'%s' is either not a valid localizer or it is missing the needed ranking file.", localizer);
				Log.err(this, "Error while querying sentences and/or combining rankings. Skipping project '"
						+ project + "', bug '" + id + "'.");
				return false;
			}
			rankingFiles.add(temp);
		}

		List<Path> traceFiles = new SearchForFilesOrDirsModule(false, true, "**/ranking/*.{trc}", false, true)
				.submit(Paths.get(prop.buggyWorkDir))
				.getResult();
		
		String traceFile = null;
		boolean foundSingleTraceFile = false;
		String sentenceOutput = prop.buggyWorkDir + Prop.SEP + "ranking" + Prop.SEP + Prop.FILENAME_SENTENCE_OUT;
		String globalRankingFile = prop.buggyWorkDir + Prop.SEP + "ranking" + Prop.SEP + Prop.FILENAME_LM_RANKING;
		
		//if a single trace file has been found, then compute the global and local rankings only once
		if (traceFiles.size() == 1) {
			foundSingleTraceFile = true;
			traceFile = traceFiles.get(0).toAbsolutePath().toString();
			
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
			
			prop.executeCommand(null, "/bin/sh", "-c", prop.kenLMqueryExecutable 
					+ " -n -c " + globalLM + " < " + sentenceOutput + " > " + globalRankingFile);

		}
		
		//iterate over all ranking files
		for (Path rankingFile : rankingFiles) {
			Log.out(this, "Processing: " + rankingFile);
			//if none or multiple trace files have been found, use the respective SBFL files
			//instead of a trace file. This queries the sentences to the LMs for each ranking file...
			if (!foundSingleTraceFile) {
				traceFile = rankingFile.toAbsolutePath().toString();

				if (depth != null) {
					TokenizeLines.tokenizeLinesDefects4JElementSemantic(archiveBuggyVersionDir + Prop.SEP + buggyMainSrcDir,
							traceFile, sentenceOutput, "10", depth);
				} else {
					TokenizeLines.tokenizeLinesDefects4JElement(archiveBuggyVersionDir + Prop.SEP + buggyMainSrcDir,
							traceFile, sentenceOutput, "10");
				}

				prop.executeCommand(null, "/bin/sh", "-c", prop.kenLMqueryExecutable 
						+ " -n -c " + prop.globalLM + " < " + sentenceOutput + " > " + globalRankingFile);
				
			}
			
			//combine the rankings
			String[] gp = prop.percentages.split(" ");
			String[] lp = { "100" };

			CombineSBFLandNLFLRanking.combineSBFLandNLFLRankingsForDefects4JElement(
					rankingFile.toAbsolutePath().toString(), traceFile,
					globalRankingFile, null, rankingFile.toAbsolutePath().getParent().toString(), gp, lp);
		}
		

		
		/* #====================================================================================
		 * # evaluate rankings based on changes in the source code files
		 * #==================================================================================== */
		/* #====================================================================================
		 * # evaluate ranking files based on changed lines
		 * #==================================================================================== */
		String modifiedLinesFile = prop.buggyWorkDir + Prop.SEP + Prop.FILENAME_MOD_LINES;
		
		List<String> lines = new FileLineProcessorModule<List<String>>(new StringsToListProcessor())
				.submit(Paths.get(modifiedLinesFile))
				.getResultFromCollectedItems();
		
		//store the change information in a map for efficiency
		//source file path identifiers are linked to all changes in the respective file
		Map<String, List<ChangeWrapper>> changeInformation = new HashMap<>();
		try {
			List<ChangeWrapper> currentElement = null;
			//iterate over all modified source files and modified lines
			Iterator<String> i = lines.listIterator();
			while (i.hasNext()) {
				String element = i.next();

				//if an entry starts with the specific marking String, then it
				//is a path identifier and a new map entry is created
				if (element.startsWith(Prop.PATH_MARK)) {
					currentElement = new ArrayList<>();
					changeInformation.put(
							element.substring(Prop.PATH_MARK.length()), 
							currentElement);
					continue;
				}

				//format: 0          1            2             3                4				   5
				// | start_line | end_line | entity type | change type | significance level | modification |
				String[] attributes = element.split(ChangeChecker.SEPARATION_CHAR);
				assert attributes.length == 6;

				//ignore change in case of comment related changes
				if (attributes[3].startsWith("COMMENT")) {
					continue;
				}
				
				//add to the list of changes
				currentElement.add(new ChangeWrapper(
						Integer.parseInt(attributes[0]), Integer.parseInt(attributes[1]),
						attributes[2], attributes[3], attributes[4], attributes[5], 0));
			}
		} catch (NullPointerException e) {
			Log.err(this, 
					"Null pointer exception thrown. Probably due to the file '" + modifiedLinesFile 
					+ "' not starting with a path identifier. (Has to begin with the sub string '"
					+ Prop.PATH_MARK + "'.)");
			Log.err(this, 
					"Error while evaluating rankings. Skipping project '"
					+ project + "', bug '" + id + "'.");
			return false;
		} catch (AssertionError e) {
			Log.err(this, 
					"Processed line is in wrong format. Maybe due to containing "
					+ "an additional separation char '" + ChangeChecker.SEPARATION_CHAR + "'.\n"
					+ e.getMessage());
			Log.err(this, 
					"Error while evaluating rankings. Skipping project '"
					+ project + "', bug '" + id + "'.");
			return false;
		}
		
		String rankingDir = prop.buggyWorkDir + Prop.SEP + "ranking";

		new ThreadedFileWalkerModule(false, false, true, "**/*{rnk}", false, 1, EvaluateRankingsCall.class, changeInformation)
		.enableTracking(5)
		.submit(Paths.get(rankingDir))
		.getResult();

		return true;
	}

}

