package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

@CoverageIgnore
public class LevelState {

//	public long index = 0;

//	public int repetitionIndex = -1;
//	public int repetitionLength = 0;
//	public int repetitionCount = 0;
//	public int repetitionCounter = 0;

    // stores the current level's state and the state of levels below to restore, if necessary
    // format: index, repetitionIndex ; length, count, counter
    public long[] indexState; // = new long[] {0, -1};
    public int[] repetitionState; // = new int[] {0, 0, 0};

//	public List<int[]> resetStateList;

    public LevelState(int level) {
        this.indexState = new long[2 + level * 2];
        this.repetitionState = new int[3 + level * 3];
        this.indexState[1] = -1;
    }

    public LevelState(LevelState state) {
        this.indexState = state.indexState.clone();
        this.repetitionState = state.repetitionState.clone();
    }

    public static LevelState[] copy(LevelState[] levelStates) {
        LevelState[] result = new LevelState[levelStates.length];
        for (int i = 0; i < levelStates.length; ++i) {
            result[i] = new LevelState(levelStates[i]);
        }
        return result;
    }

    public static void setResetPoint(LevelState[] levelStates, int currentLevel) {
        int counter = 0;
        for (int i = currentLevel - 1; i >= 0; --i) {
            ++counter;
            System.arraycopy(levelStates[i].indexState, 0, levelStates[currentLevel].indexState, counter * 2, 2);
            System.arraycopy(levelStates[i].repetitionState, 0, levelStates[currentLevel].repetitionState, counter * 3, 3);
        }
    }

    public static void resetState(LevelState[] levelStates, int currentLevel) {
        // reset index to stored repetition index
        levelStates[currentLevel].indexState[0] = levelStates[currentLevel].indexState[1];

        int counter = 0;
        for (int i = currentLevel - 1; i >= 0; --i) {
            ++counter;
            System.arraycopy(levelStates[currentLevel].indexState, counter * 2, levelStates[i].indexState, 0, 2);
            System.arraycopy(levelStates[currentLevel].repetitionState, counter * 3, levelStates[i].repetitionState, 0, 3);
        }
    }

}