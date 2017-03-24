package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BaseParameter;
import com.github.javaparser.ast.body.BodyDeclaration;
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
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
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
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralMinValueExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
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
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.TypeDeclarationStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ThrowsStmt;

public interface INodeMapper {
	
	/**
	 * Returns the mapping of the abstract syntax tree node to fit the language model
	 * @param aNode The node that will be used for the creation of the tokens
	 * @param aDepth The depth of serialization or abstraction
	 * @return A token that represents the nodes according to the used method and depth
	 */
	default public String getMappingForNode(Node aNode, int aDepth) {
		
		if (aNode instanceof Expression) {
			return getMappingForExpression((Expression) aNode, aDepth );
		} else if (aNode instanceof Type) {
			return getMappingForType((Type) aNode, aDepth );
		} else if (aNode instanceof Statement) {
			return getMappingForStatement((Statement) aNode, aDepth );
		} else if (aNode instanceof BodyDeclaration) {
			return getMappingForBodyDeclaration((BodyDeclaration) aNode, aDepth );
		} else if (aNode instanceof Comment) {
			// all comments
			if ( aNode instanceof LineComment) {
				return getMappingForLineComment((LineComment) aNode, aDepth );
			} else if ( aNode instanceof BlockComment) {
				return getMappingForBlockComment((BlockComment) aNode, aDepth );
			} else if ( aNode instanceof JavadocComment) {
				return getMappingForJavadocComment((JavadocComment) aNode, aDepth );
			}
		} else if (aNode instanceof BaseParameter) {
			if ( aNode instanceof Parameter ){
				return getMappingForParameter((Parameter) aNode, aDepth );		
			} else if ( aNode instanceof MultiTypeParameter ){
				return getMappingForMultiTypeParameter((MultiTypeParameter) aNode, aDepth );	
			}
		}

		else if( aNode instanceof PackageDeclaration ) {
			return getMappingForPackageDeclaration((PackageDeclaration) aNode, aDepth );
		} else if ( aNode instanceof ImportDeclaration ){
			return getMappingForImportDeclaration((ImportDeclaration) aNode, aDepth );
		} else if ( aNode instanceof TypeParameter ){
			return getMappingForTypeParameter((TypeParameter) aNode, aDepth );
		}
		
		else if ( aNode instanceof CatchClause ){
			return getMappingForCatchClause((CatchClause) aNode, aDepth );
		} else if ( aNode instanceof VariableDeclarator ){
			return getMappingForVariableDeclarator((VariableDeclarator) aNode, aDepth );
		} else if ( aNode instanceof VariableDeclaratorId ){
			return getMappingForVariableDeclaratorId((VariableDeclaratorId) aNode, aDepth );
		} else if ( aNode instanceof MemberValuePair ){
			return getMappingForMemberValuePair((MemberValuePair) aNode, aDepth );
		}
		
		// compilation unit
		else if ( aNode instanceof CompilationUnit) {
			return getMappingForCompilationUnit((CompilationUnit) aNode, aDepth );
		}
		
		// this should be removed after testing i guess
		// >> I wouldn't remove it, since it doesn't hurt and constitutes a default value <<
		return getMappingForUnknownNode(aNode, aDepth );
	}

	default public String getMappingForTypeDeclaration(TypeDeclaration aNode, int aDepth ) {
		// all type declarations (may all have annotations)
		if (aNode instanceof AnnotationDeclaration) {
			return getMappingForAnnotationDeclaration((AnnotationDeclaration) aNode, aDepth );
		} else if ( aNode instanceof ClassOrInterfaceDeclaration ){
			return getMappingForClassOrInterfaceDeclaration((ClassOrInterfaceDeclaration) aNode, aDepth );
		} else if ( aNode instanceof EmptyTypeDeclaration ){
			return getMappingForEmptyTypeDeclaration((EmptyTypeDeclaration) aNode, aDepth );
		} else if ( aNode instanceof EnumDeclaration ){
			return getMappingForEnumDeclaration((EnumDeclaration) aNode, aDepth );
		}

		return getMappingForUnknownNode(aNode, aDepth );
	}
	
