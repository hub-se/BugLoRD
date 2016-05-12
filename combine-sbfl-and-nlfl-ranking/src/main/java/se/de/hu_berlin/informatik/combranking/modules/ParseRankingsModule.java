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
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
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
		
		final HashMap<String, Rankings> map = new HashMap<>();
		try (final BufferedReader SBFLreader = Files.newBufferedReader(sBFLFile , StandardCharsets.UTF_8); 
				final BufferedReader linereader = Files.newBufferedReader(lineFile , StandardCharsets.UTF_8);
				final BufferedReader NLFLreader = Files.newBufferedReader(rankingFile , StandardCharsets.UTF_8)) {
			
			String rankingline = null;
			while((rankingline = SBFLreader.readLine()) != null) {
				final int pos = rankingline.lastIndexOf(':');
				if (pos == -1) {
					Misc.abort(this, "Entry \"%s\" not valid.", rankingline);
				}
				//key: "relative/path/To/File:lineNumber", 	value: "SBFL-ranking"
				map.put(rankingline.substring(0, pos), new Rankings(Double.parseDouble(rankingline.substring(pos+2, rankingline.length()))));
			}
			
			String line = null;
			String rankline = null;
			if (localRankingFile != null) {
				try (final BufferedReader localNLFLreader = Files.newBufferedReader(localRankingFile , StandardCharsets.UTF_8)) {
					String localrankline = null;
					while((line = linereader.readLine()) != null 
							& (rankline = NLFLreader.readLine()) != null 
							& (localrankline = localNLFLreader.readLine()) != null) {
						int pos = line.indexOf(':');
						if (pos == -1) {
							Misc.abort(this, "Entry \"%s\" not valid.", line);
						}

						//ranking file?
						int pos2 = line.indexOf(':', pos+1);
						if (pos2 == -1) {
							pos2 = line.length();
						}

						line = line.substring(0, pos2);
						try {
							map.get(line).setGlobalNLFLRanking(new Double(Double.parseDouble(rankline)));
						} catch (NullPointerException e) {
							Misc.abort(this, "Entry \"%s\" not found.", line);
						} catch (Exception e) {
							Misc.err(this, "Error for entry \"%s\": '%s'. Setting to default: 0.", line, rankline);
						}
						try {
							map.get(line).setlocalNLFLRanking(new Double(Double.parseDouble(localrankline)));
						} catch (Exception e) {
							Misc.err(this, "Error for entry \"%s\": '%s'. Setting to default: 0.", line, localrankline);
						}
					}
					if (line != null || rankingline != null || localrankline != null) {
						Misc.abort(this, "Trace file and ranking files don't match.");
					}
				} catch (IOException x) {
					Misc.abort(this, x, "Could not open/read file \"%s\".", localRankingFile.toString());
				}
			} else {
				while((line = linereader.readLine()) != null 
						& (rankline = NLFLreader.readLine()) != null) {
					try {
						map.get(line).setGlobalNLFLRanking(new Double(Double.parseDouble(rankline)));
					} catch (NullPointerException e) {
						Misc.abort(this, "Entry \"%s\" not found.", line);
					} catch (Exception e) {
						Misc.err(this, e, "Error for entry \"%s\": '%s'. Setting to default: 0.", line, rankline);
					}
				}
				if (line != null || rankingline != null) {
					Misc.abort(this, "Trace file and global NLFL ranking file don't match.");
				}
			}
		} catch (IOException e) {
			Misc.abort(this, "Could not open/read an input file.");
		}
		
		return map;
	}

}
