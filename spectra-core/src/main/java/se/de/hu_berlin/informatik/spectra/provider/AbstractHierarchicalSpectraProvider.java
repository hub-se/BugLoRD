/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.provider;

import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.hit.HierarchicalHitSpectra;
import se.de.hu_berlin.informatik.spectra.provider.IHierarchicalSpectraProvider;

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
public abstract class AbstractHierarchicalSpectraProvider<T, K extends ITrace<T>, D>
		extends AbstractSpectraProvider<T, K, D> implements IHierarchicalSpectraProvider<String, String> {

	final protected HierarchicalHitSpectra<String, T> methodSpectra;
	final protected HierarchicalHitSpectra<String, String> classSpectra;
	final protected HierarchicalHitSpectra<String, String> packageSpectra;

	/**
	 * Create a spectra provider that uses aggregation. That means that coverage
	 * data is added to the spectra at the point that it is added to the
	 * provider.
	 * @param lineSpectra
	 * a new spectra instance
	 * @param fullSpectra
	 * whether to add all nodes contained in the data or only the covered nodes
	 */
	public AbstractHierarchicalSpectraProvider(ISpectra<T, K> lineSpectra, boolean fullSpectra) {
		super(lineSpectra, fullSpectra);
		methodSpectra = new HierarchicalHitSpectra<>(lineSpectra, null);
		classSpectra = new HierarchicalHitSpectra<>(methodSpectra, null);
		packageSpectra = new HierarchicalHitSpectra<>(classSpectra, null);
	}

	@Override
	public HierarchicalHitSpectra<String, String> loadHierarchicalSpectra() {
		return packageSpectra;
	}

}
