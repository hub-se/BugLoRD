package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

@CoverageIgnore
public class IntegerArrayIterator implements CloneableIterator<Integer> {

    private final Integer[] array;
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