package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.List;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.TypeArguments;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.MultiTypeParameter;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
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
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.TypeDeclarationStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.UnionType;

import se.de.hu_berlin.informatik.astlmbuilder.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ImplementsStmt;

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
		
		// there are some data to be put into the string
		result += ID_MARKER + String.join( SPLIT, aTokens );
		
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

	private String getMappingForTypeArguments(TypeArguments typeArguments, int depth) {
		if( typeArguments != null && 
				typeArguments.getTypeArguments() != null &&
				!typeArguments.getTypeArguments().isEmpty() ) {
			
			String result = TYPEARG_START + TYPEARG_END;
			
			// TODO what is this diamond operator and why is he excluded?
			if (!typeArguments.isUsingDiamondOperator()) {
				
				List<Type> tArgs = typeArguments.getTypeArguments();
				result = TYPEARG_START + getMappingForType( typeArguments.getTypeArguments().get( 0 ));
				
				for( int i = 1 ; i < tArgs.size(); ++i ) {
					result += SPLIT + getMappingForType(tArgs.get( i ), depth);
				}
				
				result += TYPEARG_END;
			}
			
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
			return new MappingWrapper<>(MEMBER_VALUE_PAIR);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(MEMBER_VALUE_PAIR, 
				aNode.getName() + SPLIT + 
				getMappingForExpression(aNode.getValue(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForTypeDeclarationStmt(TypeDeclarationStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(TYPE_DECLARATION_STATEMENT);
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
			return new MappingWrapper<>(SWITCH_ENTRY_STATEMENT);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(SWITCH_ENTRY_STATEMENT, 
				getMappingForExpression(aNode.getLabel(), depth).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForUnionType(UnionType aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(TYPE_UNION);
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
			return new MappingWrapper<>(TYPE_INTERSECTION);
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
			return new MappingWrapper<>(LAMBDA_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(LAMBDA_EXPRESSION,
				(aNode.isParametersEnclosed() ? "true" : "false"), 
				getMappingForParameterList(aNode.getParameters(), depth) + SPLIT + 
				getMappingForStatement(aNode.getBody(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForInstanceOfExpr(InstanceOfExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(INSTANCEOF_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(INSTANCEOF_EXPRESSION,
				getMappingForExpression(aNode.getExpr(), depth) + SPLIT + 
				getMappingForType(aNode.getType(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForConditionalExpr(ConditionalExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(CONDITIONAL_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(CONDITIONAL_EXPRESSION,
				getMappingForExpression(aNode.getCondition(), depth) + SPLIT + 
				getMappingForExpression(aNode.getThenExpr(), depth) + SPLIT + 
				getMappingForExpression(aNode.getElseExpr(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForObjectCreationExpr(ObjectCreationExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(OBJ_CREATE_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(OBJ_CREATE_EXPRESSION,
				(aNode.getScope() != null ? getMappingForExpression(aNode.getScope(), depth) + "." : "") +
				getMappingForClassOrInterfaceType(aNode.getType(), depth) + SPLIT + 
				getMappingForTypeList(aNode.getTypeArgs(), depth) + SPLIT + 
				getMappingForExpressionList(aNode.getArgs(), depth) + SPLIT + 
				getMappingForBodyDeclarationList(aNode.getAnonymousClassBody(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(CLASS_OR_INTERFACE_TYPE);
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
			return new MappingWrapper<>(ENCLOSED_EXPRESSION);
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
			return new MappingWrapper<>(ARRAY_INIT_EXPRESSION);
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
			return new MappingWrapper<>(ARRAY_CREATE_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(ARRAY_CREATE_EXPRESSION,
				getMappingForType(aNode.getType()) + SPLIT + 
				aNode.getArrayCount() + SPLIT + 
				getMappingForExpressionList(aNode.getDimensions(), depth) +
				(aNode.getInitializer() != null ? SPLIT + getMappingForArrayInitializerExpr(aNode.getInitializer(), depth) : "")));
	}

	@Override
	public MappingWrapper<String> getMappingForArrayAccessExpr(ArrayAccessExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(ARRAY_ACCESS_EXPRESSION);
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
			return new MappingWrapper<>(TYPE_PAR);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(TYPE_PAR,
				getMappingForClassOrInterfaceTypeList(aNode.getTypeBound(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclaratorId(VariableDeclaratorId aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(VARIABLE_DECLARATION_ID);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(VARIABLE_DECLARATION_ID,
				String.valueOf(aNode.getArrayCount())));
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarator(VariableDeclarator aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(VARIABLE_DECLARATION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(VARIABLE_DECLARATION,
				getMappingForVariableDeclaratorId(aNode.getId(), depth) + 
				(aNode.getInit() != null ? SPLIT + getMappingForExpression(aNode.getInit(), depth) : "")));
	}

	@Override
	public MappingWrapper<String> getMappingForImportDeclaration(ImportDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(IMPORT_DECLARATION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(IMPORT_DECLARATION,
				aNode.getName().toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForPackageDeclaration(PackageDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(PACKAGE_DECLARATION);
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
			return new MappingWrapper<>(MULTI_TYPE_PARAMETER);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(MULTI_TYPE_PARAMETER,
				getMappingForNode(aNode.getType(), depth) + SPLIT + 
				ModifierMapper.getModifierEnclosed(aNode.getModifiers())));
	}

	@Override
	public MappingWrapper<String> getMappingForParameter(Parameter aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(PARAMETER);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(PARAMETER,
				getMappingForNode(aNode.getType(), depth) + SPLIT + 
				ModifierMapper.getModifierEnclosed(aNode.getModifiers())));
	}

	@Override
	public MappingWrapper<String> getMappingForEnumDeclaration(EnumDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(ENUM_DECLARATION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(ENUM_DECLARATION,
				ModifierMapper.getModifierEnclosed(aNode.getModifiers())));
	}

	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			if (aNode.isInterface()) {
				return new MappingWrapper<>(INTERFACE_DECLARATION);
			} else {
				return new MappingWrapper<>(CLASS_DECLARATION);
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
			return new MappingWrapper<>(ENUM_CONSTANT_DECLARATION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(ENUM_CONSTANT_DECLARATION,
				getMappingForExpressionList(aNode.getArgs(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForMethodDeclaration(MethodDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(METHOD_DECLARATION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		
		String mods = ModifierMapper.getModifierEnclosed(aNode.getModifiers());
		String type = getMappingForType( aNode.getType() ).toString();
		String pars = getMappingForParameterList( aNode.getParameters(), depth );
		String tPars = getMappingsForTypeParameterList(aNode.getTypeParameters(), depth);

		return new MappingWrapper<>(combineData2String(METHOD_DECLARATION, mods, type, pars, tPars));
	}

	@Override
	public MappingWrapper<String> getMappingForFieldDeclaration(FieldDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(FIELD_DECLARATION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(FIELD_DECLARATION,
				ModifierMapper.getModifierEnclosed(aNode.getModifiers()),
				getMappingForType(aNode.getType(), depth) + SPLIT + 
				getMappingForVariableDeclaratorList(aNode.getVariables(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForConstructorDeclaration(ConstructorDeclaration aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(CONSTRUCTOR_DECLARATION);
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
			return new MappingWrapper<>(WHILE_STATEMENT);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(WHILE_STATEMENT,
				getMappingForExpression(aNode.getCondition(), depth).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchStmt(SwitchStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(SWITCH_STATEMENT);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(SWITCH_STATEMENT,
				getMappingForExpression(aNode.getSelector(), depth).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForForStmt(ForStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(FOR_STATEMENT);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(FOR_STATEMENT,
				getMappingForExpressionList(aNode.getInit(), depth),
				getMappingForExpression(aNode.getCompare(), depth).toString(),
				getMappingForExpressionList(aNode.getUpdate(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForForeachStmt(ForeachStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(FOR_EACH_STATEMENT);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(FOR_EACH_STATEMENT,
				getMappingForVariableDeclarationExpr(aNode.getVariable(), depth).toString(),
				getMappingForExpression(aNode.getIterable(), depth).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForExplicitConstructorInvocationStmt(
			ExplicitConstructorInvocationStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(EXPLICIT_CONSTRUCTOR_STATEMENT);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(EXPLICIT_CONSTRUCTOR_STATEMENT,
				(aNode.isThis() ? "this" : "super") + SPLIT +
				getMappingForExpressionList(aNode.getArgs(), depth) + SPLIT + 
				getMappingForTypeList(aNode.getTypeArgs(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForDoStmt(DoStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(DO_STATEMENT);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(DO_STATEMENT,
				getMappingForExpression(aNode.getCondition(), depth).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForAssertStmt(AssertStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(ASSERT_STMT);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(ASSERT_STMT,
				getMappingForExpression(aNode.getCheck(), depth) + 
				(aNode.getMessage() != null ? SPLIT + getMappingForExpression(aNode.getMessage()) : "")));
	}

	@Override
	public MappingWrapper<String> getMappingForReferenceType(ReferenceType aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(TYPE_REFERENCE);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(TYPE_REFERENCE,
				getMappingForType(aNode.getType(), depth) + SPLIT + 
				aNode.getArrayCount()));
	}

	@Override
	public MappingWrapper<String> getMappingForPrimitiveType(PrimitiveType aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(TYPE_PRIMITIVE);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(TYPE_PRIMITIVE,
				aNode.getType().toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(VARIABLE_DECLARATION_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(VARIABLE_DECLARATION_EXPRESSION,
				ModifierMapper.getModifierEnclosed(aNode.getModifiers()),
				getMappingForType(aNode.getType(), depth) + SPLIT + 
				getMappingForVariableDeclaratorList(aNode.getVars(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(METHOD_REFERENCE_EXPRESSION);
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
			return new MappingWrapper<>(METHOD_CALL_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		String method = "";
		if( privMethodBL.contains( aNode.getName() ) ) {
			method += PRIVATE_METHOD_CALL_EXPRESSION;
		} else {
			if (aNode.getScope() != null) {
				method += getMappingForExpression(aNode.getScope(), depth) + ".";
			}
			method += aNode.getName();
		}
		
		return new MappingWrapper<>(combineData2String(METHOD_CALL_EXPRESSION,
				method + SPLIT +
				getMappingForExpressionList(aNode.getArgs(), depth) + SPLIT + 
				getMappingForTypeList(aNode.getTypeArgs(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForFieldAccessExpr(FieldAccessExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(FIELD_ACCESS_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(FIELD_ACCESS_EXPRESSION,
				getMappingForNameExpr(aNode.getFieldExpr(), depth) + SPLIT + 
				getMappingForTypeList(aNode.getTypeArgs(), depth)));
		
		//		if ( aFieldAccessExpr.getScope() != null ) {
		//			result2 += getMappingForNode(aFieldAccessExpr.getScope()) + ".";
		//		}
	}
	
	//TODO: rework implements and extends -> add as simple tokens and push the list traversal in the ASTTokenReader class
	@Override
	public MappingWrapper<String> getMappingForExtendsStmt(ExtendsStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(EXTENDS_STATEMENT);
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
			return new MappingWrapper<>(IMPLEMENTS_STATEMENT);
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
			return new MappingWrapper<>(TYPE_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(TYPE_EXPRESSION,
				getMappingForType(aNode.getType(), depth).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForUnaryExpr(UnaryExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(UNARY_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(UNARY_EXPRESSION,
				aNode.getOperator() + SPLIT + 
				getMappingForExpression(aNode.getExpr(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForClassExpr(ClassExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(CLASS_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(CLASS_EXPRESSION,
				getMappingForType(aNode.getType(), depth).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForCastExpr(CastExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(CAST_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(CAST_EXPRESSION,
				getMappingForType(aNode.getType(), depth)+ SPLIT + 
				getMappingForExpression(aNode.getExpr(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForBinaryExpr(BinaryExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(BINARY_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(BINARY_EXPRESSION,
				getMappingForExpression(aNode.getLeft(), depth)+ SPLIT + 
				aNode.getOperator() + SPLIT + 
				getMappingForExpression(aNode.getRight(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForAssignExpr(AssignExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(ASSIGN_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(ASSIGN_EXPRESSION,
				getMappingForExpression(aNode.getTarget(), depth) + SPLIT + 
				aNode.getOperator() + SPLIT + 
				getMappingForExpression(aNode.getValue(), depth)));
	}

	@Override
	public MappingWrapper<String> getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(DOUBLE_LITERAL_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(DOUBLE_LITERAL_EXPRESSION,
				aNode.getValue()));
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralExpr(LongLiteralExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(LONG_LITERAL_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(LONG_LITERAL_EXPRESSION,
				aNode.getValue()));
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralMinValueExpr(LongLiteralMinValueExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(LONG_LITERAL_MIN_VALUE_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(LONG_LITERAL_MIN_VALUE_EXPRESSION,
				aNode.getValue()));
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(INTEGER_LITERAL_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(INTEGER_LITERAL_EXPRESSION,
				aNode.getValue()));
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralMinValueExpr(IntegerLiteralMinValueExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(INTEGER_LITERAL_MIN_VALUE_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(INTEGER_LITERAL_MIN_VALUE_EXPRESSION,
				aNode.getValue()));
	}

	@Override
	public MappingWrapper<String> getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(BOOLEAN_LITERAL_EXPRESSION);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(BOOLEAN_LITERAL_EXPRESSION,
				String.valueOf(aNode.getValue())));
	}
	
	@Override
	public MappingWrapper<String> getMappingForIfStmt(IfStmt aNode, Integer... values) {
		int depth = getAbstractionDepth(values);
		if (depth == 0) { //maximum abstraction
			return new MappingWrapper<>(IF_STATEMENT);
		} else { //still at a higher level of abstraction (either negative or greater than 0)
			--depth;
		}
		return new MappingWrapper<>(combineData2String(IF_STATEMENT,
				getMappingForExpression(aNode.getCondition(), depth).toString()));
	}

}
