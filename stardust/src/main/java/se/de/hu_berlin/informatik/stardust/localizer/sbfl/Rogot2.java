/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer.sbfl;

import se.de.hu_berlin.informatik.stardust.spectra.INode;

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
    public double suspiciousness(final INode<T> node) {
        double frac1 = (double)node.getEF() / (double)(node.getEF() + node.getEP());
        if (node.getEF() == 0) {
        	frac1 = 0;
        }
        double frac2 = (double)node.getEF() / (double)(node.getEF() + node.getNF());
        if (node.getEF() == 0) {
        	frac2 = 0;
        }
        double frac3 = (double)node.getNP() / (double)(node.getNP() + node.getEP());
        if (node.getNP() == 0) {
        	frac3 = 0;
        }
        double frac4 = (double)node.getNP() / (double)(node.getNP() + node.getNF());
        if (node.getNP() == 0) {
        	frac4 = 0;
        }
        return 0.25d * (frac1 + frac2 + frac3 + frac4);
    }

    @Override
    public String getName() {
        return "Rogot2";
    }

}
