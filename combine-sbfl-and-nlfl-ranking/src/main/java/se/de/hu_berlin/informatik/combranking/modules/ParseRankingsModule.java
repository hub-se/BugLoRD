/**
 * 
 */
package se.de.hu_berlin.informatik.combranking.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import se.de.hu_berlin.informatik.combranking.Rankings;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.tm.moduleframework.AModule;

/**
 * Produces a {@link HashMap} which links file paths (as Strings) to {@link Rankings} objects.
 * 
 * @author Simon Heiden
 * 
 * @see Rankings
 */
public class ParseRankingsModule extends AModule<Object, Map<String, Rankings>> {

	private Path sBFLFile = null;
	private Path lineFile = null;
	private Path rankingFile = null;
	private Path localRankingFile = null;
	private boolean errorOccurred = false;
	
	/**
	 * Creates a new {@link ParseRankingsModule} object with the given parameters.
	 * @param sBFLFile
	 * file with SBFL ranking (format: relative/path/To/File:lineNumber: ranking)
	 * @param lineFile
	 * file with file names and line numbers (format: relative/path/To/File:lineNumber)
	 * @param rankingFile
	 * file with global NLFL rankings corresponding to the line file (format: ranking)
	 * @param localRankingFile
	 * file with local NLFL rankings corresponding to the line file (format: ranking)
	 */
	public ParseRankingsModule(Path sBFLFile, Path lineFile, Path rankingFile, Path localRankingFile) {
		super(false);
		this.sBFLFile = sBFLFile;
		this.lineFile = lineFile;
		this.rankingFile = rankingFile;
		this.localRankingFile = localRankingFile;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.ITransmitter#processItem(java.lang.Object)
	 */
	public Map<String, Rankings> processItem(Object item) {
		errorOccurred = false;
		
		final Map<String, Rankings> map = new HashMap<>();
		try (final BufferedReader SBFLreader = Files.newBufferedReader(sBFLFile , StandardCharsets.UTF_8); 
				final BufferedReader linereader = Files.newBufferedReader(lineFile , StandardCharsets.UTF_8);
				final BufferedReader NLFLreader = Files.newBufferedReader(rankingFile , StandardCharsets.UTF_8)) {
			
			//parse all SBFL rankings into a map
			String rankingline = null;
			while((rankingline = SBFLreader.readLine()) != null) {
				final int pos = rankingline.lastIndexOf(':');
				if (pos == -1) {
					Log.abort(this, "Entry \"%s\" not valid.", rankingline);
				}
				//key: "relative/path/To/File:lineNumber", 	value: "SBFL-ranking"
				map.put(rankingline.substring(0, pos), new Rankings(Double.parseDouble(rankingline.substring(pos+2, rankingline.length()))));
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
						setRankings(map, line, rankline, localrankline);
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
					setRankings(map, line, rankline, null);
				}
				if (line != null || rankingline != null) {
					Log.abort(this, "Trace file and global NLFL ranking file don't match in size.");
				}
			}
		} catch (IOException e) {
			Log.abort(this, "Could not open/read an input file.");
		}
		
		if (errorOccurred) {
			Log.err(this, "Some rankings were not parseable and were set to '0'.");
		}
		
		return map;
	}

	private void setRankings(Map<String, Rankings> map, 
			String traceFileLine, String globalRankingLine, String localRankingLine) {
		int pos = traceFileLine.indexOf(':');
		if (pos == -1) {
			Log.abort(this, "Entry \"%s\" not valid.", traceFileLine);
		}

		//is the trace file an SBFL ranking file? Then pos2 != -1
		int pos2 = traceFileLine.indexOf(':', pos+1);
		if (pos2 != -1) {
			traceFileLine = traceFileLine.substring(0, pos2);
		}

		try {
			map.get(traceFileLine).setGlobalNLFLRanking(Double.valueOf(globalRankingLine));
		} catch (NullPointerException e) {
			Log.abort(this, "Entry \"%s\" not found.", traceFileLine);
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
}
