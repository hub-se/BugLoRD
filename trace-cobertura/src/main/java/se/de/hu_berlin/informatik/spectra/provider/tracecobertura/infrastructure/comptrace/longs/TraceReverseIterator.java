package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.LevelState;

public class TraceReverseIterator implements ReplaceableCloneableIterator {
	
	private final EfficientCompressedLongTrace trace;
	private final LevelState[] levelStates;
	
	// level 0 is the lowest level
	private int currentLevel = 0;
	
	public TraceReverseIterator(EfficientCompressedLongTrace trace) {
		this.trace = trace;
		if (trace.getRepetitionMarkers() != null) {
			levelStates = new LevelState[trace.levelCount() + 1];
			for (int i = 0; i < trace.levelCount() + 1; ++i) {
				levelStates[i] = new LevelState(i);
			}
			for (int i = 1; i < levelStates.length; i++) {
				levelStates[i].indexState[0] = trace.getRepetitionMarkers()[i-1].traceSize() - 1;
//				System.out.println(i + ": " + levelStates[i].indexState[0]);
			}
//			levelStates[1].indexState[0] = trace.getCompressedTrace().size() - 1;
//			System.out.println(1 + ": " + levelStates[1].indexState[0]);
		} else {
			levelStates = new LevelState[] { new LevelState(0) };
		}
//		if (trace.getCompressedTrace().size() - 1 > Integer.MAX_VALUE) {
//			throw new IllegalStateException("Size of trace too large: " + trace.getCompressedTrace().size());
//		}
		levelStates[0].indexState[0] = trace.getCompressedTrace().size() - 1;
		resetCurrentLevel();
	}
	
	// clone constructor
	private TraceReverseIterator(TraceReverseIterator iterator) {
		this.trace = iterator.trace;
		levelStates = LevelState.copy(iterator.levelStates);
		this.currentLevel = iterator.currentLevel;
	}

	public TraceReverseIterator clone() {
		return new TraceReverseIterator(this);
	}

	@Override
	public boolean hasNext() {
		return levelStates[0].indexState[0] > -1;
	}

	@Override
	public long next() {
		if (currentLevel <= 0) {
			resetCurrentLevel();
//			System.out.println("s: 0, " + levelStates[0].indexState[0]);
			return trace.getCompressedTrace().get(levelStates[0].indexState[0]--);
		} else {
			// prioritize repetitions in parent 
			// (parent repetitions should be contained in child repetitions)
			if (levelStates[currentLevel].indexState[1] >= 0) {
				// inside of a repeated sequence
				if (levelStates[currentLevel].indexState[0] == levelStates[currentLevel].indexState[1] - levelStates[currentLevel].repetitionState[0] + 1) {
					// right at the end of the repeated sequence
					++levelStates[currentLevel].repetitionState[2];
					if (levelStates[currentLevel].repetitionState[2] < levelStates[currentLevel].repetitionState[1]) {
						// still an iteration to go
						long lastElementOfRepetition = peek();
						// reset to previous reset point
						LevelState.resetState(levelStates, currentLevel);
						resetCurrentLevel();
						return lastElementOfRepetition;
					} else {
						// no further iteration
						levelStates[currentLevel].indexState[1] = -1;
						--levelStates[currentLevel].indexState[0];
						--currentLevel;
						return next();
					}
				} else {
					// still inside of the repeated sequence
					--levelStates[currentLevel].indexState[0];
					--currentLevel;
					return next();
				}
			} else {
				// check if we are in a repeated sequence
				int[] repMarker = (levelStates[currentLevel].indexState[0] <= Integer.MAX_VALUE ? 
						trace.getRepetitionMarkers()[currentLevel-1].getBackwardsRepetitionMarkers().get((int) levelStates[currentLevel].indexState[0]) : null);
//				System.out.println("s: " + currentLevel + ", " + levelStates[currentLevel].indexState[0]);
				if (repMarker != null) {
//					System.out.println(currentLevel + ", " + levelStates[currentLevel].indexState[0]);
					// we are in a new repeated sequence!
					// [length, repeat_count]
					levelStates[currentLevel].indexState[1] = levelStates[currentLevel].indexState[0];
					levelStates[currentLevel].repetitionState[0] = repMarker[0];
					levelStates[currentLevel].repetitionState[1] = repMarker[1];
					levelStates[currentLevel].repetitionState[2] = 0;
					// set the reset point to this exact point
					LevelState.setResetPoint(levelStates, currentLevel);
					// stay on the same level!
					return next();
				} else {
					// not in a repeated sequence!
					--levelStates[currentLevel].indexState[0];
					--currentLevel;
					return next();
				}
			}
		}
	}
	
