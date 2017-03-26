package se.de.hu_berlin.informatik.astlmbuilder.reader;

import java.util.List;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
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
import com.github.javaparser.ast.expr.Expression;
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
import com.github.javaparser.ast.stmt.Statement;
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
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.OperatorMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.TypeMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.UnknownNode;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.dispatcher.IKeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.dispatcher.KeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.dispatcher.KeyWordDispatcherShort;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.BodyStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ElseStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ExtendsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ImplementsStmt;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.stmts.ThrowsStmt;

public class ASTLMAbstractionDeserializer implements IASTLMDeserializer {

	public IKeyWordDispatcher kwDispatcher = new KeyWordDispatcher(); // the one using the long key words is the default here
	
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
	private Node deserializeNode(String aSerializedString ) {

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
	
	private NodeList<Parameter> getParameterFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		NodeList<Parameter> result = new NodeList<>();

		List<String> allPars = u.cutTopLevelNodes( aSerializedNode );
		for( String s : allPars ) {
			String[] parsedKW = u.parseKeywordFromSeri( s );
			// I can assume that the keyword has to be $PAR or the short version of it
			result.add( createParameter( parsedKW[1]));
		}
		return result;
	}
	
	private NodeList<TypeParameter> getTypeParameterFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		NodeList<TypeParameter> result = new NodeList<>();

