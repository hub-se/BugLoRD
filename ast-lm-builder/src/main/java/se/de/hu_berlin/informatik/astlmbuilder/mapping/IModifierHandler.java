package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.EnumSet;
import com.github.javaparser.ast.Modifier;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IBasicKeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstants;

public interface IModifierHandler extends IBasicKeyWords {

	/**
	 * Builds the string representation of all modifiers stored in the given
	 * integer and marks them with group tags
	 * 
	 * @param modifiers
	 *            the modifiers as an integer
	 * @return The string representation of the modifiers
	 */
	default public String getModifierEnclosed(final EnumSet<Modifier> modifiers) {
		return KeyWordConstants.GROUP_START + getModifier(modifiers) + KeyWordConstants.GROUP_END;
	}

	/**
	 * Assumes that the list of modifications, that a class of this interface
	 * created previously is given as a parameter and creates the integer value
	 * that was the original source.
	 * 
	 * @param aAllMods
	 *            A list of modifiers in one string without group start or end
	 *            symbols
	 * @return The integer value that stores all modifiers
	 */
	default EnumSet<Modifier> getAllModsAsSet(String aAllMods) {
		EnumSet<Modifier> result = EnumSet.noneOf(Modifier.class);

		for (String s : aAllMods.split(",")) {
			result = getOrAddModifier(s, result);
		}

		return result;
	}

	/**
	 * Getting or changing a modifier to have a given modification
	 * 
	 * @param aMod
	 *            a modification as a String
	 * @param aBase
	 *            given modifiers?
	 * @return the modifiers as an integer
	 */
	default public EnumSet<Modifier> getOrAddModifier(String aMod, EnumSet<Modifier> aBase) { throw new UnsupportedOperationException(); }

	/**
	 * Builds the string representation of all modifiers stored in the given
	 * integer and DOES NOT mark them with group tags
	 * 
	 * @param modifiers
	 *            the modifiers as an integer
	 * @return The string representation of the modifiers
	 */
	default public String getModifier(final EnumSet<Modifier> modifiers) { throw new UnsupportedOperationException(); }
	
}
