/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider.cobertura;

import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.stardust.provider.IHierarchicalSpectraProvider;
import se.de.hu_berlin.informatik.stardust.provider.ISpectraProvider;
import se.de.hu_berlin.informatik.stardust.spectra.HierarchicalSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;

/**
 * Loads Cobertura coverage data to {@link Spectra} objects where each covered line is represented by one node and each file
 * represents one trace in the resulting spectra.
 * 
 * @author Simon
 *
 * @param <T>
 * the type of nodes in the spectra to provide
 * @param <K>
 * the type of the coverage data that is used
 */
public abstract class AbstractSpectraFromCoberturaProvider<T, K> implements ISpectraProvider<T>, IHierarchicalSpectraProvider<String, String> {

    /** List of coverage data objects to load. */
    private final List<K> dataList = new ArrayList<>();
    
    public List<K> getDataList() {
		return dataList;
	}

	private K initialData = null;
    private boolean populated = false;
    
    private ISpectra<T> aggregateSpectra = null;

    public ISpectra<T> getAggregateSpectra() {
		return aggregateSpectra;
	}

	private boolean usesAggregate = false;
	private boolean storeHits = false;

    public boolean usesAggregate() {
		return usesAggregate;
	}
    
    public boolean storeHits() {
    	return storeHits;
    }

	/**
     * Create a cobertura provider that uses aggregation.
     */
    public AbstractSpectraFromCoberturaProvider() {
        this(true, false);
    }
    
    /**
     * Create a cobertura provider that may use aggregation.
     * That means that coverage data is loaded at the point that it is
     * added to the provider.
     * @param usesAggregate
     * whether aggregation shall be used
     * @param storeHits
     * whether to store the hit counts
     */
    public AbstractSpectraFromCoberturaProvider(boolean usesAggregate, boolean storeHits) {
        super();
        if (usesAggregate) {
        		aggregateSpectra = new Spectra<>();
        	this.usesAggregate = true;
        }
        this.storeHits = storeHits;
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
     * Populates the spectra with nodes extracted from the given coverage data.
     * @param coverageData
     * initial coverage data
     */
    public void addInitialCoverageData(final K coverageData) {
    	initialData = coverageData;
    }
    
	public K getInitialCoverageData() {
		return initialData;
	}
    
    @Override
    public ISpectra<T> loadSpectra() throws IllegalStateException {
    	//if aggregated spectra used, return it
    	if (usesAggregate) {
    		//populate with given initial project data (if any)
    		if (!this.populateSpectraNodes(aggregateSpectra)) {
    			throw new IllegalStateException("Could not load initial population. Providing spectra failed.");
    		}
    		return aggregateSpectra;
    	} else {
    		final ISpectra<T> spectra = new Spectra<>();
    		//populate with given initial project data (if any)
    		if (!this.populateSpectraNodes(aggregateSpectra)) {
    			throw new IllegalStateException("Could not load initial population. Providing spectra failed.");
    		}
    		//add all reports
    		for (final K report : this.dataList) {
    			if (!this.loadSingleCoverageData(report, spectra)) {
    				throw new IllegalStateException("Could not load coverage trace. Providing spectra failed.");
        		}
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
    public boolean loadSingleCoverageData(K coverageData, final ISpectra<T> spectra) {
        return this.loadSingleCoverageData(coverageData, spectra, null, null, null, false);
    }
    
    /**
     * Populates the given spectra with nodes extracted from the 
     * initial coverage data (if any).
     * @param spectra
     * the spectra to add the file to
     * @return
     * true if successful; false otherwise
     */
    private boolean populateSpectraNodes(final ISpectra<T> spectra) {
    	if (!populated) {
    		populated = true;
    		return this.loadSingleCoverageData(getDataFromInitialPopulation(), spectra, null, null, null, true);
    	}
		return true;
    }

    public abstract K getDataFromInitialPopulation();

	/**
     * Loads a single Cobertura coverage data object to the given spectra or only adds the nodes extracted
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
    public abstract boolean loadSingleCoverageData(final K coverageData, final ISpectra<T> lineSpectra,
            final HierarchicalSpectra<String, T> methodSpectra,
            final HierarchicalSpectra<String, String> classSpectra,
            final HierarchicalSpectra<String, String> packageSpectra,
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
    public HierarchicalSpectra<String, String> loadHierarchicalSpectra() throws Exception {
        // create spectras
        final ISpectra<T> lineSpectra = new Spectra<>();
        final HierarchicalSpectra<String, T> methodSpectra = new HierarchicalSpectra<>(lineSpectra);
        final HierarchicalSpectra<String, String> classSpectra = new HierarchicalSpectra<>(methodSpectra);
        final HierarchicalSpectra<String, String> packageSpectra = new HierarchicalSpectra<>(classSpectra);

        for (final K report : this.dataList) {
            this.loadSingleCoverageData(report, lineSpectra, 
            		methodSpectra, classSpectra, packageSpectra, false);
        }
        return packageSpectra;
    }

}
