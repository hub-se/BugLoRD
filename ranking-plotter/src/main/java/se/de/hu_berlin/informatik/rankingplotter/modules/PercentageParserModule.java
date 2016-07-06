/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.modules;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.rankingplotter.plotter.RankingFileWrapper;
import se.de.hu_berlin.informatik.utils.fileoperations.SearchForFilesOrDirsModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * Parses the SBFL ranking file
 * and each combined ranking file from a submitted directory,
 * producing a {@link List} of {@link RankingFileWrapper} objects.
 * The SBFL ranking file has to be located in the parent directory of
 * the submitted directory.
 * 
 * @author Simon Heiden
 */
public class PercentageParserModule extends AModule<Path, List<RankingFileWrapper>> {

	public enum ParserStrategy { BEST_CASE, AVERAGE_CASE, WORST_CASE, NO_CHANGE }
	
	private boolean parseRankings = false;
	private ParserStrategy strategy;
	private boolean computeAverages = false;
	private boolean ignoreZeroAndBelow = false;
	private boolean ignoreMainRanking;
	
	/**
	 * Creates a new {@link PercentageParserModule} object.
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
	 * @param ignoreMainRanking
	 * whether to ignore the main ranking file
	 */
	public PercentageParserModule(boolean parseRankings, ParserStrategy strategy, 
			boolean computeAverages, boolean ignoreZeroAndBelow, boolean ignoreMainRanking) {
		super(true);
		this.parseRankings = parseRankings;
		this.strategy = strategy;
		this.computeAverages = computeAverages;
		this.ignoreZeroAndBelow = ignoreZeroAndBelow;
		this.ignoreMainRanking = ignoreMainRanking;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public List<RankingFileWrapper> processItem(Path traceFileFolder) {
		if (!traceFileFolder.toFile().exists()) {
			Misc.abort(this, "Folder '%s' doesn't exist.", traceFileFolder.toString());
		} 
		if (!traceFileFolder.toFile().isDirectory()) {
			Misc.abort(this, "'%s' has to be a directory.", traceFileFolder.toString());
		}
		
		int SBFLpercentage = 0;
		int NLFLpercentage = 0;
		int localNLFLpercentage = 0;
		
		//a list of files with parsed SBFL and NLFL percentages (for sorting later on)
		final List<RankingFileWrapper> files = new ArrayList<>();
			
		if (!ignoreMainRanking) {
			//list of all SBFL ranking files
			List<Path> list = new SearchForFilesOrDirsModule("**/*.rnk", false, true, false)
					.submit(traceFileFolder.getParent())
					.getResult();
			if (list.size() == 0) {
				Misc.abort(this, "No SBFL ranking file could be found in '%s'.", traceFileFolder.toString());
			}

			//add the SBFL ranking file
			files.add(new RankingFileWrapper(list.get(0).toFile(), 100, 0, 0,
					parseRankings, strategy, computeAverages, ignoreZeroAndBelow));
		}

		//add the combined ranking files from the given sub folder
		for (final File file : traceFileFolder.toFile().listFiles()) {
	        if (file.isDirectory()) {
	        	final String temp = file.getName();
	        	int pos = temp.indexOf("SBFL_");
	        	if (pos == -1) {
	        		Misc.err(this, "Subfolder '%s' can not be parsed.", temp);
	        		continue;
	        	}
	        	SBFLpercentage = Integer.parseInt(temp.substring(pos+5));
	        	NLFLpercentage = 100 - SBFLpercentage;
	        	for (final File subfile : file.listFiles()) {
	        		if (subfile.getName().endsWith(".crnk")) {	        			
	        			final String temp2 = subfile.getName();
	        			localNLFLpercentage = Integer.parseInt(temp2.substring(temp2.lastIndexOf('_')+1, temp2.length()-5));
	        			files.add(new RankingFileWrapper(subfile, SBFLpercentage, NLFLpercentage, localNLFLpercentage, 
	        					parseRankings, strategy, computeAverages, ignoreZeroAndBelow));
	        		}
	        	}
	        } else {
	        	if (file.getName().endsWith(".crnk")) {
	        		final String temp = file.getName();
	        		int pos = temp.indexOf("SBFL_");
		        	if (pos == -1) {
		        		Misc.err(this, "File '%s' can not be parsed.");
		        		continue;
		        	}
		        	SBFLpercentage = Integer.parseInt(temp.substring(pos+5, temp.indexOf('_', pos+5)));
		        	NLFLpercentage = 100 - SBFLpercentage;
	        		localNLFLpercentage = 0;
	        		files.add(new RankingFileWrapper(file, SBFLpercentage, NLFLpercentage, localNLFLpercentage, 
	        				parseRankings, strategy, computeAverages, ignoreZeroAndBelow));
	        	}
	        }
	    }
		
		//sort the files
		files.sort(null);
		
		return files;
	}

}
