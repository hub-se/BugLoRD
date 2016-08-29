package se.de.hu_berlin.informatik.astlmbuilder.reader;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;

import se.de.hu_berlin.informatik.astlmbuilder.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ThrowsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITokenMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ITokenMapperShort;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.SimpleNode2StringMapping;

public class ASTLMDeserializer {

	private final ITokenMapperShort<String, Integer> t_mapper = new SimpleNode2StringMapping<Integer>();
	public static final char startGroup = ITokenMapper.BIG_GROUP_START.charAt( 0 );
	public static final char endGroup = ITokenMapper.BIG_GROUP_END.charAt( 0 );

	/**
	 * Parses the given serialization for the keyword that indicates which type
	 * of node was serialized.
	 * 
	 * @param aSerializedNode
	 * @return The identifying keyword from the serialized node or null if the
	 *         string could not be parsed
	 */
	private String[] parseKeywordFromSeri(String aSerializedNode) {
		String[] result = new String[2]; // first is the keyword second is the rest
		
		// if the string is null or to short this method is not able to create a
		// node
		if (aSerializedNode == null || aSerializedNode.length() < 6) {
			return null;
		}

		int startIdx = aSerializedNode.indexOf(ITokenMapper.KEYWORD_SERIALIZE);

		// if there is no serialization keyword the string is malformed
		if (startIdx == -1) {
			return null;
		}
		
		// find the closing
		int bigCloseTag = aSerializedNode.lastIndexOf( ITokenMapper.BIG_GROUP_END );
		
		if( bigCloseTag == -1 ) {
			throw new IllegalArgumentException( "The serialization " + aSerializedNode + " had no valid closing tag after index " + startIdx );
		}

		int keywordEndIdx = bigCloseTag;
		
		// the end is not the closing group tag if this node had children which is indicated by a new group
		int childGroupStartTag = aSerializedNode.indexOf( ITokenMapper.GROUP_START, startIdx + 1 );
		
		if( childGroupStartTag != -1 ) {
			keywordEndIdx = childGroupStartTag;
		}

		result[0] = aSerializedNode.substring( startIdx + 1, keywordEndIdx );
		
		if ( childGroupStartTag != -1 ) {
			// there are children that can be cut out
			// this includes the start and end keyword for the child groups
			result[1] = aSerializedNode.substring( keywordEndIdx, bigCloseTag );
		} else {
			// no children, no cutting
			result[1] = null;
		}

		return result;
	}
	
	/**
	 * Searches for all top level child nodes in the given string and splits the serialization
	 * @param aSerializedNode
	 * @param startIdx
	 * @return A list with all child nodes of this serialization or null if something went wrong
	 */
	private List<String> getAllChildNodesFromSeri( String aSerializedNode ) {
		List<String> result = new ArrayList<String>();
		
		int depth = 0;
		int lastStart = 0;

		for ( int i = 0; i < aSerializedNode.length(); ++i ) {
			if ( aSerializedNode.charAt( i ) == startGroup ) {
				if ( depth == 0 ) {
					lastStart = i;
				}
				++depth;
			} else if ( aSerializedNode.charAt( i ) == endGroup ) {
				--depth;
				if( depth == 0 ) {
					result.add( aSerializedNode.substring( lastStart, i+1 ) );
				}
			}

		}
		
		return result.size() > 0 ? result : null;
	}

