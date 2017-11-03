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
import com.github.javaparser.ast.stmt.EmptyStmt;
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
	default public T getMappingForNode(Node aNode, int aDepth, boolean includeParent) {
		T result = null;
		// old: just to avoid some null pointer exceptions when a null object is
		// legit
		// update: should catch null in calling methods instead (since we use
		// generics)
		if (aNode == null) {
			return null;
		}

		else if (aNode instanceof Expression) {
			result = getMappingForExpression((Expression) aNode, aDepth, includeParent);
		} else if (aNode instanceof Type) {
			result = getMappingForType((Type) aNode, aDepth, includeParent);
		} else if (aNode instanceof Statement) {
			result = getMappingForStatement((Statement) aNode, aDepth, includeParent);
		} else if (aNode instanceof BodyDeclaration) {
			result = getMappingForBodyDeclaration((BodyDeclaration<?>) aNode, aDepth, includeParent);
		} else if (aNode instanceof Comment) {
			// all comments
			if (aNode instanceof LineComment) {
				result = getMappingForLineComment((LineComment) aNode, aDepth, includeParent);
			} else if (aNode instanceof BlockComment) {
				result = getMappingForBlockComment((BlockComment) aNode, aDepth, includeParent);
			} else if (aNode instanceof JavadocComment) {
				result = getMappingForJavadocComment((JavadocComment) aNode, aDepth, includeParent);
			}
		} else if (aNode instanceof Parameter) {
			result = getMappingForParameter((Parameter) aNode, aDepth, includeParent);
		}

		else if (aNode instanceof PackageDeclaration) {
			result = getMappingForPackageDeclaration((PackageDeclaration) aNode, aDepth, includeParent);
		} else if (aNode instanceof ImportDeclaration) {
			result = getMappingForImportDeclaration((ImportDeclaration) aNode, aDepth, includeParent);
		}

		else if (aNode instanceof CatchClause) {
			result = getMappingForCatchClause((CatchClause) aNode, aDepth, includeParent);
		} else if (aNode instanceof VariableDeclarator) {
			result = getMappingForVariableDeclarator((VariableDeclarator) aNode, aDepth, includeParent);
		} else if (aNode instanceof MemberValuePair) {
			result = getMappingForMemberValuePair((MemberValuePair) aNode, aDepth, includeParent);
		}

		// compilation unit
		else if (aNode instanceof CompilationUnit) {
			result = getMappingForCompilationUnit((CompilationUnit) aNode, aDepth, includeParent);
		}

		else if (aNode instanceof Name) {
			result = getMappingForName((Name) aNode, aDepth, includeParent);
		} else if (aNode instanceof SimpleName) {
			result = getMappingForSimpleName((SimpleName) aNode, aDepth, includeParent);
		} else if (aNode instanceof ArrayCreationLevel) {
			result = getMappingForArrayCreationLevel((ArrayCreationLevel) aNode, aDepth, includeParent);
		} else if (aNode instanceof ModuleDeclaration) {
			result = getMappingForModuleDeclaration((ModuleDeclaration) aNode, aDepth, includeParent);
		} else if (aNode instanceof ModuleStmt) {
			// all module statements
			if (aNode instanceof ModuleExportsStmt) {
				result = getMappingForModuleExportsStmt((ModuleExportsStmt) aNode, aDepth, includeParent);
			} else if (aNode instanceof ModuleUsesStmt) {
				result = getMappingForModuleUsesStmt((ModuleUsesStmt) aNode, aDepth, includeParent);
			} else if (aNode instanceof ModuleProvidesStmt) {
				result = getMappingForModuleProvidesStmt((ModuleProvidesStmt) aNode, aDepth, includeParent);
			} else if (aNode instanceof ModuleRequiresStmt) {
				result = getMappingForModuleRequiresStmt((ModuleRequiresStmt) aNode, aDepth, includeParent);
			} else if (aNode instanceof ModuleOpensStmt) {
				result = getMappingForModuleOpensStmt((ModuleOpensStmt) aNode, aDepth, includeParent);
			}
		}

		// this should be removed after testing i guess
		// >> I wouldn't remove it, since it doesn't hurt and constitutes a
		// value <<
		else {
			result = getMappingForUnknownNode(aNode, aDepth, includeParent);
		}
		
		return finalizeMapping(result, aNode, aDepth, includeParent);
	}

	default public T finalizeMapping(T mapping, Node aNode, int aDepth, boolean includeParent) {
		return mapping;
	}

	default public T getMappingForTypeDeclaration(TypeDeclaration<?> aNode, int aDepth, boolean includeParent) {
		// old: just to avoid some null pointer exceptions when a null object is
		// legit
		// update: should catch null in calling methods instead (since we use
		// generics)
		if (aNode == null) {
			return null;
		}

		// all type declarations (may all have annotations)
		if (aNode instanceof AnnotationDeclaration) {
			return getMappingForAnnotationDeclaration((AnnotationDeclaration) aNode, aDepth, includeParent);
		} else if (aNode instanceof ClassOrInterfaceDeclaration) {
			return getMappingForClassOrInterfaceDeclaration((ClassOrInterfaceDeclaration) aNode, aDepth, includeParent);
		} else if (aNode instanceof EnumDeclaration) {
			return getMappingForEnumDeclaration((EnumDeclaration) aNode, aDepth, includeParent);
		}

		return getMappingForUnknownNode(aNode, aDepth, includeParent);
	}

	default public T getMappingForBodyDeclaration(BodyDeclaration<?> aNode, int aDepth, boolean includeParent) {
		// old: just to avoid some null pointer exceptions when a null object is
		// legit
		// update: should catch null in calling methods instead (since we use
		// generics)
		if (aNode == null) {
			return null;
		}

		// all declarations (may all have annotations)
		if (aNode instanceof InitializerDeclaration) {
			return getMappingForInitializerDeclaration((InitializerDeclaration) aNode, aDepth, includeParent);
		} else if (aNode instanceof FieldDeclaration) {
			return getMappingForFieldDeclaration((FieldDeclaration) aNode, aDepth, includeParent);
		} else if (aNode instanceof EnumConstantDeclaration) {
			return getMappingForEnumConstantDeclaration((EnumConstantDeclaration) aNode, aDepth, includeParent);
		} else if (aNode instanceof AnnotationMemberDeclaration) {
			return getMappingForAnnotationMemberDeclaration((AnnotationMemberDeclaration) aNode, aDepth, includeParent);
		} else if (aNode instanceof TypeDeclaration) {
			return getMappingForTypeDeclaration((TypeDeclaration<?>) aNode, aDepth, includeParent);
		} else if (aNode instanceof CallableDeclaration) {
			if (aNode instanceof ConstructorDeclaration) {
				return getMappingForConstructorDeclaration((ConstructorDeclaration) aNode, aDepth, includeParent);
			} else if (aNode instanceof MethodDeclaration) {
				return getMappingForMethodDeclaration((MethodDeclaration) aNode, aDepth, includeParent);
			}
		}

		return getMappingForUnknownNode(aNode, aDepth, includeParent);
	}

	default public T getMappingForStatement(Statement aNode, int aDepth, boolean includeParent) {
		// old: just to avoid some null pointer exceptions when a null object is
		// legit
		// update: should catch null in calling methods instead (since we use
		// generics)
		if (aNode == null) {
			return null;
		}

		// all statements
		if (aNode instanceof AssertStmt) {
			return getMappingForAssertStmt((AssertStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof BlockStmt) {
			return getMappingForBlockStmt((BlockStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof BreakStmt) {
			return getMappingForBreakStmt((BreakStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof ContinueStmt) {
			return getMappingForContinueStmt((ContinueStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof DoStmt) {
			return getMappingForDoStmt((DoStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof ExplicitConstructorInvocationStmt) {
			return getMappingForExplicitConstructorInvocationStmt((ExplicitConstructorInvocationStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof ExpressionStmt) {
			return getMappingForExpressionStmt((ExpressionStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof ForeachStmt) {
			return getMappingForForeachStmt((ForeachStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof ForStmt) {
			return getMappingForForStmt((ForStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof IfStmt) {
			return getMappingForIfStmt((IfStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof LabeledStmt) {
			return getMappingForLabeledStmt((LabeledStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof ReturnStmt) {
			return getMappingForReturnStmt((ReturnStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof SwitchEntryStmt) {
			return getMappingForSwitchEntryStmt((SwitchEntryStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof SwitchStmt) {
			return getMappingForSwitchStmt((SwitchStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof SynchronizedStmt) {
			return getMappingForSynchronizedStmt((SynchronizedStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof ThrowStmt) {
			return getMappingForThrowStmt((ThrowStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof TryStmt) {
			return getMappingForTryStmt((TryStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof WhileStmt) {
			return getMappingForWhileStmt((WhileStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof LocalClassDeclarationStmt) {
			return getMappingForLocalClassDeclarationStmt((LocalClassDeclarationStmt) aNode, aDepth, includeParent);
		} else if (aNode instanceof EmptyStmt) {
			return getMappingForEmptyStmt((EmptyStmt) aNode, includeParent);
		}

		return getMappingForUnknownNode(aNode, aDepth, includeParent);
	}

	default public T getMappingForType(Type aNode, int aDepth, boolean includeParent) {
		// old: just to avoid some null pointer exceptions when a null object is
		// legit
		// update: should catch null in calling methods instead (since we use
		// generics)
		if (aNode == null) {
			return null;
		}

		// all types
		if (aNode instanceof IntersectionType) {
			return getMappingForIntersectionType((IntersectionType) aNode, aDepth, includeParent);
		} else if (aNode instanceof PrimitiveType) {
			return getMappingForPrimitiveType((PrimitiveType) aNode, aDepth, includeParent);
		} else if (aNode instanceof ReferenceType) {
			if (aNode instanceof ClassOrInterfaceType) {
				return getMappingForClassOrInterfaceType((ClassOrInterfaceType) aNode, aDepth, includeParent);
			} else if (aNode instanceof TypeParameter) {
				return getMappingForTypeParameter((TypeParameter) aNode, aDepth, includeParent);
			} else if (aNode instanceof ArrayType) {
				return getMappingForArrayType((ArrayType) aNode, aDepth, includeParent);
			}
		} else if (aNode instanceof UnionType) {
			return getMappingForUnionType((UnionType) aNode, aDepth, includeParent);
		} else if (aNode instanceof UnknownType) {
			return getMappingForUnknownType((UnknownType) aNode, includeParent);
		} else if (aNode instanceof VoidType) {
			return getMappingForVoidType((VoidType) aNode, includeParent);
		} else if (aNode instanceof WildcardType) {
			return getMappingForWildcardType((WildcardType) aNode, aDepth, includeParent);
		}

		return getMappingForUnknownNode(aNode, aDepth, includeParent);
	}

	default public T getMappingForExpression(Expression aNode, int aDepth, boolean includeParent) {
		// old: just to avoid some null pointer exceptions when a null object is
		// legit
		// update: should catch null in calling methods instead (since we use
		// generics)
		if (aNode == null) {
			return null;
		}

		// all expressions
		if (aNode instanceof LiteralExpr) {
			if (aNode instanceof NullLiteralExpr) {
				return getMappingForNullLiteralExpr((NullLiteralExpr) aNode, includeParent);
			} else if (aNode instanceof BooleanLiteralExpr) {
				return getMappingForBooleanLiteralExpr((BooleanLiteralExpr) aNode, includeParent);
			} else if (aNode instanceof LiteralStringValueExpr) {
				if (aNode instanceof StringLiteralExpr) {
					return getMappingForStringLiteralExpr((StringLiteralExpr) aNode, includeParent);
				} else if (aNode instanceof CharLiteralExpr) {
					return getMappingForCharLiteralExpr((CharLiteralExpr) aNode, includeParent);
				} else if (aNode instanceof IntegerLiteralExpr) {
					return getMappingForIntegerLiteralExpr((IntegerLiteralExpr) aNode, includeParent);
				} else if (aNode instanceof LongLiteralExpr) {
					return getMappingForLongLiteralExpr((LongLiteralExpr) aNode, includeParent);
				} else if (aNode instanceof DoubleLiteralExpr) {
					return getMappingForDoubleLiteralExpr((DoubleLiteralExpr) aNode, includeParent);
				}
			}
		} else if (aNode instanceof ArrayAccessExpr) {
			return getMappingForArrayAccessExpr((ArrayAccessExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof ArrayCreationExpr) {
			return getMappingForArrayCreationExpr((ArrayCreationExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof ArrayInitializerExpr) {
			return getMappingForArrayInitializerExpr((ArrayInitializerExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof AssignExpr) {
			return getMappingForAssignExpr((AssignExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof BinaryExpr) {
			return getMappingForBinaryExpr((BinaryExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof CastExpr) {
			return getMappingForCastExpr((CastExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof ClassExpr) {
			return getMappingForClassExpr((ClassExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof ConditionalExpr) {
			return getMappingForConditionalExpr((ConditionalExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof FieldAccessExpr) {
			return getMappingForFieldAccessExpr((FieldAccessExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof InstanceOfExpr) {
			return getMappingForInstanceOfExpr((InstanceOfExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof LambdaExpr) {
			return getMappingForLambdaExpr((LambdaExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof MethodCallExpr) {
			return getMappingForMethodCallExpr((MethodCallExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof MethodReferenceExpr) {
			return getMappingForMethodReferenceExpr((MethodReferenceExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof ThisExpr) {
			return getMappingForThisExpr((ThisExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof EnclosedExpr) {
			return getMappingForEnclosedExpr((EnclosedExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof ObjectCreationExpr) {
			return getMappingForObjectCreationExpr((ObjectCreationExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof UnaryExpr) {
			return getMappingForUnaryExpr((UnaryExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof SuperExpr) {
			return getMappingForSuperExpr((SuperExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof TypeExpr) {
			return getMappingForTypeExpr((TypeExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof VariableDeclarationExpr) {
			return getMappingForVariableDeclarationExpr((VariableDeclarationExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof NameExpr) {
			return getMappingForNameExpr((NameExpr) aNode, aDepth, includeParent);
		} else if (aNode instanceof AnnotationExpr) {
			if (aNode instanceof MarkerAnnotationExpr) {
				return getMappingForMarkerAnnotationExpr((MarkerAnnotationExpr) aNode, aDepth, includeParent);
			} else if (aNode instanceof NormalAnnotationExpr) {
				return getMappingForNormalAnnotationExpr((NormalAnnotationExpr) aNode, aDepth, includeParent);
			} else if (aNode instanceof SingleMemberAnnotationExpr) {
				return getMappingForSingleMemberAnnotationExpr((SingleMemberAnnotationExpr) aNode, aDepth, includeParent);
			}
		}

		return getMappingForUnknownNode(aNode, aDepth, includeParent);
	}

	public T getMappingForUnknownNode(Node aNode, int aDepth, boolean includeParent);

	public T getMappingForCompilationUnit(CompilationUnit aNode, int aDepth, boolean includeParent);

	public T getMappingForMemberValuePair(MemberValuePair aNode, int aDepth, boolean includeParent);

	public T getMappingForVariableDeclarator(VariableDeclarator aNode, int aDepth, boolean includeParent);

	public T getMappingForCatchClause(CatchClause aNode, int aDepth, boolean includeParent);

	public T getMappingForTypeParameter(TypeParameter aNode, int aDepth, boolean includeParent);

	public T getMappingForImportDeclaration(ImportDeclaration aNode, int aDepth, boolean includeParent);

	public T getMappingForPackageDeclaration(PackageDeclaration aNode, int aDepth, boolean includeParent);

	public T getMappingForParameter(Parameter aNode, int aDepth, boolean includeParent);

	public T getMappingForJavadocComment(JavadocComment aNode, int aDepth, boolean includeParent);

	public T getMappingForBlockComment(BlockComment aNode, int aDepth, boolean includeParent);

	public T getMappingForLineComment(LineComment aNode, int aDepth, boolean includeParent);

	public T getMappingForEnumDeclaration(EnumDeclaration aNode, int aDepth, boolean includeParent);

	public T getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, int aDepth, boolean includeParent);

	public T getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, int aDepth, boolean includeParent);

	public T getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, int aDepth, boolean includeParent);

	public T getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, int aDepth, boolean includeParent);

	public T getMappingForMethodDeclaration(MethodDeclaration aNode, int aDepth, boolean includeParent);

	public T getMappingForFieldDeclaration(FieldDeclaration aNode, int aDepth, boolean includeParent);

	public T getMappingForInitializerDeclaration(InitializerDeclaration aNode, int aDepth, boolean includeParent);

	public T getMappingForConstructorDeclaration(ConstructorDeclaration aNode, int aDepth, boolean includeParent);

	public T getMappingForWhileStmt(WhileStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForTryStmt(TryStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForThrowStmt(ThrowStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForSynchronizedStmt(SynchronizedStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForSwitchStmt(SwitchStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForReturnStmt(ReturnStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForLabeledStmt(LabeledStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForIfStmt(IfStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForForStmt(ForStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForForeachStmt(ForeachStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForExpressionStmt(ExpressionStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForDoStmt(DoStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForContinueStmt(ContinueStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForBreakStmt(BreakStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForBlockStmt(BlockStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForAssertStmt(AssertStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForWildcardType(WildcardType aNode, int aDepth, boolean includeParent);

	public T getMappingForVoidType(VoidType aNode, boolean includeParent);

	public T getMappingForUnknownType(UnknownType aNode, boolean includeParent);

	public T getMappingForUnionType(UnionType aNode, int aDepth, boolean includeParent);

	public T getMappingForPrimitiveType(PrimitiveType aNode, int aDepth, boolean includeParent);

	public T getMappingForIntersectionType(IntersectionType aNode, int aDepth, boolean includeParent);

	public T getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, int aDepth, boolean includeParent);

	public T getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForNameExpr(NameExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForTypeExpr(TypeExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForSuperExpr(SuperExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForUnaryExpr(UnaryExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForObjectCreationExpr(ObjectCreationExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForEnclosedExpr(EnclosedExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForThisExpr(ThisExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForMethodCallExpr(MethodCallExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForLambdaExpr(LambdaExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForInstanceOfExpr(InstanceOfExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForFieldAccessExpr(FieldAccessExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForConditionalExpr(ConditionalExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForClassExpr(ClassExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForCastExpr(CastExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForBinaryExpr(BinaryExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForAssignExpr(AssignExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForArrayCreationExpr(ArrayCreationExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForArrayAccessExpr(ArrayAccessExpr aNode, int aDepth, boolean includeParent);

	public T getMappingForStringLiteralExpr(StringLiteralExpr aNode, boolean includeParent);

	public T getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, boolean includeParent);

	public T getMappingForLongLiteralExpr(LongLiteralExpr aNode, boolean includeParent);

	public T getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, boolean includeParent);

	public T getMappingForCharLiteralExpr(CharLiteralExpr aNode, boolean includeParent);

	public T getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, boolean includeParent);

	public T getMappingForNullLiteralExpr(NullLiteralExpr aNode, boolean includeParent);

	public T getMappingForName(Name aNode, int aDepth, boolean includeParent);

	public T getMappingForSimpleName(SimpleName aNode, int aDepth, boolean includeParent);

	public T getMappingForLocalClassDeclarationStmt(LocalClassDeclarationStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForArrayType(ArrayType aNode, int aDepth, boolean includeParent);

	public T getMappingForArrayCreationLevel(ArrayCreationLevel aNode, int aDepth, boolean includeParent);

	public T getMappingForModuleDeclaration(ModuleDeclaration aNode, int aDepth, boolean includeParent);

	public T getMappingForModuleExportsStmt(ModuleExportsStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForModuleOpensStmt(ModuleOpensStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForModuleProvidesStmt(ModuleProvidesStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForModuleRequiresStmt(ModuleRequiresStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForModuleUsesStmt(ModuleUsesStmt aNode, int aDepth, boolean includeParent);

	public T getMappingForEmptyStmt(EmptyStmt aNode, boolean includeParent);

}
