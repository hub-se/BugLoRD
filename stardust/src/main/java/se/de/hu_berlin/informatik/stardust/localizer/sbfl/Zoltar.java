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
 * Zoltar fault localizer
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
        final double denomPart = new Double(10000d * node.getNF() * node.getEP()) / new Double(node.getEF());
        return new Double(node.getEF()) / new Double(node.getEF() + node.getNF() + node.getEP() + denomPart);
    }

    @Override
    public String getName() {
        return "Zoltar";
    }

}
