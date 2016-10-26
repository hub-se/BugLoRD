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
import se.de.hu_berlin.informatik.stardust.spectra.INode;

/**
 * Class used to create a ranking of nodes with corresponding suspiciousness set.
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public class HitRanking<T> extends SBFLRanking<T> {

    /**
     * Create a new ranking.
     */
    public HitRanking() {
        super();
    }

    /**
     * Saves the ranking result to a given file.
     *
     * @param filename
     *            the file name to save the ranking to
     * @throws IOException
     * 			  in case of not being able to write to the given path
     */
    @Override
    public void save(final String filename) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filename);
            for (final RankedElement<INode<T>> el : getRankedElements()) {
                writer.write(String.format("%s\n", el.getIdentifier()));
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

//    /**
//     * Class used to store node and suspiciousness in order to use the {@link SortedSet} interface for actual node
//     * ordering.
//     */
//    protected final class RankedElement implements Comparable<RankedElement> {
//        /** Node of the ranked element */
//        protected final INode<T> node;
//        /** Suspiciousness of the ranked element */
//        protected final Double suspicousness;
//
//        private RankedElement(final INode<T> node, final double suspiciousness) {
//            super();
//            this.node = node;
//            this.suspicousness = suspiciousness;
//        }
//
//        @Override
//        public boolean equals(final Object other) {
//            if (!(other instanceof HitRanking.RankedElement)) {
//                return false;
//            }
//            @SuppressWarnings("unchecked")
//            final RankedElement el = (RankedElement) other;
//            return this.node.equals(el.node) && this.suspicousness.equals(el.suspicousness);
//        }
//
//        @Override
//        public int hashCode() {
//            return this.node.getIdentifier().hashCode();
//        }
//
//        /**
//         * {@inheritDoc}
//         */
//        @Override
//        public int compareTo(final RankedElement other) {
//            final int compareTo = other.suspicousness.compareTo(this.suspicousness);
//            if (compareTo != 0) {
//                return compareTo;
//            }
//            // TODO: as TreeSet consideres compareTo == 0 as equal, we need to ensure all elements have a total order.
//            return Integer.valueOf(other.hashCode()).compareTo(this.hashCode());
//        }
//    }

}
