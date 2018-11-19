package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

public class ArrayIterator implements CloneableIterator<Integer> {
	
	private int[] array;
	private int index = 0;

	public ArrayIterator(int[] array) {
		this.array = array;
	}
	
	// clone constructor
	private ArrayIterator(ArrayIterator iterator) {
		array = iterator.array;
		index = iterator.index;
	}

	public ArrayIterator clone() {
		return new ArrayIterator(this);
	}

	@Override
	public boolean hasNext() {
		return index < array.length;
	}

	@Override
	public Integer next() {
		return Integer.valueOf(array[index++]);
	}
	
	public Integer peek() {
		return Integer.valueOf(array[index]);
	}

}