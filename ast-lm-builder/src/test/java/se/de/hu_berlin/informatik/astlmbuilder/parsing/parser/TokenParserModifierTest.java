package se.de.hu_berlin.informatik.astlmbuilder.parsing.parser;

import java.util.EnumSet;

import org.junit.Test;

import com.github.javaparser.ast.Modifier;

import junit.framework.TestCase;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstants;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.Node2AbstractionMapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class TokenParserModifierTest extends TestCase {

	// I need the implementation instead of the interface to access the map modifier method
	// the parsing can be handles by the t_parser_long because the method is accessible
	Node2AbstractionMapper modMapper = new Node2AbstractionMapper.Builder(new KeyWordConstants()).usesStringAndCharAbstraction().build();
	ITokenParser t_parser_long = new SimpleTokenParser(new KeyWordConstants());
	
	/**
	 * Uses the annotation declaration for tests regarding the serialization and deserialization
	 * of the modifiers.
	 * @param mapper The mapper that can use the short or the long keywords
	 * @param parser The parser that can use the short or the long keywords
	 */
	@Test
	public void testModifiers() {

		EnumSet<Modifier> allMods = EnumSet.noneOf( Modifier.class );
		
		// all single mods
		for( Modifier m : Modifier.values() ) {
			testSpecificEnumSet( EnumSet.of( m ) );
			allMods.add( m );
		}
		
		// all mods in one set
		Log.out(this, "Testing all mods in one set:" );
		testSpecificEnumSet( allMods );

		// could test lots of combinations but should be fine
	}
	
	/**
	 * Creates a token from the given enum set, parses the token and compares both
	 * @param aES The enum set to check
	 */
	private void testSpecificEnumSet( EnumSet<Modifier> aES ) {
		String modsSerialized = modMapper.getMappingForModifiers( aES );
		Log.out( this, modsSerialized );
		EnumSet<Modifier> secondSet = t_parser_long.parseModifiersFromToken( modsSerialized );
		
		assertTrue( compareEnumSets( aES, secondSet ) );
	}
	
	/**
	 * Compares two sets of modifiers
	 * @param aFirstSet
	 * the first set
	 * @param aSecondSet
	 * the second set
	 * @return true if and only if all entries from the first set are contained in the second and vice versa
	 */
	private boolean compareEnumSets( EnumSet<Modifier> aFirstSet, EnumSet<Modifier> aSecondSet ) {
		// same size of course
		if( aFirstSet.size() != aSecondSet.size() ) {
			return false;
		}
		
		// all mods from the first set must be in the second
		for( Modifier m : aFirstSet ) {
			if ( !aSecondSet.contains( m ) ) {
				return false;
			}
		}
		
		// all mods from the second set must be in the first
		for( Modifier m : aSecondSet ) {
			if( !aFirstSet.contains( m ) ) {
				return false;
			}
		}
		
		return true;
	}
	
}
