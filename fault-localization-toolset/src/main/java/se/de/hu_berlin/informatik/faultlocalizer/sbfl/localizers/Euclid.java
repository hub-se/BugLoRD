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
 * Euclid fault localizer $\sqrt{\EF+\NP}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Euclid<T> extends AbstractFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Euclid() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        return Math.sqrt(node.getEF(strategy) + node.getNP(strategy));
    }

}
