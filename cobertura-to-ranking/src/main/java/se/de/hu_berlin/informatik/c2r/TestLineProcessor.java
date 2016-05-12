/**
 * 
 */
package se.de.hu_berlin.informatik.c2r;

import java.util.ArrayList;
import java.util.List;
import se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor;

/**
 * Takes a {@link String} that is the identifier of a JUnit test 
 * and adds it to a result list.
 * 
 * @author Simon Heiden
 */
public class TestLineProcessor implements IStringProcessor {

	List<String> lines = new ArrayList<>();;
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.stringprocessor.IStringProcessor#process(java.lang.String)
	 */
	public boolean process(String test) {
		lines.add(test);
		return true;
	}

	/**
	 * @return 
	 * a {@link List} of {@link String}s corresponding to all found test cases
	 */
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor#getResult()
	 */
	public Object getResult() {
		return lines;
	}

}
