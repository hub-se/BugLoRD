package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.TypeArguments;
import com.github.javaparser.ast.TypeParameter;
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
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.comments.BlockComment;
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

/**
 * Maps nodes to sequences of tokens that are either the abstract identifiers themselves, 
 * or they wrap the identifiers and various information of the respecting nodes in the following 
 * manner:
 * 
 * <p> {@code ($NODE_IDENTIFIER) ($NODE_IDENTIFIER;[list,with,information],information) ($NODE_IDENTIFIER;more,information) ...}
 * 
 * @author Simon
 */
public class Node2OneStringMapping extends SimpleNode2StringMapping<Integer> {
	
	/**
	 * All tokens will be put together into one string that can be recreated very easy
	 * @param aIdentifier The keyword of the node
	 * @param aTokens All its data blocks
	 * @return A finished string for the language model
	 */
	private String combineData2String( String aIdentifier, String... aTokens ) {
		String result = BIG_GROUP_START + aIdentifier;
		
		if ( aTokens == null || aTokens.length == 0 ) {
			return result + BIG_GROUP_END;
		}
		
		// fix the tokens that did not get the child group brackets
		String[] fixedTokens = new String[ aTokens.length ];
		
		for( int i = 0; i < aTokens.length; ++i ) {
			String fixedT = aTokens[i];
			if( !fixedT.startsWith( GROUP_START ) ) {
				fixedT = GROUP_START + fixedT + GROUP_END;
			}
			
			fixedTokens[i] = fixedT;
		}
		
		// there are some data to be put into the string
		result += ID_MARKER + String.join( SPLIT, fixedTokens );
		
		return result + BIG_GROUP_END;
	}

	
	private String getMappingForVariableDeclaratorList(List<VariableDeclarator> vars, int depth) {
		if( vars != null && !vars.isEmpty() ) {
			String result = GROUP_START;

			// first element has no leading split
			result += getMappingForVariableDeclarator(vars.get( 0 ), depth);
			
			for( int i = 1; i < vars.size(); ++i ) {	
				result += SPLIT + getMappingForVariableDeclarator(vars.get( i ), depth);
			}
			
			return result + GROUP_END;
		} else {
			return GROUP_START + GROUP_END;
		}
	}

	private String getMappingForTypeList(List<? extends Type> types, int depth) {
		if( types != null && !types.isEmpty()) {
			String result = GROUP_START;
			
			// first element again
			result += getMappingForType(types.get( 0 ), depth);
			
			for( int i = 1; i < types.size(); ++i ) {
				result += SPLIT + getMappingForType(types.get( i ), depth);
			}
			
			return result + GROUP_END;
		} else {
			return GROUP_START + GROUP_END;
		}
	}
	
	private String getMappingForScope( Expression scope, int depth) {
		if( scope != null ) {
			return GROUP_START + getMappingForExpression(scope, depth)+ GROUP_END;
		} else {
			return GROUP_START + GROUP_END;
		}
	}

	private String getMappingForTypeArguments(TypeArguments typeArguments, int depth) {
		if( typeArguments != null && 
				typeArguments.getTypeArguments() != null &&
				!typeArguments.getTypeArguments().isEmpty() && !typeArguments.isUsingDiamondOperator() ) {
				
			List<Type> tArgs = typeArguments.getTypeArguments();
			String result = TYPEARG_START + getMappingForType( typeArguments.getTypeArguments().get( 0 ));
			
			for( int i = 1 ; i < tArgs.size(); ++i ) {
				result += SPLIT + getMappingForType(tArgs.get( i ), depth);
			}
			
			result += TYPEARG_END;
			
			return result;
		} else {
			return TYPEARG_START + TYPEARG_END ;
		}
	}

	private String getMappingForParameterList(List<Parameter> parameters, int depth) {
		if( parameters != null && !parameters.isEmpty()) {
			String result = GROUP_START + getMappingForParameter( parameters.get( 0 ), depth );
			
			for( int i = 1; i < parameters.size(); ++i ) {
				result += SPLIT + getMappingForParameter(parameters.get( i ), depth);
			}
			
			return result + GROUP_END;
		} else {
			return GROUP_START + GROUP_END;
		}
	}

