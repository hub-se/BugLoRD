package se.de.hu_berlin.informatik.astlmbuilder.mapping.shortKW;

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
import se.de.hu_berlin.informatik.astlmbuilder.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ThrowsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITokenMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.MappingWrapper;

public class SimpleNode2StringMappingShort<V> extends KeyWordConstantsShort implements ITokenMapper<String,V> {
	
	// a collection of blacklisted private method names
	// the simple mapper makes no use of this
	public Collection<String> privMethodBL = null;


	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.ITokenMapper#getClosingToken(com.github.javaparser.ast.Node)
	 */
	@Override
	public String getClosingToken(Node aNode, @SuppressWarnings("unchecked") V... values) {
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
			return CLOSING_SWITCH;
		} else if (aNode instanceof EnclosedExpr) {
			return CLOSING_ENCLOSED;
		} else if (aNode instanceof BlockStmt) {
			return CLOSING_BLOCK_STMT;
		} else if (aNode instanceof ExpressionStmt) {
			return CLOSING_EXPRESSION_STMT;
		} else if (aNode instanceof CompilationUnit) {
			return CLOSING_COMPILATION_UNIT;
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
	public MappingWrapper<String> getMappingForExtendsStmt(ExtendsStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(EXTENDS_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForImplementsStmt(ImplementsStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(IMPLEMENTS_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForUnknownNode(Node aNode, @SuppressWarnings("unchecked") V... values) {
		if (aNode != null) {
			return new MappingWrapper<>(UNKNOWN + GROUP_START + aNode.getClass() + GROUP_END);
		} else {
			return new MappingWrapper<>(UNKNOWN);
		}
	}

	@Override
	public MappingWrapper<String> getMappingForCompilationUnit(CompilationUnit aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(COMPILATION_UNIT);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodBodyStmt(BodyStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(BODY_STMT);
	}
	
	@Override
	public MappingWrapper<String> getMappingForThrowsStmt(ThrowsStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(THROWS_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForMemberValuePair(MemberValuePair aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(MEMBER_VALUE_PAIR);
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclaratorId(VariableDeclaratorId aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(VARIABLE_DECLARATION_ID);
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarator(VariableDeclarator aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(VARIABLE_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForCatchClause(CatchClause aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(CATCH_CLAUSE_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForTypeParameter(TypeParameter aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(TYPE_PAR);
	}

	@Override
	public MappingWrapper<String> getMappingForImportDeclaration(ImportDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(IMPORT_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForPackageDeclaration(PackageDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(PACKAGE_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForMultiTypeParameter(MultiTypeParameter aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(MULTI_TYPE_PARAMETER);
	}

	@Override
	public MappingWrapper<String> getMappingForParameter(Parameter aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(PARAMETER);
	}

	@Override
	public MappingWrapper<String> getMappingForJavadocComment(JavadocComment aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(JAVADOC_COMMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForBlockComment(BlockComment aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(BLOCK_COMMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForLineComment(LineComment aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(LINE_COMMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForEnumDeclaration(EnumDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(ENUM_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForEmptyTypeDeclaration(EmptyTypeDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(EMPTY_TYPE_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(CLASS_OR_INTERFACE_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(ANNOTATION_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForEmptyMemberDeclaration(EmptyMemberDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(EMPTY_MEMBER_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(ANNOTATION_MEMBER_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(ENUM_CONSTANT_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodDeclaration(MethodDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(METHOD_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForFieldDeclaration(FieldDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(FIELD_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForInitializerDeclaration(InitializerDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(INITIALIZER_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForConstructorDeclaration(ConstructorDeclaration aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(CONSTRUCTOR_DECLARATION);
	}

	@Override
	public MappingWrapper<String> getMappingForWhileStmt(WhileStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(WHILE_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForTypeDeclarationStmt(TypeDeclarationStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(TYPE_DECLARATION_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForTryStmt(TryStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(TRY_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForThrowStmt(ThrowStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(THROW_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForSynchronizedStmt(SynchronizedStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(SYNCHRONIZED_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchStmt(SwitchStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(SWITCH_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(SWITCH_ENTRY_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForReturnStmt(ReturnStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(RETURN_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForLabeledStmt(LabeledStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(LABELED_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForElseStmt(ElseStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(ELSE_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForIfStmt(IfStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(IF_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForForStmt(ForStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(FOR_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForForeachStmt(ForeachStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(FOR_EACH_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForExpressionStmt(ExpressionStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(EXPRESSION_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForExplicitConstructorInvocationStmt(
			ExplicitConstructorInvocationStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(EXPLICIT_CONSTRUCTOR_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForEmptyStmt(EmptyStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(EMPTY_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForDoStmt(DoStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(DO_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForContinueStmt(ContinueStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(CONTINUE_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForBreakStmt(BreakStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(BREAK);
	}

	@Override
	public MappingWrapper<String> getMappingForBlockStmt(BlockStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(BLOCK_STATEMENT);
	}

	@Override
	public MappingWrapper<String> getMappingForAssertStmt(AssertStmt aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(ASSERT_STMT);
	}

	@Override
	public MappingWrapper<String> getMappingForWildcardType(WildcardType aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(TYPE_WILDCARD);
	}

	@Override
	public MappingWrapper<String> getMappingForVoidType(VoidType aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(TYPE_VOID);
	}

	@Override
	public MappingWrapper<String> getMappingForUnknownType(UnknownType aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(TYPE_UNKNOWN);
	}

	@Override
	public MappingWrapper<String> getMappingForUnionType(UnionType aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(TYPE_UNION);
	}

	@Override
	public MappingWrapper<String> getMappingForReferenceType(ReferenceType aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(TYPE_REFERENCE);
	}

	@Override
	public MappingWrapper<String> getMappingForPrimitiveType(PrimitiveType aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(TYPE_PRIMITIVE);
	}

	@Override
	public MappingWrapper<String> getMappingForIntersectionType(IntersectionType aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(TYPE_INTERSECTION);
	}

	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(CLASS_OR_INTERFACE_TYPE);
	}

	@Override
	public MappingWrapper<String> getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(SINGLE_MEMBER_ANNOTATION_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(NORMAL_ANNOTATION_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(MARKER_ANNOTATION_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForNameExpr(NameExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(NAME_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForQualifiedNameExpr(QualifiedNameExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(QUALIFIED_NAME_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(VARIABLE_DECLARATION_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForTypeExpr(TypeExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(TYPE_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForSuperExpr(SuperExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(SUPER_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForUnaryExpr(UnaryExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(UNARY_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForObjectCreationExpr(ObjectCreationExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(OBJ_CREATE_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForEnclosedExpr(EnclosedExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(ENCLOSED_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForThisExpr(ThisExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(THIS_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(METHOD_REFERENCE_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodCallExpr(MethodCallExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(METHOD_CALL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForLambdaExpr(LambdaExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(LAMBDA_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForInstanceOfExpr(InstanceOfExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(INSTANCEOF_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForFieldAccessExpr(FieldAccessExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(FIELD_ACCESS_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForConditionalExpr(ConditionalExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(CONDITIONAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForClassExpr(ClassExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(CLASS_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForCastExpr(CastExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(CAST_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForBinaryExpr(BinaryExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(BINARY_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForAssignExpr(AssignExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(ASSIGN_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(ARRAY_INIT_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForArrayCreationExpr(ArrayCreationExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(ARRAY_CREATE_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForArrayAccessExpr(ArrayAccessExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(ARRAY_ACCESS_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForStringLiteralExpr(StringLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(STRING_LITERAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(DOUBLE_LITERAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralExpr(LongLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(LONG_LITERAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralMinValueExpr(LongLiteralMinValueExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(LONG_LITERAL_MIN_VALUE_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(INTEGER_LITERAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralMinValueExpr(IntegerLiteralMinValueExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(INTEGER_LITERAL_MIN_VALUE_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForCharLiteralExpr(CharLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(CHAR_LITERAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(BOOLEAN_LITERAL_EXPRESSION);
	}

	@Override
	public MappingWrapper<String> getMappingForNullLiteralExpr(NullLiteralExpr aNode, @SuppressWarnings("unchecked") V... values) {
		return new MappingWrapper<>(NULL_LITERAL_EXPRESSION);
	}
	
}
