/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.cobertura;

import java.util.Iterator;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.coverage.LineWrapper;
import se.de.hu_berlin.informatik.stardust.spectra.HierarchicalSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.IMutableTrace;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Loads Cobertura reports to {@link Spectra} objects where each covered line is represented by one node and each file
 * represents one trace in the resulting spectra.
 * 
 * @author Simon
 *
 * @param <T>
 * the type of nodes in the spectra to provide
 */
public abstract class AbstractSpectraFromCoberturaReportProvider<T> extends AbstractSpectraFromCoberturaProvider<T, CoberturaReportWrapper> {

    /**
     * Create a cobertura provider.
     */
    public AbstractSpectraFromCoberturaReportProvider() {
        this(true, false);
    }
    
    public AbstractSpectraFromCoberturaReportProvider(boolean usesAggregate, boolean storeHits) {
        super(usesAggregate, storeHits);
    }
    
    @Override
	public boolean addData(CoberturaReportWrapper reportWrapper) {
    	if (getInitialCoverageData() == null) {
    		addInitialCoverageData(reportWrapper);
    	}
    	
    	//uncomment this to NOT add traces that did not cover any lines...
//        if (!FileUtils.readFile2String(Paths.get(file)).matches(".*hits=\"[1-9].*")) {
//        	Log.warn(this, "Did not add file '%s' as it did not execute a single node.", file);
//            return;
//        }
        
        if (usesAggregate()) {
        	return this.loadSingleCoverageData(reportWrapper, getAggregateSpectra());
        } else {
        	getDataList().add(reportWrapper);
        }
        return true;
	}
    

	@Override
	public CoberturaReportWrapper getDataFromInitialPopulation() {
		if (getInitialCoverageData() == null) {
			return null;
		} else {
			return new CoberturaReportWrapper(null, getInitialCoverageData().getInitialProjectData(), null, false);
		}
	}

	@Override
    public boolean loadSingleCoverageData(final CoberturaReportWrapper reportWrapper, final ISpectra<T> lineSpectra,
            final HierarchicalSpectra<String, T> methodSpectra,
            final HierarchicalSpectra<String, String> classSpectra,
            final HierarchicalSpectra<String, String> packageSpectra,
            final boolean onlyAddInitialNodes) {
		if (reportWrapper == null) {
    		return false;
    	}
		
        IMutableTrace<T> trace = null;

        ProjectData projectData = null;
        if (onlyAddInitialNodes) {
        	projectData = getInitialCoverageData().getInitialProjectData();
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
    		return false;
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
	            			LineWrapper lineData = new LineWrapper(itLines.next());
	            			
	            			// set node involvement
	                        final T lineIdentifier = getIdentifier(
	                        		packageName, sourceFilePath, methodNameAndSig, lineData.getLineNumber());
	                        
	                        if (onlyAddInitialNodes) {
	                        	lineSpectra.getOrCreateNode(lineIdentifier);
	                        } else {
	                        	if (lineData.getHits() > 0) {
	                        		trace.setInvolvement(lineIdentifier, true);
	                        	}
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
		return true;
    }
    
}
