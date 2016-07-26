package se.de.hu_berlin.informatik.astlmbuilder;

import com.github.javaparser.ast.body.ModifierSet;

public class ModifierMapper {
	
	public static String getModifier(final int modifiers) {
		String result = "";
		boolean first = true;
		
		if (ModifierSet.isPrivate(modifiers)) {
			if( first ) {
				result = "PRIV";
				first = false;
			} else{
				result += ",PRIV";
			}
		}
		if (ModifierSet.isPublic(modifiers)) {
			if( first ) {
				result = "PUB";
				first = false;
			} else{
				result += ",PUB";
			}
		}
		if (ModifierSet.isProtected(modifiers)) {
			if( first ) {
				result = "PROT";
				first = false;
			} else{
				result += ",PROT";
			}
		}
		if (ModifierSet.isAbstract(modifiers)) {
			if( first ) {
				result = "ABS";
				first = false;
			} else{
				result += ",ABS";
			}
		}
		if (ModifierSet.isStatic(modifiers)) {
			if( first ) {
				result = "STATIC";
				first = false;
			} else{
				result += ",STATIC";
			}
		}
		if (ModifierSet.isFinal(modifiers)) {
			if( first ) {
				result = "FINAL";
				first = false;
			} else{
				result += ",FINAL";
			}
		}
		if (ModifierSet.isNative(modifiers)) {
			if( first ) {
				result = "NATIVE";
				first = false;
			} else{
				result += ",NATIVE";
			}
		}
		if (ModifierSet.isStrictfp(modifiers)) {
			if( first ) {
				result = "STRICT";
				first = false;
			} else{
				result += ",STRICT";
			}
		}
		if (ModifierSet.isSynchronized(modifiers)) {
			if( first ) {
				result = "SYNC";
				first = false;
			} else{
				result += ",SYNC";
			}
		}
		if (ModifierSet.isTransient(modifiers)) {
			if( first ) {
				result = "TRANS";
				first = false;
			} else{
				result += ",TRANS";
			}
		}
		if (ModifierSet.isVolatile(modifiers)) {
			if( first ) {
				result = "VOLATILE";
				first = false;
			} else{
				result += ",VOLATILE";
			}
		}
		
		return result;
	}
}
