package se.de.hu_berlin.informatik.astlmbuilder.parser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstantsShort;
import se.de.hu_berlin.informatik.astlmbuilder.parser.dispatcher.IKeyWordDispatcher;

/**
 * A simple implementation of the token parser using the short keywords
 */
public class TokenParserShort implements IKeyWordDispatcher {

	IKeyWordProvider<String> kwp = new KeyWordConstantsShort();

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
	public NodeList<BodyDeclaration<?>> guessBodyDeclarationList(int listMemberCount, InformationWrapper info) {
		return new NodeList<>();
	}

	@Override
	public NodeList<TypeDeclaration<?>> guessTypeDeclarationList(int listMemberCount, InformationWrapper info) {
		return new NodeList<>();
	}

}
