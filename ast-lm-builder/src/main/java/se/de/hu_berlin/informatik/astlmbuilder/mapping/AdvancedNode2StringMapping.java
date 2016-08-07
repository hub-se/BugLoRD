package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.List;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
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
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;

public class AdvancedNode2StringMapping extends SimpleNode2StringMapping {
	
	@Override
	public MappingWrapper<String> getMappingForVariableDeclaratorId(VariableDeclaratorId aNode) {
		return new MappingWrapper<>(VARIABLE_DECLARATION_ID + "(" + aNode.getArrayCount() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForVariableDeclarator(VariableDeclarator aNode) {
		if (((VariableDeclarator)aNode).getInit() != null) {
			return new MappingWrapper<>(VARIABLE_DECLARATION, "(VD," + getMappingForNode(aNode.getInit()) + ")");
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
		String result2 = "(CID";
		
		if( aNode.getTypeParameters() != null ) {
			// add some information regarding the arguments
			List<TypeParameter> args = aNode.getTypeParameters();
			// first the number of arguments
			result2 += "," + args.size();
			// afterwards the arguments themselves
			for( TypeParameter singleArg : args ) {
				result2 += "," + getMappingForNode(singleArg);
			}
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode) {
		String result1 = ENUM_CONSTANT_DECLARATION;
		String result2 = "(ED";
		
		if( aNode.getArgs() != null ) {
			// add some information regarding the arguments
			List<Expression> args = aNode.getArgs();
			// first the number of arguments
			result2 += "," + args.size();
			// afterwards the arguments themselves
			for( Expression singleArg : args ) {
				result2 += "," + getMappingForNode(singleArg);
			}
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodDeclaration(MethodDeclaration aNode) {
		String result1 = METHOD_DECLARATION;
		String result2 = "(MD,";
		
		// first argument is always the return type
		result2 += aNode.getType();
		
		if( aNode.getParameters() != null ) {
			// add some information regarding the parameters
			List<Parameter> pars = aNode.getParameters();
			// first the number of parameters
			result2 += "," + pars.size();
			// afterwards the simple type of them
			for( Parameter singlePar : pars ) {
				//result2 += "," + singlePar.getType();
				result2 += "," + getMappingForNode(singlePar);
			}
		}
		
		if( aNode.getTypeParameters() != null ) {
			// add some information regarding the parameters
			List<TypeParameter> pars = aNode.getTypeParameters();
			// first the number of parameters
			result2 += "," + pars.size();
			// afterwards the simple type of them
			for( TypeParameter singlePar : pars ) {
				result2 += "," + getMappingForNode(singlePar);
			}
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForFieldDeclaration(FieldDeclaration aNode) {
		return new MappingWrapper<>(FIELD_DECLARATION + "(" + ModifierMapper.getModifier(aNode.getModifiers()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForConstructorDeclaration(ConstructorDeclaration aNode) {
		String result1 = CONSTRUCTOR_DECLARATION;
		String result2 = "(CD";
		
		if( aNode.getParameters() != null ) {
			// add some information regarding the parameters
			List<Parameter> pars = aNode.getParameters();
			// first the number of parameters
			result2 += "," + pars.size();
			// afterwards the simple type of them
			for( Parameter singlePar : pars ) {
				//result2 += "," + singlePar.getType();
				result2 += "," + getMappingForNode(singlePar);
			}
		}
		
		if( aNode.getTypeParameters() != null ) {
			// add some information regarding the parameters
			List<TypeParameter> pars = aNode.getTypeParameters();
			// first the number of parameters
			result2 += "," + pars.size();
			// afterwards the simple type of them
			for( TypeParameter singlePar : pars ) {
				result2 += "," + getMappingForNode(singlePar);
			}
		}
		
		result2 += ")";
		
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
		String result2 = "(F";
		
		if( aNode.getInit() != null ) {
			List<Expression> args = aNode.getInit();
			result2 += ",init(" + args.size();
			for( Expression singleArg : args ) {
				result2 += "," + getMappingForNode(singleArg);
			}
			result2 += ")";
		}
		
		if( aNode.getCompare() != null ) {
			result2 += ",comp(" + getMappingForNode(aNode.getCompare()) + ")";
		}
		
		if( aNode.getUpdate() != null ) {
			List<Expression> args = aNode.getUpdate();
			result2 += ",update(" + args.size();
			for( Expression singleArg : args ) {
				result2 += "," + getMappingForNode(singleArg);
			}
			result2 += ")";
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForForeachStmt(ForeachStmt aNode) {
		String result1 = FOR_EACH_STATEMENT;
		String result2 = "(FE";
		
		if( aNode.getVariable() != null ) {
			result2 += ",var(" + getMappingForNode(aNode.getVariable()) + ")";
		}
		
		if( aNode.getIterable() != null ) {
			result2 += ",iter(" + getMappingForNode(aNode.getIterable()) + ")";
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForExplicitConstructorInvocationStmt(
			ExplicitConstructorInvocationStmt aNode) {
		String result1 = EXPLICIT_CONSTRUCTOR_STATEMENT;
		String result2 = "(";

		if (aNode.isThis()) {
			result2 += "this";
		} else {
			result2 += "super";
		}
		
		if( aNode.getArgs() != null ) {
			List<Expression> args = aNode.getArgs();
			result2 += "," + args.size();
			for( Expression singleArg : args ) {
				result2 += "," + getMappingForNode(singleArg);
			}
		}
		
		if( aNode.getTypeArgs() != null ) {
			List<Type> args = aNode.getTypeArgs();
			result2 += "," + args.size();
			for( Type singleArg : args ) {
				result2 += "," + getMappingForNode(singleArg);
			}
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForDoStmt(DoStmt aNode) {
		return new MappingWrapper<>(DO_STATEMENT,
				"(DO," + getMappingForNode(aNode.getCondition()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForAssertStmt(AssertStmt aNode) {
		return new MappingWrapper<>(ASSERT_STMT,
				"(AT," + getMappingForNode(aNode.getCheck())
				+ (aNode.getMessage() != null ? "," + getMappingForNode(aNode.getMessage()) : "") 
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
		
		result2 += aNode.getType();
		
		if( aNode.getVars() != null ) {
			List<VariableDeclarator> args = aNode.getVars();
			result2 += "," + args.size();
			for( VariableDeclarator singleArg : args ) {
				result2 += "," + getMappingForNode(singleArg);
			}
			result2 += ")";
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodReferenceExpr(MethodReferenceExpr aNode) {
		String result1 = METHOD_REFERENCE_EXPRESSION;
		String result2 = "(MR,";
		
		if( aNode.getTypeParameters() != null ) {
			List<TypeParameter> args = aNode.getTypeParameters();
			result2 += "," + args.size();
			for( TypeParameter singleArg : args ) {
				result2 += "," + getMappingForNode(singleArg);
			}
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForMethodCallExpr(MethodCallExpr aNode) {
		String result1 = METHOD_CALL_EXPRESSION;
		String result2 = "(MC,";

		if( privMethodBL.contains( aNode.getName() ) ) {
			result2 += PRIVATE_METHOD_CALL_EXPRESSION;
		} else {
			result2 += getMethodNameWithScope(aNode);
		}
		
		if( aNode.getArgs() != null ) {
			List<Expression> args = aNode.getArgs();
			result2 += "," + args.size();
			for( Expression singleArg : args ) {
				result2 += "," + getMappingForNode(singleArg);
			}
		}
		
		if( aNode.getTypeArgs() != null ) {
			List<Type> args = aNode.getTypeArgs();
			result2 += "," + args.size();
			for( Type singleArg : args ) {
				result2 += "," + getMappingForNode(singleArg);
			}
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	@Override
	public MappingWrapper<String> getMappingForFieldAccessExpr(FieldAccessExpr aNode) {
		String result1 = FIELD_ACCESS_EXPRESSION;
		String result2 = "(FA,";
		
//		if ( aFieldAccessExpr.getScope() != null ) {
//			result2 += getMappingForNode(aFieldAccessExpr.getScope()) + ".";
//		}
		
		result2 += getMappingForNode(aNode.getFieldExpr());
		
		if( aNode.getTypeArgs() != null ) {
			List<Type> args = aNode.getTypeArgs();
			result2 += "," + args.size();
			for( Type singleArg : args ) {
				result2 += "," + getMappingForNode(singleArg);
			}
		}
		
		result2 += ")";
		
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
				+ "," + getMappingForNode(aNode.getExpr()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForClassExpr(ClassExpr aNode) {
		return new MappingWrapper<>(CLASS_EXPRESSION + "(" + aNode.getType() + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForCastExpr(CastExpr aNode) {
		return new MappingWrapper<>(CAST_EXPRESSION,
				"(C," + aNode.getType()
				+ "," + getMappingForNode(aNode.getExpr()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForBinaryExpr(BinaryExpr aNode) {
		return new MappingWrapper<>(BINARY_EXPRESSION, 
				"(B," + getMappingForNode(aNode.getLeft()) 
				+ "," + aNode.getOperator() 
				+ "," + getMappingForNode(aNode.getRight()) + ")");
	}

	@Override
	public MappingWrapper<String> getMappingForAssignExpr(AssignExpr aNode) {
		return new MappingWrapper<>(ASSIGN_EXPRESSION, 
				"(A," + getMappingForNode(aNode.getTarget()) 
				+ "," + aNode.getOperator() 
				+ "," + getMappingForNode(aNode.getValue()) + ")");
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
