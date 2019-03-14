/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers;

import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractSpectrumBasedFaultLocalizer;
import se.de.hu_berlin.informatik.spectra.core.ComputationStrategies;
import se.de.hu_berlin.informatik.spectra.core.INode;

/**
 * Zoltar fault localizer $\frac{\EF}{\EF+\NF+\EP+\frac{10000\NF\EP}{\EF}}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Zoltar<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Zoltar() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        double denomPart = (10000d * node.getNF(strategy) * node.getEP(strategy)) / node.getEF(strategy);
        if (node.getNF(strategy) * node.getEP(strategy) == 0) {
        	denomPart = 0;
        }
        if (node.getEF(strategy) == 0) {
        	return 0;
        }
        return node.getEF(strategy) / (node.getEF(strategy) + node.getNF(strategy) + node.getEP(strategy) + denomPart);
    }

    @Override
    public String getName() {
        return "Zoltar";
    }

}
