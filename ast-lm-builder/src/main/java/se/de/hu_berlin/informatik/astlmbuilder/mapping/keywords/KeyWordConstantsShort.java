package se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords;

public class KeyWordConstantsShort implements IKeyWordProvider<String> {

    @Override
    public KeyWords StringToKeyWord(String token) throws IllegalArgumentException {
        // this is a char, so we need to convert it to a String before comparing
        if (token.equals(String.valueOf(IBasicKeyWords.KEYWORD_NULL))) {
            return KeyWords.NULL;
        }
        if (token.equals(IBasicKeyWords.KEYWORD_NULL_LIST)) {
            return KeyWords.NULL_LIST;
        }
        if (token.equals(IBasicKeyWords.KEYWORD_EMPTY_LIST)) {
            return KeyWords.EMPTY_LIST;
        }
        return KeyWords.values()[Integer.parseUnsignedInt(token, 16)];
    }

    @Override
    public String getKeyWord(KeyWords keyWord) {
        if (keyWord.equals(KeyWords.NULL)) {
            return String.valueOf(IBasicKeyWords.KEYWORD_NULL);
        }
        if (keyWord.equals(KeyWords.NULL_LIST)) {
            return IBasicKeyWords.KEYWORD_NULL_LIST;
        }
        if (keyWord.equals(KeyWords.EMPTY_LIST)) {
            return IBasicKeyWords.KEYWORD_EMPTY_LIST;
        }
        return Integer.toHexString(keyWord.ordinal());
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
