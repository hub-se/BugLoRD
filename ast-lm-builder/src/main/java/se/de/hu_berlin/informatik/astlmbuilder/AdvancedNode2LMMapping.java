package se.de.hu_berlin.informatik.astlmbuilder;

import java.util.List;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
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
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralMinValueExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralMinValueExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
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
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

public class AdvancedNode2LMMapping implements TokenMapperIf {
	
	/**
	 * Returns the mapping of the abstract syntax tree node to fit the language model
	 * This variant handles method and constructor declarations differently and inspects
	 * method calls more closely.
	 * @param aNode The node that should be mapped
	 * @return the string representation
	 */
	public String getMappingForNode( Node aNode ) {
		
		// all declarations
		if( aNode instanceof PackageDeclaration ) {
			return PACKAGE_DECLARATION;
		} else if ( aNode instanceof ConstructorDeclaration ){
			return buildCDec( (ConstructorDeclaration) aNode );
		} else if ( aNode instanceof ImportDeclaration ){
			return IMPORT_DECLARATION;
		} else if ( aNode instanceof ClassOrInterfaceDeclaration ){
			return CLASS_DECLARATION;
		} else if ( aNode instanceof InitializerDeclaration ){
			return INITIALIZER_DECLARATION;
		} else if ( aNode instanceof MethodDeclaration ){
			return buildMDec( (MethodDeclaration) aNode );
		} else if ( aNode instanceof VariableDeclarator ){
			return VARIABLE_DECLARATION;
		}
		
		// all types
		else if ( aNode instanceof ClassOrInterfaceType ){			
			return CLASS_TYPE;
		} else if ( aNode instanceof IntersectionType ){			
			return TYPE_INTERSECTION;
		} else if ( aNode instanceof PrimitiveType ){
			return TYPE_PRIMITIVE + "(" + ((PrimitiveType)aNode).getType() + ")";
		} else if ( aNode instanceof ReferenceType ){
			return TYPE_REFERENCE;
		}  else if ( aNode instanceof VariableDeclaratorId ){
			return VARIABLE_DECLARATION_ID;
		}  else if ( aNode instanceof Parameter ){
			return PARAMETER + "(" + ModifierMapper.getModifier(((Parameter)aNode).getModifiers()) + ")";		
		} else if ( aNode instanceof FieldDeclaration ){
			return FIELD_DECLARATION + "(" + ModifierMapper.getModifier(((FieldDeclaration)aNode).getModifiers()) + ")";	
		} else if ( aNode instanceof UnionType ){
			return TYPE_UNION;
		} else if ( aNode instanceof VoidType ){
			return TYPE_VOID;
		} else if ( aNode instanceof TypeParameter ){
			return TYPE_PAR;
		} else if ( aNode instanceof WildcardType ){
			return TYPE_WILDCARD;
		}
		
		// all statements
		else if ( aNode instanceof AssertStmt ){
			return ASSERT_STMT;
		} else if ( aNode instanceof BlockStmt ){
			return BLOCK_STATEMENT; // think about ignoring this
		} else if ( aNode instanceof BreakStmt ){
			return BREAK;
		} else if ( aNode instanceof CatchClause ){
			return CATCH_CLAUSE_STATEMENT;
		} else if ( aNode instanceof ContinueStmt ){
			return CONTINUE_STATEMENT;
		} else if ( aNode instanceof DoStmt ){
			return DO_STATEMENT;
		} else if ( aNode instanceof EmptyStmt ){
			return EMPTY_STATEMENT; // hopefully there are only a few
		} else if ( aNode instanceof ExplicitConstructorInvocationStmt ){
			return EXPLICIT_CONSTRUCTOR_STATEMENT; // in case i add the constructors to the language model
		} else if ( aNode instanceof ExpressionStmt ){
			return EXPRESSION_STATEMENT; // is it a statement or an expression...
		} else if ( aNode instanceof ForeachStmt ){
			return FOR_EACH_STATEMENT;
		} else if ( aNode instanceof ForStmt ){
			return FOR_STATEMENT;
		} else if ( aNode instanceof IfStmt ){
			return IF_STATEMENT;
		} else if ( aNode instanceof LabeledStmt ){
			return LABELED_STATEMENT; // what is this supposed to be?
		} else if ( aNode instanceof ReturnStmt ){
			return RETURN_STATEMENT;
		} else if ( aNode instanceof SwitchEntryStmt ){
			return SWITCH_ENTRY_STATEMENT;
		} else if ( aNode instanceof SwitchStmt ){
			return SWITCH_STATEMENT;
		} else if ( aNode instanceof SynchronizedStmt ){
			return SYNCHRONIZED_STATEMENT;
		} else if ( aNode instanceof ThrowStmt ){
			return THROW_STATEMENT;
		} else if ( aNode instanceof TryStmt ){
			return TRY_STATEMENT;
		} else if ( aNode instanceof TypeDeclarationStmt ){
			return TYPE_DECLARATION_STATEMENT;
		} else if ( aNode instanceof WhileStmt ){
			return WHILE_STATEMENT;
		}
		
		// all expressions
		if ( aNode instanceof ArrayAccessExpr ){
			return ARRAY_ACCESS_EXPRESSION;
		} else if ( aNode instanceof ArrayCreationExpr ){
			return ARRAY_CREATE_EXPRESSION;
		} else if ( aNode instanceof ArrayInitializerExpr ){
			return ARRAY_INIT_EXPRESSION;
		} else if ( aNode instanceof AssignExpr ){
			return ASSIGN_EXPRESSION;
		} else if ( aNode instanceof BinaryExpr ){
			return BINARY_EXPRESSION + "(" + ((BinaryExpr)aNode).getOperator() + ")";
		} else if ( aNode instanceof CastExpr ){
			return CAST_EXPRESSION;
		} else if ( aNode instanceof ClassExpr ){
			return CLASS_EXPRESSION;
		} else if ( aNode instanceof ConditionalExpr ){
			return CONDITIONAL_EXPRESSION;
		} else if ( aNode instanceof FieldAccessExpr ){
			return FIELD_ACCESS_EXPRESSION;
		} else if ( aNode instanceof InstanceOfExpr ){
			return INSTANCEOF_EXPRESSION;
		} else if ( aNode instanceof LambdaExpr ){
			return LAMBDA_EXPRESSION;
		} else if ( aNode instanceof MethodCallExpr ){
			return METHOD_CALL_EXPRESSION;
		} else if ( aNode instanceof MethodReferenceExpr ){
			return METHOD_REFERENCE_EXPRESSION;
		} else if ( aNode instanceof NameExpr ){
			return NAME_EXPRESSION;
		} else if ( aNode instanceof NullLiteralExpr ){
			return NULL_LITERAL_EXPRESSION;
		} else if ( aNode instanceof IntegerLiteralExpr ){
			return INTEGER_LITERAL_EXPRESSION;
		} else if ( aNode instanceof IntegerLiteralMinValueExpr ){
			return INTEGER_LITERAL_MIN_VALUE_EXPRESSION;
		} else if ( aNode instanceof DoubleLiteralExpr ){
			return DOUBLE_LITERAL_EXPRESSION;
		} else if ( aNode instanceof StringLiteralExpr ){
			return STRING_LITERAL_EXPRESSION;
		} else if ( aNode instanceof BooleanLiteralExpr ){
			return BOOLEAN_LITERAL_EXPRESSION;
		} else if ( aNode instanceof CharLiteralExpr ){
			return CHAR_LITERAL_EXPRESSION;
		} else if ( aNode instanceof LongLiteralExpr ){
			return LONG_LITERAL_EXPRESSION;
		} else if ( aNode instanceof LongLiteralMinValueExpr ){
			return LONG_LITERAL_MIN_VALUE_EXPRESSION;
		} else if ( aNode instanceof ThisExpr ){
			return THIS_EXPRESSION;
		} else if ( aNode instanceof EnclosedExpr ){
			return ENCLOSED_EXPRESSION;
		}  else if ( aNode instanceof ObjectCreationExpr ){
			return OBJ_CREATE_EXPRESSION;
		} else if ( aNode instanceof UnaryExpr ){
			return UNARY_EXPRESSION + "(" + ((UnaryExpr)aNode).getOperator() + ")";
		} else if ( aNode instanceof QualifiedNameExpr ){
			return QUALIFIED_NAME_EXPRESSION;
		} else if ( aNode instanceof SuperExpr ){
			return SUPER_EXPRESSION;
		} else if ( aNode instanceof TypeExpr ){
			return TYPE_EXPRESSION;
		} else if ( aNode instanceof VariableDeclarationExpr ){
			return VARIABLE_DECLARATION_EXPRESSION;
		}
		
		// this should be removed after testing i guess
		return TYPE_UNKNOWN + "(" + aNode.getClass().getSimpleName() + ")";
	}
	
