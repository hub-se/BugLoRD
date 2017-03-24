package se.de.hu_berlin.informatik.astlmbuilder.mapping;

public interface IBasicMapper {

	public final char SPLIT = ',';
	public final char ID_MARKER = ';';
	public final char GROUP_START = '[';
	public final char GROUP_END = ']';
	public final char BIG_GROUP_START = '(';
	public final char BIG_GROUP_END = ')';
	public final char TYPEARG_START = '<';
	public final char TYPEARG_END = '>';

	public final char KEYWORD_MARKER = '$';
	public final char KEYWORD_SERIALIZE = '%'; // marks the beginning of
														// the serialization

	/**
	 * Builds the string representation of all modifiers stored in the given
	 * integer and marks them with group tags
	 * 
	 * @param modifiers
	 *            the modifiers as an integer
	 * @return The string representation of the modifiers
	 */
	public String getModifierEnclosed(final int modifiers);

	/**
	 * Builds the string representation of all modifiers stored in the given
	 * integer and DOES NOT mark them with group tags
	 * 
	 * @param modifiers
	 *            the modifiers as an integer
	 * @return The string representation of the modifiers
	 */
	public String getModifier(final int modifiers);

	/**
	 * Assumes that the list of modifications, that a class of this interface
	 * created previously is given as a parameter and creates the integer value
	 * that was the original source.
	 * 
	 * @param aAllMods
	 *            A list of modifiers in one string without group start or end
	 *            symbols
	 * @return The integer value that stores all modifiers
	 */
	public int getAllModsAsInt(String aAllMods);

	/**
	 * Getting or changing a modifier to have a given modification
	 * 
	 * @param aMod
	 *            a modification as a String
	 * @param aBase
	 *            given modifiers?
	 * @return the modifiers as an integer
	 */
	public int getOrAddModifier(String aMod, int aBase);

	public default char getSplit() {
		return SPLIT;
	}

	public default char getIdMarker() {
		return ID_MARKER;
	}

	public default char getGroupStart() {
		return GROUP_START;
	}

	public default char getGroupEnd() {
		return GROUP_END;
	}

	public default char getBigGroupStart() {
		return BIG_GROUP_START;
	}

	public default char getBigGroupEnd() {
		return BIG_GROUP_END;
	}

	public default char getTypeArgStart() {
		return TYPEARG_START;
	}

	public default char getTypeArgEnd() {
		return TYPEARG_END;
	}

	public default char getKeyWordMarker() {
		return KEYWORD_MARKER;
	}

	public default char getKeyWordSerialize() {
		return KEYWORD_SERIALIZE;
	}

}
