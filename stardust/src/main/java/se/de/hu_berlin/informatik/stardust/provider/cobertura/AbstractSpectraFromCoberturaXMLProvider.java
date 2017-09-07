/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.cobertura;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import se.de.hu_berlin.informatik.stardust.provider.AbstractSpectraProvider;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.stardust.spectra.HitSpectra;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Loads cobertura.xml files to {@link HitSpectra} objects where each covered line is represented by one node and each file
 * represents one trace in the resulting spectra. (Does not use initial coverage data.)
 * 
 * @author Simon
 *
 * @param <T>
 * the type of nodes in the spectra to provide
 */
public abstract class AbstractSpectraFromCoberturaXMLProvider<T> extends AbstractSpectraProvider<T, ITrace<T>, CoberturaCoverageWrapper> {

    /**
     * Create a cobertura provider.
     */
    public AbstractSpectraFromCoberturaXMLProvider(CoberturaCoverageWrapper initialCoverageData) {
        this(initialCoverageData, false);
    }
    
    public AbstractSpectraFromCoberturaXMLProvider(CoberturaCoverageWrapper initialCoverageData, boolean storeHits) {
        super(initialCoverageData, storeHits);
    }

	@Override
    public boolean loadSingleCoverageData(final CoberturaCoverageWrapper traceFile,
            final boolean onlyAddNodes) {
		if (onlyAddNodes) {
			return true;
		}
		//ignore coverage dtd file (unnecessary http requests, possibly failing if server is down...)
    	String fileWithoutDTD = null;
		try {
			fileWithoutDTD = new String(Files.readAllBytes(traceFile.getXmlCoverageFile().toPath()));
		} catch (IOException e) {
			Log.err(this, "Could not read coverage xml file '%s'.", traceFile.getXmlCoverageFile());
			return false;
		}
    	int pos = fileWithoutDTD.indexOf("<!DOCTYPE coverage");
    	if (pos != -1) {
    		int pos2 = fileWithoutDTD.indexOf(">", pos);
    		if (pos2 != -1) {
    			fileWithoutDTD = fileWithoutDTD.substring(0, pos) + fileWithoutDTD.substring(pos2+1);
    		}
    	}
    	
        final ITrace<T> trace;
        if (traceFile.getIdentifier() == null) {
        	trace = lineSpectra.addTrace(
        			FileUtils.getFileNameWithoutExtension(traceFile.getXmlCoverageFile().toString()), 
        			traceFile.isSuccessful());	
        } else {
        	trace = lineSpectra.addTrace(
        			traceFile.getIdentifier(), 
        			traceFile.isSuccessful());
        }
        
        Document doc = null;
		try {
			doc = new SAXBuilder().build(new StringReader(fileWithoutDTD));
		} catch (JDOMException e) {
			Log.err(this, e, "JDOMException in coverage xml file '%s'.", traceFile.getXmlCoverageFile());
			return false;
		} catch (IOException e) {
			Log.err(this, e, "Could not parse coverage xml file '%s'.", traceFile.getXmlCoverageFile());
			return false;
		}

        // loop over all packages of the trace file
        for (final Object pckgObj : doc.getRootElement().getChild("packages").getChildren()) {
            final Element pckg = (Element) pckgObj;
            final String packageName = pckg.getAttributeValue("name");

            // loop over all classes of the package
            for (final Object clssObj : pckg.getChild("classes").getChildren()) {
                final Element clss = (Element) clssObj;
                final String className = clss.getAttributeValue("filename");

                // create hierarchical spectra
                packageSpectra.setParent(packageName, className);

                // loop over all methods of the class
                for (final Object mthdObj : clss.getChild("methods").getChildren()) {
                    final Element method = (Element) mthdObj;
                    final String methodName = method.getAttributeValue("name") + method.getAttributeValue("signature");
                    final String methodIdentifier = String.format("%s:%s", className, methodName);

                    // create hierarchical spectra
                    classSpectra.setParent(className, methodIdentifier);

                    // loop over all lines of the method
                    for (final Object lineObj : method.getChild("lines").getChildren()) {
                        final Element line = (Element) lineObj;

                        // set node involvement
                        final T lineIdentifier = getIdentifier(packageName, className, 
                        		methodName, Integer.valueOf(line.getAttributeValue("number")));
                        final boolean involved = Long.parseLong(line.getAttributeValue("hits")) > 0;
                        trace.setInvolvement(lineIdentifier, involved);

                        // create hierarchical spectra
                        methodSpectra.setParent(methodIdentifier, lineIdentifier);
                    }
                }
            }
        }
		return true;
    }
    
}
