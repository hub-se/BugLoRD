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
 * Rogot2 fault localizer $\frac{1}{4}\left(\frac{\EF}{\EF+\EP}+\frac{\EF}{\EF+\NF}+\frac{\NP}{\NP+\EP}+\frac{\NP}{\NP+\NF}\right)$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Rogot2<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Rogot2() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        double frac1 = (double)node.getEF(strategy) / (double)(node.getEF(strategy) + node.getEP(strategy));
        if (node.getEF(strategy) == 0) {
        	frac1 = 0;
        }
        double frac2 = (double)node.getEF(strategy) / (double)(node.getEF(strategy) + node.getNF(strategy));
        if (node.getEF(strategy) == 0) {
        	frac2 = 0;
        }
        double frac3 = (double)node.getNP(strategy) / (double)(node.getNP(strategy) + node.getEP(strategy));
        if (node.getNP(strategy) == 0) {
        	frac3 = 0;
        }
        double frac4 = (double)node.getNP(strategy) / (double)(node.getNP(strategy) + node.getNF(strategy));
        if (node.getNP(strategy) == 0) {
        	frac4 = 0;
        }
        return 0.25d * (frac1 + frac2 + frac3 + frac4);
    }

    @Override
    public String getName() {
        return "Rogot2";
    }

}
