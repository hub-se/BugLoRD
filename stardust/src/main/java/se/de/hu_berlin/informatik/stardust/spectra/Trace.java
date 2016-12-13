/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.spectra;

import java.util.Collection;
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
    
    /** Holds the identifier (test case name) of this trace */
    private final String identifier;

    /** Holds the spectra this trace belongs to */
    private final ISpectra<T> spectra;

    /**
     * Stores the involvement of all nodes for this trace. Use {@link Spectra#getNodes()} to get all nodes.
     */
    private final Set<INode<T>> involvement = new HashSet<>();

    /**
     * Create a trace for a spectra.
     * @param spectra
     * the spectra that the trace belongs to
     * @param identifier
     * the identifier of the trace (usually the test case name)
     * @param successful
     * true if the trace originates from a successful execution, false otherwise
     */
    protected Trace(final ISpectra<T> spectra, final String identifier, final boolean successful) {
        this.successful = successful;
        this.spectra = spectra;
        this.identifier = identifier;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSuccessful() {
        return this.successful;
    }

    /** {@inheritDoc} */
    @Override
    public void setInvolvement(final T identifier, final boolean involved) {
    	setInvolvement(spectra.getOrCreateNode(identifier), involved);
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

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public int involvedNodesCount() {
		return involvement.size();
	}

	@Override
	public Collection<INode<T>> getInvolvedNodes() {
		return involvement;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + (isSuccessful() ? 1 : 0);
		result = 31 * result + getIdentifier().hashCode();
		result = 31 * result + involvedNodesCount();
		for (INode<T> node : getInvolvedNodes()) {
			result = 31 * result + node.hashCode();
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Trace) {
			Trace<?> oTrace = (Trace<?>) obj;
			if (this.isSuccessful() != oTrace.isSuccessful() ||
					this.involvedNodesCount() != oTrace.involvedNodesCount() ||
					!this.getIdentifier().equals(oTrace.getIdentifier())) {
				return false;
			}
			for (INode<?> otherNode : oTrace.getInvolvedNodes()) {
				boolean foundEqual = false;
				for (INode<T> node : this.getInvolvedNodes()) {
					if (node.equals(otherNode)) {
						foundEqual = true;
						break;
					}
				}
				if (!foundEqual) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	
}