	/**
	 * Creates a new node object for a given serialized string
	 * 
	 * @param childDataStr
	 *            The keyword that the mapper used for the original node
	 * @return a node of the same type as the original one that got serialized
	 */
	public Node deserializeNode(String aSerializedString ) {

		// this name is awful but the result is the one keyword and maybe the child string...
		String[] parsedParts = parseKeywordFromSeri( aSerializedString );
		String keyword = parsedParts[0];
		String childDataStr = parsedParts[1];
		
		if( keyword == null ) {
			return null;
		}

		Node result = null;
		switch (keyword) {
		case (ITokenMapper.CONSTRUCTOR_DECLARATION):
			result = createConstructorDeclaration(childDataStr);
			break;
		case (ITokenMapper.INITIALIZER_DECLARATION):
			result = createInitializerDeclaration(childDataStr);
			break;
		case (ITokenMapper.ENUM_CONSTANT_DECLARATION):
			result = createEnumConstantDeclaration(childDataStr);
			break;
		case (ITokenMapper.VARIABLE_DECLARATION):
			result = createVariableDeclarator(childDataStr);
			break;
		case (ITokenMapper.ENUM_DECLARATION):
			result = createEnumDeclaration(childDataStr);
			break;
		case (ITokenMapper.ANNOTATION_DECLARATION):
			result = createAnnotationDeclaration(childDataStr);
			break;
		case (ITokenMapper.ANNOTATION_MEMBER_DECLARATION):
			result = createAnnotationMemberDeclaration(childDataStr);
			break;
		case (ITokenMapper.EMPTY_MEMBER_DECLARATION):
			result = createEmptyMemberDeclaration(childDataStr);
			break;
		case (ITokenMapper.EMPTY_TYPE_DECLARATION):
			result = createEmptyTypeDeclaration(childDataStr);
			break;
		case (ITokenMapper.WHILE_STATEMENT):
			result = createWhileStmt(childDataStr);
			break;
		case (ITokenMapper.TRY_STATEMENT):
			result = createTryStmt(childDataStr);
			break;
		case (ITokenMapper.THROW_STATEMENT):
			result = createThrowStmt(childDataStr);
			break;
		case (ITokenMapper.THROWS_STATEMENT):
			result = createThrowsStmt(childDataStr);
			break;
		case (ITokenMapper.SYNCHRONIZED_STATEMENT):
			result = createSynchronizedStmt(childDataStr);
			break;
		case (ITokenMapper.SWITCH_STATEMENT):
			result = createSwitchStmt(childDataStr);
			break;
		case (ITokenMapper.SWITCH_ENTRY_STATEMENT):
			result = createSwitchEntryStmt(childDataStr);
			break;
		case (ITokenMapper.RETURN_STATEMENT):
			result = createReturnStmt(childDataStr);
			break;
		case (ITokenMapper.LABELED_STATEMENT):
			result = createLabeledStmt(childDataStr);
			break;
		case (ITokenMapper.IF_STATEMENT):
			result = createIfStmt(childDataStr);
			break;
		case (ITokenMapper.ELSE_STATEMENT):
			result = createElseStmt(childDataStr);
			break;
		case (ITokenMapper.FOR_STATEMENT):
			result = createForStmt(childDataStr);
			break;
		case (ITokenMapper.FOR_EACH_STATEMENT):
			result = createForeachStmt(childDataStr);
			break;
		case (ITokenMapper.EXPRESSION_STATEMENT):
			result = createExpressionStmt(childDataStr);
			break;
		case (ITokenMapper.EXPLICIT_CONSTRUCTOR_STATEMENT):
			result = createExplicitConstructorInvocationStmt(childDataStr);
			break;
		case (ITokenMapper.EMPTY_STATEMENT):
			result = createEmptyStmt(childDataStr);
			break;
		case (ITokenMapper.DO_STATEMENT):
			result = createDoStmt(childDataStr);
			break;
		case (ITokenMapper.CONTINUE_STATEMENT):
			result = createContinueStmt(childDataStr);
			break;
		case (ITokenMapper.CATCH_CLAUSE_STATEMENT):
			result = createCatchClause(childDataStr);
			break;
		case (ITokenMapper.BLOCK_STATEMENT):
			result = createBlockStmt(childDataStr);
			break;
		case (ITokenMapper.VARIABLE_DECLARATION_ID):
			result = createVariableDeclaratorId(childDataStr);
			break;
		case (ITokenMapper.VARIABLE_DECLARATION_EXPRESSION):
			result = createVariableDeclarationExpr(childDataStr);
			break;
		case (ITokenMapper.TYPE_EXPRESSION):
			result = createTypeExpr(childDataStr);
			break;
		case (ITokenMapper.SUPER_EXPRESSION):
			result = createSuperExpr(childDataStr);
			break;
		case (ITokenMapper.QUALIFIED_NAME_EXPRESSION):
			result = createQualifiedNameExpr(childDataStr);
			break;
		case (ITokenMapper.NULL_LITERAL_EXPRESSION):
			result = createNullLiteralExpr(childDataStr);
			break;
		case (ITokenMapper.METHOD_REFERENCE_EXPRESSION):
			result = createMethodReferenceExpr(childDataStr);
			break;
		case (ITokenMapper.BODY_STMT):
			result = createBodyStmt(childDataStr);
			break;
		case (ITokenMapper.LONG_LITERAL_MIN_VALUE_EXPRESSION):
			result = createLongLiteralMinValueExpr(childDataStr);
			break;
		case (ITokenMapper.LAMBDA_EXPRESSION):
			result = createLambdaExpr(childDataStr);
			break;
		case (ITokenMapper.INTEGER_LITERAL_MIN_VALUE_EXPRESSION):
			result = createIntegerLiteralMinValueExpr(childDataStr);
			break;
		case (ITokenMapper.INSTANCEOF_EXPRESSION):
			result = createInstanceOfExpr(childDataStr);
			break;
		case (ITokenMapper.FIELD_ACCESS_EXPRESSION):
			result = createFieldAccessExpr(childDataStr);
			break;
		case (ITokenMapper.CONDITIONAL_EXPRESSION):
			result = createConditionalExpr(childDataStr);
			break;
		case (ITokenMapper.CLASS_EXPRESSION):
			result = createClassExpr(childDataStr);
			break;
		case (ITokenMapper.CAST_EXPRESSION):
			result = createCastExpr(childDataStr);
			break;
		case (ITokenMapper.ASSIGN_EXPRESSION):
			result = createAssignExpr(childDataStr);
			break;
		case (ITokenMapper.ARRAY_INIT_EXPRESSION):
			result = createArrayInitializerExpr(childDataStr);
			break;
		case (ITokenMapper.ARRAY_CREATE_EXPRESSION):
			result = createArrayCreationExpr(childDataStr);
			break;
		case (ITokenMapper.ARRAY_ACCESS_EXPRESSION):
			result = createArrayAccessExpr(childDataStr);
			break;
		case (ITokenMapper.PACKAGE_DECLARATION):
			result = createPackageDeclaration(childDataStr);
			break;
		case (ITokenMapper.IMPORT_DECLARATION):
			result = createImportDeclaration(childDataStr);
			break;
		case (ITokenMapper.FIELD_DECLARATION):
			result = createFieldDeclaration(childDataStr);
			break;
		case (ITokenMapper.CLASS_OR_INTERFACE_TYPE):
			result = createClassOrInterfaceType(childDataStr);
			break;
		case (ITokenMapper.CLASS_OR_INTERFACE_DECLARATION):
			result = createClassOrInterfaceDeclaration(childDataStr);
			break;
		case (ITokenMapper.CLASS_DECLARATION):
			result = createClassOrInterfaceDeclaration(childDataStr);
			break;
		case (ITokenMapper.INTERFACE_DECLARATION):
			result = createClassOrInterfaceDeclaration(childDataStr);
			break;
		case (ITokenMapper.EXTENDS_STATEMENT):
			result = createExtendsStmt(childDataStr);
			break;
		case (ITokenMapper.IMPLEMENTS_STATEMENT):
			result = createImplementsStmt(childDataStr);
			break;
		case (ITokenMapper.METHOD_DECLARATION):
			result = createMethodDeclaration(childDataStr);
			break;
		case (ITokenMapper.BINARY_EXPRESSION):
			result = createBinaryExpr(childDataStr);
			break;
		case (ITokenMapper.UNARY_EXPRESSION):
			result = createUnaryExpr(childDataStr);
			break;
		case (ITokenMapper.METHOD_CALL_EXPRESSION):
			result = createMethodCallExpr(childDataStr);
			break;
		// if a private method is called we handle it differently
		case (ITokenMapper.PRIVATE_METHOD_CALL_EXPRESSION):
			result = createPrivMethodCallExpr(childDataStr);
			break;
		case (ITokenMapper.NAME_EXPRESSION):
			result = createNameExpr(childDataStr);
			break;
		case (ITokenMapper.INTEGER_LITERAL_EXPRESSION):
			result = createIntegerLiteralExpr(childDataStr);
			break;
		case (ITokenMapper.DOUBLE_LITERAL_EXPRESSION):
			result = createDoubleLiteralExpr(childDataStr);
			break;
		case (ITokenMapper.STRING_LITERAL_EXPRESSION):
			result = createStringLiteralExpr(childDataStr);
			break;
		case (ITokenMapper.BOOLEAN_LITERAL_EXPRESSION):
			result = createBooleanLiteralExpr(childDataStr);
			break;
		case (ITokenMapper.CHAR_LITERAL_EXPRESSION):
			result = createCharLiteralExpr(childDataStr);
			break;
		case (ITokenMapper.LONG_LITERAL_EXPRESSION):
			result = createLongLiteralExpr(childDataStr);
			break;
		case (ITokenMapper.THIS_EXPRESSION):
			result = createThisExpr(childDataStr);
			break;
		case (ITokenMapper.BREAK):
			result = createBreakStmt(childDataStr);
			break;
		case (ITokenMapper.OBJ_CREATE_EXPRESSION):
			result = createObjectCreationExpr(childDataStr);
			break;
		case (ITokenMapper.MARKER_ANNOTATION_EXPRESSION):
			result = createMarkerAnnotationExpr(childDataStr);
			break;
		case (ITokenMapper.NORMAL_ANNOTATION_EXPRESSION):
			result = createNormalAnnotationExpr(childDataStr);
			break;
		case (ITokenMapper.SINGLE_MEMBER_ANNOTATION_EXPRESSION):
			result = createSingleMemberAnnotationExpr(childDataStr);
			break;

		case (ITokenMapper.PARAMETER):
			result = createParameter(childDataStr);
			break;
		case (ITokenMapper.MULTI_TYPE_PARAMETER):
			result = createMultiTypeParameter(childDataStr);
			break;
		case (ITokenMapper.ENCLOSED_EXPRESSION):
			result = createEnclosedExpr(childDataStr);
			break;
		case (ITokenMapper.ASSERT_STMT):
			result = createAssertStmt(childDataStr);
			break;
		case (ITokenMapper.MEMBER_VALUE_PAIR):
			result = createMemberValuePair(childDataStr);
			break;

		case (ITokenMapper.TYPE_DECLARATION_STATEMENT):
			result = createTypeDeclarationStmt(childDataStr);
			break;
		case (ITokenMapper.TYPE_REFERENCE):
			result = createReferenceType(childDataStr);
			break;
		case (ITokenMapper.TYPE_PRIMITIVE):
			result = createPrimitiveType(childDataStr);
			break;
		case (ITokenMapper.TYPE_UNION):
			result = createUnionType(childDataStr);
			break;
		case (ITokenMapper.TYPE_INTERSECTION):
			result = createIntersectionType(childDataStr);
			break;
		case (ITokenMapper.TYPE_PAR):
			result = createTypeParameter(childDataStr);
			break;
		case (ITokenMapper.TYPE_WILDCARD):
			result = createWildcardType(childDataStr);
			break;
		case (ITokenMapper.TYPE_VOID):
			result = createVoidType(childDataStr);
			break;

		default:
			result = null; // the unknown keyword will result in an unknown
							// token
		}

		return result;
	}
	
