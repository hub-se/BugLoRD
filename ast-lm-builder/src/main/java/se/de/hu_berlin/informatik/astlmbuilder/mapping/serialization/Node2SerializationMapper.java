package se.de.hu_berlin.informatik.astlmbuilder.mapping.serialization;

import se.de.hu_berlin.informatik.astlmbuilder.ASTLMBOptions;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.SimpleMapper;

/**
 * Adds the functionality of serialization to the experimental advanced node 2 string mapper
 *
 */
public class Node2SerializationMapper extends SimpleMapper<String> implements ISerializationMapper {
	
	final private int maxSeriChildren;
	
	/**
	 * Constructor without the setting for the maximum number of children to serialize.
	 * This is mostly used for testing.
	 * @param provider
	 * a keyword provider
	 */
	public Node2SerializationMapper(IKeyWordProvider<String> provider) {
		super(provider);
		// the parsing will not fail and needs no try/catch
		this.maxSeriChildren = Integer.parseInt( ASTLMBOptions.SERIALIZATION_MAX_CHILDREN_DEFAULT );
	}
	
	/**
	 * Constructor
	 * @param aMaxSerializationChildren 
	 * The maximum number of children that should be
	 * included in the serialization of tokens. Use -1 to include all children for each node.
	 * @param provider
	 * a keyword provider
	 */
	public Node2SerializationMapper(IKeyWordProvider<String> provider, int aMaxSerializationChildren ) {
		super(provider);
		this.maxSeriChildren = aMaxSerializationChildren;
	}

	@Override
	public int getMaxSerializationChildren() {
		return maxSeriChildren;
	}

}
