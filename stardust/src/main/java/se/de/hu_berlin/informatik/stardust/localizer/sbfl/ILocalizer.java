/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer.sbfl;

import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer.ComputationStrategies;
import se.de.hu_berlin.informatik.stardust.spectra.INode;

/**
 * @param <T>
 * type used to identify nodes in the system
 */
public interface ILocalizer<T> {

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
