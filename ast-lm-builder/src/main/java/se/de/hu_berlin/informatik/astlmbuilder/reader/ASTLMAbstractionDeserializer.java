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
import com.github.javaparser.ast.body.TypeDeclaration;
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
import com.github.javaparser.ast.expr.Expression;
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
import se.de.hu_berlin.informatik.astlmbuilder.mapping.OperatorMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.TypeMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.UnknownNode;
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
	
	private DSUtils u = new DSUtils( this, kwDispatcher );
	
	/**
	 * The constructor initializes assuming the long keyword mode was used to generate the language model
	 */
	public ASTLMAbstractionDeserializer() {
		kwDispatcher = new KeyWordDispatcher(); // the one using the long key words is the default here
	}
	
	/**
	 * In case the language model was created using the short keywords
	 */
	public void useShortKeywords() {
		kwDispatcher = new KeyWordDispatcherShort();
		u.changeKeyWordDispatcher( kwDispatcher );
	}
	
	/**
	 * In case the language model was created using the long keywords
	 */
	public void useLongKeywords() {
		kwDispatcher = new KeyWordDispatcher();
		u.changeKeyWordDispatcher( kwDispatcher );
	}

	/**
	 * Creates a new node object for a given serialized string
	 * 
	 * @param aSerializedString
	 *            the serialized string
	 * @return a node of the same type as the original one that got serialized
	 */
	public Node deserializeNode(String aSerializedString ) {

		if( aSerializedString == null || aSerializedString.length() == 0 ) {
			return null;
		}
		
		// this name is awful but the result is the one keyword and maybe the child string...
		String[] parsedParts = u.parseKeywordFromSeri( aSerializedString );
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

		List<String> allPars = u.cutTopLevelNodes( aSerializedNode );
		for( String s : allPars ) {
			String[] parsedKW = u.parseKeywordFromSeri( s );
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

		List<String> allPars = u.cutTopLevelNodes( aSerializedNode );
		for( String s : allPars ) {
			String[] parsedKW = u.parseKeywordFromSeri( s );
			// I can assume that the keyword has to be $PAR or the short version of it
			result.add( createTypeParameter( parsedKW[1]));
		}
		return result;
	}
	
	private List<ClassOrInterfaceType> getClassOrInterfaceTypeListFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		List<ClassOrInterfaceType> result = new ArrayList<ClassOrInterfaceType>();

		List<String> allCITs = u.cutTopLevelNodes( aSerializedNode );
		for( String s : allCITs ) {
			String[] parsedKW = u.parseKeywordFromSeri( s );
			result.add( createClassOrInterfaceType( parsedKW[1]));
		}
		return result;
	}
	
	private List<Expression> getExpressionListFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		List<Expression> result = new ArrayList<Expression>();

		List<String> allPars = u.cutTopLevelNodes( aSerializedNode );
		for( String s : allPars ) {
			String[] parsedKW = u.parseKeywordFromSeri( s );
			// depending on the instance of the expression a different node has to be created
			// but it will always be some kind of expression
			Expression e = (Expression) kwDispatcher.dispatchAndDesi( parsedKW[0], parsedKW[1], this);
			result.add( e );
		}
		return result;
	}
	
	private List<Type> getTypeListFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		List<Type> result = new ArrayList<Type>();

		List<String> allPars = u.cutTopLevelNodes( aSerializedNode );
		for( String s : allPars ) {
			String[] parsedKW = u.parseKeywordFromSeri( s );
			// depending on the instance of the expression a different node has to be created
			// but it will always be some kind of expression
			Type t = (Type) kwDispatcher.dispatchAndDesi( parsedKW[0], parsedKW[1], this);
			result.add( t );
		}
		return result;
	}
	
	private List<VariableDeclarator> getVariableDeclaratorListFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		List<VariableDeclarator> result = new ArrayList<VariableDeclarator>();

		List<String> allPars = u.cutTopLevelNodes( aSerializedNode );
		for( String s : allPars ) {
			String[] parsedKW = u.parseKeywordFromSeri( s );
			// depending on the instance of the expression a different node has to be created
			// but it will always be some kind of expression
			VariableDeclarator t = (VariableDeclarator) kwDispatcher.dispatchAndDesi( parsedKW[0], parsedKW[1], this);
			result.add( t );
		}
		return result;
	}
	
	
	
	public ConstructorDeclaration createConstructorDeclaration(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
				
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
		
		// this type has never any children
		
		return result;
	}

	public EnumConstantDeclaration createEnumConstantDeclaration(String aSerializedNode) {
		EnumConstantDeclaration result = new EnumConstantDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}

		List<String> childData = u.cutChildData( aSerializedNode );
		result.setArgs( getExpressionListFromMapping( childData.get( 0 )) );
		
		return result;
	}

	public VariableDeclarator createVariableDeclarator(String aSerializedNode) {
		VariableDeclarator result = new VariableDeclarator();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}

		List<String> childData = u.cutChildData( aSerializedNode );
		result.setInit( getExpressionListFromMapping( childData.get( 0 ) ).get( 0 ) );
		
		return result;
	}

	public EnumDeclaration createEnumDeclaration(String aSerializedNode) {
		EnumDeclaration result = new EnumDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setModifiers( ModifierMapper.getAllModsAsInt( childData.get( 0 ) ) );
		
		return result;
	}

	public AnnotationDeclaration createAnnotationDeclaration(String aSerializedNode) {
		AnnotationDeclaration result = new AnnotationDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	public AnnotationMemberDeclaration createAnnotationMemberDeclaration(String aSerializedNode) {
		AnnotationMemberDeclaration result = new AnnotationMemberDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	public EmptyMemberDeclaration createEmptyMemberDeclaration(String aSerializedNode) {
		EmptyMemberDeclaration result = new EmptyMemberDeclaration();
			
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	public EmptyTypeDeclaration createEmptyTypeDeclaration(String aSerializedNode) {
		EmptyTypeDeclaration result = new EmptyTypeDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	public WhileStmt createWhileStmt(String aSerializedNode) {
		WhileStmt result = new WhileStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setCondition( getExpressionListFromMapping( childData.get( 0 ) ).get( 0 ));
		
		return result;
	}

	public TryStmt createTryStmt(String aSerializedNode) {
		TryStmt result = new TryStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	public ThrowStmt createThrowStmt(String aSerializedNode) {
		ThrowStmt result = new ThrowStmt();
			
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	// This may never be used
	public ThrowsStmt createThrowsStmt(String aSerializedNode) {
		ThrowsStmt result = null;
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children	
		
		return result;
	}

	public SynchronizedStmt createSynchronizedStmt(String aSerializedNode) {
		SynchronizedStmt result = new SynchronizedStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	public SwitchStmt createSwitchStmt(String aSerializedNode) {
		SwitchStmt result = new SwitchStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setSelector( getExpressionListFromMapping( childData.get( 0 )).get( 0 ) );
		
		return result;
	}

	public SwitchEntryStmt createSwitchEntryStmt(String aSerializedNode) {
		SwitchEntryStmt result = new SwitchEntryStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setLabel( getExpressionListFromMapping( childData.get( 0 ) ).get( 0 ) );
		
		return result;
	}

	public ReturnStmt createReturnStmt(String aSerializedNode) {
		ReturnStmt result = new ReturnStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	public LabeledStmt createLabeledStmt(String aSerializedNode) {
		LabeledStmt result = new LabeledStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	public IfStmt createIfStmt(String aSerializedNode) {
		IfStmt result = new IfStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setCondition( getExpressionListFromMapping( childData.get( 0 ) ).get( 0 ) );
		
		return result;
	}

	// this may never be used
	public ElseStmt createElseStmt(String aSerializedNode) {
		ElseStmt result = new ElseStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	public ForStmt createForStmt(String aSerializedNode) {
		ForStmt result = new ForStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setInit( getExpressionListFromMapping( childData.get( 0 ) ) );
		result.setCompare( getExpressionListFromMapping( childData.get( 1 ) ).get( 0 ) );
		result.setUpdate( getExpressionListFromMapping( childData.get( 2 ) ) );
		
		return result;
	}

	public ForeachStmt createForeachStmt(String aSerializedNode) {
		ForeachStmt result = new ForeachStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setVariable( (VariableDeclarationExpr) deserializeNode( childData.get( 0 ) ) );
		result.setIterable( getExpressionListFromMapping( childData.get( 0 ) ).get( 0 ) );
		
		return result;
	}

	// the basic expression will mostly never be used
	public ExpressionStmt createExpressionStmt(String aSerializedNode) {
		ExpressionStmt result = new ExpressionStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	public ExplicitConstructorInvocationStmt createExplicitConstructorInvocationStmt(String aSerializedNode) {
		ExplicitConstructorInvocationStmt result = new ExplicitConstructorInvocationStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setThis( childData.get( 0 ).equals( "this" )); // otherwise this value is "super"
		result.setArgs( getExpressionListFromMapping( childData.get( 1 ) ) );
		result.setTypeArgs( getTypeListFromMapping( childData.get( 2 ) ) );
		
		return result;
	}

	public EmptyStmt createEmptyStmt(String aSerializedNode) {
		EmptyStmt result = new EmptyStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// well... its empty so there is not even a need to parse children
		// and the check above should always trigger
		
		return result;
	}

	public DoStmt createDoStmt(String aSerializedNode) {
		DoStmt result = new DoStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setCondition( getExpressionListFromMapping( childData.get( 0 )).get( 0 ) );
		
		return result;
	}

	public ContinueStmt createContinueStmt(String aSerializedNode) {
		ContinueStmt result = new ContinueStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type never has any children
		
		return result;
	}

	public CatchClause createCatchClause(String aSerializedNode) {
		CatchClause result = new CatchClause();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// we could parse the parameter and the block here but we do not serialize any of the two
		// TODO may add one or both to serialization and deserialization
		
		return result;
	}

	public BlockStmt createBlockStmt(String aSerializedNode) {
		BlockStmt result = new BlockStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// again this is only a closing block statement
		
		
		return result;
	}

	public VariableDeclaratorId createVariableDeclaratorId(String aSerializedNode) {
		VariableDeclaratorId result = new VariableDeclaratorId();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setArrayCount( Integer.parseInt( childData.get( 0 ) ) );
		
		return result;
	}

	public VariableDeclarationExpr createVariableDeclarationExpr(String aSerializedNode) {
		VariableDeclarationExpr result = new VariableDeclarationExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setModifiers( ModifierMapper.getAllModsAsInt( childData.get( 0 ) ));
		result.setType( getTypeListFromMapping( childData.get( 1 ) ).get( 0 ) );
		result.setVars( getVariableDeclaratorListFromMapping( childData.get( 2 ) ) );
		
		return result;
	}

	public TypeExpr createTypeExpr(String aSerializedNode) {
		TypeExpr result = new TypeExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setType( getTypeListFromMapping( childData.get( 0 )).get( 0 ) );
		
		return result;
	}

	public SuperExpr createSuperExpr(String aSerializedNode) {
		SuperExpr result = new SuperExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// no children here
		
		return result;
	}

	public QualifiedNameExpr createQualifiedNameExpr(String aSerializedNode) {
		QualifiedNameExpr result = new QualifiedNameExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// no children here
		
		return result;
	}

	public NullLiteralExpr createNullLiteralExpr(String aSerializedNode) {
		NullLiteralExpr result = new NullLiteralExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// null!
		
		return result;
	}

	public MethodReferenceExpr createMethodReferenceExpr(String aSerializedNode) {
		MethodReferenceExpr result = new MethodReferenceExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setScope( getExpressionListFromMapping( childData.get( 0 ) ).get( 0 ) );
		result.setTypeArguments( u.getTypeArgumentsFromMapping( childData.get( 1 ) ) );
		
		return result;
	}

	// this may never be used
	public BodyStmt createBodyStmt(String aSerializedNode) {
		BodyStmt result = null;
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// nothing here
		
		return result;
	}

	public LongLiteralMinValueExpr createLongLiteralMinValueExpr(String aSerializedNode) {
		LongLiteralMinValueExpr result = new LongLiteralMinValueExpr();
			
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setValue( childData.get( 0 ) );
		
		return result;
	}

	public LambdaExpr createLambdaExpr(String aSerializedNode) {
		LambdaExpr result = new LambdaExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setParametersEnclosed( childData.get( 0 ).equals( "true" ));
		result.setParameters( getParameterFromMapping( childData.get( 1 )));
		result.setBody( (Statement) deserializeNode( childData.get( 2 ) ) );
		
		return result;
	}

	public IntegerLiteralMinValueExpr createIntegerLiteralMinValueExpr(String aSerializedNode) {
		IntegerLiteralMinValueExpr result = new IntegerLiteralMinValueExpr();
			
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setValue( childData.get( 0 ) );
		
		return result;
	}

	public InstanceOfExpr createInstanceOfExpr(String aSerializedNode) {
		InstanceOfExpr result = new InstanceOfExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setExpr( (Expression) deserializeNode( childData.get( 0 )));
		result.setType( (Type) deserializeNode( childData.get( 1 )));
		
		return result;
	}

	public FieldAccessExpr createFieldAccessExpr(String aSerializedNode) {
		FieldAccessExpr result = new FieldAccessExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setFieldExpr( (NameExpr) deserializeNode( childData.get( 0 ))); 
		result.setTypeArgs(getTypeListFromMapping( childData.get( 1 )) );
		
		return result;
	}

	public ConditionalExpr createConditionalExpr(String aSerializedNode) {
		ConditionalExpr result = new ConditionalExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setCondition( (Expression) deserializeNode( childData.get( 0 )));
		result.setThenExpr( (Expression) deserializeNode( childData.get( 1 )));
		result.setElseExpr((Expression) deserializeNode( childData.get( 2 )));
		
		return result;
	}

	public ClassExpr createClassExpr(String aSerializedNode) {
		ClassExpr result = new ClassExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setType( (Type) deserializeNode( childData.get( 0 ) ));
		
		return result;
	}

	public CastExpr createCastExpr(String aSerializedNode) {
		CastExpr result = new CastExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setType( (Type) deserializeNode( childData.get( 0 ) ));
		result.setExpr( (Expression) deserializeNode( childData.get( 1 ) ));
			
		return result;
	}

	public AssignExpr createAssignExpr(String aSerializedNode) {
		AssignExpr result = new AssignExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setTarget((Expression) deserializeNode( childData.get( 0 ) ));
		result.setOperator( OperatorMapper.getAssignOperatorFromMapping( childData.get( 1 ) ));
		result.setValue( (Expression) deserializeNode( childData.get( 2 )));
		
		return result;
	}

	public ArrayInitializerExpr createArrayInitializerExpr(String aSerializedNode) {
		ArrayInitializerExpr result = new ArrayInitializerExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setValues( getExpressionListFromMapping(childData.get( 0 ) ) );
		
		return result;
	}

	public ArrayCreationExpr createArrayCreationExpr(String aSerializedNode) {
		ArrayCreationExpr result = new ArrayCreationExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setType( (Type) deserializeNode( childData.get( 0 )));
		result.setDimensions( getExpressionListFromMapping( childData.get( 1 )));
		result.setArrayCount( Integer.parseInt( childData.get( 2 )));
		result.setInitializer( (ArrayInitializerExpr) deserializeNode( childData.get( 3 )));
		
		return result;
	}

	public ArrayAccessExpr createArrayAccessExpr(String aSerializedNode) {
		ArrayAccessExpr result = new ArrayAccessExpr();
			
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setIndex( (Expression) deserializeNode( childData.get( 0 )));
		
		return result;
	}

	public PackageDeclaration createPackageDeclaration(String aSerializedNode) {
		PackageDeclaration result = new PackageDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		// TODO this could be wrong because package name and the actual name are kind of different
		// but the api has no set package name...
		result.setName( (NameExpr) deserializeNode( childData.get( 0 )));;
		
		return result;
	}

	// this may never be used
	public ImportDeclaration createImportDeclaration(String aSerializedNode) {
		ImportDeclaration result = null;
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
//		List<String> childData = u.cutChildData( aSerializedNode );
//		result.setName( (NameExpr) deserializeNode( childData.get( 0 )));;
		
		
		return result;
	}

	public FieldDeclaration createFieldDeclaration(String aSerializedNode) {
		FieldDeclaration result = new FieldDeclaration();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setModifiers( ModifierMapper.getAllModsAsInt( childData.get( 0 )));
		result.setType( (Type) deserializeNode( childData.get( 1 )));
		result.setVariables(getVariableDeclaratorListFromMapping( childData.get( 2 ) ));
		
		return result;
	}

	public ClassOrInterfaceType createClassOrInterfaceType(String aSerializedNode) {
		ClassOrInterfaceType result = new ClassOrInterfaceType();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setScope( u.getCITypeFromFullScopeMapping( childData.get( 0 )));
		result.setTypeArguments(u.getTypeArgumentsFromMapping( childData.get( 1 ) ) );
		
		return result;
	}


	/**
	 * Better use the class or interface specific methods
	 */
	@Deprecated
	public ClassOrInterfaceDeclaration createClassOrInterfaceDeclaration(String aSerializedNode) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setTypeParameters(getTypeParameterFromMapping(childData.get( 0 )));
		result.setExtends(getClassOrInterfaceTypeListFromMapping( childData.get( 1) ));
		result.setImplements(getClassOrInterfaceTypeListFromMapping( childData.get( 2 )));
//		result.setInterface( false ); // we cant know because the keyword for this method could be from either one
		
		return result;
	}
	
	public ClassOrInterfaceDeclaration createClassDeclaration(String aSerializedNode) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setTypeParameters(getTypeParameterFromMapping(childData.get( 0 )));
		result.setExtends(getClassOrInterfaceTypeListFromMapping( childData.get( 1) ));
		result.setImplements(getClassOrInterfaceTypeListFromMapping( childData.get( 2 )));
		result.setInterface( false );
		
		return result;
	}
	
	public ClassOrInterfaceDeclaration createInterfaceDeclaration(String aSerializedNode) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setTypeParameters(getTypeParameterFromMapping(childData.get( 0 )));
		result.setExtends(getClassOrInterfaceTypeListFromMapping( childData.get( 1) ));
		result.setImplements(getClassOrInterfaceTypeListFromMapping( childData.get( 2 )));
		result.setInterface( true );
		
		return result;
	}

	public MethodDeclaration createMethodDeclaration(String aSerializedNode) {
		MethodDeclaration result = new MethodDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setModifiers( ModifierMapper.getAllModsAsInt( childData.get( 0 )));
		result.setType(getTypeListFromMapping( childData.get( 1 )).get( 0 ));
		result.setParameters(getParameterFromMapping( childData.get( 2 )));
		result.setTypeParameters(getTypeParameterFromMapping( childData.get( 3 )));
		
		return result;
	}

	public BinaryExpr createBinaryExpr(String aSerializedNode) {
		BinaryExpr result = new BinaryExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setLeft( (Expression) deserializeNode( childData.get( 0 )));
		result.setOperator( OperatorMapper.getBinaryOperatorFromMapping( childData.get( 1 )));
		result.setRight( (Expression) deserializeNode( childData.get( 2 )));
		
		return result;
	}

	public UnaryExpr createUnaryExpr(String aSerializedNode) {
		UnaryExpr result = new UnaryExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setOperator( OperatorMapper.getUnaryOperatorFromMapping( childData.get( 0 )) );
		result.setExpr( (Expression) deserializeNode( childData.get( 1 )));		
		
		return result;
	}

	public MethodCallExpr createMethodCallExpr(String aSerializedNode) {
		MethodCallExpr result = new MethodCallExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setScope( (Expression) deserializeNode( childData.get( 0 )));
		result.setName(childData.get( 1 ));
		result.setArgs(getExpressionListFromMapping(childData.get( 2 )));
		result.setTypeArgs(getTypeListFromMapping(childData.get( 3 )));		
		
		return result;
	}

	// this is actually the same as the normal method call expression but without the name
	public MethodCallExpr createPrivMethodCallExpr(String aSerializedNode) {
		MethodCallExpr result = new MethodCallExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setScope( (Expression) deserializeNode( childData.get( 0 ))); // this and the next value are meaningless
		result.setName(childData.get( 1 ));
		result.setArgs(getExpressionListFromMapping(childData.get( 2 )));
		result.setTypeArgs(getTypeListFromMapping(childData.get( 3 )));	
	
		return result;
	}

	public NameExpr createNameExpr(String aSerializedNode) {
		NameExpr result = new NameExpr();
			
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this has not child data and including all names of all objects would be pointless

		return result;
	}

	public ConstructorDeclaration createIntegerLiteralExpr(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setModifiers( ModifierMapper.getAllModsAsInt( childData.get( 0 )));
		result.setParameters( getParameterFromMapping( childData.get( 1 )));
		result.setTypeParameters(getTypeParameterFromMapping( childData.get( 2 )));
		
		return result;
	}

	public DoubleLiteralExpr createDoubleLiteralExpr(String aSerializedNode) {
		DoubleLiteralExpr result = new DoubleLiteralExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setValue( childData.get( 0 ) );
		
		return result;
	}

	public StringLiteralExpr createStringLiteralExpr(String aSerializedNode) {
		StringLiteralExpr result = new StringLiteralExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// there are no data for string literals because they are to different
		
		return result;
	}

	public BooleanLiteralExpr createBooleanLiteralExpr(String aSerializedNode) {
		BooleanLiteralExpr result = new BooleanLiteralExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setValue( Boolean.parseBoolean( childData.get( 0 ) ) );
		
		return result;
	}

	public CharLiteralExpr createCharLiteralExpr(String aSerializedNode) {
		CharLiteralExpr result = new CharLiteralExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// same argument as with the strings
		
		return result;
	}

	public LongLiteralExpr createLongLiteralExpr(String aSerializedNode) {
		LongLiteralExpr result = new LongLiteralExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setValue( childData.get( 0 ) );
		
		return result;
	}

	public ThisExpr createThisExpr(String aSerializedNode) {
		ThisExpr result = new ThisExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// its this and nothing more
		
		return result;
	}

	public BreakStmt createBreakStmt(String aSerializedNode) {
		BreakStmt result = new BreakStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// guess what? break has nothing more to say than to break free :)
		
		return result;
	}

	public ObjectCreationExpr createObjectCreationExpr(String aSerializedNode) {
		ObjectCreationExpr result = new ObjectCreationExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setScope( (Expression) deserializeNode( childData.get( 0 ) ) ); // this is almost always empty
		result.setType( (ClassOrInterfaceType) deserializeNode( childData.get( 1 )));
		result.setTypeArgs(getTypeListFromMapping( childData.get( 2 )));
		result.setArgs(getExpressionListFromMapping( childData.get( 3 )));
		result.setAnonymousClassBody(u.getBodyDeclaratorListFromMapping( childData.get( 4 )));
		
		return result;
	}

	public MarkerAnnotationExpr createMarkerAnnotationExpr(String aSerializedNode) {
		MarkerAnnotationExpr result = new MarkerAnnotationExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// just an annotation
		
		return result;
	}

	public NormalAnnotationExpr createNormalAnnotationExpr(String aSerializedNode) {
		NormalAnnotationExpr result = new NormalAnnotationExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// just an annotation expression
		
		return result;
	}

	public SingleMemberAnnotationExpr createSingleMemberAnnotationExpr(String aSerializedNode) {
		SingleMemberAnnotationExpr result = new SingleMemberAnnotationExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// i think we could have skipped the annotations because we never mutate them anyway
		
		return result;
	}

	public Parameter createParameter(String aSerializedNode) {
		Parameter result = new Parameter();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setType( (Type) deserializeNode( childData.get( 0 )));
		result.setModifiers( ModifierMapper.getAllModsAsInt( childData.get( 1 )));		
		
		return result;
	}

	public MultiTypeParameter createMultiTypeParameter(String aSerializedNode) {
		MultiTypeParameter result = new MultiTypeParameter();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setType( (UnionType) deserializeNode( childData.get( 0 ))); // they could have named the method setUnionType to save me some time...
		result.setModifiers( ModifierMapper.getAllModsAsInt( childData.get( 1 )));
		
		return result;
	}

	public EnclosedExpr createEnclosedExpr(String aSerializedNode) {
		EnclosedExpr result = new EnclosedExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setInner( (Expression) deserializeNode( childData.get( 0 )));		
		
		return result;
	}

	public AssertStmt createAssertStmt(String aSerializedNode) {
		AssertStmt result = new AssertStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setCheck( (Expression) deserializeNode( childData.get( 0 )));
		result.setMessage( (Expression) deserializeNode( childData.get( 1 )));		
		
		return result;
	}

	public ConstructorDeclaration createMemberValuePair(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setModifiers( ModifierMapper.getAllModsAsInt( childData.get( 0 )));
		result.setParameters(getParameterFromMapping(childData.get( 1 )));
		result.setTypeParameters(getTypeParameterFromMapping(childData.get( 2 )));		
		
		return result;
	}

	public TypeDeclarationStmt createTypeDeclarationStmt(String aSerializedNode) {
		TypeDeclarationStmt result = new TypeDeclarationStmt();
				
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setTypeDeclaration( (TypeDeclaration) deserializeNode( childData.get( 0 )));

		return result;
	}

	public ReferenceType createReferenceType(String aSerializedNode) {
		ReferenceType result = new ReferenceType();
			
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setType( (Type) deserializeNode( childData.get( 0 )));		
		
		return result;
	}

	public PrimitiveType createPrimitiveType(String aSerializedNode) {
		PrimitiveType result = new PrimitiveType();
			
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setType( TypeMapper.getPrimTypeFromMapping( childData.get( 0 )));
		
		return result;
	}

	public UnionType createUnionType(String aSerializedNode) {		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		// for some reason i need the list of reference type for the constructor... kind of inconsistent
		UnionType result = new UnionType(u.getReferenceTypeListFromMapping( childData.get( 0 )));
		
		return result;
	}

	public IntersectionType createIntersectionType(String aSerializedNode) {		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		IntersectionType result = new IntersectionType( u.getReferenceTypeListFromMapping( childData.get( 0 )));
		
		return result;
	}

	public TypeParameter createTypeParameter(String aSerializedNode) {
		TypeParameter result = new TypeParameter();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setTypeBound(getClassOrInterfaceTypeListFromMapping(childData.get( 0 )));
		
		return result;
	}

	public WildcardType createWildcardType(String aSerializedNode) {
		WildcardType result = new WildcardType();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// no child data
		
		return result;
	}

	public VoidType createVoidType(String aSerializedNode) {
		VoidType result = new VoidType();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// ...
		
		return result;
	}

	// this may never be used
	public ExtendsStmt createExtendsStmt(String aSerializedNode) {
		ExtendsStmt result = new ExtendsStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setExtends( getClassOrInterfaceTypeListFromMapping(childData.get( 0 )));		
		
		return result;
	}

	// this may never be used
	public ImplementsStmt createImplementsStmt(String aSerializedNode) {
		ImplementsStmt result = new ImplementsStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setImplements( getClassOrInterfaceTypeListFromMapping(childData.get( 0 )));	
		
		return result;
	}

	@Override
	public UnknownNode createUnknown() {
		return new UnknownNode();
	}

}
