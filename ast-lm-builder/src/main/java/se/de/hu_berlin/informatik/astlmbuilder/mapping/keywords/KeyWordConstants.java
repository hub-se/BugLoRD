package se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords;

public class KeyWordConstants implements IKeyWordProvider<String> {

	@Override
	public KeyWords StringToKeyWord(String token) throws IllegalArgumentException {
		return KeyWords.valueOf(token);
	}

	@Override
	public String getKeyWord(KeyWords keyWord) {
		return keyWord.toString();
	}

	@Override
	public String markAsClosing(String mapping) {
		return "_" + mapping;
	}

}
