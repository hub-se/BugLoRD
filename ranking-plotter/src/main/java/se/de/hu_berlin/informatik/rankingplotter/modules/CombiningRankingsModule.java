/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter.ParserStrategy;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JConstants;
import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Plotter;
import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingFileWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.Rankings;
import se.de.hu_berlin.informatik.utils.fileoperations.FileLineProcessorModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AbstractModule;
import se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.StringsToListProcessor;

/**
 * Parses the SBFL ranking file
 * and each combined ranking file from a submitted directory,
 * producing a {@link List} of {@link RankingFileWrapper} objects.
 * The SBFL ranking file has to be located in the parent directory of
 * the submitted directory.
 * 
 * @author Simon Heiden
 */
public class CombiningRankingsModule extends AbstractModule<Path, List<RankingFileWrapper>> {
	
	private boolean errorOccurred = false;
	
	private boolean parseRankings = false;
	private ParserStrategy strategy;
	private boolean computeAverages = false;
	private boolean ignoreZeroAndBelow = false;
	
	private String[] sbflPercentages;
	private String[] nlflPercentages;
	
	/**
	 * Creates a new {@link CombiningRankingsModule} object.
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
	 * @param sbflPercentages
	 * an array of percentage values that determine the weighting 
	 * of the SBFL ranking to the NLFL ranking
	 * @param nlflPercentages
	 * an array of percentage values that determine the weighting 
	 * of the global NLFL ranking to the local NLFL ranking
	 */
	public CombiningRankingsModule(boolean parseRankings, ParserStrategy strategy, 
			boolean computeAverages, boolean ignoreZeroAndBelow,
			String[] sbflPercentages, String[] nlflPercentages) {
		super(true);
		errorOccurred = false;
		this.parseRankings = parseRankings;
		this.strategy = strategy;
		this.computeAverages = computeAverages;
		this.ignoreZeroAndBelow = ignoreZeroAndBelow;
		
		this.sbflPercentages = sbflPercentages;
		this.nlflPercentages = nlflPercentages;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public List<RankingFileWrapper> processItem(Path sbflRankingFile) {
		errorOccurred = false;
		double maxSbflRanking = Double.NEGATIVE_INFINITY;
		try (final BufferedReader SBFLreader = Files.newBufferedReader(sbflRankingFile , StandardCharsets.UTF_8)) {
			//get the maximal ranking value that is NOT infinity
			String rankingline = null;
			while((rankingline = SBFLreader.readLine()) != null) {
				final int pos = rankingline.lastIndexOf(':');
				if (pos == -1) {
					Log.abort(this, "Entry '%s' not valid in '%s'.", rankingline, sbflRankingFile.toAbsolutePath());
				}
				//key: "relative/path/To/File:lineNumber", 	value: "SBFL-ranking"
				double ranking = Double.parseDouble(rankingline.substring(pos+2, rankingline.length()));
				if (Double.isNaN(ranking) && ranking != Double.POSITIVE_INFINITY) {
					if (ranking > maxSbflRanking) {
						maxSbflRanking = ranking;
					}
				}
			}
		} catch (IOException e1) {
			Log.abort(this, "Could not open/read the SBFL ranking file '%s'.", sbflRankingFile);
		}
		
		//TODO: set these paths correctly
		Path lineFile = sbflRankingFile.getParent().getParent().resolve(Defects4JConstants.FILENAME_TRACE_FILE);
		Path globalRankingFile = sbflRankingFile.getParent().getParent().resolve(Defects4JConstants.FILENAME_LM_RANKING);
		Path localRankingFile = sbflRankingFile.getParent().getParent().resolve(Defects4JConstants.FILENAME_LM_RANKING);
		if (!localRankingFile.toFile().exists()) {
			localRankingFile = null;
		}
		final Map<String, Rankings> map = new HashMap<>();
		try (final BufferedReader SBFLreader = Files.newBufferedReader(sbflRankingFile , StandardCharsets.UTF_8); 
				final BufferedReader linereader = Files.newBufferedReader(lineFile , StandardCharsets.UTF_8);
				final BufferedReader NLFLreader = Files.newBufferedReader(globalRankingFile , StandardCharsets.UTF_8)) {
			
			//parse all SBFL rankings into a map
			String rankingline = null;
			while((rankingline = SBFLreader.readLine()) != null) {
				final int pos = rankingline.lastIndexOf(':');
				if (pos == -1) {
					Log.abort(this, "Entry '%s' not valid in '%s'.", rankingline, sbflRankingFile.toAbsolutePath());
				}
				//key: "relative/path/To/File:lineNumber", 	value: "SBFL-ranking"
				double ranking = Double.parseDouble(rankingline.substring(pos+2, rankingline.length()));
				if (ranking == Double.POSITIVE_INFINITY) {
					ranking = maxSbflRanking + 1;
				}
				map.put(rankingline.substring(0, pos), new Rankings(rankingline.substring(0, pos), ranking));
			}
			
			//parse all global and local NLFL rankings which are corresponding to the lines in the given 
			//line file (possibly the SBFL ranking file, but may be another) 
			String line = null;
			String rankline = null;
			if (localRankingFile != null) {
				try (final BufferedReader localNLFLreader = Files.newBufferedReader(localRankingFile , StandardCharsets.UTF_8)) {
					String localrankline = null;
					while((line = linereader.readLine()) != null 
							& (rankline = NLFLreader.readLine()) != null 
							& (localrankline = localNLFLreader.readLine()) != null) {
						setRankings(lineFile, map, line, rankline, localrankline);
					}
					if (line != null || rankingline != null || localrankline != null) {
						Log.abort(this, "Trace file and ranking files don't match in size.");
					}
				} catch (IOException x) {
					Log.abort(this, x, "Could not open/read file \"%s\".", localRankingFile.toString());
				}
			} else {
				while((line = linereader.readLine()) != null 
						& (rankline = NLFLreader.readLine()) != null) {
					setRankings(lineFile, map, line, rankline, null);
				}
				if (line != null || rankingline != null) {
					Log.abort(this, "Trace file and global NLFL ranking file don't match in size.");
				}
			}
		} catch (IOException e) {
			Log.abort(this, "Could not open/read an input file.");
		}
		
		if (errorOccurred) {
			Log.warn(this, "Some rankings were not parseable and were set to '0'.");
		}
		
		
		Map<String, List<ChangeWrapper>> changeInformation = getChangeInformation(sbflRankingFile.toAbsolutePath().getParent().getParent().getParent());
		
		
		//a list of files with parsed SBFL and NLFL percentages (for sorting later on)
		final List<RankingFileWrapper> files = new ArrayList<>();
		
		double[] sBFLpercentages = {0.0, 10.0, 20.0, 50.0, 75.0, 90.0, 100.0};
		double[] globalNLFLpercentages = {100.0};
		if (sbflPercentages != null) {
			sBFLpercentages = new double[sbflPercentages.length];
			for (int i = 0; i < sbflPercentages.length; ++i) {
				sBFLpercentages[i] = Double.parseDouble(sbflPercentages[i]);
			}
		}
		
		if (nlflPercentages != null) {
			globalNLFLpercentages = new double[nlflPercentages.length];
			for (int i = 0; i < nlflPercentages.length; ++i) {
				globalNLFLpercentages[i] = Double.parseDouble(nlflPercentages[i]);
			}
		}

		Path mainDir = sbflRankingFile.getParent().getParent().getParent();
		String project = mainDir.getParent().getFileName().toString();
		String bugDirName = mainDir.getFileName().toString();
		int bugId = Integer.valueOf(bugDirName.substring(0, bugDirName.length()-1));
		for (double sbflPercentage : sBFLpercentages) {
			for (double nlflPercentage : globalNLFLpercentages) {
				files.add(new RankingFileWrapper(project, bugId, 
						map, sbflPercentage, nlflPercentage, changeInformation, 
						parseRankings, strategy, computeAverages, ignoreZeroAndBelow));
			}	
		}
		
		//sort the ranking wrappers
		files.sort(null);
		
		return files;
	}
	
	
	private void setRankings(Path lineFile, Map<String, Rankings> map, 
			String traceFileLine, String globalRankingLine, String localRankingLine) {
		int pos = traceFileLine.indexOf(':');
		if (pos == -1) {
			Log.abort(this, "Entry '%s' not valid in '%s'.", traceFileLine, lineFile.toAbsolutePath());
		}

		//is the trace file an SBFL ranking file? Then pos2 != -1
		int pos2 = traceFileLine.indexOf(':', pos+1);
		if (pos2 != -1) {
			traceFileLine = traceFileLine.substring(0, pos2);
		}

		try {
			map.get(traceFileLine).setGlobalNLFLRanking(Double.valueOf(globalRankingLine));
		} catch (NullPointerException e) {
			Log.abort(this, "Entry '%s' not found in '%s'.", traceFileLine, lineFile.toAbsolutePath());
		} catch (Exception e) {
//			Misc.err(this, "Error for global NLFL ranking entry \"%s\": '%s'. Setting to: -Infinity.", traceFileLine, globalRankingLine);
			errorOccurred = true;
			map.get(traceFileLine).setGlobalNLFLRanking(0);
		}
		if (localRankingLine != null) {
			try {
				map.get(traceFileLine).setlocalNLFLRanking(Double.valueOf(localRankingLine));
			} catch (Exception e) {
//				Misc.err(this, "Error for local NLFL ranking entry \"%s\": '%s'. Setting to: -Infinity.", traceFileLine, localRankingLine);
				errorOccurred = true;
				map.get(traceFileLine).setlocalNLFLRanking(0);
			}
		}
	}

	private static Map<String, List<ChangeWrapper>> getChangeInformation(Path mainDir) {
		String modifiedLinesFile = mainDir + File.separator + Defects4JConstants.FILENAME_MOD_LINES;
		
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
				if (element.startsWith(Defects4JConstants.PATH_MARK)) {
					currentElement = new ArrayList<>();
					changeInformation.put(
							element.substring(Defects4JConstants.PATH_MARK.length()), 
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
			Log.abort(Plotter.class, 
					"Null pointer exception thrown. Probably due to the file '" + modifiedLinesFile 
					+ "' not starting with a path identifier. (Has to begin with the sub string '"
					+ Defects4JConstants.PATH_MARK + "'.)");
		} catch (AssertionError e) {
			Log.abort(Plotter.class, 
					"Processed line is in wrong format. Maybe due to containing "
					+ "an additional separation char '" + ChangeChecker.SEPARATION_CHAR + "'.\n"
					+ e.getMessage());
		}
		
		return changeInformation;
	}
	
}
