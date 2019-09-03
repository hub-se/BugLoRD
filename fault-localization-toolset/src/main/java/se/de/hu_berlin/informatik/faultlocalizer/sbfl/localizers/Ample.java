/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers;

import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.INode;

/**
 * Ample fault localizer $\left|\frac{\EF}{\EF+\NF}-\frac{\EP}{\EP+\NP}\right|$
 *
 * @param <T>
 * type used to identify nodes in the system
 */
public class Ample<T> extends AbstractFaultLocalizer<T> {

	/**
	 * Create fault localizer
	 */
	public Ample() {
		super();
	}

	@Override
	public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
		double left = node.getEF(strategy) / (node.getEF(strategy) + node.getNF(strategy));
		if (node.getEF(strategy) == 0) {
			left = 0;
		}
		double right = node.getEP(strategy) / (node.getEP(strategy) + node.getNP(strategy));
		if (node.getEP(strategy) == 0) {
			right = 0;
		}
		return Math.abs(left - right);
	}

	@Override
	public String getName() {
		return "Ample";
	}

}
