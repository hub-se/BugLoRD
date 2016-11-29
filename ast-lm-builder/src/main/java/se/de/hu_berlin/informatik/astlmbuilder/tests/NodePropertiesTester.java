package se.de.hu_berlin.informatik.astlmbuilder.tests;

import se.de.hu_berlin.informatik.astlmbuilder.ASTLMBuilder;

public class NodePropertiesTester {

	/**
	 * The constructor
	 */
	public NodePropertiesTester() {
		
	}
	
	/**
	 * The static entry method
	 * @param args
	 */
	public static void main(String[] args) {
		NodePropertiesTester npt = new NodePropertiesTester();
		npt.doAction(args);
	}
	
	/**
	 * The non static entry method
	 * @param args
	 */
	public void doAction( String[] args ) {
		System.out.println( "Node properties tester started." );
		
		ASTLMBuilder builder = new ASTLMBuilder( args );
		builder.doAction();
		
		System.out.println( "Node properties tester finished." );
	}

}
