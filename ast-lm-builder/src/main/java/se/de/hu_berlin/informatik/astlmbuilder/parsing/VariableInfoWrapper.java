package se.de.hu_berlin.informatik.astlmbuilder.parsing;

import com.github.javaparser.ast.Node;

/**
 * A wrapper object for different types of variables found in the history of
 * a node.
 * 
 */
public class VariableInfoWrapper {

	public static final String UNKNOWN_STR_VALUE = VariableScope.UNKNOWN.toString();
	
	public enum VariableScope {
		GLOBAL,
		ARGUMENT,
		LOCAL,
		UNKNOWN
	}
	
	private String type = UNKNOWN_STR_VALUE;
	private String name = UNKNOWN_STR_VALUE;
	private String lastKnownValue = UNKNOWN_STR_VALUE;
	private boolean primitive = false;
	private VariableScope scope = VariableScope.UNKNOWN;
	// can be null if we do not need the original node or lose it for some reason
	private Node originalNode = null;
	
	/**
	 * Constructor for a variable info wrapper object
	 * 
	 * @param aType
	 * The type of the variable
	 * @param aName
	 * The name of the variable declaration
	 * @param aLastKnownValue
	 * The last known value if it could be extracted easily
	 * @param aIsPrimitive
	 * Flag if the variable is a primitive
	 * @param aScope
	 * Global, Argument, Local or unknown
	 * @param aOriginalNode
	 * The complete node in case we want to get some additional data
	 */
	public VariableInfoWrapper( String aType, String aName, String aLastKnownValue, boolean aIsPrimitive, VariableScope aScope, Node aOriginalNode ) {
		type = aType == null ? UNKNOWN_STR_VALUE : aType.trim().toLowerCase();
		name = aName == null ? UNKNOWN_STR_VALUE : aName.trim().toLowerCase();
		lastKnownValue =  aLastKnownValue == null ? UNKNOWN_STR_VALUE : aLastKnownValue; // no trim or lower case needed here
		primitive = aIsPrimitive;
		scope = aScope;
	}
	
	/**
	 * Returns the type of the variable
	 * @return The type of the variable. Integer for example.
	 */
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	
	public String getName() {
		return name;
	}

	
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The last known value if a statement could be found.
	 */
	public String getLastKnownValue() {
		return lastKnownValue;
	}
	
	public void setLastKnownValue(String lastKnownValue) {
		this.lastKnownValue = lastKnownValue;
	}

	/**
	 * @return True if it is a primitive variable. False otherwise.
	 */
	public boolean isPrimitive() {
		return primitive;
	}

	
	public void setPrimitive(boolean primitive) {
		this.primitive = primitive;
	}

	
	public VariableScope getScope() {
		return scope;
	}

	
	public void setScope(VariableScope scope) {
		this.scope = scope;
	}

	
	public Node getOriginalNode() {
		return originalNode;
	}

	
	public void setOriginalNode(Node originalNode) {
		this.originalNode = originalNode;
	}
	
	public String toString() {
		return "VIW(t=" + type + ", n=" + name + ", v=" + lastKnownValue + ", s=" + scope + ", p=" + primitive + ")";
	}
}
