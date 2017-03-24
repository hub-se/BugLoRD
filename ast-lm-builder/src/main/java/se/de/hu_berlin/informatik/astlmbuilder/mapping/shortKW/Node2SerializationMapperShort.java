package se.de.hu_berlin.informatik.astlmbuilder.mapping.shortKW;

import se.de.hu_berlin.informatik.astlmbuilder.ASTLMBOptions;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ISerializationMapper;

/**
 * Adds the functionality of serialization to the experimental advanced node 2 string mapper
 *
 */
public class Node2SerializationMapperShort extends KeyWordConstantsShort implements ISerializationMapper {
	
	private int maxSeriChildren = 0;
	
	/**
	 * Constructor without the setting for the maximum number of children to serialize.
	 * This is mostly used for testing.
	 */
	public Node2SerializationMapperShort() {
		// the parsing will not fail and needs no try/catch
		maxSeriChildren = Integer.parseInt( ASTLMBOptions.SERIALIZATION_MAX_CHILDREN_DEFAULT );
	}
	
	/**
	 * Constructor
	 * @param aMaxSerializationChildren The maximum number of children that should be
	 * included in the serialization of tokens. Use -1 to include all children for each node.
	 */
	public Node2SerializationMapperShort( int aMaxSerializationChildren ) {
		maxSeriChildren = aMaxSerializationChildren;
	}

	@Override
	public int getMaxSerializationChildren() {
		return maxSeriChildren;
	}	
	
}
