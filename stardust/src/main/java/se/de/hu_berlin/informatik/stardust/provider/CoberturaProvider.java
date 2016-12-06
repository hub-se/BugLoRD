/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.HierarchicalSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.IMutableTrace;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;
import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;

/**
 * Loads cobertura.xml files to {@link Spectra} objects where each covered line is represented by one node and each file
 * represents one trace in the resulting spectra.
 */
public class CoberturaProvider implements ISpectraProvider<SourceCodeBlock>, IHierarchicalSpectraProvider<String, String> {

    /** List of trace files to load. */
    private final List<CoverageWrapper> files = new ArrayList<>();
    
    private Spectra<SourceCodeBlock> aggregateSpectra = null;

    private boolean usesAggregate = false;
    
    /**
     * Create a cobertura provider.
     */
    public CoberturaProvider() {
        this(false);
    }
    
    /**
     * Create a cobertura provider that may use aggregation.
     * That means that trace files are loaded at the point that they
     * are added to the provider.
     * @param usesAggregate
     * whether aggregation shall be used
     */
    public CoberturaProvider(boolean usesAggregate) {
        super();
        if (usesAggregate) {
        	aggregateSpectra = new Spectra<>();
        	this.usesAggregate = true;
        }
    }

    /**
     * Adds a trace file to the provider.
     *
     * @param file
     *            path to a cobertura xml file
     * @param traceIdentifier
     * the identifier of the trace (usually the test case name)
     * @param successful
     *            true if the trace file contains a successful trace;
     *            false if the trace file contains a failing trace
     * @throws IOException
     * 			  in case the xml file cannot be loaded
     * @throws JDOMException 
     * 			  in case the xml file cannot be loaded
     */
    public void addTraceFile(final String file, final String traceIdentifier, 
    		final boolean successful) throws IOException, JDOMException {
    	//uncomment this to not add traces that did not cover any lines...
//        if (!FileUtils.readFile2String(Paths.get(file)).matches(".*hits=\"[1-9].*")) {
//        	Log.warn(this, "Did not add file '%s' as it did not execute a single node.", file);
//            return;
//        }
        this.files.add(new CoverageWrapper(new File(file), traceIdentifier, successful));
        
        if (usesAggregate) {
        	this.loadSingleTrace(new CoverageWrapper(new File(file), traceIdentifier, successful), 
        			aggregateSpectra);
        }
    }
    
//    private String fileToString(final String filename) throws IOException {
//        final BufferedReader reader = new BufferedReader(new FileReader(filename));
//        final StringBuilder builder = new StringBuilder();
//        String line;
//
//        // For every line in the file, append it to the string builder
//        while ((line = reader.readLine()) != null) {
//            builder.append(line);
//        }
//        reader.close();
//        return builder.toString();
//    }

    @Override
    public ISpectra<SourceCodeBlock> loadSpectra() throws Exception {
    	if (usesAggregate) {
    		return aggregateSpectra;
    	}
        final Spectra<SourceCodeBlock> spectra = new Spectra<>();
        for (final CoverageWrapper traceFile : this.files) {
            this.loadSingleTrace(traceFile, spectra);
        }
        return spectra;
    }

    /**
     * Loads a single trace file to the given spectra as line spectra.
     * @param traceFile
     * the trace file wrapper
     * @param spectra
     * the spectra to add the file to
     * @throws JDOMException
     *             in case the xml file cannot be loaded
     * @throws IOException
     *             in case the xml file cannot be loaded
     */
    private void loadSingleTrace(CoverageWrapper traceFile, final Spectra<SourceCodeBlock> spectra)
            throws JDOMException, IOException {
        this.loadSingleTrace(traceFile, spectra, null, null, null);
    }

