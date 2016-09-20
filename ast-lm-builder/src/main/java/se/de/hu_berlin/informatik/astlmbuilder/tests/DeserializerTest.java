package se.de.hu_berlin.informatik.astlmbuilder.tests;

import java.util.List;

import org.junit.Test;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;

import junit.framework.TestCase;
import se.de.hu_berlin.informatik.astlmbuilder.reader.ASTLMAbstractionDeserializer;
import se.de.hu_berlin.informatik.astlmbuilder.reader.ASTLMDeserializer;

public class DeserializerTest extends TestCase {

	private ASTLMDeserializer dSeri = new ASTLMDeserializer();	
	
	public static void main(String[] args) {
		DeserializerTest dt = new DeserializerTest();
		dt.doAction( args );
	}
	
	/**
	 * The non static entry
	 * @param args
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
		String cnstrDec = "($CNSTR_DEC;[PUB],[($PAR;($REF_TYPE;$CI_TYPE,1),[]),($PAR;($PRIM_TYPE;Long),[]),($PAR;($PRIM_TYPE;Long),[])],[])";
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

}
