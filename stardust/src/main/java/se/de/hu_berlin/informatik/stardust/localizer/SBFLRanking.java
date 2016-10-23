/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer;

import java.io.FileWriter;
import java.io.IOException;
import se.de.hu_berlin.informatik.benchmark.ranking.RankedElement;
import se.de.hu_berlin.informatik.benchmark.ranking.SimpleRanking;
import se.de.hu_berlin.informatik.stardust.spectra.INode;

/**
 * Class used to create a ranking of nodes with corresponding suspiciousness set.
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public class SBFLRanking<T> extends SimpleRanking<INode<T>> {

    /**
     * Create a new ranking.
     */
    public SBFLRanking() {
        super();
    }

    /**
     * Returns the suspiciousness of the given node.
     *
     * @param node
     *            the node to get the suspiciousness of
     * @return suspiciousness
     */
    public double getSuspiciousness(final INode<T> node) {
        return getRankingValue(node);
    }

    /**
     * Saves the ranking result to a given file.
     *
     * @param filename
     *            the file name to save the ranking to
     * @throws IOException
     * 			  in case of not being able to write to the given path
     */
    public void save(final String filename) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filename);
            for (final RankedElement<INode<T>> el : this.rankedNodes) {
                writer.write(String.format("%s: %f\n", el.getIdentifier(), el.getRankingValue()));
            }
        } catch (final Exception e) {
            throw new RuntimeException("Saving the ranking failed.", e);
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    } 

}
