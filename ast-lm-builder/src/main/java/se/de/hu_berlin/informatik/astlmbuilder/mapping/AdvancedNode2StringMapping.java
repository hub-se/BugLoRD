package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.List;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BaseParameter;
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
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
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
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralMinValueExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
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
import com.github.javaparser.ast.expr.QualifiedNameExpr;
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
import com.github.javaparser.ast.stmt.Statement;
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

import se.de.hu_berlin.informatik.astlmbuilder.ElseStmt;

public class AdvancedNode2StringMapping extends SimpleNode2StringMapping {
	
	/**
	 * Returns the mapping of the abstract syntax tree node to fit the language model
	 * This variant handles method and constructor declarations differently and inspects
	 * method calls more closely.
	 * @param aNode the node that should be mapped
	 * @return the string representation enclosed in a wrapper
	 */
	public MappingWrapper<String> getMappingForNode( Node aNode ) {
		
		if (aNode instanceof Expression) {
			// all expressions
			if ( aNode instanceof LiteralExpr ){
				if ( aNode instanceof NullLiteralExpr ){
					return new MappingWrapper<>(NULL_LITERAL_EXPRESSION);
				} else if ( aNode instanceof BooleanLiteralExpr ){
					return new MappingWrapper<>(BOOLEAN_LITERAL_EXPRESSION + "(" + ((BooleanLiteralExpr)aNode).getValue() + ")");
				} else if ( aNode instanceof StringLiteralExpr ){
					if ( aNode instanceof CharLiteralExpr ){
						return new MappingWrapper<>(CHAR_LITERAL_EXPRESSION); // + "(" + ((BooleanLiteralExpr)aNode).getValue() + ")");
					} else if ( aNode instanceof IntegerLiteralExpr ){
						if ( aNode instanceof IntegerLiteralMinValueExpr ){
							return new MappingWrapper<>(INTEGER_LITERAL_MIN_VALUE_EXPRESSION + "(" + ((IntegerLiteralMinValueExpr)aNode).getValue() + ")");
						} else {
							return new MappingWrapper<>(INTEGER_LITERAL_EXPRESSION + "(" + ((IntegerLiteralExpr)aNode).getValue() + ")");
						}
					} else if ( aNode instanceof LongLiteralExpr ){
						if ( aNode instanceof LongLiteralMinValueExpr ){
							return new MappingWrapper<>(LONG_LITERAL_MIN_VALUE_EXPRESSION + "(" + ((LongLiteralMinValueExpr)aNode).getValue() + ")");
						} else {
							return new MappingWrapper<>(LONG_LITERAL_EXPRESSION + "(" + ((LongLiteralExpr)aNode).getValue() + ")");
						}
					} else if ( aNode instanceof DoubleLiteralExpr ){
						return new MappingWrapper<>(DOUBLE_LITERAL_EXPRESSION + "(" + ((DoubleLiteralExpr)aNode).getValue() + ")");
					} else {
						return new MappingWrapper<>(STRING_LITERAL_EXPRESSION); // + "(" + ((StringLiteralExpr)aNode).getValue() + ")");
					}
				}
			} else if ( aNode instanceof ArrayAccessExpr ){
				return new MappingWrapper<>(ARRAY_ACCESS_EXPRESSION);
			} else if ( aNode instanceof ArrayCreationExpr ){
				return new MappingWrapper<>(ARRAY_CREATE_EXPRESSION);
			} else if ( aNode instanceof ArrayInitializerExpr ){
				return new MappingWrapper<>(ARRAY_INIT_EXPRESSION);
			} else if ( aNode instanceof AssignExpr ){
				return new MappingWrapper<>(ASSIGN_EXPRESSION + "(" + ((AssignExpr)aNode).getOperator() + ")");
			} else if ( aNode instanceof CastExpr ){
				return new MappingWrapper<>(CAST_EXPRESSION + "(" + ((CastExpr)aNode).getType() + ")");
			} else if ( aNode instanceof BinaryExpr ){
				return new MappingWrapper<>(BINARY_EXPRESSION , "(B," + ((BinaryExpr)aNode).getOperator() + ")");
			} else if ( aNode instanceof ClassExpr ){
				return new MappingWrapper<>(CLASS_EXPRESSION);
			} else if ( aNode instanceof ConditionalExpr ){
				return new MappingWrapper<>(CONDITIONAL_EXPRESSION);
			} else if ( aNode instanceof FieldAccessExpr ){
				return new MappingWrapper<>(FIELD_ACCESS_EXPRESSION);
			} else if ( aNode instanceof InstanceOfExpr ){
				return new MappingWrapper<>(INSTANCEOF_EXPRESSION);
			} else if ( aNode instanceof LambdaExpr ){
				return new MappingWrapper<>(LAMBDA_EXPRESSION);
			} else if ( aNode instanceof MethodCallExpr ){
				return buildMCall( (MethodCallExpr) aNode );
			} else if ( aNode instanceof MethodReferenceExpr ){
				return new MappingWrapper<>(METHOD_REFERENCE_EXPRESSION);
			} else if ( aNode instanceof ThisExpr ){
				return new MappingWrapper<>(THIS_EXPRESSION);
			} else if ( aNode instanceof EnclosedExpr ){
				return new MappingWrapper<>(ENCLOSED_EXPRESSION);
			}  else if ( aNode instanceof ObjectCreationExpr ){
				return new MappingWrapper<>(OBJ_CREATE_EXPRESSION);
			} else if ( aNode instanceof UnaryExpr ){
				return new MappingWrapper<>(UNARY_EXPRESSION, "(U" + ((UnaryExpr)aNode).getOperator() + ")");
			} else if ( aNode instanceof SuperExpr ){
				return new MappingWrapper<>(SUPER_EXPRESSION);
			} else if ( aNode instanceof TypeExpr ){
				return new MappingWrapper<>(TYPE_EXPRESSION + "(" + ((TypeExpr)aNode).getType() + ")");
			} else if ( aNode instanceof VariableDeclarationExpr ){
				return new MappingWrapper<>(VARIABLE_DECLARATION_EXPRESSION);
			} else if ( aNode instanceof NameExpr ){
				if ( aNode instanceof QualifiedNameExpr ){
					return new MappingWrapper<>(QUALIFIED_NAME_EXPRESSION);
				} else {
					return new MappingWrapper<>(NAME_EXPRESSION); // + "(" + ((NameExpr)aNode).getName() + ")");
				}
			} else if ( aNode instanceof AnnotationExpr ){
				if ( aNode instanceof MarkerAnnotationExpr ){
					return new MappingWrapper<>(MARKER_ANNOTATION_EXPRESSION);
				} else if ( aNode instanceof NormalAnnotationExpr ){
					return new MappingWrapper<>(NORMAL_ANNOTATION_EXPRESSION);
				} else if ( aNode instanceof SingleMemberAnnotationExpr ){
					return new MappingWrapper<>(SINGLE_MEMBER_ANNOTATION_EXPRESSION);
				}
			}
		} else if (aNode instanceof Type) {
			// all types
			if ( aNode instanceof ClassOrInterfaceType ){			
				return new MappingWrapper<>(CLASS_TYPE);
			} else if ( aNode instanceof IntersectionType ){			
				return new MappingWrapper<>(TYPE_INTERSECTION);
			} else if ( aNode instanceof PrimitiveType ){
				return new MappingWrapper<>(TYPE_PRIMITIVE + "(" + ((PrimitiveType)aNode).getType() + ")");
			} else if ( aNode instanceof ReferenceType ){
				return new MappingWrapper<>(TYPE_REFERENCE);
			} else if ( aNode instanceof UnionType ){
				return new MappingWrapper<>(TYPE_UNION);
			} else if ( aNode instanceof UnknownType ){
				return new MappingWrapper<>(TYPE_UNKNOWN);
			} else if ( aNode instanceof VoidType ){
				return new MappingWrapper<>(TYPE_VOID);
			} else if ( aNode instanceof WildcardType ){
				return new MappingWrapper<>(TYPE_WILDCARD);
			}
		} else if (aNode instanceof Statement) {
			// all statements
			if ( aNode instanceof AssertStmt ){
				return new MappingWrapper<>(ASSERT_STMT);
			} else if ( aNode instanceof BlockStmt ){
				return new MappingWrapper<>(BLOCK_STATEMENT); // think about ignoring this
			} else if ( aNode instanceof BreakStmt ){
				return new MappingWrapper<>(BREAK);
			} else if ( aNode instanceof ContinueStmt ){
				return new MappingWrapper<>(CONTINUE_STATEMENT);
			} else if ( aNode instanceof DoStmt ){
				return new MappingWrapper<>(DO_STATEMENT);
			} else if ( aNode instanceof EmptyStmt ){
				return new MappingWrapper<>(EMPTY_STATEMENT); // hopefully there are only a few
			} else if ( aNode instanceof ExplicitConstructorInvocationStmt ){
				return new MappingWrapper<>(EXPLICIT_CONSTRUCTOR_STATEMENT);
			} else if ( aNode instanceof ExpressionStmt ){
				return new MappingWrapper<>(EXPRESSION_STATEMENT); // is it a statement or an expression...
			} else if ( aNode instanceof ForeachStmt ){
				return new MappingWrapper<>(FOR_EACH_STATEMENT);
			} else if ( aNode instanceof ForStmt ){
				return new MappingWrapper<>(FOR_STATEMENT);
			} else if ( aNode instanceof IfStmt ){
				return new MappingWrapper<>(IF_STATEMENT);
			} else if ( aNode instanceof ElseStmt ){
				return new MappingWrapper<>(ELSE_STATEMENT);
			} else if ( aNode instanceof LabeledStmt ){
				return new MappingWrapper<>(LABELED_STATEMENT); // what is this supposed to be?
			} else if ( aNode instanceof ReturnStmt ){
				return new MappingWrapper<>(RETURN_STATEMENT);
			} else if ( aNode instanceof SwitchEntryStmt ){
				return new MappingWrapper<>(SWITCH_ENTRY_STATEMENT);
			} else if ( aNode instanceof SwitchStmt ){
				return new MappingWrapper<>(SWITCH_STATEMENT);
			} else if ( aNode instanceof SynchronizedStmt ){
				return new MappingWrapper<>(SYNCHRONIZED_STATEMENT);
			} else if ( aNode instanceof ThrowStmt ){
				return new MappingWrapper<>(THROW_STATEMENT);
			} else if ( aNode instanceof TryStmt ){
				return new MappingWrapper<>(TRY_STATEMENT);
			} else if ( aNode instanceof TypeDeclarationStmt ){
				return new MappingWrapper<>(TYPE_DECLARATION_STATEMENT);
			} else if ( aNode instanceof WhileStmt ){
				return new MappingWrapper<>(WHILE_STATEMENT);
			}
		} else if (aNode instanceof BodyDeclaration) {
			// all declarations
			if ( aNode instanceof ConstructorDeclaration ){
				return buildCDec( (ConstructorDeclaration) aNode );
			} else if ( aNode instanceof InitializerDeclaration ){
				return new MappingWrapper<>(INITIALIZER_DECLARATION);
			} else if ( aNode instanceof FieldDeclaration ){
				return new MappingWrapper<>(FIELD_DECLARATION + "(" + ModifierMapper.getModifier(((FieldDeclaration)aNode).getModifiers()) + ")");	
			} else if ( aNode instanceof MethodDeclaration ){
				return buildMDec( (MethodDeclaration) aNode );
			} else if ( aNode instanceof EnumConstantDeclaration ){
				return buildEnumDec( (EnumConstantDeclaration) aNode );
			} else if ( aNode instanceof AnnotationMemberDeclaration ){
				return new MappingWrapper<>(ANNOTATION_MEMBER_DECLARATION);
			}  else if ( aNode instanceof EmptyMemberDeclaration ){
				return new MappingWrapper<>(EMPTY_MEMBER_DECLARATION);
			}else if (aNode instanceof TypeDeclaration) {
				if (aNode instanceof AnnotationDeclaration) {
					return new MappingWrapper<>(ANNOTATION_DECLARATION);
				} else if ( aNode instanceof ClassOrInterfaceDeclaration ){
					return new MappingWrapper<>(CLASS_DECLARATION);
				} else if ( aNode instanceof EmptyTypeDeclaration ){
					return new MappingWrapper<>(EMPTY_TYPE_DECLARATION);
				} else if ( aNode instanceof EnumDeclaration ){
					return new MappingWrapper<>(ENUM_DECLARATION);
				}
			}
		} else if (aNode instanceof Comment) {
			// all comments
			if ( aNode instanceof LineComment) {
				return new MappingWrapper<>(LINE_COMMENT);
			} else if ( aNode instanceof BlockComment) {
				return new MappingWrapper<>(BLOCK_COMMENT);
			} else if ( aNode instanceof JavadocComment) {
				return new MappingWrapper<>(JAVADOC_COMMENT);
			}
		} else if (aNode instanceof BaseParameter) {
			if ( aNode instanceof Parameter ){
				return new MappingWrapper<>(PARAMETER + "(" + ModifierMapper.getModifier(((Parameter)aNode).getModifiers()) + ")");		
			} else if ( aNode instanceof MultiTypeParameter ){
				return new MappingWrapper<>(MULTI_TYPE_PARAMETER);	
			}
		}

		else if( aNode instanceof PackageDeclaration ) {
			return new MappingWrapper<>(PACKAGE_DECLARATION + "(" + ((PackageDeclaration)aNode).getPackageName() + ")");
		} else if ( aNode instanceof ImportDeclaration ){
			return new MappingWrapper<>(IMPORT_DECLARATION + "(" + ((ImportDeclaration)aNode).getName() + ")");
		} else if ( aNode instanceof TypeParameter ){
			return new MappingWrapper<>(TYPE_PAR);
		}
		
		else if ( aNode instanceof CatchClause ){
			return new MappingWrapper<>(CATCH_CLAUSE_STATEMENT);
		} else if ( aNode instanceof VariableDeclarator ){
			if (((VariableDeclarator)aNode).getInit() != null) {
				return new MappingWrapper<>(VARIABLE_DECLARATION, "(VD," + getMappingForNode(((VariableDeclarator)aNode).getInit()) + ")");
			}
			return new MappingWrapper<>(VARIABLE_DECLARATION); // + "(" + ((VariableDeclarator)aNode).getId() + ")");
		} else if ( aNode instanceof VariableDeclaratorId ){
			return new MappingWrapper<>(VARIABLE_DECLARATION_ID);
		} else if ( aNode instanceof MemberValuePair ){
			return new MappingWrapper<>(MEMBER_VALUE_PAIR);
		}

		return super.getMappingForNode(aNode);
	}
	
