/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenizelines;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;
import se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.StringProcessor;

/**
 * Takes a String of format: 'relative/path/To/File:lineNumber'
 * and adds a matching sentence to the result list. The matching sentence
 * is obtained from the given sentence map that maps Strings in the
 * input String format to corresponding sentences.
 * 
 * @author Simon Heiden
 */
public class LineMatcher implements StringProcessor<List<String>> {

	
	private Map<String, String> sentenceMap;
	
	private List<String> lines;
	
	/**
	 * Creates a new {@link LineMatcher} object with the given parameters.
	 * @param sentenceMap
	 * maps trace file lines to sentences
	 */
	public LineMatcher(Map<String, String> sentenceMap) {
		this.sentenceMap = sentenceMap;
		lines = new ArrayList<>();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.stringprocessor.IStringProcessor#process(java.lang.String)
	 */
	public boolean process(String line) {
		int pos = line.indexOf(':');
		if (pos == -1) {
			return false;
		}
		
		//ranking file?
		int pos2 = line.indexOf(':', pos+1);
		if (pos2 == -1) {
			pos2 = line.length();
		}
		
		line = line.substring(0, pos2);
		String sentence;
		if ((sentence = sentenceMap.get(line)) != null) {
			lines.add(Misc.replaceNewLinesInString(sentence, "_"));
		} else {
			lines.add("");
		}
		return true;
	}

	/**
	 * @return 
	 * the given {@link Map}
	 */
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor#getResult()
	 */
	public List<String> getResult() {
		return lines;
	}

}
