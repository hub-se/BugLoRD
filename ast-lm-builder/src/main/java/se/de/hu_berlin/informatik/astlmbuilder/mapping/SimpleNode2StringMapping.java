package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.Collection;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;

import se.de.hu_berlin.informatik.astlmbuilder.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ImplementsStmt;

public class SimpleNode2StringMapping implements ITokenMapper<String> {
	
	// a collection of blacklisted private method names
	// the simple mapper makes no use of this
	public Collection<String> privMethodBL = null;


	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.ITokenMapper#getClosingToken(com.github.javaparser.ast.Node)
	 */
	@Override
	public String getClosingToken(Node aNode) {
		if (aNode == null) {
			return null;
		}

		if (aNode instanceof MethodDeclaration) {
			return CLOSING_MDEC;
		} else if (aNode instanceof ConstructorDeclaration) {
			return CLOSING_CNSTR;
		} else if (aNode instanceof IfStmt) {
			return CLOSING_IF;
		} else if (aNode instanceof WhileStmt) {
			return CLOSING_WHILE;
		} else if (aNode instanceof ForStmt) {
			return CLOSING_FOR;
		} else if (aNode instanceof TryStmt) {
			return CLOSING_TRY;
		} else if (aNode instanceof CatchClause) {
			return CLOSING_CATCH;
		} else if (aNode instanceof ForeachStmt) {
			return CLOSING_FOR_EACH;
		} else if (aNode instanceof DoStmt) {
			return CLOSING_DO;
		} else if (aNode instanceof SwitchStmt) {
			return SWITCH_DO;
		}

		return null;
	}

	@Override
	public void setPrivMethodBlackList(Collection<String> aBL) {
		privMethodBL = aBL;
	}

	@Override
	public void clearPrivMethodBlackList() {
		privMethodBL = null;
	}


