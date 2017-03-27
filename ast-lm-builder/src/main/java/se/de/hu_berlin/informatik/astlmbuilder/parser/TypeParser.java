package se.de.hu_berlin.informatik.astlmbuilder.parser;

import com.github.javaparser.ast.type.PrimitiveType.Primitive;

public interface TypeParser {

	public static final String PT_BOOLEAN = Primitive.BOOLEAN.asString();
	public static final String PT_CHAR = Primitive.CHAR.asString();
	public static final String PT_BYTE = Primitive.BYTE.asString();
	public static final String PT_SHORT = Primitive.SHORT.asString();
	public static final String PT_INT = Primitive.INT.asString();
	public static final String PT_LONG = Primitive.LONG.asString();
	public static final String PT_FLOAT = Primitive.FLOAT.asString();
	public static final String PT_DOUBLE = Primitive.DOUBLE.asString();
	
	public static Primitive getPrimTypeFromMapping( String token ) {
		if( token == null || token.length() == 0 ) {
			return null;
		}
		
		if( token.equals( PT_BOOLEAN ) ) {
			return Primitive.BOOLEAN;
		}
		
		if( token.equals( PT_CHAR ) ) {
			return Primitive.CHAR;
		}
		
		if( token.equals( PT_BYTE ) ) {
			return Primitive.BYTE;
		}
		
		if( token.equals( PT_SHORT ) ) {
			return Primitive.SHORT;
		}
		
		if( token.equals( PT_INT ) ) {
			return Primitive.INT;
		}
		
		if( token.equals( PT_LONG ) ) {
			return Primitive.LONG;
		}
		
		if( token.equals( PT_FLOAT ) ) {
			return Primitive.FLOAT;
		}
		
		if( token.equals( PT_DOUBLE ) ) {
			return Primitive.DOUBLE;
		}
				
		throw new IllegalArgumentException("Illegal token: '" + token + "'.");
	}
	
}