		List<String> allPars = u.cutTopLevelNodes( aSerializedNode );
		for( String s : allPars ) {
			String[] parsedKW = u.parseKeywordFromSeri( s );
			// I can assume that the keyword has to be $PAR or the short version of it
			result.add( createTypeParameter( parsedKW[1]));
		}
		return result;
	}
	
	private NodeList<ClassOrInterfaceType> getClassOrInterfaceTypeListFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		NodeList<ClassOrInterfaceType> result = new NodeList<>();

		List<String> allCITs = u.cutTopLevelNodes( aSerializedNode );
		for( String s : allCITs ) {
			String[] parsedKW = u.parseKeywordFromSeri( s );
			result.add( createClassOrInterfaceType( parsedKW[1]));
		}
		return result;
	}
	
	private NodeList<Expression> getExpressionListFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		NodeList<Expression> result = new NodeList<>();

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
	
	private NodeList<ArrayCreationLevel> getArrayCreationLevelsListFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		NodeList<ArrayCreationLevel> result = new NodeList<>();

		List<String> allPars = u.cutTopLevelNodes( aSerializedNode );
		for( String s : allPars ) {
			String[] parsedKW = u.parseKeywordFromSeri( s );
			// depending on the instance of the expression a different node has to be created
			// but it will always be some kind of expression
			ArrayCreationLevel e = (ArrayCreationLevel) kwDispatcher.dispatchAndDesi( parsedKW[0], parsedKW[1], this);
			result.add( e );
		}
		return result;
	}
	
	private NodeList<Type> getTypeListFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		NodeList<Type> result = new NodeList<Type>();

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
	
	private NodeList<VariableDeclarator> getVariableDeclaratorListFromMapping( String aSerializedNode ) {
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		NodeList<VariableDeclarator> result = new NodeList<>();

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
	
	

	@Override
	public ConstructorDeclaration createConstructorDeclaration(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
				
		result.setModifiers( kwDispatcher.getAllModsAsSet( childData.get( 0 ) ) );
		result.setParameters( getParameterFromMapping( childData.get( 1 )) );
		result.setTypeParameters( getTypeParameterFromMapping( childData.get( 2 )) );
		
		return result;
	}

	@Override
	public InitializerDeclaration createInitializerDeclaration(String aSerializedNode) {
		InitializerDeclaration result = new InitializerDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	@Override
	public EnumConstantDeclaration createEnumConstantDeclaration(String aSerializedNode) {
		EnumConstantDeclaration result = new EnumConstantDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}

		List<String> childData = u.cutChildData( aSerializedNode );
		result.setArguments( getExpressionListFromMapping( childData.get( 0 )) );
		
		return result;
	}

	@Override
	public VariableDeclarator createVariableDeclarator(String aSerializedNode) {
		VariableDeclarator result = new VariableDeclarator();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}

		List<String> childData = u.cutChildData( aSerializedNode );
		result.setInitializer( getExpressionListFromMapping( childData.get( 0 ) ).get( 0 ) );
		
		return result;
	}

	@Override
	public EnumDeclaration createEnumDeclaration(String aSerializedNode) {
		EnumDeclaration result = new EnumDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setModifiers( kwDispatcher.getAllModsAsSet( childData.get( 0 ) ) );
		
		return result;
	}

	@Override
	public AnnotationDeclaration createAnnotationDeclaration(String aSerializedNode) {
		AnnotationDeclaration result = new AnnotationDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	@Override
	public AnnotationMemberDeclaration createAnnotationMemberDeclaration(String aSerializedNode) {
		AnnotationMemberDeclaration result = new AnnotationMemberDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	@Override
	public WhileStmt createWhileStmt(String aSerializedNode) {
		WhileStmt result = new WhileStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setCondition( getExpressionListFromMapping( childData.get( 0 ) ).get( 0 ));
		
		return result;
	}

	@Override
	public TryStmt createTryStmt(String aSerializedNode) {
		TryStmt result = new TryStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	@Override
	public ThrowStmt createThrowStmt(String aSerializedNode) {
		ThrowStmt result = new ThrowStmt();
			
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	// This may never be used
	@Override
	public ThrowsStmt createThrowsStmt(String aSerializedNode) {
		ThrowsStmt result = null;
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children	
		
		return result;
	}

	@Override
	public SynchronizedStmt createSynchronizedStmt(String aSerializedNode) {
		SynchronizedStmt result = new SynchronizedStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	@Override
	public SwitchStmt createSwitchStmt(String aSerializedNode) {
		SwitchStmt result = new SwitchStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setSelector( getExpressionListFromMapping( childData.get( 0 )).get( 0 ) );
		
		return result;
	}

	@Override
	public SwitchEntryStmt createSwitchEntryStmt(String aSerializedNode) {
		SwitchEntryStmt result = new SwitchEntryStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setLabel( getExpressionListFromMapping( childData.get( 0 ) ).get( 0 ) );
		
		return result;
	}

	@Override
	public ReturnStmt createReturnStmt(String aSerializedNode) {
		ReturnStmt result = new ReturnStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	@Override
	public LabeledStmt createLabeledStmt(String aSerializedNode) {
		LabeledStmt result = new LabeledStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	@Override
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
	@Override
	public ElseStmt createElseStmt(String aSerializedNode) {
		ElseStmt result = new ElseStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	@Override
	public ForStmt createForStmt(String aSerializedNode) {
		ForStmt result = new ForStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setInitialization( getExpressionListFromMapping( childData.get( 0 ) ) );
		result.setCompare( getExpressionListFromMapping( childData.get( 1 ) ).get( 0 ) );
		result.setUpdate( getExpressionListFromMapping( childData.get( 2 ) ) );
		
		return result;
	}

	@Override
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
	@Override
	public ExpressionStmt createExpressionStmt(String aSerializedNode) {
		ExpressionStmt result = new ExpressionStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type has never any children
		
		return result;
	}

	@Override
	public ExplicitConstructorInvocationStmt createExplicitConstructorInvocationStmt(String aSerializedNode) {
		ExplicitConstructorInvocationStmt result = new ExplicitConstructorInvocationStmt();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setThis( childData.get( 0 ).equals( "this" )); // otherwise this value is "super"
		result.setArguments( getExpressionListFromMapping( childData.get( 1 ) ) );
		result.setTypeArguments( getTypeListFromMapping( childData.get( 2 ) ) );
		
		return result;
	}

	@Override
	public DoStmt createDoStmt(String aSerializedNode) {
		DoStmt result = new DoStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setCondition( getExpressionListFromMapping( childData.get( 0 )).get( 0 ) );
		
		return result;
	}

	@Override
	public ContinueStmt createContinueStmt(String aSerializedNode) {
		ContinueStmt result = new ContinueStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this type never has any children
		
		return result;
	}

	@Override
	public CatchClause createCatchClause(String aSerializedNode) {
		CatchClause result = new CatchClause();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// we could parse the parameter and the block here but we do not serialize any of the two
		// TODO may add one or both to serialization and deserialization
		
		return result;
	}

	@Override
	public BlockStmt createBlockStmt(String aSerializedNode) {
		BlockStmt result = new BlockStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// again this is only a closing block statement
		
		
		return result;
	}

	@Override
	public VariableDeclarationExpr createVariableDeclarationExpr(String aSerializedNode) {
		VariableDeclarationExpr result = new VariableDeclarationExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setModifiers( kwDispatcher.getAllModsAsSet( childData.get( 0 ) ));
//		result.setType( getTypeListFromMapping( childData.get( 1 ) ).get( 0 ) );
		result.setVariables( getVariableDeclaratorListFromMapping( childData.get( 2 ) ) );
		
		return result;
	}

	@Override
	public TypeExpr createTypeExpr(String aSerializedNode) {
		TypeExpr result = new TypeExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setType( getTypeListFromMapping( childData.get( 0 )).get( 0 ) );
		
		return result;
	}

	@Override
	public SuperExpr createSuperExpr(String aSerializedNode) {
		SuperExpr result = new SuperExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// no children here
		
		return result;
	}

	@Override
	public NullLiteralExpr createNullLiteralExpr(String aSerializedNode) {
		NullLiteralExpr result = new NullLiteralExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// null!
		
		return result;
	}

	@Override
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
	@Override
	public BodyStmt createBodyStmt(String aSerializedNode) {
		BodyStmt result = null;
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// nothing here
		
		return result;
	}

	@Override
	public LambdaExpr createLambdaExpr(String aSerializedNode) {
		LambdaExpr result = new LambdaExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setEnclosingParameters( childData.get( 0 ).equals( "true" ));
		result.setParameters( getParameterFromMapping( childData.get( 1 )));
		result.setBody( (Statement) deserializeNode( childData.get( 2 ) ) );
		
		return result;
	}

	@Override
	public InstanceOfExpr createInstanceOfExpr(String aSerializedNode) {
		InstanceOfExpr result = new InstanceOfExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setExpression( (Expression) deserializeNode( childData.get( 0 )));
		result.setType( (ReferenceType<?>) deserializeNode( childData.get( 1 )));
		
		return result;
	}

	@Override
	public FieldAccessExpr createFieldAccessExpr(String aSerializedNode) {
		FieldAccessExpr result = new FieldAccessExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setName( (SimpleName) deserializeNode( childData.get( 0 ))); 
		result.setTypeArguments(getTypeListFromMapping( childData.get( 1 )) );
		
		return result;
	}

	@Override
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

	@Override
	public ClassExpr createClassExpr(String aSerializedNode) {
		ClassExpr result = new ClassExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setType( (Type) deserializeNode( childData.get( 0 ) ));
		
		return result;
	}

	@Override
	public CastExpr createCastExpr(String aSerializedNode) {
		CastExpr result = new CastExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setType( (Type) deserializeNode( childData.get( 0 ) ));
		result.setExpression( (Expression) deserializeNode( childData.get( 1 ) ));
			
		return result;
	}

	@Override
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

	@Override
	public ArrayInitializerExpr createArrayInitializerExpr(String aSerializedNode) {
		ArrayInitializerExpr result = new ArrayInitializerExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setValues( getExpressionListFromMapping(childData.get( 0 ) ) );
		
		return result;
	}

	@Override
	public ArrayCreationExpr createArrayCreationExpr(String aSerializedNode) {
		ArrayCreationExpr result = new ArrayCreationExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setElementType( (Type) deserializeNode( childData.get( 0 )));
		result.setLevels( getArrayCreationLevelsListFromMapping( childData.get( 1 )));
//		result.setArrayCount( Integer.parseInt( childData.get( 2 )));
		result.setInitializer( (ArrayInitializerExpr) deserializeNode( childData.get( 3 )));
		
		return result;
	}

	@Override
	public ArrayAccessExpr createArrayAccessExpr(String aSerializedNode) {
		ArrayAccessExpr result = new ArrayAccessExpr();
			
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setIndex( (Expression) deserializeNode( childData.get( 0 )));
		
		return result;
	}

	@Override
	public PackageDeclaration createPackageDeclaration(String aSerializedNode) {
		PackageDeclaration result = new PackageDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		// TODO this could be wrong because package name and the actual name are kind of different
		// but the api has no set package name...
		result.setName( (Name) deserializeNode( childData.get( 0 )));;
		
		return result;
	}

	// this may never be used
	@Override
	public ImportDeclaration createImportDeclaration(String aSerializedNode) {
		ImportDeclaration result = null;
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
//		List<String> childData = u.cutChildData( aSerializedNode );
//		result.setName( (NameExpr) deserializeNode( childData.get( 0 )));;
		
		
		return result;
	}

	@Override
	public FieldDeclaration createFieldDeclaration(String aSerializedNode) {
		FieldDeclaration result = new FieldDeclaration();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setModifiers( kwDispatcher.getAllModsAsSet( childData.get( 0 )));
//		result.setElementType( (Type) deserializeNode( childData.get( 1 )));
		result.setVariables(getVariableDeclaratorListFromMapping( childData.get( 2 ) ));
		
		return result;
	}

	@Override
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


//	/**
//	 * Better use the class or interface specific methods
//	 */
//	@Deprecated
//	public ClassOrInterfaceDeclaration createClassOrInterfaceDeclaration(String aSerializedNode) {
//		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration();
//		
//		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
//			return result;
//		}
//		
//		List<String> childData = u.cutChildData( aSerializedNode );
//		result.setTypeParameters(getTypeParameterFromMapping(childData.get( 0 )));
//		result.setExtends(getClassOrInterfaceTypeListFromMapping( childData.get( 1) ));
//		result.setImplements(getClassOrInterfaceTypeListFromMapping( childData.get( 2 )));
////		result.setInterface( false ); // we cant know because the keyword for this method could be from either one
//		
//		return result;
//	}

	@Override
	public ClassOrInterfaceDeclaration createClassDeclaration(String aSerializedNode) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setTypeParameters(getTypeParameterFromMapping(childData.get( 0 )));
		result.setExtendedTypes(getClassOrInterfaceTypeListFromMapping( childData.get( 1) ));
		result.setImplementedTypes(getClassOrInterfaceTypeListFromMapping( childData.get( 2 )));
		result.setInterface( false );
		
		return result;
	}

	@Override
	public ClassOrInterfaceDeclaration createInterfaceDeclaration(String aSerializedNode) {
		ClassOrInterfaceDeclaration result = new ClassOrInterfaceDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setTypeParameters(getTypeParameterFromMapping(childData.get( 0 )));
		result.setExtendedTypes(getClassOrInterfaceTypeListFromMapping( childData.get( 1) ));
		result.setImplementedTypes(getClassOrInterfaceTypeListFromMapping( childData.get( 2 )));
		result.setInterface( true );
		
		return result;
	}

	@Override
	public MethodDeclaration createMethodDeclaration(String aSerializedNode) {
		MethodDeclaration result = new MethodDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setModifiers( kwDispatcher.getAllModsAsSet( childData.get( 0 )));
		result.setType(getTypeListFromMapping( childData.get( 1 )).get( 0 ));
		result.setParameters(getParameterFromMapping( childData.get( 2 )));
		result.setTypeParameters(getTypeParameterFromMapping( childData.get( 3 )));
		
		return result;
	}

	@Override
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

	@Override
	public UnaryExpr createUnaryExpr(String aSerializedNode) {
		UnaryExpr result = new UnaryExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setOperator( OperatorMapper.getUnaryOperatorFromMapping( childData.get( 0 )) );
		result.setExpression( (Expression) deserializeNode( childData.get( 1 )));		
		
		return result;
	}

	@Override
	public MethodCallExpr createMethodCallExpr(String aSerializedNode) {
		MethodCallExpr result = new MethodCallExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setScope( (Expression) deserializeNode( childData.get( 0 )));
		result.setName(childData.get( 1 ));
		result.setArguments(getExpressionListFromMapping(childData.get( 2 )));
		result.setTypeArguments(getTypeListFromMapping(childData.get( 3 )));		
		
		return result;
	}

	// this is actually the same as the normal method call expression but without the name
	@Override
	public MethodCallExpr createPrivMethodCallExpr(String aSerializedNode) {
		MethodCallExpr result = new MethodCallExpr();
		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setScope( (Expression) deserializeNode( childData.get( 0 ))); // this and the next value are meaningless
		result.setName(childData.get( 1 ));
		result.setArguments(getExpressionListFromMapping(childData.get( 2 )));
		result.setTypeArguments(getTypeListFromMapping(childData.get( 3 )));	
	
		return result;
	}

	@Override
	public NameExpr createNameExpr(String aSerializedNode) {
		NameExpr result = new NameExpr();
			
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// this has not child data and including all names of all objects would be pointless

		return result;
	}

	@Override
	public ConstructorDeclaration createIntegerLiteralExpr(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setModifiers( kwDispatcher.getAllModsAsSet( childData.get( 0 )));
		result.setParameters( getParameterFromMapping( childData.get( 1 )));
		result.setTypeParameters(getTypeParameterFromMapping( childData.get( 2 )));
		
		return result;
	}

	@Override
	public DoubleLiteralExpr createDoubleLiteralExpr(String aSerializedNode) {
		DoubleLiteralExpr result = new DoubleLiteralExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setValue( childData.get( 0 ) );
		
		return result;
	}

	@Override
	public StringLiteralExpr createStringLiteralExpr(String aSerializedNode) {
		StringLiteralExpr result = new StringLiteralExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// there are no data for string literals because they are to different
		
		return result;
	}

	@Override
	public BooleanLiteralExpr createBooleanLiteralExpr(String aSerializedNode) {
		BooleanLiteralExpr result = new BooleanLiteralExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setValue( Boolean.parseBoolean( childData.get( 0 ) ) );
		
		return result;
	}

	@Override
	public CharLiteralExpr createCharLiteralExpr(String aSerializedNode) {
		CharLiteralExpr result = new CharLiteralExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// same argument as with the strings
		
		return result;
	}

	@Override
	public LongLiteralExpr createLongLiteralExpr(String aSerializedNode) {
		LongLiteralExpr result = new LongLiteralExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setValue( childData.get( 0 ) );
		
		return result;
	}

	@Override
	public ThisExpr createThisExpr(String aSerializedNode) {
		ThisExpr result = new ThisExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// its this and nothing more
		
		return result;
	}

	@Override
	public BreakStmt createBreakStmt(String aSerializedNode) {
		BreakStmt result = new BreakStmt();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// guess what? break has nothing more to say than to break free :)
		
		return result;
	}

	@Override
	public ObjectCreationExpr createObjectCreationExpr(String aSerializedNode) {
		ObjectCreationExpr result = new ObjectCreationExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setScope( (Expression) deserializeNode( childData.get( 0 ) ) ); // this is almost always empty
		result.setType( (ClassOrInterfaceType) deserializeNode( childData.get( 1 )));
		result.setTypeArguments(getTypeListFromMapping( childData.get( 2 )));
		result.setArguments(getExpressionListFromMapping( childData.get( 3 )));
		result.setAnonymousClassBody(u.getBodyDeclaratorListFromMapping( childData.get( 4 )));
		
		return result;
	}

	@Override
	public MarkerAnnotationExpr createMarkerAnnotationExpr(String aSerializedNode) {
		MarkerAnnotationExpr result = new MarkerAnnotationExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// just an annotation
		
		return result;
	}

	@Override
	public NormalAnnotationExpr createNormalAnnotationExpr(String aSerializedNode) {
		NormalAnnotationExpr result = new NormalAnnotationExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// just an annotation expression
		
		return result;
	}

	@Override
	public SingleMemberAnnotationExpr createSingleMemberAnnotationExpr(String aSerializedNode) {
		SingleMemberAnnotationExpr result = new SingleMemberAnnotationExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// i think we could have skipped the annotations because we never mutate them anyway
		
		return result;
	}

	@Override
	public Parameter createParameter(String aSerializedNode) {
		Parameter result = new Parameter();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setType( (Type) deserializeNode( childData.get( 0 )));
		result.setModifiers( kwDispatcher.getAllModsAsSet( childData.get( 1 )));		
		
		return result;
	}

	@Override
	public EnclosedExpr createEnclosedExpr(String aSerializedNode) {
		EnclosedExpr result = new EnclosedExpr();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setInner( (Expression) deserializeNode( childData.get( 0 )));		
		
		return result;
	}

	@Override
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

	@Override
	public ConstructorDeclaration createMemberValuePair(String aSerializedNode) {
		ConstructorDeclaration result = new ConstructorDeclaration();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setModifiers( kwDispatcher.getAllModsAsSet( childData.get( 0 )));
		result.setParameters(getParameterFromMapping(childData.get( 1 )));
		result.setTypeParameters(getTypeParameterFromMapping(childData.get( 2 )));		
		
		return result;
	}

	@Override
	public PrimitiveType createPrimitiveType(String aSerializedNode) {
		PrimitiveType result = new PrimitiveType();
			
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setType( TypeMapper.getPrimTypeFromMapping( childData.get( 0 )));
		
		return result;
	}

	@Override
	public UnionType createUnionType(String aSerializedNode) {		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		// for some reason i need the list of reference type for the constructor... kind of inconsistent
		UnionType result = new UnionType(u.getReferenceTypeListFromMapping( childData.get( 0 )));
		
		return result;
	}

	@Override
	public IntersectionType createIntersectionType(String aSerializedNode) {		
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return null;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		IntersectionType result = new IntersectionType( u.getReferenceTypeListFromMapping( childData.get( 0 )));
		
		return result;
	}

	@Override
	public TypeParameter createTypeParameter(String aSerializedNode) {
		TypeParameter result = new TypeParameter();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		List<String> childData = u.cutChildData( aSerializedNode );
		result.setTypeBound(getClassOrInterfaceTypeListFromMapping(childData.get( 0 )));
		
		return result;
	}

	@Override
	public WildcardType createWildcardType(String aSerializedNode) {
		WildcardType result = new WildcardType();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// no child data
		
		return result;
	}
	
	@Override
	public VoidType createVoidType(String aSerializedNode) {
		VoidType result = new VoidType();
		
		if( aSerializedNode == null || aSerializedNode.length() == 0 ) {
			return result;
		}
		
		// ...
		
		return result;
	}

	// this may never be used
	@Override
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
	@Override
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
	public UnknownNode createUnknown(String aSerializedNode) {
		return new UnknownNode();
	}

	@Override
	public ClassOrInterfaceDeclaration createClassOrInterfaceDeclaration(String aSerializedNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UnknownType createUnknownType(String aSerializedNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Name createName(String aSerializedNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleName createSimpleName(String aSerializedNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocalClassDeclarationStmt createLocalClassDeclarationStmt(String aSerializedNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayType createArrayType(String aSerializedNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayCreationLevel createArrayCreationLevel(String aSerializedNode) {
		// TODO Auto-generated method stub
		return null;
	}

}
