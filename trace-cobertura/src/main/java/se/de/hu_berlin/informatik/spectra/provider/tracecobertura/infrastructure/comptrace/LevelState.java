package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace;

public class LevelState {
	
//	public int index = 0;
//	public int repetitionIndex = -1;
//	public int repetitionLength = 0;
//	public int repetitionCount = 0;
//	public int repetitionCounter = 0;
	
	// stores the current level's state and the state of levels below to restore, if necessary
	// format: index, repetitionIndex, length, count, counter
	public int[] state; // = new int[] {0, -1, 0, 0, 0};

//	public List<int[]> resetStateList;

	public LevelState(int level) {
		this.state = new int[5 + level * 5];
		this.state[1] = -1;
	}

	public LevelState(LevelState state) {
		this.state = state.state.clone();
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
			System.arraycopy(levelStates[i].state, 0, levelStates[currentLevel].state, counter * 5, 5);
		}
	}

	public static void resetState(LevelState[] levelStates, int currentLevel) {
		levelStates[currentLevel].state[0] = levelStates[currentLevel].state[1];

		int counter = 0;
		for (int i = currentLevel - 1; i >= 0; --i) {
			++counter;
			System.arraycopy(levelStates[currentLevel].state, counter * 5, levelStates[i].state, 0, 5);
		}
	}

}