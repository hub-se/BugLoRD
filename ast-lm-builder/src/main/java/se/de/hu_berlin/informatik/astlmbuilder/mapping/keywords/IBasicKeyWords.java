package se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords;

public interface IBasicKeyWords {

	public final char SPLIT = ',';
	public final char ID_MARKER = ';';
	public final char GROUP_START = '[';
	public final char GROUP_END = ']';
	public final char BIG_GROUP_START = '(';
	public final char BIG_GROUP_END = ')';
	public final char TYPEARG_START = '<';
	public final char TYPEARG_END = '>';

	public final char KEYWORD_LIST = '#';
	public final char KEYWORD_MARKER = '$';
	public final char KEYWORD_SERIALIZE = '%'; // marks the beginning of
														// the serialization

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
	
	public default char getListMarker() {
		return KEYWORD_LIST;
	}

	public default char getKeyWordMarker() {
		return KEYWORD_MARKER;
	}

	public default char getKeyWordSerialize() {
		return KEYWORD_SERIALIZE;
	}

}