	private String getMappingForExpressionList(List<Expression> expressions, int depth) {
		if( expressions != null && !expressions.isEmpty()) {
			String result = GROUP_START + getMappingForExpression(expressions.get( 0 ), depth);;
			
			for( int i = 1; i < expressions.size(); ++i ) {
				result += SPLIT + getMappingForExpression(expressions.get( i ), depth);
			}
			
			return result + GROUP_END;
		} else {
			return GROUP_START + GROUP_END;
		}
	}

	private String getMappingForBodyDeclarationList(List<BodyDeclaration> bodyDeclarations, int depth) {
		if( bodyDeclarations != null && !bodyDeclarations.isEmpty() ) {
			String result = GROUP_START + getMappingForBodyDeclaration( bodyDeclarations.get( 0 ), depth);
			
			for( int i = 1; i < bodyDeclarations.size(); ++i ) {
				result += SPLIT + getMappingForBodyDeclaration( bodyDeclarations.get( i ), depth);
			}

			return result + GROUP_END;
		} else {
			return GROUP_START + GROUP_END;
		}
	}

	private String getMappingForClassOrInterfaceTypeList(List<ClassOrInterfaceType> types, int depth) {
		if( types != null && !types.isEmpty() ) {
			String result = GROUP_START + getMappingForType( types.get( 0 ), depth );
			
			for( int i = 1; i < types.size(); ++i ) {
				result += SPLIT + getMappingForType(types.get( i ), depth);
			}

			return result + GROUP_END;
		} else {
			return GROUP_START + GROUP_END;
		}
	}

	private String getMappingsForTypeParameterList(List<TypeParameter> typeParameters, int depth) {
		if( typeParameters != null  && !typeParameters.isEmpty() ) {
			String result = GROUP_START + getMappingForTypeParameter(typeParameters.get( 0 ), depth);
			
			for( int i = 1; i < typeParameters.size(); ++i ) {
				result += SPLIT + getMappingForTypeParameter(typeParameters.get( i ), depth);
			}
			
			return result + GROUP_END ;
		} else {
			return GROUP_START + GROUP_END;
		}
	}
	
	private String getMappingForScope( ClassOrInterfaceType aNode ) {
		if ( aNode == null ) {
			return GROUP_START + GROUP_END;
		}
		
		return GROUP_START + getFullScope( aNode ) + GROUP_END;
	}
	
	private String getFullScope( ClassOrInterfaceType aNode ) {
		if ( aNode.getScope() == null ) {
			return aNode.getName();
		} else {
			return getFullScope( aNode.getScope() ) + "." + aNode.getName();
		}
	}

	protected int getAbstractionDepth(Integer[] values) {
		if (values != null && values.length > 0) {
			return values[0];
		} else {
			return -1;
		}
	}
	
	
	@Override
	public MappingWrapper<String> getMappingForMemberValuePair(MemberValuePair aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(MEMBER_VALUE_PAIR));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String name = aNode.getName();
		String expr = getMappingForExpression(aNode.getValue(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(MEMBER_VALUE_PAIR, name, expr));
	}

	@Override
	public MappingWrapper<String> getMappingForTypeDeclarationStmt(TypeDeclarationStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(TYPE_DECLARATION_STATEMENT));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(TYPE_DECLARATION_STATEMENT, 
				getMappingForTypeDeclaration(aNode.getTypeDeclaration(), depth).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(SWITCH_ENTRY_STATEMENT));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String label = getMappingForExpression(aNode.getLabel(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(SWITCH_ENTRY_STATEMENT, label) );
	}

