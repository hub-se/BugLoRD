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
 * Sokal fault localizer $\frac{2(\EF+\NP)}{2(\EF+\NP)+\NF+\EP}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Sokal<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Sokal() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
    	double numerator = 2.0d * (node.getEF(strategy) + node.getNP(strategy));
    	if (numerator == 0) {
    		return 0;
    	}
        return numerator / (double)(2.0d * (node.getEF(strategy) + node.getNP(strategy)) + node.getNF(strategy) + node.getEP(strategy));
    }

    @Override
    public String getName() {
        return "Sokal";
    }

}
