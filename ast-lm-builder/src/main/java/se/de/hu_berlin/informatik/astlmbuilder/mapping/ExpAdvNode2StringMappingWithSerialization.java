package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.javaparser.ast.Node;

/**
 * Adds the functionality of serialization to the experimental advanced node 2 string mapper
 *
 */
public class ExpAdvNode2StringMappingWithSerialization extends ExperimentalAdvancedNode2StringMapping {
	
	private SimpleNode2StringMapping<String> simpleMapper = new SimpleNode2StringMapping<String>();
	
	private int maxSeriChildren = 0;
	
	/**
	 * Constructor
	 * @param aMaxSerializationChildren The maximum number of children that should be
	 * included in the serialization of tokens. Use -1 to include all children for each node.
	 */
	public ExpAdvNode2StringMappingWithSerialization( int aMaxSerializationChildren ) {
		maxSeriChildren = aMaxSerializationChildren;
	}
	
	// if there is a second entry in the values array it is considered to be the
	// depth of the serialization
	protected int getSerializationDepth(Integer[] values) {
		if (values != null && values.length > 1) {
			return values[1];
		} else {
			return -1;
		}
	}
	
	/**
	 * Builds the serialization string for a given node
	 * @param aNode
	 * @param sBuilder
	 */
	private void serializeNode( Node aNode, StringBuilder aSBuilder, int aSeriDepth ) {
		if( aNode == null ) {
			return;
		}

		aSBuilder.append( BIG_GROUP_START );
		
		// get the keywords after the mapper did the dispatching and before it calls the mapping methods
		aSBuilder.append( KEYWORD_SERIALIZE + simpleMapper.getMappingForNode( aNode ).getMappings().get( 0 ) );
		
		List<Node> children = aNode.getChildrenNodes();
		if ( aSeriDepth != 0 && children != null && children.size() > 0 ) {
			
			aSBuilder.append( GROUP_START );
					
			// get the serialization for all children with one depth less
			int upperBound = maxSeriChildren == -1 ?
					children.size()
					: Math.min( maxSeriChildren, children.size());
			
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
	
	/**
	 * Adds serialization to the generation of tokens
	 */
	public MappingWrapper<String> getMappingForNode(Node aNode, Integer... values) {
		List<String> allStrings = new ArrayList<String>();
		
		// first create the keyword and the abstraction string
		if ( getAbstractionDepth( values ) != 0 ) {
			// use the method from the abstraction string mapper
			allStrings.addAll( super.getMappingForNode( aNode, values ).getMappings() );
		}
		
		StringBuilder result = new StringBuilder();
		
		// afterwards create the serialization string
		int depth = getSerializationDepth(values);
		if( depth != 0 ) { //still at a higher level of abstraction (either negative or greater than 0)
			serializeNode( aNode, result, depth );
		}
		
		// finalize the string and add it to the list for the mapping wrapper
		allStrings.add( result.toString() );
		
		return new MappingWrapper<String>( allStrings );
	}

}
