/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.jacoco;

import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.stardust.provider.IHierarchicalSpectraProvider;
import se.de.hu_berlin.informatik.stardust.provider.IHitSpectraProvider;
import se.de.hu_berlin.informatik.stardust.spectra.HierarchicalHitSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.HitSpectra;

/**
 * Loads JaCoCo coverage data to {@link HitSpectra} objects where each covered line is represented by one node and each file
 * represents one trace in the resulting spectra.
 * 
 * @author Simon
 *
 * @param <T>
 * the type of nodes in the spectra to provide
 * @param <K>
 * the type of the coverage data that is used
 */
public abstract class AbstractSpectraFromJaCoCoProvider<T, K> implements IHitSpectraProvider<T>, IHierarchicalSpectraProvider<String, String> {

    /** List of coverage data objects to load. */
    private final List<K> dataList = new ArrayList<>();
    
    public List<K> getDataList() {
		return dataList;
	}

	private K lastData = null;
    private boolean populated = false;
    
    private HitSpectra<T> aggregateSpectra = null;

    public HitSpectra<T> getAggregateSpectra() {
		return aggregateSpectra;
	}

	private boolean usesAggregate = false;
	private boolean storeHits = false;
	final private boolean fullSpectra;
	

    public boolean usesAggregate() {
		return usesAggregate;
	}
    
    public boolean storeHits() {
    	return storeHits;
    }

	/**
     * Create a JaCoCo provider that uses aggregation.
     */
    public AbstractSpectraFromJaCoCoProvider() {
        this(true, false, false);
    }
    
    /**
     * Create a JaCoCo provider that may use aggregation.
     * That means that coverage data is loaded at the point that it is
     * added to the provider.
     * @param usesAggregate
     * whether aggregation shall be used
     * @param storeHits
     * whether to store the hit counts
     * @param fullSpectra
     * whether to store information about all executable lines or only about actually covered lines
     */
    public AbstractSpectraFromJaCoCoProvider(boolean usesAggregate, boolean storeHits, boolean fullSpectra) {
        super();
        if (usesAggregate) {
        		aggregateSpectra = new HitSpectra<>();
        	this.usesAggregate = true;
        }
        this.storeHits = storeHits;
        this.fullSpectra = fullSpectra;
    }

    /**
     * Adds coverage data to the provider.
     * @param data
     * a coverage data object
     * @return
     * true if not using aggregation; otherwise true if successful; false otherwise
     */
    public abstract boolean addData(final K data);
    
    /**
     * Store the last coverage data if the full spectra shall be created in the end.
     * @param coverageData
     * last coverage data
     */
    public void storeLastCoverageData(final K coverageData) {
    	if (fullSpectra) {
    		lastData = coverageData;
    	}
    }
    
	public K getLastCoverageData() {
		return lastData;
	}
    
    @Override
    public HitSpectra<T> loadHitSpectra() throws IllegalStateException {
    	//if aggregated spectra used, return it
    	if (usesAggregate) {
    		//populate with given initial project data (if any)
    		if (!this.populateSpectraNodes(aggregateSpectra)) {
    			throw new IllegalStateException("Could not load initial population. Providing spectra failed.");
    		}
    		return aggregateSpectra;
    	} else {
    		final HitSpectra<T> spectra = new HitSpectra<>();
    		//add all reports
    		for (final K report : this.dataList) {
    			if (!this.loadSingleCoverageData(report, spectra)) {
    				throw new IllegalStateException("Could not load coverage trace. Providing spectra failed.");
        		}
    		}
    		//populate with given initial project data (if any)
    		if (!this.populateSpectraNodes(aggregateSpectra)) {
    			throw new IllegalStateException("Could not load initial population. Providing spectra failed.");
    		}
    		return spectra;
    	}
    }

    /**
     * Loads a single coverage data object into the spectra.
     * @param coverageData
     * the coverage data object
     * @param spectra
     * the spectra to add the file to
     * @return
     * true if successful; false otherwise
     */
    public boolean loadSingleCoverageData(K coverageData, final HitSpectra<T> spectra) {
        return this.loadSingleCoverageData(coverageData, spectra, null, null, null, false);
    }
    
    /**
     * Populates the given spectra with nodes extracted from the 
     * last coverage data (if any).
     * @param spectra
     * the spectra to add the file to
     * @return
     * true if successful; false otherwise
     */
    private boolean populateSpectraNodes(final HitSpectra<T> spectra) {
    	if (!populated) {
    		populated = true;
    		if (fullSpectra) {
    			return this.loadSingleCoverageData(getLastCoverageData(), spectra, null, null, null, true);
    		}
    	}
		return true;
    }

	/**
     * Loads a single JaCoCo coverage data object to the given spectra or only adds the nodes extracted
     * from the data object if the respective parameter is set.
     * @param lineSpectra
     * the spectra to add the data to
     * @param methodSpectra
     * a method spectra (or null)
     * @param classSpectra
     * a class spectra (or null)
     * @param packageSpectra
     * a package spectra (or null)
     * @param coverageData
     * the coverage data object
     * @param onlyAddInitialNodes
     * whether to only add the nodes from the initial coverage data, if any (does not add a trace)
     * @return
     * true if successful; false otherwise
     */
    public abstract boolean loadSingleCoverageData(final K coverageData, final HitSpectra<T> lineSpectra,
            final HierarchicalHitSpectra<String, T> methodSpectra,
            final HierarchicalHitSpectra<String, String> classSpectra,
            final HierarchicalHitSpectra<String, String> packageSpectra,
            final boolean onlyAddInitialNodes);
    

    /**
     * Provides an identifier of type T, generated from the given parameters.
     * @param packageName
     * a package name
     * @param sourceFilePath
     * a source file path
     * @param methodNameAndSig
     * a method name and signature
     * @param lineNumber
     * a line number
     * @return
     * an identifier (object) of type T
     */
    public abstract T getIdentifier(String packageName, String sourceFilePath, String methodNameAndSig, int lineNumber);

	@Override
    public HierarchicalHitSpectra<String, String> loadHierarchicalSpectra() throws Exception {
        // create spectras
        final HitSpectra<T> lineSpectra = new HitSpectra<>();
        final HierarchicalHitSpectra<String, T> methodSpectra = new HierarchicalHitSpectra<>(lineSpectra);
        final HierarchicalHitSpectra<String, String> classSpectra = new HierarchicalHitSpectra<>(methodSpectra);
        final HierarchicalHitSpectra<String, String> packageSpectra = new HierarchicalHitSpectra<>(classSpectra);

        for (final K report : this.dataList) {
            this.loadSingleCoverageData(report, lineSpectra, 
            		methodSpectra, classSpectra, packageSpectra, false);
        }
        return packageSpectra;
    }

}
