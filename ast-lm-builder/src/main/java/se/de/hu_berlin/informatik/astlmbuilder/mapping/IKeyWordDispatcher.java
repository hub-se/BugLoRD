package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import com.github.javaparser.ast.Node;

import se.de.hu_berlin.informatik.astlmbuilder.reader.IASTLMDeserializer;

public interface IKeyWordDispatcher extends IBasicMapper {
	
	/**
	 * Creates a new node object for a given serialized string
	 * @param aKeyWord
	 * the keyword that the mapper used for the original node
	 * @param aChildData
	 * the child data
	 * @param aDesi
	 * the deserializer to use
	 * @return a node of the same type as the original one that got serialized
	 */
	public Node dispatchAndDesi( String aKeyWord, String aChildData, IASTLMDeserializer aDesi );
	

}
