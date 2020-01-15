package se.de.hu_berlin.informatik.spectra.core.traces;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedMap;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.EfficientCompressedIntegerTrace;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.TraceIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.TraceReverseIterator;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs.EfficientCompressedLongTrace;
import se.de.hu_berlin.informatik.spectra.util.CompressedByteArrayToRepetitionMarkerMapListProcessor;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.compression.ziputils.ZipFileWrapper;

/**
 * An execution trace consists structurally of a list of executed nodes (or references to node lists)
 * and a list of tuples that mark repeated sequences in the trace.
 *
 */
public class ExecutionTrace extends EfficientCompressedIntegerTrace implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -1586811553594468248L;
	
	private String repetitionFile;
	private ZipFileWrapper zipFileWrapper;
	private File tmpOutputDir;

	public ExecutionTrace(BufferedIntArrayQueue trace, boolean log) {
		super(trace, log);
	}
	
	public ExecutionTrace(BufferedIntArrayQueue trace, EfficientCompressedIntegerTrace otherCompressedTrace) {
		super(trace, otherCompressedTrace);
	}
	
	public ExecutionTrace(BufferedIntArrayQueue trace, EfficientCompressedLongTrace otherCompressedTrace) {
		super(trace, otherCompressedTrace);
	}

	public ExecutionTrace(BufferedIntArrayQueue compressedTrace, List<BufferedMap<int[]>> repMarkerLists, boolean log) {
		super(compressedTrace, repMarkerLists, log);
	}
	

	public ExecutionTrace(File outputDir, String prefix, int nodeSize, int mapSize, boolean deleteOnExit) {
		super(outputDir, prefix, nodeSize, mapSize, deleteOnExit);
	}

	public ExecutionTrace(BufferedIntArrayQueue compressedTrace, ZipFileWrapper zipFileWrapper, File tmpOutputDir,
			String repetitionFile, boolean log) {
		super(compressedTrace, null, log);
		// load the repetition marker maps later, if they are needed...
		this.zipFileWrapper = zipFileWrapper;
		this.tmpOutputDir = tmpOutputDir;
		this.repetitionFile = repetitionFile;
	}
	
	private void loadRepetitionMarkerMap() {
		// lazily load repetition markers, if necessary;
		// will not be necessary, if we only want to iterate over the compressed base trace
		if (zipFileWrapper != null) {
			CompressedByteArrayToRepetitionMarkerMapListProcessor repProcessor = 
					new CompressedByteArrayToRepetitionMarkerMapListProcessor(zipFileWrapper, true, new SpectraFileUtils.MapSupplier(tmpOutputDir));
			List<BufferedMap<int[]>> repetitionMarkers = repProcessor.submit(repetitionFile).getResult();
			setRepetitionMarkers(repetitionMarkers);
			zipFileWrapper = null;
		}
	}

	/**
	 * Constructs the full execution trace. Usually, you should NOT be using this. Use an iterator instead!
	 * @param indexer
	 * indexer that is used to connect the element IDs in the execution trace to the respective sub traces
	 * that contain node IDs
	 * @return
	 * array that contains all executed node IDs
	 */
	public int[] reconstructFullMappedTrace(SequenceIndexerCompressed indexer) {
		TraceIterator indexedFullTrace = iterator();
		List<Integer> fullTrace = new ArrayList<>();
		while (indexedFullTrace.hasNext()) {
			Iterator<Integer> sequence = indexer.getFullSequenceIterator(indexedFullTrace.next());
			while (sequence.hasNext()) {
				fullTrace.add(sequence.next());
			}
		}
		return fullTrace.stream().mapToInt(i -> i).toArray();
	}
	
	/**
	 * iterates over all node IDs in the execution trace.
	 * @param sequenceIndexer
	 * indexer that is used to connect the element IDs in the execution trace to the respective sub traces
	 * that contain node IDs
	 * @return
	 * iterator
	 */
	public Iterator<Integer> mappedIterator(SequenceIndexerCompressed sequenceIndexer) {
		return new Iterator<Integer>(){
			
			final TraceIterator iterator = ExecutionTrace.this.iterator();
			Iterator<Integer> currentSequence;

			@Override
			public boolean hasNext() {
				if (currentSequence == null || !currentSequence.hasNext()) {
					currentSequence = null;
					while (iterator.hasNext()) {
						currentSequence = sequenceIndexer.getFullSequenceIterator(iterator.next());
						if (currentSequence.hasNext()) {
							// found a "good" sequence
							break;
						}
						currentSequence = null;
					}
					
					// found no sequence?
                    return currentSequence != null;
				}
				
				return true;
			}

			@Override
			public Integer next() {
				return currentSequence.next();
			}};
	}
	
	/**
	 * iterates over all node IDs in the execution trace, starting from the end of the trace.
	 * @param sequenceIndexer
	 * indexer that is used to connect the element IDs in the execution trace to the respective sub traces
	 * that contain node IDs
	 * @return
	 * iterator
	 */
	public Iterator<Integer> mappedReverseIterator(SequenceIndexerCompressed sequenceIndexer) {
		return new Iterator<Integer>(){
			
			final TraceReverseIterator iterator = ExecutionTrace.this.reverseIterator();
			Iterator<Integer> currentSequence;

			@Override
			public boolean hasNext() {
				if (currentSequence == null || !currentSequence.hasNext()) {
					currentSequence = null;
					while (iterator.hasNext()) {
						currentSequence = sequenceIndexer.getFullSequenceReverseIterator(iterator.next());
						if (currentSequence.hasNext()) {
							// found a "good" sequence
							break;
						}
						currentSequence = null;
					}
					
					// found no sequence?
                    return currentSequence != null;
				}
				
				return true;
			}

			@Override
			public Integer next() {
				return currentSequence.next();
			}};
	}
	
	@Override
	public TraceIterator iterator() {
		loadRepetitionMarkerMap();
		return super.iterator();
	}
	
	@Override
	public TraceReverseIterator reverseIterator() {
		loadRepetitionMarkerMap();
		return super.reverseIterator();
	}
}
