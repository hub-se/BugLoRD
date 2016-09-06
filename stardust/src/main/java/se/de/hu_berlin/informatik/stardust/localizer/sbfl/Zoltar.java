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
    public double suspiciousness(final INode<T> node) {
        double denomPart = (double)(10000d * node.getNF() * node.getEP()) / (double)node.getEF();
        if (node.getNF() * node.getEP() == 0) {
        	denomPart = 0;
        }
        if (node.getEF() == 0) {
        	return 0;
        }
        return (double)node.getEF() / (double)(node.getEF() + node.getNF() + node.getEP() + denomPart);
    }

    @Override
    public String getName() {
        return "Zoltar";
    }

}
