package se.de.hu_berlin.informatik.astlmbuilder.parsing;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.Node;

/**
 * Should contain information about available variables, etc.
 * 
 * @author Simon Heiden
 *
 */
public class InformationWrapper {
	
	private List<Node> nodeHistory;
	private List<Class<? extends Node>> classHistory;
	
	public InformationWrapper() {
		this.nodeHistory = new ArrayList<>();
		this.classHistory = new ArrayList<>();
	}
	
	public InformationWrapper(List<Node> nodeHistory, List<Class<? extends Node>> classHistory) {
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
				new ArrayList<>(nodeHistory),
				new ArrayList<>(classHistory));
	}
	
	public void addNodeToHistory(Node node) {
		nodeHistory.add(node);
	}
	
	public List<Node> getNodeHistory() {
		return nodeHistory;
	}
	
	public void addNodeClassToHistory(Class<? extends Node> nodeClass) {
		classHistory.add(nodeClass);
	}
	
	public List<Class<? extends Node>> getNodeClassHistory() {
		return classHistory;
	}
	
}
