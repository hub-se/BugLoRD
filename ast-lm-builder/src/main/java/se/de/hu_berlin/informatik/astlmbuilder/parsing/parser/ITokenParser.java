package se.de.hu_berlin.informatik.astlmbuilder.parsing.parser;

import java.util.List;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
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
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider.KeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.nodes.UnknownNode;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;

public interface ITokenParser extends ITokenParserBasics {

	default public <T extends Node> InformationWrapper updateGeneralInfo(Class<T> lastSeenNodeClass,
			InformationWrapper info, boolean useCopy) {
		if (useCopy) {
			info = info.getCopy();
		}
		info.addNodeClassToHistory(lastSeenNodeClass);

		return info;
	}

	// TODO: (maybe there exists a more elegant way?)
	// Attention: Parsing of Modifiers, types, booleans and operators is already
	// implemented in the respective Handler-interfaces!

	// expected token format: id, or id[member_1]...[member_n]), or ~ for
	// null

	public default ConstructorDeclaration parseConstructorDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.CONSTRUCTOR_DECLARATION, 7);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessConstructorDeclaration(info);
		} else {
			return getCreator().createConstructorDeclaration(memberData, info);
		}
	}

	public default InitializerDeclaration parseInitializerDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.INITIALIZER_DECLARATION, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessInitializerDeclaration(info);
		} else {
			return getCreator().createInitializerDeclaration(memberData, info);
		}
	}

	public default EnumConstantDeclaration parseEnumConstantDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.ENUM_CONSTANT_DECLARATION, 4);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessEnumConstantDeclaration(info);
		} else {
			return getCreator().createEnumConstantDeclaration(memberData, info);
		}
	}

    public default VariableDeclarator parseVariableDeclarator(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.VARIABLE_DECLARATOR, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessVariableDeclarator(info);
		} else {
			return getCreator().createVariableDeclarator(memberData, info);
		}
	}

	public default EnumDeclaration parseEnumDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.ENUM_DECLARATION, 6);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessEnumDeclaration(info);
		} else {
			return getCreator().createEnumDeclaration(memberData, info);
		}
	}

	public default AnnotationDeclaration parseAnnotationDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.ANNOTATION_DECLARATION, 4);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessAnnotationDeclaration(info);
		} else {
			return getCreator().createAnnotationDeclaration(memberData, info);
		}
	}

	public default AnnotationMemberDeclaration parseAnnotationMemberDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.ANNOTATION_MEMBER_DECLARATION, 5);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessAnnotationMemberDeclaration(info);
		} else {
			return getCreator().createAnnotationMemberDeclaration(memberData, info);
		}
	}

	public default WhileStmt parseWhileStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.WHILE_STMT, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessWhileStmt(info);
		} else {
			return getCreator().createWhileStmt(memberData, info);
		}
	}

	public default TryStmt parseTryStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.TRY_STMT, 4);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessTryStmt(info);
		} else {
			return getCreator().createTryStmt(memberData, info);
		}
	}

	public default ThrowStmt parseThrowStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.THROW_STMT, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessThrowStmt(info);
		} else {
			return getCreator().createThrowStmt(memberData, info);
		}
	}

	public default SynchronizedStmt parseSynchronizedStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.SYNCHRONIZED_STMT, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessSynchronizedStmt(info);
		} else {
			return getCreator().createSynchronizedStmt(memberData, info);
		}
	}

	public default SwitchStmt parseSwitchStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.SWITCH_STMT, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessSwitchStmt(info);
		} else {
			return getCreator().createSwitchStmt(memberData, info);
		}
	}

	public default SwitchEntryStmt parseSwitchEntryStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.SWITCH_ENTRY_STMT, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessSwitchEntryStmt(info);
		} else {
			return getCreator().createSwitchEntryStmt(memberData, info);
		}
	}

	public default ReturnStmt parseReturnStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.RETURN_STMT, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessReturnStmt(info);
		} else {
			return getCreator().createReturnStmt(memberData, info);
		}
	}

	public default LabeledStmt parseLabeledStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.LABELED_STMT, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessLabeledStmt(info);
		} else {
			return getCreator().createLabeledStmt(memberData, info);
		}
	}

	public default IfStmt parseIfStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.IF_STMT, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessIfStmt(info);
		} else {
			return getCreator().createIfStmt(memberData, info);
		}
	}

	public default ForStmt parseForStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.FOR_STMT, 4);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessForStmt(info);
		} else {
			return getCreator().createForStmt(memberData, info);
		}
	}

	public default ForeachStmt parseForeachStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.FOR_EACH_STMT, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessForeachStmt(info);
		} else {
			return getCreator().createForeachStmt(memberData, info);
		}
	}

	public default ExpressionStmt parseExpressionStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.EXPRESSION_STMT, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessExpressionStmt(info);
		} else {
			return getCreator().createExpressionStmt(memberData, info);
		}
	}

	public default ExplicitConstructorInvocationStmt parseExplicitConstructorInvocationStmt(String token,
			InformationWrapper info) throws IllegalArgumentException {
		info = updateGeneralInfo(ExplicitConstructorInvocationStmt.class, info, false);
		List<String> memberData = parseAndCheckMembers(
				token, KeyWords.EXPL_CONSTR_INVOC_STMT, 4);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessExplicitConstructorInvocationStmt(info);
		} else {
			return getCreator().createExplicitConstructorInvocationStmt(memberData, info);
		}
	}

	public default DoStmt parseDoStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.DO_STMT, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessDoStmt(info);
		} else {
			return getCreator().createDoStmt(memberData, info);
		}
	}

	public default ContinueStmt parseContinueStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.CONTINUE_STMT, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessContinueStmt(info);
		} else {
			return getCreator().createContinueStmt(memberData, info);
		}
	}

	public default CatchClause parseCatchClause(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.CATCH_CLAUSE_STMT, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessCatchClause(info);
		} else {
			return getCreator().createCatchClause(memberData, info);
		}
	}

	public default BlockStmt parseBlockStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.BLOCK_STMT, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessBlockStmt(info);
		} else {
			return getCreator().createBlockStmt(memberData, info);
		}
	}

	public default VariableDeclarationExpr parseVariableDeclarationExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(
				token, KeyWords.VARIABLE_DECLARATION_EXPRESSION, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessVariableDeclarationExpr(info);
		} else {
			return getCreator().createVariableDeclarationExpr(memberData, info);
		}
	}

	public default TypeExpr parseTypeExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.TYPE_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessTypeExpr(info);
		} else {
			return getCreator().createTypeExpr(memberData, info);
		}
	}

	public default SuperExpr parseSuperExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.SUPER_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessSuperExpr(info);
		} else {
			return getCreator().createSuperExpr(memberData, info);
		}
	}

	public default NullLiteralExpr parseNullLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.NULL_LITERAL_EXPRESSION, 0);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessNullLiteralExpr(info);
		} else {
			return getCreator().createNullLiteralExpr(memberData, info);
		}
	}

	public default MethodReferenceExpr parseMethodReferenceExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.METHOD_REFERENCE_EXPRESSION, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessMethodReferenceExpr(info);
		} else {
			return getCreator().createMethodReferenceExpr(memberData, info);
		}
	}

	public default LambdaExpr parseLambdaExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.LAMBDA_EXPRESSION, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessLambdaExpr(info);
		} else {
			return getCreator().createLambdaExpr(memberData, info);
		}
	}

	public default InstanceOfExpr parseInstanceOfExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.INSTANCEOF_EXPRESSION, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessInstanceOfExpr(info);
		} else {
			return getCreator().createInstanceOfExpr(memberData, info);
		}
	}

	public default FieldAccessExpr parseFieldAccessExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.FIELD_ACCESS_EXPRESSION, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessFieldAccessExpr(info);
		} else {
			return getCreator().createFieldAccessExpr(memberData, info);
		}
	}

	public default ConditionalExpr parseConditionalExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.CONDITIONAL_EXPRESSION, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessConditionalExpr(info);
		} else {
			return getCreator().createConditionalExpr(memberData, info);
		}
	}

	public default ClassExpr parseClassExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.CLASS_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessClassExpr(info);
		} else {
			return getCreator().createClassExpr(memberData, info);
		}
	}

	public default CastExpr parseCastExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.CAST_EXPRESSION, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessCastExpr(info);
		} else {
			return getCreator().createCastExpr(memberData, info);
		}
	}

	public default AssignExpr parseAssignExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.ASSIGN_EXPRESSION, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessAssignExpr(info);
		} else {
			return getCreator().createAssignExpr(memberData, info);
		}
	}

	public default ArrayInitializerExpr parseArrayInitializerExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.ARRAY_INIT_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessArrayInitializerExpr(info);
		} else {
			return getCreator().createArrayInitializerExpr(memberData, info);
		}
	}

	public default ArrayCreationExpr parseArrayCreationExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.ARRAY_CREATE_EXPRESSION, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessArrayCreationExpr(info);
		} else {
			return getCreator().createArrayCreationExpr(memberData, info);
		}
	}

	public default ArrayAccessExpr parseArrayAccessExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.ARRAY_ACCESS_EXPRESSION, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessArrayAccessExpr(info);
		} else {
			return getCreator().createArrayAccessExpr(memberData, info);
		}
	}

	public default PackageDeclaration parsePackageDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.PACKAGE_DECLARATION, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessPackageDeclaration(info);
		} else {
			return getCreator().createPackageDeclaration(memberData, info);
		}
	}

	public default ImportDeclaration parseImportDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.IMPORT_DECLARATION, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessImportDeclaration(info);
		} else {
			return getCreator().createImportDeclaration(memberData, info);
		}
	}

	public default FieldDeclaration parseFieldDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.FIELD_DECLARATION, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessFieldDeclaration(info);
		} else {
			return getCreator().createFieldDeclaration(memberData, info);
		}
	}

	public default ClassOrInterfaceType parseClassOrInterfaceType(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.CLASS_OR_INTERFACE_TYPE, 4);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessClassOrInterfaceType(info);
		} else {
			return getCreator().createClassOrInterfaceType(memberData, info);
		}
	}

	public default ClassOrInterfaceDeclaration parseClassOrInterfaceDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.CLASS_OR_INTERFACE_DECLARATION, 8);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessClassOrInterfaceDeclaration(info);
		} else {
			return getCreator().createClassOrInterfaceDeclaration(memberData, info);
		}
	}

	public default MethodDeclaration parseMethodDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.METHOD_DECLARATION, 8);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessMethodDeclaration(info);
		} else {
			return getCreator().createMethodDeclaration(memberData, info);
		}
	}

	public default BinaryExpr parseBinaryExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.BINARY_EXPRESSION, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessBinaryExpr(info);
		} else {
			return getCreator().createBinaryExpr(memberData, info);
		}
	}

	public default UnaryExpr parseUnaryExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.UNARY_EXPRESSION, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessUnaryExpr(info);
		} else {
			return getCreator().createUnaryExpr(memberData, info);
		}
	}

	public default MethodCallExpr parseMethodCallExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.METHOD_CALL_EXPRESSION, 4);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessMethodCallExpr(info);
		} else {
			return getCreator().createMethodCallExpr(memberData, info);
		}
	}

	public default NameExpr parseNameExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.NAME_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessNameExpr(info);
		} else {
			return getCreator().createNameExpr(memberData, info);
		}
	}

	public default IntegerLiteralExpr parseIntegerLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.INTEGER_LITERAL_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessIntegerLiteralExpr(info);
		} else {
			return getCreator().createIntegerLiteralExpr(memberData, info);
		}
	}

	public default DoubleLiteralExpr parseDoubleLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.DOUBLE_LITERAL_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessDoubleLiteralExpr(info);
		} else {
			return getCreator().createDoubleLiteralExpr(memberData, info);
		}
	}

	public default StringLiteralExpr parseStringLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.STRING_LITERAL_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessStringLiteralExpr(info);
		} else {
			return getCreator().createStringLiteralExpr(memberData, info);
		}
	}

	public default BooleanLiteralExpr parseBooleanLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.BOOLEAN_LITERAL_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessBooleanLiteralExpr(info);
		} else {
			return getCreator().createBooleanLiteralExpr(memberData, info);
		}
	}

	public default CharLiteralExpr parseCharLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.CHAR_LITERAL_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessCharLiteralExpr(info);
		} else {
			return getCreator().createCharLiteralExpr(memberData, info);
		}
	}

	public default LongLiteralExpr parseLongLiteralExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.LONG_LITERAL_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessLongLiteralExpr(info);
		} else {
			return getCreator().createLongLiteralExpr(memberData, info);
		}
	}

	public default ThisExpr parseThisExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.THIS_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessThisExpr(info);
		} else {
			return getCreator().createThisExpr(memberData, info);
		}
	}

	public default BreakStmt parseBreakStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.BREAK, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessBreakStmt(info);
		} else {
			return getCreator().createBreakStmt(memberData, info);
		}
	}

	public default ObjectCreationExpr parseObjectCreationExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.OBJ_CREATE_EXPRESSION, 5);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessObjectCreationExpr(info);
		} else {
			return getCreator().createObjectCreationExpr(memberData, info);
		}
	}

	public default MarkerAnnotationExpr parseMarkerAnnotationExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.MARKER_ANNOTATION_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessMarkerAnnotationExpr(info);
		} else {
			return getCreator().createMarkerAnnotationExpr(memberData, info);
		}
	}

	public default NormalAnnotationExpr parseNormalAnnotationExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.NORMAL_ANNOTATION_EXPRESSION, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessNormalAnnotationExpr(info);
		} else {
			return getCreator().createNormalAnnotationExpr(memberData, info);
		}
	}

	public default SingleMemberAnnotationExpr parseSingleMemberAnnotationExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		info = updateGeneralInfo(SingleMemberAnnotationExpr.class, info, false);
		List<String> memberData = parseAndCheckMembers(
				token, KeyWords.SINGLE_MEMBER_ANNOTATION_EXPRESSION, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessSingleMemberAnnotationExpr(info);
		} else {
			return getCreator().createSingleMemberAnnotationExpr(memberData, info);
		}
	}

	public default Parameter parseParameter(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.PARAMETER, 6);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessParameter(info);
		} else {
			return getCreator().createParameter(memberData, info);
		}
	}

	public default EnclosedExpr parseEnclosedExpr(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.ENCLOSED_EXPRESSION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessEnclosedExpr(info);
		} else {
			return getCreator().createEnclosedExpr(memberData, info);
		}
	}

	public default AssertStmt parseAssertStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.ASSERT_STMT, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessAssertStmt(info);
		} else {
			return getCreator().createAssertStmt(memberData, info);
		}
	}

	public default MemberValuePair parseMemberValuePair(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.MEMBER_VALUE_PAIR, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessMemberValuePair(info);
		} else {
			return getCreator().createMemberValuePair(memberData, info);
		}
	}

	public default PrimitiveType parsePrimitiveType(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.TYPE_PRIMITIVE, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessPrimitiveType(info);
		} else {
			return getCreator().createPrimitiveType(memberData, info);
		}
	}

	public default UnionType parseUnionType(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.TYPE_UNION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessUnionType(info);
		} else {
			return getCreator().createUnionType(memberData, info);
		}
	}

	public default IntersectionType parseIntersectionType(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.TYPE_INTERSECTION, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessIntersectionType(info);
		} else {
			return getCreator().createIntersectionType(memberData, info);
		}
	}

	public default TypeParameter parseTypeParameter(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.TYPE_PAR, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessTypeParameter(info);
		} else {
			return getCreator().createTypeParameter(memberData, info);
		}
	}

	public default WildcardType parseWildcardType(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.TYPE_WILDCARD, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessWildcardType(info);
		} else {
			return getCreator().createWildcardType(memberData, info);
		}
	}

	public default VoidType parseVoidType(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.TYPE_VOID, 0);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessVoidType(info);
		} else {
			return getCreator().createVoidType(memberData, info);
		}
	}

	public default UnknownType parseUnknownType(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.TYPE_UNKNOWN, 0);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessUnknownType(info);
		} else {
			return getCreator().createUnknownType(memberData, info);
		}
	}

	public default Name parseName(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.NAME, 3);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessName(info);
		} else {
			return getCreator().createName(memberData, info);
		}
	}

	public default SimpleName parseSimpleName(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.SIMPLE_NAME, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessSimpleName(info);
		} else {
			return getCreator().createSimpleName(memberData, info);
		}
	}

	public default LocalClassDeclarationStmt parseLocalClassDeclarationStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.LOCAL_CLASS_DECLARATION_STMT, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessLocalClassDeclarationStmt(info);
		} else {
			return getCreator().createLocalClassDeclarationStmt(memberData, info);
		}
	}

	public default ArrayType parseArrayType(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.ARRAY_TYPE, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessArrayType(info);
		} else {
			return getCreator().createArrayType(memberData, info);
		}
	}

	public default ArrayCreationLevel parseArrayCreationLevel(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.ARRAY_CREATION_LEVEL, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessArrayCreationLevel(info);
		} else {
			return getCreator().createArrayCreationLevel(memberData, info);
		}
	}

	public default ModuleDeclaration parseModuleDeclaration(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.MODULE_DECLARATION, 4);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessModuleDeclaration(info);
		} else {
			return getCreator().createModuleDeclaration(memberData, info);
		}
	}

	public default ModuleExportsStmt parseModuleExportsStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.MODULE_EXPORTS_STMT, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessModuleExportsStmt(info);
		} else {
			return getCreator().createModuleExportsStmt(memberData, info);
		}
	}

	public default ModuleOpensStmt parseModuleOpensStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.MODULE_OPENS_STMT, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessModuleOpensStmt(info);
		} else {
			return getCreator().createModuleOpensStmt(memberData, info);
		}
	}

	public default ModuleProvidesStmt parseModuleProvidesStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.MODULE_PROVIDES_STMT, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessModuleProvidesStmt(info);
		} else {
			return getCreator().createModuleProvidesStmt(memberData, info);
		}
	}

	public default ModuleRequiresStmt parseModuleRequiresStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.MODULE_REQUIRES_STMT, 2);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessModuleRequiresStmt(info);
		} else {
			return getCreator().createModuleRequiresStmt(memberData, info);
		}
	}

	public default ModuleUsesStmt parseModuleUsesStmt(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.MODULE_USES_STMT, 1);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessModuleUsesStmt(info);
		} else {
			return getCreator().createModuleUsesStmt(memberData, info);
		}
	}

	public default CompilationUnit parseCompilationUnit(String token, InformationWrapper info)
			throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.COMPILATION_UNIT, 4);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessCompilationUnit(info);
		} else {
			return getCreator().createCompilationUnit(memberData, info);
		}
	}
	
	public default EmptyStmt parseEmptyStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, KeyWords.EMPTY_STMT, 0);
		if (memberData == null) {
			return null;
		} else if (memberData == KEYWORD_DUMMY) { // token: id
			return getGuesser().guessEmptyStmt(info);
		} else {
			return getCreator().createEmptyStmt(memberData, info);
		}
	}

	public default UnknownNode parseUnknownNode(String token, InformationWrapper info) throws IllegalArgumentException {
		throw new IllegalArgumentException("Argument was an unknown node!");
	}

}