	default public String getMappingForBodyDeclaration(BodyDeclaration aNode, int aDepth ) {
		// all declarations (may all have annotations)
		if ( aNode instanceof ConstructorDeclaration ){
			return getMappingForConstructorDeclaration((ConstructorDeclaration) aNode, aDepth );
		} else if ( aNode instanceof InitializerDeclaration ){
			return getMappingForInitializerDeclaration((InitializerDeclaration) aNode, aDepth );
		} else if ( aNode instanceof FieldDeclaration ){
			return getMappingForFieldDeclaration((FieldDeclaration) aNode, aDepth );	
		} else if ( aNode instanceof MethodDeclaration ){
			return getMappingForMethodDeclaration((MethodDeclaration) aNode, aDepth );
		} else if ( aNode instanceof EnumConstantDeclaration ){
			return getMappingForEnumConstantDeclaration((EnumConstantDeclaration) aNode, aDepth );
		} else if ( aNode instanceof AnnotationMemberDeclaration ){
			return getMappingForAnnotationMemberDeclaration((AnnotationMemberDeclaration) aNode, aDepth );
		}  else if ( aNode instanceof EmptyMemberDeclaration ){
			return getMappingForEmptyMemberDeclaration((EmptyMemberDeclaration) aNode, aDepth );
		}else if (aNode instanceof TypeDeclaration) {
			return getMappingForTypeDeclaration((TypeDeclaration) aNode, aDepth );
		}

		return getMappingForUnknownNode(aNode, aDepth );
	}
	
	default public String getMappingForStatement(Statement aNode, int aDepth ) {
		// all statements
		if ( aNode instanceof AssertStmt ){
			return getMappingForAssertStmt((AssertStmt) aNode, aDepth );
		} else if ( aNode instanceof BlockStmt ){
			return getMappingForBlockStmt((BlockStmt) aNode, aDepth );
		} else if ( aNode instanceof BreakStmt ){
			return getMappingForBreakStmt((BreakStmt) aNode, aDepth );
		} else if ( aNode instanceof ContinueStmt ){
			return getMappingForContinueStmt((ContinueStmt) aNode, aDepth );
		} else if ( aNode instanceof DoStmt ){
			return getMappingForDoStmt((DoStmt) aNode, aDepth );
		} else if ( aNode instanceof EmptyStmt ){
			return getMappingForEmptyStmt((EmptyStmt) aNode, aDepth );
		} else if ( aNode instanceof ExplicitConstructorInvocationStmt ){
			return getMappingForExplicitConstructorInvocationStmt((ExplicitConstructorInvocationStmt) aNode, aDepth );
		} else if ( aNode instanceof ExpressionStmt ){
			return getMappingForExpressionStmt((ExpressionStmt) aNode, aDepth );
		} else if ( aNode instanceof ForeachStmt ){
			return getMappingForForeachStmt((ForeachStmt) aNode, aDepth );
		} else if ( aNode instanceof ForStmt ){
			return getMappingForForStmt((ForStmt) aNode, aDepth );
		} else if ( aNode instanceof IfStmt ){
			return getMappingForIfStmt((IfStmt) aNode, aDepth );
		} else if ( aNode instanceof ElseStmt ){
			return getMappingForElseStmt((ElseStmt) aNode, aDepth );
		} else if ( aNode instanceof BodyStmt ){
			return getMappingForMethodBodyStmt((BodyStmt) aNode, aDepth );
		} else if ( aNode instanceof ThrowsStmt ){
			return getMappingForThrowsStmt((ThrowsStmt) aNode, aDepth );
		} else if ( aNode instanceof LabeledStmt ){
			return getMappingForLabeledStmt((LabeledStmt) aNode, aDepth );
		} else if ( aNode instanceof ReturnStmt ){
			return getMappingForReturnStmt((ReturnStmt) aNode, aDepth );
		} else if ( aNode instanceof SwitchEntryStmt ){
			return getMappingForSwitchEntryStmt((SwitchEntryStmt) aNode, aDepth );
		} else if ( aNode instanceof SwitchStmt ){
			return getMappingForSwitchStmt((SwitchStmt) aNode, aDepth );
		} else if ( aNode instanceof SynchronizedStmt ){
			return getMappingForSynchronizedStmt((SynchronizedStmt) aNode, aDepth );
		} else if ( aNode instanceof ThrowStmt ){
			return getMappingForThrowStmt((ThrowStmt) aNode, aDepth );
		} else if ( aNode instanceof TryStmt ){
			return getMappingForTryStmt((TryStmt) aNode, aDepth );
		} else if ( aNode instanceof TypeDeclarationStmt ){
			return getMappingForTypeDeclarationStmt((TypeDeclarationStmt) aNode, aDepth );
		} else if ( aNode instanceof WhileStmt ){
			return getMappingForWhileStmt((WhileStmt) aNode, aDepth );
		} else if ( aNode instanceof ExtendsStmt ){
			return getMappingForExtendsStmt((ExtendsStmt) aNode, aDepth );
		} else if ( aNode instanceof ImplementsStmt ){
			return getMappingForImplementsStmt((ImplementsStmt) aNode, aDepth );
		}

		return getMappingForUnknownNode(aNode, aDepth );
	}

