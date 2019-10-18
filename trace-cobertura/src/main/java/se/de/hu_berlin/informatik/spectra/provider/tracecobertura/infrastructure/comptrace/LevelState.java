package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.infrastructure.comptrace;

import java.util.ArrayList;
import java.util.List;

public class LevelState {
	
	public int index = 0;
	public int repetitionIndex = -1;
	public int repetitionLength = 0;
	public int repetitionCount = 0;
	public int repetitionCounter = 0;

	public List<int[]> resetStateList;

	public LevelState() {
	}

	public LevelState(LevelState state) {
		this.index = state.index;
		this.repetitionIndex = state.repetitionIndex;
		this.repetitionLength = state.repetitionLength;
		this.repetitionCount = state.repetitionCount;
		this.repetitionCounter = state.repetitionCounter;
		this.resetStateList = state.resetStateList == null ? null : 
			new ArrayList<>(state.resetStateList);
	}
	
	public static LevelState[] copy(LevelState[] levelStates) {
		LevelState[] result = new LevelState[levelStates.length];
		for (int i = 0; i < levelStates.length; ++i) {
			result[i] = new LevelState(levelStates[i]);
		}
		return result;
	}
	
	public static void setResetPoint(LevelState[] levelStates, int currentLevel) {
		levelStates[currentLevel].resetStateList = new ArrayList<>(currentLevel);
		getAndStoreState(levelStates, levelStates[currentLevel].resetStateList, currentLevel - 1);
	}

	private static void getAndStoreState(LevelState[] levelStates, List<int[]> resetIndexList, int level) {
		if (level > -1) {
			resetIndexList.add(new int[] {
					levelStates[level].index, 
					levelStates[level].repetitionIndex,
					levelStates[level].repetitionLength,
					levelStates[level].repetitionCount,
					levelStates[level].repetitionCounter});
			getAndStoreState(levelStates, resetIndexList, --level);
		}
	}

	public static void resetState(LevelState[] levelStates, int currentLevel) {
		levelStates[currentLevel].index = levelStates[currentLevel].repetitionIndex;
		setState(levelStates, levelStates[currentLevel].resetStateList, 0, currentLevel - 1);
	}

	private static void setState(LevelState[] levelStates, List<int[]> resetIndexList, int index, int level) {
		if (level > -1) {
			int[] state = resetIndexList.get(index);
			levelStates[level].index = state[0];
			levelStates[level].repetitionIndex = state[1];
			levelStates[level].repetitionLength = state[2];
			levelStates[level].repetitionCount = state[3];
			levelStates[level].repetitionCounter = state[4];
			setState(levelStates, resetIndexList, ++index, --level);
		}
	}
}