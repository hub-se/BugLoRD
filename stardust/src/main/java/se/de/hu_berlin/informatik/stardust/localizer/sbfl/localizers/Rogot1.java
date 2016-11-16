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
 * Rogot1 fault localizer $\frac{1}{2}\left(\frac{\EF}{2\EF+\NF+\EP}+\frac{\NP}{2\NP+\NF+\EP}\right)$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Rogot1<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Rogot1() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        double left = (double)node.getEF() / (double)(2.0d * node.getEF() + node.getNF() + node.getEP());
        if (node.getEF() == 0) {
        	left = 0;
        }
        double right = (double)node.getNP() / (double)(2.0d * node.getNP() + node.getNF() + node.getEP());
        if (node.getNP() == 0) {
        	right = 0;
        }
        return 0.5d * (left + right);
    }

    @Override
    public String getName() {
        return "Rogot1";
    }

}
