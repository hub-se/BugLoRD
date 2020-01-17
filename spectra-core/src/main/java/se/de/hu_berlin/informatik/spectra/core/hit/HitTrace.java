/*
 * This file is part of the "STARDUST" project. (c) Fabian Keller
 * <hello@fabian-keller.de> For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.spectra.core.hit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.zip.ZipException;

import se.de.hu_berlin.informatik.spectra.core.INode;
import se.de.hu_berlin.informatik.spectra.core.ISpectra;
import se.de.hu_berlin.informatik.spectra.core.ITrace;
import se.de.hu_berlin.informatik.spectra.core.traces.ExecutionTrace;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * This class represents a single execution trace and its success state.
 *
 * @author Fabian Keller 'dev@fabian-keller.de'
 *
 * @param <T>
 * type used to identify nodes in the system.
 */
public class HitTrace<T> implements ITrace<T> {

	/** Holds the success state of this trace */
	private final boolean successful;

	/** Holds the identifier (test case name) of this trace */
	private final String identifier;
	private final int index;

	/** Holds the spectra this trace belongs to */
	protected final ISpectra<T, ?> spectra;

	/**
	 * Stores the involvement of all nodes for this trace. Use
	 * {@link HitSpectra#getNodes()} to get all nodes.
	 */
	private final Set<Integer> involvement = new HashSet<>();
	
	/**
	 * Holds all execution traces for all threads separately. (Lists of node IDs)
	 */
	private Collection<ExecutionTrace> executionTraces;
	
