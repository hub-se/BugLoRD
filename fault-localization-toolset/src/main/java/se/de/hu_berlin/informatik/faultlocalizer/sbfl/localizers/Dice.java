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
 * Dice fault localizer $\frac{2\EF}{\EF+\NF+\EP}$
 *
 * @param <T> type used to identify nodes in the system
 */
public class Dice<T> extends AbstractFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Dice() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        double numerator = 2.0d * node.getEF(strategy);
        if (numerator == 0) {
            return 0;
        }
        return numerator / (node.getEF(strategy) + node.getNF(strategy) + node.getEP(strategy));
    }

}
