package se.de.hu_berlin.informatik.spectra.core.branch;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.BufferedMap;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue;
import se.de.hu_berlin.informatik.spectra.util.CachedIntArrayMap;
import se.de.hu_berlin.informatik.spectra.util.CachedMap;
import se.de.hu_berlin.informatik.spectra.util.SpectraFileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class SubTraceSequencePool {

    private static final int SEQUENCE_POOL_SUB_MAP_SIZE = 50000;

	private Path tempOutputDir;

	public SubTraceSequencePool(Path tempOutputDir) {
        this.tempOutputDir = tempOutputDir;
		this.subTraceStartToSequenceTreeMap = new BufferedMap<SimpleIntGSArrayTree>(
				tempOutputDir.resolve(SpectraFileUtils.SUB_TRACE_ID_SEQUENCE_TREES_DIR).toFile(), 
				"stsPool", SEQUENCE_POOL_SUB_MAP_SIZE, true);
    }

    // maps start subtrace id to all sub trace sequences starting with that id
	private BufferedMap<SimpleIntGSArrayTree> subTraceStartToSequenceTreeMap;

    // maps sub trace integer IDs to existing sub trace sequences
    private CachedMap<int[]> existingSubTraceSequences;

    // used to generate unique IDs for the sequences (id 0 is reserved for BAD_INDEX here!)
    private AtomicInteger idGenerator = new AtomicInteger(1);
    int maxId = 0;

    public int addSubTraceSequence(SingleLinkedIntArrayQueue subTraceSequenceToCheck) {
    	SimpleIntGSArrayTree existingSequences = subTraceStartToSequenceTreeMap.get(subTraceSequenceToCheck.element());

        if (existingSequences == null) {
            // definitely the first time seeing this sequence of subtraces
            existingSequences = new SimpleIntGSArrayTree();
            subTraceStartToSequenceTreeMap.put(subTraceSequenceToCheck.element(), existingSequences);
        }

        int index = existingSequences.addSequence(idGenerator, subTraceSequenceToCheck);
        if (index > maxId) {
        	subTraceStartToSequenceTreeMap.setLastObtainedNodeToModified();
        	maxId = index;
        }

        return index;
    }

    public CachedMap<int[]> getExistingSubTraceSequences() {
    	if (this.existingSubTraceSequences == null) {
    		this.existingSubTraceSequences = new CachedIntArrayMap(
    				tempOutputDir.resolve("subTraceIdSequences.zip"), 0, 
    				SpectraFileUtils.SUB_TRACE_ID_SEQUENCES_DIR, true);
    		System.err.println("stsPool stats: " + subTraceStartToSequenceTreeMap.getStats());
    		
    		// move from GS trees to int arrays
    		Iterator<Entry<Integer, SimpleIntGSArrayTree>> iterator = subTraceStartToSequenceTreeMap.entrySetIterator();
    		while (iterator.hasNext()) {
    			SimpleIntGSArrayTree tree = iterator.next().getValue();
    			for (Pair<Integer, int[]> pair : tree) {
    				this.existingSubTraceSequences.put(pair.first(), pair.second());
    			}
    		}
    		this.subTraceStartToSequenceTreeMap.clear();
    	}
        return existingSubTraceSequences;
    }
}