	default public String getMappingForType(Type aNode, int aDepth ) {
		// all types
		if ( aNode instanceof ClassOrInterfaceType ){			
			return getMappingForClassOrInterfaceType((ClassOrInterfaceType) aNode, aDepth );
		} else if ( aNode instanceof IntersectionType ){			
			return getMappingForIntersectionType((IntersectionType) aNode, aDepth );
		} else if ( aNode instanceof PrimitiveType ){
			return getMappingForPrimitiveType((PrimitiveType) aNode, aDepth );
		} else if ( aNode instanceof ReferenceType ){
			return getMappingForReferenceType((ReferenceType) aNode, aDepth );
		} else if ( aNode instanceof UnionType ){
			return getMappingForUnionType((UnionType) aNode, aDepth );
		} else if ( aNode instanceof UnknownType ){
			return getMappingForUnknownType((UnknownType) aNode, aDepth );
		} else if ( aNode instanceof VoidType ){
			return getMappingForVoidType((VoidType) aNode, aDepth );
		} else if ( aNode instanceof WildcardType ){
			return getMappingForWildcardType((WildcardType) aNode, aDepth );
		}
		
		return getMappingForUnknownNode(aNode, aDepth );
	}
	
	default public String getMappingForExpression(Expression aNode, int aDepth ) {
		
		// just to avoid some null pointer exceptions when a null object is legit
		if( aNode == null ) {
			return new String();
		}
		
		// all expressions
		if ( aNode instanceof LiteralExpr ){
			if ( aNode instanceof NullLiteralExpr ){
				return getMappingForNullLiteralExpr((NullLiteralExpr) aNode, aDepth );
			} else if ( aNode instanceof BooleanLiteralExpr ){
				return getMappingForBooleanLiteralExpr((BooleanLiteralExpr) aNode, aDepth );
			} else if ( aNode instanceof StringLiteralExpr ){
				if ( aNode instanceof CharLiteralExpr ){
					return getMappingForCharLiteralExpr((CharLiteralExpr) aNode, aDepth );
				} else if ( aNode instanceof IntegerLiteralExpr ){
					if ( aNode instanceof IntegerLiteralMinValueExpr ){
						return getMappingForIntegerLiteralMinValueExpr((IntegerLiteralMinValueExpr) aNode, aDepth );
					} else {
						return getMappingForIntegerLiteralExpr((IntegerLiteralExpr) aNode, aDepth );
					}
				} else if ( aNode instanceof LongLiteralExpr ){
					if ( aNode instanceof LongLiteralMinValueExpr ){
						return getMappingForLongLiteralMinValueExpr((LongLiteralMinValueExpr) aNode, aDepth );
					} else {
						return getMappingForLongLiteralExpr((LongLiteralExpr) aNode, aDepth );
					}
				} else if ( aNode instanceof DoubleLiteralExpr ){
					return getMappingForDoubleLiteralExpr((DoubleLiteralExpr) aNode, aDepth );
				} else {
					return getMappingForStringLiteralExpr((StringLiteralExpr) aNode, aDepth );
				}
			}
		} else if ( aNode instanceof ArrayAccessExpr ){
			return getMappingForArrayAccessExpr((ArrayAccessExpr) aNode, aDepth );
		} else if ( aNode instanceof ArrayCreationExpr ){
			return getMappingForArrayCreationExpr((ArrayCreationExpr) aNode, aDepth );
		} else if ( aNode instanceof ArrayInitializerExpr ){
			return getMappingForArrayInitializerExpr((ArrayInitializerExpr) aNode, aDepth );
		} else if ( aNode instanceof AssignExpr ){
			return getMappingForAssignExpr((AssignExpr) aNode, aDepth );
		} else if ( aNode instanceof BinaryExpr ){
			return getMappingForBinaryExpr((BinaryExpr) aNode, aDepth );
		} else if ( aNode instanceof CastExpr ){
			return getMappingForCastExpr((CastExpr) aNode, aDepth );
		} else if ( aNode instanceof ClassExpr ){
			return getMappingForClassExpr((ClassExpr) aNode, aDepth );
		} else if ( aNode instanceof ConditionalExpr ){
			return getMappingForConditionalExpr((ConditionalExpr) aNode, aDepth );
		} else if ( aNode instanceof FieldAccessExpr ){
			return getMappingForFieldAccessExpr((FieldAccessExpr) aNode, aDepth );
		} else if ( aNode instanceof InstanceOfExpr ){
			return getMappingForInstanceOfExpr((InstanceOfExpr) aNode, aDepth );
		} else if ( aNode instanceof LambdaExpr ){
			return getMappingForLambdaExpr((LambdaExpr) aNode, aDepth );
		} else if ( aNode instanceof MethodCallExpr ){
			return getMappingForMethodCallExpr((MethodCallExpr) aNode, aDepth );
		} else if ( aNode instanceof MethodReferenceExpr ){
			return getMappingForMethodReferenceExpr((MethodReferenceExpr) aNode, aDepth );
		} else if ( aNode instanceof ThisExpr ){
			return getMappingForThisExpr((ThisExpr) aNode, aDepth );
		} else if ( aNode instanceof EnclosedExpr ){
			return getMappingForEnclosedExpr((EnclosedExpr) aNode, aDepth );
		}  else if ( aNode instanceof ObjectCreationExpr ){
			return getMappingForObjectCreationExpr((ObjectCreationExpr) aNode, aDepth );
		} else if ( aNode instanceof UnaryExpr ){
			return getMappingForUnaryExpr((UnaryExpr) aNode, aDepth );
		} else if ( aNode instanceof SuperExpr ){
			return getMappingForSuperExpr((SuperExpr) aNode, aDepth );
		} else if ( aNode instanceof TypeExpr ){
			return getMappingForTypeExpr((TypeExpr) aNode, aDepth );
		} else if ( aNode instanceof VariableDeclarationExpr ){
			return getMappingForVariableDeclarationExpr((VariableDeclarationExpr) aNode, aDepth );
		} else if ( aNode instanceof NameExpr ){
			if ( aNode instanceof QualifiedNameExpr ){
				return getMappingForQualifiedNameExpr((QualifiedNameExpr) aNode, aDepth );
			} else {
				return getMappingForNameExpr((NameExpr) aNode, aDepth );
			}
		} else if ( aNode instanceof AnnotationExpr ){
			if ( aNode instanceof MarkerAnnotationExpr ){
				return getMappingForMarkerAnnotationExpr((MarkerAnnotationExpr) aNode, aDepth );
			} else if ( aNode instanceof NormalAnnotationExpr ){
				return getMappingForNormalAnnotationExpr((NormalAnnotationExpr) aNode, aDepth );
			} else if ( aNode instanceof SingleMemberAnnotationExpr ){
				return getMappingForSingleMemberAnnotationExpr((SingleMemberAnnotationExpr) aNode, aDepth );
			}
		}
		
		return getMappingForUnknownNode(aNode, aDepth );
	}
	
