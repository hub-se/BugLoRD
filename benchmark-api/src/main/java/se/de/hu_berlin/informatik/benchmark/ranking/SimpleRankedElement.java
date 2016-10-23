package se.de.hu_berlin.informatik.benchmark.ranking;

import java.util.SortedSet;

/**
 * Class used to store node and suspiciousness in order to use the {@link SortedSet} interface for actual node
 * ordering.
 * @param <T>
 * The type of the node
 */
public class SimpleRankedElement<T> implements RankedElement<T> {
    /** Node of the ranked element */
    protected final T node;
    /** Suspiciousness of the ranked element */
    protected final double suspicousness;

    public SimpleRankedElement(final T node, final double suspiciousness) {
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
        return getElement().equals(el.getElement()) && Double.compare(getRankingValue(), el.getRankingValue()) == 0;
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

	@Override
	public int compareTo(RankedElement<T> other) {
		final int compareTo = Double.compare(other.getRankingValue(), this.getRankingValue());
        if (compareTo != 0) {
            return compareTo;
        }
        // TODO: as TreeSet consideres compareTo == 0 as equal, we need to ensure all elements have a total order.
        return Integer.compare(other.hashCode(), this.hashCode());
	}

	@Override
	public T getElement() {
		return node;
	}

	@Override
	public String getIdentifier() {
		return node.toString();
	}

	@Override
	public double getRankingValue() {
		return suspicousness;
	}

}
