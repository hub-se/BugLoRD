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
 * Jaccard fault localizer
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Jaccard<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Jaccard() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
    	if (node.getEF() == 0) {
    		return 0;
    	}
        return (double)node.getEF() / (double)(node.getEF() + node.getNF() + node.getEP());
    }

    @Override
    public String getName() {
        return "Jaccard";
    }

}
