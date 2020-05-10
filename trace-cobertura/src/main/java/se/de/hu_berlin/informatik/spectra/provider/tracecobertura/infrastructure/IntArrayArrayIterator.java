package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

public class IntArrayArrayIterator implements CloneableIterator<int[]> {

    private final int[][] array;
    private int index = 0;

    public IntArrayArrayIterator(int[][] array) {
        this.array = array;
    }

    // clone constructor
    private IntArrayArrayIterator(IntArrayArrayIterator iterator) {
        array = iterator.array;
        index = iterator.index;
    }

    public IntArrayArrayIterator clone() {
        return new IntArrayArrayIterator(this);
    }

    @Override
    public boolean hasNext() {
        return index < array.length;
    }

    @Override
    public int[] next() {
        return array[index++];
    }

    public int[] peek() {
        return array[index];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}