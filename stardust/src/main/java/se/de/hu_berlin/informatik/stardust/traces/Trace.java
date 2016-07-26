/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.traces;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a single execution trace and its success state.
 *
 * @author Fabian Keller 'dev@fabian-keller.de'
 *
 * @param <T>
 *            type used to identify nodes in the system.
 */
public class Trace<T> implements IMutableTrace<T> {

    /** Holds the success state of this trace */
    private final boolean successful;

    /** Holds the spectra this trace belongs to */
    private final ISpectra<T> spectra;

    /**
     * Stores the involvement of all nodes for this trace. Use {@link Spectra#getNodes()} to get all nodes.
     */
    private final Set<INode<T>> involvement = new HashSet<>();

    /**
     * Create a trace for a spectra.
     *
     * @param spectra
     *            the trace belongs to
     * @param successful
     *            true if the trace originates from a successful execution, false otherwise
     */
    protected Trace(final ISpectra<T> spectra, final boolean successful) {
        this.successful = successful;
        this.spectra = spectra;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSuccessful() {
        return this.successful;
    }

//    /** {@inheritDoc} */
//    @Override
//    public ISpectra<T> getSpectra() {
//        return this.spectra;
//    }

    /** {@inheritDoc} */
    @Override
    public void setInvolvement(final T identifier, final boolean involved) {
        setInvolvement(spectra.getNode(identifier), involved);
    }

    /** {@inheritDoc} */
    @Override
    public void setInvolvement(final INode<T> node, final boolean involved) {
    	if (involved) {
    		involvement.add(node);
    	}
    }

    /** {@inheritDoc} */
    @Override
    public void setInvolvementForIdentifiers(final Map<T, Boolean> nodeInvolvement) {
        for (final Map.Entry<T, Boolean> cur : nodeInvolvement.entrySet()) {
            setInvolvement(cur.getKey(), cur.getValue());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setInvolvementForNodes(final Map<INode<T>, Boolean> nodeInvolvement) {
        for (final Map.Entry<INode<T>, Boolean> cur : nodeInvolvement.entrySet()) {
            setInvolvement(cur.getKey(), cur.getValue());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInvolved(final INode<T> node) {
        return involvement.contains(node);
    }
}
