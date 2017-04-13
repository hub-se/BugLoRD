package se.de.hu_berlin.informatik.astlmbuilder.parsing.parser;

import java.util.EnumSet;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.dispatcher.IKeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.dispatcher.SimpleDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.guesser.INodeGuesser;

/**
 * A simple implementation of the token parser using the human readable keywords
 */
public class SimpleTokenParser implements ITokenParser, INodeGuesser {

	final private IKeyWordProvider<String> kwp;
	final private IKeyWordDispatcher dispatcher = new SimpleDispatcher(this, this);
	
	public SimpleTokenParser(IKeyWordProvider<String> kwp) {
		this.kwp = kwp;
	}
	
	@Override
	public IKeyWordProvider<String> getKeyWordProvider() {
		return kwp;
	}
	
	@Override
	public IKeyWordDispatcher getDispatcher() {
		return dispatcher;
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
	public <T extends Node> NodeList<T> guessList(Class<T> expectedSuperClazz, InformationWrapper info) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeList<BodyDeclaration<?>> guessBodyDeclarationList(InformationWrapper info) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeList<TypeDeclaration<?>> guessTypeDeclarationList(InformationWrapper info) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnumSet<Modifier> guessModifiers(InformationWrapper info) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean guessBoolean(InformationWrapper info) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String guessStringValue(InformationWrapper info) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Primitive guessPrimitive(InformationWrapper info) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Operator guessAssignOperator(InformationWrapper info) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public com.github.javaparser.ast.expr.UnaryExpr.Operator guessUnaryOperator(InformationWrapper info) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public com.github.javaparser.ast.expr.BinaryExpr.Operator guessBinaryOperator(InformationWrapper info) {
		// TODO Auto-generated method stub
		return null;
	}

}
