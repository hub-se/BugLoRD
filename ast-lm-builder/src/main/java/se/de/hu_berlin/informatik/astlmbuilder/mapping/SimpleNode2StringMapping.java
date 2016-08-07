package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.Collection;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;

import se.de.hu_berlin.informatik.astlmbuilder.ElseStmt;

public class SimpleNode2StringMapping implements ITokenMapper<String> {
	
	// a collection of blacklisted private method names
	// the simple mapper makes no use of this
	public Collection<String> privMethodBL = null;
	
	/**
	 * Returns the mapping of the abstract syntax tree node to fit the language model
	 * @param aNode The node that should be mapped
	 * @return the string representation enclosed in a wrapper
	 */
	public MappingWrapper<String> getMappingForNode( Node aNode ) {
		
		if (aNode instanceof Expression) {
			// all expressions
			if ( aNode instanceof LiteralExpr ){
				if ( aNode instanceof NullLiteralExpr ){
					return new MappingWrapper<>(NULL_LITERAL_EXPRESSION);
				} else if ( aNode instanceof BooleanLiteralExpr ){
					return new MappingWrapper<>(BOOLEAN_LITERAL_EXPRESSION);
				} else if ( aNode instanceof StringLiteralExpr ){
					if ( aNode instanceof CharLiteralExpr ){
						return new MappingWrapper<>(CHAR_LITERAL_EXPRESSION);
					} else if ( aNode instanceof IntegerLiteralExpr ){
						if ( aNode instanceof IntegerLiteralMinValueExpr ){
							return new MappingWrapper<>(INTEGER_LITERAL_MIN_VALUE_EXPRESSION);
						} else {
							return new MappingWrapper<>(INTEGER_LITERAL_EXPRESSION);
						}
					} else if ( aNode instanceof LongLiteralExpr ){
						if ( aNode instanceof LongLiteralMinValueExpr ){
							return new MappingWrapper<>(LONG_LITERAL_MIN_VALUE_EXPRESSION);
						} else {
							return new MappingWrapper<>(LONG_LITERAL_EXPRESSION);
						}
					} else if ( aNode instanceof DoubleLiteralExpr ){
						return new MappingWrapper<>(DOUBLE_LITERAL_EXPRESSION);
					} else {
						return new MappingWrapper<>(STRING_LITERAL_EXPRESSION);
					}
				}
			} else if ( aNode instanceof ArrayAccessExpr ){
				return new MappingWrapper<>(ARRAY_ACCESS_EXPRESSION);
			} else if ( aNode instanceof ArrayCreationExpr ){
				return new MappingWrapper<>(ARRAY_CREATE_EXPRESSION);
			} else if ( aNode instanceof ArrayInitializerExpr ){
				return new MappingWrapper<>(ARRAY_INIT_EXPRESSION);
			} else if ( aNode instanceof AssignExpr ){
				return new MappingWrapper<>(ASSIGN_EXPRESSION);
			} else if ( aNode instanceof BinaryExpr ){
				return new MappingWrapper<>(BINARY_EXPRESSION + "(" + ((BinaryExpr)aNode).getOperator() + ")");
			} else if ( aNode instanceof CastExpr ){
				return new MappingWrapper<>(CAST_EXPRESSION);
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
				return new MappingWrapper<>(METHOD_CALL_EXPRESSION);
			} else if ( aNode instanceof MethodReferenceExpr ){
				return new MappingWrapper<>(METHOD_REFERENCE_EXPRESSION);
			} else if ( aNode instanceof ThisExpr ){
				return new MappingWrapper<>(THIS_EXPRESSION);
			} else if ( aNode instanceof EnclosedExpr ){
				return new MappingWrapper<>(ENCLOSED_EXPRESSION);
			}  else if ( aNode instanceof ObjectCreationExpr ){
				return new MappingWrapper<>(OBJ_CREATE_EXPRESSION);
			} else if ( aNode instanceof UnaryExpr ){
				return new MappingWrapper<>(UNARY_EXPRESSION + "(" + ((UnaryExpr)aNode).getOperator() + ")");
			} else if ( aNode instanceof SuperExpr ){
				return new MappingWrapper<>(SUPER_EXPRESSION);
			} else if ( aNode instanceof TypeExpr ){
				return new MappingWrapper<>(TYPE_EXPRESSION);
			} else if ( aNode instanceof VariableDeclarationExpr ){
				return new MappingWrapper<>(VARIABLE_DECLARATION_EXPRESSION);
			} else if ( aNode instanceof NameExpr ){
				if ( aNode instanceof QualifiedNameExpr ){
					return new MappingWrapper<>(QUALIFIED_NAME_EXPRESSION);
				} else {
					return new MappingWrapper<>(NAME_EXPRESSION);
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
			// all declarations (may all have annotations)
			if ( aNode instanceof ConstructorDeclaration ){
				return new MappingWrapper<>(CONSTRUCTOR_DECLARATION);
			} else if ( aNode instanceof InitializerDeclaration ){
				return new MappingWrapper<>(INITIALIZER_DECLARATION);
			} else if ( aNode instanceof FieldDeclaration ){
				return new MappingWrapper<>(FIELD_DECLARATION + "(" + ModifierMapper.getModifier(((FieldDeclaration)aNode).getModifiers()) + ")");	
			} else if ( aNode instanceof MethodDeclaration ){
				return new MappingWrapper<>(METHOD_DECLARATION);
			} else if ( aNode instanceof EnumConstantDeclaration ){
				return new MappingWrapper<>(ENUM_CONSTANT_DECLARATION);
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
			return new MappingWrapper<>(PACKAGE_DECLARATION);
		} else if ( aNode instanceof ImportDeclaration ){
			return new MappingWrapper<>(IMPORT_DECLARATION);
		} else if ( aNode instanceof TypeParameter ){
			return new MappingWrapper<>(TYPE_PAR);
		}
		
		else if ( aNode instanceof CatchClause ){
			return new MappingWrapper<>(CATCH_CLAUSE_STATEMENT);
		} else if ( aNode instanceof VariableDeclarator ){
			return new MappingWrapper<>(VARIABLE_DECLARATION);
		} else if ( aNode instanceof VariableDeclaratorId ){
			return new MappingWrapper<>(VARIABLE_DECLARATION_ID);
		} else if ( aNode instanceof MemberValuePair ){
			return new MappingWrapper<>(MEMBER_VALUE_PAIR);
		}
		
		// compilation unit
		else if ( aNode instanceof CompilationUnit) {
			return new MappingWrapper<>(COMPILATION_UNIT);
		}
		
		// this should be removed after testing i guess
		// >> I wouldn't remove it, since it doesn't hurt and constitutes a default value <<
		return new MappingWrapper<>(UNKNOWN + "(" + aNode.getClass().getSimpleName() + ")");
	}
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.astlmbuilder.ITokenMapper#getClosingToken(com.github.javaparser.ast.Node)
	 */
	@Override
	public String getClosingToken(Node aNode) {
		if (aNode == null) {
			return null;
		}

		if (aNode instanceof MethodDeclaration) {
			return CLOSING_MDEC;
		} else if (aNode instanceof ConstructorDeclaration) {
			return CLOSING_CNSTR;
		} else if (aNode instanceof IfStmt) {
			return CLOSING_IF;
		} else if (aNode instanceof WhileStmt) {
			return CLOSING_WHILE;
		} else if (aNode instanceof ForStmt) {
			return CLOSING_FOR;
		} else if (aNode instanceof TryStmt) {
			return CLOSING_TRY;
		} else if (aNode instanceof CatchClause) {
			return CLOSING_CATCH;
		} else if (aNode instanceof ForeachStmt) {
			return CLOSING_FOR_EACH;
		} else if (aNode instanceof DoStmt) {
			return CLOSING_DO;
		} else if (aNode instanceof SwitchStmt) {
			return SWITCH_DO;
		}

		return null;
	}

	@Override
	public void setPrivMethodBlackList(Collection<String> aBL) {
		privMethodBL = aBL;
	}

	@Override
	public void clearPrivMethodBlackList() {
		privMethodBL = null;
	}
}
