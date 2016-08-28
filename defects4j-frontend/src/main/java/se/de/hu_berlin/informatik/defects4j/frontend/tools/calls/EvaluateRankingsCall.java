package se.de.hu_berlin.informatik.defects4j.frontend.tools.calls;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.changechecker.ChangeChecker;
import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.defects4j.frontend.Prop;
import se.de.hu_berlin.informatik.utils.fileoperations.ListToFileWriterModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;

/**
 * Parses combined ranking files and searches for the rankings of modified lines.
 * 
 * @author Simon
 */
public class EvaluateRankingsCall extends CallableWithPaths<Path,Boolean> {

	final Map<String, List<ChangeWrapper>> changeInformation;
	
	public EvaluateRankingsCall(Map<String, List<ChangeWrapper>> changeInformation) {
		super();
		this.changeInformation = changeInformation;
	}

	@Override
	public Boolean call() throws Exception {
		Path rankingFile = getInput();
		
		new ListToFileWriterModule<List<String>>(
				rankingFile.getParent().resolve(rankingFile.getFileName() + Prop.EXTENSION_MOD_LINES), true)
		.submit(parseRankingFile(rankingFile.toString(), changeInformation));
		
		return null;
	}

	/**
	 * Parses a ranking file and returns a list of Strings that connect lines of the ranking
	 * file with respective changes. Format of the lines is:
	 * <p>line_numer:start_line:end_line:entity_type:change_type:significance_level
	 * @param rankingFile
	 * path to a ranking file as a String
	 * @param changeInformation
	 * a mapping that maps path identifiers to change information
	 * @return
	 * a list of Strings, connecting lines of the ranking file with their respective changes
	 */
	private List<String> parseRankingFile(String rankingFile, Map<String, List<ChangeWrapper>> changeInformation) {
		List<String> lines = new ArrayList<>();
		try (BufferedReader bufRead = new BufferedReader(new FileReader(rankingFile))) {
			String line = null;
			int lineCounter = 0;
			while ((line = bufRead.readLine()) != null) {
				++lineCounter;
				//format: path:line_number: ranking
				String[] ranking = line.split(":");
				assert ranking.length == 3;
				
				if (changeInformation.containsKey(ranking[0])) {
					int lineNumber = Integer.parseInt(ranking[1]);
					List<ChangeWrapper> changes = changeInformation.get(ranking[0]);
					
					for (ChangeWrapper entry : changes) {
						//is the ranked line inside of a changed statement?
						if (lineNumber >= entry.getStart() && lineNumber <= entry.getEnd()) {
							lines.add(lineCounter + ChangeChecker.SEPARATION_CHAR
									+ lineNumber + ChangeChecker.SEPARATION_CHAR
									+ ranking[2].substring(1) + ChangeChecker.SEPARATION_CHAR
									+ entry.toString());
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			Log.abort(this, 
					"Ranking file does not exist: '" + rankingFile + "'.");
		} catch (IOException e) {
			Log.abort(this, 
					"IOException while reading ranking file: '" + rankingFile + "'.");
		}
		
		return lines;
	}
	
}
