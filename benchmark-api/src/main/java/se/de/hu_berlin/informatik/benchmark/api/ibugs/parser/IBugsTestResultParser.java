/**
 * 
 */
package se.de.hu_berlin.informatik.benchmark.api.ibugs.parser;

import java.io.IOException;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * @author Roy Lieck
 *
 */
public class IBugsTestResultParser {
	
	// this file stores the test results from a previous run
	public static final String TESTRESULTS_FILE_NAME = "/testresults.xml";

	public IBugsTestSuiteWrapper parseTestResultXML( String aRepoRootDir ) {
		
		IBugsTestSuiteWrapper result = new IBugsTestSuiteWrapper();
		
		// get the root directory of the project
		String results_path = Paths.get( aRepoRootDir, TESTRESULTS_FILE_NAME ).toFile().getAbsolutePath();
		
		// this is more like a debug at the moment
		Log.out( this, "Start parsing file " + results_path );
		
		try {
			// aside from the new content handler this code is taken from the oracle guide on how to use the SAX framework
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			SAXEventHandler4IBugsTestresultXML eventHandler = new SAXEventHandler4IBugsTestresultXML();
			xmlReader.setContentHandler(eventHandler);
			xmlReader.parse( results_path );
			
			// save the results
			result.setAllTests( eventHandler.getAllTests() );
			result.setAllTestsWithErrors( eventHandler.getAllTestsWithErrors() );
			result.setFailing( eventHandler.getFailing() );
			result.setPassing( eventHandler.getPassing() );
			result.setSize( eventHandler.getSize() );
			
		} catch (IOException e) {
			Log.err( this, e );
		} catch (SAXException e) {
			Log.err( this, e);
		} catch (ParserConfigurationException e) {
			Log.err( this, e);
		}
		// StringReader seem to not need to be closed in a finally block
		
		return result;
	}
}
