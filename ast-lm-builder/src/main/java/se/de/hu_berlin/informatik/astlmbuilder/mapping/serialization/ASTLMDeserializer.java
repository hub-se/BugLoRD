package se.de.hu_berlin.informatik.astlmbuilder.mapping.serialization;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
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
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
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
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.UnknownNode;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.dispatcher.IKeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.dispatcher.KeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.dispatcher.KeyWordDispatcherShort;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ThrowsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.parser.ITokenParser;

public class ASTLMDeserializer implements ITokenParser {

	private IKeyWordDispatcher kwDispatcher;
	
	/**
	 * The constructor initializes with the given keyword dispatcher
	 * @param kwDispatcher
	 */
	public ASTLMDeserializer(IKeyWordDispatcher kwDispatcher) {
		this.kwDispatcher = kwDispatcher;
	}
	
	/**
	 * In case the language model was created using the short keywords
	 */
	public void useShortKeywords() {
		kwDispatcher = new KeyWordDispatcherShort();
	}
	
	/**
	 * In case the language model was created using the long keywords
	 */
	public void useLongKeywords() {
		kwDispatcher = new KeyWordDispatcher();
	}
	
	/**
	 * All nodes need to create their children objects and check their properties afterwards
	 * @param aNode
	 * a node
	 * @param aSeriChildren
	 * serialized children?
	 */
	public void deserializeAllChildren( Node aNode, String aSeriChildren ) {
		// check if there are children to add
		if( aSeriChildren != null ) {
			List<String> childSeris = getAllChildNodesFromSeri(aSeriChildren);
			
			for( String singleChild : childSeris ) {
				aNode.getChildNodes().add( deserializeNode( singleChild ) );
			}
		}
	}
	
