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
 * Overlap fault localizer $\frac{\EF}{\min(\EF.\NF.\EP)}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Overlap<T> extends AbstractFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Overlap() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
    	if (node.getEF(strategy) == 0) {
    		return 0;
    	}
    	return node.getEF(strategy) / Math.min(node.getEF(strategy), Math.min(node.getNF(strategy), node.getEP(strategy)));
    }

}
