package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * We assume that a sub trace is uniquely determined by 
 * looking at its first and last statement.
 * 
 * We need the last statement for sequences that differ
 * without a statement that marks a decision like,
 * for example, for code that sometimes throws an exception
 * and sometimes doesn't.
 * 
 * If this wrapper is not temporary, i.e. is stored in a map
 * or such, then the internal array can be replaced by a
 * simplified array that only stores the first and last
 * statement.
 */
public class SubTraceIntArrayWrapper {

	private List<int[]> subTrace;
	private int[] array = null;

	public SubTraceIntArrayWrapper(List<int[]> subTrace) {
		this.subTrace = Objects.requireNonNull(subTrace);
	}
	
	/**
	 * Should be called if this wrapper is not discarded.
	 * (When used as a key in a map, for example.)
	 */
	public void simplify() {
		if (subTrace != null) {
			int size = this.subTrace.size();
			if (subTrace.isEmpty()) {
				array = new int[] {};
			} else if (size == 1) {
				array = new int[] {subTrace.get(0)[0], subTrace.get(0)[1]};
			} else {
				array = new int[] {subTrace.get(0)[0], subTrace.get(0)[1], 
						subTrace.get(size-1)[0], subTrace.get(size-1)[1]};
			}
			// release the pointer to the actual sub trace
			subTrace = null;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof SubTraceIntArrayWrapper) {
			SubTraceIntArrayWrapper o = (SubTraceIntArrayWrapper) obj;
			if (this.subTrace != null) {
				// this: only sub trace stored
				if (o.subTrace != null) {
					// other: only sub trace stored
					if (o.subTrace.size() != this.subTrace.size()) {
						return false;
					}
					int size = this.subTrace.size();
					if (size > 0) {
						if (this.subTrace.get(0)[0] != o.subTrace.get(0)[0] ||
								this.subTrace.get(0)[1] != o.subTrace.get(0)[1]) {
							return false;
						}
						if (size > 1) {
							if (this.subTrace.get(size-1)[0] != o.subTrace.get(size-1)[0] ||
									this.subTrace.get(size-1)[1] != o.subTrace.get(size-1)[1]) {
								return false;
							}
						}
					}
				} else {
					// other: simplified -> only array stored
					int size = this.subTrace.size();
					if (size > 0) {
						if (this.subTrace.get(0)[0] != o.array[0] ||
								this.subTrace.get(0)[1] != o.array[1]) {
							return false;
						}
						if (size > 1) {
							if (o.array.length < 4) {
								return false;
							}
							if (this.subTrace.get(size-1)[0] != o.array[2] ||
									this.subTrace.get(size-1)[1] != o.array[3]) {
								return false;
							}
						}
					} else if (o.array.length > 0) {
						return false;
					}
				}
			} else {
				// this: simplified -> only array stored
				if (o.subTrace != null) {
					// other: only sub trace stored
					int size = o.subTrace.size();
					if (size > 0) {
						if (o.subTrace.get(0)[0] != this.array[0] ||
								o.subTrace.get(0)[1] != this.array[1]) {
							return false;
						}
						if (size > 1) {
							if (o.array.length < 4) {
								return false;
							}
							if (o.subTrace.get(size-1)[0] != this.array[2] ||
									o.subTrace.get(size-1)[1] != this.array[3]) {
								return false;
							}
						}
					} else if (this.array.length > 0) {
						return false;
					}
				} else {
					// other: simplified -> only array stored
					if (o.array.length != this.array.length) {
						return false;
					}
					for (int i = 0; i < array.length; ++i) {
						if (this.array[i] != o.array[i]) {
							return false;
						}
					}
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (array != null) {
			int hash = 31 * (17 + array.length);
			for (int i1 : array) {
				hash = 31 * hash + i1;
			}
			return hash;
		} else {
			int size = this.subTrace.size();
			if (subTrace.isEmpty()) {
				return 527; // = 31 * 17
			} else if (size == 1) {
				int hash = 31 * (17 + 2);
				hash = 31 * hash + subTrace.get(0)[0];
				hash = 31 * hash + subTrace.get(0)[1];
				return hash;
			} else {
				int hash = 31 * (17 + 2);
				hash = 31 * hash + subTrace.get(0)[0];
				hash = 31 * hash + subTrace.get(0)[1];
				hash = 31 * hash + subTrace.get(size-1)[0];
				hash = 31 * hash + subTrace.get(size-1)[1];
				return hash;
			}
		}
	}
	
	@Override
	public String toString() {
		return Arrays.toString(array);
	}
}
