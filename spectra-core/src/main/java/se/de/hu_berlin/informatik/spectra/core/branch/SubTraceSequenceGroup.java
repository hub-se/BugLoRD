package se.de.hu_berlin.informatik.spectra.core.branch;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.SingleLinkedIntArrayQueue;

public class SubTraceSequenceGroup {

	SingleLinkedIntArrayQueue subTraceSequence;
	
	public SubTraceSequenceGroup(SingleLinkedIntArrayQueue subTraceSequence) {
		this.subTraceSequence = subTraceSequence;
		
	}
	
}
