package se.de.hu_berlin.informatik.astlmbuilder.reader;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
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
import com.github.javaparser.ast.expr.IntegerLiteralMinValueExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralMinValueExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
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

import se.de.hu_berlin.informatik.astlmbuilder.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.ThrowsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.IKeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.KeyWordConstants;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.KeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.ModifierMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.shortKW.KeyWordDispatcherShort;

public class ASTLMAbstractionDeserializer implements IASTLMDesirializer {

	public IKeyWordDispatcher kwDispatcher = new KeyWordDispatcher(); // the one using the long key words is the default here
	
	public static final char startBG = KeyWordConstants.C_BIG_GROUP_START;
	public static final char endBG = KeyWordConstants.C_BIG_GROUP_END;
	
	public static final char startSG = KeyWordConstants.C_GROUP_START;
	public static final char endSG = KeyWordConstants.C_GROUP_END;
	
	public static final char kwSerialize = KeyWordConstants.C_KEYWORD_SERIALIZE; // this should not be used by this class
	public static final char kwAbstraction = KeyWordConstants.C_KEYWORD_MARKER;
	public static final char kwSep = KeyWordConstants.C_ID_MARKER;
	
	/**
	 * The constructor initializes assuming the long keyword mode was used to generate the language model
	 */
	public ASTLMAbstractionDeserializer() {
		kwDispatcher = new KeyWordDispatcher(); // the one using the long key words is the default here
	}
	
	/**
	 * In case the language model was created using the short keywords
	 * @param aTMapper
	 */
	public void useShortKeywords() {
		kwDispatcher = new KeyWordDispatcherShort();
	}
	
	/**
	 * In case the language model was created using the long keywords
	 * @param aTMapper
	 */
	public void useLongKeywords() {
		kwDispatcher = new KeyWordDispatcher();
	}
	

	/**
	 * Parses the given serialization for the keyword that indicates which type
	 * of node was serialized.
	 * 
	 * @param aSerializedNode
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

		int startIdx = aSerializedNode.indexOf(kwAbstraction);

		// if there is no serialization keyword the string is malformed
		if (startIdx == -1) {
			return null;
		}
		
		// find the closing
		// this is faster with finding the end but may fail if we combine abstraction with serialization
		int bigCloseTag = aSerializedNode.lastIndexOf( endBG );
		
		if( bigCloseTag == -1 ) {
			throw new IllegalArgumentException( "The abstraction " + aSerializedNode + " had no valid closing tag after index " + startIdx );
		}
		
		int keyWordEndIdx = aSerializedNode.indexOf( kwSep, startIdx + 1 );
		
		if( keyWordEndIdx == -1 ) {
			keyWordEndIdx = bigCloseTag;
		}

		result[0] = aSerializedNode.substring( startIdx, keyWordEndIdx );
		
		if ( keyWordEndIdx != bigCloseTag ) {
			// there are children that can be cut out
			// this includes the start and end keyword for the child groups
			result[1] = aSerializedNode.substring( keyWordEndIdx + 1, bigCloseTag );
		} else {
			// no children, no cutting
			result[1] = null;
		}

		return result;
	}
	
	/**
	 * Searches for all child data objects in the given string which are
	 * identified by a starting and closing group symbol on the right depth
	 * @param aSeriChildData The child data from the language model
	 * @return All child data after cutting and putting into an array
	 */
	private List<String> cutChildData( String aSeriChildData ) {
		if( aSeriChildData == null || aSeriChildData.length() == 0 ) {
			return null;
		}
		
		List<String> allChildren = new ArrayList<String>();

		int depth = 0;
		int startIdx = 0;
		
		for( int idx = 0; idx < aSeriChildData.length(); ++idx ) {
			switch( aSeriChildData.charAt( idx ) ) {
				case startSG : if( ++depth == 1 ) { // mark this only if it starts a group at depth 1
									startIdx = idx+1; 
								}; break; 
				case endSG : if ( --depth == 0 ) { // this may add empty strings to the result set which is fine
									allChildren.add( aSeriChildData.substring( startIdx, idx ) );
									startIdx = idx +1; 
								}; break;
				default : break;
			}
		}
		
		return allChildren;
	}
	
	/**
	 * Very much like the cutChildData but the cutting is triggered by the big group symbols instead
	 * of the small ones and the brackets are kept for further investigation
	 * @param aSeriChildData The child data from the language model
	 * @return All child data after cutting and putting into an array
	 */
	private List<String> cutTopLevelNodes( String aSeriChildData ) {
		if( aSeriChildData == null || aSeriChildData.length() == 0 ) {
			return null;
		}
		
		List<String> allChildren = new ArrayList<String>();

		int depth = 0;
		int startIdx = 0;
		
		for( int idx = 0; idx < aSeriChildData.length(); ++idx ) {
			switch( aSeriChildData.charAt( idx ) ) {
				case startBG : if( ++depth == 1 ) { // mark this only if it starts a group at depth 1
									startIdx = idx; 
								}; break; 
				case endBG : if ( --depth == 0 ) { 
									allChildren.add( aSeriChildData.substring( startIdx, idx+1 ) );
									startIdx = idx +1;
								}; break;
				default : break;
			}
		}
		
		return allChildren;
	}

