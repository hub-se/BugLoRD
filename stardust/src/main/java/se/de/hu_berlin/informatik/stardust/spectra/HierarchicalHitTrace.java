package se.de.hu_berlin.informatik.stardust.spectra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This trace implementation ensures the involvement of all child nodes 
 * of a parent node are compiled into a single involvement information.
 */
public class HierarchicalHitTrace<P, C> extends HitTrace<P> {

	/** contains the spectra this trace belongs to */
	private final AbstractHierarchicalSpectra<P,C,?> spectra;

    /** Holds the associated child trace of this trace */
    private final ITrace<C> childTrace;

    /**
     * Proxy to parent constructor.
     *
     * @param hierarchicalSpectra
     * @param successful
     */
    protected HierarchicalHitTrace(final AbstractHierarchicalSpectra<P, C, ?> hierarchicalSpectra, final ITrace<C> childTrace) {
        super(hierarchicalSpectra, childTrace.getIdentifier(), childTrace.isSuccessful());
        this.spectra = hierarchicalSpectra;
        this.childTrace = childTrace;
    }

    @Override
    public boolean isInvolved(final INode<P> node) {
        for (final INode<C> childNode : spectra.getChildrenOf(node)) {
            if (this.childTrace.isInvolved(childNode)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
	public boolean isInvolved(P identifier) {
    	if (spectra.hasNode(identifier)) {
    		return isInvolved(spectra.getOrCreateNode(identifier));
    	} else {
    		return false;
    	}
	}

	@Override
	public int involvedNodesCount() {
		int involvedCount = 0;
		for (INode<P> node : spectra.getNodes()) {
			if (isInvolved(node)) {
				++involvedCount;
			}
		}
		return involvedCount;
	}

	@Override
	public Collection<P> getInvolvedNodes() {
		List<P> nodes = new ArrayList<>();
		for (INode<P> node : spectra.getNodes()) {
			if (isInvolved(node)) {
				nodes.add(node.getIdentifier());
			}
		}
		return nodes;
	}

	@Override
	public void setInvolvement(P identifier, boolean involved) {
		throw new UnsupportedOperationException("Not able to set involvement in hierarchical spectra.");
	}

	@Override
	public void setInvolvement(INode<P> node, boolean involved) {
		throw new UnsupportedOperationException("Not able to set involvement in hierarchical spectra.");
	}

	@Override
	public void setInvolvementForIdentifiers(Map<P, Boolean> involvement) {
		throw new UnsupportedOperationException("Not able to set involvement in hierarchical spectra.");
	}

	@Override
	public void setInvolvementForNodes(Map<INode<P>, Boolean> involvement) {
		throw new UnsupportedOperationException("Not able to set involvement in hierarchical spectra.");
	}

}