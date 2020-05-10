package se.de.hu_berlin.informatik.faultlocalizer.sbfl.ranking;

import se.de.hu_berlin.informatik.utils.experiments.ranking.SimpleRankedElement;

import java.util.SortedSet;

/**
 * Class used to store node and suspiciousness in order to use the {@link SortedSet} interface for actual node
 * ordering.
 *
 * @param <T> The type of the node
 */
public final class SBFLRankedElement<T> extends SimpleRankedElement<T> {

    public SBFLRankedElement(T node, double suspiciousness) {
        super(node, suspiciousness);
    }

}