	/**
	 * Searches for all top level child nodes in the given string and splits the serialization
	 * @param aSerializedNode
	 * a serialized node as a string
	 * @return A list with all child nodes of this serialization or null if something went wrong
	 */
	public List<String> getAllChildNodesFromSeri( String aSerializedNode ) {
		List<String> result = new ArrayList<String>();
		
		int depth = 0;
		int lastStart = 0;

		for ( int i = 0; i < aSerializedNode.length(); ++i ) {
			if ( aSerializedNode.charAt( i ) == kwDispatcher.getGroupStart() ) {
				if ( depth == 0 ) {
					lastStart = i;
				}
				++depth;
			} else if ( aSerializedNode.charAt( i ) == kwDispatcher.getGroupEnd() ) {
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
	 * @param aSerializedString
	 * a serialized string
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

		return kwDispatcher.dispatchAndDesi( keyword, childDataStr, this );
	}
	
	/**
	 * Parses the given serialization for the keyword that indicates which type
	 * of node was serialized.
	 * 
	 * @param aSerializedNode
	 * a serialization string
	 * @return The identifying keyword from the serialized node or null if the
	 *         string could not be parsed
	 */
	public String[] parseKeywordFromSeri(String aSerializedNode) {
		String[] result = new String[2]; // first is the keyword second is the rest
		
		// if the string is null or to short this method is not able to create a
		// node
		if (aSerializedNode == null || aSerializedNode.length() < 6) {
			return null;
		}

		int startIdx = aSerializedNode.indexOf(kwDispatcher.getKeyWordSerialize());

		// if there is no serialization keyword the string is malformed
		if (startIdx == -1) {
			return null;
		}
		
		// find the closing
		//
		int bigCloseTag = aSerializedNode.lastIndexOf( kwDispatcher.getBigGroupEnd() );
		
		if( bigCloseTag == -1 ) {
			throw new IllegalArgumentException( "The serialization " + aSerializedNode + " had no valid closing tag after index " + startIdx );
		}

		int keywordEndIdx = bigCloseTag;
		
		// the end is not the closing group tag if this node had children which is indicated by a new group
		int childGroupStartTag = aSerializedNode.indexOf( kwDispatcher.getGroupStart(), startIdx + 1 );
		
		if( childGroupStartTag != -1 ) {
			keywordEndIdx = childGroupStartTag - 1;
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
	

	// (%$CNSTR_DEC[(%$PAR[(%$VAR_DEC_ID),(%$REF_TYPE)]),(%$PAR[(%$VAR_DEC_ID),(%$REF_TYPE)]),(%$PAR[(%$VAR_DEC_ID),(%$REF_TYPE)]),(%$PAR[(%$VAR_DEC_ID),(%$PRIM_TYPE)]),(%$PAR[(%$VAR_DEC_ID),(%$PRIM_TYPE)])])
	public ConstructorDeclaration createConstructorDeclaration(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public InitializerDeclaration createInitializerDeclaration(String aSerializedNode) {
		InitializerDeclaration result = new InitializerDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public EnumConstantDeclaration createEnumConstantDeclaration(String aSerializedNode) {
		EnumConstantDeclaration result = new EnumConstantDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public VariableDeclarator createVariableDeclarator(String aSerializedNode) {
		VariableDeclarator result = new VariableDeclarator();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public EnumDeclaration createEnumDeclaration(String aSerializedNode) {
		EnumDeclaration result = new EnumDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public AnnotationDeclaration createAnnotationDeclaration(String aSerializedNode) {
		AnnotationDeclaration result = new AnnotationDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public AnnotationMemberDeclaration createAnnotationMemberDeclaration(String aSerializedNode) {
		AnnotationMemberDeclaration result = new AnnotationMemberDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public WhileStmt createWhileStmt(String aSerializedNode) {
		WhileStmt result = new WhileStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public TryStmt createTryStmt(String aSerializedNode) {
		TryStmt result = new TryStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ThrowStmt createThrowStmt(String aSerializedNode) {
		ThrowStmt result = new ThrowStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	// This may never be used
	public ThrowsStmt createThrowsStmt(String aSerializedNode) {
		ThrowsStmt result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public SynchronizedStmt createSynchronizedStmt(String aSerializedNode) {
		SynchronizedStmt result = new SynchronizedStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public SwitchStmt createSwitchStmt(String aSerializedNode) {
		SwitchStmt result = new SwitchStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public SwitchEntryStmt createSwitchEntryStmt(String aSerializedNode) {
		SwitchEntryStmt result = new SwitchEntryStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ReturnStmt createReturnStmt(String aSerializedNode) {
		ReturnStmt result = new ReturnStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public LabeledStmt createLabeledStmt(String aSerializedNode) {
		LabeledStmt result = new LabeledStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public IfStmt createIfStmt(String aSerializedNode) {
		IfStmt result = new IfStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	// this may never be used
	public ElseStmt createElseStmt(String aSerializedNode) {
		ElseStmt result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ForStmt createForStmt(String aSerializedNode) {
		ForStmt result = new ForStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ForeachStmt createForeachStmt(String aSerializedNode) {
		ForeachStmt result = new ForeachStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ExpressionStmt createExpressionStmt(String aSerializedNode) {
		ExpressionStmt result = new ExpressionStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ExplicitConstructorInvocationStmt createExplicitConstructorInvocationStmt(String aSerializedNode) {
		ExplicitConstructorInvocationStmt result = new ExplicitConstructorInvocationStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public DoStmt createDoStmt(String aSerializedNode) {
		DoStmt result = new DoStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ContinueStmt createContinueStmt(String aSerializedNode) {
		ContinueStmt result = new ContinueStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public CatchClause createCatchClause(String aSerializedNode) {
		CatchClause result = new CatchClause();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public BlockStmt createBlockStmt(String aSerializedNode) {
		BlockStmt result = new BlockStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public VariableDeclarationExpr createVariableDeclarationExpr(String aSerializedNode) {
		VariableDeclarationExpr result = new VariableDeclarationExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public TypeExpr createTypeExpr(String aSerializedNode) {
		TypeExpr result = new TypeExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public SuperExpr createSuperExpr(String aSerializedNode) {
		SuperExpr result = new SuperExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public NullLiteralExpr createNullLiteralExpr(String aSerializedNode) {
		NullLiteralExpr result = new NullLiteralExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public MethodReferenceExpr createMethodReferenceExpr(String aSerializedNode) {
		MethodReferenceExpr result = new MethodReferenceExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	// this may never be used
	public BodyStmt createBodyStmt(String aSerializedNode) {
		BodyStmt result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public LambdaExpr createLambdaExpr(String aSerializedNode) {
		LambdaExpr result = new LambdaExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public InstanceOfExpr createInstanceOfExpr(String aSerializedNode) {
		InstanceOfExpr result = new InstanceOfExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public FieldAccessExpr createFieldAccessExpr(String aSerializedNode) {
		FieldAccessExpr result = new FieldAccessExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ConditionalExpr createConditionalExpr(String aSerializedNode) {
		ConditionalExpr result = new ConditionalExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ClassExpr createClassExpr(String aSerializedNode) {
		ClassExpr result = new ClassExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public CastExpr createCastExpr(String aSerializedNode) {
		CastExpr result = new CastExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public AssignExpr createAssignExpr(String aSerializedNode) {
		AssignExpr result = new AssignExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ArrayInitializerExpr createArrayInitializerExpr(String aSerializedNode) {
		ArrayInitializerExpr result = new ArrayInitializerExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ArrayCreationExpr createArrayCreationExpr(String aSerializedNode) {
		ArrayCreationExpr result = new ArrayCreationExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ArrayAccessExpr createArrayAccessExpr(String aSerializedNode) {
		ArrayAccessExpr result = new ArrayAccessExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public PackageDeclaration createPackageDeclaration(String aSerializedNode) {
		PackageDeclaration result = new PackageDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	// this may never be used
	public ImportDeclaration createImportDeclaration(String aSerializedNode) {
		ImportDeclaration result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public FieldDeclaration createFieldDeclaration(String aSerializedNode) {
		FieldDeclaration result = new FieldDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ClassOrInterfaceType createClassOrInterfaceType(String aSerializedNode) {
		ClassOrInterfaceType result = new ClassOrInterfaceType();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ClassOrInterfaceDeclaration createClassOrInterfaceDeclaration(String aSerializedNode) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public MethodDeclaration createMethodDeclaration(String aSerializedNode) {
		MethodDeclaration result = new MethodDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public BinaryExpr createBinaryExpr(String aSerializedNode) {
		BinaryExpr result = new BinaryExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public UnaryExpr createUnaryExpr(String aSerializedNode) {
		UnaryExpr result = new UnaryExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public MethodCallExpr createMethodCallExpr(String aSerializedNode) {
		MethodCallExpr result = new MethodCallExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public MethodCallExpr createPrivMethodCallExpr(String aSerializedNode) {
		MethodCallExpr result = new MethodCallExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public NameExpr createNameExpr(String aSerializedNode) {
		NameExpr result = new NameExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ConstructorDeclaration createIntegerLiteralExpr(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public DoubleLiteralExpr createDoubleLiteralExpr(String aSerializedNode) {
		DoubleLiteralExpr result = new DoubleLiteralExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public StringLiteralExpr createStringLiteralExpr(String aSerializedNode) {
		StringLiteralExpr result = new StringLiteralExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public BooleanLiteralExpr createBooleanLiteralExpr(String aSerializedNode) {
		BooleanLiteralExpr result = new BooleanLiteralExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public CharLiteralExpr createCharLiteralExpr(String aSerializedNode) {
		CharLiteralExpr result = new CharLiteralExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public LongLiteralExpr createLongLiteralExpr(String aSerializedNode) {
		LongLiteralExpr result = new LongLiteralExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ThisExpr createThisExpr(String aSerializedNode) {
		ThisExpr result = new ThisExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public BreakStmt createBreakStmt(String aSerializedNode) {
		BreakStmt result = new BreakStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ObjectCreationExpr createObjectCreationExpr(String aSerializedNode) {
		ObjectCreationExpr result = new ObjectCreationExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public MarkerAnnotationExpr createMarkerAnnotationExpr(String aSerializedNode) {
		MarkerAnnotationExpr result = new MarkerAnnotationExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public NormalAnnotationExpr createNormalAnnotationExpr(String aSerializedNode) {
		NormalAnnotationExpr result = new NormalAnnotationExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public SingleMemberAnnotationExpr createSingleMemberAnnotationExpr(String aSerializedNode) {
		SingleMemberAnnotationExpr result = new SingleMemberAnnotationExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public Parameter createParameter(String aSerializedNode) {
		Parameter result = new Parameter();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public EnclosedExpr createEnclosedExpr(String aSerializedNode) {
		EnclosedExpr result = new EnclosedExpr();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public AssertStmt createAssertStmt(String aSerializedNode) {
		AssertStmt result = new AssertStmt();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public ConstructorDeclaration createMemberValuePair(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public PrimitiveType createPrimitiveType(String aSerializedNode) {
		PrimitiveType result = new PrimitiveType();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	// this may never be used
	public UnionType createUnionType(String aSerializedNode) {
		UnionType result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public IntersectionType createIntersectionType(String aSerializedNode) {
		// TODO fix the construction
		IntersectionType result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public TypeParameter createTypeParameter(String aSerializedNode) {
		TypeParameter result = new TypeParameter();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public WildcardType createWildcardType(String aSerializedNode) {
		WildcardType result = new WildcardType();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	public VoidType createVoidType(String aSerializedNode) {
		VoidType result = new VoidType();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	// this may never be used
	public ExtendsStmt createExtendsStmt(String aSerializedNode) {
		ExtendsStmt result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	// this may never be used
	public ImplementsStmt createImplementsStmt(String aSerializedNode) {
		ImplementsStmt result = null;
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	@Override
	public UnknownNode createUnknown(String aSerializedNode) {
		return new UnknownNode();
	}
	
	@Override
	public UnknownType createUnknownType(String aSerializedNode) {
		return new UnknownType();
	}

	@Override
	public ClassOrInterfaceDeclaration createClassDeclaration(String aSerializedNode) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration();
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	@Override
	public ClassOrInterfaceDeclaration createInterfaceDeclaration(String aSerializedNode) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration();
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	@Override
	public Name createName(String aSerializedNode) {
		Name result = new Name();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	@Override
	public SimpleName createSimpleName(String aSerializedNode) {
		SimpleName result = new SimpleName();
		
		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );
		
		return result;
	}

	@Override
	public LocalClassDeclarationStmt createLocalClassDeclarationStmt(String aSerializedNode) {
		LocalClassDeclarationStmt result = new LocalClassDeclarationStmt();

		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );

		return result;
	}

	@Override
	public ArrayType createArrayType(String aSerializedNode) {
		ArrayType result = null;

		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );

		return result;
	}

	@Override
	public ArrayCreationLevel createArrayCreationLevel(String aSerializedNode) {
		ArrayCreationLevel result = new ArrayCreationLevel();

		// check if there are children to add
		deserializeAllChildren( result, aSerializedNode );

		return result;
	}

}