	@Override
	public MappingWrapper<String> getMappingForExtendsStmt(ExtendsStmt aNode) {
		return new MappingWrapper<>(EXTENDS_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForImplementsStmt(ImplementsStmt aNode) {
		return new MappingWrapper<>(IMPLEMENTS_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForUnknownNode(Node aNode) {
		return new MappingWrapper<>(UNKNOWN + "(" + aNode.getClass().getSimpleName() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForCompilationUnit(CompilationUnit aNode) {
		return new MappingWrapper<>(COMPILATION_UNIT);
	}

	@Override
	public MappingWrapper<String> getMappingForMemberValuePair(MemberValuePair aNode) {
		return new MappingWrapper<>(MEMBER_VALUE_PAIR);
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclaratorId(VariableDeclaratorId aNode) {
		return new MappingWrapper<>(VARIABLE_DECLARATION_ID);
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarator(VariableDeclarator aNode) {
		return new MappingWrapper<>(VARIABLE_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForCatchClause(CatchClause aNode) {
		return new MappingWrapper<>(CATCH_CLAUSE_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForTypeParameter(TypeParameter aNode) {
		return new MappingWrapper<>(TYPE_PAR);
	}

	@Override
	public MappingWrapper<String> getMappingForImportDeclaration(ImportDeclaration aNode) {
		return new MappingWrapper<>(IMPORT_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForPackageDeclaration(PackageDeclaration aNode) {
		return new MappingWrapper<>(PACKAGE_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForMultiTypeParameter(MultiTypeParameter aNode) {
		return new MappingWrapper<>(MULTI_TYPE_PARAMETER);
	}

	@Override
	public MappingWrapper<String> getMappingForParameter(Parameter aNode) {
		return new MappingWrapper<>(PARAMETER);
	}

	@Override
	public MappingWrapper<String> getMappingForJavadocComment(JavadocComment aNode) {
		return new MappingWrapper<>(JAVADOC_COMMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForBlockComment(BlockComment aNode) {
		return new MappingWrapper<>(BLOCK_COMMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForLineComment(LineComment aNode) {
		return new MappingWrapper<>(LINE_COMMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForEnumDeclaration(EnumDeclaration aNode) {
		return new MappingWrapper<>(ENUM_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForEmptyTypeDeclaration(EmptyTypeDeclaration aNode) {
		return new MappingWrapper<>(EMPTY_TYPE_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode) {
		return new MappingWrapper<>(CLASS_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForAnnotationDeclaration(AnnotationDeclaration aNode) {
		return new MappingWrapper<>(ANNOTATION_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForEmptyMemberDeclaration(EmptyMemberDeclaration aNode) {
		return new MappingWrapper<>(EMPTY_MEMBER_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode) {
		return new MappingWrapper<>(ANNOTATION_MEMBER_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode) {
		return new MappingWrapper<>(ENUM_CONSTANT_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodDeclaration(MethodDeclaration aNode) {
		return new MappingWrapper<>(METHOD_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForFieldDeclaration(FieldDeclaration aNode) {
		return new MappingWrapper<>(FIELD_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForInitializerDeclaration(InitializerDeclaration aNode) {
		return new MappingWrapper<>(INITIALIZER_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForConstructorDeclaration(ConstructorDeclaration aNode) {
		return new MappingWrapper<>(CONSTRUCTOR_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForWhileStmt(WhileStmt aNode) {
		return new MappingWrapper<>(WHILE_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForTypeDeclarationStmt(TypeDeclarationStmt aNode) {
		return new MappingWrapper<>(TYPE_DECLARATION_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForTryStmt(TryStmt aNode) {
		return new MappingWrapper<>(TRY_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForThrowStmt(ThrowStmt aNode) {
		return new MappingWrapper<>(THROW_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForSynchronizedStmt(SynchronizedStmt aNode) {
		return new MappingWrapper<>(SYNCHRONIZED_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchStmt(SwitchStmt aNode) {
		return new MappingWrapper<>(SWITCH_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchEntryStmt(SwitchEntryStmt aNode) {
		return new MappingWrapper<>(SWITCH_ENTRY_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForReturnStmt(ReturnStmt aNode) {
		return new MappingWrapper<>(RETURN_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForLabeledStmt(LabeledStmt aNode) {
		return new MappingWrapper<>(LABELED_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForElseStmt(ElseStmt aNode) {
		return new MappingWrapper<>(ELSE_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForIfStmt(IfStmt aNode) {
		return new MappingWrapper<>(IF_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForForStmt(ForStmt aNode) {
		return new MappingWrapper<>(FOR_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForForeachStmt(ForeachStmt aNode) {
		return new MappingWrapper<>(FOR_EACH_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForExpressionStmt(ExpressionStmt aNode) {
		return new MappingWrapper<>(EXPRESSION_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForExplicitConstructorInvocationStmt(
			ExplicitConstructorInvocationStmt aNode) {
		return new MappingWrapper<>(EXPLICIT_CONSTRUCTOR_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForEmptyStmt(EmptyStmt aNode) {
		return new MappingWrapper<>(EMPTY_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForDoStmt(DoStmt aNode) {
		return new MappingWrapper<>(DO_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForContinueStmt(ContinueStmt aNode) {
		return new MappingWrapper<>(CONTINUE_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForBreakStmt(BreakStmt aNode) {
		return new MappingWrapper<>(BREAK);
	}

	@Override
	public MappingWrapper<String> getMappingForBlockStmt(BlockStmt aNode) {
		return new MappingWrapper<>(BLOCK_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForAssertStmt(AssertStmt aNode) {
		return new MappingWrapper<>(ASSERT_STMT);
	}

	@Override
	public MappingWrapper<String> getMappingForWildcardType(WildcardType aNode) {
		return new MappingWrapper<>(TYPE_WILDCARD);
	}

	@Override
	public MappingWrapper<String> getMappingForVoidType(VoidType aNode) {
		return new MappingWrapper<>(TYPE_VOID);
	}

	@Override
	public MappingWrapper<String> getMappingForUnknownType(UnknownType aNode) {
		return new MappingWrapper<>(TYPE_UNKNOWN);
	}

	@Override
	public MappingWrapper<String> getMappingForUnionType(UnionType aNode) {
		return new MappingWrapper<>(TYPE_UNION);
	}

	@Override
	public MappingWrapper<String> getMappingForReferenceType(ReferenceType aNode) {
		return new MappingWrapper<>(TYPE_REFERENCE);
	}

	@Override
	public MappingWrapper<String> getMappingForPrimitiveType(PrimitiveType aNode) {
		return new MappingWrapper<>(TYPE_PRIMITIVE);
	}

	@Override
	public MappingWrapper<String> getMappingForIntersectionType(IntersectionType aNode) {
		return new MappingWrapper<>(TYPE_INTERSECTION);
	}

	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode) {
		return new MappingWrapper<>(CLASS_TYPE);
	}

	@Override
	public MappingWrapper<String> getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode) {
		return new MappingWrapper<>(SINGLE_MEMBER_ANNOTATION_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode) {
		return new MappingWrapper<>(NORMAL_ANNOTATION_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode) {
		return new MappingWrapper<>(MARKER_ANNOTATION_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForNameExpr(NameExpr aNode) {
		return new MappingWrapper<>(NAME_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForQualifiedNameExpr(QualifiedNameExpr aNode) {
		return new MappingWrapper<>(QUALIFIED_NAME_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode) {
		return new MappingWrapper<>(VARIABLE_DECLARATION_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForTypeExpr(TypeExpr aNode) {
		return new MappingWrapper<>(TYPE_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForSuperExpr(SuperExpr aNode) {
		return new MappingWrapper<>(SUPER_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForUnaryExpr(UnaryExpr aNode) {
		return new MappingWrapper<>(UNARY_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForObjectCreationExpr(ObjectCreationExpr aNode) {
		return new MappingWrapper<>(OBJ_CREATE_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForEnclosedExpr(EnclosedExpr aNode) {
		return new MappingWrapper<>(ENCLOSED_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForThisExpr(ThisExpr aNode) {
		return new MappingWrapper<>(THIS_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodReferenceExpr(MethodReferenceExpr aNode) {
		return new MappingWrapper<>(METHOD_REFERENCE_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodCallExpr(MethodCallExpr aNode) {
		return new MappingWrapper<>(METHOD_CALL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForLambdaExpr(LambdaExpr aNode) {
		return new MappingWrapper<>(LAMBDA_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForInstanceOfExpr(InstanceOfExpr aNode) {
		return new MappingWrapper<>(INSTANCEOF_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForFieldAccessExpr(FieldAccessExpr aNode) {
		return new MappingWrapper<>(FIELD_ACCESS_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForConditionalExpr(ConditionalExpr aNode) {
		return new MappingWrapper<>(CONDITIONAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForClassExpr(ClassExpr aNode) {
		return new MappingWrapper<>(CLASS_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForCastExpr(CastExpr aNode) {
		return new MappingWrapper<>(CAST_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForBinaryExpr(BinaryExpr aNode) {
		return new MappingWrapper<>(BINARY_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForAssignExpr(AssignExpr aNode) {
		return new MappingWrapper<>(ASSIGN_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode) {
		return new MappingWrapper<>(ARRAY_INIT_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForArrayCreationExpr(ArrayCreationExpr aNode) {
		return new MappingWrapper<>(ARRAY_CREATE_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForArrayAccessExpr(ArrayAccessExpr aNode) {
		return new MappingWrapper<>(ARRAY_ACCESS_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForStringLiteralExpr(StringLiteralExpr aNode) {
		return new MappingWrapper<>(STRING_LITERAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode) {
		return new MappingWrapper<>(DOUBLE_LITERAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralExpr(LongLiteralExpr aNode) {
		return new MappingWrapper<>(LONG_LITERAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralMinValueExpr(LongLiteralMinValueExpr aNode) {
		return new MappingWrapper<>(LONG_LITERAL_MIN_VALUE_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode) {
		return new MappingWrapper<>(INTEGER_LITERAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralMinValueExpr(IntegerLiteralMinValueExpr aNode) {
		return new MappingWrapper<>(INTEGER_LITERAL_MIN_VALUE_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForCharLiteralExpr(CharLiteralExpr aNode) {
		return new MappingWrapper<>(CHAR_LITERAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode) {
		return new MappingWrapper<>(BOOLEAN_LITERAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForNullLiteralExpr(NullLiteralExpr aNode) {
		return new MappingWrapper<>(NULL_LITERAL_EXPRESSION);
	}
}
