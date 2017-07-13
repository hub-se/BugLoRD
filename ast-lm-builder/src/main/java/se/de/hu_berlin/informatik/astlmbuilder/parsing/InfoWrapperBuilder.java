package se.de.hu_berlin.informatik.astlmbuilder.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.stmt.ForStmt;

/**
 * Class to build the info wrapper objects that store something similar to a
 * symbol table for nodes
 *
 */
public class InfoWrapperBuilder {

	/**
	 * Collects all relevant data like variable names, scopes and types from a
	 * node and its parents and stores them into an information wrapper object.
	 * 
	 * @param aNode
	 * The node of interest
	 * @return A information wrapper containing data
	 */
	public InformationWrapper buildInfoWrapperForNode(Node aNode) {
		List<Optional<Node>> nodeHistory = getNodeHistory(aNode);
		List<Class<? extends Node>> classHistory = getClassHistory(aNode);
		List<Node> symbolTable = getSymbolTable( aNode, nodeHistory );
		
		// TODO implement

		InformationWrapper result = new InformationWrapper(nodeHistory, classHistory);
		result.setSymbolTable( symbolTable );
		return result;
	}

	private List<Optional<Node>> getNodeHistory(Node aNode) {
		
		if( aNode == null ) {
			return null;
		}
		
		List<Optional<Node>> result = new ArrayList<Optional<Node>>();
		addAllParentsToHistory( aNode, result);
		
		return result;
	}
	
	/**
	 * Searches this node and its parents for declarations of variables that could be used
	 * for mutations.
	 * Because this method works on the node history it has to be created first.
	 * 
	 * @param aNode The node of interest
	 * @return an unordered list with variable declarations from the parents
	 */
	private List<Node> getSymbolTable(Node aNode, List<Optional<Node>> aNodeHistory) {
		List<Node> result = new ArrayList<Node>();
		
		if( aNodeHistory == null || aNodeHistory.isEmpty() ) {
			return result;
		}
		
		// check each parent node from the node history
		for( Optional<Node> on : aNodeHistory ) {
			// search for all variable declarations that should be valid
			for( Node child : on.get().getChildNodes() ) {
				// TODO select all children that are relevant
				if( child instanceof FieldDeclaration ) {
					result.add( child );
				}
			}
		}
		
		
		
		// TODO implement
		return result;
	}
	
	/**
	 * Recursive approach because the optionals are weird
	 * @param aNode The node that may has one or multiple parents
	 * @param aList A list of all parents that were found
	 */
	private void addAllParentsToHistory(Node aNode, List<Optional<Node>> aList ) {
		Optional<Node> parentOpt = aNode.getParentNode();
		if( parentOpt.isPresent() ) {	
			addAllParentsToHistory( parentOpt.get(), aList );
			aList.add( parentOpt );
		} 
	}

	private List<Class<? extends Node>> getClassHistory(Node aNode) {
		List<Class<? extends Node>> result = new ArrayList<Class<? extends Node>>();

		// TODO implement

		return result;
	}

}
