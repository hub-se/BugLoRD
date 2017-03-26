package se.de.hu_berlin.informatik.astlmbuilder.mapping.dispatcher;

import com.github.javaparser.ast.Node;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.IModifierHandler;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IBasicKeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.reader.IASTLMDeserializer;

public interface IKeyWordDispatcher extends IBasicKeyWords, IModifierHandler {
	
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
