package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import com.github.javaparser.ast.type.PrimitiveType.Primitive;

public class TypeMapper {

	public static final String PT_BOOLEAN = "Boolean";
	public static final String PT_CHAR = "Char";
	public static final String PT_BYTE = "Byte";
	public static final String PT_SHORT = "Short";
	public static final String PT_INT = "Int";
	public static final String PT_LONG = "Long";
	public static final String PT_FLOAT = "Float";
	public static final String PT_DOUBLE = "Double";
	
	public static Primitive getPrimTypeFromMapping( String aSerializedPrimType ) {
		if( aSerializedPrimType == null || aSerializedPrimType.length() == 0 ) {
			return null;
		}		
		
		// This is a bit tricky with the integer because we use the toString when serializing
		
		if( aSerializedPrimType.equals( PT_BOOLEAN ) ) {
			return Primitive.BOOLEAN;
		}
		
		if( aSerializedPrimType.equals( PT_CHAR ) ) {
			return Primitive.CHAR;
		}
		
		if( aSerializedPrimType.equals( PT_BYTE ) ) {
			return Primitive.BYTE;
		}
		
		if( aSerializedPrimType.equals( PT_SHORT ) ) {
			return Primitive.SHORT;
		}
		
		// this is the mean part because the string value would be "Integer"
		if( aSerializedPrimType.equals( PT_INT ) ) {
			return Primitive.INT;
		}
		
		if( aSerializedPrimType.equals( PT_LONG ) ) {
			return Primitive.LONG;
		}
		
		if( aSerializedPrimType.equals( PT_FLOAT ) ) {
			return Primitive.FLOAT;
		}
		
		if( aSerializedPrimType.equals( PT_DOUBLE ) ) {
			return Primitive.DOUBLE;
		}
				
		return null;
	}
	
}
