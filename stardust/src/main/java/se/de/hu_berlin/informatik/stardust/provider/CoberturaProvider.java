/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageData;
import net.sourceforge.cobertura.coveragedata.LineData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import net.sourceforge.cobertura.reporting.NativeReport;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.HierarchicalSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.IMutableTrace;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;

/**
 * Loads Cobertura reports to {@link Spectra} objects where each covered line is represented by one node and each file
 * represents one trace in the resulting spectra.
 */
public class CoberturaProvider implements ISpectraProvider<SourceCodeBlock>, IHierarchicalSpectraProvider<String, String> {

    /** List of Cobertura reports to load. */
    private final List<ReportWrapper> reports = new ArrayList<>();
    
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
     * @param report
     *            a Cobertura coverage report
     * @param traceIdentifier
     * the identifier of the trace (usually the test case name)
     * @param successful
     *            true if the trace file contains a successful trace;
     *            false if the trace file contains a failing trace
     */
    public void addReport(final NativeReport report, final String traceIdentifier, 
    		final boolean successful) {
    	//uncomment this to not add traces that did not cover any lines...
//        if (!FileUtils.readFile2String(Paths.get(file)).matches(".*hits=\"[1-9].*")) {
//        	Log.warn(this, "Did not add file '%s' as it did not execute a single node.", file);
//            return;
//        }
        this.reports.add(new ReportWrapper(report, traceIdentifier, successful));
        
        if (usesAggregate) {
        	this.loadSingleTrace(new ReportWrapper(report, traceIdentifier, successful), 
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
    public ISpectra<SourceCodeBlock> loadSpectra() {
    	if (usesAggregate) {
    		return aggregateSpectra;
    	}
        final Spectra<SourceCodeBlock> spectra = new Spectra<>();
        for (final ReportWrapper report : this.reports) {
            this.loadSingleTrace(report, spectra);
        }
        return spectra;
    }

    /**
     * Loads a single Cobertura report to the given spectra as line spectra.
     * @param reportWrapper
     * the Cobertura report wrapper
     * @param spectra
     * the spectra to add the file to
     */
    private void loadSingleTrace(ReportWrapper reportWrapper, final Spectra<SourceCodeBlock> spectra) {
        this.loadSingleTrace(reportWrapper, spectra, null, null, null);
    }

    /**
     * Loads a single Cobertura report to the given spectra.
     * @param lineSpectra
     * the spectra to add the trace file to
     * @param methodSpectra
     * a method spectra (or null)
     * @param classSpectra
     * a class spectra (or null)
     * @param packageSpectra
     * a package spectra (or null)
     * @param reportWrapper
     * the Cobertura report wrapper
     */
    private void loadSingleTrace(final ReportWrapper reportWrapper, final Spectra<SourceCodeBlock> lineSpectra,
            final HierarchicalSpectra<String, SourceCodeBlock> methodSpectra,
            final HierarchicalSpectra<String, String> classSpectra,
            final HierarchicalSpectra<String, String> packageSpectra) {
    	
        final IMutableTrace<SourceCodeBlock> trace;
        if (reportWrapper.getIdentifier() == null) {
        	trace = lineSpectra.addTrace(
        			"_", 
        			reportWrapper.isSuccessful());	
        } else {
        	trace = lineSpectra.addTrace(
        			reportWrapper.getIdentifier(), 
        			reportWrapper.isSuccessful());
        }
        
        final boolean createHierarchicalSpectra = methodSpectra != null && classSpectra != null
                && packageSpectra != null;

        ProjectData projectData = reportWrapper.getReport().getProjectData();
        
        // loop over all packages
        @SuppressWarnings("unchecked")
		Iterator<PackageData> itPackages = projectData.getPackages().iterator();
		while (itPackages.hasNext()) {
			PackageData packageData = itPackages.next();
			final String packageName = packageData.getName();
			
			// loop over all classes of the package
			@SuppressWarnings("unchecked")
			Iterator<SourceFileData> itSourceFiles = packageData.getSourceFiles().iterator();
			while (itSourceFiles.hasNext()) {
				@SuppressWarnings("unchecked")
				Iterator<ClassData> itClasses = itSourceFiles.next().getClasses().iterator();
				while (itClasses.hasNext()) {
					ClassData classData = itClasses.next();
					//TODO: use actual class name!?
					final String actualClassName = classData.getName();
					final String className = classData.getSourceFileName();
					
					// if necessary, create hierarchical spectra
	                if (createHierarchicalSpectra) {
	                    packageSpectra.setParent(packageName, className);
	                }
	                
	                // loop over all methods of the class
	                SortedSet<String> sortedMethods = new TreeSet<>();
	        		sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
	        		Iterator<String> itMethods = sortedMethods.iterator();
	        		while (itMethods.hasNext()) {
	        			final String methodNameAndSig = itMethods.next();
//	        			String name = methodNameAndSig.substring(0, methodNameAndSig.indexOf('('));
//	        			String signature = methodNameAndSig.substring(methodNameAndSig.indexOf('('));
	        			
	                    final String methodIdentifier = String.format("%s:%s", className, methodNameAndSig);
	                    
	                    // if necessary, create hierarchical spectra
	                    if (createHierarchicalSpectra) {
	                        classSpectra.setParent(className, methodIdentifier);
	                    }

	                    // loop over all lines of the method
	                    Collection<CoverageData> lines = classData.getLines(methodNameAndSig);
	                    SortedSet<CoverageData> sortedLines = new TreeSet<>();
	            		sortedLines.addAll(lines);
	            		Iterator<CoverageData> itLines = sortedLines.iterator();
	            		while (itLines.hasNext()) {
	            			LineData lineData = (LineData) itLines.next();
	            			
	            			// set node involvement
	                        final SourceCodeBlock lineIdentifier = new SourceCodeBlock(
	                        		packageName, className, methodNameAndSig, lineData.getLineNumber());
	                        final boolean involved = lineData.getHits() > 0;
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

        for (final ReportWrapper report : this.reports) {
            this.loadSingleTrace(report, lineSpectra, 
            		methodSpectra, classSpectra, packageSpectra);
        }
        return packageSpectra;
    }
}
