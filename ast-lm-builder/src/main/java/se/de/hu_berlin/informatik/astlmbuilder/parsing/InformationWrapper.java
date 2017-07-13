package se.de.hu_berlin.informatik.astlmbuilder.parsing;

import java.util.ArrayList;
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
	private List<Node> symbolTable;
	private List<Optional<Node>> nodeHistory;
	private List<Class<? extends Node>> classHistory;
	
	public InformationWrapper() {
		this.nodeHistory = new ArrayList<>();
		this.classHistory = new ArrayList<>();
	}
	
	public InformationWrapper(List<Optional<Node>> nodeHistory, List<Class<? extends Node>> classHistory) {
		this.nodeHistory = nodeHistory;
		this.classHistory = classHistory;
	}

	/**
	 * TODO: actually implement this! 
	 * @return
	 * an independent copy of this InformationWrapper object
	 */
	public InformationWrapper getCopy() {
		return new InformationWrapper(
				getCopyOfNodeHistory(),
				new ArrayList<>(classHistory));
	}
	
	private List<Optional<Node>> getCopyOfNodeHistory() {
		List<Optional<Node>> copy = new ArrayList<Optional<Node>>( nodeHistory.size() );
		
		for( Optional<Node> node : nodeHistory ) {
			// TODO copy the node optional object if needed
			Optional<Node> newNode = Optional.of( node.get() );
			copy.add( newNode );
		}
		
		return copy;
	}
	
	/**
	 * Sets the symbol table storing all variable nodes that could be used later on
	 * @param aST The symbol table
	 */
	public void setSymbolTable( List<Node> aST ) {
		symbolTable = aST;
	}
	
	/**
	 * Returns the symbol table storing all variable nodes that could be used later on
	 * @return The symbol table
	 */
	public List<Node> getSymbolTabl() {
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
	
}
