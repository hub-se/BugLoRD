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
			String result = "(";
			result += vars.size();
			for( VariableDeclarator singleType : vars ) {
				result += "," + getMappingForVariableDeclarator(singleType);
			}
			result += ")";
			return result;
		} else {
			return "null";
		}
	}
	
	private String getMappingForTypeList(List<? extends Type> types) {
		if( types != null ) {
			String result = "(";
			result += types.size();
			for( Type singleType : types ) {
				result += "," + getMappingForType(singleType);
			}
			result += ")";
			return result;
		} else {
			return "null";
		}
	}
	
//	private String getMappingForTypeParameterList(List<TypeParameter> typeParameters) {
//		if( typeParameters != null ) {
//			String result = "(";
//			result += typeParameters.size();
//			for( TypeParameter singleType : typeParameters ) {
//				result += "," + getMappingForTypeParameter(singleType);
//			}
//			result += ")";
//			return result;
//		} else {
//			return "null";
//		}
//	}
	
	private String getMappingForTypeArguments(TypeArguments typeArguments) {
		if( typeArguments != null && 
				typeArguments.getTypeArguments() != null &&
				typeArguments.getTypeArguments().size() > 0) {
			String result = "<";
			if (!typeArguments.isUsingDiamondOperator()) {
				result += typeArguments.getTypeArguments().size();
				for( Type singleType : typeArguments.getTypeArguments() ) {
					result += "," + getMappingForType(singleType);
				}
			}
			result += ">";
			return result;
		} else {
			return "null";
		}
	}
	
	private String getMappingForParameterList(List<Parameter> parameters) {
		if( parameters != null ) {
			String result = "(";
			result += parameters.size();
			for( Parameter singleType : parameters ) {
				result += "," + getMappingForParameter(singleType);
			}
			result += ")";
			return result;
		} else {
			return "null";
		}
	}
	
	private String getMappingForExpressionList(List<Expression> expressions) {
		if( expressions != null ) {
			String result = "(";
			result += expressions.size();
			for( Expression singleType : expressions ) {
				result += "," + getMappingForExpression(singleType);
			}
			result += ")";
			return result;
		} else {
			return "null";
		}
	}
	
	private String getMappingForBodyDeclarationList(List<BodyDeclaration> bodyDeclarations) {
		if( bodyDeclarations != null ) {
			String result = "(";
			result += bodyDeclarations.size();
			for( BodyDeclaration singleType : bodyDeclarations ) {
				result += "," + getMappingForBodyDeclaration(singleType);
			}
			result += ")";
			return result;
		} else {
			return "null";
		}
	}
	
	private String getMappingForClassOrInterfaceTypeList(List<ClassOrInterfaceType> types) {
		if( types != null ) {
			String result = "(";
			result += types.size();
			for( Type singleType : types ) {
				result += "," + getMappingForType(singleType);
			}
			result += ")";
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
		return new MappingWrapper<>(MEMBER_VALUE_PAIR + "(" + aNode.getName() + 
				"," + getMappingForExpression(aNode.getValue()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForTypeDeclarationStmt(TypeDeclarationStmt aNode) {
		return new MappingWrapper<>(TYPE_DECLARATION_STATEMENT + 
				"(" + getMappingForTypeDeclaration(aNode.getTypeDeclaration()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchEntryStmt(SwitchEntryStmt aNode) {
		return new MappingWrapper<>(SWITCH_ENTRY_STATEMENT + 
				"(" + getMappingForExpression(aNode.getLabel()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForUnionType(UnionType aNode) {
		return new MappingWrapper<>(TYPE_UNION + 
				"(" + getMappingForTypeList(aNode.getElements()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForIntersectionType(IntersectionType aNode) {
		return new MappingWrapper<>(TYPE_INTERSECTION + 
				"(" + getMappingForTypeList(aNode.getElements()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForLambdaExpr(LambdaExpr aNode) {
		return new MappingWrapper<>(LAMBDA_EXPRESSION + 
				"(" + (aNode.isParametersEnclosed() ? "true" : "false") + ")", 
				"(L," + getMappingForParameterList(aNode.getParameters()) + 
				"," + getMappingForStatement(aNode.getBody()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForInstanceOfExpr(InstanceOfExpr aNode) {
		return new MappingWrapper<>(INSTANCEOF_EXPRESSION + 
				"(" + getMappingForExpression(aNode.getExpr()) + 
				"," + getMappingForType(aNode.getType()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForConditionalExpr(ConditionalExpr aNode) {
		String result1 = CONDITIONAL_EXPRESSION;
		String result2 = "(COND," + getMappingForExpression(aNode.getCondition()) + 
				"," + getMappingForExpression(aNode.getThenExpr()) + 
				"," + getMappingForExpression(aNode.getElseExpr()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForObjectCreationExpr(ObjectCreationExpr aNode) {
		String result1 = OBJ_CREATE_EXPRESSION;
		String result2 = "(NEW,";
		
		if (aNode.getScope() != null) {
			result2 += aNode.getScope() + ".";
		}
		result2 += getMappingForClassOrInterfaceType(aNode.getType()) + 
				"," + getMappingForTypeList(aNode.getTypeArgs()) + 
				"," + getMappingForExpressionList(aNode.getArgs()) + 
				"," + getMappingForBodyDeclarationList(aNode.getAnonymousClassBody()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode) {
		//TODO boxed types?
		String result1 = CLASS_OR_INTERFACE_TYPE;
		String result2 = "(CIT,";
		
		if (aNode.getScope() != null) {
			result2 += aNode.getScope() + ".";
		}
		result2 += aNode.getName() + 
				"," + getMappingForTypeArguments(aNode.getTypeArguments()) + ")";
		
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
		String result2 = "(ARR_INIT," + getMappingForExpressionList(aNode.getValues()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForArrayCreationExpr(ArrayCreationExpr aNode) {
		String result1 = ARRAY_CREATE_EXPRESSION;
		String result2 = "(ARR_CREATE," + getMappingForType(aNode.getType()) + 
				"," + aNode.getArrayCount() + 
				"," + getMappingForExpressionList(aNode.getDimensions()) +
				(aNode.getInitializer() != null ? "," + getMappingForArrayInitializerExpr(aNode.getInitializer()) : "") + ")";

		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForArrayAccessExpr(ArrayAccessExpr aNode) {
		return new MappingWrapper<>(ARRAY_ACCESS_EXPRESSION + "(" + getMappingForExpression(aNode.getIndex()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForTypeParameter(TypeParameter aNode) {
		return new MappingWrapper<>(TYPE_PAR + "(" + getMappingForClassOrInterfaceTypeList(aNode.getTypeBound()) + ")");
	}
	
	@Override
	public MappingWrapper<String> getMappingForVariableDeclaratorId(VariableDeclaratorId aNode) {
		return new MappingWrapper<>(VARIABLE_DECLARATION_ID + "(" + aNode.getArrayCount() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarator(VariableDeclarator aNode) {
		if (((VariableDeclarator)aNode).getInit() != null) {
			return new MappingWrapper<>(VARIABLE_DECLARATION, 
					"(VD," + getMappingForVariableDeclaratorId(aNode.getId()) + 
					"," + getMappingForExpression(aNode.getInit()) + ")");
		}
		return new MappingWrapper<>(VARIABLE_DECLARATION, 
				"(VD," + getMappingForVariableDeclaratorId(aNode.getId()) + ")"); // + "(" + aNode.getId() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForImportDeclaration(ImportDeclaration aNode) {
		return new MappingWrapper<>(IMPORT_DECLARATION + "(" + aNode.getName() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForPackageDeclaration(PackageDeclaration aNode) {
		return new MappingWrapper<>(PACKAGE_DECLARATION + "(" + aNode.getPackageName() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForMultiTypeParameter(MultiTypeParameter aNode) {
		return new MappingWrapper<>(MULTI_TYPE_PARAMETER + "(" + getMappingForNode(aNode.getType()) + "," + ModifierMapper.getModifier(aNode.getModifiers()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForParameter(Parameter aNode) {
		return new MappingWrapper<>(PARAMETER + "(" + getMappingForNode(aNode.getType()) + "," + ModifierMapper.getModifier(aNode.getModifiers()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForEnumDeclaration(EnumDeclaration aNode) {
		return new MappingWrapper<>(ENUM_DECLARATION + "(" + ModifierMapper.getModifier(aNode.getModifiers()) + ")");
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
		
		String result2 = "(ED," + getMappingForExpressionList(aNode.getArgs()) + ")";

		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodDeclaration(MethodDeclaration aNode) {
		String result1 = METHOD_DECLARATION + "(" + ModifierMapper.getModifier(aNode.getModifiers()) + ")";
		
		String result2 = "(MD," + getMappingForType(aNode.getType()) + 
				"," + getMappingForParameterList(aNode.getParameters()) + ")";
		
		MappingWrapper<String> mapping = new MappingWrapper<>(result1, result2);
		
		mapping.addMappings(getMappingsForTypeParameterList(aNode.getTypeParameters()).getMappings());
		
		return mapping;
	}

	@Override
	public MappingWrapper<String> getMappingForFieldDeclaration(FieldDeclaration aNode) {
		String result1 = FIELD_DECLARATION + "(" + ModifierMapper.getModifier(aNode.getModifiers()) + ")";
		
		String result2 = "(FD," + getMappingForType(aNode.getType()) + 
				"," + getMappingForVariableDeclaratorList(aNode.getVariables()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForConstructorDeclaration(ConstructorDeclaration aNode) {
		String result1 = CONSTRUCTOR_DECLARATION + "(" + ModifierMapper.getModifier(aNode.getModifiers()) + ")";
		
		String result2 = "(CD," + getMappingForParameterList(aNode.getParameters()) + ")";
		
		MappingWrapper<String> mapping = new MappingWrapper<>(result1, result2);
		
		mapping.addMappings(getMappingsForTypeParameterList(aNode.getTypeParameters()).getMappings());
		
		return mapping;
	}

	@Override
	public MappingWrapper<String> getMappingForWhileStmt(WhileStmt aNode) {
		return new MappingWrapper<>(WHILE_STATEMENT,
				"(W," + getMappingForExpression(aNode.getCondition()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchStmt(SwitchStmt aNode) {
		return new MappingWrapper<>(SWITCH_STATEMENT + "(" + getMappingForExpression(aNode.getSelector()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForForStmt(ForStmt aNode) {
		String result1 = FOR_STATEMENT;
		String result2 = "(F_INIT," + getMappingForExpressionList(aNode.getInit()) + ")";
		String result3 = "(F_COMP," + getMappingForExpression(aNode.getCompare()) + ")";
		String result4 = "(F_UPD," + getMappingForExpressionList(aNode.getUpdate()) + ")";
		
		return new MappingWrapper<>(result1, result2, result3, result4);
	}

	@Override
	public MappingWrapper<String> getMappingForForeachStmt(ForeachStmt aNode) {
		String result1 = FOR_EACH_STATEMENT;
		String result2 = "(FE_VAR," + getMappingForVariableDeclarationExpr(aNode.getVariable()) + ")";
		String result3 = "(FE_ITER," + getMappingForExpression(aNode.getIterable()) + ")";
		
		return new MappingWrapper<>(result1, result2, result3);
	}

	@Override
	public MappingWrapper<String> getMappingForExplicitConstructorInvocationStmt(
			ExplicitConstructorInvocationStmt aNode) {
		String result1 = EXPLICIT_CONSTRUCTOR_STATEMENT;
		String result2 = "(";

		if (aNode.isThis()) {
			result2 += "this,";
		} else {
			result2 += "super,";
		}
		
		result2 += getMappingForExpressionList(aNode.getArgs()) + 
				"," + getMappingForTypeList(aNode.getTypeArgs()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForDoStmt(DoStmt aNode) {
		return new MappingWrapper<>(DO_STATEMENT,
				"(DO," + getMappingForExpression(aNode.getCondition()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForAssertStmt(AssertStmt aNode) {
		return new MappingWrapper<>(ASSERT_STMT,
				"(AT," + getMappingForExpression(aNode.getCheck())
				+ (aNode.getMessage() != null ? "," + getMappingForExpression(aNode.getMessage()) : "") 
				+ ")");
	}

	@Override
	public MappingWrapper<String> getMappingForReferenceType(ReferenceType aNode) {
		return new MappingWrapper<>(TYPE_REFERENCE + "(" + getMappingForType(aNode.getType()) + "," + aNode.getArrayCount() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForPrimitiveType(PrimitiveType aNode) {
		return new MappingWrapper<>(TYPE_PRIMITIVE + "(" + aNode.getType() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode) {
		String result1 = VARIABLE_DECLARATION_EXPRESSION + "(" + ModifierMapper.getModifier(aNode.getModifiers()) + ")";
		String result2 = "(VDE," + getMappingForType(aNode.getType()) + 
				"," + getMappingForVariableDeclaratorList(aNode.getVars()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodReferenceExpr(MethodReferenceExpr aNode) {
		String result1 = METHOD_REFERENCE_EXPRESSION;
		String result2 = "(MR,";
		
		if (aNode.getScope() != null) {
			result2 += aNode.getScope() + "::";
		}
		result2 += aNode.getIdentifier() + ")";
		
		MappingWrapper<String> mapping = new MappingWrapper<>(result1, result2);
		
		mapping.addMappings(getMappingsForTypeParameterList(aNode.getTypeParameters()).getMappings());
		
		return mapping;
	}

	@Override
	public MappingWrapper<String> getMappingForMethodCallExpr(MethodCallExpr aNode) {
		String result1 = METHOD_CALL_EXPRESSION;
		String result2 = "(MC,";

		if( privMethodBL.contains( aNode.getName() ) ) {
			result2 += PRIVATE_METHOD_CALL_EXPRESSION + ",";
		} else {
			if (aNode.getScope() != null) {
				result2 += aNode.getScope() + ".";
			}
			result2 += aNode.getName() + ",";
		}
		
		result2 += getMappingForExpressionList(aNode.getArgs()) + 
				"," + getMappingForTypeList(aNode.getTypeArgs()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForFieldAccessExpr(FieldAccessExpr aNode) {
		String result1 = FIELD_ACCESS_EXPRESSION;
		String result2 = "(FA,";
		
//		if ( aFieldAccessExpr.getScope() != null ) {
//			result2 += getMappingForNode(aFieldAccessExpr.getScope()) + ".";
//		}
		
		result2 += getMappingForNode(aNode.getFieldExpr()) + 
				"," + getMappingForTypeList(aNode.getTypeArgs()) + ")";
		
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
		return new MappingWrapper<>(TYPE_EXPRESSION + "(" + getMappingForType(aNode.getType()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForUnaryExpr(UnaryExpr aNode) {
		return new MappingWrapper<>(UNARY_EXPRESSION, 
				"(U," + aNode.getOperator() 
				+ "," + getMappingForExpression(aNode.getExpr()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForClassExpr(ClassExpr aNode) {
		return new MappingWrapper<>(CLASS_EXPRESSION + "(" + getMappingForType(aNode.getType()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForCastExpr(CastExpr aNode) {
		return new MappingWrapper<>(CAST_EXPRESSION,
				"(C," + getMappingForType(aNode.getType())
				+ "," + getMappingForExpression(aNode.getExpr()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForBinaryExpr(BinaryExpr aNode) {
		return new MappingWrapper<>(BINARY_EXPRESSION, 
				"(B," + getMappingForExpression(aNode.getLeft()) 
				+ "," + aNode.getOperator() 
				+ "," + getMappingForExpression(aNode.getRight()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForAssignExpr(AssignExpr aNode) {
		return new MappingWrapper<>(ASSIGN_EXPRESSION, 
				"(A," + getMappingForExpression(aNode.getTarget()) 
				+ "," + aNode.getOperator() 
				+ "," + getMappingForExpression(aNode.getValue()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForStringLiteralExpr(StringLiteralExpr aNode) {
		return new MappingWrapper<>(STRING_LITERAL_EXPRESSION); // + "(" + aNode.getValue() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode) {
		return new MappingWrapper<>(DOUBLE_LITERAL_EXPRESSION + "(" + aNode.getValue() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralExpr(LongLiteralExpr aNode) {
		return new MappingWrapper<>(LONG_LITERAL_EXPRESSION + "(" + aNode.getValue() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForLongLiteralMinValueExpr(LongLiteralMinValueExpr aNode) {
		return new MappingWrapper<>(LONG_LITERAL_MIN_VALUE_EXPRESSION + "(" + aNode.getValue() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode) {
		return new MappingWrapper<>(INTEGER_LITERAL_EXPRESSION + "(" + aNode.getValue() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForIntegerLiteralMinValueExpr(IntegerLiteralMinValueExpr aNode) {
		return new MappingWrapper<>(INTEGER_LITERAL_MIN_VALUE_EXPRESSION + "(" + aNode.getValue() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForCharLiteralExpr(CharLiteralExpr aNode) {
		return new MappingWrapper<>(CHAR_LITERAL_EXPRESSION); // + "(" + aNode.getValue() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode) {
		return new MappingWrapper<>(BOOLEAN_LITERAL_EXPRESSION + "(" + aNode.getValue() + ")");
	}
	
}