	default public String getMappingForUnknownNode(Node aNode, int aDepth ) { return null; }
	default public String getMappingForCompilationUnit(CompilationUnit aNode, int aDepth ) { return null; }
	default public String getMappingForMemberValuePair(MemberValuePair aNode, int aDepth ) { return null; }
	default public String getMappingForVariableDeclaratorId(VariableDeclaratorId aNode, int aDepth ) { return null; }
	default public String getMappingForVariableDeclarator(VariableDeclarator aNode, int aDepth ) { return null; }
	default public String getMappingForCatchClause(CatchClause aNode, int aDepth ) { return null; }
	default public String getMappingForTypeParameter(TypeParameter aNode, int aDepth ) { return null; }
	default public String getMappingForImportDeclaration(ImportDeclaration aNode, int aDepth ) { return null; }
	default public String getMappingForPackageDeclaration(PackageDeclaration aNode, int aDepth ) { return null; }
	default public String getMappingForMultiTypeParameter(MultiTypeParameter aNode, int aDepth ) { return null; }
	default public String getMappingForParameter(Parameter aNode, int aDepth ) { return null; }
	default public String getMappingForJavadocComment(JavadocComment aNode, int aDepth ) { return null; }
	default public String getMappingForBlockComment(BlockComment aNode, int aDepth ) { return null; }
	default public String getMappingForLineComment(LineComment aNode, int aDepth ) { return null; }
	default public String getMappingForEnumDeclaration(EnumDeclaration aNode, int aDepth ) { return null; }
	default public String getMappingForEmptyTypeDeclaration(EmptyTypeDeclaration aNode, int aDepth ) { return null; }
	default public String getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, int aDepth ) { return null; }
	default public String getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aDepth ) { return null; }
	default public String getMappingForEmptyMemberDeclaration(EmptyMemberDeclaration aNode, int aDepth ) { return null; }
	default public String getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aDepth ) { return null; }
	default public String getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aDepth ) { return null; }
	default public String getMappingForMethodDeclaration(MethodDeclaration aNode, int aDepth ) { return null; }
	default public String getMappingForFieldDeclaration(FieldDeclaration aNode, int aDepth ) { return null; }
	default public String getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aDepth ) { return null; }
	default public String getMappingForConstructorDeclaration(ConstructorDeclaration aNode, int aDepth ) { return null; }
	default public String getMappingForWhileStmt(WhileStmt aNode, int aDepth ) { return null; }
	default public String getMappingForTypeDeclarationStmt(TypeDeclarationStmt aNode, int aDepth ) { return null; }
	default public String getMappingForTryStmt(TryStmt aNode, int aDepth ) { return null; }
	default public String getMappingForThrowStmt(ThrowStmt aNode, int aDepth ) { return null; }
	default public String getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aDepth ) { return null; }
	default public String getMappingForSwitchStmt(SwitchStmt aNode, int aDepth ) { return null; }
	default public String getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aDepth ) { return null; }
	default public String getMappingForReturnStmt(ReturnStmt aNode, int aDepth ) { return null; }
	default public String getMappingForLabeledStmt(LabeledStmt aNode, int aDepth ) { return null; }
	default public String getMappingForElseStmt(ElseStmt aNode, int aDepth ) { return null; }
	default public String getMappingForExtendsStmt(ExtendsStmt aNode, int aDepth ) { return null; }
	default public String getMappingForImplementsStmt(ImplementsStmt aNode, int aDepth ) { return null; }
	default public String getMappingForMethodBodyStmt(BodyStmt aNode, int aDepth ) { return null; }
	default public String getMappingForThrowsStmt(ThrowsStmt aNode, int aDepth ) { return null; }
	default public String getMappingForIfStmt(IfStmt aNode, int aDepth ) { return null; }
	default public String getMappingForForStmt(ForStmt aNode, int aDepth ) { return null; }
	default public String getMappingForForeachStmt(ForeachStmt aNode, int aDepth ) { return null; }
	default public String getMappingForExpressionStmt(ExpressionStmt aNode, int aDepth ) { return null; }
	default public String getMappingForExplicitConstructorInvocationStmt(
			ExplicitConstructorInvocationStmt aNode, int aDepth ) { return null; }
	default public String getMappingForEmptyStmt(EmptyStmt aNode, int aDepth ) { return null; }
	default public String getMappingForDoStmt(DoStmt aNode, int aDepth ) { return null; }
	default public String getMappingForContinueStmt(ContinueStmt aNode, int aDepth ) { return null; }
	default public String getMappingForBreakStmt(BreakStmt aNode, int aDepth ) { return null; }
	default public String getMappingForBlockStmt(BlockStmt aNode, int aDepth ) { return null; }
	default public String getMappingForAssertStmt(AssertStmt aNode, int aDepth ) { return null; }
	default public String getMappingForWildcardType(WildcardType aNode, int aDepth ) { return null; }
	default public String getMappingForVoidType(VoidType aNode, int aDepth ) { return null; }
	default public String getMappingForUnknownType(UnknownType aNode, int aDepth ) { return null; }
	default public String getMappingForUnionType(UnionType aNode, int aDepth ) { return null; }
	default public String getMappingForReferenceType(ReferenceType aNode, int aDepth ) { return null; }
	default public String getMappingForPrimitiveType(PrimitiveType aNode, int aDepth ) { return null; }
	default public String getMappingForIntersectionType(IntersectionType aNode, int aDepth ) { return null; }
	default public String getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aDepth ) { return null; }
	default public String getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aDepth ) { return null; }
	default public String getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aDepth ) { return null; }
	default public String getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aDepth ) { return null; }
	default public String getMappingForNameExpr(NameExpr aNode, int aDepth ) { return null; }
	default public String getMappingForQualifiedNameExpr(QualifiedNameExpr aNode, int aDepth ) { return null; }
	default public String getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, int aDepth ) { return null; }
	default public String getMappingForTypeExpr(TypeExpr aNode, int aDepth ) { return null; }
	default public String getMappingForSuperExpr(SuperExpr aNode, int aDepth ) { return null; }
	default public String getMappingForUnaryExpr(UnaryExpr aNode, int aDepth ) { return null; }
	default public String getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aDepth ) { return null; }
	default public String getMappingForEnclosedExpr(EnclosedExpr aNode, int aDepth ) { return null; }
	default public String getMappingForThisExpr(ThisExpr aNode, int aDepth ) { return null; }
	default public String getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aDepth ) { return null; }
	default public String getMappingForMethodCallExpr(MethodCallExpr aNode, int aDepth ) { return null; }
	default public String getMappingForLambdaExpr(LambdaExpr aNode, int aDepth ) { return null; }
	default public String getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aDepth ) { return null; }
	default public String getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aDepth ) { return null; }
	default public String getMappingForConditionalExpr(ConditionalExpr aNode, int aDepth ) { return null; }
	default public String getMappingForClassExpr(ClassExpr aNode, int aDepth ) { return null; }
	default public String getMappingForCastExpr(CastExpr aNode, int aDepth ) { return null; }
	default public String getMappingForBinaryExpr(BinaryExpr aNode, int aDepth ) { return null; }
	default public String getMappingForAssignExpr(AssignExpr aNode, int aDepth ) { return null; }
	default public String getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aDepth ) { return null; }
	default public String getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aDepth ) { return null; }
	default public String getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aDepth ) { return null; }
	default public String getMappingForStringLiteralExpr(StringLiteralExpr aNode, int aDepth ) { return null; }
	default public String getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, int aDepth ) { return null; }
	default public String getMappingForLongLiteralExpr(LongLiteralExpr aNode, int aDepth ) { return null; }
	default public String getMappingForLongLiteralMinValueExpr(LongLiteralMinValueExpr aNode, int aDepth ) { return null; }
	default public String getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, int aDepth ) { return null; }
	default public String getMappingForIntegerLiteralMinValueExpr(IntegerLiteralMinValueExpr aNode, int aDepth ) { return null; }
	default public String getMappingForCharLiteralExpr(CharLiteralExpr aNode, int aDepth ) { return null; }
	default public String getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, int aDepth ) { return null; }
	default public String getMappingForNullLiteralExpr(NullLiteralExpr aNode, int aDepth ) { return null; }
	
}
