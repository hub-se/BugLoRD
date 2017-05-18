package se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords;

public class KeyWordConstantsShort implements IKeyWordProvider<String> {

	@Override
	public KeyWords StringToKeyWord(String token) throws IllegalArgumentException{
		return KeyWords.values()[Integer.parseUnsignedInt(token, 16)];
	}

	@Override
	public String getKeyWord(KeyWords keyWord) {
		return Integer.toHexString(keyWord.ordinal());
	}

}
