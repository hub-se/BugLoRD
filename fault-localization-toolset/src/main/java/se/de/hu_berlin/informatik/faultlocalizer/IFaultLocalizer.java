/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer;

import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.ILocalizerCache;
import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;

/**
 * Interface used to implement fault localization algorithms.
 * 
 * @param <T>
 * type used to identify nodes in the system
 */
public interface IFaultLocalizer<T> {

	/**
	 * Returns a human-understandable name of this fault localizer.
	 * 
	 * @return name
	 */
	String getName();

	/**
	 * Creates a fault location ranking for all nodes in the given spectra.
	 * 
	 * @param spectra
	 * the spectra to perform the fault localization on
	 * @return nodes ranked by suspiciousness of actually causing the failure
	 */
	default Ranking<INode<T>> localize(ISpectra<T,?> spectra) {
		return localize(spectra, ComputationStrategies.STANDARD_SBFL);
	}

	/**
	 * Creates a fault location ranking for all nodes in the given spectra.
	 * 
	 * @param spectra
	 * the spectra to perform the fault localization on
	 * @param strategy
	 * the strategy to use for computation
	 * @return nodes ranked by suspiciousness of actually causing the failure
	 */
	default Ranking<INode<T>> localize(ISpectra<T,?> spectra, ComputationStrategies strategy) {
		return localize(spectra.getLocalizer(), strategy);
	}
	
	/**
	 * Creates a fault location ranking for all nodes in the given spectra.
	 * 
	 * @param localizer
	 * the localizer
	 * @return nodes ranked by suspiciousness of actually causing the failure
	 */
	default Ranking<INode<T>> localize(ILocalizerCache<T> localizer) {
		return localize(localizer, ComputationStrategies.STANDARD_SBFL);
	}

	/**
	 * Creates a fault location ranking for all nodes in the given spectra.
	 * 
	 * @param localizer
	 * the localizer
	 * @param strategy
	 * the strategy to use for computation
	 * @return nodes ranked by suspiciousness of actually causing the failure
	 */
	Ranking<INode<T>> localize(ILocalizerCache<T> localizer, ComputationStrategies strategy);
	
	/**
	 * Computes the suspiciousness of a single node.
	 *
	 * @param node
	 * the node to compute the suspiciousness of
	 * @return the suspiciousness of the node
	 */
	default public double suspiciousness(INode<T> node) {
		return suspiciousness(node, ComputationStrategies.STANDARD_SBFL);
	}

	/**
	 * Computes the suspiciousness of a single node.
	 *
	 * @param node
	 * the node to compute the suspiciousness of
	 * @param strategy
	 * the strategy to use for computation
	 * @return the suspiciousness of the node
	 */
	public double suspiciousness(INode<T> node, ComputationStrategies strategy);
	
}
