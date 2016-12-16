/**
 * 
 */
package se.de.hu_berlin.informatik.experiments.ibugs.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * An event handler for the parsing of the properties file of iBugs
 * 
 * @author Roy Lieck
 */
public class SAXEventHandler4IBugsProperties extends DefaultHandler {
	
	// different constants for reading the properties.xml of iBugs
	public static final String ANT_KEY = "ANT_EXECUTABLE";
	public static final String PROPS_PARENT_KEY = "target";
	public static final String PROPS_KEY = "property";
	public static final String NAME_KEY = "name";
	public static final String VALUE_KEY = "value";
	
	public static final String VERSION_SUBDIR_KEY = "VERSION_DIRECTORY";
	public static final String LOCATION_KEY = "location";

	private String antExecutablePath = null;
	private String versionsSubDirPath = null;

	/**
	 * @return the antExecutablePath
	 */
	public String getAntExecutablePath() {
		return antExecutablePath;
	}

	/**
	 * @param antExecutablePath the antExecutablePath to set
	 */
	public void setAntExecutablePath(String antExecutablePath) {
		this.antExecutablePath = antExecutablePath;
	}

	/**
	 * @return the versionsSubDirPath
	 */
	public String getVersionsSubDirPath() {
		return versionsSubDirPath;
	}

	/**
	 * @param versionsSubDirPath the versionsSubDirPath to set
	 */
	public void setVersionsSubDirPath(String versionsSubDirPath) {
		this.versionsSubDirPath = versionsSubDirPath;
	}

	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		
		if( localName.equalsIgnoreCase( PROPS_KEY ) ) {
			String name = atts.getValue( NAME_KEY );
			if( name.equalsIgnoreCase( ANT_KEY ) ) {
				antExecutablePath = atts.getValue( VALUE_KEY );
			} else if ( name.equalsIgnoreCase( VERSION_SUBDIR_KEY ) ) {
				versionsSubDirPath = atts.getValue( LOCATION_KEY );
			}
		}

	}

}
