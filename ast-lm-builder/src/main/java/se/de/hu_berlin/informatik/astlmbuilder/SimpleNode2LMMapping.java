package se.de.hu_berlin.informatik.astlmbuilder;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;

public class SimpleNode2LMMapping implements TokenMapperIf {
	
	/**
	 * Returns the mapping of the abstract syntax tree node to fit the language model
	 * @param aNode The node that should be mapped
	 * @return the string representation
	 */
	public String getMappingForNode( Node aNode ) {
		
		// all declarations
		// TODO maybe add more information to all of these tokens
		if( aNode instanceof PackageDeclaration ) {
			return PACKAGE_DECLARATION;
		} else if ( aNode instanceof ConstructorDeclaration ){
			// TODO add more information to this token
			return CONSTRUCTOR_DECLARATION;
		} else if ( aNode instanceof ImportDeclaration ){
			return IMPORT_DECLARATION;
		} else if ( aNode instanceof ClassOrInterfaceDeclaration ){
			return CLASS_DECLARATION;
		} else if ( aNode instanceof InitializerDeclaration ){
			return INITIALIZER_DECLARATION;
		} else if ( aNode instanceof MethodDeclaration ){
			// TODO add more information to this token
			return METHOD_DECLARATION;
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
	
}