	@Override
	public MappingWrapper<String> getMappingForUnionType(UnionType aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(TYPE_UNION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(TYPE_UNION, 
				getMappingForTypeList(aNode.getElements(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForIntersectionType(IntersectionType aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(TYPE_INTERSECTION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(TYPE_INTERSECTION, 
				getMappingForTypeList(aNode.getElements(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForLambdaExpr(LambdaExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(LAMBDA_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String parEnclosed = (aNode.isParametersEnclosed() ? "true" : "false");
		String parList = getMappingForParameterList(aNode.getParameters(), depth);
		String stmt = getMappingForStatement(aNode.getBody(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(LAMBDA_EXPRESSION, parEnclosed, parList, stmt));
	}

	@Override
	public MappingWrapper<String> getMappingForInstanceOfExpr(InstanceOfExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(INSTANCEOF_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String expr = getMappingForExpression(aNode.getExpr(), depth).toString();
		String type = getMappingForType(aNode.getType(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(INSTANCEOF_EXPRESSION, expr, type));
	}

	@Override
	public MappingWrapper<String> getMappingForConditionalExpr(ConditionalExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(CONDITIONAL_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String condExpr = getMappingForExpression(aNode.getCondition(), depth).toString();
		String thenExpr = getMappingForExpression(aNode.getThenExpr(), depth).toString();
		String elseExpr = getMappingForExpression(aNode.getElseExpr(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(CONDITIONAL_EXPRESSION, condExpr, thenExpr, elseExpr));
	}

	@Override
	public MappingWrapper<String> getMappingForObjectCreationExpr(ObjectCreationExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(OBJ_CREATE_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String scope = getMappingForScope( aNode.getScope(), depth);
		String ci_type = getMappingForClassOrInterfaceType(aNode.getType(), depth).toString();
		String type_list = getMappingForTypeList(aNode.getTypeArgs(), depth);
		String expr_list = getMappingForExpressionList(aNode.getArgs(), depth);
		String body_dec_list = getMappingForBodyDeclarationList(aNode.getAnonymousClassBody(), depth);
		
		return new MappingWrapper<>(combineData2String(OBJ_CREATE_EXPRESSION,scope, ci_type, type_list, expr_list, body_dec_list ));
	}

	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(CLASS_OR_INTERFACE_TYPE));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String scope = getMappingForScope( aNode );
		String tArgs = getMappingForTypeArguments(aNode.getTypeArguments(), depth);
		
		return new MappingWrapper<>(combineData2String(CLASS_OR_INTERFACE_TYPE, scope, tArgs));
	}

	@Override
	public MappingWrapper<String> getMappingForEnclosedExpr(EnclosedExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(ENCLOSED_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(ENCLOSED_EXPRESSION,
				getMappingForExpression(aNode.getInner(), depth).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(ARRAY_INIT_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(ARRAY_INIT_EXPRESSION,
				getMappingForExpressionList(aNode.getValues(), depth).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForArrayCreationExpr(ArrayCreationExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(ARRAY_CREATE_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}

		String type = getMappingForType(aNode.getType() ).toString();
		String exprList = getMappingForExpressionList(aNode.getDimensions(), depth);
		String arrCount = String.valueOf( aNode.getArrayCount() );
		String init = aNode.getInitializer() != null ? getMappingForArrayInitializerExpr(aNode.getInitializer(), depth).toString() : "";
		
		return new MappingWrapper<>(combineData2String(ARRAY_CREATE_EXPRESSION, type, exprList, arrCount, init ));
	}

	@Override
	public MappingWrapper<String> getMappingForArrayAccessExpr(ArrayAccessExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(ARRAY_ACCESS_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(ARRAY_ACCESS_EXPRESSION,
				getMappingForExpression(aNode.getIndex(), depth).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForTypeParameter(TypeParameter aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(TYPE_PAR));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String ci_typeList = getMappingForClassOrInterfaceTypeList(aNode.getTypeBound(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(combineData2String(TYPE_PAR,ci_typeList)));
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclaratorId(VariableDeclaratorId aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(VARIABLE_DECLARATION_ID));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String arrayCount = String.valueOf(aNode.getArrayCount());
		
		return new MappingWrapper<>(combineData2String(VARIABLE_DECLARATION_ID, arrayCount) );
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarator(VariableDeclarator aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(VARIABLE_DECLARATION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}

		String init = aNode.getInit() != null ? getMappingForExpression(aNode.getInit(), depth).toString() : "";
		
		return new MappingWrapper<>(combineData2String(VARIABLE_DECLARATION, init ) );
	}

	@Override
	public MappingWrapper<String> getMappingForImportDeclaration(ImportDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(IMPORT_DECLARATION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(IMPORT_DECLARATION, aNode.getName().toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForPackageDeclaration(PackageDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(PACKAGE_DECLARATION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(PACKAGE_DECLARATION,
				aNode.getPackageName()));
	}

	@Override
	public MappingWrapper<String> getMappingForMultiTypeParameter(MultiTypeParameter aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(MULTI_TYPE_PARAMETER));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String type = getMappingForNode(aNode.getType(), depth).toString();
		String mods = ModifierMapper.getModifierEnclosed(aNode.getModifiers());
		
		return new MappingWrapper<>(combineData2String(MULTI_TYPE_PARAMETER, type, mods));
	}

	@Override
	public MappingWrapper<String> getMappingForParameter(Parameter aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(PARAMETER));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String type = getMappingForNode(aNode.getType(), depth).toString();
		String mods = ModifierMapper.getModifierEnclosed(aNode.getModifiers());
		
		return new MappingWrapper<>(combineData2String(PARAMETER, type, mods));
	}

	@Override
	public MappingWrapper<String> getMappingForEnumDeclaration(EnumDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(ENUM_DECLARATION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String mods = ModifierMapper.getModifierEnclosed(aNode.getModifiers());
		
		return new MappingWrapper<>(combineData2String(ENUM_DECLARATION, mods ));
	}

	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			if (aNode.isInterface()) {
				return new MappingWrapper<>(combineData2String(INTERFACE_DECLARATION));
			} else {
				return new MappingWrapper<>(combineData2String(CLASS_DECLARATION));
			}
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}

		String tPars = getMappingsForTypeParameterList(aNode.getTypeParameters(), depth);
		String extendsList = getMappingForClassOrInterfaceTypeList( aNode.getExtends(), depth);
		String implementsList = getMappingForClassOrInterfaceTypeList( aNode.getImplements(), depth);
		
		if (aNode.isInterface()) {
			return new MappingWrapper<>(combineData2String(INTERFACE_DECLARATION, tPars, extendsList, implementsList));
		} else {
			return new MappingWrapper<>(combineData2String(CLASS_DECLARATION, tPars, extendsList, implementsList));
		}
	}

	@Override
	public MappingWrapper<String> getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(ENUM_CONSTANT_DECLARATION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String exprList = getMappingForExpressionList(aNode.getArgs(), depth);
		
		return new MappingWrapper<>(combineData2String(ENUM_CONSTANT_DECLARATION, exprList ));
	}

	@Override
	public MappingWrapper<String> getMappingForMethodDeclaration(MethodDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(METHOD_DECLARATION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String mods = ModifierMapper.getModifierEnclosed(aNode.getModifiers());
		String type = getMappingForType( aNode.getType(), depth ).toString();
		String pars = getMappingForParameterList( aNode.getParameters(), depth );
		String tPars = getMappingsForTypeParameterList(aNode.getTypeParameters(), depth);

		return new MappingWrapper<>(combineData2String(METHOD_DECLARATION, mods, type, pars, tPars));
	}

	@Override
	public MappingWrapper<String> getMappingForFieldDeclaration(FieldDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(FIELD_DECLARATION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String mods = ModifierMapper.getModifierEnclosed(aNode.getModifiers() );
		String type = getMappingForType(aNode.getType(), depth).toString();
		String varDecList = getMappingForVariableDeclaratorList(aNode.getVariables(), depth);
		
		return new MappingWrapper<>(combineData2String(FIELD_DECLARATION, mods, type, varDecList));
	}

	@Override
	public MappingWrapper<String> getMappingForConstructorDeclaration(ConstructorDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(CONSTRUCTOR_DECLARATION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String mods = ModifierMapper.getModifierEnclosed(aNode.getModifiers());
		String pars = getMappingForParameterList(aNode.getParameters(), depth);
		String typePars = getMappingsForTypeParameterList(aNode.getTypeParameters(), depth);

		return new MappingWrapper<>(combineData2String(CONSTRUCTOR_DECLARATION, mods, pars, typePars) );
	}

	@Override
	public MappingWrapper<String> getMappingForWhileStmt(WhileStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(WHILE_STATEMENT));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String expr = getMappingForExpression( aNode.getCondition(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(WHILE_STATEMENT, expr));
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchStmt(SwitchStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(SWITCH_STATEMENT));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String expr = getMappingForExpression(aNode.getSelector(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(SWITCH_STATEMENT, expr) );
	}

	@Override
	public MappingWrapper<String> getMappingForForStmt(ForStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(FOR_STATEMENT));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String init = getMappingForExpressionList(aNode.getInit(), depth);
		String compare = getMappingForExpression(aNode.getCompare(), depth).toString();
		String update = getMappingForExpressionList(aNode.getUpdate(), depth);
		
		return new MappingWrapper<>(combineData2String(FOR_STATEMENT, init, compare, update ) );
	}

	@Override
	public MappingWrapper<String> getMappingForForeachStmt(ForeachStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(FOR_EACH_STATEMENT));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String var = getMappingForVariableDeclarationExpr(aNode.getVariable(), depth).toString();
		String iterable = getMappingForExpression(aNode.getIterable(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(FOR_EACH_STATEMENT, var, iterable) );
	}

	@Override
	public MappingWrapper<String> getMappingForExplicitConstructorInvocationStmt(
			ExplicitConstructorInvocationStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(EXPLICIT_CONSTRUCTOR_STATEMENT));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String thisOrSuper = aNode.isThis() ? "this" : "super";
		String exprList = getMappingForExpressionList(aNode.getArgs(), depth);
		String typeList = getMappingForTypeList(aNode.getTypeArgs(), depth);
		
		return new MappingWrapper<>(combineData2String(EXPLICIT_CONSTRUCTOR_STATEMENT, thisOrSuper, exprList, typeList ));
	}

	@Override
	public MappingWrapper<String> getMappingForDoStmt(DoStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(DO_STATEMENT));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String expr = getMappingForExpression(aNode.getCondition(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(DO_STATEMENT, expr) );
	}

	@Override
	public MappingWrapper<String> getMappingForAssertStmt(AssertStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(ASSERT_STMT));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String expr = getMappingForExpression(aNode.getCheck(), depth).toString();
		String msg = getMappingForExpression(aNode.getMessage()).toString();
		
		return new MappingWrapper<>(combineData2String(ASSERT_STMT, expr, msg));
	}

	@Override
	public MappingWrapper<String> getMappingForReferenceType(ReferenceType aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(TYPE_REFERENCE));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String type = getMappingForType(aNode.getType(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(TYPE_REFERENCE, type));
	}

	@Override
	public MappingWrapper<String> getMappingForPrimitiveType(PrimitiveType aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(TYPE_PRIMITIVE));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String type = aNode.getType().toString();
		
		return new MappingWrapper<>(combineData2String(TYPE_PRIMITIVE, type) );
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(VARIABLE_DECLARATION_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String mods = ModifierMapper.getModifierEnclosed(aNode.getModifiers());
		String types = getMappingForType(aNode.getType(), depth).toString();
		String varDecList = getMappingForVariableDeclaratorList(aNode.getVars(), depth);
		
		return new MappingWrapper<>(combineData2String(VARIABLE_DECLARATION_EXPRESSION, mods, types, varDecList));
	}

	@Override
	public MappingWrapper<String> getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(METHOD_REFERENCE_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String scope = ((aNode.getScope() != null ? getMappingForExpression(aNode.getScope(), depth) + "::" : "") +
				aNode.getIdentifier());

		String tArgs = getMappingForTypeArguments(aNode.getTypeArguments(), depth) ;

		return new MappingWrapper<>(combineData2String(METHOD_REFERENCE_EXPRESSION, scope, tArgs ));
	}

	@Override
	public MappingWrapper<String> getMappingForMethodCallExpr(MethodCallExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(METHOD_CALL_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		String method = "";
		if( privMethodBL.contains( aNode.getName() ) ) {
			method += PRIVATE_METHOD_CALL_EXPRESSION;
		} else {
			if (aNode.getScope() != null) {
				method += getMappingForExpression(aNode.getScope(), depth);
			}
		}
		
		String name = aNode.getName();
		String exprList = getMappingForExpressionList(aNode.getArgs(), depth);
		String typeList = getMappingForTypeList(aNode.getTypeArgs(), depth);
		
		return new MappingWrapper<>(combineData2String(METHOD_CALL_EXPRESSION, method, name, exprList, typeList ) );
	}

	@Override
	public MappingWrapper<String> getMappingForFieldAccessExpr(FieldAccessExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(FIELD_ACCESS_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String nameExpr = getMappingForNameExpr(aNode.getFieldExpr(), depth).toString();
		String typeList = getMappingForTypeList(aNode.getTypeArgs(), depth);
		
		
		return new MappingWrapper<>(combineData2String(FIELD_ACCESS_EXPRESSION, nameExpr, typeList ));
		
		//		if ( aFieldAccessExpr.getScope() != null ) {
		//			result2 += getMappingForNode(aFieldAccessExpr.getScope()) + ".";
		//		}
	}
	
	//TODO: rework implements and extends -> add as simple tokens and push the list traversal in the ASTTokenReader class
	@Override
	public MappingWrapper<String> getMappingForExtendsStmt(ExtendsStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(EXTENDS_STATEMENT));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}

		// TODO what is this?
//		mappings.addAll(getMappingsForClassOrInterfaceTypeList(aNode.getExtends(), depth).getMappings());
		String extendsStr = getMappingForClassOrInterfaceTypeList(aNode.getExtends(), depth);

		return new MappingWrapper<>(combineData2String(EXTENDS_STATEMENT, extendsStr));
	}

	@Override
	public MappingWrapper<String> getMappingForImplementsStmt(ImplementsStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(IMPLEMENTS_STATEMENT));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
//		mappings.addAll(getMappingsForClassOrInterfaceTypeList(aNode.getImplements(), depth).getMappings());
		String implementsStr = getMappingForClassOrInterfaceTypeList(aNode.getImplements(), depth);

		return new MappingWrapper<>(combineData2String(IMPLEMENTS_STATEMENT, implementsStr ));
	}

	@Override
	public MappingWrapper<String> getMappingForTypeExpr(TypeExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(TYPE_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String type = getMappingForType(aNode.getType(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(TYPE_EXPRESSION, type));
	}

	@Override
	public MappingWrapper<String> getMappingForUnaryExpr(UnaryExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(UNARY_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String operator = aNode.getOperator().toString();
		String expr = getMappingForExpression(aNode.getExpr(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(UNARY_EXPRESSION, operator, expr));
	}

	@Override
	public MappingWrapper<String> getMappingForClassExpr(ClassExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(CLASS_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String type = getMappingForType(aNode.getType(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(CLASS_EXPRESSION, type));
	}

	@Override
	public MappingWrapper<String> getMappingForCastExpr(CastExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(CAST_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String type = getMappingForType(aNode.getType(), depth).toString();
		String expr = getMappingForExpression(aNode.getExpr(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(CAST_EXPRESSION, type, expr));
	}

	@Override
	public MappingWrapper<String> getMappingForBinaryExpr(BinaryExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(BINARY_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String exprLeft = getMappingForExpression(aNode.getLeft(), depth).toString();
		String operator = aNode.getOperator().toString();
		String exprRight = getMappingForExpression(aNode.getRight(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(BINARY_EXPRESSION, exprLeft, operator, exprRight));
	}

	@Override
	public MappingWrapper<String> getMappingForAssignExpr(AssignExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(ASSIGN_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String exprTar = getMappingForExpression(aNode.getTarget(), depth).toString();
		String operator = aNode.getOperator().toString();
		String exprValue = getMappingForExpression(aNode.getValue(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(ASSIGN_EXPRESSION, exprTar, operator, exprValue));
	}

	@Override
	public MappingWrapper<String> getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(DOUBLE_LITERAL_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String value = aNode.getValue();
		
		return new MappingWrapper<>(combineData2String(DOUBLE_LITERAL_EXPRESSION, value ));
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralExpr(LongLiteralExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(LONG_LITERAL_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String value = aNode.getValue();
		
		return new MappingWrapper<>(combineData2String(LONG_LITERAL_EXPRESSION, value ));
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralMinValueExpr(LongLiteralMinValueExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(LONG_LITERAL_MIN_VALUE_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String value = aNode.getValue();
		
		return new MappingWrapper<>(combineData2String(LONG_LITERAL_MIN_VALUE_EXPRESSION, value ));
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(INTEGER_LITERAL_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String value = aNode.getValue();
		
		return new MappingWrapper<>(combineData2String(INTEGER_LITERAL_EXPRESSION, value));
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralMinValueExpr(IntegerLiteralMinValueExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(INTEGER_LITERAL_MIN_VALUE_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String value = aNode.getValue();
		
		return new MappingWrapper<>(combineData2String(INTEGER_LITERAL_MIN_VALUE_EXPRESSION, value));
	}

	@Override
	public MappingWrapper<String> getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(BOOLEAN_LITERAL_EXPRESSION));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String value = String.valueOf( aNode.getValue() );
		
		return new MappingWrapper<>(combineData2String(BOOLEAN_LITERAL_EXPRESSION, value ));
	}
	
	@Override
	public MappingWrapper<String> getMappingForIfStmt(IfStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(combineData2String(IF_STATEMENT));
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String cond = getMappingForExpression(aNode.getCondition(), depth).toString();
		
		return new MappingWrapper<>(combineData2String(IF_STATEMENT, cond));
	}
	
	// Here are some special cases that will always only consist of their keyword but I need to overwrite the simple mapper anyway to get the group brackets
	@Override
	public MappingWrapper<String> getMappingForInitializerDeclaration(InitializerDeclaration aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(INITIALIZER_DECLARATION));
	}
	
	@Override
	public MappingWrapper<String> getMappingForThrowStmt(ThrowStmt aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(THROW_STATEMENT));
	}
	
	@Override
	public MappingWrapper<String> getMappingForNameExpr(NameExpr aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(NAME_EXPRESSION));
	}
	
	@Override
	public MappingWrapper<String> getMappingForTryStmt(TryStmt aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(TRY_STATEMENT));
	}
	
	@Override
	public MappingWrapper<String> getMappingForMethodBodyStmt(BodyStmt aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(BODY_STMT));
	}
	
	@Override
	public MappingWrapper<String> getMappingForStringLiteralExpr(StringLiteralExpr aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(STRING_LITERAL_EXPRESSION));
	}

	@Override
	public MappingWrapper<String> getMappingForCharLiteralExpr(CharLiteralExpr aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(CHAR_LITERAL_EXPRESSION));
	}


	@Override
	public MappingWrapper<String> getMappingForNullLiteralExpr(NullLiteralExpr aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(NULL_LITERAL_EXPRESSION));
	}
	
	@Override
	public MappingWrapper<String> getMappingForThisExpr(ThisExpr aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(THIS_EXPRESSION));
	}
	
	@Override
	public MappingWrapper<String> getMappingForBlockComment(BlockComment aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(BLOCK_COMMENT));
	}
	
	@Override
	public MappingWrapper<String> getMappingForExpressionStmt(ExpressionStmt aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(EXPRESSION_STATEMENT));
	}

	@Override
	public MappingWrapper<String> getMappingForSuperExpr(SuperExpr aNode,Integer... values) {
		return new MappingWrapper<>(combineData2String(SUPER_EXPRESSION));
	}
	
	@Override
	public MappingWrapper<String> getMappingForReturnStmt(ReturnStmt aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(RETURN_STATEMENT));
	}
	
	@Override
	public MappingWrapper<String> getMappingForLabeledStmt(LabeledStmt aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(LABELED_STATEMENT));
	}
	
	@Override
	public MappingWrapper<String> getMappingForThrowsStmt(ThrowsStmt aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(THROWS_STATEMENT));
	}
	
	@Override
	public MappingWrapper<String> getMappingForElseStmt(ElseStmt aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(ELSE_STATEMENT));
	}
	
	@Override
	public MappingWrapper<String> getMappingForBreakStmt(BreakStmt aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(BREAK));
	}
	
	@Override
	public MappingWrapper<String> getMappingForEmptyTypeDeclaration(EmptyTypeDeclaration aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(EMPTY_TYPE_DECLARATION));
	}
	
	@Override
	public MappingWrapper<String> getMappingForEmptyMemberDeclaration(EmptyMemberDeclaration aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(EMPTY_MEMBER_DECLARATION));
	}
	
	@Override
	public MappingWrapper<String> getMappingForEmptyStmt(EmptyStmt aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(EMPTY_STATEMENT));
	}
	
	@Override
	public MappingWrapper<String> getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(SINGLE_MEMBER_ANNOTATION_EXPRESSION));
	}
	

	@Override
	public MappingWrapper<String> getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(NORMAL_ANNOTATION_EXPRESSION));
	}

	@Override
	public MappingWrapper<String> getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(MARKER_ANNOTATION_EXPRESSION));
	}
	
	@Override
	public MappingWrapper<String> getMappingForWildcardType(WildcardType aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(TYPE_WILDCARD));
	}

	@Override
	public MappingWrapper<String> getMappingForVoidType(VoidType aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(TYPE_VOID));
	}

	@Override
	public MappingWrapper<String> getMappingForUnknownType(UnknownType aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(TYPE_UNKNOWN));
	}
	
	@Override
	public MappingWrapper<String> getMappingForBlockStmt(BlockStmt aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(BLOCK_STATEMENT));
	}
	
	@Override
	public MappingWrapper<String> getMappingForContinueStmt(ContinueStmt aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(CONTINUE_STATEMENT));
	}
	
	@Override
	public MappingWrapper<String> getMappingForSynchronizedStmt(SynchronizedStmt aNode,Integer... values) {
		return new MappingWrapper<>(combineData2String(SYNCHRONIZED_STATEMENT));
	}
	
	@Override
	public MappingWrapper<String> getMappingForCatchClause(CatchClause aNode, Integer... values) {
		return new MappingWrapper<>(combineData2String(CATCH_CLAUSE_STATEMENT));
	}
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.ITokenMapper#getClosingToken(com.github.javaparser.ast.Node)
	 */
	@Override
	public String getClosingToken(Node aNode, Integer... values) {
		if (aNode == null) {
			return null;
		}
		
		String result = null;

		if (aNode instanceof MethodDeclaration) {
			result = CLOSING_MDEC;
		} else if (aNode instanceof ConstructorDeclaration) {
			result = CLOSING_CNSTR;
		} else if (aNode instanceof IfStmt) {
			result = CLOSING_IF;
		} else if (aNode instanceof WhileStmt) {
			result = CLOSING_WHILE;
		} else if (aNode instanceof ForStmt) {
			result = CLOSING_FOR;
		} else if (aNode instanceof TryStmt) {
			result = CLOSING_TRY;
		} else if (aNode instanceof CatchClause) {
			result = CLOSING_CATCH;
		} else if (aNode instanceof ForeachStmt) {
			result = CLOSING_FOR_EACH;
		} else if (aNode instanceof DoStmt) {
			result = CLOSING_DO;
		} else if (aNode instanceof SwitchStmt) {
			result = CLOSING_SWITCH;
		} else if (aNode instanceof EnclosedExpr) {
			result = CLOSING_ENCLOSED;
		} else if (aNode instanceof BlockStmt) {
			result = CLOSING_BLOCK_STMT;
		} else if (aNode instanceof ExpressionStmt) {
			result = CLOSING_EXPRESSION_STMT;
		} else if (aNode instanceof CompilationUnit) {
			result = CLOSING_COMPILATION_UNIT;
		}
		
		if ( result != null ) {
			return combineData2String(result);
		}

		return null;
	}

}
