package se.de.hu_berlin.informatik.spectra.core.branch;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.util.CachedIntArrayMap;
import se.de.hu_berlin.informatik.spectra.util.CachedMap;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SubTraceSequencePool {

    public SubTraceSequencePool(Path tempOutputDir) {
        this.existingSubTraceSequences = new CachedIntArrayMap(tempOutputDir.resolve("subTraceIdSequences.zip"),
                0, SpectraFileUtils.SUB_TRACE_ID_SEQUENCES_DIR, true);
    }

    // maps start subtrace id to all sub trace sequences starting with that id
    Map<Integer, SimpleIntGSArrayTree> subTraceStartToSequenceTreeMap = new HashMap<>();

    // maps sub trace integer IDs to existing sub trace sequences
    private CachedMap<int[]> existingSubTraceSequences;

    // used to generate unique IDs for the sequences
    private AtomicInteger idGenerator = new AtomicInteger(0);

    public int addSubTraceSequence(SingleLinkedIntArrayQueue subTraceSequenceToCheck) {
    	SimpleIntGSArrayTree existingSequences = subTraceStartToSequenceTreeMap.get(subTraceSequenceToCheck.element());

        if (existingSequences == null) {
            // definitely the first time seeing this sequence of subtraces
            existingSequences = new SimpleIntGSArrayTree(idGenerator);
            subTraceStartToSequenceTreeMap.put(subTraceSequenceToCheck.element(), existingSequences);
        }
        
        return addToExistingSequences(subTraceSequenceToCheck, existingSequences);
    }

    private int addToExistingSequences(SingleLinkedIntArrayQueue subTraceSequenceToCheck, SimpleIntGSArrayTree existingSequences) {
//    	subTraceSequenceToCheck.trim();
    	int index = existingSequences.addSequence(subTraceSequenceToCheck);
//    	subTraceSequenceToIdMap.put(subTraceSequenceToCheck, index);

    	// first time seeing this sequence?
    	if (!existingSubTraceSequences.containsKey(index)) {
    		int subTraceSequenceLength = subTraceSequenceToCheck.size();
    		// using integer arrays
    		int[] subTraceSequence = new int[subTraceSequenceLength];
    		int j = 0;
    		for (Integer id : subTraceSequenceToCheck) {
    			// fill array
    			subTraceSequence[j++] = id;
    		}

    		// add sub trace sequence to the list of existing sub trace sequences (together with the id)
    		existingSubTraceSequences.put(index, subTraceSequence);
    	}
    	return index;
    }

    public CachedMap<int[]> getExistingSubTraceSequences() {
        return existingSubTraceSequences;
    }
}
