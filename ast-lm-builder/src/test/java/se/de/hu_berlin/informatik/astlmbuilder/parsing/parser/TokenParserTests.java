package se.de.hu_berlin.informatik.astlmbuilder.parsing.parser;

import java.util.EnumSet;

import org.junit.Test;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleExportsStmt;
import com.github.javaparser.ast.modules.ModuleOpensStmt;
import com.github.javaparser.ast.modules.ModuleStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;

import junit.framework.TestCase;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstants;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstantsShort;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IBasicNodeMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.Node2AbstractionMapper;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class TokenParserTests extends TestCase {

	private static int testDepth = 3;
	
	ITokenParser t_parser_long = new SimpleTokenParser(new KeyWordConstants());
	IBasicNodeMapper<String> mapper_long = new Node2AbstractionMapper.Builder(new KeyWordConstants())
			.usesStringAndCharAbstraction()
//			.usesVariableNameAbstraction()
//			.usesPrivateMethodAbstraction()
//			.usesClassNameAbstraction()
//			.usesMethodNameAbstraction()
//			.usesGenericTypeNameAbstraction()
			.build();
	
	ITokenParser t_parser_short = new SimpleTokenParser(new KeyWordConstantsShort());
	IBasicNodeMapper<String> mapper_short = new Node2AbstractionMapper.Builder(new KeyWordConstantsShort())
			.usesStringAndCharAbstraction()
//			.usesVariableNameAbstraction()
//			.usesPrivateMethodAbstraction()
//			.usesClassNameAbstraction()
//			.usesMethodNameAbstraction()
//			.usesGenericTypeNameAbstraction()
			.build();
	
	@Test
	public void testTokenParserAnnotationDeclarationParent() {
		testTokenParserAnnotationDeclaration(mapper_short, t_parser_short);
		testTokenParserAnnotationDeclaration(mapper_long, t_parser_long);
	}
	
	private void testTokenParserAnnotationDeclaration(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// SimpleName name
		// NodeList<BodyDeclaration<?>> members
		Node node = new AnnotationDeclaration(
				EnumSet.of(Modifier.PUBLIC, Modifier.VOLATILE),
				new NodeList<AnnotationExpr>(),
				new SimpleName("TestAnnotationDeclaration"), 
				new NodeList<BodyDeclaration<?>>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, testDepth);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof AnnotationDeclaration);
		
		AnnotationDeclaration castedNode = (AnnotationDeclaration) parsedNode;
		
		assertTrue(castedNode.getNameAsString().equals("TestAnnotationDeclaration"));
		assertTrue(castedNode.getModifiers().contains(Modifier.PUBLIC));
		assertTrue(castedNode.getModifiers().contains(Modifier.VOLATILE));
		assertFalse(castedNode.getModifiers().contains(Modifier.FINAL));
		assertTrue(castedNode.getModifiers().size() == 2);
		
		assertNotNull( castedNode.getAnnotations() );
		assertNotNull( castedNode.getMembers() );
	}
	
	@Test
	public void testTokenParserAnnotationMemberDeclarationParent() {
		testTokenParserAnnotationMemberDeclaration(mapper_short, t_parser_short);
		testTokenParserAnnotationMemberDeclaration(mapper_long, t_parser_long);
	}
	
	private void testTokenParserAnnotationMemberDeclaration(IBasicNodeMapper<String> mapper, ITokenParser parser) {
	
		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// Type type
		// SimpleName name
		// Expression defaultValue
		Node node = new AnnotationMemberDeclaration(
				EnumSet.of(Modifier.PUBLIC, Modifier.VOLATILE),
				new NodeList<AnnotationExpr>(),
				new PrimitiveType(),
				new SimpleName("TestAnnotationMemberDeclaration"), 
				new NormalAnnotationExpr());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, testDepth);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof AnnotationMemberDeclaration);
		
		AnnotationMemberDeclaration castedNode = (AnnotationMemberDeclaration) parsedNode;
		
		assertTrue(castedNode.getNameAsString().equals("TestAnnotationMemberDeclaration"));
		assertTrue(castedNode.getModifiers().contains(Modifier.PUBLIC));
		assertTrue(castedNode.getModifiers().contains(Modifier.VOLATILE));
		assertFalse(castedNode.getModifiers().contains(Modifier.FINAL));
		assertTrue(castedNode.getModifiers().size() == 2);
		
		assertNotNull( castedNode.getAnnotations() );
		assertNotNull( castedNode.getType() );
		assertNotNull( castedNode.getDefaultValue() );
	}
	
	@Test
	public void testTokenParserArrayAccessExprParent() {
		testTokenParserArrayAccessExpr(mapper_short, t_parser_short);
		testTokenParserArrayAccessExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserArrayAccessExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Expression name
		// Expression index
		Node node = new ArrayAccessExpr(
				new NameExpr( new SimpleName( "TestArrayAccessExpr") ),
				new FieldAccessExpr());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, testDepth);
		Log.out(this, token); //output the token for debugging purposes

		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ArrayAccessExpr);
		
		ArrayAccessExpr castedNode = (ArrayAccessExpr) parsedNode;
		
		assertNotNull( castedNode.getIndex() );
		assertNotNull( castedNode.getName() );
		assertTrue( ((NameExpr) castedNode.getName() ).getNameAsString().equals("TestArrayAccessExpr") );
	}
	
	@Test
	public void testTokenParserArrayCreationExprParent() {
		testTokenParserArrayCreationExpr(mapper_short, t_parser_short);
		testTokenParserArrayCreationExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserArrayCreationExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Type elementType
		// NodeList<ArrayCreationLevel> levels
		// ArrayInitializerExpr initializer
		Node node =  new ArrayCreationExpr(
						new PrimitiveType( Primitive.INT ), 
						new NodeList<ArrayCreationLevel>(), 
						new ArrayInitializerExpr());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, testDepth);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ArrayCreationExpr);
		
		ArrayCreationExpr castedNode = (ArrayCreationExpr) parsedNode;
		
		assertNotNull( castedNode.getElementType() );
		assertNotNull( castedNode.getLevels() );
		assertNotNull( castedNode.getInitializer() );
	}
	
	@Test
	public void testTokenParserArrayCreationLevelParent() {
		testTokenParserArrayCreationLevel(mapper_short, t_parser_short);
		testTokenParserArrayCreationLevel(mapper_long, t_parser_long);
	}
	
	private void testTokenParserArrayCreationLevel(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Expression dimension
		// NodeList<AnnotationExpr> annotations
		Node node = new ArrayCreationLevel(
					new NameExpr( "DimensionNameExpressionForTest"), 
					new NodeList<AnnotationExpr>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, testDepth);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ArrayCreationLevel);
		
		ArrayCreationLevel castedNode = (ArrayCreationLevel) parsedNode;
		
		assertNotNull( castedNode.getDimension() );
		assertNotNull( castedNode.getAnnotations() );
	}
	
	@Test
	public void testTokenParserModuleDeclarationParent() {
		testTokenParserModuleDeclaration(mapper_short, t_parser_short);
		testTokenParserModuleDeclaration(mapper_long, t_parser_long);
	}
	
	private void testTokenParserModuleDeclaration(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// NodeList<AnnotationExpr> annotations
		// Name name
		// boolean isOpen
		// NodeList<ModuleStmt> moduleStmts
		Node node = new ModuleDeclaration( 
					new NodeList<AnnotationExpr>(), 
					new Name( "TestModuleDeclaration" ),
					false,
					new NodeList<ModuleStmt>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, testDepth);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ModuleDeclaration);
		
		ModuleDeclaration castedNode = (ModuleDeclaration) parsedNode;
		
		assertNotNull( castedNode.getAnnotations() );
		assertNotNull( castedNode.getNameAsString().equals( "TestModuleDeclaration" ) );
		assertFalse( castedNode.isOpen() );
		assertNotNull( castedNode.getModuleStmts() );
	}
	
	@Test
	public void testTokenParserModuleExportsStmtParent() {
		testTokenParserModuleExportsStmt(mapper_short, t_parser_short);
		testTokenParserModuleExportsStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserModuleExportsStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Name name
		// NodeList<Name> moduleNames
		Node node = new ModuleExportsStmt( 
				new Name( "TestModuleExportsStmt" ),
				new NodeList<Name>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, testDepth);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ModuleExportsStmt);
		
		ModuleExportsStmt castedNode = (ModuleExportsStmt) parsedNode;
		
		assertNotNull( castedNode.getModuleNames() );
		assertNotNull( castedNode.getNameAsString().equals( "TestModuleExportsStmt" ) );
	}
	
	@Test
	public void testTokenParserModuleOpensStmtParent() {
		testTokenParserModuleOpensStmt(mapper_short, t_parser_short);
		testTokenParserModuleOpensStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserModuleOpensStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Name name
		// NodeList<Name> moduleNames
		Node node = new ModuleOpensStmt( 
				new Name( "TestModuleOpensStmt" ),
				new NodeList<Name>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, testDepth);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ModuleOpensStmt);
		
		ModuleOpensStmt castedNode = (ModuleOpensStmt) parsedNode;
		
		assertNotNull( castedNode.getModuleNames() );
		assertNotNull( castedNode.getNameAsString().equals( "TestModuleOpensStmt" ) );
	}
	
