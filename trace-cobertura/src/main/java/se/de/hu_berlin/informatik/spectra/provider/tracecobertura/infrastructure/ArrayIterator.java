package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

@CoverageIgnore
public class ArrayIterator<T> implements CloneableIterator<T> {

    private final T[] array;
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

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}