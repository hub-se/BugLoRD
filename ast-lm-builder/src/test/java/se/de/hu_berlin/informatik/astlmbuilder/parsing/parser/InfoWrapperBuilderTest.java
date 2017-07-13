package se.de.hu_berlin.informatik.astlmbuilder.parsing.parser;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForStmt;

import junit.framework.TestCase;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InfoWrapperBuilder;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

/**
 * Test class suite for the builder of the information wrapper objects
 */
public class InfoWrapperBuilderTest extends TestCase {

	InfoWrapperBuilder iwb = new InfoWrapperBuilder();
	
	@Test
	public void testBuildInfoWrapperForSimpleMethod() {
		Log.out( this, "Started first test" );
		
		ClassLoader classLoader = getClass().getClassLoader();
		
		File testFile = new File(classLoader.getResource("test_files/TestClassForInfoWrapper.java").getFile());

		if( !testFile.exists() ) {
			Log.err( this, "Could not find the test file at ", testFile.getAbsolutePath() );
			return;
		}

		CompilationUnit cu = null;
		
		try {
			cu = JavaParser.parse( testFile );
			
			// search for a node deep in the AST
			ForStmt forNode = (ForStmt) getSomeInterestingNode( cu );
			
			// TODO extract the history and parents and data
			InformationWrapper iw = iwb.buildInfoWrapperForNode( forNode );
			
			assertNotNull( iw );
			assertNotNull( iw.getNodeHistory() );
			
		} catch (FileNotFoundException e) {
			Log.err(this, e);
		}

		
		Log.out( this, "Finished first test" );
	}
	
	/**
	 * Works only for the test class in the resource directory
	 * Currently searches for the for statement in the calc sum method
	 * 
	 * @param aCU The compilation unit
	 * @return The loop node in the calcSumFromTo method
	 */
	private Node getSomeInterestingNode( CompilationUnit aCU ) {
		
		// get the class declaration
		ClassOrInterfaceDeclaration cdec = getNodeFromChildren( aCU, ClassOrInterfaceDeclaration.class );
		if ( cdec == null ) {
			return null;
		}
		
		MethodDeclaration calcMD = getNodeFromChildren( cdec, MethodDeclaration.class, "calcSumFromTo");
		if( calcMD == null ) {
			return null;
		}
		
		// get the block statement
		BlockStmt block = getNodeFromChildren( calcMD, BlockStmt.class );
		if( block == null ) {
			return null;
		}
		
		// get the for stmt
		ForStmt forStmt = getNodeFromChildren( block, ForStmt.class );	
		return forStmt;
	}
	
	/**
	 * The variant with a name currently only works for method declarations because of the getNameAsString method
	 * @param aParentNode The node that has children
	 * @param aTypeOfNode The type of node that should be returned
	 * @param aName The name of the node that should be returned
	 * @return The node with the given type and name or null
	 */
	private <T extends Node> T getNodeFromChildren( Node aParentNode, Class<T> aTypeOfNode, String aName ) {
		
		if ( aParentNode == null || aParentNode.getChildNodes() == null ) {
			return null;
		}
		
		for( Node n : aParentNode.getChildNodes() ) {
			if( aTypeOfNode.isInstance( n ) ){
				// only works for method decs currently
				// could be adjusted if there is a pattern to node with names
				if( n instanceof MethodDeclaration ) {
					if ( ((MethodDeclaration) n).getNameAsString().equalsIgnoreCase( aName ) ) {
						return (T) n;
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * Searches for a node of the given type in the list of children and returns it
	 * @param aParentNode The node with children
	 * @param aTypeOfNode The type of node that should be returned
	 * @return The first node with the given type
	 */
	private <T extends Node> T getNodeFromChildren( Node aParentNode, Class<T> aTypeOfNode ) {
		
		if ( aParentNode == null || aParentNode.getChildNodes() == null ) {
			return null;
		}
		
		for( Node n : aParentNode.getChildNodes() ) {
			if( aTypeOfNode.isInstance( n ) ){
				return (T) n;
			}
		}
		
		return null;
	}
}
