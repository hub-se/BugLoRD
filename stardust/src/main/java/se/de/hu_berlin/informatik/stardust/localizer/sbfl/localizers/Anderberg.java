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
 * Anderberg fault localizer $\frac{\EF}{\EF+2(\NF+\EP)}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Anderberg<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Anderberg() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
    	double numerator = node.getEF(strategy);
    	if (numerator == 0) {
    		return 0;
    	}
        return  numerator / (double)(node.getEF(strategy) + 2.0d * (node.getNF(strategy) + node.getEP(strategy)));
    }

    @Override
    public String getName() {
        return "Anderberg";
    }

}
