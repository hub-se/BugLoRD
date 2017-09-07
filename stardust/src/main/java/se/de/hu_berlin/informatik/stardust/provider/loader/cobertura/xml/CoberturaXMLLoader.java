/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.loader.cobertura.xml;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.xml.CoberturaCoverageWrapper;
import se.de.hu_berlin.informatik.stardust.provider.loader.ICoverageDataLoader;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public abstract class CoberturaXMLLoader<T, K extends ITrace<T>>
		implements ICoverageDataLoader<T, K, CoberturaCoverageWrapper> {

	@Override
	public boolean loadSingleCoverageData(ISpectra<T, K> lineSpectra, final CoberturaCoverageWrapper traceFile,
			final boolean fullSpectra) {
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

			onNewPackage(packageName);
			
            // loop over all classes of the package
            for (final Object clssObj : pckg.getChild("classes").getChildren()) {
                final Element clss = (Element) clssObj;
                final String sourceFilePath = clss.getAttributeValue("filename");

                onNewClass(packageName, sourceFilePath);

                // loop over all methods of the class
                for (final Object mthdObj : clss.getChild("methods").getChildren()) {
                    final Element method = (Element) mthdObj;
                    final String methodName = method.getAttributeValue("name") + method.getAttributeValue("signature");
                    final String methodIdentifier = String.format("%s:%s", sourceFilePath, methodName);

                    onNewMethod(packageName, sourceFilePath, methodIdentifier);

                    // loop over all lines of the method
                    for (final Object lineObj : method.getChild("lines").getChildren()) {
                        final Element line = (Element) lineObj;

                        // set node involvement
                        final T lineIdentifier = getIdentifier(packageName, sourceFilePath, 
                        		methodName, Integer.valueOf(line.getAttributeValue("number")));
                        final boolean involved = Long.parseLong(line.getAttributeValue("hits")) > 0;
                        
                        if (involved) {
							trace.setInvolvement(lineIdentifier, true);
							onNewLine(packageName, sourceFilePath, methodIdentifier, lineIdentifier);
						} else if (fullSpectra) {
							lineSpectra.getOrCreateNode(lineIdentifier);
							onNewLine(packageName, sourceFilePath, methodIdentifier, lineIdentifier);
						}
                        
                    }
                }
            }
        }
		return true;
	}

	protected void onNewPackage(String packageName) {
		// nothing to do
	}

	protected void onNewClass(String packageName, String classFilePath) {
		// nothing to do
	}

	protected void onNewMethod(String packageName, String classFilePath, String methodName) {
		// nothing to do
	}

	protected void onNewLine(String packageName, String classFilePath, String methodName, T lineIdentifier) {
		// nothing to do
	}

}
