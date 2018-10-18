/**
 * 
 */
package se.de.hu_berlin.informatik.javatokenizer.tokenizelines;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.de.hu_berlin.informatik.spectra.core.SourceCodeBlock;
import se.de.hu_berlin.informatik.utils.files.processors.FileLineProcessor.StringProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.ComparablePair;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Takes a String that represents a {@link SourceCodeBlock}
 * and generates and updates a {@link Map} which links the respective
 * file path to a {@link List} of line numbers. (The list is
 * created if no entry exists.)
 * 
 * @author Simon Heiden
 */
public class LineParser implements StringProcessor<Map<String, Set<ComparablePair<Integer, Integer>>>> {

	
	private Map<String, Set<ComparablePair<Integer, Integer>>> map;
	
	/**
	 * Creates a new {@link LineParser} object.
	 */
	public LineParser() {
		this.map = new HashMap<>();
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.stringprocessor.IStringProcessor#process(java.lang.String)
	 */
	@Override
	public boolean process(String line) {
		try {
		SourceCodeBlock block = SourceCodeBlock.getNewBlockFromString(line);
		
		map.computeIfAbsent(block.getFilePath(), k -> new HashSet<ComparablePair<Integer, Integer>>())
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
