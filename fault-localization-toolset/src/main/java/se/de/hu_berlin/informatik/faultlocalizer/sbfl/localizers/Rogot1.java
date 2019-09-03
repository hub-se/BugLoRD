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
 * Rogot1 fault localizer $\frac{1}{2}\left(\frac{\EF}{2\EF+\NF+\EP}+\frac{\NP}{2\NP+\NF+\EP}\right)$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Rogot1<T> extends AbstractFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Rogot1() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        double left = node.getEF(strategy) / (2.0d * node.getEF(strategy) + node.getNF(strategy) + node.getEP(strategy));
        if (node.getEF(strategy) == 0) {
        	left = 0;
        }
        double right = node.getNP(strategy) / (2.0d * node.getNP(strategy) + node.getNF(strategy) + node.getEP(strategy));
        if (node.getNP(strategy) == 0) {
        	right = 0;
        }
        return 0.5d * (left + right);
    }

    @Override
    public String getName() {
        return "Rogot1";
    }

}
