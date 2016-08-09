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
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
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

public class AdvancedNode2StringMapping extends SimpleNode2StringMapping {

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
		return new MappingWrapper<>(MEMBER_VALUE_PAIR + 
				GROUP_START + aNode.getName() + 
				SPLIT + getMappingForExpression(aNode.getValue()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForTypeDeclarationStmt(TypeDeclarationStmt aNode) {
		return new MappingWrapper<>(TYPE_DECLARATION_STATEMENT + 
				GROUP_START + getMappingForTypeDeclaration(aNode.getTypeDeclaration()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchEntryStmt(SwitchEntryStmt aNode) {
		return new MappingWrapper<>(SWITCH_ENTRY_STATEMENT + 
				GROUP_START + getMappingForExpression(aNode.getLabel()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForUnionType(UnionType aNode) {
		return new MappingWrapper<>(TYPE_UNION + 
				GROUP_START + getMappingForTypeList(aNode.getElements()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForIntersectionType(IntersectionType aNode) {
		return new MappingWrapper<>(TYPE_INTERSECTION + 
				GROUP_START + getMappingForTypeList(aNode.getElements()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForLambdaExpr(LambdaExpr aNode) {
		return new MappingWrapper<>(LAMBDA_EXPRESSION + 
				GROUP_START + (aNode.isParametersEnclosed() ? "true" : "false") + GROUP_END, 
				GROUP_START + "L" + SPLIT + getMappingForParameterList(aNode.getParameters()) + 
				SPLIT + getMappingForStatement(aNode.getBody()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForInstanceOfExpr(InstanceOfExpr aNode) {
		return new MappingWrapper<>(INSTANCEOF_EXPRESSION + 
				GROUP_START + getMappingForExpression(aNode.getExpr()) + 
				SPLIT + getMappingForType(aNode.getType()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForConditionalExpr(ConditionalExpr aNode) {
		String result1 = CONDITIONAL_EXPRESSION;
		String result2 = GROUP_START + "COND" + 
				SPLIT + getMappingForExpression(aNode.getCondition()) + 
				SPLIT + getMappingForExpression(aNode.getThenExpr()) + 
				SPLIT + getMappingForExpression(aNode.getElseExpr()) + GROUP_END;

		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForObjectCreationExpr(ObjectCreationExpr aNode) {
		String result1 = OBJ_CREATE_EXPRESSION;
		String result2 = GROUP_START + "NEW" + SPLIT;

		if (aNode.getScope() != null) {
			result2 += aNode.getScope() + ".";
		}
		result2 += getMappingForClassOrInterfaceType(aNode.getType()) + 
				SPLIT + getMappingForTypeList(aNode.getTypeArgs()) + 
				SPLIT + getMappingForExpressionList(aNode.getArgs()) + 
				SPLIT + getMappingForBodyDeclarationList(aNode.getAnonymousClassBody()) + GROUP_END;

		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode) {
		//TODO boxed types?
		String result1 = CLASS_OR_INTERFACE_TYPE;
		String result2 = GROUP_START + "CIT" + SPLIT;

		if (aNode.getScope() != null) {
			result2 += aNode.getScope() + ".";
		}
		result2 += aNode.getName() + 
				SPLIT + getMappingForTypeArguments(aNode.getTypeArguments()) + GROUP_END;

		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForEnclosedExpr(EnclosedExpr aNode) {
		MappingWrapper<String> mapping = new MappingWrapper<>(ENCLOSED_EXPRESSION);

		mapping.addMappings(getMappingForExpression(aNode.getInner()).getMappings());

		mapping.addMapping(CLOSING_ENCLOSED);

		return mapping;
	}

	@Override
	public MappingWrapper<String> getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode) {
		String result1 = ARRAY_INIT_EXPRESSION;
		String result2 = GROUP_START + "ARR_INIT" + SPLIT + getMappingForExpressionList(aNode.getValues()) + GROUP_END;

		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForArrayCreationExpr(ArrayCreationExpr aNode) {
		String result1 = ARRAY_CREATE_EXPRESSION;
		String result2 = GROUP_START + "ARR_CREATE" + SPLIT + getMappingForType(aNode.getType()) + 
				SPLIT + aNode.getArrayCount() + 
				SPLIT + getMappingForExpressionList(aNode.getDimensions()) +
				(aNode.getInitializer() != null ? SPLIT + getMappingForArrayInitializerExpr(aNode.getInitializer()) : "") + GROUP_END;

		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForArrayAccessExpr(ArrayAccessExpr aNode) {
		return new MappingWrapper<>(ARRAY_ACCESS_EXPRESSION + 
				GROUP_START + getMappingForExpression(aNode.getIndex()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForTypeParameter(TypeParameter aNode) {
		return new MappingWrapper<>(TYPE_PAR + 
				GROUP_START + getMappingForClassOrInterfaceTypeList(aNode.getTypeBound()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclaratorId(VariableDeclaratorId aNode) {
		return new MappingWrapper<>(VARIABLE_DECLARATION_ID + 
				GROUP_START + aNode.getArrayCount() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarator(VariableDeclarator aNode) {
		if (((VariableDeclarator)aNode).getInit() != null) {
			return new MappingWrapper<>(VARIABLE_DECLARATION, 
					GROUP_START + "VD" + SPLIT + getMappingForVariableDeclaratorId(aNode.getId()) + 
					SPLIT + getMappingForExpression(aNode.getInit()) + GROUP_END);
		}
		return new MappingWrapper<>(VARIABLE_DECLARATION, 
				GROUP_START + "VD" + SPLIT + getMappingForVariableDeclaratorId(aNode.getId()) + GROUP_END); 
		// + GROUP_START + aNode.getId() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForImportDeclaration(ImportDeclaration aNode) {
		return new MappingWrapper<>(IMPORT_DECLARATION + 
				GROUP_START + aNode.getName() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForPackageDeclaration(PackageDeclaration aNode) {
		return new MappingWrapper<>(PACKAGE_DECLARATION + 
				GROUP_START + aNode.getPackageName() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForMultiTypeParameter(MultiTypeParameter aNode) {
		return new MappingWrapper<>(MULTI_TYPE_PARAMETER + 
				GROUP_START + getMappingForNode(aNode.getType()) + 
				SPLIT + ModifierMapper.getModifier(aNode.getModifiers()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForParameter(Parameter aNode) {
		return new MappingWrapper<>(PARAMETER + 
				GROUP_START + getMappingForNode(aNode.getType()) + 
				SPLIT + ModifierMapper.getModifier(aNode.getModifiers()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForEnumDeclaration(EnumDeclaration aNode) {
		return new MappingWrapper<>(ENUM_DECLARATION + 
				GROUP_START + ModifierMapper.getModifier(aNode.getModifiers()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode) {
		MappingWrapper<String> mapping = new MappingWrapper<>();
		if (aNode.isInterface()) {
			mapping.addMapping(INTERFACE_DECLARATION);
		} else {
			mapping.addMapping(CLASS_DECLARATION);
		}

		mapping.addMappings(getMappingsForTypeParameterList(aNode.getTypeParameters()).getMappings());

		return mapping;
	}

	@Override
	public MappingWrapper<String> getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode) {
		String result1 = ENUM_CONSTANT_DECLARATION;

		String result2 = GROUP_START + "ED" + 
				SPLIT + getMappingForExpressionList(aNode.getArgs()) + GROUP_END;

		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodDeclaration(MethodDeclaration aNode) {
		String result1 = METHOD_DECLARATION + 
				GROUP_START + ModifierMapper.getModifier(aNode.getModifiers()) + GROUP_END;

		String result2 = GROUP_START + "MD" + 
				SPLIT + getMappingForType(aNode.getType()) + 
				SPLIT + aNode.getArrayCount() +
				SPLIT + getMappingForParameterList(aNode.getParameters()) + 
				(aNode.isDefault() ? ",default" : "") + GROUP_END;

		MappingWrapper<String> mapping = new MappingWrapper<>(result1, result2);

		mapping.addMappings(getMappingsForTypeParameterList(aNode.getTypeParameters()).getMappings());

		return mapping;
	}

	@Override
	public MappingWrapper<String> getMappingForFieldDeclaration(FieldDeclaration aNode) {
		String result1 = FIELD_DECLARATION + GROUP_START + ModifierMapper.getModifier(aNode.getModifiers()) + GROUP_END;

		String result2 = GROUP_START + "FD" + SPLIT + getMappingForType(aNode.getType()) + 
				SPLIT + getMappingForVariableDeclaratorList(aNode.getVariables()) + GROUP_END;

		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForConstructorDeclaration(ConstructorDeclaration aNode) {
		String result1 = CONSTRUCTOR_DECLARATION + 
				GROUP_START + ModifierMapper.getModifier(aNode.getModifiers()) + GROUP_END;

		String result2 = GROUP_START + "CD" + 
				SPLIT + getMappingForParameterList(aNode.getParameters()) + GROUP_END;

		MappingWrapper<String> mapping = new MappingWrapper<>(result1, result2);

		mapping.addMappings(getMappingsForTypeParameterList(aNode.getTypeParameters()).getMappings());

		return mapping;
	}

	@Override
	public MappingWrapper<String> getMappingForWhileStmt(WhileStmt aNode) {
		return new MappingWrapper<>(WHILE_STATEMENT,
				GROUP_START + "W" + SPLIT + getMappingForExpression(aNode.getCondition()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchStmt(SwitchStmt aNode) {
		return new MappingWrapper<>(SWITCH_STATEMENT + GROUP_START + getMappingForExpression(aNode.getSelector()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForForStmt(ForStmt aNode) {
		String result1 = FOR_STATEMENT;
		String result2 = GROUP_START + "F_INIT" + 
				SPLIT + getMappingForExpressionList(aNode.getInit()) + GROUP_END;
		String result3 = GROUP_START + "F_COMP" + 
				SPLIT + getMappingForExpression(aNode.getCompare()) + GROUP_END;
		String result4 = GROUP_START + "F_UPD" + 
				SPLIT + getMappingForExpressionList(aNode.getUpdate()) + GROUP_END;

		return new MappingWrapper<>(result1, result2, result3, result4);
	}

	@Override
	public MappingWrapper<String> getMappingForForeachStmt(ForeachStmt aNode) {
		String result1 = FOR_EACH_STATEMENT;
		String result2 = GROUP_START + "FE_VAR" + 
				SPLIT + getMappingForVariableDeclarationExpr(aNode.getVariable()) + GROUP_END;
		String result3 = GROUP_START + "FE_ITER" + 
				SPLIT + getMappingForExpression(aNode.getIterable()) + GROUP_END;

		return new MappingWrapper<>(result1, result2, result3);
	}

	@Override
	public MappingWrapper<String> getMappingForExplicitConstructorInvocationStmt(
			ExplicitConstructorInvocationStmt aNode) {
		String result1 = EXPLICIT_CONSTRUCTOR_STATEMENT;
		String result2 = GROUP_START;

		if (aNode.isThis()) {
			result2 += "this" + SPLIT;
		} else {
			result2 += "super" + SPLIT;
		}

		result2 += getMappingForExpressionList(aNode.getArgs()) + 
				SPLIT + getMappingForTypeList(aNode.getTypeArgs()) + GROUP_END;

		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForDoStmt(DoStmt aNode) {
		return new MappingWrapper<>(DO_STATEMENT, 
				GROUP_START + "DO" + SPLIT + getMappingForExpression(aNode.getCondition()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForAssertStmt(AssertStmt aNode) {
		return new MappingWrapper<>(ASSERT_STMT,
				GROUP_START + "AT" + SPLIT + getMappingForExpression(aNode.getCheck())
				+ (aNode.getMessage() != null ? SPLIT + getMappingForExpression(aNode.getMessage()) : "") 
				+ GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForReferenceType(ReferenceType aNode) {
		return new MappingWrapper<>(TYPE_REFERENCE + 
				GROUP_START + getMappingForType(aNode.getType()) + 
				SPLIT + aNode.getArrayCount() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForPrimitiveType(PrimitiveType aNode) {
		return new MappingWrapper<>(TYPE_PRIMITIVE + GROUP_START + aNode.getType() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode) {
		String result1 = VARIABLE_DECLARATION_EXPRESSION + 
				GROUP_START + ModifierMapper.getModifier(aNode.getModifiers()) + GROUP_END;
		String result2 = GROUP_START + "VDE" + SPLIT + getMappingForType(aNode.getType()) + 
				SPLIT + getMappingForVariableDeclaratorList(aNode.getVars()) + GROUP_END;

		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodReferenceExpr(MethodReferenceExpr aNode) {
		String result1 = METHOD_REFERENCE_EXPRESSION;
		String result2 = GROUP_START + "MR" + SPLIT;

		if (aNode.getScope() != null) {
			result2 += aNode.getScope() + "::";
		}
		result2 += aNode.getIdentifier() + GROUP_END;

		MappingWrapper<String> mapping = new MappingWrapper<>(result1, result2);

		mapping.addMappings(getMappingsForTypeParameterList(aNode.getTypeParameters()).getMappings());

		return mapping;
	}

	@Override
	public MappingWrapper<String> getMappingForMethodCallExpr(MethodCallExpr aNode) {
		String result1 = METHOD_CALL_EXPRESSION;
		String result2 = GROUP_START + "MC" + SPLIT;

		if( privMethodBL.contains( aNode.getName() ) ) {
			result2 += PRIVATE_METHOD_CALL_EXPRESSION + SPLIT;
		} else {
			if (aNode.getScope() != null) {
				result2 += aNode.getScope() + ".";
			}
			result2 += aNode.getName() + SPLIT;
		}

		result2 += getMappingForExpressionList(aNode.getArgs()) + 
				SPLIT + getMappingForTypeList(aNode.getTypeArgs()) + GROUP_END;

		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForFieldAccessExpr(FieldAccessExpr aNode) {
		String result1 = FIELD_ACCESS_EXPRESSION;
		String result2 = GROUP_START + "FA" + SPLIT;

		//		if ( aFieldAccessExpr.getScope() != null ) {
		//			result2 += getMappingForNode(aFieldAccessExpr.getScope()) + ".";
		//		}

		result2 += getMappingForNode(aNode.getFieldExpr()) + 
				SPLIT + getMappingForTypeList(aNode.getTypeArgs()) + GROUP_END;

		return new MappingWrapper<>(result1, result2);
	}



	@Override
	public MappingWrapper<String> getMappingForExtendsStmt(ExtendsStmt aNode) {
		MappingWrapper<String> mapping = new MappingWrapper<>(EXTENDS_STATEMENT);

		mapping.addMappings(getMappingsForClassOrInterfaceTypeList(aNode.getExtends()).getMappings());

		return mapping;
	}

	@Override
	public MappingWrapper<String> getMappingForImplementsStmt(ImplementsStmt aNode) {
		MappingWrapper<String> mapping = new MappingWrapper<>(IMPLEMENTS_STATEMENT);

		mapping.addMappings(getMappingsForClassOrInterfaceTypeList(aNode.getImplements()).getMappings());

		return mapping;
	}

	@Override
	public MappingWrapper<String> getMappingForTypeExpr(TypeExpr aNode) {
		return new MappingWrapper<>(TYPE_EXPRESSION + 
				GROUP_START + getMappingForType(aNode.getType()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForUnaryExpr(UnaryExpr aNode) {
		return new MappingWrapper<>(UNARY_EXPRESSION, 
				GROUP_START + "U" + SPLIT + aNode.getOperator() 
				+ SPLIT + getMappingForExpression(aNode.getExpr()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForClassExpr(ClassExpr aNode) {
		return new MappingWrapper<>(CLASS_EXPRESSION + 
				GROUP_START + getMappingForType(aNode.getType()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForCastExpr(CastExpr aNode) {
		return new MappingWrapper<>(CAST_EXPRESSION,
				GROUP_START + "C" + SPLIT + getMappingForType(aNode.getType())
				+ SPLIT + getMappingForExpression(aNode.getExpr()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForBinaryExpr(BinaryExpr aNode) {
		return new MappingWrapper<>(BINARY_EXPRESSION, 
				GROUP_START + "B" + SPLIT + getMappingForExpression(aNode.getLeft()) 
				+ SPLIT + aNode.getOperator() 
				+ SPLIT + getMappingForExpression(aNode.getRight()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForAssignExpr(AssignExpr aNode) {
		return new MappingWrapper<>(ASSIGN_EXPRESSION, 
				GROUP_START + "A" + SPLIT + getMappingForExpression(aNode.getTarget()) 
				+ SPLIT + aNode.getOperator() 
				+ SPLIT + getMappingForExpression(aNode.getValue()) + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForStringLiteralExpr(StringLiteralExpr aNode) {
		return new MappingWrapper<>(STRING_LITERAL_EXPRESSION); 
		// + GROUP_START + aNode.getValue() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode) {
		return new MappingWrapper<>(DOUBLE_LITERAL_EXPRESSION + 
				GROUP_START + aNode.getValue() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralExpr(LongLiteralExpr aNode) {
		return new MappingWrapper<>(LONG_LITERAL_EXPRESSION + 
				GROUP_START + aNode.getValue() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralMinValueExpr(LongLiteralMinValueExpr aNode) {
		return new MappingWrapper<>(LONG_LITERAL_MIN_VALUE_EXPRESSION + 
				GROUP_START + aNode.getValue() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode) {
		return new MappingWrapper<>(INTEGER_LITERAL_EXPRESSION + 
				GROUP_START + aNode.getValue() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralMinValueExpr(IntegerLiteralMinValueExpr aNode) {
		return new MappingWrapper<>(INTEGER_LITERAL_MIN_VALUE_EXPRESSION + 
				GROUP_START + aNode.getValue() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForCharLiteralExpr(CharLiteralExpr aNode) {
		return new MappingWrapper<>(CHAR_LITERAL_EXPRESSION); // + GROUP_START + aNode.getValue() + GROUP_END);
	}

	@Override
	public MappingWrapper<String> getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode) {
		return new MappingWrapper<>(BOOLEAN_LITERAL_EXPRESSION + 
				GROUP_START + aNode.getValue() + GROUP_END);
	}

}
