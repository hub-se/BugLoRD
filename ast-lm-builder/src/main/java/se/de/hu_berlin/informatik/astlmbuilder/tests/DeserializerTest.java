package se.de.hu_berlin.informatik.astlmbuilder.tests;

import java.util.List;

import org.junit.Test;

import com.github.javaparser.ast.Node;

import junit.framework.TestCase;
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
		
		testConstDec();
		
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

}
