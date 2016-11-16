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
 * Ochiai fault localizer $\frac{\EF}{\sqrt{(\EF+\NF)\cdot(\EF+\EP)}}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Ochiai<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Ochiai() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
    	if (node.getEF() == 0) {
    		return 0;
    	}
        return (double)node.getEF()
                / Math.sqrt((node.getEF() + node.getNF()) * (node.getEF() + node.getEP()));
    }

    @Override
    public String getName() {
        return "ochiai";
    }

}
