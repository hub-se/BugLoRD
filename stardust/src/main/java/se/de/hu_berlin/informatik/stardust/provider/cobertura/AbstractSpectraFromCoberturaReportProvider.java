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
import se.de.hu_berlin.informatik.stardust.provider.AbstractSpectraProvider;
import se.de.hu_berlin.informatik.stardust.provider.cobertura.coverage.LineWrapper;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;
import se.de.hu_berlin.informatik.stardust.spectra.HitSpectra;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Loads Cobertura reports to {@link HitSpectra} objects where each covered line is represented by one node and each file
 * represents one trace in the resulting spectra.
 * 
 * @author Simon
 *
 * @param <T>
 * the type of nodes in the spectra to provide
 */
public abstract class AbstractSpectraFromCoberturaReportProvider<T> extends AbstractSpectraProvider<T, ITrace<T>, CoberturaReportWrapper> {

	int traceCount = 0;

    public AbstractSpectraFromCoberturaReportProvider(CoberturaReportWrapper initialCoverageData) {
        this(initialCoverageData, false);
    }
    
    public AbstractSpectraFromCoberturaReportProvider(CoberturaReportWrapper initialCoverageData, boolean storeHits) {
        super(initialCoverageData, storeHits);
    }

	@Override
    public boolean loadSingleCoverageData(final CoberturaReportWrapper reportWrapper,
    		final boolean onlyAddNodes) {
		if (reportWrapper == null) {
    		return false;
    	}
		
        ITrace<T> trace = null;

        ProjectData projectData = reportWrapper.getReport().getProjectData();
        if (projectData == null) {
    		return false;
    	}
        
        if (!onlyAddNodes) {
        	if (reportWrapper.getIdentifier() == null) {
        		trace = lineSpectra.addTrace(
        				String.valueOf(++traceCount), 
        				reportWrapper.isSuccessful());	
        	} else {
        		trace = lineSpectra.addTrace(
        				reportWrapper.getIdentifier(), 
        				reportWrapper.isSuccessful());
        	}
        }
        
        if (onlyAddNodes) {
        	Log.out(this, "Populating spectra with given nodes...");
        }
        
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
					
					// create hierarchical spectra
					packageSpectra.setParent(packageName, sourceFilePath);
	                
	                // loop over all methods of the class
//	                SortedSet<String> sortedMethods = new TreeSet<>();
//	        		sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
	        		Iterator<String> itMethods = classData.getMethodNamesAndDescriptors().iterator();
	        		while (itMethods.hasNext()) {
	        			final String methodNameAndSig = itMethods.next();
//	        			String name = methodNameAndSig.substring(0, methodNameAndSig.indexOf('('));
//	        			String signature = methodNameAndSig.substring(methodNameAndSig.indexOf('('));
	        			
	                    final String methodIdentifier = String.format("%s:%s", actualClassName, methodNameAndSig);
	                    
	                    // create hierarchical spectra
	                    classSpectra.setParent(sourceFilePath, methodIdentifier);

	                    // loop over all lines of the method
//	                    SortedSet<CoverageData> sortedLines = new TreeSet<>();
//	            		sortedLines.addAll(classData.getLines(methodNameAndSig));
	            		Iterator<CoverageData> itLines = classData.getLines(methodNameAndSig).iterator();
	            		while (itLines.hasNext()) {
	            			LineWrapper lineData = new LineWrapper(itLines.next());
	            			
	            			// set node involvement
	                        final T lineIdentifier = getIdentifier(
	                        		packageName, sourceFilePath, methodNameAndSig, lineData.getLineNumber());
	                        
	                        if (onlyAddNodes) {
	                        	lineSpectra.getOrCreateNode(lineIdentifier);
	                        } else {
	                        	if (lineData.getHits() > 0) {
	                        		trace.setInvolvement(lineIdentifier, true);
	                        	}
	                        }

	                        // create hierarchical spectra
	                        methodSpectra.setParent(methodIdentifier, lineIdentifier);
	            		}
	        		}
				}
			}
		}
		return true;
    }
    
}
