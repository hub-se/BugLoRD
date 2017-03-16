/**
 * 
 */
package se.de.hu_berlin.informatik.junittestutils.testlister;

import java.util.ArrayList;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import se.de.hu_berlin.informatik.utils.files.processors.FileLineProcessor.StringProcessor;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Takes a {@link String} that is the name of a class and collects all JUnit tests.
 * 
 * @author Simon Heiden
 */
public class TestClassLineProcessor implements StringProcessor<List<String>> {

	List<String> lines = new ArrayList<>();;
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.stringprocessor.IStringProcessor#process(java.lang.String)
	 */
	@Override
	public boolean process(String className) {
		try {
			Class<?> testClazz = Class.forName(className);		
			
			JUnit4TestAdapter tests = new JUnit4TestAdapter(testClazz);
			for (Test t : tests.getTests()) {
				String temp = t.toString();
				if (temp.contains("(")) {
					temp = temp.substring(temp.indexOf('(') + 1, temp.length() - 1) + "::" + temp.substring(0, temp.indexOf('('));
					lines.add(temp);
				} else {
					Log.warn(this, "Test '%s' not parseable.", temp);
				}
			}

			return true;
		} catch (ClassNotFoundException e) {
			Log.err(this, "Class '%s' not found.", className);
		}
		return false;
	}

	/**
	 * @return 
	 * a {@link List} of {@link String}s corresponding to all found test cases
	 */
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor#getResult()
	 */
	@Override
	public List<String> getFileResult() {
		List<String> temp = lines;
		lines = new ArrayList<>();
		return temp;
	}

}