//	@Test
//	public void testTokenParserModuleProvidesStmtParent() {
//		testTokenParserModuleProvidesStmt(mapper_short, t_parser_short);
//		testTokenParserModuleProvidesStmt(mapper_long, t_parser_long);
//	}
//	
//	private void testTokenParserModuleProvidesStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
//		
//		// Type type TODO what type of type should this be?
//		// NodeList<Type> withTypes
//		return new ModuleProvidesStmt( 
//				new Type(),
//				new NodeList<Type>());
//		
//		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
//		String token = mapper.getMappingForNode(node, testDepth);
//		Log.out(this, token); //output the token for debugging purposes
//	
//		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
//		
//		Node parsedNode = parser.parseNodeFromToken(token, info);
//		
//		assertTrue(parsedNode instanceof ModuleProvidesStmt);
//		
//		ModuleProvidesStmt castedNode = (ModuleProvidesStmt) parsedNode;
//		
//		assertNotNull( castedNode.getModuleNames() );
//		assertNotNull( castedNode.getNameAsString().equals( "TestModuleOpensStmt" ) );
//	}
	
	
	@Test
	public void testTokenParserConstructorDeclarationParent() {
		testTokenParserConstructorDeclaration(mapper_short, t_parser_short);
		testTokenParserConstructorDeclaration(mapper_long, t_parser_long);
	}
	
	//reuse testing methods for short AND long keywords
	private void testTokenParserConstructorDeclaration(IBasicNodeMapper<String> mapper, ITokenParser parser) {
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
		String token = mapper.getMappingForNode(node, testDepth);
		Log.out(this, token); //output the token for debugging purposes
		//in the long keyword version, this produces:
		//(CONSTRUCTOR_DECLARATION,[33],[(#0,[])],[(#0,[])],[(SIMPLE_NAME,[TestName])],[(#0,[])],[(#0,[])],[(BLOCK_STMT,[(#0,[])])])
		
		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ConstructorDeclaration);
		
		ConstructorDeclaration castedNode = (ConstructorDeclaration) parsedNode;
		
		assertTrue(castedNode.getNameAsString().equals("TestName"));
		assertTrue(castedNode.getModifiers().contains(Modifier.PUBLIC));
		assertTrue(castedNode.getModifiers().contains(Modifier.FINAL));
		assertTrue(castedNode.getModifiers().size() == 2);
	}
	
	@Test
	public void testTokenParserEnumConstantDeclarationParent() {
		testTokenParserEnumConstantDeclaration(mapper_short, t_parser_short);
		testTokenParserEnumConstantDeclaration(mapper_long, t_parser_long);
	}
	
	private void testTokenParserEnumConstantDeclaration(IBasicNodeMapper<String> mapper, ITokenParser parser) {
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
		String token = mapper.getMappingForNode(node, testDepth);
		Log.out(this, token); //output the token for debugging purposes
		//in the long keyword version, this produces:
		//(ENUM_CONSTANT_DECLARATION,[(#0,[])],[(SIMPLE_NAME,[TestName])],[(#0,[])],[(#1,[(FIELD_DECLARATION,[1],[#0],[#0])])])

		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
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
