package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.integer.ReplaceableCloneableIntIterator;

public class IntArrayIterator implements ReplaceableCloneableIntIterator {
	
	private final int[] array;
	private int index = 0;

	public IntArrayIterator(int[] array) {
		this.array = array;
	}
	
	// clone constructor
	private IntArrayIterator(IntArrayIterator iterator) {
		array = iterator.array;
		index = iterator.index;
	}

	public IntArrayIterator clone() {
		return new IntArrayIterator(this);
	}

	@Override
	public boolean hasNext() {
		return index < array.length;
	}

	@Override
	public int next() {
		return array[index++];
	}
	
	public int peek() {
		return array[index];
	}

	@Override
	public int processNextAndReplaceWithResult(Function<Integer, Integer> function) {
		throw new UnsupportedOperationException();
	}

}