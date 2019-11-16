package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.LevelState;

public class LongTraceReverseIterator implements ReplaceableCloneableLongIterator {
	
	private final EfficientCompressedLongTrace trace;
	private final LevelState[] levelStates;
	
	// level 0 is the lowest level
	private int currentLevel = 0;
	
	public LongTraceReverseIterator(EfficientCompressedLongTrace trace) {
		this.trace = trace;
		if (trace.getRepetitionMarkers() != null) {
			levelStates = new LevelState[trace.getRepetitionMarkers().size() + 1];
			for (int i = 0; i < trace.getRepetitionMarkers().size() + 1; ++i) {
				levelStates[i] = new LevelState();
			}
			for (int i = 1; i < levelStates.length; i++) {
				levelStates[i].index = trace.getRepetitionMarkers().get(i-1).traceSize() - 1;
//				System.out.println(i + ": " + levelStates[i].index);
			}
//			levelStates[1].index = trace.getCompressedTrace().size() - 1;
//			System.out.println(1 + ": " + levelStates[1].index);
		} else {
			levelStates = new LevelState[] { new LevelState() };
		}
		levelStates[0].index = trace.getCompressedTrace().size() - 1;
		resetCurrentLevel();
	}
	
	// clone constructor
	private LongTraceReverseIterator(LongTraceReverseIterator iterator) {
		this.trace = iterator.trace;
		levelStates = LevelState.copy(iterator.levelStates);
		this.currentLevel = iterator.currentLevel;
	}

	public LongTraceReverseIterator clone() {
		return new LongTraceReverseIterator(this);
	}

	@Override
	public boolean hasNext() {
		return levelStates[0].index > -1;
	}

	@Override
	public long next() {
		if (currentLevel <= 0) {
			resetCurrentLevel();
//			System.out.println("s: 0, " + levelStates[0].index);
			return trace.getCompressedTrace().get(levelStates[0].index--);
		} else {
			// prioritize repetitions in parent 
			// (parent repetitions should be contained in child repetitions)
			if (levelStates[currentLevel].repetitionIndex >= 0) {
				// inside of a repeated sequence
				if (levelStates[currentLevel].index == levelStates[currentLevel].repetitionIndex - levelStates[currentLevel].repetitionLength + 1) {
					// right at the end of the repeated sequence
					++levelStates[currentLevel].repetitionCounter;
					if (levelStates[currentLevel].repetitionCounter < levelStates[currentLevel].repetitionCount) {
						// still an iteration to go
						long lastElementOfRepetition = peek();
						// reset to previous reset point
						LevelState.resetState(levelStates, currentLevel);
						resetCurrentLevel();
						return lastElementOfRepetition;
					} else {
						// no further iteration
						levelStates[currentLevel].repetitionIndex = -1;
						--levelStates[currentLevel].index;
						--currentLevel;
						return next();
					}
				} else {
					// still inside of the repeated sequence
					--levelStates[currentLevel].index;
					--currentLevel;
					return next();
				}
			} else {
				// check if we are in a repeated sequence
				int[] repMarker = trace.getRepetitionMarkers().get(currentLevel-1).getBackwardsRepetitionMarkers().get(levelStates[currentLevel].index);
//				System.out.println("s: " + currentLevel + ", " + levelStates[currentLevel].index);
				if (repMarker != null) {
//					System.out.println(currentLevel + ", " + levelStates[currentLevel].index);
					// we are in a new repeated sequence!
					// [length, repeat_count]
					levelStates[currentLevel].repetitionIndex = levelStates[currentLevel].index;
					levelStates[currentLevel].repetitionLength = repMarker[0];
					levelStates[currentLevel].repetitionCount = repMarker[1];
					levelStates[currentLevel].repetitionCounter = 0;
					// set the reset point to this exact point
					LevelState.setResetPoint(levelStates, currentLevel);
					// stay on the same level!
					return next();
				} else {
					// not in a repeated sequence!
					--levelStates[currentLevel].index;
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
		return trace.getCompressedTrace().get(levelStates[0].index);
	}

	public boolean isStartOfRepetition() {
		for (int level = levelStates.length - 1; level > 0; --level) {
			// check if we are in a repeated sequence
			if (trace.getRepetitionMarkers().get(level-1).getBackwardsRepetitionMarkers().containsKey(levelStates[level].index)) {
				return true;
			}
		}
		return false;
	}

	public boolean isEndOfRepetition() {
		for (int level = levelStates.length - 1; level > 0; --level) {
			// prioritize repetitions in parent 
			// (parent repetitions should be contained in child repetitions)
			if (levelStates[level].repetitionIndex >= 0) {
				// inside of a repeated sequence
				if (levelStates[level].index == levelStates[level].repetitionIndex - levelStates[level].repetitionLength + 1) {
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
			return trace.getCompressedTrace().getAndReplaceWith(levelStates[0].index--, function);
		} else {
			// prioritize repetitions in parent 
			// (parent repetitions should be contained in child repetitions)
			if (levelStates[currentLevel].repetitionIndex >= 0) {
				// inside of a repeated sequence
				if (levelStates[currentLevel].index == levelStates[currentLevel].repetitionIndex - levelStates[currentLevel].repetitionLength + 1) {
					// right at the end of the repeated sequence
					++levelStates[currentLevel].repetitionCounter;
					if (levelStates[currentLevel].repetitionCounter < levelStates[currentLevel].repetitionCount) {
						// still an iteration to go
						long lastElementOfRepetition = peek();
						// reset to previous reset point
						LevelState.resetState(levelStates, currentLevel);
						resetCurrentLevel();
						return lastElementOfRepetition;
					} else {
						// no further iteration
						levelStates[currentLevel].repetitionIndex = -1;
						--levelStates[currentLevel].index;
						--currentLevel;
						return next();
					}
				} else {
					// still inside of the repeated sequence
					--levelStates[currentLevel].index;
					--currentLevel;
					return next();
				}
			} else {
				// check if we are in a repeated sequence
				int[] repMarker = trace.getRepetitionMarkers().get(currentLevel-1).getBackwardsRepetitionMarkers().get(levelStates[currentLevel].index);
				if (repMarker != null) {
					// we are in a new repeated sequence!
					// [length, repeat_count]
					levelStates[currentLevel].repetitionIndex = levelStates[currentLevel].index;
					levelStates[currentLevel].repetitionLength = repMarker[0];
					levelStates[currentLevel].repetitionCount = repMarker[1];
					levelStates[currentLevel].repetitionCounter = 0;
					// set the reset point to this exact point
					LevelState.setResetPoint(levelStates, currentLevel);
					// stay on the same level!
					return next();
				} else {
					// not in a repeated sequence!
					--levelStates[currentLevel].index;
					--currentLevel;
					return next();
				}
			}
		}
	}
	
}