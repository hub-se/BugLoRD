package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.ModifierSet;

public class ModifierMapper {
	
	public static String getModifier(final int modifiers) {
		List<String> result = new ArrayList<>();

		if (ModifierSet.isPrivate(modifiers)) {
			result.add("PRIV");
		}
		if (ModifierSet.isPublic(modifiers)) {
			result.add("PUB");
		}
		if (ModifierSet.isProtected(modifiers)) {
			result.add("PROT");
		}
		if (ModifierSet.isAbstract(modifiers)) {
			result.add("ABS");
		}
		if (ModifierSet.isStatic(modifiers)) {
			result.add("STATIC");
		}
		if (ModifierSet.isFinal(modifiers)) {
			result.add("FINAL");
		}
		if (ModifierSet.isNative(modifiers)) {
			result.add("NATIVE");
		}
		if (ModifierSet.isStrictfp(modifiers)) {
			result.add("STRICTFP");
		}
		if (ModifierSet.isSynchronized(modifiers)) {
			result.add("SYNC");
		}
		if (ModifierSet.isTransient(modifiers)) {
			result.add("TRANS");
		}
		if (ModifierSet.isVolatile(modifiers)) {
			result.add("VOLATILE");
		}
		
		return String.join(",", result);
	}
}
