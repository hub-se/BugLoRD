/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer.sbfl.localizers;

import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer;
import se.de.hu_berlin.informatik.stardust.spectra.INode;

/**
 * Op2 (Naish et. al) fault localizer $\EF -\frac{\EP}{\EP+\NP+1}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Op2<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Op2() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        return (double)node.getEF(strategy) - (double)node.getEP(strategy) / (double)(node.getEP(strategy) + node.getNP(strategy) + 1);
    }

    @Override
    public String getName() {
        return "op2";
    }

}
