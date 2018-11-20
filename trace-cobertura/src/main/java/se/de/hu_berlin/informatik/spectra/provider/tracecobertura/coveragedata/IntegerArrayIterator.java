package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

public class IntegerArrayIterator implements CloneableIterator<Integer> {
	
	private Integer[] array;
	private int index = 0;

	public IntegerArrayIterator(Integer[] array) {
		this.array = array;
	}
	
	// clone constructor
	private IntegerArrayIterator(IntegerArrayIterator iterator) {
		array = iterator.array;
		index = iterator.index;
	}

	public IntegerArrayIterator clone() {
		return new IntegerArrayIterator(this);
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