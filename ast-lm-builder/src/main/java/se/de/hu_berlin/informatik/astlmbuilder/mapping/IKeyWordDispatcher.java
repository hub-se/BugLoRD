package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import com.github.javaparser.ast.Node;

import se.de.hu_berlin.informatik.astlmbuilder.reader.IASTLMDesirializer;

public interface IKeyWordDispatcher {
	
	/**
	 * Creates a new node object for a given serialized string
	 * 
	 * @param aChildData
	 *            The keyword that the mapper used for the original node
	 * @return a node of the same type as the original one that got serialized
	 */
	public Node dispatchAndDesi( String aKeyWord, String aChildData, IASTLMDesirializer aDesi );
	
	public char getKeyWordBigGroupStart();
	public char getKeyWordBigGroupEnd();
	public char getKeyWordSerialize();
	public char getKeyWordAbstraction();
	public char getKeyWordSeparator();
	
}
