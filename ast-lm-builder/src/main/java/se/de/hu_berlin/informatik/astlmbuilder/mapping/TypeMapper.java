package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import com.github.javaparser.ast.type.PrimitiveType.Primitive;

public class TypeMapper {

	public static Primitive getPrimTypeFromMapping( String aSerializedPrimType ) {
		if( aSerializedPrimType == null || aSerializedPrimType.length() == 0 ) {
			return null;
		}		
		
		// This is a bit tricky with the integer because we use the toString when serializing
		
		if( aSerializedPrimType.equals( "Boolean" ) ) {
			return Primitive.Boolean;
		}
		
		if( aSerializedPrimType.equals( "Char" ) ) {
			return Primitive.Char;
		}
		
		if( aSerializedPrimType.equals( "Byte" ) ) {
			return Primitive.Byte;
		}
		
		if( aSerializedPrimType.equals( "Short" ) ) {
			return Primitive.Short;
		}
		
		// this is the mean part because the string value would be "Integer"
		if( aSerializedPrimType.equals( "Int" ) ) {
			return Primitive.Int;
		}
		
		if( aSerializedPrimType.equals( "Long" ) ) {
			return Primitive.Long;
		}
		
		if( aSerializedPrimType.equals( "Float" ) ) {
			return Primitive.Float;
		}
		
		if( aSerializedPrimType.equals( "Double" ) ) {
			return Primitive.Double;
		}
				
		return null;
	}
	
}
