package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.ArrayList;
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

public class ExperimentalAdvancedNode2StringMapping extends SimpleNode2StringMapping {
	
	private List<String> getMarkedTokenList(String identifier, String... tokens) {
		List<String> result = new ArrayList<>(tokens.length);
		result.add(BIG_GROUP_START + identifier + BIG_GROUP_END);
		for (String token : tokens) {
			result.add(BIG_GROUP_START + identifier + ID_MARKER + token + BIG_GROUP_END);
		}
		return result;
	}
	
	private List<String> getMarkedTokenList(String identifier, List<String> tokens) {
		List<String> result = new ArrayList<>(tokens.size());
		result.add(BIG_GROUP_START + identifier + BIG_GROUP_END);
		for (String token : tokens) {
			result.add(BIG_GROUP_START + identifier + ID_MARKER + token + BIG_GROUP_END);
		}
		return result;
	}

	private String getMappingForVariableDeclaratorList(List<VariableDeclarator> vars) {
		if( vars != null ) {
			String result = GROUP_START;
			result += vars.size();
			for( VariableDeclarator singleType : vars ) {
				result += SPLIT + getMappingForVariableDeclarator(singleType);
			}
			result += GROUP_END;
			return result;
		} else {
			return "null";
		}
	}

	private String getMappingForTypeList(List<? extends Type> types) {
		if( types != null ) {
			String result = GROUP_START;
			result += types.size();
			for( Type singleType : types ) {
				result += SPLIT + getMappingForType(singleType);
			}
			result += GROUP_END;
			return result;
		} else {
			return "null";
		}
	}

	//	private String getMappingForTypeParameterList(List<TypeParameter> typeParameters) {
	//		if( typeParameters != null ) {
	//			String result = GROUP_START;
	//			result += typeParameters.size();
	//			for( TypeParameter singleType : typeParameters ) {
	//				result += SPLIT + getMappingForTypeParameter(singleType);
	//			}
	//			result += GROUP_END;
	//			return result;
	//		} else {
	//			return "null";
	//		}
	//	}

	private String getMappingForTypeArguments(TypeArguments typeArguments) {
		if( typeArguments != null && 
				typeArguments.getTypeArguments() != null &&
				typeArguments.getTypeArguments().size() > 0) {
			String result = TYPEARG_START;
			if (!typeArguments.isUsingDiamondOperator()) {
				result += typeArguments.getTypeArguments().size();
				for( Type singleType : typeArguments.getTypeArguments() ) {
					result += SPLIT + getMappingForType(singleType);
				}
			}
			result += TYPEARG_END;
			return result;
		} else {
			return "null";
		}
	}

	private String getMappingForParameterList(List<Parameter> parameters) {
		if( parameters != null ) {
			String result = GROUP_START;
			result += parameters.size();
			for( Parameter singleType : parameters ) {
				result += SPLIT + getMappingForParameter(singleType);
			}
			result += GROUP_END;
			return result;
		} else {
			return "null";
		}
	}

	private String getMappingForExpressionList(List<Expression> expressions) {
		if( expressions != null ) {
			String result = GROUP_START;
			result += expressions.size();
			for( Expression singleType : expressions ) {
				result += SPLIT + getMappingForExpression(singleType);
			}
			result += GROUP_END;
			return result;
		} else {
			return "null";
		}
	}

	private String getMappingForBodyDeclarationList(List<BodyDeclaration> bodyDeclarations) {
		if( bodyDeclarations != null ) {
			String result = GROUP_START;
			result += bodyDeclarations.size();
			for( BodyDeclaration singleType : bodyDeclarations ) {
				result += SPLIT + getMappingForBodyDeclaration(singleType);
			}
			result += GROUP_END;
			return result;
		} else {
			return "null";
		}
	}

	private String getMappingForClassOrInterfaceTypeList(List<ClassOrInterfaceType> types) {
		if( types != null ) {
			String result = GROUP_START;
			result += types.size();
			for( Type singleType : types ) {
				result += SPLIT + getMappingForType(singleType);
			}
			result += GROUP_END;
			return result;
		} else {
			return "null";
		}
	}

	private MappingWrapper<String> getMappingsForClassOrInterfaceTypeList(List<ClassOrInterfaceType> types) {
		if( types != null && types.size() > 0) {
			MappingWrapper<String> result = new MappingWrapper<>();
			for( Type singleType : types ) {
				result.addMappings(getMappingForType(singleType).getMappings());
			}
			return result;
		} else {
			return new MappingWrapper<>();
		}
	}

