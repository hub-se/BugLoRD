package se.de.hu_berlin.informatik.astlmbuilder;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;

public class Node2LMMapping {

	public static final String CONSTRUCTOR_DECLARATION = "CNSTR_DEC";
	public static final String INITIALIZER_DECLARATION = "INIT_DEC";
	public static final String VARIABLE_DECLARATION = "VAR_DEC";
	public static final String UNION_TYPE = "UNION_TYPE";
	public static final String INTERSECTION_TYPE = "INTERSECT_TYPE";
	public static final String WHILE_STATEMENT = "WHILE";
	public static final String TYPE_DECLARATION_STATEMENT = "TYPE_DEC";
	public static final String TRY_STATEMENT = "TRY";
	public static final String THROW_STATEMENT = "THROW";
	public static final String SYNCHRONIZED_STATEMENT = "SYNC";
	public static final String SWITCH_STATEMENT = "SWITCH";
	public static final String SWITCH_ENTRY_STATEMENT = "SWITCH_ENTRY";
	public static final String RETURN_STATEMENT = "RETURN";
	public static final String LABELED_STATEMENT = "LABELED";
	public static final String IF_STATEMENT = "IF";
	public static final String FOR_STATEMENT = "FOR";
	public static final String FOR_EACH_STATEMENT = "FOR_EACH";
	public static final String EXPRESSION_STATEMENT = "EXPR_STMT";
	public static final String EXPLICIT_CONSTRUCTOR_STATEMENT = "EXPL_CONSTR";
	public static final String EMPTY_STATEMENT = "EMPTY";
	public static final String DO_STATEMENT = "DO";
	public static final String CONTINUE_STATEMENT = "CONTINUE";
	public static final String CATCH_CLAUSE_STATEMENT = "CATCH";
	public static final String BLOCK_STATEMENT = "BLOCK";
	public static final String VARIABLE_DECLARATION_ID = "VAR_DEC_ID";
	public static final String VARIABLE_DECLARATION_EXPRESSION = "VAR_DEC_EXPR";
	public static final String TYPE_EXPRESSION = "TYPE_EXPR";
	public static final String SUPER_EXPRESSION = "SUPER";
	public static final String QUALIFIED_NAME_EXPRESSION = "QUALIFIED_NAME";
	public static final String NULL_LITERAL_EXPRESSION = "NULL_LIT";
	public static final String METHOD_REFERENCE_EXPRESSION = "M_REF";
	public static final String LONG_LITERAL_MIN_VALUE_EXPRESSION = "LONG_LIT_MIN";
	public static final String LAMBDA_EXPRESSION = "LAMBDA";
	public static final String INTEGER_LITERAL_MIN_VALUE_EXPRESSION = "INT_LIT_MIN";
	public static final String INSTANCEOF_EXPRESSION = "INSTANCEOF";
	public static final String FIELD_ACCESS_EXPRESSION = "FIELD_ACC";
	public static final String CONDITIONAL_EXPRESSION = "CONDITION";
	public static final String CLASS_EXPRESSION = "CLASS";
	public static final String CAST_EXPRESSION = "CAST";
	public static final String ASSIGN_EXPRESSION = "ASSIGN";
	public static final String ARRAY_INIT_EXPRESSION = "INIT_ARR";
	public static final String ARRAY_CREATE_EXPRESSION = "CREATE_ARR";
	public static final String ARRAY_ACCESS_EXPRESSION = "ARR_ACC";
	public static final String TYPE_UNKNOWN = "T_UNKNOWN";
	public static final String PACKAGE_DECLARATION = "P_DEC";
	public static final String IMPORT_DECLARATION = "IMP_DEC";
	public static final String FIELD_DECLARATION = "FIELD_DEC";
	public static final String JAVA_DOC_COMMENT = "COMMENT";
	public static final String CLASS_TYPE = "CLASS_TYPE";
	public static final String CLASS_DECLARATION = "CLASS_DEC";
	public static final String METHOD_DECLARATION = "M_DEC";
	public static final String REFERENCE_TYPE = "REF_TYPE";
	public static final String PRIMITIVE_TYPE = "PRIM_TYPE";
	public static final String BINARY_EXPRESSION = "BIN_EXPR";
	public static final String UNARY_EXPRESSION = "UNARY_EXPR";
	public static final String METHOD_CALL_EXPRESSION = "M_CALL";
	public static final String NAME_EXPRESSION = "NAME_EXPR";
	public static final String INTEGER_LITERAL_EXPRESSION = "INT_LIT";
	public static final String DOUBLE_LITERAL_EXPRESSION = "DOUBLE_LIT";
	public static final String STRING_LITERAL_EXPRESSION = "STR_LIT";
	public static final String BOOLEAN_LITERAL_EXPRESSION = "BOOL_LIT";
	public static final String CHAR_LITERAL_EXPRESSION = "CHAR_LIT";
	public static final String LONG_LITERAL_EXPRESSION = "LONG_LIT";
	public static final String THIS_EXPRESSION = "THIS";
	public static final String VOID = "VOID";
	public static final String BREAK = "BREAK";
	public static final String OBJ_CREATE_EXPRESSION = "CREATE_OBJ";
	public static final String PARAMETER = "PAR"; 
	public static final String TYPE_PAR = "TYPE_PAR"; 
	public static final String TYPE_WILDCARD = "TYPE_WILDCARD"; 
	public static final String ENCLOSED_EXPRESSION = "ENCLOSED";
	public static final String ASSERT_STMT = "ASSERT";
	
	/**
	 * Returns the mapping of the abstract syntax tree node to fit the language model
	 * @param aNode The node that should be mapped
	 * @return the string representation
	 */
	public static String getMappingForNode( Node aNode ) {
		
		// all declarations
		if( aNode instanceof PackageDeclaration ) {
			return PACKAGE_DECLARATION;
		} else if ( aNode instanceof ConstructorDeclaration ){
			return CONSTRUCTOR_DECLARATION;
		} else if ( aNode instanceof ImportDeclaration ){
			return IMPORT_DECLARATION;
		} else if ( aNode instanceof ClassOrInterfaceDeclaration ){
			return CLASS_DECLARATION;
		} else if ( aNode instanceof InitializerDeclaration ){
			return INITIALIZER_DECLARATION;
		} else if ( aNode instanceof MethodDeclaration ){
			return METHOD_DECLARATION;
		} else if ( aNode instanceof VariableDeclarator ){
			return VARIABLE_DECLARATION;
		}
		
		// all types
		else if ( aNode instanceof ClassOrInterfaceType ){			
			return CLASS_TYPE;
		} else if ( aNode instanceof IntersectionType ){			
			return INTERSECTION_TYPE;
		} else if ( aNode instanceof PrimitiveType ){
			return PRIMITIVE_TYPE + "(" + ((PrimitiveType)aNode).getType() + ")";
		} else if ( aNode instanceof ReferenceType ){
			return REFERENCE_TYPE;
		}  else if ( aNode instanceof VariableDeclaratorId ){
			return VARIABLE_DECLARATION_ID;
		}  else if ( aNode instanceof Parameter ){
			return PARAMETER + "(" + ModifierMapper.getModifier(((Parameter)aNode).getModifiers()) + ")";		
		} else if ( aNode instanceof FieldDeclaration ){
			return FIELD_DECLARATION + "(" + ModifierMapper.getModifier(((FieldDeclaration)aNode).getModifiers()) + ")";	
		} else if ( aNode instanceof UnionType ){
			return UNION_TYPE;
		} else if ( aNode instanceof VoidType ){
			return VOID;
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
	
}
