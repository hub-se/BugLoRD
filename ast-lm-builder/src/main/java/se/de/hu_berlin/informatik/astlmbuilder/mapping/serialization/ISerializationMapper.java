package se.de.hu_berlin.informatik.astlmbuilder.mapping.serialization;

import java.util.List;

import com.github.javaparser.ast.Node;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IBasicKeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IMapper;

/**
 * Adds the functionality of serialization to the mapper
 *
 */
@Deprecated
public interface ISerializationMapper extends IMapper<String> {
	
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
	 * <p> format: {@code %($id,[...],...,[...]);($id,[...],...,[...]);...;($id,[...],...,[...])}
	 * @param aNode The node that should be serialized
	 * @param sBuilder The string builder that will be filled with the correct serialization
	 * @param aSeriDepth The depth for the serialization. 0 means only the keyword for the node type
	 * 	will be included and for each level a layer of children is added.
	 */
	public default void serializeNode( Node aNode, StringBuilder aSBuilder, int aSeriDepth ) {
		if( aNode == null ) {
			return;
		}

		aSBuilder.append(IBasicKeyWords.BIG_GROUP_START);
		
		// get the keywords after the mapper did the dispatching and before it calls the mapping methods
		aSBuilder.append( IBasicKeyWords.KEYWORD_SERIALIZE + IMapper.super.getMappingForNode( aNode, 0 ) );
		
		List<Node> children = aNode.getChildNodes();
		if ( aSeriDepth != 0 && children != null && children.size() > 0 ) {
			
			//aSBuilder.append( getKeyWordProvider().getGroupStart() );
					
			// get the serialization for all children with one depth less
			int upperBound = getMaxSerializationChildren() < 0 ?
					children.size()
					: Math.min(getMaxSerializationChildren(), children.size());
			
			for( int i = 0; i < upperBound; ++i ) {
				serializeNode( children.get( i ), aSBuilder, aSeriDepth - 1 );
				// do not append the split symbol after the last child
				if ( i != upperBound - 1 ) {
					aSBuilder.append(IBasicKeyWords.SPLIT);
				}
			}
			
			//aSBuilder.append( getKeyWordProvider().getGroupEnd() );
		}
		
		aSBuilder.append(IBasicKeyWords.BIG_GROUP_END);
	}
	
}
