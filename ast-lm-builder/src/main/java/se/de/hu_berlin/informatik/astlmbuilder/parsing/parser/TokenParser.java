package se.de.hu_berlin.informatik.astlmbuilder.parsing.parser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstants;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.dispatcher.IKeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.dispatcher.SimpleDispatcher;

/**
 * A simple implementation of the token parser using the human readable keywords
 */
public class TokenParser implements ITokenParser {

	private IKeyWordProvider<String> kwp = new KeyWordConstants();
	private IKeyWordDispatcher dispatcher = new SimpleDispatcher(this);
	
	@Override
	public IKeyWordProvider<String> getKeyWordProvider() {
		return kwp;
	}

	@Override
	public <T extends Node> T guessNodeFromKeyWord(Class<T> expectedSuperClazz, String keyWord,
			InformationWrapper info) {
		// TODO implement
		return null;
	}

	@Override
	public <T extends Node> T guessNode(Class<T> expectedSuperClazz, InformationWrapper info) {
		// TODO implement
		return null;
	}

	@Override
	public <T extends Node> NodeList<T> guessList(Class<T> expectedSuperClazz, int listMemberCount, InformationWrapper info) {
		return new NodeList<>();
	}

	@Override
	public String guessMethodIdentifier(InformationWrapper info) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IKeyWordDispatcher getDispatcher() {
		return dispatcher;
	}

}
