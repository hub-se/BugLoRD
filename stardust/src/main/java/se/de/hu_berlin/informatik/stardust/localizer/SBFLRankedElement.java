package se.de.hu_berlin.informatik.stardust.localizer;

import java.util.SortedSet;

import se.de.hu_berlin.informatik.benchmark.ranking.SimpleRankedElement;

/**
 * Class used to store node and suspiciousness in order to use the {@link SortedSet} interface for actual node
 * ordering.
 * @param <T>
 * The type of the node
 */
public final class SBFLRankedElement<T> extends SimpleRankedElement<T> {

	public SBFLRankedElement(T node, double suspiciousness) {
		super(node, suspiciousness);
	}

}
