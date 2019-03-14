package se.de.hu_berlin.informatik.benchmark.api.ibugs.parser;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * 
 * An event handler for the extraction of bug ids and their properties from the
 * repository.xml file that is supposed to be inside the iBugs project root
 * directory.
 * 
 * @author Roy Lieck
 */
public class SAXEventHandler4IBugsTestresultXML extends DefaultHandler {

	// those keys are used for the root element and describe the entirety of the
	// test runs
	private static final String TESTSUITE_KEY = "testsuite";
	private static final String FAILING_ATTR = "FAILING";
	private static final String PASSING_ATTR = "PASSING";
	private static final String SIZE_ATTR = "SIZE"; // should always be failing
													// + passing

	// those keys are for the attributes of each of the test cases
	private static final String TEST_KEY = "test";
	private static final String FILENAME_ATTR = "FILENAME";
	private static final String JARNAME_ATTR = "JARFILE";
	private static final String NAME_ATTR = "NAME";

	// this is like a property just as an own tag
	private static final String ERROR_KEY = "error";

	private int failing = 0;
	private int passing = 0;
	private int size = 0;
	private List<IBugsTestResultWrapper> allTests = new ArrayList<>();
	private final List<IBugsTestResultWrapper> allTestsWithErrors = new ArrayList<>();

	/**
	 * @return the allTests
	 */
	public List<IBugsTestResultWrapper> getAllTests() {
		return allTests;
	}
	
	/**
	 * @return the allTests
	 */
	public List<IBugsTestResultWrapper> getAllTestsWithErrors() {
		return allTestsWithErrors;
	}

	// is called each time an xml element is opened
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {

		// this should trigger size times
		if (localName.equalsIgnoreCase(TEST_KEY)) {
			String fileName = atts.getValue(FILENAME_ATTR);
			String jarName = atts.getValue(JARNAME_ATTR);
			String name = atts.getValue(NAME_ATTR);

			allTests.add(new IBugsTestResultWrapper(fileName, jarName, name));
		} else if (localName.equalsIgnoreCase(ERROR_KEY)) {
			// mark the last test entry that was added
			 IBugsTestResultWrapper lastWrapper = allTests.remove(allTests.size() - 1);
			
			lastWrapper.finishedWithError(true);
			allTestsWithErrors.add( lastWrapper );
			
		} else if (localName.equalsIgnoreCase(TESTSUITE_KEY)) {
			// I am confident that the parsing from string to int will always
			// succedd
			String failingStr = atts.getValue(FAILING_ATTR);
			failing = Integer.parseInt(failingStr);

			String passingStr = atts.getValue(PASSING_ATTR);
			passing = Integer.parseInt(passingStr);

			String sizeStr = atts.getValue(SIZE_ATTR);
			size = Integer.parseInt(sizeStr);
		} else {
			// mostly for debugging purposes
			Log.out( this, "Parsed an unknown tag with name " + localName );
		}
	}

	/**
	 * @return the failing
	 */
	public int getFailing() {
		return failing;
	}

	/**
	 * @param failing the failing to set
	 */
	public void setFailing(int failing) {
		this.failing = failing;
	}

	/**
	 * @return the passing
	 */
	public int getPassing() {
		return passing;
	}

	/**
	 * @param passing the passing to set
	 */
	public void setPassing(int passing) {
		this.passing = passing;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @param allTests the allTests to set
	 */
	public void setAllTests(List<IBugsTestResultWrapper> allTests) {
		this.allTests = allTests;
	}

}
