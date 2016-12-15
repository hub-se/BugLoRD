/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.ibugs.utils;

import java.io.IOException;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import se.de.hu_berlin.informatik.benchmark.api.ibugs.IBugs;
import se.de.hu_berlin.informatik.benchmark.api.ibugs.IBugsOptions.IBugsCmdOptions;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

/**
 * @author Roy Lieck
 *
 */
public class IBugsPropertiesXMLParser {
	
	/**
	 * Two important options are stored in the properties.xml file from iBugs.
	 * The path to the ant executable is one of them and needs to  be extracted.
	 * The other one is the the name of the subdirectory for the different versions of the repositories.
	 * 
	 * After this method is called the two static variables ANT_EXE and VERSION_SUBDIR
	 * in the IBugs class are updated with the values found in the properties.xml file.
	 * 
	 * This method is not static for logging reasons.
	 * 
	 * @param aOP The option parser with the path to the root directory of the project
	 */
	public void parseXMLProps( OptionParser aOP ) {
		
		// get the root directory of the project
		String project_root = aOP.getOptionValue( IBugsCmdOptions.PROJECT_ROOT_DIR );
		String props_path = Paths.get( project_root, IBugs.PROPERTIES_FILE_NAME ).toFile().getAbsolutePath();
		
		try {
			// aside from the new content handler this code is taken from the oracle guide on how to use the SAX framework
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			SAXEventHandler4IBugs xmlCH = new SAXEventHandler4IBugs();
			xmlReader.setContentHandler(xmlCH);
			xmlReader.parse( props_path );
		
			String value = xmlCH.getAntExecutablePath();
			if ( value != null ) {
				Log.out( this, "Found ant executable in properties: " + value );
				IBugs.ANT_EXE = value;
			}
			
			value = xmlCH.getVersionsSubDirPath();
			if ( value != null ) {
				Log.out( this, "Found the name for the versions sub directories: " + value );
				IBugs.VERSION_SUBDIR = value;
			}
			
		} catch (IOException e) {
			Log.err( this, e );
		} catch (SAXException e) {
			Log.err( this, e);
		} catch (ParserConfigurationException e) {
			Log.err( this, e);
		}
		// StringReader seem to not need to be closed in a finally block
	}
}