	/**
	 * Creates a new node object for a given serialized string
	 * 
	 * @param childDataStr
	 *            The keyword that the mapper used for the original node
	 * @return a node of the same type as the original one that got serialized
	 */
	public Node deserializeNode(String aSerializedString ) {

		if( aSerializedString == null || aSerializedString.length() == 0 ) {
			return null;
		}
		
		// this name is awful but the result is the one keyword and maybe the child string...
		String[] parsedParts = parseKeywordFromSeri( aSerializedString );
		String keyword = parsedParts[0];
		String childDataStr = parsedParts[1];
		
		if( keyword == null ) {
			return null;
		}

		KeyWordDispatcher kwd = new KeyWordDispatcher();
		
		return kwd.dispatchAndDesi( keyword, childDataStr, this );
	}
	
	private List<Parameter> getParameterFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		List<Parameter> result = new ArrayList<Parameter>();

		List<String> allPars = cutTopLevelNodes( aSerializedNode );
		for( String s : allPars ) {
			String[] parsedKW = parseKeywordFromSeri( s );
			// I can assume that the keyword has to be $PAR or the short version of it
			result.add( createParameter( parsedKW[1]));
		}
		return result;
	}
	
	private List<TypeParameter> getTypeParameterFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		List<TypeParameter> result = new ArrayList<TypeParameter>();

		List<String> allPars = cutTopLevelNodes( aSerializedNode );
		for( String s : allPars ) {
			String[] parsedKW = parseKeywordFromSeri( s );
			// I can assume that the keyword has to be $PAR or the short version of it
			result.add( createTypeParameter( parsedKW[1]));
		}
		return result;
	}
	
