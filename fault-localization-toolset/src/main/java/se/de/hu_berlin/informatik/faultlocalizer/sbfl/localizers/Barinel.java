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
 * Barinel fault localizer $1 -\frac{\EP}{\EP+\EF}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Barinel<T> extends AbstractFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Barinel() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        return 1.0 - node.getEP(strategy) / (node.getEP(strategy) + node.getEF(strategy));
    }

    @Override
    public String getName() {
        return "barinel";
    }

}
