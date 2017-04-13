package se.de.hu_berlin.informatik.astlmbuilder.parsing.parser;

import java.util.List;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
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

import se.de.hu_berlin.informatik.astlmbuilder.nodes.UnknownNode;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;

public interface ITokenParser extends ITokenParserBasics {
	
	//TODO: (maybe there exists a more elegant way?)
	//Attention: Parsing of Modifiers, types, booleans and operators is already implemented in the respective Handler-interfaces!

	//expected token format: id, or (id,[member_1],...,[member_n]), or ~ for null

	default public ConstructorDeclaration createConstructorDeclaration(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getConstructorDeclaration(), 7); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ConstructorDeclaration.class, token/* == keyWord*/, info.getCopy());
		}

		//EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// NodeList<TypeParameter> typeParameters 
		// SimpleName name
		// NodeList<Parameter> parameters
		// NodeList<ReferenceType> thrownExceptions
		// BlockStmt body
		return new ConstructorDeclaration(
				parseModifiersFromToken(memberData.get(0)), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()), 
				parseListFromToken(TypeParameter.class, memberData.get(2), info.getCopy()), 
				createSimpleName(memberData.get(3), info.getCopy()), 
				parseListFromToken(Parameter.class, memberData.get(4), info.getCopy()), 
				parseListFromToken(ReferenceType.class, memberData.get(5), info.getCopy()), 
				createBlockStmt(memberData.get(6), info.getCopy()));
	}

	public default InitializerDeclaration createInitializerDeclaration(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getInitializerDeclaration(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(InitializerDeclaration.class, token, info.getCopy());
		}

		// boolean isStatic
		// BlockStmt body
		return new InitializerDeclaration(
				parseBooleanFromToken(memberData.get(0)), 
				createBlockStmt(memberData.get(1), info.getCopy()));
	}

	public default EnumConstantDeclaration createEnumConstantDeclaration(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getEnumConstantDeclaration(), 4); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(EnumConstantDeclaration.class, token, info.getCopy());
		}

		// NodeList<AnnotationExpr> annotations,
		// SimpleName name
		// NodeList<Expression> arguments
		// NodeList<BodyDeclaration<?>> classBody
		return new EnumConstantDeclaration(
				parseListFromToken( AnnotationExpr.class, memberData.get(0), info.getCopy()),
				createSimpleName(memberData.get(1), info.getCopy()),
				parseListFromToken( Expression.class, memberData.get(2), info.getCopy()),
				parseBodyDeclarationListFromToken( memberData.get(3), info.getCopy()));
	};

	public default VariableDeclarator createVariableDeclarator(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getVariableDeclaration(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(VariableDeclarator.class, token, info.getCopy());
		}

		// Type type
		// SimpleName name
		// Expression initializer
		return new VariableDeclarator(
				createNodeFromToken( Type.class, memberData.get(0), info.getCopy() ),
				createSimpleName(memberData.get(1), info.getCopy()), 
				createNodeFromToken( Expression.class, memberData.get(2), info.getCopy()));
	}

	public default EnumDeclaration createEnumDeclaration(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getEnumDeclaration(), 6); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(EnumDeclaration.class, token, info.getCopy());
		}

		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// SimpleName name
		// NodeList<ClassOrInterfaceType> implementedTypes
		// NodeList<EnumConstantDeclaration> entries
		// NodeList<BodyDeclaration<?>> members
		return new EnumDeclaration(
				parseModifiersFromToken(memberData.get(0)), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()), 
				createSimpleName(memberData.get(2), info.getCopy()), 
				parseListFromToken(ClassOrInterfaceType.class, memberData.get(3), info.getCopy()), 
				parseListFromToken(EnumConstantDeclaration.class, memberData.get(4), info.getCopy()), 
				parseBodyDeclarationListFromToken( memberData.get(5), info.getCopy()));
	}

	public default AnnotationDeclaration createAnnotationDeclaration(String token, InformationWrapper info)throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getAnnotationDeclaration(), 4); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(AnnotationDeclaration.class, token, info.getCopy());
		}

		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// SimpleName name
		// NodeList<BodyDeclaration<?>> members
		return new AnnotationDeclaration(
				parseModifiersFromToken(memberData.get(0)), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()),
				createSimpleName(memberData.get(2), info.getCopy()), 
				parseBodyDeclarationListFromToken( memberData.get(3), info.getCopy()));
	}

	public default AnnotationMemberDeclaration createAnnotationMemberDeclaration(String token, InformationWrapper info)throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getAnnotationMemberDeclaration(), 5); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(AnnotationMemberDeclaration.class, token, info.getCopy());
		}

		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// Type type
		// SimpleName name
		// Expression defaultValue
		return new AnnotationMemberDeclaration(
				parseModifiersFromToken(memberData.get(0)), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()),
				createNodeFromToken( Type.class, memberData.get(2), info.getCopy() ), 
				createSimpleName(memberData.get(3), info.getCopy()),
				createNodeFromToken( Expression.class,  memberData.get(4), info.getCopy() ));
	}

	public default WhileStmt createWhileStmt(String token, InformationWrapper info)throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getWhileStatement(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(WhileStmt.class, token, info.getCopy());
		}

		// final Expression condition
		// final Statement body
		return new WhileStmt(
				createNodeFromToken( Expression.class, memberData.get(0), info.getCopy() ), 
				createBlockStmt(memberData.get(1), info.getCopy()));
	}

	public default TryStmt createTryStmt(String token, InformationWrapper info)throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTryStatement(), 4); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(TryStmt.class, token, info.getCopy());
		}

		// NodeList<VariableDeclarationExpr> resources
		// final BlockStmt tryBlock
		// final NodeList<CatchClause> catchClauses
		// final BlockStmt finallyBlock
		return new TryStmt(
				parseListFromToken(VariableDeclarationExpr.class, memberData.get(0), info.getCopy()), 
				createBlockStmt(memberData.get(1), info.getCopy()),
				parseListFromToken(CatchClause.class, memberData.get(2), info.getCopy()), 
				createBlockStmt(memberData.get(3), info.getCopy()));
	}

	public default ThrowStmt createThrowStmt(String token, InformationWrapper info)throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getThrowStatement(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ThrowStmt.class, token, info.getCopy());
		}

		// final Expression expression
		return new ThrowStmt(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy() ));
	}

	public default SynchronizedStmt createSynchronizedStmt(String token, InformationWrapper info)throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getSynchronizedStatement(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(SynchronizedStmt.class, token, info.getCopy());
		}

		// final Expression expression
		// final BlockStmt body
		return new SynchronizedStmt(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy() ),
				createBlockStmt(memberData.get(1), info.getCopy()));
	}

	public default SwitchStmt createSwitchStmt(String token, InformationWrapper info)throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getSwitchStatement(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(SwitchStmt.class, token, info.getCopy());
		}

		// final Expression selector
		// final NodeList<SwitchEntryStmt> entries
		return new SwitchStmt(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy() ), 
				parseListFromToken(SwitchEntryStmt.class, memberData.get(1), info.getCopy()));
	}

	public default SwitchEntryStmt createSwitchEntryStmt(String token, InformationWrapper info)throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getSwitchEntryStatement(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(SwitchEntryStmt.class, token, info.getCopy());
		}

		// final Expression label
		// final NodeList<Statement> statements
		return new SwitchEntryStmt(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy() ), 
				parseListFromToken(Statement.class, memberData.get(1), info.getCopy()));
	}

	public default ReturnStmt createReturnStmt(String token, InformationWrapper info)throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getReturnStatement(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ReturnStmt.class, token, info.getCopy());
		}

		// final Expression expression
		return new ReturnStmt(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy()));
	}

	public default LabeledStmt createLabeledStmt(String token, InformationWrapper info)throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getLabeledStatement(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(LabeledStmt.class, token, info.getCopy());
		}

		// final String label
		// final Statement statement
		return new LabeledStmt(
				parseStringValueFromToken( memberData.get(0) ), 
				createNodeFromToken( Statement.class, memberData.get(1), info.getCopy()));
	}

	public default IfStmt createIfStmt(String token, InformationWrapper info)throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getIfStatement(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(IfStmt.class, token, info.getCopy());
		}

		// final Expression condition
		// final Statement thenStmt
		// final Statement elseStmt
		return new IfStmt(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy()),
				createNodeFromToken( Statement.class, memberData.get(1), info.getCopy()),
				createNodeFromToken( Statement.class, memberData.get(2), info.getCopy()));
	}


	public default ForStmt createForStmt(String token, InformationWrapper info)throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getForStatement(), 4); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ForStmt.class, token, info.getCopy());
		}

		// final NodeList<Expression> initialization
		// final Expression compare
		// final NodeList<Expression> update
		// final Statement body
		return new ForStmt(
				parseListFromToken(Expression.class, memberData.get(0), info.getCopy()),
				createNodeFromToken( Expression.class,  memberData.get(1), info.getCopy()),
				parseListFromToken(Expression.class, memberData.get(2), info.getCopy()),
				createNodeFromToken( Statement.class, memberData.get(3), info.getCopy()));
	}

	public default ForeachStmt createForeachStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getForEachStatement(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ForeachStmt.class, token, info.getCopy());
		}

		// final VariableDeclarationExpr variable
		// final Expression iterable
		// final Statement body
		return new ForeachStmt(
				createVariableDeclarationExpr(memberData.get(0), info.getCopy()), 
				createNodeFromToken( Expression.class,  memberData.get(1), info.getCopy()), 
				createNodeFromToken( Statement.class, memberData.get(2), info.getCopy()));
	}

	public default ExpressionStmt createExpressionStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getExpressionStatement(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ExpressionStmt.class, token, info.getCopy());
		}

		// final Expression expression
		return new ExpressionStmt(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy()));
	}

	public default ExplicitConstructorInvocationStmt createExplicitConstructorInvocationStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getExplicitConstructorStatement(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ExplicitConstructorInvocationStmt.class, token, info.getCopy());
		}

		// final boolean isThis
		// final Expression expression
		// final NodeList<Expression> arguments
		return new ExplicitConstructorInvocationStmt(
				parseBooleanFromToken(memberData.get(0)), 
				createNodeFromToken( Expression.class,  memberData.get(1), info.getCopy()),
				parseListFromToken(Expression.class, memberData.get(2), info.getCopy()));
	}

	public default DoStmt createDoStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getDoStatement(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(DoStmt.class, token, info.getCopy());
		}

		// final Statement body
		// final Expression condition
		return new DoStmt(
				createNodeFromToken( Statement.class, memberData.get(0), info.getCopy()),
				createNodeFromToken( Expression.class,  memberData.get(1), info.getCopy()));
	}

	public default ContinueStmt createContinueStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getContinueStatement(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ContinueStmt.class, token, info.getCopy());
		}

		// final SimpleName label
		return new ContinueStmt(
				createSimpleName(memberData.get(0), info.getCopy()));
	}

	public default CatchClause createCatchClause(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getCatchClauseStatement(), 5); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(CatchClause.class, token, info.getCopy());
		}

		// final EnumSet<Modifier> exceptModifier
		// final NodeList<AnnotationExpr> exceptAnnotations
		// final ClassOrInterfaceType exceptType
		// final SimpleName exceptName
		// final BlockStmt body
		return new CatchClause(
				parseModifiersFromToken(memberData.get(0)), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()), 
				createClassOrInterfaceType( memberData.get(2), info.getCopy()), 
				createSimpleName(memberData.get(3), info.getCopy()), 
				createBlockStmt(memberData.get(4), info.getCopy()));
	}

	public default BlockStmt createBlockStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getBlockStatement(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(BlockStmt.class, token, info.getCopy());
		}

		// final NodeList<Statement> statements
		return new BlockStmt( 
				parseListFromToken(Statement.class, memberData.get(0), info.getCopy()));
	}

	public default VariableDeclarationExpr createVariableDeclarationExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getVariableDeclarationExpression(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(VariableDeclarationExpr.class, token, info.getCopy());
		}

		// final EnumSet<Modifier> modifiers
		// final NodeList<AnnotationExpr> annotations
		// final NodeList<VariableDeclarator> variables
		return new VariableDeclarationExpr(
				parseModifiersFromToken(memberData.get(0)), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()), 
				parseListFromToken(VariableDeclarator.class, memberData.get(2), info.getCopy()));
	}

	public default TypeExpr createTypeExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypeExpression(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(TypeExpr.class, token, info.getCopy());
		}

		// Type type
		return new TypeExpr(
				createNodeFromToken( Type.class,memberData.get(0), info.getCopy() ));
	}

	public default SuperExpr createSuperExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getSuperExpression(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(SuperExpr.class, token, info.getCopy());
		}

		// final Expression classExpr
		return new SuperExpr(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy() ));
	}

	public default NullLiteralExpr createNullLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getNullLiteralExpression(), 0); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(NullLiteralExpr.class, token, info.getCopy());
		}

		// funny :D
		return new NullLiteralExpr();
	}

	public default MethodReferenceExpr createMethodReferenceExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getMethodReferenceExpression(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(MethodReferenceExpr.class, token, info.getCopy());
		}

		// Expression scope
		// NodeList<Type> typeArguments
		// String identifier
		return new MethodReferenceExpr(
				createNodeFromToken( Expression.class, memberData.get(0), info.getCopy()), 
				parseListFromToken(Type.class, memberData.get(1), info.getCopy()),
				parseMethodIdentifierFromToken( memberData.get(2), info.getCopy()));
	}

	public default LambdaExpr createLambdaExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getLambdaExpression(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(LambdaExpr.class, token, info.getCopy());
		}

		// NodeList<Parameter> parameters
		// Statement body
		// boolean isEnclosingParameters
		return new LambdaExpr(
				parseListFromToken(Parameter.class, memberData.get(0), info.getCopy()), 
				createNodeFromToken( Statement.class, memberData.get( 1 ), info.getCopy() ),
				parseBooleanFromToken(memberData.get(2)));
	}

	public default InstanceOfExpr createInstanceOfExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getInstanceofExpression(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(InstanceOfExpr.class, token, info.getCopy());
		}

		// final Expression expression
		// final ReferenceType<?> type
		return new InstanceOfExpr(
				createNodeFromToken( Expression.class,  memberData.get( 0 ), info.getCopy() ), 
				createNodeFromToken( ReferenceType.class, memberData.get(1), info.getCopy()));
	}

	public default FieldAccessExpr createFieldAccessExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getFieldAccessExpression(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(FieldAccessExpr.class, token, info.getCopy());
		}

		// final Expression scope
		// final NodeList<Type> typeArguments
		// final SimpleName name
		return new FieldAccessExpr(
				createNodeFromToken( Expression.class, memberData.get(0), info.getCopy()), 
				parseListFromToken(Type.class, memberData.get(1), info.getCopy()), 
				createSimpleName(memberData.get(2), info.getCopy()));
	}

	public default ConditionalExpr createConditionalExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getConditionalExpression(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ConditionalExpr.class, token, info.getCopy());
		}

		// Expression condition
		// Expression thenExpr
		// Expression elseExpr
		return new ConditionalExpr(
				createNodeFromToken( Expression.class, memberData.get(0), info.getCopy()), 
				createNodeFromToken( Expression.class, memberData.get(1), info.getCopy()), 
				createNodeFromToken( Expression.class, memberData.get(2), info.getCopy()));
	}

	public default ClassExpr createClassExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getClassExpression(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ClassExpr.class, token, info.getCopy());
		}

		// Type type
		return new ClassExpr(
				createNodeFromToken( Type.class,memberData.get(0), info.getCopy()));
	}

	public default CastExpr createCastExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getCastExpression(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(CastExpr.class, token, info.getCopy());
		}

		// Type type
		// Expression expression
		return new CastExpr(
				createNodeFromToken( Type.class,memberData.get(0), info.getCopy()),
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy()));
	}

	public default AssignExpr createAssignExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getAssignExpression(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(AssignExpr.class, token, info.getCopy());
		}

		// Expression target
		// Expression value
		// Operator operator
		return new AssignExpr(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy()),
				createNodeFromToken( Expression.class,  memberData.get(1), info.getCopy()),
				parseAssignOperatorFromToken( memberData.get(2)));
	}

	public default ArrayInitializerExpr createArrayInitializerExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getArrayInitExpression(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ArrayInitializerExpr.class, token, info.getCopy());
		}

		// NodeList<Expression> values
		return new ArrayInitializerExpr(
				parseListFromToken(Expression.class, memberData.get(0), info.getCopy()));
	}

	public default ArrayCreationExpr createArrayCreationExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getArrayCreateExpression(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ArrayCreationExpr.class, token, info.getCopy());
		}

		// Type elementType
		// NodeList<ArrayCreationLevel> levels
		// ArrayInitializerExpr initializer
		return new ArrayCreationExpr(
				createNodeFromToken( Type.class, memberData.get(0), info.getCopy()), 
				parseListFromToken(ArrayCreationLevel.class, memberData.get(1), info.getCopy()), 
				createArrayInitializerExpr( memberData.get(2), info.getCopy()));
	}

	public default ArrayAccessExpr createArrayAccessExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getArrayAccessExpression(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ArrayAccessExpr.class, token, info.getCopy());
		}

		// Expression name
		// Expression index
		return new ArrayAccessExpr(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy()),
				createNodeFromToken( Expression.class,  memberData.get(1), info.getCopy()));
	}

	public default PackageDeclaration createPackageDeclaration(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getPackageDeclaration(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(PackageDeclaration.class, token, info.getCopy());
		}

		// NodeList<AnnotationExpr> annotations
		// Name name
		return new PackageDeclaration(
				parseListFromToken(AnnotationExpr.class, memberData.get(0), info.getCopy()), 
				createName(memberData.get(1), info.getCopy()));
	}

	// this may never be used
	public default ImportDeclaration createImportDeclaration(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getImportDeclaration(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ImportDeclaration.class, token, info.getCopy());
		}

		// Name name
		// boolean isStatic
		// boolean isAsterisk
		return new ImportDeclaration(
				createName(memberData.get(0), info.getCopy()),
				parseBooleanFromToken(memberData.get(1)),
				parseBooleanFromToken(memberData.get(2)));
	}

	public default FieldDeclaration createFieldDeclaration(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getFieldDeclaration(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(FieldDeclaration.class, token, info.getCopy());
		}

		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// NodeList<VariableDeclarator> variables
		return new FieldDeclaration(
				parseModifiersFromToken(memberData.get(0)), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()), 
				parseListFromToken(VariableDeclarator.class, memberData.get(2), info.getCopy()));
	}

	public default ClassOrInterfaceType createClassOrInterfaceType(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getClassOrInterfaceType(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ClassOrInterfaceType.class, token, info.getCopy());
		}

		// final ClassOrInterfaceType scope
		// final SimpleName name
		// final NodeList<Type> typeArguments
		return new ClassOrInterfaceType(
				createClassOrInterfaceType(memberData.get(0), info.getCopy()),  
				createSimpleName(memberData.get(1), info.getCopy()), 
				parseListFromToken(Type.class, memberData.get(2), info.getCopy()));
	}

	public default ClassOrInterfaceDeclaration createClassOrInterfaceDeclaration(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getClassOrInterfaceDeclaration(), 8); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ClassOrInterfaceDeclaration.class, token, info.getCopy());
		}

		// final EnumSet<Modifier> modifiers
		// final NodeList<AnnotationExpr> annotations
		// final boolean isInterface
		// final SimpleName name
		// final NodeList<TypeParameter> typeParameters
		// final NodeList<ClassOrInterfaceType> extendedTypes
		// final NodeList<ClassOrInterfaceType> implementedTypes
		// final NodeList<BodyDeclaration<?>> members
		return new ClassOrInterfaceDeclaration(
				parseModifiersFromToken(memberData.get(0)), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()), 
				parseBooleanFromToken(memberData.get(2)), 
				createSimpleName(memberData.get(3), info.getCopy()), 
				parseListFromToken(TypeParameter.class, memberData.get(4), info.getCopy()), 
				parseListFromToken(ClassOrInterfaceType.class, memberData.get(5), info.getCopy()),
				parseListFromToken(ClassOrInterfaceType.class, memberData.get(6), info.getCopy()),
				parseBodyDeclarationListFromToken(memberData.get(7), info.getCopy()));
	}

	public default MethodDeclaration createMethodDeclaration(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getMethodDeclaration(), 9); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(MethodDeclaration.class, token, info.getCopy());
		}

		// final EnumSet<Modifier> modifiers
		// final NodeList<AnnotationExpr> annotations
		// final NodeList<TypeParameter> typeParameters
		// final Type type
		// final SimpleName name
		// final boolean isDefault
		// final NodeList<Parameter> parameters
		// final NodeList<ReferenceType> thrownExceptions
		// final BlockStmt body
		return new MethodDeclaration(
				parseModifiersFromToken(memberData.get(0)), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()), 
				parseListFromToken(TypeParameter.class, memberData.get(2), info.getCopy()),
				createNodeFromToken( Type.class, memberData.get(3), info.getCopy()),
				createSimpleName(memberData.get(4), info.getCopy()),
				parseBooleanFromToken( memberData.get( 5 )),
				parseListFromToken(Parameter.class, memberData.get(6), info.getCopy()), 
				parseListFromToken(ReferenceType.class, memberData.get(7), info.getCopy()), 
				createBlockStmt(memberData.get(8), info.getCopy()));
	}

	public default BinaryExpr createBinaryExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getBinaryExpression(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(BinaryExpr.class, token, info.getCopy());
		}

		// Expression left
		// Expression right
		// Operator operator
		return new BinaryExpr(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy()),
				createNodeFromToken( Expression.class,  memberData.get(1), info.getCopy()),
				parseBinaryOperatorFromToken( memberData.get(2)));
	}

	public default UnaryExpr createUnaryExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getUnaryExpression(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(UnaryExpr.class, token, info.getCopy());
		}

		// final Expression expression
		// final Operator operator
		return new UnaryExpr(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy()),
				parseUnaryOperatorFromToken( memberData.get(1)));
	}

	public default MethodCallExpr createMethodCallExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getMethodCallExpression(), 4); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(MethodCallExpr.class, token, info.getCopy());
		}

		// final Expression scope
		// final NodeList<Type> typeArguments
		// final SimpleName name
		// final NodeList<Expression> arguments
		return new MethodCallExpr(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy() ), 
				parseListFromToken(Type.class, memberData.get(1), info.getCopy()), 
				createSimpleName(memberData.get(2), info.getCopy()), 
				parseListFromToken(Expression.class, memberData.get(3), info.getCopy()));
	}

	public default NameExpr createNameExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getNameExpression(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(NameExpr.class, token, info.getCopy());
		}

		// final SimpleName name
		return new NameExpr(
				createSimpleName(memberData.get(2), info.getCopy()));
	}

	public default ConstructorDeclaration createIntegerLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getConstructorDeclaration(), 7); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ConstructorDeclaration.class, token, info.getCopy());
		}

		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// NodeList<TypeParameter> typeParameters
		// SimpleName name
		// NodeList<Parameter> parameters
		// NodeList<ReferenceType> thrownExceptions
		// BlockStmt body
		return new ConstructorDeclaration(
				parseModifiersFromToken(memberData.get(0)), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()), 
				parseListFromToken(TypeParameter.class, memberData.get(2), info.getCopy()), 
				createSimpleName(memberData.get(3), info.getCopy()), 
				parseListFromToken(Parameter.class, memberData.get(4), info.getCopy()), 
				parseListFromToken(ReferenceType.class, memberData.get(5), info.getCopy()), 
				createBlockStmt(memberData.get(6), info.getCopy()));
	}

	public default DoubleLiteralExpr createDoubleLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getDoubleLiteralExpression(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(DoubleLiteralExpr.class, token, info.getCopy());
		}

		// final String value
		return new DoubleLiteralExpr(
				parseStringValueFromToken( memberData.get(0) ));
	}

	public default StringLiteralExpr createStringLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getStringLiteralExpression(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(StringLiteralExpr.class, token, info.getCopy());
		}

		// final String value
		return new StringLiteralExpr(
				parseStringValueFromToken( memberData.get(0) ));
	}

	public default BooleanLiteralExpr createBooleanLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getBooleanLiteralExpression(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(BooleanLiteralExpr.class, token, info.getCopy());
		}

		// boolean value
		return new BooleanLiteralExpr(
				parseBooleanFromToken( memberData.get(0) ));
	}

	public default CharLiteralExpr createCharLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getCharLiteralExpression(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(CharLiteralExpr.class, token, info.getCopy());
		}

		// String value
		return new CharLiteralExpr(
				parseStringValueFromToken( memberData.get(0) ));
	}

	public default LongLiteralExpr createLongLiteralExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getLongLiteralExpression(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(LongLiteralExpr.class, token, info.getCopy());
		}

		// final String value
		return new LongLiteralExpr(
				parseStringValueFromToken( memberData.get(0) ));
	}

	public default ThisExpr createThisExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getThisExpression(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ThisExpr.class, token, info.getCopy());
		}

		// final Expression classExpr
		return new ThisExpr(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy() ));
	}

	public default BreakStmt createBreakStmt(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getBreak(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(BreakStmt.class, token, info.getCopy());
		}

		// final SimpleName label
		return new BreakStmt(
				createSimpleName(memberData.get(0), info.getCopy()));
	}

	public default ObjectCreationExpr createObjectCreationExpr(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getObjCreateExpression(), 5); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ObjectCreationExpr.class, token, info.getCopy());
		}

		// final Expression scope
		// final ClassOrInterfaceType type
		// final NodeList<Type> typeArguments
		// final NodeList<Expression> arguments
		// final NodeList<BodyDeclaration<?>> anonymousClassBody
		return new ObjectCreationExpr(
				createNodeFromToken( Expression.class, memberData.get(0), info.getCopy()), 
				createClassOrInterfaceType( memberData.get(1), info.getCopy()), 
				parseListFromToken(Type.class, memberData.get(2), info.getCopy()), 
				parseListFromToken(Expression.class, memberData.get(3), info.getCopy()), 
				parseBodyDeclarationListFromToken(memberData.get(4), info.getCopy()));
	}

	public default MarkerAnnotationExpr createMarkerAnnotationExpr(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getMarkerAnnotationExpression(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(MarkerAnnotationExpr.class, token, info.getCopy());
		}

		// final Name name
		return new MarkerAnnotationExpr( 
				createName(memberData.get(0), info.getCopy())); 
	}

	public default NormalAnnotationExpr createNormalAnnotationExpr(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getNormalAnnotationExpression(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(NormalAnnotationExpr.class, token, info.getCopy());
		}

		// final Name name
		// final NodeList<MemberValuePair> pairs
		return new NormalAnnotationExpr(
				createName(memberData.get(0), info.getCopy()),
				parseListFromToken(MemberValuePair.class, memberData.get(1), info.getCopy()));
	}

	public default SingleMemberAnnotationExpr createSingleMemberAnnotationExpr(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getSingleMemberAnnotationExpression(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(SingleMemberAnnotationExpr.class, token, info.getCopy());
		}

		// final Name name
		// final Expression memberValue
		return new SingleMemberAnnotationExpr(
				createName(memberData.get(0), info.getCopy()),
				createNodeFromToken( Expression.class,  memberData.get(1), info.getCopy()));
	}

	public default Parameter createParameter(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getParameter(), 6); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(Parameter.class, token, info.getCopy());
		}

		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// Type type
		// boolean isVarArgs
		// NodeList<AnnotationExpr> varArgsAnnotations
		// SimpleName name
		return new Parameter(
				parseModifiersFromToken(memberData.get(0)), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()), 
				createNodeFromToken( Type.class, memberData.get(2), info.getCopy()),
				parseBooleanFromToken( memberData.get(3) ),
				parseListFromToken(AnnotationExpr.class, memberData.get(4), info.getCopy()), 
				createSimpleName(memberData.get(5), info.getCopy()));
	}

	public default EnclosedExpr createEnclosedExpr(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getEnclosedExpression(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(EnclosedExpr.class, token, info.getCopy());
		}

		// final Expression inner
		return new EnclosedExpr(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy()));
	}

	public default AssertStmt createAssertStmt(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getAssertStmt(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(AssertStmt.class, token, info.getCopy());
		}

		// final Expression check
		// final Expression message
		return new AssertStmt(
				createNodeFromToken( Expression.class,  memberData.get(0), info.getCopy()),
				createNodeFromToken( Expression.class,  memberData.get(1), info.getCopy()));
	}

	public default ConstructorDeclaration createMemberValuePair(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getConstructorDeclaration(), 7); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ConstructorDeclaration.class, token, info.getCopy());
		}

		// EnumSet<Modifier> modifiers
		// NodeList<AnnotationExpr> annotations
		// NodeList<TypeParameter> typeParameters
		// SimpleName name
		// NodeList<Parameter> parameters
		// NodeList<ReferenceType> thrownExceptions
		// BlockStmt body
		return new ConstructorDeclaration(
				parseModifiersFromToken(memberData.get(0)), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()), 
				parseListFromToken(TypeParameter.class, memberData.get(2), info.getCopy()), 
				createSimpleName(memberData.get(3), info.getCopy()), 
				parseListFromToken(Parameter.class, memberData.get(4), info.getCopy()), 
				parseListFromToken(ReferenceType.class, memberData.get(5), info.getCopy()), 
				createBlockStmt(memberData.get(6), info.getCopy()));
	}

	public default PrimitiveType createPrimitiveType(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypePrimitive(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(PrimitiveType.class, token, info.getCopy());
		}

		// final Primitive type
		return new PrimitiveType(
				parsePrimitiveFromToken( memberData.get(0)));
	}

	// this may never be used
	public default UnionType createUnionType(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypeUnion(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(UnionType.class, token, info.getCopy());
		}

		// NodeList<ReferenceType> elements
		return new UnionType(
				parseListFromToken(ReferenceType.class, memberData.get(0), info.getCopy()));
	}

	public default IntersectionType createIntersectionType(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypeIntersection(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(IntersectionType.class, token, info.getCopy());
		}

		// NodeList<ReferenceType> elements
		return new IntersectionType( 
				parseListFromToken(ReferenceType.class, memberData.get(0), info.getCopy()));
	}

	public default TypeParameter createTypeParameter(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypePar(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(TypeParameter.class, token, info.getCopy());
		}

		// SimpleName name
		// NodeList<ClassOrInterfaceType> typeBound
		// NodeList<AnnotationExpr> annotations
		return new TypeParameter( 
				createSimpleName(memberData.get(0), info.getCopy()), 
				parseListFromToken(ClassOrInterfaceType.class, memberData.get(1), info.getCopy()), 
				parseListFromToken(AnnotationExpr.class, memberData.get(2), info.getCopy()));
	}

	public default WildcardType createWildcardType(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypeWildcard(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(WildcardType.class, token, info.getCopy());
		}

		// final ReferenceType extendedType
		// final ReferenceType superType
		return new WildcardType(
				createNodeFromToken( ReferenceType.class, memberData.get(0), info.getCopy() ),
				createNodeFromToken( ReferenceType.class, memberData.get(0), info.getCopy() ));
	}

	public default VoidType createVoidType(String token, InformationWrapper info)  throws IllegalArgumentException {
		return new VoidType();
	}

	public default UnknownType createUnknownType(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getTypeUnknown(), 0); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(UnknownType.class, token, info.getCopy());
		}

		// none
		return new UnknownType();
	}

	// only needed for debugging
	public default UnknownNode createUnknown(String token, InformationWrapper info)  throws IllegalArgumentException {	
		// none
		return new UnknownNode();
	}

	public default Name createName(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getName(), 3); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(Name.class, token, info.getCopy());
		}

		// final String identifier
		// NodeList<AnnotationExpr> annotations
		return new Name(
				createName( memberData.get(0), info.getCopy() ), // this will return null eventually but is this a bug or a feature?
				parseStringValueFromToken( memberData.get(1) ),
				parseListFromToken(AnnotationExpr.class, memberData.get(2), info.getCopy()));
	}

	public default SimpleName createSimpleName(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getSimpleName(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(SimpleName.class, token, info.getCopy());
		}

		// final String identifier
		return new SimpleName(
				parseStringValueFromToken( memberData.get(0) ));
	}

	public default LocalClassDeclarationStmt createLocalClassDeclarationStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getLocalClassDeclarationStmt(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(LocalClassDeclarationStmt.class, token, info.getCopy());
		}

		// final ClassOrInterfaceDeclaration classDeclaration
		return new LocalClassDeclarationStmt(
				createClassOrInterfaceDeclaration( memberData.get(0), info.getCopy()));
	}
	public default ArrayType createArrayType(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getArrayType(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ArrayType.class, token, info.getCopy());
		}

		// Type componentType
		// NodeList<AnnotationExpr> annotations
		return new ArrayType(
				createNodeFromToken( Type.class, memberData.get(0), info.getCopy()), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()));
	}

	public default ArrayCreationLevel createArrayCreationLevel(String token, InformationWrapper info)  throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getArrayCreationLevel(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ArrayCreationLevel.class, token, info.getCopy());
		}

		// Expression dimension
		// NodeList<AnnotationExpr> annotations
		return new ArrayCreationLevel(
				createNodeFromToken( Expression.class, memberData.get(0), info.getCopy()), 
				parseListFromToken(AnnotationExpr.class, memberData.get(1), info.getCopy()));
	}

	public default ModuleDeclaration createModuleDeclaration(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getModuleDeclaration(), 4); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ModuleDeclaration.class, token, info.getCopy());
		}

		// NodeList<AnnotationExpr> annotations
		// Name name
		// boolean isOpen
		// NodeList<ModuleStmt> moduleStmts
		return new ModuleDeclaration( 
				parseListFromToken(AnnotationExpr.class, memberData.get(0), info.getCopy()), 
				createName(memberData.get(1), info.getCopy()),
				parseBooleanFromToken(memberData.get(2)),
				parseListFromToken(ModuleStmt.class, memberData.get(3), info.getCopy()));
	}
	
	public default ModuleExportsStmt createModuleExportsStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getModuleExportsStmt(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ModuleExportsStmt.class, token, info.getCopy());
		}

		// Name name
		// NodeList<Name> moduleNames
		return new ModuleExportsStmt( 
				createName(memberData.get(0), info.getCopy()),
				parseListFromToken(Name.class, memberData.get(1), info.getCopy()));
	}
	
	public default ModuleOpensStmt createModuleOpensStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getModuleOpensStmt(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ModuleOpensStmt.class, token, info.getCopy());
		}

		// Name name
		// NodeList<Name> moduleNames
		return new ModuleOpensStmt( 
				createName(memberData.get(0), info.getCopy()),
				parseListFromToken(Name.class, memberData.get(1), info.getCopy()));
	}
	
	public default ModuleProvidesStmt createModuleProvidesStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getModuleProvidesStmt(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ModuleProvidesStmt.class, token, info.getCopy());
		}

		// Type type
		// NodeList<Type> withTypes
		return new ModuleProvidesStmt( 
				createNodeFromToken( Type.class, memberData.get(0), info.getCopy()),
				parseListFromToken(Type.class, memberData.get(1), info.getCopy()));
	}
	
	public default ModuleRequiresStmt createModuleRequiresStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getModuleRequiresStmt(), 2); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ModuleRequiresStmt.class, token, info.getCopy());
		}

		// EnumSet<Modifier> modifiers
		// Name name
		return new ModuleRequiresStmt( 
				parseModifiersFromToken(memberData.get(0)), 
				createName(memberData.get(1), info.getCopy()));
	}
	
	public default ModuleUsesStmt createModuleUsesStmt(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getModuleUsesStmt(), 1); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(ModuleUsesStmt.class, token, info.getCopy());
		}

		// Type type
		return new ModuleUsesStmt( 
				createNodeFromToken( Type.class, memberData.get(0), info.getCopy()));
	}
	
	public default CompilationUnit createCompilationUnit(String token, InformationWrapper info) throws IllegalArgumentException {
		List<String> memberData = parseAndCheckMembers(token, getKeyWordProvider().getCompilationUnit(), 4); 
		if (memberData == null) {
			return null;
		} else if (memberData.isEmpty()) { //token: id
			return guessNodeFromKeyWord(CompilationUnit.class, token, info.getCopy());
		}

		// PackageDeclaration packageDeclaration
		// NodeList<ImportDeclaration> imports
		// NodeList<TypeDeclaration<?>> types
		// ModuleDeclaration module
		return new CompilationUnit( 
				createNodeFromToken( PackageDeclaration.class, memberData.get(0), info.getCopy()),
				parseListFromToken(ImportDeclaration.class, memberData.get(1), info.getCopy()),
				parseTypeDeclarationListFromToken(memberData.get(2), info.getCopy()),
				createNodeFromToken( ModuleDeclaration.class, memberData.get(3), info.getCopy()));
	}


}
