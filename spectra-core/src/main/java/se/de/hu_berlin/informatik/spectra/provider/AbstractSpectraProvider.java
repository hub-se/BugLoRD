/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.provider.loader.ICoverageDataLoader;

/**
 * Loads coverage data to {@link ISpectra} objects where each covered line is
 * represented by one node and each coverage data object represents one trace in
 * the resulting spectra.
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
public abstract class AbstractSpectraProvider<T, K extends ITrace<T>, D> implements ISpectraProvider<T, K> {

	protected final ISpectra<T, K> lineSpectra;
	private final boolean fullSpectra;

	/**
	 * Create a spectra provider that uses aggregation. That means that coverage
	 * data is added to the spectra at the point that it is added to the
	 * provider.
	 * @param lineSpectra
	 * a new spectra instance
	 * @param fullSpectra
	 * whether to add all nodes contained in the data or only the covered nodes
	 */
	public AbstractSpectraProvider(ISpectra<T, K> lineSpectra, boolean fullSpectra) {
		super();
		this.lineSpectra = lineSpectra;
		this.fullSpectra = fullSpectra;
	}

	protected abstract ICoverageDataLoader<T, K, D> getLoader();

	/**
	 * Adds coverage data to the provider.
	 * @param data
	 * a coverage data object
	 * @return true if successful; false otherwise
	 */
	public boolean addData(final D data) {
		return getLoader().loadSingleCoverageData(lineSpectra, data, fullSpectra);
	}

	@Override
	public ISpectra<T, ? super K> loadSpectra() throws IllegalStateException {
		return lineSpectra;
	}

}
