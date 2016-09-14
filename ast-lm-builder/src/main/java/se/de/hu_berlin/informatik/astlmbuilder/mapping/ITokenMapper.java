package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.Collection;

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

import se.de.hu_berlin.informatik.astlmbuilder.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ThrowsStmt;

public interface ITokenMapper<T,V> {
	
	/**
	 * Returns the mapping of the abstract syntax tree node to fit the language model
	 * @param aNode 
	 * the node that should be mapped
	 * @param values
	 * some optional configuration values
	 * @return 
	 * the string representation enclosed in a wrapper
	 */
	default public MappingWrapper<T> getMappingForNode(Node aNode, @SuppressWarnings("unchecked") V... values) {
		
		if (aNode instanceof Expression) {
			return getMappingForExpression((Expression) aNode, values);
		} else if (aNode instanceof Type) {
			return getMappingForType((Type) aNode, values);
		} else if (aNode instanceof Statement) {
			return getMappingForStatement((Statement) aNode, values);
		} else if (aNode instanceof BodyDeclaration) {
			return getMappingForBodyDeclaration((BodyDeclaration) aNode, values);
		} else if (aNode instanceof Comment) {
			// all comments
			if ( aNode instanceof LineComment) {
				return getMappingForLineComment((LineComment) aNode, values);
			} else if ( aNode instanceof BlockComment) {
				return getMappingForBlockComment((BlockComment) aNode, values);
			} else if ( aNode instanceof JavadocComment) {
				return getMappingForJavadocComment((JavadocComment) aNode, values);
			}
		} else if (aNode instanceof BaseParameter) {
			if ( aNode instanceof Parameter ){
				return getMappingForParameter((Parameter) aNode, values);		
			} else if ( aNode instanceof MultiTypeParameter ){
				return getMappingForMultiTypeParameter((MultiTypeParameter) aNode, values);	
			}
		}

		else if( aNode instanceof PackageDeclaration ) {
			return getMappingForPackageDeclaration((PackageDeclaration) aNode, values);
		} else if ( aNode instanceof ImportDeclaration ){
			return getMappingForImportDeclaration((ImportDeclaration) aNode, values);
		} else if ( aNode instanceof TypeParameter ){
			return getMappingForTypeParameter((TypeParameter) aNode, values);
		}
		
		else if ( aNode instanceof CatchClause ){
			return getMappingForCatchClause((CatchClause) aNode, values);
		} else if ( aNode instanceof VariableDeclarator ){
			return getMappingForVariableDeclarator((VariableDeclarator) aNode, values);
		} else if ( aNode instanceof VariableDeclaratorId ){
			return getMappingForVariableDeclaratorId((VariableDeclaratorId) aNode, values);
		} else if ( aNode instanceof MemberValuePair ){
			return getMappingForMemberValuePair((MemberValuePair) aNode, values);
		}
		
		// compilation unit
		else if ( aNode instanceof CompilationUnit) {
			return getMappingForCompilationUnit((CompilationUnit) aNode, values);
		}
		
		// this should be removed after testing i guess
		// >> I wouldn't remove it, since it doesn't hurt and constitutes a default value <<
		return getMappingForUnknownNode(aNode, values);
	}

	default public MappingWrapper<T> getMappingForTypeDeclaration(TypeDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		// all type declarations (may all have annotations)
		if (aNode instanceof AnnotationDeclaration) {
			return getMappingForAnnotationDeclaration((AnnotationDeclaration) aNode, values);
		} else if ( aNode instanceof ClassOrInterfaceDeclaration ){
			return getMappingForClassOrInterfaceDeclaration((ClassOrInterfaceDeclaration) aNode, values);
		} else if ( aNode instanceof EmptyTypeDeclaration ){
			return getMappingForEmptyTypeDeclaration((EmptyTypeDeclaration) aNode, values);
		} else if ( aNode instanceof EnumDeclaration ){
			return getMappingForEnumDeclaration((EnumDeclaration) aNode, values);
		}

		return getMappingForUnknownNode(aNode, values);
	}
	
