package se.de.hu_berlin.informatik.astlmbuilder.tests;

import java.util.List;

import org.junit.Test;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.stmt.WhileStmt;

import junit.framework.TestCase;
import se.de.hu_berlin.informatik.astlmbuilder.reader.ASTLMAbstractionDeserializer;
import se.de.hu_berlin.informatik.astlmbuilder.reader.ASTLMDeserializer;

/**
 * TODO make this right
 * maybe one test with all tokens from the big language model
 * the few tests are pretty shitty and partly outdated because of changes of the serialization
 */
public class DeserializerTest extends TestCase {

	private ASTLMDeserializer dSeri = new ASTLMDeserializer();	
	
	public static void main(String[] args) {
		DeserializerTest dt = new DeserializerTest();
		dt.doAction( args );
	}
	
	/**
	 * The non static entry
	 * @param args
	 * command line arguments
	 */
	public void doAction( String[] args ) {
		System.out.println( "Deserializer Test started" );
		
//		testConstDec();
		testConstDecAbstraction();
		
		System.out.println( "Deserializer Test finished" );
	}
	
	@Test
	private void testConstDec() {
		String cnstrDecSeri = "(%$CNSTR_DEC[(%$PAR[(%$VAR_DEC_ID),(%$REF_TYPE)]),(%$PAR[(%$VAR_DEC_ID),(%$REF_TYPE)]),(%$PAR[(%$VAR_DEC_ID),(%$REF_TYPE)]),(%$PAR[(%$VAR_DEC_ID),(%$PRIM_TYPE)]),(%$PAR[(%$VAR_DEC_ID),(%$PRIM_TYPE)])])";
		
		Node cnstrDecNode = dSeri.deserializeNode( cnstrDecSeri );
		
		if ( cnstrDecNode == null ) {
			System.out.println( "Creating root node failed." );
			return;
		}
		
		List<Node> children = cnstrDecNode.getChildrenNodes();
		
		if ( children == null ) {
			System.out.println( "Creating child nodes failed." );
			return;
		}
		
		if ( children.size() != 5 ) { // five parameter
			System.out.println( "Creating all child nodes failed." );
			return;
		}
		
		System.out.println( "Constructor deserialization successfull" );
	}
	
	@Test
	private void testConstDecAbstraction() {
		// one of the longest examples from my test language model
		String cnstrDec = "($CNSTR_DEC;[PUB],[($PAR;[($REF_TYPE;[($CI_TYPE)])],[]),($PAR;[($REF_TYPE;[($CI_TYPE)])],[]),($PAR;[($REF_TYPE;[($CI_TYPE)])],[]),($PAR;[($PRIM_TYPE;[Boolean])],[]),($PAR;[($REF_TYPE;[($CI_TYPE)])],[]),($PAR;[($REF_TYPE;[($CI_TYPE)])],[])],[])";
		ASTLMAbstractionDeserializer absDesi = new ASTLMAbstractionDeserializer();
		
		Node result = absDesi.deserializeNode( cnstrDec );
		
		if( !(result instanceof ConstructorDeclaration) ) {
			System.out.println( "The node had the wrong type..." );
		}
		
		ConstructorDeclaration cd = (ConstructorDeclaration) result;
		if( cd.getModifiers() == 0 ) {
			System.out.println( "The constructor was not public..." );
		}
		
		if( cd.getParameters() == null ) {
			System.out.println( "The constructor had no parameter..." );
		}
		
		if( cd.getTypeParameters() != null && cd.getTypeParameters().size() > 0 ) {
			System.out.println( "Where are those type parameters are coming from?");
		}
	}
	
	private void testEnumDecAbstraction() {
		// TODO find a good example that is not from elastic search
		String enumDecSeri = "($ENUM_DEC;[])";
		
		ASTLMAbstractionDeserializer absDesi = new ASTLMAbstractionDeserializer();
		
		Node result = absDesi.deserializeNode( enumDecSeri );
		
		if( !(result instanceof EnumDeclaration) ) {
			System.out.println( "The node had the wrong type..." );
		}
	}
	
	private void testWhileStatement() {
		String whileSeri = "($WHILE;($UNARY_EXPR;not,($MT_CALL;awaitBusy,[$NAME_EXPR,$NAME_EXPR,$NAME_EXPR],[])))";
		
		ASTLMAbstractionDeserializer absDesi = new ASTLMAbstractionDeserializer();
		
		Node result = absDesi.deserializeNode( whileSeri );
		
		if( !(result instanceof WhileStmt) ) {
			System.out.println( "The node had the wrong type..." );
		}
		
		WhileStmt whileNode = (WhileStmt) result;
		
		List<Node> children = whileNode.getChildrenNodes();
		
		if ( children == null || children.isEmpty() ) {
			System.out.println( "The node had no children..." );
		}
	}
	
	// something for do
	// ($DO;($BIN_EXPR;($FIELD_ACC;$NAME_EXPR,[]),greater,($INT_LIT;0)))
	
	// something for the explicit constructor
	// ($EXPL_CONSTR;this,[($NEW_OBJ;($CI_TYPE;[InetSocketAddress],<>),[],[$NAME_EXPR,$NAME_EXPR],[])],[])

}
