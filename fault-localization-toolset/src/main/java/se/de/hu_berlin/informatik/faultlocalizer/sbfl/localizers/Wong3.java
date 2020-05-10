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

//			  $\begin{array}{rl}
//            \EF - \EP & \text{ if } \EP\leq 2 \\
//            \EF - \left(2+\frac{1}{10}(\EP-2)\right) & \text{ if } 2 < \EP\leq 10 \\
//            \EF - \left(2.8+\frac{1}{1000}(\EP-10)\right) & \text{ otherwise}
//            \end{array}$

/**
 * Wong3 fault localizer
 *
 * @param <T> type used to identify nodes in the system
 */
public class Wong3<T> extends AbstractFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Wong3() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        if (node.getEP(strategy) <= 2) {
            return node.getEF(strategy) - node.getEP(strategy);
        } else if (node.getEP(strategy) <= 10) {
            return node.getEF(strategy) - (2.0d + 0.1d * (node.getEP(strategy) - 2.0d));
        } else {
            return node.getEF(strategy) - (2.8d + 0.001d * (node.getEP(strategy) - 10.0d));
        }
    }

}
