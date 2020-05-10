/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.faultlocalizer.sbfl.localizers.simfl.depricated;

import se.de.hu_berlin.informatik.faultlocalizer.sbfl.AbstractFaultLocalizer;
import se.de.hu_berlin.informatik.spectra.core.*;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

import java.util.Collection;

/**
 * Similarity based Fault Localizer
 *
 * @param <T> type used to identify nodes in the system
 */
public class AsymmetricSimilarityFL<T> extends AbstractFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public AsymmetricSimilarityFL() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        ISpectra<T, ? extends ITrace<T>> spectra = node.getSpectra();
        Collection<? extends ITrace<T>> failingTraces = spectra.getFailingTraces();
        int failingTracesCount = failingTraces.size();
        if (failingTracesCount == 0) {
            return 0; // reevaluate this
        }

        double sum = 0.0;
        // have to compute a value for each failing trace (-> primary failing test case)
        for (final ITrace<T> failingTrace : failingTraces) {
            boolean nodeInvolvedInFailingTrace = failingTrace.isInvolved(node);
            double failingTraceSize = failingTrace.getInvolvedNodes().size();
            if (failingTraceSize <= 0) {
                // skip traces that did not execute any nodes...
                continue;
            }
            if (nodeInvolvedInFailingTrace) {
                // the primary failing test case executed this node -> bug is more likely to be here!
                double count = 0.0;
                for (final ITrace<T> trace : spectra.getTraces()) {
                    if (!trace.isSuccessful()) {
                        continue;
                    }
                    // get the info
                    TraceInfo similarityScore = spectra.getSimilarityMap(failingTrace).get(trace);
                    if (similarityScore == null) {
                        Log.abort(this, "Similarity Score is null.");
                    }

                    if (trace.getInvolvedNodes().size() <= 0) {
                        // skip traces that did not execute any nodes...
                        continue;
                    }

                    // this test case was successful -> bug is less likely to be here if covered!
                    if (trace.isInvolved(node)) {
                        // node is involved in the test case -> less suspicious
                        // -> lower suspiciousness if both traces share more hits
                        // (i.e. the failing trace shares a lot of functionality with the successful one)
                        count -= similarityScore.getSameHitCount() / failingTraceSize;
                    } else {
                        // node is NOT involved in the test case -> more suspicious
                        // -> higher suspiciousness if both traces share more hits
                        // (i.e. the failing trace shares a lot of functionality with the successful one, 
                        // but on this node, there is an anomaly...)
                        count += similarityScore.getSameHitCount() / failingTraceSize;
                    }

                }

                // score is between -1 and 1
                sum += (count / spectra.getTraces().size());
            } else {
                // node is not covered by the primary failing trace -> the node has less relevance (to be erroneous)
                double count = 0.0;
                for (final ITrace<T> trace : spectra.getTraces()) {
                    if (!trace.isSuccessful()) {
                        continue;
                    }
                    // get the info
                    TraceInfo similarityScore = spectra.getSimilarityMap(failingTrace).get(trace);
                    if (similarityScore == null) {
                        Log.abort(this, "Similarity Score is null.");
                    }

                    if (trace.getInvolvedNodes().size() <= 0) {
                        // skip traces that did not execute any nodes...
                        continue;
                    }

                    // this test case was successful -> bug is less likely to be here if covered!
                    if (trace.isInvolved(node)) {
                        // node is involved in the test case -> less suspicious
                        // -> higher suspiciousness if both traces share more hits
                        // (i.e. the failing trace shares a lot of functionality with the successful one)
                        // the idea is that there may be some functionality missing in the primary failing test case
                        count -= (1 - similarityScore.getSameHitCount() / failingTraceSize);
                    } else {
                        // node is NOT involved in the test case -> less suspicious (since not involved in the primary failing test case)
                        // (node is neither involved in the primary failing test case nor in this test case)
                        count -= 1;
                    }

                }

                // score is between -1 and 1
                sum += (count / spectra.getTraces().size());
            }

        }
        // average over all failing traces
        sum /= failingTracesCount;
        return sum;
    }

}
