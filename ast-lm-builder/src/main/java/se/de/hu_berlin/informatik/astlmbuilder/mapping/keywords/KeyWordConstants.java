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

	@Override
	public boolean isMarkedAsClosing(String mapping) {
		if (mapping == null || mapping.length() < 1) {
			return false;
		} else if (mapping.charAt(0) == '_') {
			return true;
		} else {
			return false;
		}
	}

}
