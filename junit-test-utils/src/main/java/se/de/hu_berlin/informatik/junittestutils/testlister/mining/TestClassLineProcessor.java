/**
 * 
 */
package se.de.hu_berlin.informatik.junittestutils.testlister.mining;

import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.java7.testrunner.TestWrapper;
import se.de.hu_berlin.informatik.utils.files.processors.FileLineProcessor.StringProcessor;
import se.de.hu_berlin.informatik.utils.processors.basics.ItemCollector;
import se.de.hu_berlin.informatik.utils.processors.sockets.module.ModuleLinker;

/**
 * Takes a {@link String} that is the name of a class and collects all JUnit tests.
 * 
 * @author Simon Heiden
 */
public class TestClassLineProcessor implements StringProcessor<List<TestWrapper>> {

	private List<TestWrapper> lines = new ArrayList<>();
	private ClassLoader testClassLoader;
	
	public TestClassLineProcessor(ClassLoader testClassLoader) {
		this.testClassLoader = testClassLoader;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.stringprocessor.IStringProcessor#process(java.lang.String)
	 */
	@Override
	public boolean process(String className) {
		
		ItemCollector<TestWrapper> itemCollector = new ItemCollector<TestWrapper>();
		new ModuleLinker().append(
				new TestMinerProcessor(testClassLoader, false),
				itemCollector)
		.submit(className);
		
		lines = itemCollector.getCollectedItems();
		
		return true;
	}
	
	@Override
	public List<TestWrapper> getLineResult() {
		List<TestWrapper> temp = lines;
		lines = new ArrayList<>();
		return temp;
	}


}
