package se.de.hu_berlin.informatik.astlmbuilder.refactortests;

import org.junit.Test;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.UnknownType;

import junit.framework.TestCase;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstants;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstantsShort;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider.KeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.Node2AbstractionMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.SimpleMapper;

public class MappingTester extends TestCase {

	@Test
	public void testAbstractionMapping1() {
		IMapper<String> mapper = new Node2AbstractionMapper.Builder(new KeyWordConstants()).build();
		IKeyWordProvider<String> kwc = new KeyWordConstants(); 
		testWithMapperAndProvider(mapper, kwc);
	}
	
	@Test
	public void testAbstractionMapping2() {
		IMapper<String> mapper = new Node2AbstractionMapper.Builder(new KeyWordConstantsShort()).build();
		IKeyWordProvider<String> kwc = new KeyWordConstantsShort(); 
		testWithMapperAndProvider(mapper, kwc);
	}
	
	@Test
	public void testSimpleMapping1() {
		IMapper<String> mapper = new SimpleMapper<>(new KeyWordConstants());
		IKeyWordProvider<String> kwc = new KeyWordConstants(); 
		testWithMapperAndProvider(mapper, kwc);
	}
	
	@Test
	public void testSimpleMapping2() {
		IMapper<String> mapper = new SimpleMapper<>(new KeyWordConstantsShort());
		IKeyWordProvider<String> kwc = new KeyWordConstantsShort(); 
		testWithMapperAndProvider(mapper, kwc);
	}