	/**
	 * All nodes need to create their children objects and check their properties afterwards
	 * @param aNode
	 * @param aSeriChildren
	 */
	private void deserializeAllChildren( Node aNode, String aSeriChildren ) {
		// check if there are children to add
		if( aSeriChildren != null ) {
			List<String> childSeris = getAllChildNodesFromSeri(aSeriChildren);
			
			for( String singleChild : childSeris ) {
				aNode.getChildrenNodes().add( deserializeNode( singleChild ) );
			}
		}
	}

	// (%$CNSTR_DEC[(%$PAR[(%$VAR_DEC_ID),(%$REF_TYPE)]),(%$PAR[(%$VAR_DEC_ID),(%$REF_TYPE)]),(%$PAR[(%$VAR_DEC_ID),(%$REF_TYPE)]),(%$PAR[(%$VAR_DEC_ID),(%$PRIM_TYPE)]),(%$PAR[(%$VAR_DEC_ID),(%$PRIM_TYPE)])])
	private ConstructorDeclaration createConstructorDeclaration(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO some nodes may be more than only children
			// these are the values that we may expect
//				    private int modifiers;
			
			for( Node n : result.getChildrenNodes() ) {
				if( n instanceof Parameter ) {
					if ( result.getParameters() == null ) {
						List<Parameter> pars = new ArrayList<Parameter>();
						result.setParameters( pars );
					}
					result.getParameters().add( (Parameter) n );
				} else if ( n instanceof NameExpr ) {
					result.setNameExpr( (NameExpr) n );
				} else if ( n instanceof BlockStmt ) {
					result.setBlock( (BlockStmt) n );
				}
			}
	
//				    private List<Parameter> parameters;
//				    private List<NameExpr> throws_;

		}
		
