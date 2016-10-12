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
public class TestLineProcessor implements IStringProcessor<List<String>> {

	final private List<String> lines = new ArrayList<>();

	@Override
	public boolean process(final String test) {
		lines.add(test);
		return true;
	}

	/**
	 * @return 
	 * a {@link List} of {@link String}s corresponding to all found test cases
	 */
	@Override
	public List<String> getResult() {
		return lines;
	}

}
