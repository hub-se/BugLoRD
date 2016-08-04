package se.de.hu_berlin.informatik.stardust.localizer;

import java.util.SortedSet;

import se.de.hu_berlin.informatik.stardust.spectra.INode;

/**
 * Class used to store node and suspiciousness in order to use the {@link SortedSet} interface for actual node
 * ordering.
 * @param <T>
 * The type of the node
 */
public final class RankedElement<T> implements Comparable<RankedElement<T>> {
    /** Node of the ranked element */
    protected final INode<T> node;
    /** Suspiciousness of the ranked element */
    protected final Double suspicousness;

    public RankedElement(final INode<T> node, final double suspiciousness) {
        super();
        this.node = node;
        this.suspicousness = suspiciousness;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof RankedElement)) {
            return false;
        }
        @SuppressWarnings("unchecked")
		final RankedElement<T> el = (RankedElement<T>) other;
        return node.equals(el.node) && suspicousness.equals(el.suspicousness);
    }

    @Override
    public int hashCode() {
        return node.getIdentifier().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final RankedElement<T> other) {
        final int compareTo = other.suspicousness.compareTo(this.suspicousness);
        if (compareTo != 0) {
            return compareTo;
        }
        // TODO: as TreeSet consideres compareTo == 0 as equal, we need to ensure all elements have a total order.
        return Integer.valueOf(other.hashCode()).compareTo(this.hashCode());
    }

}
