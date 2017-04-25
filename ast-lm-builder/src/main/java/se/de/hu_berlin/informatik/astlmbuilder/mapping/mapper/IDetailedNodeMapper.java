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
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleExportsStmt;
import com.github.javaparser.ast.modules.ModuleOpensStmt;
import com.github.javaparser.ast.modules.ModuleProvidesStmt;
import com.github.javaparser.ast.modules.ModuleRequiresStmt;
import com.github.javaparser.ast.modules.ModuleStmt;
import com.github.javaparser.ast.modules.ModuleUsesStmt;
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

public interface IDetailedNodeMapper<T> extends IBasicNodeMapper<T> {
	
	@Override
	default public T getMappingForNode(Node aNode, int aDepth) {
		// old: just to avoid some null pointer exceptions when a null object is legit
		// update: should catch null in calling methods instead (since we use generics)
		if( aNode == null ) {
			return null;
		}
		
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
		} else if ( aNode instanceof ModuleDeclaration) {
			return getMappingForModuleDeclaration((ModuleDeclaration) aNode, aDepth );
		} else if ( aNode instanceof ModuleStmt) {
			// all module statements
			if ( aNode instanceof ModuleExportsStmt) {
				return getMappingForModuleExportsStmt((ModuleExportsStmt) aNode, aDepth );
			} else if ( aNode instanceof ModuleUsesStmt) {
				return getMappingForModuleUsesStmt((ModuleUsesStmt) aNode, aDepth );
			} else if ( aNode instanceof ModuleProvidesStmt) {
				return getMappingForModuleProvidesStmt((ModuleProvidesStmt) aNode, aDepth );
			} else if ( aNode instanceof ModuleRequiresStmt) {
				return getMappingForModuleRequiresStmt((ModuleRequiresStmt) aNode, aDepth );
			} else if ( aNode instanceof ModuleOpensStmt) {
				return getMappingForModuleOpensStmt((ModuleOpensStmt) aNode, aDepth );
			}
		}
		