	private void resetCurrentLevel() {
		currentLevel = levelStates.length - 1;
	}

	public long peek() {
		return trace.getCompressedTrace().get(levelStates[0].indexState[0]);
	}

	public boolean isStartOfRepetition() {
		for (int level = levelStates.length - 1; level > 0; --level) {
			// check if we are in a repeated sequence
			if (levelStates[level].indexState[0] <= Integer.MAX_VALUE && 
					trace.getRepetitionMarkers()[level-1].getBackwardsRepetitionMarkers().containsKey((int) levelStates[level].indexState[0])) {
				return true;
			}
		}
		return false;
	}

	public boolean isEndOfRepetition() {
		for (int level = levelStates.length - 1; level > 0; --level) {
			// prioritize repetitions in parent 
			// (parent repetitions should be contained in child repetitions)
			if (levelStates[level].indexState[1] >= 0) {
				// inside of a repeated sequence
				if (levelStates[level].indexState[0] == levelStates[level].indexState[1] - levelStates[level].repetitionState[0] + 1) {
					// at the end of the repeated sequence
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public long processNextAndReplaceWithResult(Function<Long, Long> function) {
		if (currentLevel <= 0) {
			resetCurrentLevel();
			return trace.getCompressedTrace().getAndReplaceWith(levelStates[0].indexState[0]--, function);
		} else {
			// prioritize repetitions in parent 
			// (parent repetitions should be contained in child repetitions)
			if (levelStates[currentLevel].indexState[1] >= 0) {
				// inside of a repeated sequence
				if (levelStates[currentLevel].indexState[0] == levelStates[currentLevel].indexState[1] - levelStates[currentLevel].repetitionState[0] + 1) {
					// right at the end of the repeated sequence
					++levelStates[currentLevel].repetitionState[2];
					if (levelStates[currentLevel].repetitionState[2] < levelStates[currentLevel].repetitionState[1]) {
						// still an iteration to go
						long lastElementOfRepetition = peek();
						// reset to previous reset point
						LevelState.resetState(levelStates, currentLevel);
						resetCurrentLevel();
						return lastElementOfRepetition;
					} else {
						// no further iteration
						levelStates[currentLevel].indexState[1] = -1;
						--levelStates[currentLevel].indexState[0];
						--currentLevel;
						return next();
					}
				} else {
					// still inside of the repeated sequence
					--levelStates[currentLevel].indexState[0];
					--currentLevel;
					return next();
				}
			} else {
				// check if we are in a repeated sequence
				int[] repMarker = (levelStates[currentLevel].indexState[0] <= Integer.MAX_VALUE ? 
						trace.getRepetitionMarkers()[currentLevel-1].getBackwardsRepetitionMarkers().get((int) levelStates[currentLevel].indexState[0]) : null);
				if (repMarker != null) {
					// we are in a new repeated sequence!
					// [length, repeat_count]
					levelStates[currentLevel].indexState[1] = levelStates[currentLevel].indexState[0];
					levelStates[currentLevel].repetitionState[0] = repMarker[0];
					levelStates[currentLevel].repetitionState[1] = repMarker[1];
					levelStates[currentLevel].repetitionState[2] = 0;
					// set the reset point to this exact point
					LevelState.setResetPoint(levelStates, currentLevel);
					// stay on the same level!
					return next();
				} else {
					// not in a repeated sequence!
					--levelStates[currentLevel].indexState[0];
					--currentLevel;
					return next();
				}
			}
		}
	}

}