	private MappingWrapper<String> getMappingsForTypeParameterList(List<TypeParameter> typeParameters) {
		if( typeParameters != null  && typeParameters.size() > 0 ) {
			MappingWrapper<String> result = new MappingWrapper<>(TYPE_PARAMETERS_START);
			for( TypeParameter singleType : typeParameters ) {
				result.addMappings(getMappingForTypeParameter(singleType).getMappings());
			}
			return result;
		} else {
			return new MappingWrapper<>();
		}
	}

	@Override
	public MappingWrapper<String> getMappingForMemberValuePair(MemberValuePair aNode) {
		return new MappingWrapper<>(getMarkedTokenList(MEMBER_VALUE_PAIR, 
				aNode.getName() + SPLIT + 
				getMappingForExpression(aNode.getValue())));
	}

	@Override
	public MappingWrapper<String> getMappingForTypeDeclarationStmt(TypeDeclarationStmt aNode) {
		return new MappingWrapper<>(getMarkedTokenList(TYPE_DECLARATION_STATEMENT, 
				getMappingForTypeDeclaration(aNode.getTypeDeclaration()).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchEntryStmt(SwitchEntryStmt aNode) {
		return new MappingWrapper<>(getMarkedTokenList(SWITCH_ENTRY_STATEMENT, 
				getMappingForExpression(aNode.getLabel()).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForUnionType(UnionType aNode) {
		return new MappingWrapper<>(getMarkedTokenList(TYPE_UNION, 
				getMappingForTypeList(aNode.getElements())));
	}

	@Override
	public MappingWrapper<String> getMappingForIntersectionType(IntersectionType aNode) {
		return new MappingWrapper<>(getMarkedTokenList(TYPE_INTERSECTION, 
				getMappingForTypeList(aNode.getElements())));
	}

	@Override
	public MappingWrapper<String> getMappingForLambdaExpr(LambdaExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(LAMBDA_EXPRESSION,
				(aNode.isParametersEnclosed() ? "true" : "false"), 
				getMappingForParameterList(aNode.getParameters()) + SPLIT + 
				getMappingForStatement(aNode.getBody())));
	}

	@Override
	public MappingWrapper<String> getMappingForInstanceOfExpr(InstanceOfExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(INSTANCEOF_EXPRESSION,
				getMappingForExpression(aNode.getExpr()) + SPLIT + 
				getMappingForType(aNode.getType())));
	}

	@Override
	public MappingWrapper<String> getMappingForConditionalExpr(ConditionalExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(CONDITIONAL_EXPRESSION,
				getMappingForExpression(aNode.getCondition()) + SPLIT + 
				getMappingForExpression(aNode.getThenExpr()) + SPLIT + 
				getMappingForExpression(aNode.getElseExpr())));
	}

	@Override
	public MappingWrapper<String> getMappingForObjectCreationExpr(ObjectCreationExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(OBJ_CREATE_EXPRESSION,
				(aNode.getScope() != null ? getMappingForExpression(aNode.getScope()) + "." : "") +
				getMappingForClassOrInterfaceType(aNode.getType()) + SPLIT + 
				getMappingForTypeList(aNode.getTypeArgs()) + SPLIT + 
				getMappingForExpressionList(aNode.getArgs()) + SPLIT + 
				getMappingForBodyDeclarationList(aNode.getAnonymousClassBody())));
	}

	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode) {
		//TODO boxed types?
		return new MappingWrapper<>(getMarkedTokenList(CLASS_OR_INTERFACE_TYPE,
				(aNode.getScope() != null ? getMappingForClassOrInterfaceType(aNode.getScope()) + "." : "") +
				aNode.getName() + SPLIT + 
				getMappingForTypeArguments(aNode.getTypeArguments())));
	}

	@Override
	public MappingWrapper<String> getMappingForEnclosedExpr(EnclosedExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(ENCLOSED_EXPRESSION,
				getMappingForExpression(aNode.getInner()).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(ARRAY_INIT_EXPRESSION,
				getMappingForExpressionList(aNode.getValues()).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForArrayCreationExpr(ArrayCreationExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(ARRAY_CREATE_EXPRESSION,
				getMappingForType(aNode.getType()) + SPLIT + 
				aNode.getArrayCount() + SPLIT + 
				getMappingForExpressionList(aNode.getDimensions()) +
				(aNode.getInitializer() != null ? SPLIT + getMappingForArrayInitializerExpr(aNode.getInitializer()) : "")));
	}

	@Override
	public MappingWrapper<String> getMappingForArrayAccessExpr(ArrayAccessExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(ARRAY_ACCESS_EXPRESSION,
				getMappingForExpression(aNode.getIndex()).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForTypeParameter(TypeParameter aNode) {
		return new MappingWrapper<>(getMarkedTokenList(TYPE_PAR,
				getMappingForClassOrInterfaceTypeList(aNode.getTypeBound())));
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclaratorId(VariableDeclaratorId aNode) {
		return new MappingWrapper<>(getMarkedTokenList(VARIABLE_DECLARATION_ID,
				String.valueOf(aNode.getArrayCount())));
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarator(VariableDeclarator aNode) {
		if (((VariableDeclarator)aNode).getInit() != null) {
			return new MappingWrapper<>(getMarkedTokenList(VARIABLE_DECLARATION,
					getMappingForVariableDeclaratorId(aNode.getId()) + SPLIT + 
					getMappingForExpression(aNode.getInit())));
		}
		return new MappingWrapper<>(getMarkedTokenList(VARIABLE_DECLARATION,
				getMappingForVariableDeclaratorId(aNode.getId()).toString())); 
		// + GROUP_START + aNode.getId() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForImportDeclaration(ImportDeclaration aNode) {
		return new MappingWrapper<>(getMarkedTokenList(IMPORT_DECLARATION,
				aNode.getName().toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForPackageDeclaration(PackageDeclaration aNode) {
		return new MappingWrapper<>(getMarkedTokenList(PACKAGE_DECLARATION,
				aNode.getPackageName()));
	}

	@Override
	public MappingWrapper<String> getMappingForMultiTypeParameter(MultiTypeParameter aNode) {
		return new MappingWrapper<>(getMarkedTokenList(MULTI_TYPE_PARAMETER,
				getMappingForNode(aNode.getType()) + SPLIT + 
				ModifierMapper.getModifier(aNode.getModifiers())));
	}

	@Override
	public MappingWrapper<String> getMappingForParameter(Parameter aNode) {
		return new MappingWrapper<>(getMarkedTokenList(PARAMETER,
				getMappingForNode(aNode.getType()) + SPLIT + 
				ModifierMapper.getModifier(aNode.getModifiers())));
	}

	@Override
	public MappingWrapper<String> getMappingForEnumDeclaration(EnumDeclaration aNode) {
		return new MappingWrapper<>(getMarkedTokenList(ENUM_DECLARATION,
				ModifierMapper.getModifier(aNode.getModifiers())));
	}

	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode) {
		//TODO is this splitting up of type parameters sensible?
		List<String> mappings = new ArrayList<>();
		mappings.addAll(getMappingsForTypeParameterList(aNode.getTypeParameters()).getMappings());
		
		if (aNode.isInterface()) {
			return new MappingWrapper<>(getMarkedTokenList(INTERFACE_DECLARATION, mappings));
		} else {
			return new MappingWrapper<>(getMarkedTokenList(CLASS_DECLARATION, mappings));
		}
	}

	@Override
	public MappingWrapper<String> getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode) {
		return new MappingWrapper<>(getMarkedTokenList(ENUM_CONSTANT_DECLARATION,
				getMappingForExpressionList(aNode.getArgs())));
	}

	@Override
	public MappingWrapper<String> getMappingForMethodDeclaration(MethodDeclaration aNode) {
		List<String> mappings = new ArrayList<>();
		
		mappings.add(ModifierMapper.getModifier(aNode.getModifiers()));
		mappings.add(getMappingForType(aNode.getType()) + SPLIT + 
				aNode.getArrayCount() + SPLIT + 
				getMappingForParameterList(aNode.getParameters()) + 
				(aNode.isDefault() ? SPLIT + "default" : ""));
		
		mappings.addAll(getMappingsForTypeParameterList(aNode.getTypeParameters()).getMappings());

		return new MappingWrapper<>(getMarkedTokenList(METHOD_DECLARATION, mappings));
	}

	@Override
	public MappingWrapper<String> getMappingForFieldDeclaration(FieldDeclaration aNode) {
		return new MappingWrapper<>(getMarkedTokenList(FIELD_DECLARATION,
				ModifierMapper.getModifier(aNode.getModifiers()),
				getMappingForType(aNode.getType()) + SPLIT + 
				getMappingForVariableDeclaratorList(aNode.getVariables())));
	}

	@Override
	public MappingWrapper<String> getMappingForConstructorDeclaration(ConstructorDeclaration aNode) {
		List<String> mappings = new ArrayList<>();
		
		mappings.add(ModifierMapper.getModifier(aNode.getModifiers()));
		mappings.add(getMappingForParameterList(aNode.getParameters()));
		
		mappings.addAll(getMappingsForTypeParameterList(aNode.getTypeParameters()).getMappings());

		return new MappingWrapper<>(getMarkedTokenList(CONSTRUCTOR_DECLARATION, mappings));
	}

	@Override
	public MappingWrapper<String> getMappingForWhileStmt(WhileStmt aNode) {
		return new MappingWrapper<>(getMarkedTokenList(WHILE_STATEMENT,
				getMappingForExpression(aNode.getCondition()).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchStmt(SwitchStmt aNode) {
		return new MappingWrapper<>(getMarkedTokenList(SWITCH_STATEMENT,
				getMappingForExpression(aNode.getSelector()).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForForStmt(ForStmt aNode) {
		return new MappingWrapper<>(getMarkedTokenList(FOR_STATEMENT,
				getMappingForExpressionList(aNode.getInit()),
				getMappingForExpression(aNode.getCompare()).toString(),
				getMappingForExpressionList(aNode.getUpdate())));
	}

	@Override
	public MappingWrapper<String> getMappingForForeachStmt(ForeachStmt aNode) {
		return new MappingWrapper<>(getMarkedTokenList(FOR_EACH_STATEMENT,
				getMappingForVariableDeclarationExpr(aNode.getVariable()).toString(),
				getMappingForExpression(aNode.getIterable()).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForExplicitConstructorInvocationStmt(
			ExplicitConstructorInvocationStmt aNode) {
		return new MappingWrapper<>(getMarkedTokenList(EXPLICIT_CONSTRUCTOR_STATEMENT,
				(aNode.isThis() ? "this" : "super") + SPLIT +
				getMappingForExpressionList(aNode.getArgs()) + SPLIT + 
				getMappingForTypeList(aNode.getTypeArgs())));
	}

	@Override
	public MappingWrapper<String> getMappingForDoStmt(DoStmt aNode) {
		return new MappingWrapper<>(getMarkedTokenList(DO_STATEMENT,
				getMappingForExpression(aNode.getCondition()).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForAssertStmt(AssertStmt aNode) {
		return new MappingWrapper<>(getMarkedTokenList(ASSERT_STMT,
				getMappingForExpression(aNode.getCheck()) + 
				(aNode.getMessage() != null ? SPLIT + getMappingForExpression(aNode.getMessage()) : "")));
	}

	@Override
	public MappingWrapper<String> getMappingForReferenceType(ReferenceType aNode) {
		return new MappingWrapper<>(getMarkedTokenList(TYPE_REFERENCE,
				getMappingForType(aNode.getType()) + SPLIT + 
				aNode.getArrayCount()));
	}

	@Override
	public MappingWrapper<String> getMappingForPrimitiveType(PrimitiveType aNode) {
		return new MappingWrapper<>(getMarkedTokenList(TYPE_PRIMITIVE,
				aNode.getType().toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(VARIABLE_DECLARATION_EXPRESSION,
				ModifierMapper.getModifier(aNode.getModifiers()),
				getMappingForType(aNode.getType()) + SPLIT + 
				getMappingForVariableDeclaratorList(aNode.getVars())));
	}

	@Override
	public MappingWrapper<String> getMappingForMethodReferenceExpr(MethodReferenceExpr aNode) {
		List<String> mappings = new ArrayList<>();
		
		mappings.add((aNode.getScope() != null ? getMappingForExpression(aNode.getScope()) + "::" : "") +
				aNode.getIdentifier());

		mappings.addAll(getMappingsForTypeParameterList(aNode.getTypeParameters()).getMappings());

		return new MappingWrapper<>(getMarkedTokenList(METHOD_REFERENCE_EXPRESSION, mappings));
	}

	@Override
	public MappingWrapper<String> getMappingForMethodCallExpr(MethodCallExpr aNode) {
		String method = "";
		if( privMethodBL.contains( aNode.getName() ) ) {
			method += PRIVATE_METHOD_CALL_EXPRESSION;
		} else {
			if (aNode.getScope() != null) {
				method += getMappingForExpression(aNode.getScope()) + ".";
			}
			method += aNode.getName();
		}
		
		return new MappingWrapper<>(getMarkedTokenList(METHOD_CALL_EXPRESSION,
				method + SPLIT +
				getMappingForExpressionList(aNode.getArgs()) + SPLIT + 
				getMappingForTypeList(aNode.getTypeArgs())));
	}

	@Override
	public MappingWrapper<String> getMappingForFieldAccessExpr(FieldAccessExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(FIELD_ACCESS_EXPRESSION,
				getMappingForNode(aNode.getFieldExpr()) + SPLIT + 
				getMappingForTypeList(aNode.getTypeArgs())));
		
		//		if ( aFieldAccessExpr.getScope() != null ) {
		//			result2 += getMappingForNode(aFieldAccessExpr.getScope()) + ".";
		//		}
	}

	@Override
	public MappingWrapper<String> getMappingForExtendsStmt(ExtendsStmt aNode) {
		List<String> mappings = new ArrayList<>();
		mappings.addAll(getMappingsForClassOrInterfaceTypeList(aNode.getExtends()).getMappings());

		return new MappingWrapper<>(getMarkedTokenList(EXTENDS_STATEMENT, mappings));
	}

	@Override
	public MappingWrapper<String> getMappingForImplementsStmt(ImplementsStmt aNode) {
		List<String> mappings = new ArrayList<>();
		mappings.addAll(getMappingsForClassOrInterfaceTypeList(aNode.getImplements()).getMappings());

		return new MappingWrapper<>(getMarkedTokenList(IMPLEMENTS_STATEMENT, mappings));
	}

	@Override
	public MappingWrapper<String> getMappingForTypeExpr(TypeExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(TYPE_EXPRESSION,
				getMappingForType(aNode.getType()).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForUnaryExpr(UnaryExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(UNARY_EXPRESSION,
				aNode.getOperator() + SPLIT + 
				getMappingForExpression(aNode.getExpr())));
	}

	@Override
	public MappingWrapper<String> getMappingForClassExpr(ClassExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(CLASS_EXPRESSION,
				getMappingForType(aNode.getType()).toString()));
	}

	@Override
	public MappingWrapper<String> getMappingForCastExpr(CastExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(CAST_EXPRESSION,
				getMappingForType(aNode.getType())+ SPLIT + 
				getMappingForExpression(aNode.getExpr())));
	}

	@Override
	public MappingWrapper<String> getMappingForBinaryExpr(BinaryExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(BINARY_EXPRESSION,
				getMappingForExpression(aNode.getLeft())+ SPLIT + 
				aNode.getOperator() + SPLIT + 
				getMappingForExpression(aNode.getRight())));
	}

	@Override
	public MappingWrapper<String> getMappingForAssignExpr(AssignExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(ASSIGN_EXPRESSION,
				getMappingForExpression(aNode.getTarget()) + SPLIT + 
				aNode.getOperator() + SPLIT + 
				getMappingForExpression(aNode.getValue())));
	}

	@Override
	public MappingWrapper<String> getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(DOUBLE_LITERAL_EXPRESSION,
				aNode.getValue()));
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralExpr(LongLiteralExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(LONG_LITERAL_EXPRESSION,
				aNode.getValue()));
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralMinValueExpr(LongLiteralMinValueExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(LONG_LITERAL_MIN_VALUE_EXPRESSION,
				aNode.getValue()));
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(INTEGER_LITERAL_EXPRESSION,
				aNode.getValue()));
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralMinValueExpr(IntegerLiteralMinValueExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(INTEGER_LITERAL_MIN_VALUE_EXPRESSION,
				aNode.getValue()));
	}

	@Override
	public MappingWrapper<String> getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode) {
		return new MappingWrapper<>(getMarkedTokenList(BOOLEAN_LITERAL_EXPRESSION,
				String.valueOf(aNode.getValue())));
	}

}
