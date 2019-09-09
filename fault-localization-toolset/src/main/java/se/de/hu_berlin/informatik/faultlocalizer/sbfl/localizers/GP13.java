/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers;

import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.INode;

/**
 * GP13 (genetic algorithm derived formula) fault localizer $\EF \left(1+ \frac{1}{2\EP+\EF}\right)$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class GP13<T> extends AbstractFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public GP13() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
    	if (node.getEF(strategy) == 0) {
    		return 0;
    	}
        return node.getEF(strategy) * (1.0 + 1.0 / (2*node.getEP(strategy) + node.getEF(strategy)));
    }

}
