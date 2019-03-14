package se.de.hu_berlin.informatik.astlmbuilder.parsing;

import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.Node;

/**
 * Should contain information about available variables, etc.
 * 
 * @author Simon Heiden
 *
 */
public class InformationWrapper {
	
	// storing all the variables that may be of use for later
	private SymbolTable symbolTable;	
	private final List<Optional<Node>> nodeHistory;
	private final List<Class<? extends Node>> classHistory;
	private Node node;
	
	/**
	 * I just removed this to see where it is called
	 */
//	public InformationWrapper() {
//		this.nodeHistory = new ArrayList<>();
//		this.classHistory = new ArrayList<>();
//	}
	
	public InformationWrapper(List<Optional<Node>> nodeHistory, List<Class<? extends Node>> classHistory, SymbolTable aSymbolTable ) {
		this.nodeHistory = nodeHistory;
		this.classHistory = classHistory;
		this.symbolTable = aSymbolTable;
	}

	/**
	 * Creates a copy of this information wrapper object.
	 * Currently simply a new info wrapper object is build for the original node.
	 * @return
	 * an independent copy of this InformationWrapper object
	 */
	public InformationWrapper getCopy() {
		return InfoWrapperBuilder.buildInfoWrapperForNode( node );
	}
	
	/**
	 * Sets the symbol table storing all variable nodes that could be used later on
	 * @param aST The symbol table
	 */
	public void setSymbolTable( SymbolTable aST ) {
		symbolTable = aST;
	}
	
	/**
	 * Returns the symbol table storing all variable nodes that could be used later on
	 * @return The symbol table
	 */
	public SymbolTable getSymbolTable() {
		return symbolTable;
	}
	
	public void addNodeToHistory(Optional<Node> node) {
		nodeHistory.add(node);
	}
	
	public List<Optional<Node>> getNodeHistory() {
		return nodeHistory;
	}
	
	public void addNodeClassToHistory(Class<? extends Node> nodeClass) {
		classHistory.add(nodeClass);
	}
	
	public List<Class<? extends Node>> getNodeClassHistory() {
		return classHistory;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
	
}
