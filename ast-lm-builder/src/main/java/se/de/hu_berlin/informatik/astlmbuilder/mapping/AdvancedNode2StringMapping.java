package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.List;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.TypeArguments;
import com.github.javaparser.ast.TypeParameter;
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
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralMinValueExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralMinValueExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;

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
			return "";
		}
	}
	
	private String getMappingForTypeList(List<Type> types) {
		if( types != null ) {
			String result = "(";
			result += types.size();
			for( Type singleType : types ) {
				result += "," + getMappingForType(singleType);
			}
			result += ")";
			return result;
		} else {
			return "";
		}
	}
	
	private String getMappingForTypeParameterList(List<TypeParameter> typeParameters) {
		if( typeParameters != null ) {
			String result = "(";
			result += typeParameters.size();
			for( TypeParameter singleType : typeParameters ) {
				result += "," + getMappingForTypeParameter(singleType);
			}
			result += ")";
			return result;
		} else {
			return "";
		}
	}
	
	private String getMappingForTypeArguments(TypeArguments typeArguments) {
		if( typeArguments != null && typeArguments.getTypeArguments() != null) {
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
			return "";
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
			return "";
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
			return "";
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
			return "";
		}
	}
	
	@Override
	public MappingWrapper<String> getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode) {
		//TODO boxed types?
		String result1 = CLASS_OR_INTERFACE_TYPE;
		String result2 = "(CIT,";
		
		if (aNode.getScope() != null) {
			result2 += aNode.getScope() + ".";
		}
		result2 += aNode.getName() + ",";
		
		result2 += getMappingForTypeArguments(aNode.getTypeArguments()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForTypeParameter(TypeParameter aNode) {
		return new MappingWrapper<>(TYPE_PAR);
	}
	
	@Override
	public MappingWrapper<String> getMappingForVariableDeclaratorId(VariableDeclaratorId aNode) {
		return new MappingWrapper<>(VARIABLE_DECLARATION_ID + "(" + aNode.getArrayCount() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarator(VariableDeclarator aNode) {
		if (((VariableDeclarator)aNode).getInit() != null) {
			return new MappingWrapper<>(VARIABLE_DECLARATION, "(VD," + getMappingForExpression(aNode.getInit()) + ")");
		}
		return new MappingWrapper<>(VARIABLE_DECLARATION); // + "(" + aNode.getId() + ")");
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
		String result1 = CLASS_DECLARATION;
		String result2 = "(CID,";
		
		result2 += getMappingForTypeParameterList(aNode.getTypeParameters()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode) {
		String result1 = ENUM_CONSTANT_DECLARATION;
		String result2 = "(ED";
		
		result2 += getMappingForExpressionList(aNode.getArgs()) + ")";

		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodDeclaration(MethodDeclaration aNode) {
		String result1 = METHOD_DECLARATION;
		String result2 = "(MD,";
		
		// first argument is always the return type
		result2 += aNode.getType() + ",";
		
		result2 += getMappingForParameterList(aNode.getParameters()) + ",";
		
		result2 += getMappingForTypeParameterList(aNode.getTypeParameters()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForFieldDeclaration(FieldDeclaration aNode) {
		return new MappingWrapper<>(FIELD_DECLARATION + "(" + ModifierMapper.getModifier(aNode.getModifiers()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForConstructorDeclaration(ConstructorDeclaration aNode) {
		String result1 = CONSTRUCTOR_DECLARATION;
		String result2 = "(CD,";
		
		result2 += getMappingForParameterList(aNode.getParameters()) + ",";
		
		result2 += getMappingForTypeParameterList(aNode.getTypeParameters()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForWhileStmt(WhileStmt aNode) {
		return new MappingWrapper<>(WHILE_STATEMENT,
				"(W," + getMappingForNode(aNode.getCondition()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForSwitchStmt(SwitchStmt aNode) {
		return new MappingWrapper<>(SWITCH_STATEMENT + "(" + getMappingForNode(aNode.getSelector()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForForStmt(ForStmt aNode) {
		String result1 = FOR_STATEMENT;
		String result2 = "(F,INIT:";
		
		result2 += getMappingForExpressionList(aNode.getInit()) + ",COMPARE:";
		
		result2 += getMappingForExpression(aNode.getCompare()) + ",UPDATE:";
		
		result2 += getMappingForExpressionList(aNode.getUpdate()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForForeachStmt(ForeachStmt aNode) {
		String result1 = FOR_EACH_STATEMENT;
		String result2 = "(FE,VAR:";
		
		result2 += getMappingForVariableDeclarationExpr(aNode.getVariable()) + ",ITER:";
		
		result2 += getMappingForExpression(aNode.getIterable()) + ")";
		
		return new MappingWrapper<>(result1, result2);
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
		
		result2 += getMappingForExpressionList(aNode.getArgs()) + ",";
		
		result2 += getMappingForTypeList(aNode.getTypeArgs()) + ")";
		
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
		return new MappingWrapper<>(TYPE_REFERENCE + "(" + aNode.getType() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForPrimitiveType(PrimitiveType aNode) {
		return new MappingWrapper<>(TYPE_PRIMITIVE + "(" + aNode.getType() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode) {
		String result1 = VARIABLE_DECLARATION_EXPRESSION;
		String result2 = "(VDE,";
		
		result2 += aNode.getType() + ",";
		
		result2 += getMappingForVariableDeclaratorList(aNode.getVars()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodReferenceExpr(MethodReferenceExpr aNode) {
		String result1 = METHOD_REFERENCE_EXPRESSION;
		String result2 = "(MR,";
		
		result2 += getMappingForTypeParameterList(aNode.getTypeParameters()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodCallExpr(MethodCallExpr aNode) {
		String result1 = METHOD_CALL_EXPRESSION;
		String result2 = "(MC,";

		if( privMethodBL.contains( aNode.getName() ) ) {
			result2 += PRIVATE_METHOD_CALL_EXPRESSION + ",";
		} else {
			result2 += getMethodNameWithScope(aNode) + ",";
		}
		
		result2 += getMappingForExpressionList(aNode.getArgs()) + ",";
		
		result2 += getMappingForTypeList(aNode.getTypeArgs()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForFieldAccessExpr(FieldAccessExpr aNode) {
		String result1 = FIELD_ACCESS_EXPRESSION;
		String result2 = "(FA,";
		
//		if ( aFieldAccessExpr.getScope() != null ) {
//			result2 += getMappingForNode(aFieldAccessExpr.getScope()) + ".";
//		}
		
		result2 += getMappingForNode(aNode.getFieldExpr()) + ",";
		
		result2 += getMappingForTypeList(aNode.getTypeArgs()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForExtendsStmt(ExtendsStmt aNode) {
		String result1 = EXTENDS_STATEMENT;
		String result2 = "(EXT,";
		
		result2 += getMappingForClassOrInterfaceTypeList(aNode.getExtends()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForImplementsStmt(ImplementsStmt aNode) {
		String result1 = IMPLEMENTS_STATEMENT;
		String result2 = "(IMPL,";
		
		result2 += getMappingForClassOrInterfaceTypeList(aNode.getImplements()) + ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForTypeExpr(TypeExpr aNode) {
		return new MappingWrapper<>(TYPE_EXPRESSION + "(" + aNode.getType() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForUnaryExpr(UnaryExpr aNode) {
		return new MappingWrapper<>(UNARY_EXPRESSION, 
				"(U," + aNode.getOperator() 
				+ "," + getMappingForExpression(aNode.getExpr()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForClassExpr(ClassExpr aNode) {
		return new MappingWrapper<>(CLASS_EXPRESSION + "(" + aNode.getType() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForCastExpr(CastExpr aNode) {
		return new MappingWrapper<>(CAST_EXPRESSION,
				"(C," + aNode.getType()
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
	
	
	
	/**
	 * Returns the method name with its scope (if it exists).
	 * @param aMCall
	 * a method call expression
	 * @return
	 * the method name with its scope (if it exists)
	 */
	private String getMethodNameWithScope(MethodCallExpr aMCall) {
		if (aMCall.getScope() != null) {
			return aMCall.getScope().toString() + "." + aMCall.getName();
		} else {
			return aMCall.getName();
		}
	}
	
}
