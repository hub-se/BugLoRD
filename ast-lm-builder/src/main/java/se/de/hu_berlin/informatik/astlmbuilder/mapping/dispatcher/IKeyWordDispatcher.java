package se.de.hu_berlin.informatik.astlmbuilder.mapping.dispatcher;

import com.github.javaparser.ast.Node;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.IModifierHandler;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IBasicKeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.parser.ITokenParser;
import se.de.hu_berlin.informatik.astlmbuilder.parser.InformationWrapper;

public interface IKeyWordDispatcher extends IBasicKeyWords, IModifierHandler {
	
	/**
	 * Creates a new node object for a given token
	 * @param keyWord
	 * the keyword for choosing the node to create
	 * @param token
	 * the complete token
	 * @param info
	 * an object that holds relevant information about current variable scopes, etc.
	 * @param parser
	 * the parser to use
	 * @return
	 * the parsed node
	 * @param <T>
	 * the type of returned nodes
	 */
	public <T extends Node> T dispatch( String keyWord, String token, InformationWrapper info, ITokenParser parser );
	
}
