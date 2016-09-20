package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.ModifierSet;

public class ModifierMapper {
	
	public static final String PRIV = "PRIV";
	public static final String PUB = "PUB";
	public static final String PROT = "PROT";
	public static final String ABS = "ABS";
	public static final String STATIC = "STATIC";
	public static final String FINAL = "FINAL";
	public static final String NATIVE = "NATIVE";
	public static final String STRICTFP = "STRICTFP";
	public static final String SYNC = "SYNC";
	public static final String TRANS = "TRANS";
	public static final String VOLATILE = "VOLATILE";
	
	
	/**
	 * Builds the string representation of all modifiers stored in the given integer
	 * and marks them with group tags
	 * @param modifiers
	 * @return The string representation of the modifiers
	 */
	public static String getModifierEnclosed( final int modifiers ) {
		return KeyWordConstants.GROUP_START + getModifier( modifiers ) + KeyWordConstants.GROUP_END;
	}
	
	public static String getModifier(final int modifiers) {
		List<String> result = new ArrayList<>();

		if (ModifierSet.isPrivate(modifiers)) {
			result.add(PRIV);
		}
		if (ModifierSet.isPublic(modifiers)) {
			result.add(PUB);
		}
		if (ModifierSet.isProtected(modifiers)) {
			result.add(PROT);
		}
		if (ModifierSet.isAbstract(modifiers)) {
			result.add(ABS);
		}
		if (ModifierSet.isStatic(modifiers)) {
			result.add(STATIC);
		}
		if (ModifierSet.isFinal(modifiers)) {
			result.add(FINAL);
		}
		if (ModifierSet.isNative(modifiers)) {
			result.add(NATIVE);
		}
		if (ModifierSet.isStrictfp(modifiers)) {
			result.add(STRICTFP);
		}
		if (ModifierSet.isSynchronized(modifiers)) {
			result.add(SYNC);
		}
		if (ModifierSet.isTransient(modifiers)) {
			result.add(TRANS);
		}
		if (ModifierSet.isVolatile(modifiers)) {
			result.add(VOLATILE);
		}
		
		return String.join(",", result);
	}
	
	/**
	 * Assumes that the list of modifications, that this class created previously is given as a parameter
	 * and creates the integer value that was the original source.
	 * @param aAllMods A list of modifiers in one string without group start or end symbols
	 * @return The integer value that stores all modifiers
	 */
	public static int getAllModsAsInt( String aAllMods ) {
		int result = 0; // 0 means no mods att all
		
		for( String s : aAllMods.split( "," ) ) {
			result = getOrAddModifier( s, result );
		}
		
		return result;
	}
	
	/**
	 * Getting or changing a modifier to have a given modification
	 * @param aMod
	 * @return
	 */
	public static int getOrAddModifier( String aMod, int aBase) {
		int result = aBase; // call by value, I know but i prefer to name it result

		int mappedMod = 0;
		
		switch( aMod ) {
			case PRIV : mappedMod = ModifierSet.PRIVATE; break;
			case PUB : mappedMod = ModifierSet.PUBLIC; break;
			case PROT : mappedMod = ModifierSet.PROTECTED; break;
			case ABS : mappedMod = ModifierSet.ABSTRACT; break;
			case STATIC : mappedMod = ModifierSet.STATIC; break;
			case FINAL : mappedMod = ModifierSet.FINAL; break;
			case NATIVE : mappedMod = ModifierSet.NATIVE; break;
			case STRICTFP : mappedMod = ModifierSet.STRICTFP; break;
			case TRANS : mappedMod = ModifierSet.TRANSIENT; break;
			case VOLATILE : mappedMod = ModifierSet.VOLATILE; break;
			default : mappedMod = 0;  break; // added no modifier because the given String was unknown
		}
		
		return ModifierSet.addModifier( result , mappedMod );

	}
}
