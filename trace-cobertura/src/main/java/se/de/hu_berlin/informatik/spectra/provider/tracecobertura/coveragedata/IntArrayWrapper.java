package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.util.Arrays;
import java.util.Objects;

public class IntArrayWrapper {

	private final int[] array;
	private final int hash;
	
	public IntArrayWrapper(int[] array) {
		this.array = Objects.requireNonNull(array);
		
		int hash = 31 * (17 + array.length);
		for (int i = 0; i < array.length; ++i) {
			hash = 31 * hash + array[i];
		}
		this.hash = hash;
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
		return hash;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(array);
	}
}
