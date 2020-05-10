/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.core.count;

import se.de.hu_berlin.informatik.spectra.core.AbstractSpectra;

import java.nio.file.Path;

/**
 * The spectra class holds all nodes and traces belonging to the spectra.
 * <p>
 * You can imagine the information accessible through this class has a matrix layout:
 *
 * <pre>
 *          | Trace1 | Trace2 | Trace3 | ... | TraceN |
 *  --------|--------|--------|--------|-----|--------|
 *  Node1   |   1    |   0    |   0    | ... |   1    |
 *  Node2   |   2    |   0    |   3    | ... |   2    |
 *  Node3   |   0    |   5    |   0    | ... |   4    |
 *  ...     |  ...   |  ...   |  ...   | ... |  ...   |
 *  NodeX   |   13   |   1    |   4    | ... |   0    |
 *  --------|--------|--------|--------|-----|--------|
 *  Result  |   1    |   1    |   0    | ... |   0    |
 * </pre>
 * <p>
 * The nodes are the components of a system that are analyzed. For each trace the involvement of the node is stored.
 * The numbers denote node involvement (number of hits), a '0' denotes no involvement of the node in the current
 * execution trace. For each execution trace we also know whether the execution was successful or not.
 * <p>
 * Given this information, it is possible to use this spectra as input for various fault localization techniques.
 *
 * @param <T> type used to identify nodes in the system.
 */
public class CountSpectra<T> extends AbstractSpectra<T, CountTrace<T>> {

//    /**
//     * Creates a new spectra.
//     */
//    public CountSpectra() {
//        super();
//    }

    public CountSpectra(Path spectraZipFile) {
        super(spectraZipFile);
    }

    @Override
    protected CountTrace<T> createNewTrace(String identifier, int traceIndex, boolean successful) {
        return new CountTrace<>(this, identifier, traceIndex, successful);
    }

}
