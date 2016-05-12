/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenizelines;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor;

/**
 * Takes a String of format: 'relative/path/To/File:lineNumber'
 * and generates and updates a {@link Map} which links the 
 * file path to a {@link List} of line numbers. (The list is
 * created if no entry exists.)
 * 
 * @author Simon Heiden
 */
public class LineParser implements IStringProcessor {

	
	private Map<String, Set<Integer>> map;
	
	/**
	 * Creates a new {@link LineParser} object with the given parameters.
	 * @param map
	 * links trace file relative paths given as {@link String}s with a {@link List} 
	 * of line numbers
	 */
	public LineParser(Map<String, Set<Integer>> map) {
		this.map = map;
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
		
		String path = line.substring(0, pos);
		Integer lineNo = new Integer(Integer.parseInt(line.substring(pos+1, pos2)));
		
		if (map.containsKey(path)) {
			map.get(path).add(lineNo);
		} else {
			map.put(path, new HashSet<Integer>());
			map.get(path).add(lineNo);
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
	@Override
	public Object getResult() {
		return map;
	}

}
