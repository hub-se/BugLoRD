package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

public class IntArrayIterator implements CloneableIterator<Integer> {
	
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
	public Integer next() {
		return array[index++];
	}
	
	public Integer peek() {
		return array[index];
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}