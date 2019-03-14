package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.util.ArrayList;
import java.util.List;

public class TraceIterator<T> implements CloneableRepetitionIterator<T> {
	
	private final CompressedTraceBase<T,?> trace;
	public final TraceIterator<T> childIterator;
	
	public int index = 0;
	private int repetitionIndex = -1;
	private int repetitionLength = 0;
	private int repetitionCount = 0;
	private int repetitionCounter = 0;

	private List<int[]> resetStateList;

	public TraceIterator(CompressedTraceBase<T,?> trace) {
		this.trace = trace;
		childIterator = (trace.getChild() == null ? null : new TraceIterator<>(trace.getChild()));
	}
	
	// clone constructor
	private TraceIterator(TraceIterator<T> iterator) {
		trace = iterator.trace;
		childIterator = iterator.childIterator == null ? null : 
			iterator.childIterator.clone();
		index = iterator.index;
		repetitionIndex = iterator.repetitionIndex;
		repetitionLength = iterator.repetitionLength;
		repetitionCount = iterator.repetitionCount;
		repetitionCounter = iterator.repetitionCounter;
		resetStateList = iterator.resetStateList == null ? null : 
			new ArrayList<>(iterator.resetStateList);
	}

	public TraceIterator<T> clone() {
		return new TraceIterator<>(this);
	}

	private void setResetPoint() {
		resetStateList = new ArrayList<>();
		getAndStoreState(resetStateList);
	}

	private void getAndStoreState(List<int[]> resetIndexList2) {
		if (childIterator != null) {
			resetIndexList2.add(new int[] {
					childIterator.index, 
					childIterator.repetitionIndex,
					childIterator.repetitionLength,
					childIterator.repetitionCount,
					childIterator.repetitionCounter});
			childIterator.getAndStoreState(resetIndexList2);
		}
	}

	private void resetState() {
		this.index = repetitionIndex;
		if (childIterator != null) {
			childIterator.setState(resetStateList, 0);
		}
	}

	private void setState(List<int[]> indexList, int index) {
		this.index = indexList.get(index)[0];
		this.repetitionIndex = indexList.get(index)[1];
		this.repetitionLength = indexList.get(index)[2];
		this.repetitionCount = indexList.get(index)[3];
		this.repetitionCounter = indexList.get(index)[4];
		if (childIterator != null) {
			childIterator.setState(indexList, ++index);
		}
	}

	@Override
	public boolean hasNext() {
		if (childIterator == null) {
			return index < trace.getCompressedTrace().size();
		} else {
			return childIterator.hasNext();
		}
	}

	@Override
	public T next() {
		if (childIterator == null) {
			return trace.getCompressedTrace().get(index++);
		} else {
			// prioritize repetitions in parent 
			// (parent repetitions should be contained in child repetitions)
			if (repetitionIndex >= 0) {
				// inside of a repeated sequence
				if (index == repetitionIndex + repetitionLength - 1) {
					// right at the end of the repeated sequence
					++repetitionCounter;
					if (repetitionCounter < repetitionCount) {
						// still an iteration to go
						T lastElementOfRepetition = childIterator.peek();
						// reset to previous reset point
						resetState();
						return lastElementOfRepetition;
					} else {
						// no further iteration
						repetitionIndex = -1;
						++index;
						return childIterator.next();
					}
				} else {
					// still inside of the repeated sequence
					++index;
					return childIterator.next();
				}
			} else {
				// check if we are in a repeated sequence
				int[] repMarker = trace.getRepetitionMarkers().get(index);
				if (repMarker != null) {
					// we are in a new repeated sequence!
					// [length, repeat_count]
					repetitionIndex = index;
					repetitionLength = repMarker[0];
					repetitionCount = repMarker[1];
					repetitionCounter = 0;
					// set the reset point to this exact point
					setResetPoint();
					return next();
				} else {
					// not in a repeated sequence!
					++index;
					return childIterator.next();
				}
			}
		}
	}

	public T peek() {
		if (childIterator == null) {
			return trace.getCompressedTrace().get(index);
		} else {
			return childIterator.peek();
		}
	}

	@Override
	public boolean isStartOfRepetition() {
		if (childIterator == null) {
			return false;
		} else {
			// check if we are in a repeated sequence
			if (trace.getRepetitionMarkers().containsKey(index)) {
				return true;
			} else {
				return childIterator.isStartOfRepetition();
			}
		}
	}

	@Override
	public boolean isEndOfRepetition() {
		if (childIterator == null) {
			return false;
		} else {
			// prioritize repetitions in parent 
			// (parent repetitions should be contained in child repetitions)
			if (repetitionIndex >= 0) {
				// inside of a repeated sequence
				if (index == repetitionIndex + repetitionLength - 1) {
					// at the end of the repeated sequence
					return true;
				}
			}
			return childIterator.isEndOfRepetition();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}