/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @param <T> type used to identify nodes in the system.
 */
public class LocalizerCache<T> implements ILocalizerCache<T> {

    /**
     * The spectra this node belongs to
     */
    private final ISpectra<T, ? extends ITrace<T>> spectra;

    /**
     * Holds the number of traces that were available in the spectra when the
     * cache was created
     */
    private int __cacheTraceCount = -1;

    // TODO: arrays instead of maps
    /**
     * cache EF
     */
    private Map<T, Double> __cacheEF;
    /**
     * cache EP
     */
    private Map<T, Double> __cacheEP;
    /**
     * cache NF
     */
    private Map<T, Double> __cacheNF;
    /**
     * cache NP
     */
    private Map<T, Double> __cacheNP;

    /**
     * Constructs the localizer
     *
     * @param spectra the spectra this node belongs to
     */
    public LocalizerCache(final ISpectra<T, ? extends ITrace<T>> spectra) {
        this.spectra = spectra;
    }

    private double computeValue(ComputationStrategies strategy, Predicate<? super ITrace<T>> predicate) {
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
            case SIMILARITY_FL: {
                throw new UnsupportedOperationException();
//			Collection<? extends ITrace<T>> failingTraces = this.spectra.getFailingTraces();
//			int failingTracesCount = failingTraces.size();
//			if (failingTracesCount == 0) {
//				return 0; // reevaluate this
//			}
//
//			double count = 0.0;
//			// have to compute a value for each failing trace
//			for (final ITrace<T> failingTrace : failingTraces) {
//				for (final ITrace<T> trace : this.spectra.getTraces()) {
//					if (predicate.test(trace)) {
//						// get the similarity score (ranges from 0 to 1)
//						TraceInfo similarityScore = this.spectra.getSimilarityMap(failingTrace).get(trace);
//						if (similarityScore == null) {
//							Log.abort(this, "Similarity Score is null.");
//						}
//						count += similarityScore;
//					}
//				}
//			}
//			// average over all failing traces
//			count /= failingTracesCount;
//
//			return count;
            }
            default:
                throw new UnsupportedOperationException("Not yet implemented.");
        }
    }

    @Override
    public double getNP(INode<T> node, ComputationStrategies strategy) {
        if (this.cacheOutdated()) {
            resetCache();
        }
        Double np = this.__cacheNP.get(node.getIdentifier());
        if (np == null) {
            np = computeValue(strategy, trace -> (trace.isSuccessful() && !trace.isInvolved(node)));
            this.__cacheNP.put(node.getIdentifier(), np);
        }
        return np;
    }

    @Override
    public double getNF(INode<T> node, ComputationStrategies strategy) {
        if (this.cacheOutdated()) {
            resetCache();
        }
        Double nf = this.__cacheNF.get(node.getIdentifier());
        if (nf == null) {
            nf = computeValue(strategy, trace -> (!trace.isSuccessful() && !trace.isInvolved(node)));
            this.__cacheNF.put(node.getIdentifier(), nf);
        }
        return nf;
    }

    @Override
    public double getEP(INode<T> node, ComputationStrategies strategy) {
        if (this.cacheOutdated()) {
            resetCache();
        }
        Double ep = this.__cacheEP.get(node.getIdentifier());
        if (ep == null) {
            ep = computeValue(strategy, trace -> (trace.isSuccessful() && trace.isInvolved(node)));
            this.__cacheEP.put(node.getIdentifier(), ep);
        }
        return ep;
    }

    @Override
    public double getEF(INode<T> node, ComputationStrategies strategy) {
        if (this.cacheOutdated()) {
            resetCache();
        }
        Double ef = this.__cacheEF.get(node.getIdentifier());
        if (ef == null) {
            ef = computeValue(strategy, trace -> (!trace.isSuccessful() && trace.isInvolved(node)));
            this.__cacheEF.put(node.getIdentifier(), ef);
        }
        return ef;
    }

    /**
     * Check if the cache is outdated
     *
     * @return true if the cache is outdated, false otherwise.
     */
    private boolean cacheOutdated() {
        return this.__cacheTraceCount < 0 || this.__cacheTraceCount != this.spectra.getTraces().size();
    }

    @Override
    public void invalidateCachedValues() {
        resetCache();
    }

    private void resetCache() {
        this.__cacheEF = new HashMap<>();
        this.__cacheEP = new HashMap<>();
        this.__cacheNF = new HashMap<>();
        this.__cacheNP = new HashMap<>();
        this.__cacheTraceCount = this.spectra.getTraces().size();
    }

    @Override
    public Collection<INode<T>> getNodes() {
        return spectra.getNodes();
    }

    @Override
    public Collection<? extends ITrace<T>> getTraces() {
        return spectra.getTraces();
    }

//	@Override
//	public Ranking<INode<T>> localize(IFaultLocalizer<T> localizer, ComputationStrategies strategy) {
//		return localizer.localize(this.spectra, strategy);
//	}

}
