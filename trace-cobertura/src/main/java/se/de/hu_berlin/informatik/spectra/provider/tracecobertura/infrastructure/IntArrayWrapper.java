package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure;

import java.util.Arrays;
import java.util.Objects;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

@CoverageIgnore
public class IntArrayWrapper {

    private final int[] array;

    public IntArrayWrapper(int[] array) {
        this.array = Objects.requireNonNull(array);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof IntArrayWrapper) {
            IntArrayWrapper o = (IntArrayWrapper) obj;
            if (o.array.length != this.array.length) {
                return false;
            }
            for (int i = 0; i < array.length; ++i) {
                if (this.array[i] != o.array[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 31 * (17 + array.length);
        for (int i1 : array) {
            hash = 31 * hash + i1;
        }
        return hash;
    }

    @Override
    public String toString() {
        return Arrays.toString(array);
    }
}