	default public MappingWrapper<T> getMappingForBodyDeclaration(BodyDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		// all declarations (may all have annotations)
		if ( aNode instanceof ConstructorDeclaration ){
			return getMappingForConstructorDeclaration((ConstructorDeclaration) aNode, values);
		} else if ( aNode instanceof InitializerDeclaration ){
			return getMappingForInitializerDeclaration((InitializerDeclaration) aNode, values);
		} else if ( aNode instanceof FieldDeclaration ){
			return getMappingForFieldDeclaration((FieldDeclaration) aNode, values);	
		} else if ( aNode instanceof MethodDeclaration ){
			return getMappingForMethodDeclaration((MethodDeclaration) aNode, values);
		} else if ( aNode instanceof EnumConstantDeclaration ){
			return getMappingForEnumConstantDeclaration((EnumConstantDeclaration) aNode, values);
		} else if ( aNode instanceof AnnotationMemberDeclaration ){
			return getMappingForAnnotationMemberDeclaration((AnnotationMemberDeclaration) aNode, values);
		}  else if ( aNode instanceof EmptyMemberDeclaration ){
			return getMappingForEmptyMemberDeclaration((EmptyMemberDeclaration) aNode, values);
		}else if (aNode instanceof TypeDeclaration) {
			return getMappingForTypeDeclaration((TypeDeclaration) aNode, values);
		}

		return getMappingForUnknownNode(aNode, values);
	}
	
	default public MappingWrapper<T> getMappingForStatement(Statement aNode, @SuppressWarnings("unchecked") V... values) {
		// all statements
		if ( aNode instanceof AssertStmt ){
			return getMappingForAssertStmt((AssertStmt) aNode, values);
		} else if ( aNode instanceof BlockStmt ){
			return getMappingForBlockStmt((BlockStmt) aNode, values);
		} else if ( aNode instanceof BreakStmt ){
			return getMappingForBreakStmt((BreakStmt) aNode, values);
		} else if ( aNode instanceof ContinueStmt ){
			return getMappingForContinueStmt((ContinueStmt) aNode, values);
		} else if ( aNode instanceof DoStmt ){
			return getMappingForDoStmt((DoStmt) aNode, values);
		} else if ( aNode instanceof EmptyStmt ){
			return getMappingForEmptyStmt((EmptyStmt) aNode, values);
		} else if ( aNode instanceof ExplicitConstructorInvocationStmt ){
			return getMappingForExplicitConstructorInvocationStmt((ExplicitConstructorInvocationStmt) aNode, values);
		} else if ( aNode instanceof ExpressionStmt ){
			return getMappingForExpressionStmt((ExpressionStmt) aNode, values);
		} else if ( aNode instanceof ForeachStmt ){
			return getMappingForForeachStmt((ForeachStmt) aNode, values);
		} else if ( aNode instanceof ForStmt ){
			return getMappingForForStmt((ForStmt) aNode, values);
		} else if ( aNode instanceof IfStmt ){
			return getMappingForIfStmt((IfStmt) aNode, values);
		} else if ( aNode instanceof ElseStmt ){
			return getMappingForElseStmt((ElseStmt) aNode, values);
		} else if ( aNode instanceof BodyStmt ){
			return getMappingForMethodBodyStmt((BodyStmt) aNode, values);
		} else if ( aNode instanceof ThrowsStmt ){
			return getMappingForThrowsStmt((ThrowsStmt) aNode, values);
		} else if ( aNode instanceof LabeledStmt ){
			return getMappingForLabeledStmt((LabeledStmt) aNode, values);
		} else if ( aNode instanceof ReturnStmt ){
			return getMappingForReturnStmt((ReturnStmt) aNode, values);
		} else if ( aNode instanceof SwitchEntryStmt ){
			return getMappingForSwitchEntryStmt((SwitchEntryStmt) aNode, values);
		} else if ( aNode instanceof SwitchStmt ){
			return getMappingForSwitchStmt((SwitchStmt) aNode, values);
		} else if ( aNode instanceof SynchronizedStmt ){
			return getMappingForSynchronizedStmt((SynchronizedStmt) aNode, values);
		} else if ( aNode instanceof ThrowStmt ){
			return getMappingForThrowStmt((ThrowStmt) aNode, values);
		} else if ( aNode instanceof TryStmt ){
			return getMappingForTryStmt((TryStmt) aNode, values);
		} else if ( aNode instanceof TypeDeclarationStmt ){
			return getMappingForTypeDeclarationStmt((TypeDeclarationStmt) aNode, values);
		} else if ( aNode instanceof WhileStmt ){
			return getMappingForWhileStmt((WhileStmt) aNode, values);
		} else if ( aNode instanceof ExtendsStmt ){
			return getMappingForExtendsStmt((ExtendsStmt) aNode, values);
		} else if ( aNode instanceof ImplementsStmt ){
			return getMappingForImplementsStmt((ImplementsStmt) aNode, values);
		}

		return getMappingForUnknownNode(aNode, values);
	}