		return result;
	}

	private InitializerDeclaration createInitializerDeclaration(String aSerializedNode) {
		InitializerDeclaration result = new InitializerDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private EnumConstantDeclaration createEnumConstantDeclaration(String aSerializedNode) {
		EnumConstantDeclaration result = new EnumConstantDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private VariableDeclarator createVariableDeclarator(String aSerializedNode) {
		VariableDeclarator result = new VariableDeclarator();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private EnumDeclaration createEnumDeclaration(String aSerializedNode) {
		EnumDeclaration result = new EnumDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private AnnotationDeclaration createAnnotationDeclaration(String aSerializedNode) {
		AnnotationDeclaration result = new AnnotationDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private AnnotationMemberDeclaration createAnnotationMemberDeclaration(String aSerializedNode) {
		AnnotationMemberDeclaration result = new AnnotationMemberDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private EmptyMemberDeclaration createEmptyMemberDeclaration(String aSerializedNode) {
		EmptyMemberDeclaration result = new EmptyMemberDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private EmptyTypeDeclaration createEmptyTypeDeclaration(String aSerializedNode) {
		EmptyTypeDeclaration result = new EmptyTypeDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private WhileStmt createWhileStmt(String aSerializedNode) {
		WhileStmt result = new WhileStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private TryStmt createTryStmt(String aSerializedNode) {
		TryStmt result = new TryStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ThrowStmt createThrowStmt(String aSerializedNode) {
		ThrowStmt result = new ThrowStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	// This may never be used
	private ThrowsStmt createThrowsStmt(String aSerializedNode) {
		ThrowsStmt result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private SynchronizedStmt createSynchronizedStmt(String aSerializedNode) {
		SynchronizedStmt result = new SynchronizedStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private SwitchStmt createSwitchStmt(String aSerializedNode) {
		SwitchStmt result = new SwitchStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private SwitchEntryStmt createSwitchEntryStmt(String aSerializedNode) {
		SwitchEntryStmt result = new SwitchEntryStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ReturnStmt createReturnStmt(String aSerializedNode) {
		ReturnStmt result = new ReturnStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private LabeledStmt createLabeledStmt(String aSerializedNode) {
		LabeledStmt result = new LabeledStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private IfStmt createIfStmt(String aSerializedNode) {
		IfStmt result = new IfStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	// this may never be used
	private ElseStmt createElseStmt(String aSerializedNode) {
		ElseStmt result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ForStmt createForStmt(String aSerializedNode) {
		ForStmt result = new ForStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ForeachStmt createForeachStmt(String aSerializedNode) {
		ForeachStmt result = new ForeachStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ExpressionStmt createExpressionStmt(String aSerializedNode) {
		ExpressionStmt result = new ExpressionStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ExplicitConstructorInvocationStmt createExplicitConstructorInvocationStmt(String aSerializedNode) {
		ExplicitConstructorInvocationStmt result = new ExplicitConstructorInvocationStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private EmptyStmt createEmptyStmt(String aSerializedNode) {
		EmptyStmt result = new EmptyStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private DoStmt createDoStmt(String aSerializedNode) {
		DoStmt result = new DoStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ContinueStmt createContinueStmt(String aSerializedNode) {
		ContinueStmt result = new ContinueStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private CatchClause createCatchClause(String aSerializedNode) {
		CatchClause result = new CatchClause();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private BlockStmt createBlockStmt(String aSerializedNode) {
		BlockStmt result = new BlockStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private VariableDeclaratorId createVariableDeclaratorId(String aSerializedNode) {
		VariableDeclaratorId result = new VariableDeclaratorId();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private VariableDeclarationExpr createVariableDeclarationExpr(String aSerializedNode) {
		VariableDeclarationExpr result = new VariableDeclarationExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private TypeExpr createTypeExpr(String aSerializedNode) {
		TypeExpr result = new TypeExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private SuperExpr createSuperExpr(String aSerializedNode) {
		SuperExpr result = new SuperExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private QualifiedNameExpr createQualifiedNameExpr(String aSerializedNode) {
		QualifiedNameExpr result = new QualifiedNameExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private NullLiteralExpr createNullLiteralExpr(String aSerializedNode) {
		NullLiteralExpr result = new NullLiteralExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private MethodReferenceExpr createMethodReferenceExpr(String aSerializedNode) {
		MethodReferenceExpr result = new MethodReferenceExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	// this may never be used
	private BodyStmt createBodyStmt(String aSerializedNode) {
		BodyStmt result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private LongLiteralMinValueExpr createLongLiteralMinValueExpr(String aSerializedNode) {
		LongLiteralMinValueExpr result = new LongLiteralMinValueExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private LambdaExpr createLambdaExpr(String aSerializedNode) {
		LambdaExpr result = new LambdaExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private IntegerLiteralMinValueExpr createIntegerLiteralMinValueExpr(String aSerializedNode) {
		IntegerLiteralMinValueExpr result = new IntegerLiteralMinValueExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private InstanceOfExpr createInstanceOfExpr(String aSerializedNode) {
		InstanceOfExpr result = new InstanceOfExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private FieldAccessExpr createFieldAccessExpr(String aSerializedNode) {
		FieldAccessExpr result = new FieldAccessExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ConditionalExpr createConditionalExpr(String aSerializedNode) {
		ConditionalExpr result = new ConditionalExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ClassExpr createClassExpr(String aSerializedNode) {
		ClassExpr result = new ClassExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private CastExpr createCastExpr(String aSerializedNode) {
		CastExpr result = new CastExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private AssignExpr createAssignExpr(String aSerializedNode) {
		AssignExpr result = new AssignExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ArrayInitializerExpr createArrayInitializerExpr(String aSerializedNode) {
		ArrayInitializerExpr result = new ArrayInitializerExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ArrayCreationExpr createArrayCreationExpr(String aSerializedNode) {
		ArrayCreationExpr result = new ArrayCreationExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ArrayAccessExpr createArrayAccessExpr(String aSerializedNode) {
		ArrayAccessExpr result = new ArrayAccessExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private PackageDeclaration createPackageDeclaration(String aSerializedNode) {
		PackageDeclaration result = new PackageDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	// this may never be used
	private ImportDeclaration createImportDeclaration(String aSerializedNode) {
		ImportDeclaration result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private FieldDeclaration createFieldDeclaration(String aSerializedNode) {
		FieldDeclaration result = new FieldDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ClassOrInterfaceType createClassOrInterfaceType(String aSerializedNode) {
		ClassOrInterfaceType result = new ClassOrInterfaceType();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ClassOrInterfaceDeclaration createClassOrInterfaceDeclaration(String aSerializedNode) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private MethodDeclaration createMethodDeclaration(String aSerializedNode) {
		MethodDeclaration result = new MethodDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private BinaryExpr createBinaryExpr(String aSerializedNode) {
		BinaryExpr result = new BinaryExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private UnaryExpr createUnaryExpr(String aSerializedNode) {
		UnaryExpr result = new UnaryExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private MethodCallExpr createMethodCallExpr(String aSerializedNode) {
		MethodCallExpr result = new MethodCallExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private MethodCallExpr createPrivMethodCallExpr(String aSerializedNode) {
		MethodCallExpr result = new MethodCallExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private NameExpr createNameExpr(String aSerializedNode) {
		NameExpr result = new NameExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ConstructorDeclaration createIntegerLiteralExpr(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private DoubleLiteralExpr createDoubleLiteralExpr(String aSerializedNode) {
		DoubleLiteralExpr result = new DoubleLiteralExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private StringLiteralExpr createStringLiteralExpr(String aSerializedNode) {
		StringLiteralExpr result = new StringLiteralExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private BooleanLiteralExpr createBooleanLiteralExpr(String aSerializedNode) {
		BooleanLiteralExpr result = new BooleanLiteralExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private CharLiteralExpr createCharLiteralExpr(String aSerializedNode) {
		CharLiteralExpr result = new CharLiteralExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private LongLiteralExpr createLongLiteralExpr(String aSerializedNode) {
		LongLiteralExpr result = new LongLiteralExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ThisExpr createThisExpr(String aSerializedNode) {
		ThisExpr result = new ThisExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private BreakStmt createBreakStmt(String aSerializedNode) {
		BreakStmt result = new BreakStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ObjectCreationExpr createObjectCreationExpr(String aSerializedNode) {
		ObjectCreationExpr result = new ObjectCreationExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private MarkerAnnotationExpr createMarkerAnnotationExpr(String aSerializedNode) {
		MarkerAnnotationExpr result = new MarkerAnnotationExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private NormalAnnotationExpr createNormalAnnotationExpr(String aSerializedNode) {
		NormalAnnotationExpr result = new NormalAnnotationExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private SingleMemberAnnotationExpr createSingleMemberAnnotationExpr(String aSerializedNode) {
		SingleMemberAnnotationExpr result = new SingleMemberAnnotationExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private Parameter createParameter(String aSerializedNode) {
		Parameter result = new Parameter();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private MultiTypeParameter createMultiTypeParameter(String aSerializedNode) {
		MultiTypeParameter result = new MultiTypeParameter();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private EnclosedExpr createEnclosedExpr(String aSerializedNode) {
		EnclosedExpr result = new EnclosedExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private AssertStmt createAssertStmt(String aSerializedNode) {
		AssertStmt result = new AssertStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ConstructorDeclaration createMemberValuePair(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private TypeDeclarationStmt createTypeDeclarationStmt(String aSerializedNode) {
		TypeDeclarationStmt result = new TypeDeclarationStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private ReferenceType createReferenceType(String aSerializedNode) {
		ReferenceType result = new ReferenceType();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private PrimitiveType createPrimitiveType(String aSerializedNode) {
		PrimitiveType result = new PrimitiveType();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	// this may never be used
	private UnionType createUnionType(String aSerializedNode) {
		UnionType result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private IntersectionType createIntersectionType(String aSerializedNode) {
		// TODO fix the construction
		IntersectionType result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private TypeParameter createTypeParameter(String aSerializedNode) {
		TypeParameter result = new TypeParameter();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private WildcardType createWildcardType(String aSerializedNode) {
		WildcardType result = new WildcardType();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	private VoidType createVoidType(String aSerializedNode) {
		VoidType result = new VoidType();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	// this may never be used
	private ExtendsStmt createExtendsStmt(String aSerializedNode) {
		ExtendsStmt result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

	// this may never be used
	private ImplementsStmt createImplementsStmt(String aSerializedNode) {
		ImplementsStmt result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		// did the children deserialization create something?
		if ( result.getChildrenNodes() != null ) {
			// TODO do something with the nodes if necessary
		}
		
		return result;
	}

}
