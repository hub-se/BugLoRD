package se.de.hu_berlin.informatik.astlmbuilder.parsing.parser;

import java.util.EnumSet;

import org.junit.Test;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import junit.framework.TestCase;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstants;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstantsShort;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IBasicNodeMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.Node2AbstractionMapper;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.parser.ITokenParserBasics;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.parser.TokenParser;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.parser.TokenParserShort;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class TokenParserTests extends TestCase {

	ITokenParserBasics t_parser_long = new TokenParser();
	IBasicNodeMapper<String> mapper_long = new Node2AbstractionMapper.Builder(new KeyWordConstants())
			.usesStringAndCharAbstraction()
//			.usesVariableNameAbstraction()
//			.usesPrivateMethodAbstraction()
//			.usesClassNameAbstraction()
//			.usesMethodNameAbstraction()
//			.usesGenericTypeNameAbstraction()
			.build();
	
	ITokenParserBasics t_parser_short = new TokenParserShort();
	IBasicNodeMapper<String> mapper_short = new Node2AbstractionMapper.Builder(new KeyWordConstantsShort())
			.usesStringAndCharAbstraction()
//			.usesVariableNameAbstraction()
//			.usesPrivateMethodAbstraction()
//			.usesClassNameAbstraction()
//			.usesMethodNameAbstraction()
//			.usesGenericTypeNameAbstraction()
			.build();

	
	@Test
	public void testTokenParserShort() {
		testTokenParserConstructorDeclaration(mapper_short, t_parser_short);
		testTokenParserEnumConstantDeclaration(mapper_short, t_parser_short);
	}
	
	@Test
	public void testTokenParserLong() {
		testTokenParserConstructorDeclaration(mapper_long, t_parser_long);
		testTokenParserEnumConstantDeclaration(mapper_long, t_parser_long);
	}
	
	//reuse testing methods for short AND long keywords
	private void testTokenParserConstructorDeclaration(IBasicNodeMapper<String> mapper, ITokenParserBasics parser) {
		//EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations, NodeList<TypeParameter> typeParameters, 
		//SimpleName name, NodeList<Parameter> parameters, NodeList<ReferenceType> thrownExceptions, BlockStmt body
		Node node = new ConstructorDeclaration(
				EnumSet.of(Modifier.PUBLIC, Modifier.FINAL),
				new NodeList<>(),
				new NodeList<>(),
				new SimpleName("TestName"),
				new NodeList<>(),
				new NodeList<>(),
				new BlockStmt(new NodeList<>()));
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, 3);
//		Log.out(this, token); //output the token for debugging purposes
		//in the long keyword version, this produces:
		//(CONSTRUCTOR_DECLARATION,[33],[(#0,[])],[(#0,[])],[(SIMPLE_NAME,[TestName])],[(#0,[])],[(#0,[])],[(BLOCK_STMT,[(#0,[])])])
		
		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
		
		Node parsedNode = parser.createNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ConstructorDeclaration);
		
		ConstructorDeclaration castedNode = (ConstructorDeclaration) parsedNode;
		
		assertTrue(castedNode.getNameAsString().equals("TestName"));
		assertTrue(castedNode.getModifiers().contains(Modifier.PUBLIC));
		assertTrue(castedNode.getModifiers().contains(Modifier.FINAL));
		assertTrue(castedNode.getModifiers().size() == 2);
	}
	
	private void testTokenParserEnumConstantDeclaration(IBasicNodeMapper<String> mapper, ITokenParserBasics parser) {
		NodeList<BodyDeclaration<?>> classBody = new NodeList<>();
		//EnumSet<Modifier> modifiers, NodeList<AnnotationExpr> annotations, NodeList<VariableDeclarator> variables
		classBody.add(new FieldDeclaration(
				EnumSet.of(Modifier.PUBLIC),
				new NodeList<>(),
				new NodeList<>()));
		
		// NodeList<AnnotationExpr> annotations,
				// SimpleName name
				// NodeList<Expression> arguments
				// NodeList<BodyDeclaration<?>> classBody
		Node node = new EnumConstantDeclaration(
				new NodeList<>(),
				new SimpleName("TestName"),
				new NodeList<>(),
				classBody);
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, 3);
		Log.out(this, token); //output the token for debugging purposes
		//in the long keyword version, this produces:
		//(ENUM_CONSTANT_DECLARATION,[(#0,[])],[(SIMPLE_NAME,[TestName])],[(#0,[])],[(#1,[(FIELD_DECLARATION,[1],[#0],[#0])])])

		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
		
		Node parsedNode = parser.createNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof EnumConstantDeclaration);
		
		EnumConstantDeclaration castedNode = (EnumConstantDeclaration) parsedNode;
		
		assertTrue(castedNode.getNameAsString().equals("TestName"));
		assertTrue(castedNode.getClassBody().size() == 1);
		
		assertTrue(castedNode.getClassBody().get(0) instanceof FieldDeclaration);
		
		FieldDeclaration castedFieldNode = (FieldDeclaration) castedNode.getClassBody().get(0);
		
		assertTrue(castedFieldNode.getModifiers().contains(Modifier.PUBLIC));
		assertTrue(castedFieldNode.getModifiers().size() == 1);
	}
	
}