	/**
	 * Builds a token for the method call including some information about the
	 * method name, number of arguments and their types
	 * @param aMCall
	 * @return an advanced token string
	 */
	private MappingWrapper<String> buildMCall( MethodCallExpr aMCall ) {
		String result1 = METHOD_CALL_EXPRESSION;
		String result2 = "(MC,";
		
		// first argument is the name
		if( privMethodBL.contains( aMCall.getName() ) ) {
			result2 += PRIVATE_METHOD_CALL_EXPRESSION;
		} else {
			result2 += getMethodNameWithScope(aMCall);
		}
		
		if( aMCall.getArgs() != null ) {
			// add some information regarding the arguments
			List<Expression> args = aMCall.getArgs();
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
	
	/**
	 * Builds a token for the method declaration including some information about the
	 * return value, number of parameters and modifiers
	 * @param aMDec
	 * @return an advanced token string
	 */
	private MappingWrapper<String> buildMDec( MethodDeclaration aMDec ) {
		String result1 = METHOD_DECLARATION;
		String result2 = "(MD,";
		
		// first argument is always the return type
		result2 += aMDec.getType();
		
		if( aMDec.getParameters() != null ) {
			// add some information regarding the parameters
			List<Parameter> pars = aMDec.getParameters();
			// first the number of parameters
			result2 += "," + pars.size();
			// afterwards the simple type of them
			for( Parameter singlePar : pars ) {
				result2 += "," + singlePar.getType();
			}
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}
	
	/**
	 * Builds a token for the constructor declaration including some information about the
	 * return value, number of parameters and modifiers
	 * @param aCDec
	 * @return an advanced token string
	 */
	private MappingWrapper<String> buildCDec( ConstructorDeclaration aCDec ) {
		String result1 = CONSTRUCTOR_DECLARATION;
		String result2 = "(CD";
		
		if( aCDec.getParameters() != null ) {
			// add some information regarding the parameters
			List<Parameter> pars = aCDec.getParameters();
			// first the number of parameters
			result2 += "," + pars.size();
			// afterwards the simple type of them
			for( Parameter singlePar : pars ) {
				result2 += "," + singlePar.getType();
			}
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	/**
	 * Builds a token for the enum declaration including some information about the
	 * return value, number of parameters and modifiers
	 * @param aEnumDec
	 * @return an advanced token string
	 */
	private MappingWrapper<String> buildEnumDec( EnumConstantDeclaration aEnumDec ) {
		String result1 = ENUM_CONSTANT_DECLARATION;
		String result2 = "(ED";
		
		if( aEnumDec.getArgs() != null ) {
			// add some information regarding the arguments
			List<Expression> args = aEnumDec.getArgs();
			// first the number of arguments
			result2 += "," + args.size();
			// afterwards the arguments themselves
			for( Expression singleArg : args ) {
//				result += "," + mapObjectTypes( singlePar.getType() );
				result2 += "," + getMappingForNode(singleArg);
			}
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}
	
}