	default public MappingWrapper<T> getMappingForType(Type aNode, @SuppressWarnings("unchecked") V... values) {
		// all types
		if ( aNode instanceof ClassOrInterfaceType ){			
			return getMappingForClassOrInterfaceType((ClassOrInterfaceType) aNode, values);
		} else if ( aNode instanceof IntersectionType ){			
			return getMappingForIntersectionType((IntersectionType) aNode, values);
		} else if ( aNode instanceof PrimitiveType ){
			return getMappingForPrimitiveType((PrimitiveType) aNode, values);
		} else if ( aNode instanceof ReferenceType ){
			return getMappingForReferenceType((ReferenceType) aNode, values);
		} else if ( aNode instanceof UnionType ){
			return getMappingForUnionType((UnionType) aNode, values);
		} else if ( aNode instanceof UnknownType ){
			return getMappingForUnknownType((UnknownType) aNode, values);
		} else if ( aNode instanceof VoidType ){
			return getMappingForVoidType((VoidType) aNode, values);
		} else if ( aNode instanceof WildcardType ){
			return getMappingForWildcardType((WildcardType) aNode, values);
		}
		
		return getMappingForUnknownNode(aNode, values);
	}
	
	default public MappingWrapper<T> getMappingForExpression(Expression aNode, @SuppressWarnings("unchecked") V... values) {
		// all expressions
		if ( aNode instanceof LiteralExpr ){
			if ( aNode instanceof NullLiteralExpr ){
				return getMappingForNullLiteralExpr((NullLiteralExpr) aNode, values);
			} else if ( aNode instanceof BooleanLiteralExpr ){
				return getMappingForBooleanLiteralExpr((BooleanLiteralExpr) aNode, values);
			} else if ( aNode instanceof StringLiteralExpr ){
				if ( aNode instanceof CharLiteralExpr ){
					return getMappingForCharLiteralExpr((CharLiteralExpr) aNode, values);
				} else if ( aNode instanceof IntegerLiteralExpr ){
					if ( aNode instanceof IntegerLiteralMinValueExpr ){
						return getMappingForIntegerLiteralMinValueExpr((IntegerLiteralMinValueExpr) aNode, values);
					} else {
						return getMappingForIntegerLiteralExpr((IntegerLiteralExpr) aNode, values);
					}
				} else if ( aNode instanceof LongLiteralExpr ){
					if ( aNode instanceof LongLiteralMinValueExpr ){
						return getMappingForLongLiteralMinValueExpr((LongLiteralMinValueExpr) aNode, values);
					} else {
						return getMappingForLongLiteralExpr((LongLiteralExpr) aNode, values);
					}
				} else if ( aNode instanceof DoubleLiteralExpr ){
					return getMappingForDoubleLiteralExpr((DoubleLiteralExpr) aNode, values);
				} else {
					return getMappingForStringLiteralExpr((StringLiteralExpr) aNode, values);
				}
			}
		} else if ( aNode instanceof ArrayAccessExpr ){
			return getMappingForArrayAccessExpr((ArrayAccessExpr) aNode, values);
		} else if ( aNode instanceof ArrayCreationExpr ){
			return getMappingForArrayCreationExpr((ArrayCreationExpr) aNode, values);
		} else if ( aNode instanceof ArrayInitializerExpr ){
			return getMappingForArrayInitializerExpr((ArrayInitializerExpr) aNode, values);
		} else if ( aNode instanceof AssignExpr ){
			return getMappingForAssignExpr((AssignExpr) aNode, values);
		} else if ( aNode instanceof BinaryExpr ){
			return getMappingForBinaryExpr((BinaryExpr) aNode, values);
		} else if ( aNode instanceof CastExpr ){
			return getMappingForCastExpr((CastExpr) aNode, values);
		} else if ( aNode instanceof ClassExpr ){
			return getMappingForClassExpr((ClassExpr) aNode, values);
		} else if ( aNode instanceof ConditionalExpr ){
			return getMappingForConditionalExpr((ConditionalExpr) aNode, values);
		} else if ( aNode instanceof FieldAccessExpr ){
			return getMappingForFieldAccessExpr((FieldAccessExpr) aNode, values);
		} else if ( aNode instanceof InstanceOfExpr ){
			return getMappingForInstanceOfExpr((InstanceOfExpr) aNode, values);
		} else if ( aNode instanceof LambdaExpr ){
			return getMappingForLambdaExpr((LambdaExpr) aNode, values);
		} else if ( aNode instanceof MethodCallExpr ){
			return getMappingForMethodCallExpr((MethodCallExpr) aNode, values);
		} else if ( aNode instanceof MethodReferenceExpr ){
			return getMappingForMethodReferenceExpr((MethodReferenceExpr) aNode, values);
		} else if ( aNode instanceof ThisExpr ){
			return getMappingForThisExpr((ThisExpr) aNode, values);
		} else if ( aNode instanceof EnclosedExpr ){
			return getMappingForEnclosedExpr((EnclosedExpr) aNode, values);
		}  else if ( aNode instanceof ObjectCreationExpr ){
			return getMappingForObjectCreationExpr((ObjectCreationExpr) aNode, values);
		} else if ( aNode instanceof UnaryExpr ){
			return getMappingForUnaryExpr((UnaryExpr) aNode, values);
		} else if ( aNode instanceof SuperExpr ){
			return getMappingForSuperExpr((SuperExpr) aNode, values);
		} else if ( aNode instanceof TypeExpr ){
			return getMappingForTypeExpr((TypeExpr) aNode, values);
		} else if ( aNode instanceof VariableDeclarationExpr ){
			return getMappingForVariableDeclarationExpr((VariableDeclarationExpr) aNode, values);
		} else if ( aNode instanceof NameExpr ){
			if ( aNode instanceof QualifiedNameExpr ){
				return getMappingForQualifiedNameExpr((QualifiedNameExpr) aNode, values);
			} else {
				return getMappingForNameExpr((NameExpr) aNode, values);
			}
		} else if ( aNode instanceof AnnotationExpr ){
			if ( aNode instanceof MarkerAnnotationExpr ){
				return getMappingForMarkerAnnotationExpr((MarkerAnnotationExpr) aNode, values);
			} else if ( aNode instanceof NormalAnnotationExpr ){
				return getMappingForNormalAnnotationExpr((NormalAnnotationExpr) aNode, values);
			} else if ( aNode instanceof SingleMemberAnnotationExpr ){
				return getMappingForSingleMemberAnnotationExpr((SingleMemberAnnotationExpr) aNode, values);
			}
		}
		
		return getMappingForUnknownNode(aNode, values);
	}
	
