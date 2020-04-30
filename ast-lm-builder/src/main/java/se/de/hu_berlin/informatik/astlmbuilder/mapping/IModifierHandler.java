package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import com.github.javaparser.ast.Modifier;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

import java.util.EnumSet;

public interface IModifierHandler {

    /**
     * Assumes that the list of modifications, that a class of this interface
     * created previously is given as a parameter and creates the integer value
     * that was the original source.
     *
     * @param encodedModifiers an integer that encodes an enum set
     * @return the decoded enum set
     * @throws NumberFormatException if the given String is not an integer
     */
    default EnumSet<Modifier> parseModifiersFromToken(final String encodedModifiers) throws NumberFormatException {
        return Misc.decode(Integer.valueOf(encodedModifiers), Modifier.class);
    }

    /**
     * Builds the string representation of all modifiers stored in the given
     * integer and DOES NOT mark them with group tags
     *
     * @param modifiers the modifiers as an integer
     * @return The string representation of the modifiers
     */
    default public String getMappingForModifiers(final EnumSet<Modifier> modifiers) {
        return String.valueOf(Misc.encode(modifiers));
    }

}
