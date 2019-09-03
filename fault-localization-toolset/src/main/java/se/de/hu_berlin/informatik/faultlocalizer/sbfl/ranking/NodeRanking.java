/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.utils.experiments.ranking.SimpleRanking;

/**
 * Class used to create a ranking of nodes with corresponding suspiciousness set.
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public class NodeRanking<T> extends SimpleRanking<INode<T>> {

    /**
     * Create a new ranking.
     */
    public NodeRanking() {
        super(false);
    }

}
