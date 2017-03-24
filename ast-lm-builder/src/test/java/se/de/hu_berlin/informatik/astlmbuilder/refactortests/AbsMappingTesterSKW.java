package se.de.hu_berlin.informatik.astlmbuilder.refactortests;

import org.junit.Test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EmptyMemberDeclaration;
import com.github.javaparser.ast.body.EmptyTypeDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.MultiTypeParameter;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
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
import com.github.javaparser.ast.expr.IntegerLiteralMinValueExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralMinValueExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
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
import com.github.javaparser.ast.stmt.EmptyStmt;
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
import com.github.javaparser.ast.stmt.TypeDeclarationStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import junit.framework.TestCase;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.IAbsTokenMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.shortKW.KeyWordConstantsShort;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.shortKW.Node2AbstractionTokenMapperShort;

public class AbsMappingTesterSKW extends TestCase {

	IAbsTokenMapper mapper = new Node2AbstractionTokenMapperShort();
	KeyWordConstantsShort kwc = new KeyWordConstantsShort(); 

	@Test
	public void testAbstractionMapping1() {
		CompilationUnit node1 = new CompilationUnit();
		assertEquals(mapper.getMappingForNode( node1, 0 ), mapper.combineData2String( kwc.getCompilationUnit() ) );
		
		AnnotationDeclaration node2 = new AnnotationDeclaration();
		assertEquals(mapper.getMappingForNode( node2, 0 ), mapper.combineData2String( kwc.getAnnotationDeclaration() ) );
		
		AnnotationMemberDeclaration node3 = new AnnotationMemberDeclaration();
		assertEquals(mapper.getMappingForNode( node3, 0 ), mapper.combineData2String( kwc.getAnnotationMemberDeclaration() ) );
		
		ClassOrInterfaceDeclaration node4 = new ClassOrInterfaceDeclaration();
		node4.setInterface( true );
		assertEquals(mapper.getMappingForNode( node4, 0 ), mapper.combineData2String( kwc.getInterfaceDeclaration() ) );
		
		ClassOrInterfaceDeclaration node4_2 = new ClassOrInterfaceDeclaration();
		node4_2.setInterface( false );
		assertEquals(mapper.getMappingForNode( node4_2, 0 ), mapper.combineData2String( kwc.getClassDeclaration() ) );
	
		ConstructorDeclaration node5 = new ConstructorDeclaration();
		assertEquals(mapper.getMappingForNode( node5, 0 ), mapper.combineData2String( kwc.getConstructorDeclaration() ) );
		
		EmptyMemberDeclaration node6 = new EmptyMemberDeclaration();
		assertEquals(mapper.getMappingForNode( node6, 0 ), mapper.combineData2String( kwc.getEmptyMemberDeclaration() ) );
		
		EmptyTypeDeclaration node7 = new EmptyTypeDeclaration();
		assertEquals(mapper.getMappingForNode( node7, 0 ), mapper.combineData2String( kwc.getEmptyTypeDeclaration() ) );
		
		EnumConstantDeclaration node8 = new EnumConstantDeclaration();
		assertEquals(mapper.getMappingForNode( node8, 0 ), mapper.combineData2String( kwc.getEnumConstantDeclaration() ) );
		
		EnumDeclaration node9 = new EnumDeclaration();
		assertEquals(mapper.getMappingForNode( node9, 0 ), mapper.combineData2String( kwc.getEnumDeclaration() ) );
		
		FieldDeclaration node10 = new FieldDeclaration();
		assertEquals(mapper.getMappingForNode( node10, 0 ), mapper.combineData2String( kwc.getFieldDeclaration() ) );
		
		InitializerDeclaration node11 = new InitializerDeclaration();
		assertEquals(mapper.getMappingForNode( node11, 0 ), mapper.combineData2String( kwc.getInitializerDeclaration() ) );
		
		MethodDeclaration node12 = new MethodDeclaration();
		assertEquals(mapper.getMappingForNode( node12, 0 ), mapper.combineData2String( kwc.getMethodDeclaration() ) );
	
		MultiTypeParameter node13 = new MultiTypeParameter();
		assertEquals(mapper.getMappingForNode( node13, 0 ), mapper.combineData2String( kwc.getMultiTypeParameter() ) );
		
		Parameter node14 = new Parameter();
		assertEquals(mapper.getMappingForNode( node14, 0 ), mapper.combineData2String( kwc.getParameter() ) );
		
		VariableDeclarator node15 = new VariableDeclarator();
		assertEquals(mapper.getMappingForNode( node15, 0 ), mapper.combineData2String( kwc.getVariableDeclaration() ) );
		
		VariableDeclaratorId node16 = new VariableDeclaratorId();
		assertEquals(mapper.getMappingForNode( node16, 0 ), mapper.combineData2String( kwc.getVariableDeclarationId() ) );
		
		AssignExpr node17 = new AssignExpr();
		assertEquals(mapper.getMappingForNode( node17, 0 ), mapper.combineData2String( kwc.getAssignExpression() ) );
		
		BinaryExpr node18 = new BinaryExpr();
		assertEquals(mapper.getMappingForNode( node18, 0 ), mapper.combineData2String( kwc.getBinaryExpression() ) );
		
		BooleanLiteralExpr node19 = new BooleanLiteralExpr();
		assertEquals(mapper.getMappingForNode( node19, 0 ), mapper.combineData2String( kwc.getBooleanLiteralExpression() ) );
		
		CastExpr node20 = new CastExpr();
		assertEquals(mapper.getMappingForNode( node20, 0 ), mapper.combineData2String( kwc.getCastExpression() ) );
		
		CharLiteralExpr node21 = new CharLiteralExpr();
		assertEquals(mapper.getMappingForNode( node21, 0 ), mapper.combineData2String( kwc.getCharLiteralExpression() ) );
		
		ClassExpr node22 = new ClassExpr();
		assertEquals(mapper.getMappingForNode( node22, 0 ), mapper.combineData2String( kwc.getClassExpression() ) );
		
		ConditionalExpr node23 = new ConditionalExpr();
		assertEquals(mapper.getMappingForNode( node23, 0 ), mapper.combineData2String( kwc.getConditionalExpression() ) );
		
		DoubleLiteralExpr node24 = new DoubleLiteralExpr();
		assertEquals(mapper.getMappingForNode( node24, 0 ), mapper.combineData2String( kwc.getDoubleLiteralExpression() ) );
		
		EnclosedExpr node25 = new EnclosedExpr();
		assertEquals(mapper.getMappingForNode( node25, 0 ), mapper.combineData2String( kwc.getEnclosedExpression() ) );
		
		FieldAccessExpr node26 = new FieldAccessExpr();
		assertEquals(mapper.getMappingForNode( node26, 0 ), mapper.combineData2String( kwc.getFieldAccessExpression() ) );
		
		InstanceOfExpr node27 = new InstanceOfExpr();
		assertEquals(mapper.getMappingForNode( node27, 0 ), mapper.combineData2String( kwc.getInstanceofExpression() ) );
		
		IntegerLiteralExpr node28 = new IntegerLiteralExpr();
		assertEquals(mapper.getMappingForNode( node28, 0 ), mapper.combineData2String( kwc.getIntegerLiteralExpression() ) );
		
		IntegerLiteralMinValueExpr node29 = new IntegerLiteralMinValueExpr();
		assertEquals(mapper.getMappingForNode( node29, 0 ), mapper.combineData2String( kwc.getIntegerLiteralMinValueExpression() ) );
		
		LambdaExpr node30 = new LambdaExpr();
		assertEquals(mapper.getMappingForNode( node30, 0 ), mapper.combineData2String( kwc.getLambdaExpression() ) );
		
		LongLiteralExpr node31 = new LongLiteralExpr();
		assertEquals(mapper.getMappingForNode( node31, 0 ), mapper.combineData2String( kwc.getLongLiteralExpression() ) );
		
		LongLiteralMinValueExpr node32 = new LongLiteralMinValueExpr();
		assertEquals(mapper.getMappingForNode( node32, 0 ), mapper.combineData2String( kwc.getLongLiteralMinValueExpression() ) );
		
		MarkerAnnotationExpr node33 = new MarkerAnnotationExpr();
		assertEquals(mapper.getMappingForNode( node33, 0 ), mapper.combineData2String( kwc.getMarkerAnnotationExpression() ) );
		
		MemberValuePair node34 = new MemberValuePair();
		assertEquals(mapper.getMappingForNode( node34, 0 ), mapper.combineData2String( kwc.getMemberValuePair() ) );
		
		MethodCallExpr node35 = new MethodCallExpr();
		assertEquals(mapper.getMappingForNode( node35, 0 ), mapper.combineData2String( kwc.getMethodCallExpression() ) );
		
		MethodReferenceExpr node36 = new MethodReferenceExpr();
		assertEquals(mapper.getMappingForNode( node36, 0 ), mapper.combineData2String( kwc.getMethodReferenceExpression() ) );
		
		NameExpr node37 = new NameExpr();
		assertEquals(mapper.getMappingForNode( node37, 0 ), mapper.combineData2String( kwc.getNameExpression() ) );
		
		NormalAnnotationExpr node38= new NormalAnnotationExpr();
		assertEquals(mapper.getMappingForNode( node38, 0 ), mapper.combineData2String( kwc.getNormalAnnotationExpression() ) );
		
		NullLiteralExpr node39= new NullLiteralExpr();
		assertEquals(mapper.getMappingForNode( node39, 0 ), mapper.combineData2String( kwc.getNullLiteralExpression() ) );
		
		ObjectCreationExpr node40 = new ObjectCreationExpr();
		assertEquals(mapper.getMappingForNode( node40, 0 ), mapper.combineData2String( kwc.getObjCreateExpression() ) );
		
		QualifiedNameExpr node41 = new QualifiedNameExpr();
		assertEquals(mapper.getMappingForNode( node41, 0 ), mapper.combineData2String( kwc.getQualifiedNameExpression() ) );
		
		SingleMemberAnnotationExpr node42 = new SingleMemberAnnotationExpr();
		assertEquals(mapper.getMappingForNode( node42, 0 ), mapper.combineData2String( kwc.getSingleMemberAnnotationExpression() ) );
		
		StringLiteralExpr node43 = new StringLiteralExpr();
		assertEquals(mapper.getMappingForNode( node43, 0 ), mapper.combineData2String( kwc.getStringLiteralExpression() ) );
		
		SuperExpr node44 = new SuperExpr();
		assertEquals(mapper.getMappingForNode( node44, 0 ), mapper.combineData2String( kwc.getSuperExpression() ) );
		
		ThisExpr node45 = new ThisExpr();
		assertEquals(mapper.getMappingForNode( node45, 0 ), mapper.combineData2String( kwc.getThisExpression() ) );
		
		TypeExpr node46 = new TypeExpr();
		assertEquals(mapper.getMappingForNode( node46, 0 ), mapper.combineData2String( kwc.getTypeExpression() ) );
		
		UnaryExpr node47 = new UnaryExpr();
		assertEquals(mapper.getMappingForNode( node47, 0 ), mapper.combineData2String( kwc.getUnaryExpression() ) );
		
		VariableDeclarationExpr node48 = new VariableDeclarationExpr();
		assertEquals(mapper.getMappingForNode( node48, 0 ), mapper.combineData2String( kwc.getVariableDeclarationExpression() ) );
		
		AssertStmt node49 = new AssertStmt();
		assertEquals(mapper.getMappingForNode( node49, 0 ), mapper.combineData2String( kwc.getAssertStmt() ) );
		
		BlockStmt node50 = new BlockStmt();
		assertEquals(mapper.getMappingForNode( node50, 0 ), mapper.combineData2String( kwc.getBlockStatement() ) );
		
		BreakStmt node51 = new BreakStmt();
		assertEquals(mapper.getMappingForNode( node51, 0 ), mapper.combineData2String( kwc.getBreak() ) );
		
		CatchClause node52 = new CatchClause();
		assertEquals(mapper.getMappingForNode( node52, 0 ), mapper.combineData2String( kwc.getCatchClauseStatement() ) );
		
		ContinueStmt node53 = new ContinueStmt();
		assertEquals(mapper.getMappingForNode( node53, 0 ), mapper.combineData2String( kwc.getContinueStatement() ) );
		
		DoStmt node54 = new DoStmt();
		assertEquals(mapper.getMappingForNode( node54, 0 ), mapper.combineData2String( kwc.getDoStatement() ) );
		
		EmptyStmt node55 = new EmptyStmt();
		assertEquals(mapper.getMappingForNode( node55, 0 ), mapper.combineData2String( kwc.getEmptyStatement() ) );
		
		ExplicitConstructorInvocationStmt node56 = new ExplicitConstructorInvocationStmt();
		assertEquals(mapper.getMappingForNode( node56, 0 ), mapper.combineData2String( kwc.getExplicitConstructorStatement() ) );
		
		ExpressionStmt node57 = new ExpressionStmt();
		assertEquals(mapper.getMappingForNode( node57, 0 ), mapper.combineData2String( kwc.getExpressionStatement() ) );
		
		ForStmt node58 = new ForStmt();
		assertEquals(mapper.getMappingForNode( node58, 0 ), mapper.combineData2String( kwc.getForStatement() ) );
		
		ForeachStmt node59 = new ForeachStmt();
		assertEquals(mapper.getMappingForNode( node59, 0 ), mapper.combineData2String( kwc.getForEachStatement() ) );
		
		IfStmt node60 = new IfStmt();
		assertEquals(mapper.getMappingForNode( node60, 0 ), mapper.combineData2String( kwc.getIfStatement() ) );
		
		LabeledStmt node61 = new LabeledStmt();
		assertEquals(mapper.getMappingForNode( node61, 0 ), mapper.combineData2String( kwc.getLabeledStatement() ) );
		
		ReturnStmt node62 = new ReturnStmt();
		assertEquals(mapper.getMappingForNode( node62, 0 ), mapper.combineData2String( kwc.getReturnStatement() ) );
		
		SwitchEntryStmt node63 = new SwitchEntryStmt();
		assertEquals(mapper.getMappingForNode( node63, 0 ), mapper.combineData2String( kwc.getSwitchEntryStatement() ) );
		
		SwitchStmt node64 = new SwitchStmt();
		assertEquals(mapper.getMappingForNode( node64, 0 ), mapper.combineData2String( kwc.getSwitchStatement() ) );
		
		SynchronizedStmt node65 = new SynchronizedStmt();
		assertEquals(mapper.getMappingForNode( node65, 0 ), mapper.combineData2String( kwc.getSynchronizedStatement() ) );
		
		// we have two throw variants but I check for the simple one from the java parser here
		ThrowStmt node66 = new ThrowStmt();
		assertEquals(mapper.getMappingForNode( node66, 0 ), mapper.combineData2String( kwc.getThrowStatement() ) );
		
		TryStmt node67 = new TryStmt();
		assertEquals(mapper.getMappingForNode( node67, 0 ), mapper.combineData2String( kwc.getTryStatement() ) );
		
		TypeDeclarationStmt node68 = new TypeDeclarationStmt();
		assertEquals(mapper.getMappingForNode( node68, 0 ), mapper.combineData2String( kwc.getTypeDeclarationStatement() ) );
		
		WhileStmt node69 = new WhileStmt();
		assertEquals(mapper.getMappingForNode( node69, 0 ), mapper.combineData2String( kwc.getWhileStatement() ) );
		
		ClassOrInterfaceType node70 = new ClassOrInterfaceType();
		assertEquals(mapper.getMappingForNode( node70, 0 ), mapper.combineData2String( kwc.getClassOrInterfaceType() ) );
		
		// some nodes that were added after the first generation
		BlockComment node71 = new BlockComment();
		assertEquals(mapper.getMappingForNode( node71, 0 ), mapper.combineData2String( kwc.getBlockComment() ) );
		
		JavadocComment node72 = new JavadocComment();
		assertEquals(mapper.getMappingForNode( node72, 0 ), mapper.combineData2String( kwc.getJavadocComment() ) );
		
		LineComment node73 = new LineComment();
		assertEquals(mapper.getMappingForNode( node73, 0 ), mapper.combineData2String( kwc.getLineComment() ) );
		
		ArrayAccessExpr node74 = new ArrayAccessExpr();
		assertEquals(mapper.getMappingForNode( node74, 0 ), mapper.combineData2String( kwc.getArrayAccessExpression() ) );
		
		ArrayCreationExpr node75 = new ArrayCreationExpr();
		assertEquals(mapper.getMappingForNode( node75, 0 ), mapper.combineData2String( kwc.getArrayCreateExpression() ) );
		
		ArrayInitializerExpr node76 = new ArrayInitializerExpr();
		assertEquals(mapper.getMappingForNode( node76, 0 ), mapper.combineData2String( kwc.getArrayInitExpression() ) );
	}
}
