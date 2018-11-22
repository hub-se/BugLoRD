package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

public class ArrayIterator<T> implements CloneableIterator<T> {
	
	private T[] array;
	private int index = 0;

	public ArrayIterator(T[] array) {
		this.array = array;
	}
	
	// clone constructor
	private ArrayIterator(ArrayIterator<T> iterator) {
		array = iterator.array;
		index = iterator.index;
	}

	public ArrayIterator<T> clone() {
		return new ArrayIterator<>(this);
	}

	@Override
	public boolean hasNext() {
		return index < array.length;
	}

	@Override
	public T next() {
		return array[index++];
	}
	
	public T peek() {
		return array[index];
	}

}