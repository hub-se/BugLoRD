package se.de.hu_berlin.informatik.astlmbuilder.parser;

import org.junit.Test;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Name;

import junit.framework.TestCase;

public class TokenParserTests extends TestCase {

	ITokenParser t_parser = new TokenParser();
	
	/**
	 * 
	 */
	@Test
	public void testTokenParserHRKW1() {
		// this is the first token in my language model based on the elastic search repository
		// (changed that due to other token format... change it to whatever you like, though.)
		String firstTokenFromLM = "(NAME,[~],[org],[#0])";
		InformationWrapper info = new InformationWrapper(); // This may be filled with data later on
		
		Node firstNode = t_parser.createNodeFromToken( Node.class, firstTokenFromLM, info);
		
		assertTrue( "hm?", firstNode instanceof Name );
	}
	
}
