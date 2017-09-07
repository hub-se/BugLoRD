/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.provider;

import se.de.hu_berlin.informatik.stardust.provider.IHierarchicalSpectraProvider;
import se.de.hu_berlin.informatik.stardust.spectra.CountSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.HierarchicalHitSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.HitSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.ITrace;

/**
 * Loads coverage data to {@link ISpectra} objects where each covered line is represented by one node and each coverage data object
 * represents one trace in the resulting spectra.
 * 
 * @author Simon
 *
 * @param <T>
 * the type of nodes in the spectra to provide
 * @param <K>
 * the type of traces in the specra
 * @param <D>
 * the type of the coverage data that is used
 */
public abstract class AbstractSpectraProvider<T, K extends ITrace<T>, D> implements ISpectraProvider<T, K>, IHierarchicalSpectraProvider<String, String> {

	final protected HierarchicalHitSpectra<String, T> methodSpectra;
	final protected HierarchicalHitSpectra<String, String> classSpectra;
	final protected HierarchicalHitSpectra<String, String> packageSpectra;

	final protected ISpectra<T, K> lineSpectra;

	/**
	 * Create a spectra provider that uses aggregation.
	 * That means that coverage data is added to the spectra at the point that it is
	 * added to the provider.
	 * @param initialCoverageData
	 * coverage data which should contain all existing program elements;
	 * may be null if no initial data should be added
	 */
	public AbstractSpectraProvider(D initialCoverageData) {
		this(initialCoverageData, false);
	}

	/**
	 * Create a spectra provider that uses aggregation.
	 * That means that coverage data is added to the spectra at the point that it is
	 * added to the provider.
	 * @param initialCoverageData
	 * coverage data which should contain all existing program elements;
	 * may be null if no initial data should be added
	 * @param storeHits
	 * whether to store the hit counts
	 */
	@SuppressWarnings("unchecked")
	public AbstractSpectraProvider(D initialCoverageData, boolean storeHits) {
		super();
		if (storeHits) {
			lineSpectra = (ISpectra<T, K>) new CountSpectra<T>();
		}	else {
			lineSpectra = (ISpectra<T, K>) new HitSpectra<T>();
		}
		
		methodSpectra = new HierarchicalHitSpectra<>(lineSpectra);
		classSpectra = new HierarchicalHitSpectra<>(methodSpectra);
		packageSpectra = new HierarchicalHitSpectra<>(classSpectra);

		if (initialCoverageData != null) {
			loadSingleCoverageData(initialCoverageData, true);
		}
	}

	/**
	 * Adds coverage data to the provider.
	 * @param data
	 * a coverage data object
	 * @return
	 * true if successful; false otherwise
	 */
	public boolean addData(final D data) {
		return loadSingleCoverageData(data, false);
	}

	@Override
	public ISpectra<T,K> loadSpectra() throws IllegalStateException {
		return lineSpectra;
	}

	@Override
	public HierarchicalHitSpectra<String, String> loadHierarchicalSpectra() throws Exception {
		return packageSpectra;
	}

	/**
	 * Loads a single Cobertura coverage data object to the given spectra or only adds the nodes extracted
	 * from the data object if the respective parameter is set.
	 * @param coverageData
	 * the coverage data object
	 * @param onlyAddNodes
	 * whether to only add the nodes from the coverage data (does not add a trace)
	 * @return
	 * true if successful; false otherwise
	 */
	protected abstract boolean loadSingleCoverageData(final D coverageData, final boolean onlyAddNodes);


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
	protected abstract T getIdentifier(String packageName, String sourceFilePath, String methodNameAndSig, int lineNumber);

}
