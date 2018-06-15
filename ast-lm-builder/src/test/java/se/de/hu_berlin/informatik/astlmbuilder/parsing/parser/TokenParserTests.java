package se.de.hu_berlin.informatik.astlmbuilder.parsing.parser;

import static com.github.javaparser.JavaParser.parseName;

import java.util.EnumSet;

import org.junit.Test;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleExportsStmt;
import com.github.javaparser.ast.modules.ModuleOpensStmt;
import com.github.javaparser.ast.modules.ModuleProvidesStmt;
import com.github.javaparser.ast.modules.ModuleRequiresStmt;
import com.github.javaparser.ast.modules.ModuleStmt;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import junit.framework.TestCase;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstants;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstantsShort;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IBasicNodeMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.Node2AbstractionMapper;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InfoWrapperBuilder;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class TokenParserTests extends TestCase {

	private static int testDepth = -1;
	
	ITokenParser t_parser_long = new SimpleTokenParser(new KeyWordConstants());
	IBasicNodeMapper<String> mapper_long = new Node2AbstractionMapper.Builder(new KeyWordConstants())
			.usesStringAbstraction()
//			.usesVariableNameAbstraction()
//			.usesPrivateMethodAbstraction()
//			.usesClassNameAbstraction()
//			.usesMethodNameAbstraction()
//			.usesGenericTypeNameAbstraction()
			.build();
	
	ITokenParser t_parser_short = new SimpleTokenParser(new KeyWordConstantsShort());
	IBasicNodeMapper<String> mapper_short = new Node2AbstractionMapper.Builder(new KeyWordConstantsShort())
			.usesStringAbstraction()
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
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
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
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
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
				new NameExpr( "TestArrayAccessExpr" ),
				new FieldAccessExpr());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes

		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
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
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ArrayCreationExpr);
		
		ArrayCreationExpr castedNode = (ArrayCreationExpr) parsedNode;
		
		assertNotNull( castedNode.getElementType() );
		assertNotNull( castedNode.getLevels() );
		assertNotNull( castedNode.getInitializer() );
	}
	
	@Test
	public void testTokenParserArrayTypeParent() {
		testTokenParserArrayType(mapper_short, t_parser_short);
		testTokenParserArrayType(mapper_long, t_parser_long);
	}
	
	private void testTokenParserArrayType(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Type componentType
		// NodeList<AnnotationExpr> annotations
		Node node =  new ArrayType(
						new PrimitiveType( Primitive.INT ), 
						new NodeList<AnnotationExpr>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ArrayType);
		
		ArrayType castedNode = (ArrayType) parsedNode;
		
		assertNotNull( castedNode.getElementType() );
		assertTrue( castedNode.getElementType() instanceof PrimitiveType );
		assertTrue(((PrimitiveType) castedNode.getElementType()).getType() == Primitive.INT );
		assertNotNull( castedNode.getAnnotations() );

	}

	@Test
	public void testTokenParserAssertStmtParent() {
		testTokenParserAssertStmt(mapper_short, t_parser_short);
		testTokenParserAssertStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserAssertStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Expression check
		// final Expression message
		Node node =  new AssertStmt(
						new BinaryExpr(), 
						new StringLiteralExpr( "Assert Failed"));
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof AssertStmt);
		
		AssertStmt castedNode = (AssertStmt) parsedNode;
		
		assertNotNull( castedNode.getCheck() );
		assertTrue( castedNode.getCheck() instanceof BinaryExpr );
		assertNotNull( castedNode.getMessage() );
		assertTrue( castedNode.getMessage().isPresent() );
		// who thinks this optional object is a helpful invention? -.-
		assertTrue( castedNode.getMessage().get() instanceof StringLiteralExpr );
		
		// the value of the message will not be stored and replaced in the parsing process by something else
	}
	
	@Test
	public void testTokenParserAssignExprParent() {
		testTokenParserAssignExpr(mapper_short, t_parser_short);
		testTokenParserAssignExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserAssignExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Expression target
		// Expression value
		// Operator operator
		Node node =  new AssignExpr(
						new VariableDeclarationExpr(), 
						new NameExpr( "SomethingWithValue" ), 
						AssignExpr.Operator.ASSIGN );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof AssignExpr);
		
		AssignExpr castedNode = (AssignExpr) parsedNode;
		
		assertNotNull( castedNode.getTarget() );
		assertTrue( castedNode.getTarget() instanceof VariableDeclarationExpr );
		assertNotNull( castedNode.getValue() );
		assertTrue( castedNode.getValue() instanceof NameExpr );
		assertTrue( ((NameExpr) castedNode.getValue()).getNameAsString().equals( "SomethingWithValue" ));
		assertNotNull( castedNode.getOperator() );
		assertTrue( castedNode.getOperator() == Operator.ASSIGN );
	}	

	@Test
	public void testTokenParserBinaryExprParent() {
		testTokenParserBinaryExpr(mapper_short, t_parser_short);
		testTokenParserBinaryExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserBinaryExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Expression left
		// Expression right
		// Operator operator
		Node node =  new BinaryExpr(
						new IntegerLiteralExpr( "47" ), 
						new IntegerLiteralExpr( "11" ), 
						BinaryExpr.Operator.GREATER );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof BinaryExpr);
		
		BinaryExpr castedNode = (BinaryExpr) parsedNode;
		
		assertNotNull( castedNode.getLeft() );
		assertTrue( castedNode.getLeft() instanceof IntegerLiteralExpr );
		assertTrue( ((IntegerLiteralExpr) castedNode.getLeft()).getValue().equals( "47" ));
		assertNotNull( castedNode.getRight() );
		assertTrue( castedNode.getRight() instanceof IntegerLiteralExpr );
		assertTrue( ((IntegerLiteralExpr) castedNode.getRight()).getValue().equals( "11" ));
		assertNotNull( castedNode.getOperator() );
		assertTrue( castedNode.getOperator() == BinaryExpr.Operator.GREATER );
		
	}
	
	@Test
	public void testTokenParserExpressionStmtParent() {
		testTokenParserExpressionStmt(mapper_short, t_parser_short);
		testTokenParserExpressionStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserExpressionStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//final Expression expression
		Node node =  new ExpressionStmt(
						new EnclosedExpr());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ExpressionStmt);
		
		ExpressionStmt castedNode = (ExpressionStmt) parsedNode;
		
		assertNotNull( castedNode.getExpression() );
		assertTrue( castedNode.getExpression() instanceof EnclosedExpr );
	}
	
	@Test
	public void testTokenParserFieldAccessExprParent() {
		testTokenParserFieldAccessExpr(mapper_short, t_parser_short);
		testTokenParserFieldAccessExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserFieldAccessExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Expression scope
		// final NodeList<Type> typeArguments
		// final SimpleName name
		Node node =  new FieldAccessExpr(
						new NameExpr( "TestScope" ), 
						new NodeList<Type>(), 
						new SimpleName( "TestFieldAccessExpr"));
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof FieldAccessExpr);
		
		FieldAccessExpr castedNode = (FieldAccessExpr) parsedNode;
		
		assertNotNull( castedNode.getScope() );
		assertTrue( castedNode.getScope() instanceof NameExpr );
		assertNotNull( castedNode.getTypeArguments() );
		assertNotNull( castedNode.getNameAsString() );
	}
	
	@Test
	public void testTokenParserFieldDeclarationParent() {
		testTokenParserFieldDeclaration(mapper_short, t_parser_short);
		testTokenParserFieldDeclaration(mapper_long, t_parser_long);
	}
	
	private void testTokenParserFieldDeclaration(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// NodeList<VariableDeclarator> variables
		Node node =  new FieldDeclaration(
						EnumSet.of(Modifier.PUBLIC), 
						new NodeList<AnnotationExpr>(), 
						new NodeList<VariableDeclarator>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof FieldDeclaration);
		
		FieldDeclaration castedNode = (FieldDeclaration) parsedNode;
		
		assertNotNull( castedNode.getModifiers() );
		assertNotNull( castedNode.getAnnotations() );
		assertNotNull( castedNode.getVariables() );
	}
	

	@Test
	public void testTokenParserForeachStmtParent() {
		testTokenParserForeachStmt(mapper_short, t_parser_short);
		testTokenParserForeachStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserForeachStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final VariableDeclarationExpr variable
		// final Expression iterable
		// final Statement body
		Node node =  new ForeachStmt(
						new VariableDeclarationExpr(), 
						new NameExpr(), 
						new ReturnStmt());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ForeachStmt);
		
		ForeachStmt castedNode = (ForeachStmt) parsedNode;
		
		assertNotNull( castedNode.getVariable() );
		assertNotNull( castedNode.getIterable() );
		assertNotNull( castedNode.getBody() );
	}

	@Test
	public void testTokenParserForStmtParent() {
		testTokenParserForStmt(mapper_short, t_parser_short);
		testTokenParserForStmt(mapper_long, t_parser_long);
	}

	private void testTokenParserForStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final NodeList<Expression> initialization
		// final Expression compare
		// final NodeList<Expression> update
		// final Statement body
		Node node =  new ForStmt(
						new NodeList<>(), 
						new BooleanLiteralExpr(), 
						new NodeList<>(), 
						new ReturnStmt());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes

		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ForStmt);
		
		ForStmt castedNode = (ForStmt) parsedNode;
		
		assertNotNull( castedNode.getInitialization() );
		assertNotNull( castedNode.getCompare() );
		assertNotNull( castedNode.getUpdate() );
		assertNotNull( castedNode.getBody() );
	}

	@Test
	public void testTokenParserIfStmtParent() {
		testTokenParserIfStmt(mapper_short, t_parser_short);
		testTokenParserIfStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserIfStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Expression condition
		// final Statement thenStmt
		// final Statement elseStmt
		Node node =  new IfStmt(
						new BooleanLiteralExpr(),
						new ReturnStmt(), 
						new ReturnStmt());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof IfStmt);
		
		IfStmt castedNode = (IfStmt) parsedNode;
		
		assertNotNull( castedNode.getCondition() );
		assertNotNull( castedNode.getThenStmt() );
		assertNotNull( castedNode.getElseStmt() );
	}

	@Test
	public void testTokenParserImportDeclarationParent() {
		testTokenParserImportDeclaration(mapper_short, t_parser_short);
		testTokenParserImportDeclaration(mapper_long, t_parser_long);
	}
	
	private void testTokenParserImportDeclaration(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Name name
		// boolean isStatic
		// boolean isAsterisk
		Node node =  new ImportDeclaration(
						new Name(),
						false, 
						false);
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ImportDeclaration);
		
		ImportDeclaration castedNode = (ImportDeclaration) parsedNode;
		
		assertNotNull( castedNode.getNameAsString() );
		assertFalse( castedNode.isStatic() );
		assertFalse( castedNode.isAsterisk() );
	}

	@Test
	public void testTokenParserInitializerDeclarationParent() {
		testTokenParserInitializerDeclaration(mapper_short, t_parser_short);
		testTokenParserInitializerDeclaration(mapper_long, t_parser_long);
	}
	
	private void testTokenParserInitializerDeclaration(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// boolean isStatic
		// BlockStmt body
		Node node =  new InitializerDeclaration(
						false,
						new BlockStmt() );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof InitializerDeclaration);
		
		InitializerDeclaration castedNode = (InitializerDeclaration) parsedNode;
		
		assertFalse( castedNode.isStatic() );
		assertNotNull( castedNode.getBody() );
	}
	

	@Test
	public void testTokenParserInstanceOfExprParent() {
		testTokenParserInstanceOfExpr(mapper_short, t_parser_short);
		testTokenParserInstanceOfExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserInstanceOfExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Expression expression
		// final ReferenceType<?> type
		Node node =  new InstanceOfExpr(
						new NameExpr(),
						new ClassOrInterfaceType());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof InstanceOfExpr);
		
		InstanceOfExpr castedNode = (InstanceOfExpr) parsedNode;
		
		assertNotNull( castedNode.getExpression() );
		assertNotNull( castedNode.getType() );
	}
	
	@Test
	public void testTokenParserIntegerLiteralExprParent() {
		testTokenParserIntegerLiteralExpr(mapper_short, t_parser_short);
		testTokenParserIntegerLiteralExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserIntegerLiteralExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//final String value
		Node node =  new IntegerLiteralExpr(
						"Test-Value" );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof IntegerLiteralExpr);
		
		IntegerLiteralExpr castedNode = (IntegerLiteralExpr) parsedNode;
		
		assertNotNull( castedNode.getValue() );
		assertTrue( castedNode.getValue().equals( "Test-Value" ) );

	}

	@Test
	public void testTokenParserIntersectionTypeParent() {
		testTokenParserIntersectionType(mapper_short, t_parser_short);
		testTokenParserIntersectionType(mapper_long, t_parser_long);
	}
	
	private void testTokenParserIntersectionType(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//NodeList<ReferenceType> elements
		Node node =  new IntersectionType(
						new NodeList<>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof IntersectionType);
		
		IntersectionType castedNode = (IntersectionType) parsedNode;
		
		assertNotNull( castedNode.getElements() );
	}

	@Test
	public void testTokenParserLabeledStmtParent() {
		testTokenParserLabeledStmt(mapper_short, t_parser_short);
		testTokenParserLabeledStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserLabeledStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final SimpleName label
		// final Statement statement
		Node node =  new LabeledStmt(
						new SimpleName(), 
						new ReturnStmt());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof LabeledStmt);
		
		LabeledStmt castedNode = (LabeledStmt) parsedNode;
		
		assertNotNull( castedNode.getLabel() );
		assertNotNull( castedNode.getStatement() );
	}

	@Test
	public void testTokenParserLambdaExprParent() {
		testTokenParserLambdaExpr(mapper_short, t_parser_short);
		testTokenParserLambdaExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserLambdaExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// NodeList<Parameter> parameters
		// Statement body
		// boolean isEnclosingParameters
		Node node =  new LambdaExpr(
						new NodeList<>(), 
						new ReturnStmt(),
						false);
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof LambdaExpr);
		
		LambdaExpr castedNode = (LambdaExpr) parsedNode;
		
		assertNotNull( castedNode.getParameters() );
		assertNotNull( castedNode.getBody() );
		assertFalse( castedNode.isEnclosingParameters() );
	}
	
	@Test
	public void testTokenParserLocalClassDeclarationStmtParent() {
		testTokenParserLocalClassDeclarationStmt(mapper_short, t_parser_short);
		testTokenParserLocalClassDeclarationStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserLocalClassDeclarationStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final ClassOrInterfaceDeclaration classDeclaration
		Node node =  new LocalClassDeclarationStmt(
						new ClassOrInterfaceDeclaration());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof LocalClassDeclarationStmt);
		
		LocalClassDeclarationStmt castedNode = (LocalClassDeclarationStmt) parsedNode;
		
		assertNotNull( castedNode.getClassDeclaration() );
	}
	
	@Test
	public void testTokenParserLongLiteralExprParent() {
		testTokenParserLongLiteralExpr(mapper_short, t_parser_short);
		testTokenParserLongLiteralExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserLongLiteralExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//final String value
		Node node =  new LongLiteralExpr(
						"1.0l" );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof LongLiteralExpr);
		
		LongLiteralExpr castedNode = (LongLiteralExpr) parsedNode;
		
		assertNotNull( castedNode.getValue() );
		assertTrue( castedNode.getValue().equals( "1.0l" ) );
	}

	@Test
	public void testTokenParserMarkerAnnotationExprParent() {
		testTokenParserMarkerAnnotationExpr(mapper_short, t_parser_short);
		testTokenParserMarkerAnnotationExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserMarkerAnnotationExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Name name
		Node node =  new MarkerAnnotationExpr(
						parseName( "MarkerAnnotationExpr" ));
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof MarkerAnnotationExpr);
		
		MarkerAnnotationExpr castedNode = (MarkerAnnotationExpr) parsedNode;
		
		assertNotNull( castedNode.getNameAsString() );
		assertTrue( castedNode.getNameAsString().equals( "MarkerAnnotationExpr" ));
	}

	@Test
	public void testTokenParserMemberValuePairParent() {
		testTokenParserMemberValuePair(mapper_short, t_parser_short);
		testTokenParserMemberValuePair(mapper_long, t_parser_long);
	}
	
	private void testTokenParserMemberValuePair(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final SimpleName name
		// final Expression value
		Node node =  new MemberValuePair(
						new SimpleName( "MemberValuePairName"), 
						new StringLiteralExpr( "MemberValuePairValue"));
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof MemberValuePair);
		
		MemberValuePair castedNode = (MemberValuePair) parsedNode;
		
		assertNotNull( castedNode.getNameAsString() );
		assertTrue( castedNode.getNameAsString().equals( "MemberValuePairName") );
		assertNotNull( castedNode.getValue() );
		assertTrue( castedNode.getValue() instanceof StringLiteralExpr );
		assertTrue( ( (StringLiteralExpr) castedNode.getValue()).getValue().equals( parser.getGuesser().getDefaultStringLiteralValue() ) );
	}
	
	@Test
	public void testTokenParserMethodCallExprParent() {
		testTokenParserMethodCallExpr(mapper_short, t_parser_short);
		testTokenParserMethodCallExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserMethodCallExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Expression scope
		// final NodeList<Type> typeArguments
		// final SimpleName name
		// final NodeList<Expression> arguments
		Node node =  new MethodCallExpr(
						new NameExpr( "TestScope" ), 
						new NodeList<>(), 
						new SimpleName( "MethodCallExprName" ), 
						new NodeList<>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof MethodCallExpr);
		
		MethodCallExpr castedNode = (MethodCallExpr) parsedNode;
		
		assertNotNull( castedNode.getScope() );
		assertNotNull( castedNode.getTypeArguments() );
		assertNotNull( castedNode.getName() );
		assertTrue( castedNode.getNameAsString().equals( "MethodCallExprName" ));
		assertNotNull( castedNode.getArguments() );
	}
	
	@Test
	public void testTokenParserMethodDeclarationParent() {
		testTokenParserMethodDeclaration(mapper_short, t_parser_short);
		testTokenParserMethodDeclaration(mapper_long, t_parser_long);
	}
	
	private void testTokenParserMethodDeclaration(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final EnumSet<Modifier> modifiers
		// final NodeList<AnnotationExpr> annotations
		// final NodeList<TypeParameter> typeParameters, 
		// final Type type
		// final SimpleName name
		// final boolean isDefault
		// final NodeList<Parameter> parameters, 
		// final NodeList<ReferenceType> thrownExceptions
		// final BlockStmt body
		Node node =  new MethodDeclaration(
						EnumSet.of(Modifier.PUBLIC, Modifier.DEFAULT),
						new NodeList<AnnotationExpr>(), 
						new NodeList<TypeParameter>(), 
						new ClassOrInterfaceType(), 
						new SimpleName( "MethodDeclarationName" ),
						new NodeList<Parameter>(), 
						new NodeList<>(), 
						new BlockStmt());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof MethodDeclaration);
		
		MethodDeclaration castedNode = (MethodDeclaration) parsedNode;
		
		assertNotNull( castedNode.getModifiers() );
		assertTrue( castedNode.isPublic() );
		assertNotNull( castedNode.getAnnotations() );
		assertNotNull( castedNode.getTypeParameters() );
		assertNotNull( castedNode.getName() );
		assertTrue( castedNode.getNameAsString().equals( "MethodDeclarationName" ));
		assertTrue( castedNode.isDefault() );
		assertNotNull( castedNode.getParameters() );
		assertNotNull( castedNode.getThrownExceptions() );
		assertNotNull( castedNode.getBody() );
	}

	@Test
	public void testTokenParserMethodReferenceExprParent() {
		testTokenParserMethodReferenceExpr(mapper_short, t_parser_short);
		testTokenParserMethodReferenceExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserMethodReferenceExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Expression scope
		// NodeList<Type> typeArguments
		// String identifier
		Node node =  new MethodReferenceExpr(
							new NameExpr( "Method.Ref.Scope.Test" ), 
							new NodeList<>(), 
							"MethodReferenceExprID");
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof MethodReferenceExpr);
		
		MethodReferenceExpr castedNode = (MethodReferenceExpr) parsedNode;
		
		assertNotNull( castedNode.getScope() );
		assertTrue( castedNode.getScope() instanceof NameExpr );
		assertTrue( ((NameExpr) castedNode.getScope()).getNameAsString().equals( "Method.Ref.Scope.Test" ) );
		assertNotNull( castedNode.getTypeArguments() );
		assertNotNull( castedNode.getIdentifier() );
		assertTrue( castedNode.getIdentifier().equals( "MethodReferenceExprID" ) );
	}
	
	@Test
	public void testTokenParserArrayCreationLevelParent() {
		testTokenParserArrayInitializerExpr(mapper_short, t_parser_short);
		testTokenParserArrayInitializerExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserArrayInitializerExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// //NodeList<Expression> values
		Node node = new ArrayInitializerExpr( 
					new NodeList<Expression>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ArrayInitializerExpr);
		
		ArrayInitializerExpr castedNode = (ArrayInitializerExpr) parsedNode;
		
		assertNotNull( castedNode.getValues() );
	}
	
	@Test
	public void testTokenParserArrayInitializerExprParent() {
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
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
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
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
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
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
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
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ModuleOpensStmt);
		
		ModuleOpensStmt castedNode = (ModuleOpensStmt) parsedNode;
		
		assertNotNull( castedNode.getModuleNames() );
		assertNotNull( castedNode.getNameAsString().equals( "TestModuleOpensStmt" ) );
	}
	
	@Test
	public void testTokenParserModuleProvidesStmtParent() {
		testTokenParserModuleProvidesStmt(mapper_short, t_parser_short);
		testTokenParserModuleProvidesStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserModuleProvidesStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Type type
		// NodeList<Type> withTypes
		Node node = new ModuleProvidesStmt( 
				new VoidType(), // who knows if it makes any sense to provide a void type
				new NodeList<Type>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ModuleProvidesStmt);
		
		ModuleProvidesStmt castedNode = (ModuleProvidesStmt) parsedNode;
		
		assertNotNull( castedNode.getType() );
		assertNotNull( castedNode.getWithTypes() );
	}
	
	@Test
	public void testTokenParserModuleRequiresStmtParent() {
		testTokenParserModuleRequiresStmt(mapper_short, t_parser_short);
		testTokenParserModuleRequiresStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserModuleRequiresStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// EnumSet<Modifier> modifiers
		// Name name
		Node node = new ModuleRequiresStmt( 
				EnumSet.of(Modifier.PUBLIC, Modifier.VOLATILE), // who knows if it makes any sense to provide a void type
				new Name( "TestModuleRequiresStmt") );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ModuleRequiresStmt);
		
		ModuleRequiresStmt castedNode = (ModuleRequiresStmt) parsedNode;
		
		assertNotNull( castedNode.getModifiers() );
		assertNotNull( castedNode.getName() );
		assertTrue( castedNode.getNameAsString().equals( "TestModuleRequiresStmt" ));
	}
	
	@Test
	public void testTokenParserModuleUsesStmtParent() {
		testTokenParserModuleUsesStmt(mapper_short, t_parser_short);
		testTokenParserModuleUsesStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserModuleUsesStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// EnumSet<Modifier> modifiers
		// Name name
		Node node = new ModuleRequiresStmt( 
				EnumSet.of(Modifier.PUBLIC, Modifier.VOLATILE), // who knows if it makes any sense to provide a void type
				new Name( "TestModuleRequiresStmt") );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ModuleRequiresStmt);
		
		ModuleRequiresStmt castedNode = (ModuleRequiresStmt) parsedNode;
		
		assertNotNull( castedNode.getModifiers() );
		assertNotNull( castedNode.getName() );
		assertTrue( castedNode.getNameAsString().equals( "TestModuleRequiresStmt" ));
	}
	
	@Test
	public void testTokenParserNameParent() {
		testTokenParserName(mapper_short, t_parser_short);
		testTokenParserName(mapper_long, t_parser_long);
	}
	
	private void testTokenParserName(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Name qualifier
		// final String identifier
		// NodeList<AnnotationExpr> annotations
		Node node =  new Name(
						new Name( "SomeName" ), 
						"NameID" , 
						new NodeList<AnnotationExpr>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof Name);
		
		Name castedNode = (Name) parsedNode;
		
		assertNotNull( castedNode.getQualifier() );
		assertTrue( castedNode.getQualifier().get().asString().equals( "SomeName" ) );
		assertNotNull( castedNode.getIdentifier() );
		assertTrue( castedNode.getIdentifier().equals( "NameID" ) );
		assertNotNull( castedNode.getAnnotations() );
	}
	
	@Test
	public void testTokenParserNameExprParent() {
		testTokenParserNameExpr(mapper_short, t_parser_short);
		testTokenParserNameExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserNameExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//final SimpleName name
		Node node =  new NameExpr(
						new SimpleName( "NameExprSN") );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof NameExpr);
		
		NameExpr castedNode = (NameExpr) parsedNode;
		
		assertNotNull( castedNode.getName() );
		assertTrue( castedNode.getNameAsString().equals( "NameExprSN" ));
	}
	
	@Test
	public void testTokenParserNormalAnnotationExprParent() {
		testTokenParserNormalAnnotationExpr(mapper_short, t_parser_short);
		testTokenParserNormalAnnotationExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserNormalAnnotationExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Name name
		// final NodeList<MemberValuePair> pairs
		Node node =  new NormalAnnotationExpr(
						new Name( "NormalAnnotationExprName" ),
						new NodeList<MemberValuePair>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof NormalAnnotationExpr);
		
		NormalAnnotationExpr castedNode = (NormalAnnotationExpr) parsedNode;
		
		assertNotNull( castedNode.getName() );
		assertTrue( castedNode.getNameAsString().equals( "NormalAnnotationExprName"));
		assertNotNull( castedNode.getPairs());
	}
	
	@Test
	public void testTokenParserNullLiteralExprParent() {
		testTokenParserNullLiteralExpr(mapper_short, t_parser_short);
		testTokenParserNullLiteralExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserNullLiteralExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// :)
		Node node =  new NullLiteralExpr();
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof NullLiteralExpr);
		
		// assertNull :D
	}
	
	@Test
	public void testTokenParserObjectCreationExprParent() {
		testTokenParserObjectCreationExpr(mapper_short, t_parser_short);
		testTokenParserObjectCreationExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserObjectCreationExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Expression scope
		// final ClassOrInterfaceType type
		// final NodeList<Type> typeArguments, 
		// final NodeList<Expression> arguments
		// final NodeList<BodyDeclaration<?>> anonymousClassBody
		Node node =  new ObjectCreationExpr(
						new NameExpr( "Obj.Creation.Expr.Scope.Test" ),
						new ClassOrInterfaceType(),
						new NodeList<Type>(),
						new NodeList<Expression>(), 
						new NodeList<>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ObjectCreationExpr);
		
		ObjectCreationExpr castedNode = (ObjectCreationExpr) parsedNode;
		
		assertNotNull( castedNode.getScope() );
		assertNotNull( castedNode.getScope().get() );
		assertTrue( castedNode.getScope().get() instanceof NameExpr);
		assertEquals("Obj.Creation.Expr.Scope.Test", ((NameExpr) castedNode.getScope().get()).getNameAsString() );
		assertNotNull( castedNode.getType() );
		assertNotNull( castedNode.getTypeArguments() );
		assertNotNull( castedNode.getArguments() );
		assertNotNull( castedNode.getAnonymousClassBody() );
	}
	
	@Test
	public void testTokenParserPackageDeclarationParent() {
		testTokenParserPackageDeclaration(mapper_short, t_parser_short);
		testTokenParserPackageDeclaration(mapper_long, t_parser_long);
	}
	
	private void testTokenParserPackageDeclaration(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// NodeList<AnnotationExpr> annotations
		// Name name
		Node node =  new PackageDeclaration(
						new NodeList<AnnotationExpr>(), 
						new Name( "AnnotationExprName" ));
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof PackageDeclaration);
		
		PackageDeclaration castedNode = (PackageDeclaration) parsedNode;
		
		assertNotNull( castedNode.getAnnotations() );
		assertNotNull( castedNode.getName() );
		assertTrue( castedNode.getNameAsString().equals( "AnnotationExprName" ) );
	}

	@Test
	public void testTokenParserParameterParent() {
		testTokenParserParameter(mapper_short, t_parser_short);
		testTokenParserParameter(mapper_long, t_parser_long);
	}
	
	private void testTokenParserParameter(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// Type type, 
		// boolean isVarArgs
		// NodeList<AnnotationExpr> varArgsAnnotations
		// SimpleName name
		Node node =  new Parameter(
						EnumSet.of(Modifier.PUBLIC), 
						new NodeList<AnnotationExpr>(), 
						new PrimitiveType( Primitive.CHAR ),
						true,
						new NodeList<AnnotationExpr>(),
						new SimpleName( "ParameterSN"));
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof Parameter);
		
		Parameter castedNode = (Parameter) parsedNode;
		
		assertNotNull( castedNode.getModifiers() );
		assertTrue( castedNode.getModifiers().contains(Modifier.PUBLIC) );
		assertNotNull( castedNode.getAnnotations() );
		assertNotNull( castedNode.getType() );
		assertTrue( castedNode.isVarArgs() );
		assertNotNull( castedNode.getVarArgsAnnotations() );
		assertNotNull( castedNode.getName() );
		assertTrue( castedNode.getNameAsString().equals( "ParameterSN" ) );
	}
	
	@Test
	public void testTokenParserPrimitiveTypeParent() {
		testTokenParserPrimitiveType(mapper_short, t_parser_short);
		testTokenParserPrimitiveType(mapper_long, t_parser_long);
	}
	
	private void testTokenParserPrimitiveType(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//final Primitive type
		Node node =  new PrimitiveType( Primitive.INT );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof PrimitiveType);
		
		PrimitiveType castedNode = (PrimitiveType) parsedNode;
		
		assertNotNull( castedNode.getType() );
		assertTrue( castedNode.getType() == Primitive.INT );
	}
	
	@Test
	public void testTokenParserReturnStmtParent() {
		testTokenParserReturnStmt(mapper_short, t_parser_short);
		testTokenParserReturnStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserReturnStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//final Expression expression
		Node node =  new ReturnStmt(
						new NameExpr( "ReturnMe"));
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ReturnStmt);
		
		ReturnStmt castedNode = (ReturnStmt) parsedNode;
		
		assertNotNull( castedNode.getExpression() );
		assertTrue( castedNode.getExpression().get() instanceof NameExpr );
		assertTrue( ((NameExpr) castedNode.getExpression().get()).getNameAsString().equals( "ReturnMe" ));
	}
	
	@Test
	public void testTokenParserSimpleNameParent() {
		testTokenParserSimpleName(mapper_short, t_parser_short);
		testTokenParserSimpleName(mapper_long, t_parser_long);
	}
	
	private void testTokenParserSimpleName(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//final String identifier
		Node node =  new SimpleName( "SimpleNameName" );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof SimpleName);
		
		SimpleName castedNode = (SimpleName) parsedNode;
		
		assertNotNull( castedNode.getIdentifier() );
		assertTrue( castedNode.getIdentifier().equals( "SimpleNameName" ) );
	}

	@Test
	public void testTokenParserSingleMemberAnnotationExprParent() {
		testTokenParserSingleMemberAnnotationExpr(mapper_short, t_parser_short);
		testTokenParserSingleMemberAnnotationExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserSingleMemberAnnotationExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Name name
		// final Expression memberValue
		Node node =  new SingleMemberAnnotationExpr(
							new Name( "SingleMemberAnnotationExprName"), 
							new StringLiteralExpr( "memberValue" ) );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof SingleMemberAnnotationExpr);
		
		SingleMemberAnnotationExpr castedNode = (SingleMemberAnnotationExpr) parsedNode;
		
		assertNotNull( castedNode.getName() );
		assertTrue( castedNode.getNameAsString().equals( "SingleMemberAnnotationExprName" ) );
		assertNotNull( castedNode.getMemberValue() );
		assertTrue( castedNode.getMemberValue() instanceof StringLiteralExpr );
		assertTrue( ((StringLiteralExpr) castedNode.getMemberValue()).getValue().equals( parser.getGuesser().getDefaultStringLiteralValue() ) );
	}

	@Test
	public void testTokenParserStringLiteralExprParent() {
		testTokenParserStringLiteralExpr(mapper_short, t_parser_short);
		testTokenParserStringLiteralExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserStringLiteralExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//final String value
		Node node =  new StringLiteralExpr( "StringLiteralExprValue" );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof StringLiteralExpr);
		
		StringLiteralExpr castedNode = (StringLiteralExpr) parsedNode;
		
		assertNotNull( castedNode.getValue() );
		assertTrue( castedNode.getValue().equals( parser.getGuesser().getDefaultStringLiteralValue() ) );
	}
	
	@Test
	public void testTokenParserSuperExprParent() {
		testTokenParserSuperExpr(mapper_short, t_parser_short);
		testTokenParserSuperExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserSuperExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//final Expression classExpr
		Node node =  new SuperExpr(
						new ClassExpr());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof SuperExpr);
		
		SuperExpr castedNode = (SuperExpr) parsedNode;
		
		assertNotNull( castedNode.getClassExpr() );
		assertTrue( castedNode.getClassExpr().get() instanceof ClassExpr );
	}

	@Test
	public void testTokenParserSwitchEntryStmtParent() {
		testTokenParserSwitchEntryStmt(mapper_short, t_parser_short);
		testTokenParserSwitchEntryStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserSwitchEntryStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Expression label
		// final NodeList<Statement> statements
		Node node =  new SwitchEntryStmt(
						new NormalAnnotationExpr(), 
						new NodeList<>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof SwitchEntryStmt);
		
		SwitchEntryStmt castedNode = (SwitchEntryStmt) parsedNode;
		
		assertNotNull( castedNode.getLabel() );
		assertTrue( castedNode.getLabel().get() instanceof NormalAnnotationExpr );
		assertNotNull( castedNode.getStatements() );
	}
	
	@Test
	public void testTokenParserSwitchStmtParent() {
		testTokenParserSwitchStmt(mapper_short, t_parser_short);
		testTokenParserSwitchStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserSwitchStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Expression selector
		// final NodeList<SwitchEntryStmt> entries
		Node node =  new SwitchStmt(
						new NameExpr( "SelectorExpression"), 
						new NodeList<>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof SwitchStmt);
		
		SwitchStmt castedNode = (SwitchStmt) parsedNode;
		
		assertNotNull( castedNode.getSelector() );
		assertNotNull( castedNode.getEntries() );
	}
	
	@Test
	public void testTokenParserSynchronizedStmtParent() {
		testTokenParserSynchronizedStmt(mapper_short, t_parser_short);
		testTokenParserSynchronizedStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserSynchronizedStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Expression expression
		// final BlockStmt body
		Node node =  new SynchronizedStmt(
						new NameExpr( "SynchExprName"), 
						new BlockStmt());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof SynchronizedStmt);
		
		SynchronizedStmt castedNode = (SynchronizedStmt) parsedNode;
		
		assertNotNull( castedNode.getExpression() );
		assertTrue( castedNode.getExpression() instanceof NameExpr );
		assertNotNull( castedNode.getBody() );
	}
	
	@Test
	public void testTokenParserThisExprParent() {
		testTokenParserThisExpr(mapper_short, t_parser_short);
		testTokenParserThisExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserThisExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//final Expression classExpr
		Node node =  new ThisExpr( new ClassExpr());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ThisExpr);
		
		ThisExpr castedNode = (ThisExpr) parsedNode;
		
		assertNotNull( castedNode.getClassExpr() );
		assertNotNull( castedNode.getClassExpr().get() );
		assertTrue( castedNode.getClassExpr().get() instanceof ClassExpr );
	}
	
	@Test
	public void testTokenParserThrowStmtParent() {
		testTokenParserThrowStmt(mapper_short, t_parser_short);
		testTokenParserThrowStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserThrowStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//final Expression expression
		Node node =  new ThrowStmt( new NameExpr( "ThrowStmtExpr") );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ThrowStmt);
		
		ThrowStmt castedNode = (ThrowStmt) parsedNode;
		
		assertNotNull( castedNode.getExpression() );
		assertTrue( castedNode.getExpression() instanceof NameExpr );
	}
	
	@Test
	public void testTokenParserTryStmtParent() {
		testTokenParserTryStmt(mapper_short, t_parser_short);
		testTokenParserTryStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserTryStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// NodeList<VariableDeclarationExpr> resources
		// final BlockStmt tryBlock
		// final NodeList<CatchClause> catchClauses
		// final BlockStmt finallyBlock
		Node node =  new TryStmt(
							new NodeList<VariableDeclarationExpr>(), 
							new BlockStmt(), 
							new NodeList<CatchClause>(), 
							new BlockStmt());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof TryStmt);
		
		TryStmt castedNode = (TryStmt) parsedNode;
		
		assertNotNull( castedNode.getResources() );
		assertNotNull( castedNode.getTryBlock() );
		assertNotNull( castedNode.getCatchClauses() );
		assertNotNull( castedNode.getFinallyBlock() );
	}
	
	@Test
	public void testTokenParserTypeExprParent() {
		testTokenParserTypeExpr(mapper_short, t_parser_short);
		testTokenParserTypeExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserTypeExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//Type type
		Node node =  new TypeExpr( new ClassOrInterfaceType() );
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof TypeExpr);
		
		TypeExpr castedNode = (TypeExpr) parsedNode;
		
		assertNotNull( castedNode.getType() );
		assertTrue( castedNode.getType() instanceof ClassOrInterfaceType );
	}
	
	@Test
	public void testTokenParserTypeParameterParent() {
		testTokenParserTypeParameter(mapper_short, t_parser_short);
		testTokenParserTypeParameter(mapper_long, t_parser_long);
	}
	
	private void testTokenParserTypeParameter(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// SimpleName name
		// NodeList<ClassOrInterfaceType> typeBound
		// NodeList<AnnotationExpr> annotations
		Node node =  new TypeParameter(
						new SimpleName( "TypeParameterSN" ),
						new NodeList<ClassOrInterfaceType>(),
						new NodeList<AnnotationExpr>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof TypeParameter);
		
		TypeParameter castedNode = (TypeParameter) parsedNode;
		
		assertNotNull( castedNode.getName() );
		assertTrue( castedNode.getNameAsString().equals( "TypeParameterSN" ));
		assertNotNull( castedNode.getTypeBound() );
		assertNotNull( castedNode.getAnnotations() );
	}

	@Test
	public void testTokenParserUnaryExprParent() {
		testTokenParserUnaryExpr(mapper_short, t_parser_short);
		testTokenParserUnaryExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserUnaryExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Expression expression
		// final Operator operator
		Node node =  new UnaryExpr(
						new IntegerLiteralExpr( "4711" ), 
						UnaryExpr.Operator.POSTFIX_INCREMENT);
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof UnaryExpr);
		
		UnaryExpr castedNode = (UnaryExpr) parsedNode;
		
		assertNotNull( castedNode.getExpression() );
		assertNotNull( castedNode.getOperator() );
		assertTrue( castedNode.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT );
	}
	
	@Test
	public void testTokenParserUnionTypeParent() {
		testTokenParserUnionType(mapper_short, t_parser_short);
		testTokenParserUnionType(mapper_long, t_parser_long);
	}
	
	private void testTokenParserUnionType(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		//NodeList<ReferenceType> elements
		Node node =  new UnionType(
						new NodeList<>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof UnionType);
		
		UnionType castedNode = (UnionType) parsedNode;
		
		assertNotNull( castedNode.getElements() );
	}
	
	@Test
	public void testTokenParserUnknownTypeParent() {
		testTokenParserUnknownType(mapper_short, t_parser_short);
		testTokenParserUnknownType(mapper_long, t_parser_long);
	}
	
	private void testTokenParserUnknownType(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Type elementType
		// NodeList<ArrayCreationLevel> levels
		// ArrayInitializerExpr initializer
		Node node =  new UnknownType();
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof UnknownType);
	}
	
	@Test
	public void testTokenParserVariableDeclarationExprParent() {
		testTokenParserVariableDeclarationExpr(mapper_short, t_parser_short);
		testTokenParserVariableDeclarationExpr(mapper_long, t_parser_long);
	}
	
	private void testTokenParserVariableDeclarationExpr(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final EnumSet<Modifier> modifiers
		// final NodeList<AnnotationExpr> annotations
		// final NodeList<VariableDeclarator> variables
		Node node =  new VariableDeclarationExpr(
						EnumSet.of(Modifier.PUBLIC ), 
						new NodeList<AnnotationExpr>(), 
						new NodeList<VariableDeclarator>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof VariableDeclarationExpr);
		
		VariableDeclarationExpr castedNode = (VariableDeclarationExpr) parsedNode;
		
		assertNotNull( castedNode.getModifiers() );
		assertTrue( castedNode.getModifiers().contains(Modifier.PUBLIC) );
		assertNotNull( castedNode.getAnnotations() );
		assertNotNull( castedNode.getVariables() );
	}
	
	@Test
	public void testTokenParserVariableDeclaratorParent() {
		testTokenParserVariableDeclarator(mapper_short, t_parser_short);
		testTokenParserVariableDeclarator(mapper_long, t_parser_long);
	}
	
	private void testTokenParserVariableDeclarator(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// Type type
		// SimpleName name
		// Expression initializer
		Node node =  new VariableDeclarator(
						new PrimitiveType( Primitive.INT ), 
						new SimpleName( "VariableDeclaratorSN" ), 
						new VariableDeclarationExpr()); // this is actually not a good example but there is no init expr
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof VariableDeclarator);
		
		VariableDeclarator castedNode = (VariableDeclarator) parsedNode;
		
		assertNotNull( castedNode.getType() );
		assertTrue( castedNode.getType() instanceof PrimitiveType );
		assertTrue( ((PrimitiveType) castedNode.getType()).getType() == Primitive.INT );
		assertNotNull( castedNode.getName() );
		assertNotNull( castedNode.getInitializer() );
		assertNotNull( castedNode.getInitializer().get() );
	}
	
	@Test
	public void testTokenParserVoidTypeParent() {
		testTokenParserVoidType(mapper_short, t_parser_short);
		testTokenParserVoidType(mapper_long, t_parser_long);
	}
	
	private void testTokenParserVoidType(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		Node node =  new VoidType();
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof VoidType);
	}
	
	@Test
	public void testTokenParserWhileStmtParent() {
		testTokenParserWhileStmt(mapper_short, t_parser_short);
		testTokenParserWhileStmt(mapper_long, t_parser_long);
	}
	
	private void testTokenParserWhileStmt(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final Expression condition
		// final Statement body
		Node node =  new WhileStmt(
						new BooleanLiteralExpr(), 
						new ReturnStmt());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof WhileStmt);
		
		WhileStmt castedNode = (WhileStmt) parsedNode;
		
		assertNotNull( castedNode.getCondition() );
		assertNotNull( castedNode.getBody() );
	}
	
	@Test
	public void testTokenParserWildcardTypeParent() {
		testTokenParserWildcardType(mapper_short, t_parser_short);
		testTokenParserWildcardType(mapper_long, t_parser_long);
	}
	
	private void testTokenParserWildcardType(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		
		// final ReferenceType extendedType
		// final ReferenceType superType
		// final NodeList<AnnotationExpr> annotations
		Node node =  new WildcardType(
						new TypeParameter(),
						new TypeParameter(), 
						new NodeList<>());
		
		//using the mapper here instead of fixed tokens spares us from fixing the tests when we change the mapping or the keywords around
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
	
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof WildcardType);
		
		WildcardType castedNode = (WildcardType) parsedNode;
		
		assertNotNull( castedNode.getExtendedType() );
		assertNotNull( castedNode.getSuperType() );
	}
	
	
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
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
		
		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
		Node parsedNode = parser.parseNodeFromToken(token, info);
		
		assertTrue(parsedNode instanceof ConstructorDeclaration);
		
		ConstructorDeclaration castedNode = (ConstructorDeclaration) parsedNode;
		
		assertTrue(castedNode.getNameAsString().equals("TestName"));
		assertTrue(castedNode.getModifiers().contains(Modifier.PUBLIC));
		assertTrue(castedNode.getModifiers().contains(Modifier.FINAL));
		assertTrue(castedNode.getModifiers().size() == 2);
	}
	
	// This fails currently because the class body is not mapped how we anticipate it
	// but we usually use the method declarations as entry point and never create enum decs anyway
	// maybe its because we decrease the abstraction by 2 each time? TODO check this
	@Test
	public void testTokenParserEnumConstantDeclarationParent() {
		testTokenParserEnumConstantDeclaration(mapper_short, t_parser_short);
		testTokenParserEnumConstantDeclaration(mapper_long, t_parser_long);
	}
	
	private void testTokenParserEnumConstantDeclaration(IBasicNodeMapper<String> mapper, ITokenParser parser) {
		NodeList<BodyDeclaration<?>> classBody = new NodeList<>();
		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// NodeList<VariableDeclarator> variables
		classBody.add( new FieldDeclaration(
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
		String token = mapper.getMappingForNode(node, null, testDepth, false, null);
		Log.out(this, token); //output the token for debugging purposes
		//in the long keyword version, this produces:
		//(ENUM_CONSTANT_DECLARATION,[(#0,[])],[(SIMPLE_NAME,[TestName])],[(#0,[])],[(#1,[(FIELD_DECLARATION,[1],[#0],[#0])])])

		InformationWrapper info = InfoWrapperBuilder.buildInfoWrapperForNode( node ); // This may be filled with data later on
		
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