	public void testWithMapperAndProvider(IMapper<String> mapper, IKeyWordProvider<String> kwc) {
		CompilationUnit node1 = new CompilationUnit();
		assertEquals(mapper.getMappingForNode( node1, 0 ), kwc.getKeyWord(KeyWords.COMPILATION_UNIT));
		
		AnnotationDeclaration node2 = new AnnotationDeclaration();
		assertEquals(mapper.getMappingForNode( node2, 0 ), kwc.getKeyWord(KeyWords.ANNOTATION_DECLARATION));
		
		AnnotationMemberDeclaration node3 = new AnnotationMemberDeclaration();
		assertEquals(mapper.getMappingForNode( node3, 0 ), kwc.getKeyWord(KeyWords.ANNOTATION_MEMBER_DECLARATION));
		
		ClassOrInterfaceDeclaration node4 = new ClassOrInterfaceDeclaration();
		assertEquals(mapper.getMappingForNode( node4, 0 ), kwc.getKeyWord(KeyWords.CLASS_OR_INTERFACE_DECLARATION));
	
		ConstructorDeclaration node5 = new ConstructorDeclaration();
		assertEquals(mapper.getMappingForNode( node5, 0 ), kwc.getKeyWord(KeyWords.CONSTRUCTOR_DECLARATION));
		
		EnumConstantDeclaration node8 = new EnumConstantDeclaration();
		assertEquals(mapper.getMappingForNode( node8, 0 ), kwc.getKeyWord(KeyWords.ENUM_CONSTANT_DECLARATION));
		
		EnumDeclaration node9 = new EnumDeclaration();
		assertEquals(mapper.getMappingForNode( node9, 0 ), kwc.getKeyWord(KeyWords.ENUM_DECLARATION));
		
		FieldDeclaration node10 = new FieldDeclaration();
		assertEquals(mapper.getMappingForNode( node10, 0 ), kwc.getKeyWord(KeyWords.FIELD_DECLARATION));
		
		InitializerDeclaration node11 = new InitializerDeclaration();
		assertEquals(mapper.getMappingForNode( node11, 0 ), kwc.getKeyWord(KeyWords.INITIALIZER_DECLARATION));
		
		MethodDeclaration node12 = new MethodDeclaration();
		assertEquals(mapper.getMappingForNode( node12, 0 ), kwc.getKeyWord(KeyWords.METHOD_DECLARATION));
	
		Parameter node14 = new Parameter();
		assertEquals(mapper.getMappingForNode( node14, 0 ), kwc.getKeyWord(KeyWords.PARAMETER));
		
		VariableDeclarator node15 = new VariableDeclarator(new UnknownType(), "name");
		assertEquals(mapper.getMappingForNode( node15, 0 ), kwc.getKeyWord(KeyWords.VARIABLE_DECLARATOR));
		
		AssignExpr node17 = new AssignExpr();
		assertEquals(mapper.getMappingForNode( node17, 0 ), kwc.getKeyWord(KeyWords.ASSIGN_EXPRESSION));
		
		BinaryExpr node18 = new BinaryExpr();
		assertEquals(mapper.getMappingForNode( node18, 0 ), kwc.getKeyWord(KeyWords.BINARY_EXPRESSION));
		
		BooleanLiteralExpr node19 = new BooleanLiteralExpr();
		assertEquals(mapper.getMappingForNode( node19, 0 ), kwc.getKeyWord(KeyWords.BOOLEAN_LITERAL_EXPRESSION));
		
		CastExpr node20 = new CastExpr();
		assertEquals(mapper.getMappingForNode( node20, 0 ), kwc.getKeyWord(KeyWords.CAST_EXPRESSION));
		
		CharLiteralExpr node21 = new CharLiteralExpr();
		assertEquals(mapper.getMappingForNode( node21, 0 ), kwc.getKeyWord(KeyWords.CHAR_LITERAL_EXPRESSION));
		
		ClassExpr node22 = new ClassExpr();
		assertEquals(mapper.getMappingForNode( node22, 0 ), kwc.getKeyWord(KeyWords.CLASS_EXPRESSION));
		
		ConditionalExpr node23 = new ConditionalExpr();
		assertEquals(mapper.getMappingForNode( node23, 0 ), kwc.getKeyWord(KeyWords.CONDITIONAL_EXPRESSION));
		
		DoubleLiteralExpr node24 = new DoubleLiteralExpr();
		assertEquals(mapper.getMappingForNode( node24, 0 ), kwc.getKeyWord(KeyWords.DOUBLE_LITERAL_EXPRESSION));
		
		EnclosedExpr node25 = new EnclosedExpr();
		assertEquals(mapper.getMappingForNode( node25, 0 ), kwc.getKeyWord(KeyWords.ENCLOSED_EXPRESSION));
		
		FieldAccessExpr node26 = new FieldAccessExpr();
		assertEquals(mapper.getMappingForNode( node26, 0 ), kwc.getKeyWord(KeyWords.FIELD_ACCESS_EXPRESSION));
		
		InstanceOfExpr node27 = new InstanceOfExpr();
		assertEquals(mapper.getMappingForNode( node27, 0 ), kwc.getKeyWord(KeyWords.INSTANCEOF_EXPRESSION));
		
		IntegerLiteralExpr node28 = new IntegerLiteralExpr();
		assertEquals(mapper.getMappingForNode( node28, 0 ), kwc.getKeyWord(KeyWords.INTEGER_LITERAL_EXPRESSION));
		
		LambdaExpr node30 = new LambdaExpr();
		assertEquals(mapper.getMappingForNode( node30, 0 ), kwc.getKeyWord(KeyWords.LAMBDA_EXPRESSION));
		
		LongLiteralExpr node31 = new LongLiteralExpr();
		assertEquals(mapper.getMappingForNode( node31, 0 ), kwc.getKeyWord(KeyWords.LONG_LITERAL_EXPRESSION));
		
		MarkerAnnotationExpr node33 = new MarkerAnnotationExpr();
		assertEquals(mapper.getMappingForNode( node33, 0 ), kwc.getKeyWord(KeyWords.MARKER_ANNOTATION_EXPRESSION));
		
		MemberValuePair node34 = new MemberValuePair();
		assertEquals(mapper.getMappingForNode( node34, 0 ), kwc.getKeyWord(KeyWords.MEMBER_VALUE_PAIR));
		
		MethodCallExpr node35 = new MethodCallExpr();
		assertEquals(mapper.getMappingForNode( node35, 0 ), kwc.getKeyWord(KeyWords.METHOD_CALL_EXPRESSION));
		
		MethodReferenceExpr node36 = new MethodReferenceExpr();
		assertEquals(mapper.getMappingForNode( node36, 0 ), kwc.getKeyWord(KeyWords.METHOD_REFERENCE_EXPRESSION));
		
		NameExpr node37 = new NameExpr();
		assertEquals(mapper.getMappingForNode( node37, 0 ), kwc.getKeyWord(KeyWords.NAME_EXPRESSION));
		
		NormalAnnotationExpr node38= new NormalAnnotationExpr();
		assertEquals(mapper.getMappingForNode( node38, 0 ), kwc.getKeyWord(KeyWords.NORMAL_ANNOTATION_EXPRESSION));
		
		NullLiteralExpr node39= new NullLiteralExpr();
		assertEquals(mapper.getMappingForNode( node39, 0 ), kwc.getKeyWord(KeyWords.NULL_LITERAL_EXPRESSION));
		
		ObjectCreationExpr node40 = new ObjectCreationExpr();
		assertEquals(mapper.getMappingForNode( node40, 0 ), kwc.getKeyWord(KeyWords.OBJ_CREATE_EXPRESSION));
		
		SingleMemberAnnotationExpr node42 = new SingleMemberAnnotationExpr();
		assertEquals(mapper.getMappingForNode( node42, 0 ), kwc.getKeyWord(KeyWords.SINGLE_MEMBER_ANNOTATION_EXPRESSION));
		
		StringLiteralExpr node43 = new StringLiteralExpr();
		assertEquals(mapper.getMappingForNode( node43, 0 ), kwc.getKeyWord(KeyWords.STRING_LITERAL_EXPRESSION));
		
		SuperExpr node44 = new SuperExpr();
		assertEquals(mapper.getMappingForNode( node44, 0 ), kwc.getKeyWord(KeyWords.SUPER_EXPRESSION));
		
		ThisExpr node45 = new ThisExpr();
		assertEquals(mapper.getMappingForNode( node45, 0 ), kwc.getKeyWord(KeyWords.THIS_EXPRESSION));
		
		TypeExpr node46 = new TypeExpr();
		assertEquals(mapper.getMappingForNode( node46, 0 ), kwc.getKeyWord(KeyWords.TYPE_EXPRESSION));
		
		UnaryExpr node47 = new UnaryExpr();
		assertEquals(mapper.getMappingForNode( node47, 0 ), kwc.getKeyWord(KeyWords.UNARY_EXPRESSION));
		
		VariableDeclarationExpr node48 = new VariableDeclarationExpr();
		assertEquals(mapper.getMappingForNode( node48, 0 ), kwc.getKeyWord(KeyWords.VARIABLE_DECLARATION_EXPRESSION));
		
		AssertStmt node49 = new AssertStmt();
		assertEquals(mapper.getMappingForNode( node49, 0 ), kwc.getKeyWord(KeyWords.ASSERT_STMT));
		
		BlockStmt node50 = new BlockStmt();
		assertEquals(mapper.getMappingForNode( node50, 0 ), kwc.getKeyWord(KeyWords.BLOCK_STMT));
		
		BreakStmt node51 = new BreakStmt();
		assertEquals(mapper.getMappingForNode( node51, 0 ), kwc.getKeyWord(KeyWords.BREAK));
		
		CatchClause node52 = new CatchClause();
		assertEquals(mapper.getMappingForNode( node52, 0 ), kwc.getKeyWord(KeyWords.CATCH_CLAUSE_STMT));
		
		ContinueStmt node53 = new ContinueStmt();
		assertEquals(mapper.getMappingForNode( node53, 0 ), kwc.getKeyWord(KeyWords.CONTINUE_STMT));
		
		DoStmt node54 = new DoStmt();
		assertEquals(mapper.getMappingForNode( node54, 0 ), kwc.getKeyWord(KeyWords.DO_STMT));
		
		ExplicitConstructorInvocationStmt node56 = new ExplicitConstructorInvocationStmt();
		assertEquals(mapper.getMappingForNode( node56, 0 ), kwc.getKeyWord(KeyWords.EXPL_CONSTR_INVOC_STMT));
		
		ExpressionStmt node57 = new ExpressionStmt();
		assertEquals(mapper.getMappingForNode( node57, 0 ), kwc.getKeyWord(KeyWords.EXPRESSION_STMT));
		
		ForStmt node58 = new ForStmt();
		assertEquals(mapper.getMappingForNode( node58, 0 ), kwc.getKeyWord(KeyWords.FOR_STMT));
		
		ForeachStmt node59 = new ForeachStmt();
		assertEquals(mapper.getMappingForNode( node59, 0 ), kwc.getKeyWord(KeyWords.FOR_EACH_STMT));
		
		IfStmt node60 = new IfStmt();
		assertEquals(mapper.getMappingForNode( node60, 0 ), kwc.getKeyWord(KeyWords.IF_STMT));
		
		LabeledStmt node61 = new LabeledStmt();
		assertEquals(mapper.getMappingForNode( node61, 0 ), kwc.getKeyWord(KeyWords.LABELED_STMT));
		
		ReturnStmt node62 = new ReturnStmt();
		assertEquals(mapper.getMappingForNode( node62, 0 ), kwc.getKeyWord(KeyWords.RETURN_STMT));
		
		SwitchEntryStmt node63 = new SwitchEntryStmt();
		assertEquals(mapper.getMappingForNode( node63, 0 ), kwc.getKeyWord(KeyWords.SWITCH_ENTRY_STMT));
		
		SwitchStmt node64 = new SwitchStmt();
		assertEquals(mapper.getMappingForNode( node64, 0 ), kwc.getKeyWord(KeyWords.SWITCH_STMT));
		
		SynchronizedStmt node65 = new SynchronizedStmt();
		assertEquals(mapper.getMappingForNode( node65, 0 ), kwc.getKeyWord(KeyWords.SYNCHRONIZED_STMT));
		
		ThrowStmt node66 = new ThrowStmt();
		assertEquals(mapper.getMappingForNode( node66, 0 ), kwc.getKeyWord(KeyWords.THROW_STMT));
		
		TryStmt node67 = new TryStmt();
		assertEquals(mapper.getMappingForNode( node67, 0 ), kwc.getKeyWord(KeyWords.TRY_STMT));
		
		WhileStmt node69 = new WhileStmt();
		assertEquals(mapper.getMappingForNode( node69, 0 ), kwc.getKeyWord(KeyWords.WHILE_STMT));
		
		ClassOrInterfaceType node70 = new ClassOrInterfaceType();
		assertEquals(mapper.getMappingForNode( node70, 0 ), kwc.getKeyWord(KeyWords.CLASS_OR_INTERFACE_TYPE));
		
		// some nodes that were added after the first generation
		BlockComment node71 = new BlockComment();
		assertEquals(mapper.getMappingForNode( node71, 0 ), kwc.getKeyWord(KeyWords.BLOCK_COMMENT));
		
		JavadocComment node72 = new JavadocComment();
		assertEquals(mapper.getMappingForNode( node72, 0 ), kwc.getKeyWord(KeyWords.JAVADOC_COMMENT));
		
		LineComment node73 = new LineComment();
		assertEquals(mapper.getMappingForNode( node73, 0 ), kwc.getKeyWord(KeyWords.LINE_COMMENT));
		
		ArrayAccessExpr node74 = new ArrayAccessExpr();
		assertEquals(mapper.getMappingForNode( node74, 0 ), kwc.getKeyWord(KeyWords.ARRAY_ACCESS_EXPRESSION));
		
		ArrayCreationExpr node75 = new ArrayCreationExpr();
		assertEquals(mapper.getMappingForNode( node75, 0 ), kwc.getKeyWord(KeyWords.ARRAY_CREATE_EXPRESSION));
		
		ArrayInitializerExpr node76 = new ArrayInitializerExpr();
		assertEquals(mapper.getMappingForNode( node76, 0 ), kwc.getKeyWord(KeyWords.ARRAY_INIT_EXPRESSION));
	}
}