	default public MappingWrapper<T> getMappingForUnknownNode(Node aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForCompilationUnit(CompilationUnit aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForMemberValuePair(MemberValuePair aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForVariableDeclaratorId(VariableDeclaratorId aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForVariableDeclarator(VariableDeclarator aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForCatchClause(CatchClause aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForTypeParameter(TypeParameter aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForImportDeclaration(ImportDeclaration aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForPackageDeclaration(PackageDeclaration aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForMultiTypeParameter(MultiTypeParameter aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForParameter(Parameter aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForJavadocComment(JavadocComment aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForBlockComment(BlockComment aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForLineComment(LineComment aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForEnumDeclaration(EnumDeclaration aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForEmptyTypeDeclaration(EmptyTypeDeclaration aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForEmptyMemberDeclaration(EmptyMemberDeclaration aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForMethodDeclaration(MethodDeclaration aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForFieldDeclaration(FieldDeclaration aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForInitializerDeclaration(InitializerDeclaration aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForConstructorDeclaration(ConstructorDeclaration aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForWhileStmt(WhileStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForTypeDeclarationStmt(TypeDeclarationStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForTryStmt(TryStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForThrowStmt(ThrowStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForSynchronizedStmt(SynchronizedStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForSwitchStmt(SwitchStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForReturnStmt(ReturnStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForLabeledStmt(LabeledStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForElseStmt(ElseStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForExtendsStmt(ExtendsStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForImplementsStmt(ImplementsStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForMethodBodyStmt(BodyStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForThrowsStmt(ThrowsStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForIfStmt(IfStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForForStmt(ForStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForForeachStmt(ForeachStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForExpressionStmt(ExpressionStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForExplicitConstructorInvocationStmt(
			ExplicitConstructorInvocationStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForEmptyStmt(EmptyStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForDoStmt(DoStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForContinueStmt(ContinueStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForBreakStmt(BreakStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForBlockStmt(BlockStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForAssertStmt(AssertStmt aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForWildcardType(WildcardType aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForVoidType(VoidType aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForUnknownType(UnknownType aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForUnionType(UnionType aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForReferenceType(ReferenceType aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForPrimitiveType(PrimitiveType aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForIntersectionType(IntersectionType aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForNameExpr(NameExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForQualifiedNameExpr(QualifiedNameExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForTypeExpr(TypeExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForSuperExpr(SuperExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForUnaryExpr(UnaryExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForObjectCreationExpr(ObjectCreationExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForEnclosedExpr(EnclosedExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForThisExpr(ThisExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForMethodCallExpr(MethodCallExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForLambdaExpr(LambdaExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForInstanceOfExpr(InstanceOfExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForFieldAccessExpr(FieldAccessExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForConditionalExpr(ConditionalExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForClassExpr(ClassExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForCastExpr(CastExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForBinaryExpr(BinaryExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForAssignExpr(AssignExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForArrayCreationExpr(ArrayCreationExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForArrayAccessExpr(ArrayAccessExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForStringLiteralExpr(StringLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForLongLiteralExpr(LongLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForLongLiteralMinValueExpr(LongLiteralMinValueExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForIntegerLiteralMinValueExpr(IntegerLiteralMinValueExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForCharLiteralExpr(CharLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	default public MappingWrapper<T> getMappingForNullLiteralExpr(NullLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) { return null; }
	
	/**
	 * Returns a closing token for some block nodes
	 * 
	 * @param aNode
	 * an AST node for which the closing token shall be generated
	 * @param values
	 * some optional configuration values
	 * @return 
	 * closing token or null if the node has none
	 */
	public T getClosingToken(Node aNode, @SuppressWarnings("unchecked") V... values);
	
	/**
	 * Passes a black list of method names to the mapper.
	 * @param aBL 
	 * a collection of method names that should be handled differently
	 */
	public void setPrivMethodBlackList( Collection<String> aBL );
	
	/**
	 * Clears the black list of method names from this mapper
	 */
	public void clearPrivMethodBlackList();
	
}
