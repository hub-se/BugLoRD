/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.core.hit;

import java.nio.file.Path;

import se.de.hu_berlin.informatik.spectra.core.AbstractSpectra;
import se.de.hu_berlin.informatik.spectra.util.SpectraUtils;

/**
 * The spectra class holds all nodes and traces belonging to the spectra.
 *
 * You can imagine the information accessible through this class has a matrix
 * layout:
 *
 * <pre>
 *          | Trace1 | Trace2 | Trace3 | ... | TraceN |
 *  --------|--------|--------|--------|-----|--------|
 *  Node1   |   1    |   0    |   0    | ... |   1    |
 *  Node2   |   1    |   0    |   1    | ... |   1    |
 *  Node3   |   0    |   1    |   0    | ... |   1    |
 *  ...     |  ...   |  ...   |  ...   | ... |  ...   |
 *  NodeX   |   1    |   1    |   1    | ... |   0    |
 *  --------|--------|--------|--------|-----|--------|
 *  Result  |   1    |   1    |   0    | ... |   0    |
 * </pre>
 *
 * The nodes are the components of a system that are analyzed. For each trace
 * the involvement of the node is stored. A '1' denotes node involvement, a '0'
 * denotes no involvement of the node in the current execution trace. For each
 * execution trace we also know whether the execution was successful or not.
 *
 * Given this information, it is possible to use this spectra as input for
 * various fault localization techniques.
 *
 * @param <T>
 * type used to identify nodes in the system.
 */
public class HitSpectra<T> extends AbstractSpectra<T, HitTrace<T>> {

//	/**
//	 * Creates a new spectra.
//	 */
//	public HitSpectra() {
//		super();
//	}
	
	public HitSpectra(Path spectraZipFile) {
		super(spectraZipFile);
	}

	@Override
	protected HitTrace<T> createNewTrace(String identifier, int traceIndex, boolean successful) {
		return new HitTrace<>(this, identifier, traceIndex, successful);
	}

	/**
	 * Inverts involvements of nodes for successful and/or failing traces to the
	 * respective opposite. Returns a new Spectra object that has the required
	 * properties. This spectra is left unmodified. Node identifiers are shared
	 * between the two spectra objects, though.
	 * @param invertSuccessfulTraces
	 * whether to invert involvements of nodes in successful traces
	 * @param invertFailedTraces
	 * whether to invert involvements of nodes in failed traces
	 * @return a new spectra with inverted involvements
	 */
	public HitSpectra<T> createInvertedSpectra(boolean invertSuccessfulTraces, boolean invertFailedTraces) {
		return SpectraUtils.createInvertedSpectrum(this, invertSuccessfulTraces, invertFailedTraces);
	}

}