	/**
	 * Builds a token for the method declaration including some information about the
	 * return value, number of parameters and modifiers
	 * @param aMDec
	 * @return an advanced token string
	 */
	private String buildMDec( MethodDeclaration aMDec ) {
		String result = METHOD_DECLARATION + "(";
		
		// first argument is always the return type
		result += aMDec.getType();
		
		if( aMDec.getParameters() != null ) {
			// add some information regarding the parameters
			List<Parameter> pars = aMDec.getParameters();
			// first the number of parameters
			result += "," + pars.size();
			// afterwards the simple type of them
			for( Parameter singlePar : pars ) {
//				result += "," + mapObjectTypes( singlePar.getType() );
				result += "," + singlePar.getType();
			}
		}
		
		return result + ")";
	}
	
	/**
	 * Builds a token for the constructor declaration including some information about the
	 * return value, number of parameters and modifiers
	 * @param aCDec
	 * @return an advanced token string
	 */
	private String buildCDec( ConstructorDeclaration aCDec ) {
		String result = CONSTRUCTOR_DECLARATION + "(";
		
		if( aCDec.getParameters() != null ) {
			// add some information regarding the parameters
			List<Parameter> pars = aCDec.getParameters();
			// first the number of parameters
			result += "," + pars.size();
			// afterwards the simple type of them
			for( Parameter singlePar : pars ) {
//				result += "," + mapObjectTypes( singlePar.getType() );
				result += "," + singlePar.getType();
			}
		}
		
		return result + ")";
	}
	
//	/**
//	 * Maps the types of objects to a standard format
//	 * @param aType
//	 * @return
//	 */
//	private String mapObjectTypes( Type aType ) {
//		// TODO remove 
//		
//		if( aType instanceof PrimitiveType ) {
//			// those are already ready to use
//			return aType.toString();
//		} else if( aType instanceof ReferenceType ){
//			return TYPE_REFERENCE;
//		} else if( aType instanceof IntersectionType ){
//			return TYPE_INTERSECTION;
//		} else if( aType instanceof VoidType ){
//			return TYPE_VOID;
//		} else if( aType instanceof WildcardType ){
//			return TYPE_WILDCARD;
//		} else if( aType instanceof UnknownType ){
//			return "UKN_T";
//		}
//		
//		// well whatever
//		return aType.toString();
//	}
	
}
