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
 * Kulczynski2 fault localizer $\frac{1}{2}\left(\frac{\EF}{\EF+\NF}+\frac{\EF}{\EF+\EP}\right)$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Kulczynski2<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Kulczynski2() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        double left = node.getEF(strategy) / (node.getEF(strategy) + node.getNF(strategy));
        if (node.getEF(strategy) == 0) {
        	left = 0;
        }
        double right = node.getEF(strategy) / (node.getEF(strategy) + node.getEP(strategy));
        if (node.getEF(strategy) == 0) {
        	right = 0;
        }
        return 0.5d * (left + right);
    }

    @Override
    public String getName() {
        return "Kulczynski2";
    }

}
