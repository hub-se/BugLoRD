package se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords;

public class KeyWordConstants implements IKeyWordProvider<String> {

    @Override
    public KeyWords StringToKeyWord(String token) throws IllegalArgumentException {
        if (token.equals(String.valueOf(IBasicKeyWords.KEYWORD_NULL))) {
            return KeyWords.NULL;
        }
        return KeyWords.valueOf(token);
    }

    @Override
    public String getKeyWord(KeyWords keyWord) {
        if (keyWord.equals(KeyWords.NULL)) {
            return String.valueOf(IBasicKeyWords.KEYWORD_NULL);
        }
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
        } else return mapping.charAt(0) == '_';
    }

}
