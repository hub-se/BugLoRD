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
 * Op2 (Naish et. al) fault localizer $\EF -\frac{\EP}{\EP+\NP+1}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Op2<T> extends AbstractFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Op2() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        return node.getEF(strategy) - node.getEP(strategy) / (node.getEP(strategy) + node.getNP(strategy) + 1);
    }

    @Override
    public String getName() {
        return "op2";
    }

}
