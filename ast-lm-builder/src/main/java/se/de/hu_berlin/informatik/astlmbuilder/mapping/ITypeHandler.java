package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import com.github.javaparser.ast.type.PrimitiveType.Primitive;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IBasicKeyWords;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

public interface ITypeHandler {
	
	public final char KEYWORD_TRUE = 'T';
	public final char KEYWORD_FALSE = 'F';

	public default Primitive parsePrimitiveFromToken(String token) {
		return Primitive.values()[Integer.valueOf(token)];
	}
	
	public default String getMappingForPrimitive(Primitive type) {
		return String.valueOf(type.ordinal());
	}
	
	public default String getMappingForBoolean(boolean value) {
		return value ? String.valueOf(KEYWORD_TRUE) : String.valueOf(KEYWORD_FALSE);
	}
	
	/**
	 * Parses a boolean value from the given token.
	 * <p> Expected token format: {@code T} or {@code F}.
	 * @param token
	 * the token to parse
	 * @return
	 * the boolean value
	 * @throws IllegalArgumentException
	 * if the given token is of the wrong format
	 */
	default public boolean parseBooleanFromToken(String token) throws IllegalArgumentException {
		char start = token.charAt(0);
		if (start == KEYWORD_TRUE) { //found boolean true
			int firstSplitIndex = token.indexOf(IBasicKeyWords.GROUP_START);
			if (firstSplitIndex > 0) { //found split char
				throw new IllegalArgumentException("Illegal token: '" + token + "'.");
			} else {
				return true;
			}
		} else if (start == KEYWORD_FALSE) { //found boolean false
			int firstSplitIndex = token.indexOf(IBasicKeyWords.GROUP_START);
			if (firstSplitIndex > 0) { //found split char
				throw new IllegalArgumentException("Illegal token: '" + token + "'.");
			} else {
				return false;
			}
		} else { //this should not happen ever and should throw an exception
			throw new IllegalArgumentException("Illegal token: '" + token + "'.");
		}
	}
	
	public default String getMappingForString(String value) {
		return getParseableString(value);
	}
	
	static String getParseableString(String original) {
		return Misc.replaceWhitespacesInString(original, "_")
				.replace(IBasicKeyWords.GROUP_START, '_')
				.replace(IBasicKeyWords.GROUP_END, '_');
	}
	
	/**
	 * Parses a String value from the given token.
	 * <p> Expected token format: {@code ~} (null) or {@code s}.
	 * @param token
	 * the token to parse
	 * @return
	 * the String value
	 * @throws IllegalArgumentException
	 * if the given token is of the wrong format
	 */
	default public String parseStringValueFromToken(String token) throws IllegalArgumentException {
		char start = token.charAt(0);
		if (start == IBasicKeyWords.KEYWORD_NULL) { //return null if this is the only char in the given String
			if (token.length() == 1) {
				return null;
			} else {
				throw new IllegalArgumentException("Illegal null token: '" + token + "'.");
			}
		} else {
			return token;
		}
	}
	
	public default String getMappingForChar(String value) {
		return getParseableString(value);
	}
	
	/**
	 * Parses a char value from the given token.
	 * <p> Expected token format: {@code c}.
	 * @param token
	 * the token to parse
	 * @return
	 * the char value
	 * @throws IllegalArgumentException
	 * if the given token is of the wrong format
	 */
	default public char parseCharValueFromToken(String token) throws IllegalArgumentException {
		if (token.length() == 1) {
			return token.charAt(0);
		} else { //this should not happen ever and should throw an exception
			throw new IllegalArgumentException("Illegal token: '" + token + "'.");
		}
	}
	
}
