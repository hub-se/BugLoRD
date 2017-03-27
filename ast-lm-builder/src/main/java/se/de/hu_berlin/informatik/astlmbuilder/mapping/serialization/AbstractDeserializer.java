package se.de.hu_berlin.informatik.astlmbuilder.mapping.serialization;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.Node;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.dispatcher.IKeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.dispatcher.KeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.dispatcher.KeyWordDispatcherShort;
import se.de.hu_berlin.informatik.astlmbuilder.parser.ITokenParser;

public abstract class AbstractDeserializer implements ITokenParser {

	private IKeyWordDispatcher kwDispatcher;
	
	/**
	 * The constructor initializes with the given keyword dispatcher
	 * @param kwDispatcher
	 */
	public AbstractDeserializer(IKeyWordDispatcher kwDispatcher) {
		this.kwDispatcher = kwDispatcher;
	}
	
	/**
	 * In case the language model was created using the short keywords
	 */
	public void useShortKeywords() {
		kwDispatcher = new KeyWordDispatcherShort();
	}
	
	/**
	 * In case the language model was created using the long keywords
	 */
	public void useLongKeywords() {
		kwDispatcher = new KeyWordDispatcher();
	}
	
	/**
	 * All nodes need to create their children objects and check their properties afterwards
	 * @param aNode
	 * a node
	 * @param aSeriChildren
	 * serialized children?
	 */
	public void deserializeAllChildren( Node aNode, String aSeriChildren ) {
		// check if there are children to add
		if( aSeriChildren != null ) {
			List<String> childSeris = getAllChildNodesFromSeri(aSeriChildren);
			
			for( String singleChild : childSeris ) {
				aNode.getChildNodes().add( deserializeNode( singleChild ) );
			}
		}
	}
	
	/**
	 * Searches for all top level child nodes in the given string and splits the serialization
	 * @param aSerializedNode
	 * a serialized node as a string
	 * @return A list with all child nodes of this serialization or null if something went wrong
	 */
	public List<String> getAllChildNodesFromSeri( String aSerializedNode ) {
		List<String> result = new ArrayList<String>();
		
		int depth = 0;
		int lastStart = 0;

		for ( int i = 0; i < aSerializedNode.length(); ++i ) {
			if ( aSerializedNode.charAt( i ) == kwDispatcher.getGroupStart() ) {
				if ( depth == 0 ) {
					lastStart = i;
				}
				++depth;
			} else if ( aSerializedNode.charAt( i ) == kwDispatcher.getGroupEnd() ) {
				--depth;
				if( depth == 0 ) {
					result.add( aSerializedNode.substring( lastStart, i+1 ) );
				}
			}

		}
		
		return result.size() > 0 ? result : null;
	}

	/**
	 * Creates a new node object for a given serialized string
	 * 
	 * @param aSerializedString
	 * a serialized string
	 * @return a node of the same type as the original one that got serialized
	 */
	public Node deserializeNode(String aSerializedString ) {

		// this name is awful but the result is the one keyword and maybe the child string...
		String[] parsedParts = parseKeywordFromSeri( aSerializedString );
		String keyword = parsedParts[0];
		String childDataStr = parsedParts[1];
		
		if( keyword == null ) {
			return null;
		}

		return kwDispatcher.dispatchAndDesi( keyword, childDataStr, this );
	}
	
	/**
	 * Parses the given serialization for the keyword that indicates which type
	 * of node was serialized.
	 * 
	 * @param aSerializedNode
	 * a serialization string
	 * @return The identifying keyword from the serialized node or null if the
	 *         string could not be parsed
	 */
	public String[] parseKeywordFromSeri(String aSerializedNode) {
		String[] result = new String[2]; // first is the keyword second is the rest
		
		// if the string is null or to short this method is not able to create a
		// node
		if (aSerializedNode == null || aSerializedNode.length() < 6) {
			return null;
		}

		int startIdx = aSerializedNode.indexOf(kwDispatcher.getKeyWordSerialize());

		// if there is no serialization keyword the string is malformed
		if (startIdx == -1) {
			return null;
		}
		
		// find the closing
		//
		int bigCloseTag = aSerializedNode.lastIndexOf( kwDispatcher.getBigGroupEnd() );
		
		if( bigCloseTag == -1 ) {
			throw new IllegalArgumentException( "The serialization " + aSerializedNode + " had no valid closing tag after index " + startIdx );
		}

		int keywordEndIdx = bigCloseTag;
		
		// the end is not the closing group tag if this node had children which is indicated by a new group
		int childGroupStartTag = aSerializedNode.indexOf( kwDispatcher.getGroupStart(), startIdx + 1 );
		
		if( childGroupStartTag != -1 ) {
			keywordEndIdx = childGroupStartTag - 1;
		}

		result[0] = aSerializedNode.substring( startIdx + 1, keywordEndIdx );
		
		if ( childGroupStartTag != -1 ) {
			// there are children that can be cut out
			// this includes the start and end keyword for the child groups
			result[1] = aSerializedNode.substring( keywordEndIdx, bigCloseTag );
		} else {
			// no children, no cutting
			result[1] = null;
		}

		return result;
	}
	

}
