package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.Collection;
import java.util.List;

import com.github.javaparser.ast.Node;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITokenMapper;

/**
 * Adds the functionality of serialization to the mapper
 *
 */
public interface ISerializationMapper extends ITokenMapper<String> {
	
	public int getMaxSerializationChildren();
	
	/**
	 * Adds serialization to the generation of tokens
	 */
	@Override
	public default String getMappingForNode(Node aNode, int aSeriDepth) {
		
		StringBuilder result = new StringBuilder();
		serializeNode( aNode, result, aSeriDepth );
		
		return result.toString();
	}
	
	/**
	 * Builds the serialization string for a given node.
	 * This works recursive and stores the results in the StringBuilder object
	 * 
	 * @param aNode The node that should be serialized
	 * @param sBuilder The string builder that will be filled with the correct serialization
	 * @param aSeriDepth The depth for the serialization. 0 means only the keyword for the node type
	 * 	will be included and for each level a layer of children is added.
	 */
	public default void serializeNode( Node aNode, StringBuilder aSBuilder, int aSeriDepth ) {
		if( aNode == null ) {
			return;
		}

		aSBuilder.append( BIG_GROUP_START );
		
		// get the keywords after the mapper did the dispatching and before it calls the mapping methods
		aSBuilder.append( KEYWORD_SERIALIZE + ITokenMapper.super.getMappingForNode( aNode, aSeriDepth ) );
		
		List<Node> children = aNode.getChildrenNodes();
		if ( aSeriDepth != 0 && children != null && children.size() > 0 ) {
			
			aSBuilder.append( GROUP_START );
					
			// get the serialization for all children with one depth less
			int upperBound = getMaxSerializationChildren() == -1 ?
					children.size()
					: Math.min(getMaxSerializationChildren(), children.size());
			
			for( int i = 0; i < upperBound; ++i ) {
				serializeNode( children.get( i ), aSBuilder, aSeriDepth - 1 );
				// do not append the split symbol after the last child
				if ( i != upperBound - 1 ) {
					aSBuilder.append( SPLIT );
				}
			}
			
			aSBuilder.append( GROUP_END );
		}
		
		aSBuilder.append( BIG_GROUP_END );
	}

	@Override
	public default void setPrivMethodBlackList(Collection<String> aBL) {
		// this is not needed by the serialization
	}

	@Override
	public default void clearPrivMethodBlackList() {
		// this is not needed by the serialization
	}
	
}