//	($CNSTR_DEC;[PUB],[($PAR;($REF_TYPE;$CI_TYPE,1),[]),($PAR;($PRIM_TYPE;Long),[]),($PAR;($PRIM_TYPE;Long),[])],[])
	public ConstructorDeclaration createConstructorDeclaration(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = cutChildData( aSerializedNode );
				
		result.setModifiers( ModifierMapper.getAllModsAsInt( childData.get( 0 ) ) );
		result.setParameters( getParameterFromMapping( childData.get( 1 )) );
		result.setTypeParameters( getTypeParameterFromMapping( childData.get( 2 )) );
		
		return result;
	}

	public InitializerDeclaration createInitializerDeclaration(String aSerializedNode) {
		InitializerDeclaration result = new InitializerDeclaration();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		return result;
	}

	public EnumConstantDeclaration createEnumConstantDeclaration(String aSerializedNode) {
		EnumConstantDeclaration result = new EnumConstantDeclaration();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public VariableDeclarator createVariableDeclarator(String aSerializedNode) {
		VariableDeclarator result = new VariableDeclarator();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public EnumDeclaration createEnumDeclaration(String aSerializedNode) {
		EnumDeclaration result = new EnumDeclaration();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public AnnotationDeclaration createAnnotationDeclaration(String aSerializedNode) {
		AnnotationDeclaration result = new AnnotationDeclaration();
		
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		return result;
	}

	public AnnotationMemberDeclaration createAnnotationMemberDeclaration(String aSerializedNode) {
		AnnotationMemberDeclaration result = new AnnotationMemberDeclaration();
		
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		return result;
	}

	public EmptyMemberDeclaration createEmptyMemberDeclaration(String aSerializedNode) {
		EmptyMemberDeclaration result = new EmptyMemberDeclaration();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public EmptyTypeDeclaration createEmptyTypeDeclaration(String aSerializedNode) {
		EmptyTypeDeclaration result = new EmptyTypeDeclaration();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public WhileStmt createWhileStmt(String aSerializedNode) {
		WhileStmt result = new WhileStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public TryStmt createTryStmt(String aSerializedNode) {
		TryStmt result = new TryStmt();
		
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		return result;
	}

	public ThrowStmt createThrowStmt(String aSerializedNode) {
		ThrowStmt result = new ThrowStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	// This may never be used
	public ThrowsStmt createThrowsStmt(String aSerializedNode) {
		ThrowsStmt result = null;
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public SynchronizedStmt createSynchronizedStmt(String aSerializedNode) {
		SynchronizedStmt result = new SynchronizedStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public SwitchStmt createSwitchStmt(String aSerializedNode) {
		SwitchStmt result = new SwitchStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public SwitchEntryStmt createSwitchEntryStmt(String aSerializedNode) {
		SwitchEntryStmt result = new SwitchEntryStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ReturnStmt createReturnStmt(String aSerializedNode) {
		ReturnStmt result = new ReturnStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public LabeledStmt createLabeledStmt(String aSerializedNode) {
		LabeledStmt result = new LabeledStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public IfStmt createIfStmt(String aSerializedNode) {
		IfStmt result = new IfStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	// this may never be used
	public ElseStmt createElseStmt(String aSerializedNode) {
		ElseStmt result = null;
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ForStmt createForStmt(String aSerializedNode) {
		ForStmt result = new ForStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ForeachStmt createForeachStmt(String aSerializedNode) {
		ForeachStmt result = new ForeachStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ExpressionStmt createExpressionStmt(String aSerializedNode) {
		ExpressionStmt result = new ExpressionStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ExplicitConstructorInvocationStmt createExplicitConstructorInvocationStmt(String aSerializedNode) {
		ExplicitConstructorInvocationStmt result = new ExplicitConstructorInvocationStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public EmptyStmt createEmptyStmt(String aSerializedNode) {
		EmptyStmt result = new EmptyStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public DoStmt createDoStmt(String aSerializedNode) {
		DoStmt result = new DoStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ContinueStmt createContinueStmt(String aSerializedNode) {
		ContinueStmt result = new ContinueStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public CatchClause createCatchClause(String aSerializedNode) {
		CatchClause result = new CatchClause();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public BlockStmt createBlockStmt(String aSerializedNode) {
		BlockStmt result = new BlockStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public VariableDeclaratorId createVariableDeclaratorId(String aSerializedNode) {
		VariableDeclaratorId result = new VariableDeclaratorId();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public VariableDeclarationExpr createVariableDeclarationExpr(String aSerializedNode) {
		VariableDeclarationExpr result = new VariableDeclarationExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public TypeExpr createTypeExpr(String aSerializedNode) {
		TypeExpr result = new TypeExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public SuperExpr createSuperExpr(String aSerializedNode) {
		SuperExpr result = new SuperExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public QualifiedNameExpr createQualifiedNameExpr(String aSerializedNode) {
		QualifiedNameExpr result = new QualifiedNameExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public NullLiteralExpr createNullLiteralExpr(String aSerializedNode) {
		NullLiteralExpr result = new NullLiteralExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public MethodReferenceExpr createMethodReferenceExpr(String aSerializedNode) {
		MethodReferenceExpr result = new MethodReferenceExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	// this may never be used
	public BodyStmt createBodyStmt(String aSerializedNode) {
		BodyStmt result = null;
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public LongLiteralMinValueExpr createLongLiteralMinValueExpr(String aSerializedNode) {
		LongLiteralMinValueExpr result = new LongLiteralMinValueExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public LambdaExpr createLambdaExpr(String aSerializedNode) {
		LambdaExpr result = new LambdaExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public IntegerLiteralMinValueExpr createIntegerLiteralMinValueExpr(String aSerializedNode) {
		IntegerLiteralMinValueExpr result = new IntegerLiteralMinValueExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public InstanceOfExpr createInstanceOfExpr(String aSerializedNode) {
		InstanceOfExpr result = new InstanceOfExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public FieldAccessExpr createFieldAccessExpr(String aSerializedNode) {
		FieldAccessExpr result = new FieldAccessExpr();
		
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		return result;
	}

	public ConditionalExpr createConditionalExpr(String aSerializedNode) {
		ConditionalExpr result = new ConditionalExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ClassExpr createClassExpr(String aSerializedNode) {
		ClassExpr result = new ClassExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public CastExpr createCastExpr(String aSerializedNode) {
		CastExpr result = new CastExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public AssignExpr createAssignExpr(String aSerializedNode) {
		AssignExpr result = new AssignExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ArrayInitializerExpr createArrayInitializerExpr(String aSerializedNode) {
		ArrayInitializerExpr result = new ArrayInitializerExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ArrayCreationExpr createArrayCreationExpr(String aSerializedNode) {
		ArrayCreationExpr result = new ArrayCreationExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ArrayAccessExpr createArrayAccessExpr(String aSerializedNode) {
		ArrayAccessExpr result = new ArrayAccessExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public PackageDeclaration createPackageDeclaration(String aSerializedNode) {
		PackageDeclaration result = new PackageDeclaration();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	// this may never be used
	public ImportDeclaration createImportDeclaration(String aSerializedNode) {
		ImportDeclaration result = null;
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public FieldDeclaration createFieldDeclaration(String aSerializedNode) {
		FieldDeclaration result = new FieldDeclaration();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ClassOrInterfaceType createClassOrInterfaceType(String aSerializedNode) {
		ClassOrInterfaceType result = new ClassOrInterfaceType();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ClassOrInterfaceDeclaration createClassOrInterfaceDeclaration(String aSerializedNode) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public MethodDeclaration createMethodDeclaration(String aSerializedNode) {
		MethodDeclaration result = new MethodDeclaration();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public BinaryExpr createBinaryExpr(String aSerializedNode) {
		BinaryExpr result = new BinaryExpr();
		
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		return result;
	}

	public UnaryExpr createUnaryExpr(String aSerializedNode) {
		UnaryExpr result = new UnaryExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public MethodCallExpr createMethodCallExpr(String aSerializedNode) {
		MethodCallExpr result = new MethodCallExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public MethodCallExpr createPrivMethodCallExpr(String aSerializedNode) {
		MethodCallExpr result = new MethodCallExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public NameExpr createNameExpr(String aSerializedNode) {
		NameExpr result = new NameExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ConstructorDeclaration createIntegerLiteralExpr(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public DoubleLiteralExpr createDoubleLiteralExpr(String aSerializedNode) {
		DoubleLiteralExpr result = new DoubleLiteralExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public StringLiteralExpr createStringLiteralExpr(String aSerializedNode) {
		StringLiteralExpr result = new StringLiteralExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public BooleanLiteralExpr createBooleanLiteralExpr(String aSerializedNode) {
		BooleanLiteralExpr result = new BooleanLiteralExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public CharLiteralExpr createCharLiteralExpr(String aSerializedNode) {
		CharLiteralExpr result = new CharLiteralExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public LongLiteralExpr createLongLiteralExpr(String aSerializedNode) {
		LongLiteralExpr result = new LongLiteralExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ThisExpr createThisExpr(String aSerializedNode) {
		ThisExpr result = new ThisExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public BreakStmt createBreakStmt(String aSerializedNode) {
		BreakStmt result = new BreakStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ObjectCreationExpr createObjectCreationExpr(String aSerializedNode) {
		ObjectCreationExpr result = new ObjectCreationExpr();
		
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		return result;
	}

	public MarkerAnnotationExpr createMarkerAnnotationExpr(String aSerializedNode) {
		MarkerAnnotationExpr result = new MarkerAnnotationExpr();
		
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		return result;
	}

	public NormalAnnotationExpr createNormalAnnotationExpr(String aSerializedNode) {
		NormalAnnotationExpr result = new NormalAnnotationExpr();
		
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		return result;
	}

	public SingleMemberAnnotationExpr createSingleMemberAnnotationExpr(String aSerializedNode) {
		SingleMemberAnnotationExpr result = new SingleMemberAnnotationExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public Parameter createParameter(String aSerializedNode) {
		Parameter result = new Parameter();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public MultiTypeParameter createMultiTypeParameter(String aSerializedNode) {
		MultiTypeParameter result = new MultiTypeParameter();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public EnclosedExpr createEnclosedExpr(String aSerializedNode) {
		EnclosedExpr result = new EnclosedExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public AssertStmt createAssertStmt(String aSerializedNode) {
		AssertStmt result = new AssertStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ConstructorDeclaration createMemberValuePair(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public TypeDeclarationStmt createTypeDeclarationStmt(String aSerializedNode) {
		TypeDeclarationStmt result = new TypeDeclarationStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public ReferenceType createReferenceType(String aSerializedNode) {
		ReferenceType result = new ReferenceType();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public PrimitiveType createPrimitiveType(String aSerializedNode) {
		PrimitiveType result = new PrimitiveType();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	// this may never be used
	public UnionType createUnionType(String aSerializedNode) {
		UnionType result = null;
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public IntersectionType createIntersectionType(String aSerializedNode) {
		// TODO fix the construction
		IntersectionType result = null;
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public TypeParameter createTypeParameter(String aSerializedNode) {
		TypeParameter result = new TypeParameter();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	public WildcardType createWildcardType(String aSerializedNode) {
		WildcardType result = new WildcardType();
		
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		return result;
	}

	public VoidType createVoidType(String aSerializedNode) {
		VoidType result = new VoidType();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	// this may never be used
	public ExtendsStmt createExtendsStmt(String aSerializedNode) {
		ExtendsStmt result = null;
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	// this may never be used
	public ImplementsStmt createImplementsStmt(String aSerializedNode) {
		ImplementsStmt result = null;
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// TODO implement
		
		
		return result;
	}

	@Override
	public void createUnknown() {
		// TODO hm... think about it
		
	}

}
