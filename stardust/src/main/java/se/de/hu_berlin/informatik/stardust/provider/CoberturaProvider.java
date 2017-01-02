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
import java.util.Iterator;
import java.util.List;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageData;
import net.sourceforge.cobertura.coveragedata.LineData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import se.de.hu_berlin.informatik.stardust.localizer.SourceCodeBlock;
import se.de.hu_berlin.informatik.stardust.spectra.HierarchicalSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.IMutableTrace;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Loads Cobertura reports to {@link Spectra} objects where each covered line is represented by one node and each file
 * represents one trace in the resulting spectra.
 */
public class CoberturaProvider implements ISpectraProvider<SourceCodeBlock>, IHierarchicalSpectraProvider<String, String> {

    /** List of Cobertura reports to load. */
    private final List<ReportWrapper> reports = new ArrayList<>();
    private ProjectData initialProjectData = null;
    private boolean populated = false;
    
    private Spectra<SourceCodeBlock> aggregateSpectra = null;

    private boolean usesAggregate = false;
    
    /**
     * Create a cobertura provider.
     */
    public CoberturaProvider() {
        this(true);
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
     * @param reportWrapper
     *            a Cobertura coverage report wrapper
     */
    public void addReport(final ReportWrapper reportWrapper) {
    	if (this.initialProjectData == null) {
    		addInitialNodePopulation(reportWrapper.getInitialProjectData());
    	}
    	
    	//uncomment this to NOT add traces that did not cover any lines...
//        if (!FileUtils.readFile2String(Paths.get(file)).matches(".*hits=\"[1-9].*")) {
//        	Log.warn(this, "Did not add file '%s' as it did not execute a single node.", file);
//            return;
//        }
        
        if (usesAggregate) {
        	this.loadSingleTrace(reportWrapper, aggregateSpectra);
        } else {
        	this.reports.add(reportWrapper);
        }
    }
    
    /**
     * Populates the spectra with nodes extracted from the given project data.
     * @param projectData
     *            a Cobertura project data container
     */
    public void addInitialNodePopulation(final ProjectData projectData) {
    	this.initialProjectData = projectData;
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
    	//if aggregated spectra used, return it
    	if (usesAggregate) {
    		//populate with given initial project data (if any)
    		this.populateSpectraNodes(aggregateSpectra);
    		return aggregateSpectra;
    	} else {
    		final Spectra<SourceCodeBlock> spectra = new Spectra<>();
    		//populate with given initial project data (if any)
    		this.populateSpectraNodes(spectra);
    		//add all reports
    		for (final ReportWrapper report : this.reports) {
    			this.loadSingleTrace(report, spectra);
    		}
    		return spectra;
    	}
    }

    /**
     * Loads a single Cobertura report to the given spectra as line spectra.
     * @param reportWrapper
     * the Cobertura report wrapper
     * @param spectra
     * the spectra to add the file to
     */
    private void loadSingleTrace(ReportWrapper reportWrapper, final Spectra<SourceCodeBlock> spectra) {
        this.loadSingleTrace(reportWrapper, spectra, null, null, null, false);
    }
    
    /**
     * Populates the given spectra with node extracted from the 
     * initial project data.
     * @param spectra
     * the spectra to add the file to
     */
    private void populateSpectraNodes(final Spectra<SourceCodeBlock> spectra) {
    	if (!populated) {
    		this.loadSingleTrace(new ReportWrapper(null, this.initialProjectData, null, false), spectra, null, null, null, true);
    		populated = true;
    	}
    }

    /**
     * Loads a single Cobertura report to the given spectra or only adds the nodes extracted
     * from the project data contained in the given wrapper.
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
     * @param onlyAddInitialNodes
     * whether to only add the initial nodes from the initial project data (does not add a trace)
     */
    private void loadSingleTrace(final ReportWrapper reportWrapper, final Spectra<SourceCodeBlock> lineSpectra,
            final HierarchicalSpectra<String, SourceCodeBlock> methodSpectra,
            final HierarchicalSpectra<String, String> classSpectra,
            final HierarchicalSpectra<String, String> packageSpectra,
            final boolean onlyAddInitialNodes) {
    	
        IMutableTrace<SourceCodeBlock> trace = null;

        ProjectData projectData = null;
        if (onlyAddInitialNodes) {
        	projectData = reportWrapper.getInitialProjectData();
        } else {
        	projectData = reportWrapper.getReport().getProjectData();
        	if (reportWrapper.getIdentifier() == null) {
            	trace = lineSpectra.addTrace(
            			"_", 
            			reportWrapper.isSuccessful());	
            } else {
            	trace = lineSpectra.addTrace(
            			reportWrapper.getIdentifier(), 
            			reportWrapper.isSuccessful());
            }
        }
        
        if (projectData == null) {
    		return;
    	}
        
        if (onlyAddInitialNodes) {
        	Log.out(this, "Populating spectra with initial set of nodes...");
        }
        
        final boolean createHierarchicalSpectra = methodSpectra != null && classSpectra != null
                && packageSpectra != null;
        
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
					final String sourceFilePath = classData.getSourceFileName();
					
					// if necessary, create hierarchical spectra
	                if (createHierarchicalSpectra) {
	                    packageSpectra.setParent(packageName, sourceFilePath);
	                }
	                
	                // loop over all methods of the class
//	                SortedSet<String> sortedMethods = new TreeSet<>();
//	        		sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
	        		Iterator<String> itMethods = classData.getMethodNamesAndDescriptors().iterator();
	        		while (itMethods.hasNext()) {
	        			final String methodNameAndSig = itMethods.next();
//	        			String name = methodNameAndSig.substring(0, methodNameAndSig.indexOf('('));
//	        			String signature = methodNameAndSig.substring(methodNameAndSig.indexOf('('));
	        			
	                    final String methodIdentifier = String.format("%s:%s", actualClassName, methodNameAndSig);
	                    
	                    // if necessary, create hierarchical spectra
	                    if (createHierarchicalSpectra) {
	                        classSpectra.setParent(sourceFilePath, methodIdentifier);
	                    }

	                    // loop over all lines of the method
//	                    SortedSet<CoverageData> sortedLines = new TreeSet<>();
//	            		sortedLines.addAll(classData.getLines(methodNameAndSig));
	            		Iterator<CoverageData> itLines = classData.getLines(methodNameAndSig).iterator();
	            		while (itLines.hasNext()) {
	            			LineData lineData = (LineData) itLines.next();
	            			
	            			// set node involvement
	                        final SourceCodeBlock lineIdentifier = new SourceCodeBlock(
	                        		packageName, sourceFilePath, methodNameAndSig, lineData.getLineNumber());
	                        
	                        if (onlyAddInitialNodes) {
	                        	lineSpectra.getOrCreateNode(lineIdentifier);
	                        } else {
	                        	final boolean involved = lineData.getHits() > 0;
	                        	trace.setInvolvement(lineIdentifier, involved);
	                        }

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
            		methodSpectra, classSpectra, packageSpectra, false);
        }
        return packageSpectra;
    }
}
