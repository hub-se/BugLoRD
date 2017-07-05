/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.spectra;

import java.util.Collection;
import java.util.function.Predicate;

import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer.ComputationStrategies;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Represents a single node in a system.
 *
 * @param <T>
 * type used to identify nodes in the system.
 */
public class Node<T> implements INode<T> {

	/** The identifier of this node */
	private final T identifier;

	/** The spectra this node belongs to */
	private final ISpectra<T> spectra;

	/**
	 * Holds the number of traces that were available in the spectra when the
	 * cache was created
	 */
	private double __cacheTraceCount = Double.NaN; // NOCS
	/** cache IF */
	private double __cacheIF = Double.NaN; // NOCS
	/** cache IS */
	private double __cacheIS = Double.NaN; // NOCS
	/** cache NF */
	private double __cacheNF = Double.NaN; // NOCS
	/** cache IS */
	private double __cacheNS = Double.NaN; // NOCS

	/**
	 * Constructs the node
	 *
	 * @param identifier
	 * the identifier of this node
	 * @param spectra
	 * the spectra this node belongs to
	 */
	protected Node(final T identifier, final ISpectra<T> spectra) {
		this.identifier = identifier;
		this.spectra = spectra;
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getIdentifier()
	 */
	@Override
	public T getIdentifier() {
		return this.identifier;
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getSpectra()
	 */
	@Override
	public ISpectra<T> getSpectra() {
		return this.spectra;
	}

	private double computeValue(ComputationStrategies strategy, Predicate<ITrace<T>> predicate) {
		switch (strategy) {
		case STANDARD_SBFL: {
			int count = 0;
			for (final ITrace<T> trace : this.spectra.getTraces()) {
				if (predicate.test(trace)) {
					++count;
				}
			}
			return count;
		}
		case SIMILARITY_SBFL: {
			Collection<ITrace<T>> failingTraces = this.spectra.getFailingTraces();
			int failingTracesCount = failingTraces.size();
			if (failingTracesCount == 0) {
				return 0; // reevaluate this
			}

			double count = 0.0;
			// have to compute a value for each failing trace
			for (final ITrace<T> failingTrace : failingTraces) {
				for (final ITrace<T> trace : this.spectra.getTraces()) {
					if (predicate.test(trace)) {
						// get the similarity score (ranges from 0 to 1)
						Double similarityScore = this.getSpectra().getSimilarityMap(failingTrace).get(trace);
						if (similarityScore == null) {
							Log.abort(this, "Similarity Score is null.");
						}
						count += similarityScore;
					}
				}
			}
			// average over all failing traces
			count /= failingTracesCount;

			return count;
		}
		default:
			throw new UnsupportedOperationException("Not yet implemented.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getNS()
	 */
	@Override
	public double getNP(ComputationStrategies strategy) {
		if (this.cacheOutdated()) {
			resetCache();
		}
		if (Double.isNaN(this.__cacheNS)) {
			this.__cacheNS = computeValue(strategy, trace -> (trace.isSuccessful() && !trace.isInvolved(this)));
		}
		return this.__cacheNS;
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getNF()
	 */
	@Override
	public double getNF(ComputationStrategies strategy) {
		if (this.cacheOutdated()) {
			resetCache();
		}
		if (Double.isNaN(this.__cacheNF)) {
			this.__cacheNF = computeValue(strategy, trace -> (!trace.isSuccessful() && !trace.isInvolved(this)));
		}
		return this.__cacheNF;
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getIS()
	 */
	@Override
	public double getEP(ComputationStrategies strategy) {
		if (this.cacheOutdated()) {
			resetCache();
		}
		if (Double.isNaN(this.__cacheIS)) {
			this.__cacheIS = computeValue(strategy, trace -> (trace.isSuccessful() && trace.isInvolved(this)));
		}
		return this.__cacheIS;
	}

	/*
	 * (non-Javadoc)
	 * @see fk.stardust.traces.INode#getIF()
	 */
	@Override
	public double getEF(ComputationStrategies strategy) {
		if (this.cacheOutdated()) {
			resetCache();
		}
		if (Double.isNaN(this.__cacheIF)) {
			this.__cacheIF = computeValue(strategy, trace -> (!trace.isSuccessful() && trace.isInvolved(this)));
		}
		return this.__cacheIF;
	}

	/**
	 * Check if the cache is outdated
	 *
	 * @return true if the cache is outdated, false otherwise.
	 */
	private boolean cacheOutdated() {
		return Double.isNaN(this.__cacheTraceCount) || this.__cacheTraceCount != this.spectra.getTraces().size();
	}

	@Override
	public void invalidateCachedValues() {
		resetCache();
	}

	private void resetCache() {
		this.__cacheIF = Double.NaN;
		this.__cacheIS = Double.NaN;
		this.__cacheNF = Double.NaN;
		this.__cacheNS = Double.NaN;
		this.__cacheTraceCount = this.spectra.getTraces().size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.identifier.toString();
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + getIdentifier().hashCode();
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Node) {
			Node<?> oNode = (Node<?>) obj;
			if (!this.getIdentifier().equals(oNode.getIdentifier())) {
				return false;
			}
			return true;
		}
		return false;
	}

}
