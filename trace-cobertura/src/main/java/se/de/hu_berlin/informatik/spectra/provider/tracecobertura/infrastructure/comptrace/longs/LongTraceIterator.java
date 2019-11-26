package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.longs;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata.Function;
import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace.LevelState;

public class LongTraceIterator implements ReplaceableCloneableLongIterator {
	
	private final EfficientCompressedLongTrace trace;
	private final LevelState[] levelStates;
	
	// level 0 is the lowest level
	private int currentLevel = 0;
	
	public LongTraceIterator(EfficientCompressedLongTrace trace) {
		this.trace = trace;
		if (trace.getRepetitionMarkers() != null) {
			levelStates = new LevelState[trace.getRepetitionMarkers().size() + 1];
			for (int i = 0; i < trace.getRepetitionMarkers().size() + 1; ++i) {
				levelStates[i] = new LevelState(i);
			}
		} else {
			levelStates = new LevelState[] { new LevelState(0) };
		}
		resetCurrentLevel();
	}
	
	// clone constructor
	private LongTraceIterator(LongTraceIterator iterator) {
		this.trace = iterator.trace;
		levelStates = LevelState.copy(iterator.levelStates);
		this.currentLevel = iterator.currentLevel;
	}

	public LongTraceIterator clone() {
		return new LongTraceIterator(this);
	}

	@Override
	public boolean hasNext() {
		return levelStates[0].state[0] < trace.getCompressedTrace().size();
	}

	@Override
	public long next() {
		if (currentLevel <= 0) {
			resetCurrentLevel();
			return trace.getCompressedTrace().get(levelStates[0].state[0]++);
		} else {
			// prioritize repetitions in parent 
			// (parent repetitions should be contained in child repetitions)
			if (levelStates[currentLevel].state[1] >= 0) {
				// inside of a repeated sequence
				if (levelStates[currentLevel].state[0] == levelStates[currentLevel].state[1] + levelStates[currentLevel].state[2] - 1) {
					// right at the end of the repeated sequence
					++levelStates[currentLevel].state[4];
					if (levelStates[currentLevel].state[4] < levelStates[currentLevel].state[3]) {
						// still an iteration to go
						long lastElementOfRepetition = peek();
						// reset to previous reset point
						LevelState.resetState(levelStates, currentLevel);
						resetCurrentLevel();
						return lastElementOfRepetition;
					} else {
						// no further iteration
						levelStates[currentLevel].state[1] = -1;
						++levelStates[currentLevel].state[0];
						--currentLevel;
						return next();
					}
				} else {
					// still inside of the repeated sequence
					++levelStates[currentLevel].state[0];
					--currentLevel;
					return next();
				}
			} else {
				// check if we are in a repeated sequence
				int[] repMarker = trace.getRepetitionMarkers().get(currentLevel-1).getRepetitionMarkers().get(levelStates[currentLevel].state[0]);
				if (repMarker != null) {
					// we are in a new repeated sequence!
					// [length, repeat_count]
					levelStates[currentLevel].state[1] = levelStates[currentLevel].state[0];
					levelStates[currentLevel].state[2] = repMarker[0];
					levelStates[currentLevel].state[3] = repMarker[1];
					levelStates[currentLevel].state[4] = 0;
					// set the reset point to this exact point
					LevelState.setResetPoint(levelStates, currentLevel);
					// stay on the same level!
					return next();
				} else {
					// not in a repeated sequence!
					++levelStates[currentLevel].state[0];
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
		return trace.getCompressedTrace().get(levelStates[0].state[0]);
	}

	public boolean isStartOfRepetition() {
		for (int level = levelStates.length - 1; level > 0; --level) {
			// check if we are in a repeated sequence
			if (trace.getRepetitionMarkers().get(level-1).getRepetitionMarkers().containsKey(levelStates[level].state[0])) {
				return true;
			}
		}
		return false;
	}

	public boolean isEndOfRepetition() {
		for (int level = levelStates.length - 1; level > 0; --level) {
			// prioritize repetitions in parent 
			// (parent repetitions should be contained in child repetitions)
			if (levelStates[level].state[1] >= 0) {
				// inside of a repeated sequence
				if (levelStates[level].state[0] == levelStates[level].state[1] + levelStates[level].state[2] - 1) {
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
			return trace.getCompressedTrace().getAndReplaceWith(levelStates[0].state[0]++, function);
		} else {
			// prioritize repetitions in parent 
			// (parent repetitions should be contained in child repetitions)
			if (levelStates[currentLevel].state[1] >= 0) {
				// inside of a repeated sequence
				if (levelStates[currentLevel].state[0] == levelStates[currentLevel].state[1] + levelStates[currentLevel].state[2] - 1) {
					// right at the end of the repeated sequence
					++levelStates[currentLevel].state[4];
					if (levelStates[currentLevel].state[4] < levelStates[currentLevel].state[3]) {
						// still an iteration to go
						long lastElementOfRepetition = peek();
						// reset to previous reset point
						LevelState.resetState(levelStates, currentLevel);
						resetCurrentLevel();
						return lastElementOfRepetition;
					} else {
						// no further iteration
						levelStates[currentLevel].state[1] = -1;
						++levelStates[currentLevel].state[0];
						--currentLevel;
						return next();
					}
				} else {
					// still inside of the repeated sequence
					++levelStates[currentLevel].state[0];
					--currentLevel;
					return next();
				}
			} else {
				// check if we are in a repeated sequence
				int[] repMarker = trace.getRepetitionMarkers().get(currentLevel-1).getRepetitionMarkers().get(levelStates[currentLevel].state[0]);
				if (repMarker != null) {
					// we are in a new repeated sequence!
					// [length, repeat_count]
					levelStates[currentLevel].state[1] = levelStates[currentLevel].state[0];
					levelStates[currentLevel].state[2] = repMarker[0];
					levelStates[currentLevel].state[3] = repMarker[1];
					levelStates[currentLevel].state[4] = 0;
					// set the reset point to this exact point
					LevelState.setResetPoint(levelStates, currentLevel);
					// stay on the same level!
					return next();
				} else {
					// not in a repeated sequence!
					++levelStates[currentLevel].state[0];
					--currentLevel;
					return next();
				}
			}
		}
	}

}