    /**
     * Loads a single trace file to the given spectra.
     * @param lineSpectra
     * the spectra to add the trace file to
     * @param methodSpectra
     * a method spectra (or null)
     * @param classSpectra
     * a class spectra (or null)
     * @param packageSpectra
     * a package spectra (or null)
     * @param traceFile
     * the trace file wrapper
     * @throws JDOMException
     *             in case the xml file cannot be loaded
     * @throws IOException
     *             in case the xml file cannot be loaded
     */
    private void loadSingleTrace(final CoverageWrapper traceFile, final Spectra<SourceCodeBlock> lineSpectra,
            final HierarchicalSpectra<String, SourceCodeBlock> methodSpectra,
            final HierarchicalSpectra<String, String> classSpectra,
            final HierarchicalSpectra<String, String> packageSpectra) throws JDOMException, IOException {
    	//ignore coverage dtd file (unnecessary http requests, possibly failing if server is down...)
    	String fileWithoutDTD = new String(Files.readAllBytes(traceFile.getXmlCoverageFile().toPath()));
    	int pos = fileWithoutDTD.indexOf("<!DOCTYPE coverage");
    	if (pos != -1) {
    		int pos2 = fileWithoutDTD.indexOf(">", pos);
    		if (pos2 != -1) {
    			fileWithoutDTD = fileWithoutDTD.substring(0, pos) + fileWithoutDTD.substring(pos2+1);
    		}
    	}
    	
        final IMutableTrace<SourceCodeBlock> trace;
        if (traceFile.getIdentifier() == null) {
        	trace = lineSpectra.addTrace(
        			FileUtils.getFileNameWithoutExtension(traceFile.getXmlCoverageFile().toString()), 
        			traceFile.isSuccessful());	
        } else {
        	trace = lineSpectra.addTrace(
        			traceFile.getIdentifier(), 
        			traceFile.isSuccessful());
        }
        
        final Document doc = new SAXBuilder().build(new StringReader(fileWithoutDTD));
        final boolean createHierarchicalSpectra = methodSpectra != null && classSpectra != null
                && packageSpectra != null;

        // loop over all packages of the trace file
        for (final Object pckgObj : doc.getRootElement().getChild("packages").getChildren()) {
            final Element pckg = (Element) pckgObj;
            final String packageName = pckg.getAttributeValue("name");

            // loop over all classes of the package
            for (final Object clssObj : pckg.getChild("classes").getChildren()) {
                final Element clss = (Element) clssObj;
                final String className = clss.getAttributeValue("filename");

                // if necessary, create hierarchical spectra
                if (createHierarchicalSpectra) {
                    packageSpectra.setParent(packageName, className);
                }

                // loop over all methods of the class
                for (final Object mthdObj : clss.getChild("methods").getChildren()) {
                    final Element method = (Element) mthdObj;
                    final String methodName = method.getAttributeValue("name") + method.getAttributeValue("signature");
                    final String methodIdentifier = String.format("%s:%s", className, methodName);

                    // if necessary, create hierarchical spectra
                    if (createHierarchicalSpectra) {
                        classSpectra.setParent(className, methodIdentifier);
                    }

                    // loop over all lines of the method
                    for (final Object lineObj : method.getChild("lines").getChildren()) {
                        final Element line = (Element) lineObj;

                        // set node involvement
                        final SourceCodeBlock lineIdentifier = new SourceCodeBlock(
                        		packageName, className, methodName, Integer.valueOf(line.getAttributeValue("number")));
                        final boolean involved = Integer.parseInt(line.getAttributeValue("hits")) > 0;
                        trace.setInvolvement(lineIdentifier, involved);

                        // if necessary, create hierarchical spectra
                        if (createHierarchicalSpectra) {
                            methodSpectra.setParent(methodIdentifier, lineIdentifier);
                        }
                    }
                }
            }
        }
    }

//    /**
//     * Creates a node identifier using the given classname and line number
//     *
//     * @param className
//     *            class name of node
//     * @param lineNumber
//     *            line number of node
//     * @return node identifier
//     */
//    public static String createNodeIdentifier(final String className, final int lineNumber) {
//        return createNodeIdentifier(className, String.valueOf(lineNumber));
//    }
//
//    /**
//     * Creates a node identifier using the given classname and line number
//     *
//     * @param className
//     *            class name of node
//     * @param lineNumber
//     *            line number of node
//     * @return node identifier
//     */
//    private static String createNodeIdentifier(final String className, final String lineNumber) {
//        return String.format("%s:%s", className, lineNumber);
//    }

    @Override
    public HierarchicalSpectra<String, String> loadHierarchicalSpectra() throws Exception {
        // create spectras
        final Spectra<SourceCodeBlock> lineSpectra = new Spectra<>();
        final HierarchicalSpectra<String, SourceCodeBlock> methodSpectra = new HierarchicalSpectra<>(lineSpectra);
        final HierarchicalSpectra<String, String> classSpectra = new HierarchicalSpectra<>(methodSpectra);
        final HierarchicalSpectra<String, String> packageSpectra = new HierarchicalSpectra<>(classSpectra);

        for (final CoverageWrapper traceFile : this.files) {
            this.loadSingleTrace(traceFile, lineSpectra, 
            		methodSpectra, classSpectra, packageSpectra);
        }
        return packageSpectra;
    }
}
