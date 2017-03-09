/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenizelines;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.files.processors.FileLineProcessor.StringProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.ComparablePair;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Takes a String of format: 'relative/path/To/File:lineNumber'
 * and generates and updates a {@link Map} which links the 
 * file path to a {@link List} of line numbers. (The list is
 * created if no entry exists.)
 * 
 * @author Simon Heiden
 */
public class LineParser implements StringProcessor<Map<String, Set<ComparablePair<Integer, Integer>>>> {

	
	private Map<String, Set<ComparablePair<Integer, Integer>>> map;
	
	/**
	 * Creates a new {@link LineParser} object with the given parameters.
	 * @param map
	 * links trace file relative paths given as {@link String}s with a {@link List} 
	 * of line numbers
	 */
	public LineParser(Map<String, Set<ComparablePair<Integer, Integer>>> map) {
		this.map = map;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.stringprocessor.IStringProcessor#process(java.lang.String)
	 */
	public boolean process(String line) {
		try {
		SourceCodeBlock block = SourceCodeBlock.getNewBlockFromString(line);
		
		map.computeIfAbsent(block.getClassName(), k -> new HashSet<ComparablePair<Integer, Integer>>())
		.add(new ComparablePair<>(block.getStartLineNumber(), block.getEndLineNumber()));
		} catch (IllegalArgumentException e) {
			Log.err(this, e);
			return false;
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
	public Map<String, Set<ComparablePair<Integer, Integer>>> getFileResult() {
		return map;
	}

}
