package se.de.hu_berlin.informatik.spectra.core.branch;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.util.CachedIntArrayMap;
import se.de.hu_berlin.informatik.spectra.util.CachedMap;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SubTraceSequencePool {

    public SubTraceSequencePool(Path tempOutputDir) {
        this.existingSubTraceSequences = new CachedIntArrayMap(tempOutputDir.resolve("subTraceIdSequences.zip"),
                0, SpectraFileUtils.SUB_TRACE_ID_SEQUENCES_DIR, true);
    }

    // maps start subtrace id to all sub trace sequences starting with that id
    Map<Integer, List<SingleLinkedIntArrayQueue>> subTraceStartToSequenceListMap = new HashMap<>();

    // maps existing sub trace sequences to unique integer IDs
    Map<SingleLinkedIntArrayQueue, Integer> subTraceSequenceToIdMap = new HashMap<>();

    // maps sub trace integer IDs to existing sub trace sequences
    private CachedMap<int[]> existingSubTraceSequences;

    private int currentId = 0;

    public int addSubTraceSequence(SingleLinkedIntArrayQueue subTraceSequenceToCheck) {
        List<SingleLinkedIntArrayQueue> existingSequences = subTraceStartToSequenceListMap.get(subTraceSequenceToCheck.element());

        if (existingSequences == null) {
            // definitely the first time seeing this sequence of subtraces
            existingSequences = new LinkedList<>();
            addToExistingSequences(subTraceSequenceToCheck, existingSequences);

            return currentId;
        } else {
            // check for equality for all possible sequences
            for (SingleLinkedIntArrayQueue sequence : existingSequences) {
                if (equal(sequence, subTraceSequenceToCheck)) {
                    // return the id for the exisiting sequence
                    return subTraceSequenceToIdMap.get(sequence);
                }
            }

            // if we get to here, we didn't find a matching sequence in the list
            addToExistingSequences(subTraceSequenceToCheck, existingSequences);
            return currentId;
        }
    }

    private void addToExistingSequences(SingleLinkedIntArrayQueue subTraceSequenceToCheck, List<SingleLinkedIntArrayQueue> existingSequences) {
        subTraceSequenceToCheck.trim();
        existingSequences.add(subTraceSequenceToCheck);
        subTraceStartToSequenceListMap.put(subTraceSequenceToCheck.element(), existingSequences);
        subTraceSequenceToIdMap.put(subTraceSequenceToCheck, ++currentId);

        int subTraceSequenceLength = subTraceSequenceToCheck.size();
        // using integer arrays
        int[] subTraceSequence = new int[subTraceSequenceLength];
        int j = 0;
        for (Integer id : subTraceSequenceToCheck) {
            // fill array
            subTraceSequence[j++] = id;
        }

        // add sub trace sequence to the list of existing sub trace sequences (together with the id)
        existingSubTraceSequences.put(currentId, subTraceSequence);
    }

    private boolean equal(SingleLinkedIntArrayQueue sequence,
                          SingleLinkedIntArrayQueue subTraceSequenceToCheck) {
        // check two sequences for equality
        if (sequence.size() != subTraceSequenceToCheck.size()) {
            return false;
        }

        SingleLinkedIntArrayQueue.MyIterator iterator = sequence.iterator2();
        SingleLinkedIntArrayQueue.MyIterator iterator2 = subTraceSequenceToCheck.iterator2();
        while (iterator.hasNext()) {
            if (iterator.nextNoAutoBoxing() != iterator2.nextNoAutoBoxing()) {
                return false;
            }
        }

        return true;
    }

    public int getID(SingleLinkedIntArrayQueue subTraceSequence) {
        Integer id = subTraceSequenceToIdMap.get(subTraceSequence);
        if (id == null) {
            id = addSubTraceSequence(subTraceSequence);
        }
        return id;
    }

    public CachedMap<int[]> getExistingSubTraceSequences() {
        return existingSubTraceSequences;
    }
}