	/**
	 * Create a trace for a spectra.
	 * @param spectra
	 * the spectra that the trace belongs to
	 * @param identifier
	 * the identifier of the trace (usually the test case name)
	 * @param traceIndex
	 * the integer index of the trace
	 * @param successful
	 * true if the trace originates from a successful execution, false otherwise
	 */
	protected HitTrace(final ISpectra<T, ?> spectra, final String identifier, 
			final int traceIndex, final boolean successful) {
		this.successful = successful;
		this.spectra = Objects.requireNonNull(spectra);
		this.identifier = Objects.requireNonNull(identifier);
		this.index = traceIndex;
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
		if (node == null) {
			return;
		}
		if (involved) {
			if (involvement.add(node.getIndex())) {
				node.invalidateCachedValues();
			}
		} else if (involvement.contains(node.getIndex())) {
			involvement.remove(node.getIndex());
			node.invalidateCachedValues();
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void setInvolvement(final int index, final boolean involved) {
		setInvolvement(spectra.getNode(index), involved);
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
		if (node != null) {
			return involvement.contains(node.getIndex());
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isInvolved(final T identifier) {
		return isInvolved(spectra.getNode(identifier));
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isInvolved(final int index) {
		return involvement.contains(index);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}
	
	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public int involvedNodesCount() {
		return involvement.size();
	}

	@Override
	public Collection<Integer> getInvolvedNodes() {
		return involvement;
	}

	@Override
	public int hashCode() {
		// equality of traces is bound to identifiers
		return getIdentifier().hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HitTrace) {
			HitTrace<?> oTrace = (HitTrace<?>) obj;
			if (!this.getIdentifier().equals(oTrace.getIdentifier()) || 
					this.involvedNodesCount() != oTrace.involvedNodesCount()) {
				return false;
			}
			try {
				// check whether the same nodes are involved in the trace
				for (int nodeID : oTrace.getInvolvedNodes()) {
					INode<T> node = this.spectra.getNode((T) oTrace.spectra.getNode(nodeID).getIdentifier());
					if (!this.getInvolvedNodes().contains(node.getIndex())) {
						return false;
					}
				}
			} catch (ClassCastException e) {
				return false;
			}
			return true;
		}
		return false;
	}
	 
	private static File tmpOutputDir = null;
	
	private static File getTmpDir(Path alternatePath) {
		if (tmpOutputDir == null) {
			tmpOutputDir = SpectraFileUtils.getTemporaryOutputDir("executionTraces_tmp", alternatePath);
		}
		return tmpOutputDir;
	}

	@Override
	public Collection<ExecutionTrace> getExecutionTraces() {
		// try to load execution traces directly from zip file, if possible (do not store them in memory)
		if (executionTraces == null && spectra.getPathToSpectraZipFile() != null) {
			ZipFileWrapper zip = ZipFileWrapper.getZipFileWrapper(spectra.getPathToSpectraZipFile());
			try {
				return SpectraFileUtils.loadExecutionTraces(getTmpDir(
						zip.getzipFilePath().getParent().resolve("execTraceTemp").toAbsolutePath()), 
						zip, this.getIndex());
			} catch (ZipException e) {
				Log.abort(this, e, "Could not get execution traces from spectra zip file.");
			}
		} else if (executionTraces == null && spectra.getRawTraceCollector() != null) {
			List<ExecutionTrace> traces = null;
			try {
				traces = spectra.getRawTraceCollector().calculateExecutionTraces(this.getIndex(), false);
			} catch (ZipException e) {
				Log.abort(this, e, "Could not get execution traces from raw trace collector.");
			}
			return traces == null ? Collections.emptyList() : traces;
		}
		// may be null
		return executionTraces == null ? Collections.emptyList() : executionTraces;
	}
	
//	@Override
//	public Collection<byte[]> getExecutionTracesByteArrays() {
//		Collection<byte[]> eTraces = null;
//		// try to load execution traces directly from zip file, if possible (do not store them in memory)
//		if (executionTraces == null && spectra.getPathToSpectraZipFile() != null) {
//			ZipFileWrapper zip = new ZipFileReader().submit(spectra.getPathToSpectraZipFile()).getResult();
//			return SpectraFileUtils.loadExecutionTracesByteArrays(zip, this.getIndex());
//		} else if (spectra.getRawTraceCollector() != null) {
//			eTraces = spectra.getRawTraceCollector().getExecutionTracesByteArrays(this.getIndex(), false);
//		} else if (executionTraces != null) {
//			eTraces = new ArrayList<>(executionTraces.size());
//			for (ExecutionTrace executionTrace : executionTraces) {
//				eTraces.add(SpectraFileUtils.storeAsByteArray(executionTrace));
//			}
//		}
//		// may be null
//		return eTraces == null ? Collections.emptyList() : eTraces;
//	}
	
	@Override
	public boolean storeExecutionTracesInZipFile(Path outputFile, Supplier<String> traceFileNameSupplier,
			Supplier<String> repMarkerFileNameSupplier) {
		if (executionTraces == null && spectra.getPathToSpectraZipFile() != null) {
			if (spectra.getPathToSpectraZipFile().toAbsolutePath().equals(outputFile.toAbsolutePath())) {
				// storing execution traces in the same zip file?
				Log.abort(this, "Trying to move execution traces to the same zip file...");
				return false;
			}
			ZipFileWrapper zip = ZipFileWrapper.getZipFileWrapper(spectra.getPathToSpectraZipFile());
			try {
				return SpectraFileUtils.moveExecutionTraces(zip, this.getIndex(), 
						outputFile, traceFileNameSupplier, repMarkerFileNameSupplier);
			} catch (ZipException e) {
				Log.abort(this, e, "Could not move execution traces.");
			}
		} else if (spectra.getRawTraceCollector() != null) {
			try {
				return spectra.getRawTraceCollector().moveExecutionTraces(this.getIndex(), 
						outputFile, traceFileNameSupplier, repMarkerFileNameSupplier);
			} catch (ZipException e) {
				e.printStackTrace();
			}
		} else if (executionTraces != null) {
			if (executionTraces.isEmpty()) {
				return false;
			}
			for (ExecutionTrace executionTrace : executionTraces) {
				try {
					SpectraFileUtils.storeCompressedIntegerTrace(executionTrace, outputFile, traceFileNameSupplier.get(), repMarkerFileNameSupplier.get(), true);
				} catch (IOException e) {
					Log.abort(this, e, "Trying to store execution traces in zip file failed.");
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void addExecutionTrace(ExecutionTrace executionTrace) {
		if (executionTraces == null) {
			executionTraces = new ArrayList<>(1);
		}
		executionTraces.add(executionTrace);
	}

	@Override
	public void sleep() {
		if (executionTraces != null) {
			for (ExecutionTrace trace : executionTraces) {
				trace.sleep();
			}
		}
	}

}
