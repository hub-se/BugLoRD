/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer.sbfl;

import se.de.hu_berlin.informatik.stardust.localizer.IFaultLocalizer;
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer.ComputationStrategies;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;

/**
 * @param <T>
 * type used to identify nodes in the system
 */
public interface ILocalizer<T> {

	/**
	 * Creates a fault location ranking for all nodes with the given localizer.
	 * 
	 * @param localizer
	 * the localizer to use
	 * @return nodes ranked by suspiciousness of actually causing the failure
	 */
	default Ranking<INode<T>> localize(IFaultLocalizer<T> localizer) {
		return localize(localizer, ComputationStrategies.STANDARD_SBFL);
	}

	/**
	 * Creates a fault location ranking for all nodes with the given localizer.
	 * 
	 * @param localizer
	 * the localizer to use
	 * @param strategy
	 * the strategy to use for computation
	 * @return nodes ranked by suspiciousness of actually causing the failure
	 */
	Ranking<INode<T>> localize(IFaultLocalizer<T> localizer, ComputationStrategies strategy);
	
	/**
	 * Returns the amount of traces the node was not involved in, but passed.
	 * @param node
	 * the node
	 * @param strategy
	 * the strategy to use for computation
	 * @return amount of traces in spectra
	 */
	public double getNP(INode<T> node, ComputationStrategies strategy);

	/**
	 * Returns the amount of traces the node was not involved in and failed.
	 * @param node
	 * the node
	 * @param strategy
	 * the strategy to use for computation
	 * @return amount of traces in spectra
	 */
	public double getNF(INode<T> node, ComputationStrategies strategy);

	/**
	 * Returns the amount of traces where the node was executed and which
	 * passed.
	 * @param node
	 * the node
	 * @param strategy
	 * the strategy to use for computation
	 * @return amount of traces in spectra
	 */
	public double getEP(INode<T> node, ComputationStrategies strategy);

	/**
	 * Returns the amount of traces where the node was executed and which
	 * failed.
	 * @param node
	 * the node
	 * @param strategy
	 * the strategy to use for computation
	 * @return amount of traces in spectra
	 */
	public double getEF(INode<T> node, ComputationStrategies strategy);

	/**
	 * Returns the amount of traces the node was not involved in, but passed.
	 * @param node
	 * the node
	 * @return amount of traces in spectra
	 */
	default public double getNP(INode<T> node) {
		return getNP(node, ComputationStrategies.STANDARD_SBFL);
	}

	/**
	 * Returns the amount of traces the node was not involved in and failed.
	 * @param node
	 * the node
	 * @return amount of traces in spectra
	 */
	default public double getNF(INode<T> node) {
		return getNF(node, ComputationStrategies.STANDARD_SBFL);
	}

	/**
	 * Returns the amount of traces where the node was executed and which
	 * passed.
	 * @param node
	 * the node
	 * @return amount of traces in spectra
	 */
	default public double getEP(INode<T> node) {
		return getEP(node, ComputationStrategies.STANDARD_SBFL);
	}

	/**
	 * Returns the amount of traces where the node was executed and which
	 * failed.
	 * @param node
	 * the node
	 * @return amount of traces in spectra
	 */
	default public double getEF(INode<T> node) {
		return getEF(node, ComputationStrategies.STANDARD_SBFL);
	}

	/**
	 * Invalidates any cached values that may have been stored.
	 */
	public void invalidateCachedValues();

}
