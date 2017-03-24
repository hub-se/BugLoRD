package se.de.hu_berlin.informatik.astlmbuilder.reader;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.TypeArguments;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.IBasicMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.IKeyWordDispatcher;

public class DSUtils {
	
	public IKeyWordDispatcher kwDispatcher;
	private IASTLMDeserializer desi;
	
	public DSUtils( IASTLMDeserializer aDeserializer, IKeyWordDispatcher aKwDispatcher ){
		kwDispatcher = aKwDispatcher;
		desi = aDeserializer;
	}
	
	public void changeKeyWordDispatcher( IKeyWordDispatcher aKwDispatcher ) {
		kwDispatcher = aKwDispatcher;
	}

	/**
	 * Parses the given serialization for the keyword that indicates which type
	 * of node was serialized.
	 * 
	 * @param aSerializedNode
	 * a serialized node as a String
	 * @return The identifying keyword from the serialized node or null if the
	 *         string could not be parsed
	 */
	public String[] parseKeywordFromSeri(String aSerializedNode) {
		String[] result = new String[2]; // first is the keyword second is the rest
		
		// if the string is null or to short this method is not able to create a
		// node
		if (aSerializedNode == null || aSerializedNode.length() < 4) {
			return null;
		}

		int startIdx = aSerializedNode.indexOf(kwDispatcher.getKeyWordMarker());

		// if there is no serialization keyword the string is malformed
		if (startIdx == -1) {
			return null;
		}
		
		// find the closing
		// this is faster with finding the end but may fail if we combine abstraction with serialization
		int bigCloseTag = aSerializedNode.lastIndexOf( kwDispatcher.getBigGroupEnd() );
		
		if( bigCloseTag == -1 ) {
			throw new IllegalArgumentException( "The abstraction " + aSerializedNode + " had no valid closing tag after index " + startIdx );
		}
		
		int keyWordEndIdx = aSerializedNode.indexOf( kwDispatcher.getIdMarker(), startIdx + 1 );
		
		if( keyWordEndIdx == -1 ) {
			keyWordEndIdx = bigCloseTag;
		}

		result[0] = aSerializedNode.substring( startIdx, keyWordEndIdx );
		
		if ( keyWordEndIdx != bigCloseTag ) {
			// there are children that can be cut out
			// this includes the start and end keyword for the child groups
			result[1] = aSerializedNode.substring( keyWordEndIdx + 1, bigCloseTag );
		} else {
			// no children, no cutting
			result[1] = null;
		}

		return result;
	}
	
	/**
	 * Searches for all child data objects in the given string which are
	 * identified by a starting and closing group symbol on the right depth
	 * @param aSeriChildData The child data from the language model
	 * @return All child data after cutting and putting into an array
	 */
	public List<String> cutChildData( String aSeriChildData ) {
		if( aSeriChildData == null || aSeriChildData.length() == 0 ) {
			return null;
		}
		
		List<String> allChildren = new ArrayList<String>();

		int depth = 0;
		int startIdx = 0;
		
		for( int idx = 0; idx < aSeriChildData.length(); ++idx ) {
			switch( aSeriChildData.charAt( idx ) ) {
				case IBasicMapper.GROUP_START : if( ++depth == 1 ) { // mark this only if it starts a group at depth 1
									startIdx = idx+1; 
								}; break; 
				case IBasicMapper.GROUP_END : if ( --depth == 0 ) { // this may add empty strings to the result set which is fine
									allChildren.add( aSeriChildData.substring( startIdx, idx ) );
									startIdx = idx +1; 
								}; break;
				default : break;
			}
		}
		
		return allChildren;
	}
	
	/**
	 * Very much like the cutChildData but the cutting is triggered by the big group symbols instead
	 * of the small ones and the brackets are kept for further investigation
	 * @param aSeriChildData The child data from the language model
	 * @return All child data after cutting and putting into an array
	 */
	public List<String> cutTopLevelNodes( String aSeriChildData ) {
		if( aSeriChildData == null || aSeriChildData.length() == 0 ) {
			return null;
		}
		
		List<String> allChildren = new ArrayList<String>();

		int depth = 0;
		int startIdx = 0;
		
		for( int idx = 0; idx < aSeriChildData.length(); ++idx ) {
			switch( aSeriChildData.charAt( idx ) ) {
				case IBasicMapper.BIG_GROUP_START : if( ++depth == 1 ) { // mark this only if it starts a group at depth 1
									startIdx = idx; 
								}; break; 
				case IBasicMapper.BIG_GROUP_END : if ( --depth == 0 ) { 
									allChildren.add( aSeriChildData.substring( startIdx, idx+1 ) );
									startIdx = idx +1;
								}; break;
				default : break;
			}
		}
		
		return allChildren;
	}
	
	public List<BodyDeclaration> getBodyDeclaratorListFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		List<BodyDeclaration> result = new ArrayList<BodyDeclaration>();

		List<String> allPars = cutTopLevelNodes( aSerializedNode );
		for( String s : allPars ) {
			String[] parsedKW = parseKeywordFromSeri( s );
			// depending on the instance of the expression a different node has to be created
			// but it will always be some kind of expression
			BodyDeclaration t = (BodyDeclaration) kwDispatcher.dispatchAndDesi( parsedKW[0], parsedKW[1], desi);
			result.add( t );
		}
		return result;
	}
	
	public List<ReferenceType> getReferenceTypeListFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		List<ReferenceType> result = new ArrayList<ReferenceType>();

		List<String> allPars = cutTopLevelNodes( aSerializedNode );
		for( String s : allPars ) {
			String[] parsedKW = parseKeywordFromSeri( s );
			// depending on the instance of the expression a different node has to be created
			// but it will always be some kind of expression
			ReferenceType t = (ReferenceType) kwDispatcher.dispatchAndDesi( parsedKW[0], parsedKW[1], desi);
			result.add( t );
		}
		return result;
	}
	
	public ClassOrInterfaceType getCITypeFromFullScopeMapping( String aSerializedScope ) {
		
		if( aSerializedScope == null || aSerializedScope.length() == 0 ) {
			return null;
		}
		
		ClassOrInterfaceType result = new ClassOrInterfaceType();
		
		String name = aSerializedScope;
		ClassOrInterfaceType scope = null;
		
		int idxNameStart = aSerializedScope.lastIndexOf( "." );
		
		if( idxNameStart != -1 ) {
			name = aSerializedScope.substring( idxNameStart + 1 );
			scope = getCITypeFromFullScopeMapping( aSerializedScope.substring( 0, idxNameStart ) );
		}
		
		result.setName( name );
		result.setScope( scope );
		return result;
	}
	
	
	
	public TypeArguments getTypeArgumentsFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		List<Type> types = new ArrayList<Type>();

		List<String> allPars = cutTopLevelNodes( aSerializedNode );
		for( String s : allPars ) {
			String[] parsedKW = parseKeywordFromSeri( s );
			// depending on the instance of the expression a different node has to be created
			// but it will always be some kind of expression
			Type t = (Type) kwDispatcher.dispatchAndDesi( parsedKW[0], parsedKW[1], desi);
			types.add( t );
		}
		
		TypeArguments result = null;
		
		// this style of construction should prevent a types argument object that has arguments and the diamond flag
		if( types.isEmpty() ) {
			result = TypeArguments.withDiamondOperator();
		} else {
			result = TypeArguments.withArguments( types );
		}
	
		return result;
	}
	
}