		// this should be removed after testing i guess
		// >> I wouldn't remove it, since it doesn't hurt and constitutes a value <<
		return getMappingForUnknownNode(aNode, aDepth );
	}

	default public T getMappingForTypeDeclaration(TypeDeclaration<?> aNode, int aDepth ) {
		// old: just to avoid some null pointer exceptions when a null object is legit
		// update: should catch null in calling methods instead (since we use generics)
		if( aNode == null ) {
			return null;
		}

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
		// old: just to avoid some null pointer exceptions when a null object is legit
		// update: should catch null in calling methods instead (since we use generics)
		if( aNode == null ) {
			return null;
		}

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
		// old: just to avoid some null pointer exceptions when a null object is legit
		// update: should catch null in calling methods instead (since we use generics)
		if( aNode == null ) {
			return null;
		}

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
		} else if ( aNode instanceof LocalClassDeclarationStmt ){
			return getMappingForLocalClassDeclarationStmt((LocalClassDeclarationStmt) aNode, aDepth );
		}

		return getMappingForUnknownNode(aNode, aDepth );
	}

	default public T getMappingForType(Type aNode, int aDepth ) {
		// old: just to avoid some null pointer exceptions when a null object is legit
		// update: should catch null in calling methods instead (since we use generics)
		if( aNode == null ) {
			return null;
		}

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
	
	public T getMappingForUnknownNode(Node aNode, int aDepth );
	public T getMappingForCompilationUnit(CompilationUnit aNode, int aDepth );
	public T getMappingForMemberValuePair(MemberValuePair aNode, int aDepth );
	public T getMappingForVariableDeclarator(VariableDeclarator aNode, int aDepth );
	public T getMappingForCatchClause(CatchClause aNode, int aDepth );
	public T getMappingForTypeParameter(TypeParameter aNode, int aDepth );
	public T getMappingForImportDeclaration(ImportDeclaration aNode, int aDepth );
	public T getMappingForPackageDeclaration(PackageDeclaration aNode, int aDepth );
	public T getMappingForParameter(Parameter aNode, int aDepth );
	public T getMappingForJavadocComment(JavadocComment aNode, int aDepth );
	public T getMappingForBlockComment(BlockComment aNode, int aDepth );
	public T getMappingForLineComment(LineComment aNode, int aDepth );
	public T getMappingForEnumDeclaration(EnumDeclaration aNode, int aDepth );
	public T getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, int aDepth );
	public T getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aDepth );
	public T getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aDepth );
	public T getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aDepth );
	public T getMappingForMethodDeclaration(MethodDeclaration aNode, int aDepth );
	public T getMappingForFieldDeclaration(FieldDeclaration aNode, int aDepth );
	public T getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aDepth );
	public T getMappingForConstructorDeclaration(ConstructorDeclaration aNode, int aDepth );
	public T getMappingForWhileStmt(WhileStmt aNode, int aDepth );
	public T getMappingForTryStmt(TryStmt aNode, int aDepth );
	public T getMappingForThrowStmt(ThrowStmt aNode, int aDepth );
	public T getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aDepth );
	public T getMappingForSwitchStmt(SwitchStmt aNode, int aDepth );
	public T getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aDepth );
	public T getMappingForReturnStmt(ReturnStmt aNode, int aDepth );
	public T getMappingForLabeledStmt(LabeledStmt aNode, int aDepth );
	public T getMappingForIfStmt(IfStmt aNode, int aDepth );
	public T getMappingForForStmt(ForStmt aNode, int aDepth );
	public T getMappingForForeachStmt(ForeachStmt aNode, int aDepth );
	public T getMappingForExpressionStmt(ExpressionStmt aNode, int aDepth );
	public T getMappingForExplicitConstructorInvocationStmt(
			ExplicitConstructorInvocationStmt aNode, int aDepth );
	public T getMappingForDoStmt(DoStmt aNode, int aDepth );
	public T getMappingForContinueStmt(ContinueStmt aNode, int aDepth );
	public T getMappingForBreakStmt(BreakStmt aNode, int aDepth );
	public T getMappingForBlockStmt(BlockStmt aNode, int aDepth );
	public T getMappingForAssertStmt(AssertStmt aNode, int aDepth );
	public T getMappingForWildcardType(WildcardType aNode, int aDepth );
	public T getMappingForVoidType(VoidType aNode, int aDepth );
	public T getMappingForUnknownType(UnknownType aNode, int aDepth );
	public T getMappingForUnionType(UnionType aNode, int aDepth );
	public T getMappingForPrimitiveType(PrimitiveType aNode, int aDepth );
	public T getMappingForIntersectionType(IntersectionType aNode, int aDepth );
	public T getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aDepth );
	public T getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aDepth );
	public T getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aDepth );
	public T getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aDepth );
	public T getMappingForNameExpr(NameExpr aNode, int aDepth );
	public T getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, int aDepth );
	public T getMappingForTypeExpr(TypeExpr aNode, int aDepth );
	public T getMappingForSuperExpr(SuperExpr aNode, int aDepth );
	public T getMappingForUnaryExpr(UnaryExpr aNode, int aDepth );
	public T getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aDepth );
	public T getMappingForEnclosedExpr(EnclosedExpr aNode, int aDepth );
	public T getMappingForThisExpr(ThisExpr aNode, int aDepth );
	public T getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aDepth );
	public T getMappingForMethodCallExpr(MethodCallExpr aNode, int aDepth );
	public T getMappingForLambdaExpr(LambdaExpr aNode, int aDepth );
	public T getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aDepth );
	public T getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aDepth );
	public T getMappingForConditionalExpr(ConditionalExpr aNode, int aDepth );
	public T getMappingForClassExpr(ClassExpr aNode, int aDepth );
	public T getMappingForCastExpr(CastExpr aNode, int aDepth );
	public T getMappingForBinaryExpr(BinaryExpr aNode, int aDepth );
	public T getMappingForAssignExpr(AssignExpr aNode, int aDepth );
	public T getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aDepth );
	public T getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aDepth );
	public T getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aDepth );
	public T getMappingForStringLiteralExpr(StringLiteralExpr aNode, int aDepth );
	public T getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, int aDepth );
	public T getMappingForLongLiteralExpr(LongLiteralExpr aNode, int aDepth );
	public T getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, int aDepth );
	public T getMappingForCharLiteralExpr(CharLiteralExpr aNode, int aDepth );
	public T getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, int aDepth );
	public T getMappingForNullLiteralExpr(NullLiteralExpr aNode, int aDepth );
	public T getMappingForName(Name aNode, int aDepth );
	public T getMappingForSimpleName(SimpleName aNode, int aDepth );
	public T getMappingForLocalClassDeclarationStmt(LocalClassDeclarationStmt aNode, int aDepth );
	public T getMappingForArrayType(ArrayType aNode, int aDepth );
	public T getMappingForArrayCreationLevel(ArrayCreationLevel aNode, int aDepth );
	public T getMappingForModuleDeclaration(ModuleDeclaration aNode, int aDepth );
	public T getMappingForModuleExportsStmt(ModuleExportsStmt aNode, int aDepth );
	public T getMappingForModuleOpensStmt(ModuleOpensStmt aNode, int aDepth );
	public T getMappingForModuleProvidesStmt(ModuleProvidesStmt aNode, int aDepth );
	public T getMappingForModuleRequiresStmt(ModuleRequiresStmt aNode, int aDepth );
	public T getMappingForModuleUsesStmt(ModuleUsesStmt aNode, int aDepth );
	
}
