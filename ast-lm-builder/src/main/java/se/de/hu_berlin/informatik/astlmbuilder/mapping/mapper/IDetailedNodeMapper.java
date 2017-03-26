package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
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
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
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
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
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
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ThrowsStmt;

public interface IDetailedNodeMapper<T> extends IBasicNodeMapper<T> {
	
	@Override
	default public T getMappingForNode(Node aNode, int aDepth) {
		
		if (aNode instanceof Expression) {
			return getMappingForExpression((Expression) aNode, aDepth );
		} else if (aNode instanceof Type) {
			return getMappingForType((Type) aNode, aDepth );
		} else if (aNode instanceof Statement) {
			return getMappingForStatement((Statement) aNode, aDepth );
		} else if (aNode instanceof BodyDeclaration) {
			return getMappingForBodyDeclaration((BodyDeclaration<?>) aNode, aDepth );
		} else if (aNode instanceof Comment) {
			// all comments
			if ( aNode instanceof LineComment) {
				return getMappingForLineComment((LineComment) aNode, aDepth );
			} else if ( aNode instanceof BlockComment) {
				return getMappingForBlockComment((BlockComment) aNode, aDepth );
			} else if ( aNode instanceof JavadocComment) {
				return getMappingForJavadocComment((JavadocComment) aNode, aDepth );
			}
		} else if ( aNode instanceof Parameter ){
			return getMappingForParameter((Parameter) aNode, aDepth );		
		}

		else if( aNode instanceof PackageDeclaration ) {
			return getMappingForPackageDeclaration((PackageDeclaration) aNode, aDepth );
		} else if ( aNode instanceof ImportDeclaration ){
			return getMappingForImportDeclaration((ImportDeclaration) aNode, aDepth );
		}
		
		else if ( aNode instanceof CatchClause ){
			return getMappingForCatchClause((CatchClause) aNode, aDepth );
		} else if ( aNode instanceof VariableDeclarator ){
			return getMappingForVariableDeclarator((VariableDeclarator) aNode, aDepth );
		} else if ( aNode instanceof MemberValuePair ){
			return getMappingForMemberValuePair((MemberValuePair) aNode, aDepth );
		}
		
		// compilation unit
		else if ( aNode instanceof CompilationUnit) {
			return getMappingForCompilationUnit((CompilationUnit) aNode, aDepth );
		}
		
		else if ( aNode instanceof Name) {
			return getMappingForName((Name) aNode, aDepth );
		} else if ( aNode instanceof SimpleName) {
			return getMappingForSimpleName((SimpleName) aNode, aDepth );
		} else if ( aNode instanceof ArrayCreationLevel) {
			return getMappingForArrayCreationLevel((ArrayCreationLevel) aNode, aDepth );
		}
		
		// this should be removed after testing i guess
		// >> I wouldn't remove it, since it doesn't hurt and constitutes a default value <<
		return getMappingForUnknownNode(aNode, aDepth );
	}

	default public T getMappingForTypeDeclaration(TypeDeclaration<?> aNode, int aDepth ) {
		// all type declarations (may all have annotations)
		if (aNode instanceof AnnotationDeclaration) {
			return getMappingForAnnotationDeclaration((AnnotationDeclaration) aNode, aDepth );
		} else if ( aNode instanceof ClassOrInterfaceDeclaration ){
			return getMappingForClassOrInterfaceDeclaration((ClassOrInterfaceDeclaration) aNode, aDepth );
		} else if ( aNode instanceof EnumDeclaration ){
			return getMappingForEnumDeclaration((EnumDeclaration) aNode, aDepth );
		}

		return getMappingForUnknownNode(aNode, aDepth );
	}
	
	default public T getMappingForBodyDeclaration(BodyDeclaration<?> aNode, int aDepth ) {
		// all declarations (may all have annotations)
		if ( aNode instanceof InitializerDeclaration ){
			return getMappingForInitializerDeclaration((InitializerDeclaration) aNode, aDepth );
		} else if ( aNode instanceof FieldDeclaration ){
			return getMappingForFieldDeclaration((FieldDeclaration) aNode, aDepth );	
		} else if ( aNode instanceof EnumConstantDeclaration ){
			return getMappingForEnumConstantDeclaration((EnumConstantDeclaration) aNode, aDepth );
		} else if ( aNode instanceof AnnotationMemberDeclaration ){
			return getMappingForAnnotationMemberDeclaration((AnnotationMemberDeclaration) aNode, aDepth );
		} else if (aNode instanceof TypeDeclaration) {
			return getMappingForTypeDeclaration((TypeDeclaration<?>) aNode, aDepth );
		} else if (aNode instanceof CallableDeclaration) {
			if ( aNode instanceof ConstructorDeclaration ){
				return getMappingForConstructorDeclaration((ConstructorDeclaration) aNode, aDepth );
			} else if ( aNode instanceof MethodDeclaration ){
				return getMappingForMethodDeclaration((MethodDeclaration) aNode, aDepth );
			}
		}

		return getMappingForUnknownNode(aNode, aDepth );
	}
	
	default public T getMappingForStatement(Statement aNode, int aDepth ) {
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
		} else if ( aNode instanceof WhileStmt ){
			return getMappingForWhileStmt((WhileStmt) aNode, aDepth );
		} else if ( aNode instanceof ExtendsStmt ){
			return getMappingForExtendsStmt((ExtendsStmt) aNode, aDepth );
		} else if ( aNode instanceof ImplementsStmt ){
			return getMappingForImplementsStmt((ImplementsStmt) aNode, aDepth );
		} else if ( aNode instanceof LocalClassDeclarationStmt ){
			return getMappingForLocalClassDeclarationStmt((LocalClassDeclarationStmt) aNode, aDepth );
		}

