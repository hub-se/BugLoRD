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
 * Tarantula fault localizer $\frac{\frac{\EF}{\EF+\NF}}{\frac{\EF}{\EF+\NF}+\frac{\EP}{\EP+\NP}}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Tarantula<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Tarantula() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        double part = (double)node.getEF(strategy) / (double)(node.getEF(strategy) + node.getNF(strategy));
        if (node.getEF(strategy) == 0) {
        	return 0;
        }
        double part2 = (double)node.getEP(strategy) / (double)(node.getEP(strategy) + node.getNP(strategy));
        if (node.getEP(strategy) == 0) {
        	part2 = 0;
        }
        return part / (double)(part + part2);
    }

    @Override
    public String getName() {
        return "tarantula";
    }

}