		return getMappingForUnknownNode(aNode, aDepth );
	}

	default public T getMappingForType(Type aNode, int aDepth ) {
		// all types
		if ( aNode instanceof IntersectionType ){			
			return getMappingForIntersectionType((IntersectionType) aNode, aDepth );
		} else if ( aNode instanceof PrimitiveType ){
			return getMappingForPrimitiveType((PrimitiveType) aNode, aDepth );
		} else if ( aNode instanceof ReferenceType ){
			if ( aNode instanceof ClassOrInterfaceType ){			
				return getMappingForClassOrInterfaceType((ClassOrInterfaceType) aNode, aDepth );
			} else if ( aNode instanceof TypeParameter ){
				return getMappingForTypeParameter((TypeParameter) aNode, aDepth );
			} else if ( aNode instanceof ArrayType ){
				return getMappingForArrayType((ArrayType) aNode, aDepth );
			}
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
	
	default public T getMappingForExpression(Expression aNode, int aDepth ) {
		
		// old: just to avoid some null pointer exceptions when a null object is legit
		// update: should catch null in calling methods instead (since we use generics)
		if( aNode == null ) {
			return null;
		}
		
		// all expressions
		if ( aNode instanceof LiteralExpr ){
			if ( aNode instanceof NullLiteralExpr ){
				return getMappingForNullLiteralExpr((NullLiteralExpr) aNode, aDepth );
			} else if ( aNode instanceof BooleanLiteralExpr ){
				return getMappingForBooleanLiteralExpr((BooleanLiteralExpr) aNode, aDepth );
			} else if ( aNode instanceof LiteralStringValueExpr ){
				if ( aNode instanceof StringLiteralExpr ){
					return getMappingForStringLiteralExpr((StringLiteralExpr) aNode, aDepth );
				} else if ( aNode instanceof CharLiteralExpr ){
					return getMappingForCharLiteralExpr((CharLiteralExpr) aNode, aDepth );
				} else if ( aNode instanceof IntegerLiteralExpr ){
					return getMappingForIntegerLiteralExpr((IntegerLiteralExpr) aNode, aDepth );
				} else if ( aNode instanceof LongLiteralExpr ){
					return getMappingForLongLiteralExpr((LongLiteralExpr) aNode, aDepth );
				} else if ( aNode instanceof DoubleLiteralExpr ){
					return getMappingForDoubleLiteralExpr((DoubleLiteralExpr) aNode, aDepth );
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
			return getMappingForNameExpr((NameExpr) aNode, aDepth );
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
	
	default public T getMappingForUnknownNode(Node aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForCompilationUnit(CompilationUnit aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForMemberValuePair(MemberValuePair aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForVariableDeclarator(VariableDeclarator aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForCatchClause(CatchClause aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForTypeParameter(TypeParameter aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForImportDeclaration(ImportDeclaration aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForPackageDeclaration(PackageDeclaration aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForParameter(Parameter aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForJavadocComment(JavadocComment aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForBlockComment(BlockComment aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForLineComment(LineComment aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForEnumDeclaration(EnumDeclaration aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForMethodDeclaration(MethodDeclaration aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForFieldDeclaration(FieldDeclaration aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForConstructorDeclaration(ConstructorDeclaration aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForWhileStmt(WhileStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForTryStmt(TryStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForThrowStmt(ThrowStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForSwitchStmt(SwitchStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForReturnStmt(ReturnStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForLabeledStmt(LabeledStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForElseStmt(ElseStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForExtendsStmt(ExtendsStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForImplementsStmt(ImplementsStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForMethodBodyStmt(BodyStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForThrowsStmt(ThrowsStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForIfStmt(IfStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForForStmt(ForStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForForeachStmt(ForeachStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForExpressionStmt(ExpressionStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForExplicitConstructorInvocationStmt(
			ExplicitConstructorInvocationStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForDoStmt(DoStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForContinueStmt(ContinueStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForBreakStmt(BreakStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForBlockStmt(BlockStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForAssertStmt(AssertStmt aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForWildcardType(WildcardType aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForVoidType(VoidType aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForUnknownType(UnknownType aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForUnionType(UnionType aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForPrimitiveType(PrimitiveType aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForIntersectionType(IntersectionType aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForNameExpr(NameExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForTypeExpr(TypeExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForSuperExpr(SuperExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForUnaryExpr(UnaryExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForEnclosedExpr(EnclosedExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForThisExpr(ThisExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForMethodCallExpr(MethodCallExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForLambdaExpr(LambdaExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForConditionalExpr(ConditionalExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForClassExpr(ClassExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForCastExpr(CastExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForBinaryExpr(BinaryExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForAssignExpr(AssignExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForStringLiteralExpr(StringLiteralExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForLongLiteralExpr(LongLiteralExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForCharLiteralExpr(CharLiteralExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	default public T getMappingForNullLiteralExpr(NullLiteralExpr aNode, int aDepth ) { throw new UnsupportedOperationException(); }
	public T getMappingForName(Name aNode, int aDepth );
	public T getMappingForSimpleName(SimpleName aNode, int aDepth );
	public T getMappingForLocalClassDeclarationStmt(LocalClassDeclarationStmt aNode, int aDepth );
	public T getMappingForArrayType(ArrayType aNode, int aDepth );
	public T getMappingForArrayCreationLevel(ArrayCreationLevel aNode, int aDepth );
	
}
